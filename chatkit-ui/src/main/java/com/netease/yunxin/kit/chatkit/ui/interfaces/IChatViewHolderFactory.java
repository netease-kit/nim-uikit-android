// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.interfaces;

import android.view.ViewGroup;
import androidx.annotation.NonNull;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;

public interface IChatViewHolderFactory {

  ChatBaseViewHolder createViewHolder(@NonNull ViewGroup parent, int viewType);

  public int getItemViewType(ChatMessageBean messageBean);
}
