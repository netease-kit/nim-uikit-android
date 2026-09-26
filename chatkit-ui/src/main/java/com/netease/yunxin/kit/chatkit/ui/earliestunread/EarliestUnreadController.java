// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.earliestunread;

import static com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant.LIB_TAG;

import android.text.TextUtils;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.model.IMMessageInfo;
import com.netease.yunxin.kit.chatkit.ui.interfaces.IChatView;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.chatkit.ui.page.viewmodel.ChatBaseViewModel;
import com.netease.yunxin.kit.corekit.im2.extend.FetchCallback;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Owns the earliest-unread reminder state and orchestration for one chat page instance. */
public final class EarliestUnreadController {

  private static final String LOG_TAG = "EarliestUnreadController";
  private static final int MAX_CLICK_LOCATION_CHECKS = 5;

  public interface Host {
    boolean isPageActive();

    boolean supportsEarliestUnread();

    IChatView getChatView();

    ChatBaseViewModel getViewModel();

    void initializeChat(boolean unreadAlreadyCleared);

    void updateEarliestUnreadEntry(boolean visible, int count);

    void showLocationFailure();
  }

  private final Host host;
  private final boolean enabled;
  private final Runnable stateRunnable = this::updateState;

  private boolean readTimeReady;
  private long frozenReadTime;
  private int frozenUnreadCount;
  private long frozenEarliestUnreadTime;
  @Nullable private IMMessageInfo frozenEarliestUnreadMessage;
  private boolean countInitialized;
  private boolean consumed;
  private boolean locating;
  private String earliestUnreadMessageId;
  private final Set<String> snapshotUnreadMessageIds = new HashSet<>();
  private final Set<String> shownUnreadMessageIds = new HashSet<>();
  private boolean countTruncated;
  private int generation;
  private int countRequestId;
  private int locateRequestId;
  private int clickLocationCheckCount;

  public EarliestUnreadController(Host host, boolean globalEnabled) {
    this.host = host;
    this.enabled = globalEnabled && host.supportsEarliestUnread();
  }

  public void prepareConversation() {
    final int requestId = ++generation;
    if (!enabled) {
      host.initializeChat(false);
      return;
    }
    host.getViewModel()
        .getConversationReadTime(
            new FetchCallback<Long>() {
              @Override
              public void onSuccess(@Nullable Long data) {
                if (!isCurrent(requestId)) return;
                if (data == null) {
                  readTimeReady = false;
                  host.initializeChat(false);
                  return;
                }
                frozenReadTime = Math.max(0L, data);
                readTimeReady = true;
                prepareUnreadCount(requestId);
              }

              @Override
              public void onError(int errorCode, @Nullable String errorMsg) {
                if (!isCurrent(requestId)) return;
                ALog.w(LIB_TAG, LOG_TAG, "get conversation read time failed:" + errorCode);
                readTimeReady = false;
                host.initializeChat(false);
              }
            });
  }

  private void prepareUnreadCount(int requestId) {
    final int queryId = ++countRequestId;
    host.getViewModel()
        .getConversationUnreadCount(
            new FetchCallback<Integer>() {
              @Override
              public void onSuccess(@Nullable Integer unreadCount) {
                if (!isCurrent(requestId, queryId)) return;
                if (unreadCount == null || unreadCount <= 0) {
                  countInitialized = true;
                  finishPreparation(queryId);
                  return;
                }
                queryUnreadMessages(queryId, requestId);
              }

              @Override
              public void onError(int errorCode, @Nullable String errorMsg) {
                if (!isCurrent(requestId, queryId)) return;
                handleCountError(queryId, errorCode);
                finishPreparation(queryId);
              }
            });
  }

  private void queryUnreadMessages(int queryId, int requestId) {
    host.getViewModel()
        .queryEarliestUnreadInfo(
            frozenReadTime,
            new FetchCallback<ChatBaseViewModel.EarliestUnreadInfo>() {
              @Override
              public void onSuccess(@Nullable ChatBaseViewModel.EarliestUnreadInfo data) {
                if (!isCurrent(requestId, queryId)) return;
                frozenUnreadCount = data == null ? 0 : data.getCount();
                frozenEarliestUnreadTime = data == null ? 0L : data.getEarliestUnreadTime();
                frozenEarliestUnreadMessage = data == null ? null : data.getEarliestUnreadMessage();
                snapshotUnreadMessageIds.clear();
                shownUnreadMessageIds.clear();
                countTruncated = data != null && data.isCountTruncated();
                if (data != null && data.getEffectiveUnreadMessageIds() != null) {
                  snapshotUnreadMessageIds.addAll(data.getEffectiveUnreadMessageIds());
                }
                earliestUnreadMessageId =
                    frozenEarliestUnreadMessage == null
                            || frozenEarliestUnreadMessage.getMessage() == null
                        ? null
                        : frozenEarliestUnreadMessage.getMessage().getMessageClientId();
                countInitialized = true;
                finishPreparation(queryId);
              }

              @Override
              public void onError(int errorCode, @Nullable String errorMsg) {
                if (!isCurrent(requestId, queryId)) return;
                handleCountError(queryId, errorCode);
                finishPreparation(queryId);
              }
            });
  }

