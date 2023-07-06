// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.conversationkit.ui.normal;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.yunxin.kit.chatkit.model.ConversationInfo;
import com.netease.yunxin.kit.common.ui.viewholder.BaseViewHolder;
import com.netease.yunxin.kit.conversationkit.ui.IConversationFactory;
import com.netease.yunxin.kit.conversationkit.ui.common.ConversationConstant;
import com.netease.yunxin.kit.conversationkit.ui.databinding.ConversationViewHolderBinding;
import com.netease.yunxin.kit.conversationkit.ui.model.ConversationBean;
import com.netease.yunxin.kit.conversationkit.ui.normal.viewholder.ConversationP2PViewHolder;
import com.netease.yunxin.kit.conversationkit.ui.normal.viewholder.ConversationTeamViewHolder;
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant;

/** conversation view holder factory to create view holder in recyclerview */
public class ViewHolderFactory implements IConversationFactory {

  @Override
  public ConversationBean CreateBean(ConversationInfo info) {
    ConversationBean bean = new ConversationBean(info);
    if (info.getSessionType() == SessionTypeEnum.P2P) {
      return new ConversationBean(
          info,
          RouterConstant.PATH_CHAT_P2P_PAGE,
          ConversationConstant.ViewType.CHAT_VIEW,
          RouterConstant.CHAT_ID_KRY,
          info.getContactId());
    } else if (info.getSessionType() == SessionTypeEnum.Team
        || info.getSessionType() == SessionTypeEnum.SUPER_TEAM) {
      return new ConversationBean(
          info,
          RouterConstant.PATH_CHAT_TEAM_PAGE,
          ConversationConstant.ViewType.TEAM_VIEW,
          RouterConstant.CHAT_ID_KRY,
          info.getContactId());
    }
    return bean;
  }

  @Override
  public int getItemViewType(ConversationBean data) {
    return data.viewType;
  }

  @Override
  public BaseViewHolder<ConversationBean> createViewHolder(
      @NonNull ViewGroup parent, int viewType) {
    ConversationViewHolderBinding binding =
        ConversationViewHolderBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false);
    if (viewType == ConversationConstant.ViewType.TEAM_VIEW) {
      return new ConversationTeamViewHolder(binding);
    } else {
      return new ConversationP2PViewHolder(binding);
    }
  }
}
