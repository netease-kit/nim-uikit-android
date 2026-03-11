// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.page.viewmodel;

import static com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant.LIB_TAG;

import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChatSearchMemberViewModel extends ChatSearchViewModel {
  private static final String TAG = "ChatSearchMemberViewModel";
  // 搜索发送人
  protected String accountId;

  public void searchMessageBySender(String senderAccountIds) {
    ALog.d(LIB_TAG, TAG, "searchLocalMessages:" + senderAccountIds);
    // 发送人改变，清空分页参数
    accountId = null;
    clearSearchMessages();
    boolean hasSenderFilter = senderAccountIds != null && !senderAccountIds.isEmpty();
    // 没有搜索关键词和发送人过滤条件，直接返回空列表，页面清理上次搜索结果
    if (!hasSenderFilter) {
      FetchResult<List<ChatMessageBean>> result = new FetchResult<>(LoadStatus.Success);
      result.setData(new ArrayList<>());
      searchMessagesLiveData.postValue(result);
      return;
    }
    accountId = senderAccountIds;
    searchMessage(null, Collections.singletonList(accountId));
  }

  public void searchNextPageBySender() {
    searchMessage(null, Collections.singletonList(accountId));
  }
}
