// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.textSelectionHelper;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.Layout;
import android.text.Spannable;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.BackgroundColorSpan;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.TextView;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.corekit.im2.IMKitClient;

/** TextView 选择帮助类 */
public class SelectableTextHelper {
  //选择效果的TextView
  private TextView mTextView;

  //mTextView 的layout
  private Layout mLayout;

  //mTextView 的Spannable
  private Spannable mSpannable;
  //选中信息
  private final SelectionInfo mSelectionInfo = new SelectionInfo();

  //选中的背景色
  private BackgroundColorSpan mSpan;
  //游标颜色
  private int mCursorHandleColor = R.color.color_bcccfa;
  //选择的文本颜色
  private int mSelectedColor = R.color.color_bcccfa;
  //开始的游标
  private CursorHandle mStartHandle;
  //结束的游标
  private CursorHandle mEndHandle;
  //是否show
  private boolean isShowing = true;

  //选中的数据回调
  private SelectableOnChangeListener onChangeListener;

  //消息位置
  private int mPosition;

  //消息
  private ChatMessageBean mMessage;

  private static SelectableTextHelper instance;

  private SelectableTextHelper() {}

  /**
   * 获取单例，非线程安全，请在主线程调用
   *
   * @return SelectableTextHelper 实例
   */
  public static SelectableTextHelper getInstance() {
    if (instance == null) {
      instance = new SelectableTextHelper();
    }
    return instance;
  }

  /**
   * 获取选中的文案
   *
   * @return 选中的文案
   */
  public String getSelectedText() {
    return mSelectionInfo.mSelectionContent;
  }

  /**
   * 获取选中的消息
   *
   * @return 选中的消息
   */
  public ChatMessageBean getMessage() {
    return mMessage;
  }

  /**
   * 设置选中的文案回调
   *
   * @param onChangeListener 选中的文案回调
   */
  public void setSelectableOnChangeListener(SelectableOnChangeListener onChangeListener) {
    this.onChangeListener = onChangeListener;
  }

  /**
   * 展示选择框
   *
   * @param textView TextView
   * @param layout Layout
   */
  public void showSelectView(TextView textView, Layout layout, int pos, ChatMessageBean msg) {
    if (textView == null || layout == null) {
      throw new SelectFrameLayoutException("textView or layout is null");
    }

    hideSelectView();
    resetSelectionInfo();

    mTextView = textView;
    mLayout = layout;
    mTextView.setText(mTextView.getText(), TextView.BufferType.SPANNABLE);
    mPosition = pos;
    mMessage = msg;

    isShowing = true;
    if (mStartHandle == null) mStartHandle = new CursorHandle(true);
    if (mEndHandle == null) mEndHandle = new CursorHandle(false);

    //全选
    int startOffset = 0;
    int endOffset = mTextView.length();
    if (mTextView.getText() instanceof Spannable) {
      mSpannable = (Spannable) mTextView.getText();
    }
    if (mSpannable == null || startOffset >= mTextView.getText().length()) {
      return;
    }
    selectText(startOffset, endOffset, false);
    showCursorHandle(mStartHandle);
    showCursorHandle(mEndHandle);
  }

  /**
   * 显示游标
   *
   * @param cursorHandle 游标
   */
  private void showCursorHandle(CursorHandle cursorHandle) {
    int offset =
        cursorHandle.isLeft ? mSelectionInfo.getStart(mTextView) : mSelectionInfo.getEnd(mTextView);
    cursorHandle.show(
        (int) mLayout.getPrimaryHorizontal(offset),
        mLayout.getLineBottom(mLayout.getLineForOffset(offset)));
  }

  /**
   * 获取选择的View
   *
   * @return
   */
  public View getSelectView() {
    return mTextView;
  }

  /** 恢复选择框 */
  public void resumeSelection() {
    if (mStartHandle != null
        && mEndHandle != null
        && !TextUtils.isEmpty(mSelectionInfo.mSelectionContent)
        && mLayout != null) {
      showCursorHandle(mStartHandle);
      showCursorHandle(mEndHandle);
      isShowing = true;
    }
  }

