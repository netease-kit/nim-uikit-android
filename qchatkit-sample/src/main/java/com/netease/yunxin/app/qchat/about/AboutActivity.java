// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.qchat.about;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.netease.yunxin.app.qchat.R;
import com.netease.yunxin.app.qchat.databinding.ActivityAboutBinding;
import com.netease.yunxin.app.qchat.utils.AppUtils;
import com.netease.yunxin.kit.common.ui.activities.BrowseActivity;

public class AboutActivity extends AppCompatActivity {

  private ActivityAboutBinding viewBinding;
  private final String productUrl = "https://netease.im/m/";

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    viewBinding = ActivityAboutBinding.inflate(getLayoutInflater());
    setContentView(viewBinding.getRoot());
    viewBinding.flProduct.setOnClickListener(
        v -> {
          BrowseActivity.Companion.launch(
              AboutActivity.this, getString(R.string.mine_about), productUrl);
        });
    viewBinding.tvVersion.setText(AppUtils.getAppVersionName(this));
  }
}
