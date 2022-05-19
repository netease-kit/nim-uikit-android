/*
 * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.kit.qchatkit.ui.message.view;

import android.text.TextUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import com.netease.yunxin.kit.qchatkit.ui.databinding.QChatMessageInputLayoutBinding;
import com.netease.yunxin.kit.qchatkit.ui.message.interfaces.IMessageProxy;

/**
 * message input bar layout
 * include send text and image
 */
public class MessageInputLayout {

    private QChatMessageInputLayoutBinding viewBinding;
    private IMessageProxy messageProxy;

    public MessageInputLayout(QChatMessageInputLayoutBinding viewBinding, IMessageProxy proxy){
        this.viewBinding = viewBinding;
        this.messageProxy = proxy;
        initView();
    }

    private void initView(){
        if(viewBinding != null){
            viewBinding.qChatMessageInputEt.setOnEditorActionListener(actionListener);
            viewBinding.qChatMessageImageLayout.setOnClickListener(view ->{
                messageProxy.sendImage();
            });
            viewBinding.qChatMessageEmojiLayout.setOnClickListener( view ->{
                messageProxy.sendEmoji();
            });
            viewBinding.qChatMessageFileLayout.setOnClickListener( view ->{
                messageProxy.sendFile();
            });

            viewBinding.qChatMessageMoreLayout.setOnClickListener( view ->{
                messageProxy.onInputPanelExpand();
            });

            viewBinding.qChatMessageVoiceLayout.setOnClickListener( view ->{
                messageProxy.sendVoice();
            });
        }
    }


    private final EditText.OnEditorActionListener actionListener = (v, actionId, event) -> {
        if (actionId == EditorInfo.IME_ACTION_SEND) {
            String msg = v.getEditableText().toString();
            if (!TextUtils.isEmpty(msg) && messageProxy != null) {
                boolean hasSend = messageProxy.sendTextMessage(msg);
                if (hasSend){
                    v.setText(null);
                }
            }
        }
        return false;
    };


}
