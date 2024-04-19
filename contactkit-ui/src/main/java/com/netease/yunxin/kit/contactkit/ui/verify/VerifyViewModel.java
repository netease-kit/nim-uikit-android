// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.verify;

import static com.netease.yunxin.kit.contactkit.ui.ContactConstant.LIB_TAG;

import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import com.netease.nimlib.sdk.v2.friend.enums.V2NIMFriendAddApplicationStatus;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.repo.ContactRepo;
import com.netease.yunxin.kit.common.ui.viewmodel.BaseViewModel;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.contactkit.ui.model.ContactVerifyInfoBean;
import com.netease.yunxin.kit.corekit.im2.IMKitClient;
import com.netease.yunxin.kit.corekit.im2.extend.FetchCallback;
import com.netease.yunxin.kit.corekit.im2.listener.ContactListener;
import com.netease.yunxin.kit.corekit.im2.listener.V2FriendChangeType;
import com.netease.yunxin.kit.corekit.im2.model.FriendAddApplicationInfo;
import com.netease.yunxin.kit.corekit.im2.model.FriendAddApplicationResult;
import com.netease.yunxin.kit.corekit.im2.model.UserWithFriend;
import com.netease.yunxin.kit.corekit.im2.model.V2UserInfo;
import java.util.ArrayList;
import java.util.List;

public class VerifyViewModel extends BaseViewModel {

  private final String TAG = "VerifyViewModel";
  private static final int PAGE_LIMIT = 100;
  //7 day expire time
  private static final long expireLimit = 604800000;
  private long index = 0;
  private boolean hasMore = true;
  //自己主动同意的用户ID，在onFriendChange中 add case判断
  private String agreeUserId;
  //自己主动拒绝的用户ID
  private String disagreeUserId;
  private final MutableLiveData<FetchResult<List<ContactVerifyInfoBean>>> resultLiveData =
      new MutableLiveData<>();
  private final FetchResult<List<ContactVerifyInfoBean>> fetchResult =
      new FetchResult<>(LoadStatus.Finish);
  private final List<ContactVerifyInfoBean> verifyBeanList = new ArrayList<>();
  private final List<ContactVerifyInfoBean> updateList = new ArrayList<>();

  private final ContactListener infoObserver;

  public MutableLiveData<FetchResult<List<ContactVerifyInfoBean>>> getFetchResult() {
    return resultLiveData;
  }

