// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.teamkit.ui.activity;

import static com.netease.yunxin.kit.corekit.im.utils.RouterConstant.KEY_TEAM_ICON;
import static com.netease.yunxin.kit.corekit.im.utils.RouterConstant.KEY_TEAM_ID;
import static com.netease.yunxin.kit.teamkit.ui.activity.TeamInfoActivity.KEY_TEAM_UPDATE_INFO_PRIVILEGE;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.Nullable;
import com.bumptech.glide.Glide;
import com.netease.yunxin.kit.common.ui.activities.BaseActivity;
import com.netease.yunxin.kit.common.ui.photo.PhotoChoiceDialog;
import com.netease.yunxin.kit.common.ui.utils.CommonCallback;
import com.netease.yunxin.kit.common.utils.NetworkUtils;
import com.netease.yunxin.kit.corekit.im.provider.FetchCallback;
import com.netease.yunxin.kit.corekit.im.repo.CommonRepo;
import com.netease.yunxin.kit.teamkit.ui.R;
import com.netease.yunxin.kit.teamkit.ui.databinding.TeamUpdateIconActivityBinding;
import com.netease.yunxin.kit.teamkit.ui.viewmodel.TeamSettingViewModel;
import com.netease.yunxin.kit.teamkit.utils.IconUrlUtils;
import java.io.File;

/** set team icon activity */
public class TeamUpdateIconActivity extends BaseActivity {
  private TeamUpdateIconActivityBinding binding;
  private final TeamSettingViewModel model = new TeamSettingViewModel();

  private View lastFocusView;
  private String lastUrl;
  private String iconUrl;
  private String teamId;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = TeamUpdateIconActivityBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());

    lastUrl = getIntent().getStringExtra(KEY_TEAM_ICON);
    teamId = getIntent().getStringExtra(KEY_TEAM_ID);
    boolean hasUpdatePrivilege = getIntent().getBooleanExtra(KEY_TEAM_UPDATE_INFO_PRIVILEGE, false);
    binding.groupPrivilege.setVisibility(hasUpdatePrivilege ? View.VISIBLE : View.GONE);

    Glide.with(getApplicationContext()).load(lastUrl).circleCrop().into(binding.ivIcon);
    changeStatusBarColor(R.color.color_eff1f4);
    binding.ivCamera.setOnClickListener(v -> choicePhoto());

    int index = IconUrlUtils.getDefaultIconUrlIndex(lastUrl);
    switch (index) {
      case 0:
        updateFocusBg(binding.ivDefault1, lastUrl);
        break;
      case 1:
        updateFocusBg(binding.ivDefault2, lastUrl);
        break;
      case 2:
        updateFocusBg(binding.ivDefault3, lastUrl);
        break;
      case 3:
        updateFocusBg(binding.ivDefault4, lastUrl);
        break;
      case 4:
        updateFocusBg(binding.ivDefault5, lastUrl);
        break;
    }

    binding.ivDefault1.setOnClickListener(v -> updateFocusBg(v, IconUrlUtils.getDefaultIconUrl(0)));
    binding.ivDefault2.setOnClickListener(v -> updateFocusBg(v, IconUrlUtils.getDefaultIconUrl(1)));
    binding.ivDefault3.setOnClickListener(v -> updateFocusBg(v, IconUrlUtils.getDefaultIconUrl(2)));
    binding.ivDefault4.setOnClickListener(v -> updateFocusBg(v, IconUrlUtils.getDefaultIconUrl(3)));
    binding.ivDefault5.setOnClickListener(v -> updateFocusBg(v, IconUrlUtils.getDefaultIconUrl(4)));

    binding.tvCancel.setOnClickListener(v -> finish());

    binding.tvSave.setOnClickListener(
        v -> {
          showLoading();
          model.updateIcon(teamId, iconUrl);
        });
    model
        .getIconData()
        .observe(
            this,
            stringResultInfo -> {
              dismissLoading();
              if (!stringResultInfo.getSuccess()) {
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

  private void updateFocusBg(View view, String url) {
    if (lastFocusView != null) {
      lastFocusView.setBackground(null);
    }
    if (view != null) {
      view.setBackgroundResource(R.drawable.bg_team_default);
    }
    iconUrl = url;
    Glide.with(getApplicationContext()).load(iconUrl).circleCrop().into(binding.ivIcon);
    lastFocusView = view;
  }

  private void choicePhoto() {
    new PhotoChoiceDialog(this)
        .show(
            new CommonCallback<File>() {
              @Override
              public void onSuccess(@Nullable File param) {
                if (NetworkUtils.isConnected()) {
                  CommonRepo.uploadImage(
                      param,
                      new FetchCallback<String>() {
                        @Override
                        public void onSuccess(@Nullable String urlParam) {
                          updateFocusBg(null, urlParam);
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

  public static void launch(
      Context context,
      boolean hasUpdatePrivilege,
      String teamId,
      String url,
      ActivityResultLauncher<Intent> launcher) {
    Intent intent = new Intent(context, TeamUpdateIconActivity.class);
    intent.putExtra(KEY_TEAM_UPDATE_INFO_PRIVILEGE, hasUpdatePrivilege);
    intent.putExtra(KEY_TEAM_ICON, url);
    intent.putExtra(KEY_TEAM_ID, teamId);
    if (!(context instanceof Activity)) {
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }
    launcher.launch(intent);
  }
}
