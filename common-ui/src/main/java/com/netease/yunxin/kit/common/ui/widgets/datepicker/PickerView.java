// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.common.ui.widgets.datepicker;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import androidx.core.content.ContextCompat;
import com.netease.yunxin.kit.common.ui.R;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/** Data Time picker */
public class PickerView extends View {

  private Context mContext;

  private Paint mPaint;
  private int mLightColor, mDarkColor;
  private float mHalfWidth, mHalfHeight, mQuarterHeight;
  private float mMinTextSize, mTextSizeRange;
  private float mTextSpacing, mHalfTextSpacing;

  private float mScrollDistance;
  private float mLastTouchY;
  private List<String> mDataList = new ArrayList<>();
  private int mSelectedIndex;
  private boolean mCanScroll = true;
  private boolean mCanScrollLoop = true;
  private OnSelectListener mOnSelectListener;
  private ObjectAnimator mScrollAnim;
  private boolean mCanShowAnim = true;

  private Timer mTimer = new Timer();
  private TimerTask mTimerTask;
  private Handler mHandler = new ScrollHandler(this);

  /** auto scroll */
  private static final float AUTO_SCROLL_SPEED = 10;

  /** alpha */
  private static final int TEXT_ALPHA_MIN = 120;

  private static final int TEXT_ALPHA_RANGE = 135;

  /** select listener */
  public interface OnSelectListener {
    void onSelect(View view, String selected);
  }

  private static class ScrollTimerTask extends TimerTask {
    private WeakReference<Handler> mWeakHandler;

    private ScrollTimerTask(Handler handler) {
      mWeakHandler = new WeakReference<>(handler);
    }

    @Override
    public void run() {
      Handler handler = mWeakHandler.get();
      if (handler == null) return;

      handler.sendEmptyMessage(0);
    }
  }

  private static class ScrollHandler extends Handler {
    private WeakReference<PickerView> mWeakView;

    private ScrollHandler(PickerView view) {
      mWeakView = new WeakReference<>(view);
    }

    @Override
    public void handleMessage(Message msg) {
      PickerView view = mWeakView.get();
      if (view == null) return;

      view.keepScrolling();
    }
  }

  public PickerView(Context context, AttributeSet attrs) {
    super(context, attrs);

    mContext = context;
    initPaint();
  }

  private void initPaint() {
    mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    mPaint.setStyle(Style.FILL);
    mPaint.setTextAlign(Align.CENTER);
    mLightColor = ContextCompat.getColor(mContext, R.color.color_537ff4);
    mDarkColor = ContextCompat.getColor(mContext, R.color.color_333333);
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    mHalfWidth = getMeasuredWidth() / 2f;
    int height = getMeasuredHeight();
    mHalfHeight = height / 2f;
    mQuarterHeight = height / 4f;
    float maxTextSize = height / 7f;
    mMinTextSize = maxTextSize / 2.2f;
    mTextSizeRange = maxTextSize - mMinTextSize;
    mTextSpacing = mMinTextSize * 2.8f;
    mHalfTextSpacing = mTextSpacing / 2f;
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);

    if (mSelectedIndex >= mDataList.size()) return;

    drawText(canvas, mLightColor, mScrollDistance, mDataList.get(mSelectedIndex));

    for (int i = 1; i <= mSelectedIndex; i++) {
      drawText(
          canvas,
          mDarkColor,
          mScrollDistance - i * mTextSpacing,
          mDataList.get(mSelectedIndex - i));
    }

