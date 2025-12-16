// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.team;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import com.netease.nimlib.coexist.sdk.v2.team.model.V2NIMTeam;
import com.netease.yunxin.kit.chatkit.model.TeamMemberWithUserInfo;
import com.netease.yunxin.kit.chatkit.utils.TeamUtils;
import com.netease.yunxin.kit.common.ui.activities.BaseLocalActivity;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.common.ui.widgets.BackTitleBar;
import com.netease.yunxin.kit.common.ui.widgets.ContactAvatarView;
import com.netease.yunxin.kit.common.utils.NetworkUtils;
import com.netease.yunxin.kit.contactkit.ui.R;
import com.netease.yunxin.kit.contactkit.ui.utils.ColorUtils;
import com.netease.yunxin.kit.contactkit.ui.utils.ContactUtils;
import com.netease.yunxin.kit.corekit.coexist.im2.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import java.util.Objects;

public abstract class BaseTeamProfileActivity extends BaseLocalActivity {
  protected TeamProfileViewModel viewModel;
  protected ContactAvatarView ivTeamAvatar;
  protected TextView tvTeamName;
  protected TextView tvTeamDesc;
  protected TextView tvTeamId;
  protected TextView tvTeamOwner;
  protected TextView tvChat;
  protected String teamId;
  private View rootView;

  protected BackTitleBar titleBar;

  protected V2NIMTeam teamInfo;

  protected abstract View initViewAndGetRootView(Bundle savedInstanceState);

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    rootView = initViewAndGetRootView(savedInstanceState);
    checkViews();
    setContentView(rootView);
    titleBar.setOnBackIconClickListener(v -> onBackPressed());
    initView();
    initData();
  }

  protected void checkViews() {
    Objects.requireNonNull(rootView);
    Objects.requireNonNull(titleBar);
    Objects.requireNonNull(ivTeamAvatar);
  }

  private void initView() {}

  protected void goChat() {
    String path = RouterConstant.PATH_CHAT_TEAM_PAGE;
    XKitRouter.withKey(path)
        .withParam(RouterConstant.CHAT_ID_KRY, teamId)
        .withContext(BaseTeamProfileActivity.this)
        .navigate();
    finish();
  }

  private void initData() {
    viewModel = new ViewModelProvider(this).get(TeamProfileViewModel.class);
    teamId = getIntent().getStringExtra(RouterConstant.KEY_ACCOUNT_ID_KEY);
    if (TextUtils.isEmpty(teamId)) {
      finish();
    }
    viewModel.init(teamId);
    viewModel
        .getTeamLiveData()
        .observe(
            this,
            fetchResult -> {
              if (fetchResult.getLoadStatus() == LoadStatus.Success
                  && fetchResult.getData() != null) {
                bindTeamData(fetchResult.getData());
              } else {
                if (!NetworkUtils.isConnected()) {
                  Toast.makeText(this, R.string.contact_network_error_tip, Toast.LENGTH_SHORT)
                      .show();
                }
              }
            });

    viewModel
        .getTeamOwnerLiveData()
        .observe(
            this,
            result -> {
              if (result.getLoadStatus() == LoadStatus.Success) {
                bindTeamOwnerData(result.getData());
              }
            });

    // 申请入群，如果不需要同意则直接进入聊天页面
    viewModel
        .getToTeamChatLiveData()
        .observe(
            this,
            result -> {
              if (result.getLoadStatus() == LoadStatus.Success
                  && TextUtils.equals(result.getData(), teamId)) {
                goChat();
              }
            });
    viewModel.getTeamInfoAndTeamOwner();
  }

  protected void bindTeamData(V2NIMTeam team) {
    if (team != null) {
      teamInfo = team;
      if (ivTeamAvatar != null) {
        ivTeamAvatar.setData(
            team.getAvatar(), team.getName(), ColorUtils.avatarColor(team.getTeamId()));
      }
      if (tvTeamName != null) {
        tvTeamName.setText(team.getName());
      }
      if (tvTeamDesc != null) {

        tvTeamDesc.setText(team.getIntro());
      }
      if (tvTeamId != null) {
        String teamName = String.format(getString(R.string.team_info_id_text), team.getTeamId());
        tvTeamId.setText(teamName);
      }
      if (team.isValidTeam()) {
        tvChat.setText(R.string.chat);
      } else {
        tvChat.setText(R.string.join_team_title);
      }
      tvChat.setOnClickListener(
          v -> {
            if (teamInfo.isValidTeam()) {
              goChat();
            } else {
              applyJoinTeam();
            }
          });
      if (TeamUtils.isTeamGroup(team)) {
        tvTeamOwner.setVisibility(View.GONE);
      } else {
        tvTeamOwner.setVisibility(View.VISIBLE);
      }
    }
  }

  protected void bindTeamOwnerData(TeamMemberWithUserInfo memberWithUserInfo) {
    if (memberWithUserInfo != null) {
      if (tvTeamOwner != null) {
        String teamIntroduce =
            String.format(
                getString(R.string.team_info_owner_text),
                memberWithUserInfo.getNameWithoutFriendAlias());
        tvTeamOwner.setText(teamIntroduce);
      }
    }
  }

  protected void applyJoinTeam() {
    if (ContactUtils.checkNetworkAndToast(this)) {
      viewModel.applyJoinTeam();
    }
  }
}
