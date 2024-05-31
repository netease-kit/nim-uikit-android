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
import com.netease.yunxin.kit.chatkit.IMKitConfigCenter;
import com.netease.yunxin.kit.common.ui.widgets.ContentListPopView;
import com.netease.yunxin.kit.common.ui.widgets.TitleBarView;
import com.netease.yunxin.kit.common.utils.SizeUtils;
import com.netease.yunxin.kit.conversationkit.ui.ConversationKitClient;
import com.netease.yunxin.kit.conversationkit.ui.ConversationUIConfig;
import com.netease.yunxin.kit.conversationkit.ui.R;
import com.netease.yunxin.kit.conversationkit.ui.common.ConversationConstant;
import com.netease.yunxin.kit.conversationkit.ui.databinding.FunConversationFragmentBinding;
import com.netease.yunxin.kit.conversationkit.ui.fun.FunPopItemFactory;
import com.netease.yunxin.kit.conversationkit.ui.fun.FunViewHolderFactory;
import com.netease.yunxin.kit.conversationkit.ui.page.ConversationBaseFragment;
import com.netease.yunxin.kit.corekit.im2.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;

/** 娱乐版会话列表Fragment, 用于展示娱乐版会话列表 */
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

  /** 初始化视图，父类处理业务逻辑，需要按照父类使用到的View进行初始化，保证父类业务逻辑正常展示 */
  private void initView() {
    conversationView = viewBinding.conversationView;
    titleBarView = viewBinding.titleBar;
    networkErrorView = viewBinding.errorTv;
    emptyView = viewBinding.emptyLayout;
    viewBinding.titleBar.getRight2ImageView().setVisibility(View.GONE);

    setViewHolderFactory(new FunViewHolderFactory());
    viewBinding.conversationView.addItemDecoration(getItemDecoration());
    // 设置标题栏右侧按钮点击事件
    viewBinding.titleBar.setRightImageClick(
        v -> {
          // 如果配置了自定义的右侧按钮点击事件，则执行自定义的点击事件
          if (ConversationKitClient.getConversationUIConfig() != null
              && ConversationKitClient.getConversationUIConfig().titleBarRightClick != null) {
            ConversationKitClient.getConversationUIConfig().titleBarRightClick.onClick(v);
            return;
          }
          // 组件支持是否使用群的配置，如果关闭则相关群的功能都不在展示
          if (IMKitConfigCenter.getTeamEnable()) {
            Context context = getContext();
            int memberLimit = ConversationConstant.MAX_TEAM_MEMBER;
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
            // 如果关闭则只展示添加好友的功能，跳转到添加好友页面
            XKitRouter.withKey(RouterConstant.PATH_FUN_ADD_FRIEND_PAGE)
                .withContext(requireContext())
                .navigate();
          }
        });

    // 设置搜索按钮点击事件
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

  /** 加载UI配置，主要根据支持的个性化UI配置ConversationUIConfig，加载外部配置的UI特性 */
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

  /**
   * 获取标题栏
   *
   * @return 标题栏
   */
  public TitleBarView getTitleBar() {
    return viewBinding.titleBar;
  }

  /**
   * 获取顶部布局
   *
   * @return 顶部布局
   */
  public LinearLayout getTopLayout() {
    return viewBinding.topLayout;
  }

  /**
   * 获取主体布局
   *
   * @return 主体布局
   */
  public LinearLayout getBodyLayout() {
    return viewBinding.bodyLayout;
  }

  /**
   * 获取底部布局
   *
   * @return 底部布局
   */
  public FrameLayout getBottomLayout() {
    return viewBinding.bottomLayout;
  }

  /**
   * 获取主体顶部布局的顶部布局 如果需要再会话列表和标题之间添加一些UI，可以使用此布局
   *
   * @return 主体顶部布局
   */
  public FrameLayout getBodyTopLayout() {
    return viewBinding.bodyTopLayout;
  }

  /**
   * 获取展示错误信息的TextView 当前断网的错误信息展示在此TextView上
   *
   * @return 主体底部布局
   */
  public TextView getErrorTextView() {
    return viewBinding.errorTv;
  }

  /** 设置空布局是否可见 */
  public void setEmptyViewVisible(int visible) {
    viewBinding.emptyLayout.setVisibility(visible);
  }

  /**
   * 获取空布局 当会话列表为空时，展示此布局。当前空布局包含文本和图片
   *
   * @return 空布局
   */
  public View getEmptyView() {
    return viewBinding.emptyLayout;
  }
}
