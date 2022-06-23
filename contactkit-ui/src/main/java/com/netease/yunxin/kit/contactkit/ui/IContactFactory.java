package com.netease.yunxin.kit.contactkit.ui;

import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.netease.yunxin.kit.contactkit.ui.model.BaseContactBean;
import com.netease.yunxin.kit.contactkit.ui.view.viewholder.BaseContactViewHolder;

public interface IContactFactory {

    //Adapter获取数据对应的ViewType
    int getItemViewType(BaseContactBean data);

    //创建ViewHolder
    BaseContactViewHolder createViewHolder(@NonNull ViewGroup parent, int viewType);

}
