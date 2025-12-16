// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.common;

import android.text.TextUtils;
import com.netease.nimlib.coexist.sdk.v2.ai.model.V2NIMAIUser;
import com.netease.nimlib.coexist.sdk.v2.ai.params.V2NIMAIModelCallContent;
import com.netease.nimlib.coexist.sdk.v2.ai.params.V2NIMAIModelCallMessage;
import com.netease.nimlib.coexist.sdk.v2.message.V2NIMMessage;
import com.netease.nimlib.coexist.sdk.v2.message.V2NIMMessageCreator;
import com.netease.nimlib.coexist.sdk.v2.message.attachment.V2NIMMessageAudioAttachment;
import com.netease.nimlib.coexist.sdk.v2.message.attachment.V2NIMMessageFileAttachment;
import com.netease.nimlib.coexist.sdk.v2.message.attachment.V2NIMMessageImageAttachment;
import com.netease.nimlib.coexist.sdk.v2.message.attachment.V2NIMMessageLocationAttachment;
import com.netease.nimlib.coexist.sdk.v2.message.attachment.V2NIMMessageVideoAttachment;
import com.netease.nimlib.coexist.sdk.v2.message.config.V2NIMMessageConfig;
import com.netease.nimlib.coexist.sdk.v2.message.config.V2NIMMessagePushConfig;
import com.netease.nimlib.coexist.sdk.v2.message.enums.V2NIMMessageType;
import com.netease.nimlib.coexist.sdk.v2.message.params.V2NIMMessageAIConfigParams;
import com.netease.nimlib.coexist.sdk.v2.message.params.V2NIMSendMessageParams;
import com.netease.nimlib.coexist.sdk.v2.utils.V2NIMConversationIdUtil;
import com.netease.yunxin.kit.chatkit.IMKitConfigCenter;
import com.netease.yunxin.kit.chatkit.IMKitCustomFactory;
import com.netease.yunxin.kit.chatkit.manager.AIUserManager;
import java.util.List;

public class MessageCreator {

  //创建新的消息，只要消息体内容，其他都不需要
  // 主要用于收藏消息时，只保留内容即可
  public static V2NIMMessage createMessage(V2NIMMessage message) {
    if (message == null) {
      return null;
    }
    V2NIMMessage result = null;
    if (message.getMessageType() == V2NIMMessageType.V2NIM_MESSAGE_TYPE_TEXT) {
      result = V2NIMMessageCreator.createTextMessage(message.getText());
    } else if (message.getMessageType() == V2NIMMessageType.V2NIM_MESSAGE_TYPE_IMAGE) {
      V2NIMMessageImageAttachment attachment =
          (V2NIMMessageImageAttachment) message.getAttachment();
      if (attachment != null) {
        String path = attachment.getPath();
        if (TextUtils.isEmpty(path)) {
          path = attachment.getUrl();
        }
        result =
            V2NIMMessageCreator.createImageMessage(
                path,
                attachment.getName(),
                attachment.getSceneName(),
                attachment.getWidth(),
                attachment.getHeight());
        result.setAttachment(attachment);
      }

    } else if (message.getMessageType() == V2NIMMessageType.V2NIM_MESSAGE_TYPE_FILE) {
      V2NIMMessageFileAttachment attachment = (V2NIMMessageFileAttachment) message.getAttachment();
      if (attachment != null) {
        String path = attachment.getPath();
        if (TextUtils.isEmpty(path)) {
          path = attachment.getUrl();
        }
        result =
            V2NIMMessageCreator.createFileMessage(
                path, attachment.getName(), attachment.getSceneName());
        result.setAttachment(attachment);
      }

    } else if (message.getMessageType() == V2NIMMessageType.V2NIM_MESSAGE_TYPE_AUDIO) {
      V2NIMMessageAudioAttachment attachment =
          (V2NIMMessageAudioAttachment) message.getAttachment();
      if (attachment != null) {
        String path = attachment.getPath();
        if (TextUtils.isEmpty(path)) {
          path = attachment.getUrl();
        }
        result =
            V2NIMMessageCreator.createAudioMessage(
                path, attachment.getName(), attachment.getSceneName(), attachment.getDuration());
        result.setAttachment(attachment);
      }

    } else if (message.getMessageType() == V2NIMMessageType.V2NIM_MESSAGE_TYPE_VIDEO) {
      V2NIMMessageVideoAttachment attachment =
          (V2NIMMessageVideoAttachment) message.getAttachment();
      if (attachment != null) {
        String path = attachment.getPath();
        if (TextUtils.isEmpty(path)) {
          path = attachment.getUrl();
        }
        result =
            V2NIMMessageCreator.createVideoMessage(
                path,
                attachment.getName(),
                attachment.getSceneName(),
                attachment.getDuration(),
                attachment.getWidth(),
                attachment.getHeight());
        result.setAttachment(attachment);
      }

    } else if (message.getMessageType() == V2NIMMessageType.V2NIM_MESSAGE_TYPE_LOCATION) {
      V2NIMMessageLocationAttachment attachment =
          (V2NIMMessageLocationAttachment) message.getAttachment();
      if (attachment != null) {
        result =
            V2NIMMessageCreator.createLocationMessage(
                attachment.getLatitude(), attachment.getLongitude(), attachment.getAddress());
        result.setAttachment(attachment);
      }

    } else if (message.getMessageType() == V2NIMMessageType.V2NIM_MESSAGE_TYPE_CUSTOM) {
      result =
          V2NIMMessageCreator.createCustomMessage(
              message.getText(), message.getAttachment().getRaw());
      result.setAttachment(message.getAttachment());
    }
    if (result != null) {
      result.setText(message.getText());
    }
    return result;
  }

