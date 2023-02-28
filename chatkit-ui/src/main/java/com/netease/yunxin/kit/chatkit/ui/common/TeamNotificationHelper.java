// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.common;

import android.text.TextUtils;
import com.netease.nimlib.sdk.msg.attachment.NotificationAttachment;
import com.netease.nimlib.sdk.team.constant.TeamAllMuteModeEnum;
import com.netease.nimlib.sdk.team.constant.TeamBeInviteModeEnum;
import com.netease.nimlib.sdk.team.constant.TeamFieldEnum;
import com.netease.nimlib.sdk.team.constant.TeamInviteModeEnum;
import com.netease.nimlib.sdk.team.constant.TeamUpdateModeEnum;
import com.netease.nimlib.sdk.team.constant.VerifyTypeEnum;
import com.netease.nimlib.sdk.team.model.MemberChangeAttachment;
import com.netease.nimlib.sdk.team.model.MuteMemberAttachment;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.nimlib.sdk.team.model.UpdateTeamAttachment;
import com.netease.yunxin.kit.chatkit.model.IMMessageInfo;
import com.netease.yunxin.kit.chatkit.repo.ChatRepo;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.corekit.im.IMKitClient;
import com.netease.yunxin.kit.corekit.im.model.UserInfo;
import com.netease.yunxin.kit.corekit.im.utils.IMKitUtils;
import java.util.List;
import java.util.Map;

public class TeamNotificationHelper {

  public static String getTeamNotificationText(IMMessageInfo message) {
    return buildNotification(
        message.getMessage().getSessionId(),
        message.getFromUser(),
        (NotificationAttachment) message.getMessage().getAttachment());
  }

  private static String buildNotification(
      String tid, UserInfo fromUser, NotificationAttachment attachment) {
    String text;
    switch (attachment.getType()) {
      case InviteMember:
      case SUPER_TEAM_INVITE:
        text = buildInviteMemberNotification(tid, ((MemberChangeAttachment) attachment), fromUser);
        break;
      case KickMember:
      case SUPER_TEAM_KICK:
        text = buildKickMemberNotification(tid, ((MemberChangeAttachment) attachment));
        break;
      case LeaveTeam:
      case SUPER_TEAM_LEAVE:
        text = buildLeaveTeamNotification(tid, fromUser);
        break;
      case DismissTeam:
      case SUPER_TEAM_DISMISS:
        text = buildDismissTeamNotification(tid, fromUser);
        break;
      case UpdateTeam:
      case SUPER_TEAM_UPDATE_T_INFO:
        text = buildUpdateTeamNotification(tid, fromUser, (UpdateTeamAttachment) attachment);
        break;
      case PassTeamApply:
      case SUPER_TEAM_APPLY_PASS:
        text = buildManagerPassTeamApplyNotification(tid, (MemberChangeAttachment) attachment);
        break;
      case TransferOwner:
      case SUPER_TEAM_CHANGE_OWNER:
        text = buildTransferOwnerNotification(tid, fromUser, (MemberChangeAttachment) attachment);
        break;
      case AddTeamManager:
      case SUPER_TEAM_ADD_MANAGER:
        text = buildAddTeamManagerNotification(tid, (MemberChangeAttachment) attachment);
        break;
      case RemoveTeamManager:
      case SUPER_TEAM_REMOVE_MANAGER:
        text = buildRemoveTeamManagerNotification(tid, (MemberChangeAttachment) attachment);
        break;
      case AcceptInvite:
      case SUPER_TEAM_INVITE_ACCEPT:
        text = buildAcceptInviteNotification(tid, fromUser, (MemberChangeAttachment) attachment);
        break;
      case MuteTeamMember:
      case SUPER_TEAM_MUTE_TLIST:
        text = buildMuteTeamNotification(tid, (MuteMemberAttachment) attachment);
        break;
      default:
        text = getTeamMemberDisplayName(tid, fromUser) + ": unknown message";
        break;
    }

    return text;
  }

  private static String getTeamMemberDisplayName(String tid, String account) {
    return MessageHelper.getTeamMemberDisplayNameYou(tid, account);
  }

  private static String getTeamMemberDisplayName(String tid, UserInfo userInfo) {
    return MessageHelper.getTeamMemberDisplayName(tid, userInfo);
  }

  private static String buildMemberListString(String tid, List<String> members, UserInfo fromUser) {
    StringBuilder sb = new StringBuilder();
    for (String account : members) {
      if (fromUser != null && TextUtils.equals(account, fromUser.getAccount())) {
        continue;
      }
      sb.append(getTeamMemberDisplayName(tid, account));
      sb.append(",");
    }
    sb.deleteCharAt(sb.length() - 1);

    return sb.toString();
  }

