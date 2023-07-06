// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.normal.contact;

import android.view.ViewGroup;
import com.netease.yunxin.kit.contactkit.ui.normal.view.ContactViewHolderFactory;
import com.netease.yunxin.kit.contactkit.ui.view.viewholder.BaseContactViewHolder;

public class ContactDefaultFactory extends ContactViewHolderFactory {
  @Override
  protected BaseContactViewHolder getCustomViewHolder(ViewGroup view, int viewType) {
    return null;
  }
}
