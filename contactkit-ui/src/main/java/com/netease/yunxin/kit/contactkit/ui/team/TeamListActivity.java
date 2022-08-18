// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.team;

import android.content.Intent;
import android.view.ViewGroup;
import androidx.lifecycle.ViewModelProvider;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.contactkit.ui.R;
import com.netease.yunxin.kit.contactkit.ui.activity.BaseListActivity;
import com.netease.yunxin.kit.contactkit.ui.model.IViewTypeConstant;
import com.netease.yunxin.kit.contactkit.ui.view.ContactViewHolderFactory;
import com.netease.yunxin.kit.contactkit.ui.view.viewholder.BaseContactViewHolder;
import com.netease.yunxin.kit.contactkit.ui.view.viewholder.TeamListViewHolder;
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;

public class TeamListActivity extends BaseListActivity {

  private TeamListViewModel viewModel;

  private boolean isSelector;

  @Override
  protected void initView() {
    binding.title.setTitle(R.string.my_team);
    binding.contactListView.setViewHolderFactory(
        new ContactViewHolderFactory() {
          @Override
          protected BaseContactViewHolder getCustomViewHolder(ViewGroup view, int viewType) {
            if (viewType == IViewTypeConstant.CONTACT_TEAM_LIST) {
              TeamListViewHolder viewHolder = new TeamListViewHolder(view);
              viewHolder.setItemClickListener(
                  data -> {
                    if (isSelector) {
                      Intent result = new Intent();
                      result.putExtra(RouterConstant.KEY_TEAM_ID, data.data.getId());
                      setResult(RESULT_OK, result);
                      finish();
                    } else {
                      XKitRouter.withKey(data.router)
                          .withParam(RouterConstant.CHAT_KRY, data.data)
                          .withContext(TeamListActivity.this)
                          .navigate();
                    }
                  });
              return viewHolder;
            }
            return null;
          }
        });
  }

  @Override
  protected void initData() {
    isSelector = getIntent().getBooleanExtra(RouterConstant.KEY_TEAM_LIST_SELECT, false);
    viewModel = new ViewModelProvider(this).get(TeamListViewModel.class);
    viewModel
        .getFetchResult()
        .observe(
            this,
            result -> {
              if (result.getLoadStatus() == LoadStatus.Success) {
                binding.contactListView.clearContactData();
                binding.contactListView.addContactData(result.getData());
              } else if (result.getLoadStatus() == LoadStatus.Finish) {
                if (result.getType() == FetchResult.FetchType.Add) {
                  binding.contactListView.addContactData(result.getData());
                } else if (result.getType() == FetchResult.FetchType.Remove) {
                  binding.contactListView.addContactData(result.getData());
                }
              }
            });
  }

  @Override
  protected void onResume() {
    super.onResume();
    viewModel.fetchTeamList();
  }
}