  public static V2NIMSendMessageParams createSendMessageParam(
      V2NIMMessage message,
      String conversationId,
      List<String> pushList,
      String remoteExtension,
      V2NIMAIUser aiAgent,
      List<V2NIMAIModelCallMessage> aiMessage,
      boolean needACK,
      boolean showRead) {
    V2NIMMessageConfig.V2NIMMessageConfigBuilder configBuilder =
        V2NIMMessageConfig.V2NIMMessageConfigBuilder.builder();
    configBuilder.withReadReceiptEnabled(needACK && showRead);
    V2NIMMessagePushConfig.V2NIMMessagePushConfigBuilder pushConfigBuilder =
        V2NIMMessagePushConfig.V2NIMMessagePushConfigBuilder.builder();
    if (pushList != null && !pushList.isEmpty()) {
      pushConfigBuilder.withForcePush(true).withForcePushAccountIds(pushList);
    }
    if (IMKitCustomFactory.getPushConfig() != null) {
      V2NIMMessagePushConfig pushConfig = IMKitCustomFactory.getPushConfig();
      pushConfigBuilder.withForcePush(pushConfig.isForcePush());
      pushConfigBuilder.withContent(pushConfig.getPushContent());
      pushConfigBuilder.withPushEnabled(pushConfig.isPushEnabled());
      pushConfigBuilder.withPayload(pushConfig.getPushPayload());
      pushConfigBuilder.withForcePushContent(pushConfig.getForcePushContent());
      pushConfigBuilder.withPushNickEnabled(pushConfig.isPushNickEnabled());
    }
    V2NIMSendMessageParams.V2NIMSendMessageParamsBuilder paramsBuilder =
        V2NIMSendMessageParams.V2NIMSendMessageParamsBuilder.builder()
            .withMessageConfig(configBuilder.build())
            .withPushConfig(pushConfigBuilder.build());

    //remoteExtension设置
    if (!TextUtils.isEmpty(remoteExtension)) {
      message.setServerExtension(remoteExtension);
    }

    //@ 代理设置
    V2NIMMessageAIConfigParams aiConfigParams = null;
    String chatId = V2NIMConversationIdUtil.conversationTargetId(conversationId);
    if (aiAgent == null && AIUserManager.isAIUser(chatId)) {
      aiAgent = AIUserManager.getAIUserById(chatId);
    }
    if (aiAgent != null) {
      aiConfigParams = new V2NIMMessageAIConfigParams(aiAgent.getAccountId());
      if (!TextUtils.isEmpty(MessageHelper.getAIContentMsg(message))) {
        V2NIMAIModelCallContent content =
            new V2NIMAIModelCallContent(MessageHelper.getAIContentMsg(message), 0);
        aiConfigParams.setContent(content);
      }
      boolean aiStream = IMKitConfigCenter.getEnableAIStream();
      aiConfigParams.setAIStream(aiStream);
    }
    //AI消息上下文设置
    if (aiConfigParams != null && aiMessage != null && !aiMessage.isEmpty()) {
      aiConfigParams.setMessages(aiMessage);
    }
    if (aiConfigParams != null) {
      paramsBuilder.withAIConfig(aiConfigParams);
    }
    return paramsBuilder.build();
  }
}
