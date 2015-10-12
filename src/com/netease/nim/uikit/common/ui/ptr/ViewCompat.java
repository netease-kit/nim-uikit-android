package com.netease.nim.uikit.common.ui.ptr;

import android.annotation.TargetApi;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.view.View;

@SuppressWarnings("deprecation")
public class ViewCompat {
	public static void postOnAnimation(View view, Runnable runnable) {
		if (VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN) {
			SDK16.postOnAnimation(view, runnable);
		} else {
			view.postDelayed(runnable, 16);
		}
	}

	public static void setBackground(View view, Drawable background) {
		if (VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN) {
			SDK16.setBackground(view, background);
		} else {
			view.setBackgroundDrawable(background);
		}
	}

	public static void setLayerType(View view, int layerType) {
		if (VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB) {
			SDK11.setLayerType(view, layerType);
		}
	}

	@TargetApi(11)
	static class SDK11 {

		public static void setLayerType(View view, int layerType) {
			view.setLayerType(layerType, null);
		}
	}

	@TargetApi(16)
	static class SDK16 {

		public static void postOnAnimation(View view, Runnable runnable) {
			view.postOnAnimation(runnable);
		}

		public static void setBackground(View view, Drawable background) {
			view.setBackground(background);
		}

	}
}
