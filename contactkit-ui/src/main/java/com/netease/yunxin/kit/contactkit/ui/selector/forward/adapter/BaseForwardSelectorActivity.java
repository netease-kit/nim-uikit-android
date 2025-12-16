// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.selector.forward.adapter;

import static com.netease.yunxin.kit.contactkit.ui.ContactUIConfig.DEFAULT_SESSION_MAX_SELECT_COUNT;
import static com.netease.yunxin.kit.corekit.coexist.im2.utils.RouterConstant.KEY_FORWARD_SELECTED_CONVERSATIONS;
import static com.netease.yunxin.kit.corekit.coexist.im2.utils.RouterConstant.KEY_FORWARD_SELECTOR_MODE;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.IMKitConfigCenter;
import com.netease.yunxin.kit.chatkit.model.RecentForward;
import com.netease.yunxin.kit.common.ui.activities.BaseLocalActivity;
import com.netease.yunxin.kit.common.ui.adapter.BaseFragmentAdapter;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.common.ui.widgets.BackTitleBar;
import com.netease.yunxin.kit.common.utils.NetworkUtils;
import com.netease.yunxin.kit.contactkit.ui.R;
import com.netease.yunxin.kit.contactkit.ui.model.SelectableBean;
import com.netease.yunxin.kit.contactkit.ui.selector.BaseSelectedSelectorAdapter;
import com.netease.yunxin.kit.contactkit.ui.selector.SelectableListener;
import com.netease.yunxin.kit.contactkit.ui.selector.forward.ContactSelectorViewModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class BaseForwardSelectorActivity extends BaseLocalActivity {

  private static final String TAG = "BaseForwardSelectorActivity";

  protected BackTitleBar titleBar;

  protected View rootView;

  protected ViewPager2 viewPager;

  //搜索框
  protected EditText searchEditText;

  //清除搜索框
  protected ImageView ivSearchClear;

  //最近转发列表
  protected RecyclerView recentForwardRecyclerView;

  //控制是否显示最近转发模块
  protected View recentForwardLayout;

  //展示已选中的会话
  protected RecyclerView rvSelected;

  //展示已选中的会话布局
  protected View selectedLayout;

  //查看已选中的会话详情
  protected ImageView ivSelectedDetail;

  protected TabLayout tabLayout;

  //已选分割线
  protected View selectedDivider;

  TabLayout.Tab tabConversation;
  TabLayout.Tab tabFriend;

  TabLayout.Tab tabTeam;

  TabLayoutMediator mediator;

  protected List<Fragment> fragments = new ArrayList<>();

  // 是否多选
  protected boolean isMultiSelect = false;

  protected ContactSelectorViewModel viewModel;

  protected BaseRecentForwardSelectorAdapter recentForwardSelectorAdapter;

  protected BaseSelectedSelectorAdapter selectedAdapter;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    rootView = initViewAndGetRootView(savedInstanceState);
    checkViews();
    setContentView(rootView);
    int selectModel = getIntent().getIntExtra(KEY_FORWARD_SELECTOR_MODE, 0);
    isMultiSelect = selectModel == 1;
    initView();
    initData();
  }

  protected void checkViews() {
    Objects.requireNonNull(rootView);
    Objects.requireNonNull(titleBar);
    Objects.requireNonNull(viewPager);
    Objects.requireNonNull(searchEditText);
    Objects.requireNonNull(recentForwardRecyclerView);
    Objects.requireNonNull(tabLayout);
    Objects.requireNonNull(recentForwardLayout);
    Objects.requireNonNull(rvSelected);
    Objects.requireNonNull(selectedLayout);
    Objects.requireNonNull(ivSelectedDetail);
    Objects.requireNonNull(selectedDivider);
    Objects.requireNonNull(ivSearchClear);
  }

  protected abstract View initViewAndGetRootView(Bundle savedInstanceState);

  protected abstract void initFragments();

  protected void initView() {
    tabConversation = tabLayout.newTab();
    tabFriend = tabLayout.newTab();
    if (IMKitConfigCenter.getEnableTeam()) {
      tabTeam = tabLayout.newTab();
      tabLayout.addTab(tabTeam);
    }
    //后添加的显示在前面
    tabLayout.addTab(tabFriend);
    tabLayout.addTab(tabConversation);

    initFragments();

    BaseFragmentAdapter fragmentAdapter = new BaseFragmentAdapter(this);
    fragmentAdapter.setFragmentList(fragments);
    viewPager.setAdapter(fragmentAdapter);

    mediator = new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {});

    mediator.attach();
    //tab 选中的时候再去搜索
    tabLayout.addOnTabSelectedListener(
        new TabLayout.OnTabSelectedListener() {
          @Override
          public void onTabSelected(TabLayout.Tab tab) {
            ALog.d(TAG, "onTabSelected" + tab.getPosition());
            if (viewModel != null) {
              switch (tab.getPosition()) {
                case 0:
                  viewModel.setSearchType(ContactSelectorViewModel.SearchType.CONVERSATION);
                  viewModel.searchConversation();
                  break;
                case 1:
                  viewModel.setSearchType(ContactSelectorViewModel.SearchType.FRIEND);
                  viewModel.searchFriend();
                  break;
                case 2:
                  viewModel.setSearchType(ContactSelectorViewModel.SearchType.TEAM);
                  viewModel.searchTeam();
                  break;
                default:
                  break;
              }
            }
          }

          @Override
          public void onTabUnselected(TabLayout.Tab tab) {
            ALog.d(TAG, "onTabUnselected" + tab.getPosition());
          }

          @Override
          public void onTabReselected(TabLayout.Tab tab) {
            ALog.d(TAG, "onTabReselected" + tab.getPosition());
          }
        });

    tabConversation.setText(getString(R.string.recent_conversation));
    tabFriend.setText(getString(R.string.my_friend));
    tabTeam.setText(getString(R.string.my_team));

    //设置适配器
    setSelectorAdapter();

    //最近转发
    LinearLayoutManager layoutManager =
        new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false);
    recentForwardRecyclerView.setLayoutManager(layoutManager);
    recentForwardSelectorAdapter.setSelectableListener(
        (SelectableListener<RecentForward>)
            (selectableBean, selected) -> {
              if (viewModel != null) {
                if (selected && viewModel.selectCountOverflow()) {
                  Toast.makeText(
                          this,
                          getString(
                              R.string.contact_selector_session_max_count,
                              String.valueOf(DEFAULT_SESSION_MAX_SELECT_COUNT)),
                          Toast.LENGTH_SHORT)
                      .show();
                } else {
                  viewModel.selectRecentForward(selectableBean.data, selected);
                }
              }
            });
    recentForwardRecyclerView.setAdapter(recentForwardSelectorAdapter);

    //已经选择
    rvSelected.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
    rvSelected.setAdapter(selectedAdapter);

    rvSelected.setOnTouchListener(
        (v, event) -> {
          if (event.getAction() == MotionEvent.ACTION_UP) {
            showSelectedDetail();
            return true;
          }
          return false;
        });

    ivSelectedDetail.setOnClickListener(
        v -> {
          showSelectedDetail();
        });

    configTitle();

    ivSearchClear.setOnClickListener(v -> searchEditText.setText(""));

    searchEditText.addTextChangedListener(
        new TextWatcher() {
          @Override
          public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

          @Override
          public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

          @Override
          public void afterTextChanged(Editable editable) {
            String key = editable.toString();
            if (TextUtils.isEmpty(key)) {
              ivSearchClear.setVisibility(View.GONE);
            } else {
              ivSearchClear.setVisibility(View.VISIBLE);
            }
            if (viewModel != null) {
              viewModel.setSearchKey(key);
            }
          }
        });
  };

  /** 展示已选中的会话详情 */
  protected abstract void showSelectedDetail();

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
                ArrayList<String> select = viewModel.getSelectedSelectedConversation();
                if (select.size() < 1) {
                  Toast.makeText(this, getString(R.string.select_empty_tips), Toast.LENGTH_SHORT)
                      .show();
                  return;
                }
                if (select.size() > DEFAULT_SESSION_MAX_SELECT_COUNT) {
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
                result.putExtra(KEY_FORWARD_SELECTED_CONVERSATIONS, select);
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
    viewModel = new ViewModelProvider(this).get(ContactSelectorViewModel.class);
    viewModel.setMultiSelectMode(isMultiSelect);
    viewModel.loadRecentForward();
    viewModel
        .getRecentForwardListLiveData()
        .observe(
            this,
            result -> {
              if (result == null) {
                return;
              }
              if (result.getLoadStatus() == LoadStatus.Finish
                  && result.getData() != null
                  && !result.getData().isEmpty()) {
                for (SelectableBean<RecentForward> bean : result.getData()) {
                  recentForwardSelectorAdapter.updateData(bean);
                }
              } else if (result.getLoadStatus() == LoadStatus.Success) {
                if (result.getData() != null && !result.getData().isEmpty()) {
                  recentForwardLayout.setVisibility(View.VISIBLE);
                  recentForwardSelectorAdapter.setData(result.getData());
                } else {
                  recentForwardLayout.setVisibility(View.GONE);
                }
              }
            });

    viewModel
        .getSelectedLiveData()
        .observe(
            this,
            selected -> {
              if (!isMultiSelect && !selected.isEmpty()) {
                //单选直接返回
                selectedLayout.setVisibility(View.GONE);
                ArrayList<String> select = viewModel.getSelectedSelectedConversation();
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
                if (recentForwardLayout.getVisibility() == View.VISIBLE) {
                  selectedDivider.setVisibility(View.VISIBLE);
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
              recentForwardSelectorAdapter.setMultiSelectMode(isMultiSelect);
            });
    viewModel
        .getSearchKeyLiveData()
        .observe(
            this,
            key -> {
              if (TextUtils.isEmpty(key) && !viewModel.getAllRecentForwardList().isEmpty()) {
                recentForwardLayout.setVisibility(View.VISIBLE);
              } else {
                recentForwardLayout.setVisibility(View.GONE);
              }
            });
  }

  /**
   * 设置标题栏右侧数字
   *
   * @param number 数字
   */
  protected abstract void setTitleBarActionNumber(int number);
}
