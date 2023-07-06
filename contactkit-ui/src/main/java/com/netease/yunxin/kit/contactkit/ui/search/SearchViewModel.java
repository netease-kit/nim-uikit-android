// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.search;

import android.text.TextUtils;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.model.FriendSearchInfo;
import com.netease.yunxin.kit.chatkit.model.TeamSearchInfo;
import com.netease.yunxin.kit.chatkit.repo.SearchRepo;
import com.netease.yunxin.kit.common.ui.viewholder.BaseBean;
import com.netease.yunxin.kit.common.ui.viewmodel.BaseViewModel;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.contactkit.ui.R;
import com.netease.yunxin.kit.contactkit.ui.model.SearchFriendBean;
import com.netease.yunxin.kit.contactkit.ui.model.SearchTeamBean;
import com.netease.yunxin.kit.contactkit.ui.model.SearchTitleBean;
import com.netease.yunxin.kit.corekit.im.provider.FetchCallback;
import java.util.ArrayList;
import java.util.List;

/** to provider search data and operation */
public class SearchViewModel extends BaseViewModel {

  private static final String TAG = "SearchViewModel";
  private static final String LIB_TAG = "SearchKit-UI";
  private final MutableLiveData<FetchResult<List<BaseBean>>> queryLiveData =
      new MutableLiveData<>();

  private final List<BaseBean> resultList = new ArrayList<>();

  public MutableLiveData<FetchResult<List<BaseBean>>> getQueryLiveData() {
    return queryLiveData;
  }

  protected String routerFriend;
  protected String routerTeam;

  public void setRouter(String friend, String team) {
    routerFriend = friend;
    routerTeam = team;
  }

  public void query(String text) {

    resultList.clear();
    if (!TextUtils.isEmpty(text)) {
      SearchRepo.searchFriend(
          text,
          new FetchCallback<List<FriendSearchInfo>>() {
            @Override
            public void onSuccess(@Nullable List<FriendSearchInfo> param) {
              if (param != null && param.size() > 0) {
                List<BaseBean> friendResult = new ArrayList<>();
                friendResult.add(new SearchTitleBean(R.string.global_search_friend_title));
                ALog.d(LIB_TAG, TAG, "searchFriend,onSuccess,friend");
                for (int index = 0; index < param.size(); index++) {
                  friendResult.add(new SearchFriendBean(param.get(index), routerFriend));
                  ALog.d(
                      LIB_TAG,
                      TAG,
                      "searchFriend,onSuccess:" + param.get(index).getFriendInfo().getName());
                }
                resultList.addAll(0, friendResult);
              }
              FetchResult<List<BaseBean>> fetchResult = new FetchResult<>(LoadStatus.Success);
              fetchResult.setData(resultList);
              queryLiveData.postValue(fetchResult);
            }

            @Override
            public void onFailed(int code) {
              ALog.d(LIB_TAG, TAG, "searchFriend,onFailed:" + code);
            }

            @Override
            public void onException(@Nullable Throwable exception) {
              ALog.d(LIB_TAG, TAG, "searchFriend,onException");
            }
          });

      SearchRepo.searchGroup(
          text,
          new FetchCallback<List<TeamSearchInfo>>() {
            @Override
            public void onSuccess(@Nullable List<TeamSearchInfo> param) {
              if (param != null && param.size() > 0) {
                List<BaseBean> groupResult = new ArrayList<>();
                groupResult.add(new SearchTitleBean(R.string.global_search_group_title));
                for (int index = 0; index < param.size(); index++) {
                  groupResult.add(new SearchTeamBean(param.get(index), routerTeam));
                }
                ALog.d(LIB_TAG, TAG, "searchTeamm,onSuccess,team:" + param.size());
                resultList.addAll(groupResult);
              }
              FetchResult<List<BaseBean>> fetchResult = new FetchResult<>(LoadStatus.Success);
              fetchResult.setData(resultList);
              queryLiveData.postValue(fetchResult);
            }

            @Override
            public void onFailed(int code) {
              ALog.d(LIB_TAG, TAG, "searchFriend,onFailed:" + code);
            }

            @Override
            public void onException(@Nullable Throwable exception) {
              ALog.d(LIB_TAG, TAG, "searchFriend:onException");
            }
          });

      SearchRepo.searchTeam(
          text,
          new FetchCallback<List<TeamSearchInfo>>() {
            @Override
            public void onSuccess(@Nullable List<TeamSearchInfo> param) {
              if (param != null && param.size() > 0) {
                List<BaseBean> friendResult = new ArrayList<>();
                friendResult.add(new SearchTitleBean(R.string.global_search_team_title));
                for (int index = 0; index < param.size(); index++) {
                  friendResult.add(new SearchTeamBean(param.get(index), routerTeam));
                }
                resultList.addAll(friendResult);
                ALog.d(LIB_TAG, TAG, "searchTeam,onSuccess:" + friendResult.size());
              }
              FetchResult<List<BaseBean>> fetchResult = new FetchResult<>(LoadStatus.Success);
              fetchResult.setData(resultList);
              queryLiveData.postValue(fetchResult);
            }

            @Override
            public void onFailed(int code) {
              ALog.d(LIB_TAG, TAG, "searchFriend,onFailed:" + code);
            }

            @Override
            public void onException(@Nullable Throwable exception) {
              ALog.d(LIB_TAG, TAG, "searchFriend,onException");
            }
          });

    } else {
      FetchResult<List<BaseBean>> fetchResult = new FetchResult<>(LoadStatus.Success);
      fetchResult.setData(resultList);
      queryLiveData.postValue(fetchResult);
    }
  }
}
