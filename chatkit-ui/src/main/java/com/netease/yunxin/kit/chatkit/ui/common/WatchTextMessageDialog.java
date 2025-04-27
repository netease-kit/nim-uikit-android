// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.common;

import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import com.netease.nimlib.sdk.v2.message.enums.V2NIMMessageType;
import com.netease.yunxin.kit.chatkit.model.IMMessageInfo;
import com.netease.yunxin.kit.chatkit.ui.custom.RichTextAttachment;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatMessageDialogLayoutBinding;
import com.netease.yunxin.kit.chatkit.ui.view.MarkDownViwUtils;
import com.netease.yunxin.kit.common.ui.dialog.BaseDialog;
import java.util.Objects;

/** 查看文本消息的弹窗,支持文本消息和富文本消息 PIN页面点击文本消息或者富文本消息，弹出此弹窗 */
public class WatchTextMessageDialog extends BaseDialog {

  private ChatMessageDialogLayoutBinding viewBinding;
  private IMMessageInfo messageInfo;
  private float curEventX;
  private float curEventY;
  private final Integer rootBgRes;

  public WatchTextMessageDialog(Integer rootBgRes) {
    super();
    this.rootBgRes = rootBgRes;
  }

  public WatchTextMessageDialog() {
    this(null);
  }

  public void setMessageInfo(IMMessageInfo message) {
    messageInfo = message;
  }

  @Nullable
  @Override
  protected View getRootView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
    viewBinding = ChatMessageDialogLayoutBinding.inflate(inflater, container, false);
    if (rootBgRes != null) {
      viewBinding.getRoot().setBackgroundResource(rootBgRes);
    }
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

    String content = "";
    String title = "";
    if (messageInfo != null) {
      if (messageInfo.getMessage().getMessageType() == V2NIMMessageType.V2NIM_MESSAGE_TYPE_TEXT) {
        content = messageInfo.getMessage().getText();
      } else if (MessageHelper.isRichTextMsg(messageInfo)) {
        RichTextAttachment attachment = (RichTextAttachment) messageInfo.getAttachment();
        if (attachment != null) {
          content = attachment.body;
          title = attachment.title;
        }
      }
    }
    if (TextUtils.isEmpty(title)) {
      viewBinding.messageTitle.setVisibility(View.GONE);
      viewBinding.message.setGravity(Gravity.CENTER);
    } else {
      viewBinding.messageTitle.setVisibility(View.VISIBLE);
      viewBinding.messageTitle.setText(title);
      viewBinding.messageTitle.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
      viewBinding.message.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
    }
    if (!TextUtils.isEmpty(content)) {
      if (MessageHelper.isAIResponseMessage(messageInfo)) {
        MarkDownViwUtils.makeMarkDown(
            viewBinding.getRoot().getContext(), viewBinding.message, content);
      } else {
        MessageHelper.identifyExpression(
            viewBinding.getRoot().getContext(),
            viewBinding.message,
            content,
            messageInfo.getMessage());
      }
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

  public static WatchTextMessageDialog launchDialog(
      FragmentManager manager, String tag, IMMessageInfo messageInfo) {
    WatchTextMessageDialog messageDialog = new WatchTextMessageDialog();
    messageDialog.show(manager, tag);
    messageDialog.setMessageInfo(messageInfo);
    return messageDialog;
  }

  public static WatchTextMessageDialog launchDialog(
      FragmentManager manager, String tag, IMMessageInfo messageInfo, Integer rootBgRes) {
    WatchTextMessageDialog messageDialog = new WatchTextMessageDialog(rootBgRes);
    messageDialog.show(manager, tag);
    messageDialog.setMessageInfo(messageInfo);
    return messageDialog;
  }
}
