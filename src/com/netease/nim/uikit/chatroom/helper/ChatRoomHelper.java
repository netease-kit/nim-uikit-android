package com.netease.nim.uikit.chatroom.helper;

import com.netease.nim.uikit.NimUIKit;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomMember;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomMessage;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by huangjun on 2017/9/18.
 */
public class ChatRoomHelper {
    public static void buildMemberTypeInRemoteExt(ChatRoomMessage message, String roomId) {
        Map<String, Object> ext = new HashMap<>();
        ChatRoomMember chatRoomMember = ChatRoomMemberCache.getInstance().getChatRoomMember(roomId, NimUIKit.getAccount());
        if (chatRoomMember != null && chatRoomMember.getMemberType() != null) {
            ext.put("type", chatRoomMember.getMemberType().getValue());
            message.setRemoteExtension(ext);
        }
    }
}
