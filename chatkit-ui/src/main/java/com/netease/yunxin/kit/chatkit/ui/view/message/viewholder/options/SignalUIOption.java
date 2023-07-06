// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.view.message.viewholder.options;

import androidx.annotation.DrawableRes;

/** 消息标记相关ui设置 onMessageSignal，颜色通过 span 来处理 */
public class SignalUIOption {
  /** 标记功能是否可用，默认可用 */
  public Boolean enable;
  /** 点对点聊天中标记提示文案 */
  public CharSequence signalTipTextForP2p;
  /** 群组聊天中标记提示文案 */
  public CharSequence signalTipTextForGroup;
  /** 标记背景资源 */
  public @DrawableRes Integer signalBgRes;

  @Override
  public String toString() {
    return "SignalUIOption{"
        + "enable="
        + enable
        + ", signalTipTextForP2p="
        + signalTipTextForP2p
        + ", signalTipTextForGroup="
        + signalTipTextForGroup
        + ", signalBgRes="
        + signalBgRes
        + '}';
  }
}
