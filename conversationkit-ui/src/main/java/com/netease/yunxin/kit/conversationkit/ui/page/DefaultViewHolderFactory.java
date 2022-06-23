/*
 * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.kit.conversationkit.ui.page;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.yunxin.kit.conversationkit.model.ConversationInfo;
import com.netease.yunxin.kit.conversationkit.ui.IConversationFactory;
import com.netease.yunxin.kit.conversationkit.ui.common.ConversationConstant;
import com.netease.yunxin.kit.conversationkit.ui.databinding.P2pViewHolderLayoutBinding;
import com.netease.yunxin.kit.conversationkit.ui.databinding.TeamViewHolderLayoutBinding;
import com.netease.yunxin.kit.common.ui.viewholder.BaseViewHolder;
import com.netease.yunxin.kit.conversationkit.ui.model.ConversationBean;
import com.netease.yunxin.kit.conversationkit.ui.view.P2PViewHolder;
import com.netease.yunxin.kit.conversationkit.ui.view.TeamViewHolder;
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant;

/**
 * conversation view holder factory to create view holder in recyclerview
 */
public class DefaultViewHolderFactory implements IConversationFactory {

    @Override
    public ConversationBean CreateBean(ConversationInfo info) {
        ConversationBean bean = new ConversationBean(info);
        if (info.getSessionType() == SessionTypeEnum.P2P) {
            return new ConversationBean(info, RouterConstant.PATH_CHAT_P2P_PAGE, ConversationConstant.ViewType.CHAT_VIEW, RouterConstant.CHAT_KRY,
                    info.getUserInfo());
        } else if (info.getSessionType() == SessionTypeEnum.Team
                || info.getSessionType() == SessionTypeEnum.SUPER_TEAM) {
            return new ConversationBean(info, RouterConstant.PATH_CHAT_TEAM_PAGE, ConversationConstant.ViewType.TEAM_VIEW, RouterConstant.CHAT_KRY,
                    info.getTeamInfo());
        }
        return bean;
    }

    @Override
    public int getItemViewType(ConversationBean data) {
        return data.viewType;
    }

    @Override
    public BaseViewHolder<ConversationBean> createViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == ConversationConstant.ViewType.CHAT_VIEW) {
            return new P2PViewHolder(P2pViewHolderLayoutBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        } else if (viewType == ConversationConstant.ViewType.TEAM_VIEW) {
            return new TeamViewHolder(TeamViewHolderLayoutBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        }
        return null;

    }
}
