/*
 * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.kit.qchatkit.ui.message.viewholder;

import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.netease.nimlib.sdk.msg.constant.MsgStatusEnum;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;
import com.netease.yunxin.kit.common.ui.message.MessageCommonBaseViewHolder;
import com.netease.yunxin.kit.common.ui.utils.AvatarColor;
import com.netease.yunxin.kit.common.ui.utils.TimeFormatUtils;
import com.netease.yunxin.kit.corekit.im.XKitImClient;
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import com.netease.yunxin.kit.qchatkit.repo.QChatUserRepo;
import com.netease.yunxin.kit.qchatkit.repo.model.QChatMessageInfo;
import com.netease.yunxin.kit.qchatkit.ui.R;
import com.netease.yunxin.kit.qchatkit.ui.common.QChatCallback;
import com.netease.yunxin.kit.qchatkit.ui.message.interfaces.IMessageOptionCallBack;
import com.netease.yunxin.kit.qchatkit.ui.message.view.QChatMessageAdapter;

import java.util.List;

/**
 * base  message view holder for qchat
 */
public abstract class QChatBaseMessageViewHolder extends MessageCommonBaseViewHolder<QChatMessageInfo> {

    private static final int SHOW_TIME_INTERVAL = 5 * 60 * 1000;

    boolean isMine = false;

    IMessageOptionCallBack optionCallBack;

    public QChatBaseMessageViewHolder(@NonNull ViewGroup parent) {
        super(parent);
        addContainer();
    }

    public void setOptionCallBack(IMessageOptionCallBack callBack) {
        optionCallBack = callBack;
    }

    @Override
    public void bindData(QChatMessageInfo data, @NonNull List<?> payload) {
        if (!payload.isEmpty() && TextUtils.equals(payload.get(0).toString(), QChatMessageAdapter.STATUS_PAYLOAD)) {
            setStatus(data);
        }
    }

    @Override
    public void bindData(QChatMessageInfo data, QChatMessageInfo lastMessage) {
        String name = TextUtils.isEmpty(data.getFromNick()) ? data.getFromAccount() : data.getFromNick();
        String myAccId = XKitImClient.account();
        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) getMessageBody().getLayoutParams();
        isMine = TextUtils.equals(myAccId, data.getFromAccount());
        if (!isMine) {
            getFromAvatar().setVisibility(View.VISIBLE);
            QChatUserRepo.fetchUserAvatar(data.getFromAccount(), new QChatCallback<String>(itemView.getContext()) {
                @Override
                public void onSuccess(@Nullable String param) {
                    getFromAvatar().setData(param, name, AvatarColor.avatarColor(data.getFromAccount()));
                }
            });
            getMyAvatar().setVisibility(View.GONE);
            getContainer().setBackgroundResource(R.drawable.chat_message_other_bg);
            getMessageStatus().setVisibility(View.GONE);
            layoutParams.horizontalBias = 0f;
            getFromAvatar().setOnClickListener(v ->{
                XKitRouter.withKey(RouterConstant.PATH_USER_INFO_ACTIVITY)
                        .withContext(v.getContext())
                        .withParam(RouterConstant.KEY_ACCOUNT_ID_KEY, data.getFromAccount())
                        .navigate();
            });
        } else {
            getMyAvatar().setVisibility(View.VISIBLE);
            NimUserInfo userInfo = XKitImClient.getUserInfo();
            if (userInfo != null) {
                String nickname = TextUtils.isEmpty(userInfo.getName()) ? userInfo.getAccount() : userInfo.getName();
                getMyAvatar().setData(userInfo.getAvatar(), nickname, AvatarColor.avatarColor(userInfo.getAccount()));
            }
            getFromAvatar().setVisibility(View.GONE);
            layoutParams.horizontalBias = 1f;
            getContainer().setBackgroundResource(R.drawable.chat_message_self_bg);
            getMessageStatus().setVisibility(View.VISIBLE);
            setStatus(data);
        }
        long createTime = data.getTime() == 0 ? System.currentTimeMillis() : data.getTime();
        if (lastMessage != null
                && createTime - lastMessage.getTime() < SHOW_TIME_INTERVAL) {
            getTvTime().setVisibility(View.INVISIBLE);
        } else {
            getTvTime().setVisibility(View.VISIBLE);
            getTvTime().setText(TimeFormatUtils.formatMillisecond(itemView.getContext(), createTime));
        }

    }

    protected void setStatus(QChatMessageInfo data) {
        if (data.getSendMsgStatus() == MsgStatusEnum.sending) {
            getMessageSending().setVisibility(View.VISIBLE);
            getIvStatus().setVisibility(View.GONE);
        } else if ((data.getSendMsgStatus() == MsgStatusEnum.fail)) {
            getIvStatus().setVisibility(View.VISIBLE);
            getMessageSending().setVisibility(View.GONE);
            getIvStatus().setOnClickListener(v -> {
                if (optionCallBack != null) {
                    optionCallBack.reSend(data);
                }
            });
        } else {
            getMessageSending().setVisibility(View.GONE);
            getIvStatus().setVisibility(View.GONE);
        }
    }
}