  private void finishPreparation(int queryId) {
    if (queryId != countRequestId || !host.isPageActive()) return;
    host.getViewModel()
        .clearConversationUnreadCount(
            new FetchCallback<Void>() {
              @Override
              public void onSuccess(Void data) {
                if (queryId == countRequestId && host.isPageActive()) {
                  host.initializeChat(true);
                }
              }

              @Override
              public void onError(int errorCode, @Nullable String errorMsg) {
                if (queryId == countRequestId && host.isPageActive()) {
                  host.initializeChat(true);
                }
              }
            });
  }

  public void onMessageListChanged() {
    IChatView chatView = host.getChatView();
    if (chatView == null) return;
    chatView.getMessageListView().post(stateRunnable);
    if (locating) {
      final int requestId = locateRequestId;
      chatView.getMessageListView().post(() -> completeLocation(requestId));
    }
  }

  public void onScroll() {
    IChatView chatView = host.getChatView();
    if (chatView != null) chatView.getMessageListView().post(stateRunnable);
  }

  public void onMultiSelectChanged(boolean multiSelect) {
    if (multiSelect) {
      host.updateEarliestUnreadEntry(false, 0);
    } else {
      onScroll();
    }
  }

  public void onEntryClick() {
    if (!enabled || !host.supportsEarliestUnread() || locating || frozenUnreadCount <= 0) return;
    IChatView chatView = host.getChatView();
    ChatBaseViewModel viewModel = host.getViewModel();
    if (chatView == null || viewModel == null) return;
    if (frozenEarliestUnreadMessage == null
        || frozenEarliestUnreadMessage.getMessage() == null
        || TextUtils.isEmpty(earliestUnreadMessageId)) {
      return;
    }
    List<ChatMessageBean> messages = chatView.getMessageList();
    int position = -1;
    if (messages != null) {
      for (int index = 0; index < messages.size(); index++) {
        ChatMessageBean message = messages.get(index);
        if (message != null
            && TextUtils.equals(message.getMsgClientId(), earliestUnreadMessageId)) {
          position = index;
          break;
        }
      }
    }
    if (position >= 0) {
      locating = true;
      final int requestId = ++locateRequestId;
      clickLocationCheckCount = 0;
      chatView.getMessageListView().scrollToPosition(position);
      scheduleClickLocationCheck(requestId);
      return;
    }
    locating = true;
    final int requestId = ++locateRequestId;
    clickLocationCheckCount = 0;
    IChatView currentChatView = host.getChatView();
    if (currentChatView == null) {
      locating = false;
      return;
    }
    currentChatView.clearMessageList();
    currentChatView.appendMessage(new ChatMessageBean(frozenEarliestUnreadMessage));
    viewModel.getMessageListForEarliestUnread(
        frozenEarliestUnreadMessage.getMessage(),
        new FetchCallback<Void>() {
          @Override
          public void onSuccess(@Nullable Void data) {
            if (!isCurrentLocation(requestId) || !locating) return;
            IChatView loadedChatView = host.getChatView();
            if (loadedChatView != null) {
              loadedChatView.getMessageListView().post(() -> completeLocation(requestId));
            }
          }

          @Override
          public void onError(int errorCode, @Nullable String errorMsg) {
            if (!isCurrentLocation(requestId) || !locating) return;
            handleLocationFailure();
          }
        });
  }

  public void onDestroyView() {
    IChatView chatView = host.getChatView();
    if (chatView != null) chatView.getMessageListView().removeCallbacks(stateRunnable);
    generation++;
    countRequestId++;
    locateRequestId++;
    locating = false;
  }

  private void handleCountError(int requestId, int errorCode) {
    if (!isCurrentCount(requestId)) return;
    countInitialized = true;
    frozenUnreadCount = 0;
    frozenEarliestUnreadTime = 0L;
    ALog.w(LIB_TAG, LOG_TAG, "query earliest unread count failed:" + errorCode);
    renderEntry(false, 0);
  }

