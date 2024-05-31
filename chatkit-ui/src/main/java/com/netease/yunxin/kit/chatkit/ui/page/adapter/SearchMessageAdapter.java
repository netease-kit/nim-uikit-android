// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.page.adapter;

import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.netease.yunxin.kit.chatkit.ui.model.ChatSearchBean;
import com.netease.yunxin.kit.chatkit.ui.view.input.ActionConstants;
import com.netease.yunxin.kit.chatkit.ui.view.message.viewholder.SearchMessageEmptyViewHolder;
import com.netease.yunxin.kit.common.ui.viewholder.BaseViewHolder;
import com.netease.yunxin.kit.common.ui.viewholder.IViewHolderFactory;
import com.netease.yunxin.kit.common.ui.viewholder.ViewHolderClickListener;
import java.util.ArrayList;
import java.util.List;

/** history message search adapter */
public class SearchMessageAdapter extends RecyclerView.Adapter<BaseViewHolder> {

  private final String TAG = "SearchMessageAdapter";
  private IViewHolderFactory viewHolderFactory;
  private final List<ChatSearchBean> dataList = new ArrayList<>();
  private ViewHolderClickListener clickListener;

  public void setData(List<ChatSearchBean> data) {
    dataList.clear();
    if (data != null) {
      dataList.addAll(data);
      notifyDataSetChanged();
    }
  }

  public void addForwardData(List<ChatSearchBean> data) {
    if (data != null) {
      dataList.addAll(0, data);
    }
  }

  public void appendData(List<ChatSearchBean> data) {
    if (data != null) {
      dataList.addAll(data);
    }
  }

  public void removeData(ChatSearchBean data) {
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

  public void removeData(int position) {
    if (position >= 0 && position < dataList.size()) {
      dataList.remove(position);
      notifyItemRemoved(position);
    }
  }

  /**
   * update user list
   *
   * @param accounts user account list
   */
  public void updateUserList(List<String> accounts) {
    for (String account : accounts) {
      updateUser(account);
    }
  }

  /**
   * update user info
   *
   * @param accId user account
   */
  public void updateUser(String accId) {
    if (accId == null) {
      return;
    }
    List<String> payload = new ArrayList<>();
    payload.add(ActionConstants.PAYLOAD_USERINFO);
    for (int j = 0; j < dataList.size(); j++) {
      if (accId.equals(dataList.get(j).getAccount())) {
        notifyItemChanged(j, payload);
      }
    }
  }

  public void setViewHolderFactory(IViewHolderFactory factory) {
    this.viewHolderFactory = factory;
  }

  public void setViewHolderClickListener(ViewHolderClickListener listener) {
    this.clickListener = listener;
  }

  @NonNull
  @Override
  public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    BaseViewHolder baseViewHolder = null;
    if (viewHolderFactory != null) {
      baseViewHolder = viewHolderFactory.createViewHolder(parent, viewType);
    }
    if (baseViewHolder == null) {
      TextView emptyTv = new TextView(parent.getContext());
      baseViewHolder = new SearchMessageEmptyViewHolder(emptyTv);
    }
    return baseViewHolder;
  }

  @Override
  public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {
    holder.onBindData(dataList.get(position), position);
    holder.setItemOnClickListener(clickListener);
  }

  @Override
  public void onBindViewHolder(
      @NonNull BaseViewHolder holder, int position, @NonNull List<Object> payloads) {
    super.onBindViewHolder(holder, position, payloads);
    holder.onBindData(dataList.get(position), position, payloads);
  }

  @Override
  public int getItemCount() {
    return dataList.size();
  }

  public ChatSearchBean getData(int index) {
    if (index >= 0 && index < dataList.size()) {
      return dataList.get(index);
    }
    return null;
  }
}
