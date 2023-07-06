// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.model;

import com.netease.yunxin.kit.chatkit.model.FriendSearchInfo;
import com.netease.yunxin.kit.common.ui.viewholder.BaseBean;
import com.netease.yunxin.kit.contactkit.ui.ContactConstant;
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant;
import java.util.Objects;

public class SearchFriendBean extends BaseBean {
  private static final String TAG = "SearchFriendBean";
  public FriendSearchInfo friendSearchInfo;

  public SearchFriendBean(FriendSearchInfo searchInfo, String router) {
    this.friendSearchInfo = searchInfo;
    this.viewType = ContactConstant.SearchViewType.USER;
    this.router = router;
    this.paramKey = RouterConstant.CHAT_KRY;
    this.param = searchInfo.getFriendInfo().getUserInfo();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof SearchFriendBean)) return false;
    SearchFriendBean that = (SearchFriendBean) o;
    return Objects.equals(
        friendSearchInfo.getFriendInfo().getAccount(),
        that.friendSearchInfo.getFriendInfo().getAccount());
  }

  @Override
  public int hashCode() {
    return Objects.hash(friendSearchInfo.getFriendInfo().getAccount());
  }
}
