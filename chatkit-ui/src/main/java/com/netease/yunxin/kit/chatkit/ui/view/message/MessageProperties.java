// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.view.message;

import android.graphics.drawable.Drawable;
import android.view.View;
import androidx.annotation.ColorInt;
import com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant;
import java.util.Map;

/** 消息UI配置 该类用于配置消息的UI属性，如消息背景、消息文字颜色、消息文字大小等 */
public class MessageProperties {

  // 接受消息背景
  @Deprecated public Drawable receiveMessageBg;
  // 自己发送消息的背景
  @Deprecated public Drawable selfMessageBg;
  // 自己发送消息的背景颜色
  public Integer selfMessageBgRes = null;
  // 接收消息的背景颜色
  public Integer receiveMessageBgRes = null;
  // 用户昵称颜色
  @ColorInt public Integer userNickColor = null;
  // 用户昵称文字大小
  public Integer userNickTextSize = null;
  // 接受到消息文字颜色
  @ColorInt public Integer receiveMessageTextColor = null;

  // 发送消息文字颜色
  @ColorInt public Integer selfMessageTextColor = null;
  // 消息文字大小
  public Integer receiveMessageTextSize = null;
  // 发送消息文字大小
  public Integer selfMessageTextSize = null;
  // 头像圆角半径
  public Float avatarCornerRadius = null;
  // 时间文字大小
  public Integer timeTextSize = null;
  // 时间文字颜色
  @ColorInt public Integer timeTextColor = null;
  // 信号背景颜色
  @ColorInt public Integer signalBgColor = null;

  // 是否显示贴纸消息
  public boolean showStickerMessage = false;
  // 是否显示点对点消息状态
  public boolean showP2pMessageStatus = true;
  // 是否显示群聊消息状态
  public boolean showTeamMessageStatus = true;

  // 是否显示标题栏
  public boolean showTitleBar = true;

  // 是否显示标题栏右侧图标
  public boolean showTitleBarRightIcon = true;

  // 标题栏右侧图标资源
  public Integer titleBarRightRes = null;

  // 标题栏右侧图标点击事件
  public View.OnClickListener titleBarRightClick;

  // 聊天界面背景
  public Drawable chatViewBackground;

  //文件图标格式对应图片
  public Map<String, Drawable> fileDrawable;

  //发送文件的大小限制，单位MB，默认200MB
  public long sendFileLimit = ChatKitUIConstant.FILE_LIMIT;

  /**
   * 设置接收消息的背景，该方法已过时
   *
   * @param receiveMessageBg 接收消息的背景图
   */
  @Deprecated
  public void setReceiveMessageBg(Drawable receiveMessageBg) {
    this.receiveMessageBg = receiveMessageBg;
  }

  /**
   * 获取接收消息的背景
   *
   * @return 接收消息的背景图
   */
  public Drawable getReceiveMessageBg() {
    return receiveMessageBg;
  }

  /**
   * 设置自己发送消息的背景，该方法已过时
   *
   * @param selfMessageBg 自己发送消息的背景图
   */
  @Deprecated
  public void setSelfMessageBg(Drawable selfMessageBg) {
    this.selfMessageBg = selfMessageBg;
  }

  /**
   * 获取自己发送消息的背景
   *
   * @return 自己发送消息的背景图
   */
  public Drawable getSelfMessageBg() {
    return selfMessageBg;
  }

  /**
   * 设置用户昵称颜色
   *
   * @param userNickColor 用户昵称颜色值
   */
  public void setUserNickColor(@ColorInt int userNickColor) {
    this.userNickColor = userNickColor;
  }

  /**
   * 获取用户昵称颜色
   *
   * @return 用户昵称颜色值
   */
  @ColorInt
  public Integer getUserNickColor() {
    return userNickColor;
  }

  /**
   * 设置用户昵称文字大小
   *
   * @param textSize 用户昵称文字大小
   */
  public void setUserNickTextSize(int textSize) {
    this.userNickTextSize = textSize;
  }

  /**
   * 获取用户昵称文字大小
   *
   * @return 用户昵称文字大小
   */
  public Integer getUserNickTextSize() {
    return userNickTextSize;
  }

