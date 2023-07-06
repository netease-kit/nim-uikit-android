// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.blacklist;

import static com.netease.yunxin.kit.contactkit.ui.ContactConstant.LIB_TAG;

import android.text.TextUtils;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.repo.ContactObserverRepo;
import com.netease.yunxin.kit.chatkit.repo.ContactRepo;
import com.netease.yunxin.kit.common.ui.viewmodel.BaseViewModel;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.contactkit.ui.model.ContactBlackListBean;
import com.netease.yunxin.kit.corekit.im.model.UserInfo;
import com.netease.yunxin.kit.corekit.im.provider.FetchCallback;
import com.netease.yunxin.kit.corekit.im.provider.FriendChangeType;
import com.netease.yunxin.kit.corekit.im.provider.FriendObserver;
import java.util.ArrayList;
import java.util.List;

public class BlackListViewModel extends BaseViewModel {
  private static final String TAG = "BlackListViewModel";

  private final MutableLiveData<FetchResult<List<ContactBlackListBean>>> resultLiveData =
      new MutableLiveData<>();
  private final FetchResult<List<ContactBlackListBean>> fetchResult =
      new FetchResult<>(LoadStatus.Finish);
  private final List<ContactBlackListBean> blackList = new ArrayList<>();
  private final FriendObserver friendObserver;

  public MutableLiveData<FetchResult<List<ContactBlackListBean>>> getFetchResult() {
    return resultLiveData;
  }

  public BlackListViewModel() {
    friendObserver =
        (friendChangeType, accountList) -> {
          if (friendChangeType == FriendChangeType.RemoveBlack) {
            removeBlackData(accountList);
          }
          if (friendChangeType == FriendChangeType.AddBlack) {
            addBlackData(accountList);
          }
        };
    ContactObserverRepo.registerFriendObserver(friendObserver);
  }

  public void fetchBlackList() {
    ALog.d(LIB_TAG, TAG, "fetchBlackList");
    fetchResult.setStatus(LoadStatus.Loading);
    resultLiveData.postValue(fetchResult);
    ContactRepo.getBlackList(
        new FetchCallback<List<UserInfo>>() {
          @Override
          public void onSuccess(@Nullable List<UserInfo> param) {
            ALog.d(
                LIB_TAG,
                TAG,
                "fetchBlackList,onSuccess:" + (param == null ? "null" : param.size()));
            blackList.clear();
            if (param != null && param.size() > 0) {
              fetchResult.setStatus(LoadStatus.Success);
              for (UserInfo contactInfo : param) {
                ContactBlackListBean friendBean = new ContactBlackListBean(contactInfo);
                friendBean.friendInfo = ContactRepo.getFriend(contactInfo.getAccount());
                blackList.add(friendBean);
              }
              fetchResult.setData(blackList);
            } else {
              fetchResult.setData(null);
              fetchResult.setStatus(LoadStatus.Success);
            }
            resultLiveData.postValue(fetchResult);
          }

          @Override
          public void onFailed(int code) {
            ALog.d(LIB_TAG, TAG, "fetchBlackList,onFailed:" + code);
            fetchResult.setError(code, "");
            resultLiveData.postValue(fetchResult);
          }

          @Override
          public void onException(@Nullable Throwable exception) {
            ALog.d(LIB_TAG, TAG, "fetchBlackList,onException");
            fetchResult.setError(-1, "");
            resultLiveData.postValue(fetchResult);
          }
        });
  }

  public void addBlackOp(String account, FetchCallback<Void> callback) {
    ALog.d(LIB_TAG, TAG, "addBlackOp:" + account);
    if (!TextUtils.isEmpty(account)) {
      ContactRepo.addBlackList(account, callback);
    }
  }

  public void removeBlackOp(String account, FetchCallback<Void> callback) {
    ALog.d(LIB_TAG, TAG, "removeBlackOp:" + account);
    ContactRepo.removeBlackList(account, callback);
  }

  private void removeBlackData(List<String> accountList) {
    ALog.d(LIB_TAG, TAG, "removeBlackData:" + (accountList == null ? "null" : accountList.size()));
    if (accountList == null || accountList.size() < 1) {
      return;
    }

    List<ContactBlackListBean> delete = new ArrayList<>();
    for (String account : accountList) {
      for (ContactBlackListBean bean : blackList) {
        if (TextUtils.equals(bean.data.getAccount(), account)) {
          delete.add(bean);
          blackList.remove(bean);
          break;
        }
      }
    }

    //black list match
    if (accountList.size() == delete.size()) {
      fetchResult.setFetchType(FetchResult.FetchType.Remove);
      fetchResult.setData(delete);
      resultLiveData.setValue(fetchResult);
    } else {
      //black list has error,need to fetch
      fetchBlackList();
    }
  }

  private void addBlackData(List<String> accountList) {
    ALog.d(LIB_TAG, TAG, "addBlackData:" + (accountList == null ? "null" : accountList.size()));
    if (accountList == null || accountList.size() < 1) {
      return;
    }
    List<ContactBlackListBean> add = new ArrayList<>();
    ContactRepo.getUserInfo(
        accountList,
        new FetchCallback<List<UserInfo>>() {
          @Override
          public void onSuccess(@Nullable List<UserInfo> param) {
            ALog.d(
                LIB_TAG, TAG, "addBlackData,onSuccess:" + (param == null ? "null" : param.size()));
            if (param != null && param.size() > 0) {
              for (UserInfo contactInfo : param) {
                ContactBlackListBean blackBean = new ContactBlackListBean(contactInfo);
                blackBean.friendInfo = ContactRepo.getFriend(contactInfo.getAccount());
                add.add(blackBean);
                blackList.add(0, blackBean);
              }
              fetchResult.setFetchType(FetchResult.FetchType.Add);
              fetchResult.setData(add);
              resultLiveData.setValue(fetchResult);
            }
          }

          @Override
          public void onFailed(int code) {
            ALog.d(LIB_TAG, TAG, "addBlackData,onFailed:" + code);
          }

          @Override
          public void onException(@Nullable Throwable exception) {
            ALog.d(LIB_TAG, TAG, "addBlackData,exception");
          }
        });
  }

  @Override
  protected void onCleared() {
    super.onCleared();
    ContactObserverRepo.unregisterFriendObserver(friendObserver);
  }
}
