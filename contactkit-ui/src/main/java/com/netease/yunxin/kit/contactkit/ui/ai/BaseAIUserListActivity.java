// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.ai;

import android.view.ViewGroup;
import android.widget.Toast;
import androidx.lifecycle.ViewModelProvider;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.common.utils.NetworkUtils;
import com.netease.yunxin.kit.contactkit.ui.R;
import com.netease.yunxin.kit.contactkit.ui.activity.BaseListActivity;
import com.netease.yunxin.kit.contactkit.ui.model.IViewTypeConstant;
import com.netease.yunxin.kit.contactkit.ui.normal.view.ContactViewHolderFactory;
import com.netease.yunxin.kit.contactkit.ui.view.viewholder.AIUserViewHolder;
import com.netease.yunxin.kit.contactkit.ui.view.viewholder.BaseContactViewHolder;
import com.netease.yunxin.kit.corekit.im2.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;

/**
 * AI数日子列表基类 ，子类UI差异分别为协同版(AIUserListActivity)和通用版(FunAIUserListActivity)
 *
 * <p>
 */
public class BaseAIUserListActivity extends BaseListActivity {

  // AI数字人ViewModel
  protected AIUserListViewModel viewModel;

  private final String TAG = "BaseAIUserListActivity";

  protected void initView() {
    configViewHolderFactory();
  }

  protected void initData() {
    viewModel = new ViewModelProvider(this).get(AIUserListViewModel.class);
    setViewHolder();
    viewModel
        .getAIUserListLiveData()
        .observe(
            this,
            result -> {
              if (result.getLoadStatus() == LoadStatus.Success && result.getData() != null) {
                ALog.d(TAG, "FetchResult", "Success:" + result.getData().size());
                if (result.getType() == FetchResult.FetchType.Add) {
                  ALog.d(TAG, "FetchResult", "Add:" + result.getData().size());
                  binding.contactListView.addContactData(result.getData());
                } else if (result.getType() == FetchResult.FetchType.Remove) {
                  ALog.d(TAG, "FetchResult", "Remove:" + result.getData().size());
                  binding.contactListView.removeContactData(result.getData());
                } else {
                  binding.contactListView.addContactData(result.getData());
                }
                if (binding.contactListView.getDataList() == null
                    || binding.contactListView.getDataList().size() == 0) {
                  showEmptyView(true);
                } else {
                  showEmptyView(false);
                }
              }
            });
    checkNetwork();
    viewModel.getAIUserList();
  }

  protected void setViewHolder() {
    binding.contactListView.setViewHolderFactory(
        new ContactViewHolderFactory() {
          @Override
          protected BaseContactViewHolder getCustomViewHolder(ViewGroup view, int viewType) {
            if (viewType == IViewTypeConstant.CONTACT_AI_USER) {
              AIUserViewHolder viewHolder = new AIUserViewHolder(view, false);
              viewHolder.setItemClickListener(
                  bean -> {
                    // 点击事件
                    XKitRouter.withKey(RouterConstant.PATH_USER_INFO_PAGE)
                        .withContext(BaseAIUserListActivity.this)
                        .withParam(RouterConstant.KEY_ACCOUNT_ID_KEY, bean.getAccountId())
                        .navigate();
                  });
              return viewHolder;
            }
            return null;
          }
        });
  }

  // 检查网络并弹出Toast提示
  protected boolean checkNetwork() {
    if (!NetworkUtils.isConnected()) {
      Toast.makeText(
              BaseAIUserListActivity.this, R.string.contact_network_error_tip, Toast.LENGTH_SHORT)
          .show();
      return false;
    }
    return true;
  }
}
