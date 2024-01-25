// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.fun.viewholder;

import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.netease.yunxin.kit.chatkit.repo.ContactRepo;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.common.ChatUserCache;
import com.netease.yunxin.kit.chatkit.ui.common.MessageHelper;
import com.netease.yunxin.kit.chatkit.ui.databinding.FunChatSearchViewHolderBinding;
import com.netease.yunxin.kit.chatkit.ui.model.ChatSearchBean;
import com.netease.yunxin.kit.common.ui.utils.AvatarColor;
import com.netease.yunxin.kit.common.ui.utils.TimeFormatUtils;
import com.netease.yunxin.kit.common.ui.viewholder.BaseViewHolder;
import com.netease.yunxin.kit.corekit.im.model.UserInfo;
import com.netease.yunxin.kit.corekit.im.provider.FetchCallback;
import java.util.ArrayList;
import java.util.List;

public class FunSearchMessageViewHolder extends BaseViewHolder<ChatSearchBean> {

  private FunChatSearchViewHolderBinding viewBinding;

  public FunSearchMessageViewHolder(@NonNull View itemView) {
    super(itemView);
  }

  public FunSearchMessageViewHolder(@NonNull FunChatSearchViewHolderBinding viewBinding) {
    this(viewBinding.getRoot());
    this.viewBinding = viewBinding;
  }

  @Override
  public void onBindData(ChatSearchBean data, int position) {
    if (data != null) {
      setUserInfo(data);
      viewBinding.messageTv.setText(
          data.getSpannableString(
              viewBinding
                  .getRoot()
                  .getContext()
                  .getResources()
                  .getColor(R.color.fun_chat_search_message_hit_color)));

      viewBinding.timeTv.setText(
          TimeFormatUtils.formatMillisecond(viewBinding.getRoot().getContext(), data.getTime()));

      viewBinding.getRoot().setOnClickListener(v -> itemListener.onClick(v, data, position));
    }
  }

  private void setUserInfo(ChatSearchBean data) {
    //get nick name
    UserInfo userInfo = ChatUserCache.getUserInfo(data.getAccount());
    if (userInfo == null) {
      ContactRepo.fetchUserInfo(
          data.getMessage().getFromAccount(),
          new FetchCallback<UserInfo>() {
            @Override
            public void onSuccess(@Nullable UserInfo param) {
              List<UserInfo> userInfoList = new ArrayList<>();
              userInfoList.add(param);
              ChatUserCache.addUserInfo(userInfoList);
              loadNickAndAvatar(data, param);
            }

            @Override
            public void onFailed(int code) {
              loadNickAndAvatar(data, null);
            }

            @Override
            public void onException(@Nullable Throwable exception) {
              loadNickAndAvatar(data, null);
            }
          });
    } else {
      loadNickAndAvatar(data, userInfo);
    }
  }

  private void loadNickAndAvatar(ChatSearchBean data, UserInfo userInfo) {
    String name = MessageHelper.getChatMessageUserName(data.getMessage());
    String avatar = userInfo == null ? "" : userInfo.getAvatar();
    String avatarName = userInfo == null ? data.getAccount() : userInfo.getName();
    viewBinding.avatarView.setData(avatar, avatarName, AvatarColor.avatarColor(data.getAccount()));
    viewBinding.nameTv.setText(name);
  }
}
