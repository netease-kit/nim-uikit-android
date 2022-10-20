// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.searchkit.ui.page;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import com.netease.yunxin.kit.common.ui.viewholder.BaseViewHolder;
import com.netease.yunxin.kit.common.ui.viewholder.IViewHolderFactory;
import com.netease.yunxin.kit.searchkit.ui.common.SearchConstant;
import com.netease.yunxin.kit.searchkit.ui.databinding.SearchTitleItemLayoutBinding;
import com.netease.yunxin.kit.searchkit.ui.databinding.SearchUserItemLayoutBinding;
import com.netease.yunxin.kit.searchkit.ui.view.FriendViewHolder;
import com.netease.yunxin.kit.searchkit.ui.view.TeamViewHolder;
import com.netease.yunxin.kit.searchkit.ui.view.TitleViewHolder;

/** search result list view holder factory */
public class SearchViewHolderFactory implements IViewHolderFactory {

  @Override
  public BaseViewHolder createViewHolder(@NonNull ViewGroup parent, int viewType) {

    if (viewType == SearchConstant.ViewType.TITLE) { //title view
      return new TitleViewHolder(
          SearchTitleItemLayoutBinding.inflate(
              LayoutInflater.from(parent.getContext()), parent, false));
    } else if (viewType == SearchConstant.ViewType.USER) { // friend view
      return new FriendViewHolder(
          SearchUserItemLayoutBinding.inflate(
              LayoutInflater.from(parent.getContext()), parent, false));
    } else if (viewType == SearchConstant.ViewType.TEAM) { //team view
      return new TeamViewHolder(
          SearchUserItemLayoutBinding.inflate(
              LayoutInflater.from(parent.getContext()), parent, false));
    }
    return null;
  }
}
