// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.teamkit.ui.utils;

import android.text.TextUtils;

/** 颜色工具类 */
public class ColorUtils {

  /**
   * 获取头像背景颜色，根据不同的用户信息
   *
   * @param content 成员账号ID
   * @return 颜色
   */
  public static int avatarColor(String content) {
    if (!TextUtils.isEmpty(content)) {
      return content.charAt(content.length() - 1);
    }
    return 0;
  }
}
