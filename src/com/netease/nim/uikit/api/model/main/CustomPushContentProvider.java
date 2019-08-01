package com.netease.nim.uikit.api.model.main;

import com.netease.nimlib.sdk.msg.model.IMMessage;

import java.util.Map;

/**
 * 用户自定义推送 content 以及 payload 的接口
 */

public interface CustomPushContentProvider {

    /**
     * 在消息发出去之前，回调此方法，用户需实现自定义的推送文案
     *
     * @param message
     */
    String getPushContent(IMMessage message);

    /**
     * 在消息发出去之前，回调此方法，用户需实现自定义的推送payload，它可以被消息接受者在通知栏点击之后得到
     *
     * @param message
     */
    Map<String, Object> getPushPayload(IMMessage message);

}
