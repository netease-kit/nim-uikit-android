// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.normal.page.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.netease.yunxin.kit.chatkit.ui.common.ChatUtils;
import com.netease.yunxin.kit.chatkit.ui.databinding.NormalChatFragmentBinding;
import com.netease.yunxin.kit.chatkit.ui.dialog.ChatBaseForwardSelectDialog;
import com.netease.yunxin.kit.chatkit.ui.normal.ChatMessageForwardSelectDialog;
import com.netease.yunxin.kit.chatkit.ui.page.fragment.ChatBaseFragment;
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant;

public abstract class NormalChatFragment extends ChatBaseFragment {
  NormalChatFragmentBinding viewBinding;

  @Override
  public View initViewAndGetRootView(
      @NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
    viewBinding = NormalChatFragmentBinding.inflate(inflater, container, false);
    chatView = viewBinding.chatView;
    return viewBinding.getRoot();
  }

  @Override
  protected void initViewModel() {}

  @Override
  protected ChatBaseForwardSelectDialog getForwardSelectDialog() {
    ChatMessageForwardSelectDialog dialog = new ChatMessageForwardSelectDialog();
    dialog.setSelectedCallback(
        new ChatMessageForwardSelectDialog.ForwardTypeSelectedCallback() {
          @Override
          public void onTeamSelected() {
            ChatUtils.startTeamList(
                getContext(), RouterConstant.PATH_MY_TEAM_PAGE, forwardTeamLauncher);
          }

          @Override
          public void onP2PSelected() {
            ChatUtils.startP2PSelector(
                getContext(),
                RouterConstant.PATH_CONTACT_SELECTOR_PAGE,
                sessionID,
                forwardP2PLauncher);
          }
        });
    return dialog;
  }

  @Override
  protected void initData(Bundle bundle) {}

  @Override
  public String getUserInfoRoutePath() {
    return RouterConstant.PATH_USER_INFO_PAGE;
  }
}
