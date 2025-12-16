// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.conversationkit.ui.normal.viewholder;

import android.graphics.drawable.Drawable;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import com.netease.yunxin.kit.common.ui.utils.AppLanguageConfig;
import com.netease.yunxin.kit.common.ui.viewholder.BaseViewHolder;
import com.netease.yunxin.kit.conversationkit.ui.ConversationKitClient;
import com.netease.yunxin.kit.conversationkit.ui.ConversationUIConfig;
import com.netease.yunxin.kit.conversationkit.ui.R;
import com.netease.yunxin.kit.conversationkit.ui.common.ConversationUtils;
import com.netease.yunxin.kit.conversationkit.ui.databinding.ConversationViewHolderBinding;
import com.netease.yunxin.kit.conversationkit.ui.model.ConversationBean;
import com.netease.yunxin.kit.corekit.coexist.im2.IMKitClient;
import com.netease.yunxin.kit.corekit.coexist.im2.utils.TimeFormatLocalUtils;
import java.util.Locale;

/** 普通版会话列表基础ViewHolder 加载会话列表的基础UI，包括头像、名称、消息内容、时间、未读数、置顶状态等 */
public class ConversationBaseViewHolder extends BaseViewHolder<ConversationBean> {

  protected ConversationViewHolderBinding viewBinding;
  protected Drawable stickTopDrawable;
  protected Drawable itemDrawable;

  public ConversationBaseViewHolder(@NonNull ConversationViewHolderBinding binding) {
    super(binding.getRoot());
    viewBinding = binding;
  }

  @Override
  public void onBindData(ConversationBean data, int position) {
    loadUIConfig();

    // 置顶信息，ConversationBean中保存的是在UI操作结果后的置顶状态，如果没有置顶状态，则使用infoData中的置顶状态
    boolean isStickTop = data.isStickTop() != null ? data.isStickTop() : data.infoData.isStickTop();
    if (isStickTop) {
      viewBinding.rootView.setBackground(stickTopDrawable);
    } else {
      viewBinding.rootView.setBackground(itemDrawable);
    }

    viewBinding.messageTv.setText(
        ConversationUtils.getConversationText(viewBinding.getRoot().getContext(), data.infoData));
    Locale locale =
        new Locale(
            AppLanguageConfig.getInstance().getAppLanguage(IMKitClient.getApplicationContext()));
    viewBinding.timeTv.setText(
        TimeFormatLocalUtils.formatMillisecond(
            viewBinding.getRoot().getContext(), data.getLastMsgTime(), locale));

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

    viewBinding.contentLayout.setOnClickListener(v -> itemListener.onClick(v, data, position));
    viewBinding.contentLayout.setOnLongClickListener(
        v -> itemListener.onLongClick(v, data, position));
    viewBinding.avatarLayout.setOnClickListener(v -> itemListener.onAvatarClick(v, data, position));
    viewBinding.avatarLayout.setOnLongClickListener(
        v -> itemListener.onAvatarLongClick(v, data, position));
  }

  /** 加载UI配置,包括字体颜色、大小、背景等 */
  private void loadUIConfig() {
    itemDrawable =
        ResourcesCompat.getDrawable(
            viewBinding.getRoot().getContext().getResources(),
            R.drawable.conversation_view_holder_selector,
            null);

    stickTopDrawable =
        ResourcesCompat.getDrawable(
            viewBinding.getRoot().getContext().getResources(),
            R.drawable.conversation_view_holder_stick_selector,
            null);
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
