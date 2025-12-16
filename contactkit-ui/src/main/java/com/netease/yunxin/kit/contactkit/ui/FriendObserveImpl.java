// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui;

import androidx.annotation.NonNull;
import com.netease.yunxin.kit.corekit.coexist.im2.listener.ContactChangeType;
import com.netease.yunxin.kit.corekit.coexist.im2.listener.ContactListener;
import com.netease.yunxin.kit.corekit.coexist.im2.model.FriendAddApplicationInfo;
import com.netease.yunxin.kit.corekit.coexist.im2.model.UserWithFriend;
import java.util.List;

public class FriendObserveImpl implements ContactListener {
  @Override
  public void onContactChange(
      @NonNull ContactChangeType changeType, @NonNull List<? extends UserWithFriend> contactList) {}

  @Override
  public void onFriendAddApplication(@NonNull FriendAddApplicationInfo friendApplication) {}

  @Override
  public void onFriendAddRejected(@NonNull FriendAddApplicationInfo rejectionInfo) {}
}
