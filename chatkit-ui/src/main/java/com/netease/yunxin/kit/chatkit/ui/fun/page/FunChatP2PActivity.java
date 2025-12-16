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
import com.netease.nimlib.coexist.sdk.msg.model.IMMessage;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.model.IMMessageInfo;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.fun.page.fragment.FunChatFragment;
import com.netease.yunxin.kit.chatkit.ui.fun.page.fragment.FunChatP2PFragment;
import com.netease.yunxin.kit.chatkit.ui.page.ChatBaseActivity;
import com.netease.yunxin.kit.corekit.coexist.im2.utils.RouterConstant;

/** Fun皮肤单聊聊天界面Activity，继承自ChatBaseActivity */
public class FunChatP2PActivity extends ChatBaseActivity {

  private static final String TAG = "ChatP2PFunActivity";
  private FunChatFragment chatFragment;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    changeStatusBarColor(R.color.fun_chat_page_bg_color);
  }

  @Override
  public void onConfigurationChanged(@NonNull Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
  }

  @Override
  public void initChat() {
    String conversationId = getIntent().getStringExtra(RouterConstant.CHAT_ID_KRY);
    ALog.e(LIB_TAG, TAG, "initChat:" + conversationId);
    if (TextUtils.isEmpty(conversationId)) {
      ALog.e(LIB_TAG, TAG, "user info is null && accid is null:");
      finish();
      return;
    }
    //set fragment
    chatFragment = new FunChatP2PFragment();
    Bundle bundle = new Bundle();
    bundle.putSerializable(RouterConstant.CHAT_ID_KRY, conversationId);
    IMMessage message = (IMMessage) getIntent().getSerializableExtra(RouterConstant.KEY_MESSAGE);
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
}
