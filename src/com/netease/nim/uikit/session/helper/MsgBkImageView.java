package com.netease.nim.uikit.session.helper;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

public class MsgBkImageView extends ImageView {
	public MsgBkImageView(Context context) {
		super(context);
	
		init();
	}

	public MsgBkImageView(Context context, AttributeSet attrs) {
		super(context, attrs);

		init();
	}

	public MsgBkImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	
		init();
	}
	
	private final void init() {
		super.setScaleType(ScaleType.CENTER_CROP);
	}
	
	@Override
	public final void setScaleType(ScaleType scaleType) {
		// REJECT
	}

	@Override
	protected void onDraw(Canvas canvas) {
		Drawable dr = getDrawable();
		
		if (dr == null) {
			super.onDraw(canvas);
			
			return;
		}
		
        int dwidth = dr.getBounds().width();
        int dheight = dr.getBounds().height();

        int vwidth = getWidth() - getPaddingLeft() - getPaddingRight();
        int vheight = getHeight() - getPaddingTop() - getPaddingBottom();
        
        float scale;
        float dx = 0, dy = 0;

        if (dwidth * vheight > vwidth * dheight) {
            scale = (float) vheight / (float) dheight; 
            dx = (vwidth - dwidth * scale) * 0.5f;
        } else {
            scale = (float) vwidth / (float) dwidth;
            dy = (vheight - dheight * scale) * 0.5f;
        }
        
        canvas.save();
		
        canvas.translate(0, -(int) (dy + 0.5f));
        
		super.onDraw(canvas);

		canvas.restore();
	}
}
