// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.teamkit.ui.viewmodel;

import static com.netease.yunxin.kit.teamkit.ui.utils.TeamUIKitConstant.KEY_EXTENSION_AT_ALL;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import com.netease.nimlib.sdk.v2.team.enums.V2NIMTeamInviteMode;
import com.netease.nimlib.sdk.v2.team.enums.V2NIMTeamUpdateInfoMode;
import com.netease.nimlib.sdk.v2.team.model.V2NIMTeam;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.repo.TeamRepo;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.corekit.im2.extend.FetchCallback;
import java.util.Objects;
import org.json.JSONObject;

/** 群管理ViewModel , 包含修改编辑权限、邀请权限、@所有人权限以及管理员管理入口 */
public class TeamManagerViewModel extends TeamBaseViewModel {

  private static final String TAG = "TeamManagerViewModels";
  private static final String LIB_TAG = "TeamKit-UI";

  // 更新邀请权限
  private final MutableLiveData<FetchResult<Integer>> updateInviteLiveData =
      new MutableLiveData<>();
  // 更新群信息权限
  private final MutableLiveData<FetchResult<Integer>> updateTeamLiveData = new MutableLiveData<>();

  // 更新@所有人权限
  private final MutableLiveData<FetchResult<String>> updateAtLiveData = new MutableLiveData<>();

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

  public void requestManagerCount(String teamId) {
    TeamRepo.getTeamManagerCount(
        teamId,
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
    TeamRepo.updateTeamInfoPrivilege(
        teamId,
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
    } catch (Exception e) {
      ALog.e(TAG, "updateAtPrivilege-putOpt", e);
    }
    String extension = obj.toString();
    TeamRepo.updateTeamExtension(
        team.getTeamId(),
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
}
