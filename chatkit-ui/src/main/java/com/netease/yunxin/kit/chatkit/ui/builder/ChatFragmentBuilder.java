/*
 * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.kit.chatkit.ui.builder;

import com.netease.yunxin.kit.chatkit.ui.page.fragment.ChatBaseFragment;
import com.netease.yunxin.kit.chatkit.ui.view.message.ChatMessageViewHolderFactory;

public abstract class ChatFragmentBuilder<T extends ChatBaseFragment> {

    protected ChatMessageViewHolderFactory chatMessageViewHolderFactory;

    protected IChatViewCustom chatViewCustom;

    abstract T getFragment();

    public T build() {
        return getFragment();
    }

    public ChatFragmentBuilder<T> setChatViewCustom(IChatViewCustom chatViewCustom) {
        this.chatViewCustom = chatViewCustom;
        return this;
    }

    public ChatFragmentBuilder<T> setChatMessageViewHolderFactory(ChatMessageViewHolderFactory chatMessageViewHolderFactory) {
        this.chatMessageViewHolderFactory = chatMessageViewHolderFactory;
        return this;
    }

    /**
     * must call when fragment attach to activity
     */
    public void attachFragment(T fragment) {
        fragment.setMessageViewHolderFactory(chatMessageViewHolderFactory);
        fragment.setChatViewCustom(chatViewCustom);
    }
}
