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
import com.netease.nimlib.coexist.sdk.v2.message.enums.V2NIMMessageType;
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
import com.netease.yunxin.kit.corekit.coexist.im2.utils.RouterConstant;
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
    chatView.getTitleBar().getTitleTextView().setEllipsize(TextUtils.TruncateAt.MIDDLE);
    return viewBinding.getRoot();
  }

  @Override
  protected void initViewModel() {}

  @Override
  protected ChatBaseForwardSelectDialog getForwardSelectDialog() {
    return new ChatMessageForwardSelectDialog();
  }

  @Override
  protected void onStartForward(String action) {
    super.onStartForward(action);
    ChatUtils.startForwardSelector(
        getContext(), RouterConstant.PATH_FORWARD_SELECTOR_PAGE, false, forwardLauncher);
  }

  @Override
  public void showForwardConfirmDialog(ArrayList<String> conversationIds) {
    ChatMessageForwardConfirmDialog confirmDialog =
        ChatMessageForwardConfirmDialog.createForwardConfirmDialog(
            conversationIds, getConversationName(), true, forwardAction);
    confirmDialog.setCallback(
        (inputMsg) -> {
          if (!NetworkUtils.isConnected()) {
            Toast.makeText(getContext(), R.string.chat_network_error_tip, Toast.LENGTH_SHORT)
                .show();
            return;
          }
          if (TextUtils.equals(forwardAction, ActionConstants.POP_ACTION_TRANSMIT)) {
            ChatMessageBean msg = getForwardMessage();
            viewModel.sendForwardMessage(msg, inputMsg, conversationIds);
          } else if (TextUtils.equals(forwardAction, ActionConstants.ACTION_TYPE_MULTI_FORWARD)) {
            viewModel.sendMultiForwardMessage(
                getConversationName(), inputMsg, conversationIds, ChatMsgCache.getMessageList());
            clearMessageMultiSelectStatus();
          } else if (TextUtils.equals(forwardAction, ActionConstants.ACTION_TYPE_SINGLE_FORWARD)) {
            viewModel.sendForwardMessages(inputMsg, conversationIds, ChatMsgCache.getMessageList());
            clearMessageMultiSelectStatus();
          }
        });
    confirmDialog.show(getParentFragmentManager(), ChatMessageForwardConfirmDialog.TAG);
  }

  @Override
  protected void clickMessage(IMMessageInfo messageInfo, int position, boolean isReply) {
    if (messageInfo == null) {
      return;
    }
    if (messageInfo.getMessage().getMessageType() == V2NIMMessageType.V2NIM_MESSAGE_TYPE_CUSTOM) {
      if (messageInfo.getAttachment() instanceof MultiForwardAttachment) {
        XKitRouter.withKey(RouterConstant.PATH_CHAT_FORWARD_PAGE)
            .withContext(getContext())
            .withParam(RouterConstant.KEY_MESSAGE, messageInfo)
            .navigate();
        return;
      }
    }
    super.clickMessage(messageInfo, position, isReply);
  }

  @Override
  public String getConversationName() {
    return accountId;
  }

  @Override
  protected void initData(Bundle bundle) {}

  @Override
  public String getUserInfoRoutePath() {
    return RouterConstant.PATH_USER_INFO_PAGE;
  }
}
