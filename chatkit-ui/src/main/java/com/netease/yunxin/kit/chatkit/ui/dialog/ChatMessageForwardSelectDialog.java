// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.dialog;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatMessageForwardLayoutBinding;
import com.netease.yunxin.kit.common.ui.dialog.BaseBottomDialog;

public class ChatMessageForwardSelectDialog extends BaseBottomDialog {

  public static final String TAG = "ChatMessageForwardDialog";

  private ForwardTypeSelectedCallback selectedCallback;

  @Nullable
  @Override
  protected View getRootView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
    ChatMessageForwardLayoutBinding binding =
        ChatMessageForwardLayoutBinding.inflate(inflater, container, false);
    binding.tvTeam.setOnClickListener(
        v -> {
          hide();
          if (selectedCallback != null) {
            selectedCallback.onTeamSelected();
          }
        });
    binding.tvP2p.setOnClickListener(
        v -> {
          hide();
          if (selectedCallback != null) {
            selectedCallback.onP2PSelected();
          }
        });
    binding.tvCancel.setOnClickListener(v -> hide());
    return binding.getRoot();
  }

  public void hide() {
    if (getDialog() != null && getDialog().isShowing()) {
      dismiss();
    }
  }

  public void setSelectedCallback(ForwardTypeSelectedCallback selectedCallback) {
    this.selectedCallback = selectedCallback;
  }

  public interface ForwardTypeSelectedCallback {
    void onTeamSelected();

    void onP2PSelected();
  }
}
