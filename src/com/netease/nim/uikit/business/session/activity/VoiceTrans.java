package com.netease.nim.uikit.business.session.activity;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.netease.nim.uikit.R;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.netease.nimlib.sdk.AbortableFuture;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.attachment.AudioAttachment;
import com.netease.nimlib.sdk.msg.model.IMMessage;

/**
 * Created by hzxuwen on 2015/7/9.
 */
public class VoiceTrans {
    private static final String TAG = VoiceTrans.class.getSimpleName();

    private final Activity baseActivity;

    // view
    private View textLayout;
    private TextView voiceTransText;
    private View cancelBtn;
    private ProgressBar refreshingIndicator;
    private View failIcon;

    private AbortableFuture<String> callFuture;

    public VoiceTrans(Activity baseActivity) {
        this.baseActivity = baseActivity;
        findViews();
        setListener();
    }

    private void hideKeyBoard() {
        InputMethodManager imm = (InputMethodManager) baseActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (baseActivity.getCurrentFocus() != null) {
            imm.hideSoftInputFromWindow(baseActivity.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    private void findViews() {
        textLayout = baseActivity.findViewById(R.id.voice_trans_layout);
        if (textLayout == null) {
            LayoutInflater layoutInflater = LayoutInflater.from(baseActivity);
            textLayout = layoutInflater.inflate(R.layout.nim_voice_trans_layout, null);
            ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            baseActivity.addContentView(textLayout, layoutParams);
        }
        voiceTransText = (TextView) textLayout.findViewById(R.id.voice_trans_text);
        cancelBtn = textLayout.findViewById(R.id.cancel_btn);
        refreshingIndicator = (ProgressBar) textLayout.findViewById(R.id.refreshing_indicator);
        failIcon = textLayout.findViewById(R.id.trans_fail_icon);
    }

    private void setListener() {
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hide();
            }
        });
        textLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hide();
            }
        });
    }

    public void hide() {
        if (callFuture != null) {
            callFuture.abort();
        }
        voiceTransText.scrollTo(0, 0);
        textLayout.setVisibility(View.GONE);
    }

    public void show() {
        hideKeyBoard();
        textLayout.setVisibility(View.VISIBLE);
        voiceTransText.setText("正在转换");
    }

    public boolean isShow() {
        return textLayout.getVisibility() == View.VISIBLE;
    }

    public void voiceToText(IMMessage msg) {
        AudioAttachment attachment = (AudioAttachment) msg.getAttachment();
        String voiceUrl = attachment.getUrl();
        String path = attachment.getPath();
        refreshStartUI();
        callFuture = NIMClient.getService(MsgService.class).transVoiceToText(voiceUrl, path, attachment.getDuration());
        callFuture.setCallback(new RequestCallback<String>() {
            @Override
            public void onSuccess(String param) {
                voiceTransText.setText(param);
                updateUI();
            }

            @Override
            public void onFailed(int code) {
                LogUtil.e(TAG, "voice to text failed, code=" + code);
                voiceTransText.setText(R.string.trans_voice_failed);
                failIcon.setVisibility(View.VISIBLE);
                updateUI();
            }

            @Override
            public void onException(Throwable exception) {
                LogUtil.e(TAG, "voice to text throw exception, e=" + exception.getMessage());
                voiceTransText.setText("参数错误");
                failIcon.setVisibility(View.VISIBLE);
                updateUI();
            }
        });
        show();
    }

    private void refreshStartUI() {
        failIcon.setVisibility(View.GONE);
        cancelBtn.setVisibility(View.VISIBLE);
        refreshingIndicator.setVisibility(View.VISIBLE);
    }

    private void updateUI() {
        refreshingIndicator.setVisibility(View.GONE);
        cancelBtn.setVisibility(View.GONE);
    }
}
