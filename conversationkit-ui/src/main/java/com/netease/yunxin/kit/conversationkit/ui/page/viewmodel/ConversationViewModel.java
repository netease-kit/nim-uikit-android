/*
 * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.kit.conversationkit.ui.page.viewmodel;

import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;

import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.friend.model.MuteListChangedNotify;
import com.netease.nimlib.sdk.msg.constant.DeleteTypeEnum;
import com.netease.nimlib.sdk.msg.model.StickTopSessionInfo;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.yunxin.kit.common.ui.viewmodel.BaseViewModel;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.conversationkit.model.ConversationInfo;
import com.netease.yunxin.kit.conversationkit.repo.ConversationRepo;
import com.netease.yunxin.kit.conversationkit.ui.IConversationFactory;
import com.netease.yunxin.kit.conversationkit.ui.common.ConversationUtils;
import com.netease.yunxin.kit.conversationkit.ui.model.ConversationBean;
import com.netease.yunxin.kit.conversationkit.ui.common.XLog;
import com.netease.yunxin.kit.conversationkit.ui.page.DefaultViewHolderFactory;
import com.netease.yunxin.kit.corekit.im.model.EventObserver;
import com.netease.yunxin.kit.corekit.im.model.FriendInfo;
import com.netease.yunxin.kit.corekit.im.model.UserInfo;
import com.netease.yunxin.kit.corekit.im.provider.FetchCallback;
import com.netease.yunxin.kit.corekit.im.provider.FriendChangeType;
import com.netease.yunxin.kit.corekit.im.provider.FriendObserver;
import com.netease.yunxin.kit.corekit.im.provider.UserInfoObserver;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * conversation view model
 * to fetch data or operate conversation
 */
public class ConversationViewModel extends BaseViewModel {

    private final String TAG = "ConversationViewModel";
    private final MutableLiveData<FetchResult<List<ConversationBean>>> queryLiveData = new MutableLiveData<>();
    private final MutableLiveData<FetchResult<List<ConversationBean>>> changeLiveData = new MutableLiveData<>();
    private final MutableLiveData<FetchResult<ConversationBean>> stickLiveData = new MutableLiveData<>();
    private final MutableLiveData<FetchResult<String>> addRemoveStickLiveData = new MutableLiveData<>();
    private final MutableLiveData<FetchResult<List<UserInfo>>> userInfoLiveData = new MutableLiveData<>();
    private final MutableLiveData<FetchResult<List<FriendInfo>>> friendInfoLiveData = new MutableLiveData<>();
    private final MutableLiveData<FetchResult<List<Team>>> teamInfoLiveData = new MutableLiveData<>();
    private final MutableLiveData<FetchResult<MuteListChangedNotify>> muteInfoLiveData = new MutableLiveData<>();

    private Comparator<ConversationInfo> comparator;
    private IConversationFactory conversationFactory = new DefaultViewHolderFactory();
    private final static int PAGE_LIMIT = 50;
    private boolean hasMore = true;

    public ConversationViewModel() {
        //register observer
        ConversationRepo.INSTANCE.registerSessionChangedObserver(changeObserver);
        ConversationRepo.INSTANCE.registerSessionDeleteObserver(deleteObserver);
        ConversationRepo.INSTANCE.registerUserInfoObserver(userInfoObserver);
        ConversationRepo.INSTANCE.registerFriendObserver(friendObserver);
        ConversationRepo.INSTANCE.registerTeamUpdateObserver(teamUpdateObserver );
        ConversationRepo.INSTANCE.registerFriendMuteObserver(muteObserver );
        ConversationRepo.INSTANCE.registerAddStickTopObserver(addStickObserver);
        ConversationRepo.INSTANCE.registerRemoveStickTopObserver(removeStickObserver );
    }

    /**
     * comparator to sort conversation list
     */
    public void setComparator(Comparator<ConversationInfo> comparator) {
        this.comparator = comparator;
    }

    /**
     * comparator to sort conversation list
     */
    public void setConversationFactory(IConversationFactory factory) {
        this.conversationFactory = factory;
    }

    /**
     * query conversation live data
     */
    public MutableLiveData<FetchResult<List<ConversationBean>>> getQueryLiveData() {
        return queryLiveData;
    }

    /**
     * conversation change live data
     */
    public MutableLiveData<FetchResult<List<ConversationBean>>> getChangeLiveData() {
        return changeLiveData;
    }

    /**
     * conversation stick live data
     */
    public MutableLiveData<FetchResult<ConversationBean>> getStickLiveData() {
        return stickLiveData;
    }

