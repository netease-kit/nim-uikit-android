// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.page.viewmodel;

import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import com.netease.nimlib.sdk.v2.conversation.model.V2NIMConversation;
import com.netease.nimlib.sdk.v2.conversation.result.V2NIMConversationOperationResult;
import com.netease.nimlib.sdk.v2.conversation.result.V2NIMLocalConversationOperationResult;
import com.netease.nimlib.sdk.v2.message.V2NIMMessage;
import com.netease.nimlib.sdk.v2.message.V2NIMMessageDeletedNotification;
import com.netease.nimlib.sdk.v2.message.enums.V2NIMQueryDirection;
import com.netease.nimlib.sdk.v2.message.enums.V2NIMSortOrder;
import com.netease.nimlib.sdk.v2.topic.V2NIMTopic;
import com.netease.nimlib.sdk.v2.topic.V2NIMTopicListener;
import com.netease.nimlib.sdk.v2.topic.V2NIMTopicRefer;
import com.netease.nimlib.sdk.v2.topic.option.V2NIMTopicListOption;
import com.netease.nimlib.sdk.v2.topic.option.V2NIMTopicMessageListOption;
import com.netease.nimlib.sdk.v2.topic.params.V2NIMRemoveTopicsParams;
import com.netease.nimlib.sdk.v2.topic.params.V2NIMUpdateTopicParams;
import com.netease.nimlib.sdk.v2.topic.result.V2NIMTopicListResult;
import com.netease.nimlib.sdk.v2.topic.result.V2NIMTopicMessageListResult;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.impl.ConversationListenerImpl;
import com.netease.yunxin.kit.chatkit.impl.LocalConversationListenerImpl;
import com.netease.yunxin.kit.chatkit.impl.MessageListenerImpl;
import com.netease.yunxin.kit.chatkit.listener.MessageRevokeNotification;
import com.netease.yunxin.kit.chatkit.model.IMMessageInfo;
import com.netease.yunxin.kit.chatkit.repo.ChatRepo;
import com.netease.yunxin.kit.chatkit.repo.ConversationRepo;
import com.netease.yunxin.kit.chatkit.repo.LocalConversationRepo;
import com.netease.yunxin.kit.chatkit.repo.TopicRepo;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.common.BotSubSessionUtils;
import com.netease.yunxin.kit.chatkit.ui.model.BotSubSessionItem;
import com.netease.yunxin.kit.common.ui.viewmodel.BaseViewModel;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.corekit.im2.IMKitClient;
import com.netease.yunxin.kit.corekit.im2.extend.FetchCallback;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class ChatBotSubSessionListViewModel extends BaseViewModel {

  private static final String TAG = "ChatBotSubSessionListViewModel";
  private static final int PAGE_SIZE = 30;
  private static final int SUMMARY_LIMIT = 1;
  private static final int SUMMARY_PARALLEL_LIMIT = 3;

  private final MutableLiveData<FetchResult<List<BotSubSessionItem>>> topicListLiveData =
      new MutableLiveData<>();
  private final List<V2NIMTopic> allTopics = new ArrayList<>();
  private final Map<Long, BotSubSessionItem> itemMap = new HashMap<>();
  private final Map<Long, Boolean> latestMessageFromSelfMap = new HashMap<>();
  private final Deque<V2NIMTopic> summaryQueue = new ArrayDeque<>();
  private String conversationId;
  private String nextToken;
  private boolean hasMore;
  private boolean loading;
  private boolean topicListLoaded;
  private int runningSummaryCount;
  private int summaryRequestVersion;
  private String keyword = "";
  private long conversationReadTime;
  private boolean messageListenerAdded;

  private final V2NIMTopicListener topicListener =
      new V2NIMTopicListener() {
        @Override
        public void onTopicAdded(V2NIMTopic topic) {
          if (isSameConversation(topic)) {
            topicListLoaded = true;
            upsertTopic(topic);
            publishFiltered(FetchResult.FetchType.Update, LoadStatus.Success);
            loadTopicSummaries(Collections.singletonList(topic));
          }
        }

        @Override
        public void onTopicsRemoved(List<V2NIMTopicRefer> topics) {
          if (topics == null || topics.isEmpty()) {
            return;
          }
          boolean changed = false;
          for (V2NIMTopicRefer refer : topics) {
            if (refer != null && TextUtils.equals(refer.getConversationId(), conversationId)) {
              changed |= removeTopic(refer);
            }
          }
          if (changed) {
            publishFiltered(FetchResult.FetchType.Update, LoadStatus.Success);
          }
        }

        @Override
        public void onTopicUpdated(V2NIMTopic topic) {
          if (isSameConversation(topic)) {
            topicListLoaded = true;
            upsertTopic(topic);
            BotSubSessionItem oldItem = itemMap.get(topic.getTopicId());
            itemMap.put(
                topic.getTopicId(),
                new BotSubSessionItem(
                    topic,
                    oldItem == null ? null : oldItem.getSummary(),
                    oldItem == null ? topic.getUpdateTime() : oldItem.getTime(),
                    oldItem != null && oldItem.hasUnread()));
            publishFiltered(FetchResult.FetchType.Update, LoadStatus.Success);
            loadTopicSummaries(Collections.singletonList(topic));
          }
        }
      };

  private final MessageListenerImpl messageObserver =
      new MessageListenerImpl() {
        @Override
        public void onReceiveMessages(@NonNull List<IMMessageInfo> messages) {
          if (messages == null || messages.isEmpty()) {
            return;
          }
          Set<Long> topicIds = new HashSet<>();
          for (IMMessageInfo messageInfo : messages) {
            V2NIMMessage message = messageInfo == null ? null : messageInfo.getMessage();
            collectTopicIdsForMessage(message, topicIds);
          }
          refreshTopicsByIds(topicIds);
        }

        @Override
        public void onMessageDeletedNotifications(
            @NonNull List<? extends V2NIMMessageDeletedNotification> messages) {
          // Ignore delete notifications for sub-session list refresh.
        }

        @Override
        public void onMessageRevokeNotifications(
            @Nullable List<MessageRevokeNotification> revokeNotifications) {
          refreshTopicSummariesByRevokeNotifications(revokeNotifications);
        }
      };

  private final ConversationListenerImpl conversationListener =
      new ConversationListenerImpl() {
        @Override
        public void onConversationReadTimeUpdated(@Nullable String conversationId, long readTime) {
          refreshUnreadStateIfCurrentConversation(conversationId);
        }
      };

  private final LocalConversationListenerImpl localConversationListener =
      new LocalConversationListenerImpl() {
        @Override
        public void onConversationReadTimeUpdated(@Nullable String conversationId, long readTime) {
          refreshUnreadStateIfCurrentConversation(conversationId);
        }
      };

  public MutableLiveData<FetchResult<List<BotSubSessionItem>>> getTopicListLiveData() {
    return topicListLiveData;
  }

  public void init(@NonNull String conversationId) {
    this.conversationId = conversationId;
    TopicRepo.addTopicListener(topicListener);
    addMessageListener();
    ConversationRepo.addConversationListener(conversationListener);
    LocalConversationRepo.addConversationListener(localConversationListener);
  }

  public boolean hasMore() {
    return hasMore;
  }

  public void loadTopics() {
    if (TextUtils.isEmpty(conversationId) || loading) {
      return;
    }
    loading = true;
    topicListLoaded = false;
    nextToken = null;
    hasMore = false;
    allTopics.clear();
    itemMap.clear();
    latestMessageFromSelfMap.clear();
    clearSummaryRequests();
    postLoading(FetchResult.FetchType.Init);
    V2NIMTopicListOption option =
        new V2NIMTopicListOption(
            conversationId, 0, 0, null, PAGE_SIZE, V2NIMQueryDirection.V2NIM_QUERY_DIRECTION_DESC);
    TopicRepo.getTopicList(
        option,
        new FetchCallback<V2NIMTopicListResult>() {
          @Override
          public void onError(int errorCode, @Nullable String errorMsg) {
            loading = false;
            postError(errorCode, FetchResult.FetchType.Init);
          }

          @Override
          public void onSuccess(@Nullable V2NIMTopicListResult data) {
            loading = false;
            topicListLoaded = true;
            List<V2NIMTopic> topics = data == null ? null : data.getTopicList();
            if (topics != null) {
              for (V2NIMTopic topic : topics) {
                upsertTopic(topic);
              }
            }
            nextToken = data == null ? null : data.getNextToken();
            hasMore = data != null && data.hasMore();
            publishFiltered(FetchResult.FetchType.Init, LoadStatus.Success);
            loadTopicSummaries(topics);
          }
        });
  }

  public void loadMoreTopics() {
    if (TextUtils.isEmpty(conversationId) || loading || !hasMore) {
      return;
    }
    loading = true;
    V2NIMTopicListOption option =
        new V2NIMTopicListOption(
            conversationId,
            0,
            0,
            nextToken,
            PAGE_SIZE,
            V2NIMQueryDirection.V2NIM_QUERY_DIRECTION_DESC);
    TopicRepo.getTopicList(
        option,
        new FetchCallback<V2NIMTopicListResult>() {
          @Override
          public void onError(int errorCode, @Nullable String errorMsg) {
            loading = false;
            postError(errorCode, FetchResult.FetchType.Add);
          }

          @Override
          public void onSuccess(@Nullable V2NIMTopicListResult data) {
            loading = false;
            List<V2NIMTopic> topics = data == null ? null : data.getTopicList();
            if (topics != null) {
              for (V2NIMTopic topic : topics) {
                upsertTopic(topic);
              }
            }
            nextToken = data == null ? null : data.getNextToken();
            hasMore = data != null && data.hasMore();
            publishFiltered(FetchResult.FetchType.Add, LoadStatus.Success);
            loadTopicSummaries(topics);
          }
        });
  }

  public void refreshAllTopics() {
    loadTopics();
  }

  public void refreshSummaries() {
    if (!topicListLoaded || allTopics.isEmpty()) {
      return;
    }
    clearSummaryRequests();
    loadTopicSummaries(new ArrayList<>(allTopics));
  }

  public void clearUnread() {
    if (!TextUtils.isEmpty(conversationId)) {
      if (IMKitClient.enableV2CloudConversation()) {
        ConversationRepo.clearUnreadCountById(
            conversationId,
            new FetchCallback<List<V2NIMConversationOperationResult>>() {
              @Override
              public void onError(int errorCode, @Nullable String errorMsg) {
                ALog.e(TAG, "clear cloud unread error:" + errorCode + " msg:" + errorMsg);
                refreshCloudConversationReadTime();
              }

              @Override
              public void onSuccess(@Nullable List<V2NIMConversationOperationResult> data) {
                refreshCloudConversationReadTime();
              }
            });
      } else {
        LocalConversationRepo.clearUnreadCountById(
            conversationId,
            new FetchCallback<List<V2NIMLocalConversationOperationResult>>() {
              @Override
              public void onError(int errorCode, @Nullable String errorMsg) {
                ALog.e(TAG, "clear local unread error:" + errorCode + " msg:" + errorMsg);
                refreshLocalConversationReadTime();
              }

              @Override
              public void onSuccess(@Nullable List<V2NIMLocalConversationOperationResult> data) {
                refreshLocalConversationReadTime();
              }
            });
      }
    }
  }

  public void markTopicRead(@Nullable V2NIMTopic topic) {
    refreshConversationReadTime();
  }

  public void refreshUnreadState() {
    refreshConversationReadTime();
  }

  public void search(@Nullable String keyword) {
    this.keyword = keyword == null ? "" : keyword.trim();
    if (!topicListLoaded && allTopics.isEmpty()) {
      return;
    }
    publishFiltered(FetchResult.FetchType.Update, LoadStatus.Success);
  }

  public void renameTopic(@NonNull V2NIMTopic topic, @NonNull String newName) {
    renameTopic(topic, newName, null);
  }

  public void renameTopic(
      @NonNull V2NIMTopic topic,
      @NonNull String newName,
      @Nullable FetchCallback<V2NIMTopic> callback) {
    V2NIMUpdateTopicParams params =
        new V2NIMUpdateTopicParams(topic, newName, topic.getServerExtension());
    TopicRepo.updateTopic(
        params,
        new FetchCallback<V2NIMTopic>() {
          @Override
          public void onError(int errorCode, @Nullable String errorMsg) {
            if (callback != null) {
              callback.onError(errorCode, errorMsg);
            } else {
              postError(errorCode, FetchResult.FetchType.Update);
            }
          }

          @Override
          public void onSuccess(@Nullable V2NIMTopic data) {
            if (data != null) {
              upsertTopic(data);
              BotSubSessionItem oldItem = itemMap.get(data.getTopicId());
              itemMap.put(
                  data.getTopicId(),
                  new BotSubSessionItem(
                      data,
                      oldItem == null ? null : oldItem.getSummary(),
                      oldItem == null ? data.getUpdateTime() : oldItem.getTime(),
                      oldItem != null && oldItem.hasUnread()));
            }
            publishFiltered(FetchResult.FetchType.Update, LoadStatus.Success);
            if (callback != null) {
              callback.onSuccess(data);
            }
          }
        });
  }

  public void deleteTopic(@NonNull V2NIMTopic topic) {
    deleteTopic(topic, null);
  }

  public void deleteTopic(@NonNull V2NIMTopic topic, @Nullable FetchCallback<Void> callback) {
    V2NIMRemoveTopicsParams params = new V2NIMRemoveTopicsParams(Collections.singletonList(topic));
    TopicRepo.removeTopics(
        params,
        new FetchCallback<Void>() {
          @Override
          public void onError(int errorCode, @Nullable String errorMsg) {
            if (callback != null) {
              callback.onError(errorCode, errorMsg);
            } else {
              ALog.e(TAG, "deleteTopic error:" + errorCode + " msg:" + errorMsg);
            }
          }

          @Override
          public void onSuccess(@Nullable Void data) {
            removeTopic(topic);
            publishFiltered(FetchResult.FetchType.Remove, LoadStatus.Success);
            if (callback != null) {
              callback.onSuccess(data);
            }
          }
        });
  }

  @Override
  protected void onCleared() {
    super.onCleared();
    TopicRepo.removeTopicListener(topicListener);
    removeMessageListener();
    ConversationRepo.removeConversationListener(conversationListener);
    LocalConversationRepo.removeConversationListener(localConversationListener);
  }

  public void loadTopicSummaries(@Nullable List<V2NIMTopic> topics) {
    if (topics == null || topics.isEmpty()) {
      return;
    }
    summaryQueue.addAll(topics);
    drainSummaryQueue();
  }

  private void drainSummaryQueue() {
    while (runningSummaryCount < SUMMARY_PARALLEL_LIMIT && !summaryQueue.isEmpty()) {
      loadTopicSummary(summaryQueue.poll());
    }
  }

  private void loadTopicSummary(@Nullable V2NIMTopic topic) {
    if (topic == null) {
      return;
    }
    final int requestVersion = summaryRequestVersion;
    runningSummaryCount++;
    V2NIMTopicMessageListOption option =
        new V2NIMTopicMessageListOption(
            topic,
            0,
            0,
            null,
            SUMMARY_LIMIT,
            V2NIMQueryDirection.V2NIM_QUERY_DIRECTION_DESC,
            V2NIMSortOrder.V2NIM_SORT_ORDER_DESC);
    TopicRepo.getTopicMessageList(
        option,
        new FetchCallback<V2NIMTopicMessageListResult>() {
          @Override
          public void onError(int errorCode, @Nullable String errorMsg) {
            ALog.e(TAG, "loadTopicSummary error:" + errorCode + " msg:" + errorMsg);
            if (requestVersion == summaryRequestVersion) {
              updateSummaryItem(topic, null);
            }
            finishSummaryRequest();
          }

          @Override
          public void onSuccess(@Nullable V2NIMTopicMessageListResult data) {
            if (requestVersion != summaryRequestVersion) {
              finishSummaryRequest();
              return;
            }
            V2NIMMessage lastMessage = null;
            if (data != null && data.getReplyList() != null && !data.getReplyList().isEmpty()) {
              lastMessage = findLatestMessage(data.getReplyList());
            }
            updateSummaryItem(topic, lastMessage);
            finishSummaryRequest();
          }
        });
  }

  private void updateSummaryItem(@NonNull V2NIMTopic topic, @Nullable V2NIMMessage message) {
    long topicId = topic.getTopicId();
    String summary =
        BotSubSessionUtils.getMessageSummary(IMKitClient.getApplicationContext(), message);
    long time = message == null ? topic.getUpdateTime() : message.getCreateTime();
    boolean fromSelf =
        message != null && TextUtils.equals(message.getSenderId(), IMKitClient.account());
    if (message == null) {
      latestMessageFromSelfMap.remove(topicId);
    } else {
      latestMessageFromSelfMap.put(topicId, fromSelf);
    }
    boolean hasUnread = message != null && isTopicUnread(topic, time);
    BotSubSessionItem oldItem = itemMap.get(topicId);
    boolean changed =
        oldItem == null
            || !TextUtils.equals(oldItem.getSummary(), summary)
            || oldItem.getTime() != time
            || oldItem.hasUnread() != hasUnread;
    itemMap.put(topicId, new BotSubSessionItem(topic, summary, time, hasUnread));
    if (changed) {
      publishFiltered(FetchResult.FetchType.Update, LoadStatus.Success);
    }
  }

  private void finishSummaryRequest() {
    runningSummaryCount = Math.max(0, runningSummaryCount - 1);
    drainSummaryQueue();
  }

  @Nullable
  private V2NIMMessage findLatestMessage(@NonNull List<V2NIMMessage> messages) {
    V2NIMMessage latest = null;
    for (V2NIMMessage message : messages) {
      if (message == null) {
        continue;
      }
      if (latest == null || message.getCreateTime() > latest.getCreateTime()) {
        latest = message;
      }
    }
    return latest;
  }

  private boolean isSameConversation(@Nullable V2NIMTopicRefer topic) {
    return topic != null && TextUtils.equals(topic.getConversationId(), conversationId);
  }

  private void upsertTopic(@Nullable V2NIMTopic topic) {
    if (topic == null) {
      return;
    }
    for (int index = 0; index < allTopics.size(); index++) {
      if (sameTopic(allTopics.get(index), topic)) {
        allTopics.set(index, topic);
        return;
      }
    }
    allTopics.add(topic);
  }

  private boolean removeTopic(@Nullable V2NIMTopicRefer topic) {
    if (topic == null) {
      return false;
    }
    boolean removed = false;
    Iterator<V2NIMTopic> iterator = allTopics.iterator();
    while (iterator.hasNext()) {
      V2NIMTopic item = iterator.next();
      if (sameTopic(item, topic)) {
        iterator.remove();
        removed = true;
      }
    }
    itemMap.remove(topic.getTopicId());
    latestMessageFromSelfMap.remove(topic.getTopicId());
    return removed;
  }

  private boolean sameTopic(V2NIMTopicRefer left, V2NIMTopicRefer right) {
    return left != null
        && right != null
        && TextUtils.equals(left.getConversationId(), right.getConversationId())
        && left.getTopicId() == right.getTopicId()
        && left.getCreateTime() == right.getCreateTime();
  }

  private boolean isTopicUnread(@Nullable V2NIMTopic topic, long updateTime) {
    if (topic == null || updateTime <= 0) {
      return false;
    }
    long topicId = topic.getTopicId();
    if (!latestMessageFromSelfMap.containsKey(topicId)) {
      return false;
    }
    Boolean latestMessageFromSelf = latestMessageFromSelfMap.get(topicId);
    if (Boolean.TRUE.equals(latestMessageFromSelf)) {
      return false;
    }
    return conversationReadTime > 0 && updateTime > conversationReadTime;
  }

  private void refreshConversationReadTime() {
    if (IMKitClient.enableV2CloudConversation()) {
      refreshCloudConversationReadTime();
    } else {
      refreshLocalConversationReadTime();
    }
  }

  private void refreshUnreadStateIfCurrentConversation() {
    refreshUnreadStateIfCurrentConversation(conversationId);
  }

  private void refreshUnreadStateIfCurrentConversation(@Nullable String updatedConversationId) {
    if (TextUtils.isEmpty(conversationId)) {
      return;
    }
    if (!TextUtils.isEmpty(updatedConversationId)
        && !TextUtils.equals(conversationId, updatedConversationId)) {
      return;
    }
    refreshConversationReadTime();
  }

  private void refreshCloudConversationReadTime() {
    if (TextUtils.isEmpty(conversationId)) {
      return;
    }
    ConversationRepo.getConversation(
        conversationId,
        new FetchCallback<V2NIMConversation>() {
          @Override
          public void onError(int errorCode, @Nullable String errorMsg) {
            ALog.e(TAG, "getConversation readTime error:" + errorCode + " msg:" + errorMsg);
          }

          @Override
          public void onSuccess(@Nullable V2NIMConversation data) {
            conversationReadTime = data == null ? 0 : data.getLastReadTime();
            refreshUnreadStateForItems();
            publishUnreadStateIfTopicListLoaded();
          }
        });
  }

  private void refreshLocalConversationReadTime() {
    if (TextUtils.isEmpty(conversationId)) {
      return;
    }
    LocalConversationRepo.getConversationReadTime(
        conversationId,
        new FetchCallback<Long>() {
          @Override
          public void onError(int errorCode, @Nullable String errorMsg) {
            ALog.e(TAG, "getLocalConversationReadTime error:" + errorCode + " msg:" + errorMsg);
          }

          @Override
          public void onSuccess(@Nullable Long data) {
            conversationReadTime = data == null ? 0 : data;
            refreshUnreadStateForItems();
            publishUnreadStateIfTopicListLoaded();
          }
        });
  }

  private void publishUnreadStateIfTopicListLoaded() {
    if (topicListLoaded || !allTopics.isEmpty()) {
      publishFiltered(FetchResult.FetchType.Update, LoadStatus.Success);
    }
  }

  private void clearSummaryRequests() {
    summaryRequestVersion++;
    runningSummaryCount = 0;
    summaryQueue.clear();
  }

  private void addMessageListener() {
    if (!messageListenerAdded) {
      ChatRepo.addMessageListener(messageObserver);
      messageListenerAdded = true;
    }
  }

  private void removeMessageListener() {
    if (messageListenerAdded) {
      ChatRepo.removeMessageListener(messageObserver);
      messageListenerAdded = false;
    }
  }

  private void collectTopicIdsForMessage(
      @Nullable V2NIMMessage message, @NonNull Set<Long> topicIds) {
    if (message == null) {
      return;
    }
    if (!TextUtils.equals(message.getConversationId(), conversationId)) {
      return;
    }
    V2NIMTopicRefer topicRefer = message.getTopicRefer();
    if (topicRefer == null) {
      return;
    }
    if (itemMap.containsKey(topicRefer.getTopicId())) {
      topicIds.add(topicRefer.getTopicId());
    }
  }

  private void refreshTopicSummariesByRevokeNotifications(
      @Nullable List<MessageRevokeNotification> revokeNotifications) {
    Set<Long> topicIds = new HashSet<>();
    if (revokeNotifications == null || revokeNotifications.isEmpty()) {
      return;
    }
    for (MessageRevokeNotification notification : revokeNotifications) {
      if (notification == null) {
        continue;
      }
      V2NIMMessage message = notification.getMessage();
      if (message == null || !TextUtils.equals(message.getConversationId(), conversationId)) {
        continue;
      }
      V2NIMTopicRefer topicRefer = message.getTopicRefer();
      if (topicRefer != null && itemMap.containsKey(topicRefer.getTopicId())) {
        topicIds.add(topicRefer.getTopicId());
      }
    }
    refreshTopicSummariesByIds(topicIds);
  }

  private void refreshTopicsByIds(@NonNull Set<Long> topicIds) {
    if (topicIds.isEmpty()) {
      return;
    }
    List<V2NIMTopic> topics = new ArrayList<>();
    for (V2NIMTopic topic : allTopics) {
      if (topicIds.contains(topic.getTopicId())) {
        topics.add(topic);
      }
    }
    if (topics.isEmpty()) {
      return;
    }
    refreshConversationReadTime();
    loadTopicSummaries(topics);
  }

  private void refreshTopicSummariesByIds(@NonNull Set<Long> topicIds) {
    if (topicIds.isEmpty()) {
      return;
    }
    List<V2NIMTopic> topics = new ArrayList<>();
    for (V2NIMTopic topic : allTopics) {
      if (topicIds.contains(topic.getTopicId())) {
        topics.add(topic);
      }
    }
    if (topics.isEmpty()) {
      return;
    }
    loadTopicSummaries(topics);
  }

  private void refreshUnreadStateForItems() {
    for (V2NIMTopic topic : allTopics) {
      BotSubSessionItem item = itemMap.get(topic.getTopicId());
      if (item == null) {
        continue;
      }
      itemMap.put(
          topic.getTopicId(),
          new BotSubSessionItem(
              item.getTopic(),
              item.getSummary(),
              item.getTime(),
              isTopicUnread(item.getTopic(), item.getTime())));
    }
  }

  private void publishFiltered(FetchResult.FetchType type, LoadStatus status) {
    List<BotSubSessionItem> result = new ArrayList<>();
    String lowerKeyword = keyword.toLowerCase(Locale.getDefault());
    Collections.sort(
        allTopics,
        new Comparator<V2NIMTopic>() {
          @Override
          public int compare(V2NIMTopic left, V2NIMTopic right) {
            return Long.compare(getSortTime(right), getSortTime(left));
          }
        });
    for (V2NIMTopic topic : allTopics) {
      BotSubSessionItem item = itemMap.get(topic.getTopicId());
      if (item == null) {
        item = new BotSubSessionItem(topic, null, topic.getUpdateTime(), false);
        itemMap.put(topic.getTopicId(), item);
      }
      String title = topic.getTopicName() == null ? "" : topic.getTopicName();
      if (TextUtils.isEmpty(lowerKeyword)
          || title.toLowerCase(Locale.getDefault()).contains(lowerKeyword)) {
        result.add(item);
      }
    }
    FetchResult<List<BotSubSessionItem>> fetchResult = new FetchResult<>(status);
    fetchResult.setType(type);
    fetchResult.setData(result);
    topicListLiveData.setValue(fetchResult);
  }

  private long getSortTime(@NonNull V2NIMTopic topic) {
    BotSubSessionItem item = itemMap.get(topic.getTopicId());
    long summaryTime = item == null ? 0 : item.getTime();
    return Math.max(topic.getUpdateTime(), summaryTime);
  }

  private void postLoading(FetchResult.FetchType type) {
    FetchResult<List<BotSubSessionItem>> result = new FetchResult<>(LoadStatus.Loading);
    result.setType(type);
    topicListLiveData.setValue(result);
  }

  private void postError(int errorCode, FetchResult.FetchType type) {
    FetchResult<List<BotSubSessionItem>> result = new FetchResult<>(LoadStatus.Error);
    result.setType(type);
    result.setError(errorCode, R.string.chat_bot_sub_session_load_error);
    topicListLiveData.setValue(result);
  }
}
