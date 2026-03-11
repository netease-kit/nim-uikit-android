// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.page.viewmodel;

import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import com.netease.nimlib.sdk.v2.message.V2NIMCollection;
import com.netease.nimlib.sdk.v2.message.V2NIMMessage;
import com.netease.nimlib.sdk.v2.message.enums.V2NIMMessageType;
import com.netease.nimlib.sdk.v2.message.enums.V2NIMSearchDirection;
import com.netease.nimlib.sdk.v2.message.enums.V2NIMSearchStrategy;
import com.netease.nimlib.sdk.v2.message.params.V2NIMAddCollectionParams;
import com.netease.nimlib.sdk.v2.message.params.V2NIMMessageSearchExParams;
import com.netease.nimlib.sdk.v2.message.result.V2NIMMessageSearchResult;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.IMKitConfigCenter;
import com.netease.yunxin.kit.chatkit.repo.ChatRepo;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.common.ChatUserCache;
import com.netease.yunxin.kit.chatkit.ui.common.MessageHelper;
import com.netease.yunxin.kit.chatkit.utils.ErrorUtils;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.corekit.im2.IMKitClient;
import com.netease.yunxin.kit.corekit.im2.extend.FetchCallback;
import java.util.ArrayList;
import java.util.List;

public class ChatSearchFileViewModel extends ChatSearchBaseViewModel {

  private static final String TAG = "SearchFileViewModel";
  private final MutableLiveData<FetchResult<List<V2NIMMessage>>> searchFileLiveData =
      new MutableLiveData<>();
  private String nextToken = null;
  private boolean isLoading = false;
  private boolean hasMore = true;
  private static final int PAGE_SIZE = 50;

  public MutableLiveData<FetchResult<List<V2NIMMessage>>> getSearchFileLiveData() {
    return searchFileLiveData;
  }

  public void searchFileMessages() {
    if (isLoading) {
      return;
    }
    isLoading = true;
    V2NIMMessageSearchExParams params =
        V2NIMMessageSearchExParams.V2NIMMessageSearchExParamsBuilder.builder()
            .withMessageTypes(List.of(V2NIMMessageType.V2NIM_MESSAGE_TYPE_FILE))
            .withConversationId(mConversationId)
            .withSearchDirection(V2NIMSearchDirection.V2NIM_SEARCH_DIRECTION_BACKWARD)
            .withSearchStrategy(V2NIMSearchStrategy.V2NIM_SEARCH_STRATEGY_FTS)
            .withLimit(PAGE_SIZE)
            .withPageToken(nextToken)
            .build();

    if (IMKitConfigCenter.getEnableCloudSearch()) {
      ChatRepo.searchCloudMessagesEx(
          params,
          new FetchCallback<V2NIMMessageSearchResult>() {
            @Override
            public void onError(int errorCode, @Nullable String errorMsg) {
              loadFailed(errorCode, errorMsg);
            }

            @Override
            public void onSuccess(@Nullable V2NIMMessageSearchResult data) {
              loadSuccess(data);
            }
          });
    } else {
      ChatRepo.searchLocalMessages(
          params,
          new FetchCallback<V2NIMMessageSearchResult>() {
            @Override
            public void onSuccess(@Nullable V2NIMMessageSearchResult data) {
              loadSuccess(data);
            }

            @Override
            public void onError(int errorCode, @Nullable String errorMsg) {
              loadFailed(errorCode, errorMsg);
            }
          });
    }
  }

  private void loadSuccess(V2NIMMessageSearchResult data) {
    List<V2NIMMessage> messages = new ArrayList<>();
    if (data != null) {
      for (V2NIMMessageSearchResult.V2NIMMessageSearchItem item : data.getItems()) {
        List<V2NIMMessage> message = item.getMessages();
        if (message != null) {
          messages.addAll(message);
        }
      }
      nextToken = data.getNextPageToken();
      ALog.i(
          TAG,
          "searchFileMessages: nextToken = "
              + nextToken
              + "pageSize = "
              + String.valueOf(messages.size()));
    }
    if (messages.isEmpty()) {
      hasMore = false;
    } else {
      hasMore = nextToken != null && !nextToken.isEmpty();
    }
    FetchResult<List<V2NIMMessage>> result = new FetchResult<>(messages);
    searchFileLiveData.postValue(result);
    isLoading = false;
  }

  private void loadFailed(int errorCode, @Nullable String errorMsg) {
    FetchResult<List<V2NIMMessage>> result = new FetchResult<>(errorCode, errorMsg);
    searchFileLiveData.postValue(result);
    isLoading = false;
  }

  public void addMsgCollection(V2NIMMessage message) {
    if (message == null) {
      return;
    }
    String conversationName =
        ChatUserCache.getInstance().getConversationInfo(message.getConversationId());
    V2NIMAddCollectionParams params =
        MessageHelper.createCollectionParams(conversationName, message);
    ChatRepo.addCollection(
        params,
        new FetchCallback<V2NIMCollection>() {
          @Override
          public void onSuccess(@Nullable V2NIMCollection data) {
            ToastX.showShortToast(R.string.chat_message_collection_tip);
          }

          @Override
          public void onError(int errorCode, @Nullable String errorMsg) {
            ErrorUtils.showErrorCodeToast(IMKitClient.getApplicationContext(), errorCode);
          }
        });
  }

  public boolean hasMore() {
    return hasMore;
  }
}
