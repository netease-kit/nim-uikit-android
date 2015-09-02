package com.netease.nim.uikit.team;

import android.text.TextUtils;

import com.netease.nim.uikit.NimUIKit;
import com.netease.nim.uikit.uinfo.UserInfoHelper;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.team.TeamService;
import com.netease.nimlib.sdk.team.TeamServiceObserver;
import com.netease.nimlib.sdk.team.constant.TeamTypeEnum;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.nimlib.sdk.team.model.TeamMember;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 群信息/群成员数据监听&缓存
 * <p/>
 * Created by huangjun on 2015/3/1.
 */
public class TeamDataCache {
    private static TeamDataCache instance;

    public static synchronized TeamDataCache getInstance() {
        if (instance == null) {
            instance = new TeamDataCache();
        }

        return instance;
    }

    /**
     * *
     * ********************** 观察者 ************************
     */

    public interface TeamDataChangedObserver {
        public void onUpdateTeams(List<Team> teams);

        public void onRemoveTeam(Team team);
    }

    public interface TeamMemberDataChangedObserver {
        public void onUpdateTeamMember(List<TeamMember> members);

        public void onRemoveTeamMember(TeamMember member);
    }

    public interface QueryTeamCallback {
        public void onResult(List<Team> teams);
    }

    private List<TeamDataChangedObserver> teamObservers = new ArrayList<>();
    private List<TeamMemberDataChangedObserver> memberObservers = new ArrayList<>();

    public void init() {
        NIMClient.getService(TeamServiceObserver.class).observeTeamUpdate(teamUpdateObserver, true);
        NIMClient.getService(TeamServiceObserver.class).observeTeamRemove(teamRemoveObserver, true);
        NIMClient.getService(TeamServiceObserver.class).observeMemberUpdate(memberUpdateObserver, true);
        NIMClient.getService(TeamServiceObserver.class).observeMemberRemove(memberRemoveObserver, true);
    }

    public void release() {
        NIMClient.getService(TeamServiceObserver.class).observeTeamUpdate(teamUpdateObserver, false);
        NIMClient.getService(TeamServiceObserver.class).observeTeamRemove(teamRemoveObserver, false);
        NIMClient.getService(TeamServiceObserver.class).observeMemberUpdate(memberUpdateObserver, false);
        NIMClient.getService(TeamServiceObserver.class).observeMemberRemove(memberRemoveObserver, false);
    }

    // 群资料变动观察者通知。新建群和群更新的通知都通过该接口传递
    private Observer<List<Team>> teamUpdateObserver = new Observer<List<Team>>() {
        @Override
        public void onEvent(List<Team> teams) {
            TeamDataCache.getInstance().addOrUpdateTeam(teams);
            notifyTeamDataUpdate(teams);
        }
    };

    // 移除群的观察者通知。自己退群，群被解散，自己被踢出群时，会收到该通知
    private Observer<Team> teamRemoveObserver = new Observer<Team>() {
        @Override
        public void onEvent(Team team) {
            TeamDataCache.getInstance().addOrUpdateTeam(team);
            notifyTeamDataRemove(team);
        }
    };

    // 群成员资料变化观察者通知。可通过此接口更新缓存。
    private Observer<List<TeamMember>> memberUpdateObserver = new Observer<List<TeamMember>>() {
        @Override
        public void onEvent(List<TeamMember> members) {
            TeamDataCache.getInstance().addOrUpdateTeamMembers(members);
            notifyTeamMemberDataUpdate(members);
        }
    };

    // 移除群成员的观察者通知。
    private Observer<TeamMember> memberRemoveObserver = new Observer<TeamMember>() {
        @Override
        public void onEvent(TeamMember member) {
            TeamDataCache.getInstance().removeTeamMember(member);
            notifyTeamMemberRemove(member);
        }
    };

    public void registerTeamDataChangedObserver(TeamDataChangedObserver o) {
        if (teamObservers.contains(o)) {
            return;
        }

        teamObservers.add(o);
    }

    public void unregisterTeamDataChangedObserver(TeamDataChangedObserver o) {
        teamObservers.remove(o);
    }

    public void registerTeamMemberDataChangedObserver(TeamMemberDataChangedObserver o) {
        if (memberObservers.contains(o)) {
            return;
        }

        memberObservers.add(o);
    }

    public void unregisterTeamMemberDataChangedObserver(TeamMemberDataChangedObserver o) {
        memberObservers.remove(o);
    }

    private void notifyTeamDataUpdate(List<Team> teams) {
        for (TeamDataChangedObserver o : teamObservers) {
            o.onUpdateTeams(teams);
        }
    }

    private void notifyTeamDataRemove(Team team) {
        for (TeamDataChangedObserver o : teamObservers) {
            o.onRemoveTeam(team);
        }
    }

    private void notifyTeamMemberDataUpdate(List<TeamMember> members) {
        for (TeamMemberDataChangedObserver o : memberObservers) {
            o.onUpdateTeamMember(members);
        }
    }

    private void notifyTeamMemberRemove(TeamMember member) {
        for (TeamMemberDataChangedObserver o : memberObservers) {
            o.onRemoveTeamMember(member);
        }
    }

    /**
     * *
     * ********************** 群缓存与持久 ************************
     */
    private Map<String, Team> id2TeamMap = new HashMap<>();

    private ReadWriteLock lock = new ReentrantReadWriteLock();

    public void initTeamCache() {
        queryTeamList();
    }

    public List<Team> queryTeamList() {
        List<Team> teams = NIMClient.getService(TeamService.class).queryTeamListBlock();
        TeamDataCache.getInstance().addOrUpdateTeam(teams);
        return teams;
    }

