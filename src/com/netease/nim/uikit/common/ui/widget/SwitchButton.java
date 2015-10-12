package com.netease.nim.uikit.common.ui.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

import com.netease.nim.uikit.R;


/**
 * 仿iphone 开关按钮
 * 
 * @author sunyoujun
 * 
 */
public class SwitchButton extends View implements OnTouchListener {

	private boolean isChoose = false;// 记录当前按钮是否打开,true为打开,flase为关闭

	private boolean isChecked;

	private boolean onSlip = false;// 记录用户是否在滑动的变量

	private float down_x, now_x;// 按下时的x,当前的x

	private Rect btn_off, btn_on;// 打开和关闭状态下,游标的Rect .

	private boolean isChangeOn = false;

	private boolean isInterceptOn = false;

	private OnChangedListener onChangedListener;

	private Bitmap bg_on, bg_off, slip_btn;

	public SwitchButton(Context context) {
		super(context);
		init();
	}

	public SwitchButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public SwitchButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {// 初始化
		bg_on = BitmapFactory.decodeResource(getResources(), R.drawable.nim_slide_toggle_on);
		bg_off = BitmapFactory.decodeResource(getResources(), R.drawable.nim_slide_toggle_off);
		slip_btn = BitmapFactory.decodeResource(getResources(), R.drawable.nim_slide_toggle);
		btn_off = new Rect(0, 0, slip_btn.getWidth(), slip_btn.getHeight());
		btn_on = new Rect(bg_off.getWidth() - slip_btn.getWidth(), 0, bg_off.getWidth(), slip_btn.getHeight());
		setOnTouchListener(this);// 设置监听器,也可以直接复写OnTouchEvent
	}

	@SuppressLint("DrawAllocation")
	@Override
	protected void onDraw(Canvas canvas) {// 绘图函数

		super.onDraw(canvas);

		Matrix matrix = new Matrix();
		Paint paint = new Paint();
		float x;
		// 滑动到前半段与后半段的背景不同,在此做判断
		if (now_x < (bg_on.getWidth() / 2)) {
			x = now_x - slip_btn.getWidth() / 2;
			canvas.drawBitmap(bg_off, matrix, paint);// 画出关闭时的背景
		} else {
			x = bg_on.getWidth() - slip_btn.getWidth() / 2;
			canvas.drawBitmap(bg_on, matrix, paint);// 画出打开时的背景
		}
		// 是否是在滑动状态
		if (onSlip) {
			if (now_x >= bg_on.getWidth()) {// 是否划出指定范围,不能让游标跑到外头,必须做这个判断
				x = bg_on.getWidth() - slip_btn.getWidth() / 2;// 减去游标1/2的长度...
			} else if (now_x < 0) {
				x = 0;
			} else {
				x = now_x - slip_btn.getWidth() / 2;
			}
		} else {// 非滑动状态
			if (isChoose) {// 根据现在的开关状态设置画游标的位置
				x = btn_on.left;
				canvas.drawBitmap(bg_on, matrix, paint);// 初始状态为true时应该画出打开状态图片
			} else {
				x = btn_off.left;
			}
		}
		if (isChecked) {
			canvas.drawBitmap(bg_on, matrix, paint);
			x = btn_on.left;
			isChecked = !isChecked;
		}

		// 对游标位置进行异常判断...
		if (x < 0) {
			x = 0;
		} else if (x > bg_on.getWidth() - slip_btn.getWidth()) {
			x = bg_on.getWidth() - slip_btn.getWidth();
		}
		canvas.drawBitmap(slip_btn, x, 0, paint);// 画出游标.
	}

	public boolean onTouch(View v, MotionEvent event) {
		boolean old = isChoose;
		switch (event.getAction()) {
		case MotionEvent.ACTION_MOVE:// 滑动
			now_x = event.getX();
			break;
		case MotionEvent.ACTION_DOWN:// 按下
			if (event.getX() > bg_on.getWidth() || event.getY() > bg_on.getHeight()) {
				return false;
			}
			onSlip = true;
			down_x = event.getX();
			now_x = down_x;
			break;
		case MotionEvent.ACTION_CANCEL: // 移到控件外部
			onSlip = false;
			boolean choose = isChoose;
			if (now_x >= (bg_on.getWidth() / 2)) {
				now_x = bg_on.getWidth() - slip_btn.getWidth() / 2;
				isChoose = true;
			} else {
				now_x = now_x - slip_btn.getWidth() / 2;
				isChoose = false;
			}
			if (isChangeOn && (choose != isChoose)) { // 如果设置了监听器,就调用其方法..
				onChangedListener.OnChanged(this, isChoose);
			}
			break;
		case MotionEvent.ACTION_UP:// 松开
			onSlip = false;
			boolean lastChoose = isChoose;
			if (event.getX() >= (bg_on.getWidth() / 2)) {
				now_x = bg_on.getWidth() - slip_btn.getWidth() / 2;
				isChoose = true;
			} else {
				now_x = now_x - slip_btn.getWidth() / 2;
				isChoose = false;
			}
			if (lastChoose == isChoose) {// 相等表示点击状态未切换，之后切换状态
				if (event.getX() >= (bg_on.getWidth() / 2)) {
					now_x = 0;
					isChoose = false;
				} else {
					now_x = bg_on.getWidth() - slip_btn.getWidth() / 2;
					isChoose = true;
				}
			}
			// 如果设置了监听器,就调用其方法..
			if (isChangeOn) {
				onChangedListener.OnChanged(this, isChoose);
			}
			break;
		default:
		}
		if (!old && isInterceptOn) {
			isChoose = false;
		} else {
			invalidate();// 重画控件
		}
		return true;
	}

	public void setOnChangedListener(OnChangedListener listener) {// 设置监听器,当状态修改的时候
		isChangeOn = true;
		onChangedListener = listener;
	}

	public interface OnChangedListener {

		abstract void OnChanged(View v, boolean checkState);
	}

	public void setCheck(boolean isChecked) {
		this.isChecked = isChecked;
		isChoose = isChecked;
		if (isChecked == false) {
			now_x = 0;
		}
		invalidate();
	}
	
	public boolean isChoose(){
		return this.isChoose;
	}
	
	public boolean getCheck(){
		return this.isChecked;
	}

	public void setInterceptState(boolean isIntercept) {// 设置监听器,是否在重画钱拦截事件,状态由false变true时 拦截事件
		isInterceptOn = isIntercept;
		// onInterceptListener = listener;
	}
}
