// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.userinfo;

import static com.netease.yunxin.kit.contactkit.ui.ContactConstant.LIB_TAG;

import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import com.netease.nimlib.coexist.sdk.v2.friend.enums.V2NIMFriendAddMode;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.repo.ContactRepo;
import com.netease.yunxin.kit.common.ui.viewmodel.BaseViewModel;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.contactkit.ui.FriendObserveImpl;
import com.netease.yunxin.kit.contactkit.ui.model.ContactUserInfoBean;
import com.netease.yunxin.kit.corekit.coexist.im2.extend.FetchCallback;
import com.netease.yunxin.kit.corekit.coexist.im2.listener.ContactChangeType;
import com.netease.yunxin.kit.corekit.coexist.im2.model.FriendVerifyType;
import com.netease.yunxin.kit.corekit.coexist.im2.model.UserWithFriend;
import com.netease.yunxin.kit.corekit.coexist.im2.model.V2UserInfo;
import java.util.Collections;
import java.util.List;

public class UserInfoViewModel extends BaseViewModel {
  private final String TAG = "UserInfoViewModel";

  private final MutableLiveData<FetchResult<ContactUserInfoBean>> friendLiveData =
      new MutableLiveData<>();
  private final FetchResult<ContactUserInfoBean> fetchResult = new FetchResult<>(LoadStatus.Finish);

  private final FetchResult<List<V2UserInfo>> userInfoFetchResult =
      new FetchResult<>(LoadStatus.Finish);

  private final MutableLiveData<FetchResult<UserWithFriend>> friendChangeLiveData =
      new MutableLiveData<>();
  private final FetchResult<UserWithFriend> friendChangeFetchResult =
      new FetchResult<>(LoadStatus.Finish);

  private String accountId;

  public UserInfoViewModel() {
    registerObserver();
  }

  public void init(String account) {
    accountId = account;
  }

  private final FriendObserveImpl friendObserver =
      new FriendObserveImpl() {
        @Override
        public void onContactChange(
            @NonNull ContactChangeType changeType,
            @NonNull List<? extends UserWithFriend> contactList) {
          ALog.d(LIB_TAG, TAG, "friendObserver:" + changeType + "," + contactList.size());
          if (changeType == ContactChangeType.AddFriend
              || changeType == ContactChangeType.DeleteFriend
              || changeType == ContactChangeType.Update) {
            for (UserWithFriend friend : contactList) {
              if (TextUtils.equals(accountId, friend.getAccount())) {
                friendChangeFetchResult.setData(friend);
                friendChangeFetchResult.setLoadStatus(LoadStatus.Finish);
                if (changeType == ContactChangeType.AddFriend) {
                  friendChangeFetchResult.setFetchType(FetchResult.FetchType.Add);
                } else if (changeType == ContactChangeType.DeleteFriend) {
                  friendChangeFetchResult.setFetchType(FetchResult.FetchType.Remove);
                } else {
                  friendChangeFetchResult.setFetchType(FetchResult.FetchType.Update);
                }
                friendChangeLiveData.setValue(friendChangeFetchResult);
              }
            }
          }
        }
      };

  public void registerObserver() {
    ContactRepo.addContactListener(friendObserver);
  }

  public MutableLiveData<FetchResult<ContactUserInfoBean>> getFriendFetchResult() {
    return friendLiveData;
  }

  public MutableLiveData<FetchResult<UserWithFriend>> getFriendChangeLiveData() {
    return friendChangeLiveData;
  }

  public void getUserWithFriend(String account) {
    ALog.d(LIB_TAG, TAG, "getUserWithFriend:" + account);
    if (TextUtils.isEmpty(account)) {
      return;
    }
    ContactRepo.getFriendUserInfo(
        account,
        false,
        new FetchCallback<UserWithFriend>() {
          @Override
          public void onError(int errorCode, String errorMsg) {
            ALog.d(LIB_TAG, TAG, "getUserWithFriend,onError:" + errorCode + "," + errorMsg);
            fetchResult.setError(errorCode, errorMsg);
            friendLiveData.postValue(fetchResult);
          }

          @Override
          public void onSuccess(@Nullable UserWithFriend param) {
            ALog.d(
                LIB_TAG,
                TAG,
                "getUserWithFriend,onSuccess:" + (param == null ? "null" : param.getAccount()));
            if (param != null && param.getUserInfo() != null) {
              ContactUserInfoBean userInfo = new ContactUserInfoBean(param.getUserInfo());
              userInfo.friendInfo = param;
              userInfo.isBlack = isBlack(account);
              userInfo.isFriend = isFriend(account) && param.getFriend() != null;
              fetchResult.setData(userInfo);
              fetchResult.setStatus(LoadStatus.Success);
              updateUserInfoFromCloud(account);
            } else {
              fetchResult.setError(-1, "");
            }
            friendLiveData.postValue(fetchResult);
          }
        });
  }

