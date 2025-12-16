// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.fun.page.fragment;

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
import com.netease.yunxin.kit.chatkit.ui.databinding.FunChatFragmentBinding;
import com.netease.yunxin.kit.chatkit.ui.dialog.ChatBaseForwardSelectDialog;
import com.netease.yunxin.kit.chatkit.ui.fun.FunChatForwardSelectDialog;
import com.netease.yunxin.kit.chatkit.ui.fun.FunChatMessageForwardConfirmDialog;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.chatkit.ui.page.fragment.ChatBaseFragment;
import com.netease.yunxin.kit.chatkit.ui.view.input.ActionConstants;
import com.netease.yunxin.kit.common.utils.NetworkUtils;
import com.netease.yunxin.kit.corekit.coexist.im2.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import java.util.ArrayList;

/** Fun皮肤聊天界面Fragment，继承自ChatBaseFragment Fun皮肤下的差异化UI在这里实现，基础功能在父类中实现 */
public abstract class FunChatFragment extends ChatBaseFragment {

  FunChatFragmentBinding viewBinding;

  @Override
  public View initViewAndGetRootView(
      @NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
    viewBinding = FunChatFragmentBinding.inflate(inflater, container, false);
    chatView = viewBinding.chatView;
    chatView.getTitleBar().getTitleTextView().setEllipsize(TextUtils.TruncateAt.MIDDLE);
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
    return new FunChatForwardSelectDialog();
  }

  @Override
  protected void onStartForward(String action) {
    super.onStartForward(action);
    ChatUtils.startForwardSelector(
        getContext(), RouterConstant.PATH_FUN_FORWARD_SELECTOR_PAGE, false, forwardLauncher);
  }

  @Override
  public void showForwardConfirmDialog(ArrayList<String> conversationIds) {
    FunChatMessageForwardConfirmDialog confirmDialog =
        FunChatMessageForwardConfirmDialog.createForwardConfirmDialog(
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
    confirmDialog.show(getParentFragmentManager(), FunChatMessageForwardConfirmDialog.TAG);
  }

  public String getConversationName() {
    return accountId;
  }

  @Override
  protected void clickMessage(IMMessageInfo messageInfo, int position, boolean isReply) {
    if (messageInfo == null) {
      return;
    }
    if (messageInfo.getMessage().getMessageType() == V2NIMMessageType.V2NIM_MESSAGE_TYPE_CUSTOM) {
      if (messageInfo.getAttachment() instanceof MultiForwardAttachment) {
        XKitRouter.withKey(RouterConstant.PATH_FUN_CHAT_FORWARD_PAGE)
            .withContext(getContext())
            .withParam(RouterConstant.KEY_MESSAGE, messageInfo)
            .navigate();
        return;
      }
    }
    super.clickMessage(messageInfo, position, isReply);
  }
}
