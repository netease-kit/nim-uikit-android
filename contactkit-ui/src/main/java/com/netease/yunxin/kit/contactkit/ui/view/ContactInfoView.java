// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.netease.yunxin.kit.common.utils.NetworkUtils;
import com.netease.yunxin.kit.contactkit.ui.R;
import com.netease.yunxin.kit.contactkit.ui.databinding.UserInfoLayoutBinding;
import com.netease.yunxin.kit.contactkit.ui.model.ContactUserInfoBean;
import com.netease.yunxin.kit.contactkit.ui.utils.ColorUtils;

public class ContactInfoView extends FrameLayout {

  private UserInfoLayoutBinding binding;

  private IUserCallback userCallback;

  public ContactInfoView(@NonNull Context context) {
    super(context);
    init(null);
  }

  public ContactInfoView(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    init(attrs);
  }

  public ContactInfoView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(attrs);
  }

  private void init(AttributeSet attrs) {
    LayoutInflater layoutInflater = LayoutInflater.from(getContext());
    binding = UserInfoLayoutBinding.inflate(layoutInflater, this, true);
  }

  public void setUserCallback(IUserCallback userCallback) {
    this.userCallback = userCallback;
  }

  public void setData(ContactUserInfoBean userInfo) {
    String name = userInfo.data.getUserInfoName();
    String nickName = null;
    if (userInfo.friendInfo != null) {
      nickName = userInfo.friendInfo.getAlias();
    }
    //avatar
    binding.avatarView.setData(
        userInfo.data.getAvatar(),
        userInfo.getName(),
        ColorUtils.avatarColor(userInfo.data.getAccountId()));

    //name
    if (TextUtils.isEmpty(nickName)) {
      binding.tvName.setText(name);
      binding.tvAccount.setText(
          String.format(
              getContext().getString(R.string.contact_user_info_account),
              userInfo.data.getAccountId()));
      binding.tvCommentName.setVisibility(GONE);
    } else {
      binding.tvName.setText(nickName);
      binding.tvAccount.setText(
          String.format(getContext().getString(R.string.contact_user_info_nickname), name));
      binding.tvCommentName.setText(
          String.format(
              getContext().getString(R.string.contact_user_info_account),
              userInfo.data.getAccountId()));
      binding.tvCommentName.setVisibility(VISIBLE);
    }

    binding.tvBirthday.setText(userInfo.data.getBirthday());
    binding.tvPhone.setText(userInfo.data.getMobile());
    binding.tvEmail.setText(userInfo.data.getEmail());
    binding.tvSignature.setText(userInfo.data.getSign());

    if (userInfo.isFriend) {
      binding.tvDelete.setText(getResources().getText(R.string.delete_friend));
    } else {
      binding.tvDelete.setText(getResources().getText(R.string.add_friend));
    }
    binding.scBlackList.setOnCheckedChangeListener(null);
    binding.scBlackList.setChecked(userInfo.isBlack);

    binding.scBlackList.setOnCheckedChangeListener(
        (buttonView, isChecked) -> {
          if (!NetworkUtils.isConnected()) {
            Toast.makeText(getContext(), R.string.contact_network_error_tip, Toast.LENGTH_SHORT)
                .show();
            binding.scBlackList.toggle();
            return;
          }
          if (userCallback != null) {
            userCallback.addBlackList(isChecked);
          }
        });
    setIsFriend(userInfo.isFriend);
  }

  public void setCommentClickListener(OnClickListener onClickListener) {
    binding.rlyComment.setOnClickListener(onClickListener);
  }

  public void setDeleteClickListener(OnClickListener clickListener) {
    binding.tvDelete.setOnClickListener(clickListener);
  }

  private void setIsFriend(boolean isFriend) {
    if (isFriend) {
      binding.llyFriend.setVisibility(VISIBLE);
      binding.rlyComment.setVisibility(VISIBLE);
      binding.tvChat.setOnClickListener(
          v -> {
            if (userCallback != null) {
              userCallback.goChat();
            }
          });
      binding.tvChat.setText(R.string.chat);
      binding.tvDelete.setVisibility(VISIBLE);
    } else {
      binding.llyFriend.setVisibility(GONE);
      binding.rlyComment.setVisibility(GONE);
      binding.tvChat.setText(R.string.add_friend);
      binding.tvChat.setOnClickListener(
          v -> {
            if (userCallback != null) {
              userCallback.addFriend();
            }
          });
      binding.tvDelete.setVisibility(GONE);
    }
  }

  public void configSCBlackSwitch(int trackRes, int thumbRes) {
    binding.scBlackList.setTrackResource(trackRes);
    binding.scBlackList.setThumbResource(thumbRes);
  }

  public void configAvatarCorner(int cornerSize) {
    binding.avatarView.setCornerRadius(cornerSize);
  }

  public void configChatBtnColor(int color) {
    binding.tvChat.setTextColor(color);
  }

  public void configDivideLineColor(int color) {
    binding.divideLine1.setBackgroundColor(color);
    binding.divideLine2.setBackgroundColor(color);
    binding.divideLine3.setBackgroundColor(color);
  }

  public void configRootBgColor(int color) {
    binding.getRoot().setBackgroundColor(color);
  }

  public void configSignatureMaxLines(int lines) {
    binding.tvSignature.setMaxLines(lines);
  }

  public interface IUserCallback {
    void goChat();

    void addFriend();

    void openMessageNotify(boolean open);

    void addBlackList(boolean add);
  }
}
