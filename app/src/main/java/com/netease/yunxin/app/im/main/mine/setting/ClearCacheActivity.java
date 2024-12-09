// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.im.main.mine.setting;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import com.netease.yunxin.app.im.R;
import com.netease.yunxin.app.im.databinding.ActivityClearCacheBinding;
import com.netease.yunxin.app.im.utils.DataUtils;
import com.netease.yunxin.kit.common.ui.activities.BaseLocalActivity;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;

public class ClearCacheActivity extends BaseLocalActivity {

  private ActivityClearCacheBinding viewBinding;
  private ClearCacheViewModel viewModel;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    changeStatusBarColor(R.color.color_e9eff5);
    viewBinding = ActivityClearCacheBinding.inflate(getLayoutInflater());
    viewModel = new ViewModelProvider(this).get(ClearCacheViewModel.class);
    setContentView(viewBinding.getRoot());
    initView();
  }

  @Override
  protected void onResume() {
    super.onResume();
    viewModel.getSdkCacheSize();
  }

  private void initView() {
    viewBinding.clearSdkFl.setOnClickListener(
        v -> {
          viewModel.clearSDKCache();
          viewBinding.clearSdkSizeTv.setText(getString(R.string.cache_size_null_text));
        });
    viewBinding.clearMessageFl.setOnClickListener(
        v -> {
          viewModel.clearMessageCache();
          ToastX.showShortToast(R.string.clear_message_tips);
        });
    viewModel
        .getSdkCacheLiveData()
        .observe(
            this,
            result -> {
              if (result.getLoadStatus() == LoadStatus.Success) {
                long size = result.getData() != null ? result.getData() : 0;
                String text =
                    String.format(getString(R.string.cache_size_text), DataUtils.getSizeToM(size));
                viewBinding.clearSdkSizeTv.setText(text);
              }
            });
    viewBinding.settingTitleBar.setOnBackIconClickListener(v -> onBackPressed());
  }
}
