// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.fun.viewholder;

import android.view.View;
import androidx.annotation.NonNull;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.databinding.FunChatSearchViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.model.ChatSearchBean;
import com.netease.yunxin.kit.common.ui.utils.AvatarColor;
import com.netease.yunxin.kit.common.ui.utils.TimeFormatUtils;
import com.netease.yunxin.kit.common.ui.viewholder.BaseViewHolder;

public class FunSearchMessageViewHolder extends BaseViewHolder<ChatSearchBean> {

  private FunChatSearchViewHolderBinding viewBinding;

  public FunSearchMessageViewHolder(@NonNull View itemView) {
    super(itemView);
  }

  public FunSearchMessageViewHolder(@NonNull FunChatSearchViewHolderBinding viewBinding) {
    this(viewBinding.getRoot());
    this.viewBinding = viewBinding;
  }

  @Override
  public void onBindData(ChatSearchBean data, int position) {
    if (data != null) {
      viewBinding.avatarView.setData(
          data.getAvatar(), data.getNickName(), AvatarColor.avatarColor(data.getAccount()));

      viewBinding.nameTv.setText(data.getNickName());

      viewBinding.messageTv.setText(
          data.getSpannableString(
              viewBinding
                  .getRoot()
                  .getContext()
                  .getResources()
                  .getColor(R.color.fun_chat_search_message_hit_color)));

      viewBinding.timeTv.setText(
          TimeFormatUtils.formatMillisecond(viewBinding.getRoot().getContext(), data.getTime()));

      viewBinding.getRoot().setOnClickListener(v -> itemListener.onClick(v, data, position));
    }
  }
}
