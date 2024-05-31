// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.team;

import static com.netease.yunxin.kit.contactkit.ui.ContactConstant.LIB_TAG;

import android.text.TextUtils;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import com.netease.nimlib.sdk.v2.team.V2NIMTeamListener;
import com.netease.nimlib.sdk.v2.team.model.V2NIMTeam;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.impl.TeamListenerImpl;
import com.netease.yunxin.kit.chatkit.repo.TeamRepo;
import com.netease.yunxin.kit.common.ui.viewmodel.BaseViewModel;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.contactkit.ui.model.ContactTeamBean;
import com.netease.yunxin.kit.corekit.im2.extend.FetchCallback;
import com.netease.yunxin.kit.corekit.im2.utils.RouterConstant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/** 群组列表ViewModel */
public class TeamListViewModel extends BaseViewModel {
  private final String TAG = "TeamListViewModel";

  // 群组列表数据
  private final MutableLiveData<FetchResult<List<ContactTeamBean>>> resultLiveData =
      new MutableLiveData<>();
  private final FetchResult<List<ContactTeamBean>> fetchResult =
      new FetchResult<>(LoadStatus.Finish);
  private final List<ContactTeamBean> teamBeanList = new ArrayList<>();
  // 默认跳转路径
  private String defaultRoutePath = RouterConstant.PATH_CHAT_TEAM_PAGE;

  public void configRoutePath(String path) {
    this.defaultRoutePath = path;
  }

  public MutableLiveData<FetchResult<List<ContactTeamBean>>> getFetchResult() {
    return resultLiveData;
  }

  public TeamListViewModel() {
    TeamRepo.addTeamListener(teamListener);
  }

  // 群组监听
  private V2NIMTeamListener teamListener =
      new TeamListenerImpl() {

        @Override
        public void onTeamCreated(V2NIMTeam team) {
          ALog.d(LIB_TAG, TAG, "onTeamCreated:" + (team != null ? team.getTeamId() : "null"));
          updateTeamData(team);
        }

        @Override
        public void onTeamDismissed(V2NIMTeam team) {
          ALog.d(LIB_TAG, TAG, "onTeamDismissed:" + (team != null ? team.getTeamId() : "null"));

          removeTeamData(team);
        }

        @Override
        public void onTeamJoined(V2NIMTeam team) {
          ALog.d(LIB_TAG, TAG, "onTeamJoined:" + (team != null ? team.getTeamId() : "null"));

          updateTeamData(team);
        }

        @Override
        public void onTeamLeft(V2NIMTeam team, boolean isKicked) {
          ALog.d(LIB_TAG, TAG, "onTeamLeft:" + (team != null ? team.getTeamId() : "null"));

          removeTeamData(team);
        }

        @Override
        public void onTeamInfoUpdated(V2NIMTeam team) {
          ALog.d(LIB_TAG, TAG, "onTeamInfoUpdated:" + (team != null ? team.getTeamId() : "null"));

          updateTeamData(team);
        }
      };

  // 获取群组列表
  public void getTeamList() {
    ALog.d(LIB_TAG, TAG, "getTeamList");
    fetchResult.setStatus(LoadStatus.Loading);
    resultLiveData.postValue(fetchResult);
    TeamRepo.getTeamList(
        new FetchCallback<>() {
          @Override
          public void onError(int errorCode, @Nullable String errorMsg) {
            ALog.d(LIB_TAG, TAG, "getTeamList,onFailed:" + errorCode);
            fetchResult.setError(errorCode, errorMsg);
            resultLiveData.postValue(fetchResult);
          }

          @Override
          public void onSuccess(@Nullable List<V2NIMTeam> data) {
            ALog.d(LIB_TAG, TAG, "getTeamList,onSuccess:" + (data == null ? "null" : data.size()));
            teamBeanList.clear();
            if (data != null && data.size() > 0) {
              fetchResult.setStatus(LoadStatus.Success);
              for (V2NIMTeam teamInfo : data) {
                ContactTeamBean teamBean = new ContactTeamBean(teamInfo);
                teamBean.router = defaultRoutePath;
                teamBeanList.add(0, teamBean);
              }
              Collections.sort(teamBeanList, teamComparator);
              fetchResult.setData(teamBeanList);
            } else {
              fetchResult.setData(null);
              fetchResult.setStatus(LoadStatus.Success);
            }
            resultLiveData.postValue(fetchResult);
          }
        });
  }

  // 移除群组数据
  private void removeTeamData(V2NIMTeam teamInfo) {
    if (teamInfo != null) {
      ALog.d(LIB_TAG, TAG, "removeTeamData:" + teamInfo.getTeamId());
      List<ContactTeamBean> remove = new ArrayList<>();
      for (ContactTeamBean bean : teamBeanList) {
        if (TextUtils.equals(teamInfo.getTeamId(), bean.data.getTeamId())) {
          remove.add(bean);
          break;
        }
      }

      if (remove.size() > 0) {
        fetchResult.setFetchType(FetchResult.FetchType.Remove);
        fetchResult.setData(remove);
        resultLiveData.postValue(fetchResult);
      } else {
        getTeamList();
      }
    }
  }

  // 更新群组数据
  private void updateTeamData(V2NIMTeam teamInfo) {
    if (teamInfo != null) {
      ALog.d(LIB_TAG, TAG, "updateTeamData:" + teamInfo.getTeamId());
      List<ContactTeamBean> add = new ArrayList<>();
      boolean has = false;
      for (ContactTeamBean bean : teamBeanList) {
        if (TextUtils.equals(teamInfo.getTeamId(), bean.data.getTeamId())) {
          has = true;
          break;
        }
      }
      if (!has) {
        ContactTeamBean teamBean = new ContactTeamBean(teamInfo);
        teamBean.router = defaultRoutePath;
        add.add(teamBean);
        teamBeanList.add(0, teamBean);
      }

      if (add.size() > 0) {
        fetchResult.setFetchType(FetchResult.FetchType.Add);
        fetchResult.setData(add);
        resultLiveData.postValue(fetchResult);
      }
    }
  }

  // 群组排序
  private final Comparator<ContactTeamBean> teamComparator =
      (bean1, bean2) -> {
        int result;
        if (bean1 == null) {
          result = 1;
        } else if (bean2 == null) {
          result = -1;
        } else if (bean1.data.getCreateTime() >= bean2.data.getCreateTime()) {
          result = -1;
        } else {
          result = 0;
        }
        return result;
      };

  // 清除数据
  @Override
  protected void onCleared() {
    super.onCleared();
    TeamRepo.removeTeamListener(teamListener);
  }
}
