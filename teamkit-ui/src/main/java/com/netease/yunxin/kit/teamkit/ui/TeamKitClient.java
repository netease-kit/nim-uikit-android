// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.teamkit.ui;

import com.netease.yunxin.kit.chatkit.interfaces.ITeamCustom;
import com.netease.yunxin.kit.chatkit.interfaces.TeamCustomProvider;

public class TeamKitClient {
  public static final String LIB_TAG = "TeamKit-UI";

  public static void setTeamCustom(ITeamCustom custom) {
    TeamCustomProvider.INSTANCE.setTeamCustom(custom);
  }

  public static ITeamCustom getTeamCustom() {
    return TeamCustomProvider.INSTANCE.getTeamCustom();
  }
}
