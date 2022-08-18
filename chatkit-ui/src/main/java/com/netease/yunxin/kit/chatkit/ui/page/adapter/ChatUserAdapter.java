// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.page.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import com.netease.yunxin.kit.chatkit.ui.common.MessageHelper;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatUserItemLayoutBinding;
import com.netease.yunxin.kit.common.ui.activities.adapter.CommonMoreAdapter;
import com.netease.yunxin.kit.common.ui.activities.viewholder.BaseMoreViewHolder;
import com.netease.yunxin.kit.common.ui.utils.AvatarColor;
import com.netease.yunxin.kit.corekit.im.model.UserInfo;

/** team message read state adapter */
public class ChatUserAdapter extends CommonMoreAdapter<UserInfo, ChatUserItemLayoutBinding> {

  String tid;

  @NonNull
  @Override
  public UserItemViewHolder getViewHolder(@NonNull ViewGroup parent, int viewType) {
    ChatUserItemLayoutBinding binding =
        ChatUserItemLayoutBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
    return new UserItemViewHolder(binding).setTid(tid);
  }

  public void setTid(String tid) {
    this.tid = tid;
  }

  public static class UserItemViewHolder
      extends BaseMoreViewHolder<UserInfo, ChatUserItemLayoutBinding> {

    String tid;

    public UserItemViewHolder setTid(String tid) {
      this.tid = tid;
      return this;
    }

    public UserItemViewHolder(@NonNull ChatUserItemLayoutBinding binding) {
      super(binding);
    }

    @Override
    public void bind(UserInfo item) {
      if (item != null) {
        String name = MessageHelper.getTeamMemberDisplayName(tid, item);
        getBinding()
            .avatar
            .setData(item.getAvatar(), name, AvatarColor.avatarColor(item.getAccount()));
        getBinding().nickname.setText(name);
      }
    }
  }
}
