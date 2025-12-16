// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.teamkit.ui.fun.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import com.netease.nimlib.coexist.sdk.v2.team.enums.V2NIMTeamMemberRole;
import com.netease.nimlib.coexist.sdk.v2.team.enums.V2NIMTeamType;
import com.netease.yunxin.kit.chatkit.model.TeamMemberWithUserInfo;
import com.netease.yunxin.kit.corekit.coexist.im2.IMKitClient;
import com.netease.yunxin.kit.corekit.coexist.im2.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import com.netease.yunxin.kit.teamkit.ui.R;
import com.netease.yunxin.kit.teamkit.ui.adapter.BaseTeamMemberListAdapter;
import com.netease.yunxin.kit.teamkit.ui.databinding.FunTeamMemberListItemBinding;
import com.netease.yunxin.kit.teamkit.ui.utils.ColorUtils;

/**
 * 娱乐版群成员列表适配器
 *
 * @param <V> ViewBinding 成员Item布局
 */
public class FunTeamMemberListAdapter
    extends BaseTeamMemberListAdapter<FunTeamMemberListItemBinding> {

  public FunTeamMemberListAdapter(
      Context context,
      V2NIMTeamType teamTypeEnum,
      Class<FunTeamMemberListItemBinding> viewBinding) {
    super(context, teamTypeEnum, viewBinding);
  }

  @Override
  public void onBindViewHolder(
      FunTeamMemberListItemBinding binding,
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
        if (data.getMemberRole() == V2NIMTeamMemberRole.V2NIM_TEAM_MEMBER_ROLE_OWNER) {
          binding.tvIdentify.setText(binding.getRoot().getContext().getText(R.string.team_owner));
          binding.tvIdentify.setTextColor(
              binding.getRoot().getContext().getResources().getColor(R.color.color_58be6b));
          binding.tvIdentify.setBackgroundResource(R.drawable.fun_bg_item_team_owner);
        } else {
          binding.tvIdentify.setText(
              binding.getRoot().getContext().getText(R.string.team_type_manager));
          binding.tvIdentify.setTextColor(
              binding.getRoot().getContext().getResources().getColor(R.color.color_ea8339));
          binding.tvIdentify.setBackgroundResource(R.drawable.fun_bg_item_team_manager);
        }

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
              XKitRouter.withKey(RouterConstant.PATH_FUN_USER_INFO_PAGE)
                  .withContext(v.getContext())
                  .withParam(RouterConstant.KEY_ACCOUNT_ID_KEY, data.getAccountId())
                  .navigate();
            }
          };
      binding.getRoot().setOnClickListener(clickListener);
    }
  }
}