    /**
     * conversation remove stick live data
     */
    public MutableLiveData<FetchResult<String>> getAddRemoveStickLiveData() {
        return addRemoveStickLiveData;
    }

    /**
     * userinfo changed live data
     */
    public MutableLiveData<FetchResult<List<UserInfo>>> getUserInfoLiveData() {
        return userInfoLiveData;
    }

    /**
     * friend changed live data
     */
    public MutableLiveData<FetchResult<List<FriendInfo>>> getFriendInfoLiveData() {
        return friendInfoLiveData;
    }

    /**
     * team changed live data
     */
    public MutableLiveData<FetchResult<List<Team>>> getTeamInfoLiveData() {
        return teamInfoLiveData;
    }

    /**
     * mute changed live data
     */
    public MutableLiveData<FetchResult<MuteListChangedNotify>> getMuteInfoLiveData() {
        return muteInfoLiveData;
    }

    public int getUnreadCount() {
        return ConversationRepo.INSTANCE.getMsgUnreadCount();
    }

    public void fetchConversation() {
        queryConversation(null);
    }

    public void loadMore(ConversationBean data) {
        if (data != null && data.infoData != null) {
            queryConversation(data.infoData);
        }
    }

    private void queryConversation(ConversationInfo data) {
        ConversationRepo.INSTANCE.getSessionList(data, PAGE_LIMIT, comparator, new FetchCallback<List<ConversationInfo>>() {
            @Override
            public void onSuccess(@Nullable List<ConversationInfo> param) {
                FetchResult<List<ConversationBean>> result = new FetchResult<>(LoadStatus.Success);
                if (data != null) {
                    result.setLoadStatus(LoadStatus.Finish);
                }
                List<ConversationBean> resultData = new ArrayList<>();
                for (int index = 0; param != null && index < param.size(); index++) {
                    resultData.add(conversationFactory.CreateBean(param.get(index)));
                    XLog.d(TAG, "queryConversation:onSuccess", param.get(index).getContactId());
                }
                hasMore = param != null && param.size() == PAGE_LIMIT;
                result.setData(resultData);
                queryLiveData.setValue(result);
            }

            @Override
            public void onFailed(int code) {
            }

            @Override
            public void onException(@Nullable Throwable exception) {

            }
        });
    }

    public void deleteConversation(ConversationBean data) {
        ConversationRepo.INSTANCE.deleteSession(data.infoData.getContactId(), data.infoData.getSessionType(), DeleteTypeEnum.LOCAL_AND_REMOTE, true, new FetchCallback<Void>() {
            @Override
            public void onSuccess(@Nullable Void param) {
                XLog.d(TAG, "deleteConversation:onSuccess", data.infoData.getContactId());
                FetchResult<List<ConversationBean>> result = new FetchResult<>(LoadStatus.Finish);
                result.setFetchType(FetchResult.FetchType.Remove);
                List<ConversationBean> beanList = new ArrayList<>();
                beanList.add(data);
                result.setData(beanList);
                changeLiveData.setValue(result);
            }

            @Override
            public void onFailed(int code) {

            }

            @Override
            public void onException(@Nullable Throwable exception) {

            }
        });
    }

    public void addStickTop(ConversationBean data) {

        ConversationRepo.INSTANCE.addStickTop(data.infoData.getContactId(), data.infoData.getSessionType(), "", new FetchCallback<StickTopSessionInfo>() {
            @Override
            public void onSuccess(@Nullable StickTopSessionInfo param) {
                if (param != null) {
                    FetchResult<ConversationBean> result = new FetchResult<>(LoadStatus.Success);
                    data.infoData.setStickTop(true);
                    result.setData(data);
                    XLog.d(TAG, "addStickTop:onSuccess", param.getSessionId());
                    stickLiveData.setValue(result);
                }
            }

            @Override
            public void onFailed(int code) {

            }

            @Override
            public void onException(@Nullable Throwable exception) {

            }
        });
    }

    public void removeStick(ConversationBean data) {
        ConversationRepo.INSTANCE.removeStickTop(data.infoData.getContactId(), data.infoData.getSessionType(), "", new FetchCallback<Void>() {
            @Override
            public void onSuccess(@Nullable Void param) {
                FetchResult<ConversationBean> result = new FetchResult<>(LoadStatus.Success);
                data.infoData.setStickTop(false);
                result.setData(data);
                XLog.d(TAG, "removeStick:onSuccess", data.infoData.getContactId());
                stickLiveData.setValue(result);
            }

            @Override
            public void onFailed(int code) {

            }

            @Override
            public void onException(@Nullable Throwable exception) {

            }
        });
    }