  private static String buildInviteMemberNotification(
      String tid, MemberChangeAttachment a, UserInfo fromUser) {
    StringBuilder sb = new StringBuilder();
    String selfName = getTeamMemberDisplayName(tid, fromUser);

    sb.append(selfName);
    sb.append(IMKitClient.getApplicationContext().getString(R.string.chat_invite));
    sb.append(buildMemberListString(tid, a.getTargets(), fromUser));
    Team team = getTeam(tid);
    if (team != null && !IMKitUtils.isTeamGroup(team)) {
      sb.append(IMKitClient.getApplicationContext().getString(R.string.chat_join_team));
    } else {
      sb.append(IMKitClient.getApplicationContext().getString(R.string.chat_join_discuss_team));
    }

    return sb.toString();
  }

  private static String buildKickMemberNotification(String tid, MemberChangeAttachment a) {
    StringBuilder sb = new StringBuilder();
    sb.append(buildMemberListString(tid, a.getTargets(), null));
    Team team = getTeam(tid);
    if (team != null && !IMKitUtils.isTeamGroup(team)) {
      sb.append(IMKitClient.getApplicationContext().getString(R.string.chat_removed_team));
    } else {
      sb.append(IMKitClient.getApplicationContext().getString(R.string.chat_removed_discuss_team));
    }

    return sb.toString();
  }

  private static String buildLeaveTeamNotification(String tid, UserInfo fromUser) {
    String tip;
    Team team = getTeam(tid);
    if (team != null && !IMKitUtils.isTeamGroup(team)) {
      tip = IMKitClient.getApplicationContext().getString(R.string.chat_left_team);
    } else {
      tip = IMKitClient.getApplicationContext().getString(R.string.chat_left_discuss_team);
    }
    return getTeamMemberDisplayName(tid, fromUser) + tip;
  }

  private static Team getTeam(String teamId) {
    Team team = ChatRepo.getCurrentTeam();
    if (team == null || !TextUtils.equals(teamId, team.getId())) {
      team = ChatRepo.getTeamInfo(teamId);
    }
    return team;
  }

  private static String buildDismissTeamNotification(String tid, UserInfo fromUser) {
    return getTeamMemberDisplayName(tid, fromUser)
        + IMKitClient.getApplicationContext().getString(R.string.chat_dismiss_team);
  }