  public VerifyViewModel() {
    infoObserver =
        new ContactListener() {
          @Override
          public void onFriendChange(
              @NonNull V2FriendChangeType friendChangeType,
              @NonNull List<? extends UserWithFriend> friendList) {
            if (friendChangeType == V2FriendChangeType.Add && friendList.size() > 0) {
              List<ContactVerifyInfoBean> update = new ArrayList<>(updateList);
              List<UserWithFriend> newFriends = new ArrayList<>(friendList);
              for (ContactVerifyInfoBean verifyInfoBean : verifyBeanList) {
                for (UserWithFriend friend : friendList) {
                  if (TextUtils.equals(friend.getAccount(), agreeUserId)) {
                    newFriends.remove(friend);
                  } else if (TextUtils.equals(
                          verifyInfoBean.data.getApplicantAccountId(), friend.getAccount())
                      || TextUtils.equals(
                          verifyInfoBean.data.getRecipientAccountId(), friend.getAccount())) {
                    //接收者是自己，表明是自己在其他端接受了申请，不更新
                    if (!TextUtils.equals(
                        verifyInfoBean.data.getRecipientAccountId(), IMKitClient.account())) {
                      verifyInfoBean.updateStatus(
                          V2NIMFriendAddApplicationStatus
                              .V2NIM_FRIEND_ADD_APPLICATION_STATUS_AGREED);
                      update.add(verifyInfoBean);
                    }
                    newFriends.remove(friend);
                  }
                }
              }
              //更新状态
              if (!update.isEmpty()) {
                fetchResult.setLoadStatus(LoadStatus.Finish);
                fetchResult.setData(update);
                fetchResult.setFetchType(FetchResult.FetchType.Update);
                resultLiveData.setValue(fetchResult);
              }
              if (!newFriends.isEmpty()) {
                List<ContactVerifyInfoBean> addNotify = new ArrayList<>();
                for (UserWithFriend friend : newFriends) {
                  FriendAddApplicationInfo info =
                      new FriendAddApplicationInfo(
                          friend.getAccount(),
                          IMKitClient.account(),
                          friend.getAccount(),
                          null,
                          V2NIMFriendAddApplicationStatus
                              .V2NIM_FRIEND_ADD_APPLICATION_STATUS_AGREED,
                          System.currentTimeMillis(),
                          null,
                          false,
                          null);
                  info.setOperatorUserInfo(
                      new V2UserInfo(friend.getAccount(), friend.getUserInfo()));
                  info.setApplicantUserInfo(
                      new V2UserInfo(IMKitClient.account(), IMKitClient.currentUser()));
                  ContactVerifyInfoBean verifyInfoBean = new ContactVerifyInfoBean(info);
                  verifyBeanList.add(verifyInfoBean);
                  addNotify.add(verifyInfoBean);
                }
                fetchResult.setLoadStatus(LoadStatus.Finish);
                fetchResult.setData(addNotify);
                fetchResult.setFetchType(FetchResult.FetchType.Add);
                resultLiveData.setValue(fetchResult);
              }
            }
          }

          @Override
          public void onFriendAddApplication(@NonNull FriendAddApplicationInfo friendApplication) {
            List<ContactVerifyInfoBean> add;

            List<FriendAddApplicationInfo> list = new ArrayList<>();
            list.add(friendApplication);
            add = mergeSystemMessageList(list);
            // 如果有新的消息合并，需要更新原有消息
            if (updateList.size() > 0) {
              List<ContactVerifyInfoBean> update = new ArrayList<>(updateList);
              updateList.clear();
              fetchResult.setLoadStatus(LoadStatus.Finish);
              fetchResult.setData(update);
              fetchResult.setFetchType(FetchResult.FetchType.Update);
              resultLiveData.setValue(fetchResult);
            }
            //update
            if (add.size() > 0) {
              fetchResult.setLoadStatus(LoadStatus.Finish);
              fetchResult.setData(add);
              fetchResult.setFetchType(FetchResult.FetchType.Add);
              resultLiveData.setValue(fetchResult);
            }
          }

          @Override
          public void onFriendAddRejected(@NonNull FriendAddApplicationInfo rejectionInfo) {
            //如果申请者是被自己拒绝的，则不更新
            if (TextUtils.equals(rejectionInfo.getApplicantAccountId(), disagreeUserId)
                || TextUtils.equals(rejectionInfo.getRecipientAccountId(), IMKitClient.account())) {
              return;
            }
            List<ContactVerifyInfoBean> update = new ArrayList<>();
            update.add(new ContactVerifyInfoBean(rejectionInfo));
            //更新状态
            fetchResult.setLoadStatus(LoadStatus.Finish);
            fetchResult.setData(update);
            fetchResult.setFetchType(FetchResult.FetchType.Add);
            resultLiveData.setValue(fetchResult);
          }
        };
    ContactRepo.addFriendListener(infoObserver);
  }

  public void fetchVerifyList(boolean nextPage) {
    ALog.d(LIB_TAG, TAG, "fetchVerifyList,nextPage:" + nextPage);
    fetchResult.setStatus(LoadStatus.Loading);
    resultLiveData.postValue(fetchResult);
    if (nextPage && !hasMore) {
      return;
    }

    ContactRepo.getNotificationList(
        index,
        PAGE_LIMIT,
        new FetchCallback<>() {
          @Override
          public void onError(int errorCode, @Nullable String errorMsg) {
            ALog.d(LIB_TAG, TAG, "fetchVerifyList,onError:" + errorCode);
            fetchResult.setError(errorCode, errorMsg);
            resultLiveData.setValue(fetchResult);
          }

          @Override
          public void onSuccess(@Nullable FriendAddApplicationResult data) {
            ALog.d(LIB_TAG, TAG, "fetchVerifyList,onSuccess:");
            fetchResult.setStatus(LoadStatus.Success);
            if (data != null && data.getApplications().size() > 0) {
              hasMore = !data.getFinished();
              index = data.getOffset();
              resetMessageStatus(data.getApplications());
              List<ContactVerifyInfoBean> add = mergeSystemMessageList(data.getApplications());
              fetchResult.setData(add);
            } else {
              hasMore = false;
              fetchResult.setData(null);
            }
            resultLiveData.setValue(fetchResult);
          }
        });
  }

