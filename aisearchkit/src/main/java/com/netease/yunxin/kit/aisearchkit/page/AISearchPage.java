// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.aisearchkit.page;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.netease.yunxin.kit.aisearchkit.R;
import com.netease.yunxin.kit.aisearchkit.databinding.AiSearchPageLayoutBinding;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.common.ui.activities.BaseActivity;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import com.netease.yunxin.kit.common.utils.NetworkUtils;

public class AISearchPage extends BaseActivity {

  private static final String TAG = "AISearchPage";

  AiSearchPageLayoutBinding binding;

  private String searchKey;

  public static String AI_SEARCH_KEY = "ai_search_key";

  AISearchViewModel viewModel;

  private AIMessageAdapter adapter;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = AiSearchPageLayoutBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());
    initData();
    initView();
  }

  /** 初始化数据 */
  private void initData() {
    searchKey = getIntent().getStringExtra(AI_SEARCH_KEY);
    ALog.d(TAG, "search key: " + searchKey);
    viewModel = new ViewModelProvider(this).get(AISearchViewModel.class);
    if (!TextUtils.isEmpty(searchKey)) {
      viewModel.aiSearch(searchKey);
    }
    viewModel
        .getSearchResult()
        .observe(
            this,
            s -> {
              ALog.d("search result: " + s);
              adapter.addMessage(s);
              binding.aiSearchRecycler.scrollToPosition(0);
            });

    viewModel
        .getIsLoadingLiveData()
        .observe(
            this,
            isLoading -> {
              if (isLoading) {
                binding.aiSearchProgress.setVisibility(View.VISIBLE);
                binding.aiSearchTitle.setText(R.string.ai_searching);
              } else {
                binding.aiSearchProgress.setVisibility(View.GONE);
                binding.aiSearchTitle.setText(R.string.ai_search);
              }
            });
  }

  /** 初始化视图 */
  private void initView() {
    //设置消息展示的适配器
    binding.aiSearchRecycler.setLayoutManager(new LinearLayoutManager(this));
    adapter = new AIMessageAdapter();
    binding.aiSearchRecycler.setAdapter(adapter);
    //空白点击直接关闭页面
    binding.blankView.setOnClickListener(
        v -> {
          finish();
        });
    //设置标题栏
    binding.aiSearchCancel.setOnClickListener(
        v -> {
          finish();
        });

    binding.aiSearchExtend.setOnClickListener(
        v -> {
          // 点击事件
          LinearLayout.LayoutParams params =
              (LinearLayout.LayoutParams) binding.aiSearchLayout.getLayoutParams();
          params.weight = 7;
          binding.aiSearchLayout.setLayoutParams(params);
          binding.aiSearchExtend.setVisibility(ViewGroup.GONE);
          binding.aiSearchInputLayout.setVisibility(View.VISIBLE);

          binding.blankView.setBackgroundColor(getResources().getColor(R.color.color_80000000));
          changeStatusBarColor(R.color.color_80000000);
        });

    binding.tvConfirm.setOnClickListener(
        v -> {
          // 确认搜索
          searchKey = binding.aiSearchEdit.getText().toString();
          if (NetworkUtils.isConnected()) {
            viewModel.aiSearch(searchKey);
            binding.aiSearchEdit.setText("");
          } else {
            ToastX.showShortToast(R.string.ai_search_network_error_tip);
          }
        });

    binding.aiSearchEdit.addTextChangedListener(
        new TextWatcher() {
          @Override
          public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

          @Override
          public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

          @Override
          public void afterTextChanged(Editable editable) {
            if (editable != null && TextUtils.isEmpty(editable.toString())) {
              binding.tvConfirm.setEnabled(false);
              binding.tvConfirm.setAlpha(0.5f);
            } else {
              binding.tvConfirm.setEnabled(true);
              binding.tvConfirm.setAlpha(1f);
            }
          }
        });
  }
}
