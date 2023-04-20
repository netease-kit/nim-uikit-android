// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.conversationkit.ui.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import androidx.annotation.NonNull;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.yunxin.kit.common.ui.utils.AvatarColor;
import com.netease.yunxin.kit.common.ui.utils.TimeFormatUtils;
import com.netease.yunxin.kit.common.ui.viewholder.BaseViewHolder;
import com.netease.yunxin.kit.conversationkit.ui.ConversationKitClient;
import com.netease.yunxin.kit.conversationkit.ui.ConversationUIConfig;
import com.netease.yunxin.kit.conversationkit.ui.R;
import com.netease.yunxin.kit.conversationkit.ui.common.ConversationHelper;
import com.netease.yunxin.kit.conversationkit.ui.common.ConversationUtils;
import com.netease.yunxin.kit.conversationkit.ui.databinding.TeamViewHolderLayoutBinding;
import com.netease.yunxin.kit.conversationkit.ui.model.ConversationBean;

public class TeamViewHolder extends BaseViewHolder<ConversationBean> {

  private TeamViewHolderLayoutBinding viewBinding;
  private Drawable stickTopDrawable;
  private Drawable itemDrawable;

  public TeamViewHolder(@NonNull TeamViewHolderLayoutBinding binding) {
    super(binding.getRoot());
    viewBinding = binding;
  }

  @SuppressLint("UseCompatLoadingForDrawables")
  @Override
  public void onBindData(ConversationBean data, int position) {
    Context context = viewBinding.getRoot().getContext();
    loadUIConfig();
    if (data.infoData.getTeamInfo() != null) {
      Team teamInfo = data.infoData.getTeamInfo();
      viewBinding.avatarView.setData(
          teamInfo.getIcon(), teamInfo.getName(), AvatarColor.avatarColor(teamInfo.getId()));
      viewBinding.conversationNameTv.setText(teamInfo.getName());
    }
    if (data.infoData.isStickTop()) {
      viewBinding.rootView.setBackground(stickTopDrawable);
    } else {
      viewBinding.rootView.setBackground(itemDrawable);
    }

    if (data.infoData.getMute()) {
      viewBinding.conversationMuteIv.setVisibility(View.VISIBLE);
      viewBinding.conversationUnreadTv.setVisibility(View.GONE);
    } else {
      viewBinding.conversationMuteIv.setVisibility(View.GONE);
      if (data.infoData.getUnreadCount() > 0) {
        int count = data.infoData.getUnreadCount();
        String content;
        if (count >= 100) {
          content = "99+";
        } else {
          content = String.valueOf(count);
        }
        viewBinding.conversationUnreadTv.setText(content);
        viewBinding.conversationUnreadTv.setVisibility(View.VISIBLE);
      } else {
        viewBinding.conversationUnreadTv.setVisibility(View.GONE);
      }
    }

    if (ConversationHelper.hasAit(data.infoData.getContactId())
        && data.infoData.getUnreadCount() > 0) {
      viewBinding.conversationAitTv.setVisibility(View.VISIBLE);
    } else {
      viewBinding.conversationAitTv.setVisibility(View.GONE);
    }
    viewBinding.conversationMessageTv.setText(
        ConversationUtils.getConversationText(context, data.infoData));
    viewBinding.conversationTime.setText(
        TimeFormatUtils.formatMillisecond(context, data.infoData.getTime()));
    viewBinding.conversationTimeFl.setOnClickListener(v -> itemListener.onClick(data, position));
    viewBinding.conversationTimeFl.setOnLongClickListener(
        v -> itemListener.onLongClick(data, position));
    viewBinding.conversationAvatarFl.setOnClickListener(
        v -> itemListener.onAvatarClick(data, position));
    viewBinding.conversationAvatarFl.setOnLongClickListener(
        v -> itemListener.onAvatarLongClick(data, position));
  }

  private void loadUIConfig() {
    if (ConversationKitClient.getConversationUIConfig() != null) {
      ConversationUIConfig config = ConversationKitClient.getConversationUIConfig();
      if (config.itemTitleColor != ConversationUIConfig.INT_DEFAULT_NULL) {
        viewBinding.conversationNameTv.setTextColor(config.itemTitleColor);
      }
      if (config.itemTitleSize != ConversationUIConfig.INT_DEFAULT_NULL) {
        viewBinding.conversationNameTv.setTextSize(config.itemTitleSize);
      }

      if (config.itemContentColor != ConversationUIConfig.INT_DEFAULT_NULL) {
        viewBinding.conversationMessageTv.setTextColor(config.itemContentColor);
      }
      if (config.itemContentSize != ConversationUIConfig.INT_DEFAULT_NULL) {
        viewBinding.conversationMessageTv.setTextSize(config.itemContentSize);
      }

      if (config.itemDateColor != ConversationUIConfig.INT_DEFAULT_NULL) {
        viewBinding.conversationTime.setTextColor(config.itemDateColor);
      }
      if (config.itemDateSize != ConversationUIConfig.INT_DEFAULT_NULL) {
        viewBinding.conversationTime.setTextSize(config.itemDateSize);
      }

      if (config.avatarCornerRadius != ConversationUIConfig.INT_DEFAULT_NULL) {
        viewBinding.avatarView.setCornerRadius(config.avatarCornerRadius);
      }
      if (config.itemBackground != null) {
        itemDrawable = config.itemBackground;
      }
      if (config.itemStickTopBackground != null) {
        stickTopDrawable = config.itemStickTopBackground;
      }
    } else {
      itemDrawable =
          viewBinding.getRoot().getContext().getDrawable(R.drawable.view_normal_selector);
      stickTopDrawable =
          viewBinding.getRoot().getContext().getDrawable(R.drawable.view_select_selector);
    }
  }
}
