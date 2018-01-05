package com.netease.nim.uikit.business.chatroom.helper;

import android.text.TextUtils;

import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomNotificationAttachment;

import java.util.List;

/**
 * Created by huangjun on 2016/1/13.
 */
public class ChatRoomNotificationHelper {
    public static String getNotificationText(ChatRoomNotificationAttachment attachment) {
        if (attachment == null) {
            return "";
        }

        String targets = getTargetNicks(attachment);
        String text;
        switch (attachment.getType()) {
            case ChatRoomMemberIn:
                text = buildText("欢迎", targets, "进入直播间");
                break;
            case ChatRoomMemberExit:
                text = buildText(targets, "离开了直播间");
                break;
            case ChatRoomMemberBlackAdd:
                text = buildText(targets, "被管理员拉入黑名单");
                break;
            case ChatRoomMemberBlackRemove:
                text = buildText(targets, "被管理员解除拉黑");
                break;
            case ChatRoomMemberMuteAdd:
                text = buildText(targets, "被管理员禁言");
                break;
            case ChatRoomMemberMuteRemove:
                text = buildText(targets, "被管理员解除禁言");
                break;
            case ChatRoomManagerAdd:
                text = buildText(targets, "被任命管理员身份");
                break;
            case ChatRoomManagerRemove:
                text = buildText(targets, "被解除管理员身份");
                break;
            case ChatRoomCommonAdd:
                text = buildText(targets, "被设为普通成员");
                break;
            case ChatRoomCommonRemove:
                text = buildText(targets, "被取消普通成员");
                break;
            case ChatRoomClose:
                text = buildText("直播间被关闭");
                break;
            case ChatRoomInfoUpdated:
                text = buildText("直播间信息已更新");
                break;
            case ChatRoomMemberKicked:
                text = buildText(targets, "被踢出直播间");
                break;
            case ChatRoomMemberTempMuteAdd:
                text = buildText(targets, "被临时禁言");
                break;
            case ChatRoomMemberTempMuteRemove:
                text = buildText(targets, "被解除临时禁言");
                break;
            case ChatRoomMyRoomRoleUpdated:
                text = buildText(targets, "更新了自己的角色信息");
                break;
            case ChatRoomQueueChange:
                text = buildText(targets, "麦序队列中有变更");
                break;
            case ChatRoomRoomMuted:
                text = buildText("全体禁言，管理员可发言");
                break;
            case ChatRoomRoomDeMuted:
                text = buildText("解除全体禁言");
                break;
            case ChatRoomQueueBatchChange:
                text = buildText("批量变更");
                break;
            default:
                text = attachment.toString();
                break;
        }

        return text;
    }

    private static String getTargetNicks(final ChatRoomNotificationAttachment attachment) {
        StringBuilder sb = new StringBuilder();
        List<String> accounts = attachment.getTargets();
        List<String> targets = attachment.getTargetNicks();
        if (attachment.getTargetNicks() != null) {
            for (int i = 0; i < targets.size(); i++) {
                sb.append(NimUIKit.getAccount().equals(accounts.get(i)) ? "你" : targets.get(i));
                sb.append(",");
            }
            sb.deleteCharAt(sb.length() - 1);
        }

        return sb.toString();
    }

    private static String buildText(String pre, String targets, String operate) {
        StringBuilder sb = new StringBuilder();
        if (!TextUtils.isEmpty(pre)) {
            sb.append(pre);
        }

        if (!TextUtils.isEmpty(targets)) {
            sb.append(targets);
        }

        if (!TextUtils.isEmpty(operate)) {
            sb.append(operate);
        }

        return sb.toString();
    }

    private static String buildText(String targets, String operate) {
        return buildText(null, targets, operate);
    }

    private static String buildText(String operate) {
        return buildText(null, operate);
    }
}
