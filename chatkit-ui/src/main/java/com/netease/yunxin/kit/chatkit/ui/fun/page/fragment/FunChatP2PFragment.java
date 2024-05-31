// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.fun.page.fragment;

import static com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant.LIB_TAG;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.ViewTreeObserver;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import com.netease.nimlib.sdk.v2.conversation.enums.V2NIMConversationType;
import com.netease.nimlib.sdk.v2.message.V2NIMMessage;
import com.netease.nimlib.sdk.v2.message.V2NIMP2PMessageReadReceipt;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.model.IMMessageInfo;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.common.ChatUserCache;
import com.netease.yunxin.kit.chatkit.ui.fun.view.MessageBottomLayout;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.chatkit.ui.page.viewmodel.ChatP2PViewModel;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.corekit.im2.model.UserWithFriend;
import com.netease.yunxin.kit.corekit.im2.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;

/**
 * Fun皮肤单聊聊天界面Fragment，继承自FunChatFragment
 * 单聊和群聊有一些差异，为了方便维护，将差异化的部分抽象到FunChatP2PFragment和FunChatTeamFragment中
 */
public class FunChatP2PFragment extends FunChatFragment {
  private static final String TAG = "ChatP2PFunFragment";

  private static final int TYPE_DELAY_TIME = 3000;

  public UserWithFriend friendInfo;

  public IMMessageInfo anchorMessage;

  private final Handler handler = new Handler();

  private final Runnable stopTypingRunnable = () -> chatView.setTypeState(false);

  protected Observer<V2NIMP2PMessageReadReceipt> p2pReceiptObserver;

  @Override
  protected void initData(Bundle bundle) {
    ALog.d(LIB_TAG, TAG, "initData");
    conversationType = V2NIMConversationType.V2NIM_CONVERSATION_TYPE_P2P;
    accountId = (String) bundle.getSerializable(RouterConstant.CHAT_ID_KRY);
    if (TextUtils.isEmpty(accountId)) {
      requireActivity().finish();
      return;
    }
    anchorMessage = (IMMessageInfo) bundle.getSerializable(RouterConstant.KEY_MESSAGE_INFO);
    if (anchorMessage == null) {
      V2NIMMessage message = (V2NIMMessage) bundle.getSerializable(RouterConstant.KEY_MESSAGE);
      if (message != null) {
        anchorMessage = new IMMessageInfo(message);
      }
    }
    refreshView();
  }

  @Override
  protected void initView() {
    super.initView();
    chatView
        .getTitleBar()
        .setOnBackIconClickListener(v -> requireActivity().onBackPressed())
        .setActionImg(R.drawable.ic_more_point)
        .setActionListener(
            v -> {
              chatView.hideCurrentInput();
              XKitRouter.withKey(RouterConstant.PATH_FUN_CHAT_SETTING_PAGE)
                  .withParam(RouterConstant.CHAT_ID_KRY, accountId)
                  .withContext(requireActivity())
                  .navigate();
            });
  }

  public void refreshView() {
    String name = accountId;
    if (friendInfo != null) {
      name = friendInfo.getName();
    }
    chatView.getTitleBar().setTitle(name);
  }

  @Override
  protected void initViewModel() {
    ALog.d(LIB_TAG, TAG, "initViewModel");
    viewModel = new ViewModelProvider(this).get(ChatP2PViewModel.class);
    viewModel.init(accountId, V2NIMConversationType.V2NIM_CONVERSATION_TYPE_P2P);

    if (chatConfig != null && chatConfig.chatListener != null) {
      chatConfig.chatListener.onConversationChange(accountId, conversationType);
    }
    if (chatConfig != null && chatConfig.messageProperties != null) {
      viewModel.setShowReadStatus(chatConfig.messageProperties.showP2pMessageStatus);
    }
  }

  @Override
  protected void initData() {
    if (viewModel instanceof ChatP2PViewModel) {
      if (anchorMessage != null) {
        ((ChatP2PViewModel) viewModel).getP2PData(anchorMessage.getMessage());
      } else {
        ((ChatP2PViewModel) viewModel).getP2PData(null);
      }
    }
  }

  @Override
  protected void updateDataWhenLogin() {}

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    ((ChatP2PViewModel) viewModel).getMessageReceiptLiveData().removeObserver(p2pReceiptObserver);
  }

  @Override
  protected void initDataObserver() {
    super.initDataObserver();
    ALog.d(LIB_TAG, TAG, "initDataObserver");
    p2pReceiptObserver =
        imMessageReceiptInfo ->
            chatView.getMessageListView().setP2PReceipt(imMessageReceiptInfo.getTimestamp());
    ((ChatP2PViewModel) viewModel).getMessageReceiptLiveData().observeForever(p2pReceiptObserver);

    ((ChatP2PViewModel) viewModel)
        .getTypeStateLiveData()
        .observe(
            getViewLifecycleOwner(),
            isTyping -> {
              handler.removeCallbacks(stopTypingRunnable);
              chatView.setTypeState(isTyping);
              if (isTyping) {
                handler.postDelayed(stopTypingRunnable, TYPE_DELAY_TIME);
              }
            });
    ((ChatP2PViewModel) viewModel)
        .getFriendInfoLiveData()
        .observe(
            getViewLifecycleOwner(),
            result -> {
              if (result.getLoadStatus() == LoadStatus.Success) {
                friendInfo = result.getData();
                refreshView();
              }
            });
  }

  @Override
  public void onNewIntent(Intent intent) {
    ALog.d(LIB_TAG, TAG, "onNewIntent");
    anchorMessage = (IMMessageInfo) intent.getSerializableExtra(RouterConstant.KEY_MESSAGE_INFO);
    if (anchorMessage == null) {
      V2NIMMessage message = (V2NIMMessage) intent.getSerializableExtra(RouterConstant.KEY_MESSAGE);
      if (message != null) {
        anchorMessage = new IMMessageInfo(message);
      }
    }
    ChatMessageBean anchorMessageBean = null;
    //        (V2ChatMessageBean) intent.getSerializableExtra(RouterConstant.KEY_MESSAGE_BEAN);
    if (anchorMessage != null) {
      anchorMessageBean = new ChatMessageBean(anchorMessage);
    }
    if (anchorMessage != null) {
      int position =
          chatView
              .getMessageListView()
              .searchMessagePosition(anchorMessage.getMessage().getMessageClientId());
      if (position >= 0) {
        chatView
            .getMessageListView()
            .getViewTreeObserver()
            .addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                  @Override
                  public void onGlobalLayout() {
                    chatView
                        .getMessageListView()
                        .getViewTreeObserver()
                        .removeOnGlobalLayoutListener(this);
                    chatView
                        .getRootView()
                        .post(() -> chatView.getMessageListView().scrollToPosition(position));
                  }
                });
        chatView.getMessageListView().scrollToPosition(position);
      } else {
        chatView.clearMessageList();
        // need to add anchor message to list panel
        chatView.appendMessage(anchorMessageBean);
        viewModel.getMessageList(anchorMessage.getMessage(), false);
      }
    }
  }

  public MessageBottomLayout getMessageBottomLayout() {
    return viewBinding.chatView.getBottomInputLayout();
  }

  @Override
  public String getConversationName() {
    if (friendInfo != null) {
      return friendInfo.getName();
    }
    return super.getConversationName();
  }

  @Override
  public void updateCurrentUserInfo() {
    UserWithFriend friendInfo = ChatUserCache.getInstance().getFriendInfo(accountId);
    if (friendInfo != null) {
      this.friendInfo = friendInfo;
    }

    refreshView();
  }
}