  /** 隐藏选择操作 */
  public void dismiss() {
    resetSelectionInfo();
    hideSelectView();
    mLayout = null;
    mTextView = null;
    mMessage = null;
  }

  /** 重置选中的文案 */
  public void resetSelectionInfo() {
    mSelectionInfo.mSelectionContent = null;
    if (mSpannable != null && mSpan != null) {
      mSpannable.removeSpan(mSpan);
      mSpan = null;
    }
  }

  /** 隐藏选择游标 */
  public void hideSelectView() {
    isShowing = false;

    if (mStartHandle != null) {
      mStartHandle.dismiss();
    }
    if (mEndHandle != null) {
      mEndHandle.dismiss();
    }
  }

  /** 选择文本 startPos:起始索引 endPos：尾部索引 */
  private void selectText(int startPos, int endPos, boolean callListener) {
    if (startPos != -1) {
      mSelectionInfo.setStart(startPos);
    } else {
      startPos = mSelectionInfo.getStart(mTextView);
    }
    if (endPos != -1) {
      mSelectionInfo.setEnd(endPos);
    } else {
      endPos = mSelectionInfo.getEnd(mTextView);
    }
    if (mSelectionInfo.getStart(mTextView) > mSelectionInfo.getEnd(mTextView)) {
      int temp = mSelectionInfo.getStart(mTextView);
      mSelectionInfo.setStart(mSelectionInfo.getEnd(mTextView));
      mSelectionInfo.setEnd(temp);
    }

    if (mSpannable != null) {
      if (mSpan == null) {
        mSpan =
            new BackgroundColorSpan(
                IMKitClient.getApplicationContext().getResources().getColor(mSelectedColor));
      }

      mSelectionInfo.mSelectionContent =
          mSpannable
              .subSequence(mSelectionInfo.getStart(mSpannable), mSelectionInfo.getEnd(mSpannable))
              .toString();

      // 调用系统方法设置选中文本的状态
      mSpannable.setSpan(
          mSpan,
          mSelectionInfo.getStart(mTextView),
          mSelectionInfo.getEnd(mTextView),
          Spanned.SPAN_INCLUSIVE_EXCLUSIVE);

      if (callListener && onChangeListener != null) {
        onChangeListener.onChange(
            mTextView,
            mPosition,
            mMessage,
            mSelectionInfo.mSelectionContent,
            startPos == 0 && endPos == mTextView.getText().length());
      }
    }
  }

  public int getTextViewX() {
    int[] location = new int[2];
    mTextView.getLocationOnScreen(location);
    return location[0];
  }

  public int getTextViewY() {
    int[] location = new int[2];
    mTextView.getLocationOnScreen(location);
    return location[1];
  }

  /*
   * 游标类
   */
  class CursorHandle extends View {

    private final int mCursorHandleSize = 48;
    private PopupWindow mPopupWindow;
    private Paint mPaint;

    private int mCircleRadius = mCursorHandleSize / 2;
    private int mWidth = mCircleRadius * 2;
    private int mHeight = mCircleRadius * 2;
    private int mPadding = 25;
    private boolean isLeft;

    public CursorHandle(boolean isLeft) {
      super(mTextView.getContext());
      this.isLeft = isLeft;
      mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
      mPaint.setColor(
          IMKitClient.getApplicationContext().getResources().getColor(mCursorHandleColor));

      mPopupWindow = new PopupWindow(this);
      mPopupWindow.setClippingEnabled(false);
      mPopupWindow.setWidth(mWidth + mPadding * 2);
      mPopupWindow.setHeight(mHeight + mPadding / 2);

      invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
      canvas.drawCircle(mCircleRadius + mPadding, mCircleRadius, mCircleRadius, mPaint);
      if (isLeft) {
        canvas.drawRect(
            mCircleRadius + mPadding, 0, mCircleRadius * 2 + mPadding, mCircleRadius, mPaint);
      } else {
        canvas.drawRect(mPadding, 0, mCircleRadius + mPadding, mCircleRadius, mPaint);
      }
    }

