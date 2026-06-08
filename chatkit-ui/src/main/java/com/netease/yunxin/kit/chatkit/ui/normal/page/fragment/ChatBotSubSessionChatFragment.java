// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.normal.page.fragment;

import static com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant.LIB_TAG;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import com.netease.nimlib.sdk.v2.conversation.enums.V2NIMConversationType;
import com.netease.nimlib.sdk.v2.topic.V2NIMTopic;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.repo.ConversationRepo;
import com.netease.yunxin.kit.chatkit.repo.LocalConversationRepo;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.common.BotSubSessionMoreActionHelper;
import com.netease.yunxin.kit.chatkit.ui.common.BotSubSessionUtils;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatBotSubSessionChatFragmentBinding;
import com.netease.yunxin.kit.chatkit.ui.interfaces.IMessageLoadHandler;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.chatkit.ui.normal.view.MessageBottomLayout;
import com.netease.yunxin.kit.chatkit.ui.page.viewmodel.ChatBotSubSessionViewModel;
import com.netease.yunxin.kit.chatkit.ui.view.input.ActionConstants;
import com.netease.yunxin.kit.chatkit.ui.view.message.ChatMessageListView;
import com.netease.yunxin.kit.chatkit.utils.ConversationIdUtils;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.corekit.im2.IMKitClient;
import com.netease.yunxin.kit.corekit.im2.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.model.PluginAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChatBotSubSessionChatFragment extends ChatP2PFragment {

  private static final String TAG = "ChatBotSubSessionChatFragment";
  protected V2NIMTopic topic;
  protected String topicConversationId;
  protected String conversationDisplayName;
  private ChatBotSubSessionChatFragmentBinding botSubSessionBinding;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (getArguments() != null) {
      topic = (V2NIMTopic) getArguments().getSerializable(RouterConstant.KEY_BOT_SUB_SESSION_TOPIC);
      topicConversationId =
          getArguments().getString(RouterConstant.KEY_BOT_SUB_SESSION_CONVERSATION_ID);
      conversationDisplayName = getArguments().getString(RouterConstant.KEY_SESSION_NAME);
    }
  }

  @Override
  public View initViewAndGetRootView(
      @NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
    botSubSessionBinding = ChatBotSubSessionChatFragmentBinding.inflate(inflater, container, false);
    chatView = botSubSessionBinding.chatView;
    chatView.getTitleBar().getTitleTextView().setEllipsize(TextUtils.TruncateAt.MIDDLE);
    chatView
        .getMessageListView()
        .addOnListViewEventListener(
            new ChatMessageListView.OnListViewEventListener() {
              @Override
              public void onListViewStartScroll() {}

              @Override
              public void onListViewTouched() {}

              @Override
              public void onListViewScrolling() {}

              @Override
              public void onListViewScrollEnd() {
                if (!chatView.getMessageListView().hasMoreNewerMessages()
                    && chatView.getMessageListView().isLastItemVisible()) {
                  showMessageScrollToBottom(false);
                } else {
                  showMessageScrollToBottom(true);
                }
              }
            });
    botSubSessionBinding.messageTipsLayout.setOnClickListener(
        v -> {
          if (!chatView.getMessageListView().hasMoreNewerMessages()) {
            chatView.getMessageListView().scrollToEnd();
          } else {
            reloadMessageList();
          }
          showMessageScrollToBottom(false);
        });
    return botSubSessionBinding.getRoot();
  }

  @Override
  protected void initView() {
    super.initView();
    chatView.setLoadHandler(
        new IMessageLoadHandler() {
          @Override
          public void loadMoreForward(ChatMessageBean messageInfo) {
            getSubSessionViewModel().getMoreTopicMessages();
          }

          @Override
          public void loadMoreBackground(ChatMessageBean messageInfo) {
            getSubSessionViewModel().getNewerTopicMessages();
          }

          @Override
          public void onVisibleItemChange(List<ChatMessageBean> messages) {}
        });
    chatView
        .getTitleBar()
        .setActionListener(
            v -> {
              chatView.hideCurrentInput();
              showMoreActionDialog();
            });
  }

  @Override
  protected void initViewModel() {
    ALog.d(LIB_TAG, TAG, "initViewModel");
    viewModel = new ViewModelProvider(this).get(ChatBotSubSessionViewModel.class);
    viewModel.init(accountId, V2NIMConversationType.V2NIM_CONVERSATION_TYPE_P2P);
    getSubSessionViewModel().initTopic(topic, topicConversationId);
    getSubSessionViewModel()
        .getTopicLiveData()
        .observe(
            getViewLifecycleOwner(),
            updatedTopic -> {
              topic = updatedTopic;
              refreshView();
            });
    getSubSessionViewModel()
        .getTopicRemovedLiveData()
        .observe(
            getViewLifecycleOwner(),
            removed -> {
              if (Boolean.TRUE.equals(removed)) {
                ToastX.showShortToast(R.string.chat_bot_sub_session_removed);
                requireActivity().finish();
              }
            });
    clearConversationUnread();
  }

  private void clearConversationUnread() {
    if (viewModel == null || TextUtils.isEmpty(viewModel.getConversationId())) {
      return;
    }
    if (IMKitClient.enableV2CloudConversation()) {
      ConversationRepo.clearUnreadCountByIds(
          Collections.singletonList(viewModel.getConversationId()), null);
    } else {
      LocalConversationRepo.clearUnreadCountByIds(
          Collections.singletonList(viewModel.getConversationId()), null);
    }
  }

  @Override
  protected void initData() {
    getSubSessionViewModel()
        .getTopicData(anchorMessage == null ? null : anchorMessage.getMessage());
  }

  @Override
  protected void reloadMessagesAfterDeleteAll() {
    getSubSessionViewModel().getTopicData(null);
  }

  @Override
  protected List<PluginAction> filterMessagePopMenuActions(
      @NonNull List<PluginAction> actions, @NonNull ChatMessageBean messageBean) {
    List<PluginAction> filteredActions = new ArrayList<>();
    for (PluginAction action : actions) {
      String actionType = action.getAction();
      if (!TextUtils.equals(actionType, ActionConstants.POP_ACTION_PIN)
          && !TextUtils.equals(actionType, ActionConstants.POP_ACTION_RECALL)) {
        filteredActions.add(action);
      }
    }
    return filteredActions;
  }

  @Override
  public void refreshView() {
    if (TextUtils.isEmpty(accountId)) {
      return;
    }
    mConversationId = ConversationIdUtils.conversationId(accountId, conversationType);
    String title = getDisplayTitle();
    chatView.updateInputHintInfo(title);
    chatView.getTitleBar().setTitle(title);
    chatView
        .getTitleBar()
        .getActionImageView()
        .setVisibility(topic == null ? View.GONE : View.VISIBLE);
    chatView.notifyUserInfoChanged(Collections.singletonList(accountId));
    refreshGuideView();
  }

  private void refreshGuideView() {
    if (botSubSessionBinding == null) {
      return;
    }
    boolean hasMessage =
        chatView != null
            && chatView.getMessageListView() != null
            && chatView.getMessageListView().getMessageAdapter() != null
            && chatView.getMessageListView().getMessageAdapter().getItemCount() > 0;
    boolean showGuide = topic == null && !hasMessage;
    botSubSessionBinding.botSubSessionGuideLayout.setVisibility(
        showGuide ? View.VISIBLE : View.GONE);
    if (showGuide) {
      botSubSessionBinding.botSubSessionGuideText.setText(
          getString(R.string.chat_bot_sub_session_guide, getGuideDisplayTitle()));
    }
  }

  private String getDisplayTitle() {
    if (topic != null) {
      return BotSubSessionUtils.getTopicTitle(requireContext(), topic);
    }
    return getString(R.string.chat_bot_sub_session_new);
  }

  private String getGuideDisplayTitle() {
    if (topic != null) {
      return BotSubSessionUtils.getTopicTitle(requireContext(), topic);
    }
    String conversationName = getConversationName(true);
    if (!TextUtils.isEmpty(conversationName)) {
      return conversationName;
    }
    if (!TextUtils.isEmpty(conversationDisplayName)) {
      return conversationDisplayName;
    }
    return accountId;
  }

  @Override
  protected void onSentMessage(FetchResult<ChatMessageBean> fetchResult) {
    super.onSentMessage(fetchResult);
    refreshGuideView();
  }

  @Override
  public void showMessageScrollToBottom(boolean isShow) {
    if (botSubSessionBinding == null) {
      return;
    }
    if (isShow && !chatView.isMultiSelect()) {
      botSubSessionBinding.messageTipsLayout.setVisibility(View.VISIBLE);
      botSubSessionBinding.messageTipsLayout.setBackgroundResource(
          R.drawable.bg_shape_circle_radius);
      FrameLayout.LayoutParams layoutParams =
          (FrameLayout.LayoutParams) botSubSessionBinding.messageTipsLayout.getLayoutParams();
      layoutParams.setMarginEnd(getResources().getDimensionPixelSize(R.dimen.dimen_16_dp));
      botSubSessionBinding.messageTipsLayout.setLayoutParams(layoutParams);
      botSubSessionBinding.messageTipsTv.setText("");
      botSubSessionBinding.messageTipsTv.setVisibility(View.GONE);
    } else {
      botSubSessionBinding.messageTipsLayout.setVisibility(View.GONE);
    }
  }

  @Override
  public MessageBottomLayout getMessageBottomLayout() {
    return botSubSessionBinding == null
        ? null
        : botSubSessionBinding.chatView.getBottomInputLayout();
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    botSubSessionBinding = null;
  }

  protected String getSettingRoute() {
    return RouterConstant.PATH_CHAT_SETTING_PAGE;
  }

  protected String getTopicConversationId() {
    return topicConversationId;
  }

  protected void showMoreActionDialog() {
    BotSubSessionMoreActionHelper.showMoreActionDialog(
        requireContext(), topic, getSubSessionViewModel());
  }

  protected ChatBotSubSessionViewModel getSubSessionViewModel() {
    return (ChatBotSubSessionViewModel) viewModel;
  }
}
