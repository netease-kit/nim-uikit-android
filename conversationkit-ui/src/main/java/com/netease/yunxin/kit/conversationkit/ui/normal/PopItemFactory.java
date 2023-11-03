// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.conversationkit.ui.normal;

import static com.netease.yunxin.kit.corekit.im.utils.RouterConstant.PATH_ADD_FRIEND_PAGE;
import static com.netease.yunxin.kit.corekit.im.utils.RouterConstant.PATH_CREATE_ADVANCED_TEAM_ACTION;
import static com.netease.yunxin.kit.corekit.im.utils.RouterConstant.PATH_CREATE_NORMAL_TEAM_ACTION;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import com.netease.yunxin.kit.common.ui.widgets.ContentListPopView;
import com.netease.yunxin.kit.conversationkit.ui.R;
import com.netease.yunxin.kit.corekit.route.XKitRouter;

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
        .configView(getView(context, R.string.add_friend, R.drawable.ic_conversation_add_friend))
        .configParams(params)
        .configClickListener(
            v -> XKitRouter.withKey(PATH_ADD_FRIEND_PAGE).withContext(context).navigate())
        .build();
  }

  public static ContentListPopView.Item getCreateAdvancedTeamItem(
      Context context, int memberLimit) {
    LinearLayout.LayoutParams params = getParams(context);
    params.setMargins(
        (int) context.getResources().getDimension(R.dimen.pop_text_margin_left),
        0,
        0,
        (int) context.getResources().getDimension(R.dimen.pop_text_margin_right_top));
    int requestCode = 1;
    return new ContentListPopView.Item.Builder()
        .configView(
            getView(
                context, R.string.create_advanced_team, R.drawable.ic_conversation_advanced_team))
        .configParams(params)
        .configClickListener(
            getClickListener(context, requestCode, PATH_CREATE_ADVANCED_TEAM_ACTION, memberLimit))
        .build();
  }

  public static ContentListPopView.Item getCreateGroupTeamItem(Context context, int memberLimit) {
    LinearLayout.LayoutParams params = getParams(context);
    params.setMargins(
        (int) context.getResources().getDimension(R.dimen.pop_text_margin_left),
        0,
        (int) context.getResources().getDimension(R.dimen.pop_text_margin_right_top),
        0);
    int requestCode = 2;
    return new ContentListPopView.Item.Builder()
        .configView(
            getView(context, R.string.create_group_team, R.drawable.ic_conversation_group_team))
        .configParams(params)
        .configClickListener(
            getClickListener(context, requestCode, PATH_CREATE_NORMAL_TEAM_ACTION, memberLimit))
        .build();
  }

  private static View.OnClickListener getClickListener(
      Context context, int requestCode, String createMethod, int memberLimit) {
    return v ->
        NormalCreateTeamFactory.selectAndCreateTeam(
            context, requestCode, createMethod, null, null, memberLimit);
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
