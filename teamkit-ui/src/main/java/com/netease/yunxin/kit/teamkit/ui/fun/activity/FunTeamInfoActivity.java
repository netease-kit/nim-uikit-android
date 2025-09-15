// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.teamkit.ui.fun.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import androidx.annotation.Nullable;
import com.netease.yunxin.kit.teamkit.ui.R;
import com.netease.yunxin.kit.teamkit.ui.activity.BaseTeamInfoActivity;
import com.netease.yunxin.kit.teamkit.ui.databinding.FunTeamInfoActivityBinding;

/**
 * 娱乐版群信息页面，差异化UI展示
 *
 * <p>
 */
public class FunTeamInfoActivity extends BaseTeamInfoActivity {

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    changeStatusBarColor(R.color.color_white);
  }

  @Override
  protected View initViewAndGetRootView(Bundle savedInstanceState) {
    FunTeamInfoActivityBinding binding = FunTeamInfoActivityBinding.inflate(getLayoutInflater());
    line2 = binding.line2;
    ivBack = binding.viewTitle.getBackImageView();
    ivIcon = binding.ivIcon;
    tvTitle = binding.viewTitle.getTitleTextView();
    tvIcon = binding.tvIcon;
    tvName = binding.tvName;
    tvIntroduce = binding.tvIntroduce;
    return binding.getRoot();
  }

  protected Class<? extends Activity> getUpdateNameActivity() {
    return FunTeamUpdateNameActivity.class;
  }

  protected Class<? extends Activity> getUpdateIconActivity() {
    return FunTeamUpdateIconActivity.class;
  }

  protected Class<? extends Activity> getUpdateIntroduceActivity() {
    return FunTeamUpdateIntroduceActivity.class;
  }
}
