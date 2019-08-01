package com.netease.nim.uikit.business.recent;

import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;

import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.msg.model.MemberPushOption;
import com.netease.nimlib.sdk.msg.model.RecentContact;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by hzchenkang on 2016/12/5.
 */

public class TeamMemberAitHelper {

    private static final String KEY_AIT = "ait";

    public static String getAitAlertString(String content) {
        return "[有人@你] " + content;
    }

    public static void replaceAitForeground(String value, SpannableString mSpannableString) {
        if (TextUtils.isEmpty(value) || TextUtils.isEmpty(mSpannableString)) {
            return;
        }
        Pattern pattern = Pattern.compile("(\\[有人@你\\])");
        Matcher matcher = pattern.matcher(value);
        while (matcher.find()) {
            int start = matcher.start();
            if (start != 0) {
                continue;
            }
            int end = matcher.end();
            mSpannableString.setSpan(new ForegroundColorSpan(Color.RED), start, end, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        }
    }

    public static boolean isAitMessage(IMMessage message) {
        if (message == null || message.getSessionType() != SessionTypeEnum.Team) {
            return false;
        }
        MemberPushOption option = message.getMemberPushOption();
        boolean isForce = option != null && option.isForcePush() &&
                (option.getForcePushList() == null || option.getForcePushList().contains(NimUIKit.getAccount()));

        return isForce;
    }

    public static boolean hasAitExtension(RecentContact recentContact) {
        if (recentContact == null || recentContact.getSessionType() != SessionTypeEnum.Team) {
            return false;
        }
        Map<String, Object> ext = recentContact.getExtension();
        if (ext == null) {
            return false;
        }
        List<String> mid = (List<String>) ext.get(KEY_AIT);

        return mid != null && !mid.isEmpty();
    }

    public static void clearRecentContactAited(RecentContact recentContact) {
        if (recentContact == null || recentContact.getSessionType() != SessionTypeEnum.Team) {
            return;
        }
        Map<String, Object> exts = recentContact.getExtension();
        if (exts != null) {
            exts.put(KEY_AIT, null);
        }
        recentContact.setExtension(exts);
        NIMClient.getService(MsgService.class).updateRecent(recentContact);
    }


    public static void buildAitExtensionByMessage(Map<String, Object> extention, IMMessage message) {

        if (extention == null || message == null || message.getSessionType() != SessionTypeEnum.Team) {
            return;
        }
        List<String> mid = (List<String>) extention.get(KEY_AIT);
        if (mid == null) {
            mid = new ArrayList<>();
        }
        if (!mid.contains(message.getUuid())) {
            mid.add(message.getUuid());
        }
        extention.put(KEY_AIT, mid);
    }

    public static void setRecentContactAited(RecentContact recentContact, Set<IMMessage> messages) {

        if (recentContact == null || messages == null ||
                recentContact.getSessionType() != SessionTypeEnum.Team) {
            return;
        }

        Map<String, Object> extension = recentContact.getExtension();

        if (extension == null) {
            extension = new HashMap<>();
        }

        Iterator<IMMessage> iterator = messages.iterator();
        while (iterator.hasNext()) {
            IMMessage msg = iterator.next();
            buildAitExtensionByMessage(extension, msg);
        }

        recentContact.setExtension(extension);
        NIMClient.getService(MsgService.class).updateRecent(recentContact);
    }
}
