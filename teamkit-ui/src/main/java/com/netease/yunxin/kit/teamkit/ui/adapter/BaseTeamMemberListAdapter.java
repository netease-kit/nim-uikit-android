// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.teamkit.ui.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import androidx.viewbinding.ViewBinding;
import com.netease.nimlib.sdk.team.constant.TeamMemberType;
import com.netease.nimlib.sdk.team.constant.TeamTypeEnum;
import com.netease.yunxin.kit.chatkit.model.UserInfoWithTeam;
import com.netease.yunxin.kit.teamkit.ui.utils.FilterUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class BaseTeamMemberListAdapter<R extends ViewBinding>
    extends TeamCommonAdapter<UserInfoWithTeam, R> {
  public static final String ACTION_REMOVE = "member_remove";
  public static final String ACTION_CHECK = "member_check";
  public static final String ACTION_UNCHECK = "member_uncheck";
  protected final TeamTypeEnum teamTypeEnum;
  protected List<UserInfoWithTeam> backupTotalData;
  protected Map<String, UserInfoWithTeam> selectData = new ConcurrentHashMap<>();

  //是否展示身份标签
  protected boolean showGroupIdentify = false;

  protected TeamMemberType showRemoveTagTeamMemberType = null;

  protected boolean showSelect = false;

  protected ItemClickListener itemClickListener;

  public BaseTeamMemberListAdapter(
      Context context, TeamTypeEnum teamTypeEnum, Class<R> viewBinding) {
    super(context, viewBinding);
    this.teamTypeEnum = teamTypeEnum;
  }

  public void setGroupIdentify(boolean identify) {
    showGroupIdentify = identify;
  }

  public void setShowRemoveTagWithMemberType(TeamMemberType type) {
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

  public ArrayList<UserInfoWithTeam> getSelectData() {
    return new ArrayList<>(selectData.values());
  }

  @Override
  public void onBindViewHolder(
      R binding, int position, UserInfoWithTeam data, int bingingAdapterPosition) {}

  @Override
  public void addDataList(List<UserInfoWithTeam> data, boolean clearOld) {
    super.addDataList(data, clearOld);
    backupTotalData = new ArrayList<>(data);
    selectData.clear();
  }

  /**
   * 更新列表数据，并保留当前选中状态
   *
   * @param data
   */
  public void setDataAndSaveSelect(List<UserInfoWithTeam> data) {
    Set<String> userAccounts = new HashSet<>();
    if (data != null && data.size() > 0) {
      for (UserInfoWithTeam userInfoWithTeam : data) {
        userAccounts.add(userInfoWithTeam.getUserInfo().getAccount());
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

    List<UserInfoWithTeam> filterResult =
        FilterUtils.filter(
            backupTotalData,
            userInfoWithTeam -> {
              boolean nameContains = userInfoWithTeam.getName().contains(sequence);
              if (nameContains) {
                userInfoWithTeam.setSearchPoint(userInfoWithTeam.getName().length());
                return true;
              }

              boolean accIdContains =
                  userInfoWithTeam.getTeamInfo().getAccount().contains(sequence);
              if (accIdContains) {
                userInfoWithTeam.setSearchPoint(
                    100 + userInfoWithTeam.getTeamInfo().getAccount().length());
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
  protected void updateDataAndNotify(List<UserInfoWithTeam> list) {
    dataSource.clear();
    dataSource.addAll(list);
    notifyDataSetChanged();
  }

  protected boolean needShowRemoveTag(UserInfoWithTeam data) {
    if (showRemoveTagTeamMemberType == null) {
      return false;
    } else if (showRemoveTagTeamMemberType == TeamMemberType.Owner) {
      return true;
    } else if (showRemoveTagTeamMemberType == TeamMemberType.Manager) {
      return data.getTeamInfo().getType() == TeamMemberType.Normal
          || data.getTeamInfo().getType() == TeamMemberType.Manager;
    } else if (showRemoveTagTeamMemberType == TeamMemberType.Normal) {
      return data.getTeamInfo().getType() == TeamMemberType.Normal;
    }
    return false;
  }

  public static interface ItemClickListener {
    void onActionClick(String action, View view, UserInfoWithTeam data, int position);
  }
}
