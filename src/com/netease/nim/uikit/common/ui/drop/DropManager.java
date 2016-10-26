package com.netease.nim.uikit.common.ui.drop;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextPaint;

import com.netease.nim.uikit.R;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.netease.nim.uikit.common.util.sys.ScreenUtil;

/**
 * Created by huangjun on 2016/9/13.
 */
public class DropManager {

    // constant
    private static final String TAG = "DropManager";

    static final int TEXT_SIZE = ScreenUtil.sp2px(12); // 12sp

    static final int CIRCLE_RADIUS = ScreenUtil.dip2px(10); // 10dip

    // single instance
    private static DropManager instance;

    public static synchronized DropManager getInstance() {
        if (instance == null) {
            instance = new DropManager();
        }

        return instance;
    }

    // field
    private boolean isTouchable; // 是否响应按键事件，如果一个气泡已经在响应，其它气泡就不响应，同一界面始终最多只有一个气泡响应按键

    private int statusBarHeight; // 状态栏(通知栏)高度

    private DropCover dropCover; // Drop全屏动画

    private Object currentId; // 当前正在执行动画的业务节点

    private TextPaint textPaint; // 文本画笔共享

    private float textYOffset; // 文本y轴居中需要的offset

    private Paint circlePaint; // 圆形画笔共享

    private int[] explosionResIds = new int[]{
            R.drawable.explosion_one,
            R.drawable.explosion_two,
            R.drawable.explosion_three,
            R.drawable.explosion_four,
            R.drawable.explosion_five
    };

    // interface
    public void init(Context context, DropCover dropCover, DropCover.IDropCompletedListener listener) {
        this.isTouchable = true;
        this.statusBarHeight = ScreenUtil.getStatusBarHeight(context);
        this.dropCover = dropCover;
        this.dropCover.addDropCompletedListener(listener);

        LogUtil.i(TAG, "init DropManager, statusBarHeight=" + statusBarHeight);
    }

    public void initPaint() {
        getCirclePaint();
        getTextPaint();
    }

    public void destroy() {
        this.isTouchable = false;
        this.statusBarHeight = 0;
        this.dropCover.removeAllDropCompletedListeners();
        this.dropCover = null;
        this.currentId = null;
        this.textPaint = null;
        this.textYOffset = 0;
        this.circlePaint = null;

        LogUtil.i(TAG, "destroy DropManager");
    }

    public boolean isTouchable() {
        return isTouchable;
    }

    public void setTouchable(boolean isTouchable) {
        this.isTouchable = isTouchable;
    }

    public int getTop() {
        return statusBarHeight;
    }

    public DropCover getDropCover() {
        return this.dropCover;
    }

    public void setCurrentId(Object currentId) {
        this.currentId = currentId;
    }

    public Object getCurrentId() {
        return currentId;
    }

    public Paint getCirclePaint() {
        if (circlePaint == null) {
            circlePaint = new Paint();
            circlePaint.setColor(Color.RED);
            circlePaint.setAntiAlias(true);
        }

        return circlePaint;
    }

    public TextPaint getTextPaint() {
        if (textPaint == null) {
            textPaint = new TextPaint();
            textPaint.setAntiAlias(true);
            textPaint.setColor(Color.WHITE);
            textPaint.setTextAlign(Paint.Align.CENTER);
            textPaint.setTextSize(TEXT_SIZE);
            Paint.FontMetrics textFontMetrics = textPaint.getFontMetrics();

            /*
             * drawText从baseline开始，baseline的值为0，baseline的上面为负值，baseline的下面为正值，
             * 即这里ascent为负值，descent为正值。
             * 比如ascent为-20，descent为5，那需要移动的距离就是20 - （20 + 5）/ 2
             */
            textYOffset = -textFontMetrics.ascent - (-textFontMetrics.ascent + textFontMetrics.descent) / 2;
        }

        return textPaint;
    }

    public float getTextYOffset() {
        getTextPaint();
        return textYOffset;
    }

    public int[] getExplosionResIds() {
        return explosionResIds;
    }
}
