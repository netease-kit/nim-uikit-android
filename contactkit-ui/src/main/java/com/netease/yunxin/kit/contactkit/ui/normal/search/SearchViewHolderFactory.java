// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.normal.search;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import com.netease.yunxin.kit.common.ui.viewholder.BaseViewHolder;
import com.netease.yunxin.kit.common.ui.viewholder.IViewHolderFactory;
import com.netease.yunxin.kit.contactkit.ui.ContactConstant;
import com.netease.yunxin.kit.contactkit.ui.databinding.SearchTitleItemLayoutBinding;
import com.netease.yunxin.kit.contactkit.ui.databinding.SearchUserItemLayoutBinding;
import com.netease.yunxin.kit.contactkit.ui.normal.search.viewholder.FriendViewHolder;
import com.netease.yunxin.kit.contactkit.ui.normal.search.viewholder.TeamViewHolder;
import com.netease.yunxin.kit.contactkit.ui.normal.search.viewholder.TitleViewHolder;

/** search result list view holder factory */
public class SearchViewHolderFactory implements IViewHolderFactory {

  @Override
  public BaseViewHolder createViewHolder(@NonNull ViewGroup parent, int viewType) {

    if (viewType == ContactConstant.SearchViewType.TITLE) { //title view
      return new TitleViewHolder(
          SearchTitleItemLayoutBinding.inflate(
              LayoutInflater.from(parent.getContext()), parent, false));
    } else if (viewType == ContactConstant.SearchViewType.USER) { // friend view
      return new FriendViewHolder(
          SearchUserItemLayoutBinding.inflate(
              LayoutInflater.from(parent.getContext()), parent, false));
    } else if (viewType == ContactConstant.SearchViewType.TEAM) { //team view
      return new TeamViewHolder(
          SearchUserItemLayoutBinding.inflate(
              LayoutInflater.from(parent.getContext()), parent, false));
    }
    return null;
  }
}
