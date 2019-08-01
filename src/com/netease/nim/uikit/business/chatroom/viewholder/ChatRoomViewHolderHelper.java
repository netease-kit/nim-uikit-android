package com.netease.nim.uikit.business.chatroom.viewholder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.netease.nim.uikit.R;
import com.netease.nim.uikit.business.chatroom.helper.ChatRoomHelper;
import com.netease.nim.uikit.business.uinfo.UserInfoHelper;
import com.netease.nim.uikit.impl.NimUIKitImpl;
import com.netease.nimlib.sdk.chatroom.constant.MemberType;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomMember;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomMessage;


/**
 * 聊天室成员姓名
 * Created by hzxuwen on 2016/1/20.
 */
public class ChatRoomViewHolderHelper {

    public static String getNameText(ChatRoomMessage message) {
        // 聊天室中显示姓名
        if (message.getChatRoomMessageExtension() != null) {
            return message.getChatRoomMessageExtension().getSenderNick();
        } else {
            ChatRoomMember member = NimUIKitImpl.getChatRoomProvider().getChatRoomMember(message.getSessionId(), message.getFromAccount());
            return member == null ? UserInfoHelper.getUserName(message.getFromAccount()) : member.getNick();
        }
    }

    public static void setStyleOfNameTextView(ChatRoomMessage message, TextView nameTextView, ImageView nameIconView) {
        nameTextView.setTextColor(NimUIKitImpl.getContext().getResources().getColor(R.color.color_black_ff999999));
        MemberType type = ChatRoomHelper.getMemberTypeByRemoteExt(message);
        if (type == MemberType.ADMIN) {
            nameIconView.setImageResource(R.drawable.nim_admin_icon);
            nameIconView.setVisibility(View.VISIBLE);
        } else if (type == MemberType.CREATOR) {
            nameIconView.setImageResource(R.drawable.nim_master_icon);
            nameIconView.setVisibility(View.VISIBLE);
        } else {
            nameIconView.setVisibility(View.GONE);
        }
    }
}
