// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.teamkit.ui.activity;

import static com.netease.yunxin.kit.corekit.im.utils.RouterConstant.KEY_TEAM_ICON;
import static com.netease.yunxin.kit.corekit.im.utils.RouterConstant.KEY_TEAM_ID;
import static com.netease.yunxin.kit.corekit.im.utils.RouterConstant.KEY_TEAM_NAME;
import static com.netease.yunxin.kit.teamkit.ui.activity.BaseTeamUpdateIntroduceActivity.KEY_TEAM_INTRODUCE;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import com.netease.nimlib.sdk.team.constant.TeamTypeEnum;
import com.netease.yunxin.kit.common.ui.activities.BaseActivity;
import com.netease.yunxin.kit.common.ui.widgets.ContactAvatarView;
import com.netease.yunxin.kit.teamkit.ui.R;
import com.netease.yunxin.kit.teamkit.ui.utils.ColorUtils;
import java.util.Objects;

/** team info activity */
public abstract class BaseTeamInfoActivity extends BaseActivity {
  public static final String KEY_TEAM_UPDATE_INFO_PRIVILEGE = "update_info_privilege";
  public static final String KEY_TEAM_TYPE = "team_type";
  public static final String KEY_TEAM_IS_GROUP = "team_group_tag";
  private View rootView;
  protected View line2;
  protected View ivBack;
  protected ContactAvatarView ivIcon;
  protected TextView tvTitle;
  protected TextView tvIcon;
  protected TextView tvName;
  protected TextView tvIntroduce;

  protected ActivityResultLauncher<Intent> launcher;

  protected String teamId;
  protected String teamIconUrl;
  protected String teamIntroduce;
  protected String teamName;
  protected TeamTypeEnum teamTypeEnum;
  protected boolean isGroup = false;

  protected boolean canUpdate = false;
  protected boolean hasUpdatePrivilege = false;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    rootView = initViewAndGetRootView(savedInstanceState);
    checkViews();
    setContentView(rootView);
    launcher =
        registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
              Intent data = result.getData();
              if (result.getResultCode() != RESULT_OK || data == null) {
                return;
              }
              canUpdate = true;
              Object iconObj = data.getStringExtra(KEY_TEAM_ICON);
              if (iconObj != null) {
                teamIconUrl = String.valueOf(iconObj);
                ivIcon.setData(teamIconUrl, teamName, ColorUtils.avatarColor(teamId));
              }
              Object introduceObj = data.getStringExtra(KEY_TEAM_INTRODUCE);
              if (introduceObj != null) {
                teamIntroduce = String.valueOf(introduceObj);
              }
              Object nameObj = data.getStringExtra(KEY_TEAM_NAME);
              if (nameObj != null) {
                teamName = String.valueOf(nameObj);
              }
            });

    changeStatusBarColor(R.color.color_eff1f4);

    Intent intent = getIntent();
    hasUpdatePrivilege = intent.getBooleanExtra(KEY_TEAM_UPDATE_INFO_PRIVILEGE, false);
    teamId = intent.getStringExtra(KEY_TEAM_ID);
    teamIconUrl = intent.getStringExtra(KEY_TEAM_ICON);
    teamIntroduce = intent.getStringExtra(KEY_TEAM_INTRODUCE);
    teamName = intent.getStringExtra(KEY_TEAM_NAME);
    isGroup = intent.getBooleanExtra(KEY_TEAM_IS_GROUP, false);
    teamTypeEnum = (TeamTypeEnum) intent.getSerializableExtra(KEY_TEAM_TYPE);

    ivIcon.setData(teamIconUrl, teamName, ColorUtils.avatarColor(teamId));
    if (teamTypeEnum == TeamTypeEnum.Advanced && !isGroup) {
      tvTitle.setText(R.string.team_info_title);
      tvIcon.setText(R.string.team_icon_title);
      tvName.setText(R.string.team_name_title);
      tvIntroduce.setVisibility(View.VISIBLE);
      line2.setVisibility(View.VISIBLE);
    } else {
      tvTitle.setText(R.string.team_group_info_title);
      tvIcon.setText(R.string.team_group_icon_title);
      tvName.setText(R.string.team_group_name_title);
      tvIntroduce.setVisibility(View.GONE);
      line2.setVisibility(View.GONE);
    }
    tvIcon.setOnClickListener(
        v ->
            BaseTeamUpdateIconActivity.launch(
                BaseTeamInfoActivity.this,
                getUpdateIconActivity(),
                hasUpdatePrivilege,
                teamId,
                teamIconUrl,
                teamName,
                launcher));
    tvName.setOnClickListener(
        v ->
            BaseTeamUpdateNameActivity.launch(
                BaseTeamInfoActivity.this,
                getUpdateNameActivity(),
                hasUpdatePrivilege,
                teamTypeEnum,
                teamId,
                teamName,
                isGroup,
                launcher));
    tvIntroduce.setOnClickListener(
        v ->
            BaseTeamUpdateIntroduceActivity.launch(
                BaseTeamInfoActivity.this,
                getUpdateIntroduceActivity(),
                hasUpdatePrivilege,
                teamId,
                teamIntroduce,
                launcher));
    ivBack.setOnClickListener(v -> finish());
  }

  protected abstract View initViewAndGetRootView(Bundle savedInstanceState);

  protected void checkViews() {
    Objects.requireNonNull(rootView);
    Objects.requireNonNull(line2);
    Objects.requireNonNull(ivBack);
    Objects.requireNonNull(ivIcon);
    Objects.requireNonNull(tvTitle);
    Objects.requireNonNull(tvIcon);
    Objects.requireNonNull(tvName);
    Objects.requireNonNull(tvIntroduce);
  }

  protected Class<? extends Activity> getUpdateNameActivity() {
    return null;
  }

  protected Class<? extends Activity> getUpdateIconActivity() {
    return null;
  }

  protected Class<? extends Activity> getUpdateIntroduceActivity() {
    return null;
  }

  @Override
  public void finish() {
    if (canUpdate) {
      Intent intent = new Intent();
      intent.putExtra(KEY_TEAM_ICON, teamIconUrl);
      intent.putExtra(KEY_TEAM_NAME, teamName);
      intent.putExtra(KEY_TEAM_INTRODUCE, teamIntroduce);
      setResult(RESULT_OK, intent);
    }
    super.finish();
  }

  public static void launch(
      Context context,
      Class<? extends Activity> activity,
      boolean hasUpdatePrivilege,
      TeamTypeEnum teamTypeEnum,
      String teamId,
      String teamName,
      String teamIntroduce,
      String teamIcon,
      boolean isGroup,
      ActivityResultLauncher<Intent> launcher) {
    Intent intent = new Intent(context, activity);
    intent.putExtra(KEY_TEAM_UPDATE_INFO_PRIVILEGE, hasUpdatePrivilege);
    intent.putExtra(KEY_TEAM_TYPE, teamTypeEnum);
    intent.putExtra(KEY_TEAM_ID, teamId);
    intent.putExtra(KEY_TEAM_ICON, teamIcon);
    intent.putExtra(KEY_TEAM_NAME, teamName);
    intent.putExtra(KEY_TEAM_INTRODUCE, teamIntroduce);
    intent.putExtra(KEY_TEAM_IS_GROUP, isGroup);
    if (!(context instanceof Activity)) {
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }
    launcher.launch(intent);
  }
}
