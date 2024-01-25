// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.conversationkit.ui.fun;

import static com.netease.yunxin.kit.corekit.im.utils.RouterConstant.CHAT_KRY;
import static com.netease.yunxin.kit.corekit.im.utils.RouterConstant.KEY_CONTACT_SELECTOR_MAX_COUNT;
import static com.netease.yunxin.kit.corekit.im.utils.RouterConstant.KEY_REMOTE_EXTENSION;
import static com.netease.yunxin.kit.corekit.im.utils.RouterConstant.KEY_REQUEST_SELECTOR_NAME;
import static com.netease.yunxin.kit.corekit.im.utils.RouterConstant.KEY_REQUEST_SELECTOR_NAME_ENABLE;
import static com.netease.yunxin.kit.corekit.im.utils.RouterConstant.KEY_SESSION_ID;
import static com.netease.yunxin.kit.corekit.im.utils.RouterConstant.KEY_TEAM_CREATED_TIP;
import static com.netease.yunxin.kit.corekit.im.utils.RouterConstant.KEY_TEAM_ID;
import static com.netease.yunxin.kit.corekit.im.utils.RouterConstant.PATH_CHAT_SEND_TEAM_TIP_ACTION;
import static com.netease.yunxin.kit.corekit.im.utils.RouterConstant.PATH_FUN_CHAT_TEAM_PAGE;
import static com.netease.yunxin.kit.corekit.im.utils.RouterConstant.PATH_FUN_CONTACT_SELECTOR_PAGE;
import static com.netease.yunxin.kit.corekit.im.utils.RouterConstant.PATH_TEAM_INVITE_ACTION;
import static com.netease.yunxin.kit.corekit.im.utils.RouterConstant.REQUEST_CONTACT_SELECTOR_KEY;

import android.content.Context;
import android.content.Intent;
import com.netease.nimlib.sdk.team.model.CreateTeamResult;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.common.ui.photo.TransHelper;
import com.netease.yunxin.kit.conversationkit.ui.R;
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FunCreateTeamFactory {
  private static final String TAG = "FunCreateTeamFactory";

  public static void selectAndCreateTeam(
      Context context,
      int requestCode,
      String createMethod,
      List<String> filterList,
      List<String> addList,
      int memberLimit) {
    TransHelper.launchTask(
        context,
        requestCode,
        (activity, integer) -> {
          XKitRouter.withKey(PATH_FUN_CONTACT_SELECTOR_PAGE)
              .withParam(KEY_CONTACT_SELECTOR_MAX_COUNT, memberLimit)
              .withParam(KEY_REQUEST_SELECTOR_NAME_ENABLE, true)
              .withParam(RouterConstant.SELECTOR_CONTACT_FILTER_KEY, filterList)
              .withContext(activity)
              .withRequestCode(integer)
              .navigate();
          return null;
        },
        intentResultInfo -> {
          if (intentResultInfo == null
              || !intentResultInfo.getSuccess()
              || intentResultInfo.getValue() == null) {
            return null;
          }
          Intent data = intentResultInfo.getValue();
          ArrayList<String> memberList = data.getStringArrayListExtra(REQUEST_CONTACT_SELECTOR_KEY);
          if (memberList == null || memberList.isEmpty()) {
            ALog.e(TAG, "no one was chosen.");
            return null;
          }
          if (addList != null && addList.size() > 0) {
            memberList.addAll(addList);
          }
          XKitRouter.withKey(createMethod)
              .withParam(
                  KEY_REQUEST_SELECTOR_NAME,
                  data.getStringArrayListExtra(KEY_REQUEST_SELECTOR_NAME))
              .navigate(
                  res -> {
                    if (res.getSuccess() && res.getValue() instanceof CreateTeamResult) {
                      Team teamInfo = ((CreateTeamResult) res.getValue()).getTeam();
                      if (RouterConstant.PATH_FUN_CREATE_ADVANCED_TEAM_ACTION.equals(
                          createMethod)) {
                        Map<String, Object> map = new HashMap<>(1);
                        map.put(
                            KEY_TEAM_CREATED_TIP,
                            context.getString(R.string.create_advanced_team_success));
                        // 发送创建成功群里提示信息
                        XKitRouter.withKey(PATH_CHAT_SEND_TEAM_TIP_ACTION)
                            .withParam(KEY_SESSION_ID, teamInfo.getId())
                            .withParam(RouterConstant.KEY_MESSAGE_TIME, teamInfo.getCreateTime())
                            .withParam(KEY_REMOTE_EXTENSION, map)
                            .navigate();
                      }

                      // 邀请加入群，处理邀请通知早于创建成功同志
                      XKitRouter.withKey(PATH_TEAM_INVITE_ACTION)
                          .withParam(KEY_TEAM_ID, teamInfo.getId())
                          .withParam(REQUEST_CONTACT_SELECTOR_KEY, memberList)
                          .navigate();

                      // 跳转到会话页面
                      XKitRouter.withKey(PATH_FUN_CHAT_TEAM_PAGE)
                          .withContext(context)
                          .withParam(CHAT_KRY, teamInfo)
                          .navigate();

                    } else {
                      ALog.e(TAG, "create team failed.");
                    }
                  });
          return null;
        });
  }
}
