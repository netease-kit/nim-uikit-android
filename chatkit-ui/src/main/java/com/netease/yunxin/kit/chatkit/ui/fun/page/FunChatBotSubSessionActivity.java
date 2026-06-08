// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.fun.page;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.normal.page.ChatBotSubSessionActivity;
import com.netease.yunxin.kit.common.utils.SizeUtils;
import com.netease.yunxin.kit.corekit.im2.utils.RouterConstant;

public class FunChatBotSubSessionActivity extends ChatBotSubSessionActivity {

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    changeStatusBarColor(R.color.fun_chat_page_bg_color);
  }

  @Override
  protected void onListViewReady() {
    super.onListViewReady();
    int pageBgColor = ContextCompat.getColor(this, R.color.fun_chat_page_bg_color);
    getWindow().getDecorView().setBackgroundColor(pageBgColor);
    binding.getRoot().setBackgroundColor(pageBgColor);
    adapter.setItemBackgroundColorRes(R.color.fun_conversation_item_bg_color);
    binding.emptyActionButton.setBackgroundResource(
        R.drawable.chat_bot_sub_session_empty_button_fun_bg);
    binding.emptyActionButton.setTextColor(ContextCompat.getColor(this, R.color.fun_chat_color));
    RecyclerView recyclerView = findViewById(R.id.topic_recycler_view);
    if (recyclerView != null) {
      recyclerView.setBackgroundColor(pageBgColor);
      recyclerView.addItemDecoration(createFunDividerDecoration());
    }
    View root = findViewById(android.R.id.content);
    if (root != null) {
      root.setBackgroundColor(pageBgColor);
    }
  }

  @Override
  protected int getSearchEmptyImageRes() {
    return R.drawable.fun_ic_chat_search_empty;
  }

  @Override
  protected String getTopicChatRoute() {
    return RouterConstant.PATH_FUN_CHAT_BOT_SUB_SESSION_CHAT_PAGE;
  }

  @Override
  protected String getSettingRoute() {
    return RouterConstant.PATH_FUN_CHAT_SETTING_PAGE;
  }

  @Override
  public int getRenameConfirmButtonBackgroundRes() {
    return R.drawable.chat_bot_sub_session_rename_confirm_fun_bg;
  }

  private RecyclerView.ItemDecoration createFunDividerDecoration() {
    return new RecyclerView.ItemDecoration() {
      private final int dividerHeight = SizeUtils.dp2px(0.25f);
      private final int indent = SizeUtils.dp2px(40);
      private final Paint paint = new Paint();

      @Override
      public void onDrawOver(Canvas canvas, RecyclerView parent, RecyclerView.State state) {
        paint.setColor(
            ContextCompat.getColor(
                parent.getContext(), R.color.fun_conversation_item_divide_line_color));
        int left = parent.getPaddingLeft() + indent;
        int right = parent.getWidth() - parent.getPaddingRight();
        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount - 1; i++) {
          View child = parent.getChildAt(i);
          RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
          int top = child.getBottom() + params.bottomMargin;
          canvas.drawRect(left, top, right, top + dividerHeight, paint);
        }
      }
    };
  }
}
