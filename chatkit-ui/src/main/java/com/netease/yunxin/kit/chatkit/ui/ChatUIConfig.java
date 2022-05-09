/*
 * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.kit.chatkit.ui;

import com.netease.yunxin.kit.chatkit.ui.builder.P2PChatFragmentBuilder;
import com.netease.yunxin.kit.chatkit.ui.builder.TeamChatFragmentBuilder;

public class ChatUIConfig {
    private ChatUIConfig() {
    }

    private static volatile ChatUIConfig chatUIConfig;

    public static ChatUIConfig getInstance() {
        if (chatUIConfig == null) {
            synchronized (ChatUIConfig.class) {
                if (chatUIConfig == null) {
                    chatUIConfig = new ChatUIConfig();
                }
            }
        }

        return chatUIConfig;
    }

    P2PChatFragmentBuilder p2PChatFragmentBuilder;

    TeamChatFragmentBuilder teamChatFragmentBuilder;

    public void setP2PChatFragmentBuilder(P2PChatFragmentBuilder p2PChatFragmentBuilder) {
        this.p2PChatFragmentBuilder = p2PChatFragmentBuilder;
    }

    public P2PChatFragmentBuilder getP2PChatFragmentBuilder() {
        return p2PChatFragmentBuilder;
    }

    public void setTeamChatFragmentBuilder(TeamChatFragmentBuilder teamChatFragmentBuilder) {
        this.teamChatFragmentBuilder = teamChatFragmentBuilder;
    }

    public TeamChatFragmentBuilder getTeamChatFragmentBuilder() {
        return teamChatFragmentBuilder;
    }
}
