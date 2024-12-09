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
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.netease.nimlib.sdk.v2.conversation.enums.V2NIMConversationType;
import com.netease.nimlib.sdk.v2.team.model.V2NIMTeam;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.page.adapter.SearchMessageAdapter;
import com.netease.yunxin.kit.chatkit.ui.page.viewmodel.SearchMessageViewModel;
import com.netease.yunxin.kit.chatkit.utils.ChatKitConstant;
import com.netease.yunxin.kit.common.ui.activities.BaseLocalActivity;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.common.ui.widgets.BackTitleBar;
import com.netease.yunxin.kit.common.utils.NetworkUtils;
import com.netease.yunxin.kit.corekit.im2.utils.RouterConstant;

/**
 * History message search page for Team chat search history message and jump back to the team chat
 * page
 */
public class ChatSearchBaseActivity extends BaseLocalActivity {
  private static final String TAG = "ChatSearchActivity";
  protected RecyclerView searchRV;

  protected BackTitleBar messageSearchTitleBar;

  protected EditText searchET;
  protected View clearIV;

  protected View emptyLayout;

  protected SearchMessageViewModel viewModel;
  protected SearchMessageAdapter searchAdapter;
  protected V2NIMTeam team;

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
              if (TextUtils.isEmpty(s)) {
                //如果为空，清空搜索结果
                viewModel.searchMessage(
                    "", V2NIMConversationType.V2NIM_CONVERSATION_TYPE_TEAM, team.getTeamId());
              }
              if (clearIV != null) {
                if (TextUtils.isEmpty(String.valueOf(s))) {
                  clearIV.setVisibility(View.GONE);
                } else {
                  clearIV.setVisibility(View.VISIBLE);
                }
              }
            }
          });
      searchET.setOnEditorActionListener(
          (textView, i, keyEvent) -> {
            if (!NetworkUtils.isConnected()) {
              ToastX.showShortToast(R.string.chat_network_error_tip);
              return false;
            }
            String searchKey = String.valueOf(searchET.getEditableText());
            viewModel.searchMessage(
                searchKey, V2NIMConversationType.V2NIM_CONVERSATION_TYPE_TEAM, team.getTeamId());
            return true;
          });
    }
  }

  protected void initViewAndSetContentView(@Nullable Bundle savedInstanceState) {}

  protected void initData() {
    viewModel = new ViewModelProvider(this).get(SearchMessageViewModel.class);
    team = (V2NIMTeam) getIntent().getSerializableExtra(RouterConstant.CHAT_KRY);

    if (team == null) {
      finish();
    }

    viewModel
        .getSearchLiveData()
        .observe(
            this,
            result -> {
              if (result.getLoadStatus() == LoadStatus.Success) {
                showEmpty(
                    (result.getData() == null || result.getData().size() < 1)
                        && !TextUtils.isEmpty(String.valueOf(searchET.getEditableText())));
                searchAdapter.setData(result.getData());
              } else if (result.getLoadStatus() == LoadStatus.Error
                  && result.getError() != null
                  && result.getError().getCode() == ChatKitConstant.ERROR_CODE_PARAM_INVALID) {
                Toast.makeText(this, R.string.chat_team_error_tip_content, Toast.LENGTH_SHORT)
                    .show();
                finish();
              }
            });

    viewModel
        .getUserChangeLiveData()
        .observe(
            this,
            result -> {
              if (result.getLoadStatus() == LoadStatus.Success && result.getData() != null) {
                searchAdapter.updateUserList(result.getData());
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
