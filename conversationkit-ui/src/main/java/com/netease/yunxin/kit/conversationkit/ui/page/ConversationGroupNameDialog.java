// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.conversationkit.ui.page;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;
import com.netease.yunxin.kit.common.utils.SizeUtils;
import com.netease.yunxin.kit.conversationkit.ui.R;
import com.netease.yunxin.kit.conversationkit.ui.page.viewmodel.ConversationGroupViewModel;
import java.lang.reflect.Field;

public final class ConversationGroupNameDialog {

  public interface OnConfirmListener {
    void onConfirm(String name, Dialog dialog);
  }

  private static final float DIALOG_HEIGHT_RATIO = 0.9F;

  private ConversationGroupNameDialog() {}

  public static void show(
      Context context,
      @StringRes int titleRes,
      @Nullable String initialName,
      @ColorInt int primaryColor,
      OnConfirmListener listener) {
    int screenHeight = context.getResources().getDisplayMetrics().heightPixels;
    int dialogHeight = (int) (screenHeight * DIALOG_HEIGHT_RATIO);
    int dialogTop = screenHeight - dialogHeight;
    LinearLayout root = new LinearLayout(context);
    root.setOrientation(LinearLayout.VERTICAL);
    root.setBackgroundResource(R.drawable.conversation_group_bottom_dialog_bg);
    root.setMinimumHeight(dialogHeight);
    root.setPadding(0, 0, 0, SizeUtils.dp2px(34));

    FrameLayout titleBar = new FrameLayout(context);
    root.addView(
        titleBar,
        new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, SizeUtils.dp2px(56)));

    TextView cancel = new TextView(context);
    cancel.setText(R.string.cancel_title);
    cancel.setGravity(Gravity.CENTER);
    cancel.setTextSize(16);
    cancel.setTextColor(ContextCompat.getColor(context, R.color.color_conversation_secondary_text));
    FrameLayout.LayoutParams cancelParams =
        new FrameLayout.LayoutParams(SizeUtils.dp2px(72), FrameLayout.LayoutParams.MATCH_PARENT);
    cancelParams.gravity = Gravity.START | Gravity.CENTER_VERTICAL;
    titleBar.addView(cancel, cancelParams);

