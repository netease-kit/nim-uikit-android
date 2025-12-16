// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.conversationkit.local.ui.normal;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import com.netease.nimlib.coexist.sdk.v2.conversation.enums.V2NIMConversationType;
import com.netease.nimlib.coexist.sdk.v2.conversation.model.V2NIMLocalConversation;
import com.netease.nimlib.coexist.sdk.v2.utils.V2NIMConversationIdUtil;
import com.netease.yunxin.kit.common.ui.viewholder.BaseViewHolder;
import com.netease.yunxin.kit.conversationkit.local.ui.ILocalConversationFactory;
import com.netease.yunxin.kit.conversationkit.local.ui.common.ConversationConstant;
import com.netease.yunxin.kit.conversationkit.local.ui.databinding.LocalConversationHeaderLayoutBinding;
import com.netease.yunxin.kit.conversationkit.local.ui.databinding.LocalConversationViewHolderBinding;
import com.netease.yunxin.kit.conversationkit.local.ui.model.ConversationBean;
import com.netease.yunxin.kit.conversationkit.local.ui.normal.viewholder.ConversationHeaderViewHolder;
import com.netease.yunxin.kit.conversationkit.local.ui.normal.viewholder.ConversationP2PViewHolder;
import com.netease.yunxin.kit.conversationkit.local.ui.normal.viewholder.ConversationTeamViewHolder;
import com.netease.yunxin.kit.corekit.coexist.im2.utils.RouterConstant;

/** ViewHolder工厂 根据会话类型创建对应的ViewHolder */
public class ViewHolderFactory implements ILocalConversationFactory {

  // 创建会话列表的ViewHolder,用于设定会话列表的点击事件中的参数信息，跳转链接，跳转参数
  @Override
  public ConversationBean CreateBean(V2NIMLocalConversation info) {
    ConversationBean bean = new ConversationBean(info);
    if (info.getType() == V2NIMConversationType.V2NIM_CONVERSATION_TYPE_P2P) {
      return new ConversationBean(
          info,
          RouterConstant.PATH_CHAT_P2P_PAGE,
          ConversationConstant.ViewType.CHAT_VIEW,
          RouterConstant.CHAT_ID_KRY,
          V2NIMConversationIdUtil.conversationTargetId(info.getConversationId()));
    } else if (info.getType() == V2NIMConversationType.V2NIM_CONVERSATION_TYPE_TEAM
        || info.getType() == V2NIMConversationType.V2NIM_CONVERSATION_TYPE_SUPER_TEAM) {
      return new ConversationBean(
          info,
          RouterConstant.PATH_CHAT_TEAM_PAGE,
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

  //创建会话列表的ViewHolder
  @Override
  public BaseViewHolder<ConversationBean> createViewHolder(
      @NonNull ViewGroup parent, int viewType) {

    if (viewType == ConversationConstant.ViewType.HORIZON_VIEW) {
      LocalConversationHeaderLayoutBinding viewBinding =
          LocalConversationHeaderLayoutBinding.inflate(
              LayoutInflater.from(parent.getContext()), parent, false);
      return new ConversationHeaderViewHolder(viewBinding);
    } else if (viewType == ConversationConstant.ViewType.TEAM_VIEW) {
      LocalConversationViewHolderBinding teamBinding =
          LocalConversationViewHolderBinding.inflate(
              LayoutInflater.from(parent.getContext()), parent, false);
      return new ConversationTeamViewHolder(teamBinding);
    } else {
      LocalConversationViewHolderBinding binding =
          LocalConversationViewHolderBinding.inflate(
              LayoutInflater.from(parent.getContext()), parent, false);
      return new ConversationP2PViewHolder(binding);
    }
  }
}
