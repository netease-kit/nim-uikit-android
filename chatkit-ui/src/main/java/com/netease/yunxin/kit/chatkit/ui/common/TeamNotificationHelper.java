// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.common;

import android.text.TextUtils;
import com.netease.nimlib.sdk.v2.message.attachment.V2NIMMessageNotificationAttachment;
import com.netease.nimlib.sdk.v2.team.enums.V2NIMTeamAgreeMode;
import com.netease.nimlib.sdk.v2.team.enums.V2NIMTeamChatBannedMode;
import com.netease.nimlib.sdk.v2.team.enums.V2NIMTeamInviteMode;
import com.netease.nimlib.sdk.v2.team.enums.V2NIMTeamJoinMode;
import com.netease.nimlib.sdk.v2.team.enums.V2NIMTeamUpdateInfoMode;
import com.netease.nimlib.sdk.v2.team.model.V2NIMTeam;
import com.netease.nimlib.sdk.v2.team.model.V2NIMUpdatedTeamInfo;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.model.IMMessageInfo;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.corekit.im2.IMKitClient;
import com.netease.yunxin.kit.corekit.im2.utils.IMKitUtils;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;

/** 群通知消息文案构造类，将通知转换为消息展示文案 */
public class TeamNotificationHelper {
  // 群@功能设置权限，在扩展字段的保存@功能权限设置信息，该字段作为信息KEY值
  public static final String KEY_EXTENSION_AT_ALL = "yxAllowAt";
  // 群@功能设置权限，所有人
  public static final String TYPE_EXTENSION_VALUE_ALL = "all";
  // 群@功能设置权限，管理员
  public static final String TYPE_EXTENSION_VALUE_MANAGER = "manager";

  public static String getTeamNotificationText(IMMessageInfo message) {
    return buildNotification(
        message.getMessage().getSenderId(),
        (V2NIMMessageNotificationAttachment) message.getMessage().getAttachment());
  }

  private static String buildNotification(
      String fromAccount, V2NIMMessageNotificationAttachment attachment) {
    String text;
    switch (attachment.getType()) {
      case V2NIM_MESSAGE_NOTIFICATION_TYPE_TEAM_INVITE:
        text = buildInviteMemberNotification(attachment, fromAccount);
        break;
      case V2NIM_MESSAGE_NOTIFICATION_TYPE_TEAM_KICK:
        text = buildKickMemberNotification(attachment);
        break;
      case V2NIM_MESSAGE_NOTIFICATION_TYPE_TEAM_LAVE:
        text = buildLeaveTeamNotification(fromAccount);
        break;
      case V2NIM_MESSAGE_NOTIFICATION_TYPE_TEAM_DISMISS:
        text = buildDismissTeamNotification(fromAccount);
        break;
      case V2NIM_MESSAGE_NOTIFICATION_TYPE_TEAM_UPDATE_TINFO:
        text = buildUpdateTeamNotification(fromAccount, attachment);
        break;
      case V2NIM_MESSAGE_NOTIFICATION_TYPE_TEAM_APPLY_PASS:
        text = buildManagerPassTeamApplyNotification(attachment);
        break;
      case V2NIM_MESSAGE_NOTIFICATION_TYPE_TEAM_OWNER_TRANSFER:
        text = buildTransferOwnerNotification(fromAccount, attachment);
        break;
      case V2NIM_MESSAGE_NOTIFICATION_TYPE_TEAM_ADD_MANAGER:
        text = buildAddTeamManagerNotification(attachment);
        break;
      case V2NIM_MESSAGE_NOTIFICATION_TYPE_TEAM_REMOVE_MANAGER:
        text = buildRemoveTeamManagerNotification(attachment);
        break;
      case V2NIM_MESSAGE_NOTIFICATION_TYPE_TEAM_INVITE_ACCEPT:
        text = buildAcceptInviteNotification(fromAccount, attachment);
        break;
      case V2NIM_MESSAGE_NOTIFICATION_TYPE_TEAM_BANNED_TEAM_MEMBER:
        text = buildMuteTeamNotification(attachment);
        break;
      default:
        text = getTeamMemberDisplayName(fromAccount) + ": unknown message";
        break;
    }

    return text;
  }

  private static String getTeamMemberDisplayName(String account) {
    return MessageHelper.getTeamNotifyDisplayName(account);
  }

  private static String buildMemberListString(List<String> members, String fromAccount) {
    StringBuilder sb = new StringBuilder();
    for (String account : members) {
      if (TextUtils.equals(account, fromAccount)) {
        continue;
      }
      sb.append(getTeamMemberDisplayName(account));
      sb.append(",");
    }
    sb.deleteCharAt(sb.length() - 1);

    return sb.toString();
  }

