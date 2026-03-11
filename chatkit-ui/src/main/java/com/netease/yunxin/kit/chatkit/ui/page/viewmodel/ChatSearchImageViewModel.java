// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.page.viewmodel;

import static com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant.LIB_TAG;

import android.text.TextUtils;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import com.netease.nimlib.sdk.v2.message.V2NIMMessage;
import com.netease.nimlib.sdk.v2.message.enums.V2NIMMessageType;
import com.netease.nimlib.sdk.v2.message.enums.V2NIMSearchDirection;
import com.netease.nimlib.sdk.v2.message.enums.V2NIMSearchStrategy;
import com.netease.nimlib.sdk.v2.message.params.V2NIMMessageSearchExParams;
import com.netease.nimlib.sdk.v2.message.result.V2NIMMessageSearchResult;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.IMKitConfigCenter;
import com.netease.yunxin.kit.chatkit.repo.ChatRepo;
import com.netease.yunxin.kit.chatkit.ui.normal.search.ChatSearchImageActivity;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.corekit.im2.extend.FetchCallback;
import java.util.ArrayList;
import java.util.List;

/** Search image message view model 查询图片消息的ViewModel */
public class ChatSearchImageViewModel extends ChatSearchBaseViewModel {

  private static final String TAG = "SearchImageViewModel";
  private static final int PAGE_SIZE = 50;
  private final MutableLiveData<FetchResult<List<V2NIMMessage>>> searchImageLiveData =
      new MutableLiveData<>();
  private String nextToken = null;
  private boolean isLoading = false;
  private List<V2NIMMessageType> messageTypes = new ArrayList<>();
  private boolean hasMore = true;

  /**
   * 获取图片搜索结果的LiveData
   *
   * @return 图片搜索结果的LiveData
   */
  public MutableLiveData<FetchResult<List<V2NIMMessage>>> getSearchImageLiveData() {
    return searchImageLiveData;
  }

  public void init(String conversationId, int mode) {
    super.init(conversationId);
    mConversationId = conversationId;
    setMode(mode);
  }
  /** 搜索图片消息 */
  public void searchImageMessages() {
    ALog.d(LIB_TAG, TAG, "searchImageMessages: conversationId=" + mConversationId);
    if (isLoading || TextUtils.isEmpty(mConversationId)) {
      return;
    }
    isLoading = true;

    // 创建搜索参数，只搜索图片类型的消息
    // 注意：由于V2NIMMessageSearchExParams的构造函数是private的，我们需要使用SDK提供的正确方式创建
    // 这里使用示例，实际需要根据SDK文档调整
    // 由于无法直接创建V2NIMMessageSearchExParams，我们暂时使用null作为参数
    // 实际使用中需要根据SDK文档使用正确的方式创建
    List<V2NIMMessageType> types =
        messageTypes.isEmpty() ? List.of(V2NIMMessageType.V2NIM_MESSAGE_TYPE_IMAGE) : messageTypes;
    V2NIMMessageSearchExParams params =
        V2NIMMessageSearchExParams.V2NIMMessageSearchExParamsBuilder.builder()
            .withMessageTypes(types)
            .withConversationId(mConversationId)
            .withSearchDirection(V2NIMSearchDirection.V2NIM_SEARCH_DIRECTION_BACKWARD)
            .withSearchStrategy(V2NIMSearchStrategy.V2NIM_SEARCH_STRATEGY_FTS)
            .withPageToken(nextToken)
            .withLimit(PAGE_SIZE)
            .build();

    if (IMKitConfigCenter.getEnableCloudSearch()) {
      ChatRepo.searchCloudMessagesEx(
          params,
          new FetchCallback<V2NIMMessageSearchResult>() {
            @Override
            public void onSuccess(@Nullable V2NIMMessageSearchResult data) {
              loadSuccessData(data);
            }

            @Override
            public void onError(int errorCode, @Nullable String errorMsg) {
              loadFail(errorCode, errorMsg);
            }
          });
    } else {
      ChatRepo.searchLocalMessages(
          params,
          new FetchCallback<V2NIMMessageSearchResult>() {
            @Override
            public void onSuccess(@Nullable V2NIMMessageSearchResult data) {
              loadSuccessData(data);
            }

            @Override
            public void onError(int errorCode, @Nullable String errorMsg) {
              loadFail(errorCode, errorMsg);
            }
          });
    }
  }

  public boolean hasMore() {
    return hasMore;
  }

  public void loadSuccessData(V2NIMMessageSearchResult data) {
    ALog.i(LIB_TAG, TAG, "searchImageMessages, onSuccess: " + (data == null ? "null" : 0));
    // 创建成功结果，直接传入数据
    // 注意：由于V2NIMMessageSearchResult的具体方法未知，这里暂时返回空列表
    // 实际使用中需要根据SDK文档使用正确的方法获取消息列表
    List<V2NIMMessage> messages = new ArrayList<>();
    // 从V2NIMMessageSearchResult中getItems获取列表，将V2NIMMessageSearchItem转换为V2NIMMessage
    if (data != null) {
      for (V2NIMMessageSearchResult.V2NIMMessageSearchItem item : data.getItems()) {
        List<V2NIMMessage> message = item.getMessages();
        if (message != null) {
          messages.addAll(message);
        }
      }
      nextToken = data.getNextPageToken();
    }
    if (messages.isEmpty()) {
      hasMore = false;
    } else {
      hasMore = nextToken != null && !nextToken.isEmpty();
    }
    // 创建成功结果，直接传入数据
    FetchResult<List<V2NIMMessage>> result = new FetchResult<>(messages);
    searchImageLiveData.postValue(result);
    isLoading = false;
  }

  private void loadFail(int errorCode, @Nullable String errorMsg) {
    ALog.e(LIB_TAG, TAG, "searchImageMessages, onError: " + errorCode + ", errorMsg: " + errorMsg);
    // 创建错误结果，传入错误码和错误信息
    FetchResult<List<V2NIMMessage>> result = new FetchResult<>(errorCode, errorMsg);
    searchImageLiveData.postValue(result);
    isLoading = false;
  }

  public void setMode(int mode) {
    messageTypes.clear();
    if (mode == ChatSearchImageActivity.MODE_IMAGE) {
      messageTypes.add(V2NIMMessageType.V2NIM_MESSAGE_TYPE_IMAGE);
    } else if (mode == ChatSearchImageActivity.MODE_VIDEO) {
      messageTypes.add(V2NIMMessageType.V2NIM_MESSAGE_TYPE_VIDEO);
    } else if (mode == ChatSearchImageActivity.MODE_ALL) {
      messageTypes.add(V2NIMMessageType.V2NIM_MESSAGE_TYPE_IMAGE);
      messageTypes.add(V2NIMMessageType.V2NIM_MESSAGE_TYPE_VIDEO);
    }
  }
}
