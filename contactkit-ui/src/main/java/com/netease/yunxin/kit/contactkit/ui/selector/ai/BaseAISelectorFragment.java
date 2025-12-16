// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.selector.ai;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.netease.nimlib.coexist.sdk.v2.ai.model.V2NIMAIUser;
import com.netease.yunxin.kit.common.ui.fragments.BaseFragment;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.contactkit.ui.R;
import com.netease.yunxin.kit.contactkit.ui.model.SelectableBean;
import com.netease.yunxin.kit.contactkit.ui.selector.SelectableListener;
import com.netease.yunxin.kit.corekit.coexist.im2.utils.RouterConstant;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseAISelectorFragment extends BaseFragment {

  // 群组列表
  protected RecyclerView recyclerView;

  // 空布局
  protected LinearLayout emptyLayout;

  // 搜索结果为空
  protected LinearLayout searchEmpty;

  protected AIUserSelectorViewModel viewModel;

  protected ArrayList<String> filterUser;

  protected BaseAIUserSelectorAdapter adapter;

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    View rootView = getRootView(inflater, container);
    initView();
    initData();
    return rootView;
  }

  /**
   * 获取根布局
   *
   * @param inflater inflater
   * @param container container
   * @return 根布局
   */
  protected abstract View getRootView(
      @NonNull LayoutInflater inflater, @Nullable ViewGroup container);

  protected void initData() {
    if (getArguments() != null) {
      filterUser = getArguments().getStringArrayList(RouterConstant.SELECTOR_CONTACT_FILTER_KEY);
    }
    viewModel = new ViewModelProvider(requireActivity()).get(AIUserSelectorViewModel.class);
    viewModel.loadAIUserList();
    viewModel
        .getIsMultiSelectModeLiveData()
        .observe(
            getViewLifecycleOwner(),
            isMultiSelectMode -> {
              if (adapter != null) {
                adapter.setMultiSelectMode(isMultiSelectMode);
              }
            });
    viewModel
        .getAIUserListLiveData()
        .observe(
            getViewLifecycleOwner(),
            result -> {
              if (result == null) {
                return;
              }
              if (result.getLoadStatus() == LoadStatus.Finish) {
                if (result.getData() != null && !result.getData().isEmpty()) {
                  for (SelectableBean<V2NIMAIUser> bean : result.getData()) {
                    adapter.updateData(bean);
                  }
                }
              } else if (result.getLoadStatus() == LoadStatus.Success
                  && result.getData() != null
                  && !result.getData().isEmpty()) {
                emptyLayout.setVisibility(View.GONE);
                List<SelectableBean<V2NIMAIUser>> userInfoBeanList = filterUser(result.getData());
                adapter.setData(userInfoBeanList);
              }

              if (adapter.getItemCount() < 1) {
                recyclerView.setVisibility(View.GONE);
                emptyLayout.setVisibility(View.VISIBLE);
              } else {
                emptyLayout.setVisibility(View.GONE);
                searchEmpty.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
              }
            });
  }

  void initView() {
    LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
    recyclerView.setLayoutManager(layoutManager);
    adapter = provideAdapter();
    recyclerView.setAdapter(adapter);
    adapter.setSelectableListener(
        (SelectableListener<V2NIMAIUser>)
            (selectableBean, selected) -> {
              if (viewModel != null) {
                if (selected && viewModel.selectCountOverflow()) {
                  Toast.makeText(
                          getActivity(),
                          getString(
                              R.string.contact_selector_max_count,
                              String.valueOf(viewModel.getMaxSelectorCount())),
                          Toast.LENGTH_SHORT)
                      .show();
                } else {
                  viewModel.selectAIUser(selectableBean.data, selected);
                }
              }
            });
  }

  /**
   * 提供adapter
   *
   * @return adapter
   */
  protected abstract BaseAIUserSelectorAdapter provideAdapter();

  protected List<SelectableBean<V2NIMAIUser>> filterUser(List<SelectableBean<V2NIMAIUser>> source) {
    if (filterUser == null || filterUser.isEmpty()) {
      return source;
    }
    List<SelectableBean<V2NIMAIUser>> result = new ArrayList<>(source);
    for (SelectableBean<V2NIMAIUser> userInfoBean : source) {
      if (filterUser.contains(userInfoBean.data.getAccountId())) {
        result.remove(userInfoBean);
      }
    }
    return result;
  }
}
