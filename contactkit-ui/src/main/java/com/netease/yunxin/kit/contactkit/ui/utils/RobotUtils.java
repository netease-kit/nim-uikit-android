// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.utils;

/** 机器人名称工具类 */
public class RobotUtils {
  /**
   * 根据当前机器人数量生成默认名称。
   *
   * <p>格式：{@code Bot_<count>}，例如列表为空（size=0）时生成 "Bot_0"。
   *
   * @param count 当前机器人列表的大小
   * @return 默认机器人名称
   */
  public static String generateDefaultName(int count) {
    return "Bot_Claw";
  }

  /**
   * 生成机器人 accid。
   *
   * <p>格式：{@code Bot_<32位UUID>}，例如 "Bot_550e8400e29b41d4a716446655440000"。
   *
   * @return 生成的 accid 字符串
   */
  public static String generateAccid() {
    String uuid = java.util.UUID.randomUUID().toString().replace("-", "");
    String accid = "Bot_" + uuid;
    return accid.length() > 32 ? accid.substring(0, 32) : accid;
  }
}
