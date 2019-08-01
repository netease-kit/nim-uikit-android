package com.netease.nim.uikit.common.ui.drop;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.CycleInterpolator;
import android.view.animation.TranslateAnimation;

import com.netease.nim.uikit.common.util.sys.ScreenUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 悬浮在屏幕上的红点拖拽动画绘制区域
 * <p>
 * Created by huangjun on 2016/9/13.
 */
public class DropCover extends View {

    public interface IDropCompletedListener {
        void onCompleted(Object id, boolean explosive);
    }

    private final float MAX_RATIO = 0.8f; // 固定圆最大的缩放比例
    private final float MIN_RATIO = 0.4f; // 固定圆最小的缩放比例
    private final int DISTANCE_LIMIT = ScreenUtil.dip2px(70); // 固定圆和移动圆的圆心之间的断裂距离
    private static final int SHAKE_ANIM_DURATION = 150; // 抖动动画执行的时间
    private static final int EXPLOSION_ANIM_FRAME_INTERVAL = 50; // 爆裂动画帧之间的间隔
    private static final int CLICK_DISTANCE_LIMIT = ScreenUtil.dip2px(15); // 不超过此距离视为点击
    private static final int CLICK_DELTA_TIME_LIMIT = 10; // 超过此时长需要爆裂

    private View dropFake;
    private Path path = new Path();
    private int radius; // 移动圆形半径
    private float curX; // 当前手指x坐标
    private float curY; // 当前手指y坐标
    private float circleX; // 固定圆的圆心x坐标
    private float circleY; // 固定圆的圆心y坐标
    private float ratio = 1; // 圆缩放的比例，随着手指的移动，固定的圆越来越小
    private boolean needDraw = true; // 是否需要执行onDraw方法
    private boolean hasBroken = false; // 是否已经断裂过，断裂过就不需要再画Path了
    private boolean isDistanceOverLimit = false; // 当前移动圆和固定圆的距离是否超过限值
    private boolean click = true; // 是否在点击的距离限制范围内，超过了clickDistance则不属于点击
    private long clickTime; // 记录down的时间点
    private String text; // 显示的数字

    private Bitmap[] explosionAnim; // 爆裂动画位图
    private boolean explosionAnimStart; // 爆裂动画是否开始
    private int explosionAnimNumber; // 爆裂动画帧的个数
    private int curExplosionAnimIndex; // 爆裂动画当前帧
    private int explosionAnimWidth; // 爆裂动画帧的宽度
    private int explosionAnimHeight; // 爆裂动画帧的高度
    private List<IDropCompletedListener> dropCompletedListeners; // 拖拽动作完成，回调

    /**
     * ************************* 绘制 *************************
     */
    public DropCover(Context context, AttributeSet attrs) {
        super(context, attrs);

        DropManager.getInstance().initPaint();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 绘制两个圆/Path/文本
        if (needDraw) {
            drawCore(canvas);
        }

        // 爆裂动画
        if (explosionAnimStart) {
            drawExplosionAnimation(canvas);
        }
    }

    private void drawCore(Canvas canvas) {
        if (!needDraw) {
            return;
        }

        final Paint circlePaint = DropManager.getInstance().getCirclePaint();
        // 画固定圆（如果已经断裂过了，就不需要画固定圆了）
        if (!hasBroken && !isDistanceOverLimit) {
            canvas.drawCircle(circleX, circleY, radius * ratio, circlePaint);
        }

        // 画移动圆和连线（如果已经断裂过了，就不需要再画Path了）
        if (curX != 0 && curY != 0) {
            canvas.drawCircle(curX, curY, radius, circlePaint);
            if (!hasBroken && !isDistanceOverLimit) {
                drawPath(canvas);
            }
        }

        // 数字要最后画，否则会被连线遮掩
        if (!TextUtils.isEmpty(text)) {
            final float textMove = DropManager.getInstance().getTextYOffset();
            final TextPaint textPaint = DropManager.getInstance().getTextPaint();
            if (curX != 0 && curY != 0) {
                // 移动圆里面的数字
                canvas.drawText(text, curX, curY + textMove, textPaint);
            } else {
                // 只有初始时需要绘制固定圆里面的数字
                canvas.drawText(text, circleX, circleY + textMove, textPaint);
            }
        }
    }

