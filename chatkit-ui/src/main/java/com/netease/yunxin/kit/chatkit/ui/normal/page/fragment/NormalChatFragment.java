// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.normal.page.fragment;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.netease.nimlib.sdk.v2.message.enums.V2NIMMessageType;
import com.netease.yunxin.kit.chatkit.model.IMMessageInfo;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.common.ChatMsgCache;
import com.netease.yunxin.kit.chatkit.ui.common.ChatUtils;
import com.netease.yunxin.kit.chatkit.ui.custom.MultiForwardAttachment;
import com.netease.yunxin.kit.chatkit.ui.databinding.NormalChatFragmentBinding;
import com.netease.yunxin.kit.chatkit.ui.dialog.ChatBaseForwardSelectDialog;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.chatkit.ui.model.MessageRevokeInfo;
import com.netease.yunxin.kit.chatkit.ui.normal.ChatMessageForwardConfirmDialog;
import com.netease.yunxin.kit.chatkit.ui.normal.ChatMessageForwardSelectDialog;
import com.netease.yunxin.kit.chatkit.ui.page.fragment.ChatBaseFragment;
import com.netease.yunxin.kit.chatkit.ui.view.input.ActionConstants;
import com.netease.yunxin.kit.chatkit.ui.view.message.ChatMessageListView;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.utils.NetworkUtils;
import com.netease.yunxin.kit.corekit.im2.IMKitClient;
import com.netease.yunxin.kit.corekit.im2.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import java.util.ArrayList;
import java.util.List;

/** 标准皮肤，单聊和群聊会话页面Fragment的父类。 */
public abstract class NormalChatFragment extends ChatBaseFragment {
  private static final String TAG = "NormalChatFragment";
  NormalChatFragmentBinding viewBinding;
  private List<ChatMessageBean> lastMessageList = new ArrayList<>();

  /** 用于收到新消息后延迟执行"追加消息并滚动到底部"的 Handler，防止短时间大量消息导致滚动逻辑错乱 */
  private final Handler scrollHandler = new Handler(Looper.getMainLooper());

  /** 待追加的消息缓冲区，由 Handler 统一批量处理 */
  private final List<ChatMessageBean> pendingMessages = new ArrayList<>();

  /** 延迟时间（ms），在此时间窗口内收到的消息会被合并为一次追加+滚动操作 */
  private static final long SCROLL_DEBOUNCE_MS = 100;

  /** 标记 Handler 中当前是否有待执行的防抖任务（即用户当时处于列表底部） */
  private boolean hasPendingScrollTask = false;

  /** 执行追加消息并滚动到底部的 Runnable */
  private final Runnable appendAndScrollRunnable =
      () -> {
        hasPendingScrollTask = false;
        if (pendingMessages.isEmpty()) return;
        List<ChatMessageBean> batch = new ArrayList<>(pendingMessages);
        pendingMessages.clear();
        chatView.appendMessageList(batch, true);
      };

