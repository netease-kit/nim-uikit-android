/*
 * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.kit.chatkit.ui.page.viewmodel;

import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;

import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.msg.model.TeamMsgAckInfo;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.repo.ChatMessageRepo;
import com.netease.yunxin.kit.chatkit.ui.common.ChatCallback;
import com.netease.yunxin.kit.common.ui.viewmodel.BaseViewModel;

/**
 * chat read state info vide model
 * fetch team read state info to read state page
 */
public class ChatReadStateViewModel extends BaseViewModel {
    private static final String TAG = "ChatReadStateViewModel";

    private final MutableLiveData<TeamMsgAckInfo> teamAckInfo = new MutableLiveData<>();

    public void fetchTeamAckInfo(IMMessage message) {
        ALog.i(TAG, "fetchTeamAckInfo");
        ChatMessageRepo.fetchTeamMessageReceiptDetail(message, new ChatCallback<TeamMsgAckInfo>() {
            @Override
            public void onSuccess(@Nullable TeamMsgAckInfo param) {
                teamAckInfo.postValue(param);
            }
        });
    }

    public MutableLiveData<TeamMsgAckInfo> getTeamAckInfoLiveData() {
        return teamAckInfo;
    }
}
