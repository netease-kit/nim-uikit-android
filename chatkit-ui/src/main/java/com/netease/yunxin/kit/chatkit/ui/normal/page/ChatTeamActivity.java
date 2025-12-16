// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.normal.page;

import static com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant.LIB_TAG;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import com.netease.nimlib.coexist.sdk.v2.message.V2NIMMessage;
import com.netease.nimlib.coexist.sdk.v2.team.model.V2NIMTeam;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.model.IMMessageInfo;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.normal.page.fragment.ChatTeamFragment;
import com.netease.yunxin.kit.chatkit.ui.page.ChatBaseActivity;
import com.netease.yunxin.kit.corekit.coexist.im2.utils.RouterConstant;

/** 标准皮肤，群聊会话页面。 */
public class ChatTeamActivity extends ChatBaseActivity {

  ChatTeamFragment chatFragment;

  private static final String TAG = "ChatTeamActivity";

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    changeStatusBarColor(R.color.color_white);
  }

  @Override
  public void onConfigurationChanged(@NonNull Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
  }

  @Override
  public void initChat() {
    V2NIMTeam teamInfo = (V2NIMTeam) getIntent().getSerializableExtra(RouterConstant.CHAT_KRY);
    String teamId = getIntent().getStringExtra(RouterConstant.CHAT_ID_KRY);
    if (teamInfo == null && TextUtils.isEmpty(teamId)) {
      ALog.e(LIB_TAG, TAG, "team info is null && team id is null" + teamId);
      finish();
      return;
    }
    //set fragment
    chatFragment = new ChatTeamFragment();
    Bundle bundle = new Bundle();
    bundle.putSerializable(RouterConstant.CHAT_KRY, teamInfo);
    bundle.putSerializable(RouterConstant.CHAT_ID_KRY, teamId);
    V2NIMMessage message =
        (V2NIMMessage) getIntent().getSerializableExtra(RouterConstant.KEY_MESSAGE);
    if (message != null) {
      bundle.putSerializable(RouterConstant.KEY_MESSAGE, message);
    }
    IMMessageInfo messageInfo =
        (IMMessageInfo) getIntent().getSerializableExtra(RouterConstant.KEY_MESSAGE_INFO);
    if (messageInfo != null) {
      bundle.putSerializable(RouterConstant.KEY_MESSAGE_INFO, messageInfo);
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

  @Override
  protected void onDestroy() {
    super.onDestroy();
  }
}
