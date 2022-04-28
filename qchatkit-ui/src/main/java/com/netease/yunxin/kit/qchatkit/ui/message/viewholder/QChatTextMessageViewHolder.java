/*
 * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.kit.qchatkit.ui.message.viewholder;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.netease.yunxin.kit.qchatkit.repo.model.QChatMessageInfo;
import com.netease.yunxin.kit.qchatkit.ui.databinding.QChatTextMessageViewHolderBinding;

public class QChatTextMessageViewHolder extends QChatBaseMessageViewHolder {

    private QChatTextMessageViewHolderBinding textBinding;

    public QChatTextMessageViewHolder(@NonNull ViewGroup parent) {
        super(parent);
    }

    @Override
    public void addContainer() {
        textBinding = QChatTextMessageViewHolderBinding.inflate(LayoutInflater.from(getParent().getContext()),
                getContainer(), true);
    }

    @Override
    public void bindData(QChatMessageInfo data, QChatMessageInfo lastMessage) {
        super.bindData(data, lastMessage);
        textBinding.messageText.setText(data.getContent());
        textBinding.messageText.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ClipboardManager cmb = (ClipboardManager) itemView.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText(null, data.getContent());
                cmb.setPrimaryClip(clipData);
                if (optionCallBack != null) {
                    optionCallBack.onCopy(data);
                }
                return true;
            }
        });
    }
}
