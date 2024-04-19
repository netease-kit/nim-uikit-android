// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.blacklist;

import static com.netease.yunxin.kit.contactkit.ui.ContactConstant.LIB_TAG;

import android.text.TextUtils;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.repo.ContactRepo;
import com.netease.yunxin.kit.common.ui.viewmodel.BaseViewModel;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.contactkit.ui.R;
import com.netease.yunxin.kit.contactkit.ui.v2model.V2ContactBlackListBean;
import com.netease.yunxin.kit.corekit.im2.IMKitClient;
import com.netease.yunxin.kit.corekit.im2.extend.FetchCallback;
import com.netease.yunxin.kit.corekit.im2.listener.ContactListener;
import com.netease.yunxin.kit.corekit.im2.listener.V2FriendChangeType;
import com.netease.yunxin.kit.corekit.im2.model.FriendAddApplicationInfo;
import com.netease.yunxin.kit.corekit.im2.model.UserWithFriend;
import com.netease.yunxin.kit.corekit.im2.model.V2UserInfo;
import java.util.ArrayList;
import java.util.List;

/*
 * 黑名单ViewModel
 */
public class BlackListViewModel extends BaseViewModel {
  private static final String TAG = "BlackListViewModel";

  //黑名单查询结果LiveData
  private final MutableLiveData<FetchResult<List<V2ContactBlackListBean>>> resultLiveData =
      new MutableLiveData<>();
  private final List<V2ContactBlackListBean> blackList = new ArrayList<>();
  // 黑名单变化监听
  private final ContactListener friendListener;

  // 获取黑名单查询结果LiveData
  public MutableLiveData<FetchResult<List<V2ContactBlackListBean>>> getBlackListLiveData() {
    return resultLiveData;
  }

  public BlackListViewModel() {
    // 黑名单变化监听
    friendListener =
        new ContactListener() {
          @Override
          public void onFriendChange(
              @NonNull V2FriendChangeType friendChangeType,
              @NonNull List<? extends UserWithFriend> friendList) {

            // 移除黑名单黑名单
            if (friendChangeType == V2FriendChangeType.RemoveBlack) {
              removeBlackData(friendList);
            }
            // 添加黑名单
            else if (friendChangeType == V2FriendChangeType.AddBlack) {
              addBlackData(friendList);
            }
          }

          @Override
          public void onFriendAddApplication(@NonNull FriendAddApplicationInfo friendApplication) {}

          @Override
          public void onFriendAddRejected(@NonNull FriendAddApplicationInfo rejectionInfo) {}
        };
    ContactRepo.addFriendListener(friendListener);
  }

  public void getBlackList() {
    ALog.d(LIB_TAG, TAG, "getBlackList");
    FetchResult<List<V2ContactBlackListBean>> blackResult = new FetchResult<>(LoadStatus.Success);
    blackResult.setStatus(LoadStatus.Loading);
    resultLiveData.postValue(blackResult);
    ContactRepo.getBlockList(
        new FetchCallback<List<UserWithFriend>>() {
          @Override
          public void onError(int errorCode, @Nullable String errorMsg) {
            ALog.d(LIB_TAG, TAG, "fetchBlackList,onFailed:" + errorCode);
            blackResult.setError(errorCode, errorMsg);
            resultLiveData.postValue(blackResult);
          }

          @Override
          public void onSuccess(@Nullable List<UserWithFriend> param) {
            ALog.d(
                LIB_TAG,
                TAG,
                "fetchBlackList,onSuccess:" + (param == null ? "null" : param.size()));
            blackList.clear();
            blackResult.setStatus(LoadStatus.Success);
            if (param != null && param.size() > 0) {
              for (UserWithFriend contactInfo : param) {
                V2ContactBlackListBean friendBean =
                    new V2ContactBlackListBean(
                        contactInfo.getUserInfo() != null
                            ? new V2UserInfo(contactInfo.getAccount(), contactInfo.getUserInfo())
                            : new V2UserInfo(contactInfo.getAccount(), null));
                friendBean.friendInfo = contactInfo;
                blackList.add(friendBean);
              }
              blackResult.setData(blackList);
            } else {
              blackResult.setData(null);
            }
            resultLiveData.postValue(blackResult);
          }
        });
  }

