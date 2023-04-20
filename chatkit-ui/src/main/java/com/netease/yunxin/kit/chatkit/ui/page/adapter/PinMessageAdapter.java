// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.page.adapter;

import static com.netease.yunxin.kit.chatkit.ui.ChatMessageType.LOCATION_MESSAGE_VIEW_TYPE;
import static com.netease.yunxin.kit.chatkit.ui.ChatMessageType.NORMAL_MESSAGE_VIEW_TYPE_AUDIO;
import static com.netease.yunxin.kit.chatkit.ui.ChatMessageType.NORMAL_MESSAGE_VIEW_TYPE_FILE;
import static com.netease.yunxin.kit.chatkit.ui.ChatMessageType.NORMAL_MESSAGE_VIEW_TYPE_IMAGE;
import static com.netease.yunxin.kit.chatkit.ui.ChatMessageType.NORMAL_MESSAGE_VIEW_TYPE_VIDEO;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatBasePinViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.chatkit.ui.view.interfaces.IPinMessageClickListener;
import com.netease.yunxin.kit.chatkit.ui.view.pin.ChatAudioPinViewHolder;
import com.netease.yunxin.kit.chatkit.ui.view.pin.ChatBasePinViewHolder;
import com.netease.yunxin.kit.chatkit.ui.view.pin.ChatFilePinViewHolder;
import com.netease.yunxin.kit.chatkit.ui.view.pin.ChatImagePinViewHolder;
import com.netease.yunxin.kit.chatkit.ui.view.pin.ChatLocationPinViewHolder;
import com.netease.yunxin.kit.chatkit.ui.view.pin.ChatPinTextViewHolder;
import com.netease.yunxin.kit.chatkit.ui.view.pin.ChatVideoPinViewHolder;
import java.util.ArrayList;
import java.util.List;

/** history message search adapter */
public class PinMessageAdapter extends RecyclerView.Adapter<ChatBasePinViewHolder> {

  private final String TAG = "PinMessageAdapter";
  private final List<ChatMessageBean> dataList = new ArrayList<>();
  private IPinMessageClickListener clickListener;

  public void setData(List<ChatMessageBean> data) {
    dataList.clear();
    if (data != null) {
      dataList.addAll(data);
      notifyDataSetChanged();
    }
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
            insertIndex = index;
            break;
          }
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

  public void setViewHolderClickListener(IPinMessageClickListener listener) {
    this.clickListener = listener;
  }

  @NonNull
  @Override
  public ChatBasePinViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

    ChatBasePinViewHolder viewHolder;
    ChatBasePinViewHolderBinding viewHolderBinding =
        ChatBasePinViewHolderBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false);
    if (viewType == NORMAL_MESSAGE_VIEW_TYPE_AUDIO) {
      viewHolder = new ChatAudioPinViewHolder(viewHolderBinding, viewType);
    } else if (viewType == NORMAL_MESSAGE_VIEW_TYPE_IMAGE) {
      viewHolder = new ChatImagePinViewHolder(viewHolderBinding, viewType);
    } else if (viewType == NORMAL_MESSAGE_VIEW_TYPE_VIDEO) {
      viewHolder = new ChatVideoPinViewHolder(viewHolderBinding, viewType);
    } else if (viewType == NORMAL_MESSAGE_VIEW_TYPE_FILE) {
      viewHolder = new ChatFilePinViewHolder(viewHolderBinding, viewType);
    } else if (viewType == LOCATION_MESSAGE_VIEW_TYPE) {
      viewHolder = new ChatLocationPinViewHolder(viewHolderBinding, viewType);
    } else {
      //default as text message
      viewHolder = new ChatPinTextViewHolder(viewHolderBinding, viewType);
    }
    viewHolder.setItemClickListener(clickListener);
    return viewHolder;
  }

  @Override
  public void onBindViewHolder(@NonNull ChatBasePinViewHolder holder, int position) {
    holder.bindData(dataList.get(position), position);
  }

  @Override
  public void onBindViewHolder(
      @NonNull ChatBasePinViewHolder holder, int position, @NonNull List<Object> payloads) {
    if (payloads.isEmpty()) {
      super.onBindViewHolder(holder, position, payloads);
    } else {
      holder.bindData(dataList.get(position), position, payloads);
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
