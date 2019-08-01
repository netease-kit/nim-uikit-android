package com.netease.nim.uikit.common.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.netease.nim.uikit.R;
import com.netease.nim.uikit.common.util.string.StringUtil;
import com.netease.nim.uikit.common.util.sys.ScreenUtil;

/**
 * 简单的带有输入框的对话框
 * <p>
 * Created by huangjun on 2015/5/28.股灾
 */
public class EasyEditDialog extends Dialog {

    private TextView mTitleTextView;

    private TextView mMessageTextView;

    private EditText mEdit;

    private TextView mLengthTextView;

    private Button mPositiveBtn;

    private Button mNegativeBtn;

    private int mResourceId;

    private View.OnClickListener mPositiveBtnListener;

    private View.OnClickListener mNegativeBtnListener;

    private String mTitle;

    private int mPositiveBtnStrResId = R.string.ok;

    private int mNegativeBtnStrResId = R.string.cancel;

    private String mMessage;

    private String mEditHint;

    private int mMaxEditTextLength;

    private int mMaxLines = 0;

    private boolean mSingleLine = false;

    private boolean mShowEditTextLength = false;

    private int inputType = -1;

    public EasyEditDialog(Context context, int resourceId, int style) {
        super(context, style);
        mMaxEditTextLength = 16;
        if (-1 != resourceId) {
            setContentView(resourceId);
            this.mResourceId = resourceId;
        }
        LayoutParams Params = getWindow().getAttributes();
        Params.width = LayoutParams.MATCH_PARENT;
        Params.height = LayoutParams.MATCH_PARENT;
        getWindow().setAttributes(Params);
    }

    public EasyEditDialog(Context context, int style) {
        this(context, -1, style);
        mResourceId = R.layout.nim_easy_alert_dialog_with_edit_text;
    }

    public EasyEditDialog(Context context) {
        this(context, R.style.sdk_share_dialog);
        mResourceId = R.layout.nim_easy_alert_dialog_with_edit_text;
    }

    public void setTitle(String title) {
        if (null != title) {
            this.mTitle = title;
            if (null != mTitleTextView)
                mTitleTextView.setText(title);
        }
    }

    public void setMessage(String message) {
        if (null != message) {
            this.mMessage = message;
            if (null != mMessageTextView)
                mMessageTextView.setText(message);
        }
    }

    public void setEditHint(String hint) {
        if (!TextUtils.isEmpty(hint)) {
            this.mEditHint = hint;
            if (null != mEdit) {
                mEdit.setHint(hint);
            }
        }
    }

    public void setInputType(int type) {
        this.inputType = type;
    }

    public void setEditTextMaxLength(int maxLength) {
        this.mMaxEditTextLength = maxLength;
        this.mShowEditTextLength = true;
    }

    public void setEditTextMaxLines(int maxLines) {
        this.mMaxLines = maxLines;
    }

    public void setEditTextSingleLine() {
        this.mSingleLine = true;
    }

    public void addPositiveButtonListener(View.OnClickListener positiveBtnListener) {
        this.mPositiveBtnListener = positiveBtnListener;
    }

    public void addPositiveButtonListener(int resId, View.OnClickListener positiveBtnListener) {
        this.mPositiveBtnStrResId = resId;
        this.mPositiveBtnListener = positiveBtnListener;
    }

    public void addNegativeButtonListener(View.OnClickListener negativeBtnListener) {
        this.mNegativeBtnListener = negativeBtnListener;
    }

    public void addNegativeButtonListener(int resId, View.OnClickListener negativeBtnListener) {
        this.mNegativeBtnStrResId = resId;
        this.mNegativeBtnListener = negativeBtnListener;
    }

    public int getResourceId() {
        return mResourceId;
    }

    public void setResourceId(int resourceId) {
        this.mResourceId = resourceId;
    }

    public Button getPositiveBtn() {
        return mPositiveBtn;
    }

    public Button getNegativeBtn() {
        return mNegativeBtn;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(mResourceId);

        try {
            LinearLayout root = (LinearLayout) findViewById(R.id.easy_edit_dialog_root);
            ViewGroup.LayoutParams params = root.getLayoutParams();
            params.width = (int) ScreenUtil.getDialogWidth();
            root.setLayoutParams(params);

            if (mTitle != null) {
                mTitleTextView = (TextView) findViewById(R.id.easy_dialog_title_text_view);
                mTitleTextView.setText(mTitle);
            }

            if (mMessage != null) {
                mMessageTextView = (TextView) findViewById(R.id.easy_dialog_message_text_view);
                mMessageTextView.setText(mMessage);
                mMessageTextView.setVisibility(View.VISIBLE);
            }

            mEdit = (EditText) findViewById(R.id.easy_alert_dialog_edit_text);
            mLengthTextView = (TextView) findViewById(R.id.edit_text_length);
            // mEdit.setFilters(new InputFilter[] { new InputFilter.LengthFilter(mMaxEditTextLength) });
            mLengthTextView.setVisibility(mShowEditTextLength ? View.VISIBLE : View.GONE);
            if (inputType != -1) {
                mEdit.setInputType(inputType);
            }
            mEdit.addTextChangedListener(new EditTextWatcher(mEdit, mLengthTextView, mMaxEditTextLength,
                    mShowEditTextLength));

            if (!TextUtils.isEmpty(mEditHint)) {
                mEdit.setHint(mEditHint);
            }
            if (mMaxLines > 0) {
                mEdit.setMaxLines(mMaxLines);
            }
            if (mSingleLine) {
                mEdit.setSingleLine();
            }

            mPositiveBtn = (Button) findViewById(R.id.easy_dialog_positive_btn);
            if (mPositiveBtnStrResId != 0) {
                mPositiveBtn.setText(mPositiveBtnStrResId);
            }
            mPositiveBtn.setOnClickListener(mPositiveBtnListener);

            mNegativeBtn = (Button) findViewById(R.id.easy_dialog_negative_btn);
            if (mNegativeBtnStrResId != 0) {
                mNegativeBtn.setText(mNegativeBtnStrResId);
            }
            mNegativeBtn.setOnClickListener(mNegativeBtnListener);
            mNegativeBtn.setVisibility(View.VISIBLE);
            findViewById(R.id.easy_dialog_btn_divide_view).setVisibility(View.VISIBLE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getEditMessage() {
        if (mEdit != null)
            return mEdit.getEditableText().toString();
        else return null;
    }

    public static class EditTextWatcher implements TextWatcher {

        private EditText editText;

        private TextView lengthTV;

        private int maxLength;

        private boolean show = false;

        public EditTextWatcher(EditText editText, TextView lengthTV, int maxLength, boolean show) {
            this.maxLength = maxLength;
            this.editText = editText;
            this.lengthTV = lengthTV;
            this.show = show;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (editText == null)
                return;
            int editStart = editText.getSelectionStart();
            int editEnd = editText.getSelectionEnd();
            editText.removeTextChangedListener(this);
            while (StringUtil.counterChars(s.toString()) > maxLength) {
                s.delete(editStart - 1, editEnd);
                editStart--;
                editEnd--;
            }
            editText.setSelection(editStart);
            editText.addTextChangedListener(this);
            if (show && lengthTV != null) {
                long remainLength = maxLength - StringUtil.counterChars(s.toString());
                lengthTV.setText("" + remainLength / 2);
                lengthTV.setVisibility(View.VISIBLE);
            }
        }
    }
}
