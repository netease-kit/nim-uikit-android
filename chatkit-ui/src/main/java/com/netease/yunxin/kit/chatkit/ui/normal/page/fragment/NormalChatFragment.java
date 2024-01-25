// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.normal.page.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.yunxin.kit.chatkit.model.IMMessageInfo;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.common.ChatMsgCache;
import com.netease.yunxin.kit.chatkit.ui.common.ChatUtils;
import com.netease.yunxin.kit.chatkit.ui.custom.MultiForwardAttachment;
import com.netease.yunxin.kit.chatkit.ui.databinding.NormalChatFragmentBinding;
import com.netease.yunxin.kit.chatkit.ui.dialog.ChatBaseForwardSelectDialog;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.chatkit.ui.normal.ChatMessageForwardConfirmDialog;
import com.netease.yunxin.kit.chatkit.ui.normal.ChatMessageForwardSelectDialog;
import com.netease.yunxin.kit.chatkit.ui.page.fragment.ChatBaseFragment;
import com.netease.yunxin.kit.chatkit.ui.view.input.ActionConstants;
import com.netease.yunxin.kit.common.utils.NetworkUtils;
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import java.util.ArrayList;

/** 标准皮肤，单聊和群聊会话页面Fragment的父类。 */
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
    return new ChatMessageForwardSelectDialog();
  }

  @Override
  protected void forwardP2P() {
    ChatUtils.startP2PSelector(
        getContext(), RouterConstant.PATH_CONTACT_SELECTOR_PAGE, null, forwardP2PLauncher);
  }

  @Override
  protected void forwardTeam() {
    ChatUtils.startTeamList(getContext(), RouterConstant.PATH_MY_TEAM_PAGE, forwardTeamLauncher);
  }

  @Override
  public void showForwardConfirmDialog(SessionTypeEnum type, ArrayList<String> sessionIds) {
    ChatMessageForwardConfirmDialog confirmDialog =
        ChatMessageForwardConfirmDialog.createForwardConfirmDialog(
            type, sessionIds, getSessionName(), true, forwardAction);
    confirmDialog.setCallback(
        (inputMsg) -> {
          if (!NetworkUtils.isConnected()) {
            Toast.makeText(getContext(), R.string.chat_network_error_tip, Toast.LENGTH_SHORT)
                .show();
            return;
          }
          if (TextUtils.equals(forwardAction, ActionConstants.POP_ACTION_TRANSMIT)) {
            ChatMessageBean msg = getForwardMessage();
            if (msg != null) {
              for (String accId : sessionIds) {
                viewModel.sendForwardMessage(msg, inputMsg, accId, type);
              }
            }
          } else if (TextUtils.equals(forwardAction, ActionConstants.ACTION_TYPE_MULTI_FORWARD)) {
            viewModel.sendMultiForwardMessage(
                getSessionName(), inputMsg, sessionIds, type, ChatMsgCache.getMessageList());
            clearMessageMultiSelectStatus();
          } else if (TextUtils.equals(forwardAction, ActionConstants.ACTION_TYPE_SINGLE_FORWARD)) {
            viewModel.sendForwardMessages(
                getSessionName(), inputMsg, sessionIds, type, ChatMsgCache.getMessageList());
            clearMessageMultiSelectStatus();
          }
        });
    confirmDialog.show(getParentFragmentManager(), ChatMessageForwardConfirmDialog.TAG);
  }

  @Override
  protected void clickMessage(IMMessageInfo messageInfo, boolean isReply) {
    if (messageInfo.getMessage().getMsgType() == MsgTypeEnum.custom) {
      if (messageInfo.getMessage().getAttachment() instanceof MultiForwardAttachment) {
        XKitRouter.withKey(RouterConstant.PATH_CHAT_FORWARD_PAGE)
            .withContext(getContext())
            .withParam(RouterConstant.KEY_MESSAGE, messageInfo)
            .navigate();
        return;
      }
    }
    super.clickMessage(messageInfo, isReply);
  }

  public String getSessionName() {
    return sessionID;
  }

  @Override
  protected void initData(Bundle bundle) {}

  @Override
  public String getUserInfoRoutePath() {
    return RouterConstant.PATH_USER_INFO_PAGE;
  }
}
