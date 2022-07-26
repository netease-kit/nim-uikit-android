package com.netease.yunxin.kit.conversationkit.ui;

import android.content.Context;

import com.netease.yunxin.kit.conversationkit.ui.model.ConversationBean;

public interface ItemClickListener {
    default boolean onClick(Context context, ConversationBean data, int position){
        return false;
    }
    default boolean onLongClick(Context context,ConversationBean data,int position){
        return false;
    }
    default  boolean onAvatarClick(Context context, ConversationBean data, int position){
        return false;
    }
    default boolean onAvatarLongClick(Context context,ConversationBean data,int position){
        return false;
    }
}
