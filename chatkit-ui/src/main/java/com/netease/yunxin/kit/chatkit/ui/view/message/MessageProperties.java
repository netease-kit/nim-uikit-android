// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.view.message;

import android.graphics.drawable.Drawable;
import android.view.View;
import androidx.annotation.ColorInt;
import com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant;
import java.util.Map;

/** message item config */
public class MessageProperties {

  public Drawable receiveMessageBg;
  public Drawable selfMessageBg;
  public Integer selfMessageRes = null;
  public Integer receiveMessageRes = null;
  @ColorInt public Integer userNickColor = null;
  public Integer userNickTextSize = null;
  @ColorInt public Integer messageTextColor = null;
  public Integer messageTextSize = null;
  public Float avatarCornerRadius = null;
  public Integer timeTextSize = null;
  @ColorInt public Integer timeTextColor = null;
  @ColorInt public Integer signalBgColor = null;

  public boolean showStickerMessage = false;
  public boolean showP2pMessageStatus = true;
  public boolean showTeamMessageStatus = true;

  public boolean showTitleBar = true;

  public boolean showTitleBarRightIcon = true;

  public Integer titleBarRightRes = null;

  public View.OnClickListener titleBarRightClick;

  public Drawable chatViewBackground;

  //文件图标格式对应图片
  public Map<String, Drawable> fileDrawable;

  //发送文件的大小限制，单位MB，默认200MB
  public long sendFileLimit = ChatKitUIConstant.FILE_LIMIT;

  @Deprecated
  public void setReceiveMessageBg(Drawable receiveMessageBg) {
    this.receiveMessageBg = receiveMessageBg;
  }

  public Drawable getReceiveMessageBg() {
    return receiveMessageBg;
  }

  @Deprecated
  public void setSelfMessageBg(Drawable selfMessageBg) {
    this.selfMessageBg = selfMessageBg;
  }

  public Drawable getSelfMessageBg() {
    return selfMessageBg;
  }

  public void setUserNickColor(@ColorInt int userNickColor) {
    this.userNickColor = userNickColor;
  }

  @ColorInt
  public Integer getUserNickColor() {
    return userNickColor;
  }

  public void setUserNickTextSize(int textSize) {
    this.userNickTextSize = textSize;
  }

  public Integer getUserNickTextSize() {
    return userNickTextSize;
  }

  public void setMessageTextSize(int messageTextSize) {
    this.messageTextSize = messageTextSize;
  }

  public Integer getMessageTextSize() {
    return messageTextSize;
  }

  @ColorInt
  public Integer getMessageTextColor() {
    return messageTextColor;
  }

  public void setMessageTextColor(@ColorInt int messageTextColor) {
    this.messageTextColor = messageTextColor;
  }

  public void setAvatarCornerRadius(float radius) {
    this.avatarCornerRadius = radius;
  }

  public Float getAvatarCornerRadius() {
    return avatarCornerRadius;
  }

  public void setTimeTextSize(int textSize) {
    this.timeTextSize = textSize;
  }

  public Integer getTimeTextSize() {
    return this.timeTextSize;
  }

  public void setTimeTextColor(@ColorInt int textColor) {
    this.timeTextColor = textColor;
  }

  @ColorInt
  public Integer getTimeTextColor() {
    return this.timeTextColor;
  }

  public void setSignalBgColor(@ColorInt int textColor) {
    this.signalBgColor = textColor;
  }

  @ColorInt
  public Integer getSignalBgColor() {
    return this.signalBgColor;
  }

  public void setShowStickerMessage(boolean show) {
    this.showStickerMessage = show;
  }

  public void setShowP2pMessageStatus(boolean show) {
    this.showP2pMessageStatus = show;
  }

  public boolean getShowP2pMessageStatus() {
    return this.showP2pMessageStatus;
  }

  public void setShowTeamMessageStatus(boolean show) {
    this.showTeamMessageStatus = show;
  }

  public boolean getShowTeamMessageStatus() {
    return this.showTeamMessageStatus;
  }
}
