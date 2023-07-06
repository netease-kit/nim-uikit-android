// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.fun.search;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import com.netease.yunxin.kit.common.ui.viewholder.BaseViewHolder;
import com.netease.yunxin.kit.common.ui.viewholder.IViewHolderFactory;
import com.netease.yunxin.kit.contactkit.ui.ContactConstant;
import com.netease.yunxin.kit.contactkit.ui.databinding.FunSearchTitleViewHolderBinding;
import com.netease.yunxin.kit.contactkit.ui.databinding.FunSearchUserViewHolderBinding;
import com.netease.yunxin.kit.contactkit.ui.fun.search.viewholder.FunSearchFriendViewHolder;
import com.netease.yunxin.kit.contactkit.ui.fun.search.viewholder.FunSearchTeamViewHolder;
import com.netease.yunxin.kit.contactkit.ui.fun.search.viewholder.FunTitleViewHolder;

/** search result list view holder factory */
public class FunSearchViewHolderFactory implements IViewHolderFactory {

  @Override
  public BaseViewHolder createViewHolder(@NonNull ViewGroup parent, int viewType) {

    if (viewType == ContactConstant.SearchViewType.TITLE) { //title view
      return new FunTitleViewHolder(
          FunSearchTitleViewHolderBinding.inflate(
              LayoutInflater.from(parent.getContext()), parent, false));
    } else if (viewType == ContactConstant.SearchViewType.USER) { // friend view
      return new FunSearchFriendViewHolder(
          FunSearchUserViewHolderBinding.inflate(
              LayoutInflater.from(parent.getContext()), parent, false));
    } else if (viewType == ContactConstant.SearchViewType.TEAM) { //team view
      return new FunSearchTeamViewHolder(
          FunSearchUserViewHolderBinding.inflate(
              LayoutInflater.from(parent.getContext()), parent, false));
    }
    return null;
  }
}
