// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.teamkit.ui.utils;

import static com.netease.yunxin.kit.teamkit.ui.utils.TeamUIKitConstant.LIB_TAG;

import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.netease.nimlib.sdk.v2.V2NIMError;
import com.netease.nimlib.sdk.v2.auth.enums.V2NIMDataSyncState;
import com.netease.nimlib.sdk.v2.auth.enums.V2NIMDataSyncType;
import com.netease.nimlib.sdk.v2.team.enums.V2NIMTeamMemberRole;
import com.netease.nimlib.sdk.v2.team.enums.V2NIMTeamType;
import com.netease.nimlib.sdk.v2.team.model.V2NIMTeamMember;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.impl.LoginDetailListenerImpl;
import com.netease.yunxin.kit.chatkit.impl.TeamListenerImpl;
import com.netease.yunxin.kit.chatkit.model.TeamMemberWithUserInfo;
import com.netease.yunxin.kit.chatkit.repo.ContactRepo;
import com.netease.yunxin.kit.chatkit.repo.TeamRepo;
import com.netease.yunxin.kit.corekit.im2.IMKitClient;
import com.netease.yunxin.kit.corekit.im2.extend.FetchCallback;
import com.netease.yunxin.kit.corekit.im2.listener.ContactChangeType;
import com.netease.yunxin.kit.corekit.im2.listener.ContactListener;
import com.netease.yunxin.kit.corekit.im2.model.FriendAddApplicationInfo;
import com.netease.yunxin.kit.corekit.im2.model.UserWithFriend;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 群设置模块群成员缓存 群设置很多页面都需要群成员信息，为了避免重复请求，这里做了缓存 1. 缓存群成员信息 1、进入页面时，调用initTeamId方法，传入群ID，加载群成员信息
 * 2、注册缓存监听器addTeamMemberCacheListener(teamMemberListener)，当群成员信息变化时，通知页面更新
 * 3、调用getTeamMemberList方法，获取群成员信息 ，如果为空则重新调用loadTeamMemberList方法加载数据
 * 4、所有使用的页面都退出的时候，调用clear方法，清空缓存数据
 */
public class TeamMemberCache {

  private static final String TAG = "TeamMemberCache";
  private final Map<String, TeamMemberWithUserInfo> teamMemberMap = new HashMap<>();
  private final List<WeakReference<TeamMemberCacheListener>> cacheListenerList = new ArrayList<>();
  private String cacheTeamId;

  public static TeamMemberCache Instance() {
    return TeamMemberCacheHolder.INSTANCE;
  }

  protected class TeamMemberCacheHolder {
    private static final TeamMemberCache INSTANCE = new TeamMemberCache();
  }

  // 群成员变化监听，根据群成员的变化，修改缓存数据
  private TeamListenerImpl teamListener =
      new TeamListenerImpl() {
        @Override
        public void onTeamMemberJoined(List<V2NIMTeamMember> teamMembers) {
          ALog.d(LIB_TAG, TAG, "teamListener,onTeamMemberJoined");
          addRemoveMembersData(teamMembers, true);
        }

        @Override
        public void onTeamMemberKicked(
            String operatorAccountId, List<V2NIMTeamMember> teamMembers) {
          ALog.d(LIB_TAG, TAG, "teamListener,onTeamMemberKicked");
          addRemoveMembersData(teamMembers, false);
        }

        @Override
        public void onTeamMemberLeft(List<V2NIMTeamMember> teamMembers) {
          ALog.d(LIB_TAG, TAG, "teamListener,onTeamMemberLeft");
          addRemoveMembersData(teamMembers, false);
        }

        @Override
        public void onTeamMemberInfoUpdated(List<V2NIMTeamMember> teamMembers) {
          ALog.d(LIB_TAG, TAG, "teamListener,onTeamMemberInfoUpdated");
          if (teamMembers == null) {
            return;
          }
          ArrayList<V2NIMTeamMember> updateTeamMembers = new ArrayList<>();
          for (V2NIMTeamMember item : teamMembers) {
            if (!TextUtils.equals(item.getTeamId(), cacheTeamId)) {
              continue;
            }
            updateTeamMembers.add(item);
          }
          if (updateTeamMembers.size() > 0) {
            updateTeamMember(updateTeamMembers);
          }
        }
      };

