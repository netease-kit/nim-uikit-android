// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.teamkit.ui.normal.adapter;

import android.content.Context;
import android.text.TextUtils;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;
import com.netease.yunxin.kit.chatkit.model.UserInfoWithTeam;
import com.netease.yunxin.kit.corekit.im.IMKitClient;
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import com.netease.yunxin.kit.teamkit.ui.adapter.TeamCommonAdapter;
import com.netease.yunxin.kit.teamkit.ui.databinding.TeamSettingUserItemBinding;
import com.netease.yunxin.kit.teamkit.ui.utils.ColorUtils;

public class TeamSettingMemberAdapter
    extends TeamCommonAdapter<UserInfoWithTeam, TeamSettingUserItemBinding> {
  public TeamSettingMemberAdapter(Context context, Class<TeamSettingUserItemBinding> viewBinding) {
    super(context, viewBinding);
  }

  @Override
  public void onBindViewHolder(
      TeamSettingUserItemBinding binding,
      int position,
      UserInfoWithTeam data,
      int bingingAdapterPosition) {
    NimUserInfo userInfo = data.getUserInfo();
    if (userInfo != null) {
      binding.cavUserIcon.setData(
          userInfo.getAvatar(),
          data.getName(),
          ColorUtils.avatarColor(data.getUserInfo().getAccount()));
      binding.cavUserIcon.setOnClickListener(
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
          });
    }
  }
}
