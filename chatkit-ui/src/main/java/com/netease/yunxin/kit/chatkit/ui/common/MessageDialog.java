// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.common;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum;
import com.netease.yunxin.kit.chatkit.model.IMMessageInfo;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatMessageDialogLayoutBinding;
import com.netease.yunxin.kit.common.ui.dialog.BaseDialog;
import java.util.Objects;

public class MessageDialog extends BaseDialog {

  private ChatMessageDialogLayoutBinding viewBinding;
  private IMMessageInfo messageInfo;
  private float curEventX;
  private float curEventY;

  public MessageDialog() {
    super();
  }

  public void setMessageInfo(IMMessageInfo message) {
    messageInfo = message;
  }

  @Nullable
  @Override
  protected View getRootView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
    viewBinding = ChatMessageDialogLayoutBinding.inflate(inflater, container, false);
    viewBinding.message.setOnLongClickListener(
        v -> {
          MessageHelper.copyTextMessage(messageInfo, true);
          return true;
        });
    viewBinding.dialogContainer.setOnClickListener(v -> dismiss());
    viewBinding.message.setOnClickListener(v -> dismiss());
    viewBinding.dialogScrollview.setOnTouchListener(
        (v, event) -> {
          if (event.getAction() == MotionEvent.ACTION_DOWN) {
            curEventX = event.getX();
            curEventY = event.getY();
          }
          if (event.getAction() == MotionEvent.ACTION_UP) {
            if (Math.abs(event.getX() - curEventX) < 5 && Math.abs(event.getY() - curEventY) < 5) {
              return v.performClick();
            }
          }
          return false;
        });

    viewBinding.dialogScrollview.setOnClickListener(v -> dismiss());

    if (messageInfo != null && messageInfo.getMessage().getMsgType() == MsgTypeEnum.text) {

      MessageHelper.identifyExpression(
          viewBinding.getRoot().getContext(), viewBinding.message, messageInfo.getMessage());
    }
    return viewBinding.getRoot();
  }

  @Override
  protected void setStyle() {
    setStyle(STYLE_NORMAL, android.R.style.Theme_Light_NoTitleBar_Fullscreen);
  }

  @Override
  protected void initParams() {
    Window window = Objects.requireNonNull(getDialog()).getWindow();
    WindowManager.LayoutParams layoutParams = window.getAttributes();
    layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
    layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
    window.setAttributes(layoutParams);
    setCancelable(true);
  }

  public static MessageDialog launchDialog(
      FragmentManager manager, String tag, IMMessageInfo messageInfo) {
    MessageDialog messageDialog = new MessageDialog();
    messageDialog.show(manager, tag);
    messageDialog.setMessageInfo(messageInfo);
    return messageDialog;
  }
}
