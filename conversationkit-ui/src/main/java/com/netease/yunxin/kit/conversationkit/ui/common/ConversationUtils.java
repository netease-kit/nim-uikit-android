// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.conversationkit.ui.common;

import static com.netease.yunxin.kit.conversationkit.ui.common.ConversationConstant.LIB_TAG;

import android.content.Context;
import android.text.TextUtils;
import com.netease.nimlib.coexist.sdk.v2.conversation.model.V2NIMConversation;
import com.netease.nimlib.coexist.sdk.v2.conversation.model.V2NIMLastMessage;
import com.netease.nimlib.coexist.sdk.v2.message.attachment.V2NIMMessageAttachment;
import com.netease.nimlib.coexist.sdk.v2.message.attachment.V2NIMMessageNotificationAttachment;
import com.netease.nimlib.coexist.sdk.v2.message.enums.V2NIMMessageNotificationType;
import com.netease.nimlib.coexist.sdk.v2.message.enums.V2NIMMessageType;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.conversationkit.ui.ConversationCustom;
import com.netease.yunxin.kit.conversationkit.ui.ConversationKitClient;
import com.netease.yunxin.kit.conversationkit.ui.model.ConversationBean;
import com.netease.yunxin.kit.corekit.coexist.im2.IMKitClient;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONObject;

/** 会话工具类 */
public class ConversationUtils {

  private static final String TAG = "ConversationUtils";

  // 会话自定义，用于获取会话最近一条消息内容
  private static final ConversationCustom custom = new ConversationCustom();

  /**
   * 获取会话最近一条消息内容 可以通过设置{@link ConversationKitClient#setConversationUIConfig}来自定义会话最近一条消息内容
   *
   * @param context 上下文
   * @param conversationInfo 会话信息
   * @return 会话最近一条消息内容
   */
  public static CharSequence getConversationText(
      Context context, V2NIMConversation conversationInfo) {
    if (ConversationKitClient.getConversationUIConfig() != null
        && ConversationKitClient.getConversationUIConfig().conversationCustom != null) {
      return ConversationKitClient.getConversationUIConfig()
          .conversationCustom
          .customContentText(context, conversationInfo);
    }
    return custom.customContentText(context, conversationInfo);
  }

  /**
   * 获取话单消息的通话类型
   *
   * @param attachment 消息附件
   * @return 通话类型
   */
  public static int getMessageCallType(V2NIMMessageAttachment attachment) {
    // 此处只处理话单消息
    int callType = 0;
    if (attachment == null) {
      return callType;
    }
    String attachmentStr = attachment.getRaw();
    ALog.d(LIB_TAG, "ConversationUtils", "getMessageCallType: ");
    if (!TextUtils.isEmpty(attachmentStr)) {
      try {
        JSONObject dataJson = new JSONObject(attachmentStr);
        // 音频/视频 类型通话
        callType = dataJson.getInt("type");
      } catch (Exception e) {
        ALog.e(LIB_TAG, "ConversationUtils", "getMessageCallType: " + callType);
      }
    }
    return callType;
  }

  /**
   * 是否为离开群的消息，包括群解散和个人退出
   *
   * @param lastMessage 消息体
   * @return 是否为离开群的消息
   */
  public static boolean isDismissTeamMsg(V2NIMLastMessage lastMessage) {
    if (lastMessage != null
        && lastMessage.getMessageType() == V2NIMMessageType.V2NIM_MESSAGE_TYPE_NOTIFICATION) {
      V2NIMMessageNotificationAttachment attachment =
          (V2NIMMessageNotificationAttachment) lastMessage.getAttachment();
      ALog.d(LIB_TAG, TAG, "isDismissTeamMsg:" + attachment.getType());
      boolean result =
          attachment.getType()
              == V2NIMMessageNotificationType.V2NIM_MESSAGE_NOTIFICATION_TYPE_TEAM_DISMISS;
      if (attachment.getType()
              == V2NIMMessageNotificationType.V2NIM_MESSAGE_NOTIFICATION_TYPE_TEAM_LAVE
          && TextUtils.equals(lastMessage.getMessageRefer().getSenderId(), IMKitClient.account())) {
        result = true;
      }
      return result;
    }
    return false;
  }

  public static Comparator<ConversationBean> getConversationComparator() {
    // 会话排序规则

    return (bean1, bean2) -> {
      int result;
      if (bean1 == null) {
        result = 1;
      } else if (bean2 == null) {
        result = -1;
      } else if (bean1.isStickTop() == bean2.isStickTop()) {
        long time = bean1.getLastMsgTime() - bean2.getLastMsgTime();
        result = time == 0L ? 0 : (time > 0 ? -1 : 1);
      } else {
        result = bean1.isStickTop() ? -1 : 1;
      }
      return result;
    };
  }

  public static Comparator<ConversationBean> getConversationSortOrderComparator() {
    return new Comparator<ConversationBean>() {
      @Override
      public int compare(ConversationBean bean1, ConversationBean bean2) {
        // 避免空指针异常（若 sortOrder 可能为 null）
        long order1 = bean1.getSortOrder();
        long order2 = bean2.getSortOrder();

        // 升序排序：order1 - order2（若需降序则反过来：order2 - order1）
        return Long.compare(order2, order1);
      }
    };
  }

  public static Set<String> toHashSet(JSONArray jsonArray) {
    if (jsonArray == null) {
      return null;
    }
    Set<String> set = new HashSet<>();
    for (int i = 0; i < jsonArray.length(); i++) {
      set.add(jsonArray.optString(i));
    }
    return set;
  }
}
