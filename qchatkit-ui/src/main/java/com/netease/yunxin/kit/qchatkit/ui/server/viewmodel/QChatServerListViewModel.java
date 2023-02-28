// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.qchatkit.ui.server.viewmodel;

import static com.netease.yunxin.kit.qchatkit.ui.model.QChatConstant.ERROR_CODE_SERVER_GET_ITEM;
import static com.netease.yunxin.kit.qchatkit.ui.model.QChatConstant.ERROR_CODE_SERVER_LOAD;

import android.text.TextUtils;
import android.util.Pair;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import com.netease.yunxin.kit.common.ui.viewmodel.BaseViewModel;
import com.netease.yunxin.kit.corekit.im.IMKitClient;
import com.netease.yunxin.kit.corekit.im.model.EventObserver;
import com.netease.yunxin.kit.corekit.im.provider.FetchCallback;
import com.netease.yunxin.kit.corekit.model.ErrorMsg;
import com.netease.yunxin.kit.corekit.model.ResultInfo;
import com.netease.yunxin.kit.qchatkit.observer.ObserverUnreadInfoResultHelper;
import com.netease.yunxin.kit.qchatkit.observer.QChatUnreadInfoSubscriberHelper;
import com.netease.yunxin.kit.qchatkit.repo.QChatChannelRepo;
import com.netease.yunxin.kit.qchatkit.repo.QChatServerRepo;
import com.netease.yunxin.kit.qchatkit.repo.QChatServiceObserverRepo;
import com.netease.yunxin.kit.qchatkit.repo.model.ChannelCreate;
import com.netease.yunxin.kit.qchatkit.repo.model.ChannelRemove;
import com.netease.yunxin.kit.qchatkit.repo.model.ChannelUpdate;
import com.netease.yunxin.kit.qchatkit.repo.model.ChannelUpdateWhiteBlackRole;
import com.netease.yunxin.kit.qchatkit.repo.model.ChannelUpdateWhiteBlackRoleMember;
import com.netease.yunxin.kit.qchatkit.repo.model.QChatServerChannelIdPair;
import com.netease.yunxin.kit.qchatkit.repo.model.QChatServerInfo;
import com.netease.yunxin.kit.qchatkit.repo.model.QChatSystemNotificationInfo;
import com.netease.yunxin.kit.qchatkit.repo.model.QChatSystemNotificationTypeInfo;
import com.netease.yunxin.kit.qchatkit.repo.model.QChatUnreadInfoChangedEventInfo;
import com.netease.yunxin.kit.qchatkit.repo.model.QChatUnreadInfoItem;
import com.netease.yunxin.kit.qchatkit.repo.model.ServerCreate;
import com.netease.yunxin.kit.qchatkit.repo.model.ServerMemberApplyAccept;
import com.netease.yunxin.kit.qchatkit.repo.model.ServerMemberApplyDone;
import com.netease.yunxin.kit.qchatkit.repo.model.ServerMemberInviteAccept;
import com.netease.yunxin.kit.qchatkit.repo.model.ServerMemberInviteDone;
import com.netease.yunxin.kit.qchatkit.repo.model.ServerMemberKick;
import com.netease.yunxin.kit.qchatkit.repo.model.ServerMemberLeave;
import com.netease.yunxin.kit.qchatkit.repo.model.ServerRemove;
import com.netease.yunxin.kit.qchatkit.repo.model.ServerUpdate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** handle server list changed and get data from sdk. */
public final class QChatServerListViewModel extends BaseViewModel {
  public static final int TYPE_SERVER_CREATE = 0;
  public static final int TYPE_SERVER_REMOVE = 1;
  public static final int TYPE_SERVER_UPDATE = 2;
  public static final int TYPE_REFRESH_CHANNEL = 3;
  private static final int LOAD_MORE_LIMIT = 30;

  private final MutableLiveData<ResultInfo<List<QChatServerInfo>>> loadMoreResult =
      new MutableLiveData<>();
  private final MutableLiveData<ResultInfo<List<QChatServerInfo>>> initResult =
      new MutableLiveData<>();
  private final MutableLiveData<ResultInfo<QChatServerInfo>> updateItemResult =
      new MutableLiveData<>();
  private final MutableLiveData<List<Long>> unreadInfoResult = new MutableLiveData<>();

