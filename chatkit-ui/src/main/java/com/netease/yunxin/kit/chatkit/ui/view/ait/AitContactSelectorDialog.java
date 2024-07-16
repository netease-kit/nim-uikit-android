// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.view.ait;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatMessageAitSelectorDialogBinding;
import com.netease.yunxin.kit.chatkit.ui.model.ait.AitUserInfo;
import com.netease.yunxin.kit.chatkit.ui.page.adapter.AitContactAdapter;
import com.netease.yunxin.kit.common.utils.ScreenUtils;
import com.netease.yunxin.kit.common.utils.SizeUtils;
import java.util.List;

/** Team member @ Dialog */
public class AitContactSelectorDialog extends BottomSheetDialog {
  private final ChatMessageAitSelectorDialogBinding binding;
  private AitContactAdapter adapter;
  private ItemListener listener;

  private LinearLayoutManager layoutManager;

  //展示风格，0:办公风格 1:新版本
  private int uiStyle = 0;

  public AitContactSelectorDialog(@NonNull Context context) {
    this(context, R.style.TransBottomSheetTheme);
  }

  public AitContactSelectorDialog(@NonNull Context context, int themeResId) {
    super(context, themeResId);
    binding =
        ChatMessageAitSelectorDialogBinding.inflate(LayoutInflater.from(getContext()), null, false);
    setContentView(
        binding.getRoot(),
        new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ScreenUtils.getDisplayHeight() * 2 / 3));

    setCanceledOnTouchOutside(true);
    initViews();
  }

  public void setUIStyle(int style) {
    uiStyle = style;
    switchStyle();
  }

  private void initViews() {
    binding.contactArrowIcon.setOnClickListener(v -> dismiss());
    layoutManager = new LinearLayoutManager(getContext());
    binding.contactList.setLayoutManager(layoutManager);
    adapter = new AitContactAdapter();
    adapter.setOnItemListener(
        item -> {
          if (listener != null) {
            listener.onSelect(item);
          }
          dismiss();
        });
    binding.contactList.setAdapter(adapter);
    binding.contactList.addOnScrollListener(
        new RecyclerView.OnScrollListener() {
          @Override
          public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
              int position = layoutManager.findLastVisibleItemPosition();
              if (listener != null && adapter.getItemCount() < position + 5) {
                listener.onLoadMore();
              }
            }
          }

          @Override
          public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
          }
        });
  }

  private void switchStyle() {
    if (uiStyle == 0) {
      binding.getRoot().setBackgroundResource(R.color.color_white);
      adapter.setAitContactConfig(
          new AitContactAdapter.AitContactConfig(
              SizeUtils.dp2px(30),
              getContext().getResources().getColor(R.color.color_333333),
              R.drawable.ic_team_all));
    } else {
      binding.getRoot().setBackgroundResource(R.color.color_ededed);
      adapter.setAitContactConfig(
          new AitContactAdapter.AitContactConfig(
              SizeUtils.dp2px(4),
              getContext().getResources().getColor(R.color.color_222222),
              R.drawable.ic_chat_at_all_avatar));
    }
  }

  public void setData(List<AitUserInfo> data, boolean refresh) {
    adapter.setMembers(data);
    if (refresh) {
      adapter.notifyDataSetChanged();
    }
  }

  public void addData(List<AitUserInfo> data) {
    adapter.addMembers(data);
    adapter.notifyItemRangeInserted(adapter.getItemCount() - data.size(), data.size());
  }

  public void setData(List<AitUserInfo> data, boolean refresh, boolean showAll) {
    adapter.setShowAll(showAll);
    adapter.setMembers(data);
    if (refresh) {
      adapter.notifyDataSetChanged();
    }
  }

  public void setOnItemListener(ItemListener listener) {
    this.listener = listener;
  }

  public static class ItemListener {
    public void onSelect(AitUserInfo item) {}

    public void onLoadMore() {}
  }
}
