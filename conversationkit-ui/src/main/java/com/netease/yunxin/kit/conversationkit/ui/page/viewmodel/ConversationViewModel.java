// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.conversationkit.ui.page.viewmodel;

import static com.netease.yunxin.kit.conversationkit.ui.common.ConversationConstant.LIB_TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.friend.model.MuteListChangedNotify;
import com.netease.nimlib.sdk.msg.constant.DeleteTypeEnum;
import com.netease.nimlib.sdk.msg.model.StickTopSessionInfo;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.model.ConversationInfo;
import com.netease.yunxin.kit.chatkit.repo.ContactObserverRepo;
import com.netease.yunxin.kit.chatkit.repo.ContactRepo;
import com.netease.yunxin.kit.chatkit.repo.ConversationObserverRepo;
import com.netease.yunxin.kit.chatkit.repo.ConversationRepo;
import com.netease.yunxin.kit.chatkit.repo.TeamObserverRepo;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import com.netease.yunxin.kit.common.ui.viewmodel.BaseViewModel;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.conversationkit.ui.ConversationUIConstant;
import com.netease.yunxin.kit.conversationkit.ui.IConversationFactory;
import com.netease.yunxin.kit.conversationkit.ui.R;
import com.netease.yunxin.kit.conversationkit.ui.common.ConversationUtils;
import com.netease.yunxin.kit.conversationkit.ui.model.ConversationBean;
import com.netease.yunxin.kit.conversationkit.ui.page.DefaultViewHolderFactory;
import com.netease.yunxin.kit.corekit.event.EventCenter;
import com.netease.yunxin.kit.corekit.event.EventNotify;
import com.netease.yunxin.kit.corekit.im.custom.AitEvent;
import com.netease.yunxin.kit.corekit.im.custom.AitInfo;
import com.netease.yunxin.kit.corekit.im.model.EventObserver;
import com.netease.yunxin.kit.corekit.im.model.FriendInfo;
import com.netease.yunxin.kit.corekit.im.model.UserInfo;
import com.netease.yunxin.kit.corekit.im.provider.FetchCallback;
import com.netease.yunxin.kit.corekit.im.provider.FriendChangeType;
import com.netease.yunxin.kit.corekit.im.provider.FriendObserver;
import com.netease.yunxin.kit.corekit.im.provider.UserInfoObserver;
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** conversation view model to fetch data or operate conversation */
public class ConversationViewModel extends BaseViewModel {

  private final String TAG = "ConversationViewModel";
  private final String LIB_TAG = "ConversationKit-UI";

  private final MutableLiveData<FetchResult<Integer>> unreadCountLiveData = new MutableLiveData<>();
  private final MutableLiveData<FetchResult<List<ConversationBean>>> queryLiveData =
      new MutableLiveData<>();
  private final MutableLiveData<FetchResult<List<ConversationBean>>> changeLiveData =
      new MutableLiveData<>();
  private final MutableLiveData<FetchResult<ConversationBean>> stickLiveData =
      new MutableLiveData<>();
  private final MutableLiveData<FetchResult<String>> addRemoveStickLiveData =
      new MutableLiveData<>();
  private final MutableLiveData<FetchResult<List<UserInfo>>> userInfoLiveData =
      new MutableLiveData<>();
  private final MutableLiveData<FetchResult<List<FriendInfo>>> friendInfoLiveData =
      new MutableLiveData<>();
  private final MutableLiveData<FetchResult<List<Team>>> teamInfoLiveData = new MutableLiveData<>();
  private final MutableLiveData<FetchResult<MuteListChangedNotify>> muteInfoLiveData =
      new MutableLiveData<>();
  private final MutableLiveData<FetchResult<List<String>>> aitLiveData = new MutableLiveData<>();

  private Comparator<ConversationInfo> comparator;
  private IConversationFactory conversationFactory = new DefaultViewHolderFactory();
  private static final int PAGE_LIMIT = 50;
  private boolean hasMore = true;
  private boolean hasStart = false;

