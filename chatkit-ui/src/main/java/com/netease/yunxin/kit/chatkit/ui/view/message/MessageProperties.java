// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.view.message;

import android.graphics.drawable.Drawable;
import androidx.annotation.ColorInt;

/** message item config */
public class MessageProperties {

  public static final int INT_NULL = -1;

  private Drawable receiveMessageBg;

  private Drawable selfMessageBg;

  public int selfMessageRes = INT_NULL;

  public int receiveMessageRes = INT_NULL;

  @ColorInt private int userNickColor = INT_NULL;

  private int userNickTextSize = INT_NULL;

  @ColorInt private int messageTextColor = INT_NULL;

  private int messageTextSize = INT_NULL;

  private float avatarCornerRadius = INT_NULL;

  private int timeTextSize = INT_NULL;

  @ColorInt private int timeTextColor = INT_NULL;

  @ColorInt private int signalBgColor = INT_NULL;

  private boolean showStickerMessage = false;

  private boolean showP2pMessageStatus = true;

  private boolean showTeamMessageStatus = true;

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
  public int getUserNickColor() {
    return userNickColor;
  }

  public void setUserNickTextSize(int textSize) {
    this.userNickTextSize = textSize;
  }

  public int getUserNickTextSize() {
    return userNickTextSize;
  }

  public void setMessageTextSize(int messageTextSize) {
    this.messageTextSize = messageTextSize;
  }

  public int getMessageTextSize() {
    return messageTextSize;
  }

  @ColorInt
  public int getMessageTextColor() {
    return messageTextColor;
  }

  public void setMessageTextColor(@ColorInt int messageTextColor) {
    this.messageTextColor = messageTextColor;
  }

  public void setAvatarCornerRadius(float radius) {
    this.avatarCornerRadius = radius;
  }

  public float getAvatarCornerRadius() {
    return avatarCornerRadius;
  }

  public void setTimeTextSize(int textSize) {
    this.timeTextSize = textSize;
  }

  public int getTimeTextSize() {
    return this.timeTextSize;
  }

  public void setTimeTextColor(@ColorInt int textColor) {
    this.timeTextColor = textColor;
  }

  @ColorInt
  public int getTimeTextColor() {
    return this.timeTextColor;
  }

  public void setSignalBgColor(@ColorInt int textColor) {
    this.signalBgColor = textColor;
  }

  @ColorInt
  public int getSignalBgColor() {
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
