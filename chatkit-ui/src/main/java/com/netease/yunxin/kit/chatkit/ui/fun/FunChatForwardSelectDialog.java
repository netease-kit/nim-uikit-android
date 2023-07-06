// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.fun;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.netease.yunxin.kit.chatkit.ui.databinding.FunChatMessageForwardDialogBinding;
import com.netease.yunxin.kit.chatkit.ui.dialog.ChatBaseForwardSelectDialog;

public class FunChatForwardSelectDialog extends ChatBaseForwardSelectDialog {

  public static final String TAG = "ChatMessageForwardDialog";

  @Nullable
  @Override
  protected View getRootView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
    FunChatMessageForwardDialogBinding binding =
        FunChatMessageForwardDialogBinding.inflate(inflater, container, false);
    p2pView = binding.p2pTv;
    teamView = binding.teamTv;
    cancelView = binding.cancelTv;
    return binding.getRoot();
  }
}
