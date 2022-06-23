/*
 * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.kit.conversationkit.ui.model;

import com.netease.yunxin.kit.common.ui.viewholder.BaseBean;
import com.netease.yunxin.kit.conversationkit.model.ConversationInfo;

import java.util.Objects;

public class ConversationBean extends BaseBean {
    private static final String TAG = "ConversationBean";
    public ConversationInfo infoData;

    public ConversationBean(ConversationInfo data) {
        infoData = data;
    }

    public ConversationBean(ConversationInfo data,String router,int viewType) {
        this(data,router,viewType,null,null);
    }

    /**
     *
     * @param data 会话数据
     * @param router 会话跳转的路由地址
     * @param viewType 会话展示的View类型
     * @param paramKey 跳转需要传递的参数KEY
     * @param paramValue 跳转需要传递的参数Value
     */
    public ConversationBean(ConversationInfo data,String router,int viewType,String paramKey,Object paramValue) {
        infoData = data;
        this.router = router;
        this.viewType = viewType;
        this.paramKey = paramKey;
        this.param = paramValue;
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
