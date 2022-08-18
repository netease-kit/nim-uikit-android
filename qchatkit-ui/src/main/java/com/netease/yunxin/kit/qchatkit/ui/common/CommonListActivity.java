// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.qchatkit.ui.common;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.netease.yunxin.kit.common.ui.activities.BaseActivity;
import com.netease.yunxin.kit.qchatkit.ui.R;
import com.netease.yunxin.kit.qchatkit.ui.databinding.QChatCommonListActivityBinding;
import com.netease.yunxin.kit.qchatkit.ui.model.QChatBaseBean;
import java.util.List;

public abstract class CommonListActivity extends BaseActivity
    implements CommonViewHolderFactory<QChatBaseBean> {

  protected QChatCommonListActivityBinding viewBinding;
  protected CommonRecyclerViewAdapter recyclerViewAdapter;
  private final int LOAD_MORE_DIFF = 4;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    changeStatusBarColor(R.color.color_eff1f4);
    viewBinding = QChatCommonListActivityBinding.inflate(LayoutInflater.from(this), null, false);
    setContentView(viewBinding.getRoot());
    initView();
    LinearLayoutManager layoutManager = new LinearLayoutManager(this);
    layoutManager.setOrientation(RecyclerView.VERTICAL);
    viewBinding.commonActRecyclerView.setLayoutManager(layoutManager);
    recyclerViewAdapter = new CommonRecyclerViewAdapter();
    recyclerViewAdapter.setViewHolderFactory(CommonListActivity.this);
    viewBinding.commonActRecyclerView.setAdapter(recyclerViewAdapter);
    viewBinding.commonActTitleView.setOnBackIconClickListener(view -> finish());
    viewBinding.commonActTitleView.setActionListener(this::onTitleActionClick);
    viewBinding.commonActRecyclerView.addOnScrollListener(
        new RecyclerView.OnScrollListener() {
          @Override
          public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
              int position = layoutManager.findLastVisibleItemPosition();
              if (isLoadMore() && recyclerViewAdapter.getItemCount() < position + LOAD_MORE_DIFF) {
                QChatBaseBean last =
                    recyclerViewAdapter.getData(recyclerViewAdapter.getItemCount() - 1);
                loadMore(last);
              }
            }
          }

          @Override
          public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
          }
        });
    initData();
    viewBinding.commonActTitleView.setTitle(getTitleText());
  }

  public void setBackgroundColor(@ColorRes int color) {
    viewBinding.commonLayout.setBackgroundColor(getResources().getColor(color));
  }

  public void showEmptyView(String title, boolean show) {
    if (show) {
      viewBinding.commonEmptyLayout.setVisibility(View.VISIBLE);
      viewBinding.commonEmptyTv.setText(title);
      viewBinding.commonActRecyclerView.setVisibility(View.GONE);
    } else {
      viewBinding.commonActRecyclerView.setVisibility(View.VISIBLE);
      viewBinding.commonEmptyLayout.setVisibility(View.GONE);
    }
  }

  public void initView() {}

  public void initData() {}

  public void setData(List<? extends QChatBaseBean> data) {
    recyclerViewAdapter.setData(data);
  }

  public void addData(List<? extends QChatBaseBean> data) {
    recyclerViewAdapter.addData(data);
  }

  public void addData(int index, List<? extends QChatBaseBean> data) {
    recyclerViewAdapter.addData(index, data);
  }

  public void removeData(int index) {
    recyclerViewAdapter.removeData(index);
  }

  public abstract void onTitleActionClick(View view);

  public abstract String getTitleText();

  public abstract boolean isLoadMore();

  public void loadMore(QChatBaseBean bean) {}
}
