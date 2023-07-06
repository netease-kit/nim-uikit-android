// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.fun.view;

import android.view.ViewGroup;
import com.netease.yunxin.kit.contactkit.ui.IContactFactory;
import com.netease.yunxin.kit.contactkit.ui.fun.view.viewholder.FunContactViewHolder;
import com.netease.yunxin.kit.contactkit.ui.fun.view.viewholder.FunEntranceViewHolder;
import com.netease.yunxin.kit.contactkit.ui.model.BaseContactBean;
import com.netease.yunxin.kit.contactkit.ui.model.IViewTypeConstant;
import com.netease.yunxin.kit.contactkit.ui.view.viewholder.BaseContactViewHolder;

/** viewHolder factory */
public abstract class FunContactViewHolderFactory implements IContactFactory {

  @Override
  public int getItemViewType(BaseContactBean data) {
    return data.viewType;
  }

  public BaseContactViewHolder createViewHolder(ViewGroup view, int viewType) {
    if (viewType == IViewTypeConstant.CONTACT_FRIEND) {
      return new FunContactViewHolder(view);
    } else if (viewType == IViewTypeConstant.CONTACT_ACTION_ENTER) {
      return new FunEntranceViewHolder(view);
    } else if (viewType >= IViewTypeConstant.CUSTOM_START) {
      return getCustomViewHolder(view, viewType);
    }

    return null;
  }

  protected abstract BaseContactViewHolder getCustomViewHolder(ViewGroup view, int viewType);
}
