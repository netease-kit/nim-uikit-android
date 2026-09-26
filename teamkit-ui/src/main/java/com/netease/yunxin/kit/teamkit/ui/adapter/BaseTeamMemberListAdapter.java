// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.teamkit.ui.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import androidx.core.content.ContextCompat;
import androidx.viewbinding.ViewBinding;
import com.netease.nimlib.sdk.search.model.RecordHitInfo;
import com.netease.nimlib.sdk.v2.team.enums.V2NIMTeamMemberRole;
import com.netease.nimlib.sdk.v2.team.enums.V2NIMTeamType;
import com.netease.yunxin.kit.chatkit.model.HitType;
import com.netease.yunxin.kit.chatkit.model.TeamMemberWithUserInfo;
import com.netease.yunxin.kit.chatkit.utils.SearchEngine;
import com.netease.yunxin.kit.teamkit.ui.R;
import com.netease.yunxin.kit.teamkit.ui.utils.FilterUtils;
import com.netease.yunxin.kit.teamkit.ui.utils.TeamUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 群成员列表适配器
 *
 * @param <B> member item view binding type
 */
public class BaseTeamMemberListAdapter<B extends ViewBinding>
    extends TeamCommonAdapter<TeamMemberWithUserInfo, B> {
  public static final String ACTION_REMOVE = "member_remove";
  public static final String ACTION_CHECK = "member_check";
  public static final String ACTION_UNCHECK = "member_uncheck";
  protected final V2NIMTeamType teamTypeEnum;
  protected List<TeamMemberWithUserInfo> backupTotalData;
  private final Map<String, MemberSearchResult> searchResults = new ConcurrentHashMap<>();
  private final SearchEngine searchEngine = new SearchEngine();
  // 列表选择框选中的数据
  protected Map<String, TeamMemberWithUserInfo> selectData = new ConcurrentHashMap<>();

  //是否展示身份标签
  protected boolean showGroupIdentify = false;

  // 群成员权限信息
  protected V2NIMTeamMemberRole showRemoveTagTeamMemberType = null;

  // 是否展示选择框
  protected boolean showSelect = false;

  // 点击事件
  protected ItemClickListener itemClickListener;

  // 是否展示在线状态
  protected boolean showOnlineState = false;

  public BaseTeamMemberListAdapter(
      Context context, V2NIMTeamType teamTypeEnum, Class<B> viewBinding) {
    super(context, viewBinding);
    this.teamTypeEnum = teamTypeEnum;
  }

  // 是否展示身份标签（群主、管理员）
  public void setGroupIdentify(boolean identify) {
    showGroupIdentify = identify;
  }

  // 设置展示身份标签的群成员类型，详见{@link #needShowRemoveTag()}方法
  public void setShowRemoveTagWithMemberType(V2NIMTeamMemberRole type) {
    this.showRemoveTagTeamMemberType = type;
    if (dataSource.size() > 0) {
      notifyDataSetChanged();
    }
  }

  public void showSelect(boolean show) {
    this.showSelect = show;
  }

  public void setItemClickListener(ItemClickListener itemClickListener) {
    this.itemClickListener = itemClickListener;
  }

  public void showOnlineState(boolean show) {
    this.showOnlineState = show;
  }

  // 获取选择框选中的数据
  public ArrayList<TeamMemberWithUserInfo> getSelectData() {
    return new ArrayList<>(selectData.values());
  }

  @Override
  public void onBindViewHolder(
      B binding, int position, TeamMemberWithUserInfo data, int bingingAdapterPosition) {}

  @Override
  public void setDataList(List<TeamMemberWithUserInfo> data) {
    super.setDataList(data);
    backupTotalData = data == null ? new ArrayList<>() : new ArrayList<>(data);
    searchResults.clear();
    selectData.clear();
  }

  @Override
  public void addData(
      List<TeamMemberWithUserInfo> dataList, Comparator<TeamMemberWithUserInfo> comparator) {
    super.addData(dataList, comparator);
    if (backupTotalData == null) {
      backupTotalData = new ArrayList<>();
    }
    backupTotalData.addAll(dataList);
  }

  public void updateData(List<TeamMemberWithUserInfo> data) {
    if (data == null) {
      return;
    }
    for (TeamMemberWithUserInfo user : data) {
      String userAccount = user.getAccountId();
      for (int i = 0; i < dataSource.size(); i++) {
        if (dataSource.get(i).getAccountId().equals(userAccount)) {
          dataSource.set(i, user);
          notifyItemChanged(i);
        }
      }
      // 更新backupTotalData
      for (int i = 0; i < backupTotalData.size(); i++) {
        if (backupTotalData.get(i).getAccountId().equals(userAccount)) {
          backupTotalData.set(i, user);
        }
      }
    }
  }

  public void updateDataWithComparator(
      List<TeamMemberWithUserInfo> data, Comparator<TeamMemberWithUserInfo> comparator) {
    if (comparator == null) {
      this.updateData(data);
    } else {
      for (TeamMemberWithUserInfo user : data) {
        String userAccount = user.getAccountId();
        for (int i = 0; i < dataSource.size(); i++) {
          if (dataSource.get(i).getAccountId().equals(userAccount)) {
            dataSource.set(i, user);
          }
        }
        // 更新backupTotalData
        for (int i = 0; i < backupTotalData.size(); i++) {
          if (backupTotalData.get(i).getAccountId().equals(userAccount)) {
            backupTotalData.set(i, user);
          }
        }
      }
      Collections.sort(dataSource, comparator);
      Collections.sort(backupTotalData, comparator);
      notifyDataSetChanged();
    }
  }

  @Override
  public void removeData(List<String> accountList) {
    if (accountList == null || accountList.isEmpty()) {
      return;
    }
    for (String account : accountList) {
      TeamMemberWithUserInfo removeData = null;
      int removeIndex = -1;
      for (int index = 0; index < dataSource.size(); index++) {
        if (dataSource.get(index).getAccountId().equals(account)) {
          removeData = dataSource.get(index);
          removeIndex = index;
          break;
        }
      }
      if (removeData != null && removeIndex != -1) {
        dataSource.remove(removeData);
        notifyItemRemoved(removeIndex);
      }
      if (backupTotalData != null) {
        for (int index = backupTotalData.size() - 1; index >= 0; index--) {
          if (backupTotalData.get(index).getAccountId().equals(account)) {
            backupTotalData.remove(index);
          }
        }
      }
      searchResults.remove(account);
      selectData.remove(account);
    }
  }

  /**
   * 更新列表数据，并保留当前选中状态
   *
   * @param data 列表数据
   */
  public void setDataAndSaveSelect(List<TeamMemberWithUserInfo> data) {
    Set<String> userAccounts = new HashSet<>();
    if (data != null && data.size() > 0) {
      for (TeamMemberWithUserInfo userInfoWithTeam : data) {
        userAccounts.add(userInfoWithTeam.getAccountId());
      }
    }
    if (selectData != null && selectData.size() > 0) {
      for (String account : selectData.keySet()) {
        if (!userAccounts.contains(account)) {
          selectData.remove(account);
        }
      }
    }
    super.setDataList(data);
    backupTotalData = data == null ? new ArrayList<>() : new ArrayList<>(data);
    searchResults.clear();
  }

  public void filter(CharSequence sequence) {
    String query = sequence == null ? "" : sequence.toString().trim();
    if (TextUtils.isEmpty(query)) {
      searchResults.clear();
      updateDataAndNotify(backupTotalData);
      return;
    }

    searchResults.clear();
    List<TeamMemberWithUserInfo> filterResult =
        FilterUtils.filter(
            backupTotalData,
            userInfoWithTeam -> {
              MemberSearchResult result = findSearchResult(userInfoWithTeam, query);
              if (result == null) {
                return false;
              }
              searchResults.put(userInfoWithTeam.getAccountId(), result);
              return true;
            });
    Collections.sort(filterResult, TeamUtils.teamManagerComparator());
    updateDataAndNotify(filterResult);
  }

  private MemberSearchResult findSearchResult(TeamMemberWithUserInfo member, String query) {
    if (member == null || TextUtils.isEmpty(query)) {
      return null;
    }
    MemberSearchResult result =
        match(
            member.getFriendInfo() == null ? null : member.getFriendInfo().getAlias(),
            query,
            HitType.Alias);
    if (result != null) {
      return result;
    }
    result =
        match(
            member.getTeamMember() == null ? null : member.getTeamMember().getTeamNick(),
            query,
            HitType.TeamName);
    if (result != null) {
      return result;
    }
    result =
        match(
            member.getUserInfo() == null ? null : member.getUserInfo().getName(),
            query,
            HitType.UserName);
    if (result != null) {
      return result;
    }
    return match(member.getAccountId(), query, HitType.Account);
  }

  private MemberSearchResult match(String value, String query, HitType hitType) {
    if (TextUtils.isEmpty(value)) {
      return null;
    }
    RecordHitInfo hitInfo = searchEngine.searchTextIgnoreCase(value, query);
    return hitInfo == null ? null : new MemberSearchResult(hitType, hitInfo);
  }

  protected MemberSearchResult getSearchResult(TeamMemberWithUserInfo member) {
    return member == null ? null : searchResults.get(member.getAccountId());
  }

  protected int getSearchHighlightColor() {
    return ContextCompat.getColor(context, R.color.color_337eff);
  }

  protected String getPrimaryName(TeamMemberWithUserInfo data) {
    return data == null ? "" : data.getName();
  }

  protected String getSecondaryName(TeamMemberWithUserInfo data, MemberSearchResult result) {
    if (data == null || result == null) {
      return null;
    }
    if (result.hitType == HitType.TeamName
        && data.getTeamMember() != null
        && !TextUtils.isEmpty(
            data.getFriendInfo() == null ? null : data.getFriendInfo().getAlias())) {
      return data.getTeamMember().getTeamNick();
    }
    if (result.hitType == HitType.UserName) {
      if (!TextUtils.isEmpty(data.getFriendInfo() == null ? null : data.getFriendInfo().getAlias())
          || (data.getTeamMember() != null
              && !TextUtils.isEmpty(data.getTeamMember().getTeamNick()))) {
        return data.getUserInfo() == null ? null : data.getUserInfo().getName();
      }
    }
    if (result.hitType == HitType.Account
        && !TextUtils.equals(data.getName(), data.getAccountId())) {
      return data.getAccountId();
    }
    return null;
  }

  protected String getHighlightedName(TeamMemberWithUserInfo data, MemberSearchResult result) {
    if (data == null || result == null) {
      return null;
    }
    if (result.hitType == HitType.Alias) {
      return data.getFriendInfo() == null ? null : data.getFriendInfo().getAlias();
    }
    if (result.hitType == HitType.TeamName) {
      return data.getTeamMember() == null ? null : data.getTeamMember().getTeamNick();
    }
    if (result.hitType == HitType.UserName) {
      return data.getUserInfo() == null ? null : data.getUserInfo().getName();
    }
    return data.getAccountId();
  }

  protected RecordHitInfo getHitInfo(MemberSearchResult result) {
    return result == null ? null : result.hitInfo;
  }

  protected static class MemberSearchResult {
    final HitType hitType;
    final RecordHitInfo hitInfo;

    MemberSearchResult(HitType hitType, RecordHitInfo hitInfo) {
      this.hitType = hitType;
      this.hitInfo = hitInfo;
    }
  }

  @SuppressLint("NotifyDataSetChanged")
  protected void updateDataAndNotify(List<TeamMemberWithUserInfo> list) {
    dataSource.clear();
    dataSource.addAll(list);
    notifyDataSetChanged();
  }

  // 是否展示身份标签（群主、管理员）
  protected boolean needShowRemoveTag(TeamMemberWithUserInfo data) {
    if (showRemoveTagTeamMemberType == null) {
      return false;
    } else if (showRemoveTagTeamMemberType == V2NIMTeamMemberRole.V2NIM_TEAM_MEMBER_ROLE_OWNER) {
      return true;
    } else if (showRemoveTagTeamMemberType == V2NIMTeamMemberRole.V2NIM_TEAM_MEMBER_ROLE_MANAGER) {
      return data.getMemberRole() == V2NIMTeamMemberRole.V2NIM_TEAM_MEMBER_ROLE_NORMAL
          || data.getMemberRole() == V2NIMTeamMemberRole.V2NIM_TEAM_MEMBER_ROLE_MANAGER;
    } else if (showRemoveTagTeamMemberType == V2NIMTeamMemberRole.V2NIM_TEAM_MEMBER_ROLE_NORMAL) {
      return data.getMemberRole() == V2NIMTeamMemberRole.V2NIM_TEAM_MEMBER_ROLE_NORMAL;
    }
    return false;
  }

  public static interface ItemClickListener {
    void onActionClick(String action, View view, TeamMemberWithUserInfo data, int position);
  }
}
