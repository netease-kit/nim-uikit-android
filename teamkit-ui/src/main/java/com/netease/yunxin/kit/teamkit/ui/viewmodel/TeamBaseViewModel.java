// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.teamkit.ui.viewmodel;

import android.text.TextUtils;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import com.netease.nimlib.sdk.v2.V2NIMError;
import com.netease.nimlib.sdk.v2.team.V2NIMTeamListener;
import com.netease.nimlib.sdk.v2.team.model.V2NIMTeam;
import com.netease.nimlib.sdk.v2.team.model.V2NIMTeamJoinActionInfo;
import com.netease.nimlib.sdk.v2.team.model.V2NIMTeamMember;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.model.TeamMemberListResult;
import com.netease.yunxin.kit.chatkit.model.TeamMemberWithUserInfo;
import com.netease.yunxin.kit.chatkit.model.TeamWithCurrentMember;
import com.netease.yunxin.kit.chatkit.repo.TeamRepo;
import com.netease.yunxin.kit.chatkit.utils.ErrorUtils;
import com.netease.yunxin.kit.common.ui.viewmodel.BaseViewModel;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.corekit.im2.IMKitClient;
import com.netease.yunxin.kit.corekit.im2.extend.FetchCallback;
import java.util.ArrayList;
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
  private final MutableLiveData<FetchResult<List<String>>> addRemoveMembersData =
      new MutableLiveData<>();

  // 群成员信息变更
  private final MutableLiveData<FetchResult<List<V2NIMTeamMember>>> memberUpdateData =
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

  public MutableLiveData<FetchResult<List<String>>> getAddRemoveMembersData() {
    return addRemoveMembersData;
  }

  public MutableLiveData<FetchResult<List<V2NIMTeamMember>>> getTeamMemberUpdateData() {
    return memberUpdateData;
  }

  public MutableLiveData<FetchResult<V2NIMTeam>> getTeamUpdateData() {
    return teamUpdateData;
  }

  private final V2NIMTeamListener teamListener =
      new V2NIMTeamListener() {
        @Override
        public void onSyncStarted() {}

        @Override
        public void onSyncFinished() {}

        @Override
        public void onSyncFailed(V2NIMError error) {}

        @Override
        public void onTeamCreated(V2NIMTeam team) {}

        @Override
        public void onTeamDismissed(V2NIMTeam team) {}

        @Override
        public void onTeamJoined(V2NIMTeam team) {
          ALog.d(LIB_TAG, TAG, "teamListener,onTeamJoined");
        }

        @Override
        public void onTeamLeft(V2NIMTeam team, boolean isKicked) {
          ALog.d(LIB_TAG, TAG, "teamListener,onTeamLeft, isKicked:" + isKicked);
        }

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

        @Override
        public void onTeamMemberJoined(List<V2NIMTeamMember> teamMembers) {
          ALog.d(LIB_TAG, TAG, "teamListener,onTeamMemberJoined");
          addRemoveMembersData(teamMembers, true);
        }

        @Override
        public void onTeamMemberKicked(
            String operatorAccountId, List<V2NIMTeamMember> teamMembers) {
          ALog.d(LIB_TAG, TAG, "teamListener,onTeamMemberKicked");
          addRemoveMembersData(teamMembers, false);
        }

        @Override
        public void onTeamMemberLeft(List<V2NIMTeamMember> teamMembers) {
          ALog.d(LIB_TAG, TAG, "teamListener,onTeamMemberLeft");
          addRemoveMembersData(teamMembers, false);
        }

        @Override
        public void onTeamMemberInfoUpdated(List<V2NIMTeamMember> teamMembers) {
          ALog.d(LIB_TAG, TAG, "teamListener,onTeamMemberInfoUpdated");
          if (teamMembers == null) {
            return;
          }
          ArrayList<V2NIMTeamMember> updateTeamMembers = new ArrayList<>();
          for (V2NIMTeamMember item : teamMembers) {
            if (!TextUtils.equals(item.getTeamId(), teamId)) {
              continue;
            }
            updateTeamMembers.add(item);
          }
          if (updateTeamMembers.size() > 0) {
            memberUpdateData.setValue(new FetchResult<>(updateTeamMembers));
          }
        }

        @Override
        public void onReceiveTeamJoinActionInfo(V2NIMTeamJoinActionInfo joinActionInfo) {}
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

  /**
   * 获取群成员列表信息
   *
   * @param teamId 群ID
   */
  public void requestTeamMembers(String teamId) {
    ALog.d(LIB_TAG, TAG, "requestTeamMembers:" + teamId);
    TeamRepo.getTeamMemberListWithUserInfo(
        teamId,
        new FetchCallback<>() {
          @Override
          public void onSuccess(@Nullable TeamMemberListResult param) {
            ALog.d(
                LIB_TAG,
                TAG,
                "requestTeamMembers,onSuccess:" + (param == null ? "null" : param.isFinished()));
            if (param != null) {
              hasMore = !param.isFinished();
              nextPageTag = param.getNextToken();
              teamMemberWithUserData.setValue(new FetchResult<>(param.getMemberList()));
            }
          }

          @Override
          public void onError(int errorCode, String errorMsg) {
            ALog.d(LIB_TAG, TAG, "requestTeamMembers,onFailed:" + errorCode);
            teamMemberWithUserData.setValue(new FetchResult<>(errorCode, errorMsg));
          }
        });
  }

  /**
   * 获取群成员列表信息
   *
   * @param teamId 群ID
   */
  public void requestAllTeamMembers(String teamId) {
    ALog.d(LIB_TAG, TAG, "requestAllTeamMembers:" + teamId);
    TeamRepo.queryAllTeamMemberListWithUserInfo(
        teamId,
        new FetchCallback<>() {
          @Override
          public void onSuccess(@Nullable List<TeamMemberWithUserInfo> param) {
            ALog.d(
                LIB_TAG,
                TAG,
                "requestAllTeamMembers,onSuccess:" + (param == null ? "null" : param.size()));
            if (param != null) {
              teamMemberWithUserData.setValue(new FetchResult<>(param));
            }
          }

          @Override
          public void onError(int errorCode, String errorMsg) {
            ALog.d(LIB_TAG, TAG, "requestAllTeamMembers,onFailed:" + errorCode);
            teamMemberWithUserData.setValue(new FetchResult<>(errorCode, errorMsg));
          }
        });
  }

  // 加载更多群成员
  public void requestMoreTeamMember(String teamId) {
    ALog.d(LIB_TAG, TAG, "requestMoreTeamMember:" + teamId);
    if (!hasMore) {
      return;
    }
    TeamRepo.getTeamMemberListWithUserInfo(
        teamId,
        nextPageTag,
        new FetchCallback<>() {
          @Override
          public void onSuccess(@Nullable TeamMemberListResult param) {
            ALog.d(
                LIB_TAG,
                TAG,
                "requestMoreTeamMember,onSuccess:" + (param == null ? "null" : param.isFinished()));
            if (param != null) {
              hasMore = !param.isFinished();
              nextPageTag = param.getNextToken();
              FetchResult<List<TeamMemberWithUserInfo>> fetchResult =
                  new FetchResult<>(param.getMemberList());
              fetchResult.setFetchType(FetchResult.FetchType.Add);
              teamMemberWithUserData.setValue(fetchResult);
            }
          }

          @Override
          public void onError(int errorCode, String errorMsg) {
            ALog.d(LIB_TAG, TAG, "requestMoreTeamMember,onFailed:" + errorCode);
            teamMemberWithUserData.setValue(new FetchResult<>(errorCode, errorMsg));
          }
        });
  }

  // 是否有更多数据
  public boolean hasMore() {
    return hasMore;
  }

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
    TeamRepo.inviteUser(
        teamId,
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
    TeamRepo.removeMembers(
        teamId,
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

  /** 移除成员逻辑处理LiveData */
  protected void addRemoveMembersData(List<V2NIMTeamMember> teamMembers, boolean isAdd) {
    if (teamMembers == null) {
      return;
    }
    ALog.d(LIB_TAG, TAG, "removeMembersData,teamMembers.size():" + teamMembers.size());
    ArrayList<String> removeList = new ArrayList<>();
    for (V2NIMTeamMember item : teamMembers) {
      if (!TextUtils.equals(item.getTeamId(), teamId)) {
        continue;
      }
      removeList.add(item.getAccountId());
    }
    if (removeList.size() > 0) {
      FetchResult<List<String>> result = new FetchResult<>(removeList);
      result.setType(isAdd ? FetchResult.FetchType.Add : FetchResult.FetchType.Remove);
      addRemoveMembersData.setValue(result);
    }
  }

  @Override
  protected void onCleared() {
    super.onCleared();
    if (!TextUtils.isEmpty(teamId)) {
      TeamRepo.removeTeamListener(teamListener);
    }
  }
}
