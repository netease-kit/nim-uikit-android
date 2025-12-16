// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.selector.ai;

import static com.netease.yunxin.kit.contactkit.ui.ContactUIConfig.DEFAULT_SESSION_MAX_SELECT_COUNT;
import static com.netease.yunxin.kit.corekit.coexist.im2.utils.RouterConstant.KEY_FORWARD_SELECTED_CONVERSATIONS;
import static com.netease.yunxin.kit.corekit.coexist.im2.utils.RouterConstant.KEY_REQUEST_SELECTOR_NAME;
import static com.netease.yunxin.kit.corekit.coexist.im2.utils.RouterConstant.REQUEST_CONTACT_SELECTOR_KEY;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.netease.yunxin.kit.common.ui.activities.BaseLocalActivity;
import com.netease.yunxin.kit.common.ui.adapter.BaseFragmentAdapter;
import com.netease.yunxin.kit.common.ui.widgets.BackTitleBar;
import com.netease.yunxin.kit.common.utils.NetworkUtils;
import com.netease.yunxin.kit.contactkit.ui.R;
import com.netease.yunxin.kit.contactkit.ui.model.SelectedViewBean;
import com.netease.yunxin.kit.contactkit.ui.selector.BaseSelectedSelectorAdapter;
import com.netease.yunxin.kit.corekit.coexist.im2.utils.RouterConstant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class BaseAIContactSelectorActivity extends BaseLocalActivity {

  private static final String TAG = "BaseForwardSelectorActivity";
  public static final int DEFAULT_MAX_SELECT_COUNT = 200;

  protected BackTitleBar titleBar;

  protected View rootView;

  protected ViewPager2 viewPager;

  //展示已选中的会话
  protected RecyclerView rvSelected;

  //展示已选中的会话布局
  protected View selectedLayout;

  protected TabLayout tabLayout;

  //已选分割线
  protected View selectedDivider;
  protected TabLayout.Tab tabFriend;

  protected TabLayout.Tab tabAIUser;

  protected TabLayoutMediator mediator;

  protected List<Fragment> fragments = new ArrayList<>();
  protected int maxSelectCount = DEFAULT_MAX_SELECT_COUNT;

  protected boolean selectFinalCheckCountEnable = false;

  protected boolean checkNetworkEnable = true;

  protected boolean enableReturnName;

  protected ArrayList<String> filterUser;

  // 是否多选
  protected boolean isMultiSelect = true;

  protected AIUserSelectorViewModel viewModel;
  protected BaseSelectedSelectorAdapter selectedAdapter;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    rootView = initViewAndGetRootView(savedInstanceState);
    checkViews();
    setContentView(rootView);
    initView();
    initData();
  }

  protected void checkViews() {
    Objects.requireNonNull(rootView);
    Objects.requireNonNull(titleBar);
    Objects.requireNonNull(viewPager);
    Objects.requireNonNull(tabLayout);
    Objects.requireNonNull(rvSelected);
    Objects.requireNonNull(selectedLayout);
    Objects.requireNonNull(selectedDivider);
  }

  protected abstract View initViewAndGetRootView(Bundle savedInstanceState);

  protected abstract void initFragments();

  @SuppressLint("ClickableViewAccessibility")
  protected void initView() {
    tabFriend = tabLayout.newTab();
    tabAIUser = tabLayout.newTab();
    //后添加的显示在前面
    tabLayout.addTab(tabAIUser);
    tabLayout.addTab(tabFriend);

    initFragments();

    BaseFragmentAdapter fragmentAdapter = new BaseFragmentAdapter(this);
    fragmentAdapter.setFragmentList(fragments);
    viewPager.setAdapter(fragmentAdapter);

    mediator = new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {});

    mediator.attach();
    tabFriend.setText(getString(R.string.my_friend));
    tabAIUser.setText(getString(R.string.ai_user_title));

    //设置适配器
    setSelectorAdapter();
    //已经选择
    rvSelected.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
    rvSelected.setAdapter(selectedAdapter);
    selectedAdapter.setOnItemClickListener(
        new BaseSelectedSelectorAdapter.OnItemClickListener() {
          @Override
          public void onItemClick(SelectedViewBean bean) {
            viewModel.removeSelectedItem(bean);
          }
        });
    configTitle();
  };

  protected void configTitle() {
    titleBar
        .setLeftText(R.string.cancel)
        .setOnBackIconClickListener(v -> onBackPressed())
        .setTitle(R.string.select);
    if (isMultiSelect) {
      setTitleBarMultiSelectActionUI();
      titleBar
          .setActionEnable(false)
          .setActionListener(
              v -> {
                if (!NetworkUtils.isConnected()) {
                  Toast.makeText(this, R.string.contact_network_error_tip, Toast.LENGTH_SHORT)
                      .show();
                  return;
                }
                ArrayList<String> select = viewModel.getSelectedId();
                if (select.size() < 1) {
                  Toast.makeText(this, getString(R.string.select_empty_tips), Toast.LENGTH_SHORT)
                      .show();
                  return;
                }
                if (select.size() > maxSelectCount) {
                  Toast.makeText(
                          this,
                          getString(
                              R.string.contact_selector_session_max_count,
                              String.valueOf(DEFAULT_SESSION_MAX_SELECT_COUNT)),
                          Toast.LENGTH_LONG)
                      .show();
                  return;
                }
                Intent result = new Intent();
                result.putExtra(REQUEST_CONTACT_SELECTOR_KEY, select);
                if (enableReturnName) {
                  result.putExtra(KEY_REQUEST_SELECTOR_NAME, viewModel.getSelectedName());
                }
                setResult(RESULT_OK, result);
                finish();
              });

    } else {
      titleBar
          .setActionText(R.string.contact_selector_multi_select)
          .setActionListener(
              v -> {
                isMultiSelect = true;
                viewModel.setMultiSelectMode(true);
                configTitle();
              });
    }
  }

  /** 设置多选模式下的标题栏UI */
  protected void setTitleBarMultiSelectActionUI() {
    titleBar.setActionText(getString(R.string.selector_sure_without_num));
  }

  protected abstract void setSelectorAdapter();

  private void initData() {
    filterUser = getIntent().getStringArrayListExtra(RouterConstant.SELECTOR_CONTACT_FILTER_KEY);
    selectFinalCheckCountEnable =
        getIntent()
            .getBooleanExtra(RouterConstant.KEY_CONTACT_SELECTOR_FINAL_CHECK_COUNT_ENABLE, false);
    checkNetworkEnable =
        getIntent().getBooleanExtra(RouterConstant.KEY_CONTACT_SELECTOR_CHECK_NETWORK_ENABLE, true);
    maxSelectCount =
        getIntent()
            .getIntExtra(RouterConstant.KEY_CONTACT_SELECTOR_MAX_COUNT, DEFAULT_MAX_SELECT_COUNT);
    enableReturnName =
        getIntent().getBooleanExtra(RouterConstant.KEY_REQUEST_SELECTOR_NAME_ENABLE, false);
    viewModel = new ViewModelProvider(this).get(AIUserSelectorViewModel.class);
    viewModel.setMultiSelectMode(isMultiSelect);
    viewModel.setMaxSelectorCount(maxSelectCount);
    viewModel
        .getSelectedLiveData()
        .observe(
            this,
            selected -> {
              if (!isMultiSelect && !selected.isEmpty()) {
                //单选直接返回
                selectedLayout.setVisibility(View.GONE);
                ArrayList<String> select = viewModel.getSelectedId();
                Intent result = new Intent();
                result.putExtra(KEY_FORWARD_SELECTED_CONVERSATIONS, select);
                setResult(RESULT_OK, result);
                finish();
              } else if (selected.isEmpty()) {

                selectedLayout.setVisibility(View.GONE);
                selectedDivider.setVisibility(View.GONE);
              } else {
                selectedLayout.setVisibility(View.VISIBLE);
                // update selected adapter
                if (selectedAdapter != null) {
                  selectedAdapter.setData(selected);
                }
              }
              setTitleBarActionNumber(selected.size());
            });
    viewModel
        .getIsMultiSelectModeLiveData()
        .observe(
            this,
            aBoolean -> {
              isMultiSelect = aBoolean;
            });
  }

  /**
   * 设置标题栏右侧数字
   *
   * @param number 数字
   */
  protected abstract void setTitleBarActionNumber(int number);
}
