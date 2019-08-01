package com.netease.nim.uikit.business.team.model;

/**
 * 群公告结构(json)
 * <p/>
 * Created by huangjun on 2015/3/24.
 */
public class Announcement {
    private String id;
    private String creator;
    private String title;
    private long time;
    private String content;
    private String teamId;

    public Announcement(String id, String teamId, String creator, String title, long time, String content) {
        this.id = id;
        this.teamId = teamId;
        this.creator = creator;
        this.title = title;
        this.time = time;
        this.content = content;
    }

    public String getId() {
        return id;
    }

    public String getTeamId() {
        return teamId;
    }

    public String getCreator() {
        return creator;
    }

    public String getTitle() {
        return title;
    }

    public long getTime() {
        return time;
    }

    public String getContent() {
        return content;
    }
}
