// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.page.viewmodel;

import static com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant.LIB_TAG;

import android.text.TextUtils;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import com.netease.nimlib.coexist.sdk.v2.conversation.enums.V2NIMConversationType;
import com.netease.nimlib.coexist.sdk.v2.message.params.V2NIMMessageSearchParams;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.model.IMMessageInfo;
import com.netease.yunxin.kit.chatkit.repo.ChatRepo;
import com.netease.yunxin.kit.chatkit.ui.cache.TeamUserChangedListener;
import com.netease.yunxin.kit.chatkit.ui.cache.TeamUserManager;
import com.netease.yunxin.kit.chatkit.ui.model.ChatSearchBean;
import com.netease.yunxin.kit.common.ui.viewmodel.BaseViewModel;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.corekit.coexist.im2.extend.FetchCallback;
import java.util.ArrayList;
import java.util.List;

/** Search message info view model search history message for Team chat page */
public class SearchMessageViewModel extends BaseViewModel {

  private static final String TAG = "SearchMessageViewModel";
  private final MutableLiveData<FetchResult<List<ChatSearchBean>>> searchLiveData =
      new MutableLiveData<>();

  /** search message result live data */
  public MutableLiveData<FetchResult<List<ChatSearchBean>>> getSearchLiveData() {
    return searchLiveData;
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

  private boolean haveAddListener = false;

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

  public void searchMessage(String keyword, V2NIMConversationType type, String sessionId) {
    ALog.d(LIB_TAG, TAG, "searchMessage:" + keyword);
    if (TextUtils.isEmpty(keyword)) {
      //空字符串不搜索
      FetchResult<List<ChatSearchBean>> result = new FetchResult<>(LoadStatus.Success);
      result.setData(new ArrayList<>());
      searchLiveData.postValue(result);
      return;
    }
    addListener();
    V2NIMMessageSearchParams params;
    List<String> sessionIds = new ArrayList<>();
    sessionIds.add(sessionId);
    if (type == V2NIMConversationType.V2NIM_CONVERSATION_TYPE_P2P) {
      params =
          V2NIMMessageSearchParams.V2NIMMessageSearchParamsBuilder.builder(keyword)
              .withP2pAccountIds(sessionIds)
              .build();
    } else {
      params =
          V2NIMMessageSearchParams.V2NIMMessageSearchParamsBuilder.builder(keyword)
              .withTeamIds(sessionIds)
              .build();
    }
    ChatRepo.searchMessages(
        params,
        new FetchCallback<List<IMMessageInfo>>() {

          @Override
          public void onSuccess(@Nullable List<IMMessageInfo> data) {
            ALog.d(
                LIB_TAG, TAG, "searchMessage,onSuccess:" + (data == null ? "null" : data.size()));
            FetchResult<List<ChatSearchBean>> result = new FetchResult<>(LoadStatus.Success);
            if (data != null) {
              List<ChatSearchBean> searchBeanList = new ArrayList<>();
              for (IMMessageInfo record : data) {
                searchBeanList.add(new ChatSearchBean(record.getMessage(), keyword));
              }
              result.setData(searchBeanList);
            }
            searchLiveData.postValue(result);
          }

          @Override
          public void onError(int errorCode, @Nullable String errorMsg) {
            ALog.d(LIB_TAG, TAG, "searchMessage,onError:" + errorCode + ",errorMsg:" + errorMsg);
            FetchResult<List<ChatSearchBean>> result = new FetchResult<>(LoadStatus.Error);
            result.setError(new FetchResult.ErrorMsg(errorCode, errorMsg));
            searchLiveData.postValue(result);
          }
        });
  }

  private void addListener() {
    if (haveAddListener) {
      return;
    }
    TeamUserManager.getInstance().addMemberChangedListener(cacheUserChangedListener);
    haveAddListener = true;
  }

  @Override
  protected void onCleared() {
    super.onCleared();
    TeamUserManager.getInstance().removeMemberChangedListener(cacheUserChangedListener);
  }
}
