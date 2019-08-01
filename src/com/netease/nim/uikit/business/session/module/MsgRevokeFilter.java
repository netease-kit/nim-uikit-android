package com.netease.nim.uikit.business.session.module;

import com.netease.nimlib.sdk.msg.model.IMMessage;

/**
 * 消息撤回过滤器
 * Created by hzxuwen on 2016/8/22.
 */
public interface MsgRevokeFilter {
    /**
     * 是否过滤该消息的撤回 （默认不过滤）
     *
     * @param message 消息
     * @return 返回true为过滤，返回false则不过滤
     */
    boolean shouldIgnore(IMMessage message);
}
