/*
 * Copyright (c) 2022 NetEase, Inc. All rights reserved.
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.netease.yunxin.kit.teamkit.ui.utils;

import androidx.annotation.Nullable;
import com.netease.nimlib.sdk.v2.team.enums.V2NIMTeamMemberRole;
import com.netease.yunxin.kit.chatkit.cache.TeamMemberCache;
import com.netease.yunxin.kit.chatkit.cache.TeamMemberCacheListener;
import com.netease.yunxin.kit.chatkit.model.TeamMemberListResult;
import com.netease.yunxin.kit.chatkit.model.TeamMemberWithUserInfo;
import com.netease.yunxin.kit.chatkit.ui.cache.TeamUserChangedListener;
import com.netease.yunxin.kit.corekit.im2.IMKitClient;
import com.netease.yunxin.kit.corekit.im2.extend.FetchCallback;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Team UI facade for the shared chatkit team-member cache. */
public final class TeamKitTeamMemberCache {

  private static final TeamKitTeamMemberCache INSTANCE = new TeamKitTeamMemberCache();
  private final Map<String, Map<TeamUserChangedListener, TeamMemberCacheListener>> listenersByTeam =
      new HashMap<>();

  private TeamKitTeamMemberCache() {}

  public static TeamKitTeamMemberCache getInstance() {
    return INSTANCE;
  }

  public void initTeamId(String teamId) {
    TeamMemberCache.ensureTeam(teamId);
  }

  public List<TeamMemberWithUserInfo> getTeamMembersFromCache(
      String teamId, List<String> accountIds) {
    return TeamMemberCache.getTeamMembersFromCache(teamId, accountIds);
  }

  public List<TeamMemberWithUserInfo> getTeamMemberWithRoleListFromCache(
      String teamId, V2NIMTeamMemberRole role) {
    return TeamMemberCache.getTeamMembersWithRoleFromCache(teamId, role);
  }

  public List<String> getAllMembersAccountIds(String teamId) {
    return TeamMemberCache.getAllTeamMemberAccounts(teamId);
  }

  public String getNickname(String teamId, String accountId, boolean needAlias) {
    String name = TeamMemberCache.getNickname(teamId, accountId, needAlias);
    return name == null ? accountId : name;
  }

  public String getAvatar(String teamId, String accountId) {
    return TeamMemberCache.getAvatar(teamId, accountId);
  }

  public String getAvatarNickname(String teamId, String accountId) {
    String name = TeamMemberCache.getAvatarName(teamId, accountId);
    return name == null ? accountId : name;
  }

  public void getAllTeamMembers(
      String teamId, boolean needSelf, FetchCallback<List<TeamMemberWithUserInfo>> callback) {
    if (callback == null) {
      return;
    }
    TeamMemberCache.getAllTeamMembers(
        teamId,
        new FetchCallback<List<TeamMemberWithUserInfo>>() {
          @Override
          public void onSuccess(@Nullable List<TeamMemberWithUserInfo> data) {
            if (needSelf || data == null) {
              callback.onSuccess(data);
              return;
            }
            String account = IMKitClient.account();
            List<TeamMemberWithUserInfo> result = new ArrayList<>();
            for (TeamMemberWithUserInfo item : data) {
              if (item != null && !item.getAccountId().equals(account)) {
                result.add(item);
              }
            }
            callback.onSuccess(result);
          }

          @Override
          public void onError(int errorCode, String errorMsg) {
            callback.onError(errorCode, errorMsg);
          }
        });
  }

  public void getFirstTeamMemberPage(
      String teamId, boolean needSelf, FetchCallback<TeamMemberListResult> callback) {
    getPage(teamId, needSelf, true, callback);
  }

  public void getNextTeamMemberPage(
      String teamId, boolean needSelf, FetchCallback<TeamMemberListResult> callback) {
    getPage(teamId, needSelf, false, callback);
  }

  private void getPage(
      String teamId,
      boolean needSelf,
      boolean firstPage,
      FetchCallback<TeamMemberListResult> callback) {
    if (callback == null) {
      return;
    }
    FetchCallback<TeamMemberListResult> pageCallback =
        new FetchCallback<TeamMemberListResult>() {
          @Override
          public void onSuccess(@Nullable TeamMemberListResult result) {
            if (result != null && !needSelf) {
              result.setMemberList(filterSelf(result.getMemberList(), false));
            }
            callback.onSuccess(result);
          }

          @Override
          public void onError(int errorCode, String errorMsg) {
            callback.onError(errorCode, errorMsg);
          }
        };
    if (firstPage) {
      TeamMemberCache.getFirstTeamMemberPage(teamId, pageCallback);
    } else {
      TeamMemberCache.getNextTeamMemberPage(teamId, pageCallback);
    }
  }

  private List<TeamMemberWithUserInfo> filterSelf(
      List<TeamMemberWithUserInfo> data, boolean needSelf) {
    if (needSelf || data == null) {
      return data;
    }
    String account = IMKitClient.account();
    List<TeamMemberWithUserInfo> result = new ArrayList<>();
    for (TeamMemberWithUserInfo item : data) {
      if (item != null && !item.getAccountId().equals(account)) {
        result.add(item);
      }
    }
    return result;
  }

  public synchronized void addMemberChangedListener(
      String teamId, TeamUserChangedListener listener) {
    if (teamId == null || teamId.isEmpty() || listener == null) {
      return;
    }
    initTeamId(teamId);
    Map<TeamUserChangedListener, TeamMemberCacheListener> teamListeners =
        listenersByTeam.get(teamId);
    if (teamListeners == null) {
      teamListeners = new HashMap<>();
      listenersByTeam.put(teamId, teamListeners);
    }
    if (teamListeners.containsKey(listener)) {
      TeamMemberCache.addMemberChangedListener(teamId, teamListeners.get(listener));
      return;
    }
    TeamMemberCacheListener adapter =
        new TeamMemberCacheListener() {
          @Override
          public void onUsersChanged(String changedTeamId, List<String> accountIds) {
            if (teamId.equals(changedTeamId)) {
              listener.onUsersChanged(accountIds);
            }
          }

          @Override
          public void onUsersAdded(String changedTeamId, List<String> accountIds) {
            if (teamId.equals(changedTeamId)) {
              listener.onUsersAdd(accountIds);
            }
          }

          @Override
          public void onUsersRemoved(String changedTeamId, List<String> accountIds) {
            if (teamId.equals(changedTeamId)) {
              listener.onUserDelete(accountIds);
            }
          }
        };
    teamListeners.put(listener, adapter);
    TeamMemberCache.addMemberChangedListener(teamId, adapter);
  }

  public synchronized void removeMemberChangedListener(
      String teamId, TeamUserChangedListener listener) {
    Map<TeamUserChangedListener, TeamMemberCacheListener> teamListeners =
        listenersByTeam.get(teamId);
    if (teamListeners == null) {
      return;
    }
    TeamMemberCacheListener adapter = teamListeners.remove(listener);
    if (adapter != null) {
      TeamMemberCache.removeMemberChangedListener(teamId, adapter);
    }
    if (teamListeners.isEmpty()) {
      listenersByTeam.remove(teamId);
    }
  }
}
