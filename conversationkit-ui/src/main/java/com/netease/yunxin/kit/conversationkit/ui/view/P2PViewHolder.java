/*
 * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.kit.conversationkit.ui.view;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.netease.yunxin.kit.common.ui.utils.AvatarColor;
import com.netease.yunxin.kit.common.ui.utils.TimeFormatUtils;
import com.netease.yunxin.kit.common.ui.viewholder.BaseViewHolder;
import com.netease.yunxin.kit.conversationkit.ui.R;
import com.netease.yunxin.kit.conversationkit.ui.databinding.P2pViewHolderLayoutBinding;
import com.netease.yunxin.kit.conversationkit.ui.model.ConversationBean;

public class P2PViewHolder extends BaseViewHolder<ConversationBean> {

    private P2pViewHolderLayoutBinding viewBinding;

    public P2PViewHolder(@NonNull ViewGroup itemView) {
        super(itemView);
    }

    public P2PViewHolder(@NonNull P2pViewHolderLayoutBinding binding) {
        this(binding.getRoot());
        viewBinding = binding;
    }

    @Override
    public void onBindData(ConversationBean data, int position) {
        viewBinding.avatarView.setData(data.infoData.getAvatar(),data.infoData.getName(), AvatarColor.avatarColor(data.infoData.getContactId()));
        viewBinding.conversationNameTv.setText(data.infoData.getName());
        if (data.infoData.isStickTop()){
            viewBinding.rootView.setBackground(viewBinding.getRoot().getContext().getDrawable(R.drawable.view_select_selector));
        }else {
            viewBinding.rootView.setBackground(viewBinding.getRoot().getContext().getDrawable(R.drawable.view_normal_selector));
        }
        if (data.infoData.getMute()){
            viewBinding.conversationMuteIv.setVisibility(View.VISIBLE);
        }else {
            viewBinding.conversationMuteIv.setVisibility(View.GONE);
            if (data.infoData.getUnreadCount() > 0){
                int count = data.infoData.getUnreadCount();
                String content;
                if (count >= 100) {
                    content = "99+";
                } else {
                    content = String.valueOf(count);
                }
                viewBinding.conversationUnreadTv.setText(content);
                viewBinding.conversationUnreadTv.setVisibility(View.VISIBLE);
            }else {
                viewBinding.conversationUnreadTv.setVisibility(View.GONE);
            }
        }
        viewBinding.conversationMessageTv.setText(data.infoData.getContent());
        viewBinding.conversationTime.setText(TimeFormatUtils.formatMillisecond(viewBinding.getRoot().getContext(), data.infoData.getTime()));
        viewBinding.rootView.setOnClickListener( v -> itemListener.onClick(data,position));
        viewBinding.rootView.setOnLongClickListener(v -> itemListener.onLongClick(data,position));
    }

}
