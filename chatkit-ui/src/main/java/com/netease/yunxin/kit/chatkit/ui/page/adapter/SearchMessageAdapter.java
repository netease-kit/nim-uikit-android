/*
 * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.kit.chatkit.ui.page.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.netease.yunxin.kit.chatkit.ui.databinding.ChatSearchItemLayoutBinding;
import com.netease.yunxin.kit.chatkit.ui.model.ChatSearchBean;
import com.netease.yunxin.kit.chatkit.ui.view.message.viewholder.SearchMessageViewHolder;
import com.netease.yunxin.kit.common.ui.viewholder.BaseViewHolder;
import com.netease.yunxin.kit.common.ui.viewholder.IViewHolderFactory;
import com.netease.yunxin.kit.common.ui.viewholder.ViewHolderClickListener;

import java.util.ArrayList;
import java.util.List;

/**
 * history message search adapter
 */
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

    public void setViewHolderFactory(IViewHolderFactory factory) {
        this.viewHolderFactory = factory;
    }

    public void setViewHolderClickListener(ViewHolderClickListener listener) {
        this.clickListener = listener;
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SearchMessageViewHolder(ChatSearchItemLayoutBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {
        holder.onBindData(dataList.get(position), position);
        holder.setItemOnClickListener(clickListener);
    }

//    @Override
//    public int getItemViewType(int position) {
//        return dataList.get(position).viewType;
//    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public ChatSearchBean getData(int index){
        if (index >= 0 && index < dataList.size()){
            return dataList.get(index);
        }
        return null;
    }
}
