// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.view.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.netease.yunxin.kit.contactkit.ui.IContactFactory;
import com.netease.yunxin.kit.contactkit.ui.R;
import com.netease.yunxin.kit.contactkit.ui.interfaces.ContactActions;
import com.netease.yunxin.kit.contactkit.ui.model.BaseContactBean;
import com.netease.yunxin.kit.contactkit.ui.model.IViewTypeConstant;
import com.netease.yunxin.kit.contactkit.ui.v2model.V2ContactFriendBean;
import com.netease.yunxin.kit.contactkit.ui.view.ContactListViewAttrs;
import com.netease.yunxin.kit.contactkit.ui.view.viewholder.BaseContactViewHolder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<BaseContactViewHolder> {

  private final List<BaseContactBean> dataList;

  private final List<V2ContactFriendBean> friendList;

  private ContactActions defaultActions;

  public ContactAdapter() {
    dataList = new ArrayList<>();
    friendList = new LinkedList<>();
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
    viewHolder.setActions(defaultActions);
    return viewHolder;
  }

  @Override
  public void onBindViewHolder(@NonNull BaseContactViewHolder holder, int position) {
    BaseContactBean bean = dataList.get(position);
    holder.onBind(bean, position, contactListViewAttrs);
  }

  // 通讯录调用，添加，更新，删除 好友列表
  @SuppressLint("NotifyDataSetChanged")
  public void updateFriendData(List<V2ContactFriendBean> list) {
    if (list == null || list.isEmpty()) {
      return;
    }

    // 以前调用 removeAll 操作，数量级为 mn，
    // 由于此处目的为了移除当前所有的好友数据，直接过滤源单列表，数量级缩减为 m
    Iterator<BaseContactBean> iterator = dataList.iterator();
    while (iterator.hasNext()) {
      BaseContactBean item = iterator.next();
      if (item.viewType == IViewTypeConstant.CONTACT_FRIEND) {
        iterator.remove();
      }
    }

    friendList.clear();
    friendList.addAll(list);
    dataList.addAll(list);
    notifyDataSetChanged();
  }

  // 通讯录调用，添加，删除，更新 好友
  @SuppressLint("NotifyDataSetChanged")
  public void updateFriendData() {
    int indexStart = 0;
    if (!dataList.isEmpty()) {
      while (dataList.size() > indexStart
          && dataList.get(indexStart) != null
          && !(dataList.get(indexStart) instanceof V2ContactFriendBean)) {
        indexStart++;
      }
      while (indexStart < dataList.size()) {
        dataList.remove(indexStart);
      }
    }
    dataList.addAll(friendList);
    notifyDataSetChanged();
  }

  // 黑名单调用
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

  // 黑名单和验证消息列表调用
  @SuppressLint("NotifyDataSetChanged")
  public void removeListData(List<? extends BaseContactBean> listData) {
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

  // 暂未调用
  public void clearFriendData() {
    if (!friendList.isEmpty()) {
      friendList.clear();
    }
  }

  // 通讯录调用，用于添加头部
  public void addData(BaseContactBean data) {
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

  // 验证，好友选择，通讯录调用
  public void updateData(BaseContactBean data) {
    int index = dataList.indexOf(data);
    if (index >= 0) {
      dataList.set(index, data);
      notifyItemChanged(index);
    }
  }

  // 验证，好友选择，通讯录调用
  public void updateDataAndSort(BaseContactBean data) {
    int index = dataList.indexOf(data);
    if (index >= 0) {
      dataList.remove(index);
      notifyItemRemoved(index);
      dataList.add(0, data);
      notifyItemInserted(0);
    }
  }

  // 暂未调用
  public void updateData(int viewType, List<? extends BaseContactBean> list) {
    if (list == null || list.isEmpty()) {
      return;
    }
    int indexStart = 0;
    int indexEnd;
    if (!dataList.isEmpty()) {
      while (dataList.size() > indexStart
          && dataList.get(indexStart) != null
          && dataList.get(indexStart).viewType != viewType) {
        indexStart++;
      }
      indexEnd = indexStart;
      while (dataList.size() > indexEnd
          && dataList.get(indexEnd) != null
          && dataList.get(indexEnd).viewType == viewType) {
        indexEnd++;
      }
      if (indexEnd < dataList.size()) {
        List<BaseContactBean> temp = dataList.subList(indexStart, indexEnd);
        dataList.removeAll(temp);
      }
    }
    dataList.addAll(list);
    indexEnd = indexStart + list.size();
    notifyItemRangeChanged(indexStart, indexEnd);
  }

  // 用户配置，通讯录添加列表头部信息
  public void addListData(List<? extends BaseContactBean> listData) {
    if (listData == null || listData.isEmpty()) {
      return;
    }
    if (listData.get(0).weight > 0) {
      int index = 0;
      while (dataList.size() > index
          && dataList.get(index) != null
          && dataList.get(index).weight >= listData.get(0).weight) {
        index++;
      }
      dataList.addAll(index, listData);
      notifyItemRangeInserted(index, listData.size());
    } else {
      dataList.addAll(listData);
      notifyItemRangeInserted(dataList.size() - 1, listData.size());
    }
  }

  // 验证消息调用
  public void addForwardListData(List<? extends BaseContactBean> listData) {
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

  public ContactListViewAttrs getContactListViewAttrs() {
    return contactListViewAttrs;
  }

  public void setViewHolderFactory(IContactFactory factory) {
    viewHolderFactory = factory;
  }

  public void setDefaultActions(ContactActions actions) {
    defaultActions = actions;
  }

  public void setContactListViewAttrs(ContactListViewAttrs attrs) {
    contactListViewAttrs.setAll(attrs);
  }

  public List<BaseContactBean> getDataList() {
    return dataList;
  }

  public List<V2ContactFriendBean> getFriendList() {
    return friendList;
  }
}
