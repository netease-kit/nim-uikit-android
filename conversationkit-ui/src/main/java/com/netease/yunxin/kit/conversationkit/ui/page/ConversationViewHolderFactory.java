/*
 * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.kit.conversationkit.ui.page;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.netease.yunxin.kit.conversationkit.ui.common.ConversationConstant;
import com.netease.yunxin.kit.common.ui.viewholder.IViewHolderFactory;
import com.netease.yunxin.kit.conversationkit.ui.databinding.P2pViewHolderLayoutBinding;
import com.netease.yunxin.kit.conversationkit.ui.databinding.TeamViewHolderLayoutBinding;
import com.netease.yunxin.kit.common.ui.viewholder.BaseViewHolder;
import com.netease.yunxin.kit.conversationkit.ui.view.P2PViewHolder;
import com.netease.yunxin.kit.conversationkit.ui.view.TeamViewHolder;

/**
 * conversation view holder factory to create view holder in recyclerview
 */
public class ConversationViewHolderFactory implements IViewHolderFactory {

    public BaseViewHolder createViewHolder(@NonNull ViewGroup parent, int viewType){

        if(viewType == ConversationConstant.ViewType.CHAT_VIEW){
            return new P2PViewHolder(P2pViewHolderLayoutBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false));
        }else if (viewType == ConversationConstant.ViewType.TEAM_VIEW){
            return new TeamViewHolder(TeamViewHolderLayoutBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false));
        }
        return null;

    }
}
