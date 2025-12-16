// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.conversationkit.ui.normal;

import static com.netease.yunxin.kit.corekit.coexist.im2.utils.RouterConstant.CHAT_ID_KRY;
import static com.netease.yunxin.kit.corekit.coexist.im2.utils.RouterConstant.CHAT_KRY;
import static com.netease.yunxin.kit.corekit.coexist.im2.utils.RouterConstant.KEY_CONTACT_SELECTOR_MAX_COUNT;
import static com.netease.yunxin.kit.corekit.coexist.im2.utils.RouterConstant.KEY_MESSAGE_TIME;
import static com.netease.yunxin.kit.corekit.coexist.im2.utils.RouterConstant.KEY_REMOTE_EXTENSION;
import static com.netease.yunxin.kit.corekit.coexist.im2.utils.RouterConstant.KEY_REQUEST_SELECTOR_NAME;
import static com.netease.yunxin.kit.corekit.coexist.im2.utils.RouterConstant.KEY_REQUEST_SELECTOR_NAME_ENABLE;
import static com.netease.yunxin.kit.corekit.coexist.im2.utils.RouterConstant.KEY_SESSION_ID;
import static com.netease.yunxin.kit.corekit.coexist.im2.utils.RouterConstant.KEY_TEAM_CREATED_TIP;
import static com.netease.yunxin.kit.corekit.coexist.im2.utils.RouterConstant.KEY_TEAM_ID;
import static com.netease.yunxin.kit.corekit.coexist.im2.utils.RouterConstant.PATH_CHAT_SEND_TEAM_TIP_ACTION;
import static com.netease.yunxin.kit.corekit.coexist.im2.utils.RouterConstant.PATH_CHAT_TEAM_PAGE;
import static com.netease.yunxin.kit.corekit.coexist.im2.utils.RouterConstant.PATH_CONTACT_AI_SELECTOR_PAGE;
import static com.netease.yunxin.kit.corekit.coexist.im2.utils.RouterConstant.PATH_CONTACT_SELECTOR_PAGE;
import static com.netease.yunxin.kit.corekit.coexist.im2.utils.RouterConstant.PATH_TEAM_INVITE_ACTION;
import static com.netease.yunxin.kit.corekit.coexist.im2.utils.RouterConstant.REQUEST_CONTACT_SELECTOR_KEY;

import android.content.Context;
import android.content.Intent;
import com.netease.nimlib.coexist.sdk.v2.team.model.V2NIMTeam;
import com.netease.nimlib.coexist.sdk.v2.team.result.V2NIMCreateTeamResult;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.IMKitConfigCenter;
import com.netease.yunxin.kit.common.ui.photo.TransHelper;
import com.netease.yunxin.kit.conversationkit.ui.R;
import com.netease.yunxin.kit.corekit.coexist.im2.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** 普通版创建群组工厂类，提供创建群组的方法包括人员选择、创建群组、邀请人员加入群组等 */
public class NormalCreateTeamFactory {
  private static final String TAG = "FunCreateTeamFactory";

  /**
   * 选择人员并创建群组
   *
   * @param context 上下文
   * @param requestCode 请求码
   * @param createMethod 创建群组方法区分创建高级群还是讨论组
   * @param filterList 过滤人员列表，不需要加入到群的列表，在人员选择中会置灰相关人员
   * @param addList 添加人员列表，默认需要加入到群的列表
   * @param memberLimit 群组人员上限
   */
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
          // 路由跳转到人员选择器，并监听返回，返回结果中包含选择的人员列表
          String path = PATH_CONTACT_SELECTOR_PAGE;
          if (IMKitConfigCenter.getEnableAIUser()) {
            path = PATH_CONTACT_AI_SELECTOR_PAGE;
          }
          XKitRouter.withKey(path)
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
          // 获取选择的人员列表
          Intent data = intentResultInfo.getValue();
          ArrayList<String> memberList = data.getStringArrayListExtra(REQUEST_CONTACT_SELECTOR_KEY);
          if (memberList == null || memberList.isEmpty()) {
            ALog.e(TAG, "no one was chosen.");
            return null;
          }
          if (addList != null && addList.size() > 0) {
            memberList.addAll(addList);
          }
          // 通过路由发起群组创建请求（相关实现参考teamkit-ui:TeamUIService.java），并监听创建结果
          XKitRouter.withKey(createMethod)
              .withParam(
                  KEY_REQUEST_SELECTOR_NAME,
                  data.getStringArrayListExtra(KEY_REQUEST_SELECTOR_NAME))
              .navigate(
                  res -> {
                    // 创建成功后,结果回调
                    if (res.getSuccess() && res.getValue() instanceof V2NIMCreateTeamResult) {
                      V2NIMTeam teamInfo = ((V2NIMCreateTeamResult) res.getValue()).getTeam();
                      // 如果是创建高级群，发送创建成功群里提示信息
                      if (RouterConstant.PATH_CREATE_ADVANCED_TEAM_ACTION.equals(createMethod)) {
                        Map<String, Object> map = new HashMap<>(1);
                        map.put(
                            KEY_TEAM_CREATED_TIP,
                            context.getString(R.string.create_advanced_team_success));
                        // 发送创建成功群里提示信息
                        XKitRouter.withKey(PATH_CHAT_SEND_TEAM_TIP_ACTION)
                            .withParam(KEY_SESSION_ID, teamInfo.getTeamId())
                            .withParam(KEY_MESSAGE_TIME, teamInfo.getCreateTime())
                            .withParam(KEY_REMOTE_EXTENSION, map)
                            .navigate();
                      }

                      // 邀请加入群，处理邀请通知早于创建成功通知
                      XKitRouter.withKey(PATH_TEAM_INVITE_ACTION)
                          .withParam(KEY_TEAM_ID, teamInfo.getTeamId())
                          .withParam(REQUEST_CONTACT_SELECTOR_KEY, memberList)
                          .navigate();

                      // 跳转到会话页面
                      XKitRouter.withKey(PATH_CHAT_TEAM_PAGE)
                          .withContext(context)
                          .withParam(CHAT_KRY, teamInfo)
                          .withParam(CHAT_ID_KRY, teamInfo.getTeamId())
                          .navigate();

                    } else {
                      ALog.e(TAG, "create team failed.");
                    }
                  });
          return null;
        });
  }
}
