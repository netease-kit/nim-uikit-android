// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.contact;

import static com.netease.yunxin.kit.contactkit.ui.ContactConstant.LIB_TAG;

import android.content.Context;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.netease.nimlib.sdk.v2.V2NIMError;
import com.netease.nimlib.sdk.v2.auth.V2NIMLoginDetailListener;
import com.netease.nimlib.sdk.v2.auth.enums.V2NIMConnectStatus;
import com.netease.nimlib.sdk.v2.auth.enums.V2NIMDataSyncState;
import com.netease.nimlib.sdk.v2.auth.enums.V2NIMDataSyncType;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.repo.ContactRepo;
import com.netease.yunxin.kit.common.ui.viewmodel.BaseViewModel;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.contactkit.ui.R;
import com.netease.yunxin.kit.contactkit.ui.model.ContactEntranceBean;
import com.netease.yunxin.kit.contactkit.ui.model.ContactFriendBean;
import com.netease.yunxin.kit.contactkit.ui.model.IViewTypeConstant;
import com.netease.yunxin.kit.corekit.im2.IMKitClient;
import com.netease.yunxin.kit.corekit.im2.extend.FetchCallback;
import com.netease.yunxin.kit.corekit.im2.listener.ContactListener;
import com.netease.yunxin.kit.corekit.im2.listener.FriendApplicationCountListener;
import com.netease.yunxin.kit.corekit.im2.listener.V2FriendChangeType;
import com.netease.yunxin.kit.corekit.im2.model.FriendAddApplicationInfo;
import com.netease.yunxin.kit.corekit.im2.model.UserWithFriend;
import com.netease.yunxin.kit.corekit.im2.utils.RouterConstant;
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
  private final MutableLiveData<FetchResult<List<ContactFriendBean>>> userInfoLiveData =
      new MutableLiveData<>();
  private ContactEntranceBean verifyBean;
  private int unreadCount = 0;

  private boolean isSelectorPage = false;

  //是否已经拉取过联系人
  private boolean haveFetchContact = false;

  //是否已经注册监听
  private boolean haveRegisterListener = false;

  public void setSelectorPage(boolean selectorPage) {
    isSelectorPage = selectorPage;
  }

  public ContactViewModel() {
    registerObserver();
  }

  public LiveData<FetchResult<List<ContactFriendBean>>> getContactLiveData() {
    return contactLiveData;
  }

  public LiveData<ContactEntranceBean> getContactEntranceLiveData() {
    return contactEntranceLiveData;
  }

  public MutableLiveData<FetchResult<List<ContactFriendBean>>> getUserInfoLiveData() {
    return userInfoLiveData;
  }

  public void fetchContactList(boolean userCache) {
    ALog.i(LIB_TAG, TAG, "fetchContactList");
    if (!IMKitClient.isDataSyncComplete()) {
      ALog.i(LIB_TAG, TAG, "fetchContactList,dataSync not complete");
      return;
    }
    if (!contactFriendBeanList.isEmpty()) {
      ALog.d(LIB_TAG, TAG, "fetchContactList,contactFriendBeanList not empty");
      return;
    }
    boolean clearCache = !haveFetchContact;
    if (userCache) {
      clearCache = false;
    }
    ContactRepo.getContactList(
        clearCache,
        new FetchCallback<>() {
          @Override
          public void onError(int errorCode, String errorMsg) {
            ALog.d(LIB_TAG, TAG, "fetchContactList,onFailed:" + errorCode);
            fetchResult.setError(errorCode, errorMsg);
            contactLiveData.postValue(fetchResult);
          }

          @Override
          public void onSuccess(@Nullable List<UserWithFriend> data) {
            ALog.d(
                LIB_TAG,
                TAG,
                "fetchContactList,onSuccess:" + (data == null ? "null" : data.size()));
            haveFetchContact = true;
            contactFriendBeanList.clear();
            if (data != null) {
              for (UserWithFriend info : data) {
                ContactFriendBean bean = new ContactFriendBean(info);
                bean.viewType = IViewTypeConstant.CONTACT_FRIEND;
                contactFriendBeanList.add(bean);
              }
            }
            fetchResult.setStatus(LoadStatus.Success);
            fetchResult.setData(contactFriendBeanList);
            contactLiveData.postValue(fetchResult);
          }
        });

    requestApplicationUnreadCount();
  }

  private void requestApplicationUnreadCount() {
    ContactRepo.getUnreadApplicationCount(
        new FetchCallback<>() {
          @Override
          public void onError(int errorCode, @Nullable String errorMsg) {
            ALog.d(
                LIB_TAG, TAG, "fetchContactList,getNotificationUnreadCount,onFailed:" + errorCode);
          }

          @Override
          public void onSuccess(@Nullable Integer count) {
            ALog.d(LIB_TAG, TAG, "fetchContactList,getUnreadApplicationCount,onSuccess:" + count);
            updateVerifyNumber(count);
          }
        });
  }

  public void registerObserver() {
    if (haveRegisterListener) {
      return;
    }
    ContactRepo.addFriendListener(friendChangeObserve);
    ContactRepo.addFriendApplicationCountListener(friendApplicationCountListener);
    IMKitClient.addLoginDetailListener(loginDetailListener);
    haveRegisterListener = true;
  }

  //监听好友申请数量
  private final FriendApplicationCountListener friendApplicationCountListener =
      this::updateVerifyNumber;

  //监听好友变化
  private final ContactListener friendChangeObserve =
      new ContactListener() {
        @Override
        public void onFriendChange(
            @NonNull V2FriendChangeType friendChangeType,
            @NonNull List<? extends UserWithFriend> friendList) {
          ALog.d(LIB_TAG, TAG, "onFriendChange:" + friendChangeType + "," + friendList.size());
          switch (friendChangeType) {
            case Add:
            case RemoveBlack:
              setAddLivieData(new ArrayList<>(friendList));
              break;
            case Update:
              setUpdateLiveData(new ArrayList<>(friendList));
              break;
            case Delete:
            case AddBlack:
              removeFriend(new ArrayList<>(friendList));
              break;
          }
        }

        @Override
        public void onFriendAddApplication(@NonNull FriendAddApplicationInfo friendApplication) {
          requestApplicationUnreadCount();
        }

        @Override
        public void onFriendAddRejected(@NonNull FriendAddApplicationInfo rejectionInfo) {
          requestApplicationUnreadCount();
        }
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

  private final V2NIMLoginDetailListener loginDetailListener =
      new V2NIMLoginDetailListener() {

        @Override
        public void onConnectStatus(V2NIMConnectStatus status) {
          //do nothing
        }

        @Override
        public void onDisconnected(V2NIMError error) {
          //do nothing
        }

        @Override
        public void onConnectFailed(V2NIMError error) {
          //do nothing
        }

        @Override
        public void onDataSync(V2NIMDataSyncType type, V2NIMDataSyncState state, V2NIMError error) {
          //数据同步完成，刷新联系人列表
          if (type == V2NIMDataSyncType.V2NIM_DATA_SYNC_MAIN) {
            ALog.i(LIB_TAG, TAG, "loginSyncObserver:" + state.name());
            if (isSelectorPage && haveFetchContact) {
              ALog.i(LIB_TAG, TAG, "loginSyncObserver:isSelectorPage");
              return;
            }
            if (state == V2NIMDataSyncState.V2NIM_DATA_SYNC_STATE_COMPLETED) {
              fetchContactList(false);
            } else if (state == V2NIMDataSyncState.V2NIM_DATA_SYNC_STATE_SYNCING) {
              fetchResult.setStatus(LoadStatus.Loading);
              fetchResult.setData(null);
              contactLiveData.postValue(fetchResult);
            }
          }
        }
      };

  private void removeFriend(List<UserWithFriend> accountList) {
    ALog.d(LIB_TAG, TAG, "removeFriend:" + (accountList == null ? "null" : accountList.size()));
    if (accountList == null || accountList.isEmpty()) {
      return;
    }
    List<ContactFriendBean> removeData = new ArrayList<>();
    for (UserWithFriend friend : accountList) {
      for (ContactFriendBean bean : contactFriendBeanList) {
        if (TextUtils.equals(friend.getAccount(), bean.data.getAccount())) {
          contactFriendBeanList.remove(bean);
          removeData.add(bean);
          ALog.d(LIB_TAG, TAG, "removeFriend:removeData add" + bean.data.getAccount());
          break;
        }
      }
    }
    ALog.d(LIB_TAG, TAG, "removeFriend:removeData" + removeData.size());
    if (!removeData.isEmpty()) {
      FetchResult<List<ContactFriendBean>> removeResult =
          new FetchResult<>(FetchResult.FetchType.Remove);
      removeResult.setLoadStatus(LoadStatus.Finish);
      removeResult.setData(removeData);
      contactLiveData.setValue(removeResult);
    }
  }

  private void setAddLivieData(List<UserWithFriend> addFriendList) {
    List<ContactFriendBean> addList = new ArrayList<>();
    for (UserWithFriend friendInfo : addFriendList) {
      ContactFriendBean bean = new ContactFriendBean(friendInfo);
      bean.viewType = IViewTypeConstant.CONTACT_FRIEND;
      if (contactFriendBeanList.contains(bean)) {
        continue;
      }
      contactFriendBeanList.add(bean);
      ALog.d(LIB_TAG, TAG, "addFriend,add:" + "id=" + friendInfo.getAccount());
      addList.add(bean);
    }
    FetchResult<List<ContactFriendBean>> addResult = new FetchResult<>(FetchResult.FetchType.Add);
    addResult.setData(addList);
    addResult.setLoadStatus(LoadStatus.Finish);
    contactLiveData.setValue(addResult);
  }

  private void setUpdateLiveData(List<UserWithFriend> updateList) {
    if (contactFriendBeanList.isEmpty()) {
      return;
    }
    List<ContactFriendBean> updateBean = new ArrayList<>();
    for (UserWithFriend friendInfo : updateList) {
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
      FetchResult<List<ContactFriendBean>> updateResult =
          new FetchResult<>(FetchResult.FetchType.Update);
      updateResult.setData(updateBean);
      updateResult.setLoadStatus(LoadStatus.Finish);
      contactLiveData.setValue(updateResult);
    }
  }

  void configVerifyBean(ContactEntranceBean verifyBean) {
    this.verifyBean = verifyBean;
    if (this.verifyBean != null) {
      this.verifyBean.number = unreadCount;
    }
  }

  private List<ContactEntranceBean> getContactEntranceList(Context context) {
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
    ContactRepo.removeFriendListener(friendChangeObserve);
    ContactRepo.removeFriendApplicationCountListener(friendApplicationCountListener);
    IMKitClient.removeLoginDetailListener(loginDetailListener);
  }
}
