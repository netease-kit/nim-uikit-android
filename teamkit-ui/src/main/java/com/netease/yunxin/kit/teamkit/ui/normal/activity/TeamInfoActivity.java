// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.teamkit.ui.normal.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import com.netease.yunxin.kit.teamkit.ui.activity.BaseTeamInfoActivity;
import com.netease.yunxin.kit.teamkit.ui.databinding.TeamInfoActivityBinding;

/** team info activity */
public class TeamInfoActivity extends BaseTeamInfoActivity {

  @Override
  protected View initViewAndGetRootView(Bundle savedInstanceState) {
    TeamInfoActivityBinding binding = TeamInfoActivityBinding.inflate(getLayoutInflater());
    line2 = binding.line2;
    ivBack = binding.ivBack;
    ivIcon = binding.ivIcon;
    tvTitle = binding.tvTitle;
    tvIcon = binding.tvIcon;
    tvName = binding.tvName;
    tvIntroduce = binding.tvIntroduce;
    return binding.getRoot();
  }

  protected Class<? extends Activity> getUpdateNameActivity() {
    return TeamUpdateNameActivity.class;
  }

  protected Class<? extends Activity> getUpdateIconActivity() {
    return TeamUpdateIconActivity.class;
  }

  protected Class<? extends Activity> getUpdateIntroduceActivity() {
    return TeamUpdateIntroduceActivity.class;
  }
}
