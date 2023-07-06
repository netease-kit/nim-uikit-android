// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.team;

import androidx.lifecycle.ViewModelProvider;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.contactkit.ui.R;
import com.netease.yunxin.kit.contactkit.ui.activity.BaseListActivity;
import com.netease.yunxin.kit.contactkit.ui.databinding.BaseListActivityLayoutBinding;
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant;

public abstract class BaseTeamListActivity extends BaseListActivity {

  protected TeamListViewModel viewModel;

  protected boolean isSelector;

  protected abstract void configViewHolderFactory();

  @Override
  protected void initView() {
    configTitle(binding);
    configViewHolderFactory();
  }

  protected void configTitle(BaseListActivityLayoutBinding binding) {
    binding.title.setTitle(R.string.my_team);
  }

  @Override
  protected void initData() {
    isSelector = getIntent().getBooleanExtra(RouterConstant.KEY_TEAM_LIST_SELECT, false);
    viewModel = new ViewModelProvider(this).get(TeamListViewModel.class);
    configRoutePath(viewModel);
    viewModel
        .getFetchResult()
        .observe(
            this,
            result -> {
              if (result.getLoadStatus() == LoadStatus.Success) {
                binding.contactListView.clearContactData();
                binding.contactListView.addContactData(result.getData());
              } else if (result.getLoadStatus() == LoadStatus.Finish) {
                if (result.getType() == FetchResult.FetchType.Add) {
                  binding.contactListView.addForwardContactData(result.getData());
                  binding.contactListView.scrollToPosition(0);
                } else if (result.getType() == FetchResult.FetchType.Remove) {
                  binding.contactListView.removeContactData(result.getData());
                }
              }
            });
  }

  @Override
  protected void onResume() {
    super.onResume();
    viewModel.fetchTeamList();
  }

  protected void configRoutePath(TeamListViewModel viewModel) {}
}
