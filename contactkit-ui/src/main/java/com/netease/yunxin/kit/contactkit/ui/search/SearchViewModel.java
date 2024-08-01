// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.search;

import android.text.TextUtils;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import com.netease.nimlib.sdk.v2.team.model.V2NIMTeam;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.model.FriendSearchInfo;
import com.netease.yunxin.kit.chatkit.model.TeamSearchInfo;
import com.netease.yunxin.kit.chatkit.model.TeamSearchResult;
import com.netease.yunxin.kit.chatkit.repo.SearchRepo;
import com.netease.yunxin.kit.chatkit.repo.TeamRepo;
import com.netease.yunxin.kit.common.ui.viewholder.BaseBean;
import com.netease.yunxin.kit.common.ui.viewmodel.BaseViewModel;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.contactkit.ui.R;
import com.netease.yunxin.kit.contactkit.ui.model.SearchFriendBean;
import com.netease.yunxin.kit.contactkit.ui.model.SearchTeamBean;
import com.netease.yunxin.kit.contactkit.ui.model.SearchTitleBean;
import com.netease.yunxin.kit.corekit.im2.extend.FetchCallback;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/** 搜索页面 ViewModel 提供搜索好友、群组 */
public class SearchViewModel extends BaseViewModel {

  private static final String TAG = "SearchViewModel";
  private static final String LIB_TAG = "SearchKit-UI";
  private final MutableLiveData<FetchResult<List<BaseBean>>> queryLiveData =
      new MutableLiveData<>();

  // 查询群组信息，搜索结果中点击群组时，需要查询群组信息
  private final MutableLiveData<FetchResult<V2NIMTeam>> queryTeamLiveData = new MutableLiveData<>();

  private final List<BaseBean> resultList = new ArrayList<>();

  // 搜索标记为，查询结果都结束时，才返回结果
  private int searchTag = 0;

  // 查询结果
  public MutableLiveData<FetchResult<List<BaseBean>>> getQueryLiveData() {
    return queryLiveData;
  }

  public MutableLiveData<FetchResult<V2NIMTeam>> getQueryTeamLiveData() {
    return queryTeamLiveData;
  }

  protected String routerFriend;
  protected String routerTeam;

  // 设置路由
  public void setRouter(String friend, String team) {
    routerFriend = friend;
    routerTeam = team;
  }

  // 搜索好友和群组
  public void query(String text) {
    resultList.clear();
    searchTag = 0;
    if (!TextUtils.isEmpty(text)) {
      SearchRepo.searchFriend(
          text,
          new FetchCallback<List<FriendSearchInfo>>() {
            @Override
            public void onError(int errorCode, @Nullable String errorMsg) {
              ALog.d(LIB_TAG, TAG, "searchFriend,onFailed:" + errorCode);
              searchTag++;
            }

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
              if (searchTag > 0) {
                FetchResult<List<BaseBean>> fetchResult = new FetchResult<>(LoadStatus.Success);
                fetchResult.setData(resultList);
                queryLiveData.postValue(fetchResult);
              } else {
                searchTag++;
              }
            }
          });

      SearchRepo.searchTeam(
          text,
          new FetchCallback<TeamSearchResult>() {
            @Override
            public void onError(int errorCode, @Nullable String errorMsg) {
              ALog.d(LIB_TAG, TAG, "searchFriend,onFailed:" + errorCode);
              searchTag++;
            }

            @Override
            public void onSuccess(@Nullable TeamSearchResult param) {
              if (param != null) {
                if (param.getGroupList().size() > 0) {
                  List<BaseBean> groupResult = new ArrayList<>();
                  List<TeamSearchInfo> groupList = param.getGroupList();
                  groupResult.add(new SearchTitleBean(R.string.global_search_group_title));
                  Collections.sort(groupList, teamComparator);
                  for (int index = 0; index < groupList.size(); index++) {
                    groupResult.add(new SearchTeamBean(groupList.get(index), routerTeam));
                  }
                  ALog.d(LIB_TAG, TAG, "searchTeam,onSuccess,team:" + groupList.size());
                  resultList.addAll(groupResult);
                }

                if (param.getTeamList().size() > 0) {
                  List<BaseBean> teamResult = new ArrayList<>();
                  List<TeamSearchInfo> teamList = param.getTeamList();
                  teamResult.add(new SearchTitleBean(R.string.global_search_team_title));
                  Collections.sort(teamList, teamComparator);
                  for (int index = 0; index < teamList.size(); index++) {
                    teamResult.add(new SearchTeamBean(teamList.get(index), routerTeam));
                  }
                  resultList.addAll(teamResult);
                  ALog.d(LIB_TAG, TAG, "searchTeam,onSuccess:" + teamResult.size());
                }
              }
              if (searchTag > 0) {
                FetchResult<List<BaseBean>> fetchResult = new FetchResult<>(LoadStatus.Success);
                fetchResult.setData(resultList);
                queryLiveData.postValue(fetchResult);
              } else {
                searchTag++;
              }
            }
          });

    } else {
      FetchResult<List<BaseBean>> fetchResult = new FetchResult<>(LoadStatus.Success);
      fetchResult.setData(resultList);
      queryLiveData.postValue(fetchResult);
    }
  }

  public void queryTeam(String tid) {
    if (TextUtils.isEmpty(tid)) {
      return;
    }

    TeamRepo.getTeamInfo(
        tid,
        new FetchCallback<V2NIMTeam>() {
          @Override
          public void onError(int errorCode, @Nullable String errorMsg) {
            FetchResult<V2NIMTeam> fetchResult = new FetchResult<>(LoadStatus.Error);
            queryTeamLiveData.postValue(fetchResult);
          }

          @Override
          public void onSuccess(@Nullable V2NIMTeam data) {
            FetchResult<V2NIMTeam> fetchResult = new FetchResult<>(LoadStatus.Success);
            fetchResult.setData(data);
            queryTeamLiveData.postValue(fetchResult);
          }
        });
  }

  private Comparator<TeamSearchInfo> teamComparator =
      (bean1, bean2) -> {
        int result;
        if (bean1 == null) {
          result = 1;
        } else if (bean2 == null) {
          result = -1;
        } else if (bean1.getTeam().getCreateTime() >= bean2.getTeam().getCreateTime()) {
          result = -1;
        } else {
          result = 1;
        }
        ALog.d(LIB_TAG, TAG, "teamComparator, result:" + result);
        return result;
      };
}
