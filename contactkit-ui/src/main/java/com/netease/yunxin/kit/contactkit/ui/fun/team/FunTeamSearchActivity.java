// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.fun.team;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.Nullable;
import com.netease.nimlib.coexist.sdk.v2.team.model.V2NIMTeam;
import com.netease.yunxin.kit.contactkit.ui.R;
import com.netease.yunxin.kit.contactkit.ui.databinding.FunTeamSearchActivityBinding;
import com.netease.yunxin.kit.contactkit.ui.team.BaseTeamSearchActivity;
import com.netease.yunxin.kit.corekit.coexist.im2.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;

public class FunTeamSearchActivity extends BaseTeamSearchActivity {

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    changeStatusBarColor(R.color.color_ededed);
  }

  @Override
  protected View initViewAndGetRootView(Bundle savedInstanceState) {
    FunTeamSearchActivityBinding viewBinding =
        FunTeamSearchActivityBinding.inflate(getLayoutInflater());
    etTeamId = viewBinding.etAccount;
    ivBack = viewBinding.ivBack;
    ivClear = viewBinding.ivClear;
    emptyLayout = viewBinding.emptyLayout;
    return viewBinding.getRoot();
  }

  @Override
  protected void startTeamProfileActivity(V2NIMTeam team) {
    if (team == null) {
      return;
    }
    XKitRouter.withKey(RouterConstant.PATH_FUN_TEAM_PROFILE_PAGE)
        .withContext(this)
        .withParam(RouterConstant.KEY_ACCOUNT_ID_KEY, team.getTeamId())
        .navigate();
  }
}