  private final MutableLiveData<Pair<Integer, Long>> onRefreshResult = new MutableLiveData<>();

  private final EventObserver<QChatUnreadInfoChangedEventInfo> unreadInfoChangedEventObserver =
      new EventObserver<QChatUnreadInfoChangedEventInfo>() {
        @Override
        public void onEvent(@Nullable QChatUnreadInfoChangedEventInfo event) {
          if (event != null) {
            unreadInfoResult.setValue(
                ObserverUnreadInfoResultHelper.appendUnreadInfoList(event.getUnreadInfos()));
          }
        }
      };
  /** the receiver of system notification. */
  private final EventObserver<List<QChatSystemNotificationInfo>> notificationObserver =
      new EventObserver<List<QChatSystemNotificationInfo>>() {
        @Override
        public void onEvent(@Nullable List<QChatSystemNotificationInfo> eventList) {
          if (eventList == null || eventList.isEmpty()) {
            return;
          }
          String currentAccount = IMKitClient.account();
          for (QChatSystemNotificationInfo item : eventList) {
            if (item == null || item.getServerId() == null) {
              continue;
            }

            QChatSystemNotificationTypeInfo type = item.getType();
            if ((type == ServerMemberLeave.INSTANCE
                    && TextUtils.equals(currentAccount, item.getFromAccount()))
                || (type == ServerMemberInviteDone.INSTANCE
                    && !TextUtils.equals(currentAccount, item.getFromAccount())
                    && (item.getToAccIds() != null && item.getToAccIds().contains(currentAccount)))
                || (type == ServerMemberApplyDone.INSTANCE
                    && TextUtils.equals(currentAccount, item.getFromAccount()))) {
              // in the branch, the server list may add or reduce some, to refresh and refresh unread info.
              init();
              if (item.getServerId() != null) {
                QChatUnreadInfoSubscriberHelper.fetchServerUnreadInfoCount(
                    new QChatServerInfo(item.getServerId()),
                    result ->
                        unreadInfoResult.setValue(Collections.singletonList(item.getServerId())));
              }
              break;
            } else {
              if (type == ServerRemove.INSTANCE
                  || (type == ServerMemberKick.INSTANCE
                      && !TextUtils.equals(currentAccount, item.getFromAccount())
                      && (item.getToAccIds() != null
                          && item.getToAccIds().contains(currentAccount)))) {
                // in the branch, the server list will remove one.
                onRefreshResult.setValue(new Pair<>(TYPE_SERVER_REMOVE, item.getServerId()));
              } else if (type == ServerUpdate.INSTANCE) {
                // in the branch, the serverInfo was changed, to get the server info from sdk again.
                onRefreshResult.setValue(new Pair<>(TYPE_SERVER_UPDATE, item.getServerId()));
                if (item.getServerId() != null) {
                  QChatServerRepo.fetchServerInfoById(
                      item.getServerId(),
                      new FetchCallback<QChatServerInfo>() {
                        @Override
                        public void onSuccess(@Nullable QChatServerInfo param) {
                          updateItemResult.setValue(new ResultInfo<>(param));
                        }

                        @Override
                        public void onFailed(int code) {
                          updateItemResult.setValue(
                              new ResultInfo<>(null, false, new ErrorMsg(code)));
                        }

                        @Override
                        public void onException(@Nullable Throwable exception) {
                          updateItemResult.setValue(
                              new ResultInfo<>(
                                  null,
                                  false,
                                  new ErrorMsg(ERROR_CODE_SERVER_GET_ITEM, "", exception)));
                        }
                      });
                }
              } else if (type == ServerCreate.INSTANCE) {
                // current the server create one.
                onRefreshResult.setValue(new Pair<>(TYPE_SERVER_CREATE, item.getServerId()));
              } else {
                if (item.getChannelId() != null && item.getServerId() != null) {
                  ObserverUnreadInfoResultHelper.clear(item.getServerId(), item.getChannelId());
                  QChatChannelRepo.fetchChannelUnreadInfoList(
                      Collections.singletonList(
                          new QChatServerChannelIdPair(item.getServerId(), item.getChannelId())),
                      new FetchCallback<List<QChatUnreadInfoItem>>() {
                        @Override
                        public void onSuccess(@Nullable List<QChatUnreadInfoItem> param) {
                          ObserverUnreadInfoResultHelper.appendUnreadInfoList(param);
                          unreadInfoResult.setValue(Collections.singletonList(item.getServerId()));
                        }

                        @Override
                        public void onFailed(int code) {
                          unreadInfoResult.setValue(Collections.singletonList(item.getServerId()));
                        }

                        @Override
                        public void onException(@Nullable Throwable exception) {
                          unreadInfoResult.setValue(Collections.singletonList(item.getServerId()));
                        }
                      });
                }
                onRefreshResult.setValue(new Pair<>(TYPE_REFRESH_CHANNEL, item.getServerId()));
              }
            }
          }
        }
      };