    private int mAdjustX;
    private int mAdjustY;

    private int mBeforeDragStart;
    private int mBeforeDragEnd;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
      switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
          mBeforeDragStart = mSelectionInfo.getStart(mTextView);
          mBeforeDragEnd = mSelectionInfo.getEnd(mTextView);
          mAdjustX = (int) event.getX();
          mAdjustY = (int) event.getY();
          break;
        case MotionEvent.ACTION_UP:
        case MotionEvent.ACTION_CANCEL:
          break;
        case MotionEvent.ACTION_MOVE:
          int rawX = (int) event.getRawX();
          int rawY = (int) event.getRawY();
          update(rawX + mAdjustX - mWidth - getTextViewX(), rawY + mAdjustY - mHeight);
          break;
      }
      return true;
    }

    private void changeDirection() {
      isLeft = !isLeft;
      invalidate();
    }

    public void dismiss() {
      mPopupWindow.dismiss();
    }

    private int[] mTempCoors = new int[2];

    public void update(int x, int y) {
      mTextView.getLocationInWindow(mTempCoors);
      int oldOffset;
      if (isLeft) {
        oldOffset = mSelectionInfo.getStart(mTextView);
      } else {
        oldOffset = mSelectionInfo.getEnd(mTextView);
      }

      y -= mTempCoors[1];

      int offset = TextLayoutUtils.getHysteresisOffset(mTextView, x, y, oldOffset);

      if (offset != oldOffset) {
        resetSelectionInfo();
        if (isLeft) {
          if (offset > mBeforeDragEnd) {
            CursorHandle handle = getCursorHandle(false);
            changeDirection();
            handle.changeDirection();
            mBeforeDragStart = mBeforeDragEnd;
            selectText(mBeforeDragEnd, offset, true);
            handle.updateCursorHandle();
          } else {
            selectText(offset, -1, true);
          }
          updateCursorHandle();
        } else {
          if (offset < mBeforeDragStart) {
            CursorHandle handle = getCursorHandle(true);
            handle.changeDirection();
            changeDirection();
            mBeforeDragEnd = mBeforeDragStart;
            selectText(offset, mBeforeDragStart, true);
            handle.updateCursorHandle();
          } else {
            selectText(mBeforeDragStart, offset, true);
          }
          updateCursorHandle();
        }
      }
    }

    private void updateCursorHandle() {
      mTextView.getLocationInWindow(mTempCoors);
      if (isLeft) {
        mPopupWindow.update(
            (int) mLayout.getPrimaryHorizontal(mSelectionInfo.getStart(mTextView))
                - mWidth
                + getExtraX(),
            mLayout.getLineBottom(mLayout.getLineForOffset(mSelectionInfo.getStart(mTextView)))
                + getExtraY(),
            -1,
            -1);
      } else {
        mPopupWindow.update(
            (int) mLayout.getPrimaryHorizontal(mSelectionInfo.getEnd(mTextView)) + getExtraX(),
            mLayout.getLineBottom(mLayout.getLineForOffset(mSelectionInfo.getEnd(mTextView)))
                + getExtraY(),
            -1,
            -1);
      }
    }

    public void show(int x, int y) {
      mTextView.getLocationInWindow(mTempCoors);
      int offset = isLeft ? mWidth : 0;
      if (mTextView != null && mTextView.getWindowToken() != null) {
        mPopupWindow.showAtLocation(
            mTextView, Gravity.NO_GRAVITY, x - offset + getExtraX(), y + getExtraY());
      } else {
        dismiss();
      }
    }

    public int getExtraX() {
      return mTempCoors[0] - mPadding + mTextView.getPaddingLeft();
    }

    public int getExtraY() {
      return mTempCoors[1] + mTextView.getPaddingTop();
    }
  }

  private CursorHandle getCursorHandle(boolean isLeft) {
    if (mStartHandle.isLeft == isLeft) {
      return mStartHandle;
    } else {
      return mEndHandle;
    }
  }
}
