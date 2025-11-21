// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.conversationkit.local.ui.fun.page;

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
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.common.ui.widgets.ContentListPopView;
import com.netease.yunxin.kit.common.ui.widgets.TitleBarView;
import com.netease.yunxin.kit.common.utils.SizeUtils;
import com.netease.yunxin.kit.conversationkit.local.ui.LocalConversationKitClient;
import com.netease.yunxin.kit.conversationkit.local.ui.LocalConversationUIConfig;
import com.netease.yunxin.kit.conversationkit.local.ui.R;
import com.netease.yunxin.kit.conversationkit.local.ui.common.ConversationConstant;
import com.netease.yunxin.kit.conversationkit.local.ui.databinding.FunLocalConversationFragmentBinding;
import com.netease.yunxin.kit.conversationkit.local.ui.fun.FunPopItemFactory;
import com.netease.yunxin.kit.conversationkit.local.ui.fun.FunViewHolderFactory;
import com.netease.yunxin.kit.conversationkit.local.ui.model.AIUserBean;
import com.netease.yunxin.kit.conversationkit.local.ui.model.ConversationHeaderBean;
import com.netease.yunxin.kit.conversationkit.local.ui.page.LocalConversationBaseFragment;
import com.netease.yunxin.kit.corekit.im2.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import java.util.List;

/** 娱乐版会话列表Fragment, 用于展示娱乐版会话列表，继承自基础会话Fragment并扩展娱乐版UI特性 */
public class FunLocalConversationFragment extends LocalConversationBaseFragment {

  /** 视图绑定对象，用于安全访问布局文件中的UI组件，避免 findViewById 调用 */
  private FunLocalConversationFragmentBinding viewBinding;

