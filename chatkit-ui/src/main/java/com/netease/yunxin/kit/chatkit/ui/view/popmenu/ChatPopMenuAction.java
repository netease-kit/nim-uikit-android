// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.view.popmenu;

import android.view.View;
import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.corekit.im2.IMKitClient;

public class ChatPopMenuAction {

  private String title;
  private @DrawableRes int actionIcon;
  private OnClickListener actionClickListener;
  private final String action;

  public ChatPopMenuAction(String action, String title, @DrawableRes int actionIcon) {
    this(action, title, actionIcon, null);
  }

  public ChatPopMenuAction(
      String action,
      @StringRes int nameRes,
      @DrawableRes int actionIcon,
      OnClickListener actionClickListener) {
    this(
        action,
        IMKitClient.getApplicationContext().getString(nameRes),
        actionIcon,
        actionClickListener);
  }

  public ChatPopMenuAction(
      String action,
      String title,
      @DrawableRes int actionIcon,
      OnClickListener actionClickListener) {
    this.action = action;
    this.title = title;
    this.actionIcon = actionIcon;
    this.actionClickListener = actionClickListener;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getTitle() {
    return title;
  }

  public String getAction() {
    return action;
  }

  public void setIcon(@DrawableRes int actionIcon) {
    this.actionIcon = actionIcon;
  }

  public @DrawableRes int getIcon() {
    return actionIcon;
  }

  public void setActionClickListener(OnClickListener actionClickListener) {
    this.actionClickListener = actionClickListener;
  }

  public OnClickListener getActionClickListener() {
    return actionClickListener;
  }

  @FunctionalInterface
  public interface OnClickListener {
    void onClick(View view, ChatMessageBean messageInfo);
  }
}
