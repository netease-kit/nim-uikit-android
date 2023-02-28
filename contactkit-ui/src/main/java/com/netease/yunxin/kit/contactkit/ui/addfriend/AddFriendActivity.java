// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.addfriend;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import com.netease.yunxin.kit.common.ui.activities.BaseActivity;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.contactkit.ui.R;
import com.netease.yunxin.kit.contactkit.ui.databinding.AddFriendActivityBinding;
import com.netease.yunxin.kit.corekit.im.IMKitClient;
import com.netease.yunxin.kit.corekit.im.model.UserInfo;
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;

public class AddFriendActivity extends BaseActivity {

  private AddFriendActivityBinding viewBinding;
  private AddFriendViewModel viewModel;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    viewBinding = AddFriendActivityBinding.inflate(getLayoutInflater());
    viewModel = new ViewModelProvider(this).get(AddFriendViewModel.class);
    setContentView(viewBinding.getRoot());
    viewBinding.etAddFriendAccount.setOnEditorActionListener(actionListener);
    viewModel.getFetchResult().observe(this, this::loadData);
    viewBinding.ivAddFriendBack.setOnClickListener(v -> this.finish());
    viewBinding.ivFriendClear.setOnClickListener(v -> viewBinding.etAddFriendAccount.setText(null));
    viewBinding.etAddFriendAccount.addTextChangedListener(
        new TextWatcher() {
          @Override
          public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

          @Override
          public void onTextChanged(CharSequence s, int start, int before, int count) {}

          @Override
          public void afterTextChanged(Editable s) {
            if (TextUtils.isEmpty(s.toString())) {
              viewBinding.ivFriendClear.setVisibility(View.GONE);
              showEmptyView(false);
            } else {
              viewBinding.ivFriendClear.setVisibility(View.VISIBLE);
            }
          }
        });
  }

  private final EditText.OnEditorActionListener actionListener =
      (v, actionId, event) -> {
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
          String accountId = v.getEditableText().toString();
          if (!TextUtils.isEmpty(accountId)) {
            viewModel.fetchUser(accountId);
          }
        }
        return false;
      };

  private void loadData(FetchResult<UserInfo> result) {
    if (result.getLoadStatus() == LoadStatus.Loading) {
      //todo show loading progress
    } else if (result.getLoadStatus() == LoadStatus.Success) {
      if (result.getData() != null) {
        showEmptyView(false);
        startProfileActivity(result.getData());
      } else {
        showEmptyView(true);
      }

    } else if (result.getLoadStatus() == LoadStatus.Error) {
      Toast.makeText(
              this, getResources().getString(R.string.add_friend_search_error), Toast.LENGTH_SHORT)
          .show();
    }
  }

  private void showEmptyView(boolean show) {
    if (show) {
      viewBinding.addFriendEmptyLayout.setVisibility(View.VISIBLE);
    } else {
      viewBinding.addFriendEmptyLayout.setVisibility(View.GONE);
    }
  }

  private void startProfileActivity(UserInfo userInfo) {
    if (userInfo == null) {
      return;
    }
    if (TextUtils.equals(userInfo.getAccount(), IMKitClient.account())) {
      XKitRouter.withKey(RouterConstant.PATH_MINE_INFO_PAGE).withContext(this).navigate();
    } else {
      XKitRouter.withKey(RouterConstant.PATH_USER_INFO_PAGE)
          .withContext(this)
          .withParam(RouterConstant.KEY_ACCOUNT_ID_KEY, userInfo.getAccount())
          .navigate();
    }
  }
}
