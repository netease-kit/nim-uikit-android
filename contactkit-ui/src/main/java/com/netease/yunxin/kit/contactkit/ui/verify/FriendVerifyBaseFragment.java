// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.verify;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import com.netease.yunxin.kit.common.ui.dialog.ChoiceListener;
import com.netease.yunxin.kit.common.ui.dialog.CommonChoiceDialog;
import com.netease.yunxin.kit.common.ui.fragments.BaseFragment;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.common.utils.NetworkUtils;
import com.netease.yunxin.kit.contactkit.ui.ILoadListener;
import com.netease.yunxin.kit.contactkit.ui.R;
import com.netease.yunxin.kit.contactkit.ui.databinding.BaseFriendVerifyListLayoutBinding;
import com.netease.yunxin.kit.contactkit.ui.model.ContactVerifyInfoBean;
import java.util.List;

public class FriendVerifyBaseFragment extends BaseFragment implements ILoadListener {

  protected BaseFriendVerifyListLayoutBinding layoutBinding;
  protected FriendVerifyViewModel viewModel;
  protected boolean hasInit = false;
  protected int seriesPageCount = 0;
  protected final int seriesPageLimit = 20;
  protected final int error_duplicate = 104405;

  protected void configViewHolderFactory() {}

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    View rootView = initViewAndGetRootView(inflater, container);
    bindView();
    initData();
    return rootView;
  }

  //初始化View 并返回布局的RootView
  public View initViewAndGetRootView(
      @NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
    layoutBinding = BaseFriendVerifyListLayoutBinding.inflate(inflater, container, false);
    return layoutBinding.getRoot();
  }

  protected void bindView() {
    configViewHolderFactory();
    layoutBinding.contactListView.setLoadMoreListener(this);
  }

  @Override
  public void onResume() {
    super.onResume();
    updateView();
  }

  @Override
  public void onStop() {
    super.onStop();
    viewModel.resetUnreadCount();
    layoutBinding.contactListView.getAdapter().notifyDataSetChanged();
  }

  protected void initData() {
    viewModel = new ViewModelProvider(this).get(FriendVerifyViewModel.class);
    viewModel
        .getFetchResult()
        .observe(
            this.getViewLifecycleOwner(),
            result -> {
              if (result.getLoadStatus() == LoadStatus.Success) {
                layoutBinding.contactListView.addContactData(result.getData());
                if (result.getData() == null
                    || (result.getData() != null && result.getData().size() < 10)) {
                  if (viewModel.hasMore() && seriesPageCount < seriesPageLimit) {
                    seriesPageCount += result.getData() != null ? result.getData().size() : 0;
                    viewModel.getFriendVerifyList(true);
                  }
                  seriesPageCount = 0;
                }
                seriesPageCount = 0;

              } else if (result.getLoadStatus() == LoadStatus.Finish) {
                if (result.getType() == FetchResult.FetchType.Remove) {
                  layoutBinding.contactListView.removeContactData(result.getData());
                } else if (result.getType() == FetchResult.FetchType.Add) {
                  addNotifyData(result.getData());
                } else if (result.getType() == FetchResult.FetchType.Update) {
                  for (ContactVerifyInfoBean bean : result.getData()) {
                    layoutBinding.contactListView.updateVerifyDate(bean);
                  }
                }
              }
              hasInit = true;
              updateView();
            });

    viewModel.getFriendVerifyList(false);
  }

  protected void updateView() {
    if (layoutBinding.contactListView.getItemCount() > 0 || !hasInit) {
      layoutBinding.contactListView.configEmptyViewRes(getEmptyStateViewRes());
      layoutBinding.contactListView.setEmptyViewVisible(View.GONE, null);
    } else {
      layoutBinding.contactListView.setEmptyViewVisible(
          View.VISIBLE, getString(R.string.verify_empty_text));
    }
  }

  protected int getEmptyStateViewRes() {
    return 0;
  }

  protected void toastResult(boolean agree, int errorCode) {
    String content = null;
    if (errorCode == error_duplicate) {
      content = getResources().getString(R.string.verify_duplicate_fail);
    } else {
      content =
          agree
              ? getResources().getString(R.string.agree_add_friend_fail)
              : getResources().getString(R.string.disagree_add_friend_fail);
    }
    Toast.makeText(FriendVerifyBaseFragment.this.requireContext(), content, Toast.LENGTH_SHORT)
        .show();
  }

  protected void addNotifyData(List<ContactVerifyInfoBean> addList) {
    if (addList == null || addList.size() < 1) {
      return;
    }
    layoutBinding.contactListView.addForwardContactData(addList);
  }

  @Override
  public boolean hasMore() {
    return viewModel.hasMore();
  }

  @Override
  public void loadMore(Object last) {
    viewModel.getFriendVerifyList(true);
  }

  @Override
  public void onScrollStateIdle(int first, int end) {}

  public void clearVerifyList() {
    CommonChoiceDialog dialog = new CommonChoiceDialog();
    dialog
        .setTitleStr(getString(R.string.contact_dialog_clear_friend_verify_title))
        .setContentStr(getString(R.string.contact_dialog_clear_friend_verify_content))
        .setNegativeStr(getString(R.string.contact_dialog_cancel))
        .setPositiveStr(getString(R.string.contact_dialog_sure))
        .setConfirmListener(
            new ChoiceListener() {
              @Override
              public void onPositive() {
                if (NetworkUtils.isConnected()) {
                  if (viewModel != null) {
                    viewModel.clearNotify();
                  }
                } else {
                  Toast.makeText(
                          FriendVerifyBaseFragment.this.requireContext(),
                          R.string.contact_network_error_tip,
                          Toast.LENGTH_SHORT)
                      .show();
                }
              }

              @Override
              public void onNegative() {}
            })
        .show(getParentFragmentManager());
  }
}
