// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.dialog;

import android.view.View;
import com.netease.yunxin.kit.common.ui.dialog.BaseBottomDialog;

public abstract class ChatBaseForwardSelectDialog extends BaseBottomDialog {

  public static final String TAG = "ChatMessageForwardDialog";

  private ForwardTypeSelectedCallback selectedCallback;

  protected View teamView;

  protected View p2pView;

  protected View cancelView;

  @Override
  protected void initParams() {
    super.initParams();
    if (teamView != null) {
      teamView.setOnClickListener(
          v -> {
            hide();
            if (selectedCallback != null) {
              selectedCallback.onTeamSelected();
            }
          });
    }
    if (p2pView != null) {
      p2pView.setOnClickListener(
          v -> {
            hide();
            if (selectedCallback != null) {
              selectedCallback.onP2PSelected();
            }
          });
    }
    if (cancelView != null) {
      cancelView.setOnClickListener(v -> hide());
    }
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
