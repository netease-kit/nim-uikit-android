// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.normal.page;

import static com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant.LIB_TAG;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.normal.page.fragment.ChatP2PFragment;
import com.netease.yunxin.kit.chatkit.ui.page.ChatBaseActivity;
import com.netease.yunxin.kit.corekit.im.model.UserInfo;
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant;

/** 标准皮肤，单聊会话页面。 */
public class ChatP2PActivity extends ChatBaseActivity {

  private static final String TAG = "ChatP2PActivity";
  private ChatP2PFragment chatFragment;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    changeStatusBarColor(R.color.color_white);
  }

  @Override
  public void initChat() {
    UserInfo userInfo = (UserInfo) getIntent().getSerializableExtra(RouterConstant.CHAT_KRY);
    String accId = getIntent().getStringExtra(RouterConstant.CHAT_ID_KRY);
    ALog.e(LIB_TAG, TAG, "initChat:" + accId);
    if (userInfo == null && TextUtils.isEmpty(accId)) {
      ALog.e(LIB_TAG, TAG, "user info is null && accid is null:");
      finish();
      return;
    }
    //set fragment
    chatFragment = new ChatP2PFragment();
    Bundle bundle = new Bundle();
    bundle.putSerializable(RouterConstant.CHAT_ID_KRY, accId);
    bundle.putSerializable(RouterConstant.CHAT_KRY, userInfo);
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
