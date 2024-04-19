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
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import com.netease.yunxin.kit.chatkit.utils.ErrorUtils;
import com.netease.yunxin.kit.common.ui.activities.BaseActivity;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.corekit.im2.IMKitClient;
import com.netease.yunxin.kit.corekit.im2.model.UserWithFriend;
import com.netease.yunxin.kit.corekit.im2.model.V2UserInfo;
import com.netease.yunxin.kit.corekit.im2.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import java.util.Objects;

/**
 * 添加好友基类 根据账号ID进行搜到，然后跳转到用户详情页
 *
 * <p>
 */
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
            viewModel.getUser(accountId);
          }
        }
        return false;
      };

  // 加载搜索结果
  private void loadData(FetchResult<UserWithFriend> result) {
    if (result.getLoadStatus() == LoadStatus.Success) {
      if (result.getData() != null && result.getData().getUserInfo() != null) {
        showEmptyView(false);
        startProfileActivity(
            new V2UserInfo(result.getData().getAccount(), result.getData().getUserInfo()));
      } else {
        showEmptyView(true);
      }

    } else if (result.getLoadStatus() == LoadStatus.Error && result.getError() != null) {
      ErrorUtils.showErrorCodeToast(this, result.getError().getCode());
    }
  }

  // 显示空布局
  private void showEmptyView(boolean show) {
    if (show) {
      addFriendEmptyLayout.setVisibility(View.VISIBLE);
    } else {
      addFriendEmptyLayout.setVisibility(View.GONE);
    }
  }

  // 跳转到用户详情页
  protected void startProfileActivity(V2UserInfo userInfo) {
    if (userInfo == null) {
      return;
    }
    if (TextUtils.equals(userInfo.getAccountId(), IMKitClient.account())) {
      XKitRouter.withKey(RouterConstant.PATH_MINE_INFO_PAGE).withContext(this).navigate();
    } else {
      XKitRouter.withKey(RouterConstant.PATH_USER_INFO_PAGE)
          .withContext(this)
          .withParam(RouterConstant.KEY_ACCOUNT_ID_KEY, userInfo.getAccountId())
          .navigate();
    }
  }
}
