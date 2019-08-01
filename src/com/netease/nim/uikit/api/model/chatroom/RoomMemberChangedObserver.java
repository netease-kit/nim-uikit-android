package com.netease.nim.uikit.api.model.chatroom;

import com.netease.nimlib.sdk.chatroom.model.ChatRoomMember;

/**
 * UIKit 与 app 聊天室成员数据变更监听接口
 */

public interface RoomMemberChangedObserver {

    /**
     * 聊天室新增成员
     *
     * @param member 成员
     */
    void onRoomMemberIn(ChatRoomMember member);

    /**
     * 聊天室退出成员
     *
     * @param member 成员
     */
    void onRoomMemberExit(ChatRoomMember member);
}
