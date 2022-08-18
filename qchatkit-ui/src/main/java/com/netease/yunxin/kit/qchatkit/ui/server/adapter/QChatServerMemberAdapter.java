// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.qchatkit.ui.server.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import androidx.annotation.NonNull;
import com.netease.yunxin.kit.common.ui.utils.AvatarColor;
import com.netease.yunxin.kit.qchatkit.repo.model.QChatServerMemberWithRoleInfo;
import com.netease.yunxin.kit.qchatkit.ui.common.QChatCommonAdapter;
import com.netease.yunxin.kit.qchatkit.ui.databinding.QChatServerMbemberInfoWithRoleItemBinding;

public class QChatServerMemberAdapter
    extends QChatCommonAdapter<
        QChatServerMemberWithRoleInfo, QChatServerMbemberInfoWithRoleItemBinding> {
  public QChatServerMemberAdapter(
      Context context, Class<QChatServerMbemberInfoWithRoleItemBinding> viewBinding) {
    super(context, viewBinding);
  }

  @Override
  public void onBindViewHolder(
      @NonNull ItemViewHolder<QChatServerMbemberInfoWithRoleItemBinding> holder,
      int position,
      QChatServerMemberWithRoleInfo data) {
    super.onBindViewHolder(holder, position, data);
    QChatServerMbemberInfoWithRoleItemBinding binding = holder.binding;

    String nickname;
    if (TextUtils.isEmpty(data.getNick())) {
      nickname = data.getNicknameOfIM();
      binding.tvAccount.setVisibility(View.GONE);
    } else {
      nickname = data.getNick();
      binding.tvAccount.setVisibility(View.VISIBLE);
      binding.tvAccount.setText(data.getNicknameOfIM());
    }
    if (data.getRoleList() != null && !data.getRoleList().isEmpty()) {
      binding.flGroup.setVisibility(View.VISIBLE);
      binding.flGroup.setData(data.getRoleList());
    } else {
      binding.flGroup.setVisibility(View.GONE);
    }

    if (nickname == null) {
      nickname = "";
    }

    binding.tvName.setText(nickname);
    binding.cavIcon.setData(
        data.getAvatarUrl(), nickname, AvatarColor.avatarColor(data.getAccId()));
  }
}
