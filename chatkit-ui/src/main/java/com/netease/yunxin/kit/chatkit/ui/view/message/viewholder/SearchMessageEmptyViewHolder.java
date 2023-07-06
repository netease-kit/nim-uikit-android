// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.view.message.viewholder;

import android.view.View;
import androidx.annotation.NonNull;
import com.netease.yunxin.kit.chatkit.ui.model.ChatSearchBean;
import com.netease.yunxin.kit.common.ui.viewholder.BaseViewHolder;

public class SearchMessageEmptyViewHolder extends BaseViewHolder<ChatSearchBean> {

  public SearchMessageEmptyViewHolder(@NonNull View itemView) {
    super(itemView);
  }

  @Override
  public void onBindData(ChatSearchBean data, int position) {}
}