  public ConversationViewModel() {
    //register observer
    ConversationObserverRepo.registerSessionChangedObserver(changeObserver);
    ConversationObserverRepo.registerSessionDeleteObserver(deleteObserver);
    ContactObserverRepo.registerUserInfoObserver(userInfoObserver);
    ContactObserverRepo.registerFriendObserver(friendObserver);
    TeamObserverRepo.registerTeamUpdateObserver(teamUpdateObserver);
    ConversationObserverRepo.registerNotifyChangeObserver(muteObserver);
    ConversationObserverRepo.registerAddStickTopObserver(addStickObserver);
    ConversationObserverRepo.registerRemoveStickTopObserver(removeStickObserver);
    ConversationObserverRepo.registerSyncStickTopObserver(syncStickObserver);
    EventNotify<AitEvent> aitNotify =
        new EventNotify<AitEvent>() {
          @Override
          public void onNotify(@NonNull AitEvent message) {
            AitEvent aitEvent = (AitEvent) message;
            FetchResult<List<String>> result = new FetchResult<>(LoadStatus.Finish);
            ALog.d(LIB_TAG, TAG, "aitNotify，onSuccess:" + aitEvent.getEventType().name());
            if (aitEvent.getEventType() == AitEvent.AitEventType.Arrive
                || aitEvent.getEventType() == AitEvent.AitEventType.Load) {
              result.setFetchType(FetchResult.FetchType.Add);
            } else {
              result.setFetchType(FetchResult.FetchType.Remove);
            }
            List<AitInfo> aitInfoList = aitEvent.getAitInfoList();
            List<String> sessionIdList = new ArrayList<>();
            for (AitInfo info : aitInfoList) {
              sessionIdList.add(info.getSessionId());
            }
            result.setData(sessionIdList);
            aitLiveData.setValue(result);
          }

          @NonNull
          @Override
          public String getEventType() {
            return "AitEvent";
          }
        };
    EventCenter.registerEventNotify(aitNotify);
  }

  /** comparator to sort conversation list */
  public void setComparator(Comparator<ConversationInfo> comparator) {
    this.comparator = comparator;
  }

  /** comparator to sort conversation list */
  public void setConversationFactory(IConversationFactory factory) {
    this.conversationFactory = factory;
  }

  /** query unread count live data */
  public MutableLiveData<FetchResult<Integer>> getUnreadCountLiveData() {
    return unreadCountLiveData;
  }

  /** query conversation live data */
  public MutableLiveData<FetchResult<List<ConversationBean>>> getQueryLiveData() {
    return queryLiveData;
  }

  /** conversation change live data */
  public MutableLiveData<FetchResult<List<ConversationBean>>> getChangeLiveData() {
    return changeLiveData;
  }

  /** conversation stick live data */
  public MutableLiveData<FetchResult<ConversationBean>> getStickLiveData() {
    return stickLiveData;
  }

  /** conversation remove stick live data */
  public MutableLiveData<FetchResult<String>> getAddRemoveStickLiveData() {
    return addRemoveStickLiveData;
  }

  /** userinfo changed live data */
  public MutableLiveData<FetchResult<List<UserInfo>>> getUserInfoLiveData() {
    return userInfoLiveData;
  }

  /** friend changed live data */
  public MutableLiveData<FetchResult<List<FriendInfo>>> getFriendInfoLiveData() {
    return friendInfoLiveData;
  }

  /** team changed live data */
  public MutableLiveData<FetchResult<List<Team>>> getTeamInfoLiveData() {
    return teamInfoLiveData;
  }

  /** mute changed live data */
  public MutableLiveData<FetchResult<MuteListChangedNotify>> getMuteInfoLiveData() {
    return muteInfoLiveData;
  }

  /** conversation @ info */
  public MutableLiveData<FetchResult<List<String>>> getAitLiveData() {
    return aitLiveData;
  }

