// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.page.viewmodel;

import static com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant.LIB_TAG;

import android.text.TextUtils;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import com.netease.nimlib.sdk.v2.conversation.enums.V2NIMConversationType;
import com.netease.nimlib.sdk.v2.message.V2NIMMessage;
import com.netease.nimlib.sdk.v2.message.V2NIMMessageDeletedNotification;
import com.netease.nimlib.sdk.v2.message.enums.V2NIMMessageType;
import com.netease.nimlib.sdk.v2.message.enums.V2NIMSearchDirection;
import com.netease.nimlib.sdk.v2.message.enums.V2NIMSearchStrategy;
import com.netease.nimlib.sdk.v2.message.params.V2NIMMessageSearchExParams;
import com.netease.nimlib.sdk.v2.message.result.V2NIMMessageSearchResult;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.IMKitConfigCenter;
import com.netease.yunxin.kit.chatkit.impl.MessageListenerImpl;
import com.netease.yunxin.kit.chatkit.listener.ChatListener;
import com.netease.yunxin.kit.chatkit.listener.MessageRevokeNotification;
import com.netease.yunxin.kit.chatkit.model.IMMessageInfo;
import com.netease.yunxin.kit.chatkit.repo.ChatRepo;
import com.netease.yunxin.kit.chatkit.ui.cache.TeamUserChangedListener;
import com.netease.yunxin.kit.chatkit.ui.cache.TeamUserManager;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.chatkit.utils.ConversationIdUtils;
import com.netease.yunxin.kit.common.ui.viewmodel.BaseViewModel;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.corekit.im2.extend.FetchCallback;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jetbrains.annotations.NotNull;

/** Search message info view model search history message for Team chat page */
public class ChatSearchViewModel extends BaseViewModel {

  private static final String TAG = "ChatSearchViewModel";

  // 分页参数,每页最多返回50条
  private static final int LIMIT = 50;
  // 是否已经添加了用户变更监听
  private boolean haveAddListener = false;
  // 分页参数,下一页的token
  private String nextTokenLocal = null;
  // 是否正在加载中
  private boolean isLoadingLocal = false;
  // 是否还有更多数据可以加载
  private boolean hasMore = true;

  protected String targetId;

  protected V2NIMConversationType conversationType;

  protected String conversationId;

  protected String keyword;

  protected final MutableLiveData<FetchResult<List<ChatMessageBean>>> searchMessagesLiveData =
      new MutableLiveData<>();

  /** 消息删除监听 */
  private final MutableLiveData<FetchResult<List<String>>> messageDeletedLiveData =
      new MutableLiveData<>();

  public MutableLiveData<FetchResult<List<String>>> getMessageDeletedLiveData() {
    return messageDeletedLiveData;
  }

  public MutableLiveData<FetchResult<List<ChatMessageBean>>> getSearchMessagesLiveData() {
    return searchMessagesLiveData;
  }

  /** 用户变更监听 */
  private final MutableLiveData<FetchResult<List<String>>> userChangeLiveData =
      new MutableLiveData<>();

  /**
   * 用户变更监听
   *
   * @return 用户变更监听
   */
  public MutableLiveData<FetchResult<List<String>>> getUserChangeLiveData() {
    return userChangeLiveData;
  }

  private final ChatListener messageListener =
      new MessageListenerImpl() {
        @Override
        public void onMessageRevokeNotifications(
            @NotNull List<@NotNull MessageRevokeNotification> revokeNotifications) {
          super.onMessageRevokeNotifications(revokeNotifications);
          FetchResult<List<String>> result = new FetchResult<>(LoadStatus.Success);
          List<String> deletedMessageClientIds = new ArrayList<>();
          for (MessageRevokeNotification notification : revokeNotifications) {
            if (notification.getNimNotification().getMessageRefer() == null) {
              continue;
            }
            String msgClientId =
                notification.getNimNotification().getMessageRefer().getMessageClientId();
            if (msgClientId != null) {
              deletedMessageClientIds.add(msgClientId);
            }
          }
          result.setData(deletedMessageClientIds);
          result.setType(FetchResult.FetchType.Remove);
          messageDeletedLiveData.setValue(result);
        }

        @Override
        public void onMessageDeletedNotifications(
            @NotNull List<? extends @NotNull V2NIMMessageDeletedNotification> messages) {
          super.onMessageDeletedNotifications(messages);
          FetchResult<List<String>> result = new FetchResult<>(LoadStatus.Success);
          List<String> deletedMessageClientIds = new ArrayList<>();
          for (V2NIMMessageDeletedNotification notification : messages) {
            String msgClientId = notification.getMessageRefer().getMessageClientId();
            if (msgClientId != null) {
              deletedMessageClientIds.add(msgClientId);
            }
          }
          result.setData(deletedMessageClientIds);
          result.setType(FetchResult.FetchType.Remove);
          messageDeletedLiveData.setValue(result);
        }
      };

  private final TeamUserChangedListener cacheUserChangedListener =
      new TeamUserChangedListener() {

        @Override
        public void onUsersChanged(List<String> accountIds) {
          FetchResult<List<String>> result = new FetchResult<>(LoadStatus.Success);
          result.setData(accountIds);
          userChangeLiveData.postValue(result);
        }

        @Override
        public void onUserDelete(List<String> accountIds) {}

        @Override
        public void onUsersAdd(List<String> accountIds) {}
      };

  public void setConversationInfo(String sessionId, V2NIMConversationType conversationType) {
    this.targetId = sessionId;
    this.conversationType = conversationType;
    this.conversationId = ConversationIdUtils.conversationId(targetId, conversationType);
    addListener();
  }

