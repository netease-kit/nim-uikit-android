// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.normal.search.viewholder;

import android.text.TextUtils;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import com.netease.nimlib.sdk.search.model.RecordHitInfo;
import com.netease.yunxin.kit.chatkit.model.FriendSearchInfo;
import com.netease.yunxin.kit.chatkit.model.HitType;
import com.netease.yunxin.kit.common.ui.utils.AvatarColor;
import com.netease.yunxin.kit.common.ui.viewholder.BaseViewHolder;
import com.netease.yunxin.kit.common.ui.widgets.HitHighlightTextView;
import com.netease.yunxin.kit.contactkit.ui.R;
import com.netease.yunxin.kit.contactkit.ui.databinding.SearchUserItemLayoutBinding;
import com.netease.yunxin.kit.contactkit.ui.model.SearchFriendBean;

public class FriendViewHolder extends BaseViewHolder<SearchFriendBean> {
  private FriendSearchInfo friendInfo;
  private SearchUserItemLayoutBinding viewBinding;

  public FriendViewHolder(@NonNull View itemView) {
    super(itemView);
  }

  public FriendViewHolder(@NonNull SearchUserItemLayoutBinding viewBinding) {
    this(viewBinding.getRoot());
    this.viewBinding = viewBinding;
  }

  @Override
  public void onBindData(SearchFriendBean data, int position) {
    friendInfo = data.friendSearchInfo;
    if (friendInfo != null) {
      viewBinding.cavUserIcon.setData(
          friendInfo.getFriendInfo().getAvatar(),
          friendInfo.getFriendInfo().getAvatarName(),
          AvatarColor.avatarColor(friendInfo.getFriendInfo().getAccount()));
      if (friendInfo.getHitType() == HitType.Alias) {
        setHitText(
            viewBinding.tvNickName, friendInfo.getFriendInfo().getAlias(), friendInfo.getHitInfo());
        viewBinding.tvName.clearHitText("");
        viewBinding.tvName.setVisibility(View.GONE);
      } else if (friendInfo.getHitType() == HitType.UserName) {

        if (!TextUtils.isEmpty(friendInfo.getFriendInfo().getAlias())) {
          viewBinding.tvNickName.clearHitText(friendInfo.getFriendInfo().getAlias());
          viewBinding.tvNickName.setVisibility(View.VISIBLE);
          setHitText(
              viewBinding.tvName,
              friendInfo.getFriendInfo().getUserInfo().getName(),
              friendInfo.getHitInfo());
          viewBinding.tvName.setVisibility(View.VISIBLE);
        } else {
          setHitText(
              viewBinding.tvNickName,
              friendInfo.getFriendInfo().getUserInfo().getName(),
              friendInfo.getHitInfo());
          viewBinding.tvName.clearHitText("");
          viewBinding.tvNickName.setVisibility(View.VISIBLE);
          viewBinding.tvName.setVisibility(View.GONE);
        }
      } else {
        viewBinding.tvNickName.setVisibility(View.VISIBLE);
        if (!TextUtils.isEmpty(friendInfo.getFriendInfo().getAlias())) {
          viewBinding.tvNickName.clearHitText(friendInfo.getFriendInfo().getAlias());
          setHitText(
              viewBinding.tvName, friendInfo.getFriendInfo().getAccount(), friendInfo.getHitInfo());
          viewBinding.tvName.setVisibility(View.VISIBLE);
        } else if (!TextUtils.isEmpty(friendInfo.getFriendInfo().getUserInfo().getName())) {
          viewBinding.tvNickName.clearHitText(friendInfo.getFriendInfo().getUserInfo().getName());
          setHitText(
              viewBinding.tvName, friendInfo.getFriendInfo().getAccount(), friendInfo.getHitInfo());
          viewBinding.tvName.setVisibility(View.VISIBLE);
        } else {
          setHitText(
              viewBinding.tvNickName,
              friendInfo.getFriendInfo().getAccount(),
              friendInfo.getHitInfo());
          viewBinding.tvName.clearHitText("");
          viewBinding.tvName.setVisibility(View.GONE);
        }
      }
      viewBinding.getRoot().setOnClickListener(v -> itemListener.onClick(v, data, position));
      ;
    }
  }

  private void setHitText(HitHighlightTextView textView, String text, RecordHitInfo hitInfo) {
    if (hitInfo == null) {
      textView.clearHitText(text);
      return;
    }
    textView.setHitText(
        text,
        hitInfo.start,
        hitInfo.end,
        ContextCompat.getColor(
            viewBinding.getRoot().getContext(), R.color.color_contact_blue_primary));
  }
}
