// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.teamkit.ui;

import com.netease.nimlib.coexist.sdk.v2.team.enums.V2NIMTeamAgreeMode;
import com.netease.nimlib.coexist.sdk.v2.team.enums.V2NIMTeamJoinMode;

public class TeamKitConfigCenter {

  // 邀请入群时是否需要被邀请人的同意模式定义
  public static V2NIMTeamAgreeMode teamAgreeMode = V2NIMTeamAgreeMode.V2NIM_TEAM_AGREE_MODE_NO_AUTH;

  // 申请入群的模式
  public static V2NIMTeamJoinMode teamJoinMode = V2NIMTeamJoinMode.V2NIM_TEAM_JOIN_MODE_FREE;
}
