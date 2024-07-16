// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.teamkit.ui.viewmodel;

import android.text.TextUtils;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import com.netease.nimlib.sdk.v2.team.enums.V2NIMTeamType;
import com.netease.nimlib.sdk.v2.team.model.V2NIMTeam;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.impl.TeamListenerImpl;
import com.netease.yunxin.kit.chatkit.model.TeamMemberWithUserInfo;
import com.netease.yunxin.kit.chatkit.model.TeamWithCurrentMember;
import com.netease.yunxin.kit.chatkit.repo.TeamRepo;
import com.netease.yunxin.kit.chatkit.ui.cache.TeamUserChangedListener;
import com.netease.yunxin.kit.chatkit.ui.cache.TeamUserManager;
import com.netease.yunxin.kit.chatkit.utils.ErrorUtils;
import com.netease.yunxin.kit.common.ui.viewmodel.BaseViewModel;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.corekit.im2.IMKitClient;
import com.netease.yunxin.kit.corekit.im2.extend.FetchCallback;
import java.util.List;
import java.util.Objects;

/** 群基础ViewModel 提供群相关的复用逻辑，包括查询群信息，群成员信息，添加删除成员等 */
public class TeamBaseViewModel extends BaseViewModel {
  private static final String TAG = "TeamBaseViewModel";
  private static final String LIB_TAG = "TeamKit-UI";

  protected String teamId;

  // 复合查询，查询当前群信息(V2NIMTeam)和当前账户的群成员信息(V2NIMTeamMember)
  protected final MutableLiveData<FetchResult<TeamWithCurrentMember>> teamWithMemberData =
      new MutableLiveData<>();

  // 获取群成员信息列表
  protected final MutableLiveData<FetchResult<List<TeamMemberWithUserInfo>>>
      teamMemberWithUserData = new MutableLiveData<>();

  // 获取群成员信息列表
  protected final MutableLiveData<FetchResult<List<String>>> teamMemberData =
      new MutableLiveData<>();

  // 添加删除成员
  protected final MutableLiveData<FetchResult<List<String>>> removeMembersData =
      new MutableLiveData<>();

  // 群信息包括主动请求获取和群信息变更
  protected final MutableLiveData<FetchResult<V2NIMTeam>> teamUpdateData = new MutableLiveData<>();

  public MutableLiveData<FetchResult<TeamWithCurrentMember>> getTeamWitheMemberData() {
    return teamWithMemberData;
  }

  public MutableLiveData<FetchResult<List<TeamMemberWithUserInfo>>>
      getTeamMemberListWithUserData() {
    return teamMemberWithUserData;
  }

  public MutableLiveData<FetchResult<List<String>>> getTeamMemberListData() {
    return teamMemberData;
  }

  public MutableLiveData<FetchResult<List<String>>> getRemoveMembersData() {
    return removeMembersData;
  }

  public MutableLiveData<FetchResult<V2NIMTeam>> getTeamUpdateData() {
    return teamUpdateData;
  }

  private final TeamListenerImpl teamListener =
      new TeamListenerImpl() {
        @Override
        public void onTeamInfoUpdated(V2NIMTeam team) {
          ALog.d(LIB_TAG, TAG, "teamListener,onTeamInfoUpdated");
          if (team == null) {
            return;
          }
          if (TextUtils.equals(team.getTeamId(), teamId)) {
            teamUpdateData.setValue(new FetchResult<>(team));
          }
        }
      };

  /**
   * 配置群ID，在使用之前必须要调用，业务逻辑中teamId是必要参数
   *
   * @param teamId 群ID
   */
  public void configTeamId(String teamId) {
    if (!TextUtils.isEmpty(teamId)) {
      this.teamId = teamId;
      TeamRepo.addTeamListener(teamListener);
      TeamUserManager.getInstance().init(teamId);
      TeamUserManager.getInstance().addMemberChangedListener(teamUserChangedListener);
    }
  }

  /**
   * 获取群信息
   *
   * @param teamId 群ID
   */
  public void requestTeamData(String teamId) {
    ALog.d(LIB_TAG, TAG, "requestTeamData:" + teamId);
    TeamRepo.getTeamWithMember(
        teamId,
        Objects.requireNonNull(IMKitClient.account()),
        new FetchCallback<>() {
          @Override
          public void onSuccess(@Nullable TeamWithCurrentMember param) {
            ALog.d(LIB_TAG, TAG, "requestTeamData,onSuccess:" + (param == null));
            teamWithMemberData.setValue(new FetchResult<>(param));
          }

          @Override
          public void onError(int errorCode, String errorMsg) {
            ALog.d(LIB_TAG, TAG, "requestTeamData,onFailed:" + errorCode);
            teamWithMemberData.setValue(new FetchResult<>(errorCode, errorMsg));
          }
        });
  }

