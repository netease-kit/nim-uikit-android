// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.verify;

import android.text.TextUtils;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import com.netease.yunxin.kit.common.ui.viewmodel.BaseViewModel;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.contactkit.repo.ContactRepo;
import com.netease.yunxin.kit.contactkit.ui.model.ContactVerifyInfoBean;
import com.netease.yunxin.kit.corekit.im.model.SystemMessageInfo;
import com.netease.yunxin.kit.corekit.im.model.SystemMessageInfoStatus;
import com.netease.yunxin.kit.corekit.im.model.SystemMessageInfoType;
import com.netease.yunxin.kit.corekit.im.provider.FetchCallback;
import com.netease.yunxin.kit.corekit.im.provider.SystemMessageInfoObserver;
import java.util.ArrayList;
import java.util.List;

public class VerifyViewModel extends BaseViewModel {

  private final int pageSize = 100;
  //7 day expire time
  private final long expireLimit = 604800000;
  private int index = 0;
  private final MutableLiveData<FetchResult<List<ContactVerifyInfoBean>>> resultLiveData =
      new MutableLiveData<>();
  private final FetchResult<List<ContactVerifyInfoBean>> fetchResult =
      new FetchResult<>(LoadStatus.Finish);
  private final List<ContactVerifyInfoBean> verifyBeanList = new ArrayList<>();
  private final SystemMessageInfoObserver infoObserver;

  public MutableLiveData<FetchResult<List<ContactVerifyInfoBean>>> getFetchResult() {
    return resultLiveData;
  }

  public VerifyViewModel() {
    infoObserver =
        info -> {
          if (info.getId() > 0) {
            List<SystemMessageInfo> msgInfo = new ArrayList<>();
            msgInfo.add(info);
            ContactRepo.fillNotification(
                msgInfo,
                new FetchCallback<List<SystemMessageInfo>>() {
                  @Override
                  public void onSuccess(@Nullable List<SystemMessageInfo> param) {
                    List<ContactVerifyInfoBean> add = new ArrayList<>();
                    if (param != null && !param.isEmpty()) {
                      resetMessageStatus(param);
                      for (SystemMessageInfo msg : param) {
                        ContactVerifyInfoBean friendBean = new ContactVerifyInfoBean(msg);
                        verifyBeanList.add(0, friendBean);
                        add.add(friendBean);
                      }
                    }
                    //update
                    fetchResult.setData(add);
                    fetchResult.setFetchType(FetchResult.FetchType.Add);
                    resultLiveData.postValue(fetchResult);
                  }

                  @Override
                  public void onFailed(int code) {
                    fetchResult.setError(code, "");
                    resultLiveData.postValue(fetchResult);
                  }

                  @Override
                  public void onException(@Nullable Throwable exception) {
                    fetchResult.setError(-1, "");
                    resultLiveData.postValue(fetchResult);
                  }
                });
          }
        };
    ContactRepo.registerNotificationObserver(infoObserver);
  }

  public void fetchVerifyList(boolean nextPage) {
    fetchResult.setStatus(LoadStatus.Loading);
    resultLiveData.postValue(fetchResult);
    if (nextPage) {
      index += pageSize;
    } else {
      index = 0;
    }
    ContactRepo.getNotificationList(
        index,
        pageSize,
        new FetchCallback<List<SystemMessageInfo>>() {
          @Override
          public void onSuccess(@Nullable List<SystemMessageInfo> param) {
            if (param != null && param.size() > 0) {
              fetchResult.setStatus(LoadStatus.Success);
              resetMessageStatus(param);
              for (SystemMessageInfo contactInfo : param) {
                ContactVerifyInfoBean friendBean = new ContactVerifyInfoBean(contactInfo);
                verifyBeanList.add(friendBean);
              }
              fetchResult.setData(verifyBeanList);
            } else {
              fetchResult.setData(null);
              fetchResult.setStatus(LoadStatus.Success);
            }
            resultLiveData.postValue(fetchResult);
          }

          @Override
          public void onFailed(int code) {
            fetchResult.setError(code, "");
            resultLiveData.postValue(fetchResult);
          }

          @Override
          public void onException(@Nullable Throwable exception) {
            fetchResult.setError(-1, "");
            resultLiveData.postValue(fetchResult);
          }
        });
  }

  public void clearNotify() {
    ContactRepo.clearNotification();
    fetchResult.setFetchType(FetchResult.FetchType.Remove);
    fetchResult.setData(new ArrayList<>(verifyBeanList));
    verifyBeanList.clear();
    resultLiveData.postValue(fetchResult);
  }

  public void agree(ContactVerifyInfoBean bean, FetchCallback<Void> callback) {
    SystemMessageInfo info = bean.data;
    SystemMessageInfoType type = info.getInfoType();
    SystemMessageInfoStatus status = info.getInfoStatus();
    String account = info.getFromAccount();
    if (status == SystemMessageInfoStatus.Init && !TextUtils.isEmpty(account)) {
      if (type == SystemMessageInfoType.AddFriend) {
        ContactRepo.acceptAddFriend(account, true, callback);
      } else if (type == SystemMessageInfoType.ApplyJoinTeam) {
        ContactRepo.agreeTeamApply(info.getTargetId(), account, callback);
      } else if (type == SystemMessageInfoType.TeamInvite) {
        ContactRepo.acceptTeamInvite(info.getTargetId(), account, callback);
      }
    }
  }

  public void disagree(ContactVerifyInfoBean bean, FetchCallback<Void> callback) {
    SystemMessageInfo info = bean.data;
    SystemMessageInfoType type = info.getInfoType();
    SystemMessageInfoStatus status = info.getInfoStatus();
    String account = info.getFromAccount();
    if (status == SystemMessageInfoStatus.Init && !TextUtils.isEmpty(account)) {
      if (type == SystemMessageInfoType.AddFriend) {
        ContactRepo.acceptAddFriend(info.getFromAccount(), false, callback);

      } else if (type == SystemMessageInfoType.ApplyJoinTeam
          && !TextUtils.isEmpty(info.getTargetId())) {
        ContactRepo.rejectTeamApply(info.getTargetId(), account, "", callback);

      } else if (type == SystemMessageInfoType.TeamInvite
          && !TextUtils.isEmpty(info.getTargetId())) {
        ContactRepo.rejectTeamInvite(info.getTargetId(), account, "", callback);
      }
    }
  }

  public void setVerifyStatus(Long id, SystemMessageInfoStatus status) {
    ContactRepo.setNotificationStatus(id, status);
  }

  public void resetUnreadCount() {
    ContactRepo.clearNotificationUnreadCount();
  }

  private void resetMessageStatus(List<SystemMessageInfo> infoList) {
    if (infoList != null && infoList.size() > 0) {
      long lastTime = System.currentTimeMillis() - expireLimit;
      for (SystemMessageInfo info : infoList) {
        if (info.getInfoStatus() == SystemMessageInfoStatus.Init && info.getTime() < lastTime) {
          info.setInfoStatus(SystemMessageInfoStatus.Expired);
        }
      }
    }
  }

  @Override
  protected void onCleared() {
    super.onCleared();
    ContactRepo.registerNotificationObserver(infoObserver);
  }
}
