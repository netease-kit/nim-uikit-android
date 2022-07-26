/*
 * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.kit.contactkit.ui.view.adapter;


import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.netease.yunxin.kit.contactkit.ui.IContactFactory;
import com.netease.yunxin.kit.contactkit.ui.R;
import com.netease.yunxin.kit.contactkit.ui.contact.ContactDefaultFactory;
import com.netease.yunxin.kit.contactkit.ui.interfaces.ContactActions;
import com.netease.yunxin.kit.contactkit.ui.model.BaseContactBean;
import com.netease.yunxin.kit.contactkit.ui.model.ContactFriendBean;
import com.netease.yunxin.kit.contactkit.ui.view.ContactListViewAttrs;
import com.netease.yunxin.kit.contactkit.ui.view.viewholder.BaseContactViewHolder;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<BaseContactViewHolder> {

    private final List<BaseContactBean> dataList;

    private final List<ContactFriendBean> friendList;

    private ContactActions defaultActions;

    public ContactAdapter() {
        dataList = new ArrayList<>();
        friendList = new LinkedList<>();
        contactListViewAttrs = new ContactListViewAttrs();
        viewHolderFactory = new ContactDefaultFactory();
    }

    private IContactFactory viewHolderFactory;

    private final ContactListViewAttrs contactListViewAttrs;


    @NonNull
    @Override
    public BaseContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_list_item_container_layout, parent, false);
        BaseContactViewHolder viewHolder = viewHolderFactory.createViewHolder((ViewGroup) view, viewType);
        viewHolder.setActions(defaultActions);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull BaseContactViewHolder holder, int position) {
        BaseContactBean bean = dataList.get(position);
        holder.onBind(bean, position, contactListViewAttrs);
    }


    @SuppressLint("NotifyDataSetChanged")
    public void updateFriendData(List<ContactFriendBean> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        if (!friendList.isEmpty()) {
            dataList.removeAll(friendList);
        }
        friendList.clear();
        friendList.addAll(list);
        dataList.addAll(list);
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateFriendData() {
        int indexStart = 0;
        if (!dataList.isEmpty()) {
            while (dataList.size() > indexStart && dataList.get(indexStart) != null &&
                    !(dataList.get(indexStart) instanceof ContactFriendBean)) {
                indexStart++;
            }
            while (indexStart < dataList.size()) {
                dataList.remove(indexStart);
            }
        }
        dataList.addAll(friendList);
        notifyDataSetChanged();
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
    public void removeListData(List<? extends BaseContactBean> listData) {
        if (listData == null || listData.isEmpty()) {
            return;
        }
        if (dataList.removeAll(listData)) {
            notifyDataSetChanged();
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void clearData(){
        if (!dataList.isEmpty()){
            dataList.clear();
            notifyDataSetChanged();
        }
    }

    public void clearFriendData(){
        if (!friendList.isEmpty()){
            friendList.clear();
        }
    }


    public void addData(BaseContactBean data) {
        if (data == null) {
            return;
        }
        if (!dataList.isEmpty() && data.weight > 0) {
            int index = 0;
            while (dataList.size() > index && dataList.get(index) != null &&
                    dataList.get(index).weight >= data.weight) {
                index++;
            }
            dataList.add(index, data);
            notifyItemInserted(index);
        } else {
            dataList.add(data);
            notifyItemInserted(dataList.size() - 1);
        }
    }

    public void updateData(BaseContactBean data) {
        int index = dataList.indexOf(data);
        if (index >= 0) {
            dataList.set(index, data);
            notifyItemChanged(index);
        }
    }

    public void updateData(int viewType, List<? extends BaseContactBean> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        int indexStart = 0;
        int indexEnd;
        if (!dataList.isEmpty()) {
            while (dataList.size() > indexStart && dataList.get(indexStart) != null &&
                    dataList.get(indexStart).viewType != viewType) {
                indexStart++;
            }
            indexEnd = indexStart;
            while (dataList.size() > indexEnd && dataList.get(indexEnd) != null &&
                    dataList.get(indexEnd).viewType == viewType) {
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

    public void addListData(List<? extends BaseContactBean> listData) {
        if (listData == null || listData.isEmpty()) {
            return;
        }
        if (listData.get(0).weight > 0) {
            int index = 0;
            while (dataList.size() > index && dataList.get(index) != null &&
                    dataList.get(index).weight >= listData.get(0).weight) {
                index++;
            }
            dataList.addAll(index, listData);
            notifyItemRangeInserted(index, listData.size());
        } else {
            dataList.addAll(listData);
            notifyItemRangeInserted(dataList.size() - 1, listData.size());
        }
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

    public List<ContactFriendBean> getFriendList() {
        return friendList;
    }
}
