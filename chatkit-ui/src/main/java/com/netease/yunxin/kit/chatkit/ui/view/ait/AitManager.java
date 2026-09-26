// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.view.ait;

import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import androidx.annotation.Nullable;
import com.netease.nimlib.sdk.v2.ai.model.V2NIMAIUser;
import com.netease.nimlib.sdk.v2.team.enums.V2NIMTeamMemberRole;
import com.netease.nimlib.sdk.v2.team.model.V2NIMTeam;
import com.netease.nimlib.sdk.v2.team.model.V2NIMTeamMember;
import com.netease.yunxin.kit.chatkit.IMKitConfigCenter;
import com.netease.yunxin.kit.chatkit.cache.TeamMemberCache;
import com.netease.yunxin.kit.chatkit.cache.TeamMemberCacheListener;
import com.netease.yunxin.kit.chatkit.manager.AIUserChangeListener;
import com.netease.yunxin.kit.chatkit.manager.AIUserManager;
import com.netease.yunxin.kit.chatkit.model.TeamMemberListResult;
import com.netease.yunxin.kit.chatkit.model.TeamMemberWithUserInfo;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.common.ChatUtils;
import com.netease.yunxin.kit.chatkit.ui.model.ait.AitBlock;
import com.netease.yunxin.kit.chatkit.ui.model.ait.AitUserInfo;
import com.netease.yunxin.kit.chatkit.ui.model.ait.AtContactsModel;
import com.netease.yunxin.kit.corekit.im2.IMKitClient;
import com.netease.yunxin.kit.corekit.im2.extend.FetchCallback;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.json.JSONObject;

/** Team member @ manager */
public class AitManager implements TextWatcher {

  private final Context mContext;
  // 群id
  private final String tid;
  // @信息实体类
  private final AtContactsModel atContactsModel;
  // @文本输入监听
  private AitTextChangeListener aitTextChangeListener;
  // 当前光标位置
  private int curPos;
  // 是否忽略文本变化
  private boolean ignoreTextChange = false;
  // 文本输入开始位置
  private int editTextStart;
  // 文本输入数量
  private int editTextCount;
  // 文本输入前位置
  private int editTextBefore;
  // 是否删除操作
  private boolean delete;
  // 是否显示@所有成员
  private boolean showAll = true;
  private final AitContactSelectorFactory selectorFactory;
  private AitContactSelector aitDialog;

  // 是否展示AIUser
  private boolean showAIUser = true;

  //是否展示群成员
  private boolean showTeamMember = true;
  private boolean pageLoading;
  private boolean pageFinished;
  private boolean teamMemberListenerRegistered;
  private boolean aiUserListenerRegistered;
  private long loadGeneration;
  private final List<TeamMemberWithUserInfo> loadedTeamMembers = new ArrayList<>();
  private final List<AitUserInfo> displayAIUsers = new ArrayList<>();

  private final TeamMemberCacheListener teamMemberCacheListener =
      new TeamMemberCacheListener() {
        @Override
        public void onUsersChanged(String teamId, List<String> accountIds) {
          if (!isDialogActive(teamId) || accountIds == null || accountIds.isEmpty()) {
            return;
          }
          updateLoadedMembers(teamId, accountIds);
          refreshDialog(aitDialog);
        }

        @Override
        public void onUsersAdded(String teamId, List<String> accountIds) {
          if (!TextUtils.equals(tid, teamId)
              || !showTeamMember
              || aitDialog == null
              || !aitDialog.isShowing()
              || accountIds == null
              || accountIds.isEmpty()) {
            return;
          }
          List<TeamMemberWithUserInfo> members =
              TeamMemberCache.getTeamMembersFromCache(teamId, accountIds);
          String account = IMKitClient.account();
          List<TeamMemberWithUserInfo> filteredMembers = new ArrayList<>();
          for (TeamMemberWithUserInfo member : members) {
            if (member != null
                && !TextUtils.equals(member.getAccountId(), account)
                && !AIUserManager.isAIChatUser(member.getAccountId())
                && !containsLoadedMember(member.getAccountId())) {
              loadedTeamMembers.add(member);
              filteredMembers.add(member);
            }
          }
          if (!filteredMembers.isEmpty()) {
            aitDialog.addData(AitHelper.convertTeamMemberToAitUserInfo(filteredMembers));
          }
          refreshAIUsers(aitDialog, loadGeneration, AIUserManager.getAIChatUserList());
        }

        @Override
        public void onUsersRemoved(String teamId, List<String> accountIds) {
          if (!isDialogActive(teamId) || accountIds == null || accountIds.isEmpty()) {
            return;
          }
          for (int index = loadedTeamMembers.size() - 1; index >= 0; index--) {
            if (accountIds.contains(loadedTeamMembers.get(index).getAccountId())) {
              loadedTeamMembers.remove(index);
            }
          }
          refreshAIUsers(aitDialog, loadGeneration, AIUserManager.getAIChatUserList());
          refreshDialog(aitDialog);
        }
      };

