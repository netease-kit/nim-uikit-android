// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.teamkit.ui.normal.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import com.netease.nimlib.sdk.team.constant.TeamMemberType;
import com.netease.nimlib.sdk.team.constant.TeamTypeEnum;
import com.netease.yunxin.kit.chatkit.model.UserInfoWithTeam;
import com.netease.yunxin.kit.corekit.im.IMKitClient;
import com.netease.yunxin.kit.corekit.im.model.UserInfo;
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import com.netease.yunxin.kit.teamkit.ui.R;
import com.netease.yunxin.kit.teamkit.ui.adapter.BaseTeamMemberListAdapter;
import com.netease.yunxin.kit.teamkit.ui.databinding.TeamMemberListItemBinding;
import com.netease.yunxin.kit.teamkit.ui.utils.ColorUtils;

public class TeamMemberListAdapter extends BaseTeamMemberListAdapter<TeamMemberListItemBinding> {

  public TeamMemberListAdapter(
      Context context, TeamTypeEnum teamTypeEnum, Class<TeamMemberListItemBinding> viewBinding) {
    super(context, teamTypeEnum, viewBinding);
  }

  @Override
  public void onBindViewHolder(
      TeamMemberListItemBinding binding,
      int position,
      UserInfoWithTeam data,
      int bingingAdapterPosition) {
    binding.tvUserName.setText(data.getName());
    if (showSelect) {
      binding.selectLayout.setVisibility(View.VISIBLE);
      if (selectData.containsKey(data.getTeamInfo().getAccount())) {
        binding.selectorCb.setChecked(true);
      } else {
        binding.selectorCb.setChecked(false);
      }
      binding.tvIdentify.setVisibility(View.GONE);
      binding
          .getRoot()
          .setOnClickListener(
              v -> {
                if (binding.selectorCb.isChecked()) {
                  binding.selectorCb.setChecked(false);
                  selectData.remove(data.getTeamInfo().getAccount());
                } else {
                  selectData.put(data.getTeamInfo().getAccount(), data);
                  binding.selectorCb.setChecked(true);
                }
                if (itemClickListener != null) {
                  itemClickListener.onActionClick(
                      binding.selectorCb.isChecked() ? ACTION_CHECK : ACTION_UNCHECK,
                      v,
                      data,
                      position);
                }
              });
    } else {
      if (needShowRemoveTag(data)) {
        binding.tvRemove.setVisibility(View.VISIBLE);
        binding.tvRemove.setOnClickListener(
            v -> {
              if (itemClickListener != null) {
                itemClickListener.onActionClick(ACTION_REMOVE, v, data, position);
              }
            });
      } else {
        binding.tvRemove.setVisibility(View.GONE);
      }

      if (showGroupIdentify
          && (data.getTeamInfo().getType() == TeamMemberType.Owner
              || data.getTeamInfo().getType() == TeamMemberType.Manager)
          && teamTypeEnum == TeamTypeEnum.Advanced) {
        binding.tvIdentify.setVisibility(View.VISIBLE);
        binding.tvIdentify.setText(
            data.getTeamInfo().getType() == TeamMemberType.Owner
                ? binding.getRoot().getContext().getText(R.string.team_owner)
                : binding.getRoot().getContext().getText(R.string.team_type_manager));
      } else {
        binding.tvIdentify.setVisibility(View.GONE);
      }
    }

    UserInfo userInfo = data.getUserInfo();
    if (userInfo != null) {
      binding.cavUserIcon.setData(
          userInfo.getAvatar(), data.getName(), ColorUtils.avatarColor(userInfo.getAccount()));
      if (!showSelect) {
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
        binding.getRoot().setOnClickListener(clickListener);
      }
    }
  }
}
