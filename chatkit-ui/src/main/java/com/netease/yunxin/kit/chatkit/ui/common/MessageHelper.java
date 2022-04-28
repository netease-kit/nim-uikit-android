package com.netease.yunxin.kit.chatkit.ui.common;

import android.text.TextUtils;

import com.netease.nimlib.sdk.msg.attachment.MsgAttachment;
import com.netease.nimlib.sdk.msg.attachment.NotificationAttachment;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.team.constant.TeamTypeEnum;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.nimlib.sdk.team.model.TeamMember;
import com.netease.yunxin.kit.chatkit.model.IMMessageInfo;
import com.netease.yunxin.kit.chatkit.repo.ChatMessageRepo;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.corekit.im.XKitImClient;
import com.netease.yunxin.kit.corekit.im.model.FriendInfo;
import com.netease.yunxin.kit.corekit.im.model.UserInfo;
import com.netease.yunxin.kit.corekit.im.provider.FriendProvider;
import com.netease.yunxin.kit.corekit.im.provider.TeamProvider;
import com.netease.yunxin.kit.corekit.im.provider.UserInfoProvider;

import java.util.ArrayList;
import java.util.List;


public class MessageHelper {


    /**
     * get nickName display
     *
     * @param tid     team id
     * @param account user accId
     */
    public static String getTeamMemberDisplayNameYou(String tid, String account) {
        if (account.equals(XKitImClient.account())) {
            return XKitImClient.getApplicationContext().getString(R.string.chat_you);
        }

        return getTeamDisplayNameWithoutMe(tid, account);
    }

    /**
     * get Nick show in team
     * Allis>team comment>nickname
     */
    public static String getTeamDisplayNameWithoutMe(String tid, String account) {

        String memberNick = getTeamNick(tid, account);
        if (!TextUtils.isEmpty(memberNick)) {
            return memberNick;
        }

        return getUserNickByAccId(account, false);
    }


    public static String getTeamNick(String tid, String account) {
        Team team = TeamProvider.INSTANCE.queryTeamBlock(tid);
        if (team != null && team.getType() == TeamTypeEnum.Advanced) {
            TeamMember member = TeamProvider.INSTANCE.getTeamMember(tid, account);
            if (member != null && !TextUtils.isEmpty(member.getTeamNick())) {
                return member.getTeamNick();
            }
        }
        return null;
    }

    public static String getTeamUserAvatar(String accId) {
        UserInfo user = UserInfoProvider.INSTANCE.getUserInfo(accId);
        return user == null ? null : user.getAvatar();
    }

    public static String getUserNickByAccId(String accId, boolean withYou) {
        if (withYou && accId.equals(XKitImClient.account())) {
            return XKitImClient.getApplicationContext().getString(R.string.chat_you);
        }
        FriendInfo friend = FriendProvider.INSTANCE.getFriendInfo(accId);
        if (friend != null && !TextUtils.isEmpty(friend.getAlias())) {
            return friend.getAlias();
        }
        UserInfo user = UserInfoProvider.INSTANCE.getUserInfo(accId);
        return user == null ? null : (TextUtils.isEmpty(user.getName()) ? accId : user.getName());
    }

    public static String getChatMessageUserName(IMMessage message) {
        String name = null;
        //first is friend alias
        FriendInfo friendInfo = ChatMessageRepo.getFriendInfo(message.getFromAccount());
        if (friendInfo != null && !TextUtils.isEmpty(friendInfo.getAlias())) {
            name = friendInfo.getAlias();
        }
        if (!TextUtils.isEmpty(name)) {
            return name;
        }
        //second is team alias
        if (message.getSessionType() == SessionTypeEnum.Team) {
            name = getTeamNick(message.getSessionId(), message.getFromAccount());
            if (!TextUtils.isEmpty(name)) {
                return name;
            }
        }
        //third is user nick
        UserInfo user = UserInfoProvider.INSTANCE.getUserInfo(message.getFromAccount());
        if (user != null) {
            name = user.getName();
        }
        if (TextUtils.isEmpty(name)) {
            //last is accId
            name = message.getFromAccount();
        }
        return name;
    }

    public static String getReplyMessageInfo(String uuid) {
        if (TextUtils.isEmpty(uuid)) {
            return "...";
        }
        List<String> uuidList = new ArrayList<>(1);
        uuidList.add(uuid);
        List<IMMessageInfo> msgList = ChatMessageRepo.queryMessageListByUuidBlock(uuidList);
        if (msgList == null || msgList.isEmpty()) {
            return "...";
        }
        IMMessageInfo msg = msgList.get(0);
        String nickName = getChatMessageUserName(msg.getMessage());
        String content = getReplyMsgBrief(msg);
        return nickName + ": " +
                content;
    }

    public static String getReplyMsgBrief(IMMessageInfo replyMsg) {
        IMMessage msg = replyMsg.getMessage();
        switch (msg.getMsgType()) {
            case avchat:
                MsgAttachment attachment = msg.getAttachment();
                //todo avchat
                return "";
            case text:
            case tip:
                return msg.getContent();
            case image:
                return XKitImClient.getApplicationContext().getString(R.string.chat_reply_message_brief_image);
            case video:
                return XKitImClient.getApplicationContext().getString(R.string.chat_reply_message_brief_video);
            case audio:
                return XKitImClient.getApplicationContext().getString(R.string.chat_reply_message_brief_audio);
            case location:
                return XKitImClient.getApplicationContext().getString(R.string.chat_reply_message_brief_location);
            case file:
                return XKitImClient.getApplicationContext().getString(R.string.chat_reply_message_brief_file);
            case notification:
                return TeamNotificationHelper.getTeamNotificationText(msg.getSessionId(),
                        msg.getFromAccount(),
                        (NotificationAttachment) msg.getAttachment());
            case robot:
                return XKitImClient.getApplicationContext().getString(R.string.chat_reply_message_brief_robot);
            default:
                return XKitImClient.getApplicationContext().getString(R.string.chat_reply_message_brief_custom);
        }
    }
}
