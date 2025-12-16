// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.teamkit.ui.viewmodel;

import static com.netease.yunxin.kit.teamkit.ui.utils.TeamUIKitConstant.LIB_TAG;

import android.text.TextUtils;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import com.netease.nimlib.coexist.sdk.v2.team.model.V2NIMTeam;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.impl.TeamListenerImpl;
import com.netease.yunxin.kit.chatkit.repo.TeamRepo;
import com.netease.yunxin.kit.common.ui.viewmodel.BaseViewModel;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.corekit.coexist.im2.extend.FetchCallback;

public class TeamInfoViewModel extends BaseViewModel {

  private static final String TAG = "TeamBaseViewModel";

  protected String teamId;

  // 群信息包括主动请求获取和群信息变更
  private final MutableLiveData<FetchResult<V2NIMTeam>> teamUpdateData = new MutableLiveData<>();

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
  public void getTeamInfo(String teamId) {
    ALog.d(LIB_TAG, TAG, "getTeamInfo:" + teamId);
    TeamRepo.getTeamInfo(
        teamId,
        new FetchCallback<V2NIMTeam>() {
          @Override
          public void onSuccess(@Nullable V2NIMTeam param) {
            ALog.d(LIB_TAG, TAG, "getTeamInfo,onSuccess:" + (param == null));
            teamUpdateData.setValue(new FetchResult<>(param));
          }

          @Override
          public void onError(int errorCode, String errorMsg) {
            ALog.d(LIB_TAG, TAG, "getTeamInfo,onFailed:" + errorCode);
            teamUpdateData.setValue(new FetchResult<>(errorCode, errorMsg));
          }
        });
  }
}
