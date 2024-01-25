// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.search.page;

import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.netease.yunxin.kit.common.ui.activities.BaseActivity;
import com.netease.yunxin.kit.common.ui.viewholder.BaseBean;
import com.netease.yunxin.kit.common.ui.viewholder.ViewHolderClickListener;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.common.utils.KeyboardUtils;
import com.netease.yunxin.kit.contactkit.ui.R;
import com.netease.yunxin.kit.contactkit.ui.model.SearchTeamBean;
import com.netease.yunxin.kit.contactkit.ui.search.SearchAdapter;
import com.netease.yunxin.kit.contactkit.ui.search.SearchViewModel;
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import java.util.Objects;

/** search your friend or team */
public abstract class BaseSearchActivity extends BaseActivity {
  protected RecyclerView searchRv;

  private View rootView;

  protected View clearView;

  protected EditText searchEditText;

  protected View emptyView;

  protected View backView;

  protected SearchViewModel viewModel;
  protected SearchAdapter searchAdapter;
  protected Handler searchHandler;

  protected String routerFriend = RouterConstant.PATH_CHAT_P2P_PAGE;
  protected String routerTeam = RouterConstant.PATH_CHAT_TEAM_PAGE;

  protected Boolean isQueryTeam = false;

  protected BaseBean queryTeamData;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    rootView = initViewAndGetRootView();
    checkViews();
    setContentView(rootView);
    bindView();
    initData();
    showKeyBoard();
  }

  protected abstract View initViewAndGetRootView();

  protected void checkViews() {
    Objects.requireNonNull(rootView);
    Objects.requireNonNull(clearView);
    Objects.requireNonNull(searchEditText);
    Objects.requireNonNull(emptyView);
    Objects.requireNonNull(backView);
  }

  protected void bindView() {
    if (searchRv != null) {
      LinearLayoutManager layoutManager = new LinearLayoutManager(this);
      searchRv.setLayoutManager(layoutManager);
      searchAdapter = new SearchAdapter();
      searchAdapter.setViewHolderClickListener(
          new ViewHolderClickListener() {
            @Override
            public boolean onClick(View v, BaseBean data, int position) {

              if (!TextUtils.isEmpty(data.router)) {
                if (data instanceof SearchTeamBean) {
                  SearchTeamBean searchTeamBean = (SearchTeamBean) data;
                  if (!isQueryTeam) {
                    queryTeamData = searchTeamBean;
                    viewModel.queryTeam(searchTeamBean.teamSearchInfo.getTeam().getId());
                    isQueryTeam = true;
                  }
                } else {
                  XKitRouter.withKey(data.router)
                      .withParam(data.paramKey, data.param)
                      .withContext(BaseSearchActivity.this)
                      .navigate();
                }
              }
              return true;
            }

            @Override
            public boolean onLongClick(View v, BaseBean data, int position) {
              return false;
            }
          });
      searchRv.setAdapter(searchAdapter);
    }

    if (clearView != null && searchEditText != null) {
      clearView.setOnClickListener(v -> searchEditText.setText(""));
    }

    if (searchEditText != null) {
      searchEditText.addTextChangedListener(
          new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
              searchHandler.removeCallbacksAndMessages(null);
              searchHandler.postDelayed(() -> viewModel.query(String.valueOf(s)), 500);
              if (clearView != null) {
                if (TextUtils.isEmpty(String.valueOf(s))) {
                  clearView.setVisibility(View.GONE);
                } else {
                  clearView.setVisibility(View.VISIBLE);
                }
              }
            }
          });

      searchEditText.setOnEditorActionListener(
          (v, actionId, event) -> event.getKeyCode() == KeyEvent.KEYCODE_ENTER);
    }
    if (backView != null) {
      backView.setOnClickListener(v -> onBackPressed());
    }
  }

  private void initData() {
    searchHandler = new Handler();
    viewModel = new ViewModelProvider(this).get(SearchViewModel.class);
    viewModel.setRouter(routerFriend, routerTeam);
    viewModel
        .getQueryLiveData()
        .observe(
            this,
            result -> {
              if (result.getLoadStatus() == LoadStatus.Success) {
                showEmpty(
                    (result.getData() == null || result.getData().size() < 1)
                        && !TextUtils.isEmpty(String.valueOf(searchEditText.getEditableText())));
                searchAdapter.setData(result.getData());
              }
            });

    viewModel
        .getQueryTeamLiveData()
        .observe(
            this,
            result -> {
              isQueryTeam = false;
              if (result.getLoadStatus() == LoadStatus.Success) {
                if (result.getData() != null
                    && result.getData().isMyTeam()
                    && queryTeamData != null) {
                  XKitRouter.withKey(queryTeamData.router)
                      .withParam(queryTeamData.paramKey, queryTeamData.param)
                      .withContext(BaseSearchActivity.this)
                      .navigate();
                } else {
                  showAlertDialog();
                }
              } else {
                Toast.makeText(this, R.string.contact_error_tips, Toast.LENGTH_SHORT).show();
              }
            });
  }

  private void showKeyBoard() {
    searchHandler.postDelayed(
        () -> {
          if (searchEditText != null) {
            searchEditText.requestFocus();
            KeyboardUtils.showKeyboard(searchEditText);
          }
        },
        300);
  }

  private void showEmpty(boolean show) {
    if (emptyView != null) {
      emptyView.setVisibility(show ? View.VISIBLE : View.GONE);
      if (searchRv != null) {
        searchRv.setVisibility(show ? View.GONE : View.VISIBLE);
      }
    }
  }

  protected void showAlertDialog() {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    LayoutInflater layoutInflater = LayoutInflater.from(this);
    View dialogView = layoutInflater.inflate(R.layout.contact_alert_dialog_layout, null);
    TextView title = dialogView.findViewById(R.id.tv_dialog_title);
    TextView content = dialogView.findViewById(R.id.tv_dialog_content);
    TextView positiveBut = dialogView.findViewById(R.id.tv_dialog_positive);
    content.setText(getString(R.string.contact_team_be_removed_content));
    title.setText(getString(R.string.contact_team_be_removed_title));
    positiveBut.setText(getString(R.string.selector_sure_without_num));
    // 设置不可取消
    builder.setCancelable(false);
    builder.setView(dialogView);
    final AlertDialog alertDialog = builder.create();
    positiveBut.setOnClickListener(
        v -> {
          if (alertDialog != null) {
            alertDialog.dismiss();
          }
          searchAdapter.removeData(queryTeamData);
        });

    alertDialog.show();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (searchHandler != null) {
      searchHandler.removeCallbacksAndMessages(null);
    }
  }
}
