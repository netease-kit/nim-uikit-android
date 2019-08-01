package com.netease.nim.uikit.common.media.imagepicker.view;

import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.netease.nim.uikit.R;


/**
 */

public class GLBusyIndicatorView extends AppCompatImageView {

    private ValueAnimator animator;

    private boolean attached;

    private boolean animEnabled = true;

    private float startRotation;

    private static final TimeInterpolator sInterpolator = new LinearInterpolator();

    public GLBusyIndicatorView(Context context) {
        super(context);
        init();
    }

    public GLBusyIndicatorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GLBusyIndicatorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.icon_loading));
    }

    public void setAnimEnabled(boolean animEnabled){
        if (this.animEnabled != animEnabled){
            this.animEnabled = animEnabled;
            if (animEnabled && attached && getVisibility() == View.VISIBLE){
                startProgress();
            } else {
                stopProgress();
            }
        }
    }

    public boolean isAnimEnabled() {
        return animEnabled;
    }

    private void startProgress(){
        if (animator != null){
            animator.cancel();
            animator = null;
        }

        if (!animEnabled){
            return;
        }

        animator = ValueAnimator.ofFloat(0, 1);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setDuration(1000);
        animator.setInterpolator(sInterpolator);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float percent = (float) animation.getAnimatedValue();
                updateRotation(percent);
            }
        });
        animator.start();
    }

    private void stopProgress(){
        if (animator != null){
            animator.cancel();
            animator = null;
        }

        updateRotation(0);
    }

    /***
     * @param rotation 开始的角度, 360度
     * */
    public void setStartRotation(float rotation){
        this.startRotation = rotation;
        updateRotation(0);
    }

    private void updateRotation(float percent){
        setPivotX(getMeasuredWidth() >> 1);
        setPivotY(getMeasuredHeight() >> 1);
        setRotation(360 * percent + startRotation);
    }

    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (visibility == View.VISIBLE){
            startProgress();
        } else {
            stopProgress();
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        attached = true;
        startProgress();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        attached = false;
        stopProgress();
    }
}
