/*
 * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.kit.chatkit.ui.page.viewmodel;

import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;

import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.model.IMMessageRecord;
import com.netease.yunxin.kit.chatkit.repo.ChatMessageRepo;
import com.netease.yunxin.kit.chatkit.ui.model.ChatSearchBean;
import com.netease.yunxin.kit.common.ui.viewmodel.BaseViewModel;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.corekit.im.provider.FetchCallback;

import java.util.ArrayList;
import java.util.List;

/**
 * Search message info view model
 * search history message for Team chat page
 */
public class SearchMessageViewModel extends BaseViewModel {

    private static final String TAG = "SearchMessageViewModel";
    private final MutableLiveData<FetchResult<List<ChatSearchBean>>> searchLiveData = new MutableLiveData<>();

    /**
     * search message result live data
     */
    public MutableLiveData<FetchResult<List<ChatSearchBean>>> getSearchLiveData(){
        return searchLiveData;
    }

    public void searchMessage(String keyword, SessionTypeEnum type, String sessionId) {
        ALog.i(TAG, "searchMessage");
        ChatMessageRepo.searchMessage(keyword, type, sessionId, new FetchCallback<List<IMMessageRecord>>() {
            @Override
            public void onSuccess(@Nullable List<IMMessageRecord> param) {
                FetchResult<List<ChatSearchBean>> result = new FetchResult<>(LoadStatus.Success);
                if (param != null) {
                    List<ChatSearchBean> searchBeanList = new ArrayList<>();
                    for (IMMessageRecord record : param) {
                        searchBeanList.add(new ChatSearchBean(record));
                    }
                    result.setData(searchBeanList);
                }
                searchLiveData.postValue(result);
            }

            @Override
            public void onFailed(int code) {

            }

            @Override
            public void onException(@Nullable Throwable exception) {

            }
        });
    }
}