  /**
   * 设置消息文字大小
   *
   * @param receiveMessageTextSize 消息文字大小
   */
  public void setReceiveMessageTextSize(int receiveMessageTextSize) {
    this.receiveMessageTextSize = receiveMessageTextSize;
  }

  /**
   * 获取消息文字大小
   *
   * @return 消息文字大小
   */
  public Integer getReceiveMessageTextSize() {
    return receiveMessageTextSize;
  }

  /**
   * 获取消息文字颜色
   *
   * @return 消息文字颜色值
   */
  @ColorInt
  public Integer getReceiveMessageTextColor() {
    return receiveMessageTextColor;
  }

  /**
   * 设置消息文字颜色
   *
   * @param receiveMessageTextColor 消息文字颜色值
   */
  public void setReceiveMessageTextColor(@ColorInt int receiveMessageTextColor) {
    this.receiveMessageTextColor = receiveMessageTextColor;
  }

  /**
   * 设置自己发送消息的背景颜色
   *
   * @param selfMessageTextColor
   */
  public void setSelfMessageTextColor(@ColorInt int selfMessageTextColor) {
    this.selfMessageTextColor = selfMessageTextColor;
  }

  /**
   * 获取自己发送消息的背景颜色
   *
   * @return
   */
  public Integer getSelfMessageTextColor() {
    return this.selfMessageTextColor;
  }

  /**
   * 设置自己发送消息的背景颜色
   *
   * @param selfMessageTextSize
   */
  public void setSelfMessageTextSize(int selfMessageTextSize) {
    this.selfMessageTextSize = selfMessageTextSize;
  }

  /**
   * 获取自己发送消息的背景颜色
   *
   * @return
   */
  public Integer getSelfMessageTextSize() {
    return this.selfMessageTextSize;
  }

  /**
   * 设置头像圆角半径
   *
   * @param radius 头像圆角半径
   */
  public void setAvatarCornerRadius(float radius) {
    this.avatarCornerRadius = radius;
  }

  /**
   * 获取头像圆角半径
   *
   * @return 头像圆角半径
   */
  public Float getAvatarCornerRadius() {
    return avatarCornerRadius;
  }

  /**
   * 设置时间文字大小
   *
   * @param textSize 时间文字大小
   */
  public void setTimeTextSize(int textSize) {
    this.timeTextSize = textSize;
  }

  /**
   * 获取时间文字大小
   *
   * @return 时间文字大小
   */
  public Integer getTimeTextSize() {
    return this.timeTextSize;
  }

  /**
   * 设置时间文字颜色
   *
   * @param textColor 时间文字颜色值
   */
  public void setTimeTextColor(@ColorInt int textColor) {
    this.timeTextColor = textColor;
  }

  /**
   * 获取时间文字颜色
   *
   * @return 时间文字颜色值
   */
  @ColorInt
  public Integer getTimeTextColor() {
    return this.timeTextColor;
  }

  /**
   * 设置信号背景颜色
   *
   * @param textColor 信号背景颜色值
   */
  public void setSignalBgColor(@ColorInt int textColor) {
    this.signalBgColor = textColor;
  }

  /**
   * 获取信号背景颜色
   *
   * @return 信号背景颜色值
   */
  @ColorInt
  public Integer getSignalBgColor() {
    return this.signalBgColor;
  }

  /**
   * 设置是否显示贴纸消息
   *
   * @param show 是否显示贴纸消息
   */
  public void setShowStickerMessage(boolean show) {
    this.showStickerMessage = show;
  }

  /**
   * 设置是否显示点对点消息状态
   *
   * @param show 是否显示点对点消息状态
   */
  public void setShowP2pMessageStatus(boolean show) {
    this.showP2pMessageStatus = show;
  }

  /**
   * 获取是否显示点对点消息状态
   *
   * @return 是否显示点对点消息状态
   */
  public boolean getShowP2pMessageStatus() {
    return this.showP2pMessageStatus;
  }

  /**
   * 设置是否显示群聊消息状态
   *
   * @param show 是否显示群聊消息状态
   */
  public void setShowTeamMessageStatus(boolean show) {
    this.showTeamMessageStatus = show;
  }

  /**
   * 获取是否显示群聊消息状态
   *
   * @return 是否显示群聊消息状态
   */
  public boolean getShowTeamMessageStatus() {
    return this.showTeamMessageStatus;
  }
}
