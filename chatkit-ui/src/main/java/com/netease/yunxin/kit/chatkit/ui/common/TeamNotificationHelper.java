/*
 * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.kit.chatkit.ui.common;

import android.text.TextUtils;

import com.netease.nimlib.sdk.msg.attachment.NotificationAttachment;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.team.constant.TeamAllMuteModeEnum;
import com.netease.nimlib.sdk.team.constant.TeamFieldEnum;
import com.netease.nimlib.sdk.team.constant.TeamTypeEnum;
import com.netease.nimlib.sdk.team.constant.VerifyTypeEnum;
import com.netease.nimlib.sdk.team.model.MemberChangeAttachment;
import com.netease.nimlib.sdk.team.model.MuteMemberAttachment;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.nimlib.sdk.team.model.UpdateTeamAttachment;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.corekit.im.XKitImClient;
import com.netease.yunxin.kit.corekit.im.provider.TeamProvider;

import java.util.List;
import java.util.Map;

public class TeamNotificationHelper {
    private static final ThreadLocal<String> teamId = new ThreadLocal<>();

    public static String getMsgShowText(final IMMessage message) {
        String content = "";
        String messageTip = message.getMsgType().getSendMessageTip();
        if (messageTip.length() > 0) {
            content += "[" + messageTip + "]";
        } else {
            if (message.getSessionType() == SessionTypeEnum.Team && message.getAttachment() != null) {
                content += getTeamNotificationText(message);
            } else {
                content += message.getContent();
            }
        }

        return content;
    }

    public static String getTeamNotificationText(IMMessage message) {
        return getTeamNotificationText(message.getSessionId(), message.getFromAccount(), (NotificationAttachment) message.getAttachment());
    }

    public static String getTeamNotificationText(String tid, String fromAccount, NotificationAttachment attachment) {
        teamId.set(tid);
        String text = buildNotification(tid, fromAccount, attachment);
        teamId.set(null);
        return text;
    }

    private static String buildNotification(String tid, String fromAccount, NotificationAttachment attachment) {
        String text;
        switch (attachment.getType()) {
            case InviteMember:
            case SUPER_TEAM_INVITE:
                text = buildInviteMemberNotification(((MemberChangeAttachment) attachment), fromAccount);
                break;
            case KickMember:
            case SUPER_TEAM_KICK:
                text = buildKickMemberNotification(((MemberChangeAttachment) attachment));
                break;
            case LeaveTeam:
            case SUPER_TEAM_LEAVE:
                text = buildLeaveTeamNotification(fromAccount);
                break;
            case DismissTeam:
            case SUPER_TEAM_DISMISS:
                text = buildDismissTeamNotification(fromAccount);
                break;
            case UpdateTeam:
            case SUPER_TEAM_UPDATE_T_INFO:
                text = buildUpdateTeamNotification(tid, fromAccount, (UpdateTeamAttachment) attachment);
                break;
            case PassTeamApply:
            case SUPER_TEAM_APPLY_PASS:
                text = buildManagerPassTeamApplyNotification((MemberChangeAttachment) attachment);
                break;
            case TransferOwner:
            case SUPER_TEAM_CHANGE_OWNER:
                text = buildTransferOwnerNotification(fromAccount, (MemberChangeAttachment) attachment);
                break;
            case AddTeamManager:
            case SUPER_TEAM_ADD_MANAGER:
                text = buildAddTeamManagerNotification((MemberChangeAttachment) attachment);
                break;
            case RemoveTeamManager:
            case SUPER_TEAM_REMOVE_MANAGER:
                text = buildRemoveTeamManagerNotification((MemberChangeAttachment) attachment);
                break;
            case AcceptInvite:
            case SUPER_TEAM_INVITE_ACCEPT:
                text = buildAcceptInviteNotification(fromAccount, (MemberChangeAttachment) attachment);
                break;
            case MuteTeamMember:
            case SUPER_TEAM_MUTE_TLIST:
                text = buildMuteTeamNotification((MuteMemberAttachment) attachment);
                break;
            default:
                text = getTeamMemberDisplayName(fromAccount) + ": unknown message";
                break;
        }

        return text;
    }

    private static String getTeamMemberDisplayName(String account) {
        return MessageHelper.getTeamMemberDisplayNameYou(teamId.get(), account);
    }

    private static String buildMemberListString(List<String> members, String fromAccount) {
        StringBuilder sb = new StringBuilder();
        for (String account : members) {
            if (!TextUtils.isEmpty(fromAccount) && fromAccount.equals(account)) {
                continue;
            }
            sb.append(getTeamMemberDisplayName(account));
            sb.append(",");
        }
        sb.deleteCharAt(sb.length() - 1);

        return sb.toString();
    }

    private static String buildInviteMemberNotification(MemberChangeAttachment a, String fromAccount) {
        StringBuilder sb = new StringBuilder();
        String selfName = getTeamMemberDisplayName(fromAccount);

        sb.append(selfName);
        sb.append(XKitImClient.getApplicationContext().getString(R.string.chat_invite));
        sb.append(buildMemberListString(a.getTargets(), fromAccount));
        Team team = TeamProvider.INSTANCE.queryTeamBlock(teamId.get());
        if (team == null || team.getType() == TeamTypeEnum.Advanced) {
            sb.append(XKitImClient.getApplicationContext().getString(R.string.chat_join_team));
        } else {
            sb.append(XKitImClient.getApplicationContext().getString(R.string.chat_join_discuss_team));
        }

        return sb.toString();
    }

    private static String buildKickMemberNotification(MemberChangeAttachment a) {
        StringBuilder sb = new StringBuilder();
        sb.append(buildMemberListString(a.getTargets(), null));
        Team team = TeamProvider.INSTANCE.queryTeamBlock(teamId.get());
        if (team == null || team.getType() == TeamTypeEnum.Advanced) {
            sb.append(XKitImClient.getApplicationContext().getString(R.string.chat_removed_team));
        } else {
            sb.append(XKitImClient.getApplicationContext().getString(R.string.chat_removed_discuss_team));
        }

        return sb.toString();
    }

    private static String buildLeaveTeamNotification(String fromAccount) {
        String tip;
        Team team = TeamProvider.INSTANCE.queryTeamBlock(teamId.get());
        if (team == null || team.getType() == TeamTypeEnum.Advanced) {
            tip = XKitImClient.getApplicationContext().getString(R.string.chat_left_team);
        } else {
            tip = XKitImClient.getApplicationContext().getString(R.string.chat_left_discuss_team);
        }
        return getTeamMemberDisplayName(fromAccount) + tip;
    }

    private static String buildDismissTeamNotification(String fromAccount) {
        return getTeamMemberDisplayName(fromAccount) + XKitImClient.getApplicationContext().getString(R.string.chat_dismiss_team);
    }

    private static String buildUpdateTeamNotification(String tid, String account, UpdateTeamAttachment a) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<TeamFieldEnum, Object> field : a.getUpdatedFields().entrySet()) {
            if (field.getKey() == TeamFieldEnum.Name) {
                sb.append(XKitImClient.getApplicationContext().getString(R.string.chat_name_update)).append(field.getValue());
            } else if (field.getKey() == TeamFieldEnum.Introduce) {
                sb.append(XKitImClient.getApplicationContext().getString(R.string.chat_team_introduce_update)).append(field.getValue());
            } else if (field.getKey() == TeamFieldEnum.Announcement) {
                sb.append(MessageHelper.getTeamMemberDisplayNameYou(tid, account)).append(XKitImClient.getApplicationContext().getString(R.string.chat_team_notice_update));
            } else if (field.getKey() == TeamFieldEnum.VerifyType) {
                VerifyTypeEnum type = (VerifyTypeEnum) field.getValue();
                String auth = XKitImClient.getApplicationContext().getString(R.string.chat_team_verify_update);
                if (type == VerifyTypeEnum.Free) {
                    sb.append(auth).append(XKitImClient.getApplicationContext().getString(R.string.chat_team_allow_anyone_join));
                } else if (type == VerifyTypeEnum.Apply) {
                    sb.append(auth).append(XKitImClient.getApplicationContext().getString(R.string.chat_team_need_authentication));
                } else {
                    sb.append(auth).append(XKitImClient.getApplicationContext().getString(R.string.chat_team_not_allow_anyone_join));
                }
            } else if (field.getKey() == TeamFieldEnum.Extension) {
                sb.append(XKitImClient.getApplicationContext().getString(R.string.chat_team_extension_update)).append(field.getValue());
            } else if (field.getKey() == TeamFieldEnum.Ext_Server_Only) {
                sb.append(XKitImClient.getApplicationContext().getString(R.string.chat_team_extension_server_update)).append(field.getValue());
            } else if (field.getKey() == TeamFieldEnum.ICON) {
                sb.append(XKitImClient.getApplicationContext().getString(R.string.chat_team_avatar_update));
            } else if (field.getKey() == TeamFieldEnum.InviteMode) {
                sb.append(XKitImClient.getApplicationContext().getString(R.string.chat_team_invitation_permission_update)).append(field.getValue());
            } else if (field.getKey() == TeamFieldEnum.TeamUpdateMode) {
                sb.append(XKitImClient.getApplicationContext().getString(R.string.chat_team_modify_resource_permission_update)).append(field.getValue());
            } else if (field.getKey() == TeamFieldEnum.BeInviteMode) {
                sb.append(XKitImClient.getApplicationContext().getString(R.string.chat_team_invited_id_verify_permission_update)).append(field.getValue());
            } else if (field.getKey() == TeamFieldEnum.TeamExtensionUpdateMode) {
                sb.append(XKitImClient.getApplicationContext().getString(R.string.chat_team_modify_extension_permission_update)).append(field.getValue());
            } else if (field.getKey() == TeamFieldEnum.AllMute) {
                TeamAllMuteModeEnum teamAllMuteModeEnum = (TeamAllMuteModeEnum) field.getValue();
                if (teamAllMuteModeEnum == TeamAllMuteModeEnum.Cancel) {
                    sb.append(XKitImClient.getApplicationContext().getString(R.string.chat_team_cancel_all_mute));
                } else {
                    sb.append(XKitImClient.getApplicationContext().getString(R.string.chat_team_full_mute));
                }
            } else {
                sb.append(String.format(
                        XKitImClient.getApplicationContext().getString(R.string.chat_team_update),
                        field.getKey(), field.getValue()));
            }
            sb.append("\r\n");
        }
        if (sb.length() < 2) {
            return XKitImClient.getApplicationContext().getString(R.string.chat_team_unknown_notification);
        }
        return sb.delete(sb.length() - 2, sb.length()).toString();
    }

    private static String buildManagerPassTeamApplyNotification(MemberChangeAttachment a) {

        return String.format(XKitImClient.getApplicationContext().getString(R.string.chat_team_manager_pass_ones_application),
                buildMemberListString(a.getTargets(), null));

    }

    private static String buildTransferOwnerNotification(String from, MemberChangeAttachment a) {

        return getTeamMemberDisplayName(from) +
                XKitImClient.getApplicationContext().getString(R.string.chat_team_remove_to_another) +
                buildMemberListString(a.getTargets(), null);
    }

    private static String buildAddTeamManagerNotification(MemberChangeAttachment a) {

        return String.format(XKitImClient.getApplicationContext().getString(R.string.chat_team_appoint_manager),
                buildMemberListString(a.getTargets(), null));

    }

    private static String buildRemoveTeamManagerNotification(MemberChangeAttachment a) {

        return String.format(XKitImClient.getApplicationContext().getString(R.string.chat_team_removed_manager),
                buildMemberListString(a.getTargets(), null));
    }

    private static String buildAcceptInviteNotification(String from, MemberChangeAttachment a) {

        return getTeamMemberDisplayName(from)
                + String.format(XKitImClient.getApplicationContext().getString(R.string.chat_team_accept_ones_invent),
                buildMemberListString(a.getTargets(), null));
    }

    private static String buildMuteTeamNotification(MuteMemberAttachment a) {

        return buildMemberListString(a.getTargets(), null) +
                XKitImClient.getApplicationContext().getString(R.string.chat_team_operate_by_manager) +
                (a.isMute() ? XKitImClient.getApplicationContext().getString(R.string.chat_team_mute)
                        : XKitImClient.getApplicationContext().getString(R.string.chat_team_un_mute));
    }
}
