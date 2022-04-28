/*
 * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.kit.contactkit.ui.indexbar;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.netease.yunxin.kit.contactkit.ui.R;
import com.netease.yunxin.kit.contactkit.ui.indexbar.bean.IndexPinyinBean;
import com.netease.yunxin.kit.contactkit.ui.indexbar.helper.IIndexBarDataHelper;
import com.netease.yunxin.kit.contactkit.ui.indexbar.helper.IndexBarDataHelperImpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * index bar in the right of contact
 */
public class IndexBar extends View {

    public IndexBar(Context context) {
        this(context, null);
    }

    public IndexBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IndexBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    public static String[] INDEX_STRING = {"A", "B", "C", "D", "E", "F", "G", "H", "I",
            "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V",
            "W", "X", "Y", "Z", "#"};

    // whether need get index from data（if only A，B，C，show A B C Tag only）
    private boolean isNeedRealIndex;

    private List<String> mIndexDataList;

    //width and height for view
    private int mWidth, mHeight;

    //height for item
    private int mGapHeight;

    //pressed index
    int pressI = -1;

    private Paint mPaint;

    //paint for background
    private Paint backgroundPaint;

    private int mPressedBackground;

    @ColorInt
    private int textColor;

    @ColorInt
    private int textColorPressed;


    private IIndexBarDataHelper mDataHelper;

    //this TextView will be set outside，it will show selected tag
    private TextView mPressedShowTextView;
    private boolean isSourceDataAlreadySorted;
    private List<? extends IndexPinyinBean> mSourceData;
    private LinearLayoutManager mLayoutManager;
    private int mHeaderViewCount = 0;
    private onIndexPressedListener mOnIndexPressedListener;

    public int getHeaderViewCount() {
        return mHeaderViewCount;
    }


    public IndexBar setHeaderViewCount(int headerViewCount) {
        mHeaderViewCount = headerViewCount;
        return this;
    }

    public boolean isSourceDataAlreadySorted() {
        return isSourceDataAlreadySorted;
    }

    public IndexBar setSourceDataAlreadySorted(boolean sourceDataAlreadySorted) {
        isSourceDataAlreadySorted = sourceDataAlreadySorted;
        return this;
    }

    public IIndexBarDataHelper getDataHelper() {
        return mDataHelper;
    }


