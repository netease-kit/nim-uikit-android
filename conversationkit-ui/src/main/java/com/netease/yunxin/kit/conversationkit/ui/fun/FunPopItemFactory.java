// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.conversationkit.ui.fun;

import static com.netease.yunxin.kit.corekit.coexist.im2.utils.RouterConstant.PATH_FUN_ADD_FRIEND_PAGE;
import static com.netease.yunxin.kit.corekit.coexist.im2.utils.RouterConstant.PATH_FUN_CREATE_ADVANCED_TEAM_ACTION;
import static com.netease.yunxin.kit.corekit.coexist.im2.utils.RouterConstant.PATH_FUN_CREATE_NORMAL_TEAM_ACTION;
import static com.netease.yunxin.kit.corekit.coexist.im2.utils.RouterConstant.PATH_FUN_SEARCH_TEAM_PAGE;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import com.netease.yunxin.kit.common.ui.widgets.ContentListPopView;
import com.netease.yunxin.kit.common.utils.SizeUtils;
import com.netease.yunxin.kit.conversationkit.ui.R;
import com.netease.yunxin.kit.corekit.route.XKitRouter;

/** 会话列表点击加号弹出的pop item工厂 */
public final class FunPopItemFactory {
  private static final String TAG = "PopItemFactory";

  /**
   * 获取加号弹出的pop item:添加好友
   *
   * @param context 上下文
   * @return pop item
   */
  public static ContentListPopView.Item getAddFriendItem(Context context) {
    LinearLayout.LayoutParams params = getParams(context);
    params.setMargins(0, SizeUtils.dp2px(8), 0, 0);
    return new ContentListPopView.Item.Builder()
        .configView(
            getView(context, R.string.add_friend, R.drawable.fun_ic_conversation_add_friend))
        .configParams(params)
        .configClickListener(
            v -> XKitRouter.withKey(PATH_FUN_ADD_FRIEND_PAGE).withContext(context).navigate())
        .build();
  }

  /**
   * 获取加号弹出的pop item:创建高级群
   *
   * @param context 上下文
   * @param memberLimit 群成员上限
   * @return pop item
   */
  public static ContentListPopView.Item getCreateAdvancedTeamItem(
      Context context, int memberLimit) {
    LinearLayout.LayoutParams params = getParams(context);
    int requestCode = 1;
    return new ContentListPopView.Item.Builder()
        .configView(
            getView(
                context, R.string.create_advanced_team, R.drawable.fun_ic_conversation_create_team))
        .configParams(params)
        .configClickListener(
            getClickListener(
                context, requestCode, PATH_FUN_CREATE_ADVANCED_TEAM_ACTION, memberLimit))
        .build();
  }

  /**
   * 获取加号弹出的pop item:创建讨论组
   *
   * @param context 上下文
   * @param memberLimit 群成员上限
   * @return pop item
   */
  public static ContentListPopView.Item getCreateGroupTeamItem(Context context, int memberLimit) {
    LinearLayout.LayoutParams params = getParams(context);
    int requestCode = 2;
    return new ContentListPopView.Item.Builder()
        .configView(
            getView(
                context, R.string.create_group_team, R.drawable.fun_ic_conversation_create_team))
        .configParams(params)
        .configClickListener(
            getClickListener(context, requestCode, PATH_FUN_CREATE_NORMAL_TEAM_ACTION, memberLimit))
        .build();
  }

  /**
   * 获取加号弹出的pop item:查找群组
   *
   * @param context 上下文
   * @return pop item
   */
  public static ContentListPopView.Item getSearchGroupTeamItem(Context context) {
    LinearLayout.LayoutParams params = getParams(context);
    int requestCode = 2;
    return new ContentListPopView.Item.Builder()
        .configView(
            getView(context, R.string.join_group_team, R.drawable.fun_ic_conversation_create_team))
        .configParams(params)
        .configClickListener(
            v -> XKitRouter.withKey(PATH_FUN_SEARCH_TEAM_PAGE).withContext(context).navigate())
        .build();
  }

  /**
   * 获取加号弹出的pop item:分割线
   *
   * @param context 上下文
   * @return pop item
   */
  public static ContentListPopView.Item getDivideLineItem(Context context) {
    LinearLayout.LayoutParams params =
        new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, SizeUtils.dp2px(0.5f));
    params.setMargins(SizeUtils.dp2px(16), 0, 0, 0);
    return new ContentListPopView.Item.Builder()
        .configView(getDivideView(context))
        .configParams(params)
        .build();
  }

  /**
   * 获取加号弹出的pop item:创建点击事件，不同的按钮不同的响应事件
   *
   * @param context 上下文
   * @param requestCode 请求码 用于区分不同的点击事件
   * @param memberLimit 群成员上限
   * @return View.OnClickListener
   */
  private static View.OnClickListener getClickListener(
      Context context, int requestCode, String createMethod, int memberLimit) {
    return v ->
        FunCreateTeamFactory.selectAndCreateTeam(
            context, requestCode, createMethod, null, null, memberLimit);
  }

  /**
   * 获取加号弹出的pop item:文本+图标
   *
   * @param context 上下文
   * @param txtId 文本资源id
   * @param drawableId 图标资源id
   * @return pop item
   */
  private static View getView(Context context, int txtId, int drawableId) {
    TextView textView = new TextView(context);
    textView.setGravity(Gravity.CENTER_VERTICAL);
    textView.setTextSize(16);
    textView.setMaxLines(1);
    textView.setText(txtId);

    int marginSize =
        (int) context.getResources().getDimension(R.dimen.fun_add_pop_item_margin_right_top);
    int padding = (int) context.getResources().getDimension(R.dimen.fun_add_pop_item_padding);
    Drawable drawable = ContextCompat.getDrawable(context, drawableId);
    if (drawable != null) {
      drawable.setBounds(padding, 0, marginSize + padding, marginSize);
      textView.setCompoundDrawables(drawable, null, null, null);
    }
    textView.setPadding(
        (int) context.getResources().getDimension(R.dimen.dimen_8_dp),
        0,
        (int) context.getResources().getDimension(R.dimen.dimen_14_dp),
        0);
    textView.setMinWidth((int) context.getResources().getDimension(R.dimen.fun_add_pop_item_width));
    textView.setTextColor(
        context.getResources().getColor(R.color.fun_conversation_add_pop_text_color));
    textView.setCompoundDrawablePadding(marginSize);
    return textView;
  }

  private static View getDivideView(Context context) {
    View view = new View(context);
    view.setBackgroundResource(R.color.color_5a5a5a);
    return view;
  }

  private static LinearLayout.LayoutParams getParams(Context context) {
    return new LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.WRAP_CONTENT,
        (int) context.getResources().getDimension(R.dimen.fun_add_pop_item_height));
  }
}
