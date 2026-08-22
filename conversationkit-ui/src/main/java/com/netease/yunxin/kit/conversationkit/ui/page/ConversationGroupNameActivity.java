// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.conversationkit.ui.page;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import com.netease.nimlib.sdk.v2.conversation.model.V2NIMConversationGroup;
import com.netease.yunxin.kit.common.ui.activities.BaseLocalActivity;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import com.netease.yunxin.kit.common.utils.NetworkUtils;
import com.netease.yunxin.kit.common.utils.SizeUtils;
import com.netease.yunxin.kit.conversationkit.ui.R;
import com.netease.yunxin.kit.conversationkit.ui.common.ConversationGroupErrorHelper;
import com.netease.yunxin.kit.conversationkit.ui.common.ConversationGroupSystemBarHelper;
import com.netease.yunxin.kit.conversationkit.ui.page.viewmodel.ConversationGroupViewModel;
import com.netease.yunxin.kit.corekit.im2.extend.FetchCallback;

public class ConversationGroupNameActivity extends BaseLocalActivity {

  private ConversationGroupViewModel viewModel;
  private EditText input;
  private ImageView clearView;
  private TextView countView;
  private TextView confirmView;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    viewModel = new ViewModelProvider(this).get(ConversationGroupViewModel.class);
    ConversationGroupSystemBarHelper.apply(
        this, ContextCompat.getColor(this, R.color.fun_conversation_secondary_page_bg_color));
    setContentView(createContentView());
    input.requestFocus();
    input.postDelayed(this::showKeyboard, 200);
  }

  private View createContentView() {
    LinearLayout root = new LinearLayout(this);
    root.setOrientation(LinearLayout.VERTICAL);
    root.setBackgroundColor(
        ContextCompat.getColor(this, R.color.fun_conversation_secondary_page_bg_color));

    FrameLayout titleBar = new FrameLayout(this);
    titleBar.setBackgroundColor(
        ContextCompat.getColor(this, R.color.fun_conversation_secondary_page_bg_color));
    root.addView(
        titleBar,
        new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, SizeUtils.dp2px(56)));

    ImageView back = new ImageView(this);
    back.setImageResource(R.drawable.conversation_group_name_back);
    back.setPadding(
        SizeUtils.dp2px(16), SizeUtils.dp2px(16), SizeUtils.dp2px(16), SizeUtils.dp2px(16));
    back.setOnClickListener(v -> finish());
    FrameLayout.LayoutParams backParams =
        new FrameLayout.LayoutParams(SizeUtils.dp2px(56), ViewGroup.LayoutParams.MATCH_PARENT);
    backParams.gravity = Gravity.START | Gravity.CENTER_VERTICAL;
    titleBar.addView(back, backParams);

    TextView title = new TextView(this);
    title.setText(R.string.conversation_group_create);
    title.setTextSize(16);
    title.setGravity(Gravity.CENTER);
    title.setTextColor(ContextCompat.getColor(this, R.color.color_conversation_primary_text));
    FrameLayout.LayoutParams titleParams =
        new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
    titleParams.gravity = Gravity.CENTER;
    titleBar.addView(title, titleParams);

    confirmView = new TextView(this);
    confirmView.setText(R.string.sure_title);
    confirmView.setGravity(Gravity.CENTER);
    confirmView.setTextSize(16);
    confirmView.setOnClickListener(v -> createGroup());
    FrameLayout.LayoutParams confirmParams =
        new FrameLayout.LayoutParams(SizeUtils.dp2px(72), ViewGroup.LayoutParams.MATCH_PARENT);
    confirmParams.gravity = Gravity.END | Gravity.CENTER_VERTICAL;
    titleBar.addView(confirmView, confirmParams);

    LinearLayout inputContainer = new LinearLayout(this);
    inputContainer.setOrientation(LinearLayout.VERTICAL);
    inputContainer.setBackgroundColor(ContextCompat.getColor(this, R.color.color_white));
    inputContainer.setPadding(SizeUtils.dp2px(16), 0, SizeUtils.dp2px(16), SizeUtils.dp2px(12));
    root.addView(
        inputContainer,
        new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, SizeUtils.dp2px(132)));

    FrameLayout inputRow = new FrameLayout(this);
    inputContainer.addView(
        inputRow,
        new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, SizeUtils.dp2px(82)));

    input = new EditText(this);
    input.setSingleLine(true);
    input.setHint(R.string.conversation_group_name_hint);
    input.setHintTextColor(ContextCompat.getColor(this, R.color.fun_conversation_group_hint));
    input.setTextColor(ContextCompat.getColor(this, R.color.color_conversation_primary_text));
    input.setTextSize(16);
    input.setGravity(Gravity.CENTER_VERTICAL);
    input.setBackground(null);
    input.setPadding(0, 0, SizeUtils.dp2px(40), 0);
    input.setFilters(
        new InputFilter[] {
          new InputFilter.LengthFilter(ConversationGroupViewModel.CUSTOM_GROUP_NAME_MAX_LENGTH)
        });
    input.addTextChangedListener(createTextWatcher());
    inputRow.addView(
        input,
        new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

    clearView = new ImageView(this);
    clearView.setImageResource(R.drawable.conversation_group_name_clear);
    clearView.setScaleType(ImageView.ScaleType.CENTER);
    clearView.setVisibility(View.GONE);
    clearView.setOnClickListener(v -> input.setText(""));
    FrameLayout.LayoutParams clearParams =
        new FrameLayout.LayoutParams(SizeUtils.dp2px(32), SizeUtils.dp2px(32));
    clearParams.gravity = Gravity.END | Gravity.CENTER_VERTICAL;
    inputRow.addView(clearView, clearParams);

    countView = new TextView(this);
    countView.setGravity(Gravity.RIGHT);
    countView.setTextSize(14);
    countView.setTextColor(ContextCompat.getColor(this, R.color.fun_conversation_group_hint));
    inputContainer.addView(
        countView,
        new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    updateState();
    return root;
  }

  private TextWatcher createTextWatcher() {
    return new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
        updateState();
      }

      @Override
      public void afterTextChanged(Editable s) {}
    };
  }

  private void updateState() {
    int length = input == null || input.getText() == null ? 0 : input.getText().length();
    countView.setText(
        getString(
            R.string.conversation_group_name_count,
            length,
            ConversationGroupViewModel.CUSTOM_GROUP_NAME_MAX_LENGTH));
    clearView.setVisibility(length > 0 ? View.VISIBLE : View.GONE);
    boolean enabled = !TextUtils.isEmpty(input.getText().toString().trim());
    confirmView.setEnabled(enabled);
    confirmView.setTextColor(
        ContextCompat.getColor(
            this,
            enabled
                ? R.color.fun_conversation_group_primary
                : R.color.fun_conversation_group_primary_disabled));
  }

  private void createGroup() {
    String trimName = input.getText() == null ? "" : input.getText().toString().trim();
    if (TextUtils.isEmpty(trimName)) {
      ToastX.showShortToast(R.string.conversation_group_name_empty_tip);
      return;
    }
    if (viewModel.getCustomGroupCount() >= ConversationGroupViewModel.CUSTOM_GROUP_MAX_COUNT) {
      ToastX.showShortToast(R.string.conversation_group_create_limit_tip);
      return;
    }
    if (!NetworkUtils.isConnected()) {
      ToastX.showShortToast(R.string.conversation_network_error_tip);
      return;
    }
    viewModel.createCustomGroup(
        trimName,
        new FetchCallback<V2NIMConversationGroup>() {
          @Override
          public void onError(int errorCode, @Nullable String errorMsg) {
            ConversationGroupErrorHelper.showErrorToast(
                ConversationGroupNameActivity.this, errorCode);
          }

          @Override
          public void onSuccess(@Nullable V2NIMConversationGroup data) {
            if (data == null) {
              ToastX.showShortToast(R.string.conversation_group_create_failed);
              return;
            }
            finish();
          }
        });
  }

  private void showKeyboard() {
    InputMethodManager manager =
        (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
    if (manager != null) {
      manager.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT);
    }
  }
}
