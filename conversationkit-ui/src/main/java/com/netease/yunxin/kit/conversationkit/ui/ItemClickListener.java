package com.netease.yunxin.kit.conversationkit.ui;

import android.content.Context;

import com.netease.yunxin.kit.conversationkit.ui.model.ConversationBean;

public interface ItemClickListener {
    void onClick(Context context, ConversationBean data, int position);
    boolean onLongClick(Context context,ConversationBean data,int position);
}
