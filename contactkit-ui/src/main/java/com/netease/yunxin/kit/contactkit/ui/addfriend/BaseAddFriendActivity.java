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
import com.netease.yunxin.kit.corekit.im.IMKitClient;
import com.netease.yunxin.kit.corekit.im.model.UserInfo;
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import java.util.Objects;

public abstract class BaseAddFriendActivity extends BaseActivity {

  protected AddFriendViewModel viewModel;

  private View rootView;
  protected EditText etAddFriendAccount;
  protected View ivAddFriendBack;
  protected View ivFriendClear;
  protected View addFriendEmptyLayout;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    rootView = initViewAndGetRootView(savedInstanceState);
    checkViews();
    setContentView(rootView);
    viewModel = new ViewModelProvider(this).get(AddFriendViewModel.class);
    etAddFriendAccount.setOnEditorActionListener(actionListener);
    viewModel.getFetchResult().observe(this, this::loadData);
    ivAddFriendBack.setOnClickListener(v -> this.finish());
    ivFriendClear.setOnClickListener(v -> etAddFriendAccount.setText(null));
    etAddFriendAccount.addTextChangedListener(
        new TextWatcher() {
          @Override
          public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

          @Override
          public void onTextChanged(CharSequence s, int start, int before, int count) {}

          @Override
          public void afterTextChanged(Editable s) {
            if (TextUtils.isEmpty(s.toString())) {
              ivFriendClear.setVisibility(View.GONE);
              showEmptyView(false);
            } else {
              ivFriendClear.setVisibility(View.VISIBLE);
            }
          }
        });
  }

  protected abstract View initViewAndGetRootView(Bundle savedInstanceState);

  protected void checkViews() {
    Objects.requireNonNull(rootView);
    Objects.requireNonNull(etAddFriendAccount);
    Objects.requireNonNull(ivAddFriendBack);
    Objects.requireNonNull(ivFriendClear);
    Objects.requireNonNull(addFriendEmptyLayout);
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
      addFriendEmptyLayout.setVisibility(View.VISIBLE);
    } else {
      addFriendEmptyLayout.setVisibility(View.GONE);
    }
  }

  protected void startProfileActivity(UserInfo userInfo) {
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
