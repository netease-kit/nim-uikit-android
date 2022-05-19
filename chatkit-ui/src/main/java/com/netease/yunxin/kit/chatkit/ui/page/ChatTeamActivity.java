/*
 * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.kit.chatkit.ui.page;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.FragmentManager;

import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.ui.ChatUIConfig;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.builder.TeamChatFragmentBuilder;
import com.netease.yunxin.kit.chatkit.ui.page.fragment.ChatTeamFragment;
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant;

/**
 * Team chat page
 */
public class ChatTeamActivity extends ChatBaseActivity {

    ChatTeamFragment chatFragment;

    private static final String LOG_TOG = "ChatGroupActivity";

    @Override
    public void initChat() {
        Team teamInfo = (Team) getIntent().getSerializableExtra(RouterConstant.CHAT_KRY);
        if (teamInfo == null) {
            ALog.e(LOG_TOG, "team info is null");
            return;
        }
        //set fragment
        TeamChatFragmentBuilder fragmentBuilder = ChatUIConfig.getInstance().getTeamChatFragmentBuilder() == null ?
                new TeamChatFragmentBuilder() : ChatUIConfig.getInstance().getTeamChatFragmentBuilder();
        chatFragment = fragmentBuilder.build();
        Bundle bundle = new Bundle();
        bundle.putSerializable(RouterConstant.CHAT_KRY, teamInfo);
        IMMessage message = (IMMessage) getIntent().getSerializableExtra(RouterConstant.KEY_MESSAGE);
        if (message != null) {
            bundle.putSerializable(RouterConstant.KEY_MESSAGE, message);
        }
        chatFragment.setArguments(bundle);
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.addFragmentOnAttachListener((fragmentManager1, fragment) -> {
            if (fragment instanceof ChatTeamFragment) {
                fragmentBuilder.attachFragment((ChatTeamFragment) fragment);
            }
        });
        fragmentManager
                .beginTransaction().add(R.id.container, chatFragment)
                .commitAllowingStateLoss();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        ALog.e(LOG_TOG, "onNewIntent");
        chatFragment.onNewIntent(intent);

    }
}