  /** 移除成员逻辑处理 */
  protected void addRemoveMembersData(List<V2NIMTeamMember> teamMembers, boolean isAdd) {
    if (teamMembers == null) {
      return;
    }
    ALog.d(LIB_TAG, TAG, "removeMembersData,teamMembers.size():" + teamMembers.size());
    ArrayList<String> removeList = new ArrayList<>();
    for (V2NIMTeamMember item : teamMembers) {
      if (!TextUtils.equals(item.getTeamId(), cacheTeamId)) {
        continue;
      }
      removeList.add(item.getAccountId());
    }
    if (isAdd) {
      TeamRepo.getTeamMemberListWithUserInfoByIds(
          cacheTeamId,
          V2NIMTeamType.V2NIM_TEAM_TYPE_NORMAL,
          removeList,
          new FetchCallback<>() {
            @Override
            public void onSuccess(@Nullable List<TeamMemberWithUserInfo> param) {
              if (param != null) {
                addTeamMemberWithUserList(param);
              }
            }

            @Override
            public void onError(int errorCode, String errorMsg) {
              ALog.d(LIB_TAG, TAG, "removeMembersData,onFailed:" + errorCode);
              addTeamMemberList(teamMembers);
            }
          });
    } else {
      removeTeamMember(removeList);
      TeamMemberCache.Instance().removeTeamMember(removeList);
    }
  }

  // 好友信息变化监听，根据好友信息的变化，修改缓存数据（好友昵称）
  private ContactListener friendListener =
      new ContactListener() {

        @Override
        public void onFriendAddRejected(@NonNull FriendAddApplicationInfo rejectionInfo) {}

        @Override
        public void onFriendAddApplication(@NonNull FriendAddApplicationInfo friendApplication) {}

        @Override
        public void onContactChange(
            @NonNull ContactChangeType changeType,
            @NonNull List<? extends UserWithFriend> contactList) {
          List<TeamMemberWithUserInfo> updateList = new ArrayList<>();
          for (UserWithFriend userInfo : contactList) {
            TeamMemberWithUserInfo teamMember = teamMemberMap.get(userInfo.getAccount());
            if (teamMember != null) {
              if (changeType == ContactChangeType.AddFriend
                  || changeType == ContactChangeType.Update) {
                teamMember.setFriendInfo(userInfo.getFriend());
                teamMember.setUserInfo(userInfo.getUserInfo());
                updateList.add(teamMember);
              } else if (changeType == ContactChangeType.DeleteFriend) {
                teamMember.setFriendInfo(null);
                updateList.add(teamMember);
              }
            }
          }
          if (updateList.size() > 0) {
            notifyTeamMemberCacheUpdate(updateList);
          }
        }
      };

  private LoginDetailListenerImpl loginDetailListener =
      new LoginDetailListenerImpl() {
        @Override
        public void onDataSync(
            @Nullable V2NIMDataSyncType type,
            @Nullable V2NIMDataSyncState state,
            @Nullable V2NIMError error) {
          if (type == V2NIMDataSyncType.V2NIM_DATA_SYNC_TEAM_MEMBER
              && state == V2NIMDataSyncState.V2NIM_DATA_SYNC_STATE_COMPLETED) {
            if (!TextUtils.isEmpty(cacheTeamId)) {
              loadTeamMemberList(cacheTeamId);
            }
          }
        }
      };

  /**
   * 初始化群成员缓存 如果群ID不一致，则清空数据，加载新的群成员数据
   *
   * @param teamId 群ID
   */
  public void initTeamId(String teamId) {
    if (teamId == null || teamId.equals(this.cacheTeamId)) {
      return;
    }
    clear();
    loadTeamMemberList(cacheTeamId);
    TeamRepo.addTeamListener(teamListener);
    ContactRepo.addContactListener(friendListener);
    IMKitClient.addLoginDetailListener(loginDetailListener);
  }

