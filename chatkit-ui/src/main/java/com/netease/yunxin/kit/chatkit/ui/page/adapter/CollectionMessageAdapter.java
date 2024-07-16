// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.page.adapter;

import static com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant.LIB_TAG;
import static com.netease.yunxin.kit.chatkit.ui.view.input.ActionConstants.PAYLOAD_PROGRESS;

import android.text.TextUtils;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.ui.interfaces.ChatBaseViewHolder;
import com.netease.yunxin.kit.chatkit.ui.interfaces.IChatViewHolderFactory;
import com.netease.yunxin.kit.chatkit.ui.interfaces.IItemClickListener;
import com.netease.yunxin.kit.chatkit.ui.model.CollectionBean;
import com.netease.yunxin.kit.corekit.im2.model.IMMessageProgress;
import java.util.ArrayList;
import java.util.List;

/** 收藏消息列表Adapter */
public class CollectionMessageAdapter extends RecyclerView.Adapter<ChatBaseViewHolder> {

  private final String TAG = "CollectionMessageAdapter";
  private final List<CollectionBean> dataList = new ArrayList<>();
  private IItemClickListener clickListener;

  private IChatViewHolderFactory viewHolderFactory;

  public void setData(List<CollectionBean> data) {
    dataList.clear();
    if (data != null) {
      dataList.addAll(data);
      notifyDataSetChanged();
    }
  }

  public void addForwardData(List<CollectionBean> data) {
    if (data != null) {
      dataList.addAll(0, data);
      notifyItemInserted(0);
    }
  }

  public void addData(List<CollectionBean> data) {
    if (data != null) {
      int size = dataList.size();
      dataList.addAll(data);
      notifyItemInserted(size);
    }
  }

  public void setViewHolderFactory(IChatViewHolderFactory factory) {
    this.viewHolderFactory = factory;
  }

  public void updateMessage(CollectionBean data, Object payload) {
    int index = getMessageIndex(data);
    if (index >= 0) {
      notifyItemChanged(index, payload);
    }
  }

  public int getMessageIndex(CollectionBean data) {
    for (int i = 0; i < dataList.size(); i++) {
      if (dataList.get(i).isSame(data)) {
        return i;
      }
    }
    return -1;
  }

  public void appendData(List<CollectionBean> data) {
    if (data != null) {
      dataList.addAll(data);
    }
  }

  public void removeData(CollectionBean data) {
    if (data == null || TextUtils.isEmpty(data.getCollectionId())) {
      return;
    }
    removeDataWithClientId(data.getCollectionId());
  }

  public void removeDataWithClientIds(List<String> idList) {
    if (idList == null || idList.isEmpty()) {
      return;
    }
    int index = -1;
    for (String id : idList) {
      for (int j = 0; j < dataList.size(); j++) {
        if (TextUtils.equals(dataList.get(j).getCollectionId(), id)) {
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
      if (TextUtils.equals(dataList.get(j).getCollectionId(), id)) {
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
    CollectionBean collectionBean = searchMessage(progress.getMessageCid());
    if (collectionBean != null) {
      float pg = progress.getProgress();
      collectionBean.setLoadProgress(pg);
      ALog.d(LIB_TAG, TAG, "updateMessageProgress:" + pg);
      updateMessage(collectionBean, PAYLOAD_PROGRESS);
    }
  }

  public CollectionBean searchMessage(String messageId) {
    for (int i = dataList.size() - 1; i >= 0; i--) {
      if (dataList.get(i).getMessageData() != null
          && TextUtils.equals(messageId, dataList.get(i).getMessageData().getMessageClientId())) {
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

  public CollectionBean getData(int index) {
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
      CollectionBean messageBean = getData(position);
      if (messageBean != null) {
        return messageBean.getMessageType();
      }
    }
    return 0;
  }
}