  private void updateState() {
    if (!host.isPageActive()) return;
    IChatView chatView = host.getChatView();
    if (!enabled || chatView == null || !readTimeReady || consumed || !countInitialized) {
      renderEntry(false, 0);
      return;
    }
    List<ChatMessageBean> messages =
        chatView.getMessageListView().getMessageAdapter().getMessageList();
    if (messages == null || frozenUnreadCount <= 0 || frozenEarliestUnreadTime <= frozenReadTime) {
      renderEntry(false, 0);
      return;
    }
    LinearLayoutManager layoutManager =
        (LinearLayoutManager) chatView.getMessageListView().getLayoutManager();
    if (layoutManager != null) {
      if (isReadBoundaryVisible(layoutManager, messages)) {
        consumed = true;
        renderEntry(false, 0);
        return;
      }
      markVisibleSnapshotMessages(layoutManager, messages);
    }
    if (!countTruncated
        && !snapshotUnreadMessageIds.isEmpty()
        && shownUnreadMessageIds.size() >= snapshotUnreadMessageIds.size()) {
      consumed = true;
      renderEntry(false, 0);
      return;
    }
    renderEntry(true, frozenUnreadCount);
  }

  private boolean isReadBoundaryVisible(
      LinearLayoutManager layoutManager, List<ChatMessageBean> messages) {
    int firstVisiblePosition = layoutManager.findFirstVisibleItemPosition();
    if (firstVisiblePosition < 0 || firstVisiblePosition >= messages.size()) return false;
    ChatMessageBean firstVisibleMessage = messages.get(firstVisiblePosition);
    return firstVisibleMessage != null
        && firstVisibleMessage.getMessageData() != null
        && firstVisibleMessage.getMessageData().getMessage() != null
        && firstVisibleMessage.getMessageData().getMessage().getCreateTime() <= frozenReadTime;
  }

  private void markVisibleSnapshotMessages(
      LinearLayoutManager layoutManager, List<ChatMessageBean> messages) {
    int firstVisiblePosition = Math.max(layoutManager.findFirstVisibleItemPosition(), 0);
    int lastVisiblePosition =
        Math.min(layoutManager.findLastVisibleItemPosition(), messages.size() - 1);
    for (int position = firstVisiblePosition; position <= lastVisiblePosition; position++) {
      ChatMessageBean message = messages.get(position);
      if (message != null && snapshotUnreadMessageIds.contains(message.getMsgClientId())) {
        shownUnreadMessageIds.add(message.getMsgClientId());
      }
    }
  }

  private void renderEntry(boolean visible, int count) {
    IChatView chatView = host.getChatView();
    host.updateEarliestUnreadEntry(
        (chatView == null || !chatView.isMultiSelect()) && visible, count);
  }

  private void completeLocation(int requestId) {
    if (!isCurrentLocation(requestId) || !locating) return;
    IChatView chatView = host.getChatView();
    if (chatView == null) return;
    List<ChatMessageBean> messages = chatView.getMessageList();
    if (messages == null) return;
    int position = chatView.getMessageListView().searchMessagePosition(earliestUnreadMessageId);
    if (position < 0) {
      handleLocationFailure();
      return;
    }
    chatView.getMessageListView().scrollToPosition(position);
    scheduleClickLocationCheck(requestId);
  }

  private void scheduleClickLocationCheck(int requestId) {
    IChatView chatView = host.getChatView();
    if (chatView == null) return;
    chatView.getMessageListView().postOnAnimation(() -> completeClickLocation(requestId));
  }

  private void completeClickLocation(int requestId) {
    if (!isCurrentLocation(requestId) || !locating) return;
    IChatView chatView = host.getChatView();
    if (chatView == null || TextUtils.isEmpty(earliestUnreadMessageId)) return;
    int targetPosition =
        chatView.getMessageListView().searchMessagePosition(earliestUnreadMessageId);
    LinearLayoutManager layoutManager =
        (LinearLayoutManager) chatView.getMessageListView().getLayoutManager();
    if (targetPosition >= 0
        && layoutManager != null
        && targetPosition >= layoutManager.findFirstVisibleItemPosition()
        && targetPosition <= layoutManager.findLastVisibleItemPosition()) {
      locating = false;
      consumed = true;
      renderEntry(false, 0);
      return;
    }
    if (clickLocationCheckCount++ < MAX_CLICK_LOCATION_CHECKS) {
      scheduleClickLocationCheck(requestId);
      return;
    }
    handleLocationFailure();
  }

  private void handleLocationFailure() {
    locating = false;
    host.showLocationFailure();
    renderEntry(true, frozenUnreadCount);
  }

  private boolean isCurrent(int requestId) {
    return requestId == generation && host.isPageActive();
  }

  private boolean isCurrent(int requestId, int queryId) {
    return requestId == generation && queryId == countRequestId && host.isPageActive();
  }

  private boolean isCurrentCount(int requestId) {
    return enabled && requestId == countRequestId && host.isPageActive();
  }

  private boolean isCurrentLocation(int requestId) {
    return requestId == locateRequestId && host.isPageActive();
  }
}
