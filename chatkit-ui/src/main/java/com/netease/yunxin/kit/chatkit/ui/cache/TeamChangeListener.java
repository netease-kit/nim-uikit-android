// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.cache;

import com.netease.nimlib.sdk.v2.team.model.V2NIMTeam;

/** 缓存群变化监听 */
public interface TeamChangeListener {
  /**
   * 群信息变化
   *
   * @param team 群信息
   */
  void onTeamUpdate(V2NIMTeam team);
}
