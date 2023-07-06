// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui;

import android.view.ViewGroup;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.chatkit.ui.view.message.viewholder.ChatBaseMessageViewHolder;
import com.netease.yunxin.kit.chatkit.ui.view.message.viewholder.CommonBaseMessageViewHolder;

public interface IChatDefaultFactory extends IChatFactory {

  <T extends ChatBaseMessageViewHolder> void addCustomViewHolder(int type, Class<T> viewHolder);

  void removeCustomViewHolder(int type);

  <T extends CommonBaseMessageViewHolder> void addCommonCustomViewHolder(
      int type, Class<T> viewHolderClass, @LayoutRes int layoutRes);

  void removeCommonCustomViewHolder(int type);

  int getCustomViewType(ChatMessageBean messageBean);

  @Nullable
  CommonBaseMessageViewHolder createViewHolderCustom(@NonNull ViewGroup parent, int viewType);
}
