// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.view.message.viewholder.options;

import androidx.annotation.NonNull;
import java.util.Objects;

public class ChatMessageViewHolderUIOptions {
  public final CommonUIOption commonUIOption;
  public final RevokeUIOption revokeUIOption;
  public final MessageStatusUIOption messageStatusUIOption;
  public final ReplayUIOption replayUIOption;
  public final SignalUIOption signalUIOption;
  public final UserInfoUIOption userInfoUIOption;

  public ChatMessageViewHolderUIOptions(
      @NonNull CommonUIOption commonUIOption,
      @NonNull RevokeUIOption revokeUIOption,
      @NonNull MessageStatusUIOption messageStatusUIOption,
      @NonNull ReplayUIOption replayUIOption,
      @NonNull SignalUIOption signalUIOption,
      @NonNull UserInfoUIOption userInfoUIOption) {
    Objects.requireNonNull(commonUIOption);
    Objects.requireNonNull(revokeUIOption);
    Objects.requireNonNull(messageStatusUIOption);
    Objects.requireNonNull(replayUIOption);
    Objects.requireNonNull(signalUIOption);
    Objects.requireNonNull(userInfoUIOption);
    this.commonUIOption = commonUIOption;
    this.revokeUIOption = revokeUIOption;
    this.messageStatusUIOption = messageStatusUIOption;
    this.replayUIOption = replayUIOption;
    this.signalUIOption = signalUIOption;
    this.userInfoUIOption = userInfoUIOption;
  }

  public static ChatMessageViewHolderUIOptions.Builder wrapExitsOptions(
      ChatMessageViewHolderUIOptions uiOptions) {
    ChatMessageViewHolderUIOptions.Builder builder = new ChatMessageViewHolderUIOptions.Builder();
    if (uiOptions == null) {
      return builder;
    }
    builder
        .messageStatusUIOption(uiOptions.messageStatusUIOption)
        .signalUIOption(uiOptions.signalUIOption)
        .commonUIOption(uiOptions.commonUIOption)
        .replayUIOption(uiOptions.replayUIOption)
        .revokeUIOption(uiOptions.revokeUIOption)
        .userInfoUIOption(uiOptions.userInfoUIOption);
    return builder;
  }

  @Override
  public String toString() {
    return "ChatMessageViewHolderUIOptions{"
        + "commonUIOption="
        + commonUIOption
        + ", revokeUIOption="
        + revokeUIOption
        + ", messageStatusUIOption="
        + messageStatusUIOption
        + ", replayUIOption="
        + replayUIOption
        + ", signalUIOption="
        + signalUIOption
        + ", userInfoUIOption="
        + userInfoUIOption
        + '}';
  }

  public static class Builder {
    private CommonUIOption commonUIOption = new CommonUIOption();
    private RevokeUIOption revokeUIOption = new RevokeUIOption();
    private MessageStatusUIOption messageStatusUIOption = new MessageStatusUIOption();
    private ReplayUIOption replayUIOption = new ReplayUIOption();
    private SignalUIOption signalUIOption = new SignalUIOption();
    private UserInfoUIOption userInfoUIOption = new UserInfoUIOption();

    public Builder commonUIOption(@NonNull CommonUIOption option) {
      Objects.requireNonNull(option, "CommonUIOption");
      this.commonUIOption = option;
      return this;
    }

    public Builder revokeUIOption(@NonNull RevokeUIOption option) {
      Objects.requireNonNull(option, "RevokeUIOption");
      this.revokeUIOption = option;
      return this;
    }

    public Builder messageStatusUIOption(@NonNull MessageStatusUIOption option) {
      Objects.requireNonNull(option, "MessageStatusUIOption");
      this.messageStatusUIOption = option;
      return this;
    }

    public Builder replayUIOption(@NonNull ReplayUIOption option) {
      Objects.requireNonNull(option, "ReplayUIOption");
      this.replayUIOption = option;
      return this;
    }

    public Builder signalUIOption(@NonNull SignalUIOption option) {
      Objects.requireNonNull(option, "SignalUIOption");
      this.signalUIOption = option;
      return this;
    }

    public Builder userInfoUIOption(@NonNull UserInfoUIOption option) {
      Objects.requireNonNull(option, "UserInfoUIOption");
      this.userInfoUIOption = option;
      return this;
    }

    public ChatMessageViewHolderUIOptions build() {
      return new ChatMessageViewHolderUIOptions(
          commonUIOption,
          revokeUIOption,
          messageStatusUIOption,
          replayUIOption,
          signalUIOption,
          userInfoUIOption);
    }
  }
}
