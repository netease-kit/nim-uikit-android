// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.model;

import android.text.TextUtils;
import com.netease.nimlib.sdk.friend.model.AddFriendNotify;
import com.netease.yunxin.kit.corekit.im.model.SystemMessageInfo;
import com.netease.yunxin.kit.corekit.im.model.SystemMessageInfoStatus;
import com.netease.yunxin.kit.corekit.im.model.SystemMessageInfoType;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Contact data for verify data */
public class ContactVerifyInfoBean extends BaseContactBean {

  public SystemMessageInfo data;

  public List<SystemMessageInfo> messageList = new ArrayList<>();

  public ContactVerifyInfoBean(SystemMessageInfo data) {
    this.data = data;
    messageList.add(data);
    viewType = IViewTypeConstant.CONTACT_VERIFY_INFO;
  }

  public ContactVerifyInfoBean(SystemMessageInfo data, List<SystemMessageInfo> list) {
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
    for (SystemMessageInfo messageInfo : messageList) {
      if (messageInfo.getUnread()) {
        count++;
      }
    }
    return count;
  }

  public void clearUnreadCount() {
    for (SystemMessageInfo messageInfo : messageList) {
      if (messageInfo.getUnread()) {
        messageInfo.setUnread(false);
      }
    }
  }

  public void updateStatus(SystemMessageInfoStatus status) {
    for (SystemMessageInfo messageInfo : messageList) {
      messageInfo.setInfoStatus(status);
      messageInfo.setUnread(false);
    }
  }

  public boolean pushMessageIfSame(SystemMessageInfo messageInfo) {
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

  public boolean isSameMessage(SystemMessageInfo messageInfo) {
    if (messageInfo == null) {
      return false;
    }

    if (data.getInfoType() == messageInfo.getInfoType()
        && TextUtils.equals(data.getFromAccount(), messageInfo.getFromAccount())
        && TextUtils.equals(data.getTargetId(), messageInfo.getTargetId())
        && data.getInfoStatus() == messageInfo.getInfoStatus()) {

      if (messageInfo.getInfoType() == SystemMessageInfoType.AddFriend
          && messageInfo.getAttachObject() instanceof AddFriendNotify) {

        if (!(data.getAttachObject() instanceof AddFriendNotify)) {
          return false;
        }
        AddFriendNotify notify = (AddFriendNotify) data.getAttachObject();
        AddFriendNotify notifyInfo = (AddFriendNotify) messageInfo.getAttachObject();
        if (notify.getEvent() == notifyInfo.getEvent()
            && TextUtils.equals(notify.getAccount(), notifyInfo.getAccount())) {
          return true;
        } else {
          return false;
        }
      }
      return true;
    }
    return false;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ContactVerifyInfoBean)) return false;
    ContactVerifyInfoBean infoBean = (ContactVerifyInfoBean) o;
    return isSameMessage(infoBean.data);
  }

  @Override
  public int hashCode() {
    return Objects.hash(data);
  }
}