  // 从云端更新用户信息，保证用户信息最新
  public void updateUserInfoFromCloud(String account) {
    ALog.d(LIB_TAG, TAG, "getUserInfoFromCloud:" + account);
    if (TextUtils.isEmpty(account)) {
      return;
    }
    ContactRepo.getUserListFromCloud(Collections.singletonList(account), null);
  }

  public boolean isBlack(String account) {
    ALog.d(LIB_TAG, TAG, "isBlack:" + account);
    return ContactRepo.isBlockList(account);
  }

  public boolean isFriend(String account) {
    ALog.d(LIB_TAG, TAG, "isFriend:" + account);
    return ContactRepo.isFriend(account);
  }

  public void addBlack(String account) {
    ALog.d(LIB_TAG, TAG, "addBlack:" + account);
    ContactRepo.addBlockList(
        account,
        new FetchCallback<Void>() {
          @Override
          public void onError(int errorCode, @Nullable String errorMsg) {
            ALog.d(LIB_TAG, TAG, "addBlack,onError:" + errorCode + "," + errorMsg);
            fetchResult.setError(errorCode, errorMsg);
          }

          @Override
          public void onSuccess(@Nullable Void param) {
            ALog.d(LIB_TAG, TAG, "addBlack,onSuccess");
            getUserWithFriend(account);
          }
        });
  }

  public void removeBlack(String account) {
    ALog.d(LIB_TAG, TAG, "removeBlack:" + account);
    ContactRepo.removeBlockList(
        account,
        new FetchCallback<Void>() {
          @Override
          public void onError(int errorCode, @Nullable String errorMsg) {
            ALog.d(LIB_TAG, TAG, "removeBlack,onFailed:" + errorCode + "," + errorMsg);
            fetchResult.setError(errorCode, errorMsg);
          }

          @Override
          public void onSuccess(@Nullable Void param) {
            ALog.d(LIB_TAG, TAG, "removeBlack,onSuccess");
            getUserWithFriend(account);
          }
        });
  }

  public void deleteFriend(String account) {
    ALog.d(LIB_TAG, TAG, "deleteFriend:" + account);
    ContactRepo.deleteFriend(
        account,
        true,
        new FetchCallback<Void>() {
          @Override
          public void onError(int errorCode, @Nullable String errorMsg) {
            ALog.d(LIB_TAG, TAG, "deleteFriend,onFailed:" + errorCode + "," + errorMsg);
            fetchResult.setError(errorCode, errorMsg);
          }

          @Override
          public void onSuccess(@Nullable Void param) {
            ALog.d(LIB_TAG, TAG, "deleteFriend,onSuccess");
          }
        });
  }

  public void addFriend(String account, FriendVerifyType type, FetchCallback<Void> callback) {
    ALog.d(LIB_TAG, TAG, "addFriend:" + account);
    V2NIMFriendAddMode model =
        type == FriendVerifyType.DirectAdd
            ? V2NIMFriendAddMode.V2NIM_FRIEND_MODE_TYPE_ADD
            : V2NIMFriendAddMode.V2NIM_FRIEND_MODE_TYPE_APPLY;
    if (isBlackList(account)) {
      ALog.d(LIB_TAG, TAG, "addFriend，account in blacklist:" + account);
      removeBlackList(
          account,
          new FetchCallback<Void>() {
            @Override
            public void onError(int errorCode, @Nullable String errorMsg) {
              ALog.d(
                  LIB_TAG, TAG, "addFriend,removeBlackList onFailed:" + errorCode + "," + errorMsg);
              callback.onError(errorCode, errorMsg);
            }

            @Override
            public void onSuccess(@Nullable Void param) {
              ALog.d(LIB_TAG, TAG, "addFriend,removeBlackList onSuccess:" + account);
              ContactRepo.addFriend(account, model, null, callback);
            }
          });
    } else {
      ContactRepo.addFriend(account, model, null, callback);
    }
  }

  public boolean isBlackList(String account) {
    return ContactRepo.isBlockList(account);
  }

  public void removeBlackList(String account, FetchCallback<Void> callback) {
    ALog.d(LIB_TAG, TAG, "removeBlackList:" + account);
    ContactRepo.removeBlockList(account, callback);
  }

  public void updateAlias(String account, String alias) {
    ALog.d(LIB_TAG, TAG, "updateAlias:" + account);
    alias = TextUtils.isEmpty(alias) ? "" : alias;
    ContactRepo.updateAlias(account, alias, null);
  }

  @Override
  protected void onCleared() {
    super.onCleared();
    ContactRepo.removeContactListener(friendObserver);
  }
}