  public void addBlackOp(String account) {
    ALog.d(LIB_TAG, TAG, "addBlackOp:" + account);
    if (!TextUtils.isEmpty(account)) {
      ContactRepo.addBlockList(
          account,
          new FetchCallback<Void>() {
            @Override
            public void onError(int errorCode, @Nullable String errorMsg) {
              String content =
                  String.format(
                      IMKitClient.getApplicationContext()
                          .getResources()
                          .getString(R.string.add_black_error),
                      account);
              Toast.makeText(IMKitClient.getApplicationContext(), content, Toast.LENGTH_SHORT)
                  .show();
            }

            @Override
            public void onSuccess(@Nullable Void data) {}
          });
    }
  }

  public void removeBlackOp(String account) {
    ALog.d(LIB_TAG, TAG, "removeBlackOp:" + account);
    ContactRepo.removeBlockList(
        account,
        new FetchCallback<Void>() {
          @Override
          public void onError(int errorCode, @Nullable String errorMsg) {
            Toast.makeText(
                    IMKitClient.getApplicationContext(),
                    IMKitClient.getApplicationContext().getText(R.string.remove_black_fail),
                    Toast.LENGTH_SHORT)
                .show();
          }

          @Override
          public void onSuccess(@Nullable Void data) {}
        });
  }

  private void removeBlackData(List<? extends UserWithFriend> accountList) {
    ALog.d(
        LIB_TAG,
        TAG,
        "removeBlackData,account list size:" + (accountList == null ? "null" : accountList.size()));
    if (accountList == null || accountList.size() < 1) {
      return;
    }

    List<V2ContactBlackListBean> delete = new ArrayList<>();
    for (UserWithFriend user : accountList) {
      for (V2ContactBlackListBean bean : blackList) {
        if (TextUtils.equals(bean.data.getAccountId(), user.getAccount())) {
          delete.add(bean);
          blackList.remove(bean);
          break;
        }
      }
    }
    ALog.d(LIB_TAG, TAG, "removeBlackData:" + delete.size());

    //black list match
    if (accountList.size() == delete.size()) {
      FetchResult<List<V2ContactBlackListBean>> blackListResult =
          new FetchResult<>(LoadStatus.Success);
      blackListResult.setLoadStatus(LoadStatus.Success);
      blackListResult.setFetchType(FetchResult.FetchType.Remove);
      blackListResult.setData(delete);
      resultLiveData.setValue(blackListResult);
    } else {
      //black list has error,need to fetch
      getBlackList();
    }
  }

  private void addBlackData(List<? extends UserWithFriend> userList) {
    ALog.d(LIB_TAG, TAG, "addBlackData:" + (userList == null ? "null" : userList.size()));
    if (userList == null || userList.size() < 1) {
      return;
    }
    List<V2ContactBlackListBean> add = new ArrayList<>();
    for (UserWithFriend contactInfo : userList) {
      V2ContactBlackListBean friendBean =
          new V2ContactBlackListBean(
              contactInfo.getUserInfo() != null
                  ? new V2UserInfo(contactInfo.getAccount(), contactInfo.getUserInfo())
                  : new V2UserInfo(contactInfo.getAccount(), null));
      friendBean.friendInfo = contactInfo;
      if (!blackList.contains(friendBean)) {
        blackList.add(0, friendBean);
        add.add(friendBean);
      }
    }
    FetchResult<List<V2ContactBlackListBean>> blackListResult =
        new FetchResult<>(LoadStatus.Success);
    blackListResult.setLoadStatus(LoadStatus.Success);
    blackListResult.setFetchType(FetchResult.FetchType.Add);
    blackListResult.setData(add);
    resultLiveData.setValue(blackListResult);
  }

  @Override
  protected void onCleared() {
    super.onCleared();
    ContactRepo.removeFriendListener(friendListener);
  }
}
