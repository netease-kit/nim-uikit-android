// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.selector.forward;

import static com.netease.yunxin.kit.contactkit.ui.ContactUIConfig.DEFAULT_SESSION_MAX_SELECT_COUNT;

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
import com.netease.yunxin.kit.common.ui.fragments.BaseFragment;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.contactkit.ui.R;
import com.netease.yunxin.kit.contactkit.ui.model.ContactFriendBean;
import com.netease.yunxin.kit.contactkit.ui.selector.forward.adapter.BaseFriendSelectorAdapter;

public abstract class BaseFriendSelectorFragment extends BaseFragment {

  // 好友列表
  protected RecyclerView recyclerView;

  // 空布局
  protected LinearLayout emptyLayout;

  // 搜索结果为空
  protected LinearLayout searchEmpty;

  protected ContactSelectorViewModel viewModel;

  protected BaseFriendSelectorAdapter adapter;

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    View rootView = getRootView(inflater, container);
    bindView();
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

  protected void bindView() {

    LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
    recyclerView.setLayoutManager(layoutManager);
    adapter = provideAdapter();
    recyclerView.setAdapter(adapter);
    adapter.setSelectableListener(
        new BaseFriendSelectorAdapter.FriendSelectorListener() {
          @Override
          public void onSelected(ContactFriendBean bean, boolean selected) {
            if (viewModel != null) {
              if (selected && viewModel.selectCountOverflow()) {
                Toast.makeText(
                        getActivity(),
                        getString(
                            R.string.contact_selector_session_max_count,
                            String.valueOf(DEFAULT_SESSION_MAX_SELECT_COUNT)),
                        Toast.LENGTH_SHORT)
                    .show();
              } else {
                viewModel.selectFriend(bean.data, selected);
              }
            }
          }
        });
  }

  /**
   * 提供adapter
   *
   * @return adapter
   */
  protected abstract BaseFriendSelectorAdapter provideAdapter();

  void initData() {
    viewModel = new ViewModelProvider(requireActivity()).get(ContactSelectorViewModel.class);
    viewModel.loadFriends();
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
        .getFriendListLiveData()
        .observe(
            getViewLifecycleOwner(),
            result -> {
              if (result == null) {
                return;
              }
              emptyLayout.setVisibility(View.GONE);
              searchEmpty.setVisibility(View.GONE);
              recyclerView.setVisibility(View.VISIBLE);
              if (result.getLoadStatus() == LoadStatus.Finish) {
                if (result.getData() != null && !result.getData().isEmpty()) {
                  for (ContactFriendBean bean : result.getData()) {
                    adapter.updateData(bean);
                  }
                }
              } else if (result.getLoadStatus() == LoadStatus.Success) {
                if (result.getData() != null && !result.getData().isEmpty()) {
                  emptyLayout.setVisibility(View.GONE);
                  adapter.setData(result.getData());
                } else {
                  recyclerView.setVisibility(View.GONE);
                  emptyLayout.setVisibility(View.VISIBLE);
                }
              }
            });

    viewModel
        .getSearchFriendResultLiveData()
        .observe(
            getViewLifecycleOwner(),
            result -> {
              if (result == null) {
                return;
              }
              if (result.getLoadStatus() == LoadStatus.Success) {
                if (result.getData() != null && !result.getData().isEmpty()) {
                  emptyLayout.setVisibility(View.GONE);
                  searchEmpty.setVisibility(View.GONE);
                  recyclerView.setVisibility(View.VISIBLE);
                  adapter.setData(result.getData());
                } else {
                  showSearchResultEmptyView();
                }
              }
            });
  }

  /** 展示搜索结果为空的view */
  protected void showSearchResultEmptyView() {
    recyclerView.setVisibility(View.GONE);
    searchEmpty.setVisibility(View.VISIBLE);
  }
}
