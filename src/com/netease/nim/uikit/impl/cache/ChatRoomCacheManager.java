package com.netease.nim.uikit.impl.cache;

import com.netease.nim.uikit.impl.NimUIKitImpl;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomMember;

/**
 * Created by hzchenkang on 2017/10/18.
 * <p>
 * 聊天室相关业务缓存生命周期管理
 */

public class ChatRoomCacheManager {

    private static boolean enableCache;

    static {
        boolean userChatRoom = false;
        try {
            Class.forName("com.netease.nimlib.sdk.chatroom.ChatRoomService");
            userChatRoom = true;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        enableCache = userChatRoom && NimUIKitImpl.getOptions().buildChatRoomMemberCache;
    }

    public static void initCache() {
        if (enableCache) {
            ChatRoomMemberCache.getInstance().clear();
            ChatRoomMemberCache.getInstance().registerObservers(true);
        }
    }

    public static void clearCache() {
        if (enableCache) {
            ChatRoomMemberCache.getInstance().registerObservers(false);
            ChatRoomMemberCache.getInstance().clear();
        }
    }

    public static void clearRoomCache(String roomId) {
        if (enableCache) {
            ChatRoomMemberCache.getInstance().clearRoomCache(roomId);
        }
    }

    public static void saveMyMember(ChatRoomMember member) {
        if (enableCache) {
            ChatRoomMemberCache.getInstance().saveMyMember(member);
        }
    }
}
