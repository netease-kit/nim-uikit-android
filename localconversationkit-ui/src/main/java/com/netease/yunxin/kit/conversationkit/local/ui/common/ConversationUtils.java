// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.conversationkit.local.ui.common;

import static com.netease.yunxin.kit.conversationkit.local.ui.common.ConversationConstant.LIB_TAG;

import android.content.Context;
import android.text.TextUtils;
import com.netease.nimlib.sdk.v2.conversation.model.V2NIMLastMessage;
import com.netease.nimlib.sdk.v2.conversation.model.V2NIMLocalConversation;
import com.netease.nimlib.sdk.v2.message.attachment.V2NIMMessageAttachment;
import com.netease.nimlib.sdk.v2.message.attachment.V2NIMMessageNotificationAttachment;
import com.netease.nimlib.sdk.v2.message.enums.V2NIMMessageNotificationType;
import com.netease.nimlib.sdk.v2.message.enums.V2NIMMessageType;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.conversationkit.local.ui.LocalConversationCustom;
import com.netease.yunxin.kit.conversationkit.local.ui.LocalConversationKitClient;
import com.netease.yunxin.kit.conversationkit.local.ui.model.ConversationBean;
import com.netease.yunxin.kit.corekit.im2.IMKitClient;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONObject;

/** 会话工具类 */
public class ConversationUtils {

  private static final String TAG = "ConversationUtils";

  // 会话自定义，用于获取会话最近一条消息内容
  private static final LocalConversationCustom custom = new LocalConversationCustom();

  /**
   * 获取会话最近一条消息内容 可以通过设置{@link LocalConversationKitClient#setConversationUIConfig}来自定义会话最近一条消息内容
   *
   * @param context 上下文
   * @param conversationInfo 会话信息
   * @return 会话最近一条消息内容
   */
  public static CharSequence getConversationText(
      Context context, V2NIMLocalConversation conversationInfo) {
    if (LocalConversationKitClient.getConversationUIConfig() != null
        && LocalConversationKitClient.getConversationUIConfig().conversationCustom != null) {
      return LocalConversationKitClient.getConversationUIConfig()
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
  /**
   * 获取会话列表的排序比较器 用于对ConversationBean列表进行排序，排序规则如下： 1. 优先按置顶状态排序：置顶会话排在非置顶会话之前 2.
   * 置顶状态相同的情况下，按最后一条消息时间戳降序排序（最新消息的会话排在前面） 3. 时间戳相同则按会话ID排序，确保排序稳定性 4.
   * 处理null对象：若其中一个对象为null，非null对象排在前面
   *
   * @return 用于排序ConversationBean的Comparator实例
   */
  public static Comparator<ConversationBean> getConversationComparator() {
    // 会话排序规则：置顶优先 > 最新消息优先 > 会话ID排序 > null安全处理
    return (bean1, bean2) -> {
      int result;
      // 若bean1为null，bean2排在前面
      if (bean1 == null) {
        result = 1;
      }
      // 若bean2为null，bean1排在前面
      else if (bean2 == null) {
        result = -1;
      }
      // 置顶状态不同，置顶会话排在前面
      else if (bean1.isStickTop() != bean2.isStickTop()) {
        result = bean1.isStickTop() ? -1 : 1;
      }
      // 置顶状态相同，按最后消息时间戳降序排序
      else {
        long timeDiff = bean1.getLastMsgTime() - bean2.getLastMsgTime();
        if (timeDiff != 0) {
          result = timeDiff > 0 ? -1 : 1;
        } else {
          // 时间戳相同则按会话ID排序，确保排序稳定性
          result = bean1.getConversationId().compareTo(bean2.getConversationId());
        }
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

  /**
   * 转换JSONArray为HashSet
   *
   * @param jsonArray
   * @return
   */
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
