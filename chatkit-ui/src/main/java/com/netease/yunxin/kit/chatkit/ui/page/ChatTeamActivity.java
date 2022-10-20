// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.page;

import static com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant.LIB_TAG;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import androidx.fragment.app.FragmentManager;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.builder.TeamChatFragmentBuilder;
import com.netease.yunxin.kit.chatkit.ui.page.fragment.ChatTeamFragment;
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant;

/** Team chat page */
public class ChatTeamActivity extends ChatBaseActivity {

  ChatTeamFragment chatFragment;

  private static final String TAG = "ChatGroupActivity";

  @Override
  public void initChat() {
    Team teamInfo = (Team) getIntent().getSerializableExtra(RouterConstant.CHAT_KRY);
    String teamId = getIntent().getStringExtra(RouterConstant.CHAT_ID_KRY);
    if (teamInfo == null && TextUtils.isEmpty(teamId)) {
      ALog.e(LIB_TAG, TAG, "team info is null && team id is null" + teamId);
      finish();
      return;
    }
    //set fragment
    TeamChatFragmentBuilder fragmentBuilder = new TeamChatFragmentBuilder();
    chatFragment = fragmentBuilder.build();
    Bundle bundle = new Bundle();
    bundle.putSerializable(RouterConstant.CHAT_KRY, teamInfo);
    bundle.putSerializable(RouterConstant.CHAT_ID_KRY, teamId);
    IMMessage message = (IMMessage) getIntent().getSerializableExtra(RouterConstant.KEY_MESSAGE);
    if (message != null) {
      bundle.putSerializable(RouterConstant.KEY_MESSAGE, message);
    }
    chatFragment.setArguments(bundle);
    FragmentManager fragmentManager = getSupportFragmentManager();
    fragmentManager.beginTransaction().add(R.id.container, chatFragment).commitAllowingStateLoss();
  }

  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    ALog.e(LIB_TAG, TAG, "onNewIntent");
    chatFragment.onNewIntent(intent);
  }
}
