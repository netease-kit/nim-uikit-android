// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.utils;

import android.content.Context;
import android.widget.Toast;
import com.netease.yunxin.kit.common.ui.utils.SPUtils;
import com.netease.yunxin.kit.common.utils.NetworkUtils;
import com.netease.yunxin.kit.contactkit.ui.ContactConstant;
import com.netease.yunxin.kit.contactkit.ui.R;
import com.netease.yunxin.kit.corekit.im2.IMKitClient;

public class ContactUtils {

  private static final String teamVerifySPFile = "teamVerify";
  private static Long teamVerifyReadTime = null;

  public static long getTeamVerifyReadTime() {
    if (teamVerifyReadTime == null) {
      teamVerifyReadTime =
          SPUtils.INSTANCE.getLong(teamVerifySPFile, 0, IMKitClient.getApplicationContext());
    }
    return teamVerifyReadTime;
  }

  public static void setTeamVerifyReadTime(long time) {
    teamVerifyReadTime = time;
    SPUtils.INSTANCE.saveLong(teamVerifySPFile, time, IMKitClient.getApplicationContext());
  }

  public static boolean checkNetworkAndToast(Context context) {
    if (!NetworkUtils.isConnected()) {
      Toast.makeText(context, R.string.contact_network_error_tip, Toast.LENGTH_SHORT).show();
      return false;
    }
    return true;
  }

  public static int getErrorCodeAndToast(int errorCode) {
    switch (errorCode) {
      case ContactConstant.ERROR_TEAM_NO_PERMISSION:
        return R.string.contact_member_no_permission_error_tip;
      case ContactConstant.ERROR_TEAM_MEMBER_ALREADY_EXIT:
        return R.string.contact_member_already_exit_error_tip;
      case ContactConstant.ERROR_TEAM_HAS_DISSOLVED:
        return R.string.contact_member_dissolved_error_tip;
      case ContactConstant.ERROR_TEAM_MEMBER_LIMIT:
        return R.string.contact_member_limit_error_tip;
      case ContactConstant.ERROR_TEAM_MEMBER_JOIN_LIMIT:
        return R.string.contact_member_join_limit_error_tip;
      case ContactConstant.ERROR_TEAM_APPLICATION_HAS_DONE:
        return R.string.contact_team_verify_done_error_tip;
      case ContactConstant.ERROR_TEAM_ACTION_EXPIRED:
        return R.string.contact_member_join_expired_error_tip;
      default:
        return R.string.contact_operate_error_tip;
    }
  }
}