  /**
   * 获取当前缓存的群ID
   *
   * @return
   */
  public String getTeamId() {
    return cacheTeamId;
  }

  /**
   * 加载群成员信息，断网异常场景下，使用者根据需要调用。该方法会讲结果在TeamMemberCacheListener.onTeamMemberCacheLoad回调
   * 如果getTeamMemberList返回空，则调用此方法加载数据,简化异常场景下的处理
   *
   * @param teamId
   */
  public void loadTeamMemberList(String teamId) {
    if (teamId == null) {
      return;
    }
    if (!teamId.equals(cacheTeamId)) {
      teamMemberMap.clear();
      cacheTeamId = teamId;
    }
    TeamRepo.queryAllTeamMemberListWithUserInfo(
        teamId,
        new FetchCallback<>() {
          @Override
          public void onSuccess(@Nullable List<TeamMemberWithUserInfo> param) {
            ALog.d(
                LIB_TAG,
                TAG,
                "requestAllTeamMembers,onSuccess:" + (param == null ? "null" : param.size()));
            if (param != null) {
              teamMemberMap.clear();
              for (TeamMemberWithUserInfo teamMember : param) {
                teamMemberMap.put(teamMember.getAccountId(), teamMember);
              }
              notifyTeamMemberCacheLoad(param);
            }
          }

          @Override
          public void onError(int errorCode, String errorMsg) {
            ALog.d(LIB_TAG, TAG, "requestAllTeamMembers,onFailed:" + errorCode);
          }
        });
  }

  // 添加群成员信息
  private void addTeamMemberWithUserList(List<TeamMemberWithUserInfo> teamMemberList) {
    if (teamMemberList == null) {
      return;
    }
    for (TeamMemberWithUserInfo teamMember : teamMemberList) {
      teamMemberMap.put(teamMember.getAccountId(), teamMember);
    }
    notifyTeamMemberCacheAdd(teamMemberList);
  }

  // 添加群成员信息
  private void addTeamMemberList(List<V2NIMTeamMember> teamMemberList) {
    if (teamMemberList == null) {
      return;
    }
    for (V2NIMTeamMember teamMember : teamMemberList) {
      if (teamMemberMap.containsKey(teamMember.getAccountId())) {
        continue;
      }
      TeamMemberWithUserInfo teamMemberWithUserInfo = new TeamMemberWithUserInfo(teamMember, null);
      teamMemberMap.put(teamMember.getAccountId(), teamMemberWithUserInfo);
    }
  }

  // 更新群成员信息
  private void updateTeamMember(List<V2NIMTeamMember> teamMemberList) {
    if (teamMemberList == null) {
      return;
    }
    List<TeamMemberWithUserInfo> updateList = new ArrayList<>();
    for (V2NIMTeamMember teamMember : teamMemberList) {
      if (teamMemberMap.containsKey(teamMember.getAccountId())) {
        TeamMemberWithUserInfo teamMemberWithUserInfo =
            teamMemberMap.get(teamMember.getAccountId());
        if (teamMemberWithUserInfo != null) {
          teamMemberWithUserInfo.setTeamMember(teamMember);
        } else {
          teamMemberWithUserInfo = new TeamMemberWithUserInfo(teamMember, null);
          teamMemberMap.put(teamMember.getAccountId(), teamMemberWithUserInfo);
        }
        updateList.add(teamMemberWithUserInfo);
      }
    }
    if (updateList.size() > 0) {
      notifyTeamMemberCacheUpdate(updateList);
    }
  }

  // 移除群成员信息
  private void removeTeamMember(List<String> accountList) {
    if (accountList == null || accountList.size() < 1) {
      return;
    }
    for (String item : accountList) {
      teamMemberMap.remove(item);
    }
    notifyTeamMemberCacheRemove(accountList);
  }

