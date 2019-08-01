package com.netease.nim.uikit.business.chatroom.helper;

import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nimlib.sdk.chatroom.constant.MemberType;
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
        ChatRoomMember chatRoomMember = NimUIKit.getChatRoomProvider().getChatRoomMember(roomId, NimUIKit.getAccount());
        if (chatRoomMember != null && chatRoomMember.getMemberType() != null) {
            ext.put("type", chatRoomMember.getMemberType().getValue());
            message.setRemoteExtension(ext);
        }
    }

    public static MemberType getMemberTypeByRemoteExt(ChatRoomMessage message) {
        final String KEY = "type";
        Map<String, Object> ext = message.getRemoteExtension();

        if (ext == null || !ext.containsKey(KEY)) {
            return MemberType.UNKNOWN;
        }

        return MemberType.typeOfValue((Integer) ext.get(KEY));
    }
}
