// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.qchatkit.ui.message;

import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.common.ui.viewmodel.BaseViewModel;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.corekit.im.model.EventObserver;
import com.netease.yunxin.kit.corekit.im.provider.FetchCallback;
import com.netease.yunxin.kit.qchatkit.repo.QChatChannelRepo;
import com.netease.yunxin.kit.qchatkit.repo.QChatServiceObserverRepo;
import com.netease.yunxin.kit.qchatkit.repo.model.ChannelRemove;
import com.netease.yunxin.kit.qchatkit.repo.model.ChannelUpdate;
import com.netease.yunxin.kit.qchatkit.repo.model.ChannelUpdateWhiteBlackRoleMember;
import com.netease.yunxin.kit.qchatkit.repo.model.QChatChannelInfo;
import com.netease.yunxin.kit.qchatkit.repo.model.QChatServerMemberInfo;
import com.netease.yunxin.kit.qchatkit.repo.model.QChatServerRoleInfo;
import com.netease.yunxin.kit.qchatkit.repo.model.QChatSystemNotificationInfo;
import com.netease.yunxin.kit.qchatkit.repo.model.QChatSystemNotificationTypeInfo;
import com.netease.yunxin.kit.qchatkit.repo.model.ServerMemberApplyAccept;
import com.netease.yunxin.kit.qchatkit.repo.model.ServerMemberInviteAccept;
import com.netease.yunxin.kit.qchatkit.repo.model.ServerMemberKick;
import com.netease.yunxin.kit.qchatkit.repo.model.ServerMemberLeave;
import com.netease.yunxin.kit.qchatkit.repo.model.ServerMemberUpdate;
import com.netease.yunxin.kit.qchatkit.ui.R;
import com.netease.yunxin.kit.qchatkit.ui.message.model.ChannelMemberStatusBean;
import com.netease.yunxin.kit.qchatkit.ui.model.QChatBaseBean;
import com.netease.yunxin.kit.qchatkit.ui.model.QChatConstant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** channel member info view mode fetch channel member */
public class ChannelMemberViewModel extends BaseViewModel {

  private static final String TAG = "ChannelMemberViewModel";
  //channel member live data
  private final MutableLiveData<FetchResult<List<QChatBaseBean>>> resultLiveData =
      new MutableLiveData<>();
  private final FetchResult<List<QChatBaseBean>> fetchResult = new FetchResult<>(LoadStatus.Finish);

  //channel info live data
  private final MutableLiveData<FetchResult<QChatChannelInfo>> channelInfoLiveData =
      new MutableLiveData<>();
  private final FetchResult<QChatChannelInfo> fetchChannelInfo =
      new FetchResult<>(LoadStatus.Finish);

  //member role info live data
  private final MutableLiveData<FetchResult<List<QChatServerRoleInfo>>> memberRoleLiveData =
      new MutableLiveData<>();
  private final FetchResult<List<QChatServerRoleInfo>> memberRoleResult =
      new FetchResult<>(LoadStatus.Finish);

  //channel remove live data
  private final MutableLiveData<FetchResult<List<Long>>> channelLiveData = new MutableLiveData<>();
  private final FetchResult<List<Long>> channelListResult = new FetchResult<>(LoadStatus.Finish);

  private QChatServerMemberInfo lastRoleInfo;
  private long observerChannelId;
  private long observerServerId;
  private boolean roleHasMore = false;

  public MutableLiveData<FetchResult<List<QChatBaseBean>>> getResultLiveData() {
    return resultLiveData;
  }

  public MutableLiveData<FetchResult<List<QChatServerRoleInfo>>> getMemberRoleLiveData() {
    return memberRoleLiveData;
  }

  public MutableLiveData<FetchResult<QChatChannelInfo>> getChannelInfoLiveData() {
    return channelInfoLiveData;
  }

  public MutableLiveData<FetchResult<List<Long>>> getChannelLiveData() {
    return channelLiveData;
  }

  /** fetch member list in channel */
  public void fetchMemberList(long serverId, long channelId) {
    fetchMemberData(serverId, channelId, 0);
  }

  /** fetch member's role info */
  public void fetchMemberRoleList(long serverId, String accId) {
    ALog.d(TAG, "fetchMemberRoleList", "info:" + serverId + "," + accId);
    QChatChannelRepo.fetchServerRolesByAccId(
        serverId,
        accId,
        0,
        QChatConstant.MEMBER_PAGE_SIZE,
        new FetchCallback<List<QChatServerRoleInfo>>() {
          @Override
          public void onSuccess(@Nullable List<QChatServerRoleInfo> param) {
            if (param != null) {
              memberRoleResult.setData(param);
              memberRoleResult.setLoadStatus(LoadStatus.Success);
              memberRoleLiveData.postValue(memberRoleResult);
            }
            ALog.d(TAG, "fetchMemberRoleList", "onSuccess");
          }

          @Override
          public void onFailed(int code) {
            ALog.d(TAG, "fetchMemberRoleList", "onFailed:" + code);
          }

          @Override
          public void onException(@Nullable Throwable exception) {
            String errorMsg = exception != null ? exception.getMessage() : "";
            ALog.d(TAG, "fetchMemberRoleList", "onException:" + errorMsg);
          }
        });
  }