  public void searchMessage(String keyword, @Nullable List<String> senderAccountIds) {
    ALog.i(
        LIB_TAG,
        TAG,
        "searchMessage:"
            + keyword
            + ",nextTokenLocal:"
            + nextTokenLocal
            + ",isLoadingLocal:"
            + isLoadingLocal);
    if (isLoadingLocal) {
      return;
    }
    isLoadingLocal = true;
    this.keyword = keyword;
    V2NIMMessageSearchExParams.V2NIMMessageSearchExParamsBuilder builder =
        V2NIMMessageSearchExParams.V2NIMMessageSearchExParamsBuilder.builder()
            .withConversationId(conversationId)
            .withSearchDirection(V2NIMSearchDirection.V2NIM_SEARCH_DIRECTION_BACKWARD)
            .withMessageTypes(getSearchMessageTypeList())
            .withSearchStrategy(V2NIMSearchStrategy.V2NIM_SEARCH_STRATEGY_FTS)
            .withLimit(LIMIT)
            .withPageToken(nextTokenLocal);

    if (!TextUtils.isEmpty(keyword)) {
      builder = builder.withKeywordList(Collections.singletonList(keyword));
    }
    if (senderAccountIds != null && !senderAccountIds.isEmpty()) {
      builder = builder.withSenderAccountIds(senderAccountIds);
    }
    V2NIMMessageSearchExParams params = builder.build();
    if (IMKitConfigCenter.getEnableCloudSearch()) {
      ChatRepo.searchCloudMessagesEx(params, searchMessagesCallback);
    } else {
      ChatRepo.searchLocalMessages(params, searchMessagesCallback);
    }
  }

  private FetchCallback<V2NIMMessageSearchResult> searchMessagesCallback =
      new FetchCallback<V2NIMMessageSearchResult>() {
        @Override
        public void onSuccess(@Nullable V2NIMMessageSearchResult data) {
          ALog.i(
              LIB_TAG,
              TAG,
              "searchMessagesCallback onSuccess ,nextToken=" + data.getNextPageToken());
          loadSuccess(data);
          isLoadingLocal = false;
        }

        @Override
        public void onError(int errorCode, @Nullable String errorMsg) {
          ALog.i(
              LIB_TAG,
              TAG,
              "searchMessagesCallback,onError:" + errorCode + ",errorMsg:" + errorMsg);
          FetchResult<List<ChatMessageBean>> result = new FetchResult<>(LoadStatus.Error);
          result.setError(new FetchResult.ErrorMsg(errorCode, errorMsg));
          searchMessagesLiveData.postValue(result);
          isLoadingLocal = false;
        }
      };

  protected void clearSearchMessages() {
    nextTokenLocal = null;
    hasMore = true;
  }

  protected void loadSuccess(V2NIMMessageSearchResult data) {
    final boolean isFirstPage = (nextTokenLocal == null);
    List<ChatMessageBean> searchBeanList = new ArrayList<>();
    if (data != null && data.getItems() != null) {
      for (V2NIMMessageSearchResult.V2NIMMessageSearchItem item : data.getItems()) {
        if (item.getMessages() != null) {
          for (V2NIMMessage msg : item.getMessages()) {
            ChatMessageBean bean = new ChatMessageBean(new IMMessageInfo(msg));
            if (!bean.isRevoked()) {
              bean.setKeyword(keyword);
              searchBeanList.add(bean);
            }
          }
        }
      }
      nextTokenLocal = data.getNextPageToken();
    }
    if (searchBeanList.isEmpty()) {
      hasMore = false;
    } else {
      hasMore = nextTokenLocal != null && !nextTokenLocal.isEmpty();
    }
    FetchResult<List<ChatMessageBean>> result = new FetchResult<>(LoadStatus.Success);
    result.setFetchType(isFirstPage ? FetchResult.FetchType.Init : FetchResult.FetchType.Add);
    result.setData(searchBeanList);
    searchMessagesLiveData.postValue(result);
  }

  public boolean hasMoreLocal() {
    return hasMore;
  }

  private void addListener() {
    if (haveAddListener) {
      return;
    }
    TeamUserManager.getInstance().addMemberChangedListener(cacheUserChangedListener);
    ChatRepo.addMessageListener(messageListener);
    haveAddListener = true;
  }

  private List<V2NIMMessageType> getSearchMessageTypeList() {

    List<V2NIMMessageType> typeList = new ArrayList<>();
    typeList.add(V2NIMMessageType.V2NIM_MESSAGE_TYPE_FILE);
    typeList.add(V2NIMMessageType.V2NIM_MESSAGE_TYPE_IMAGE);
    typeList.add(V2NIMMessageType.V2NIM_MESSAGE_TYPE_AUDIO);
    typeList.add(V2NIMMessageType.V2NIM_MESSAGE_TYPE_LOCATION);
    typeList.add(V2NIMMessageType.V2NIM_MESSAGE_TYPE_CALL);
    typeList.add(V2NIMMessageType.V2NIM_MESSAGE_TYPE_TEXT);
    typeList.add(V2NIMMessageType.V2NIM_MESSAGE_TYPE_VIDEO);
    typeList.add(V2NIMMessageType.V2NIM_MESSAGE_TYPE_CUSTOM);
    return typeList;
  }

  @Override
  protected void onCleared() {
    super.onCleared();
    TeamUserManager.getInstance().removeMemberChangedListener(cacheUserChangedListener);
    ChatRepo.removeMessageListener(messageListener);
    haveAddListener = false;
  }
}
