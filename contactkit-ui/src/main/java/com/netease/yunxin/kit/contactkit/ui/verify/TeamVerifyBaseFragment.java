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
import androidx.recyclerview.widget.LinearLayoutManager;
import com.netease.yunxin.kit.common.ui.dialog.ChoiceListener;
import com.netease.yunxin.kit.common.ui.dialog.CommonChoiceDialog;
import com.netease.yunxin.kit.common.ui.fragments.BaseFragment;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.contactkit.ui.IContactFactory;
import com.netease.yunxin.kit.contactkit.ui.ILoadListener;
import com.netease.yunxin.kit.contactkit.ui.R;
import com.netease.yunxin.kit.contactkit.ui.databinding.BaseTeamVerifyListLayoutBinding;
import com.netease.yunxin.kit.contactkit.ui.model.TeamVerifyInfoBean;
import com.netease.yunxin.kit.contactkit.ui.utils.ContactUtils;
import com.netease.yunxin.kit.corekit.event.EventCenter;
import java.util.List;

public class TeamVerifyBaseFragment extends BaseFragment implements ILoadListener {

  protected BaseTeamVerifyListLayoutBinding layoutBinding;
  protected TeamVerifyViewModel viewModel;
  protected TeamVerifyAdapter adapter;
  protected boolean hasInit = false;
  protected int seriesPageCount = 0;
  protected final int seriesPageLimit = 20;
  protected final int error_duplicate = 104405;

  protected IContactFactory configViewHolderFactory() {
    return null;
  }

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
    layoutBinding = BaseTeamVerifyListLayoutBinding.inflate(inflater, container, false);
    return layoutBinding.getRoot();
  }

  protected void bindView() {
    adapter = new TeamVerifyAdapter();
    adapter.setViewHolderFactory(configViewHolderFactory());
    layoutBinding.rvList.setLayoutManager(new LinearLayoutManager(this.requireContext()));
    layoutBinding.rvList.setAdapter(adapter);
  }

  @Override
  public void onResume() {
    super.onResume();
  }

  @Override
  public void onStop() {
    super.onStop();
  }

  protected void initData() {
    viewModel = new ViewModelProvider(this).get(TeamVerifyViewModel.class);
    viewModel
        .getFetchResult()
        .observe(
            this.getViewLifecycleOwner(),
            result -> {
              if (result.getLoadStatus() == LoadStatus.Success) {
                adapter.setListData(result.getData());
                if (result.getData() == null
                    || (result.getData() != null && result.getData().size() < 10)) {
                  if (viewModel.hasMore() && seriesPageCount < seriesPageLimit) {
                    seriesPageCount += result.getData() != null ? result.getData().size() : 0;
                    viewModel.getTeamVerifyList(true);
                  }
                }
                seriesPageCount = 0;

              } else if (result.getLoadStatus() == LoadStatus.Finish) {
                if (result.getType() == FetchResult.FetchType.Remove) {
                  adapter.removeListData(result.getData());
                } else if (result.getType() == FetchResult.FetchType.Add) {
                  addNotifyData(result.getData());
                } else if (result.getType() == FetchResult.FetchType.Update) {
                  for (TeamVerifyInfoBean bean : result.getData()) {
                    adapter.updateData(bean);
                  }
                }
              }
              hasInit = true;
              updateView();
            });

    viewModel
        .getUpdateLiveData()
        .observe(
            this.getViewLifecycleOwner(),
            result -> {
              if (result.getType() == FetchResult.FetchType.Update && result.getData() != null) {
                adapter.updateData(result.getData());
              }
            });

    viewModel.getTeamVerifyList(false);
  }

  protected void updateView() {
    if (adapter.getItemCount() > 0 || !hasInit) {
      layoutBinding.emptyLayout.setVisibility(View.GONE);
    } else {
      layoutBinding.emptyIv.setImageResource(getEmptyStateViewRes());
      layoutBinding.emptyLayout.setVisibility(View.VISIBLE);
      layoutBinding.emptyTv.setText(R.string.verify_empty_text);
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
    Toast.makeText(TeamVerifyBaseFragment.this.requireContext(), content, Toast.LENGTH_SHORT)
        .show();
  }

  protected void addNotifyData(List<TeamVerifyInfoBean> addList) {
    if (addList == null || addList.size() < 1) {
      return;
    }
    adapter.addForwardListData(addList);
  }

  @Override
  public boolean hasMore() {
    return viewModel.hasMore();
  }

  @Override
  public void loadMore(Object last) {
    viewModel.getTeamVerifyList(true);
  }

  @Override
  public void onScrollStateIdle(int first, int end) {}

  public void clearVerifyList() {
    CommonChoiceDialog dialog = new CommonChoiceDialog();
    dialog
        .setTitleStr(getString(R.string.contact_dialog_clear_team_verify_title))
        .setContentStr(getString(R.string.contact_dialog_clear_team_verify_content))
        .setNegativeStr(getString(R.string.contact_dialog_cancel))
        .setPositiveStr(getString(R.string.contact_dialog_sure))
        .setConfirmListener(
            new ChoiceListener() {
              @Override
              public void onPositive() {
                if (viewModel != null) {
                  viewModel.clearNotify();
                }
                clearUnreadCount();
              }

              @Override
              public void onNegative() {}
            })
        .show(getParentFragmentManager());
  }

  public void clearUnreadCount() {
    if (viewModel != null) {
      viewModel.clearUnreadCount(this.requireContext());
    } else {
      long readTimeStamp = System.currentTimeMillis();
      ContactUtils.setTeamVerifyReadTime(readTimeStamp);
    }
    EventCenter.notifyEvent(new ClearTeamVerifyEvent());
  }
}