  public void getUnreadCount() {
    ALog.d(LIB_TAG, TAG, "getUnreadCount");
    ConversationRepo.getMsgUnreadCountAsync(
        new FetchCallback<Integer>() {
          @Override
          public void onSuccess(@Nullable Integer param) {
            ALog.d(LIB_TAG, TAG, "getUnreadCount,onSuccess");
            FetchResult<Integer> fetchResult = new FetchResult<>(LoadStatus.Success);
            fetchResult.setData(param);
            unreadCountLiveData.setValue(fetchResult);
          }

          @Override
          public void onFailed(int code) {
            ALog.e(LIB_TAG, TAG, "getUnreadCount,onFailed" + code);
            ToastX.showShortToast(String.valueOf(code));
          }

          @Override
          public void onException(@Nullable Throwable exception) {
            ALog.e(LIB_TAG, TAG, "getUnreadCount,onException");
          }
        });
  }

  public void fetchConversation() {
    XKitRouter.withKey(RouterConstant.PATH_CHAT_AIT_NOTIFY_ACTION).navigate();
    queryConversation(null);
  }

  public void loadMore(ConversationBean data) {
    if (data != null && data.infoData != null) {
      ALog.d(LIB_TAG, TAG, "loadMore:" + data.infoData.getContactId());
      queryConversation(data.infoData);
    }
  }

  private void queryConversation(ConversationInfo data) {
    ALog.d(LIB_TAG, TAG, "queryConversation:" + (data == null));
    if (hasStart) {
      ALog.d(LIB_TAG, TAG, "queryConversation,has Started return");
      return;
    }
    hasStart = true;
    ConversationRepo.getAllSessionList(
        //        data,
        //        PAGE_LIMIT,
        //        comparator,
        new FetchCallback<List<ConversationInfo>>() {
          @Override
          public void onSuccess(@Nullable List<ConversationInfo> param) {
            FetchResult<List<ConversationBean>> result = new FetchResult<>(LoadStatus.Success);
            if (data != null) {
              result.setLoadStatus(LoadStatus.Finish);
            }
            ALog.d(
                LIB_TAG,
                TAG,
                "queryConversation:onSuccess,size=" + (param != null ? param.size() : 0));
            List<ConversationBean> resultData = new ArrayList<>();
            for (int index = 0; param != null && index < param.size(); index++) {
              resultData.add(conversationFactory.CreateBean(param.get(index)));
            }
            hasMore = false; //param != null && param.size() == PAGE_LIMIT;
            result.setData(resultData);
            queryLiveData.setValue(result);
            hasStart = false;
          }

          @Override
          public void onFailed(int code) {
            ALog.d(LIB_TAG, TAG, "queryConversation,onFailed" + code);
            ToastX.showShortToast(String.valueOf(code));
            hasStart = false;
          }

          @Override
          public void onException(@Nullable Throwable exception) {
            ALog.d(LIB_TAG, TAG, "queryConversation,onException");
            hasStart = false;
          }
        });
  }

  public void deleteConversation(ConversationBean data) {
    ConversationRepo.deleteSession(
        data.infoData.getContactId(),
        data.infoData.getSessionType(),
        DeleteTypeEnum.LOCAL_AND_REMOTE,
        true,
        new FetchCallback<Void>() {
          @Override
          public void onSuccess(@Nullable Void param) {
            ALog.d(LIB_TAG, TAG, "deleteConversation,onSuccess:" + data.infoData.getContactId());
            FetchResult<List<ConversationBean>> result = new FetchResult<>(LoadStatus.Finish);
            result.setFetchType(FetchResult.FetchType.Remove);
            List<ConversationBean> beanList = new ArrayList<>();
            beanList.add(data);
            result.setData(beanList);
            changeLiveData.setValue(result);
          }

          @Override
          public void onFailed(int code) {
            ALog.d(LIB_TAG, TAG, "deleteConversation,onFailed:" + code);
            ToastX.showShortToast(String.valueOf(code));
          }

          @Override
          public void onException(@Nullable Throwable exception) {
            ALog.d(LIB_TAG, TAG, "deleteConversation,onException");
          }
        });
  }

