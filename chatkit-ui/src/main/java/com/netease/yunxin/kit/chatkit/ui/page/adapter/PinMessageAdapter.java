// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.page.adapter;

import static com.netease.yunxin.kit.chatkit.ui.view.input.ActionConstants.PAYLOAD_PROGRESS;

import android.text.TextUtils;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.netease.yunxin.kit.chatkit.ui.interfaces.ChatBaseViewHolder;
import com.netease.yunxin.kit.chatkit.ui.interfaces.IChatViewHolderFactory;
import com.netease.yunxin.kit.chatkit.ui.interfaces.IItemClickListener;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.corekit.coexist.im2.model.IMMessageProgress;
import java.util.ArrayList;
import java.util.List;

/** history message search adapter */
public class PinMessageAdapter extends RecyclerView.Adapter<ChatBaseViewHolder> {

  private final String TAG = "PinMessageAdapter";
  private final List<ChatMessageBean> dataList = new ArrayList<>();
  private IItemClickListener clickListener;
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
        long time = messageBean.getMessageData().getMessage().getCreateTime();
        for (int index = 0; index < dataList.size(); index++) {
          if (time > dataList.get(index).getMessageData().getMessage().getCreateTime()) {
            break;
          }
          insertIndex++;
        }
        dataList.add(insertIndex, messageBean);
        notifyItemInserted(insertIndex);
      }
    }
  }

  public void updateMessage(ChatMessageBean data, Object payload) {
    int index = getMessageIndex(data);
    if (index >= 0) {
      notifyItemChanged(index, payload);
    }
  }

  //更新用户信息
  public void updateUserList(List<String> accounts) {
    for (int i = 0; i < dataList.size(); i++) {
      if (accounts.contains(dataList.get(i).getSenderId())) {
        notifyItemChanged(i);
      }
    }
  }

  public int getMessageIndex(ChatMessageBean data) {
    for (int i = 0; i < dataList.size(); i++) {
      if (dataList.get(i).isSameMessage(data)) {
        return i;
      }
    }
    return -1;
  }

  public void appendData(List<ChatMessageBean> data) {
    if (data != null) {
      dataList.addAll(data);
    }
  }

  public void removeData(ChatMessageBean data) {
    if (data == null || TextUtils.isEmpty(data.getMsgClientId())) {
      return;
    }
    removeDataWithClientId(data.getMsgClientId());
  }

  public void removeDataWithClientIds(List<String> idList) {
    if (idList == null || idList.isEmpty()) {
      return;
    }
    int index = -1;
    for (String id : idList) {
      for (int j = 0; j < dataList.size(); j++) {
        if (TextUtils.equals(dataList.get(j).getMsgClientId(), id)) {
          index = j;
          break;
        }
      }
    }
    if (index > -1) {
      removeData(index);
    }
  }

  public void removeDataWithClientId(String id) {
    if (TextUtils.isEmpty(id)) {
      return;
    }
    int index = -1;
    for (int j = 0; j < dataList.size(); j++) {
      if (TextUtils.equals(dataList.get(j).getMsgClientId(), id)) {
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

  public void updateMessageProgress(IMMessageProgress progress) {
    ChatMessageBean messageBean = searchMessage(progress.getMessageCid());
    if (messageBean != null) {
      float pg = progress.getProgress();
      messageBean.progress = progress.getProgress();
      messageBean.setLoadProgress(pg);
      updateMessage(messageBean, PAYLOAD_PROGRESS);
    }
  }

  public ChatMessageBean searchMessage(String messageId) {
    for (int i = dataList.size() - 1; i >= 0; i--) {
      if (TextUtils.equals(
          messageId, dataList.get(i).getMessageData().getMessage().getMessageClientId())) {
        return dataList.get(i);
      }
    }
    return null;
  }

  public void setViewHolderClickListener(IItemClickListener listener) {
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
        viewHolder.setItemOnClickListener(clickListener);
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
    if (viewHolderFactory != null) {
      return viewHolderFactory.getItemViewType(getData(position));
    } else {
      ChatMessageBean messageBean = getData(position);
      if (messageBean != null) {
        return messageBean.getViewType();
      }
    }
    return 0;
  }
}
