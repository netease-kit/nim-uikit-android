// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.teamkit.ui.fun.adapter;

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
import com.netease.yunxin.kit.teamkit.ui.adapter.BaseTeamMemberListAdapter;
import com.netease.yunxin.kit.teamkit.ui.databinding.FunTeamMemberListItemBinding;
import com.netease.yunxin.kit.teamkit.ui.utils.ColorUtils;

public class FunTeamMemberListAdapter
    extends BaseTeamMemberListAdapter<FunTeamMemberListItemBinding> {

  public FunTeamMemberListAdapter(
      Context context, TeamTypeEnum teamTypeEnum, Class<FunTeamMemberListItemBinding> viewBinding) {
    super(context, teamTypeEnum, viewBinding);
  }

  @Override
  public void onBindViewHolder(
      FunTeamMemberListItemBinding binding,
      int position,
      UserInfoWithTeam data,
      int bingingAdapterPosition) {
    binding.tvUserName.setText(data.getName());
    if (!showGroupIdentify
        && data.getTeamInfo().getType() == TeamMemberType.Owner
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
              XKitRouter.withKey(RouterConstant.PATH_FUN_USER_INFO_PAGE)
                  .withContext(v.getContext())
                  .withParam(RouterConstant.KEY_ACCOUNT_ID_KEY, userInfo.getAccount())
                  .navigate();
            }
          };
      binding.cavUserIcon.setOnClickListener(clickListener);
      binding.tvUserName.setOnClickListener(clickListener);
    }
  }
}