    /**
     * 画固定圆和移动圆之间的连线
     */
    private void drawPath(Canvas canvas) {
        path.reset();

        float distance = (float) distance(circleX, circleY, curX, curY); // 移动圆和固定圆圆心之间的距离
        float sina = (curY - circleY) / distance; // 移动圆圆心和固定圆圆心之间的连线与X轴相交形成的角度的sin值
        float cosa = (circleX - curX) / distance; // 移动圆圆心和固定圆圆心之间的连线与X轴相交形成的角度的cos值

        float AX = circleX - sina * radius * ratio;
        float AY = circleY - cosa * radius * ratio;
        float BX = circleX + sina * radius * ratio;
        float BY = circleY + cosa * radius * ratio;
        float OX = (circleX + curX) / 2;
        float OY = (circleY + curY) / 2;
        float CX = curX + sina * radius;
        float CY = curY + cosa * radius;
        float DX = curX - sina * radius;
        float DY = curY - cosa * radius;
        path.moveTo(AX, AY); // A点坐标
        path.lineTo(BX, BY); // AB连线
        path.quadTo(OX, OY, CX, CY); // 控制点为两个圆心的中间点，贝塞尔曲线，BC连线
        path.lineTo(DX, DY); // CD连线
        path.quadTo(OX, OY, AX, AY); // 控制点也是两个圆心的中间点，贝塞尔曲线，DA连线

        canvas.drawPath(path, DropManager.getInstance().getCirclePaint());
    }

    /**
     * ************************* TouchListener回调 *************************
     */
    public void down(View fakeView, String text) {
        this.needDraw = true; // 由于DropCover是公用的，每次进来时都要确保needDraw的值为true
        this.hasBroken = false; // 未断裂
        this.isDistanceOverLimit = false; // 当前移动圆和固定圆的距离是否超过限值
        this.click = true; // 点击开始

        this.dropFake = fakeView;
        int[] position = new int[2];
        dropFake.getLocationOnScreen(position);

        this.radius = DropManager.CIRCLE_RADIUS;
        // 固定圆圆心坐标，固定圆圆心坐标y，需要减去系统状态栏高度
        this.circleX = position[0] + dropFake.getWidth() / 2;
        this.circleY = position[1] - DropManager.getInstance().getTop() + dropFake.getHeight() / 2;
        // 移动圆圆心坐标
        this.curX = this.circleX;
        this.curY = this.circleY;

        this.text = text;
        this.clickTime = System.currentTimeMillis();

        // hide fake view, show current
        dropFake.setVisibility(View.INVISIBLE); // 隐藏固定范围的DropFake
        this.setVisibility(View.VISIBLE); // 当前全屏范围的DropCover可见

        invalidate();
    }

    public void move(float curX, float curY) {
        curY -= DropManager.getInstance().getTop(); // 位置校准，去掉通知栏高度

        this.curX = curX;
        this.curY = curY;

        calculateRatio((float) distance(curX, curY, circleX, circleY)); // 计算固定圆缩放的比例

        invalidate();
    }

    /**
     * 计算固定圆缩放的比例
     */
    private void calculateRatio(float distance) {
        if (isDistanceOverLimit = distance > DISTANCE_LIMIT) {
            hasBroken = true; // 已经断裂过了
        }

        // 固定圆缩放比例0.4-0.8之间
        ratio = MIN_RATIO + (MAX_RATIO - MIN_RATIO) * (1.0f * Math.max(DISTANCE_LIMIT - distance, 0)) / DISTANCE_LIMIT;
    }

    public void up() {
        boolean longClick = click && (System.currentTimeMillis() - this.clickTime > CLICK_DELTA_TIME_LIMIT); // 长按

        // 没有超出最大移动距离&&不是长按点击事件，UP时需要让移动圆回到固定圆的位置
        if (!isDistanceOverLimit && !longClick) {
            if (hasBroken) {
                // 如果已经断裂，那么直接回原点，显示FakeView
                onDropCompleted(false);
            } else {
                // 如果还未断裂，那么执行抖动动画
                shakeAnimation();
            }
            // reset
            curX = 0;
            curY = 0;
            ratio = 1;
        } else {
            // 超出最大移动距离，那么执行爆裂帧动画
            initExplosionAnimation();

            needDraw = false;
            explosionAnimStart = true;
        }

        invalidate();
    }

