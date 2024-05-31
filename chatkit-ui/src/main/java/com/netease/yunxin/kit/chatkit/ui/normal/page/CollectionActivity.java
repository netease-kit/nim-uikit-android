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
import com.netease.nimlib.sdk.v2.conversation.enums.V2NIMConversationType;
import com.netease.nimlib.sdk.v2.message.enums.V2NIMMessageType;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.common.ChatUtils;
import com.netease.yunxin.kit.chatkit.ui.custom.MultiForwardAttachment;
import com.netease.yunxin.kit.chatkit.ui.model.CollectionBean;
import com.netease.yunxin.kit.chatkit.ui.normal.ChatMessageForwardConfirmDialog;
import com.netease.yunxin.kit.chatkit.ui.normal.factory.CollectionViewHolderFactory;
import com.netease.yunxin.kit.chatkit.ui.page.CollectionBaseActivity;
import com.netease.yunxin.kit.chatkit.ui.view.input.ActionConstants;
import com.netease.yunxin.kit.common.utils.SizeUtils;
import com.netease.yunxin.kit.corekit.im2.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import java.util.ArrayList;

/** 标准皮肤，收藏列表页面。 */
public class CollectionActivity extends CollectionBaseActivity {

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    changeStatusBarColor(R.color.color_eef1f4);
  }

  @Override
  protected void initView() {
    super.initView();
    viewBinding.getRoot().setBackgroundColor(getResources().getColor(R.color.color_eef1f4));
    collectionAdapter.setViewHolderFactory(new CollectionViewHolderFactory());
  }

  @Override
  protected void goToForwardPage() {
    ChatUtils.startForwardSelector(
        this, RouterConstant.PATH_FORWARD_SELECTOR_PAGE, false, forwardLauncher);
  }

  // 跳转到聊天页面
  public void jumpCollectionDetail(CollectionBean bean) {
    CollectionDetailActivity.launch(this, bean);
  }

  // 列表分割线
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

  // 点击自定义消息
  @Override
  protected void clickCustomMessage(CollectionBean messageBean) {
    if (messageBean.getMessageData().getMessageType()
        == V2NIMMessageType.V2NIM_MESSAGE_TYPE_CUSTOM) {
      if (messageBean.getCustomAttachment() instanceof MultiForwardAttachment) {
        XKitRouter.withKey(RouterConstant.PATH_CHAT_FORWARD_PAGE)
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
    ChatMessageForwardConfirmDialog confirmDialog =
        ChatMessageForwardConfirmDialog.createForwardConfirmDialog(
            conversationIds, "", sendTips, true, ActionConstants.POP_ACTION_TRANSMIT);
    confirmDialog.setCallback(
        (input) -> {
          if (forwardMessage != null) {
            for (String accId : conversationIds) {
              viewModel.sendForwardMessage(forwardMessage.getMessageData(), input, accId);
            }
          }
        });
    confirmDialog.show(getSupportFragmentManager(), TAG);
  }
}
