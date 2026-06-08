// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.common;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.netease.nimlib.sdk.v2.topic.V2NIMTopic;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatBotSubSessionActionDialogBinding;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatBotSubSessionDeleteDialogBinding;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatBotSubSessionRenameDialogBinding;
import com.netease.yunxin.kit.chatkit.ui.normal.page.ChatBotSubSessionActivity;
import com.netease.yunxin.kit.chatkit.ui.page.viewmodel.ChatBotSubSessionViewModel;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import com.netease.yunxin.kit.common.utils.NetworkUtils;
import com.netease.yunxin.kit.corekit.im2.extend.FetchCallback;

public final class BotSubSessionMoreActionHelper {

  private static final int TOPIC_NAME_MAX_LENGTH = 20;

  private BotSubSessionMoreActionHelper() {}

  public static void showMoreActionDialog(
      @NonNull Context context,
      @Nullable V2NIMTopic topic,
      @NonNull ChatBotSubSessionViewModel viewModel) {
    if (topic == null) {
      return;
    }
    ChatBotSubSessionActionDialogBinding binding =
        ChatBotSubSessionActionDialogBinding.inflate(LayoutInflater.from(context));
    Dialog dialog = new Dialog(context);
    dialog.setContentView(binding.getRoot());
    if (dialog.getWindow() != null) {
      dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
      dialog.getWindow().setGravity(Gravity.BOTTOM);
      dialog
          .getWindow()
          .setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }
    dialog.setCanceledOnTouchOutside(true);
    binding.topicTitle.setText(BotSubSessionUtils.getTopicTitle(context, topic));
    binding.renameButton.setOnClickListener(
        v -> {
          dialog.dismiss();
          showRenameDialog(context, topic, viewModel);
        });
    binding.deleteButton.setOnClickListener(
        v -> {
          dialog.dismiss();
          showDeleteDialog(context, topic, viewModel);
        });
    binding.cancelButton.setOnClickListener(v -> dialog.dismiss());
    dialog.show();
  }

  private static void showRenameDialog(
      @NonNull Context context,
      @NonNull V2NIMTopic topic,
      @NonNull ChatBotSubSessionViewModel viewModel) {
    ChatBotSubSessionRenameDialogBinding binding =
        ChatBotSubSessionRenameDialogBinding.inflate(LayoutInflater.from(context));
    EditText input = binding.nameInput;
    input.setSingleLine(true);
    input.setImeOptions(EditorInfo.IME_ACTION_DONE);
    input.setFilters(new InputFilter[] {new InputFilter.LengthFilter(TOPIC_NAME_MAX_LENGTH)});
    input.setHint(R.string.chat_bot_sub_session_input_name);
    input.setText(BotSubSessionUtils.getTopicTitle(context, topic));
    input.setSelectAllOnFocus(true);
    Dialog dialog = createCenterDialog(context, binding.getRoot());
    binding.cancelButton.setOnClickListener(v -> dialog.dismiss());
    View.OnClickListener saveListener =
        v -> {
          String newName = input.getText() == null ? "" : input.getText().toString().trim();
          if (TextUtils.isEmpty(newName)) {
            ToastX.showShortToast(R.string.chat_bot_sub_session_input_name);
            return;
          }
          if (!NetworkUtils.isConnected()) {
            ToastX.showShortToast(R.string.chat_network_error_tip);
            return;
          }
          viewModel.renameTopic(
              newName,
              new FetchCallback<V2NIMTopic>() {
                @Override
                public void onError(int errorCode, @Nullable String errorMsg) {
                  ToastX.showShortToast(R.string.chat_bot_sub_session_rename_failed);
                }

                @Override
                public void onSuccess(@Nullable V2NIMTopic data) {
                  dialog.dismiss();
                }
              });
        };
    binding.saveButton.setOnClickListener(saveListener);
    input.setOnEditorActionListener(
        (v, actionId, event) -> {
          if (actionId == EditorInfo.IME_ACTION_DONE) {
            saveListener.onClick(binding.saveButton);
            return true;
          }
          return false;
        });
    dialog.setOnShowListener(
        shownDialog -> {
          input.requestFocus();
          input.post(
              () -> {
                InputMethodManager imm =
                    (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                  imm.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT);
                }
              });
        });
    dialog.show();
  }

  private static void showDeleteDialog(
      @NonNull Context context,
      @NonNull V2NIMTopic topic,
      @NonNull ChatBotSubSessionViewModel viewModel) {
    showDeleteConfirmDialog(
        context, BotSubSessionUtils.getTopicTitle(context, topic), viewModel::deleteTopic);
  }

  public static void showDeleteConfirmDialog(
      @NonNull Context context, @NonNull String topicName, @NonNull Runnable confirmAction) {
    ChatBotSubSessionDeleteDialogBinding binding =
        ChatBotSubSessionDeleteDialogBinding.inflate(LayoutInflater.from(context));
    Dialog dialog = createCenterDialog(context, binding.getRoot());
    binding.dialogTitle.setText(R.string.chat_bot_sub_session_delete_title);
    binding.dialogMessage.setText(
        context.getString(R.string.chat_bot_sub_session_delete_topic, topicName));
    binding.deleteButton.setBackgroundResource(getConfirmButtonBackgroundRes(context));
    binding.cancelButton.setOnClickListener(v -> dialog.dismiss());
    binding.deleteButton.setOnClickListener(
        v -> {
          dialog.dismiss();
          confirmAction.run();
        });
    dialog.show();
  }

  private static int getConfirmButtonBackgroundRes(@NonNull Context context) {
    if (context instanceof ChatBotSubSessionActivity) {
      return ((ChatBotSubSessionActivity) context).getRenameConfirmButtonBackgroundRes();
    }
    return R.drawable.chat_bot_sub_session_rename_confirm_bg;
  }

  private static Dialog createCenterDialog(@NonNull Context context, @NonNull View contentView) {
    Dialog dialog = new Dialog(context);
    dialog.setContentView(contentView);
    dialog.setCanceledOnTouchOutside(true);
    if (dialog.getWindow() != null) {
      dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
      dialog
          .getWindow()
          .setLayout(
              (int) (context.getResources().getDisplayMetrics().widthPixels * 0.7f),
              ViewGroup.LayoutParams.WRAP_CONTENT);
    }
    return dialog;
  }
}
