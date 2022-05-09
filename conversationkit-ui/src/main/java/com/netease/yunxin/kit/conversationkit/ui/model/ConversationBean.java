/*
 * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.kit.conversationkit.ui.model;

import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.yunxin.kit.common.ui.viewholder.BaseBean;
import com.netease.yunxin.kit.conversationkit.model.ConversationInfo;
import com.netease.yunxin.kit.conversationkit.ui.common.ConversationConstant;
import com.netease.yunxin.kit.conversationkit.ui.common.XLog;
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant;

import java.util.Objects;

public class ConversationBean extends BaseBean {
    private static final String TAG = "ConversationBean";
    public ConversationInfo infoData;

    public ConversationBean(ConversationInfo data) {
        XLog.d(TAG,"ConversationBean","Type:"+data.getSessionType());
        infoData = data;
        if (data.getSessionType() == SessionTypeEnum.P2P) {
            this.router = RouterConstant.PATH_CHAT_P2P;
            this.viewType = ConversationConstant.ViewType.CHAT_VIEW;
            this.paramKey = RouterConstant.CHAT_KRY;
            this.param = data.getUserInfo();
        } else if (data.getSessionType() == SessionTypeEnum.Team
                || data.getSessionType() == SessionTypeEnum.SUPER_TEAM) {
            this.router = RouterConstant.PATH_CHAT_GROUP;
            this.viewType = ConversationConstant.ViewType.TEAM_VIEW;
            this.paramKey = RouterConstant.CHAT_KRY;
            this.param = data.getTeamInfo();
        }

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConversationBean)) return false;
        ConversationBean that = (ConversationBean) o;
        return Objects.equals(infoData.getContactId(), that.infoData.getContactId()) &&
                Objects.equals(infoData.getSessionType(), that.infoData.getSessionType());
    }

    @Override
    public int hashCode() {
        return Objects.hash(infoData.getContactId(), infoData.getSessionType());
    }
}
