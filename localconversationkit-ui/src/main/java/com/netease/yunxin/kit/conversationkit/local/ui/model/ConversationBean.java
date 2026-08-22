// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.conversationkit.local.ui.model;

import android.text.TextUtils;
import com.netease.nimlib.sdk.v2.ai.model.V2NIMAIUser;
import com.netease.nimlib.sdk.v2.ai.model.V2NIMUserAIBot;
import com.netease.nimlib.sdk.v2.conversation.enums.V2NIMConversationType;
import com.netease.nimlib.sdk.v2.conversation.model.V2NIMLocalConversation;
import com.netease.nimlib.sdk.v2.utils.V2NIMConversationIdUtil;
import com.netease.yunxin.kit.chatkit.cache.FriendUserCache;
import com.netease.yunxin.kit.chatkit.manager.AIUserManager;
import com.netease.yunxin.kit.chatkit.manager.UserAIBotManager;
import com.netease.yunxin.kit.common.ui.viewholder.BaseBean;
import com.netease.yunxin.kit.corekit.im2.model.UserWithFriend;
import java.util.Objects;

/** 会话列表数据Bean，封装会话数据，主要用于UI展示 */
public class ConversationBean extends BaseBean {
  private static final String TAG = "ConversationBean";
  // 会话信息
  public V2NIMLocalConversation infoData;
  // 是否置顶
  private Boolean stickTop;

  // 会话ID
  private String targetId = "";

  public ConversationBean(V2NIMLocalConversation data) {
    infoData = data;
  }

  public ConversationBean(V2NIMLocalConversation data, String router, int viewType) {
    this(data, router, viewType, null, null);
  }

  /**
   * @param data 会话数据
   * @param router 会话跳转的路由地址
   * @param viewType 会话展示的View类型
   * @param paramKey 跳转需要传递的参数KEY
   * @param paramValue 跳转需要传递的参数Value
   */
  public ConversationBean(
      V2NIMLocalConversation data,
      String router,
      int viewType,
      String paramKey,
      Object paramValue) {
    infoData = data;
    this.router = router;
    this.viewType = viewType;
    this.paramKey = paramKey;
    this.param = paramValue;
  }

  public String getConversationId() {
    return infoData != null ? infoData.getConversationId() : "";
  }

  public Long getSortOrder() {
    return infoData != null ? infoData.getSortOrder() : 0L;
  }

  public Boolean isStickTop() {
    if (stickTop == null) {
      stickTop = infoData.isStickTop();
    }
    return stickTop;
  }

  public void setStickTop(Boolean stickTop) {
    this.stickTop = stickTop;
  }

  /**
   * 获取会话的最后一条消息时间
   *
   * @return 最后一条消息时间
   */
  public long getLastMsgTime() {
    if (infoData.getLastMessage() == null || infoData.getLastMessage().getMessageRefer() == null) {
      return infoData.getUpdateTime();
    }
    return infoData.getLastMessage().getMessageRefer().getCreateTime();
  }

  public String getConversationName() {
    String cachedName = getCachedP2PName();
    if (!TextUtils.isEmpty(cachedName)) {
      return cachedName;
    }
    if (infoData != null && !TextUtils.isEmpty(infoData.getName())) {
      return infoData.getName();
    }
    return getTargetId();
  }

  /**
   * 获取会话的目标ID
   *
   * @return 会话目标ID，P2P返回对方的ID，群组返回群ID
   */
  public String getTargetId() {
    if (TextUtils.isEmpty(targetId) && infoData != null) {
      targetId = V2NIMConversationIdUtil.conversationTargetId(infoData.getConversationId());
    }

    return targetId;
  }

  public V2NIMConversationType getConversationType() {
    return this.infoData.getType();
  }

  /**
   * 获取会话的名称，用于展示默认头像上的文字
   *
   * @return 会话名称，P2P返回对方的昵称，群组返回群名称，如果名称为空返回会话ID
   */
  public String getAvatarName() {
    if (infoData != null
        && infoData.getType() == V2NIMConversationType.V2NIM_CONVERSATION_TYPE_P2P) {
      String cachedName = getCachedP2PName();
      if (!TextUtils.isEmpty(cachedName)) {
        return cachedName;
      }
      UserWithFriend friendInfo = FriendUserCache.getFriendByAccount(getTargetId());
      if (friendInfo != null) {
        return friendInfo.getAvatarName();
      }
    }
    return getConversationName();
  }

  public String getConversationAvatar() {
    if (infoData == null) {
      return null;
    }
    if (infoData.getType() == V2NIMConversationType.V2NIM_CONVERSATION_TYPE_P2P) {
      V2NIMUserAIBot bot = UserAIBotManager.getBotById(getTargetId());
      if (bot != null && !TextUtils.isEmpty(bot.getIcon())) {
        return bot.getIcon();
      }
      V2NIMAIUser aiUser = AIUserManager.getAIUserById(getTargetId());
      if (aiUser != null && !TextUtils.isEmpty(aiUser.getAvatar())) {
        return aiUser.getAvatar();
      }
    }
    return infoData.getAvatar();
  }

  private String getCachedP2PName() {
    if (infoData == null
        || infoData.getType() != V2NIMConversationType.V2NIM_CONVERSATION_TYPE_P2P) {
      return null;
    }
    V2NIMUserAIBot bot = UserAIBotManager.getBotById(getTargetId());
    if (bot != null && !TextUtils.isEmpty(bot.getName())) {
      return bot.getName();
    }
    V2NIMAIUser aiUser = AIUserManager.getAIUserById(getTargetId());
    return aiUser == null ? null : aiUser.getName();
  }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ConversationBean)) {
      return false;
    } else {
      ConversationBean that = (ConversationBean) o;
      return Objects.equals(infoData.getConversationId(), that.infoData.getConversationId());
    }
  }

  @Override
  public int hashCode() {
    return Objects.hash(infoData.getConversationId());
  }
}
