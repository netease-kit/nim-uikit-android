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
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.netease.yunxin.kit.chatkit.model.UserInfoWithTeam;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatMessageAitSelectorDialogBinding;
import com.netease.yunxin.kit.chatkit.ui.page.adapter.AitContactAdapter;
import com.netease.yunxin.kit.common.utils.ScreenUtils;
import java.util.List;

/** Team member @ Dialog */
public class AitContactSelectorDialog extends BottomSheetDialog {
  private final ChatMessageAitSelectorDialogBinding binding;
  private AitContactAdapter adapter;
  private AitContactAdapter.OnItemSelectListener listener;

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

  private void initViews() {
    binding.contactArrowIcon.setOnClickListener(v -> dismiss());
    binding.contactList.setLayoutManager(new LinearLayoutManager(getContext()));
    adapter = new AitContactAdapter();
    adapter.setOnItemSelectListener(
        item -> {
          if (listener != null) {
            listener.onSelect(item);
          }
          dismiss();
        });
    binding.contactList.setAdapter(adapter);
  }

  public void setData(List<UserInfoWithTeam> data) {
    adapter.setMembers(data);
  }

  public void setOnItemSelectListener(AitContactAdapter.OnItemSelectListener listener) {
    this.listener = listener;
  }
}
