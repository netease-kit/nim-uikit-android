/*
 * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.kit.conversationkit.ui.page;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.netease.yunxin.kit.conversationkit.ui.common.ConversationConstant;
import com.netease.yunxin.kit.conversationkit.ui.databinding.SelectorViewHolderLayoutBinding;
import com.netease.yunxin.kit.common.ui.viewholder.BaseViewHolder;
import com.netease.yunxin.kit.conversationkit.ui.model.ConversationBean;
import com.netease.yunxin.kit.conversationkit.ui.view.SelectorViewHolder;

/**
 * select fragment or activity view holder factory
 */
class SelectorViewHolderFactory extends DefaultViewHolderFactory {

    @Override
    public BaseViewHolder<ConversationBean> createViewHolder(@NonNull ViewGroup parent, int viewType){

        if(viewType == ConversationConstant.ViewType.CHAT_VIEW || viewType == ConversationConstant.ViewType.TEAM_VIEW){
            return new SelectorViewHolder(SelectorViewHolderLayoutBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false));
        }
        return null;

    }
}
