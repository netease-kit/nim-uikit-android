// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.userinfo;

import android.text.TextUtils;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import com.netease.yunxin.kit.common.ui.viewmodel.BaseViewModel;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.contactkit.repo.ContactRepo;
import com.netease.yunxin.kit.contactkit.ui.model.ContactUserInfoBean;
import com.netease.yunxin.kit.corekit.im.model.FriendInfo;
import com.netease.yunxin.kit.corekit.im.model.FriendVerifyType;
import com.netease.yunxin.kit.corekit.im.model.UserInfo;
import com.netease.yunxin.kit.corekit.im.provider.FetchCallback;
import com.netease.yunxin.kit.corekit.im.provider.UserInfoObserver;
import java.util.ArrayList;
import java.util.List;

public class UserInfoViewModel extends BaseViewModel {

  private final MutableLiveData<FetchResult<ContactUserInfoBean>> friendLiveData =
      new MutableLiveData<>();
  private final FetchResult<ContactUserInfoBean> fetchResult = new FetchResult<>(LoadStatus.Finish);
  private final MutableLiveData<FetchResult<List<UserInfo>>> userInfoLiveData =
      new MutableLiveData<>();
  private final FetchResult<List<UserInfo>> userInfoFetchResult =
      new FetchResult<>(LoadStatus.Finish);

  public UserInfoViewModel() {
    registerObserver();
  }

  private final UserInfoObserver userInfoObserver =
      userList -> {
        userInfoFetchResult.setLoadStatus(LoadStatus.Finish);
        userInfoFetchResult.setData(userList);
        userInfoFetchResult.setType(FetchResult.FetchType.Update);
        userInfoFetchResult.setTypeIndex(-1);
        userInfoLiveData.setValue(userInfoFetchResult);
      };

  public void registerObserver() {
    ContactRepo.registerUserInfoObserver(userInfoObserver);
  }

  public MutableLiveData<FetchResult<ContactUserInfoBean>> getFetchResult() {
    return friendLiveData;
  }

  public MutableLiveData<FetchResult<List<UserInfo>>> getUserInfoLiveData() {
    return userInfoLiveData;
  }

  public void fetchData(String account) {
    if (TextUtils.isEmpty(account)) {
      return;
    }
    List<String> accountList = new ArrayList<>();
    accountList.add(account);
    FriendInfo friendInfo = ContactRepo.getFriend(account);
    ContactRepo.getUserInfo(
        accountList,
        new FetchCallback<List<UserInfo>>() {
          @Override
          public void onSuccess(@Nullable List<UserInfo> param) {
            if (param != null && param.size() > 0) {
              ContactUserInfoBean userInfo = new ContactUserInfoBean(param.get(0));
              userInfo.friendInfo = friendInfo;
              userInfo.isBlack = isBlack(account);
              userInfo.isFriend = isFriend(account);
              fetchResult.setData(userInfo);
              fetchResult.setStatus(LoadStatus.Success);
            } else {
              fetchResult.setError(-1, "");
            }
            friendLiveData.postValue(fetchResult);
          }

          @Override
          public void onFailed(int code) {
            fetchResult.setError(code, "");
            friendLiveData.postValue(fetchResult);
          }

          @Override
          public void onException(@Nullable Throwable exception) {
            fetchResult.setError(-1, "");
            friendLiveData.postValue(fetchResult);
          }
        });
  }

  public boolean isBlack(String account) {
    return ContactRepo.isBlackList(account);
  }

  public boolean isFriend(String account) {
    return ContactRepo.isFriend(account);
  }

  public void addBlack(String account) {
    ContactRepo.addBlacklist(
        account,
        new FetchCallback<Void>() {
          @Override
          public void onSuccess(@Nullable Void param) {
            fetchData(account);
          }

          @Override
          public void onFailed(int code) {
            fetchResult.setError(code, "");
          }

          @Override
          public void onException(@Nullable Throwable exception) {
            fetchResult.setError(-1, "");
          }
        });
  }

  public void removeBlack(String account) {
    ContactRepo.removeBlacklist(
        account,
        new FetchCallback<Void>() {
          @Override
          public void onSuccess(@Nullable Void param) {
            fetchData(account);
          }

          @Override
          public void onFailed(int code) {
            fetchResult.setError(code, "");
          }

          @Override
          public void onException(@Nullable Throwable exception) {
            fetchResult.setError(-1, "");
          }
        });
  }

  public void deleteFriend(String account) {
    ContactRepo.deleteFriend(
        account,
        new FetchCallback<Void>() {
          @Override
          public void onSuccess(@Nullable Void param) {
            fetchData(account);
          }

          @Override
          public void onFailed(int code) {
            fetchResult.setError(code, "");
          }

          @Override
          public void onException(@Nullable Throwable exception) {
            fetchResult.setError(-1, "");
          }
        });
  }

  public void addFriend(String account, FriendVerifyType type, FetchCallback<Void> callback) {
    ContactRepo.addFriend(account, type, callback);
  }

  public void updateAlias(String account, String alias) {
    ContactRepo.updateAlias(account, alias);
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    ContactRepo.unregisterUserInfoObserver(userInfoObserver);
  }
}
