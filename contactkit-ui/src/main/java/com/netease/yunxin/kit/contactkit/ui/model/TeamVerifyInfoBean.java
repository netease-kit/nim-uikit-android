// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.model;

import android.text.TextUtils;
import com.netease.nimlib.coexist.sdk.v2.team.enums.V2NIMTeamJoinActionStatus;
import com.netease.nimlib.coexist.sdk.v2.team.enums.V2NIMTeamJoinActionType;
import com.netease.nimlib.coexist.sdk.v2.team.model.V2NIMTeam;
import com.netease.nimlib.coexist.sdk.v2.team.model.V2NIMTeamJoinActionInfo;
import com.netease.nimlib.coexist.sdk.v2.user.V2NIMUser;
import com.netease.yunxin.kit.alog.ALog;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Contact data for verify data */
public class TeamVerifyInfoBean extends BaseContactBean {

  public V2NIMTeamJoinActionInfo actionInfo;
  public List<V2NIMTeamJoinActionInfo> actionInfoList = new ArrayList<>();
  public V2NIMTeam joinTeam;
  public V2NIMUser operatorUser;
  public long readTimeStamp;

  public V2NIMTeamJoinActionStatus operateStatus;

  public TeamVerifyInfoBean(V2NIMTeamJoinActionInfo data) {
    this.actionInfo = data;
    actionInfoList.add(data);
    viewType = IViewTypeConstant.CONTACT_TEAM_VERIFY_INFO;
  }

  public TeamVerifyInfoBean(V2NIMTeamJoinActionInfo data, long readTime) {
    this.actionInfo = data;
    actionInfoList.add(data);
    viewType = IViewTypeConstant.CONTACT_TEAM_VERIFY_INFO;
    readTimeStamp = readTime;
  }

  public boolean pushMessageIfSame(V2NIMTeamJoinActionInfo info) {
    if (isSameMessage(info)) {
      if (actionInfo.getTimestamp() < info.getTimestamp()) {
        actionInfo = info;
      }
      actionInfoList.add(info);
      return true;
    }

    return false;
  }

  public void setReadTimeStamp(long readTime) {
    this.readTimeStamp = readTime;
  }

  public int getUnreadCount() {
    int count = 0;
    for (V2NIMTeamJoinActionInfo action : actionInfoList) {
      ALog.d(
          "TeamVerifyInfoBean",
          "getUnreadCount",
          "action:"
              + actionInfo.getActionType()
              + ",status:"
              + actionInfo.getActionStatus()
              + ",Timestamp:"
              + actionInfo.getTimestamp()
              + "readTime:"
              + readTimeStamp);
      if (action.getTimestamp() > readTimeStamp
          && getActionStatus() == V2NIMTeamJoinActionStatus.V2NIM_TEAM_JOIN_ACTION_STATUS_INIT) {
        ALog.d("TeamVerifyInfoBean", "getUnreadCount +1");
        count++;
      }
    }
    return count;
  }

  public void setJoinTeam(V2NIMTeam team) {
    this.joinTeam = team;
  }

  public void setOperatorUser(V2NIMUser userInfo) {
    this.operatorUser = userInfo;
  }

  public V2NIMTeamJoinActionType getActionType() {
    return actionInfo.getActionType();
  }

  public void setActionStatus(V2NIMTeamJoinActionStatus status) {
    this.operateStatus = status;
  }

  public V2NIMTeamJoinActionStatus getActionStatus() {
    if (operateStatus != null) {
      return operateStatus;
    }
    return actionInfo.getActionStatus();
  }

  public String getTeamName() {
    if (joinTeam != null) {
      return joinTeam.getName();
    } else {
      return actionInfo.getTeamId();
    }
  }

  public String getTeamAvatar() {
    if (joinTeam != null) {
      return joinTeam.getAvatar();
    } else {
      return actionInfo.getTeamId();
    }
  }

  public String getTeamId() {
    return actionInfo.getTeamId();
  }

  public String getOperateAccountId() {
    return actionInfo.getOperatorAccountId();
  }

  public long getCreateTimestamp() {
    return actionInfo.getTimestamp();
  }

  public String getOperatorUserName() {
    if (operatorUser != null) {
      return operatorUser.getName();
    } else {
      return actionInfo.getOperatorAccountId();
    }
  }

  public String getOperatorAvatar() {
    if (operatorUser != null) {
      return operatorUser.getAvatar();
    } else {
      return actionInfo.getOperatorAccountId();
    }
  }

  @Override
  public boolean isShowDivision() {
    return false;
  }

  @Override
  public String getAccountId() {
    return actionInfo.getTeamId();
  }

  @Override
  public String getTarget() {
    return null;
  }

  public boolean isSameMessage(V2NIMTeamJoinActionInfo messageInfo) {
    if (messageInfo == null) {
      return false;
    }
    return TextUtils.equals(actionInfo.getTeamId(), messageInfo.getTeamId())
        && TextUtils.equals(actionInfo.getOperatorAccountId(), messageInfo.getOperatorAccountId())
        && actionInfo.getActionType() == messageInfo.getActionType()
        && actionInfo.getActionStatus() == messageInfo.getActionStatus()
        // 拒绝操作之后只能修改本地数据
        && getActionStatus() == messageInfo.getActionStatus();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof TeamVerifyInfoBean)) {
      return false;
    }
    return isSameMessage(((TeamVerifyInfoBean) o).actionInfo);
  }

  @Override
  public int hashCode() {
    return Objects.hash(actionInfo);
  }
}
