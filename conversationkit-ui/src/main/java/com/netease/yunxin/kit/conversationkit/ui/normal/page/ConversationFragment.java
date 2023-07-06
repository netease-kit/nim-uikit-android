// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.conversationkit.ui.normal.page;

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
import com.netease.yunxin.kit.common.ui.widgets.ContentListPopView;
import com.netease.yunxin.kit.common.ui.widgets.TitleBarView;
import com.netease.yunxin.kit.conversationkit.ui.ConversationKitClient;
import com.netease.yunxin.kit.conversationkit.ui.ConversationUIConfig;
import com.netease.yunxin.kit.conversationkit.ui.ConversationUIConstant;
import com.netease.yunxin.kit.conversationkit.ui.R;
import com.netease.yunxin.kit.conversationkit.ui.databinding.ConversationFragmentBinding;
import com.netease.yunxin.kit.conversationkit.ui.normal.PopItemFactory;
import com.netease.yunxin.kit.conversationkit.ui.normal.ViewHolderFactory;
import com.netease.yunxin.kit.conversationkit.ui.page.ConversationBaseFragment;
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;

/** conversation list fragment show your recent conversation */
public class ConversationFragment extends ConversationBaseFragment {

  private final String TAG = "ConversationFragment";
  protected ConversationFragmentBinding viewBinding;

  @Override
  public View initViewAndGetRootView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    viewBinding = ConversationFragmentBinding.inflate(inflater, container, false);
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
    viewBinding.titleBar.setRightImageClick(
        v -> {
          if (ConversationKitClient.getConversationUIConfig() != null
              && ConversationKitClient.getConversationUIConfig().titleBarRightClick != null) {
            ConversationKitClient.getConversationUIConfig().titleBarRightClick.onClick(v);
            return;
          }
          Context context = getContext();
          int memberLimit = ConversationUIConstant.MAX_TEAM_MEMBER;
          ContentListPopView contentListPopView =
              new ContentListPopView.Builder(context)
                  .addItem(PopItemFactory.getAddFriendItem(context))
                  .addItem(PopItemFactory.getCreateGroupTeamItem(context, memberLimit))
                  .addItem(PopItemFactory.getCreateAdvancedTeamItem(context, memberLimit))
                  .build();
          contentListPopView.showAsDropDown(
              v, (int) requireContext().getResources().getDimension(R.dimen.pop_margin_right), 0);
        });

    viewBinding.titleBar.setRight2ImageClick(
        v -> {
          if (ConversationKitClient.getConversationUIConfig() != null
              && ConversationKitClient.getConversationUIConfig().titleBarRight2Click != null) {
            ConversationKitClient.getConversationUIConfig().titleBarRight2Click.onClick(v);
            return;
          }
          XKitRouter.withKey(RouterConstant.PATH_GLOBAL_SEARCH_PAGE)
              .withContext(requireContext())
              .navigate();
        });
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
