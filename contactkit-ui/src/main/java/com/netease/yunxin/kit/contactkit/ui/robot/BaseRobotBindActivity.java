// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.robot;

import static com.netease.yunxin.kit.contactkit.ui.robot.BaseRobotListActivity.ROBOT_MAX_COUNT;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.netease.yunxin.kit.chatkit.manager.UserAIBotManager;
import com.netease.yunxin.kit.common.ui.activities.BaseLocalActivity;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.common.ui.widgets.BackTitleBar;
import com.netease.yunxin.kit.common.utils.NetworkUtils;
import com.netease.yunxin.kit.contactkit.ui.ContactConstant;
import com.netease.yunxin.kit.contactkit.ui.R;
import com.netease.yunxin.kit.contactkit.ui.model.RobotInfoBean;
import com.netease.yunxin.kit.contactkit.ui.utils.RobotUtils;
import com.netease.yunxin.kit.corekit.im2.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import java.util.List;

/**
 * 绑定机器人页面基类
 *
 * <p>子类在 {@link #initViews()} 中完成布局 inflate、setContentView，并给以下字段赋值。 Base 负责所有通用数据加载和交互逻辑。
 */
public abstract class BaseRobotBindActivity extends BaseLocalActivity {

  // ---------- 子类必须在 initViews() 中赋值的 View 字段 ----------
  protected BackTitleBar titleBar;
  protected RecyclerView rvRobotList;
  protected View llEmpty;
  protected View rlyCreate;
  // ---------------------------------------------------------------

  protected RobotBindViewModel viewModel;
  protected RobotBindAdapter adapter;

  /** 记录当前正在绑定的机器人，绑定成功后跳转信息页使用 */
  private RobotInfoBean pendingBindBean;

  /**
   * 启动创建页。 创建页内部完成绑定：绑定成功则 setResult(RESULT_OK)，绑定页收到后关闭自身。 绑定失败则创建页 finish 回来，绑定页继续展示（onResume
   * 会刷新列表）。
   */
  private final ActivityResultLauncher<Intent> createLauncher =
      registerForActivityResult(
          new ActivityResultContracts.StartActivityForResult(),
          result -> {
            if (result.getResultCode() == RESULT_OK) {
              // 创建+绑定均成功，创建页已跳转信息页，绑定页关闭
              setResult(RESULT_OK);
              finish();
            }
            // 其他情况（用户取消或绑定失败）：创建页已 finish 回来，
            // onResume 会自动刷新列表，无需额外处理
          });

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    initViews();
    configTitle(titleBar);
    loadData();
    bindEvents();
  }

  @Override
  protected void onResume() {
    super.onResume();
    // 每次返回绑定页时重新拉取最新列表
    if (viewModel != null) {
      viewModel.loadRobotList();
    }
  }

  /** 子类实现：inflate 布局、setContentView，并给所有 View 字段赋值 */
  protected abstract void initViews();

  /** 子类可覆写以定制标题栏（Fun 版修改字体/背景） */
  protected void configTitle(BackTitleBar bar) {
    bar.setOnBackIconClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
  }

  private void loadData() {
    viewModel = new ViewModelProvider(this).get(RobotBindViewModel.class);

    // 观察列表数据
    viewModel
        .getRobotListLiveData()
        .observe(
            this,
            result -> {
              if (result.getLoadStatus() == LoadStatus.Success) {
                List<?> data = result.getData();
                boolean empty = data == null || data.isEmpty();
                rvRobotList.setVisibility(empty ? View.GONE : View.VISIBLE);
                llEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
                if (!empty) {
                  adapter.setData(result.getData());
                }
              } else if (result.getLoadStatus() == LoadStatus.Error) {
                Toast.makeText(this, R.string.contact_operate_error_tip, Toast.LENGTH_SHORT).show();
              }
            });

    // 观察绑定结果
    viewModel
        .getBindResultLiveData()
        .observe(
            this,
            result -> {
              if (result.getLoadStatus() == LoadStatus.Success) {
                Toast.makeText(this, R.string.contact_robot_bind_success, Toast.LENGTH_SHORT)
                    .show();
                // 绑定成功：跳转信息页，关闭绑定页
                if (pendingBindBean != null) {
                  XKitRouter.withKey(getRobotInfoRouterPath())
                      .withContext(this)
                      .withParam(RouterConstant.KEY_ACCOUNT_ID_KEY, pendingBindBean.getAccountId())
                      .navigate();
                }
                setResult(RESULT_OK);
                finish();
              } else if (result.getLoadStatus() == LoadStatus.Error) {
                int errorCode = result.getError() != null ? result.getError().getCode() : 0;
                int msgRes =
                    errorCode == ContactConstant.ERROR_QRCODE_BIND_USED
                        ? R.string.contact_robot_qrcode_bind_used
                        : R.string.contact_operate_error_tip;
                Toast.makeText(this, msgRes, Toast.LENGTH_SHORT).show();
              }
            });
  }

  private void bindEvents() {
    adapter = createAdapter();
    rvRobotList.setLayoutManager(new LinearLayoutManager(this));
    rvRobotList.setAdapter(adapter);

    String qrCodeId = getIntent().getStringExtra(RouterConstant.KEY_ROBOT_QR_CODE_ID);
    if (qrCodeId == null) qrCodeId = "";
    final String finalQrCodeId = qrCodeId;
    adapter.setOnItemClickListener(
        bean -> {
          if (!checkNetwork()) return;
          pendingBindBean = bean;
          String token = bean.getAIBot() != null ? bean.getAIBot().getToken() : "";
          viewModel.bindRobot(bean.getAccountId(), token, finalQrCodeId);
        });

    rlyCreate.setOnClickListener(v -> openCreatePage());
  }

  /** 打开创建页，超出上限则提示，子类覆写以提供对应版本的 Activity */
  protected void openCreatePage() {
    int listSize = adapter != null ? adapter.getItemCount() : 0;
    if (listSize >= ROBOT_MAX_COUNT) {
      Toast.makeText(this, R.string.contact_robot_limit_tips, Toast.LENGTH_SHORT).show();
      return;
    }
    String defaultName = RobotUtils.generateDefaultName(listSize);
    String qrCodeId = getIntent().getStringExtra(RouterConstant.KEY_ROBOT_QR_CODE_ID);
    Intent intent = new Intent(this, getCreateActivityClass());
    intent.putExtra(RouterConstant.KEY_ROBOT_NAME, defaultName);
    intent.putExtra(RouterConstant.KEY_FROM_BIND_PAGE, true);
    intent.putExtra(RouterConstant.KEY_ROBOT_QR_CODE_ID, qrCodeId != null ? qrCodeId : "");
    createLauncher.launch(intent);
  }

  /** 检查网络，无网 Toast 提示，返回 false 表示无网 */
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

  /** 创建列表 Adapter，子类可覆写以使用不同样式（如 fun 版正方形头像） */
  protected RobotBindAdapter createAdapter() {
    return new RobotBindAdapter();
  }

  /** 返回创建页 Activity Class，子类覆写 */
  protected abstract Class<?> getCreateActivityClass();

  /** 返回机器人信息页路由，子类覆写以区分 normal/fun */
  protected abstract String getRobotInfoRouterPath();
}
