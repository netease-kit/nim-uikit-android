/*
 * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.kit.chatkit.ui.page;

import android.os.Bundle;

import androidx.fragment.app.FragmentManager;

import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.ui.ChatUIConfig;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.builder.P2PChatFragmentBuilder;
import com.netease.yunxin.kit.chatkit.ui.page.fragment.ChatP2PFragment;
import com.netease.yunxin.kit.corekit.im.model.UserInfo;
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant;

/**
 * P2P chat page
 */
public class ChatP2PActivity extends ChatBaseActivity {

    private static final String LOG_TOG = "ChatP2PActivity";

    @Override
    public void initChat() {
        UserInfo userInfo = (UserInfo) getIntent().getSerializableExtra(RouterConstant.CHAT_KRY);
        if (userInfo == null) {
            ALog.e(LOG_TOG, "user info is null");
            return;
        }
        ALog.i(LOG_TOG, "userInfo account is = " + userInfo.getAccount());
        //set fragment
        P2PChatFragmentBuilder fragmentBuilder = ChatUIConfig.getInstance().getP2PChatFragmentBuilder() == null
                ? new P2PChatFragmentBuilder() : ChatUIConfig.getInstance().getP2PChatFragmentBuilder();

        ChatP2PFragment chatFragment = fragmentBuilder.build();
        Bundle bundle = new Bundle();
        bundle.putSerializable(RouterConstant.CHAT_KRY, userInfo);
        chatFragment.setArguments(bundle);

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.addFragmentOnAttachListener((fragmentManager1, fragment) -> {
            if (fragment instanceof ChatP2PFragment) {
                fragmentBuilder.attachFragment((ChatP2PFragment) fragment);
            }
        });
        fragmentManager
                .beginTransaction()
                .add(R.id.container, chatFragment)
                .commitAllowingStateLoss();
    }
}
