// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.blacklist;

import static com.netease.yunxin.kit.corekit.im2.utils.RouterConstant.REQUEST_CONTACT_SELECTOR_KEY;

import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.lifecycle.ViewModelProvider;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.common.utils.NetworkUtils;
import com.netease.yunxin.kit.contactkit.ui.R;
import com.netease.yunxin.kit.contactkit.ui.activity.BaseListActivity;
import com.netease.yunxin.kit.contactkit.ui.model.IViewTypeConstant;
import com.netease.yunxin.kit.contactkit.ui.normal.view.ContactViewHolderFactory;
import com.netease.yunxin.kit.contactkit.ui.normal.view.viewholder.BlackListViewHolder;
import com.netease.yunxin.kit.contactkit.ui.view.viewholder.BaseContactViewHolder;
import java.util.ArrayList;

/**
 * 黑名单基类 ，子类UI差异分别为普通版黑名单(BlackListActivity)和娱乐版黑名单(FunBlackListActivity)
 *
 * <p>
 */
public class BaseBlackListActivity extends BaseListActivity {

  // 黑名单ViewModel
  protected BlackListViewModel viewModel;

  // 黑名单选择器launcher
  protected ActivityResultLauncher<Intent> blackListLauncher;

  private final String TAG = "BaseBlackListActivity";

  protected void initView() {
    binding.tvTips.setVisibility(View.VISIBLE);
    binding.tvTips.setText(R.string.black_list_tips);
    configViewHolderFactory();
  }

  protected void initData() {
    viewModel = new ViewModelProvider(this).get(BlackListViewModel.class);
    blackListLauncher =
        registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
              if (result.getResultCode() == RESULT_OK
                  && result.getData() != null
                  && checkNetwork()) {
                ArrayList<String> friends =
                    result.getData().getStringArrayListExtra(REQUEST_CONTACT_SELECTOR_KEY);
                if (friends != null && friends.size() > 0) {
                  for (String account : friends) {
                    ALog.d(TAG, "addBlackList", "account:" + account);
                    viewModel.addBlackOp(account);
                  }
                }
              }
            });

    binding.contactListView.setViewHolderFactory(
        new ContactViewHolderFactory() {
          @Override
          protected BaseContactViewHolder getCustomViewHolder(ViewGroup view, int viewType) {
            if (viewType == IViewTypeConstant.CONTACT_BLACK_LIST) {
              BlackListViewHolder viewHolder = new BlackListViewHolder(view);
              viewHolder.setRelieveListener(
                  data -> {
                    if (checkNetwork()) {
                      viewModel.removeBlackOp(data.data.getAccountId());
                    }
                  });
              return viewHolder;
            }
            return null;
          }
        });
    viewModel
        .getBlackListLiveData()
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
              }
            });
    checkNetwork();
    viewModel.getBlackList();
  }

  // 检查网络并弹出Toast提示
  private boolean checkNetwork() {
    if (!NetworkUtils.isConnected()) {
      Toast.makeText(
              BaseBlackListActivity.this, R.string.contact_network_error_tip, Toast.LENGTH_SHORT)
          .show();
      return false;
    }
    return true;
  }
}
