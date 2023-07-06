// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.fun.page.fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.common.ChatUtils;
import com.netease.yunxin.kit.chatkit.ui.databinding.FunChatFragmentBinding;
import com.netease.yunxin.kit.chatkit.ui.dialog.ChatBaseForwardSelectDialog;
import com.netease.yunxin.kit.chatkit.ui.fun.FunChatForwardSelectDialog;
import com.netease.yunxin.kit.chatkit.ui.page.fragment.ChatBaseFragment;
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant;

public abstract class FunChatFragment extends ChatBaseFragment {

  FunChatFragmentBinding viewBinding;

  @Override
  public View initViewAndGetRootView(
      @NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
    viewBinding = FunChatFragmentBinding.inflate(inflater, container, false);
    chatView = viewBinding.chatView;
    return viewBinding.getRoot();
  }

  @Override
  public Integer getReplayMessageClickPreviewDialogBgRes() {
    return R.color.color_ededed;
  }

  @Override
  public String getUserInfoRoutePath() {
    return RouterConstant.PATH_FUN_USER_INFO_PAGE;
  }

  protected ChatBaseForwardSelectDialog getForwardSelectDialog() {
    ChatBaseForwardSelectDialog dialog = new FunChatForwardSelectDialog();
    dialog.setSelectedCallback(
        new ChatBaseForwardSelectDialog.ForwardTypeSelectedCallback() {
          @Override
          public void onTeamSelected() {
            ChatUtils.startTeamList(
                getContext(), RouterConstant.PATH_FUN_MY_TEAM_PAGE, forwardTeamLauncher);
          }

          @Override
          public void onP2PSelected() {
            ChatUtils.startP2PSelector(
                getContext(),
                RouterConstant.PATH_FUN_CONTACT_SELECTOR_PAGE,
                sessionID,
                forwardP2PLauncher);
          }
        });
    return dialog;
  }
}
