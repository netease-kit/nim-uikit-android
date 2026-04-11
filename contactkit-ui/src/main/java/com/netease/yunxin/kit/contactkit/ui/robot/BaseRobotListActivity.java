// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.robot;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.manager.UserAIBotManager;
import com.netease.yunxin.kit.common.ui.activities.BaseLocalActivity;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.common.utils.NetworkUtils;
import com.netease.yunxin.kit.contactkit.ui.R;
import com.netease.yunxin.kit.contactkit.ui.utils.RobotUtils;
import com.netease.yunxin.kit.corekit.im2.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;

/** 我的机器人列表基类，只负责通用数据逻辑。 子类在 {@link #initViews()} 中完成布局 inflate、setContentView，并给以下字段赋值。 */
public abstract class BaseRobotListActivity extends BaseLocalActivity {

  private static final String TAG = "BaseRobotListActivity";
  static final int ROBOT_MAX_COUNT = 10;

  // ---------- 子类必须在 initViews() 中赋值的 View 字段 ----------
  protected RecyclerView rvRobotList;
  protected View emptyLayout;
  protected RobotListAdapter adapter;
  // ---------------------------------------------------------------

  protected RobotListViewModel viewModel;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    initViews();
    initData();
  }

  @Override
  protected void onStart() {
    super.onStart();
    if (viewModel == null) return;
    if (!NetworkUtils.isConnected()) {
      boolean hasData =
          adapter != null && adapter.getData() != null && !adapter.getData().isEmpty();
      if (!hasData) {
        Toast.makeText(this, R.string.contact_network_error_tip, Toast.LENGTH_SHORT).show();
        showEmptyView(true);
      }
      return;
    }
    viewModel.getRobotList();
  }

  /** 子类实现：inflate 对应皮肤布局，setContentView，给 rvRobotList/emptyLayout/adapter 赋值 */
  protected abstract void initViews();

  private void initData() {
    viewModel = new ViewModelProvider(this).get(RobotListViewModel.class);
    viewModel
        .getRobotListLiveData()
        .observe(
            this,
            result -> {
              if (result.getLoadStatus() == LoadStatus.Success && result.getData() != null) {
                ALog.d(TAG, "FetchResult", "Success:" + result.getData().size());
                adapter.setData(result.getData());
                showEmptyView(result.getData().isEmpty());
              }
            });
  }

  public void showEmptyView(boolean show) {
    if (emptyLayout != null) {
      emptyLayout.setVisibility(show ? View.VISIBLE : View.GONE);
    }
    if (rvRobotList != null) {
      rvRobotList.setVisibility(show ? View.GONE : View.VISIBLE);
    }
  }

  /** 点击右上角「添加」按钮：检查上限，生成默认名称，跳转创建页 */
  protected void navigateToCreateRobot() {
    int listSize = adapter != null ? adapter.getItemCount() : 0;
    if (listSize >= ROBOT_MAX_COUNT) {
      Toast.makeText(this, R.string.contact_robot_limit_tips, Toast.LENGTH_SHORT).show();
      return;
    }
    String defaultName = RobotUtils.generateDefaultName(listSize);
    XKitRouter.withKey(getCreateRobotRouterPath())
        .withContext(this)
        .withParam(RouterConstant.KEY_ROBOT_NAME, defaultName)
        .navigate();
  }

  /** 返回创建页路由，Fun 版子类覆写 */
  protected abstract String getCreateRobotRouterPath();

  /** 返回信息页路由，Fun 版子类覆写 */
  protected abstract String getRobotInfoRouterPath();

  protected boolean checkNetwork() {
    if (!NetworkUtils.isConnected()) {
      Toast.makeText(this, R.string.contact_network_error_tip, Toast.LENGTH_SHORT).show();
      return false;
    }
    return true;
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    UserAIBotManager.updateAllBots();
  }
}
