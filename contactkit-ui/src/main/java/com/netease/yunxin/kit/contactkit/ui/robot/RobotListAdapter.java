// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.robot;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import com.netease.yunxin.kit.common.ui.widgets.ContactAvatarView;
import com.netease.yunxin.kit.contactkit.ui.R;
import com.netease.yunxin.kit.contactkit.ui.model.RobotInfoBean;
import com.netease.yunxin.kit.contactkit.ui.utils.ColorUtils;
import java.util.ArrayList;
import java.util.List;

/** 机器人列表 Adapter，通过 itemLayoutResId 支持 normal/fun 两套不同 item 布局 */
public class RobotListAdapter extends RecyclerView.Adapter<RobotListAdapter.ViewHolder> {

  private final int itemLayoutResId;
  private final List<RobotInfoBean> dataList = new ArrayList<>();

  @Nullable private OnItemClickListener itemClickListener;

  public RobotListAdapter(int itemLayoutResId) {
    this.itemLayoutResId = itemLayoutResId;
  }

  public void setData(List<RobotInfoBean> data) {
    dataList.clear();
    if (data != null) {
      dataList.addAll(data);
    }
    notifyDataSetChanged();
  }

  public List<RobotInfoBean> getData() {
    return dataList;
  }

  public void setItemClickListener(OnItemClickListener listener) {
    this.itemClickListener = listener;
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext()).inflate(itemLayoutResId, parent, false);
    return new ViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    holder.bind(dataList.get(position), itemClickListener);
  }

  @Override
  public int getItemCount() {
    return dataList.size();
  }

  static class ViewHolder extends RecyclerView.ViewHolder {

    private final ContactAvatarView avatarView;
    private final TextView tvName;

    ViewHolder(View itemView) {
      super(itemView);
      avatarView = itemView.findViewById(R.id.avatar_view);
      tvName = itemView.findViewById(R.id.tv_name);
    }

    void bind(RobotInfoBean bean, @Nullable OnItemClickListener listener) {
      tvName.setText(bean.getName());
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