  /**
   * 获取群成员信息
   *
   * @param teamId 群ID
   * @return 群成员信息
   */
  public List<TeamMemberWithUserInfo> getTeamMemberList(String teamId) {
    if (teamId == null || !teamId.equals(cacheTeamId)) {
      return new ArrayList<>();
    }
    return new ArrayList<>(teamMemberMap.values());
  }

  /**
   * 根据群身份获取群成员信息
   *
   * @param teamId 群ID
   * @param role 群身份
   * @return 群成员信息
   */
  public List<TeamMemberWithUserInfo> getTeamMemberWithRoleList(
      String teamId, V2NIMTeamMemberRole role) {
    if (teamId == null || !teamId.equals(cacheTeamId)) {
      return new ArrayList<>();
    }
    List<TeamMemberWithUserInfo> teamMemberList = new ArrayList<>();
    if (role != null) {
      for (TeamMemberWithUserInfo teamMember : teamMemberMap.values()) {
        if (teamMember.getTeamMember() != null
            && teamMember.getTeamMember().getMemberRole() == role) {
          teamMemberList.add(teamMember);
        }
      }
    }
    return teamMemberList;
  }

  // 获取群成员信息
  public void clear() {
    teamMemberMap.clear();
    cacheTeamId = "";
    TeamRepo.removeTeamListener(teamListener);
    ContactRepo.removeContactListener(friendListener);
    IMKitClient.removeLoginDetailListener(loginDetailListener);
  }

  // 通知群成员列表拉取完成
  private void notifyTeamMemberCacheLoad(List<TeamMemberWithUserInfo> teamMemberList) {
    for (WeakReference<TeamMemberCacheListener> weakReference : cacheListenerList) {
      TeamMemberCacheListener cacheListener = weakReference.get();
      if (cacheListener != null) {
        cacheListener.onTeamMemberCacheLoad(cacheTeamId, teamMemberList);
      }
    }
  }

  // 通知群成员信息变化
  private void notifyTeamMemberCacheUpdate(List<TeamMemberWithUserInfo> teamMemberList) {
    for (WeakReference<TeamMemberCacheListener> weakReference : cacheListenerList) {
      TeamMemberCacheListener cacheListener = weakReference.get();
      if (cacheListener != null) {
        cacheListener.onTeamMemberCacheUpdate(cacheTeamId, teamMemberList);
      }
    }
  }

  // 通知群成员信息添加
  private void notifyTeamMemberCacheAdd(List<TeamMemberWithUserInfo> teamMemberList) {
    for (WeakReference<TeamMemberCacheListener> weakReference : cacheListenerList) {
      TeamMemberCacheListener cacheListener = weakReference.get();
      if (cacheListener != null) {
        cacheListener.onTeamMemberCacheAdd(cacheTeamId, teamMemberList);
      }
    }
  }

  // 通知群成员信息移除
  private void notifyTeamMemberCacheRemove(List<String> accountList) {
    for (WeakReference<TeamMemberCacheListener> weakReference : cacheListenerList) {
      TeamMemberCacheListener cacheListener = weakReference.get();
      if (cacheListener != null) {
        cacheListener.onTeamMemberCacheRemove(cacheTeamId, accountList);
      }
    }
  }

  /**
   * 添加群成员缓存监听
   *
   * @param listener 监听器
   */
  public void addTeamMemberCacheListener(TeamMemberCacheListener listener) {
    if (listener == null) {
      return;
    }
    cacheListenerList.add(new WeakReference<>(listener));
  }

  /**
   * 移除群成员缓存监听
   *
   * @param listener 监听器
   */
  public void removeTeamMemberCacheListener(TeamMemberCacheListener listener) {
    if (listener == null) {
      return;
    }
    for (WeakReference<TeamMemberCacheListener> weakReference : cacheListenerList) {
      TeamMemberCacheListener cacheListener = weakReference.get();
      if (cacheListener == listener) {
        cacheListenerList.remove(weakReference);
        break;
      }
    }
  }
}
