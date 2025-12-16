// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.conversationkit.local.ui.fun.viewholder;

import android.graphics.drawable.Drawable;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import com.netease.yunxin.kit.common.ui.utils.AppLanguageConfig;
import com.netease.yunxin.kit.common.ui.viewholder.BaseViewHolder;
import com.netease.yunxin.kit.conversationkit.local.ui.LocalConversationKitClient;
import com.netease.yunxin.kit.conversationkit.local.ui.LocalConversationUIConfig;
import com.netease.yunxin.kit.conversationkit.local.ui.R;
import com.netease.yunxin.kit.conversationkit.local.ui.common.ConversationUtils;
import com.netease.yunxin.kit.conversationkit.local.ui.databinding.FunLocalConversationViewHolderBinding;
import com.netease.yunxin.kit.conversationkit.local.ui.model.ConversationBean;
import com.netease.yunxin.kit.corekit.coexist.im2.IMKitClient;
import com.netease.yunxin.kit.corekit.coexist.im2.utils.TimeFormatLocalUtils;
import java.util.Locale;

/** 会话列表基础ViewHolder，用于加载通用的UI 置顶、未读、免打扰、头像、会话名称、最后一条消息、时间 */
public class FunConversationBaseViewHolder extends BaseViewHolder<ConversationBean> {

  /** 视图绑定对象，用于安全访问会话列表项布局中的UI组件 */
  protected FunLocalConversationViewHolderBinding viewBinding;
  /** 置顶会话项的背景Drawable */
  protected Drawable stickTopDrawable;
  /** 普通会话项的背景Drawable */
  protected Drawable itemDrawable;

  /**
   * 构造函数，初始化视图绑定
   *
   * @param binding 会话列表项的视图绑定对象，包含布局中的所有UI组件引用
   */
  public FunConversationBaseViewHolder(@NonNull FunLocalConversationViewHolderBinding binding) {
    super(binding.getRoot()); // 调用父类构造函数，传入根视图
    viewBinding = binding; // 保存视图绑定对象引用
  }

  /**
   * 绑定会话数据到视图 根据ConversationBean中的数据更新UI组件状态，包括置顶、未读、免打扰等状态展示
   *
   * @param data 会话数据模型，包含会话基本信息和UI状态
   * @param position 当前项在列表中的位置
   */
  @Override
  public void onBindData(ConversationBean data, int position) {
    // 加载UI配置（背景、颜色等）
    loadUIConfig();

    // 确定置顶状态：优先使用UI操作后的状态，若无则使用原始数据中的状态
    boolean isStickTop = data.isStickTop() != null ? data.isStickTop() : data.infoData.isStickTop();
    // 根据置顶状态设置会话项背景
    if (isStickTop) {
      viewBinding.rootLayout.setBackground(stickTopDrawable); // 置顶项背景
    } else {
      viewBinding.rootLayout.setBackground(itemDrawable); // 普通项背景
    }

    // 处理消息免打扰状态
    if (data.infoData.isMute()) {
      // 免打扰时显示免打扰图标，隐藏未读计数
      viewBinding.muteIv.setVisibility(View.VISIBLE);
      viewBinding.unreadTv.setVisibility(View.GONE);
    } else {
      // 非免打扰时隐藏免打扰图标，根据未读数量显示未读计数
      viewBinding.muteIv.setVisibility(View.GONE);
      if (data.infoData.getUnreadCount() > 0) {
        // 未读数量≥100时显示"99+"，否则显示具体数字
        String content =
            data.infoData.getUnreadCount() >= 100
                ? "99+"
                : String.valueOf(data.infoData.getUnreadCount());
        viewBinding.unreadTv.setText(content);
        viewBinding.unreadTv.setVisibility(View.VISIBLE);
      } else {
        viewBinding.unreadTv.setVisibility(View.GONE); // 无未读时隐藏计数
      }
    }

    // 设置最后一条消息内容（通过工具类获取格式化文本）
    viewBinding.messageTv.setText(
        ConversationUtils.getConversationText(itemView.getContext(), data.infoData));

    // 设置消息时间（根据应用语言配置格式化时间戳）
    Locale locale =
        new Locale(
            AppLanguageConfig.getInstance().getAppLanguage(IMKitClient.getApplicationContext()));
    viewBinding.timeTv.setText(
        TimeFormatLocalUtils.formatMillisecond(
            viewBinding.getRoot().getContext(), data.getLastMsgTime(), locale));

    // 设置项点击事件（触发父类定义的点击监听器）
    viewBinding.getRoot().setOnClickListener(v -> itemListener.onClick(v, data, position));
    // 设置项长按事件
    viewBinding.getRoot().setOnLongClickListener(v -> itemListener.onLongClick(v, data, position));
    // 设置头像点击事件
    viewBinding.avatarLayout.setOnClickListener(v -> itemListener.onAvatarClick(v, data, position));
    // 设置头像长按事件
    viewBinding.avatarLayout.setOnLongClickListener(
        v -> itemListener.onAvatarLongClick(v, data, position));
  }

  /** 加载UI配置 初始化默认背景Drawable，并应用外部配置的个性化UI参数（如文本颜色、大小、头像圆角等） */
  private void loadUIConfig() {
    // 初始化默认背景Drawable（使用ContextCompat确保兼容性）
    itemDrawable =
        ContextCompat.getDrawable(
            viewBinding.getRoot().getContext(),
            R.drawable.fun_conversation_view_holder_selector // 普通项背景选择器
            );
    stickTopDrawable =
        ContextCompat.getDrawable(
            viewBinding.getRoot().getContext(),
            R.drawable.fun_conversation_view_holder_stick_selector // 置顶项背景选择器
            );

    // 应用外部自定义UI配置（若存在）
    if (LocalConversationKitClient.getConversationUIConfig() != null) {
      LocalConversationUIConfig config = LocalConversationKitClient.getConversationUIConfig();

      // 应用会话名称文本颜色配置
      if (config.itemTitleColor != null) {
        viewBinding.nameTv.setTextColor(config.itemTitleColor);
      }
      // 应用会话名称文本大小配置
      if (config.itemTitleSize != null) {
        viewBinding.nameTv.setTextSize(config.itemTitleSize);
      }

      // 应用消息内容文本颜色配置
      if (config.itemContentColor != null) {
        viewBinding.messageTv.setTextColor(config.itemContentColor);
      }
      // 应用消息内容文本大小配置
      if (config.itemContentSize != null) {
        viewBinding.messageTv.setTextSize(config.itemContentSize);
      }

      // 应用时间文本颜色配置
      if (config.itemDateColor != null) {
        viewBinding.timeTv.setTextColor(config.itemDateColor);
      }
      // 应用时间文本大小配置
      if (config.itemDateSize != null) {
        viewBinding.timeTv.setTextSize(config.itemDateSize);
      }

      // 应用头像圆角配置
      if (config.avatarCornerRadius != null) {
        viewBinding.avatarView.setCornerRadius(config.avatarCornerRadius);
      }
      // 覆盖普通项背景（若配置）
      if (config.itemBackground != null) {
        itemDrawable = config.itemBackground;
      }
      // 覆盖置顶项背景（若配置）
      if (config.itemStickTopBackground != null) {
        stickTopDrawable = config.itemStickTopBackground;
      }
    }
  }
}
