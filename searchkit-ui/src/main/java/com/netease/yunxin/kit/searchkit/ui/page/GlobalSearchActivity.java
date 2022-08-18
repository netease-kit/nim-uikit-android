// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.searchkit.ui.page;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.netease.yunxin.kit.common.ui.activities.BaseActivity;
import com.netease.yunxin.kit.common.ui.viewholder.BaseBean;
import com.netease.yunxin.kit.common.ui.viewholder.ViewHolderClickListener;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import com.netease.yunxin.kit.searchkit.ui.databinding.GlobalSearchActivityBinding;

/** search your friend or team */
public class GlobalSearchActivity extends BaseActivity {

  private GlobalSearchActivityBinding viewBinding;
  private SearchViewModel viewModel;
  private SearchAdapter searchAdapter;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    viewBinding = GlobalSearchActivityBinding.inflate(getLayoutInflater());
    setContentView(viewBinding.getRoot());
    initView();
    initData();
  }

  private void initView() {
    LinearLayoutManager layoutManager = new LinearLayoutManager(this);
    viewBinding.rvSearch.setLayoutManager(layoutManager);
    searchAdapter = new SearchAdapter();
    searchAdapter.setViewHolderFactory(new SearchViewHolderFactory());
    searchAdapter.setViewHolderClickListener(
        new ViewHolderClickListener() {
          @Override
          public boolean onClick(BaseBean data, int position) {
            if (!TextUtils.isEmpty(data.router)) {
              XKitRouter.withKey(data.router)
                  .withParam(data.paramKey, data.param)
                  .withContext(GlobalSearchActivity.this)
                  .navigate();
            }
            return true;
          }

          @Override
          public boolean onLongClick(BaseBean data, int position) {
            return false;
          }
        });
    viewBinding.rvSearch.setAdapter(searchAdapter);
    viewBinding.globalTitleBar.setOnBackIconClickListener(
        v -> {
          onBackPressed();
        });
    viewBinding.ivClear.setOnClickListener(
        v -> {
          viewBinding.etSearch.setText("");
        });
    viewBinding.etSearch.addTextChangedListener(
        new TextWatcher() {
          @Override
          public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

          @Override
          public void onTextChanged(CharSequence s, int start, int before, int count) {}

          @Override
          public void afterTextChanged(Editable s) {
            viewModel.query(String.valueOf(s));
            if (TextUtils.isEmpty(String.valueOf(s))) {
              viewBinding.ivClear.setVisibility(View.GONE);
            } else {
              viewBinding.ivClear.setVisibility(View.VISIBLE);
            }
          }
        });
  }

  private void initData() {

    viewModel = new ViewModelProvider(this).get(SearchViewModel.class);
    viewModel
        .getQueryLiveData()
        .observe(
            this,
            result -> {
              if (result.getLoadStatus() == LoadStatus.Success) {
                if ((result.getData() == null || result.getData().size() < 1)
                    && !TextUtils.isEmpty(String.valueOf(viewBinding.etSearch.getEditableText()))) {
                  showEmpty(true);
                } else {
                  showEmpty(false);
                }
                searchAdapter.setData(result.getData());
              }
            });
  }

  private void showEmpty(boolean show) {
    if (show) {
      viewBinding.emptyLl.setVisibility(View.VISIBLE);
      viewBinding.rvSearch.setVisibility(View.GONE);
    } else {
      viewBinding.emptyLl.setVisibility(View.GONE);
      viewBinding.rvSearch.setVisibility(View.VISIBLE);
    }
  }
}
