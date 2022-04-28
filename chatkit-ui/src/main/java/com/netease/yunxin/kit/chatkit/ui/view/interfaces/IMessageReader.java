package com.netease.yunxin.kit.chatkit.ui.view.interfaces;

import com.netease.yunxin.kit.chatkit.model.IMMessageInfo;

public interface IMessageReader {
    /**
     * call when a message have been read
     *
     * @param message read message
     */
    void messageRead(IMMessageInfo message);

}