    private final Observer<StickTopSessionInfo> addStickObserver = param -> {
        XLog.d(TAG, "addStickObserver:onSuccess", param.getSessionId());
        FetchResult<String> result = new FetchResult<>(LoadStatus.Finish);
        result.setFetchType(FetchResult.FetchType.Add);
        result.setData(param.getSessionId());
        addRemoveStickLiveData.setValue(result);
    };

    private final Observer<StickTopSessionInfo> removeStickObserver = param -> {
        XLog.d(TAG, "removeStickObserver:onSuccess", param.getSessionId());
        FetchResult<String> result = new FetchResult<>(LoadStatus.Finish);
        result.setFetchType(FetchResult.FetchType.Remove);
        result.setData(param.getSessionId());
        addRemoveStickLiveData.setValue(result);
    };

    private final EventObserver<List<ConversationInfo>> changeObserver = new EventObserver<List<ConversationInfo>>() {
        @Override
        public void onEvent(@Nullable List<ConversationInfo> param) {
            if (param != null) {
                FetchResult<List<ConversationBean>> result = new FetchResult<>(LoadStatus.Success);
                List<ConversationBean> resultData = new ArrayList<>();
                for (int index = 0; index < param.size(); index++) {
                    ConversationInfo conversationInfo = param.get(index);
                    if (ConversationUtils.isMineLeave(conversationInfo)) {
                        deleteConversation(conversationFactory.CreateBean(param.get(index)));
                        XLog.d(TAG, "changeObserver:onSuccess:DismissTeam", param.get(index).getContactId());
                        continue;
                    } else {
                        resultData.add(conversationFactory.CreateBean(param.get(index)));
                    }
                    resultData.add(conversationFactory.CreateBean(param.get(index)));
                    XLog.d(TAG, "changeObserver:onSuccess:update", param.get(index).getContactId());
                }
                result.setData(resultData);
                changeLiveData.setValue(result);
            }
        }
    };


    private final EventObserver<ConversationInfo> deleteObserver = new EventObserver<ConversationInfo>() {
        @Override
        public void onEvent(@Nullable ConversationInfo param) {
            XLog.d(TAG, "deleteObserver:onSuccess", String.valueOf(param == null));
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

    private final UserInfoObserver userInfoObserver = userList -> {
        XLog.d(TAG, "userInfoObserver", "userList:" + userList.size());
        FetchResult<List<UserInfo>> result = new FetchResult<>(LoadStatus.Success);
        result.setData(userList);
        userInfoLiveData.setValue(result);
    };

    private final FriendObserver friendObserver = (friendChangeType, accountList) -> {
        XLog.d(TAG, "friendObserver", "userList:" + accountList.size());
        if (friendChangeType == FriendChangeType.Update) {
            FetchResult<List<FriendInfo>> result = new FetchResult<>(LoadStatus.Success);
            result.setData(ConversationRepo.INSTANCE.getFriendList(accountList));
            friendInfoLiveData.setValue(result);
        }
    };

    private final Observer<List<Team>> teamUpdateObserver = teamList -> {
        if (teamList != null) {
            XLog.d(TAG, "teamUpdateObserver", "teamInfoList:" + teamList.size());
            FetchResult<List<Team>> result = new FetchResult<>(LoadStatus.Success);
            result.setData(teamList);
            teamInfoLiveData.setValue(result);
        }
    };

    private final Observer<MuteListChangedNotify> muteObserver = muteNotify -> {
        if (muteNotify != null) {
            XLog.d(TAG, "muteObserver", "muteNotify:" + muteNotify.getAccount());
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
        ConversationRepo.INSTANCE.unregisterSessionDeleteObserver(deleteObserver);
        ConversationRepo.INSTANCE.unregisterSessionChangedObserver(changeObserver);
        ConversationRepo.INSTANCE.unregisterUserInfoObserver(userInfoObserver);
        ConversationRepo.INSTANCE.unregisterFriendObserver(friendObserver);
        ConversationRepo.INSTANCE.unregisterTeamUpdateObserver(teamUpdateObserver);
        ConversationRepo.INSTANCE.unregisterFriendMuteObserver(muteObserver);
        ConversationRepo.INSTANCE.unregisterAddStickTopObserver(addStickObserver);
        ConversationRepo.INSTANCE.unregisterRemoveStickTopObserver(removeStickObserver);
    }
}

