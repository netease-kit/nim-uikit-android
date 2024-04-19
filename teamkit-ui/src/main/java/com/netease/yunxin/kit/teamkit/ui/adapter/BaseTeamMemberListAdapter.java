// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.teamkit.ui.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import androidx.viewbinding.ViewBinding;
import com.netease.nimlib.sdk.v2.team.enums.V2NIMTeamMemberRole;
import com.netease.nimlib.sdk.v2.team.enums.V2NIMTeamType;
import com.netease.yunxin.kit.chatkit.model.TeamMemberWithUserInfo;
import com.netease.yunxin.kit.teamkit.ui.utils.FilterUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 群成员列表适配器
 *
 * @param <R>
 */
public class BaseTeamMemberListAdapter<R extends ViewBinding>
    extends TeamCommonAdapter<TeamMemberWithUserInfo, R> {
  public static final String ACTION_REMOVE = "member_remove";
  public static final String ACTION_CHECK = "member_check";
  public static final String ACTION_UNCHECK = "member_uncheck";
  protected final V2NIMTeamType teamTypeEnum;
  protected List<TeamMemberWithUserInfo> backupTotalData;
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

  public BaseTeamMemberListAdapter(
      Context context, V2NIMTeamType teamTypeEnum, Class<R> viewBinding) {
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

  // 获取选择框选中的数据
  public ArrayList<TeamMemberWithUserInfo> getSelectData() {
    return new ArrayList<>(selectData.values());
  }

  @Override
  public void onBindViewHolder(
      R binding, int position, TeamMemberWithUserInfo data, int bingingAdapterPosition) {}

  @Override
  public void addDataList(List<TeamMemberWithUserInfo> data, boolean clearOld) {
    super.addDataList(data, clearOld);
    backupTotalData = new ArrayList<>(data);
    selectData.clear();
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
    super.addDataList(data, true);
    backupTotalData = new ArrayList<>(data);
  }

  public void filter(CharSequence sequence) {
    if (TextUtils.isEmpty(sequence)) {
      updateDataAndNotify(backupTotalData);
      return;
    }

    List<TeamMemberWithUserInfo> filterResult =
        FilterUtils.filter(
            backupTotalData,
            userInfoWithTeam -> {
              boolean nameContains = userInfoWithTeam.getName().contains(sequence);
              if (nameContains) {
                userInfoWithTeam.setSearchPoint(userInfoWithTeam.getName().length());
                return true;
              }

              boolean accIdContains = userInfoWithTeam.getAccountId().contains(sequence);
              if (accIdContains) {
                userInfoWithTeam.setSearchPoint(100 + userInfoWithTeam.getAccountId().length());
                return true;
              }
              return false;
            });
    Collections.sort(
        filterResult,
        (o1, o2) -> {
          if (o1 == o2) {
            return 0;
          }
          if (o1 == null) {
            return 1;
          }
          if (o2 == null) {
            return -1;
          }
          return o1.getSearchPoint() - o2.getSearchPoint();
        });
    updateDataAndNotify(filterResult);
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
