// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.contact;

import static com.netease.yunxin.kit.contactkit.ui.ContactConstant.LIB_TAG;

import android.content.Context;
import android.text.TextUtils;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import com.netease.yunxin.kit.common.ui.viewmodel.BaseViewModel;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.contactkit.repo.ContactRepo;
import com.netease.yunxin.kit.contactkit.ui.R;
import com.netease.yunxin.kit.contactkit.ui.model.ContactEntranceBean;
import com.netease.yunxin.kit.contactkit.ui.model.ContactFriendBean;
import com.netease.yunxin.kit.contactkit.ui.model.IViewTypeConstant;
import com.netease.yunxin.kit.corekit.im.model.FriendInfo;
import com.netease.yunxin.kit.corekit.im.model.UserInfo;
import com.netease.yunxin.kit.corekit.im.provider.FetchCallback;
import com.netease.yunxin.kit.corekit.im.provider.FriendChangeType;
import com.netease.yunxin.kit.corekit.im.provider.FriendObserver;
import com.netease.yunxin.kit.corekit.im.provider.LoginSyncObserver;
import com.netease.yunxin.kit.corekit.im.provider.SyncStatus;
import com.netease.yunxin.kit.corekit.im.provider.SystemUnreadCountObserver;
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant;
import java.util.ArrayList;
import java.util.List;

/** contact view model */
public class ContactViewModel extends BaseViewModel {
  private final String TAG = "ContactViewModel";
  private final MutableLiveData<FetchResult<List<ContactFriendBean>>> contactLiveData =
      new MutableLiveData<>();
  private final FetchResult<List<ContactFriendBean>> fetchResult =
      new FetchResult<>(LoadStatus.Finish);
  private final List<ContactFriendBean> contactFriendBeanList = new ArrayList<>();
  private final MutableLiveData<ContactEntranceBean> contactEntranceLiveData =
      new MutableLiveData<>();
  private ContactEntranceBean verifyBean;
  private int unreadCount = 0;

  public ContactViewModel() {
    registerObserver();
  }

  public LiveData<FetchResult<List<ContactFriendBean>>> getContactLiveData() {
    return contactLiveData;
  }

  public LiveData<ContactEntranceBean> getContactEntranceLiveData() {
    return contactEntranceLiveData;
  }

  public void fetchContactList() {
    ALog.d(LIB_TAG, TAG, "fetchContactList");
    ContactRepo.getContactList(
        new FetchCallback<List<FriendInfo>>() {
          @Override
          public void onSuccess(@Nullable List<FriendInfo> param) {
            ALog.d(
                LIB_TAG,
                TAG,
                "fetchContactList,onSuccess:" + (param == null ? "null" : param.size()));
            contactFriendBeanList.clear();
            if (param != null) {
              for (FriendInfo info : param) {
                ContactFriendBean bean = new ContactFriendBean(info);
                bean.viewType = IViewTypeConstant.CONTACT_FRIEND;
                contactFriendBeanList.add(bean);
              }
            }
            fetchResult.setStatus(LoadStatus.Success);
            fetchResult.setData(contactFriendBeanList);
            contactLiveData.postValue(fetchResult);
          }

          @Override
          public void onFailed(int code) {
            ALog.d(LIB_TAG, TAG, "fetchContactList,onFailed:" + code);
            fetchResult.setError(code, "");
            contactLiveData.postValue(fetchResult);
          }

          @Override
          public void onException(@Nullable Throwable exception) {
            ALog.d(LIB_TAG, TAG, "fetchContactList,onException");
            fetchResult.setError(-1, "");
            contactLiveData.postValue(fetchResult);
          }
        });

    ContactRepo.getNotificationUnreadCount(
        new FetchCallback<Integer>() {
          @Override
          public void onSuccess(@Nullable Integer count) {
            ALog.d(LIB_TAG, TAG, "fetchContactList,getNotificationUnreadCount,onSuccess:" + count);
            updateVerifyNumber(count);
          }

          @Override
          public void onFailed(int code) {
            ALog.d(LIB_TAG, TAG, "fetchContactList,getNotificationUnreadCount,onFailed:" + code);
          }

          @Override
          public void onException(@Nullable Throwable exception) {
            ALog.d(LIB_TAG, TAG, "fetchContactList,getNotificationUnreadCount,onException");
          }
        });
  }

