// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.page.viewmodel;

import static com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant.LIB_TAG;

import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import com.netease.nimlib.sdk.v2.conversation.enums.V2NIMConversationType;
import com.netease.nimlib.sdk.v2.message.params.V2NIMMessageSearchParams;
import com.netease.nimlib.sdk.v2.team.V2NIMTeamListener;
import com.netease.nimlib.sdk.v2.team.model.V2NIMTeamMember;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.impl.TeamListenerImpl;
import com.netease.yunxin.kit.chatkit.model.IMMessageInfo;
import com.netease.yunxin.kit.chatkit.repo.ChatRepo;
import com.netease.yunxin.kit.chatkit.repo.ContactRepo;
import com.netease.yunxin.kit.chatkit.repo.TeamRepo;
import com.netease.yunxin.kit.chatkit.ui.common.ChatUserCache;
import com.netease.yunxin.kit.chatkit.ui.model.ChatSearchBean;
import com.netease.yunxin.kit.common.ui.viewmodel.BaseViewModel;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.corekit.im2.extend.FetchCallback;
import com.netease.yunxin.kit.corekit.im2.listener.V2UserListener;
import com.netease.yunxin.kit.corekit.im2.model.V2UserInfo;
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

  /** 用户变更监听 */
  private V2UserListener userListener =
      new V2UserListener() {
        @Override
        public void onUserChanged(@NonNull List<V2UserInfo> userList) {
          List<String> accounts = new ArrayList<>();
          for (V2UserInfo userInfo : userList) {
            accounts.add(userInfo.getAccountId());
          }
          ChatUserCache.getInstance().addUserInfo(userList);
          FetchResult<List<String>> result = new FetchResult<>(LoadStatus.Success);
          result.setData(accounts);
          userChangeLiveData.setValue(result);
        }
      };

  /** 群组成员变更监听 */
  private final V2NIMTeamListener teamListener =
      new TeamListenerImpl() {

        @Override
        public void onTeamMemberInfoUpdated(@Nullable List<V2NIMTeamMember> teamMembers) {
          super.onTeamMemberInfoUpdated(teamMembers);
          if (teamMembers == null) {
            return;
          }
          ChatUserCache.getInstance().addTeamMember(teamMembers);
          List<String> accounts = new ArrayList<>();
          for (V2NIMTeamMember teamMember : teamMembers) {
            accounts.add(teamMember.getAccountId());
          }
          FetchResult<List<String>> result = new FetchResult<>(LoadStatus.Success);
          result.setData(accounts);
          userChangeLiveData.postValue(result);
        }
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
        new FetchCallback<>() {

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
    ContactRepo.addUserListener(userListener);
    TeamRepo.addTeamListener(teamListener);
    haveAddListener = true;
  }

  @Override
  protected void onCleared() {
    super.onCleared();
    ContactRepo.removeUserListener(userListener);
    TeamRepo.removeTeamListener(teamListener);
  }
}