  private static String buildUpdateTeamNotification(
      String tid, UserInfo fromUser, UpdateTeamAttachment a) {
    StringBuilder sb = new StringBuilder();
    boolean showContent = true;
    for (Map.Entry<TeamFieldEnum, Object> field : a.getUpdatedFields().entrySet()) {
      if (field.getKey() == TeamFieldEnum.Name) {
        sb.append(IMKitClient.getApplicationContext().getString(R.string.chat_name_update))
            .append(field.getValue());
      } else if (field.getKey() == TeamFieldEnum.Introduce) {
        sb.append(
                IMKitClient.getApplicationContext().getString(R.string.chat_team_introduce_update))
            .append(field.getValue());
      } else if (field.getKey() == TeamFieldEnum.Announcement) {
        sb.append(MessageHelper.getTeamMemberDisplayName(tid, fromUser))
            .append(
                IMKitClient.getApplicationContext().getString(R.string.chat_team_notice_update));
      } else if (field.getKey() == TeamFieldEnum.VerifyType) {
        VerifyTypeEnum type = (VerifyTypeEnum) field.getValue();
        String auth =
            IMKitClient.getApplicationContext().getString(R.string.chat_team_verify_update);
        if (type == VerifyTypeEnum.Free) {
          sb.append(auth)
              .append(
                  IMKitClient.getApplicationContext()
                      .getString(R.string.chat_team_allow_anyone_join));
        } else if (type == VerifyTypeEnum.Apply) {
          sb.append(auth)
              .append(
                  IMKitClient.getApplicationContext()
                      .getString(R.string.chat_team_need_authentication));
        } else {
          sb.append(auth)
              .append(
                  IMKitClient.getApplicationContext()
                      .getString(R.string.chat_team_not_allow_anyone_join));
        }
      } else if (field.getKey() == TeamFieldEnum.ICON) {
        sb.append(IMKitClient.getApplicationContext().getString(R.string.chat_team_avatar_update));
      } else if (field.getKey() == TeamFieldEnum.InviteMode) {
        sb.append(
            IMKitClient.getApplicationContext()
                .getString(R.string.chat_team_invitation_permission_update));
        TeamInviteModeEnum inviteModeEnum = (TeamInviteModeEnum) field.getValue();
        if (inviteModeEnum == TeamInviteModeEnum.All) {
          sb.append(
              IMKitClient.getApplicationContext()
                  .getString(R.string.chat_team_invitation_permission_all));
        } else {
          sb.append(
              IMKitClient.getApplicationContext()
                  .getString(R.string.chat_team_invitation_permission_manager));
        }
      } else if (field.getKey() == TeamFieldEnum.TeamUpdateMode) {
        sb.append(
            IMKitClient.getApplicationContext()
                .getString(R.string.chat_team_modify_resource_permission_update));
        TeamUpdateModeEnum updateModeEnum = (TeamUpdateModeEnum) field.getValue();
        if (updateModeEnum == TeamUpdateModeEnum.All) {
          sb.append(
              IMKitClient.getApplicationContext()
                  .getString(R.string.chat_team_modify_permission_all));
        } else {
          sb.append(
              IMKitClient.getApplicationContext()
                  .getString(R.string.chat_team_modify_permission_manager));
        }
      } else if (field.getKey() == TeamFieldEnum.BeInviteMode) {
        sb.append(
            IMKitClient.getApplicationContext()
                .getString(R.string.chat_team_invited_id_verify_permission_update));
        sb.append(
            IMKitClient.getApplicationContext()
                .getString(R.string.chat_team_invited_id_verify_permission_update));
        TeamBeInviteModeEnum inviteModeEnum = (TeamBeInviteModeEnum) field.getValue();
        if (inviteModeEnum == TeamBeInviteModeEnum.NeedAuth) {
          sb.append(
              IMKitClient.getApplicationContext()
                  .getString(R.string.chat_team_invited_permission_need));
        } else {
          sb.append(
              IMKitClient.getApplicationContext()
                  .getString(R.string.chat_team_invited_permission_no));
        }
      } else if (field.getKey() == TeamFieldEnum.AllMute) {
        TeamAllMuteModeEnum teamAllMuteModeEnum = (TeamAllMuteModeEnum) field.getValue();
        if (teamAllMuteModeEnum == TeamAllMuteModeEnum.Cancel) {
          sb.append(
              IMKitClient.getApplicationContext().getString(R.string.chat_team_cancel_all_mute));
        } else {
          sb.append(IMKitClient.getApplicationContext().getString(R.string.chat_team_full_mute));
        }
      } else if (field.getKey() == TeamFieldEnum.Extension
          || field.getKey() == TeamFieldEnum.Ext_Server_Only
          || field.getKey() == TeamFieldEnum.TeamExtensionUpdateMode) {
        showContent = false;
        continue;
      } else {
        sb.append(
            String.format(
                IMKitClient.getApplicationContext().getString(R.string.chat_team_update),
                field.getKey(),
                field.getValue()));
      }
      sb.append("\r\n");
    }
    if (sb.length() < 2 && !showContent) {
      return "";
    }
    if (sb.length() < 2) {
      return IMKitClient.getApplicationContext().getString(R.string.chat_team_unknown_notification);
    }
    return sb.delete(sb.length() - 2, sb.length()).toString();
  }

  private static String buildManagerPassTeamApplyNotification(
      String tid, MemberChangeAttachment a) {

    return String.format(
        IMKitClient.getApplicationContext()
            .getString(R.string.chat_team_manager_pass_ones_application),
        buildMemberListString(tid, a.getTargets(), null));
  }

  private static String buildTransferOwnerNotification(
      String tid, UserInfo fromUser, MemberChangeAttachment a) {

    return getTeamMemberDisplayName(tid, fromUser)
        + IMKitClient.getApplicationContext().getString(R.string.chat_team_remove_to_another)
        + buildMemberListString(tid, a.getTargets(), null);
  }

  private static String buildAddTeamManagerNotification(String tid, MemberChangeAttachment a) {

    return String.format(
        IMKitClient.getApplicationContext().getString(R.string.chat_team_appoint_manager),
        buildMemberListString(tid, a.getTargets(), null));
  }

  private static String buildRemoveTeamManagerNotification(String tid, MemberChangeAttachment a) {

    return String.format(
        IMKitClient.getApplicationContext().getString(R.string.chat_team_removed_manager),
        buildMemberListString(tid, a.getTargets(), null));
  }

  private static String buildAcceptInviteNotification(
      String tid, UserInfo fromUser, MemberChangeAttachment a) {

    return getTeamMemberDisplayName(tid, fromUser)
        + String.format(
            IMKitClient.getApplicationContext().getString(R.string.chat_team_accept_ones_invent),
            buildMemberListString(tid, a.getTargets(), null));
  }

  private static String buildMuteTeamNotification(String tid, MuteMemberAttachment a) {

    return buildMemberListString(tid, a.getTargets(), null)
        + IMKitClient.getApplicationContext().getString(R.string.chat_team_operate_by_manager)
        + (a.isMute()
            ? IMKitClient.getApplicationContext().getString(R.string.chat_team_mute)
            : IMKitClient.getApplicationContext().getString(R.string.chat_team_un_mute));
  }
}