  private void registerObserver() {
    ContactRepo.registerFriendObserver(friendObserver);
    ContactRepo.registerNotificationUnreadCountObserver(unreadCountObserver);
    ContactRepo.registerLoginSyncObserver(loginSyncObserver);
  }

  private final FriendObserver friendObserver =
      (friendChangeType, accountList) -> {
        if (friendChangeType == FriendChangeType.Delete
            || friendChangeType == FriendChangeType.AddBlack) {
          ALog.d(LIB_TAG, TAG, "friendObserver,Delete||AddBlack:" + accountList.size());
          removeFriend(accountList);
          return;
        }
        if (friendChangeType == FriendChangeType.Update) {
          ALog.d(LIB_TAG, TAG, "friendObserver,Update:" + accountList.size());
          updateFriend(accountList);
          return;
        }
        // get new friend account, add or remove
        List<String> addFriendList = new ArrayList<>();
        if (friendChangeType == FriendChangeType.RemoveBlack) {
          for (String account : accountList) {
            if (ContactRepo.isFriend(account)) {
              addFriendList.add(account);
            }
          }
        }
        if (friendChangeType == FriendChangeType.Add) {
          ALog.d(LIB_TAG, TAG, "friendObserver,Add:" + accountList.size());
          addFriendList.addAll(accountList);
        }
        // fetch friend info and update to view
        addFriend(addFriendList, FetchResult.FetchType.Add);
      };

  private final SystemUnreadCountObserver unreadCountObserver =
      (count) -> {
        updateVerifyNumber(count);
      };

  private void updateVerifyNumber(int count) {
    ALog.d(LIB_TAG, TAG, "updateVerifyNumber:" + count);
    if (verifyBean != null) {
      if (count != unreadCount) {
        verifyBean.number = count;
        unreadCount = count;
        contactEntranceLiveData.postValue(verifyBean);
      }
    } else {
      unreadCount = count;
    }
  }

  public int getVerifyCount() {
    return unreadCount;
  }

  private final LoginSyncObserver loginSyncObserver =
      (syncStatus) -> {
        ALog.d(LIB_TAG, TAG, "loginSyncObserver:" + syncStatus.name());
        if (syncStatus == SyncStatus.Complete) {
          fetchContactList();
        } else if (syncStatus == SyncStatus.BeginSync) {
          fetchResult.setStatus(LoadStatus.Loading);
          fetchResult.setData(null);
          contactLiveData.postValue(fetchResult);
        }
      };

  private void removeFriend(List<String> accountList) {
    ALog.d(LIB_TAG, TAG, "removeFriend:" + (accountList == null ? "null" : accountList.size()));
    if (accountList == null || accountList.isEmpty()) {
      return;
    }
    List<ContactFriendBean> removeData = new ArrayList<>();
    for (String account : accountList) {
      for (ContactFriendBean bean : contactFriendBeanList) {
        if (TextUtils.equals(account, bean.data.getAccount())) {
          contactFriendBeanList.remove(bean);
          removeData.add(bean);
          break;
        }
      }
    }
    if (!removeData.isEmpty()) {
      fetchResult.setFetchType(FetchResult.FetchType.Remove);
      fetchResult.setData(removeData);
      contactLiveData.postValue(fetchResult);
    }
  }