  public void loadTeamMember() {
    ALog.d(LIB_TAG, TAG, "loadTeamMemberFromCache");
    TeamUserManager.getInstance()
        .getAllTeamMembers(
            true,
            new FetchCallback<List<TeamMemberWithUserInfo>>() {
              @Override
              public void onSuccess(@Nullable List<TeamMemberWithUserInfo> param) {
                ALog.d(
                    LIB_TAG,
                    TAG,
                    "loadTeamMember,onSuccess:" + (param != null ? param.size() : "null"));
                FetchResult<List<TeamMemberWithUserInfo>> fetchResult =
                    new FetchResult<>(LoadStatus.Success);
                fetchResult.setData(param);
                teamMemberWithUserData.setValue(fetchResult);
              }

              @Override
              public void onError(int errorCode, String errorMsg) {
                ALog.d(LIB_TAG, TAG, "loadTeamMember,onFailed:" + errorCode);
                teamMemberWithUserData.setValue(new FetchResult<>(errorCode, errorMsg));
              }
            });
  }

  private final TeamUserChangedListener teamUserChangedListener =
      new TeamUserChangedListener() {
        @Override
        public void onUsersChanged(List<String> accountIds) {
          if (TextUtils.equals(teamId, TeamBaseViewModel.this.teamId)) {
            ALog.d(LIB_TAG, TAG, "teamUserChangedListener,onUsersChanged:" + accountIds.size());
            FetchResult<List<TeamMemberWithUserInfo>> userData =
                new FetchResult<>(
                    FetchResult.FetchType.Update,
                    TeamUserManager.getInstance().getTeamMembersFromCache(accountIds));
            teamMemberWithUserData.setValue(userData);
          }
        }

        @Override
        public void onUsersAdd(List<String> accountIds) {
          if (TextUtils.equals(teamId, TeamBaseViewModel.this.teamId)
              && accountIds != null
              && accountIds.size() > 0) {
            ALog.d(LIB_TAG, TAG, "teamUserChangedListener,onUsersAdd:" + accountIds.size());
            notifyAddMember(TeamUserManager.getInstance().getTeamMembersFromCache(accountIds));
          }
        }

        @Override
        public void onUserDelete(List<String> accountIds) {
          if (accountIds != null) {
            ALog.d(LIB_TAG, TAG, "teamUserChangedListener,onUserDeleter:" + accountIds.size());
            notifyRemoveMember(accountIds);
          }
        }
      };

  /**
   * 添加成员
   *
   * @param teamId 群ID
   * @param members 成员ID列表
   */
  public void addMembers(String teamId, List<String> members) {
    if (members == null || members.size() == 0) {
      return;
    }
    ALog.d(LIB_TAG, TAG, "addMembers:" + teamId + "," + members.size());
    TeamRepo.inviteTeamMembers(
        teamId,
        V2NIMTeamType.V2NIM_TEAM_TYPE_NORMAL,
        members,
        new FetchCallback<>() {
          @Override
          public void onSuccess(@Nullable List<String> param) {
            ALog.d(
                LIB_TAG,
                TAG,
                "addMembers,onSuccess:" + (param != null ? param.size() : "null") + " members");
          }

          @Override
          public void onError(int errorCode, String errorMsg) {
            ALog.d(LIB_TAG, TAG, "addMembers,onFailed:" + errorCode);
            ErrorUtils.showErrorCodeToast(IMKitClient.getApplicationContext(), errorCode);
          }
        });
  }

  /**
   * 移除成员
   *
   * @param teamId 群ID
   * @param members 成员ID列表
   */
  public void removeMember(String teamId, List<String> members) {
    if (members == null || members.size() == 0) {
      return;
    }
    ALog.d(LIB_TAG, TAG, "removeMember:" + teamId + "," + members.size());
    TeamRepo.removeTeamMembers(
        teamId,
        V2NIMTeamType.V2NIM_TEAM_TYPE_NORMAL,
        members,
        new FetchCallback<>() {
          @Override
          public void onSuccess(@Nullable Void param) {
            ALog.d(LIB_TAG, TAG, "removeMember,onSuccess");
          }

          @Override
          public void onError(int errorCode, String errorMsg) {
            ALog.d(LIB_TAG, TAG, "removeMember,onFailed:" + errorCode);
            ErrorUtils.showErrorCodeToast(IMKitClient.getApplicationContext(), errorCode);
          }
        });
  }

  protected void notifyRemoveMember(List<String> removeList) {
    if (removeList.size() > 0) {
      ALog.d(LIB_TAG, TAG, "notifyRemoveMember:" + removeList.size());
      FetchResult<List<String>> result = new FetchResult<>(removeList);
      result.setType(FetchResult.FetchType.Remove);
      removeMembersData.setValue(result);
    }
  }

  protected void notifyAddMember(List<TeamMemberWithUserInfo> addList) {
    if (addList.size() > 0) {
      ALog.d(LIB_TAG, TAG, "notifyAddMember:" + addList.size());
      FetchResult<List<TeamMemberWithUserInfo>> result = new FetchResult<>(addList);
      result.setType(FetchResult.FetchType.Add);
      teamMemberWithUserData.setValue(result);
    }
  }

  @Override
  protected void onCleared() {
    super.onCleared();
    if (!TextUtils.isEmpty(teamId)) {
      TeamRepo.removeTeamListener(teamListener);
      TeamUserManager.getInstance().removeMemberChangedListener(teamUserChangedListener);
    }
  }
}
