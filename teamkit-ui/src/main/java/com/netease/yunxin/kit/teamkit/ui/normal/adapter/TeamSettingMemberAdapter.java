// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.teamkit.ui.normal.adapter;

import android.content.Context;
import android.text.TextUtils;
import com.netease.yunxin.kit.chatkit.model.TeamMemberWithUserInfo;
import com.netease.yunxin.kit.corekit.im2.IMKitClient;
import com.netease.yunxin.kit.corekit.im2.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import com.netease.yunxin.kit.teamkit.ui.adapter.TeamCommonAdapter;
import com.netease.yunxin.kit.teamkit.ui.databinding.TeamSettingUserItemBinding;
import com.netease.yunxin.kit.teamkit.ui.utils.ColorUtils;

/**
 * 群设置成员列表适配器
 *
 * <p>
 */
public class TeamSettingMemberAdapter
    extends TeamCommonAdapter<TeamMemberWithUserInfo, TeamSettingUserItemBinding> {
  public TeamSettingMemberAdapter(Context context, Class<TeamSettingUserItemBinding> viewBinding) {
    super(context, viewBinding);
  }

  @Override
  public void onBindViewHolder(
      TeamSettingUserItemBinding binding,
      int position,
      TeamMemberWithUserInfo data,
      int bingingAdapterPosition) {
    if (data != null) {
      binding.cavUserIcon.setData(
          data.getAvatar(), data.getAvatarName(), ColorUtils.avatarColor(data.getAccountId()));
      binding.cavUserIcon.setOnClickListener(
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
          });
    }
  }
}
