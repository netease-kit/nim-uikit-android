package com.netease.nim.uikit.common.media.imagepicker.adapter;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.netease.nim.uikit.R;
import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.common.util.sys.ScreenUtil;

/**
 *
 */

public class ImageItemDecoration extends RecyclerView.ItemDecoration {
    private Paint paint = new Paint();
    private int dividerSize;

    public ImageItemDecoration() {
        super();
        paint.setColor(NimUIKit.getContext().getResources().getColor(R.color.color_grey_eaeaea));
        dividerSize = ScreenUtil.dip2px(1);
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDraw(c, parent, state);
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDrawOver(c, parent, state);

        final int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = parent.getChildAt(i);
            if (child.getWidth() == ScreenUtil.getDisplayWidth()) {
                continue;
            }

            final int left = child.getLeft();
            final int right = child.getRight();
            final int top = child.getTop();
            final int bottom = child.getBottom();

            c.drawRect(left, bottom - dividerSize, right, bottom, paint);
            c.drawRect(right - dividerSize, top, right, bottom, paint);
        }
    }
}
