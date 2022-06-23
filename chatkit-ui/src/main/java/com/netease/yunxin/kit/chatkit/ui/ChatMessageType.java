/*
 * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.kit.chatkit.ui;

import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum;

public interface ChatMessageType {

    /**
     * normal message type
     */
    int NORMAL_MESSAGE_VIEW_TYPE_TEXT = MsgTypeEnum.text.getValue();

    int NORMAL_MESSAGE_VIEW_TYPE_IMAGE = MsgTypeEnum.image.getValue();

    int NORMAL_MESSAGE_VIEW_TYPE_AUDIO = MsgTypeEnum.audio.getValue();
    
    int NORMAL_MESSAGE_VIEW_TYPE_VIDEO = MsgTypeEnum.video.getValue();

    /**
     * notice message type
     */
    int NOTICE_MESSAGE_VIEW_TYPE = MsgTypeEnum.notification.getValue();

    /**
     * tip message type
     */
    int TIP_MESSAGE_VIEW_TYPE = MsgTypeEnum.tip.getValue();

    /**
     * 自定义消息类型从1000开始
     */
    int CUSTOM_START = 1000;

    /**
     * 自定义消息贴图
     */
    int CUSTOM_STICKER = 1001;
}
