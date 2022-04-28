package com.netease.yunxin.kit.chatkit.ui.common;

import androidx.annotation.Nullable;

import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import com.netease.yunxin.kit.corekit.im.provider.FetchCallback;

public class ChatCallback<T> implements FetchCallback<T> {

    boolean showSuccess;

    public ChatCallback<T> setShowSuccess(boolean showSuccess) {
        this.showSuccess = showSuccess;
        return this;
    }

    public ChatCallback() {

    }

    @Override
    public void onSuccess(@Nullable T param) {
        if (showSuccess) {
            ToastX.showShortToast(R.string.chat_server_request_success);
        }
    }

    @Override
    public void onFailed(int code) {
        ToastX.showErrorToast(code);
    }

    @Override
    public void onException(@Nullable Throwable exception) {
        ToastX.showShortToast(R.string.chat_server_request_fail);
    }
}
