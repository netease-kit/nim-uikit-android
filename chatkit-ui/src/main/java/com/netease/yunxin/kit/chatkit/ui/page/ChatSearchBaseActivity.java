// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.page;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.yunxin.kit.chatkit.ui.page.adapter.SearchMessageAdapter;
import com.netease.yunxin.kit.chatkit.ui.page.viewmodel.SearchMessageViewModel;
import com.netease.yunxin.kit.common.ui.activities.BaseActivity;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.common.ui.widgets.BackTitleBar;
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant;

/**
 * History message search page for Team chat search history message and jump back to the team chat
 * page
 */
public class ChatSearchBaseActivity extends BaseActivity {
  private static final String TAG = "ChatSearchActivity";
  protected RecyclerView searchRV;

  protected BackTitleBar messageSearchTitleBar;

  protected EditText searchET;
  protected View clearIV;

  protected View emptyLayout;

  protected SearchMessageViewModel viewModel;
  protected SearchMessageAdapter searchAdapter;
  protected Team team;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    initViewAndSetContentView(savedInstanceState);
    bindingView();
    initData();
  }

  protected void bindingView() {
    if (searchRV != null) {
      LinearLayoutManager layoutManager = new LinearLayoutManager(this);
      searchRV.setLayoutManager(layoutManager);
      searchAdapter = new SearchMessageAdapter();
      searchRV.setAdapter(searchAdapter);
    }

    if (messageSearchTitleBar != null) {
      messageSearchTitleBar.setOnBackIconClickListener(v -> onBackPressed());
    }

    if (clearIV != null) {
      clearIV.setOnClickListener(v -> searchET.setText(""));
    }

    if (searchET != null) {
      searchET.addTextChangedListener(
          new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
              viewModel.searchMessage(String.valueOf(s), SessionTypeEnum.Team, team.getId());
              if (clearIV != null) {
                if (TextUtils.isEmpty(String.valueOf(s))) {
                  clearIV.setVisibility(View.GONE);
                } else {
                  clearIV.setVisibility(View.VISIBLE);
                }
              }
            }
          });
    }
  }

  protected void initViewAndSetContentView(@Nullable Bundle savedInstanceState) {}

  protected void initData() {
    viewModel = new ViewModelProvider(this).get(SearchMessageViewModel.class);
    team = (Team) getIntent().getSerializableExtra(RouterConstant.CHAT_KRY);

    if (team == null) {
      finish();
    }

    viewModel
        .getSearchLiveData()
        .observe(
            this,
            result -> {
              if (result.getLoadStatus() == LoadStatus.Success) {
                if ((result.getData() == null || result.getData().size() < 1)
                    && !TextUtils.isEmpty(String.valueOf(searchET.getEditableText()))) {
                  showEmpty(true);
                } else {
                  showEmpty(false);
                }
                searchAdapter.setData(result.getData());
              }
            });
  }

  protected void showEmpty(boolean show) {
    if (show) {
      emptyLayout.setVisibility(View.VISIBLE);
      searchRV.setVisibility(View.GONE);
    } else {
      emptyLayout.setVisibility(View.GONE);
      searchRV.setVisibility(View.VISIBLE);
    }
  }
}
