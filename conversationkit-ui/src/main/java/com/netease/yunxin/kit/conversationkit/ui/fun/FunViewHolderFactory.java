// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.conversationkit.ui.fun;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import com.netease.nimlib.sdk.v2.conversation.enums.V2NIMConversationType;
import com.netease.nimlib.sdk.v2.conversation.model.V2NIMConversation;
import com.netease.nimlib.sdk.v2.utils.V2NIMConversationIdUtil;
import com.netease.yunxin.kit.common.ui.viewholder.BaseViewHolder;
import com.netease.yunxin.kit.conversationkit.ui.IConversationFactory;
import com.netease.yunxin.kit.conversationkit.ui.common.ConversationConstant;
import com.netease.yunxin.kit.conversationkit.ui.databinding.FunConversationViewHolderBinding;
import com.netease.yunxin.kit.conversationkit.ui.fun.viewholder.FunConversationP2PViewHolder;
import com.netease.yunxin.kit.conversationkit.ui.fun.viewholder.FunConversationTeamViewHolder;
import com.netease.yunxin.kit.conversationkit.ui.model.ConversationBean;
import com.netease.yunxin.kit.corekit.im2.utils.RouterConstant;

/** 会话列表ViewHolder工厂，用于创建会话列表的ViewHolder */
public class FunViewHolderFactory implements IConversationFactory {

  // 创建会话列表的ViewHolder,用于设定会话列表的点击事件中的参数信息，跳转链接，跳转参数
  @Override
  public ConversationBean CreateBean(V2NIMConversation info) {
    ConversationBean bean = new ConversationBean(info);
    if (info.getType() == V2NIMConversationType.V2NIM_CONVERSATION_TYPE_P2P) {
      return new ConversationBean(
          info,
          RouterConstant.PATH_FUN_CHAT_P2P_PAGE,
          ConversationConstant.ViewType.CHAT_VIEW,
          RouterConstant.CHAT_ID_KRY,
          V2NIMConversationIdUtil.conversationTargetId(info.getConversationId()));
    } else if (info.getType() == V2NIMConversationType.V2NIM_CONVERSATION_TYPE_TEAM
        || info.getType() == V2NIMConversationType.V2NIM_CONVERSATION_TYPE_SUPER_TEAM) {
      return new ConversationBean(
          info,
          RouterConstant.PATH_FUN_CHAT_TEAM_PAGE,
          ConversationConstant.ViewType.TEAM_VIEW,
          RouterConstant.CHAT_ID_KRY,
          V2NIMConversationIdUtil.conversationTargetId(info.getConversationId()));
    }
    return bean;
  }

  @Override
  public int getItemViewType(ConversationBean data) {
    return data.viewType;
  }

  // 创建会话列表的ViewHolder,在ConversationAdapter中使用
  @Override
  public BaseViewHolder<ConversationBean> createViewHolder(
      @NonNull ViewGroup parent, int viewType) {
    FunConversationViewHolderBinding binding =
        FunConversationViewHolderBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false);
    if (viewType == ConversationConstant.ViewType.TEAM_VIEW) {
      return new FunConversationTeamViewHolder(binding);
    } else {
      return new FunConversationP2PViewHolder(binding);
    }
  }
}