  public QChatServerListViewModel() {
    // observer system notification to refresh server and channel list.
    QChatServiceObserverRepo.observerSystemNotificationWithType(
        notificationObserver,
        Arrays.asList(
            ServerCreate.INSTANCE,
            ServerRemove.INSTANCE,
            ServerUpdate.INSTANCE,
            ChannelCreate.INSTANCE,
            ChannelRemove.INSTANCE,
            ChannelUpdate.INSTANCE,
            ChannelUpdateWhiteBlackRole.INSTANCE,
            ChannelUpdateWhiteBlackRoleMember.INSTANCE,
            ServerMemberApplyAccept.INSTANCE,
            ServerMemberApplyDone.INSTANCE,
            ServerMemberInviteDone.INSTANCE,
            ServerMemberInviteAccept.INSTANCE,
            ServerMemberKick.INSTANCE,
            ServerMemberLeave.INSTANCE));
    // observer unread notification to update unread info.
    QChatServiceObserverRepo.observeUnreadInfoChanged(unreadInfoChangedEventObserver, true);
  }

  public MutableLiveData<ResultInfo<List<QChatServerInfo>>> getLoadMoreResult() {
    return loadMoreResult;
  }

  public MutableLiveData<ResultInfo<List<QChatServerInfo>>> getInitResult() {
    return initResult;
  }

  public MutableLiveData<Pair<Integer, Long>> getOnRefreshResult() {
    return onRefreshResult;
  }

  public MutableLiveData<ResultInfo<QChatServerInfo>> getUpdateItemResult() {
    return updateItemResult;
  }

  public MutableLiveData<List<Long>> getUnreadInfoResult() {
    return unreadInfoResult;
  }

  /** load the first page of server data. */
  public void init() {
    getServerList(0, initResult);
  }

  /** load more server. */
  public void loadMore(long timeTag) {
    getServerList(timeTag, loadMoreResult);
  }

  /** get server list by paging. */
  public void getServerList(
      long timeTag, MutableLiveData<ResultInfo<List<QChatServerInfo>>> result) {
    QChatServerRepo.fetchServerList(
        timeTag,
        LOAD_MORE_LIMIT,
        new FetchCallback<List<QChatServerInfo>>() {
          @Override
          public void onSuccess(@Nullable List<QChatServerInfo> param) {
            result.setValue(new ResultInfo<>(param));
          }

          public void onFailed(int code) {
            result.setValue(new ResultInfo<>(null, false, new ErrorMsg(code)));
          }

          @Override
          public void onException(@Nullable Throwable exception) {
            result.setValue(
                new ResultInfo<>(
                    null,
                    false,
                    new ErrorMsg(ERROR_CODE_SERVER_LOAD, "Error is " + exception, exception)));
          }
        });
  }

  @Override
  protected void onCleared() {
    super.onCleared();
    QChatServiceObserverRepo.observerSystemNotification(notificationObserver, false);
    QChatServiceObserverRepo.observeUnreadInfoChanged(unreadInfoChangedEventObserver, false);
  }
}
