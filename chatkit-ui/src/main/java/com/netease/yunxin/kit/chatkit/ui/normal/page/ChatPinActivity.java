// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.normal.page;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.common.ChatUtils;
import com.netease.yunxin.kit.chatkit.ui.dialog.ChatBaseForwardSelectDialog;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.chatkit.ui.normal.ChatMessageForwardConfirmDialog;
import com.netease.yunxin.kit.chatkit.ui.normal.ChatMessageForwardSelectDialog;
import com.netease.yunxin.kit.chatkit.ui.normal.factory.PinViewHolderFactory;
import com.netease.yunxin.kit.chatkit.ui.page.ChatPinBaseActivity;
import com.netease.yunxin.kit.common.utils.SizeUtils;
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import java.util.ArrayList;

public class ChatPinActivity extends ChatPinBaseActivity {

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    changeStatusBarColor(R.color.color_eef1f4);
  }

  @Override
  protected void initView() {
    super.initView();
    pinAdapter.setViewHolderFactory(new PinViewHolderFactory());
  }

  public void jumpToChat(ChatMessageBean messageInfo) {
    String router = RouterConstant.PATH_CHAT_TEAM_PAGE;
    if (mSessionType == SessionTypeEnum.P2P) {
      router = RouterConstant.PATH_CHAT_P2P_PAGE;
    }

    XKitRouter.withKey(router)
        .withParam(RouterConstant.KEY_MESSAGE_BEAN, messageInfo)
        .withParam(RouterConstant.CHAT_KRY, mSessionId)
        .withContext(this)
        .navigate();
    finish();
  }

  @Override
  public RecyclerView.ItemDecoration getItemDecoration() {
    return new RecyclerView.ItemDecoration() {

      final int lRPadding = SizeUtils.dp2px(20);
      final int topPadding = SizeUtils.dp2px(12);

      @Override
      public void getItemOffsets(
          @NonNull Rect outRect,
          @NonNull View view,
          @NonNull RecyclerView parent,
          @NonNull RecyclerView.State state) {
        outRect.set(lRPadding, topPadding, lRPadding, 0);
      }
    };
  }

  @Override
  protected ChatBaseForwardSelectDialog getForwardSelectDialog() {
    ChatMessageForwardSelectDialog dialog = new ChatMessageForwardSelectDialog();
    dialog.setSelectedCallback(
        new ChatMessageForwardSelectDialog.ForwardTypeSelectedCallback() {
          @Override
          public void onTeamSelected() {
            ChatUtils.startTeamList(
                ChatPinActivity.this, RouterConstant.PATH_MY_TEAM_PAGE, forwardTeamLauncher);
          }

          @Override
          public void onP2PSelected() {
            ChatUtils.startP2PSelector(
                ChatPinActivity.this,
                RouterConstant.PATH_CONTACT_SELECTOR_PAGE,
                null,
                forwardP2PLauncher);
          }
        });
    return dialog;
  }

  @Override
  protected void showForwardConfirmDialog(SessionTypeEnum type, ArrayList<String> sessionIds) {
    ChatMessageForwardConfirmDialog confirmDialog =
        ChatMessageForwardConfirmDialog.createForwardConfirmDialog(
            type, sessionIds, forwardMessage.getMessageData());
    confirmDialog.setCallback(
        () -> {
          if (forwardMessage != null) {
            for (String accId : sessionIds) {
              viewModel.sendForwardMessage(
                  forwardMessage.getMessageData().getMessage(), accId, type);
            }
          }
        });
    confirmDialog.show(getSupportFragmentManager(), TAG);
  }
}
