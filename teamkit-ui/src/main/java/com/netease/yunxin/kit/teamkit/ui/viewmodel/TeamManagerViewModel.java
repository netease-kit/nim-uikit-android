// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.teamkit.ui.viewmodel;

import static com.netease.yunxin.kit.chatkit.ChatConstants.KEY_EXTENSION_AT_ALL;
import static com.netease.yunxin.kit.chatkit.ChatConstants.KEY_EXTENSION_LAST_OPT_TYPE;
import static com.netease.yunxin.kit.chatkit.ChatConstants.KEY_EXTENSION_STICKY_PERMISSION;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import com.netease.nimlib.sdk.v2.team.enums.V2NIMTeamInviteMode;
import com.netease.nimlib.sdk.v2.team.enums.V2NIMTeamType;
import com.netease.nimlib.sdk.v2.team.enums.V2NIMTeamUpdateInfoMode;
import com.netease.nimlib.sdk.v2.team.model.V2NIMTeam;
import com.netease.nimlib.sdk.v2.team.model.V2NIMTeamMember;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.impl.TeamListenerImpl;
import com.netease.yunxin.kit.chatkit.repo.TeamRepo;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.corekit.im2.extend.FetchCallback;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.json.JSONObject;

/** 群管理ViewModel , 包含修改编辑权限、邀请权限、@所有人权限以及管理员管理入口 */
public class TeamManagerViewModel extends TeamBaseViewModel {

  private static final String TAG = "TeamManagerViewModel";
  private static final String LIB_TAG = "TeamKit-UI";

  // 更新邀请权限
  private final MutableLiveData<FetchResult<Integer>> updateInviteLiveData =
      new MutableLiveData<>();
  // 更新群信息权限
  private final MutableLiveData<FetchResult<Integer>> updateTeamLiveData = new MutableLiveData<>();

  // 更新@所有人权限
  private final MutableLiveData<FetchResult<String>> updateAtLiveData = new MutableLiveData<>();

  // 更新置顶权限
  private final MutableLiveData<FetchResult<String>> updateTopStickyLiveData =
      new MutableLiveData<>();

  // 群成员信息变更
  private final MutableLiveData<FetchResult<List<V2NIMTeamMember>>> memberUpdateData =
      new MutableLiveData<>();

  public MutableLiveData<FetchResult<String>> getUpdateTopStickyLiveData() {
    return updateTopStickyLiveData;
  }

  // 群管理人数
  private final MutableLiveData<FetchResult<Integer>> managerCountLiveData =
      new MutableLiveData<>();

  public MutableLiveData<FetchResult<Integer>> getUpdateInviteLiveData() {
    return updateInviteLiveData;
  }

  public MutableLiveData<FetchResult<Integer>> getUpdateTeamLiveData() {
    return updateTeamLiveData;
  }

  public MutableLiveData<FetchResult<String>> getUpdateAtLiveData() {
    return updateAtLiveData;
  }

  public MutableLiveData<FetchResult<Integer>> getManagerCountLiveData() {
    return managerCountLiveData;
  }

  public MutableLiveData<FetchResult<List<V2NIMTeamMember>>> getTeamMemberUpdateData() {
    return memberUpdateData;
  }

  @Override
  public void configTeamId(String teamId) {
    super.configTeamId(teamId);
    TeamRepo.addTeamListener(teamListener);
  }

  private TeamListenerImpl teamListener =
      new TeamListenerImpl() {
        @Override
        public void onTeamMemberInfoUpdated(@Nullable List<V2NIMTeamMember> teamMembers) {
          if (teamMembers != null && teamMembers.size() > 0) {
            ALog.d(LIB_TAG, TAG, "onTeamMemberInfoUpdated");
            List<V2NIMTeamMember> teamMemberList = new ArrayList<>();
            for (V2NIMTeamMember member : teamMembers) {
              if (member.getTeamId().equals(teamId)) {
                teamMemberList.add(member);
              }
            }
            if (teamMemberList.size() > 0) {
              memberUpdateData.setValue(new FetchResult<>(teamMemberList));
            }
          }
        }
      };

  public void requestManagerCount(String teamId) {
    TeamRepo.getTeamManagerCount(
        teamId,
        V2NIMTeamType.V2NIM_TEAM_TYPE_NORMAL,
        new FetchCallback<Integer>() {
          @Override
          public void onSuccess(@Nullable Integer data) {
            ALog.d(LIB_TAG, TAG, "getManagerCount,onSuccess:" + data);
            int count = data == null || data < 1 ? 0 : data - 1;
            managerCountLiveData.setValue(new FetchResult<>(count));
          }

          @Override
          public void onError(int code, @NonNull String msg) {
            ALog.d(LIB_TAG, TAG, "getManagerCount,onFailed:" + code);
          }
        });
  }
  /**
   * 更新添加成员权限
   *
   * @param teamId 群ID
   * @param type 权限类型
   */
  public void updateInvitePrivilege(String teamId, int type) {
    ALog.d(LIB_TAG, TAG, "updateInvitePrivilege:" + teamId + "," + type);
    TeamRepo.updateInviteMode(
        teamId,
        V2NIMTeamType.V2NIM_TEAM_TYPE_NORMAL,
        V2NIMTeamInviteMode.typeOfValue(type),
        new FetchCallback<Void>() {
          @Override
          public void onSuccess(@Nullable Void param) {
            ALog.d(LIB_TAG, TAG, "updateInvitePrivilege,onSuccess");
            updateInviteLiveData.setValue(new FetchResult<>(type));
          }

          @Override
          public void onError(int errorCode, @NonNull String errorMsg) {
            ALog.d(LIB_TAG, TAG, "updateInvitePrivilege,onFailed:" + errorCode);
            updateInviteLiveData.setValue(new FetchResult<>(errorCode, errorMsg));
          }
        });
  }

