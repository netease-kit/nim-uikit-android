// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.page.viewmodel;

import android.text.TextUtils;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import java.util.ArrayList;
import java.util.List;

public class ChatSearchKeywordViewModel extends ChatSearchViewModel {
  private static final String TAG = "ChatSearchKeywordViewModel";

  // 搜索关键词
  protected String keywords;

  public void searchMessageByKeyword(String word) {
    // 关键词改变，清空分页参数
    clearSearchMessages();
    this.keywords = word;
    // 没有搜索关键词和发送人过滤条件，直接返回空列表，页面清理上次搜索结果
    if (TextUtils.isEmpty(word)) {
      FetchResult<List<ChatMessageBean>> result = new FetchResult<>(LoadStatus.Success);
      result.setData(new ArrayList<>());
      searchMessagesLiveData.postValue(result);
      return;
    }
    searchMessage(word, null);
  }

  public void searchNextPageByKeyword() {
    if (!TextUtils.isEmpty(keywords)) {
      searchMessage(keywords, null);
    }
  }

  public String getKeywords() {
    return keywords;
  }
}
