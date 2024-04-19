// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.teamkit.ui.normal.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import com.netease.nimlib.sdk.v2.team.enums.V2NIMTeamMemberRole;
import com.netease.nimlib.sdk.v2.team.enums.V2NIMTeamType;
import com.netease.yunxin.kit.chatkit.model.TeamMemberWithUserInfo;
import com.netease.yunxin.kit.corekit.im2.IMKitClient;
import com.netease.yunxin.kit.corekit.im2.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import com.netease.yunxin.kit.teamkit.ui.R;
import com.netease.yunxin.kit.teamkit.ui.adapter.BaseTeamMemberListAdapter;
import com.netease.yunxin.kit.teamkit.ui.databinding.TeamMemberListItemBinding;
import com.netease.yunxin.kit.teamkit.ui.utils.ColorUtils;

/**
 * 群成员列表适配器,差异化UI展示
 *
 * <p>
 */
public class TeamMemberListAdapter extends BaseTeamMemberListAdapter<TeamMemberListItemBinding> {

  public TeamMemberListAdapter(
      Context context, V2NIMTeamType teamTypeEnum, Class<TeamMemberListItemBinding> viewBinding) {
    super(context, teamTypeEnum, viewBinding);
  }

  @Override
  public void onBindViewHolder(
      TeamMemberListItemBinding binding,
      int position,
      TeamMemberWithUserInfo data,
      int bingingAdapterPosition) {

    if (data == null) {
      return;
    }
    binding.tvUserName.setText(data.getName());
    if (showSelect) {
      binding.selectLayout.setVisibility(View.VISIBLE);
      binding.selectorCb.setChecked(selectData.containsKey(data.getAccountId()));
      binding.tvIdentify.setVisibility(View.GONE);
      binding
          .getRoot()
          .setOnClickListener(
              v -> {
                if (binding.selectorCb.isChecked()) {
                  binding.selectorCb.setChecked(false);
                  selectData.remove(data.getAccountId());
                } else {
                  selectData.put(data.getAccountId(), data);
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
          && (data.getMemberRole() == V2NIMTeamMemberRole.V2NIM_TEAM_MEMBER_ROLE_OWNER
              || data.getMemberRole() == V2NIMTeamMemberRole.V2NIM_TEAM_MEMBER_ROLE_MANAGER)
          && teamTypeEnum == V2NIMTeamType.V2NIM_TEAM_TYPE_NORMAL) {
        binding.tvIdentify.setVisibility(View.VISIBLE);
        binding.tvIdentify.setText(
            data.getMemberRole() == V2NIMTeamMemberRole.V2NIM_TEAM_MEMBER_ROLE_OWNER
                ? binding.getRoot().getContext().getText(R.string.team_owner)
                : binding.getRoot().getContext().getText(R.string.team_type_manager));
      } else {
        binding.tvIdentify.setVisibility(View.GONE);
      }
    }

    binding.cavUserIcon.setData(
        data.getAvatar(), data.getAvatarName(), ColorUtils.avatarColor(data.getAccountId()));
    if (!showSelect) {
      View.OnClickListener clickListener =
          v -> {
            if (TextUtils.equals(data.getAccountId(), IMKitClient.account())) {
              XKitRouter.withKey(RouterConstant.PATH_MINE_INFO_PAGE)
                  .withContext(v.getContext())
                  .navigate();
            } else {
              XKitRouter.withKey(RouterConstant.PATH_USER_INFO_PAGE)
                  .withContext(v.getContext())
                  .withParam(RouterConstant.KEY_ACCOUNT_ID_KEY, data.getAccountId())
                  .navigate();
            }
          };
      binding.getRoot().setOnClickListener(clickListener);
    }
  }
}
