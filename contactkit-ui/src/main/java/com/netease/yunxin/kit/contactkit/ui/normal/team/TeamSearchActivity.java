// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.normal.team;

import android.os.Bundle;
import android.view.View;
import com.netease.yunxin.kit.contactkit.ui.databinding.TeamSearchActivityBinding;
import com.netease.yunxin.kit.contactkit.ui.team.BaseTeamSearchActivity;

public class TeamSearchActivity extends BaseTeamSearchActivity {

  @Override
  protected View initViewAndGetRootView(Bundle savedInstanceState) {
    TeamSearchActivityBinding viewBinding = TeamSearchActivityBinding.inflate(getLayoutInflater());
    etTeamId = viewBinding.etAccount;
    ivBack = viewBinding.ivBack;
    ivClear = viewBinding.ivClear;
    emptyLayout = viewBinding.emptyLayout;
    return viewBinding.getRoot();
  }
}
