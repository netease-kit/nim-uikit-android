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
import com.netease.yunxin.kit.corekit.coexist.im2.utils.RouterConstant;

/**
 * 群组列表基类
 *
 * <p>
 */
public abstract class BaseTeamListActivity extends BaseListActivity {

  protected TeamListViewModel viewModel;

  protected boolean isSelector;

  protected abstract void configViewHolderFactory();

  // 初始化视图
  @Override
  protected void initView() {
    configTitle(binding);
    configViewHolderFactory();
  }

  // 配置标题
  protected void configTitle(BaseListActivityLayoutBinding binding) {
    binding.title.setTitle(R.string.my_team);
  }

  @Override
  protected void initData() {
    // 是否是选择模式
    isSelector = getIntent().getBooleanExtra(RouterConstant.KEY_TEAM_LIST_SELECT, false);
    // 获取群组列表
    viewModel = new ViewModelProvider(this).get(TeamListViewModel.class);
    // 配置群组点击跳转路由路径
    configRoutePath(viewModel);
    viewModel
        .getFetchResult()
        .observe(
            this,
            result -> {
              if (result.getLoadStatus() == LoadStatus.Success) {
                // 新加入群组
                if (result.getType() == FetchResult.FetchType.Add) {
                  binding.contactListView.addForwardContactData(result.getData());
                  binding.contactListView.scrollToPosition(0);
                } else if (result.getType() == FetchResult.FetchType.Remove) {
                  // 退出群组
                  binding.contactListView.removeContactData(result.getData());
                } else {
                  // 查询群组列表
                  binding.contactListView.clearContactData();
                  binding.contactListView.addContactData(result.getData());
                }
              }
            });
    // 获取群组列表
    viewModel.getTeamList();
  }

  protected void configRoutePath(TeamListViewModel viewModel) {}
}
