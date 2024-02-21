// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.blacklist;

import static com.netease.yunxin.kit.corekit.im.utils.RouterConstant.REQUEST_CONTACT_SELECTOR_KEY;

import android.content.Intent;
import android.view.View;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.common.utils.NetworkUtils;
import com.netease.yunxin.kit.contactkit.ui.FetchCallbackImpl;
import com.netease.yunxin.kit.contactkit.ui.R;
import com.netease.yunxin.kit.contactkit.ui.activity.BaseListActivity;
import com.netease.yunxin.kit.contactkit.ui.databinding.BaseListActivityLayoutBinding;
import java.util.ArrayList;

public class BaseBlackListActivity extends BaseListActivity {

  protected BlackListViewModel viewModel;

  protected ActivityResultLauncher<Intent> blackListLauncher;

  private final String TAG = "BaseBlackListActivity";

  protected void initView() {
    registerResult();
    binding.tvTips.setVisibility(View.VISIBLE);
    binding.tvTips.setText(R.string.black_list_tips);
    configTitle(binding);
    configViewHolderFactory();
  }

  protected void configTitle(BaseListActivityLayoutBinding binding) {}

  private void registerResult() {
    blackListLauncher =
        registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
              if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                if (!NetworkUtils.isConnected()) {
                  Toast.makeText(this, R.string.contact_network_error_tip, Toast.LENGTH_SHORT)
                      .show();
                  return;
                }
                ArrayList<String> friends =
                    result.getData().getStringArrayListExtra(REQUEST_CONTACT_SELECTOR_KEY);
                if (friends != null && friends.size() > 0) {
                  for (String account : friends) {
                    ALog.d(TAG, "addBlackList", "account:" + account);
                    viewModel.addBlackOp(
                        account,
                        new FetchCallbackImpl<Void>(account) {
                          @Override
                          public void onFailed(int code) {
                            String content =
                                String.format(
                                    getResources().getString(R.string.add_black_error), account);
                            Toast.makeText(BaseBlackListActivity.this, content, Toast.LENGTH_SHORT)
                                .show();
                          }

                          @Override
                          public void onException(@Nullable Throwable exception) {
                            String content =
                                String.format(
                                    getResources().getString(R.string.add_black_error), account);
                            Toast.makeText(BaseBlackListActivity.this, content, Toast.LENGTH_SHORT)
                                .show();
                          }
                        });
                  }
                }
              }
            });
  }

  protected void initData() {
    viewModel = new ViewModelProvider(this).get(BlackListViewModel.class);
    viewModel
        .getFetchResult()
        .observe(
            this,
            result -> {
              if (!NetworkUtils.isConnected()) {
                Toast.makeText(this, R.string.contact_network_error_tip, Toast.LENGTH_SHORT).show();
                return;
              }
              if (result.getLoadStatus() == LoadStatus.Success && result.getData() != null) {
                ALog.d(TAG, "FetchResult", "Success:" + result.getData().size());
                binding.contactListView.addContactData(result.getData());
              }
              // LoadStatus.Finish respect observer data changed
              if (result.getLoadStatus() == LoadStatus.Finish && result.getData() != null) {
                if (result.getType() == FetchResult.FetchType.Add) {
                  ALog.d(TAG, "FetchResult", "Add:" + result.getData().size());
                  binding.contactListView.addContactData(result.getData());
                } else if (result.getType() == FetchResult.FetchType.Remove) {
                  ALog.d(TAG, "FetchResult", "Remove:" + result.getData().size());
                  binding.contactListView.removeContactData(result.getData());
                }
              }
            });
    viewModel.fetchBlackList();
  }
}
