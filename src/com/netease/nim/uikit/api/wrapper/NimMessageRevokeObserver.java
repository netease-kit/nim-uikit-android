package com.netease.nim.uikit.api.wrapper;

import com.netease.nim.uikit.business.session.helper.MessageHelper;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.msg.model.IMMessage;

/**
 * 云信消息撤回观察者
 */

public class NimMessageRevokeObserver implements Observer<IMMessage> {

    @Override
    public void onEvent(IMMessage message) {
        if (message == null) {
            return;
        }

        MessageHelper.getInstance().onRevokeMessage(message);
    }
}
