// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.team;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import com.netease.nimlib.coexist.sdk.v2.team.model.V2NIMTeam;
import com.netease.yunxin.kit.common.ui.activities.BaseLocalActivity;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.corekit.coexist.im2.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import java.util.Objects;

/**
 * 群搜索 根据账号ID进行搜到，然后跳转到群详情页
 *
 * <p>
 */
public abstract class BaseTeamSearchActivity extends BaseLocalActivity {

  protected JoinTeamViewModel viewModel;

  private View rootView;
  protected EditText etTeamId;
  protected View ivBack;
  protected View ivClear;
  protected View emptyLayout;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    rootView = initViewAndGetRootView(savedInstanceState);
    checkViews();
    setContentView(rootView);
    viewModel = new ViewModelProvider(this).get(JoinTeamViewModel.class);
    etTeamId.setOnEditorActionListener(actionListener);
    viewModel.getFetchResult().observe(this, this::loadData);
    ivBack.setOnClickListener(v -> this.finish());
    ivClear.setOnClickListener(v -> etTeamId.setText(null));
    etTeamId.addTextChangedListener(
        new TextWatcher() {
          @Override
          public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

          @Override
          public void onTextChanged(CharSequence s, int start, int before, int count) {}

          @Override
          public void afterTextChanged(Editable s) {
            if (TextUtils.isEmpty(s.toString())) {
              ivClear.setVisibility(View.GONE);
              showEmptyView(false);
            } else {
              ivClear.setVisibility(View.VISIBLE);
            }
          }
        });
  }

  protected abstract View initViewAndGetRootView(Bundle savedInstanceState);

  protected void checkViews() {
    Objects.requireNonNull(rootView);
    Objects.requireNonNull(etTeamId);
    Objects.requireNonNull(ivBack);
    Objects.requireNonNull(ivClear);
    Objects.requireNonNull(emptyLayout);
  }

  private final EditText.OnEditorActionListener actionListener =
      (v, actionId, event) -> {
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
          String accountId = v.getEditableText().toString();
          if (!TextUtils.isEmpty(accountId)) {
            viewModel.getTeam(accountId);
          }
        }
        return false;
      };

  // 加载搜索结果
  private void loadData(FetchResult<V2NIMTeam> result) {
    if (result.getLoadStatus() == LoadStatus.Success) {
      if (result.getData() != null && result.getData() != null) {
        showEmptyView(false);
        startTeamProfileActivity(result.getData());
      } else {
        showEmptyView(true);
      }

    } else if (result.getLoadStatus() == LoadStatus.Error && result.getError() != null) {
      showEmptyView(true);
    }
  }

  // 显示空布局
  private void showEmptyView(boolean show) {
    if (show) {
      emptyLayout.setVisibility(View.VISIBLE);
    } else {
      emptyLayout.setVisibility(View.GONE);
    }
  }

  // 跳转到用户详情页
  protected void startTeamProfileActivity(V2NIMTeam team) {
    if (team == null) {
      return;
    }
    XKitRouter.withKey(RouterConstant.PATH_TEAM_PROFILE_PAGE)
        .withContext(this)
        .withParam(RouterConstant.KEY_ACCOUNT_ID_KEY, team.getTeamId())
        .navigate();
  }
}
