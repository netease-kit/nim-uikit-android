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
import com.netease.yunxin.kit.chatkit.utils.ErrorUtils;
import com.netease.yunxin.kit.common.ui.viewmodel.BaseViewModel;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.corekit.im2.IMKitClient;
import com.netease.yunxin.kit.corekit.im2.extend.FetchCallback;
import com.netease.yunxin.kit.teamkit.ui.utils.TeamMemberCache;
import com.netease.yunxin.kit.teamkit.ui.utils.TeamMemberCacheListener;
import java.util.List;
import java.util.Objects;

/** 群基础ViewModel 提供群相关的复用逻辑，包括查询群信息，群成员信息，添加删除成员等 */
public class TeamBaseViewModel extends BaseViewModel {
  private static final String TAG = "TeamBaseViewModel";
  private static final String LIB_TAG = "TeamKit-UI";

  protected String teamId;

  protected boolean hasMore = false;

  protected String nextPageTag = null;

  // 复合查询，查询当前群信息(V2NIMTeam)和当前账户的群成员信息(V2NIMTeamMember)
  private final MutableLiveData<FetchResult<TeamWithCurrentMember>> teamWithMemberData =
      new MutableLiveData<>();

  // 获取群成员信息列表
  private final MutableLiveData<FetchResult<List<TeamMemberWithUserInfo>>> teamMemberWithUserData =
      new MutableLiveData<>();

  // 添加删除成员
  private final MutableLiveData<FetchResult<List<String>>> removeMembersData =
      new MutableLiveData<>();

  // 群信息包括主动请求获取和群信息变更
  private final MutableLiveData<FetchResult<V2NIMTeam>> teamUpdateData = new MutableLiveData<>();

  public MutableLiveData<FetchResult<TeamWithCurrentMember>> getTeamWitheMemberData() {
    return teamWithMemberData;
  }

  public MutableLiveData<FetchResult<List<TeamMemberWithUserInfo>>>
      getTeamMemberListWithUserData() {
    return teamMemberWithUserData;
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
      TeamMemberCache.Instance().addTeamMemberCacheListener(teamMemberListener);
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
    TeamMemberCache.Instance().initTeamId(teamId);
    List<TeamMemberWithUserInfo> memberList = TeamMemberCache.Instance().getTeamMemberList(teamId);
    if (!memberList.isEmpty()) {
      teamMemberWithUserData.setValue(new FetchResult<>(memberList));
    } else {
      TeamMemberCache.Instance().loadTeamMemberList(teamId);
    }
  }

  private final TeamMemberCacheListener teamMemberListener =
      new TeamMemberCacheListener() {

        @Override
        public void onTeamMemberCacheUpdate(
            String teamId, List<TeamMemberWithUserInfo> teamMemberList) {
          ALog.d(LIB_TAG, TAG, "onTeamMemberCacheUpdate:" + teamId);
          if (TextUtils.equals(teamId, TeamBaseViewModel.this.teamId)) {
            FetchResult<List<TeamMemberWithUserInfo>> userData =
                new FetchResult<>(FetchResult.FetchType.Update, teamMemberList);
            teamMemberWithUserData.setValue(userData);
          }
        }

        @Override
        public void onTeamMemberCacheRemove(String teamId, List<String> accountList) {
          ALog.d(LIB_TAG, TAG, "onTeamMemberCacheRemove:" + teamId);
          if (TextUtils.equals(teamId, TeamBaseViewModel.this.teamId)
              && accountList != null
              && !accountList.isEmpty()) {
            notifyRemoveMember(accountList);
          }
        }

        @Override
        public void onTeamMemberCacheLoad(
            String teamId, List<TeamMemberWithUserInfo> teamMemberList) {
          ALog.d(LIB_TAG, TAG, "onTeamMemberCacheLoad:" + teamId);
          if (TextUtils.equals(teamId, TeamBaseViewModel.this.teamId)) {
            teamMemberWithUserData.setValue(new FetchResult<>(teamMemberList));
          }
        }

        @Override
        public void onTeamMemberCacheAdd(
            String teamId, List<TeamMemberWithUserInfo> teamMemberList) {
          if (TextUtils.equals(teamId, TeamBaseViewModel.this.teamId)
              && teamMemberList != null
              && !teamMemberList.isEmpty()) {
            notifyAddMember(teamMemberList);
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
      FetchResult<List<String>> result = new FetchResult<>(removeList);
      result.setType(FetchResult.FetchType.Remove);
      removeMembersData.setValue(result);
    }
  }

  protected void notifyAddMember(List<TeamMemberWithUserInfo> removeList) {
    if (removeList.size() > 0) {
      FetchResult<List<TeamMemberWithUserInfo>> result = new FetchResult<>(removeList);
      result.setType(FetchResult.FetchType.Add);
      teamMemberWithUserData.setValue(result);
    }
  }

  @Override
  protected void onCleared() {
    super.onCleared();
    if (!TextUtils.isEmpty(teamId)) {
      TeamRepo.removeTeamListener(teamListener);
      TeamMemberCache.Instance().removeTeamMemberCacheListener(teamMemberListener);
    }
  }
}
