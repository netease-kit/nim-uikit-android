// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.
package com.netease.yunxin.kit.chatkit.ui.normal.page;

import static com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant.LIB_TAG;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import androidx.annotation.Nullable;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatSearchItemLayoutBinding;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatSearchMessageActivityBinding;
import com.netease.yunxin.kit.chatkit.ui.normal.viewholder.SearchMessageViewHolder;
import com.netease.yunxin.kit.chatkit.ui.page.ChatSearchBaseActivity;
import com.netease.yunxin.kit.common.ui.viewholder.BaseBean;
import com.netease.yunxin.kit.common.ui.viewholder.ViewHolderClickListener;
import com.netease.yunxin.kit.common.utils.KeyboardUtils;
import com.netease.yunxin.kit.corekit.im2.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;

/** 标准皮肤，群消息搜索消息页面。 */
public class ChatSearchActivity extends ChatSearchBaseActivity {

  private final String TAG = "ChatSearchActivity";

  protected ChatSearchMessageActivityBinding viewBinding;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    changeStatusBarColor(R.color.color_white);
  }

  @Override
  protected void initViewAndSetContentView(@Nullable Bundle savedInstanceState) {
    viewBinding = ChatSearchMessageActivityBinding.inflate(getLayoutInflater());
    setContentView(viewBinding.getRoot());
    searchRV = viewBinding.searchRv;
    searchET = viewBinding.searchEt;
    clearIV = viewBinding.clearIv;
    emptyLayout = viewBinding.emptyLayout;
    messageSearchTitleBar = viewBinding.searchTitleBar;
  }

  @Override
  protected void bindingView() {
    super.bindingView();
    searchAdapter.setViewHolderClickListener(
        new ViewHolderClickListener() {
          @Override
          public boolean onClick(View v, BaseBean data, int position) {
            ALog.d(LIB_TAG, TAG, "item onClick position:" + position);
            KeyboardUtils.hideKeyboard(ChatSearchActivity.this);
            XKitRouter.withKey(RouterConstant.PATH_CHAT_TEAM_PAGE)
                .withParam(data.paramKey, data.param)
                .withParam(RouterConstant.CHAT_KRY, team)
                .withContext(ChatSearchActivity.this)
                .navigate();
            return true;
          }

          @Override
          public boolean onLongClick(View v, BaseBean data, int position) {
            return false;
          }
        });
    searchAdapter.setViewHolderFactory(
        (parent, viewType) ->
            new SearchMessageViewHolder(
                ChatSearchItemLayoutBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false)));
  }
}
