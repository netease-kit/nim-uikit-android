// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.view.message.viewholder;

import android.view.View;
import androidx.annotation.NonNull;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatSearchItemLayoutBinding;
import com.netease.yunxin.kit.chatkit.ui.model.ChatSearchBean;
import com.netease.yunxin.kit.common.ui.utils.AvatarColor;
import com.netease.yunxin.kit.common.ui.utils.TimeFormatUtils;
import com.netease.yunxin.kit.common.ui.viewholder.BaseViewHolder;

public class SearchMessageViewHolder extends BaseViewHolder<ChatSearchBean> {

  private ChatSearchItemLayoutBinding viewBinding;

  public SearchMessageViewHolder(@NonNull View itemView) {
    super(itemView);
  }

  public SearchMessageViewHolder(@NonNull ChatSearchItemLayoutBinding viewBinding) {
    this(viewBinding.getRoot());
    this.viewBinding = viewBinding;
  }

  @Override
  public void onBindData(ChatSearchBean data, int position) {
    if (data != null) {
      viewBinding.cavIcon.setData(
          data.getAvatar(), data.getNickName(), AvatarColor.avatarColor(data.getAccount()));

      viewBinding.tvNickName.setText(data.getNickName());

      viewBinding.tvMessage.setText(
          data.getSpannableString(
              viewBinding
                  .getRoot()
                  .getContext()
                  .getResources()
                  .getColor(com.netease.yunxin.kit.common.ui.R.color.color_337eff)));

      viewBinding.tvTime.setText(
          TimeFormatUtils.formatMillisecond(viewBinding.getRoot().getContext(), data.getTime()));

      viewBinding.getRoot().setOnClickListener(v -> itemListener.onClick(data, position));
    }
  }
}
