// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.model;

import com.netease.nimlib.sdk.v2.ai.model.V2NIMUserAIBot;

/**
 * 机器人数据模型，包装 SDK 返回的 {@link V2NIMUserAIBot} 对象。
 *
 * <p>外部通过 getter 访问字段；SDK 原始对象可通过 {@link #getAIBot()} 获取。
 */
public class RobotInfoBean extends BaseContactBean {

  /** SDK 原始机器人数据对象 */
  private final V2NIMUserAIBot aiBot;

  /** 副标题，用于绑定页展示额外说明（如「当前二维码对应账号已默认选中」），可为 null */
  private String subtitle;

  /** 使用 SDK 对象构造 */
  public RobotInfoBean(V2NIMUserAIBot aiBot) {
    this.aiBot = aiBot;
    this.viewType = IViewTypeConstant.CONTACT_ROBOT;
  }

  /** 兼容旧代码，使用原始字段构造（不持有 SDK 对象时使用） */
  public RobotInfoBean(String accountId, String name, String avatar) {
    this.aiBot = null;
    this.viewType = IViewTypeConstant.CONTACT_ROBOT;
    this._fallbackAccountId = accountId;
    this._fallbackName = name;
    this._fallbackAvatar = avatar;
  }

  // fallback 字段，仅在未传入 V2NIMUserAIBot 时使用
  private String _fallbackAccountId;
  private String _fallbackName;
  private String _fallbackAvatar;

  /** 获取机器人 SDK 原始对象，可能为 null（旧代码兼容构造时为 null） */
  public V2NIMUserAIBot getAIBot() {
    return aiBot;
  }

  public String getAccountId() {
    return aiBot != null ? aiBot.getAccid() : _fallbackAccountId;
  }

  public String getName() {
    if (aiBot != null) {
      String n = aiBot.getName();
      return n != null ? n : getAccountId();
    }
    return _fallbackName != null ? _fallbackName : _fallbackAccountId;
  }

  public String getAvatar() {
    return aiBot != null ? aiBot.getIcon() : _fallbackAvatar;
  }

  public String getSubtitle() {
    return subtitle;
  }

  public RobotInfoBean setSubtitle(String subtitle) {
    this.subtitle = subtitle;
    return this;
  }

  @Override
  public boolean isShowDivision() {
    return false;
  }

  @Override
  public String getTarget() {
    return getName();
  }

  @Override
  public int hashCode() {
    String id = getAccountId();
    return id != null ? id.hashCode() : 0;
  }
}
