// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.normal;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatMessageForwardLayoutBinding;
import com.netease.yunxin.kit.chatkit.ui.dialog.ChatBaseForwardSelectDialog;

/** 标准皮肤，转发选择弹窗。 */
public class ChatMessageForwardSelectDialog extends ChatBaseForwardSelectDialog {

  public static final String TAG = "ChatMessageForwardDialog";

  @Nullable
  @Override
  protected View getRootView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
    ChatMessageForwardLayoutBinding binding =
        ChatMessageForwardLayoutBinding.inflate(inflater, container, false);
    p2pView = binding.tvP2p;
    teamView = binding.tvTeam;
    cancelView = binding.tvCancel;
    return binding.getRoot();
  }
}
