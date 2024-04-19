// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.fun.page;

import android.graphics.Rect;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import com.netease.nimlib.sdk.v2.conversation.enums.V2NIMConversationType;
import com.netease.nimlib.sdk.v2.message.enums.V2NIMMessageType;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.common.ChatUserCache;
import com.netease.yunxin.kit.chatkit.ui.common.ChatUtils;
import com.netease.yunxin.kit.chatkit.ui.custom.MultiForwardAttachment;
import com.netease.yunxin.kit.chatkit.ui.dialog.ChatBaseForwardSelectDialog;
import com.netease.yunxin.kit.chatkit.ui.fun.FunChatForwardSelectDialog;
import com.netease.yunxin.kit.chatkit.ui.fun.FunChatMessageForwardConfirmDialog;
import com.netease.yunxin.kit.chatkit.ui.fun.FunChoiceDialog;
import com.netease.yunxin.kit.chatkit.ui.fun.factory.FunPinViewHolderFactory;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.chatkit.ui.page.ChatPinBaseActivity;
import com.netease.yunxin.kit.chatkit.ui.view.input.ActionConstants;
import com.netease.yunxin.kit.common.ui.dialog.BaseBottomChoiceDialog;
import com.netease.yunxin.kit.common.utils.SizeUtils;
import com.netease.yunxin.kit.corekit.im2.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import java.util.ArrayList;

/** Fun皮肤Pin列表页面，查看回话所有的Pin消息，继承自ChatPinBaseActivity */
public class FunChatPinActivity extends ChatPinBaseActivity {

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    changeStatusBarColor(R.color.fun_chat_secondary_page_bg_color);
    viewBinding.getRoot().setBackgroundResource(R.color.fun_chat_secondary_page_bg_color);
    viewBinding.emptyIv.setImageResource(R.drawable.fun_ic_chat_empty);
  }

  @Override
  protected void initView() {
    super.initView();
    pinAdapter.setViewHolderFactory(new FunPinViewHolderFactory());
  }

  @Override
  public RecyclerView.ItemDecoration getItemDecoration() {
    return new RecyclerView.ItemDecoration() {
      final int topPadding = SizeUtils.dp2px(8);

      @Override
      public void getItemOffsets(
          @NonNull Rect outRect,
          @NonNull View view,
          @NonNull RecyclerView parent,
          @NonNull RecyclerView.State state) {
        outRect.set(0, topPadding, 0, 0);
      }
    };
  }

  @Override
  public BaseBottomChoiceDialog getMoreActionDialog(ChatMessageBean messageInfo) {
    return new FunChoiceDialog(this, assembleActions(messageInfo));
  }

  @Override
  protected int getPageBackgroundColor() {
    return R.color.fun_chat_secondary_page_bg_color;
  }

  @Override
  public void jumpToChat(ChatMessageBean messageInfo) {
    String router = RouterConstant.PATH_FUN_CHAT_TEAM_PAGE;
    if (mSessionType == V2NIMConversationType.V2NIM_CONVERSATION_TYPE_P2P) {
      router = RouterConstant.PATH_FUN_CHAT_P2P_PAGE;
    }

    XKitRouter.withKey(router)
        .withParam(RouterConstant.KEY_MESSAGE_INFO, messageInfo.getMessageData())
        .withParam(RouterConstant.CHAT_KRY, mSessionId)
        .withContext(FunChatPinActivity.this)
        .navigate();
    finish();
  }

  @Override
  protected void toP2PSelected() {
    ChatUtils.startP2PSelector(
        FunChatPinActivity.this,
        RouterConstant.PATH_FUN_CONTACT_SELECTOR_PAGE,
        null,
        forwardP2PLauncher);
  }

  @Override
  protected void toTeamSelected() {
    ChatUtils.startTeamList(
        FunChatPinActivity.this, RouterConstant.PATH_FUN_MY_TEAM_PAGE, forwardTeamLauncher);
  }

  @Override
  protected void clickCustomMessage(ChatMessageBean messageBean) {
    if (messageBean.getMessageData().getMessage().getMessageType()
        == V2NIMMessageType.V2NIM_MESSAGE_TYPE_CUSTOM) {
      if (messageBean.getMessageData().getAttachment() instanceof MultiForwardAttachment) {
        XKitRouter.withKey(RouterConstant.PATH_FUN_CHAT_FORWARD_PAGE)
            .withContext(this)
            .withParam(RouterConstant.KEY_MESSAGE, messageBean.getMessageData())
            .navigate();
      }
    }
  }

  @Override
  protected ChatBaseForwardSelectDialog getForwardSelectDialog() {
    ChatBaseForwardSelectDialog dialog = new FunChatForwardSelectDialog();
    dialog.setSelectedCallback(
        new ChatBaseForwardSelectDialog.ForwardTypeSelectedCallback() {
          @Override
          public void onTeamSelected() {
            toTeamSelected();
          }

          @Override
          public void onP2PSelected() {
            toP2PSelected();
          }
        });
    return dialog;
  }

  @Override
  protected void showForwardConfirmDialog(
      V2NIMConversationType type, ArrayList<String> sessionIds) {
    if (forwardMessage == null) {
      return;
    }
    String sendName =
        TextUtils.isEmpty(mSessionName)
            ? ChatUserCache.getInstance()
                .getFriendInfo(forwardMessage.getMessageData().getMessage().getSenderId())
                .getAvatarName()
            : mSessionName;
    FunChatMessageForwardConfirmDialog confirmDialog =
        FunChatMessageForwardConfirmDialog.createForwardConfirmDialog(
            type, sessionIds, sendName, true, ActionConstants.POP_ACTION_TRANSMIT);
    confirmDialog.setCallback(
        (inputMsg) -> {
          if (forwardMessage != null) {
            for (String accId : sessionIds) {
              viewModel.sendForwardMessage(
                  forwardMessage.getMessageData().getMessage(), inputMsg, accId, type);
            }
          }
        });
    confirmDialog.show(getSupportFragmentManager(), TAG);
  }
}