  /**
   * 更新群信息权限
   *
   * @param teamId 群ID
   * @param type 权限类型
   */
  public void updateInfoPrivilege(String teamId, int type) {
    ALog.d(LIB_TAG, TAG, "updateInfoPrivilege:" + teamId + "," + type);
    TeamRepo.updateTeamInfoModel(
        teamId,
        V2NIMTeamType.V2NIM_TEAM_TYPE_NORMAL,
        V2NIMTeamUpdateInfoMode.typeOfValue(type),
        new FetchCallback<Void>() {
          @Override
          public void onSuccess(@Nullable Void param) {
            ALog.d(LIB_TAG, TAG, "updateInfoPrivilege,onSuccess");
            updateTeamLiveData.setValue(new FetchResult<>(type));
          }

          @Override
          public void onError(int errorCode, @NonNull String errorMsg) {
            ALog.d(LIB_TAG, TAG, "updateInfoPrivilege,onFailed:" + errorCode);
            updateTeamLiveData.setValue(new FetchResult<>(errorCode, errorMsg));
          }
        });
  }

  /**
   * 更新@所有人权限，需要在群扩展中添加KEY_EXTENSION_AT_ALL字段实现
   *
   * @param team 群信息
   * @param type 权限类型
   */
  public void updateAtPrivilege(V2NIMTeam team, String type) {
    if (team == null) {
      return;
    }
    ALog.d(LIB_TAG, TAG, "updateAtPrivilege:" + team + "," + type);
    JSONObject obj;
    try {
      obj = new JSONObject(team.getServerExtension());
    } catch (Exception e) {
      ALog.e(TAG, "updateAtPrivilege-parseExtension", e);
      obj = new JSONObject();
    }
    if (Objects.equals(obj.optString(KEY_EXTENSION_AT_ALL), type)) {
      return;
    }
    try {
      obj.putOpt(KEY_EXTENSION_AT_ALL, type);
      obj.put(KEY_EXTENSION_LAST_OPT_TYPE, KEY_EXTENSION_AT_ALL);
    } catch (Exception e) {
      ALog.e(TAG, "updateAtPrivilege-putOpt", e);
    }
    String extension = obj.toString();
    TeamRepo.updateTeamExtension(
        team.getTeamId(),
        V2NIMTeamType.V2NIM_TEAM_TYPE_NORMAL,
        extension,
        new FetchCallback<Void>() {
          @Override
          public void onSuccess(@Nullable Void param) {
            ALog.d(LIB_TAG, TAG, "updateAtPrivilege,onSuccess");
            updateAtLiveData.setValue(new FetchResult<>(type));
          }

          @Override
          public void onError(int errorCode, @NonNull String errorMsg) {
            ALog.d(LIB_TAG, TAG, "updateAtPrivilege,onFailed:" + errorCode);
            updateAtLiveData.setValue(new FetchResult<>(errorCode, errorMsg));
          }
        });
  }

  /**
   * 更新置顶权限，需要在群扩展中添加KEY_EXTENSION_STICKY_PERMISSION字段实现
   *
   * @param team 群信息
   * @param type 权限类型
   */
  public void updateTopStickyPrivilege(V2NIMTeam team, String type) {
    if (team == null) {
      return;
    }
    ALog.d(LIB_TAG, TAG, "updateTopStickyPrivilege:" + team + "," + type);
    JSONObject obj;
    try {
      obj = new JSONObject(team.getServerExtension());
    } catch (Exception e) {
      ALog.e(TAG, "updateTopStickyPrivilege-parseExtension", e);
      obj = new JSONObject();
    }
    if (Objects.equals(obj.optString(KEY_EXTENSION_STICKY_PERMISSION), type)) {
      return;
    }
    try {
      obj.putOpt(KEY_EXTENSION_STICKY_PERMISSION, type);
      obj.put(KEY_EXTENSION_LAST_OPT_TYPE, KEY_EXTENSION_STICKY_PERMISSION);
    } catch (Exception e) {
      ALog.e(TAG, "updateTopStickyPrivilege-putOpt", e);
    }
    String extension = obj.toString();
    TeamRepo.updateTeamExtension(
        team.getTeamId(),
        V2NIMTeamType.V2NIM_TEAM_TYPE_NORMAL,
        extension,
        new FetchCallback<>() {
          @Override
          public void onSuccess(@Nullable Void param) {
            ALog.d(LIB_TAG, TAG, "updateTopStickyPrivilege,onSuccess");
            updateTopStickyLiveData.setValue(new FetchResult<>(type));
          }

          @Override
          public void onError(int errorCode, String errorMsg) {
            ALog.d(LIB_TAG, TAG, "updateTopStickyPrivilege,onFailed:" + errorCode);
            updateTopStickyLiveData.setValue(new FetchResult<>(errorCode, errorMsg));
          }
        });
  }

  @Override
  protected void onCleared() {
    super.onCleared();
    TeamRepo.removeTeamListener(teamListener);
  }
}
