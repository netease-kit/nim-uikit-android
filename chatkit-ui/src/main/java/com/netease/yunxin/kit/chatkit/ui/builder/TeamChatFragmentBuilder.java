package com.netease.yunxin.kit.chatkit.ui.builder;

import com.netease.yunxin.kit.chatkit.ui.page.fragment.ChatTeamFragment;

public class TeamChatFragmentBuilder extends ChatFragmentBuilder<ChatTeamFragment> {

    @Override
    ChatTeamFragment getFragment() {
        return new ChatTeamFragment();
    }
}
