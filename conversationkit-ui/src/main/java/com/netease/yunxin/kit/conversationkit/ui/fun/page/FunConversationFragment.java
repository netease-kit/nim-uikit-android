// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.conversationkit.ui.fun.page;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import com.netease.yunxin.kit.common.ui.widgets.ContentListPopView;
import com.netease.yunxin.kit.common.ui.widgets.TitleBarView;
import com.netease.yunxin.kit.common.utils.SizeUtils;
import com.netease.yunxin.kit.conversationkit.ui.ConversationKitClient;
import com.netease.yunxin.kit.conversationkit.ui.ConversationUIConfig;
import com.netease.yunxin.kit.conversationkit.ui.ConversationUIConstant;
import com.netease.yunxin.kit.conversationkit.ui.R;
import com.netease.yunxin.kit.conversationkit.ui.databinding.FunConversationFragmentBinding;
import com.netease.yunxin.kit.conversationkit.ui.fun.FunPopItemFactory;
import com.netease.yunxin.kit.conversationkit.ui.fun.FunViewHolderFactory;
import com.netease.yunxin.kit.conversationkit.ui.page.ConversationBaseFragment;
import com.netease.yunxin.kit.corekit.im.IMKitClient;
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;

public class FunConversationFragment extends ConversationBaseFragment {

  private FunConversationFragmentBinding viewBinding;

  @Override
  public View initViewAndGetRootView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    viewBinding = FunConversationFragmentBinding.inflate(inflater, container, false);
    initView();
    return viewBinding.getRoot();
  }

  private void initView() {
    conversationView = viewBinding.conversationView;
    titleBarView = viewBinding.titleBar;
    networkErrorView = viewBinding.errorTv;
    emptyView = viewBinding.emptyLayout;
    viewBinding.titleBar.getRight2ImageView().setVisibility(View.GONE);

    setViewHolderFactory(new FunViewHolderFactory());
    viewBinding.conversationView.addItemDecoration(getItemDecoration());
    viewBinding.titleBar.setRightImageClick(
        v -> {
          if (ConversationKitClient.getConversationUIConfig() != null
              && ConversationKitClient.getConversationUIConfig().titleBarRightClick != null) {
            ConversationKitClient.getConversationUIConfig().titleBarRightClick.onClick(v);
            return;
          }
          if (IMKitClient.getConfigCenter().getTeamEnable()) {
            Context context = getContext();
            int memberLimit = ConversationUIConstant.MAX_TEAM_MEMBER;
            ContentListPopView contentListPopView =
                new ContentListPopView.Builder(context)
                    .addItem(FunPopItemFactory.getAddFriendItem(context))
                    .addItem(FunPopItemFactory.getDivideLineItem(context))
                    .addItem(FunPopItemFactory.getCreateGroupTeamItem(context, memberLimit))
                    .addItem(FunPopItemFactory.getDivideLineItem(context))
                    .addItem(FunPopItemFactory.getCreateAdvancedTeamItem(context, memberLimit))
                    .enableShadow(false)
                    .backgroundRes(R.drawable.fun_conversation_view_pop_bg)
                    .build();
            contentListPopView.showAsDropDown(
                v, (int) requireContext().getResources().getDimension(R.dimen.pop_margin_right), 0);
          } else {
            XKitRouter.withKey(RouterConstant.PATH_FUN_ADD_FRIEND_PAGE)
                .withContext(requireContext())
                .navigate();
          }
        });

    viewBinding.searchLayout.setOnClickListener(
        v -> {
          if (ConversationKitClient.getConversationUIConfig() != null
              && ConversationKitClient.getConversationUIConfig().titleBarRight2Click != null) {
            ConversationKitClient.getConversationUIConfig().titleBarRight2Click.onClick(v);
            return;
          }
          XKitRouter.withKey(RouterConstant.PATH_FUN_GLOBAL_SEARCH_PAGE)
              .withContext(requireContext())
              .navigate();
        });

    loadUIConfig();
  }

  public RecyclerView.ItemDecoration getItemDecoration() {
    return new RecyclerView.ItemDecoration() {
      final int topPadding = SizeUtils.dp2px(0.25f);
      final int indent = SizeUtils.dp2px(76);

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
          paint.setColor(
              parent.getResources().getColor(R.color.fun_conversation_item_divide_line_color));
          canvas.drawRect(left, top, right, bottom, paint);
        }
      }
    };
  }

  private void loadUIConfig() {
    if (ConversationKitClient.getConversationUIConfig() == null) {
      return;
    }
    ConversationUIConfig config = ConversationKitClient.getConversationUIConfig();

    viewBinding.titleBar.setLeftImageClick(
        v -> {
          if (config.titleBarLeftClick != null) {
            config.titleBarLeftClick.onClick(v);
          }
        });

    if (config.conversationComparator != null) {
      setComparator(config.conversationComparator);
    }

    if (config.conversationFactory != null) {
      setViewHolderFactory(config.conversationFactory);
    }

    if (!config.showTitleBar) {
      titleBarView.setVisibility(View.GONE);
    } else {
      titleBarView.setVisibility(View.VISIBLE);
      titleBarView.setHeadImageVisible(config.showTitleBarLeftIcon ? View.VISIBLE : View.GONE);
      titleBarView.showRightImageView(config.showTitleBarRightIcon);

      if (config.titleBarTitle != null) {
        titleBarView.setTitle(config.titleBarTitle);
      }

      if (config.titleBarTitleColor != null) {
        titleBarView.setTitleColor(config.titleBarTitleColor);
      }

      if (config.titleBarLeftRes != null) {
        titleBarView.setLeftImageRes(config.titleBarLeftRes);
      }

      if (config.titleBarLeftRes != null) {
        titleBarView.setLeftImageRes(config.titleBarLeftRes);
      }

      if (config.titleBarRightRes != null) {
        titleBarView.setRightImageRes(config.titleBarRightRes);
      }
    }

    if (config.customLayout != null) {
      config.customLayout.customizeConversationLayout(this);
    }
  }

  public TitleBarView getTitleBar() {
    return viewBinding.titleBar;
  }

  public LinearLayout getTopLayout() {
    return viewBinding.topLayout;
  }

  public LinearLayout getBodyLayout() {
    return viewBinding.bodyLayout;
  }

  public FrameLayout getBottomLayout() {
    return viewBinding.bottomLayout;
  }

  public FrameLayout getBodyTopLayout() {
    return viewBinding.bodyTopLayout;
  }

  public TextView getErrorTextView() {
    return viewBinding.errorTv;
  }

  public void setEmptyViewVisible(int visible) {
    viewBinding.emptyLayout.setVisibility(visible);
  }

  public View getEmptyView() {
    return viewBinding.emptyLayout;
  }
}