  private static String buildInviteMemberNotification(
      V2NIMMessageNotificationAttachment a, String fromAccount) {
    StringBuilder sb = new StringBuilder();
    String selfName = getTeamMemberDisplayName(fromAccount);

    sb.append(selfName);
    sb.append(IMKitClient.getApplicationContext().getString(R.string.chat_invite));
    sb.append(buildMemberListString(a.getTargetIds(), fromAccount));
    V2NIMTeam team = getTeam();
    if (team != null && !IMKitUtils.isTeamGroup(team)) {
      sb.append(IMKitClient.getApplicationContext().getString(R.string.chat_join_team));
    } else {
      sb.append(IMKitClient.getApplicationContext().getString(R.string.chat_join_discuss_team));
    }

    return sb.toString();
  }

  private static String buildKickMemberNotification(V2NIMMessageNotificationAttachment a) {
    StringBuilder sb = new StringBuilder();
    sb.append(buildMemberListString(a.getTargetIds(), null));
    V2NIMTeam team = getTeam();
    if (team != null && !IMKitUtils.isTeamGroup(team)) {
      sb.append(IMKitClient.getApplicationContext().getString(R.string.chat_removed_team));
    } else {
      sb.append(IMKitClient.getApplicationContext().getString(R.string.chat_removed_discuss_team));
    }

    return sb.toString();
  }

  private static String buildLeaveTeamNotification(String fromAccount) {
    String tip;
    V2NIMTeam team = getTeam();
    if (team != null && !IMKitUtils.isTeamGroup(team)) {
      tip = IMKitClient.getApplicationContext().getString(R.string.chat_left_team);
    } else {
      tip = IMKitClient.getApplicationContext().getString(R.string.chat_left_discuss_team);
    }
    return getTeamMemberDisplayName(fromAccount) + tip;
  }

  private static V2NIMTeam getTeam() {
    V2NIMTeam team = ChatUserCache.getInstance().getCurrentTeam();
    // todo 不存在的情况
    return team;
  }

  private static String buildDismissTeamNotification(String fromAccount) {
    return getTeamMemberDisplayName(fromAccount)
        + IMKitClient.getApplicationContext().getString(R.string.chat_dismiss_team);
  }

  private static String buildUpdateTeamNotification(
      String fromAccount, V2NIMMessageNotificationAttachment attachment) {
    StringBuilder sb = new StringBuilder();
    StringBuilder subStr = new StringBuilder();
    V2NIMUpdatedTeamInfo field = attachment.getUpdatedTeamInfo();
    subStr.append(getTeamMemberDisplayName(fromAccount)).append(" ");
    if (field.getName() != null) {
      subStr.append(
          String.format(
              IMKitClient.getApplicationContext().getString(R.string.chat_name_update),
              field.getName()));
    } else if (field.getIntro() != null) {
      subStr.append(
          IMKitClient.getApplicationContext().getString(R.string.chat_team_introduce_update));
    } else if (field.getAnnouncement() != null) {
      subStr
          .append(MessageHelper.getTeamNotifyDisplayName(fromAccount))
          .append(IMKitClient.getApplicationContext().getString(R.string.chat_team_notice_update));
    } else if (field.getJoinMode() != null) {
      V2NIMTeamJoinMode type = field.getJoinMode();
      String auth = IMKitClient.getApplicationContext().getString(R.string.chat_team_verify_update);
      if (type == V2NIMTeamJoinMode.V2NIM_TEAM_JOIN_MODE_FREE) {
        subStr
            .append(auth)
            .append(
                IMKitClient.getApplicationContext()
                    .getString(R.string.chat_team_allow_anyone_join));
      } else if (type == V2NIMTeamJoinMode.V2NIM_TEAM_JOIN_MODE_APPLY) {
        subStr
            .append(auth)
            .append(
                IMKitClient.getApplicationContext()
                    .getString(R.string.chat_team_need_authentication));
      } else {
        subStr
            .append(auth)
            .append(
                IMKitClient.getApplicationContext()
                    .getString(R.string.chat_team_not_allow_anyone_join));
      }
    } else if (field.getAvatar() != null) {
      subStr.append(
          IMKitClient.getApplicationContext().getString(R.string.chat_team_avatar_update));
    } else if (field.getInviteMode() != null) {
      subStr.append(
          IMKitClient.getApplicationContext()
              .getString(R.string.chat_team_invitation_permission_update));
      V2NIMTeamInviteMode inviteModeEnum = field.getInviteMode();
      if (inviteModeEnum == V2NIMTeamInviteMode.V2NIM_TEAM_INVITE_MODE_ALL) {
        subStr.append(
            IMKitClient.getApplicationContext()
                .getString(R.string.chat_team_invitation_permission_all));
      } else {
        subStr.append(
            IMKitClient.getApplicationContext()
                .getString(R.string.chat_team_invitation_permission_manager));
      }
    } else if (field.getUpdateInfoMode() != null) {
      subStr.append(
          IMKitClient.getApplicationContext()
              .getString(R.string.chat_team_modify_resource_permission_update));
      V2NIMTeamUpdateInfoMode updateModeEnum = field.getUpdateInfoMode();
      if (updateModeEnum == V2NIMTeamUpdateInfoMode.V2NIM_TEAM_UPDATE_INFO_MODE_ALL) {
        subStr.append(
            IMKitClient.getApplicationContext()
                .getString(R.string.chat_team_modify_permission_all));
      } else {
        subStr.append(
            IMKitClient.getApplicationContext()
                .getString(R.string.chat_team_modify_permission_manager));
      }
    } else if (field.getAgreeMode() != null) {
      subStr.append(
          IMKitClient.getApplicationContext()
              .getString(R.string.chat_team_invited_id_verify_permission_update));
      subStr.append(
          IMKitClient.getApplicationContext()
              .getString(R.string.chat_team_invited_id_verify_permission_update));
      V2NIMTeamAgreeMode inviteModeEnum = field.getAgreeMode();
      if (inviteModeEnum == V2NIMTeamAgreeMode.V2NIM_TEAM_AGREE_MODE_AUTH) {
        subStr.append(
            IMKitClient.getApplicationContext()
                .getString(R.string.chat_team_invited_permission_need));
      } else {
        subStr.append(
            IMKitClient.getApplicationContext()
                .getString(R.string.chat_team_invited_permission_no));
      }
    } else if (field.getServerExtension() != null) {
      subStr.delete(0, subStr.length());
      String mode = getTeamAtMode(field.getServerExtension());
      if (mode.equals(TYPE_EXTENSION_VALUE_ALL)) {
        subStr.append(
            IMKitClient.getApplicationContext()
                .getString(R.string.chat_team_at_permission_all_tips));
      } else {
        subStr.append(
            IMKitClient.getApplicationContext()
                .getString(R.string.chat_team_at_permission_manager_tips));
      }

    } else if (field.getChatBannedMode() != null) {
      subStr.delete(0, subStr.length());
      if (field.getChatBannedMode() == V2NIMTeamChatBannedMode.V2NIM_TEAM_CHAT_BANNED_MODE_UNBAN) {
        subStr.append(
            IMKitClient.getApplicationContext().getString(R.string.chat_team_cancel_all_mute));
      } else {
        subStr.append(IMKitClient.getApplicationContext().getString(R.string.chat_team_full_mute));
      }
    }
    sb.append(subStr);
    sb.append("\r\n");
    if (sb.length() < 2) {
      return "";
    }
    if (sb.length() < 2) {
      return IMKitClient.getApplicationContext().getString(R.string.chat_team_unknown_notification);
    }
    return sb.delete(sb.length() - 2, sb.length()).toString();
  }

