package com.netease.nim.uikit.common.ui.popupmenu;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class PopupMenuListView extends ListView {

	public PopupMenuListView(Context context) {
		super(context);
	}

	public PopupMenuListView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public PopupMenuListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int maxWidth = meathureWidthByChilds() + getPaddingLeft() + getPaddingRight();
		super.onMeasure(MeasureSpec.makeMeasureSpec(maxWidth, MeasureSpec.EXACTLY), heightMeasureSpec);
	}

	public int meathureWidthByChilds() {
		int maxWidth = 0;
		View view = null;
		for (int i = 0; i < getAdapter().getCount(); i++) {
			view = getAdapter().getView(i, view, this);
			if (view != null) {
				view.setLayoutParams(new ViewGroup.LayoutParams(0, 0));
				view.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
				if (view.getMeasuredWidth() > maxWidth) {
					maxWidth = view.getMeasuredWidth();
				}
			}
		}
		return maxWidth;
	}
}