  @Override
  public View initViewAndGetRootView(
      @NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
    viewBinding = NormalChatFragmentBinding.inflate(inflater, container, false);
    chatView = viewBinding.chatView;
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
                if (!lastMessageList.isEmpty()) {
                  ChatMessageBean lastMessage =
                      chatView.getMessageListView().getLastCompletelyVisibleMessage();
                  if (lastMessage != null
                      && lastMessage.getCreateTime() >= lastMessageList.get(0).getCreateTime()) {
                    lastMessageList.clear();
                  }
                }
                if (!chatView.getMessageListView().hasMoreNewerMessages()
                    && chatView.getMessageListView().isLastItemVisible()) {
                  showMessageScrollToBottom(false);
                } else {
                  showMessageScrollToBottom(true);
                }
              }
            });
    viewBinding.messageTipsLayout.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            if (!chatView.getMessageListView().hasMoreNewerMessages()) {
              chatView.getMessageListView().scrollToEnd();
            } else {
              reloadMessageList();
            }
            lastMessageList.clear();
            showMessageScrollToBottom(false);
          }
        });
    return viewBinding.getRoot();
  }

  @Override
  protected void initViewModel() {}

  @Override
  protected void onReceiveMessage(FetchResult<List<ChatMessageBean>> listFetchResult) {
    if (listFetchResult.getData() == null || listFetchResult.getData().isEmpty()) {
      return;
    }

    // 如果 Handler 中已有待执行的防抖任务，说明用户此前已在底部且消息正在缓冲中，
    // 直接将新消息追加进缓冲区并重置防抖计时，无需再查询 isLastItemVisible() 状态。
    if (hasPendingScrollTask) {
      pendingMessages.addAll(listFetchResult.getData());
      scrollHandler.removeCallbacks(appendAndScrollRunnable);
      scrollHandler.postDelayed(appendAndScrollRunnable, SCROLL_DEBOUNCE_MS);
      return;
    }

    // Handler 中没有任务时，实时判断用户是否在列表底部
    boolean hasMoreNewer = chatView.getMessageListView().hasMoreNewerMessages();
    boolean isAtBottom = !hasMoreNewer && chatView.getMessageListView().isLastItemVisible();

    if (isForeground && isAtBottom) {
      // 在前台且在底部：进入防抖缓冲，合并短时间内多条消息为一次追加+滚动
      hasPendingScrollTask = true;
      pendingMessages.addAll(listFetchResult.getData());
      scrollHandler.removeCallbacks(appendAndScrollRunnable);
      scrollHandler.postDelayed(appendAndScrollRunnable, SCROLL_DEBOUNCE_MS);
    } else {
      // 不在前台，或者用户已滑动到上方，或还有更新消息未加载：追加消息但不滚动，记录未读提示
      for (ChatMessageBean message : listFetchResult.getData()) {
        if (!TextUtils.equals(message.getSenderId(), IMKitClient.account())) {
          lastMessageList.add(message);
        }
      }
      boolean isListScrollable =
          chatView.getMessageListView().canScrollVertically(-1)
              || chatView.getMessageListView().canScrollVertically(1);
      if (isListScrollable) {
        showMessageScrollToBottom(true);
      }
      chatView.appendMessageList(listFetchResult.getData(), false);
    }
  }

  @Override
  protected void onSentMessage(FetchResult<ChatMessageBean> fetchResult) {
    super.onSentMessage(fetchResult);
    if (fetchResult.getType() == FetchResult.FetchType.Add) {
      if (isForeground) {
        showMessageScrollToBottom(false);
        lastMessageList.clear();
      } else {
        showMessageScrollToBottom(true);
      }
    }
  }

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
            conversationIds, getConversationName(false), true, forwardAction);
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
                getConversationName(false),
                inputMsg,
                conversationIds,
                ChatMsgCache.getMessageList());
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
  public String getConversationName(boolean useNick) {
    return accountId;
  }

  @Override
  protected void onRevokeMessage(FetchResult<List<MessageRevokeInfo>> fetchResult) {
    super.onRevokeMessage(fetchResult);
    if (fetchResult.getData() == null
        || fetchResult.getData().isEmpty()
        || lastMessageList.isEmpty()) {
      return;
    }
    for (MessageRevokeInfo revokeInfo : fetchResult.getData()) {
      String clientId = revokeInfo.getRevokeMessageClientId();
      for (int index = lastMessageList.size() - 1; index >= 0; index--) {
        ChatMessageBean message = lastMessageList.get(index);
        if (TextUtils.equals(clientId, message.getMsgClientId())) {
          lastMessageList.remove(index);
        }
      }
    }
    if (!chatView.getMessageListView().isLastItemVisible()) {
      showMessageScrollToBottom(true);
    } else {
      showMessageScrollToBottom(false);
    }
  }

  @Override
  protected void onLoadMessage(FetchResult<List<ChatMessageBean>> listFetchResult) {
    super.onLoadMessage(listFetchResult);
    if (chatView.getMessageListView().hasMoreNewerMessages()) {
      showMessageScrollToBottom(true);
    } else {
      showMessageScrollToBottom(false);
    }
  }
  /**
   * 切换多选状态时，隐藏滚动条
   *
   * @param show
   */
  @Override
  protected void switchMultiSelect(boolean show) {
    super.switchMultiSelect(show);
    if (show) {
      showMessageScrollToBottom(false);
    } else {
      if (!chatView.getMessageListView().isLastItemVisible()) {
        showMessageScrollToBottom(true);
      }
    }
  }

  public void showMessageScrollToBottom(boolean isShow) {
    if (isShow && !chatView.isMultiSelect()) {
      viewBinding.messageTipsLayout.setVisibility(View.VISIBLE);
      if (lastMessageList.size() > 0) {
        FrameLayout.LayoutParams layoutParams =
            (FrameLayout.LayoutParams) viewBinding.messageTipsLayout.getLayoutParams();
        layoutParams.setMarginEnd(0);
        viewBinding.messageTipsLayout.setLayoutParams(layoutParams);
        viewBinding.messageTipsLayout.setBackgroundResource(R.drawable.bg_shape_left_radius);
        viewBinding.messageTipsTv.setVisibility(View.VISIBLE);
        viewBinding.messageTipsTv.setText(
            String.format(getString(R.string.chat_message_see_bottom), lastMessageList.size()));
      } else {
        viewBinding.messageTipsLayout.setBackgroundResource(R.drawable.bg_shape_circle_radius);
        FrameLayout.LayoutParams layoutParams =
            (FrameLayout.LayoutParams) viewBinding.messageTipsLayout.getLayoutParams();
        layoutParams.setMarginEnd(getResources().getDimensionPixelSize(R.dimen.dimen_16_dp));
        viewBinding.messageTipsLayout.setLayoutParams(layoutParams);

        viewBinding.messageTipsTv.setText("");
        viewBinding.messageTipsTv.setVisibility(View.GONE);
      }
    } else {
      viewBinding.messageTipsLayout.setVisibility(View.GONE);
    }
  }

  @Override
  public String getUserInfoRoutePath() {
    return RouterConstant.PATH_USER_INFO_PAGE;
  }
}
