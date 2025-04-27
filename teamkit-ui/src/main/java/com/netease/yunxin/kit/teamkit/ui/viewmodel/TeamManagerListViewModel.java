// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.teamkit.ui.viewmodel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import com.netease.nimlib.sdk.v2.team.enums.V2NIMTeamMemberRole;
import com.netease.nimlib.sdk.v2.team.enums.V2NIMTeamType;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.model.TeamMemberWithUserInfo;
import com.netease.yunxin.kit.chatkit.repo.TeamRepo;
import com.netease.yunxin.kit.chatkit.ui.cache.TeamUserManager;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.corekit.im2.extend.FetchCallback;
import java.util.List;

/**
 * 群管理员列表ViewModel 管理员的添加和移除功能
 *
 * <p>
 */
public class TeamManagerListViewModel extends TeamBaseViewModel {

  private static final String TAG = "TeamManagerListViewModel";
  private static final String LIB_TAG = "TeamKit-UI";

  // 获取群成员信息列表
  private final MutableLiveData<FetchResult<List<TeamMemberWithUserInfo>>> teamManagerWithUserData =
      new MutableLiveData<>();
  private final MutableLiveData<FetchResult<List<String>>> addRemoveManagerLiveData =
      new MutableLiveData<>();

  public MutableLiveData<FetchResult<List<String>>> getAddRemoveManagerLiveData() {
    return addRemoveManagerLiveData;
  }

  public MutableLiveData<FetchResult<List<TeamMemberWithUserInfo>>> getTeamManagerWithUserData() {
    return teamManagerWithUserData;
  }

  /**
   * 获取群成员列表信息 管理员业务层现在10个
   *
   * @param teamId 群ID
   */
  public void requestTeamManagers(String teamId) {
    ALog.d(LIB_TAG, TAG, "requestTeamMembers:" + teamId);
    List<TeamMemberWithUserInfo> managerList =
        TeamUserManager.getInstance()
            .getTeamMemberWithRoleListFromCache(
                teamId, V2NIMTeamMemberRole.V2NIM_TEAM_MEMBER_ROLE_MANAGER);

    FetchResult<List<TeamMemberWithUserInfo>> result =
        new FetchResult<>(LoadStatus.Success, managerList);
    teamMemberWithUserData.setValue(result);
  }
  /**
   * 添加管理员
   *
   * @param teamId 群ID
   * @param members 成员ID列表
   */
  public void addManager(String teamId, List<String> members) {
    if (members == null || members.size() == 0) {
      return;
    }
    ALog.d(LIB_TAG, TAG, "addManager:" + teamId + "," + members.size());
    TeamRepo.addManagers(
        teamId,
        V2NIMTeamType.V2NIM_TEAM_TYPE_NORMAL,
        members,
        new FetchCallback<Void>() {
          @Override
          public void onSuccess(@Nullable Void param) {
            ALog.d(LIB_TAG, TAG, "addManager,onSuccess");
            addRemoveManagerLiveData.setValue(new FetchResult<>(LoadStatus.Success, null));
          }

          @Override
          public void onError(int errorCode, @NonNull String errorMsg) {
            ALog.d(LIB_TAG, TAG, "addManager,onFailed:" + errorCode);
            addRemoveManagerLiveData.setValue(new FetchResult<>(LoadStatus.Error));
          }
        });
  }

  /**
   * 移除管理员
   *
   * @param teamId 群ID
   * @param members 成员ID列表
   */
  public void removeManager(String teamId, List<String> members) {
    if (members == null || members.size() == 0) {
      return;
    }
    ALog.d(LIB_TAG, TAG, "removeManager:" + teamId + "," + members.size());
    TeamRepo.removeManagers(
        teamId,
        V2NIMTeamType.V2NIM_TEAM_TYPE_NORMAL,
        members,
        new FetchCallback<Void>() {
          @Override
          public void onSuccess(@Nullable Void param) {
            ALog.d(LIB_TAG, TAG, "removeManager,onSuccess");
            FetchResult<List<String>> result = new FetchResult<>(LoadStatus.Success, members);
            result.setType(FetchResult.FetchType.Remove);
            addRemoveManagerLiveData.setValue(result);
          }

          @Override
          public void onError(int errorCode, @NonNull String errorMsg) {
            ALog.d(LIB_TAG, TAG, "removeManager,onFailed:" + errorCode);
            addRemoveManagerLiveData.setValue(new FetchResult<>(LoadStatus.Error));
          }
        });
  }
}
