package com.netease.yunxin.kit.chatkit.ui.view.interfaces;

import com.netease.nimlib.sdk.msg.attachment.MsgAttachment;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;

import java.io.File;

/**
 * handle message event in Chat page
 */
public interface IMessageProxy {
    boolean sendTextMessage(String msg, ChatMessageBean replyMsg);

    void pickMedia();

    void takePicture();

    void captureVideo();

    boolean sendFile(ChatMessageBean replyMsg);

    boolean sendAudio(File audioFile, long audioLength, ChatMessageBean replyMsg);

    boolean sendCustomMessage(MsgAttachment attachment, String content);

    void onTypeStateChange(boolean isTyping);
}
