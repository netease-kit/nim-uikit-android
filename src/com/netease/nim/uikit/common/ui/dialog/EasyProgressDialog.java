package com.netease.nim.uikit.common.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.TextView;

import com.netease.nim.uikit.R;

/**
 * 
 * 一个半透明窗口,包含一个Progressbar 和 Message部分. 其中Message部分可选. 可单独使用,也可以使用
 * {@link DialogMaker} 进行相关窗口显示.
 * 
 * @author Qijun
 * 
 */
public class EasyProgressDialog extends Dialog {
	private Context mContext;

	private String mMessage;

	private int mLayoutId;

	public EasyProgressDialog(Context context, int style, int layout) {
		super(context, style);
		mContext = context;
		WindowManager.LayoutParams Params = getWindow().getAttributes();
		Params.width = LayoutParams.FILL_PARENT;
		Params.height = LayoutParams.FILL_PARENT;
		getWindow().setAttributes(Params);
		mLayoutId = layout;
	}

	public EasyProgressDialog(Context context, int layout, String msg) {
		this(context, R.style.easy_dialog_style, layout);
		setMessage(msg);
	}

	public EasyProgressDialog(Context context, String msg) {
		this(context, R.style.easy_dialog_style, R.layout.easy_progress_dialog);
		setMessage(msg);
	}

	public EasyProgressDialog(Context context) {
		this(context, R.style.easy_dialog_style, R.layout.easy_progress_dialog);
	}

	public void setMessage(String msg) {
		mMessage = msg;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(mLayoutId);
		if (!TextUtils.isEmpty(mMessage)) {
			TextView message = (TextView) findViewById(R.id.easy_progress_dialog_message);
			message.setVisibility(View.VISIBLE);
			message.setText(mMessage);
		}
	}

}