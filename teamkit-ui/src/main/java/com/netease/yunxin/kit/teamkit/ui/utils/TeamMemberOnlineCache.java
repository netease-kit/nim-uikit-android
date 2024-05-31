// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.teamkit.ui.utils;

import java.util.HashSet;
import java.util.Set;

public class TeamMemberOnlineCache {

  private static final Set<String> onlineAccounts = new HashSet<>();

  public static void addOnlineAccount(String account) {
    onlineAccounts.add(account);
  }

  public static void removeOnlineAccount(String account) {
    onlineAccounts.remove(account);
  }

  public static boolean isOnline(String account) {
    return onlineAccounts.contains(account);
  }

  public static void clear() {
    onlineAccounts.clear();
  }
}