    TextView title = new TextView(context);
    title.setText(titleRes);
    title.setTextSize(17);
    title.setGravity(Gravity.CENTER);
    title.setTextColor(ContextCompat.getColor(context, R.color.color_conversation_primary_text));
    FrameLayout.LayoutParams titleParams =
        new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.MATCH_PARENT);
    titleParams.gravity = Gravity.CENTER;
    titleBar.addView(title, titleParams);

    TextView confirm = new TextView(context);
    confirm.setText(R.string.sure_title);
    confirm.setGravity(Gravity.CENTER);
    confirm.setTextSize(16);
    FrameLayout.LayoutParams confirmParams =
        new FrameLayout.LayoutParams(SizeUtils.dp2px(72), FrameLayout.LayoutParams.MATCH_PARENT);
    confirmParams.gravity = Gravity.END | Gravity.CENTER_VERTICAL;
    titleBar.addView(confirm, confirmParams);

    EditText input = new EditText(context);
    input.setSingleLine(true);
    input.setHint(R.string.conversation_group_name_hint);
    input.setHintTextColor(
        ContextCompat.getColor(context, R.color.color_conversation_secondary_text));
    input.setTextSize(16);
    input.setTextColor(ContextCompat.getColor(context, R.color.color_conversation_primary_text));
    input.setGravity(Gravity.CENTER_VERTICAL);
    input.setPadding(SizeUtils.dp2px(16), 0, SizeUtils.dp2px(48), 0);
    input.setBackground(null);
    applyCursor(context, input);
    input.setFilters(
        new InputFilter[] {
          new InputFilter.LengthFilter(ConversationGroupViewModel.CUSTOM_GROUP_NAME_MAX_LENGTH)
        });
    if (!TextUtils.isEmpty(initialName)) {
      input.setText(initialName);
      input.setSelection(input.getText().length());
    }
    FrameLayout inputRow = new FrameLayout(context);
    inputRow.setBackgroundResource(R.drawable.conversation_group_name_input_bg);
    LinearLayout.LayoutParams inputRowParams =
        new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, SizeUtils.dp2px(52));
    inputRowParams.leftMargin = SizeUtils.dp2px(16);
    inputRowParams.rightMargin = SizeUtils.dp2px(16);
    inputRowParams.topMargin = SizeUtils.dp2px(14);
    root.addView(inputRow, inputRowParams);
    inputRow.addView(
        input,
        new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    ImageView clearView = new ImageView(context);
    clearView.setImageResource(R.drawable.conversation_group_name_clear);
    clearView.setScaleType(ImageView.ScaleType.CENTER);
    clearView.setContentDescription(
        context.getString(R.string.conversation_group_clear_content_description));
    clearView.setVisibility(TextUtils.isEmpty(input.getText()) ? View.GONE : View.VISIBLE);
    clearView.setOnClickListener(v -> input.setText(""));
    FrameLayout.LayoutParams clearParams =
        new FrameLayout.LayoutParams(SizeUtils.dp2px(40), SizeUtils.dp2px(40));
    clearParams.gravity = Gravity.END | Gravity.CENTER_VERTICAL;
    clearParams.rightMargin = SizeUtils.dp2px(4);
    inputRow.addView(clearView, clearParams);

    TextView countView = new TextView(context);
    int initialLength = input.getText() == null ? 0 : input.getText().length();
    countView.setText(
        context.getString(
            R.string.conversation_group_name_count,
            initialLength,
            ConversationGroupViewModel.CUSTOM_GROUP_NAME_MAX_LENGTH));
    countView.setGravity(Gravity.RIGHT);
    countView.setTextSize(12);
    countView.setTextColor(
        ContextCompat.getColor(context, R.color.color_conversation_secondary_text));
    LinearLayout.LayoutParams countParams =
        new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    countParams.leftMargin = SizeUtils.dp2px(16);
    countParams.rightMargin = SizeUtils.dp2px(16);
    countParams.topMargin = SizeUtils.dp2px(8);
    root.addView(countView, countParams);

    Dialog dialog = new Dialog(context, R.style.ConversationGroupBottomDialogTheme);
    dialog.setContentView(
        root, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dialogHeight));
    dialog.setCanceledOnTouchOutside(true);
    updateConfirmState(context, confirm, input.getText(), primaryColor);
    cancel.setOnClickListener(v -> dialog.dismiss());
    confirm.setOnClickListener(
        v -> {
          CharSequence text = input.getText();
          String name = text == null ? "" : text.toString().trim();
          if (TextUtils.isEmpty(name)) {
            return;
          }
          if (listener != null) {
            listener.onConfirm(text.toString(), dialog);
          }
        });
    input.addTextChangedListener(
        new TextWatcher() {
          @Override
          public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

          @Override
          public void onTextChanged(CharSequence s, int start, int before, int count) {
            int length = s == null ? 0 : s.length();
            countView.setText(
                context.getString(
                    R.string.conversation_group_name_count,
                    length,
                    ConversationGroupViewModel.CUSTOM_GROUP_NAME_MAX_LENGTH));
            clearView.setVisibility(length > 0 ? View.VISIBLE : View.GONE);
            updateConfirmState(context, confirm, s, primaryColor);
          }

          @Override
          public void afterTextChanged(Editable s) {}
        });
    dialog.setOnDismissListener(
        dialogInterface -> {
          InputMethodManager manager =
              (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
          if (manager != null) {
            manager.hideSoftInputFromWindow(input.getWindowToken(), 0);
          }
        });
    dialog.show();
    configureWindow(dialog, dialogHeight, dialogTop);
  }

  private static void configureWindow(Dialog dialog, int dialogHeight, int dialogTop) {
    Window window = dialog.getWindow();
    if (window == null) {
      return;
    }
    window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    window.setGravity(Gravity.TOP);
    window.setWindowAnimations(R.style.ConversationGroupBottomDialogAnimation);
    WindowManager.LayoutParams params = window.getAttributes();
    params.width = ViewGroup.LayoutParams.MATCH_PARENT;
    params.height = dialogHeight;
    params.gravity = Gravity.TOP;
    params.y = dialogTop;
    window.setAttributes(params);
    window.setSoftInputMode(
        WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING
            | WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
  }

  private static void updateConfirmState(
      Context context, TextView confirm, @Nullable CharSequence text, @ColorInt int primaryColor) {
    boolean enabled = !TextUtils.isEmpty(text == null ? "" : text.toString().trim());
    confirm.setEnabled(enabled);
    confirm.setTextColor(
        enabled
            ? primaryColor
            : ContextCompat.getColor(context, R.color.color_conversation_secondary_text));
  }

  @SuppressLint("SoonBlockedPrivateApi")
  private static void applyCursor(Context context, EditText input) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      input.setTextCursorDrawable(
          ContextCompat.getDrawable(context, R.drawable.conversation_group_input_cursor));
      return;
    }
    try {
      Field field = TextView.class.getDeclaredField("mCursorDrawableRes");
      field.setAccessible(true);
      field.set(input, R.drawable.conversation_group_input_cursor);
    } catch (Exception ignored) {
    }
  }
}
