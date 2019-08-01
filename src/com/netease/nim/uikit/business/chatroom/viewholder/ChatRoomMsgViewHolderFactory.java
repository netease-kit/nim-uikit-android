package com.netease.nim.uikit.business.chatroom.viewholder;

import com.netease.nimlib.sdk.chatroom.model.ChatRoomMessage;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomNotificationAttachment;
import com.netease.nimlib.sdk.msg.attachment.ImageAttachment;
import com.netease.nimlib.sdk.msg.attachment.MsgAttachment;
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum;
import com.netease.nimlib.sdk.robot.model.RobotAttachment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 聊天室消息项展示ViewHolder工厂类。
 */
public class ChatRoomMsgViewHolderFactory {

    private static HashMap<Class<? extends MsgAttachment>, Class<? extends ChatRoomMsgViewHolderBase>> viewHolders =
            new HashMap<>();

    static {
        // built in
        register(ChatRoomNotificationAttachment.class, ChatRoomMsgViewHolderNotification.class);
        register(RobotAttachment.class, ChatRoomMsgViewHolderRobot.class);
        register(ImageAttachment.class, ChatRoomMsgViewHolderPicture.class);
    }

    public static void register(Class<? extends MsgAttachment> attach, Class<? extends ChatRoomMsgViewHolderBase> viewHolder) {
        viewHolders.put(attach, viewHolder);
    }

    public static Class<? extends ChatRoomMsgViewHolderBase> getViewHolderByType(ChatRoomMessage message) {
        if (message.getMsgType() == MsgTypeEnum.text) {
            return ChatRoomMsgViewHolderText.class;
        } else {
            Class<? extends ChatRoomMsgViewHolderBase> viewHolder = null;
            if (message.getAttachment() != null) {
                Class<? extends MsgAttachment> clazz = message.getAttachment().getClass();
                while (viewHolder == null && clazz != null) {
                    viewHolder = viewHolders.get(clazz);
                    if (viewHolder == null) {
                        clazz = getSuperClass(clazz);
                    }
                }
            }
            return viewHolder == null ? ChatRoomMsgViewHolderUnknown.class : viewHolder;
        }
    }

    private static Class<? extends MsgAttachment> getSuperClass(Class<? extends MsgAttachment> derived) {
        Class sup = derived.getSuperclass();
        if (sup != null && MsgAttachment.class.isAssignableFrom(sup)) {
            return sup;
        } else {
            for (Class itf : derived.getInterfaces()) {
                if (MsgAttachment.class.isAssignableFrom(itf)) {
                    return itf;
                }
            }
        }
        return null;
    }

    public static List<Class<? extends ChatRoomMsgViewHolderBase>> getAllViewHolders() {
        List<Class<? extends ChatRoomMsgViewHolderBase>> list = new ArrayList<>();
        list.addAll(viewHolders.values());
        list.add(ChatRoomMsgViewHolderUnknown.class);
        list.add(ChatRoomMsgViewHolderText.class);
        list.add(ChatRoomMsgViewHolderPicture.class);

        return list;
    }
}
