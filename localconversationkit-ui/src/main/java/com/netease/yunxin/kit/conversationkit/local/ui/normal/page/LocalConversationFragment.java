// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.conversationkit.local.ui.normal.page;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.netease.yunxin.kit.chatkit.IMKitConfigCenter;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.common.ui.widgets.ContentListPopView;
import com.netease.yunxin.kit.common.ui.widgets.TitleBarView;
import com.netease.yunxin.kit.conversationkit.local.ui.LocalConversationKitClient;
import com.netease.yunxin.kit.conversationkit.local.ui.LocalConversationUIConfig;
import com.netease.yunxin.kit.conversationkit.local.ui.R;
import com.netease.yunxin.kit.conversationkit.local.ui.common.ConversationConstant;
import com.netease.yunxin.kit.conversationkit.local.ui.databinding.LocalConversationFragmentBinding;
import com.netease.yunxin.kit.conversationkit.local.ui.model.AIUserBean;
import com.netease.yunxin.kit.conversationkit.local.ui.model.ConversationHeaderBean;
import com.netease.yunxin.kit.conversationkit.local.ui.normal.PopItemFactory;
import com.netease.yunxin.kit.conversationkit.local.ui.normal.ViewHolderFactory;
import com.netease.yunxin.kit.conversationkit.local.ui.page.LocalConversationBaseFragment;
import com.netease.yunxin.kit.corekit.im2.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import java.util.List;

/** 普通版会话列表页面 */
public class LocalConversationFragment extends LocalConversationBaseFragment {

  private final String TAG = "ConversationFragment";

  protected LocalConversationFragmentBinding viewBinding;

  @Override
  public View initViewAndGetRootView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    viewBinding = LocalConversationFragmentBinding.inflate(inflater, container, false);
    initView();
    return viewBinding.getRoot();
  }

  public void initView() {
    conversationView = viewBinding.conversationView;
    titleBarView = viewBinding.titleBar;
    networkErrorView = viewBinding.errorTv;
    emptyView = viewBinding.emptyLayout;

    setViewHolderFactory(new ViewHolderFactory());
    loadUIConfig();
    // 设置标题栏点击事件
    viewBinding.titleBar.setRightImageClick(
        v -> {
          // 如果配置了标题栏右侧点击事件，则执行配置的点击事件
          if (LocalConversationKitClient.getConversationUIConfig() != null
              && LocalConversationKitClient.getConversationUIConfig().titleBarRightClick != null) {
            LocalConversationKitClient.getConversationUIConfig().titleBarRightClick.onClick(v);
            return;
          }
          // 如果配置了群组功能，则展示群组相关的弹窗
          if (IMKitConfigCenter.getEnableTeam()) {
            Context context = getContext();
            int memberLimit = ConversationConstant.MAX_TEAM_MEMBER;
            ContentListPopView contentListPopView =
                new ContentListPopView.Builder(context)
                    .addItem(PopItemFactory.getAddFriendItem(context))
                    .addItem(PopItemFactory.getCreateGroupTeamItem(context, memberLimit))
                    .addItem(PopItemFactory.getCreateAdvancedTeamItem(context, memberLimit))
                    .build();
            contentListPopView.showAsDropDown(
                v, (int) requireContext().getResources().getDimension(R.dimen.pop_margin_right), 0);
          } else {
            // 如果没有配置标题栏右侧点击事件，则默认跳转到添加好友页面
            XKitRouter.withKey(RouterConstant.PATH_ADD_FRIEND_PAGE)
                .withContext(requireContext())
                .navigate();
          }
        });

    // 设置标题栏右侧第二个图标点击事件
    viewBinding.titleBar.setRight2ImageClick(
        v -> {
          // 如果配置了标题栏右侧第二个图标点击事件，则执行配置的点击事件
          if (LocalConversationKitClient.getConversationUIConfig() != null
              && LocalConversationKitClient.getConversationUIConfig().titleBarRight2Click != null) {
            LocalConversationKitClient.getConversationUIConfig().titleBarRight2Click.onClick(v);
            return;
          }
          // 如果没有配置标题栏右侧第二个图标点击事件，则默认跳转到全局搜索页面
          XKitRouter.withKey(RouterConstant.PATH_GLOBAL_SEARCH_PAGE)
              .withContext(requireContext())
              .navigate();
        });
  }

  // 加载AI用户数据, 用于展示顶部横向滚动列表。父类数据拉取之后回调
  @Override
  public void loadAIUserData(FetchResult<List<AIUserBean>> result) {
    if (!IMKitConfigCenter.getEnableAIUser()) {
      return;
    }
    if (result.getLoadStatus() == LoadStatus.Success
        && result.getData() != null
        && result.getData().size() > 0) {
      ConversationHeaderBean aiBean = new ConversationHeaderBean(result.getData());
      conversationView.setHeaderData(List.of(aiBean));
    } else {
      conversationView.setHeaderData(null);
    }
  }

  // 加载UI配置, 用于设置标题栏、会话列表等UI
  private void loadUIConfig() {

    if (LocalConversationKitClient.getConversationUIConfig() == null) {
      return;
    }
    LocalConversationUIConfig config = LocalConversationKitClient.getConversationUIConfig();

    viewBinding.titleBar.setLeftImageClick(
        v -> {
          if (config.titleBarLeftClick != null) {
            config.titleBarLeftClick.onClick(v);
          }
        });

    if (config.conversationFactory != null) {
      setViewHolderFactory(config.conversationFactory);
    }

    if (titleBarView != null) {
      if (!config.showTitleBar) {
        titleBarView.setVisibility(View.GONE);
      } else {
        titleBarView.setVisibility(View.VISIBLE);
        titleBarView.setHeadImageVisible(config.showTitleBarLeftIcon ? View.VISIBLE : View.GONE);
        titleBarView.showRight2ImageView(config.showTitleBarRight2Icon);
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

        if (config.titleBarRight2Res != null) {
          titleBarView.setRight2ImageRes(config.titleBarRight2Res);
        }
      }

      if (config.customLayout != null) {
        config.customLayout.customizeConversationLayout(this);
      }
    }
  }

  // 获取标题栏
  public TitleBarView getTitleBar() {
    return viewBinding.titleBar;
  }

  // 获取顶部布局
  public LinearLayout getTopLayout() {
    return viewBinding.topLayout;
  }

  // 获取会话列表布局
  public LinearLayout getBodyLayout() {
    return viewBinding.bodyLayout;
  }

  // 获取底部布局，目前组件中没有使用，提供给开发者扩展使用。在整个会话页面最底部增加一些View的时候使用
  public FrameLayout getBottomLayout() {
    return viewBinding.bottomLayout;
  }

  // 获取会话列表的顶部布局，目前组件中没有使用，提供给开发者扩展使用。如果需要再会话列表顶部增加一些View，可以在这个布局中添加
  public FrameLayout getBodyTopLayout() {
    return viewBinding.bodyTopLayout;
  }

  // 获取展示错误信息的TextView，目前用于展示网络错误信息
  public TextView getErrorTextView() {
    return viewBinding.errorTv;
  }

  // 设置是否展示空数据的View，目前用于展示会话列表为空的情况
  public void setEmptyViewVisible(int visible) {
    viewBinding.emptyLayout.setVisibility(visible);
  }

  // 设置是否展示空数据的View，目前用于展示会话列表为空的情况
  public View getEmptyView() {
    return viewBinding.emptyLayout;
  }
}
