/*
 * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.kit.qchatkit.ui.message.viewholder;

import static com.netease.yunxin.kit.qchatkit.ui.message.utils.QChatMessageConstant.MESSAGE_USER_VIEW_TYPE_IMAGE;
import static com.netease.yunxin.kit.qchatkit.ui.message.utils.QChatMessageConstant.MESSAGE_USER_VIEW_TYPE_TEXT;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.netease.yunxin.kit.qchatkit.ui.databinding.QchatBaseMessageViewHolderBinding;

public class QChatMessageViewHolderFactory {
    public QChatBaseMessageViewHolder getViewHolder(@NonNull ViewGroup parent, int viewType) {

        QChatBaseMessageViewHolder viewHolder = null;
        QchatBaseMessageViewHolderBinding viewHolderBinding = QchatBaseMessageViewHolderBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        if (viewType == MESSAGE_USER_VIEW_TYPE_TEXT) {
            viewHolder = new QChatTextMessageViewHolder(viewHolderBinding);
        } else if (viewType == MESSAGE_USER_VIEW_TYPE_IMAGE) {
            viewHolder = new QChatImageMessageViewHolder(viewHolderBinding);
        }

        return viewHolder;
    }
}
