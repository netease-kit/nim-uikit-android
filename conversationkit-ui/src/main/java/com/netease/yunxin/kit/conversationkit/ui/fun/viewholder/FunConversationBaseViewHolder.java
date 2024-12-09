// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.conversationkit.ui.fun.viewholder;

import android.graphics.drawable.Drawable;
import android.view.View;
import androidx.annotation.NonNull;
import com.netease.yunxin.kit.common.ui.utils.AppLanguageConfig;
import com.netease.yunxin.kit.common.ui.viewholder.BaseViewHolder;
import com.netease.yunxin.kit.conversationkit.ui.ConversationKitClient;
import com.netease.yunxin.kit.conversationkit.ui.ConversationUIConfig;
import com.netease.yunxin.kit.conversationkit.ui.R;
import com.netease.yunxin.kit.conversationkit.ui.common.ConversationUtils;
import com.netease.yunxin.kit.conversationkit.ui.databinding.FunConversationViewHolderBinding;
import com.netease.yunxin.kit.conversationkit.ui.model.ConversationBean;
import com.netease.yunxin.kit.corekit.im2.IMKitClient;
import com.netease.yunxin.kit.corekit.im2.utils.TimeFormatLocalUtils;
import java.util.Locale;

/** 会话列表基础ViewHolder，用于加载通用的UI 置顶、未读、免打扰、头像、会话名称、最后一条消息、时间 */
public class FunConversationBaseViewHolder extends BaseViewHolder<ConversationBean> {

  protected FunConversationViewHolderBinding viewBinding;
  protected Drawable stickTopDrawable;
  protected Drawable itemDrawable;

  public FunConversationBaseViewHolder(@NonNull FunConversationViewHolderBinding binding) {
    super(binding.getRoot());
    viewBinding = binding;
  }

  @Override
  public void onBindData(ConversationBean data, int position) {
    loadUIConfig();
    // 置顶信息，ConversationBean中保存的是在UI操作结果后的置顶状态，如果没有置顶状态，则使用infoData中的置顶状态
    boolean isStickTop = data.isStickTop() != null ? data.isStickTop() : data.infoData.isStickTop();
    if (isStickTop) {
      viewBinding.rootLayout.setBackground(stickTopDrawable);
    } else {
      viewBinding.rootLayout.setBackground(itemDrawable);
    }
    // 消息免打扰
    if (data.infoData.isMute()) {
      viewBinding.muteIv.setVisibility(View.VISIBLE);
      viewBinding.unreadTv.setVisibility(View.GONE);
    } else {
      viewBinding.muteIv.setVisibility(View.GONE);
      if (data.infoData.getUnreadCount() > 0) {
        int count = data.infoData.getUnreadCount();
        String content;
        if (count >= 100) {
          content = "99+";
        } else {
          content = String.valueOf(count);
        }
        viewBinding.unreadTv.setText(content);
        viewBinding.unreadTv.setVisibility(View.VISIBLE);
      } else {
        viewBinding.unreadTv.setVisibility(View.GONE);
      }
    }
    // 最近一条消息
    viewBinding.messageTv.setText(
        ConversationUtils.getConversationText(itemView.getContext(), data.infoData));
    Locale locale =
        new Locale(
            AppLanguageConfig.getInstance().getAppLanguage(IMKitClient.getApplicationContext()));
    viewBinding.timeTv.setText(
        TimeFormatLocalUtils.formatMillisecond(
            viewBinding.getRoot().getContext(), data.getLastMsgTime(), locale));
    viewBinding.getRoot().setOnClickListener(v -> itemListener.onClick(v, data, position));
    viewBinding.getRoot().setOnLongClickListener(v -> itemListener.onLongClick(v, data, position));
    viewBinding.avatarLayout.setOnClickListener(v -> itemListener.onAvatarClick(v, data, position));
    viewBinding.avatarLayout.setOnLongClickListener(
        v -> itemListener.onAvatarLongClick(v, data, position));
  }

  /** 加载UI配置，通过{@link ConversationKitClient#getConversationUIConfig()}获取配置信息 */
  private void loadUIConfig() {
    itemDrawable =
        viewBinding
            .getRoot()
            .getContext()
            .getDrawable(R.drawable.fun_conversation_view_holder_selector);
    stickTopDrawable =
        viewBinding
            .getRoot()
            .getContext()
            .getDrawable(R.drawable.fun_conversation_view_holder_stick_selector);
    if (ConversationKitClient.getConversationUIConfig() != null) {
      ConversationUIConfig config = ConversationKitClient.getConversationUIConfig();
      if (config.itemTitleColor != null) {
        viewBinding.nameTv.setTextColor(config.itemTitleColor);
      }
      if (config.itemTitleSize != null) {
        viewBinding.nameTv.setTextSize(config.itemTitleSize);
      }

      if (config.itemContentColor != null) {
        viewBinding.messageTv.setTextColor(config.itemContentColor);
      }
      if (config.itemContentSize != null) {
        viewBinding.messageTv.setTextSize(config.itemContentSize);
      }

      if (config.itemDateColor != null) {
        viewBinding.timeTv.setTextColor(config.itemDateColor);
      }
      if (config.itemDateSize != null) {
        viewBinding.timeTv.setTextSize(config.itemDateSize);
      }

      if (config.avatarCornerRadius != null) {
        viewBinding.avatarView.setCornerRadius(config.avatarCornerRadius);
      }
      if (config.itemBackground != null) {
        itemDrawable = config.itemBackground;
      }
      if (config.itemStickTopBackground != null) {
        stickTopDrawable = config.itemStickTopBackground;
      }
    }
  }
}
