// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.conversationkit.ui.normal.page.viewmodel;

import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import com.netease.nimlib.sdk.v2.conversation.model.V2NIMConversation;
import com.netease.nimlib.sdk.v2.conversation.result.V2NIMConversationResult;
import com.netease.yunxin.kit.chatkit.repo.ConversationRepo;
import com.netease.yunxin.kit.common.ui.viewmodel.BaseViewModel;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.conversationkit.ui.IConversationFactory;
import com.netease.yunxin.kit.conversationkit.ui.model.ConversationBean;
import com.netease.yunxin.kit.conversationkit.ui.normal.ViewHolderFactory;
import com.netease.yunxin.kit.corekit.im2.extend.FetchCallback;
import java.util.ArrayList;
import java.util.List;

/** 会话列表选择器ViewModel */
public class SelectorViewModel extends BaseViewModel {
  private final String TAG = "SelectorViewModel";

  private final MutableLiveData<FetchResult<List<ConversationBean>>> queryLiveData =
      new MutableLiveData<>();
  private IConversationFactory conversationFactory = new ViewHolderFactory();
  private static final int PAGE_LIMIT = 50;
  private long pageOffSet = 0;
  private boolean hasMore = true;

  public MutableLiveData<FetchResult<List<ConversationBean>>> getQueryLiveData() {
    return queryLiveData;
  }

  public void getConversationData() {
    pageOffSet = 0;
    getConversation(pageOffSet);
  }

  public void loadMore() {
    getConversation(pageOffSet);
  }

  public void setConversationFactory(IConversationFactory factory) {
    this.conversationFactory = factory;
  }

  /**
   * 获取会话列表
   *
   * @param offset 分页偏移量
   */
  private void getConversation(long offset) {

    ConversationRepo.getConversationList(
        offset,
        PAGE_LIMIT,
        new FetchCallback<V2NIMConversationResult>() {
          @Override
          public void onError(int errorCode, @Nullable String errorMsg) {}

          @Override
          public void onSuccess(@Nullable V2NIMConversationResult data) {
            FetchResult<List<ConversationBean>> result = new FetchResult<>(LoadStatus.Success);
            if (data != null) {
              if (data.getConversationList() != null) {
                result.setData(createConversationBean(data.getConversationList()));
              }
              if (pageOffSet != 0) {
                result.setType(FetchResult.FetchType.Add);
              }
              hasMore = !data.isFinished();
              pageOffSet = data.getOffset();
            }
            queryLiveData.postValue(result);
          }
        });
  }

  //工具方法，将会话信息转换为会话列表数据
  public List<ConversationBean> createConversationBean(List<V2NIMConversation> data) {
    List<ConversationBean> resultData = new ArrayList<>();
    if (data != null) {
      for (int index = 0; index < data.size(); index++) {
        resultData.add(conversationFactory.CreateBean(data.get(index)));
      }
    }
    return resultData;
  }

  public boolean hasMore() {
    return hasMore;
  }
}
