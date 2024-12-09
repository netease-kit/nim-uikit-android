// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.normal.viewholder;

import android.view.View;
import androidx.annotation.NonNull;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.common.MessageHelper;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatSearchItemLayoutBinding;
import com.netease.yunxin.kit.chatkit.ui.model.ChatSearchBean;
import com.netease.yunxin.kit.chatkit.ui.view.input.ActionConstants;
import com.netease.yunxin.kit.common.ui.utils.AppLanguageConfig;
import com.netease.yunxin.kit.common.ui.utils.AvatarColor;
import com.netease.yunxin.kit.common.ui.utils.EllipsizeUtils;
import com.netease.yunxin.kit.common.ui.viewholder.BaseViewHolder;
import com.netease.yunxin.kit.corekit.im2.IMKitClient;
import com.netease.yunxin.kit.corekit.im2.utils.TimeFormatLocalUtils;
import java.util.List;
import java.util.Locale;

public class SearchMessageViewHolder extends BaseViewHolder<ChatSearchBean> {

  private ChatSearchItemLayoutBinding viewBinding;

  public SearchMessageViewHolder(@NonNull View itemView) {
    super(itemView);
  }

  public SearchMessageViewHolder(@NonNull ChatSearchItemLayoutBinding viewBinding) {
    this(viewBinding.getRoot());
    this.viewBinding = viewBinding;
  }

  @Override
  public void onBindData(ChatSearchBean data, int position) {
    if (data != null) {
      setUserInfo(data);
      EllipsizeUtils.ellipsizeAndHighlight(
          viewBinding.tvMessage,
          data.getMessage().getText(),
          data.getKeyword(),
          viewBinding.getRoot().getContext().getResources().getColor(R.color.color_337eff),
          true,
          true);
      viewBinding.tvTime.setText(
          TimeFormatLocalUtils.formatMillisecond(
              viewBinding.getRoot().getContext(),
              data.getTime(),
              new Locale(
                  AppLanguageConfig.getInstance()
                      .getAppLanguage(IMKitClient.getApplicationContext()))));

      viewBinding.getRoot().setOnClickListener(v -> itemListener.onClick(v, data, position));
    }
  }

  @Override
  public void onBindData(ChatSearchBean data, int position, @NonNull List<Object> payloads) {
    super.onBindData(data, position, payloads);
    if (payloads.contains(ActionConstants.PAYLOAD_USERINFO)) {
      setUserInfo(data);
    }
  }

  private void setUserInfo(ChatSearchBean data) {
    loadNickAndAvatar(data);
  }

  private void loadNickAndAvatar(ChatSearchBean data) {
    String name =
        MessageHelper.getChatMessageUserNameByAccount(
            data.getAccount(), data.getMessage().getConversationType());
    String avatar =
        MessageHelper.getChatCacheAvatar(
            data.getAccount(), data.getMessage().getConversationType());
    String avatarName =
        MessageHelper.getChatCacheAvatarName(
            data.getAccount(), data.getMessage().getConversationType());
    viewBinding.cavIcon.setData(avatar, avatarName, AvatarColor.avatarColor(data.getAccount()));
    viewBinding.tvNickName.setText(name);
  }
}
