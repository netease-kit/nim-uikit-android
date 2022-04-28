/*
 * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.kit.contactkit.ui.addfriend;

import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;

import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.contactkit.repo.ContactRepo;
import com.netease.yunxin.kit.corekit.im.provider.FetchCallback;
import com.netease.yunxin.kit.corekit.im.model.UserInfo;
import com.netease.yunxin.kit.common.ui.viewmodel.BaseViewModel;

import java.util.ArrayList;
import java.util.List;

public class AddFriendViewModel extends BaseViewModel {

    private final MutableLiveData<FetchResult<UserInfo>> resultLiveData = new MutableLiveData<>();
    private FetchResult<UserInfo> fetchResult = new FetchResult<>(LoadStatus.Finish);
    private final ContactRepo contactRepo = new ContactRepo();

    public MutableLiveData<FetchResult<UserInfo>> getFetchResult() {
        return resultLiveData;
    }

    public void fetchUser(String account) {
        List<String> accountList = new ArrayList<>();
        accountList.add(account);
        fetchResult.setStatus(LoadStatus.Loading);
        resultLiveData.postValue(fetchResult);
        contactRepo.fetchUserInfo(accountList, new FetchCallback<List<UserInfo>>() {
            @Override
            public void onSuccess(@Nullable List<UserInfo> param) {
                if (param != null && param.size() > 0) {
                    fetchResult.setStatus(LoadStatus.Success);
                    fetchResult.setData(param.get(0));
                } else {
                    fetchResult.setData(null);
                    fetchResult.setStatus(LoadStatus.Success);
                }
                resultLiveData.postValue(fetchResult);
            }

            @Override
            public void onFailed(int code) {
                fetchResult.setError(code,"");
                resultLiveData.postValue(fetchResult);
            }

            @Override
            public void onException(@Nullable Throwable exception) {
                fetchResult.setStatus(LoadStatus.Error);
                resultLiveData.postValue(fetchResult);
            }
        });
    }

}
