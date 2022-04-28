package com.netease.yunxin.kit.chatkit.ui.view.interfaces;


import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;

import java.util.List;

public interface IMessageLoadHandler {

    /**
     * load more forward
     *
     * @param messageInfo first message
     * @return true as have more message to load,false as have no message to load
     */
    void loadMoreForward(ChatMessageBean messageInfo);

    /**
     * load more background,should append those messages below
     *
     * @param messageInfo last message
     * @return true as have more message to load,false as have no message to load
     */
    void loadMoreBackground(ChatMessageBean messageInfo);

    /**
     * call when visible messages have changed
     *
     * @param messages those message is visible
     */
    void onVisibleItemChange(List<ChatMessageBean> messages);
}
