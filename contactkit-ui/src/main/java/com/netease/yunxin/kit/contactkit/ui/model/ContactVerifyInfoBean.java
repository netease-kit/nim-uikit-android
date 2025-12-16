// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.model;

import android.text.TextUtils;
import com.netease.nimlib.coexist.sdk.v2.friend.enums.V2NIMFriendAddApplicationStatus;
import com.netease.yunxin.kit.corekit.coexist.im2.model.FriendAddApplicationInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Contact data for verify data */
public class ContactVerifyInfoBean extends BaseContactBean {

  public FriendAddApplicationInfo data;

  public List<FriendAddApplicationInfo> messageList = new ArrayList<>();

  public ContactVerifyInfoBean(FriendAddApplicationInfo data) {
    this.data = data;
    messageList.add(data);
    viewType = IViewTypeConstant.CONTACT_VERIFY_INFO;
  }

  public ContactVerifyInfoBean(FriendAddApplicationInfo data, List<FriendAddApplicationInfo> list) {
    this.data = data;
    messageList.clear();
    messageList.add(data);
    if (list != null) {
      messageList.addAll(list);
    }
    viewType = IViewTypeConstant.CONTACT_VERIFY_INFO;
  }

  public int getUnreadCount() {
    int count = 0;
    for (FriendAddApplicationInfo messageInfo : messageList) {
      if (messageInfo.getUnread()) {
        count++;
      }
    }
    return count;
  }

  public void clearUnreadCount() {
    for (FriendAddApplicationInfo messageInfo : messageList) {
      if (messageInfo.getUnread()) {
        messageInfo.setUnread(false);
      }
    }
  }

  public void updateStatus(V2NIMFriendAddApplicationStatus status) {
    for (FriendAddApplicationInfo messageInfo : messageList) {
      messageInfo.setStatus(status);
      messageInfo.setUnread(false);
    }
  }

  public boolean pushMessageIfSame(FriendAddApplicationInfo messageInfo) {
    if (isSameMessage(messageInfo)) {
      if (data.getTime() < messageInfo.getTime()) {
        data = messageInfo;
      }
      messageList.add(messageInfo);
      return true;
    }

    return false;
  }

  @Override
  public boolean isShowDivision() {
    return false;
  }

  @Override
  public String getTarget() {
    return null;
  }

  public boolean isSameMessage(FriendAddApplicationInfo messageInfo) {
    if (messageInfo == null) {
      return false;
    }
    return TextUtils.equals(data.getApplicantAccountId(), messageInfo.getApplicantAccountId())
        && TextUtils.equals(data.getRecipientAccountId(), messageInfo.getRecipientAccountId())
        && data.getStatus() == messageInfo.getStatus();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ContactVerifyInfoBean)) {
      return false;
    }
    return isSameMessage(((ContactVerifyInfoBean) o).data);
  }

  @Override
  public int hashCode() {
    return Objects.hash(data);
  }
}
