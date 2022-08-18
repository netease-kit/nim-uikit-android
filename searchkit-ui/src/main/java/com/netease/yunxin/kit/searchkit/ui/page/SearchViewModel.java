// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.searchkit.ui.page;

import android.text.TextUtils;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import com.netease.yunxin.kit.common.ui.viewholder.BaseBean;
import com.netease.yunxin.kit.common.ui.viewmodel.BaseViewModel;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.corekit.im.provider.FetchCallback;
import com.netease.yunxin.kit.searchkit.SearchRepo;
import com.netease.yunxin.kit.searchkit.model.FriendSearchInfo;
import com.netease.yunxin.kit.searchkit.model.TeamSearchInfo;
import com.netease.yunxin.kit.searchkit.ui.R;
import com.netease.yunxin.kit.searchkit.ui.commone.XLog;
import com.netease.yunxin.kit.searchkit.ui.model.FriendBean;
import com.netease.yunxin.kit.searchkit.ui.model.TeamBean;
import com.netease.yunxin.kit.searchkit.ui.model.TitleBean;
import java.util.ArrayList;
import java.util.List;

/** to provider search data and operation */
public class SearchViewModel extends BaseViewModel {

  private static final String TAG = "SearchViewModel";
  private final MutableLiveData<FetchResult<List<BaseBean>>> queryLiveData =
      new MutableLiveData<>();

  private final List<BaseBean> resultList = new ArrayList<>();

  public MutableLiveData<FetchResult<List<BaseBean>>> getQueryLiveData() {
    return queryLiveData;
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
                friendResult.add(new TitleBean(R.string.global_search_friend_title));
                XLog.d(TAG, "searchFriend:onSuccess,friend");
                for (int index = 0; index < param.size(); index++) {
                  friendResult.add(new FriendBean(param.get(index)));
                  XLog.d(
                      TAG,
                      "searchFriend:onSuccess",
                      "friend:" + param.get(index).getFriendInfo().getName());
                }
                resultList.addAll(0, friendResult);
              }
              FetchResult<List<BaseBean>> fetchResult = new FetchResult<>(LoadStatus.Success);
              fetchResult.setData(resultList);
              queryLiveData.postValue(fetchResult);
            }

            @Override
            public void onFailed(int code) {
              XLog.d(TAG, "searchFriend:onFailed");
            }

            @Override
            public void onException(@Nullable Throwable exception) {
              XLog.d(TAG, "searchFriend:onException");
            }
          });

      SearchRepo.searchGroup(
          text,
          new FetchCallback<List<TeamSearchInfo>>() {
            @Override
            public void onSuccess(@Nullable List<TeamSearchInfo> param) {
              if (param != null && param.size() > 0) {
                List<BaseBean> groupResult = new ArrayList<>();
                groupResult.add(new TitleBean(R.string.global_search_group_title));
                XLog.d(TAG, "searchTeam:onSuccess,team");
                for (int index = 0; index < param.size(); index++) {
                  groupResult.add(new TeamBean(param.get(index)));
                  XLog.d(
                      TAG, "searchTeam:onSuccess", "team:" + param.get(index).getTeam().getName());
                }
                resultList.addAll(groupResult);
              }
              FetchResult<List<BaseBean>> fetchResult = new FetchResult<>(LoadStatus.Success);
              fetchResult.setData(resultList);
              queryLiveData.postValue(fetchResult);
            }

            @Override
            public void onFailed(int code) {
              XLog.d(TAG, "searchFriend:onFailed");
            }

            @Override
            public void onException(@Nullable Throwable exception) {
              XLog.d(TAG, "searchFriend:onException");
            }
          });

      SearchRepo.searchTeam(
          text,
          new FetchCallback<List<TeamSearchInfo>>() {
            @Override
            public void onSuccess(@Nullable List<TeamSearchInfo> param) {
              if (param != null && param.size() > 0) {
                List<BaseBean> friendResult = new ArrayList<>();
                friendResult.add(new TitleBean(R.string.global_search_team_title));
                XLog.d(TAG, "searchTeam:onSuccess,super team");
                for (int index = 0; index < param.size(); index++) {
                  friendResult.add(new TeamBean(param.get(index)));
                  XLog.d(
                      TAG, "searchTeam:onSuccess", "team:" + param.get(index).getTeam().getName());
                }
                resultList.addAll(friendResult);
              }
              FetchResult<List<BaseBean>> fetchResult = new FetchResult<>(LoadStatus.Success);
              fetchResult.setData(resultList);
              queryLiveData.postValue(fetchResult);
            }

            @Override
            public void onFailed(int code) {
              XLog.d(TAG, "searchFriend:onFailed");
            }

            @Override
            public void onException(@Nullable Throwable exception) {
              XLog.d(TAG, "searchFriend:onException");
            }
          });

    } else {
      FetchResult<List<BaseBean>> fetchResult = new FetchResult<>(LoadStatus.Success);
      fetchResult.setData(resultList);
      queryLiveData.postValue(fetchResult);
    }
  }
}
