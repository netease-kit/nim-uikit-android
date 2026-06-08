// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.fun.page;

import static com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant.LIB_TAG;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import com.netease.nimlib.sdk.v2.topic.V2NIMTopic;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.fun.page.fragment.FunChatBotSubSessionChatFragment;
import com.netease.yunxin.kit.chatkit.ui.page.ChatBaseActivity;
import com.netease.yunxin.kit.corekit.im2.utils.RouterConstant;

public class FunChatBotSubSessionChatActivity extends ChatBaseActivity {

  private static final String TAG = "FunChatBotSubSessionChatActivity";
  private FunChatBotSubSessionChatFragment chatFragment;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    changeStatusBarColor(R.color.fun_chat_page_bg_color);
  }

  @Override
  protected void initChat() {
    String accountId = getIntent().getStringExtra(RouterConstant.CHAT_ID_KRY);
    if (TextUtils.isEmpty(accountId)) {
      finish();
      return;
    }
    V2NIMTopic topic =
        (V2NIMTopic) getIntent().getSerializableExtra(RouterConstant.KEY_BOT_SUB_SESSION_TOPIC);
    chatFragment = new FunChatBotSubSessionChatFragment();
    Bundle bundle = new Bundle();
    bundle.putSerializable(RouterConstant.CHAT_ID_KRY, accountId);
    bundle.putString(
        RouterConstant.KEY_SESSION_NAME,
        getIntent().getStringExtra(RouterConstant.KEY_SESSION_NAME));
    bundle.putString(
        RouterConstant.KEY_BOT_SUB_SESSION_CONVERSATION_ID,
        getIntent().getStringExtra(RouterConstant.KEY_BOT_SUB_SESSION_CONVERSATION_ID));
    bundle.putSerializable(RouterConstant.KEY_BOT_SUB_SESSION_TOPIC, topic);
    chatFragment.setArguments(bundle);
    FragmentManager fragmentManager = getSupportFragmentManager();
    fragmentManager.beginTransaction().add(R.id.container, chatFragment).commitAllowingStateLoss();
  }

  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    ALog.i(LIB_TAG, TAG, "onNewIntent");
    if (chatFragment != null) {
      chatFragment.onNewIntent(intent);
    }
  }
}
