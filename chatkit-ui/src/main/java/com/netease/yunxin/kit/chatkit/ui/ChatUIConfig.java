/*
 * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.kit.chatkit.ui;

import com.netease.yunxin.kit.chatkit.ui.builder.IChatViewCustom;
import com.netease.yunxin.kit.chatkit.ui.view.interfaces.IMessageItemClickListener;
import com.netease.yunxin.kit.chatkit.ui.view.message.MessageProperties;

public class ChatUIConfig {

    public IMessageItemClickListener messageItemClickListener;

    public MessageProperties messageProperties;

    public IChatFactory chatFactory;

    public IChatViewCustom chatViewCustom;

}
