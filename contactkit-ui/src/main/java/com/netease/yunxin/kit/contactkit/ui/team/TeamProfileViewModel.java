// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.team;

import static com.netease.yunxin.kit.contactkit.ui.ContactConstant.LIB_TAG;

import android.text.TextUtils;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import com.netease.nimlib.coexist.sdk.v2.team.V2NIMTeamListener;
import com.netease.nimlib.coexist.sdk.v2.team.enums.V2NIMTeamType;
import com.netease.nimlib.coexist.sdk.v2.team.model.V2NIMTeam;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.impl.TeamListenerImpl;
import com.netease.yunxin.kit.chatkit.model.TeamMemberWithUserInfo;
import com.netease.yunxin.kit.chatkit.repo.TeamRepo;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import com.netease.yunxin.kit.common.ui.viewmodel.BaseViewModel;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.contactkit.ui.R;
import com.netease.yunxin.kit.contactkit.ui.utils.ContactUtils;
import com.netease.yunxin.kit.corekit.coexist.im2.extend.FetchCallback;
import java.util.ArrayList;
import java.util.List;

public class TeamProfileViewModel extends BaseViewModel {
  private final String TAG = "UserInfoViewModel";

  private final MutableLiveData<FetchResult<V2NIMTeam>> teamLiveData = new MutableLiveData<>();

  private final MutableLiveData<FetchResult<TeamMemberWithUserInfo>> teamOwnerLiveData =
      new MutableLiveData<>();

  private final MutableLiveData<FetchResult<String>> toTeamChat = new MutableLiveData<>();

  private String teamId;

  public TeamProfileViewModel() {
    TeamRepo.addTeamListener(teamListener);
  }

  public void init(String id) {
    teamId = id;
  }

  public MutableLiveData<FetchResult<V2NIMTeam>> getTeamLiveData() {
    return teamLiveData;
  }

  public MutableLiveData<FetchResult<String>> getToTeamChatLiveData() {
    return toTeamChat;
  }

  public MutableLiveData<FetchResult<TeamMemberWithUserInfo>> getTeamOwnerLiveData() {
    return teamOwnerLiveData;
  }

  private V2NIMTeamListener teamListener =
      new TeamListenerImpl() {
        @Override
        public void onTeamJoined(@Nullable V2NIMTeam team) {
          if (team != null && TextUtils.equals(team.getTeamId(), teamId)) {
            FetchResult<V2NIMTeam> fetchResult = new FetchResult<>(LoadStatus.Success);
            fetchResult.setData(team);
            teamLiveData.setValue(fetchResult);
          }
        }
      };

  public void getTeamInfoAndTeamOwner() {
    ALog.d(LIB_TAG, TAG, "getTeamInfoAndTeamOwner:" + teamId);
    if (TextUtils.isEmpty(teamId)) {
      return;
    }
    TeamRepo.getTeamInfo(
        teamId,
        new FetchCallback<V2NIMTeam>() {
          @Override
          public void onError(int errorCode, @Nullable String errorMsg) {
            ALog.d(LIB_TAG, TAG, "getTeamInfo,onError:" + errorCode + "," + errorMsg);
          }

          @Override
          public void onSuccess(@Nullable V2NIMTeam data) {
            ALog.d(
                LIB_TAG,
                TAG,
                "getTeamInfo,onSuccess:" + (data == null ? "null" : data.getTeamId()));
            if (data != null) {
              FetchResult<V2NIMTeam> fetchResult = new FetchResult<>(LoadStatus.Success);
              fetchResult.setData(data);
              teamLiveData.setValue(fetchResult);
              getTeamOwner(data.getOwnerAccountId());
            }
          }
        });
  }

  // 从云端更新用户信息，保证用户信息最新
  public void getTeamOwner(String teamOwner) {
    ALog.d(LIB_TAG, TAG, "getTeamOwner:" + teamOwner);
    if (TextUtils.isEmpty(teamOwner)) {
      return;
    }
    List<String> accountList = new ArrayList<>();
    accountList.add(teamOwner);
    TeamRepo.getTeamMemberListWithUserInfoByIds(
        teamId,
        V2NIMTeamType.V2NIM_TEAM_TYPE_NORMAL,
        accountList,
        new FetchCallback<List<TeamMemberWithUserInfo>>() {

          @Override
          public void onSuccess(@Nullable List<TeamMemberWithUserInfo> data) {
            if (data != null && !data.isEmpty()) {
              FetchResult<TeamMemberWithUserInfo> fetchResult =
                  new FetchResult<>(LoadStatus.Success);
              fetchResult.setData(data.get(0));
              teamOwnerLiveData.setValue(fetchResult);
            }
          }

          @Override
          public void onError(int errorCode, @Nullable String errorMsg) {
            ALog.e(
                LIB_TAG,
                TAG,
                "getTeamMemberListWithUserInfoByIds,onError:" + errorCode + "," + errorMsg);
          }
        });
  }

  public void applyJoinTeam() {
    TeamRepo.applyJoinTeam(
        teamId,
        V2NIMTeamType.V2NIM_TEAM_TYPE_NORMAL,
        null,
        new FetchCallback<V2NIMTeam>() {

          @Override
          public void onSuccess(@Nullable V2NIMTeam data) {

            if (data != null) {
              FetchResult<V2NIMTeam> fetchResult = new FetchResult<>(LoadStatus.Success);
              fetchResult.setData(data);
              teamLiveData.setValue(fetchResult);

              if (data.isValidTeam()) {
                FetchResult<String> chatResult = new FetchResult<>(LoadStatus.Success);
                chatResult.setData(data.getTeamId());
                toTeamChat.setValue(chatResult);
              } else {
                ToastX.showShortToast(R.string.team_apply_join_tip);
              }
            }
          }

          @Override
          public void onError(int errorCode, @Nullable String errorMsg) {
            ALog.e(LIB_TAG, TAG, "applyJoinTeam,onError:" + errorCode + "," + errorMsg);
            int errorMsgRes = ContactUtils.getErrorCodeAndToast(errorCode);
            if (errorMsgRes == R.string.contact_operate_error_tip) {
              errorMsgRes = R.string.team_apply_join_error_tip;
            }
            ToastX.showShortToast(errorMsgRes);
          }
        });
  }

  @Override
  protected void onCleared() {
    super.onCleared();
    TeamRepo.removeTeamListener(teamListener);
  }
}
