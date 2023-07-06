// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.fun.userinfo;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import com.netease.yunxin.kit.common.utils.NetworkUtils;
import com.netease.yunxin.kit.common.utils.SizeUtils;
import com.netease.yunxin.kit.contactkit.ui.R;
import com.netease.yunxin.kit.contactkit.ui.databinding.FunUserInfoActivityLayoutBinding;
import com.netease.yunxin.kit.contactkit.ui.userinfo.BaseUserInfoActivity;
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;

public class FunUserInfoActivity extends BaseUserInfoActivity {

  @Override
  protected View initViewAndGetRootView(Bundle savedInstanceState) {
    FunUserInfoActivityLayoutBinding binding =
        FunUserInfoActivityLayoutBinding.inflate(getLayoutInflater());
    contactInfoView = binding.contactUser;
    contactInfoView.configSCBlackSwitch(
        R.drawable.fun_switch_track_selector, R.drawable.fun_switch_thumb_selector);
    contactInfoView.configAvatarCorner(SizeUtils.dp2px(4));
    contactInfoView.configChatBtnColor(getResources().getColor(R.color.color_525c8c));
    contactInfoView.configDivideLineColor(getResources().getColor(R.color.color_ededed));
    contactInfoView.configRootBgColor(getResources().getColor(R.color.color_ededed));
    contactInfoView.configSignatureMaxLines(2);
    titleBar = binding.title;
    return binding.getRoot();
  }

  @Override
  protected Class<? extends Activity> getCommentActivity() {
    return FunCommentActivity.class;
  }

  protected void showDeleteConfirmDialog() {
    new FunContactBottomConfirmDialog(this)
        .configTip(
            String.format(getString(R.string.delete_contact_account), userInfoData.getName()))
        .show(
            () -> {
              if (!NetworkUtils.isConnected()) {
                Toast.makeText(this, R.string.contact_network_error_tip, Toast.LENGTH_SHORT).show();
                return;
              }
              viewModel.deleteFriend(userInfoData.data.getAccount());
              finish();
            });
  }

  protected void goChat() {
    XKitRouter.withKey(RouterConstant.PATH_FUN_CHAT_P2P_PAGE)
        .withParam(RouterConstant.CHAT_ID_KRY, userInfoData.data.getAccount())
        .withContext(FunUserInfoActivity.this)
        .navigate();
    finish();
  }
}
