/*
 * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.kit.conversationkit.ui.page.viewmodel;

import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;

import com.netease.yunxin.kit.common.ui.viewmodel.BaseViewModel;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.conversationkit.model.ConversationInfo;
import com.netease.yunxin.kit.conversationkit.repo.ConversationRepo;
import com.netease.yunxin.kit.conversationkit.ui.IConversationFactory;
import com.netease.yunxin.kit.conversationkit.ui.model.ConversationBean;
import com.netease.yunxin.kit.conversationkit.ui.common.XLog;
import com.netease.yunxin.kit.conversationkit.ui.page.DefaultViewHolderFactory;
import com.netease.yunxin.kit.corekit.im.provider.FetchCallback;

import java.util.ArrayList;
import java.util.List;

/**
 * select conversation view model
 */
public class SelectorViewModel extends BaseViewModel {
    private final String TAG = "SelectorViewModel";

    private final MutableLiveData<FetchResult<List<ConversationBean>>> queryLiveData = new MutableLiveData<>();
    private IConversationFactory conversationFactory = new DefaultViewHolderFactory();
    private final static int PAGE_LIMIT = 50;
    private boolean hasMore = true;

    public MutableLiveData<FetchResult<List<ConversationBean>>> getQueryLiveData() {
        return queryLiveData;
    }

    public void fetchConversation() {
        queryConversation(null);
    }

    public void loadMore(ConversationBean data){
        if (data != null && data.infoData != null){
            queryConversation(data.infoData);
        }
    }

    public void setConversationFactory(IConversationFactory factory) {
        this.conversationFactory = factory;
    }

    private void queryConversation(ConversationInfo data){
        ConversationRepo.INSTANCE.getSessionList(data,PAGE_LIMIT, new FetchCallback<List<ConversationInfo>>() {
            @Override
            public void onSuccess(@Nullable List<ConversationInfo> param) {
                FetchResult<List<ConversationBean>> result = new FetchResult<>(LoadStatus.Success);
                if (data != null){
                    result.setLoadStatus(LoadStatus.Finish);
                }
                List<ConversationBean> resultData = new ArrayList<>();
                for (int index = 0; param != null && index < param.size(); index++) {
                    resultData.add(conversationFactory.CreateBean(param.get(index)));
                    XLog.d(TAG, "queryConversation:onSuccess", param.get(index).getContactId());
                }
                hasMore = param != null && param.size() == PAGE_LIMIT;
                result.setData(resultData);
                queryLiveData.postValue(result);
            }

            @Override
            public void onFailed(int code) {
            }

            @Override
            public void onException(@Nullable Throwable exception) {

            }
        });
    }

    public boolean hasMore(){
        return hasMore;
    }
}
