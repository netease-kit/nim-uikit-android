// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.page.adapter;

import android.text.TextUtils;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.netease.yunxin.kit.chatkit.ui.interfaces.ChatBaseViewHolder;
import com.netease.yunxin.kit.chatkit.ui.interfaces.IChatClickListener;
import com.netease.yunxin.kit.chatkit.ui.interfaces.IChatViewHolderFactory;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import java.util.ArrayList;
import java.util.List;

/** history message search adapter */
public class PinMessageAdapter extends RecyclerView.Adapter<ChatBaseViewHolder> {

  private final String TAG = "PinMessageAdapter";
  private final List<ChatMessageBean> dataList = new ArrayList<>();
  private IChatClickListener clickListener;
  private IChatViewHolderFactory viewHolderFactory;

  public void setData(List<ChatMessageBean> data) {
    dataList.clear();
    if (data != null) {
      dataList.addAll(data);
      notifyDataSetChanged();
    }
  }

  public void setViewHolderFactory(IChatViewHolderFactory factory) {
    this.viewHolderFactory = factory;
  }

  public void addForwardData(List<ChatMessageBean> data) {
    if (data != null) {
      dataList.addAll(0, data);
      notifyItemInserted(0);
    }
  }

  public void addData(List<ChatMessageBean> data) {
    if (data != null) {
      for (ChatMessageBean messageBean : data) {
        int insertIndex = 0;
        long time = messageBean.getMessageData().getMessage().getTime();
        for (int index = 0; index < dataList.size(); index++) {
          if (time > dataList.get(index).getMessageData().getMessage().getTime()) {
            break;
          }
          insertIndex++;
        }
        dataList.add(insertIndex, messageBean);
        notifyItemInserted(insertIndex);
      }
    }
  }

  public void appendData(List<ChatMessageBean> data) {
    if (data != null) {
      dataList.addAll(data);
    }
  }

  public void removeData(ChatMessageBean data) {
    if (data == null) {
      return;
    }
    int index = -1;
    for (int j = 0; j < dataList.size(); j++) {
      if (data.equals(dataList.get(j))) {
        index = j;
        break;
      }
    }
    if (index > -1) {
      removeData(index);
    }
  }

  public void removeDataWithUuId(String id) {
    if (TextUtils.isEmpty(id)) {
      return;
    }
    int index = -1;
    for (int j = 0; j < dataList.size(); j++) {
      if (TextUtils.equals(dataList.get(j).getMessageData().getMessage().getUuid(), id)) {
        index = j;
        break;
      }
    }
    if (index > -1) {
      removeData(index);
    }
  }

  public void removeData(int position) {
    if (position >= 0 && position < dataList.size()) {
      dataList.remove(position);
      notifyItemRemoved(position);
    }
  }

  public void setViewHolderClickListener(IChatClickListener listener) {
    this.clickListener = listener;
  }

  @Override
  public void onViewDetachedFromWindow(@NonNull ChatBaseViewHolder holder) {
    holder.onDetachedFromWindow();
    super.onViewDetachedFromWindow(holder);
  }

  @Override
  public void onViewAttachedToWindow(@NonNull ChatBaseViewHolder holder) {
    holder.onAttachedToWindow();
    super.onViewAttachedToWindow(holder);
  }

  @NonNull
  @Override
  public ChatBaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    ChatBaseViewHolder viewHolder = null;
    if (viewHolderFactory != null) {
      viewHolder = viewHolderFactory.createViewHolder(parent, viewType);
      if (viewHolder != null) {
        viewHolder.setChatOnClickListener(clickListener);
      }
    }
    return viewHolder;
  }

  @Override
  public void onBindViewHolder(@NonNull ChatBaseViewHolder holder, int position) {
    holder.onBindData(dataList.get(position), position);
  }

  @Override
  public void onBindViewHolder(
      @NonNull ChatBaseViewHolder holder, int position, @NonNull List<Object> payloads) {
    super.onBindViewHolder(holder, position, payloads);
    if (payloads.isEmpty()) {
      super.onBindViewHolder(holder, position, payloads);
    } else {
      holder.onBindData(dataList.get(position), position, payloads);
    }
  }

  @Override
  public int getItemCount() {
    return dataList.size();
  }

  public ChatMessageBean getData(int index) {
    if (index >= 0 && index < dataList.size()) {
      return dataList.get(index);
    }
    return null;
  }

  @Override
  public int getItemViewType(int position) {
    ChatMessageBean messageBean = getData(position);
    if (messageBean != null) {
      return messageBean.getViewType();
    }
    return 0;
  }
}