  // 获取群@权限功能权限信息
  public static String getTeamAtMode(String extension) {
    String result = TYPE_EXTENSION_VALUE_ALL;
    if (extension == null) {
      return result;
    }
    if (extension.contains(KEY_EXTENSION_AT_ALL)) {
      try {
        JSONObject obj = new JSONObject(extension);
        result = obj.optString(KEY_EXTENSION_AT_ALL, TYPE_EXTENSION_VALUE_ALL);
      } catch (JSONException e) {
        ALog.e("TeamNotificationHelper", "getTeamNotifyAllMode", e);
      }
    }
    return result;
  }

  private static String buildManagerPassTeamApplyNotification(
      V2NIMMessageNotificationAttachment a) {

    return String.format(
        IMKitClient.getApplicationContext()
            .getString(R.string.chat_team_manager_pass_ones_application),
        buildMemberListString(a.getTargetIds(), null));
  }

  private static String buildTransferOwnerNotification(
      String fromAccount, V2NIMMessageNotificationAttachment a) {

    return getTeamMemberDisplayName(fromAccount)
        + IMKitClient.getApplicationContext().getString(R.string.chat_team_remove_to_another)
        + buildMemberListString(a.getTargetIds(), null);
  }

  private static String buildAddTeamManagerNotification(V2NIMMessageNotificationAttachment a) {

    return String.format(
        IMKitClient.getApplicationContext().getString(R.string.chat_team_appoint_manager),
        buildMemberListString(a.getTargetIds(), null));
  }

  private static String buildRemoveTeamManagerNotification(V2NIMMessageNotificationAttachment a) {

    return String.format(
        IMKitClient.getApplicationContext().getString(R.string.chat_team_removed_manager),
        buildMemberListString(a.getTargetIds(), null));
  }

  private static String buildAcceptInviteNotification(
      String fromAccount, V2NIMMessageNotificationAttachment a) {

    return getTeamMemberDisplayName(fromAccount)
        + String.format(
            IMKitClient.getApplicationContext().getString(R.string.chat_team_accept_ones_invent),
            buildMemberListString(a.getTargetIds(), null));
  }

  private static String buildMuteTeamNotification(V2NIMMessageNotificationAttachment a) {

    return buildMemberListString(a.getTargetIds(), null)
        + IMKitClient.getApplicationContext().getString(R.string.chat_team_operate_by_manager)
        + (a.isChatBanned()
            ? IMKitClient.getApplicationContext().getString(R.string.chat_team_mute)
            : IMKitClient.getApplicationContext().getString(R.string.chat_team_un_mute));
  }
}
