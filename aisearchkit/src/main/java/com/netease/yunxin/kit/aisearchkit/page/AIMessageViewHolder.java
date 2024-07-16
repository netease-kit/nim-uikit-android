// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.aisearchkit.page;

import androidx.annotation.NonNull;
import com.netease.nimlib.sdk.v2.ai.params.V2NIMAIModelCallContent;
import com.netease.yunxin.kit.aisearchkit.databinding.AiSearchMessageHolderBinding;
import com.netease.yunxin.kit.common.ui.viewholder.BaseViewHolder;

/** AI消息ViewHolder */
public class AIMessageViewHolder extends BaseViewHolder<V2NIMAIModelCallContent> {

  AiSearchMessageHolderBinding binding;

  public AIMessageViewHolder(@NonNull AiSearchMessageHolderBinding viewBinding) {
    super(viewBinding);
    this.binding = viewBinding;
  }

  @Override
  public void onBindData(V2NIMAIModelCallContent data, int position) {
    binding.tvMessage.setText(data.getMsg());
  }
}