    public double distance(float x1, float y1, float x2, float y2) {
        double distance = Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
        if (distance > CLICK_DISTANCE_LIMIT) {
            click = false; // 已经不是点击了
        }

        return distance;
    }

    /**
     * ************************* 爆炸动画(帧动画) *************************
     */
    private void initExplosionAnimation() {
        if (explosionAnim == null) {
            int[] explosionResIds = DropManager.getInstance().getExplosionResIds();
            explosionAnimNumber = explosionResIds.length;
            explosionAnim = new Bitmap[explosionAnimNumber];
            for (int i = 0; i < explosionAnimNumber; i++) {
                explosionAnim[i] = BitmapFactory.decodeResource(getResources(), explosionResIds[i]);
            }

            explosionAnimHeight = explosionAnimWidth = explosionAnim[0].getWidth(); // 每帧长宽都一致
        }
    }

    private void drawExplosionAnimation(Canvas canvas) {
        if (!explosionAnimStart) {
            return;
        }

        if (curExplosionAnimIndex < explosionAnimNumber) {
            canvas.drawBitmap(explosionAnim[curExplosionAnimIndex],
                    curX - explosionAnimWidth / 2, curY - explosionAnimHeight / 2, null);
            curExplosionAnimIndex++;
            // 每隔固定时间执行
            postInvalidateDelayed(EXPLOSION_ANIM_FRAME_INTERVAL);
        } else {
            // 动画结束
            explosionAnimStart = false;
            curExplosionAnimIndex = 0;
            curX = 0;
            curY = 0;
            onDropCompleted(true); // explosive true
        }
    }

    private void recycleBitmap() {
        if (explosionAnim != null && explosionAnim.length != 0) {
            for (int i = 0; i < explosionAnim.length; i++) {
                if (explosionAnim[i] != null && !explosionAnim[i].isRecycled()) {
                    explosionAnim[i].recycle();
                    explosionAnim[i] = null;
                }
            }

            explosionAnim = null;
        }
    }

    /**
     * ************************* 抖动动画(View平移动画) *************************
     */
    public void shakeAnimation() {
        // 避免动画抖动的频率过大，所以除以10，另外，抖动的方向跟手指滑动的方向要相反
        Animation translateAnimation = new TranslateAnimation((circleX - curX) / 10, 0, (circleY - curY) / 10, 0);
        translateAnimation.setInterpolator(new CycleInterpolator(1));
        translateAnimation.setDuration(SHAKE_ANIM_DURATION);
        startAnimation(translateAnimation);

        translateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // 抖动动画结束时，show Fake, hide current
                onDropCompleted(false);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
    }

    /**
     * ************************* 拖拽动作结束事件 *************************
     */
    public void addDropCompletedListener(IDropCompletedListener listener) {
        if (listener == null) {
            return;
        }

        if (dropCompletedListeners == null) {
            dropCompletedListeners = new ArrayList<>(1);
        }

        dropCompletedListeners.add(listener);
    }

    public void removeDropCompletedListener(IDropCompletedListener listener) {
        if (listener == null || dropCompletedListeners == null) {
            return;
        }

        dropCompletedListeners.remove(listener);
    }

    public void removeAllDropCompletedListeners() {
        if (dropCompletedListeners == null) {
            return;
        }

        dropCompletedListeners.clear();
    }

    private void onDropCompleted(boolean explosive) {
        dropFake.setVisibility(explosive ? View.INVISIBLE : View.VISIBLE); // show or hide fake view
        this.setVisibility(View.INVISIBLE); // hide current
        recycleBitmap(); // recycle

        // notify observer
        if (dropCompletedListeners != null) {
            for (IDropCompletedListener listener : dropCompletedListeners) {
                listener.onCompleted(DropManager.getInstance().getCurrentId(), explosive);
            }
        }

        // free
        DropManager.getInstance().setTouchable(true);
    }
}
