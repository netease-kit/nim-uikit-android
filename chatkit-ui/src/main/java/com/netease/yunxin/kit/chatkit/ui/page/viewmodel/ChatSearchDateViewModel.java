// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.page.viewmodel;

import static com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant.LIB_TAG;

import android.text.TextUtils;
import android.text.format.DateFormat;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import com.netease.nimlib.sdk.v2.message.V2NIMMessage;
import com.netease.nimlib.sdk.v2.message.enums.V2NIMMessageQueryDirection;
import com.netease.nimlib.sdk.v2.message.enums.V2NIMSearchDirection;
import com.netease.nimlib.sdk.v2.message.enums.V2NIMSearchStrategy;
import com.netease.nimlib.sdk.v2.message.option.V2NIMMessageListOption;
import com.netease.nimlib.sdk.v2.message.params.V2NIMMessageSearchExParams;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.model.IMMessageInfo;
import com.netease.yunxin.kit.chatkit.repo.ChatRepo;
import com.netease.yunxin.kit.common.ui.viewmodel.BaseViewModel;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.corekit.im2.extend.FetchCallback;
import java.util.Date;
import java.util.List;

/** Search date message view model 查询日期消息的ViewModel */
public class ChatSearchDateViewModel extends BaseViewModel {

  private static final String TAG = "ChatSearchDateViewModel";
  private static final int PAGE_SIZE = 1;
  private final MutableLiveData<FetchResult<V2NIMMessage>> searchDateLiveData =
      new MutableLiveData<>();

  private boolean isLoading = false;

  // 当前会话ID
  protected String mConversationId;

  /**
   * 获取图片搜索结果的LiveData
   *
   * @return 图片搜索结果的LiveData
   */
  public MutableLiveData<FetchResult<V2NIMMessage>> getSearchDateLiveData() {
    return searchDateLiveData;
  }

  public void init(String conversationId) {
    ALog.i(LIB_TAG, TAG, "init: conversationId=" + conversationId);

    mConversationId = conversationId;
  }
  /** 搜索图片消息 */
  public void searchDateMessages(long startTime) {
    if (isLoading || TextUtils.isEmpty(mConversationId)) {
      return;
    }
    if (startTime < 0) {
      startTime = 0;
    }
    isLoading = true;
    Date date = new Date(startTime);
    String dateStr = DateFormat.format("yyyy-MM-dd HH:mm:ss", date).toString();
    ALog.i(LIB_TAG, TAG, "searchDateMessages: startTime=" + dateStr);

    V2NIMMessageSearchExParams params =
        V2NIMMessageSearchExParams.V2NIMMessageSearchExParamsBuilder.builder()
            .withConversationId(mConversationId)
            .withSearchDirection(V2NIMSearchDirection.V2NIM_SEARCH_DIRECTION_FORWARD)
            .withSearchStrategy(V2NIMSearchStrategy.V2NIM_SEARCH_STRATEGY_FTS)
            .withSearchStartTime(startTime)
            .withLimit(PAGE_SIZE)
            .build();

    ALog.i(LIB_TAG, TAG, "searchDateMessages: params=" + params.toString());
    V2NIMMessageListOption.V2NIMMessageListOptionBuilder builder =
        V2NIMMessageListOption.V2NIMMessageListOptionBuilder.builder(mConversationId);
    builder.withBeginTime(startTime);
    builder.withDirection(V2NIMMessageQueryDirection.V2NIM_QUERY_DIRECTION_ASC);
    builder.withLimit(PAGE_SIZE);
    ChatRepo.getMessageList(
        builder.build(),
        new FetchCallback<List<IMMessageInfo>>() {
          @Override
          public void onError(int errorCode, @Nullable String errorMsg) {
            ALog.i(
                LIB_TAG,
                TAG,
                "searchDateMessages, onError: errorCode=" + errorCode + ", errorMsg=" + errorMsg);
            loadFail(errorCode, errorMsg);
          }

          @Override
          public void onSuccess(@Nullable List<IMMessageInfo> messages) {
            isLoading = false;
            FetchResult<V2NIMMessage> result = new FetchResult<>(LoadStatus.Success);
            if (messages != null && !messages.isEmpty()) {
              result.setData(messages.get(0).getMessage());
            }
            searchDateLiveData.postValue(result);
            isLoading = false;
          }
        });
  }

  private void loadFail(int errorCode, @Nullable String errorMsg) {
    ALog.e(LIB_TAG, TAG, "searchImageMessages, onError: " + errorCode + ", errorMsg: " + errorMsg);
    // 创建错误结果，传入错误码和错误信息
    FetchResult<V2NIMMessage> result = new FetchResult<>(errorCode, errorMsg);
    searchDateLiveData.postValue(result);
    isLoading = false;
  }
}
