// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.fun.view.ait;

import static com.google.android.material.R.id.design_bottom_sheet;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.databinding.FunChatMessageAitSelectorDialogBinding;
import com.netease.yunxin.kit.chatkit.ui.fun.page.adapter.FunAitContactAdapter;
import com.netease.yunxin.kit.chatkit.ui.model.ait.AitUserInfo;
import com.netease.yunxin.kit.chatkit.ui.view.ait.AitContactSelector;
import com.netease.yunxin.kit.common.utils.ScreenUtils;
import com.netease.yunxin.kit.common.utils.SizeUtils;
import java.util.List;

/** Team member @ Dialog */
public class FunAitContactSelectorDialog extends BottomSheetDialog implements AitContactSelector {
  private final FunChatMessageAitSelectorDialogBinding binding;
  private FunAitContactAdapter adapter;
  private AitContactSelector.ItemListener listener;

  private LinearLayoutManager layoutManager;

  public FunAitContactSelectorDialog(@NonNull Context context) {
    this(context, R.style.TransBottomSheetTheme);
  }

  public FunAitContactSelectorDialog(@NonNull Context context, int themeResId) {
    super(context, themeResId);
    binding =
        FunChatMessageAitSelectorDialogBinding.inflate(
            LayoutInflater.from(getContext()), null, false);
    setContentView(
        binding.getRoot(),
        new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ScreenUtils.getDisplayHeight() * 9 / 10));

    setCanceledOnTouchOutside(true);
    initViews();
  }

  @Override
  public void show() {
    binding.contactSearch.setText("");
    binding.contactList.stopScroll();
    layoutManager.scrollToPositionWithOffset(0, 0);
    super.show();
  }

  @Override
  protected void onStart() {
    super.onStart();
    View bottomSheet = findViewById(design_bottom_sheet);
    if (bottomSheet != null) {
      BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
      behavior.setSkipCollapsed(true);
      behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }
  }

  private void initViews() {
    binding.contactArrowIcon.setOnClickListener(v -> dismiss());
    binding.ivClear.setOnClickListener(v -> binding.contactSearch.setText(null));
    layoutManager = new LinearLayoutManager(getContext());
    binding.contactList.setLayoutManager(layoutManager);
    adapter = new FunAitContactAdapter();
    adapter.setAitContactConfig(
        new FunAitContactAdapter.AitContactConfig(
            SizeUtils.dp2px(4),
            ContextCompat.getColor(getContext(), R.color.color_222222),
            R.drawable.ic_chat_at_all_avatar));
    adapter.setOnItemListener(
        item -> {
          if (listener != null) {
            listener.onSelect(item);
          }
          dismiss();
        });
    binding.contactList.setAdapter(adapter);
    binding.contactSearch.addTextChangedListener(
        new TextWatcher() {
          @Override
          public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

          @Override
          public void onTextChanged(CharSequence s, int start, int before, int count) {
            adapter.filter(s == null ? "" : s.toString());
            binding.ivClear.setVisibility(s == null || s.length() == 0 ? View.GONE : View.VISIBLE);
            updateEmptyState();
          }

          @Override
          public void afterTextChanged(Editable s) {}
        });
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
            if (dx != 0 || dy != 0) {
              hideKeyboard();
            }
          }
        });
  }

  private void updateEmptyState() {
    boolean empty = binding.contactSearch.length() > 0 && adapter.getItemCount() == 0;
    binding.emptyLayout.setVisibility(empty ? View.VISIBLE : View.GONE);
  }

  private void hideKeyboard() {
    InputMethodManager inputMethodManager =
        (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
    if (inputMethodManager != null) {
      inputMethodManager.hideSoftInputFromWindow(binding.contactSearch.getWindowToken(), 0);
    }
  }

  @Override
  public void setData(List<AitUserInfo> data, boolean refresh, boolean showAll) {
    adapter.setShowAll(showAll);
    adapter.setMembers(data);
    if (refresh) {
      adapter.notifyDataSetChanged();
    }
    updateEmptyState();
  }

  @Override
  public void setAllowAISearch(boolean allow) {
    adapter.setAllowAISearch(allow);
  }

  @Override
  public void addData(List<AitUserInfo> data) {
    adapter.addMembers(data);
    updateEmptyState();
  }

  @Override
  public void setOnItemListener(AitContactSelector.ItemListener listener) {
    this.listener = listener;
  }
}
