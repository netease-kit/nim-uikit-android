package com.netease.nim.uikit.api.wrapper;

import android.text.TextUtils;

import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.business.team.helper.TeamHelper;
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.robot.model.RobotAttachment;
import com.netease.nimlib.sdk.team.constant.TeamMemberType;
import com.netease.nimlib.sdk.team.model.TeamMember;

/**
 * 消息撤回通知文案
 */

public class MessageRevokeTip {

    public static String getRevokeTipContent(IMMessage item, String revokeAccount) {

        String fromAccount = item.getFromAccount();
        if (item.getMsgType() == MsgTypeEnum.robot) {
            RobotAttachment robotAttachment = (RobotAttachment) item.getAttachment();
            if (robotAttachment.isRobotSend()) {
                fromAccount = robotAttachment.getFromRobotAccount();
            }
        }

        if (!TextUtils.isEmpty(
                revokeAccount) && !revokeAccount.equals(fromAccount)) {
            return getRevokeTipOfOther(item.getSessionId(), item.getSessionType(), revokeAccount);
        } else {
            String revokeNick = ""; // 撤回者
            if (item.getSessionType() == SessionTypeEnum.Team) {
                revokeNick = TeamHelper.getTeamMemberDisplayNameYou(item.getSessionId(), item.getFromAccount());
            } else if (item.getSessionType() == SessionTypeEnum.P2P) {
                revokeNick = item.getFromAccount().equals(NimUIKit.getAccount()) ? "你" : "对方";
            }
            return revokeNick + "撤回了一条消息";
        }
    }

    // 撤回其他人的消息时，获取tip
    public static String getRevokeTipOfOther(String sessionID, SessionTypeEnum sessionType, String revokeAccount) {
        if (sessionType == SessionTypeEnum.Team) {
            String revokeNick = ""; // 撤回者

            if (NimUIKit.getAccount().equals(revokeAccount)) {
                revokeNick = "你";
            } else {
                TeamMember member = NimUIKit.getTeamProvider().getTeamMember(sessionID, revokeAccount);

                String revoker = TeamHelper.getDisplayNameWithoutMe(sessionID, revokeAccount);

                if (member == null || member.getType() == TeamMemberType.Manager) {
                    revokeNick = "管理员 " + revoker + " ";
                } else if (member.getType() == TeamMemberType.Owner) {
                    revokeNick = "群主 " + revoker + " ";
                }
            }
            return revokeNick + "撤回了一条成员消息";
        } else {
            return "撤回了一条消息";
        }
    }
}
