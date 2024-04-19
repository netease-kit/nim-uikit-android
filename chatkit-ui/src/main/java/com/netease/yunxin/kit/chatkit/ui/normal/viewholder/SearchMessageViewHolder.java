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
import com.netease.yunxin.kit.common.ui.utils.AvatarColor;
import com.netease.yunxin.kit.common.ui.utils.TimeFormatUtils;
import com.netease.yunxin.kit.common.ui.viewholder.BaseViewHolder;

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
      viewBinding.tvMessage.setText(
          data.getSpannableString(
              viewBinding.getRoot().getContext().getResources().getColor(R.color.color_337eff)));
      viewBinding.tvTime.setText(
          TimeFormatUtils.formatMillisecond(viewBinding.getRoot().getContext(), data.getTime()));

      viewBinding.getRoot().setOnClickListener(v -> itemListener.onClick(v, data, position));
    }
  }

  private void setUserInfo(ChatSearchBean data) {
    //get nick name
    //todo 如果缓存不存在处理
    //    if (userInfo == null) {
    //      V2ContactRepo.getFriendInfoList(
    //          data.getMessage().getSenderId(),
    //          new FetchCallback<UserInfo>() {
    //            @Override
    //            public void onSuccess(@Nullable UserInfo param) {
    //              List<UserInfo> userInfoList = new ArrayList<>();
    //              userInfoList.add(param);
    //              ChatUserCache.addUserInfo(userInfoList);
    //              loadNickAndAvatar(data, param);
    //            }
    //
    //            @Override
    //            public void onFailed(int code) {
    //              loadNickAndAvatar(data, null);
    //            }
    //
    //            @Override
    //            public void onException(@Nullable Throwable exception) {
    //              loadNickAndAvatar(data, null);
    //            }
    //          });
    //    } else {
    loadNickAndAvatar(data);
    //    }
  }

  private void loadNickAndAvatar(ChatSearchBean data) {
    String name = MessageHelper.getChatMessageUserNameByAccount(data.getAccount());
    String avatar = MessageHelper.getChatCacheAvatar(data.getAccount());
    String avatarName = MessageHelper.getChatCacheAvatarName(data.getAccount());
    viewBinding.cavIcon.setData(avatar, avatarName, AvatarColor.avatarColor(data.getAccount()));
    viewBinding.tvNickName.setText(name);
  }
}
