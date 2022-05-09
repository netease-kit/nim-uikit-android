/*
 * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

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
