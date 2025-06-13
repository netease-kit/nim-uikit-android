// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.normal.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatAiHelperItemLayoutBinding;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatMessageAiHelperViewBinding;
import java.util.ArrayList;
import java.util.List;

public class AIHelperView extends FrameLayout {

  private AIHelperClickListener clickListener;
  private ChatMessageAiHelperViewBinding viewBinding;
  private AIHelperAdapter adapter;

  private boolean isRefresh = false;

  public AIHelperView(@NonNull Context context) {
    this(context, null);
  }

  public AIHelperView(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public AIHelperView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    this(context, attrs, defStyleAttr, 0);
  }

  public AIHelperView(
      @NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    initView(context);
  }

  protected void initView(Context context) {
    viewBinding = ChatMessageAiHelperViewBinding.inflate(LayoutInflater.from(context), this, true);
    viewBinding.rightIcon.setOnClickListener(
        v -> {
          if (clickListener != null && !isRefresh) {
            isRefresh = true;
            playLoading(true);
            clickListener.onRefreshClick();
          }
        });
    viewBinding.errorButton.setOnClickListener(
        v -> {
          if (clickListener != null && !isRefresh) {
            isRefresh = true;
            playLoading(true);
            clickListener.onTryAgainClick();
          }
        });
    adapter = new AIHelperAdapter();
    viewBinding.rowsContainerRecycler.setLayoutManager(new LinearLayoutManager(context));
    viewBinding.rowsContainerRecycler.setAdapter(adapter);
  }

  public void setLeftImageResource(int resId) {
    viewBinding.leftIcon.setImageResource(resId);
  }

  public void setLottieAnimationFile(String animationFile) {
    viewBinding.aiLoadingLv.setAnimation(animationFile);
  }

  public void show() {
    this.setVisibility(VISIBLE);
    if (adapter != null && adapter.contentList.size() > 0) {
      showContent();
    } else {
      playLoading(true);
    }
    if (clickListener != null) {
      clickListener.onShow();
    }
  }

  public void hide() {
    this.setVisibility(GONE);
    playLoading(false);
  }

  public void showLoading() {
    playLoading(true);
  }

  public void showContent() {
    playLoading(false);
    viewBinding.errorContainer.setVisibility(View.GONE);
    viewBinding.rowsContainerRecycler.setVisibility(VISIBLE);
  }

  public List<AIHelperItem> getHelperItemList() {
    if (adapter == null) {
      return new ArrayList<>();
    }
    return adapter.contentList;
  }

  public void setHelperContent(List<AIHelperItem> contentList) {
    isRefresh = false;
    if (adapter == null) {
      return;
    }
    playLoading(false);
    viewBinding.errorContainer.setVisibility(View.GONE);
    viewBinding.rightIcon.setVisibility(View.VISIBLE);
    viewBinding.rowsContainerRecycler.setVisibility(VISIBLE);
    adapter.setData(contentList);
  }

  public void showError(String errorText) {
    isRefresh = false;
    playLoading(false);
    viewBinding.errorContainer.setVisibility(View.VISIBLE);
    viewBinding.rightIcon.setVisibility(View.GONE);
    if (!TextUtils.isEmpty(errorText)) {
      viewBinding.errorText.setText(errorText);
    }
  }

  public void playLoading(boolean play) {
    if (play) {
      viewBinding.errorContainer.setVisibility(View.GONE);
      viewBinding.rowsContainerRecycler.setVisibility(View.GONE);
      viewBinding.rightIcon.setVisibility(GONE);
      viewBinding.loadingContainer.setVisibility(View.VISIBLE);
      viewBinding.aiLoadingLv.playAnimation();
    } else {
      viewBinding.aiLoadingLv.cancelAnimation();
      viewBinding.rightIcon.setVisibility(VISIBLE);
      viewBinding.loadingContainer.setVisibility(View.GONE);
    }
  }

  public void setAIHelperClickListener(AIHelperClickListener listener) {
    clickListener = listener;
    if (adapter != null) {
      adapter.setClickListener(listener);
    }
  }

  public static class AIHelperAdapter extends RecyclerView.Adapter<AIHelperViewHolder> {
    private final List<AIHelperItem> contentList = new ArrayList<>();
    private AIHelperClickListener clickListener;

    public void setData(List<AIHelperItem> content) {
      contentList.clear();
      contentList.addAll(content);
      notifyDataSetChanged();
    }

    public void setClickListener(AIHelperClickListener listener) {
      clickListener = listener;
    }

    @NonNull
    @Override
    public AIHelperViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
      ChatAiHelperItemLayoutBinding binding =
          ChatAiHelperItemLayoutBinding.inflate(
              LayoutInflater.from(parent.getContext()), parent, false);
      return new AIHelperViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AIHelperViewHolder holder, int position) {
      holder.bindData(position, contentList.get(position));
      holder.setClickListener(clickListener);
    }

    @Override
    public int getItemCount() {
      return contentList.size();
    }
  }

  public static class AIHelperViewHolder extends RecyclerView.ViewHolder {

    private final ChatAiHelperItemLayoutBinding binding;
    private AIHelperClickListener clickListener;

    public AIHelperViewHolder(@NonNull ViewGroup itemView) {
      super(itemView);
      binding =
          ChatAiHelperItemLayoutBinding.inflate(
              LayoutInflater.from(itemView.getContext()), itemView, false);
    }

    public AIHelperViewHolder(@NonNull ChatAiHelperItemLayoutBinding itemView) {
      super(itemView.getRoot());
      binding = itemView;
    }

    public void setClickListener(AIHelperClickListener listener) {
      clickListener = listener;
    }

    public void bindData(int position, AIHelperItem item) {

      binding.aiHelperItemText.setText(item.content);
      if (!TextUtils.isEmpty(item.tag)) {
        binding.aiHelperItemTag.setText(item.tag);
      }
      if (item.tagBgDrawable != 0) {
        binding.aiHelperItemTag.setBackgroundResource(item.tagBgDrawable);
      }
      if (item.tagTextColor != 0) {
        binding.aiHelperItemTag.setTextColor(
            binding.getRoot().getContext().getResources().getColor(item.tagTextColor));
      }
      binding.aiHelperItemEdit.setOnClickListener(
          v -> {
            if (clickListener != null) {
              clickListener.onItemEditClick(item);
            }
          });
      binding.aiHelperItemLayout.setOnClickListener(
          v -> {
            if (clickListener != null) {
              clickListener.onItemClick(item);
            }
          });
    }
  }

  public static class AIHelperItem {
    public String content;
    public String tag;
    public int tagBgDrawable = R.drawable.bg_corner_pink2;
    public int tagTextColor = R.color.color_e75257;

    public AIHelperItem(String content, String tag, int tagColor, int tagTextColor) {
      this.content = content;
      this.tag = tag;
      this.tagBgDrawable = tagColor;
      this.tagTextColor = tagTextColor;
    }

    public AIHelperItem(String content, String tag) {
      this.content = content;
      this.tag = tag;
    }
  }

  public interface AIHelperClickListener {
    void onShow();

    void onRefreshClick();

    void onTryAgainClick();

    void onItemClick(AIHelperItem content);

    void onItemEditClick(AIHelperItem content);
  }
}
