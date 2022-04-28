package com.netease.yunxin.kit.chatkit.ui;

import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum;
import com.netease.yunxin.kit.chatkit.ui.custom.CustomAttachmentType;

public interface ChatMessageType {

    /**
     * normal message type
     */
    int NORMAL_MESSAGE_VIEW_TYPE_TEXT = MsgTypeEnum.text.getValue();

    int NORMAL_MESSAGE_VIEW_TYPE_IMAGE = MsgTypeEnum.image.getValue();

    int NORMAL_MESSAGE_VIEW_TYPE_AUDIO = MsgTypeEnum.audio.getValue();
    
    int NORMAL_MESSAGE_VIEW_TYPE_VIDEO = MsgTypeEnum.video.getValue();

    int CUSTOM_MESSAGE_VIEW_TYPE_STICKER = CustomAttachmentType.CustomStart + CustomAttachmentType.Sticker;

    /**
     * notice message type
     */
    int NOTICE_MESSAGE_VIEW_TYPE = MsgTypeEnum.notification.getValue();

    /**
     * tip message type
     */
    int TIP_MESSAGE_VIEW_TYPE = MsgTypeEnum.tip.getValue();
}
