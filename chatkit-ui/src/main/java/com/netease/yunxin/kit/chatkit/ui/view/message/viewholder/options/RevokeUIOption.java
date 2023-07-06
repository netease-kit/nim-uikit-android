// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.view.message.viewholder.options;

/** 消息撤回相关ui配置 */
public class RevokeUIOption {
  public Boolean enable;
  /** 撤回消息动作按钮是否展示 */
  public Boolean actionBtnVisible;
  /** 撤回消息动作按钮文案 */
  public CharSequence actionBtnText;
  /** 消息撤回提示文案 */
  public CharSequence revokedTipText;

  @Override
  public String toString() {
    return "RevokeUIOption{"
        + "enable="
        + enable
        + ", actionBtnVisible="
        + actionBtnVisible
        + ", actionBtnText="
        + actionBtnText
        + ", revokedTipText="
        + revokedTipText
        + '}';
  }
}
