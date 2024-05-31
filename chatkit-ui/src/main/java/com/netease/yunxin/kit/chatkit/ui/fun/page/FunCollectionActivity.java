// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.fun.page;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import com.netease.nimlib.sdk.v2.conversation.enums.V2NIMConversationType;
import com.netease.nimlib.sdk.v2.message.enums.V2NIMMessageType;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.custom.MultiForwardAttachment;
import com.netease.yunxin.kit.chatkit.ui.fun.FunChatMessageForwardConfirmDialog;
import com.netease.yunxin.kit.chatkit.ui.fun.FunChoiceDialog;
import com.netease.yunxin.kit.chatkit.ui.fun.factory.FunCollectionViewHolderFactory;
import com.netease.yunxin.kit.chatkit.ui.model.CollectionBean;
import com.netease.yunxin.kit.chatkit.ui.page.CollectionBaseActivity;
import com.netease.yunxin.kit.chatkit.ui.view.input.ActionConstants;
import com.netease.yunxin.kit.common.ui.dialog.BaseBottomChoiceDialog;
import com.netease.yunxin.kit.common.utils.SizeUtils;
import com.netease.yunxin.kit.corekit.im2.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import java.util.ArrayList;

/** Fun皮肤收藏列表页面，查看回话所有的收藏消息，继承自CollectionBaseActivity */
public class FunCollectionActivity extends CollectionBaseActivity {

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
    collectionAdapter.setViewHolderFactory(new FunCollectionViewHolderFactory());
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
  public BaseBottomChoiceDialog getMoreActionDialog(CollectionBean messageInfo) {
    return new FunChoiceDialog(this, assembleActions(messageInfo));
  }

  @Override
  protected int getPageBackgroundColor() {
    return R.color.fun_chat_secondary_page_bg_color;
  }

  @Override
  protected void clickCustomMessage(CollectionBean messageBean) {
    if (messageBean.getMessageData().getMessageType()
        == V2NIMMessageType.V2NIM_MESSAGE_TYPE_CUSTOM) {
      if (messageBean.getMessageData().getAttachment() instanceof MultiForwardAttachment) {
        XKitRouter.withKey(RouterConstant.PATH_FUN_CHAT_FORWARD_PAGE)
            .withContext(this)
            .withParam(RouterConstant.KEY_MESSAGE, messageBean.getMessageInfo())
            .navigate();
      }
    }
  }

  @Override
  protected void showForwardConfirmDialog(ArrayList<String> conversationIds) {
    if (forwardMessage == null) {
      return;
    }
    String sendTips = "";
    if (forwardMessage.getMessageData().getConversationType()
        == V2NIMConversationType.V2NIM_CONVERSATION_TYPE_P2P) {
      sendTips =
          String.format(
              getString(R.string.chat_collection_p2p_transmit_tip),
              forwardMessage.conversationName);
    } else {
      sendTips =
          String.format(
              getString(R.string.chat_collection_team_transmit_tip),
              forwardMessage.conversationName);
    }
    FunChatMessageForwardConfirmDialog confirmDialog =
        FunChatMessageForwardConfirmDialog.createForwardConfirmDialog(
            conversationIds, "", sendTips, true, ActionConstants.POP_ACTION_TRANSMIT);
    confirmDialog.setCallback(
        (inputMsg) -> {
          if (forwardMessage != null) {
            for (String accId : conversationIds) {
              viewModel.sendForwardMessage(forwardMessage.getMessageData(), inputMsg, accId);
            }
          }
        });
    confirmDialog.show(getSupportFragmentManager(), TAG);
  }
}
