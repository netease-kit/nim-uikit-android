// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.teamkit.ui.activity;

import static com.netease.yunxin.kit.corekit.im2.utils.RouterConstant.KEY_TEAM_ICON;
import static com.netease.yunxin.kit.corekit.im2.utils.RouterConstant.KEY_TEAM_ID;
import static com.netease.yunxin.kit.corekit.im2.utils.RouterConstant.KEY_TEAM_NAME;
import static com.netease.yunxin.kit.teamkit.ui.activity.BaseTeamInfoActivity.KEY_TEAM_UPDATE_INFO_PRIVILEGE;
import static com.netease.yunxin.kit.teamkit.ui.utils.NetworkUtilsWrapper.handleNetworkBrokenResult;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.Group;
import com.netease.yunxin.kit.chatkit.repo.ResourceRepo;
import com.netease.yunxin.kit.common.ui.activities.BaseLocalActivity;
import com.netease.yunxin.kit.common.ui.photo.BasePhotoChoiceDialog;
import com.netease.yunxin.kit.common.ui.photo.PhotoChoiceDialog;
import com.netease.yunxin.kit.common.ui.utils.CommonCallback;
import com.netease.yunxin.kit.common.ui.widgets.ContactAvatarView;
import com.netease.yunxin.kit.common.utils.NetworkUtils;
import com.netease.yunxin.kit.corekit.im2.extend.FetchCallback;
import com.netease.yunxin.kit.teamkit.ui.R;
import com.netease.yunxin.kit.teamkit.ui.utils.ColorUtils;
import com.netease.yunxin.kit.teamkit.ui.utils.TeamIconUtils;
import com.netease.yunxin.kit.teamkit.ui.viewmodel.TeamSettingViewModel;
import java.io.File;
import java.util.Objects;

/** 群头像修改界面基类 子类需要实现{@link #initViewAndGetRootView(Bundle)}方法，返回界面的根布局 */
public abstract class BaseTeamUpdateIconActivity extends BaseLocalActivity {
  protected final TeamSettingViewModel model = new TeamSettingViewModel();

  private View rootView;
  protected View cancelView;
  protected Group groupPrivilege;
  protected ContactAvatarView ivIcon;
  protected ImageView ivCamera;
  protected ImageView ivDefault1;
  protected ImageView ivDefault2;
  protected ImageView ivDefault3;
  protected ImageView ivDefault4;
  protected ImageView ivDefault5;
  protected TextView tvSave;

  protected View lastFocusView;
  protected String lastUrl;
  protected String iconUrl;
  protected String teamId;
  protected String teamName;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    rootView = initViewAndGetRootView(savedInstanceState);
    checkViews();
    setContentView(rootView);

    lastUrl = getIntent().getStringExtra(KEY_TEAM_ICON);
    iconUrl = lastUrl;
    teamId = getIntent().getStringExtra(KEY_TEAM_ID);
    teamName = getIntent().getStringExtra(KEY_TEAM_NAME);
    boolean hasUpdatePrivilege = getIntent().getBooleanExtra(KEY_TEAM_UPDATE_INFO_PRIVILEGE, false);
    groupPrivilege.setVisibility(hasUpdatePrivilege ? View.VISIBLE : View.GONE);

    ivIcon.setData(lastUrl, teamName, ColorUtils.avatarColor(teamId));
    changeStatusBarColor(R.color.color_eff1f4);
    ivCamera.setOnClickListener(v -> choicePhoto());

    int index = TeamIconUtils.getDefaultIconUrlIndex(lastUrl, isCircle());
    switch (index) {
      case 0:
        updateFocusBg(ivDefault1, lastUrl);
        break;
      case 1:
        updateFocusBg(ivDefault2, lastUrl);
        break;
      case 2:
        updateFocusBg(ivDefault3, lastUrl);
        break;
      case 3:
        updateFocusBg(ivDefault4, lastUrl);
        break;
      case 4:
        updateFocusBg(ivDefault5, lastUrl);
        break;
    }

    ivDefault1.setOnClickListener(
        v -> updateFocusBg(v, TeamIconUtils.getDefaultIconUrl(0, isCircle())));
    ivDefault2.setOnClickListener(
        v -> updateFocusBg(v, TeamIconUtils.getDefaultIconUrl(1, isCircle())));
    ivDefault3.setOnClickListener(
        v -> updateFocusBg(v, TeamIconUtils.getDefaultIconUrl(2, isCircle())));
    ivDefault4.setOnClickListener(
        v -> updateFocusBg(v, TeamIconUtils.getDefaultIconUrl(3, isCircle())));
    ivDefault5.setOnClickListener(
        v -> updateFocusBg(v, TeamIconUtils.getDefaultIconUrl(4, isCircle())));