  public boolean hasMore() {
    return hasMore;
  }

  public void clearNotify() {
    ALog.d(LIB_TAG, TAG, "clearNotify");
    ContactRepo.clearNotification();
    fetchResult.setLoadStatus(LoadStatus.Finish);
    fetchResult.setFetchType(FetchResult.FetchType.Remove);
    fetchResult.setData(new ArrayList<>(verifyBeanList));
    verifyBeanList.clear();
    resultLiveData.setValue(fetchResult);
  }

  public void agree(ContactVerifyInfoBean bean, FetchCallback<Void> callback) {
    ALog.d(LIB_TAG, TAG, "agree:");
    if (bean == null) {
      return;
    }
    agreeUserId = bean.data.getApplicantAccountId();
    FriendAddApplicationInfo info = bean.data;
    V2NIMFriendAddApplicationStatus status = info.getStatus();
    String account = info.getOperatorAccountId();
    if (status == V2NIMFriendAddApplicationStatus.V2NIM_FRIEND_ADD_APPLICATION_STATUS_INIT
        && !TextUtils.isEmpty(account)
        && info.getNimApplication() != null) {
      ContactRepo.acceptAddFriend(info.getNimApplication(), true, callback);
    }
  }

  public void disagree(ContactVerifyInfoBean bean, FetchCallback<Void> callback) {
    ALog.d(LIB_TAG, TAG, "disagree:");
    if (bean == null || bean.data == null) {
      return;
    }
    disagreeUserId = bean.data.getApplicantAccountId();
    FriendAddApplicationInfo info = bean.data;
    V2NIMFriendAddApplicationStatus status = info.getStatus();
    String account = info.getOperatorAccountId();
    if (status == V2NIMFriendAddApplicationStatus.V2NIM_FRIEND_ADD_APPLICATION_STATUS_INIT
        && !TextUtils.isEmpty(account)
        && info.getNimApplication() != null) {
      ContactRepo.acceptAddFriend(info.getNimApplication(), false, callback);
    }
  }

  public void setVerifyStatus(ContactVerifyInfoBean bean, V2NIMFriendAddApplicationStatus status) {
    if (bean == null || status == null) {
      return;
    }
    ALog.d(LIB_TAG, TAG, "setVerifyStatus:" + status.name());
    bean.updateStatus(status);
  }

  public void resetUnreadCount() {
    ALog.d(LIB_TAG, TAG, "resetUnreadCount");
    ContactRepo.setAddApplicationRead(null);
    for (ContactVerifyInfoBean verifyInfoBean : verifyBeanList) {
      verifyInfoBean.clearUnreadCount();
    }
  }

  private void resetMessageStatus(List<FriendAddApplicationInfo> infoList) {
    ALog.d(LIB_TAG, TAG, "resetMessageStatus:" + (infoList == null ? "null" : infoList.size()));
    if (infoList != null && infoList.size() > 0) {
      long lastTime = System.currentTimeMillis() - expireLimit;
      for (FriendAddApplicationInfo info : infoList) {
        if (info.getStatus()
                == V2NIMFriendAddApplicationStatus.V2NIM_FRIEND_ADD_APPLICATION_STATUS_INIT
            && info.getTime() < lastTime) {
          info.setStatus(
              V2NIMFriendAddApplicationStatus.V2NIM_FRIEND_ADD_APPLICATION_STATUS_EXPIRED);
        }
      }
    }
  }

  private List<ContactVerifyInfoBean> mergeSystemMessageList(
      List<FriendAddApplicationInfo> infoList) {
    List<ContactVerifyInfoBean> add = new ArrayList<>();
    updateList.clear();
    if (infoList != null) {
      for (int index = 0; index < infoList.size(); index++) {
        boolean hasInsert = false;
        for (ContactVerifyInfoBean bean : verifyBeanList) {
          if (bean.pushMessageIfSame(infoList.get(index))) {
            hasInsert = true;
            updateList.add(bean);
            break;
          }
        }
        if (!hasInsert) {
          ContactVerifyInfoBean infoBean = new ContactVerifyInfoBean(infoList.get(index));
          verifyBeanList.add(infoBean);
          add.add(infoBean);
        }
      }
    }
    return add;
  }

  @Override
  protected void onCleared() {
    super.onCleared();
    ContactRepo.removeFriendListener(infoObserver);
  }
}
