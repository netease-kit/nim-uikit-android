/*
 * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.kit.contactkit.ui.userinfo;

import android.text.TextUtils;

import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;

import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.contactkit.repo.ContactRepo;
import com.netease.yunxin.kit.contactkit.ui.model.ContactUserInfoBean;
import com.netease.yunxin.kit.corekit.im.model.FriendInfo;
import com.netease.yunxin.kit.corekit.im.model.FriendVerifyType;
import com.netease.yunxin.kit.corekit.im.model.UserInfo;
import com.netease.yunxin.kit.corekit.im.provider.FetchCallback;
import com.netease.yunxin.kit.common.ui.viewmodel.BaseViewModel;

import java.util.ArrayList;
import java.util.List;

public class UserInfoViewModel extends BaseViewModel {

    private final MutableLiveData<FetchResult<ContactUserInfoBean>> friendLiveData = new MutableLiveData<>();
    private final FetchResult<ContactUserInfoBean> fetchResult = new FetchResult<>(LoadStatus.Finish);
    private final ContactRepo contactRepo = new ContactRepo();

    public MutableLiveData<FetchResult<ContactUserInfoBean>> getFetchResult() {
        return friendLiveData;
    }

    public void fetchData(String account){
        if (TextUtils.isEmpty(account)){
            return;
        }
        List<String> accountList = new ArrayList<>();
        accountList.add(account);
        FriendInfo friendInfo = contactRepo.getFriendInfo(account);
        contactRepo.fetchUserInfo(accountList, new FetchCallback<List<UserInfo>>() {
            @Override
            public void onSuccess(@Nullable List<UserInfo> param) {
                if (param != null && param.size() > 0) {
                    ContactUserInfoBean userInfo = new ContactUserInfoBean(param.get(0));
                    userInfo.friendInfo = friendInfo;
                    userInfo.isBlack = isBlack(account);
                    userInfo.isFriend = isFriend(account);
                    fetchResult.setData(userInfo);
                    fetchResult.setStatus(LoadStatus.Success);
                }else {
                    fetchResult.setError(-1,"");
                }
                friendLiveData.postValue(fetchResult);
            }

            @Override
            public void onFailed(int code) {
                fetchResult.setError(code,"");
                friendLiveData.postValue(fetchResult);
            }

            @Override
            public void onException(@Nullable Throwable exception) {
                fetchResult.setError(-1,"");
                friendLiveData.postValue(fetchResult);
            }
        });

    }

    public boolean isBlack(String account){
        return contactRepo.isBlack(account);
    }

    public boolean isFriend(String account){
        return contactRepo.isFriend(account);
    }

    public void addBlack(String account){
        contactRepo.addBlacklist(account, new FetchCallback<Void>() {
            @Override
            public void onSuccess(@Nullable Void param) {
                fetchData(account);
            }

            @Override
            public void onFailed(int code) {
                fetchResult.setError(code,"");
            }

            @Override
            public void onException(@Nullable Throwable exception) {
                fetchResult.setError(-1,"");
            }
        });
    }

    public void removeBlack(String account){
        contactRepo.removeFromBlacklist(account, new FetchCallback<Void>() {
            @Override
            public void onSuccess(@Nullable Void param) {
                fetchData(account);
            }

            @Override
            public void onFailed(int code) {
                fetchResult.setError(code,"");
            }

            @Override
            public void onException(@Nullable Throwable exception) {
                fetchResult.setError(-1,"");
            }
        });
    }

    public void deleteFriend(String account){
        contactRepo.deleteFriend(account, new FetchCallback<Void>() {
            @Override
            public void onSuccess(@Nullable Void param) {
                fetchData(account);
            }

            @Override
            public void onFailed(int code) {
                fetchResult.setError(code,"");
            }

            @Override
            public void onException(@Nullable Throwable exception) {
                fetchResult.setError(-1,"");
            }
        });
    }

    public void addFriend(String account,FriendVerifyType type,FetchCallback<Void> callback){
        contactRepo.addFriend(account, type,callback);
    }

    public void updateAlias(String account,String alias){
        contactRepo.updateAlias(account,alias);
    }

}
