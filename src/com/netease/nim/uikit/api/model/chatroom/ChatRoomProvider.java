package com.netease.nim.uikit.api.model.chatroom;

import com.netease.nim.uikit.api.model.SimpleCallback;
import com.netease.nimlib.sdk.chatroom.constant.MemberQueryType;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomMember;

import java.util.List;

/**
 * 聊天室成员提供者
 */

public interface ChatRoomProvider {

    /**
     * 获取聊天室成员
     *
     * @param roomId  聊天室
     * @param account 账号
     * @return ChatRoomMember
     */
    ChatRoomMember getChatRoomMember(String roomId, String account);

    /**
     * 异步获取聊天室成员
     *
     * @param roomId   聊天室
     * @param account  账号
     * @param callback 回调
     */
    void fetchMember(String roomId, String account, SimpleCallback<ChatRoomMember> callback);

    /**
     * 异步获取聊天室成员列表
     *
     * @param roomId          聊天室
     * @param memberQueryType 请求类型
     * @param time            时间
     * @param limit           条数限制
     * @param callback        回调
     */
    void fetchRoomMembers(String roomId, MemberQueryType memberQueryType, long time, int limit, SimpleCallback<List<ChatRoomMember>> callback);
}
