// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.cache;

import java.util.List;

/** 缓存用户变化监听 */
public interface TeamUserChangedListener {

  /**
   * 用户信息变化
   *
   * @param accountIds 用户账号列表
   */
  void onUsersChanged(List<String> accountIds);

  /**
   * 添加新的群成员
   *
   * @param accountIds
   */
  void onUsersAdd(List<String> accountIds);

  /**
   * 群乘以移除（主动退群）
   *
   * @param accountIds
   */
  void onUserDelete(List<String> accountIds);
}
