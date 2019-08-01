package com.netease.nim.uikit.impl.cache;

import android.text.TextUtils;

import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.api.model.SimpleCallback;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.RequestCallbackWrapper;
import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.superteam.SuperTeam;
import com.netease.nimlib.sdk.superteam.SuperTeamMember;
import com.netease.nimlib.sdk.superteam.SuperTeamService;
import com.netease.nimlib.sdk.superteam.SuperTeamServiceObserver;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 群信息/群成员数据监听&缓存
 * <p/>
 * Created by huangjun on 2015/3/1.
 */
public class SuperTeamDataCache {
    private static SuperTeamDataCache instance;

    public static synchronized SuperTeamDataCache getInstance() {
        if (instance == null) {
            instance = new SuperTeamDataCache();
        }

        return instance;
    }

    public void buildCache() {
        final List<SuperTeam> teams = NIMClient.getService(SuperTeamService.class).queryTeamListBlock();
        if (teams == null) {
            return;
        }
        LogUtil.i(UIKitLogTag.TEAM_CACHE, "start build TeamDataCache");

        addOrUpdateTeam(teams);

        LogUtil.i(UIKitLogTag.TEAM_CACHE, "build TeamDataCache completed, team count = " + teams.size());
    }

    public void clear() {
        clearTeamCache();
        clearTeamMemberCache();
    }

    /**
     * *
     * ******************************************** 观察者 ********************************************
     */

    public void registerObservers(boolean register) {
        NIMClient.getService(SuperTeamServiceObserver.class).observeTeamUpdate(teamUpdateObserver, register);
        NIMClient.getService(SuperTeamServiceObserver.class).observeTeamRemove(teamRemoveObserver, register);
        NIMClient.getService(SuperTeamServiceObserver.class).observeMemberUpdate(memberUpdateObserver, register);
        NIMClient.getService(SuperTeamServiceObserver.class).observeMemberRemove(memberRemoveObserver, register);
    }

    // 群资料变动观察者通知。新建群和群更新的通知都通过该接口传递
    private Observer<List<SuperTeam>> teamUpdateObserver = new Observer<List<SuperTeam>>() {
        @Override
        public void onEvent(final List<SuperTeam> teams) {
            if (teams == null) {
                return;
            }
            LogUtil.i(UIKitLogTag.TEAM_CACHE, "team update size:" + teams.size());
            addOrUpdateTeam(teams);
            NimUIKit.getSuperTeamChangedObservable().notifyTeamDataUpdate(teams);
        }
    };

    // 移除群的观察者通知。自己退群，群被解散，自己被踢出群时，会收到该通知
    private Observer<SuperTeam> teamRemoveObserver = new Observer<SuperTeam>() {
        @Override
        public void onEvent(SuperTeam team) {
            // team的flag被更新，isMyTeam为false
            addOrUpdateTeam(team);
            NimUIKit.getSuperTeamChangedObservable().notifyTeamDataRemove(team);
        }
    };

    // 群成员资料变化观察者通知。可通过此接口更新缓存。
    private Observer<List<SuperTeamMember>> memberUpdateObserver = new Observer<List<SuperTeamMember>>() {
        @Override
        public void onEvent(List<SuperTeamMember> members) {
            addOrUpdateTeamMembers(members);
            NimUIKit.getSuperTeamChangedObservable().notifyTeamMemberDataUpdate(members);
        }
    };

    // 移除群成员的观察者通知，仅仅 member的validFlag被更新，db 仍存在数据
    private Observer<List<SuperTeamMember>> memberRemoveObserver = new Observer<List<SuperTeamMember>>() {
        @Override
        public void onEvent(List<SuperTeamMember> member) {
            // member的validFlag被更新，isInTeam为false
            addOrUpdateTeamMembers(member);
            NimUIKit.getSuperTeamChangedObservable().notifyTeamMemberRemove(member);
        }
    };


    /**
     * *
     * ******************************************** 群资料缓存 ********************************************
     */

    private Map<String, SuperTeam> id2TeamMap = new ConcurrentHashMap<>();

    public void clearTeamCache() {
        id2TeamMap.clear();
    }

    /**
     * 异步获取Team（先从SDK DB中查询，如果不存在，则去服务器查询）
     */
    public void fetchTeamById(final String teamId, final SimpleCallback<SuperTeam> callback) {
        NIMClient.getService(SuperTeamService.class).queryTeam(teamId).setCallback(new RequestCallbackWrapper<SuperTeam>() {
            @Override
            public void onResult(int code, SuperTeam t, Throwable exception) {
                boolean success = true;
                if (code == ResponseCode.RES_SUCCESS) {
                    addOrUpdateTeam(t);
                } else {
                    success = false;
                    LogUtil.e(UIKitLogTag.TEAM_CACHE, "fetchTeamById failed, code=" + code);
                }

                if (exception != null) {
                    success = false;
                    LogUtil.e(UIKitLogTag.TEAM_CACHE, "fetchTeamById throw exception, e=" + exception.getMessage());
                }

                if (callback != null) {
                    callback.onResult(success, t, code);
                }
            }
        });
    }

    /**
     * 同步从本地获取Team（先从缓存中查询，如果不存在再从SDK DB中查询）
     */
    public SuperTeam getTeamById(String teamId) {
        if (teamId == null) {
            return null;
        }
        SuperTeam team = id2TeamMap.get(teamId);

        if (team == null) {
            team = NIMClient.getService(SuperTeamService.class).queryTeamBlock(teamId);
            addOrUpdateTeam(team);
        }

        return team;
    }

