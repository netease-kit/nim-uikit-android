// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.
package com.netease.yunxin.kit.chatkit.ui.fun.page;

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
import com.netease.yunxin.kit.chatkit.ui.fun.page.fragment.FunChatTeamFragment;
import com.netease.yunxin.kit.chatkit.ui.page.ChatBaseActivity;
import com.netease.yunxin.kit.corekit.coexist.im2.utils.RouterConstant;

/** Fun皮肤群聊聊天页面，逻辑在Fragment中实现，该Activity只做Fragment你管理 */
public class FunChatTeamActivity extends ChatBaseActivity {

  FunChatTeamFragment chatFragment;

  private static final String TAG = "ChatGroupActivity";

  @Override
  public void initChat() {
    V2NIMTeam teamInfo = (V2NIMTeam) getIntent().getSerializableExtra(RouterConstant.CHAT_KRY);
    String conversationId = getIntent().getStringExtra(RouterConstant.CHAT_ID_KRY);
    if (teamInfo == null && TextUtils.isEmpty(conversationId)) {
      ALog.e(LIB_TAG, TAG, "team info is null && team id is null" + conversationId);
      finish();
      return;
    }
    //set fragment
    chatFragment = new FunChatTeamFragment();
    Bundle bundle = new Bundle();
    bundle.putSerializable(RouterConstant.CHAT_KRY, teamInfo);
    bundle.putSerializable(RouterConstant.CHAT_ID_KRY, conversationId);
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
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    changeStatusBarColor(R.color.fun_chat_page_bg_color);
  }

  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    ALog.e(LIB_TAG, TAG, "onNewIntent");
    chatFragment.onNewIntent(intent);
  }

  @Override
  public void onConfigurationChanged(@NonNull Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
  }
}