  private void addFriend(List<String> accountList, FetchResult.FetchType type) {
    ALog.d(
        LIB_TAG,
        TAG,
        "addFriend:" + type.name() + (accountList == null ? "null" : accountList.size()));
    if (!accountList.isEmpty()) {
      ContactRepo.getUserInfo(
          accountList,
          new FetchCallback<List<UserInfo>>() {
            @Override
            public void onSuccess(@Nullable List<UserInfo> param) {
              ALog.d(
                  LIB_TAG, TAG, "addFriend,onSuccess:" + (param == null ? "null" : param.size()));
              List<ContactFriendBean> addList = new ArrayList<>();
              for (int index = 0; param != null && index < param.size(); index++) {
                FriendInfo friendInfo = ContactRepo.getFriend(param.get(index).getAccount());
                if (friendInfo != null) {
                  friendInfo.setUserInfo(param.get(index));
                  ContactFriendBean bean = new ContactFriendBean(friendInfo);
                  bean.viewType = IViewTypeConstant.CONTACT_FRIEND;
                  contactFriendBeanList.add(bean);
                  addList.add(bean);
                }
              }
              fetchResult.setFetchType(type);
              fetchResult.setData(addList);
              contactLiveData.setValue(fetchResult);
            }

            @Override
            public void onFailed(int code) {
              ALog.d(LIB_TAG, TAG, "addFriend,onFailed:" + code);
              ToastX.showShortToast(String.valueOf(code));
            }

            @Override
            public void onException(@Nullable Throwable exception) {
              ALog.d(LIB_TAG, TAG, "addFriend,onException");
            }
          });
    }
  }

  private void updateFriend(List<String> accountList) {
    ALog.d(LIB_TAG, TAG, "updateFriend:" + (accountList == null ? "null" : accountList.size()));
    List<ContactFriendBean> updateBean = new ArrayList<>();
    ContactRepo.getFriendList(
        accountList,
        new FetchCallback<List<FriendInfo>>() {
          @Override
          public void onSuccess(@Nullable List<FriendInfo> param) {
            ALog.d(
                LIB_TAG, TAG, "updateFriend,onSuccess:" + (param == null ? "null" : param.size()));
            for (FriendInfo friendInfo : param) {
              if (friendInfo == null) {
                continue;
              }
              for (ContactFriendBean bean : contactFriendBeanList) {
                if (TextUtils.equals(friendInfo.getAccount(), bean.data.getAccount())) {
                  bean.data = friendInfo;
                  updateBean.add(bean);
                  break;
                }
              }
            }
            if (updateBean.size() > 0) {
              fetchResult.setFetchType(FetchResult.FetchType.Update);
              fetchResult.setData(updateBean);
              contactLiveData.postValue(fetchResult);
            }
          }

          @Override
          public void onFailed(int code) {
            ALog.d(LIB_TAG, TAG, "updateFriend,onFailed:" + code);
            ToastX.showShortToast(String.valueOf(code));
          }

          @Override
          public void onException(@Nullable Throwable exception) {
            ALog.d(LIB_TAG, TAG, "updateFriend,onException");
          }
        });
  }

  public List<ContactEntranceBean> getContactEntranceList(Context context) {
    List<ContactEntranceBean> contactDataList = new ArrayList<>();
    //verify message
    verifyBean =
        new ContactEntranceBean(
            R.mipmap.ic_contact_verfiy_msg, context.getString(R.string.contact_list_verify_msg));
    verifyBean.number = unreadCount;
    verifyBean.router = RouterConstant.PATH_MY_NOTIFICATION_PAGE;
    //black list
    ContactEntranceBean blackBean =
        new ContactEntranceBean(
            R.mipmap.ic_contact_black_list, context.getString(R.string.contact_list_black_list));
    blackBean.router = RouterConstant.PATH_MY_BLACK_PAGE;
    //my group
    ContactEntranceBean groupBean =
        new ContactEntranceBean(
            R.mipmap.ic_contact_my_group, context.getString(R.string.contact_list_my_group));
    groupBean.router = RouterConstant.PATH_MY_TEAM_PAGE;

    contactDataList.add(verifyBean);
    contactDataList.add(blackBean);
    contactDataList.add(groupBean);
    return contactDataList;
  }

  @Override
  protected void onCleared() {
    super.onCleared();
    ContactRepo.unregisterFriendObserver(friendObserver);
    ContactRepo.unregisterNotificationUnreadCountObserver(unreadCountObserver);
  }
}
