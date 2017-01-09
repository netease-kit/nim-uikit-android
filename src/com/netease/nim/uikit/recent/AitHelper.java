package com.netease.nim.uikit.recent;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;

import com.netease.nim.uikit.NimUIKit;
import com.netease.nim.uikit.cache.NimUserInfoCache;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.msg.model.MemberPushOption;
import com.netease.nimlib.sdk.msg.model.RecentContact;
import com.netease.nimlib.sdk.team.model.TeamMember;

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

public class AitHelper {

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

    private static boolean isContentAit(String content) {
        if (TextUtils.isEmpty(content)) {
            return false;
        }
        Pattern pattern = Pattern.compile("(@" + NimUIKit.getAccount() + " )");
        Matcher matcher = pattern.matcher(content);
        return matcher.find();
    }

    public static boolean hasAitExtention(RecentContact recentContact) {
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


    public static void buildAitExtentionByMessage(Map<String, Object> extention, IMMessage message) {

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

        Map<String, Object> extention = recentContact.getExtension();

        if (extention == null) {
            extention = new HashMap<>();
        }

        Iterator<IMMessage> iterator = messages.iterator();
        while (iterator.hasNext()) {
            IMMessage msg = iterator.next();
            buildAitExtentionByMessage(extention, msg);
        }

        recentContact.setExtension(extention);
        NIMClient.getService(MsgService.class).updateRecent(recentContact);
    }

    public static ImageSpan getInputAitSpan(String name, float textsize) {
        if (TextUtils.isEmpty(name)) {
            return null;
        }
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setAntiAlias(true);
        paint.setTextSize(textsize);
        Rect rect = new Rect();

        paint.getTextBounds(name, 0, name.length(), rect);

        // 获取字符串在屏幕上的长度
        int width = (int) (paint.measureText(name));

        final Bitmap bmp = Bitmap.createBitmap(width, rect.height(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);

        canvas.drawText(name, rect.left, rect.height() - rect.bottom, paint);

        return new ImageSpan(NimUIKit.getContext(), bmp, ImageSpan.ALIGN_BOTTOM);
    }

    // 群昵称 > 用户昵称 > 账号
    public static String getAitName(TeamMember member) {

        if (member == null) {
            return "";
        }
        String memberNick = member.getTeamNick();
        if (!TextUtils.isEmpty(memberNick)) {
            return memberNick;
        }

        return NimUserInfoCache.getInstance().getUserName(member.getAccount());

    }
}
