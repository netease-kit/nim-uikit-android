// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.verify;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.netease.yunxin.kit.contactkit.ui.IContactFactory;
import com.netease.yunxin.kit.contactkit.ui.R;
import com.netease.yunxin.kit.contactkit.ui.model.BaseContactBean;
import com.netease.yunxin.kit.contactkit.ui.model.TeamVerifyInfoBean;
import com.netease.yunxin.kit.contactkit.ui.view.ContactListViewAttrs;
import com.netease.yunxin.kit.contactkit.ui.view.viewholder.BaseContactViewHolder;
import java.util.ArrayList;
import java.util.List;

public class TeamVerifyAdapter extends RecyclerView.Adapter<BaseContactViewHolder> {

  private final List<TeamVerifyInfoBean> dataList;

  public TeamVerifyAdapter() {
    dataList = new ArrayList<>();
    contactListViewAttrs = new ContactListViewAttrs();
  }

  private IContactFactory viewHolderFactory;

  private final ContactListViewAttrs contactListViewAttrs;

  @NonNull
  @Override
  public BaseContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view =
        LayoutInflater.from(parent.getContext())
            .inflate(R.layout.contact_list_item_container_layout, parent, false);
    BaseContactViewHolder viewHolder =
        viewHolderFactory.createViewHolder((ViewGroup) view, viewType);
    return viewHolder;
  }

  @Override
  public void onBindViewHolder(@NonNull BaseContactViewHolder holder, int position) {
    BaseContactBean bean = dataList.get(position);
    holder.onBind(bean, position, contactListViewAttrs);
  }

  public void removeData(BaseContactBean data) {
    if (data == null) {
      return;
    }
    int index = dataList.indexOf(data);
    if (index >= 0) {
      dataList.remove(data);
      notifyItemRemoved(index);
    }
  }

  @SuppressLint("NotifyDataSetChanged")
  public void removeListData(List<TeamVerifyInfoBean> listData) {
    if (listData == null || listData.isEmpty()) {
      return;
    }
    if (dataList.removeAll(listData)) {
      notifyDataSetChanged();
    }
  }

  // 群列表调用
  @SuppressLint("NotifyDataSetChanged")
  public void clearData() {
    if (!dataList.isEmpty()) {
      dataList.clear();
      notifyDataSetChanged();
    }
  }

  public void addData(TeamVerifyInfoBean data) {
    if (data == null) {
      return;
    }
    if (!dataList.isEmpty() && data.weight > 0) {
      int index = 0;
      while (dataList.size() > index
          && dataList.get(index) != null
          && dataList.get(index).weight >= data.weight) {
        index++;
      }
      dataList.add(index, data);
      notifyItemInserted(index);
    } else {
      dataList.add(data);
      notifyItemInserted(dataList.size() - 1);
    }
  }

  public void updateData(TeamVerifyInfoBean data) {
    if (data == null) {
      return;
    }
    int insertIndex = 0;
    int removeIndex = -1;
    boolean hasInsert = false;
    for (int index = 0; index < dataList.size(); index++) {
      if (!hasInsert && data.getCreateTimestamp() >= dataList.get(index).getCreateTimestamp()) {
        insertIndex = index;
        hasInsert = true;
      }
      if (dataList.get(index).equals(data)) {
        removeIndex = index;
        if (hasInsert) {
          break;
        }
      }
    }
    if (removeIndex == insertIndex) {
      dataList.set(insertIndex, data);
      notifyItemChanged(insertIndex);
    } else {
      if (removeIndex >= 0) {
        dataList.remove(removeIndex);
        notifyItemRemoved(removeIndex);
      }
      dataList.add(insertIndex, data);
      notifyItemInserted(insertIndex);
    }
  }

  //  public void updateData(TeamVerifyInfoBean data) {
  //    if (data == null) return;
  //    for (int index = 0; index < dataList.size(); index++) {
  //      if (dataList.get(index).equals(data)) {
  //        dataList.set(index, data);
  //        notifyItemChanged(index);
  //      }
  //    }
  //  }

  // 根据TeamID刷新
  public void updateData(List<String> accountIdList) {
    if (dataList.isEmpty()) {
      return;
    }
    for (String account : accountIdList) {
      int index = getIndexByAccount(account);
      if (index >= 0) {
        notifyItemChanged(index);
      }
    }
  }

  public int getIndexByAccount(String account) {
    for (int i = 0; i < dataList.size(); i++) {
      if (dataList.get(i).getAccountId().equals(account)) {
        return i;
      }
    }
    return 0;
  }

  public void setListData(List<TeamVerifyInfoBean> listData) {
    clearData();
    if (listData != null) {
      dataList.addAll(listData);
    }
    notifyDataSetChanged();
  }

  // 验证消息调用
  public void addForwardListData(List<TeamVerifyInfoBean> listData) {
    if (listData == null || listData.isEmpty()) {
      return;
    }
    dataList.addAll(0, listData);
    notifyItemRangeInserted(0, listData.size());
  }

  @Override
  public int getItemCount() {
    return dataList.size();
  }

  @Override
  public int getItemViewType(int position) {
    if (dataList.get(position) != null) {
      return viewHolderFactory.getItemViewType(dataList.get(position));
    }
    return super.getItemViewType(position);
  }

  public void setViewHolderFactory(IContactFactory factory) {
    viewHolderFactory = factory;
  }

  public List<TeamVerifyInfoBean> getDataList() {
    return dataList;
  }
}
