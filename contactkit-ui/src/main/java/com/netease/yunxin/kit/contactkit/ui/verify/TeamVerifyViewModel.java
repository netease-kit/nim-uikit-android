// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.verify;

import static com.netease.yunxin.kit.contactkit.ui.ContactConstant.LIB_TAG;

import android.content.Context;
import android.text.TextUtils;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import com.netease.nimlib.sdk.v2.team.V2NIMTeamListener;
import com.netease.nimlib.sdk.v2.team.enums.V2NIMTeamJoinActionStatus;
import com.netease.nimlib.sdk.v2.team.enums.V2NIMTeamJoinActionType;
import com.netease.nimlib.sdk.v2.team.enums.V2NIMTeamType;
import com.netease.nimlib.sdk.v2.team.model.V2NIMTeam;
import com.netease.nimlib.sdk.v2.team.model.V2NIMTeamJoinActionInfo;
import com.netease.nimlib.sdk.v2.team.option.V2NIMTeamJoinActionInfoQueryOption;
import com.netease.nimlib.sdk.v2.team.result.V2NIMTeamJoinActionInfoResult;
import com.netease.nimlib.sdk.v2.user.V2NIMUser;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.impl.TeamListenerImpl;
import com.netease.yunxin.kit.chatkit.repo.ContactRepo;
import com.netease.yunxin.kit.chatkit.repo.TeamRepo;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import com.netease.yunxin.kit.common.ui.viewmodel.BaseViewModel;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.contactkit.ui.model.TeamVerifyInfoBean;
import com.netease.yunxin.kit.contactkit.ui.utils.ContactUtils;
import com.netease.yunxin.kit.corekit.im2.extend.FetchCallback;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TeamVerifyViewModel extends BaseViewModel {

  private final String TAG = "TeamVerifyViewModel";
  private static final int PAGE_LIMIT = 100;
  private boolean hasMore = true;
  private final MutableLiveData<FetchResult<List<TeamVerifyInfoBean>>> resultLiveData =
      new MutableLiveData<>();
  private final FetchResult<List<TeamVerifyInfoBean>> fetchResult =
      new FetchResult<>(LoadStatus.Finish);
  private final List<TeamVerifyInfoBean> teamVerifyBeanList = new ArrayList<>();
  private final MutableLiveData<FetchResult<List<String>>> updateLiveData = new MutableLiveData<>();

  private final List<TeamVerifyInfoBean> updateList = new ArrayList<>();

  private final Map<String, V2NIMTeam> teamMap = new HashMap<>();
  private final Map<String, V2NIMUser> userMap = new HashMap<>();
  private long readTimeStamp = 0;
  private final V2NIMTeamListener teamListener;

  public MutableLiveData<FetchResult<List<TeamVerifyInfoBean>>> getFetchResult() {
    return resultLiveData;
  }

  public MutableLiveData<FetchResult<List<String>>> getUpdateLiveData() {
    return updateLiveData;
  }

  public TeamVerifyViewModel() {
    teamListener =
        new TeamListenerImpl() {

          @Override
          public void onTeamJoined(V2NIMTeam team) {
            ALog.d(LIB_TAG, TAG, "onTeamJoined:" + team.getName());
          }

          @Override
          public void onReceiveTeamJoinActionInfo(V2NIMTeamJoinActionInfo joinActionInfo) {
            ALog.d(
                LIB_TAG,
                TAG,
                "onReceiveTeamJoinActionInfo:"
                    + joinActionInfo.getTeamId()
                    + ",action:"
                    + joinActionInfo.getActionType());
            List<V2NIMTeamJoinActionInfo> joinList = new ArrayList<>();
            joinList.add(joinActionInfo);
            List<TeamVerifyInfoBean> addList = mergeAndAddTeamUserData(joinList, true);
            if (!addList.isEmpty()) {
              fetchResult.setStatus(LoadStatus.Finish);
              fetchResult.setFetchType(FetchResult.FetchType.Add);
              fetchResult.setData(addList);
              resultLiveData.setValue(fetchResult);

            } else if (!updateList.isEmpty()) {
              fetchResult.setStatus(LoadStatus.Finish);
              fetchResult.setFetchType(FetchResult.FetchType.Update);
              fetchResult.setData(updateList);
              resultLiveData.setValue(fetchResult);
            }
          }
        };
    TeamRepo.addTeamListener(teamListener);
    readTimeStamp = ContactUtils.getTeamVerifyReadTime();
  }

  public void getTeamVerifyList(boolean nextPage) {
    ALog.d(LIB_TAG, TAG, "getTeamVerifyList,nextPage:" + nextPage);
    fetchResult.setStatus(LoadStatus.Loading);
    resultLiveData.postValue(fetchResult);
    if (nextPage && !hasMore) {
      return;
    }
    V2NIMTeamJoinActionInfoQueryOption option = new V2NIMTeamJoinActionInfoQueryOption();
    option.setLimit(PAGE_LIMIT);
    TeamRepo.getTeamJoinActionInfoList(
        option,
        new FetchCallback<V2NIMTeamJoinActionInfoResult>() {
          @Override
          public void onError(int errorCode, @Nullable String errorMsg) {
            ALog.d(LIB_TAG, TAG, "getTeamVerifyList,onError:" + errorCode);
            fetchResult.setError(errorCode, errorMsg);
            resultLiveData.setValue(fetchResult);
          }

          @Override
          public void onSuccess(@Nullable V2NIMTeamJoinActionInfoResult data) {
            ALog.d(LIB_TAG, TAG, "getTeamVerifyList,onSuccess:");
            fetchResult.setStatus(LoadStatus.Success);
            fetchResult.setFetchType(FetchResult.FetchType.Add);
            if (!nextPage) {
              teamVerifyBeanList.clear();
            }
            if (data != null && !data.getInfos().isEmpty()) {
              hasMore = !data.isFinished();
              mergeAndAddTeamUserData(data.getInfos(), false);
              fetchResult.setData(teamVerifyBeanList);
            } else {
              hasMore = false;
              fetchResult.setData(null);
            }
            resultLiveData.setValue(fetchResult);
          }
        });
  }

  public void getTeamInfo(List<String> teamIdList) {
    if (teamIdList == null || teamIdList.isEmpty()) {
      return;
    }
    ALog.d(LIB_TAG, TAG, "getTeamInfo,teamIdList:" + teamIdList.size());

    TeamRepo.getTeamInfoByIds(
        teamIdList,
        V2NIMTeamType.V2NIM_TEAM_TYPE_NORMAL,
        new FetchCallback<List<V2NIMTeam>>() {
          @Override
          public void onError(int errorCode, @Nullable String errorMsg) {}

          @Override
          public void onSuccess(@Nullable List<V2NIMTeam> data) {
            if (data == null || data.isEmpty()) {
              return;
            }
            List<TeamVerifyInfoBean> teamList = new ArrayList<>();
            FetchResult<List<TeamVerifyInfoBean>> result = new FetchResult<>(LoadStatus.Finish);
            for (V2NIMTeam team : data) {
              teamMap.put(team.getTeamId(), team);
            }
            for (TeamVerifyInfoBean bean : teamVerifyBeanList) {
              for (V2NIMTeam team : data) {
                if (TextUtils.equals(team.getTeamId(), bean.getTeamId())) {
                  bean.setJoinTeam(team);
                  teamList.add(bean);
                  ALog.d(LIB_TAG, TAG, "getTeamInfo: setJoinTeam teamName:" + team.getName());
                  break;
                }
              }
            }
            result.setFetchType(FetchResult.FetchType.Update);
            result.setData(teamList);
            resultLiveData.setValue(result);
          }
        });
  }

  public void getUserInfo(List<String> accountIdList) {
    ALog.d(LIB_TAG, TAG, "getTeamInfo,getUserInfo:" + accountIdList.size());

    ContactRepo.getUserInfo(
        accountIdList,
        new FetchCallback<List<V2NIMUser>>() {
          @Override
          public void onError(int errorCode, @Nullable String errorMsg) {}

          @Override
          public void onSuccess(@Nullable List<V2NIMUser> data) {
            if (data == null || data.isEmpty()) {
              return;
            }
            List<TeamVerifyInfoBean> userList = new ArrayList<>();
            for (V2NIMUser user : data) {
              userMap.put(user.getAccountId(), user);
            }
            for (TeamVerifyInfoBean bean : teamVerifyBeanList) {
              for (V2NIMUser user : data) {
                if (TextUtils.equals(user.getAccountId(), bean.actionInfo.getOperatorAccountId())) {
                  bean.setOperatorUser(user);
                  userList.add(bean);
                  break;
                }
              }
            }
            FetchResult<List<TeamVerifyInfoBean>> result = new FetchResult<>(LoadStatus.Finish);
            result.setFetchType(FetchResult.FetchType.Update);
            result.setData(userList);
            resultLiveData.setValue(result);
          }
        });
  }

  public boolean hasMore() {
    return hasMore;
  }

  public void clearUnreadCount(Context context) {
    updateReadTime();
    for (TeamVerifyInfoBean bean : teamVerifyBeanList) {
      bean.setReadTimeStamp(readTimeStamp);
    }
    FetchResult<List<TeamVerifyInfoBean>> result = new FetchResult<>(LoadStatus.Finish);
    result.setFetchType(FetchResult.FetchType.Update);
    result.setData(teamVerifyBeanList);
    resultLiveData.setValue(result);
  }

  public void clearNotify() {
    ALog.d(LIB_TAG, TAG, "clearNotify");
    TeamRepo.clearAllTeamJoinActionInfo(null);
    fetchResult.setLoadStatus(LoadStatus.Finish);
    fetchResult.setFetchType(FetchResult.FetchType.Remove);
    fetchResult.setData(teamVerifyBeanList);
    resultLiveData.setValue(fetchResult);
    teamVerifyBeanList.clear();
  }

  public void acceptJoinApplication(TeamVerifyInfoBean bean) {
    if (bean == null) {
      return;
    }
    String operatorAccountId = bean.actionInfo.getOperatorAccountId();
    ALog.d(LIB_TAG, TAG, "acceptJoinApplication: agreeUserId" + operatorAccountId);
    if (bean.getActionType() == V2NIMTeamJoinActionType.V2NIM_TEAM_JOIN_ACTION_TYPE_APPLICATION) {
      TeamRepo.acceptJoinApplication(
          bean.actionInfo,
          new FetchCallback<Void>() {
            @Override
            public void onError(int errorCode, @Nullable String errorMsg) {
              ALog.e(LIB_TAG, TAG, "acceptJoinApplication onError: " + errorCode + errorMsg);
              ToastX.showShortToast(ContactUtils.getErrorCodeAndToast(errorCode));
              operateErrorToVerifyInfo(bean);
            }

            @Override
            public void onSuccess(@Nullable Void data) {
              bean.setActionStatus(V2NIMTeamJoinActionStatus.V2NIM_TEAM_JOIN_ACTION_STATUS_AGREED);
              List<TeamVerifyInfoBean> actionList = new ArrayList<>();
              actionList.add(bean);
              FetchResult<List<TeamVerifyInfoBean>> result = new FetchResult<>(LoadStatus.Finish);
              result.setFetchType(FetchResult.FetchType.Update);
              result.setData(actionList);
              resultLiveData.setValue(result);
            }
          });
    } else if (bean.getActionType()
        == V2NIMTeamJoinActionType.V2NIM_TEAM_JOIN_ACTION_TYPE_INVITATION) {
      TeamRepo.acceptInvite(
          bean.actionInfo,
          new FetchCallback<V2NIMTeam>() {
            @Override
            public void onError(int errorCode, @Nullable String errorMsg) {
              ALog.e(LIB_TAG, TAG, "acceptInvite onError: " + errorCode + errorMsg);
              ToastX.showShortToast(ContactUtils.getErrorCodeAndToast(errorCode));
              operateErrorToVerifyInfo(bean);
            }

            @Override
            public void onSuccess(@Nullable V2NIMTeam data) {
              bean.setActionStatus(V2NIMTeamJoinActionStatus.V2NIM_TEAM_JOIN_ACTION_STATUS_AGREED);
              List<TeamVerifyInfoBean> actionList = new ArrayList<>();
              actionList.add(bean);
              FetchResult<List<TeamVerifyInfoBean>> result = new FetchResult<>(LoadStatus.Finish);
              result.setFetchType(FetchResult.FetchType.Update);
              result.setData(actionList);
              resultLiveData.setValue(result);
            }
          });
    }
  }

  public void rejectJoinApplication(TeamVerifyInfoBean bean) {
    ALog.d(LIB_TAG, TAG, "rejectJoinApplication:");
    if (bean == null || bean.actionInfo == null) {
      return;
    }
    if (bean.getActionType() == V2NIMTeamJoinActionType.V2NIM_TEAM_JOIN_ACTION_TYPE_APPLICATION) {
      TeamRepo.rejectJoinApplication(
          bean.actionInfo,
          null,
          new FetchCallback<Void>() {
            @Override
            public void onError(int errorCode, @Nullable String errorMsg) {
              ALog.e(LIB_TAG, TAG, "acceptInvite onError: " + errorCode + errorMsg);
              ToastX.showShortToast(ContactUtils.getErrorCodeAndToast(errorCode));
              operateErrorToVerifyInfo(bean);
            }

            @Override
            public void onSuccess(@Nullable Void data) {
              bean.setActionStatus(
                  V2NIMTeamJoinActionStatus.V2NIM_TEAM_JOIN_ACTION_STATUS_REJECTED);
              List<TeamVerifyInfoBean> actionList = new ArrayList<>();
              actionList.add(bean);
              FetchResult<List<TeamVerifyInfoBean>> result = new FetchResult<>(LoadStatus.Finish);
              result.setFetchType(FetchResult.FetchType.Update);
              result.setData(actionList);
              resultLiveData.setValue(result);
            }
          });
    } else if (bean.getActionType()
        == V2NIMTeamJoinActionType.V2NIM_TEAM_JOIN_ACTION_TYPE_INVITATION) {
      TeamRepo.rejectInvite(
          bean.actionInfo,
          null,
          new FetchCallback<Void>() {
            @Override
            public void onError(int errorCode, @Nullable String errorMsg) {
              ALog.e(LIB_TAG, TAG, "acceptInvite onError: " + errorCode + errorMsg);
              ToastX.showShortToast(ContactUtils.getErrorCodeAndToast(errorCode));
              operateErrorToVerifyInfo(bean);
            }

            @Override
            public void onSuccess(@Nullable Void data) {
              bean.setActionStatus(
                  V2NIMTeamJoinActionStatus.V2NIM_TEAM_JOIN_ACTION_STATUS_REJECTED);
              List<TeamVerifyInfoBean> actionList = new ArrayList<>();
              actionList.add(bean);
              FetchResult<List<TeamVerifyInfoBean>> result = new FetchResult<>(LoadStatus.Finish);
              result.setFetchType(FetchResult.FetchType.Update);
              result.setData(actionList);
              resultLiveData.setValue(result);
            }
          });
    }
  }

  private List<TeamVerifyInfoBean> mergeAndAddTeamUserData(
      List<V2NIMTeamJoinActionInfo> infoList, boolean needUpdate) {
    List<TeamVerifyInfoBean> addList = new ArrayList<>();
    updateList.clear();
    Set<String> teamList = new HashSet<>();
    Set<String> accountList = new HashSet<>();
    if (infoList != null) {
      for (int index = 0; index < infoList.size(); index++) {
        boolean hasInsert = false;
        for (TeamVerifyInfoBean bean : teamVerifyBeanList) {
          if (bean.pushMessageIfSame(infoList.get(index))) {
            hasInsert = true;
            if (needUpdate) {
              updateList.add(bean);
            }
            break;
          }
        }
        if (!hasInsert) {
          ALog.d(
              LIB_TAG,
              TAG,
              "mergeAndAddTeamUserData,create TeamVerifyInfoBean readTime:"
                  + readTimeStamp
                  + "createTime:"
                  + infoList.get(index).getTimestamp());

          TeamVerifyInfoBean infoBean = new TeamVerifyInfoBean(infoList.get(index), readTimeStamp);
          if (userMap.containsKey(infoBean.getOperateAccountId())) {
            infoBean.setOperatorUser(userMap.get(infoBean.getOperateAccountId()));
          } else {
            accountList.add(infoBean.getOperateAccountId());
          }
          if (teamMap.containsKey(infoBean.getTeamId())) {
            infoBean.setJoinTeam(teamMap.get(infoBean.getTeamId()));
          } else {
            teamList.add(infoBean.getTeamId());
          }
          teamVerifyBeanList.add(infoBean);
          addList.add(infoBean);
        }
      }
    }
    if (!teamList.isEmpty()) {
      getTeamInfo(new ArrayList<>(teamList));
    }
    if (!accountList.isEmpty()) {
      getUserInfo(new ArrayList<>(accountList));
    }
    return addList;
  }

  public void operateErrorToVerifyInfo(TeamVerifyInfoBean bean) {
    bean.setActionStatus(V2NIMTeamJoinActionStatus.V2NIM_TEAM_JOIN_ACTION_STATUS_EXPIRED);
    List<TeamVerifyInfoBean> actionList = new ArrayList<>();
    actionList.add(bean);
    FetchResult<List<TeamVerifyInfoBean>> result = new FetchResult<>(LoadStatus.Finish);
    result.setFetchType(FetchResult.FetchType.Update);
    result.setData(actionList);
    resultLiveData.setValue(result);
  }

  public void updateReadTime() {
    readTimeStamp = System.currentTimeMillis();
    ContactUtils.setTeamVerifyReadTime(readTimeStamp);
  }

  @Override
  protected void onCleared() {
    super.onCleared();
    TeamRepo.removeTeamListener(teamListener);
  }
}
