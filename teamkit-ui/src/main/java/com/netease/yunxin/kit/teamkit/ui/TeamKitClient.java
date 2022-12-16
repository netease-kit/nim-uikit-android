// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.teamkit.ui;

import com.netease.yunxin.kit.teamkit.ITeamCustom;
import com.netease.yunxin.kit.teamkit.TeamProvider;

public class TeamKitClient {

  public static void setTeamCustom(ITeamCustom custom) {
    TeamProvider.INSTANCE.setTeamCustom(custom);
  }

  public static ITeamCustom getTeamCustom() {
    return TeamProvider.INSTANCE.getTeamCustom();
  }
}
