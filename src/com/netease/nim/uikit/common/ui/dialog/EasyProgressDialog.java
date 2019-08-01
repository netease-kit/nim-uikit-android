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
 * 一个半透明窗口,包含一个Progressbar 和 Message部分. 其中Message部分可选. 可单独使用,也可以使用
 * {@link DialogMaker} 进行相关窗口显示.
 *
 * @author Qijun
 */
public class EasyProgressDialog extends Dialog {
    private Context mContext;

    private String mMessage;

    private int mLayoutId;

    private TextView message;

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
        this(context, R.style.easy_dialog_style, R.layout.nim_easy_progress_dialog);
        setMessage(msg);
    }

    public EasyProgressDialog(Context context) {
        this(context, R.style.easy_dialog_style, R.layout.nim_easy_progress_dialog);
    }

    public void setMessage(String msg) {
        mMessage = msg;
    }

    public void updateLoadingMessage(String msg) {
        mMessage = msg;
        showMessage();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(mLayoutId);
        message = (TextView) findViewById(R.id.easy_progress_dialog_message);
        showMessage();
    }

    private void showMessage() {
        if (message != null && !TextUtils.isEmpty(mMessage)) {
            message.setVisibility(View.VISIBLE);
            message.setText(mMessage);
        }
    }
}