  private final AIUserChangeListener aiUserChangeListener =
      aiUsers -> {
        if (!showTeamMember || !showAIUser || aitDialog == null || !aitDialog.isShowing()) {
          return;
        }
        refreshAIUsers(aitDialog, loadGeneration, aiUsers);
      };

  public AitManager(Context context, String teamId, AitContactSelectorFactory selectorFactory) {
    this.mContext = context;
    this.tid = teamId;
    this.selectorFactory = selectorFactory;
    atContactsModel = new AtContactsModel();
  }

  // 更新群信息
  public void updateTeamInfo(V2NIMTeam team) {
    this.showAll = ChatUtils.teamAllowAllMemberAt(team);
  }

  // 设置是否显示@所有成员
  public void setShowAll(boolean showAll) {
    this.showAll = showAll;
  }

  // 设置@文本输入监听
  public void setAitTextChangeListener(AitTextChangeListener listener) {
    this.aitTextChangeListener = listener;
  }

  // 获取群id
  public String getTid() {
    return tid;
  }

  public void setShowAIUser(boolean showAIUser) {
    this.showAIUser = showAIUser;
  }

  public void setShowTeamMember(boolean showTeamMember) {
    this.showTeamMember = showTeamMember;
  }

  // 获取@成员名称列表
  public List<String> getAitTeamMember() {
    List<String> aitMembers = atContactsModel.getAtTeamMember();
    for (String account : aitMembers) {
      if (TextUtils.equals(AtContactsModel.ACCOUNT_ALL, account)) {
        aitMembers.clear();
        aitMembers.add(AtContactsModel.ACCOUNT_ALL);
        return aitMembers;
      }
    }
    return aitMembers;
  }

  /**
   * 获取第一个AI成员
   *
   * @return AI账号ID
   */
  public String getFirstAIMember() {
    List<AitBlock> aitBlockList = atContactsModel.getAtBlockList();
    if (aitBlockList.size() == 0) {
      return null;
    }
    Collections.sort(
        aitBlockList, (o1, o2) -> o1.getFirstSegmentStart() - o2.getFirstSegmentStart());
    for (AitBlock aitBlock : aitBlockList) {
      if (!TextUtils.isEmpty(aitBlock.getAccountId())
          && AIUserManager.isAIUser(aitBlock.getAccountId())) {
        return aitBlock.getAccountId();
      }
    }
    return null;
  }

  // 重置
  public void reset() {
    atContactsModel.reset();
    ignoreTextChange = false;
    curPos = 0;
  }

  public void release() {
    if (teamMemberListenerRegistered) {
      TeamMemberCache.removeMemberChangedListener(tid, teamMemberCacheListener);
      teamMemberListenerRegistered = false;
    }
    if (aiUserListenerRegistered) {
      AIUserManager.removeAIUserChangeListener(aiUserChangeListener);
      aiUserListenerRegistered = false;
    }
    loadGeneration++;
    loadedTeamMembers.clear();
    displayAIUsers.clear();
    aitDialog = null;
    reset();
  }