    cancelView.setOnClickListener(v -> finish());

    tvSave.setOnClickListener(
        v -> {
          if (!NetworkUtils.isConnected()) {
            Toast.makeText(this, R.string.team_network_error, Toast.LENGTH_SHORT).show();
            return;
          }
          if (!TextUtils.isEmpty(iconUrl)) {
            showLoading();
            model.updateIcon(teamId, iconUrl);
          } else {
            finish();
          }
        });
    model
        .getIconData()
        .observe(
            this,
            stringResultInfo -> {
              dismissLoading();
              if (!stringResultInfo.isSuccess()) {
                handleNetworkBrokenResult(this, stringResultInfo);
                return;
              }
              if (!TextUtils.equals(lastUrl, iconUrl)) {
                Intent intent = new Intent();
                intent.putExtra(KEY_TEAM_ICON, iconUrl);
                setResult(RESULT_OK, intent);
              }
              finish();
            });
  }

  protected abstract View initViewAndGetRootView(Bundle savedInstanceState);

  protected void checkViews() {
    Objects.requireNonNull(rootView);
    Objects.requireNonNull(cancelView);
    Objects.requireNonNull(groupPrivilege);
    Objects.requireNonNull(ivIcon);
    Objects.requireNonNull(ivCamera);
    Objects.requireNonNull(ivDefault1);
    Objects.requireNonNull(ivDefault2);
    Objects.requireNonNull(ivDefault3);
    Objects.requireNonNull(ivDefault4);
    Objects.requireNonNull(ivDefault5);
    Objects.requireNonNull(tvSave);
  }

  protected boolean isCircle() {
    return true;
  }

  private void updateFocusBg(View view, String url) {
    if (lastFocusView != null) {
      lastFocusView.setBackground(null);
    }
    if (view != null) {
      view.setBackgroundResource(R.drawable.bg_team_default);
    }
    iconUrl = url;
    ivIcon.setData(iconUrl, teamName, ColorUtils.avatarColor(teamId));
    lastFocusView = view;
  }

  private void choicePhoto() {
    getPhotoChoiceDialog()
        .show(
            new CommonCallback<File>() {
              @Override
              public void onSuccess(@Nullable File param) {
                if (NetworkUtils.isConnected() && param != null) {
                  ResourceRepo.uploadFile(
                      param,
                      new FetchCallback<String>() {
                        @Override
                        public void onError(int errorCode, @Nullable String errorMsg) {
                          Toast.makeText(
                                  getApplicationContext(),
                                  getString(R.string.team_request_fail),
                                  Toast.LENGTH_SHORT)
                              .show();
                        }

                        @Override
                        public void onSuccess(@Nullable String urlParam) {
                          updateFocusBg(null, urlParam);
                        }
                      });
                } else {
                  Toast.makeText(
                          getApplicationContext(),
                          getString(R.string.team_network_error),
                          Toast.LENGTH_SHORT)
                      .show();
                }
              }

              @Override
              public void onFailed(int code) {
                Toast.makeText(
                        getApplicationContext(),
                        getString(R.string.team_request_fail),
                        Toast.LENGTH_SHORT)
                    .show();
              }

              @Override
              public void onException(@Nullable Throwable exception) {
                Toast.makeText(
                        getApplicationContext(),
                        getString(R.string.team_request_fail),
                        Toast.LENGTH_SHORT)
                    .show();
              }
            });
  }

  protected BasePhotoChoiceDialog getPhotoChoiceDialog() {
    return new PhotoChoiceDialog(this);
  }

  /**
   * 启动群头像修改界面
   *
   * @param context 上下文
   * @param activity 群头像修改界面
   * @param hasUpdatePrivilege 是否有修改权限
   * @param teamId 群id
   * @param url 头像地址
   * @param teamName 群名称
   * @param launcher 启动器
   */
  public static void launch(
      Context context,
      Class<? extends Activity> activity,
      boolean hasUpdatePrivilege,
      String teamId,
      String url,
      String teamName,
      ActivityResultLauncher<Intent> launcher) {
    Intent intent = new Intent(context, activity);
    intent.putExtra(KEY_TEAM_UPDATE_INFO_PRIVILEGE, hasUpdatePrivilege);
    intent.putExtra(KEY_TEAM_ICON, url);
    intent.putExtra(KEY_TEAM_ID, teamId);
    intent.putExtra(KEY_TEAM_NAME, teamName);
    if (!(context instanceof Activity)) {
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }
    launcher.launch(intent);
  }
}