    int size = mDataList.size() - mSelectedIndex;
    for (int i = 1; i < size; i++) {
      drawText(
          canvas,
          mDarkColor,
          mScrollDistance + i * mTextSpacing,
          mDataList.get(mSelectedIndex + i));
    }
  }

  private void drawText(Canvas canvas, int textColor, float offsetY, String text) {
    if (TextUtils.isEmpty(text)) return;

    float scale = 1 - (float) Math.pow(offsetY / mQuarterHeight, 2);
    scale = scale < 0 ? 0 : scale;
    mPaint.setTextSize(mMinTextSize + mTextSizeRange * scale);
    mPaint.setColor(textColor);
    mPaint.setAlpha(TEXT_ALPHA_MIN + (int) (TEXT_ALPHA_RANGE * scale));

    // text 居中绘制，mHalfHeight + offsetY 是 text 的中心坐标
    Paint.FontMetrics fm = mPaint.getFontMetrics();
    float baseline = mHalfHeight + offsetY - (fm.top + fm.bottom) / 2f;
    canvas.drawText(text, mHalfWidth, baseline, mPaint);
  }

  @Override
  public boolean dispatchTouchEvent(MotionEvent event) {
    return mCanScroll && super.dispatchTouchEvent(event);
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    switch (event.getActionMasked()) {
      case MotionEvent.ACTION_DOWN:
        cancelTimerTask();
        mLastTouchY = event.getY();
        break;

      case MotionEvent.ACTION_MOVE:
        float offsetY = event.getY();
        mScrollDistance += offsetY - mLastTouchY;
        if (mScrollDistance > mHalfTextSpacing) {
          if (!mCanScrollLoop) {
            if (mSelectedIndex == 0) {
              mLastTouchY = offsetY;
              invalidate();
              break;
            } else {
              mSelectedIndex--;
            }
          } else {
            // 往下滑超过离开距离，将末尾元素移到首位
            moveTailToHead();
          }
          mScrollDistance -= mTextSpacing;
        } else if (mScrollDistance < -mHalfTextSpacing) {
          if (!mCanScrollLoop) {
            if (mSelectedIndex == mDataList.size() - 1) {
              mLastTouchY = offsetY;
              invalidate();
              break;
            } else {
              mSelectedIndex++;
            }
          } else {
            // 往上滑超过离开距离，将首位元素移到末尾
            moveHeadToTail();
          }
          mScrollDistance += mTextSpacing;
        }
        mLastTouchY = offsetY;
        invalidate();
        break;

      case MotionEvent.ACTION_UP:
        if (Math.abs(mScrollDistance) < 0.01) {
          mScrollDistance = 0;
          break;
        }
        cancelTimerTask();
        mTimerTask = new ScrollTimerTask(mHandler);
        mTimer.schedule(mTimerTask, 0, 10);
        break;
    }
    return true;
  }

  private void cancelTimerTask() {
    if (mTimerTask != null) {
      mTimerTask.cancel();
      mTimerTask = null;
    }
    if (mTimer != null) {
      mTimer.purge();
    }
  }

  private void moveTailToHead() {
    if (!mCanScrollLoop || mDataList.isEmpty()) return;

    String tail = mDataList.get(mDataList.size() - 1);
    mDataList.remove(mDataList.size() - 1);
    mDataList.add(0, tail);
  }

  private void moveHeadToTail() {
    if (!mCanScrollLoop || mDataList.isEmpty()) return;

    String head = mDataList.get(0);
    mDataList.remove(0);
    mDataList.add(head);
  }

  private void keepScrolling() {
    if (Math.abs(mScrollDistance) < AUTO_SCROLL_SPEED) {
      mScrollDistance = 0;
      if (mTimerTask != null) {
        cancelTimerTask();

        if (mOnSelectListener != null && mSelectedIndex < mDataList.size()) {
          mOnSelectListener.onSelect(this, mDataList.get(mSelectedIndex));
        }
      }
    } else if (mScrollDistance > 0) {
      // 向下滚动
      mScrollDistance -= AUTO_SCROLL_SPEED;
    } else {
      // 向上滚动
      mScrollDistance += AUTO_SCROLL_SPEED;
    }
    invalidate();
  }

  public void setDataList(List<String> list) {
    if (list == null || list.isEmpty()) return;

    mDataList = list;
    // 重置 mSelectedIndex，防止溢出
    mSelectedIndex = 0;
    invalidate();
  }

  public void setSelected(int index) {
    if (index >= mDataList.size()) return;

    mSelectedIndex = index;
    if (mCanScrollLoop) {
      int position = mDataList.size() / 2 - mSelectedIndex;
      if (position < 0) {
        for (int i = 0; i < -position; i++) {
          moveHeadToTail();
          mSelectedIndex--;
        }
      } else if (position > 0) {
        for (int i = 0; i < position; i++) {
          moveTailToHead();
          mSelectedIndex++;
        }
      }
    }
    invalidate();
  }

  /** set select listener */
  public void setOnSelectListener(OnSelectListener listener) {
    mOnSelectListener = listener;
  }

  /** can scroll */
  public void setCanScroll(boolean canScroll) {
    mCanScroll = canScroll;
  }

  /** loop scroll */
  public void setCanScrollLoop(boolean canLoop) {
    mCanScrollLoop = canLoop;
  }

  public void startAnim() {
    if (!mCanShowAnim) return;

    if (mScrollAnim == null) {
      PropertyValuesHolder alpha = PropertyValuesHolder.ofFloat("alpha", 1f, 0f, 1f);
      PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat("scaleX", 1f, 1.3f, 1f);
      PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat("scaleY", 1f, 1.3f, 1f);
      mScrollAnim =
          ObjectAnimator.ofPropertyValuesHolder(this, alpha, scaleX, scaleY).setDuration(200);
    }

    if (!mScrollAnim.isRunning()) {
      mScrollAnim.start();
    }
  }

  public void setCanShowAnim(boolean canShowAnim) {
    mCanShowAnim = canShowAnim;
  }

  public void onDestroy() {
    mOnSelectListener = null;
    mHandler.removeCallbacksAndMessages(null);
    if (mScrollAnim != null && mScrollAnim.isRunning()) {
      mScrollAnim.cancel();
    }
    cancelTimerTask();
    if (mTimer != null) {
      mTimer.cancel();
      mTimer = null;
    }
  }
}
