package com.netease.yunxin.kit.conversationkit.ui;

import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.netease.yunxin.kit.common.ui.viewholder.BaseViewHolder;
import com.netease.yunxin.kit.conversationkit.model.ConversationInfo;
import com.netease.yunxin.kit.conversationkit.ui.model.ConversationBean;

public interface IConversationFactory {
    //根据会话数据，创建ViewHolder中数据类
    ConversationBean CreateBean(ConversationInfo info);

    //Adapter获取数据对应的ViewType
    int getItemViewType(ConversationBean data);

    //创建ViewHolder
    BaseViewHolder<ConversationBean> createViewHolder(@NonNull ViewGroup parent, int viewType);
}
