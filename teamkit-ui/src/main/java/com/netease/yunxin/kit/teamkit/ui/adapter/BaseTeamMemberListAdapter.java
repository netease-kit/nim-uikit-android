// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.teamkit.ui.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import androidx.viewbinding.ViewBinding;
import com.netease.nimlib.sdk.team.constant.TeamTypeEnum;
import com.netease.yunxin.kit.chatkit.model.UserInfoWithTeam;
import com.netease.yunxin.kit.teamkit.ui.utils.FilterUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BaseTeamMemberListAdapter<R extends ViewBinding>
    extends TeamCommonAdapter<UserInfoWithTeam, R> {
  protected final TeamTypeEnum teamTypeEnum;
  protected List<UserInfoWithTeam> backupTotalData;
  protected boolean showGroupIdentify = false;

  public BaseTeamMemberListAdapter(
      Context context, TeamTypeEnum teamTypeEnum, Class<R> viewBinding) {
    super(context, viewBinding);
    this.teamTypeEnum = teamTypeEnum;
  }

  public void setGroupIdentify(boolean identify) {
    showGroupIdentify = identify;
  }

  @Override
  public void onBindViewHolder(
      R binding, int position, UserInfoWithTeam data, int bingingAdapterPosition) {}

  @Override
  public void addDataList(List<UserInfoWithTeam> data, boolean clearOld) {
    super.addDataList(data, clearOld);
    backupTotalData = new ArrayList<>(data);
  }

  public void filter(CharSequence sequence) {
    if (TextUtils.isEmpty(sequence)) {
      updateDataAndNotify(backupTotalData);
      return;
    }

    List<UserInfoWithTeam> filterResult =
        FilterUtils.filter(
            backupTotalData,
            userInfoWithTeam -> {
              boolean nameContains = userInfoWithTeam.getName().contains(sequence);
              if (nameContains) {
                userInfoWithTeam.setSearchPoint(userInfoWithTeam.getName().length());
                return true;
              }

              boolean accIdContains =
                  userInfoWithTeam.getTeamInfo().getAccount().contains(sequence);
              if (accIdContains) {
                userInfoWithTeam.setSearchPoint(
                    100 + userInfoWithTeam.getTeamInfo().getAccount().length());
                return true;
              }
              return false;
            });
    Collections.sort(
        filterResult,
        (o1, o2) -> {
          if (o1 == o2) {
            return 0;
          }
          if (o1 == null) {
            return 1;
          }
          if (o2 == null) {
            return -1;
          }
          return o1.getSearchPoint() - o2.getSearchPoint();
        });
    updateDataAndNotify(filterResult);
  }

  @SuppressLint("NotifyDataSetChanged")
  protected void updateDataAndNotify(List<UserInfoWithTeam> list) {
    dataSource.clear();
    dataSource.addAll(list);
    notifyDataSetChanged();
  }
}
