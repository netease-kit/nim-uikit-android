// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.teamkit.ui.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import com.netease.nimlib.sdk.team.constant.TeamMemberType;
import com.netease.nimlib.sdk.team.constant.TeamTypeEnum;
import com.netease.yunxin.kit.corekit.im.IMKitClient;
import com.netease.yunxin.kit.corekit.im.model.UserInfo;
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import com.netease.yunxin.kit.teamkit.model.UserInfoWithTeam;
import com.netease.yunxin.kit.teamkit.ui.databinding.TeamMemberListItemBinding;
import com.netease.yunxin.kit.teamkit.ui.utils.ColorUtils;
import com.netease.yunxin.kit.teamkit.utils.FilterUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TeamMemberListAdapter
    extends TeamCommonAdapter<UserInfoWithTeam, TeamMemberListItemBinding> {
  private final TeamTypeEnum teamTypeEnum;
  private List<UserInfoWithTeam> backupTotalData;

  public TeamMemberListAdapter(
      Context context, TeamTypeEnum teamTypeEnum, Class<TeamMemberListItemBinding> viewBinding) {
    super(context, viewBinding);
    this.teamTypeEnum = teamTypeEnum;
  }

  @Override
  public void onBindViewHolder(
      TeamMemberListItemBinding binding,
      int position,
      UserInfoWithTeam data,
      int bingingAdapterPosition) {
    binding.tvUserName.setText(data.getName());
    if (data.getTeamInfo().getType() == TeamMemberType.Owner
        && teamTypeEnum == TeamTypeEnum.Advanced) {
      binding.tvIdentify.setVisibility(View.VISIBLE);
    } else {
      binding.tvIdentify.setVisibility(View.GONE);
    }
    UserInfo userInfo = data.getUserInfo();
    if (userInfo != null) {
      binding.cavUserIcon.setData(
          userInfo.getAvatar(), data.getName(), ColorUtils.avatarColor(userInfo.getAccount()));
      View.OnClickListener clickListener =
          v -> {
            if (TextUtils.equals(userInfo.getAccount(), IMKitClient.account())) {
              XKitRouter.withKey(RouterConstant.PATH_MINE_INFO_PAGE)
                  .withContext(v.getContext())
                  .navigate();
            } else {
              XKitRouter.withKey(RouterConstant.PATH_USER_INFO_PAGE)
                  .withContext(v.getContext())
                  .withParam(RouterConstant.KEY_ACCOUNT_ID_KEY, userInfo.getAccount())
                  .navigate();
            }
          };
      binding.cavUserIcon.setOnClickListener(clickListener);
      binding.tvUserName.setOnClickListener(clickListener);
    }
  }

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
  private void updateDataAndNotify(List<UserInfoWithTeam> list) {
    dataSource.clear();
    dataSource.addAll(list);
    notifyDataSetChanged();
  }
}
