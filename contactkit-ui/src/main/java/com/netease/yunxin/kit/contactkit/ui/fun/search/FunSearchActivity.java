// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.fun.search;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import com.netease.yunxin.kit.common.utils.SizeUtils;
import com.netease.yunxin.kit.contactkit.ui.R;
import com.netease.yunxin.kit.contactkit.ui.databinding.FunSearchActivityBinding;
import com.netease.yunxin.kit.contactkit.ui.search.page.BaseSearchActivity;
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant;

public class FunSearchActivity extends BaseSearchActivity {

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    changeStatusBarColor(R.color.fun_contact_page_bg_color);
  }

  @Override
  protected View initViewAndGetRootView() {
    FunSearchActivityBinding viewBinding = FunSearchActivityBinding.inflate(getLayoutInflater());
    searchRv = viewBinding.searchRv;
    clearView = viewBinding.clearIv;
    emptyView = viewBinding.emptyLayout;
    searchEditText = viewBinding.searchEt;
    backView = viewBinding.cancelBtn;
    searchRv.addItemDecoration(getItemDecoration());
    return viewBinding.getRoot();
  }

  @Override
  protected void bindView() {
    super.bindView();
    searchAdapter.setViewHolderFactory(new FunSearchViewHolderFactory());
    routerFriend = RouterConstant.PATH_FUN_CHAT_P2P_PAGE;
    routerTeam = RouterConstant.PATH_FUN_CHAT_TEAM_PAGE;
  }

  public RecyclerView.ItemDecoration getItemDecoration() {
    return new RecyclerView.ItemDecoration() {
      final int topPadding = SizeUtils.dp2px(0.5f);
      final int indent = SizeUtils.dp2px(16);

      @Override
      public void onDrawOver(
          @NonNull Canvas canvas, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        int left = parent.getPaddingLeft() + indent;
        int right = parent.getWidth() - parent.getPaddingRight();

        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount - 1; i++) {
          View child = parent.getChildAt(i);

          RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

          int top = child.getBottom() + params.bottomMargin;
          int bottom = top + topPadding;

          Paint paint = new Paint();
          paint.setColor(Color.LTGRAY);
          canvas.drawRect(left, top, right, bottom, paint);
        }
      }
    };
  }
}
