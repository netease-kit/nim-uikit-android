// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.view.message.viewholder.options;

public class UserInfoUIOption {
  /** 当前用户头像是否展示 */
  public Boolean myAvatarVisible;
  /** 当前用户头像大小 */
  public Integer myAvatarSize;
  /** 当前用户头像圆角 */
  public Integer myAvatarRadius;
  /** 当前用户昵称是否展示 */
  public Boolean myNicknameVisible;
  /** 当前用户昵称 */
  public CharSequence myNickname;
  /** 当前用户昵称文字大小 */
  public Integer myNicknameSize;
  /** 当前用户昵称文字颜色 */
  public Integer myNickNameColor;
  /** 非当前用户头像是否展示 */
  public Boolean otherUserAvatarVisible;
  /** 非当前用户头像大小 */
  public Integer otherUserAvatarSize;
  /** 非当前用户头像圆角 */
  public Integer otherUserAvatarRadius;
  /** 非当前用户昵称是否展示 */
  public Boolean otherUserNicknameVisible;
  /** 非当前用户昵称 */
  public CharSequence otherUserNickname;
  /** 非当前用户昵称文字大小 */
  public Integer otherUserNicknameSize;
  /** 非当前用户昵称文字颜色 */
  public Integer otherUserNicknameColor;

  @Override
  public String toString() {
    return "UserInfoUIOption{"
        + "myAvatarVisible="
        + myAvatarVisible
        + ", myAvatarSize="
        + myAvatarSize
        + ", myAvatarRadius="
        + myAvatarRadius
        + ", myNicknameVisible="
        + myNicknameVisible
        + ", myNickname="
        + myNickname
        + ", myNicknameSize="
        + myNicknameSize
        + ", myNickNameColor="
        + myNickNameColor
        + ", otherUserAvatarVisible="
        + otherUserAvatarVisible
        + ", otherUserAvatarSize="
        + otherUserAvatarSize
        + ", otherUserAvatarRadius="
        + otherUserAvatarRadius
        + ", otherUserNicknameVisible="
        + otherUserNicknameVisible
        + ", otherUserNickname="
        + otherUserNickname
        + ", otherUserNicknameSize="
        + otherUserNicknameSize
        + ", otherUserNicknameColor="
        + otherUserNicknameColor
        + '}';
  }
}
