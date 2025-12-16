// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.conversationkit.local.ui;

import android.view.ViewGroup;
import androidx.annotation.NonNull;
import com.netease.nimlib.coexist.sdk.v2.conversation.model.V2NIMLocalConversation;
import com.netease.yunxin.kit.common.ui.viewholder.BaseViewHolder;
import com.netease.yunxin.kit.conversationkit.local.ui.model.ConversationBean;

/*
 * 会话列表数据工厂接口
 */
public interface ILocalConversationFactory {
  //根据会话数据，创建ViewHolder中数据类
  ConversationBean CreateBean(V2NIMLocalConversation info);

  //Adapter获取数据对应的ViewType
  int getItemViewType(ConversationBean data);

  //创建ViewHolder
  BaseViewHolder<ConversationBean> createViewHolder(@NonNull ViewGroup parent, int viewType);
}