  /**
   * 初始化视图并返回根视图 重写父类方法，通过视图绑定初始化布局，替代传统的XML inflate方式
   *
   * @param inflater 布局填充器，用于加载布局文件
   * @param container 父容器视图
   * @param savedInstanceState 保存的实例状态，用于恢复数据
   * @return 初始化后的根视图
   */
  @Override
  public View initViewAndGetRootView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    // 使用视图绑定加载布局，关联当前Fragment的视图层级
    viewBinding = FunLocalConversationFragmentBinding.inflate(inflater, container, false);
    // 初始化视图组件和事件绑定
    initView();
    // 返回绑定布局的根视图作为Fragment的内容视图
    return viewBinding.getRoot();
  }

  /** 初始化视图，父类处理业务逻辑，需要按照父类使用到的View进行初始化，保证父类业务逻辑正常展示 */
  private void initView() {
    // 将视图绑定中的组件赋值给父类定义的成员变量，供父类业务逻辑使用
    conversationView = viewBinding.conversationView; // 会话列表RecyclerView
    titleBarView = viewBinding.titleBar; // 标题栏
    networkErrorView = viewBinding.errorTv; // 网络错误提示文本
    emptyView = viewBinding.emptyLayout; // 空列表提示布局

    // 隐藏标题栏右侧第二个图标（默认不使用）
    viewBinding.titleBar.getRight2ImageView().setVisibility(View.GONE);

    // 设置娱乐版会话项视图工厂，用于创建会话列表项的ViewHolder
    setViewHolderFactory(new FunViewHolderFactory());
    // 为会话列表添加自定义分割线装饰
    viewBinding.conversationView.addItemDecoration(getItemDecoration());

    // 设置标题栏右侧按钮点击事件（"+"号菜单按钮）
    viewBinding.titleBar.setRightImageClick(
        v -> {
          // 优先使用外部配置的自定义点击事件（如果已设置）
          if (LocalConversationKitClient.getConversationUIConfig() != null
              && LocalConversationKitClient.getConversationUIConfig().titleBarRightClick != null) {
            LocalConversationKitClient.getConversationUIConfig().titleBarRightClick.onClick(v);
            return;
          }

          // 组件支持是否使用群功能（通过IM配置中心控制）
          if (IMKitConfigCenter.getEnableTeam()) {
            Context context = getContext();
            int memberLimit = ConversationConstant.MAX_TEAM_MEMBER; // 群成员上限
            // 构建功能弹窗（包含添加好友、创建群聊等选项）
            ContentListPopView contentListPopView =
                new ContentListPopView.Builder(context)
                    .addItem(FunPopItemFactory.getAddFriendItem(context)) // 添加好友选项
                    .addItem(FunPopItemFactory.getDivideLineItem(context)) // 分割线
                    .addItem(FunPopItemFactory.getSearchGroupTeamItem(context)) // 搜索群聊选项
                    .addItem(FunPopItemFactory.getDivideLineItem(context)) // 分割线
                    .addItem(
                        FunPopItemFactory.getCreateGroupTeamItem(context, memberLimit)) // 创建普通群聊
                    .addItem(FunPopItemFactory.getDivideLineItem(context)) // 分割线
                    .addItem(
                        FunPopItemFactory.getCreateAdvancedTeamItem(context, memberLimit)) // 创建高级群聊
                    .enableShadow(false) // 禁用阴影效果
                    .backgroundRes(R.drawable.fun_conversation_view_pop_bg) // 自定义背景
                    .build();
            // 在按钮下方显示弹窗，设置右侧偏移量
            contentListPopView.showAsDropDown(
                v, (int) requireContext().getResources().getDimension(R.dimen.pop_margin_right), 0);
          } else {
            // 若群功能关闭，仅显示"添加好友"功能，跳转至添加好友页面
            XKitRouter.withKey(RouterConstant.PATH_FUN_ADD_FRIEND_PAGE)
                .withContext(requireContext())
                .navigate();
          }
        });

    // 设置搜索布局点击事件（顶部搜索框）
    viewBinding.searchLayout.setOnClickListener(
        v -> {
          // 优先使用外部配置的自定义搜索点击事件
          if (LocalConversationKitClient.getConversationUIConfig() != null
              && LocalConversationKitClient.getConversationUIConfig().titleBarRight2Click != null) {
            LocalConversationKitClient.getConversationUIConfig().titleBarRight2Click.onClick(v);
            return;
          }
          // 默认跳转至全局搜索页面
          XKitRouter.withKey(RouterConstant.PATH_FUN_GLOBAL_SEARCH_PAGE)
              .withContext(requireContext())
              .navigate();
        });

    // 加载外部UI配置（如标题栏样式、自定义布局等）
    loadUIConfig();
  }

  /**
   * 加载AI用户数据（父类回调方法） 当父类完成AI用户数据获取后调用，用于更新会话列表顶部的AI用户头部
   *
   * @param result 数据获取结果，包含AI用户列表和加载状态
   */
  @Override
  public void loadAIUserData(FetchResult<List<AIUserBean>> result) {
    // 若AI用户功能未启用，直接返回
    if (!IMKitConfigCenter.getEnableAIUser()) {
      return;
    }
    // 数据加载成功且不为空时，创建头部数据并设置给会话列表
    if (result.getLoadStatus() == LoadStatus.Success
        && result.getData() != null
        && result.getData().size() > 0) {
      ConversationHeaderBean aiBean =
          new ConversationHeaderBean(result.getData()); // 包装AI用户数据为头部Bean
      conversationView.setHeaderData(List.of(aiBean)); // 设置头部数据到会话列表
    } else {
      // 数据加载失败或为空时，清除头部数据
      conversationView.setHeaderData(null);
    }
  }

  /**
   * 创建会话列表项分割线装饰 自定义RecyclerView分割线，实现左侧缩进的水平分割线效果
   *
   * @return 自定义的RecyclerView.ItemDecoration实例
   */
  public RecyclerView.ItemDecoration getItemDecoration() {
    return new RecyclerView.ItemDecoration() {
      final int topPadding = SizeUtils.dp2px(0.25f); // 分割线高度（0.25dp转换为像素）
      final int indent = SizeUtils.dp2px(76); // 左侧缩进（76dp，避开头像区域）

      @Override
      public void onDrawOver(
          @NonNull Canvas canvas, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        // 计算分割线绘制区域（左右边界）
        int left = parent.getPaddingLeft() + indent; // 左边界 = 父容器内边距 + 缩进
        int right = parent.getWidth() - parent.getPaddingRight(); // 右边界 = 父容器宽度 - 右内边距

        // 遍历可见item，绘制分割线（最后一个item不绘制）
        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount - 1; i++) {
          View child = parent.getChildAt(i); // 获取当前item视图
          RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

          // 计算分割线上下边界（位于当前item底部）
          int top = child.getBottom() + params.bottomMargin; // 上边界 = item底部 + item下外边距
          int bottom = top + topPadding; // 下边界 = 上边界 + 分割线高度

          // 创建画笔并设置分割线颜色
          Paint paint = new Paint();
          paint.setColor(
              parent.getResources().getColor(R.color.fun_conversation_item_divide_line_color));
          // 绘制分割线矩形
          canvas.drawRect(left, top, right, bottom, paint);
        }
      }
    };
  }

  /**
   * 加载UI配置 根据外部传入的{@link LocalConversationUIConfig}个性化配置，更新Fragment的UI展示特性 支持标题栏显隐、按钮样式、自定义布局等配置项
   */
  private void loadUIConfig() {
    LocalConversationUIConfig config = LocalConversationKitClient.getConversationUIConfig();
    if (config == null) { // 无配置时直接返回
      return;
    }

    // 设置标题栏左侧按钮点击事件（优先使用配置的自定义事件）
    viewBinding.titleBar.setLeftImageClick(
        v -> {
          if (config.titleBarLeftClick != null) {
            config.titleBarLeftClick.onClick(v);
          }
        });

    // 应用自定义会话项工厂（若配置）
    if (config.conversationFactory != null) {
      setViewHolderFactory(config.conversationFactory);
    }

    // 控制标题栏显隐
    if (!config.showTitleBar) {
      titleBarView.setVisibility(View.GONE);
    } else {
      titleBarView.setVisibility(View.VISIBLE);
      // 控制标题栏左侧图标显隐
      titleBarView.setHeadImageVisible(config.showTitleBarLeftIcon ? View.VISIBLE : View.GONE);
      // 控制标题栏右侧图标显隐
      titleBarView.showRightImageView(config.showTitleBarRightIcon);

      // 设置标题文本（若配置）
      if (config.titleBarTitle != null) {
        titleBarView.setTitle(config.titleBarTitle);
      }
      // 设置标题文本颜色（若配置）
      if (config.titleBarTitleColor != null) {
        titleBarView.setTitleColor(config.titleBarTitleColor);
      }
      // 设置左侧按钮图标（若配置）
      if (config.titleBarLeftRes != null) {
        titleBarView.setLeftImageRes(config.titleBarLeftRes);
      }
      // 设置右侧按钮图标（若配置）
      if (config.titleBarRightRes != null) {
        titleBarView.setRightImageRes(config.titleBarRightRes);
      }
    }

    // 应用自定义布局（若配置）
    if (config.customLayout != null) {
      config.customLayout.customizeConversationLayout(this);
    }
  }

  // 以下为布局组件 getter 方法，用于外部访问Fragment内部布局元素
  /**
   * 获取标题栏组件
   *
   * @return 标题栏{@link TitleBarView}实例
   */
  @Override
  public TitleBarView getTitleBar() {
    return viewBinding.titleBar;
  }

  /**
   * 获取顶部布局容器
   *
   * @return 顶部布局{@link LinearLayout}实例
   */
  @Override
  public LinearLayout getTopLayout() {
    return viewBinding.topLayout;
  }

  /**
   * 获取主体内容布局容器（包含会话列表）
   *
   * @return 主体布局{@link LinearLayout}实例
   */
  @Override
  public LinearLayout getBodyLayout() {
    return viewBinding.bodyLayout;
  }

  /**
   * 获取底部布局容器
   *
   * @return 底部布局{@link FrameLayout}实例
   */
  @Override
  public FrameLayout getBottomLayout() {
    return viewBinding.bottomLayout;
  }

  /**
   * 获取主体顶部布局容器（位于标题栏与会话列表之间） 用于在会话列表上方插入自定义UI元素（如公告、快捷入口等）
   *
   * @return 主体顶部布局{@link FrameLayout}实例
   */
  @Override
  public FrameLayout getBodyTopLayout() {
    return viewBinding.bodyTopLayout;
  }

  /**
   * 获取错误信息文本组件 用于展示网络错误、加载失败等提示信息
   *
   * @return 错误提示{@link TextView}实例
   */
  @Override
  public TextView getErrorTextView() {
    return viewBinding.errorTv;
  }

  /**
   * 设置空布局可见性 当会话列表为空时，控制空状态提示布局的显示/隐藏
   *
   * @param visible 可见性标识（{@link View#VISIBLE} / {@link View#GONE} / {@link View#INVISIBLE}）
   */
  @Override
  public void setEmptyViewVisible(int visible) {
    viewBinding.emptyLayout.setVisibility(visible);
  }

  /**
   * 获取空布局组件 当会话列表为空时展示的提示布局（包含文本和图片）
   *
   * @return 空布局{@link View}实例
   */
  @Override
  public View getEmptyView() {
    return viewBinding.emptyLayout;
  }
}
