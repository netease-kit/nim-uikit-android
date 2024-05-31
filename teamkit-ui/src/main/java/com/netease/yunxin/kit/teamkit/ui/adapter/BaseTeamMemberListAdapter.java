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
import java.util.Comparator;
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

  // 是否展示在线状态
  protected boolean showOnlineState = false;

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

  public void showOnlineState(boolean show) {
    this.showOnlineState = show;
  }

  // 获取选择框选中的数据
  public ArrayList<TeamMemberWithUserInfo> getSelectData() {
    return new ArrayList<>(selectData.values());
  }

  @Override
  public void onBindViewHolder(
      R binding, int position, TeamMemberWithUserInfo data, int bingingAdapterPosition) {}

  @Override
  public void setDataList(List<TeamMemberWithUserInfo> data) {
    super.setDataList(data);
    backupTotalData = new ArrayList<>(data);
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
      }
      Collections.sort(dataSource, comparator);
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
        selectData.remove(account);
        notifyItemRemoved(removeIndex);
      }
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
