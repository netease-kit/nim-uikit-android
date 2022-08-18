// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.blacklist;

import static com.netease.yunxin.kit.corekit.im.utils.RouterConstant.REQUEST_CONTACT_SELECTOR_KEY;

import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.contactkit.ui.FetchCallbackImpl;
import com.netease.yunxin.kit.contactkit.ui.R;
import com.netease.yunxin.kit.contactkit.ui.activity.BaseListActivity;
import com.netease.yunxin.kit.contactkit.ui.model.IViewTypeConstant;
import com.netease.yunxin.kit.contactkit.ui.selector.ContactSelectorActivity;
import com.netease.yunxin.kit.contactkit.ui.view.ContactViewHolderFactory;
import com.netease.yunxin.kit.contactkit.ui.view.viewholder.BaseContactViewHolder;
import com.netease.yunxin.kit.contactkit.ui.view.viewholder.BlackListViewHolder;
import com.netease.yunxin.kit.corekit.im.provider.FetchCallback;
import java.util.ArrayList;

public class BlackListActivity extends BaseListActivity {

  private BlackListViewModel viewModel;

  private ActivityResultLauncher<Intent> blackListLauncher;

  private final String TAG = "BlackListActivity";

  protected void initView() {
    registerResult();
    binding.tvTips.setVisibility(View.VISIBLE);
    binding.tvTips.setText(R.string.black_list_tips);

    binding
        .title
        .setTitle(R.string.black_list)
        .setActionImg(R.mipmap.ic_title_bar_more)
        .setActionListener(
            v -> {
              Intent intent = new Intent(this, ContactSelectorActivity.class);
              blackListLauncher.launch(intent);
            });

    binding.contactListView.setViewHolderFactory(
        new ContactViewHolderFactory() {
          @Override
          protected BaseContactViewHolder getCustomViewHolder(ViewGroup view, int viewType) {
            if (viewType == IViewTypeConstant.CONTACT_BLACK_LIST) {
              BlackListViewHolder viewHolder = new BlackListViewHolder(view);
              viewHolder.setRelieveListener(
                  data ->
                      viewModel.removeBlackOp(
                          data.data.getAccount(),
                          new FetchCallback<Void>() {

                            @Override
                            public void onException(@Nullable Throwable exception) {
                              Toast.makeText(
                                      BlackListActivity.this,
                                      getText(R.string.remove_black_fail),
                                      Toast.LENGTH_SHORT)
                                  .show();
                            }

                            @Override
                            public void onFailed(int code) {
                              Toast.makeText(
                                      BlackListActivity.this,
                                      getText(R.string.remove_black_fail),
                                      Toast.LENGTH_SHORT)
                                  .show();
                            }

                            @Override
                            public void onSuccess(@Nullable Void param) {
                              binding.contactListView.removeContactData(data);
                            }
                          }));
              return viewHolder;
            }
            return null;
          }
        });
  }

  private void registerResult() {
    blackListLauncher =
        registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
              if (result.getResultCode() == RESULT_OK && result.getData() != null) {
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
                            Toast.makeText(BlackListActivity.this, content, Toast.LENGTH_SHORT)
                                .show();
                          }

                          @Override
                          public void onException(@Nullable Throwable exception) {
                            String content =
                                String.format(
                                    getResources().getString(R.string.add_black_error), account);
                            Toast.makeText(BlackListActivity.this, content, Toast.LENGTH_SHORT)
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
