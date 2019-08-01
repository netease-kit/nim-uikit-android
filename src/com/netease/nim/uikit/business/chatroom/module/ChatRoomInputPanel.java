package com.netease.nim.uikit.business.chatroom.module;

import android.view.View;

import com.netease.nim.uikit.business.session.actions.BaseAction;
import com.netease.nim.uikit.business.session.module.Container;
import com.netease.nim.uikit.business.session.module.input.InputPanel;
import com.netease.nimlib.sdk.chatroom.ChatRoomMessageBuilder;
import com.netease.nimlib.sdk.msg.model.IMMessage;

import java.util.List;

/**
 * Created by huangjun on 2017/9/18.
 */
public class ChatRoomInputPanel extends InputPanel {

    public ChatRoomInputPanel(Container container, View view, List<BaseAction> actions, boolean isTextAudioSwitchShow) {
        super(container, view, actions, isTextAudioSwitchShow);
    }

    @Override
    protected IMMessage createTextMessage(String text) {
        return ChatRoomMessageBuilder.createChatRoomTextMessage(container.account, text);
    }
}
