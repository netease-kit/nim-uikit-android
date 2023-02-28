// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.qchatkit.ui.message.viewholder;

import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import com.netease.nimlib.sdk.msg.constant.MsgDirectionEnum;
import com.netease.nimlib.sdk.msg.constant.MsgStatusEnum;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;
import com.netease.yunxin.kit.common.ui.utils.AvatarColor;
import com.netease.yunxin.kit.common.ui.utils.TimeFormatUtils;
import com.netease.yunxin.kit.corekit.im.IMKitClient;
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import com.netease.yunxin.kit.qchatkit.repo.QChatUserRepo;
import com.netease.yunxin.kit.qchatkit.repo.model.QChatMessageInfo;
import com.netease.yunxin.kit.qchatkit.ui.R;
import com.netease.yunxin.kit.qchatkit.ui.common.QChatCallback;
import com.netease.yunxin.kit.qchatkit.ui.databinding.QchatBaseMessageViewHolderBinding;
import com.netease.yunxin.kit.qchatkit.ui.message.interfaces.IMessageOptionCallBack;
import com.netease.yunxin.kit.qchatkit.ui.message.view.QChatMessageAdapter;
import java.util.List;

/** base message view holder for qchat */
public abstract class QChatBaseMessageViewHolder extends RecyclerView.ViewHolder {

  private static final int SHOW_TIME_INTERVAL = 5 * 60 * 1000;

  boolean isMine = false;

  IMessageOptionCallBack optionCallBack;

  public QchatBaseMessageViewHolderBinding baseViewBinding;

  public QChatMessageInfo currentMessage;

  public QChatBaseMessageViewHolder(@NonNull QchatBaseMessageViewHolderBinding viewBiding) {
    super(viewBiding.baseRoot);
    baseViewBinding = viewBiding;
    addContainer();
  }

  public void setOptionCallBack(IMessageOptionCallBack callBack) {
    optionCallBack = callBack;
  }

  public void bindData(QChatMessageInfo data, @NonNull List<?> payload) {
    if (!payload.isEmpty()
        && TextUtils.equals(payload.get(0).toString(), QChatMessageAdapter.STATUS_PAYLOAD)) {
      setStatus(data);
    }
    currentMessage = data;
  }

  public void bindData(QChatMessageInfo data, QChatMessageInfo lastMessage) {
    String name =
        TextUtils.isEmpty(data.getFromNick()) ? data.getFromAccount() : data.getFromNick();
    String myAccId = IMKitClient.account();
    currentMessage = data;
    ConstraintLayout.LayoutParams layoutParams =
        (ConstraintLayout.LayoutParams) baseViewBinding.messageBody.getLayoutParams();
    isMine = TextUtils.equals(myAccId, data.getFromAccount());
    if (!isMine) {
      baseViewBinding.fromAvatar.setVisibility(View.VISIBLE);
      QChatUserRepo.fetchUserAvatar(
          data.getFromAccount(),
          new QChatCallback<String>(itemView.getContext()) {
            @Override
            public void onSuccess(@Nullable String param) {
              baseViewBinding.fromAvatar.setData(
                  param, name, AvatarColor.avatarColor(data.getFromAccount()));
            }
          });
      baseViewBinding.avatarMine.setVisibility(View.GONE);
      baseViewBinding.messageContainer.setBackgroundResource(R.drawable.chat_message_other_bg);
      baseViewBinding.messageStatus.setVisibility(View.GONE);
      layoutParams.horizontalBias = 0f;
      baseViewBinding.fromAvatar.setOnClickListener(
          v -> {
            XKitRouter.withKey(RouterConstant.PATH_USER_INFO_PAGE)
                .withContext(v.getContext())
                .withParam(RouterConstant.KEY_ACCOUNT_ID_KEY, data.getFromAccount())
                .navigate();
          });
    } else {
      baseViewBinding.avatarMine.setVisibility(View.VISIBLE);
      NimUserInfo userInfo = IMKitClient.getUserInfo();
      if (userInfo != null) {
        String nickname =
            TextUtils.isEmpty(userInfo.getName()) ? userInfo.getAccount() : userInfo.getName();
        baseViewBinding.avatarMine.setData(
            userInfo.getAvatar(), nickname, AvatarColor.avatarColor(userInfo.getAccount()));
      }
      baseViewBinding.fromAvatar.setVisibility(View.GONE);
      layoutParams.horizontalBias = 1f;
      baseViewBinding.messageContainer.setBackgroundResource(R.drawable.chat_message_self_bg);
      baseViewBinding.messageStatus.setVisibility(View.VISIBLE);
      setStatus(data);
    }
    long createTime = data.getTime() == 0 ? System.currentTimeMillis() : data.getTime();
    if (lastMessage != null && createTime - lastMessage.getTime() < SHOW_TIME_INTERVAL) {
      baseViewBinding.tvTime.setVisibility(View.INVISIBLE);
    } else {
      baseViewBinding.tvTime.setVisibility(View.VISIBLE);
      baseViewBinding.tvTime.setText(
          TimeFormatUtils.formatMillisecond(itemView.getContext(), createTime));
    }
  }

  protected void setStatus(QChatMessageInfo data) {
    if (data.getSendMsgStatus() == MsgStatusEnum.sending) {
      baseViewBinding.messageSending.setVisibility(View.VISIBLE);
      baseViewBinding.ivStatus.setVisibility(View.GONE);
    } else if ((data.getSendMsgStatus() == MsgStatusEnum.fail)) {
      baseViewBinding.ivStatus.setVisibility(View.VISIBLE);
      baseViewBinding.messageSending.setVisibility(View.GONE);
      baseViewBinding.ivStatus.setOnClickListener(
          v -> {
            if (optionCallBack != null) {
              optionCallBack.reSend(data);
            }
          });
    } else {
      baseViewBinding.messageSending.setVisibility(View.GONE);
      baseViewBinding.ivStatus.setVisibility(View.GONE);
    }
  }

  public boolean isReceivedMessage(QChatMessageInfo message) {
    return message.getMessage().getDirect() == MsgDirectionEnum.In;
  }

  public ViewGroup getParent() {
    return baseViewBinding.baseRoot;
  }

  public ViewGroup getContainer() {
    return baseViewBinding.messageContainer;
  }

  public void addContainer() {}

  public void onDetachedFromWindow() {}

  public void onAttachedToWindow() {}
}