  /** fetch channel info */
  public void fetchChannelInfo(long channelId) {
    QChatChannelRepo.fetchChannelInfo(
        channelId,
        new FetchCallback<List<QChatChannelInfo>>() {
          @Override
          public void onSuccess(@Nullable List<QChatChannelInfo> param) {
            if (param != null && param.size() > 0) {
              ALog.d(TAG, "fetchChannelInfo", "onSuccess:" + param.size());
              fetchChannelInfo.setData(param.get(0));
              fetchChannelInfo.setLoadStatus(LoadStatus.Success);
              channelInfoLiveData.postValue(fetchChannelInfo);
            }
          }

          @Override
          public void onFailed(int code) {
            ALog.d(TAG, "fetchChannelInfo", "onFailed:" + code);
          }

          @Override
          public void onException(@Nullable Throwable exception) {
            String errorMsg = exception != null ? exception.getMessage() : "";
            ALog.d(TAG, "fetchChannelInfo", "onException:" + errorMsg);
          }
        });
  }

  /** register channel change observer include channel remove ,change ,member change and so on */
  public void registerDeleteChannelObserver(long serverId, long channelId) {
    observerChannelId = channelId;
    observerServerId = serverId;
    ALog.d(TAG, "registerDeleteChannelObserver", "info:" + serverId + "," + channelId);
    QChatServiceObserverRepo.observerSystemNotificationWithType(
        notificationObserver,
        Arrays.asList(
            ChannelRemove.INSTANCE,
            ChannelUpdate.INSTANCE,
            ChannelUpdateWhiteBlackRoleMember.INSTANCE,
            ServerMemberInviteAccept.INSTANCE,
            ServerMemberApplyAccept.INSTANCE,
            ServerMemberKick.INSTANCE,
            ServerMemberLeave.INSTANCE,
            ServerMemberUpdate.INSTANCE));
  }

  /** fetch member data */
  private void fetchMemberData(long serverId, long channelId, long offset) {
    QChatChannelRepo.fetchChannelMembers(
        serverId,
        channelId,
        offset,
        QChatConstant.MEMBER_PAGE_SIZE,
        new FetchCallback<List<QChatServerMemberInfo>>() {
          @Override
          public void onSuccess(@Nullable List<QChatServerMemberInfo> param) {
            ArrayList<QChatBaseBean> addList = new ArrayList<>();
            if (param != null && param.size() > 0) {
              for (int index = 0; index < param.size(); index++) {
                QChatServerMemberInfo roleInfo = param.get(index);
                ChannelMemberStatusBean bean = new ChannelMemberStatusBean(roleInfo);
                addList.add(bean);
              }
              lastRoleInfo = param.get(param.size() - 1);
            }
            roleHasMore = param != null && param.size() >= QChatConstant.MEMBER_PAGE_SIZE;
            ALog.d(TAG, "fetchMemberData", "onSuccess:" + addList.size());

            fetchResult.setData(addList);
            if (offset == 0) {
              fetchResult.setLoadStatus(LoadStatus.Success);
            } else {
              fetchResult.setFetchType(FetchResult.FetchType.Add);
              fetchResult.setTypeIndex(-1);
            }
            resultLiveData.postValue(fetchResult);
          }

          @Override
          public void onFailed(int code) {
            ALog.d(TAG, "fetchMemberData", "onFailed:" + code);
            fetchResult.setError(code, R.string.qchat_channel_fetch_member_error);
            resultLiveData.postValue(fetchResult);
          }

          @Override
          public void onException(@Nullable Throwable exception) {
            String errorMsg = exception != null ? exception.getMessage() : "";
            ALog.d(TAG, "fetchMemberData", "onException:" + errorMsg);
            fetchResult.setError(
                QChatConstant.ERROR_CODE_CHANNEL_MEMBER_FETCH,
                R.string.qchat_channel_fetch_member_error);
            resultLiveData.postValue(fetchResult);
          }
        });
  }

  public void loadMore(long serverId, long channelId) {
    long offset = 0;
    if (lastRoleInfo != null) {
      offset = lastRoleInfo.getCreateTime();
    }
    ALog.d(TAG, "loadMore");
    fetchMemberData(serverId, channelId, offset);
  }

  private final EventObserver<List<QChatSystemNotificationInfo>> notificationObserver =
      new EventObserver<List<QChatSystemNotificationInfo>>() {
        @Override
        public void onEvent(@Nullable List<QChatSystemNotificationInfo> eventList) {
          if (eventList == null || eventList.isEmpty()) {
            return;
          }
          for (QChatSystemNotificationInfo item : eventList) {
            if (item == null) {
              continue;
            }
            QChatSystemNotificationTypeInfo type = item.getType();
            long channelId = item.getChannelId() != null ? item.getChannelId() : 0;
            long serverId = item.getServerId() != null ? item.getServerId() : 0;
            ALog.d(
                TAG,
                "notificationObserver",
                "info:" + serverId + "," + channelId + "," + type.toString());
            if (type == ChannelRemove.INSTANCE && channelId == observerChannelId) {
              channelListResult.setFetchType(FetchResult.FetchType.Remove);
              channelLiveData.setValue(channelListResult);
            } else if (type == ChannelUpdate.INSTANCE && channelId == observerChannelId) {
              fetchChannelInfo(observerChannelId);
            } else if (serverId == observerServerId
                    && channelId == observerChannelId
                    && type == ChannelUpdateWhiteBlackRoleMember.INSTANCE
                || type == ServerMemberInviteAccept.INSTANCE
                || type == ServerMemberApplyAccept.INSTANCE
                || type == ServerMemberKick.INSTANCE
                || type == ServerMemberLeave.INSTANCE
                || type == ServerMemberUpdate.INSTANCE) {
              fetchMemberList(observerServerId, observerChannelId);
            }
          }
        }
      };

  public boolean hasMore() {
    return roleHasMore;
  }

  @Override
  protected void onCleared() {
    super.onCleared();
    ALog.d(TAG, "onCleared");
    QChatServiceObserverRepo.observerSystemNotification(notificationObserver, false);
  }
}