  public void addStickTop(ConversationBean data) {

    ConversationRepo.addStickTop(
        data.infoData.getContactId(),
        data.infoData.getSessionType(),
        "",
        new FetchCallback<StickTopSessionInfo>() {
          @Override
          public void onSuccess(@Nullable StickTopSessionInfo param) {
            if (param != null) {
              FetchResult<ConversationBean> result = new FetchResult<>(LoadStatus.Success);
              data.infoData.setStickTop(true);
              result.setData(data);
              ALog.d(LIB_TAG, TAG, "addStickTop,onSuccess:" + param.getSessionId());
              stickLiveData.setValue(result);
            }
          }

          @Override
          public void onFailed(int code) {
            ALog.d(LIB_TAG, TAG, "addStickTop,onFailed:" + code);
            if (code == ConversationUIConstant.ERROR_CODE_NETWORK) {
              ToastX.showShortToast(R.string.conversation_network_error_tip);
            } else {
              ToastX.showShortToast(String.valueOf(code));
            }
          }

          @Override
          public void onException(@Nullable Throwable exception) {
            ALog.d(LIB_TAG, TAG, "addStickTop,onException");
          }
        });
  }

  public void removeStick(ConversationBean data) {
    ConversationRepo.removeStickTop(
        data.infoData.getContactId(),
        data.infoData.getSessionType(),
        "",
        new FetchCallback<Void>() {
          @Override
          public void onSuccess(@Nullable Void param) {
            FetchResult<ConversationBean> result = new FetchResult<>(LoadStatus.Success);
            data.infoData.setStickTop(false);
            result.setData(data);
            ALog.d(LIB_TAG, TAG, "removeStick,onSuccess:" + data.infoData.getContactId());
            stickLiveData.setValue(result);
          }

          @Override
          public void onFailed(int code) {
            ALog.d(LIB_TAG, TAG, "addStickTop,onFailed:" + code);
            if (code == ConversationUIConstant.ERROR_CODE_NETWORK) {
              ToastX.showShortToast(R.string.conversation_network_error_tip);
            } else {
              ToastX.showShortToast(String.valueOf(code));
            }
          }

          @Override
          public void onException(@Nullable Throwable exception) {}
        });
  }

  private final Observer<StickTopSessionInfo> addStickObserver =
      param -> {
        ALog.d(LIB_TAG, TAG, "addStickObserver，onSuccess:" + param.getSessionId());
        FetchResult<String> result = new FetchResult<>(LoadStatus.Finish);
        result.setFetchType(FetchResult.FetchType.Add);
        result.setData(param.getSessionId());
        addRemoveStickLiveData.setValue(result);
      };

  private final Observer<List<StickTopSessionInfo>> syncStickObserver =
      param -> {
        ALog.d(LIB_TAG, TAG, "syncStickObserver,onSuccess:" + param.size());
        queryConversation(null);
      };

  private final Observer<StickTopSessionInfo> removeStickObserver =
      param -> {
        ALog.d(LIB_TAG, TAG, "removeStickObserver,onSuccess:" + param.getSessionId());
        FetchResult<String> result = new FetchResult<>(LoadStatus.Finish);
        result.setFetchType(FetchResult.FetchType.Remove);
        result.setData(param.getSessionId());
        addRemoveStickLiveData.setValue(result);
      };

  private final EventObserver<List<ConversationInfo>> changeObserver =
      new EventObserver<List<ConversationInfo>>() {
        @Override
        public void onEvent(@Nullable List<ConversationInfo> param) {
          if (param != null) {
            FetchResult<List<ConversationBean>> result = new FetchResult<>(LoadStatus.Success);
            List<ConversationBean> resultData = new ArrayList<>();
            for (int index = 0; index < param.size(); index++) {
              ConversationInfo conversationInfo = param.get(index);
              if (ConversationUtils.isMineLeave(conversationInfo)) {
                deleteConversation(conversationFactory.CreateBean(param.get(index)));
                ALog.d(
                    LIB_TAG,
                    TAG,
                    "changeObserver,DismissTeam,onSuccess:" + param.get(index).getContactId());
                continue;
              } else {
                resultData.add(conversationFactory.CreateBean(param.get(index)));
              }
              ALog.d(
                  LIB_TAG,
                  TAG,
                  "changeObserver,update,onSuccess:" + param.get(index).getContactId());
            }
            result.setData(resultData);
            changeLiveData.setValue(result);
          }
        }
      };

