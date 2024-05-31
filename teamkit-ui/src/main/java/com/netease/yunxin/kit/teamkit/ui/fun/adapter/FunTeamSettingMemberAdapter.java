// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.teamkit.ui.fun.adapter;

import android.content.Context;
import android.text.TextUtils;
import com.netease.yunxin.kit.chatkit.model.TeamMemberWithUserInfo;
import com.netease.yunxin.kit.corekit.im2.IMKitClient;
import com.netease.yunxin.kit.corekit.im2.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import com.netease.yunxin.kit.teamkit.ui.adapter.TeamCommonAdapter;
import com.netease.yunxin.kit.teamkit.ui.databinding.FunTeamSettingUserItemBinding;
import com.netease.yunxin.kit.teamkit.ui.utils.ColorUtils;
import java.util.List;

/**
 * 娱乐版群设置页面成员列表适配器
 *
 * @param <V> ViewBinding 成员Item布局
 */
public class FunTeamSettingMemberAdapter
    extends TeamCommonAdapter<TeamMemberWithUserInfo, FunTeamSettingUserItemBinding> {
  public FunTeamSettingMemberAdapter(
      Context context, Class<FunTeamSettingUserItemBinding> viewBinding) {
    super(context, viewBinding);
  }

  @Override
  public void onBindViewHolder(
      FunTeamSettingUserItemBinding binding,
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
              XKitRouter.withKey(RouterConstant.PATH_FUN_USER_INFO_PAGE)
                  .withContext(v.getContext())
                  .withParam(RouterConstant.KEY_ACCOUNT_ID_KEY, data.getAccountId())
                  .navigate();
            }
          });
    }
  }

  @Override
  public void removeData(List<String> accountList) {
    if (accountList == null || accountList.isEmpty()) {
      return;
    }
    for (String account : accountList) {
      for (int i = 0; i < dataSource.size(); i++) {
        if (TextUtils.equals(dataSource.get(i).getAccountId(), account)) {
          dataSource.remove(i);
          notifyItemRemoved(i);
          break;
        }
      }
    }
  }
}