    public IndexBar setDataHelper(IIndexBarDataHelper dataHelper) {
        mDataHelper = dataHelper;
        return this;
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        int textSize = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP, 8, getResources().getDisplayMetrics());//default text size
        textColor = getContext().getResources().getColor(R.color.color_14131b);
        textColorPressed = getContext().getResources().getColor(R.color.color_ffffff);
        mPressedBackground = Color.TRANSPARENT;//default background color
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.IndexBar, defStyleAttr, 0);
        int n = typedArray.getIndexCount();
        for (int i = 0; i < n; i++) {
            int attr = typedArray.getIndex(i);
            if (attr == R.styleable.IndexBar_indexBarTextSize) {
                textSize = typedArray.getDimensionPixelSize(attr, textSize);
            } else if (attr == R.styleable.IndexBar_indexBarPressBackground) {
                mPressedBackground = typedArray.getColor(attr, mPressedBackground);
            } else if (attr == R.styleable.IndexBar_indexBarTextColor) {
                textColor = typedArray.getColor(attr, textColor);
            } else if (attr == R.styleable.IndexBar_indexBarTextColorPress) {
                textColorPressed = typedArray.getColor(attr, textColorPressed);
            }
        }
        typedArray.recycle();

        initIndexData();


        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setTextSize(textSize);
        mPaint.setColor(textColor);

        backgroundPaint = new Paint();
        backgroundPaint.setAntiAlias(true);
        backgroundPaint.setColor(context.getResources().getColor(R.color.color_537ff4));

        //set default index pressed listener
        setOnIndexPressedListener(new onIndexPressedListener() {
            @Override
            public void onIndexPressed(int index, String text) {
                //show hintTextView
                if (mPressedShowTextView != null) {
                    mPressedShowTextView.setVisibility(View.VISIBLE);
                    mPressedShowTextView.setText(text);
                }
                //scroll recycleView
                if (mLayoutManager != null) {
                    int position = getPosByTag(text);
                    if (position != -1) {
                        mLayoutManager.scrollToPositionWithOffset(position, 0);
                    }
                }
            }

            @Override
            public void onMotionEventEnd() {
                //hide hintTextView
                if (mPressedShowTextView != null) {
                    mPressedShowTextView.setVisibility(View.GONE);
                }
            }
        });

        mDataHelper = new IndexBarDataHelperImpl();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //get Mode and Size
        int wMode = MeasureSpec.getMode(widthMeasureSpec);
        int wSize = MeasureSpec.getSize(widthMeasureSpec);
        int hMode = MeasureSpec.getMode(heightMeasureSpec);
        int hSize = MeasureSpec.getSize(heightMeasureSpec);

        int measureWidth = 0, measureHeight = 0;
        //rect for index
        Rect indexBounds = new Rect();
        String index;//index tag
        for (int i = 0; i < mIndexDataList.size(); i++) {
            index = mIndexDataList.get(i);
            mPaint.getTextBounds(index, 0, index.length(), indexBounds);//get width and height for text rect
            measureWidth = Math.max(indexBounds.width(), measureWidth);//get max width for index
            measureHeight = Math.max(indexBounds.height(), measureHeight);//get max height for index
        }
        measureHeight *= mIndexDataList.size();//total index
        switch (wMode) {
            case MeasureSpec.EXACTLY:
                measureWidth = wSize;
                break;
            case MeasureSpec.AT_MOST:
                measureWidth = Math.min(measureWidth, wSize);
                break;
            case MeasureSpec.UNSPECIFIED:
                break;
        }

        switch (hMode) {
            case MeasureSpec.EXACTLY:
                measureHeight = hSize;
                break;
            case MeasureSpec.AT_MOST:
                measureHeight = Math.min(measureHeight, hSize);
                break;
            case MeasureSpec.UNSPECIFIED:
                break;
        }

        setMeasuredDimension(measureWidth, measureHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int t = getPaddingTop();
        String index;//index to draw
        for (int i = 0; i < mIndexDataList.size(); i++) {
            //draw selected index background
            if (i == pressI) {
                canvas.drawCircle((float) mWidth / 2, t + mGapHeight * (i + 0.5f), (float) Math.min(mWidth, mGapHeight) / 2, backgroundPaint);
                mPaint.setColor(textColorPressed);
            } else {
                mPaint.setColor(textColor);
            }
            index = mIndexDataList.get(i);
            Paint.FontMetrics fontMetrics = mPaint.getFontMetrics();//get FontMetrics of paint，y for drawText mean text baseLine
            int baseline = (int) ((mGapHeight - fontMetrics.bottom - fontMetrics.top) / 2);//get every index rect,baseline on vertical central
            canvas.drawText(index, (float) mWidth / 2 - mPaint.measureText(index) / 2, t + mGapHeight * i + baseline, mPaint);
        }
    }


    @Override
    public boolean performClick() {
        return super.performClick();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                setBackgroundColor(mPressedBackground);
            case MotionEvent.ACTION_MOVE:
                float y = event.getY();
                pressI = (int) ((y - getPaddingTop()) / mGapHeight);
                if (pressI < 0) {
                    pressI = 0;
                } else if (pressI >= mIndexDataList.size()) {
                    pressI = mIndexDataList.size() - 1;
                }
                if (null != mOnIndexPressedListener && pressI > -1 && pressI < mIndexDataList.size()) {
                    mOnIndexPressedListener.onIndexPressed(pressI, mIndexDataList.get(pressI));
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
                performClick();
            case MotionEvent.ACTION_CANCEL:
            default:
                setBackgroundResource(android.R.color.transparent);//set transparent when up
                if (null != mOnIndexPressedListener) {
                    mOnIndexPressedListener.onMotionEventEnd();
                }
                pressI = -1;
                invalidate();
                break;
        }
        return true;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        super.onSizeChanged(w, h, oldW, oldH);
        mWidth = w;
        mHeight = h;
        if (null == mIndexDataList || mIndexDataList.isEmpty()) {
            return;
        }
        computeGapHeight();
    }

    public onIndexPressedListener getOnIndexPressedListener() {
        return mOnIndexPressedListener;
    }

    public void setOnIndexPressedListener(onIndexPressedListener mOnIndexPressedListener) {
        this.mOnIndexPressedListener = mOnIndexPressedListener;
    }

    public IndexBar setPressedShowTextView(TextView mPressedShowTextView) {
        this.mPressedShowTextView = mPressedShowTextView;
        return this;
    }

    public IndexBar setLayoutManager(LinearLayoutManager mLayoutManager) {
        this.mLayoutManager = mLayoutManager;
        return this;
    }

    /**
     * must been invoked before {@link #setSourceData(List)}
     */
    public IndexBar setNeedRealIndex(boolean needRealIndex) {
        isNeedRealIndex = needRealIndex;
        initIndexData();
        return this;
    }

    private void initIndexData() {
        if (isNeedRealIndex) {
            mIndexDataList = new ArrayList<>();
        } else {
            mIndexDataList = Arrays.asList(INDEX_STRING);
        }
    }

    public IndexBar setSourceData(List<? extends IndexPinyinBean> mSourceDatas) {
        this.mSourceData = mSourceDatas;
        initSourceData();
        return this;
    }

    /**
     * init source data and get index data
     */
    private void initSourceData() {
        if (null == mSourceData || mSourceData.isEmpty()) {
            return;
        }
        if (!isSourceDataAlreadySorted) {
            //sort sourceData
            mDataHelper.sortSourceData(mSourceData);
        } else {
            mDataHelper.convert(mSourceData);
            mDataHelper.fillIndexTag(mSourceData);
        }
        if (isNeedRealIndex) {
            mDataHelper.getSortedIndexData(mSourceData, mIndexDataList);
            computeGapHeight();
        }
    }

    /**
     * invoked when：
     * 1 data changed
     * 2 size changed
     * get gapHeight
     */
    private void computeGapHeight() {
        mGapHeight = (mHeight - getPaddingTop() - getPaddingBottom()) / mIndexDataList.size();
    }

    private int getPosByTag(String tag) {
        if (null == mSourceData || mSourceData.isEmpty()) {
            return -1;
        }
        if (TextUtils.isEmpty(tag)) {
            return -1;
        }
        for (int i = 0; i < mSourceData.size(); i++) {
            if (tag.equals(mSourceData.get(i).getIndexTag())) {
                return i + getHeaderViewCount();
            }
        }
        return -1;
    }


    /**
     * on index pressed listener
     */
    public interface onIndexPressedListener {
        void onIndexPressed(int index, String text);

        void onMotionEventEnd();//（UP CANCEL）
    }

}
