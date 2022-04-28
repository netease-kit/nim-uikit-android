package com.netease.yunxin.kit.chatkit.ui;

import android.content.Context;

import androidx.annotation.NonNull;

import com.netease.yunxin.kit.chatkit.repo.ChatMessageRepo;
import com.netease.yunxin.kit.chatkit.ui.custom.CustomAttachParser;
import com.netease.yunxin.kit.chatkit.ChatService;
import com.netease.yunxin.kit.chatkit.ui.page.ChatP2PActivity;
import com.netease.yunxin.kit.chatkit.ui.page.ChatSearchActivity;
import com.netease.yunxin.kit.chatkit.ui.page.ChatTeamActivity;
import com.netease.yunxin.kit.chatkit.ui.view.emoji.EmojiManager;
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;

/**
 * launch service
 * when app start the ChatUIService will be created
 * it need to config in manifest
 */
public class ChatUIService extends ChatService {

    @NonNull
    @Override
    public String getServiceName() {
        return "ChatUIService";
    }

    @NonNull
    @Override
    public ChatService create(@NonNull Context context) {
        XKitRouter.registerRouter(RouterConstant.PATH_CHAT_P2P, ChatP2PActivity.class);
        XKitRouter.registerRouter(RouterConstant.PATH_CHAT_GROUP, ChatTeamActivity.class);
        XKitRouter.registerRouter(RouterConstant.PATH_CHAT_SEARCH, ChatSearchActivity.class);
        ChatMessageRepo.setCustomAttachParser(new CustomAttachParser());
        EmojiManager.init(context);
        return this;
    }
}
