// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.normal.viewholder;

import android.content.Context;
import android.view.View;
import androidx.annotation.NonNull;
import com.netease.nimlib.sdk.v2.message.V2NIMMessage;
import com.netease.nimlib.sdk.v2.message.enums.V2NIMMessageType;
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
      String display = getDisplayText(viewBinding.getRoot().getContext(), data.getMessage());
      if (data.getMessage().getMessageType() == V2NIMMessageType.V2NIM_MESSAGE_TYPE_TEXT) {
        EllipsizeUtils.ellipsizeAndHighlight(
            viewBinding.tvMessage,
            display,
            data.getKeyword(),
            viewBinding.getRoot().getContext().getResources().getColor(R.color.color_337eff),
            true,
            true);
      } else {
        viewBinding.tvMessage.setText(display);
      }
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

  private String getDisplayText(Context context, V2NIMMessage message) {
    V2NIMMessageType type = message.getMessageType();
    if (type == V2NIMMessageType.V2NIM_MESSAGE_TYPE_TEXT) {
      return message.getText();
    } else if (type == V2NIMMessageType.V2NIM_MESSAGE_TYPE_FILE) {
      return context.getString(R.string.msg_type_file);
    } else if (type == V2NIMMessageType.V2NIM_MESSAGE_TYPE_IMAGE) {
      return context.getString(R.string.msg_type_image);
    } else if (type == V2NIMMessageType.V2NIM_MESSAGE_TYPE_AUDIO) {
      return context.getString(R.string.msg_type_audio);
    } else if (type == V2NIMMessageType.V2NIM_MESSAGE_TYPE_LOCATION) {
      return context.getString(R.string.msg_type_location);
    } else if (type == V2NIMMessageType.V2NIM_MESSAGE_TYPE_VIDEO) {
      return context.getString(R.string.msg_type_video);
    } else if (type == V2NIMMessageType.V2NIM_MESSAGE_TYPE_CUSTOM) {
      return context.getString(R.string.msg_type_custom);
    } else if (type == V2NIMMessageType.V2NIM_MESSAGE_TYPE_CALL) {
      return context.getString(R.string.msg_type_rtc_audio);
    } else {
      return context.getString(R.string.msg_type_no_tips);
    }
  }
}
