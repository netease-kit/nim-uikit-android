package com.netease.nim.uikit.business.team.helper;

import android.text.TextUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.business.team.model.Announcement;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by huangjun on 2015/3/24.
 */
public class AnnouncementHelper {
    public final static String JSON_KEY_CREATOR = "creator";
    public final static String JSON_KEY_TITLE = "title";
    public final static String JSON_KEY_TIME = "time";
    public final static String JSON_KEY_CONTENT = "content";
    public final static String JSON_KEY_ID = "id";

    public static String makeAnnounceJson(String announce, String title, String content) {
        JSONArray jsonArray = null;
        try {
            jsonArray = JSONArray.parseArray(announce);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (jsonArray == null) {
            jsonArray = new JSONArray();
        }

        JSONObject json = new JSONObject();
        json.put(JSON_KEY_ID, UUID.randomUUID().toString());
        json.put(JSON_KEY_CREATOR, getCreatorName());
        json.put(JSON_KEY_TITLE, title);
        json.put(JSON_KEY_CONTENT, content);
        json.put(JSON_KEY_TIME, (System.currentTimeMillis() / 1000)); // 与ios和pc兼容
        jsonArray.add(json);
        return jsonArray.toString();
    }

    public static List<Announcement> getAnnouncements(String teamId, String announce, int limit) {
        if (TextUtils.isEmpty(announce)) {
            return null;
        }

        List<Announcement> announcements = new ArrayList<>();
        try {
            int count = 0;
            JSONArray jsonArray = JSONArray.parseArray(announce);
            for (int i = jsonArray.size() - 1; i >= 0; i--) {
                JSONObject json = jsonArray.getJSONObject(i);
                String id = json.getString(JSON_KEY_ID);
                String creator = json.getString(JSON_KEY_CREATOR);
                String title = json.getString(JSON_KEY_TITLE);
                long time = json.getLongValue(JSON_KEY_TIME);
                String content = json.getString(JSON_KEY_CONTENT);

                announcements.add(new Announcement(id, teamId, creator, title, time, content));

                if (++count >= limit) {
                    break;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return announcements;
    }

    public static Announcement getLastAnnouncement(String teamId, String announcement) {
        List<Announcement> list = getAnnouncements(teamId, announcement, 1);
        return (list == null || list.isEmpty()) ? null : list.get(0);
    }

    private static String getCreatorName() {
        return NimUIKit.getAccount();
    }
}
