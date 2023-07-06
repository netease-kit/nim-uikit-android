// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.conversationkit.ui.page;

import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import com.netease.yunxin.kit.chatkit.model.ConversationInfo;
import com.netease.yunxin.kit.common.ui.viewholder.BaseViewHolder;
import com.netease.yunxin.kit.conversationkit.ui.IConversationFactory;
import com.netease.yunxin.kit.conversationkit.ui.model.ConversationBean;

/** conversation view holder factory to create view holder in recyclerview */
public class DefaultViewHolderFactory implements IConversationFactory {

  @Override
  public ConversationBean CreateBean(ConversationInfo info) {
    return new ConversationBean(info);
  }

  @Override
  public int getItemViewType(ConversationBean data) {
    return data.viewType;
  }

  @Override
  public BaseViewHolder<ConversationBean> createViewHolder(
      @NonNull ViewGroup parent, int viewType) {
    return new BaseViewHolder<ConversationBean>(new View(parent.getContext())) {
      @Override
      public void onBindData(ConversationBean data, int position) {}
    };
  }
}
