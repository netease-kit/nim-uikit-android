// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.robot;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import com.netease.yunxin.kit.contactkit.ui.databinding.FunRobotBindItemLayoutBinding;
import com.netease.yunxin.kit.contactkit.ui.databinding.RobotBindItemLayoutBinding;
import com.netease.yunxin.kit.contactkit.ui.model.RobotInfoBean;
import com.netease.yunxin.kit.contactkit.ui.utils.ColorUtils;
import java.util.ArrayList;
import java.util.List;

/** 绑定机器人列表 Adapter */
public class RobotBindAdapter extends RecyclerView.Adapter<RobotBindAdapter.ViewHolder> {

  private final boolean funStyle;
  private final List<RobotInfoBean> dataList = new ArrayList<>();

  @Nullable private OnItemClickListener itemClickListener;

  public RobotBindAdapter() {
    this(false);
  }

  public RobotBindAdapter(boolean funStyle) {
    this.funStyle = funStyle;
  }

  public void setData(List<RobotInfoBean> data) {
    dataList.clear();
    if (data != null) {
      dataList.addAll(data);
    }
    notifyDataSetChanged();
  }

  public void setOnItemClickListener(@Nullable OnItemClickListener listener) {
    this.itemClickListener = listener;
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    LayoutInflater inflater = LayoutInflater.from(parent.getContext());
    if (funStyle) {
      FunRobotBindItemLayoutBinding binding =
          FunRobotBindItemLayoutBinding.inflate(inflater, parent, false);
      return new ViewHolder(
          binding.avatarView, binding.tvName, binding.tvSubtitle, binding.getRoot());
    } else {
      RobotBindItemLayoutBinding binding =
          RobotBindItemLayoutBinding.inflate(inflater, parent, false);
      return new ViewHolder(
          binding.avatarView, binding.tvName, binding.tvSubtitle, binding.getRoot());
    }
  }

  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    RobotInfoBean bean = dataList.get(position);
    holder.bind(bean, itemClickListener);
  }

  @Override
  public int getItemCount() {
    return dataList.size();
  }

  static class ViewHolder extends RecyclerView.ViewHolder {

    private final com.netease.yunxin.kit.common.ui.widgets.ContactAvatarView avatarView;
    private final android.widget.TextView tvName;
    private final android.widget.TextView tvSubtitle;

    ViewHolder(
        com.netease.yunxin.kit.common.ui.widgets.ContactAvatarView avatarView,
        android.widget.TextView tvName,
        android.widget.TextView tvSubtitle,
        View root) {
      super(root);
      this.avatarView = avatarView;
      this.tvName = tvName;
      this.tvSubtitle = tvSubtitle;
    }

    void bind(RobotInfoBean bean, @Nullable OnItemClickListener listener) {
      tvName.setText(bean.getName());

      // 副标题（可选）
      if (!TextUtils.isEmpty(bean.getSubtitle())) {
        tvSubtitle.setVisibility(View.VISIBLE);
        tvSubtitle.setText(bean.getSubtitle());
      } else {
        tvSubtitle.setVisibility(View.GONE);
      }

      // 头像
      avatarView.setData(
          bean.getAvatar(), bean.getName(), ColorUtils.avatarColor(bean.getAccountId()));

      itemView.setOnClickListener(
          v -> {
            if (listener != null) {
              listener.onItemClick(bean);
            }
          });
    }
  }

  public interface OnItemClickListener {
    void onItemClick(RobotInfoBean bean);
  }
}
