// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.view.viewholder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.netease.yunxin.kit.contactkit.ui.interfaces.ContactActions;
import com.netease.yunxin.kit.contactkit.ui.model.BaseContactBean;
import com.netease.yunxin.kit.contactkit.ui.view.ContactListViewAttrs;

public abstract class BaseContactViewHolder extends RecyclerView.ViewHolder {

  ContactActions actions;

  Context context;

  public BaseContactViewHolder(@NonNull ViewGroup itemView) {
    super(itemView);
    context = itemView.getContext();
    LayoutInflater layoutInflater = LayoutInflater.from(context);
    initViewBinding(layoutInflater, itemView);
  }

  public abstract void initViewBinding(LayoutInflater layoutInflater, ViewGroup container);

  public void setActions(ContactActions actions) {
    this.actions = actions;
  }

  public abstract void onBind(BaseContactBean bean, int position, ContactListViewAttrs attrs);
}