  public void setIgnoreTextChange(boolean ignoreTextChange) {
    this.ignoreTextChange = ignoreTextChange;
  }

  // 设置@数据
  public void setAitContactsModel(AtContactsModel model) {
    if (model != null) {
      List<String> accountList = model.getAtTeamMember();
      for (String account : accountList) {
        atContactsModel.addAtBlock(account, model.getAtBlock(account));
      }
    }
  }

  // 获取@数据，转换为Json格式，放在消息体扩展字段中
  public JSONObject getAitData() {
    return atContactsModel.getBlockJson();
  }

  // 设置@数据，从消息体扩展字段中解析
  public AtContactsModel getAitContactsModel() {
    return atContactsModel;
  }

  @Override
  public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    delete = count > after;
  }

  @Override
  public void onTextChanged(CharSequence s, int start, int before, int count) {
    this.editTextStart = start;
    this.editTextCount = count;
    this.editTextBefore = before;
  }

  @Override
  public void afterTextChanged(Editable s) {
    afterTextChanged(s, editTextStart, delete ? editTextBefore : editTextCount, delete);
  }

  private void afterTextChanged(Editable editable, int start, int count, boolean delete) {
    curPos = delete ? start : count + start;
    if (ignoreTextChange || !IMKitConfigCenter.getEnableAtMessage()) {
      return;
    }
    if (delete) {
      int before = start + count;
      if (deleteSegment(before, count)) {
        return;
      }
      atContactsModel.onDeleteText(before, count);
    } else {
      if (count <= 0 || editable.length() < start + count) {
        return;
      }
      CharSequence s = editable.subSequence(start, start + count);
      // 输入@符号，拉起@成员选择器
      if (s.toString().equals("@") && !TextUtils.isEmpty(tid)) {
        if (aitDialog == null) {
          aitDialog = selectorFactory.create(mContext);
        }
        aitDialog.setOnItemListener(
            new AitContactSelector.ItemListener() {
              // 选择@成员
              @Override
              public void onSelect(AitUserInfo item) {
                if (item == null) {
                  // ait all
                  insertAitMemberInner(
                      AtContactsModel.ACCOUNT_ALL,
                      mContext.getString(R.string.chat_team_ait_all),
                      curPos,
                      false);
                } else {
                  insertAitMemberInner(item.getAccount(), item.getAitName(), curPos, false);
                }
              }

              // 加载更多
              @Override
              public void onLoadMore() {
                loadNextPage(aitDialog);
              }
            });
        if (!aitDialog.isShowing()) {
          aitDialog.show();
        }
        // 加载数据，则请求数据进行异步加载
        loadData(aitDialog);
      }
      atContactsModel.onInsertText(start, s.toString());
    }
  }

  // 加载数据，请求群成员列表
  public void loadData(AitContactSelector dialog) {
    aitDialog = dialog;
    dialog.setAllowAISearch(showTeamMember && showAIUser);
    long generation = ++loadGeneration;
    if (!showTeamMember) {
      if (dialog.isShowing()) {
        List<AitUserInfo> aitUsers =
            showAIUser
                ? AitHelper.convertAIUserToAitUserInfo(AIUserManager.getAIChatUserList())
                : new ArrayList<>();
        dialog.setData(aitUsers, true, false);
      }
      return;
    }
    if (!teamMemberListenerRegistered) {
      TeamMemberCache.ensureTeam(tid);
      TeamMemberCache.addMemberChangedListener(tid, teamMemberCacheListener);
      teamMemberListenerRegistered = true;
    }
    if (!aiUserListenerRegistered) {
      AIUserManager.addAIUserChangeListener(aiUserChangeListener);
      aiUserListenerRegistered = true;
    }
    loadedTeamMembers.clear();
    displayAIUsers.clear();
    pageLoading = false;
    pageFinished = false;
    if (showAIUser) {
      refreshAIUsers(dialog, generation, AIUserManager.getAIChatUserList());
    }
    // Load the first page immediately; subsequent pages are loaded by the list scroll callback.
    TeamMemberCache.getFirstTeamMemberPage(
        tid,
        new FetchCallback<TeamMemberListResult>() {
          @Override
          public void onError(int errorCode, @Nullable String errorMsg) {
            if (generation == loadGeneration) {
              pageFinished = false;
            }
          }

          @Override
          public void onSuccess(@Nullable TeamMemberListResult result) {
            if (generation != loadGeneration || !dialog.isShowing()) {
              return;
            }
            pageFinished = result == null || result.isFinished();
            loadedTeamMembers.clear();
            String account = IMKitClient.account();
            for (TeamMemberWithUserInfo member :
                result == null || result.getMemberList() == null
                    ? Collections.<TeamMemberWithUserInfo>emptyList()
                    : result.getMemberList()) {
              if (member != null
                  && !TextUtils.equals(member.getAccountId(), account)
                  && !AIUserManager.isAIChatUser(member.getAccountId())) {
                loadedTeamMembers.add(member);
              }
            }
            refreshDialog(dialog);
          }
        });
  }

  private void loadNextPage(AitContactSelector dialog) {
    if (dialog == null || !dialog.isShowing() || pageLoading || pageFinished || !showTeamMember) {
      return;
    }
    final long generation = loadGeneration;
    pageLoading = true;
    TeamMemberCache.getNextTeamMemberPage(
        tid,
        new FetchCallback<TeamMemberListResult>() {
          @Override
          public void onError(int errorCode, @Nullable String errorMsg) {
            if (generation == loadGeneration) {
              pageLoading = false;
              pageFinished = false;
            }
          }

          @Override
          public void onSuccess(@Nullable TeamMemberListResult result) {
            if (generation != loadGeneration || !dialog.isShowing()) {
              return;
            }
            pageLoading = false;
            pageFinished = result == null || result.isFinished();
            if (result == null) {
              return;
            }
            String account = IMKitClient.account();
            List<TeamMemberWithUserInfo> members = new ArrayList<>();
            for (TeamMemberWithUserInfo member :
                result.getMemberList() == null
                    ? Collections.<TeamMemberWithUserInfo>emptyList()
                    : result.getMemberList()) {
              if (member != null && !TextUtils.equals(member.getAccountId(), account)) {
                if (!AIUserManager.isAIChatUser(member.getAccountId())
                    && !containsLoadedMember(member.getAccountId())) {
                  loadedTeamMembers.add(member);
                  members.add(member);
                }
              }
            }
            dialog.addData(AitHelper.convertTeamMemberToAitUserInfo(members));
          }
        });
  }

  private boolean isDialogActive(String teamId) {
    return TextUtils.equals(tid, teamId)
        && showTeamMember
        && aitDialog != null
        && aitDialog.isShowing();
  }

  private boolean containsLoadedMember(String accountId) {
    for (TeamMemberWithUserInfo member : loadedTeamMembers) {
      if (member != null && TextUtils.equals(member.getAccountId(), accountId)) {
        return true;
      }
    }
    return false;
  }

  private void updateLoadedMembers(String teamId, List<String> accountIds) {
    List<TeamMemberWithUserInfo> members =
        TeamMemberCache.getTeamMembersFromCache(teamId, accountIds);
    for (TeamMemberWithUserInfo member : members) {
      if (member == null || AIUserManager.isAIChatUser(member.getAccountId())) {
        continue;
      }
      for (int index = 0; index < loadedTeamMembers.size(); index++) {
        if (TextUtils.equals(loadedTeamMembers.get(index).getAccountId(), member.getAccountId())) {
          loadedTeamMembers.set(index, member);
          break;
        }
      }
    }
  }

  private void refreshAIUsers(
      AitContactSelector dialog, long generation, List<? extends V2NIMAIUser> aiUsers) {
    if (!showAIUser || !showTeamMember || generation != loadGeneration || !isDialogActive(tid)) {
      return;
    }
    final List<V2NIMAIUser> sourceUsers = new ArrayList<>();
    if (aiUsers != null) {
      for (V2NIMAIUser aiUser : aiUsers) {
        if (aiUser != null && AIUserManager.isAIChatUser(aiUser.getAccountId())) {
          sourceUsers.add(aiUser);
        }
      }
    }
    displayAIUsers.clear();
    displayAIUsers.addAll(AitHelper.convertAIUserToAitUserInfo(sourceUsers));
    List<String> accountIds = new ArrayList<>();
    for (V2NIMAIUser aiUser : sourceUsers) {
      if (aiUser != null && !TextUtils.isEmpty(aiUser.getAccountId())) {
        accountIds.add(aiUser.getAccountId());
      }
    }
    if (accountIds.isEmpty()) {
      refreshDialog(dialog);
      return;
    }
    TeamMemberCache.getTeamMembers(
        tid,
        accountIds,
        new FetchCallback<List<TeamMemberWithUserInfo>>() {
          @Override
          public void onError(int errorCode, @Nullable String errorMsg) {
            if (generation != loadGeneration || !isDialogActive(tid)) {
              return;
            }
            refreshDialog(dialog);
          }

          @Override
          public void onSuccess(@Nullable List<TeamMemberWithUserInfo> members) {
            if (generation != loadGeneration || !isDialogActive(tid)) {
              return;
            }
            List<String> validAccounts = new ArrayList<>();
            for (TeamMemberWithUserInfo member :
                members == null ? Collections.<TeamMemberWithUserInfo>emptyList() : members) {
              if (member != null && !validAccounts.contains(member.getAccountId())) {
                validAccounts.add(member.getAccountId());
              }
            }
            for (AitUserInfo aiUser : displayAIUsers) {
              if (aiUser != null) {
                aiUser.setAISearchable(validAccounts.contains(aiUser.getAccount()));
              }
            }
            refreshDialog(dialog);
          }
        });
  }

  private void refreshDialog(AitContactSelector dialog) {
    if (dialog == null || !dialog.isShowing()) {
      return;
    }
    List<AitUserInfo> data = new ArrayList<>(displayAIUsers);
    data.addAll(AitHelper.convertTeamMemberToAitUserInfo(loadedTeamMembers));
    dialog.setData(data, true, canAtAll());
  }

  // 插入@成员
  public void insertReplyAit(String account, String name) {
    insertAitMemberInner(account, name, curPos, true);
  }

  private void insertAitMemberInner(
      String account, String name, int start, boolean needInsertAitInText) {
    name = name + " ";
    String content = needInsertAitInText ? "@" + name : name;
    ignoreTextChange = true;
    if (aitTextChangeListener != null) {
      aitTextChangeListener.onTextAdd(content, start, content.length(), needInsertAitInText);
    }
    ignoreTextChange = false;

    atContactsModel.onInsertText(start, content);

    int index = needInsertAitInText ? start : start - 1;
    atContactsModel.addAtMember(account, name, index);
  }

  private boolean deleteSegment(int start, int count) {
    if (count != 1) {
      return false;
    }
    boolean result = false;
    AitBlock.AitSegment segment = atContactsModel.findAtSegmentByEndPos(start);
    if (segment != null) {
      int length = start - segment.start;
      ignoreTextChange = true;
      if (aitTextChangeListener != null) {
        aitTextChangeListener.onTextDelete(segment.start, length);
      }
      ignoreTextChange = false;
      atContactsModel.onDeleteText(start, length);
      result = true;
    }
    return result;
  }

  private boolean canAtAll() {
    String account = IMKitClient.account();
    V2NIMTeamMember teamMember =
        TextUtils.isEmpty(account) ? null : TeamMemberCache.getTeamMember(tid, account);
    return showAll
        || (teamMember != null
            && teamMember.getMemberRole() == V2NIMTeamMemberRole.V2NIM_TEAM_MEMBER_ROLE_OWNER)
        || (teamMember != null
            && teamMember.getMemberRole() == V2NIMTeamMemberRole.V2NIM_TEAM_MEMBER_ROLE_MANAGER);
  }
}
