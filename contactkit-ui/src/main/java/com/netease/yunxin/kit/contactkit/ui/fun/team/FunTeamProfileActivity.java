// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.fun.team;

import android.os.Bundle;
import android.view.View;
import com.netease.yunxin.kit.contactkit.ui.databinding.FunContactTeamProfileActivityBinding;
import com.netease.yunxin.kit.contactkit.ui.team.BaseTeamProfileActivity;
import com.netease.yunxin.kit.corekit.im2.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;

public class FunTeamProfileActivity extends BaseTeamProfileActivity {

  @Override
  protected View initViewAndGetRootView(Bundle savedInstanceState) {
    FunContactTeamProfileActivityBinding binding =
        FunContactTeamProfileActivityBinding.inflate(getLayoutInflater());
    titleBar = binding.title;
    ivTeamAvatar = binding.avatarView;
    tvTeamName = binding.tvName;
    tvTeamDesc = binding.tvIntroduceContent;
    tvTeamId = binding.tvAccount;
    tvTeamOwner = binding.tvCommentName;
    tvChat = binding.tvChat;
    return binding.getRoot();
  }

  protected void goChat() {
    String path = RouterConstant.PATH_FUN_CHAT_TEAM_PAGE;
    XKitRouter.withKey(path)
        .withParam(RouterConstant.CHAT_ID_KRY, teamId)
        .withContext(FunTeamProfileActivity.this)
        .navigate();
    finish();
  }
}
