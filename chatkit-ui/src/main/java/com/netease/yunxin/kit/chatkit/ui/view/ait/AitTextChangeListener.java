package com.netease.yunxin.kit.chatkit.ui.view.ait;

public interface AitTextChangeListener {

    /**
     * called when @ a team member
     */
    void onTextAdd(String content, int start, int length);

    /**
     * called when delete a @ member
     */
    void onTextDelete(int start, int length);
}
