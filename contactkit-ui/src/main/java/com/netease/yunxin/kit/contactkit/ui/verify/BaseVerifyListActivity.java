// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.verify;

import android.view.View;
import android.widget.Toast;
import androidx.lifecycle.ViewModelProvider;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.contactkit.ui.ILoadListener;
import com.netease.yunxin.kit.contactkit.ui.R;
import com.netease.yunxin.kit.contactkit.ui.activity.BaseListActivity;
import com.netease.yunxin.kit.contactkit.ui.databinding.BaseListActivityLayoutBinding;
import com.netease.yunxin.kit.contactkit.ui.model.ContactVerifyInfoBean;
import java.util.List;

public class BaseVerifyListActivity extends BaseListActivity implements ILoadListener {

  protected VerifyViewModel viewModel;
  protected boolean hasInit = false;
  protected int seriesPageCount = 0;
  protected final int seriesPageLimit = 20;
  protected final int error_duplicate = 104405;

  protected void configViewHolderFactory() {}

  @Override
  protected void initView() {
    configTitle(binding);
    configViewHolderFactory();
    binding.contactListView.setLoadMoreListener(this);
  }

  protected void configTitle(BaseListActivityLayoutBinding binding) {
    binding
        .title
        .setTitle(R.string.verify_msg)
        .setActionText(R.string.clear_all)
        .setActionListener(v -> viewModel.clearNotify());
  }

  @Override
  protected void onResume() {
    super.onResume();
    updateView();
  }

  @Override
  protected void onStop() {
    super.onStop();
    viewModel.resetUnreadCount();
    binding.contactListView.getAdapter().notifyDataSetChanged();
  }

  @Override
  protected void initData() {
    viewModel = new ViewModelProvider(this).get(VerifyViewModel.class);
    viewModel
        .getFetchResult()
        .observe(
            this,
            result -> {
              if (result.getLoadStatus() == LoadStatus.Success) {
                binding.contactListView.addContactData(result.getData());
                if (result.getData() == null
                    || (result.getData() != null && result.getData().size() < 10)) {
                  if (viewModel.hasMore() && seriesPageCount < seriesPageLimit) {
                    seriesPageCount += result.getData() != null ? result.getData().size() : 0;
                    viewModel.fetchVerifyList(true);
                  }
                  seriesPageCount = 0;
                }
                seriesPageCount = 0;

              } else if (result.getLoadStatus() == LoadStatus.Finish) {
                if (result.getType() == FetchResult.FetchType.Remove) {
                  binding.contactListView.removeContactData(result.getData());
                } else if (result.getType() == FetchResult.FetchType.Add) {
                  addNotifyData(result.getData());
                } else if (result.getType() == FetchResult.FetchType.Update) {
                  for (ContactVerifyInfoBean bean : result.getData()) {
                    binding.contactListView.updateVerifyDate(bean);
                  }
                }
              }
              hasInit = true;
              updateView();
            });

    viewModel.fetchVerifyList(false);
  }

  protected void updateView() {
    if (binding.contactListView.getItemCount() > 0 || !hasInit) {
      binding.contactListView.configEmptyViewRes(getEmptyStateViewRes());
      binding.contactListView.setEmptyViewVisible(View.GONE, null);
    } else {
      binding.contactListView.setEmptyViewVisible(
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
    Toast.makeText(this, content, Toast.LENGTH_SHORT).show();
  }

  protected void addNotifyData(List<ContactVerifyInfoBean> addList) {
    if (addList == null || addList.size() < 1) {
      return;
    }
    binding.contactListView.addForwardContactData(addList);
  }

  @Override
  public boolean hasMore() {
    return viewModel.hasMore();
  }

  @Override
  public void loadMore(Object last) {
    viewModel.fetchVerifyList(true);
  }
}
