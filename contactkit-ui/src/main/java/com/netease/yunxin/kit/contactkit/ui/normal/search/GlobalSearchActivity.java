// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.normal.search;

import android.view.View;
import com.netease.yunxin.kit.contactkit.ui.databinding.GlobalSearchActivityBinding;
import com.netease.yunxin.kit.contactkit.ui.search.page.BaseSearchActivity;
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant;

/** search your friend or team */
public class GlobalSearchActivity extends BaseSearchActivity {

  protected View initViewAndGetRootView() {
    GlobalSearchActivityBinding viewBinding =
        GlobalSearchActivityBinding.inflate(getLayoutInflater());
    searchRv = viewBinding.rvSearch;
    clearView = viewBinding.ivClear;
    emptyView = viewBinding.emptyLl;
    searchEditText = viewBinding.etSearch;
    backView = viewBinding.globalTitleBar.getBackImageView();
    return viewBinding.getRoot();
  }

  @Override
  protected void bindView() {
    super.bindView();
    searchAdapter.setViewHolderFactory(new SearchViewHolderFactory());
    routerFriend = RouterConstant.PATH_CHAT_P2P_PAGE;
    routerTeam = RouterConstant.PATH_CHAT_TEAM_PAGE;
  }
}
