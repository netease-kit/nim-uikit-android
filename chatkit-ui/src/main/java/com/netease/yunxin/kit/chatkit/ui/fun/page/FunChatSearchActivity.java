// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.
package com.netease.yunxin.kit.chatkit.ui.fun.page;

import static com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant.LIB_TAG;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.databinding.FunChatSearchMessageActivityBinding;
import com.netease.yunxin.kit.chatkit.ui.databinding.FunChatSearchViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.fun.viewholder.FunSearchMessageViewHolder;
import com.netease.yunxin.kit.chatkit.ui.page.ChatSearchBaseActivity;
import com.netease.yunxin.kit.common.ui.viewholder.BaseBean;
import com.netease.yunxin.kit.common.ui.viewholder.ViewHolderClickListener;
import com.netease.yunxin.kit.common.utils.KeyboardUtils;
import com.netease.yunxin.kit.common.utils.SizeUtils;
import com.netease.yunxin.kit.corekit.im2.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;

/** Fun皮肤搜索页面，继承自ChatSearchBaseActivity */
public class FunChatSearchActivity extends ChatSearchBaseActivity {

  private final String TAG = "ChatSearchFunActivity";
  private FunChatSearchMessageActivityBinding viewBinding;

  @Override
  protected void initViewAndSetContentView(@Nullable Bundle savedInstanceState) {
    changeStatusBarColor(R.color.fun_chat_secondary_page_bg_color);
    viewBinding = FunChatSearchMessageActivityBinding.inflate(getLayoutInflater());
    setContentView(viewBinding.getRoot());
    searchRV = viewBinding.searchRv;
    searchET = viewBinding.searchEt;
    clearIV = viewBinding.clearIv;
    emptyLayout = viewBinding.emptyLayout;
    viewBinding.searchRv.addItemDecoration(getItemDecoration());
    viewBinding.cancelBtn.setOnClickListener(view -> finish());
  }

  @Override
  protected void bindingView() {
    super.bindingView();
    searchAdapter.setViewHolderClickListener(
        new ViewHolderClickListener() {
          @Override
          public boolean onClick(View v, BaseBean data, int position) {
            ALog.d(LIB_TAG, TAG, "item onClick position:" + position);
            KeyboardUtils.hideKeyboard(FunChatSearchActivity.this);
            XKitRouter.withKey(RouterConstant.PATH_FUN_CHAT_TEAM_PAGE)
                .withParam(data.paramKey, data.param)
                .withParam(RouterConstant.CHAT_KRY, team)
                .withContext(FunChatSearchActivity.this)
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
            new FunSearchMessageViewHolder(
                FunChatSearchViewHolderBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false)));
  }

  public RecyclerView.ItemDecoration getItemDecoration() {
    return new RecyclerView.ItemDecoration() {
      final int topPadding = SizeUtils.dp2px(1);

      @Override
      public void getItemOffsets(
          @NonNull Rect outRect,
          @NonNull View view,
          @NonNull RecyclerView parent,
          @NonNull RecyclerView.State state) {
        outRect.set(0, topPadding, 0, 0);
      }
    };
  }
}