  private final EventObserver<ConversationInfo> deleteObserver =
      new EventObserver<ConversationInfo>() {
        @Override
        public void onEvent(@Nullable ConversationInfo param) {
          ALog.d(LIB_TAG, TAG, "deleteObserver,onSuccess:" + (param == null));
          FetchResult<List<ConversationBean>> result = new FetchResult<>(LoadStatus.Finish);
          result.setFetchType(FetchResult.FetchType.Remove);
          List<ConversationBean> beanList = new ArrayList<>();
          if (param != null) {
            beanList.add(conversationFactory.CreateBean(param));
          }
          result.setData(beanList);
          changeLiveData.setValue(result);
        }
      };

  private final UserInfoObserver userInfoObserver =
      userList -> {
        ALog.d(LIB_TAG, TAG, "userInfoObserver,userList:" + userList.size());
        FetchResult<List<UserInfo>> result = new FetchResult<>(LoadStatus.Success);
        result.setData(userList);
        userInfoLiveData.setValue(result);
      };

  private final FriendObserver friendObserver =
      (friendChangeType, accountList) -> {
        ALog.d(LIB_TAG, TAG, "friendObserver,userList:" + accountList.size());
        if (friendChangeType == FriendChangeType.Update) {
          ContactRepo.getFriendList(
              accountList,
              new FetchCallback<List<FriendInfo>>() {
                @Override
                public void onSuccess(@Nullable List<FriendInfo> param) {
                  ALog.d(
                      LIB_TAG, TAG, "friendObserver,getFriendInfo,onSuccess:" + accountList.size());
                  FetchResult<List<FriendInfo>> result = new FetchResult<>(LoadStatus.Success);
                  result.setData(param);
                  friendInfoLiveData.setValue(result);
                }

                @Override
                public void onFailed(int code) {
                  ALog.d(LIB_TAG, TAG, "friendObserver,getFriendInfo,onFailed:" + code);
                }

                @Override
                public void onException(@Nullable Throwable exception) {
                  ALog.d(
                      LIB_TAG,
                      TAG,
                      "friendObserver,getFriendInfo,onException"
                          + (exception != null ? exception.getMessage() : "null"));
                }
              });
        } else if (friendChangeType == FriendChangeType.Delete) {
          FetchResult<List<FriendInfo>> result = new FetchResult<>(LoadStatus.Success);
          List<FriendInfo> friendList = new ArrayList<>();
          for (String account : accountList) {
            friendList.add(new FriendInfo(account, null, null));
          }
          result.setData(friendList);
          friendInfoLiveData.setValue(result);
        }
      };

  private final Observer<List<Team>> teamUpdateObserver =
      teamList -> {
        if (teamList != null) {
          ALog.d(LIB_TAG, TAG, "teamUpdateObserver,teamInfoList:" + teamList.size());
          FetchResult<List<Team>> result = new FetchResult<>(LoadStatus.Success);
          result.setData(teamList);
          teamInfoLiveData.setValue(result);
        }
      };

  private final Observer<MuteListChangedNotify> muteObserver =
      muteNotify -> {
        if (muteNotify != null) {
          ALog.d(LIB_TAG, TAG, "muteObserver,muteNotify:" + muteNotify.getAccount());
          FetchResult<MuteListChangedNotify> result = new FetchResult<>(LoadStatus.Success);
          result.setData(muteNotify);
          muteInfoLiveData.setValue(result);
        }
      };

  public boolean hasMore() {
    return hasMore;
  }

  @Override
  protected void onCleared() {
    super.onCleared();
    ConversationObserverRepo.unregisterSessionDeleteObserver(deleteObserver);
    ConversationObserverRepo.unregisterSessionChangedObserver(changeObserver);
    ContactObserverRepo.unregisterUserInfoObserver(userInfoObserver);
    ContactObserverRepo.unregisterFriendObserver(friendObserver);
    TeamObserverRepo.unregisterTeamUpdateObserver(teamUpdateObserver);
    ConversationObserverRepo.unregisterNotifyChangeObserver(muteObserver);
    ConversationObserverRepo.unregisterAddStickTopObserver(addStickObserver);
    ConversationObserverRepo.unregisterSyncStickTopObserver(syncStickObserver);
    ConversationObserverRepo.unregisterRemoveStickTopObserver(removeStickObserver);
  }
}