    public void clearTeamCache() {
        lock.writeLock().lock();
        {
            id2TeamMap.clear();
        }
        lock.writeLock().unlock();
    }

    public Team getTeamById(String id) {
        Team team;
        lock.readLock().lock();
        {
            team = id2TeamMap.get(id);
        }
        lock.readLock().unlock();

        return team;
    }

    public boolean isTeamInCache(String id) {
        boolean exist;
        lock.readLock().lock();
        {
            exist = id2TeamMap.containsKey(id);
        }
        lock.readLock().unlock();

        return exist;
    }

    public String getTeamName(String id) {
        Team team = getTeamById(id);
        return team == null ? id : TextUtils.isEmpty(team.getName()) ? team.getId() : team
                .getName();
    }

    public List<Team> getAllTeams() {
        List<Team> teams = new ArrayList<>();

        lock.readLock().lock();
        {
            for (Team t : id2TeamMap.values()) {
                if (t.isMyTeam()) {
                    teams.add(t);
                }
            }
        }
        lock.readLock().unlock();

        return teams;
    }

    public List<Team> getAllAdvancedTeams() {
        return getAllTeamsByType(TeamTypeEnum.Advanced);
    }

    public List<Team> getAllNormalTeams() {
        return getAllTeamsByType(TeamTypeEnum.Normal);
    }

    private List<Team> getAllTeamsByType(TeamTypeEnum type) {
        List<Team> teams = new ArrayList<>();

        lock.readLock().lock();
        {
            for (Team t : id2TeamMap.values()) {
                if (t.isMyTeam() && t.getType() == type) {
                    teams.add(t);
                }
            }
        }
        lock.readLock().unlock();

        return teams;
    }


    public void addOrUpdateTeam(Team team) {
        if (team == null) {
            return;
        }

        lock.writeLock().lock();
        {
            id2TeamMap.put(team.getId(), team);
        }
        lock.writeLock().unlock();
    }

    public void addOrUpdateTeam(List<Team> teamList) {
        if (teamList == null || teamList.isEmpty()) {
            return;
        }

        lock.writeLock().lock();
        {
            for (Team t : teamList) {
                if (t == null) {
                    continue;
                }

                id2TeamMap.put(t.getId(), t);
            }
        }
        lock.writeLock().unlock();
    }

    /**
     * *
     * ********************** 群成员缓存 ************************
     */
    private Map<String, Map<String, TeamMember>> teamMemberCache = new HashMap<>();


    public TeamMember getTeamMemberByAccount(String tid, String account) {
        Map<String, TeamMember> map = teamMemberCache.get(tid);
        if (map == null) {
            map = new HashMap<>();
            teamMemberCache.put(tid, map);
        }

        if (!map.containsKey(account)) {
            TeamMember member = NIMClient.getService(TeamService.class).queryTeamMemberBlock(tid, account);
            map.put(account, member);
        }
        return map.get(account);
    }


    public List<TeamMember> getTeamMemberListById(String teamId) {
        List<TeamMember> members = new ArrayList<>();
        Map<String, TeamMember> map = teamMemberCache.get(teamId);
        if (map != null && !map.values().isEmpty()) {
            members.addAll(map.values());
        }

        return members;
    }

    /**
     * 获取显示名称。用户本人显示“我”
     *
     * @param tid
     * @param account
     * @return
     */
    public String getTeamMemberDisplayName(String tid, String account) {
        if (account.equals(NimUIKit.getAccount())) {
            return "我";
        }

        return getDisplayNameWithoutMe(tid, account);
    }

    /**
     * 获取显示名称。用户本人显示“你”
     *
     * @param tid
     * @param account
     * @return
     */
    public String getTeamMemberDisplayNameYou(String tid, String account) {
        if (account.equals(NimUIKit.getAccount())) {
            return "你";
        }

        return getDisplayNameWithoutMe(tid, account);
    }

    /**
     * 获取显示名称。用户本人也显示昵称
     *
     * @param tid
     * @param account
     * @return
     */
    public String getDisplayNameWithoutMe(String tid, String account) {
        Team team = getTeamById(tid);
        if (team != null && team.getType() == TeamTypeEnum.Advanced) {
            TeamMember member = getTeamMemberByAccount(tid, account);
            if (member != null && !TextUtils.isEmpty(member.getTeamNick())) {
                return member.getTeamNick();
            }
        }

        return UserInfoHelper.getUserName(account);
    }

    public void addOrUpdateTeamMember(String tid, List<TeamMember> members) {
        if (members == null || members.isEmpty() || TextUtils.isEmpty(tid)) {
            return;
        }

        Map<String, TeamMember> map = teamMemberCache.get(tid);
        if (map == null) {
            map = new HashMap<>();
            teamMemberCache.put(tid, map);
        } else {
            map.clear();
        }

        for (TeamMember m : members) {
            map.put(m.getAccount(), m);
        }
    }

    public void addOrUpdateTeamMembers(List<TeamMember> members) {
        for (TeamMember m : members) {
            addOrUpdateTeamMember(m);
        }
    }

    public void addOrUpdateTeamMember(TeamMember member) {
        if (member == null) {
            return;
        }

        Map<String, TeamMember> map = teamMemberCache.get(member.getTid());
        if (map == null) {
            map = new HashMap<>();
            teamMemberCache.put(member.getTid(), map);
        }

        map.put(member.getAccount(), member);
    }

    public void removeTeamMember(TeamMember member) {
        if (member == null) {
            return;
        }

        if (teamMemberCache.containsKey(member.getTid())) {
            teamMemberCache.get(member.getTid()).remove(member.getAccount());
        }
    }
}
