// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.common;

import com.netease.nimlib.sdk.v2.ai.model.V2NIMAIUser;
import com.netease.nimlib.sdk.v2.ai.params.V2NIMAIModelCallMessage;
import com.netease.nimlib.sdk.v2.message.V2NIMMessage;
import com.netease.nimlib.sdk.v2.message.V2NIMMessageCreator;
import com.netease.nimlib.sdk.v2.message.enums.V2NIMMessageQueryDirection;
import com.netease.nimlib.sdk.v2.message.option.V2NIMMessageListOption;
import com.netease.nimlib.sdk.v2.message.params.V2NIMSendMessageParams;
import com.netease.yunxin.kit.chatkit.model.IMMessageInfo;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.common.utils.ImageUtils;
import com.netease.yunxin.kit.corekit.im2.model.IMMessageProgress;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.json.JSONObject;

public final class MessageParamBuildUtils {
  private MessageParamBuildUtils() {}

  public static V2NIMMessage createImageMessage(File imageFile) {
    return createImageMessage(imageFile, imageFile.getName(), 0, 0);
  }

  public static V2NIMMessage createImageMessage(
      File imageFile, String filename, int width, int height) {
    if (width == 0 || height == 0) {

      int[] bounds = ImageUtils.getSize(imageFile);
      width = bounds[0];
      height = bounds[1];
    }
    V2NIMMessage imageMessage =
        V2NIMMessageCreator.createImageMessage(imageFile.getPath(), filename, null, width, height);
    imageMessage.setText(filename);
    return imageMessage;
  }

  public static V2NIMMessage createVideoMessage(
      String path, String name, int duration, int width, int height) {
    V2NIMMessage msg =
        V2NIMMessageCreator.createVideoMessage(path, name, null, duration, width, height);
    msg.setText(name);
    return msg;
  }

  public static V2NIMMessage createFileMessage(File docsFile, String displayName) {
    V2NIMMessage msg = V2NIMMessageCreator.createFileMessage(docsFile.getPath(), displayName, null);
    msg.setText(displayName);
    return msg;
  }

  public static String toJson(Map<String, Object> data) {
    if (data == null) {
      return null;
    }
    JSONObject jsonObject = new JSONObject(data);
    return jsonObject.toString();
  }

  public static V2NIMSendMessageParams buildSendParams(
      V2NIMMessage message,
      String conversationId,
      List<String> pushList,
      String remoteExtension,
      V2NIMAIUser aiAgent,
      List<V2NIMAIModelCallMessage> aiMessage,
      boolean showRead) {
    return MessageCreator.createSendMessageParam(
        message, conversationId, pushList, remoteExtension, aiAgent, aiMessage, showRead);
  }

  public static V2NIMMessageListOption buildMessageOptions(
      V2NIMMessage anchor,
      long startTime,
      String conversationId,
      int messagePageSize,
      V2NIMMessageQueryDirection direction) {
    V2NIMMessageListOption.V2NIMMessageListOptionBuilder optionBuilder =
        V2NIMMessageListOption.V2NIMMessageListOptionBuilder.builder(conversationId)
            .withLimit(messagePageSize)
            .withDirection(direction);

    if (anchor != null) {
      optionBuilder.withAnchorMessage(anchor);
      if (direction
          == com.netease.nimlib.sdk.v2.message.enums.V2NIMMessageQueryDirection
              .V2NIM_QUERY_DIRECTION_DESC) {
        optionBuilder.withEndTime(anchor.getCreateTime());
      } else {
        optionBuilder.withBeginTime(anchor.getCreateTime());
      }
    }

    if (startTime > 0) {
      if (direction
          == com.netease.nimlib.sdk.v2.message.enums.V2NIMMessageQueryDirection
              .V2NIM_QUERY_DIRECTION_DESC) {
        optionBuilder.withEndTime(startTime);
      } else {
        optionBuilder.withBeginTime(startTime);
      }
    }

    return optionBuilder.build();
  }

  public static FetchResult<IMMessageProgress> buildAttachmentProgress(
      String clientId, int progress) {
    FetchResult<IMMessageProgress> result = new FetchResult<>(LoadStatus.Success);
    result.setData(new IMMessageProgress(clientId, progress));
    result.setType(FetchResult.FetchType.Update);
    result.setTypeIndex(-1);
    return result;
  }

  public static List<ChatMessageBean> convertToChatBeans(List<IMMessageInfo> messageList) {
    if (messageList == null) {
      return null;
    }
    ArrayList<ChatMessageBean> result = new ArrayList<>(messageList.size());
    for (IMMessageInfo message : messageList) {
      result.add(new ChatMessageBean(message));
    }
    return result;
  }
}
