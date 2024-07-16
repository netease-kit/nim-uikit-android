// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.view.ai;

import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatAiLanguageItemBinding;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatAiTranslateLanguageDialogBinding;
import com.netease.yunxin.kit.common.ui.dialog.BaseBottomDialog;
import java.util.ArrayList;
import java.util.List;

public class AiTranslateLanguageDialog extends BaseBottomDialog {

  private ChatAiTranslateLanguageDialogBinding viewBinding;
  private List<LanguageModel> itemList = new ArrayList<>();

  private OnLanguageSelectListener listener;

  public AiTranslateLanguageDialog(@NonNull List<LanguageModel> list) {
    super();
    itemList.addAll(list);
  }

  @Nullable
  @Override
  protected View getRootView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
    viewBinding = ChatAiTranslateLanguageDialogBinding.inflate(inflater, container, false);
    viewBinding.languageRv.setLayoutManager(new LinearLayoutManager(getContext()));
    LanguageAdapter adapter = new LanguageAdapter();
    adapter.setData(itemList);
    viewBinding.languageRv.setAdapter(adapter);
    viewBinding.closeIv.setOnClickListener(v -> dismiss());
    return viewBinding.getRoot();
  }

  public void setOnLanguageSelectListener(OnLanguageSelectListener listener) {
    this.listener = listener;
  }

  public class LanguageAdapter extends RecyclerView.Adapter<LanguageViewHolder> {

    private List<LanguageModel> itemList = new ArrayList<>();
    private int selectedPosition = -1;

    public void setData(List<LanguageModel> list) {
      itemList.clear();
      itemList.addAll(list);
    }

    @NonNull
    @Override
    public LanguageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
      ChatAiLanguageItemBinding itemBinding =
          ChatAiLanguageItemBinding.inflate(
              LayoutInflater.from(parent.getContext()), parent, false);
      return new LanguageViewHolder(itemBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull LanguageViewHolder holder, int position) {
      holder.bindData(itemList.get(position));
      holder.viewBinding.languageItemBtn.setChecked(itemList.get(position).isSelected);
      holder.viewBinding.languageItemBtn.setOnClickListener(
          v -> {
            if (selectedPosition != holder.getLayoutPosition()) {
              if (selectedPosition != -1) {
                itemList.get(selectedPosition).isSelected = false;
                notifyItemChanged(selectedPosition);
              }
              LanguageModel model = itemList.get(holder.getLayoutPosition());
              model.isSelected = true;
              notifyItemChanged(holder.getLayoutPosition());
              selectedPosition = holder.getLayoutPosition();
              if (listener != null) {
                listener.onLanguageSelect(model);
              }
              dismiss();
            }
          });
    }

    @Override
    public int getItemCount() {
      return itemList.size();
    }

    public String getSelectedPosition() {
      return itemList.get(selectedPosition).languageTag;
    }
  }

  public class LanguageViewHolder extends RecyclerView.ViewHolder {

    private final ChatAiLanguageItemBinding viewBinding;

    public LanguageViewHolder(ChatAiLanguageItemBinding itemView) {
      super(itemView.getRoot());
      viewBinding = itemView;
      // 设置按钮选择图标大小
      Drawable drawable = viewBinding.languageItemBtn.getCompoundDrawables()[2];
      int size =
          (int)
              TypedValue.applyDimension(
                  TypedValue.COMPLEX_UNIT_DIP, 14, getResources().getDisplayMetrics());
      drawable.setBounds(0, 0, size, size); // 设置Drawable的边界，即大小
      viewBinding.languageItemBtn.setCompoundDrawables(null, null, drawable, null);
    }

    public void bindData(LanguageModel item) {
      viewBinding.languageItemBtn.setText(item.language);
    }
  }

  public interface OnLanguageSelectListener {
    void onLanguageSelect(LanguageModel language);
  }
}