    public List<SuperTeam> getAllTeams() {
        List<SuperTeam> teams = new ArrayList<>();
        for (SuperTeam t : id2TeamMap.values()) {
            if (t.isMyTeam()) {
                teams.add(t);
            }
        }
        return teams;
    }

    public void addOrUpdateTeam(SuperTeam team) {
        if (team == null) {
            return;
        }

        id2TeamMap.put(team.getId(), team);
    }

    private void addOrUpdateTeam(List<SuperTeam> teamList) {
        if (teamList == null || teamList.isEmpty()) {
            return;
        }

        for (SuperTeam t : teamList) {
            if (t == null) {
                continue;
            }

            id2TeamMap.put(t.getId(), t);
        }
    }

    /**
     * *
     * ************************************** 群成员缓存(由App主动添加缓存) ****************************************
     */

    private Map<String, Map<String, SuperTeamMember>> teamMemberCache = new ConcurrentHashMap<>();

    public void clearTeamMemberCache() {
        teamMemberCache.clear();
    }

    /**
     * （异步）查询群成员资料列表（先从SDK DB中查询，如果本地群成员资料已过期会去服务器获取最新的。）
     */
    public void fetchTeamMemberList(final String teamId, final SimpleCallback<List<SuperTeamMember>> callback) {
        NIMClient.getService(SuperTeamService.class).queryMemberList(teamId).setCallback(new RequestCallbackWrapper<List<SuperTeamMember>>() {
            @Override
            public void onResult(int code, final List<SuperTeamMember> members, Throwable exception) {
                boolean success = true;
                if (code == ResponseCode.RES_SUCCESS) {
                    replaceTeamMemberList(teamId, members);
                } else {
                    success = false;
                    LogUtil.e(UIKitLogTag.TEAM_CACHE, "fetchTeamMemberList failed, code=" + code);
                }

                if (exception != null) {
                    success = false;
                    LogUtil.e(UIKitLogTag.TEAM_CACHE, "fetchTeamMemberList throw exception, e=" + exception.getMessage());
                }

                if (callback != null) {
                    callback.onResult(success, members, code);
                }
            }
        });
    }

    /**
     * 在缓存中查询群成员列表
     */
    public List<SuperTeamMember> getTeamMemberList(String teamId) {
        List<SuperTeamMember> members = new ArrayList<>();
        Map<String, SuperTeamMember> map = teamMemberCache.get(teamId);
        if (map != null && !map.values().isEmpty()) {
            for (SuperTeamMember m : map.values()) {
                if (m.isInTeam()) {
                    members.add(m);
                }
            }
        }

        return members;
    }

    /**
     * （异步）查询群成员资料（先从SDK DB中查询，如果本地群成员资料已过期会去服务器获取最新的。）
     */
    public void fetchTeamMember(final String teamId, final String account, final SimpleCallback<SuperTeamMember> callback) {
        NIMClient.getService(SuperTeamService.class).queryTeamMember(teamId, account).setCallback(new RequestCallbackWrapper<SuperTeamMember>() {
            @Override
            public void onResult(int code, SuperTeamMember member, Throwable exception) {
                boolean success = true;
                if (code == ResponseCode.RES_SUCCESS) {
                    addOrUpdateTeamMember(member);
                } else {
                    success = false;
                    LogUtil.e(UIKitLogTag.TEAM_CACHE, "fetchTeamMember failed, code=" + code);
                }

                if (exception != null) {
                    success = false;
                    LogUtil.e(UIKitLogTag.TEAM_CACHE, "fetchTeamMember throw exception, e=" + exception.getMessage());
                }

                if (callback != null) {
                    callback.onResult(success, member, code);
                }
            }
        });
    }

    /**
     * 查询群成员资料（先从缓存中查，如果没有则从SDK DB中查询）
     */
    public SuperTeamMember getTeamMember(String teamId, String account) {
        Map<String, SuperTeamMember> map = teamMemberCache.get(teamId);
        if (map == null) {
            map = new ConcurrentHashMap<>();
            teamMemberCache.put(teamId, map);
        }

        if (!map.containsKey(account)) {
            SuperTeamMember member = NIMClient.getService(SuperTeamService.class).queryTeamMemberBlock(teamId, account);
            if (member != null) {
                map.put(account, member);
            }
        }

        return map.get(account);
    }

    private void replaceTeamMemberList(String tid, List<SuperTeamMember> members) {
        if (members == null || members.isEmpty() || TextUtils.isEmpty(tid)) {
            return;
        }

        Map<String, SuperTeamMember> map = teamMemberCache.get(tid);
        if (map == null) {
            map = new ConcurrentHashMap<>();
            teamMemberCache.put(tid, map);
        } else {
            map.clear();
        }

        for (SuperTeamMember m : members) {
            map.put(m.getAccount(), m);
        }
    }

    private void addOrUpdateTeamMember(SuperTeamMember member) {
        if (member == null) {
            return;
        }

        Map<String, SuperTeamMember> map = teamMemberCache.get(member.getTid());
        if (map == null) {
            map = new ConcurrentHashMap<>();
            teamMemberCache.put(member.getTid(), map);
        }

        map.put(member.getAccount(), member);
    }

    private void addOrUpdateTeamMembers(List<SuperTeamMember> members) {
        for (SuperTeamMember m : members) {
            addOrUpdateTeamMember(m);
        }
    }
}
