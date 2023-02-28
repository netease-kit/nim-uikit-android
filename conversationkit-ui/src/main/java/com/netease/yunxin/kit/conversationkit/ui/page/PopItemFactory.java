// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.conversationkit.ui.page;

import static com.netease.yunxin.kit.corekit.im.utils.RouterConstant.CHAT_KRY;
import static com.netease.yunxin.kit.corekit.im.utils.RouterConstant.KEY_CONTACT_SELECTOR_MAX_COUNT;
import static com.netease.yunxin.kit.corekit.im.utils.RouterConstant.KEY_REMOTE_EXTENSION;
import static com.netease.yunxin.kit.corekit.im.utils.RouterConstant.KEY_REQUEST_SELECTOR_NAME;
import static com.netease.yunxin.kit.corekit.im.utils.RouterConstant.KEY_REQUEST_SELECTOR_NAME_ENABLE;
import static com.netease.yunxin.kit.corekit.im.utils.RouterConstant.KEY_SESSION_ID;
import static com.netease.yunxin.kit.corekit.im.utils.RouterConstant.KEY_TEAM_CREATED_TIP;
import static com.netease.yunxin.kit.corekit.im.utils.RouterConstant.PATH_ADD_FRIEND_PAGE;
import static com.netease.yunxin.kit.corekit.im.utils.RouterConstant.PATH_CHAT_SEND_TEAM_TIP_ACTION;
import static com.netease.yunxin.kit.corekit.im.utils.RouterConstant.PATH_CHAT_TEAM_PAGE;
import static com.netease.yunxin.kit.corekit.im.utils.RouterConstant.PATH_CONTACT_SELECTOR_PAGE;
import static com.netease.yunxin.kit.corekit.im.utils.RouterConstant.PATH_CREATE_ADVANCED_TEAM_ACTION;
import static com.netease.yunxin.kit.corekit.im.utils.RouterConstant.PATH_CREATE_NORMAL_TEAM_ACTION;
import static com.netease.yunxin.kit.corekit.im.utils.RouterConstant.REQUEST_CONTACT_SELECTOR_KEY;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import com.netease.nimlib.sdk.team.model.CreateTeamResult;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.common.ui.photo.TransHelper;
import com.netease.yunxin.kit.common.ui.widgets.ContentListPopView;
import com.netease.yunxin.kit.conversationkit.ui.R;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/** pop menu factory */
public final class PopItemFactory {
  private static final String TAG = "PopItemFactory";

  public static ContentListPopView.Item getAddFriendItem(Context context) {
    LinearLayout.LayoutParams params = getParams(context);
    params.setMargins(
        (int) context.getResources().getDimension(R.dimen.pop_text_margin_left),
        (int) context.getResources().getDimension(R.dimen.pop_text_margin_right_top),
        0,
        0);
    return new ContentListPopView.Item.Builder()
        .configView(getView(context, R.string.add_friend, R.drawable.icon_add_friend))
        .configParams(params)
        .configClickListener(
            v -> XKitRouter.withKey(PATH_ADD_FRIEND_PAGE).withContext(context).navigate())
        .build();
  }

  public static ContentListPopView.Item getCreateAdvancedTeamItem(Context context) {
    LinearLayout.LayoutParams params = getParams(context);
    params.setMargins(
        (int) context.getResources().getDimension(R.dimen.pop_text_margin_left),
        0,
        0,
        (int) context.getResources().getDimension(R.dimen.pop_text_margin_right_top));
    int requestCode = 1;
    return new ContentListPopView.Item.Builder()
        .configView(getView(context, R.string.create_advanced_team, R.drawable.icon_advanced_team))
        .configParams(params)
        .configClickListener(
            getClickListener(context, requestCode, PATH_CREATE_ADVANCED_TEAM_ACTION))
        .build();
  }

  public static ContentListPopView.Item getCreateGroupTeamItem(Context context) {
    LinearLayout.LayoutParams params = getParams(context);
    params.setMargins(
        (int) context.getResources().getDimension(R.dimen.pop_text_margin_left),
        0,
        (int) context.getResources().getDimension(R.dimen.pop_text_margin_right_top),
        0);
    int requestCode = 2;
    return new ContentListPopView.Item.Builder()
        .configView(getView(context, R.string.create_group_team, R.drawable.icon_group_team))
        .configParams(params)
        .configClickListener(getClickListener(context, requestCode, PATH_CREATE_NORMAL_TEAM_ACTION))
        .build();
  }

  private static View.OnClickListener getClickListener(
      Context context, int requestCode, String createMethod) {
    return v ->
        TransHelper.launchTask(
            context,
            requestCode,
            (activity, integer) -> {
              XKitRouter.withKey(PATH_CONTACT_SELECTOR_PAGE)
                  .withParam(KEY_CONTACT_SELECTOR_MAX_COUNT, 199)
                  .withParam(KEY_REQUEST_SELECTOR_NAME_ENABLE, true)
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
              ArrayList<String> list = data.getStringArrayListExtra(REQUEST_CONTACT_SELECTOR_KEY);
              if (list == null || list.isEmpty()) {
                ALog.e(TAG, "no one was chosen.");
                return null;
              }
              XKitRouter.withKey(createMethod)
                  .withParam(REQUEST_CONTACT_SELECTOR_KEY, list)
                  .withParam(
                      KEY_REQUEST_SELECTOR_NAME,
                      data.getStringArrayListExtra(KEY_REQUEST_SELECTOR_NAME))
                  .navigate(
                      res -> {
                        if (res.getSuccess() && res.getValue() instanceof CreateTeamResult) {
                          Team teamInfo = ((CreateTeamResult) res.getValue()).getTeam();
                          if (TextUtils.equals(createMethod, PATH_CREATE_ADVANCED_TEAM_ACTION)) {
                            Map<String, Object> map = new HashMap<>(1);
                            map.put(
                                KEY_TEAM_CREATED_TIP,
                                context.getString(R.string.create_advanced_team_success));
                            XKitRouter.withKey(PATH_CHAT_SEND_TEAM_TIP_ACTION)
                                .withParam(KEY_SESSION_ID, teamInfo.getId())
                                .withParam(KEY_REMOTE_EXTENSION, map)
                                .navigate();
                          }
                          XKitRouter.withKey(PATH_CHAT_TEAM_PAGE)
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

  private static View getView(Context context, int txtId, int drawableId) {
    TextView textView = new TextView(context);
    textView.setGravity(Gravity.CENTER_VERTICAL);
    textView.setTextSize(14);
    textView.setMaxLines(1);
    textView.setText(txtId);
    Drawable drawable = ContextCompat.getDrawable(context, drawableId);
    if (drawable != null) {
      drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
      textView.setCompoundDrawables(drawable, null, null, null);
    }
    textView.setTextColor(ContextCompat.getColor(context, R.color.color_333333));
    textView.setCompoundDrawablePadding(
        (int) context.getResources().getDimension(R.dimen.pop_text_margin_right_top));
    return textView;
  }

  private static LinearLayout.LayoutParams getParams(Context context) {
    return new LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.WRAP_CONTENT,
        (int) context.getResources().getDimension(R.dimen.pop_item_height));
  }
}
