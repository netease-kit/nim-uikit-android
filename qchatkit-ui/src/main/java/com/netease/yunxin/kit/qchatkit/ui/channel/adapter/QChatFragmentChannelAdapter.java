// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.qchatkit.ui.channel.adapter;

import android.content.Context;
import android.view.View;
import com.netease.yunxin.kit.qchatkit.repo.model.QChatChannelInfo;
import com.netease.yunxin.kit.qchatkit.repo.model.QChatUnreadInfoItem;
import com.netease.yunxin.kit.qchatkit.ui.common.QChatCommonAdapter;
import com.netease.yunxin.kit.qchatkit.ui.databinding.QChatFragmentChannelListItemBinding;
import java.util.Map;

public class QChatFragmentChannelAdapter
    extends QChatCommonAdapter<QChatChannelInfo, QChatFragmentChannelListItemBinding> {
  private Map<Long, QChatUnreadInfoItem> unreadInfoItemMap;

  public QChatFragmentChannelAdapter(Context context) {
    super(context, QChatFragmentChannelListItemBinding.class);
  }

  public void updateUnreadCount(Map<Long, QChatUnreadInfoItem> map) {
    this.unreadInfoItemMap = map;
  }

  @Override
  public void onBindViewHolder(
      QChatFragmentChannelListItemBinding binding,
      int position,
      QChatChannelInfo data,
      int bingingAdapterPosition) {
    super.onBindViewHolder(binding, position, data, bingingAdapterPosition);

    binding.tvChannelTitle.setText(data.getName());

    int count = getUnreadCount(data.getChannelId());
    if (count > 0) {
      String content;
      if (count >= 100) {
        content = "99+";
      } else {
        content = String.valueOf(count);
      }
      binding.tvUnReadCount.setText(content);
      binding.tvUnReadCount.setVisibility(View.VISIBLE);
    } else {
      binding.tvUnReadCount.setVisibility(View.GONE);
    }
  }

  private int getUnreadCount(long channelId) {
    if (unreadInfoItemMap == null) {
      return 0;
    }
    QChatUnreadInfoItem item = unreadInfoItemMap.get(channelId);
    if (item == null) {
      return 0;
    }
    return item.getUnreadCount();
  }
}
