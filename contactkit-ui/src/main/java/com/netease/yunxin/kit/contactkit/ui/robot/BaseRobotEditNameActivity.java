// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.robot;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.Nullable;
import com.netease.yunxin.kit.common.ui.activities.BaseLocalActivity;
import com.netease.yunxin.kit.common.ui.widgets.BackTitleBar;
import com.netease.yunxin.kit.corekit.im2.utils.RouterConstant;

/**
 * 机器人名称编辑页基类
 *
 * <p>子类在 {@link #initViews()} 中完成布局 inflate、setContentView，并给以下字段赋值。
 */
public abstract class BaseRobotEditNameActivity extends BaseLocalActivity {

  /** 默认字数上限，子类可覆写 */
  protected int getMaxNameLength() {
    return 30;
  }

  // ---------- 子类必须在 initViews() 中赋值的 View 字段 ----------
  /** 使用 BackTitleBar 的子类（如 Fun 版）赋值此字段；使用自定义标题行的子类赋值 ivBack */
  protected BackTitleBar titleBar;
  /** 使用自定义返回图标的子类（如 Normal 版）赋值此字段 */
  protected View ivBack;

  protected EditText etName;
  protected ImageView ivClear;
  protected TextView tvSave;
  protected TextView tvCount;
  // ---------------------------------------------------------------

  private String originalName;

  @SuppressLint("SetTextI18n")
  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    initViews();
    configTitle(titleBar);

    originalName = getIntent().getStringExtra(RouterConstant.KEY_ROBOT_NAME);

    String maxCountStr = "/" + getMaxNameLength();
    if (!TextUtils.isEmpty(originalName)) {
      etName.setText(originalName);
      etName.setSelection(originalName.length());
      ivClear.setVisibility(View.VISIBLE);
      tvCount.setText(originalName.length() + maxCountStr);
      tvSave.setEnabled(true);
      tvSave.setAlpha(1f);
    } else {
      tvCount.setText("0" + maxCountStr);
      tvSave.setEnabled(false);
      tvSave.setAlpha(0.5f);
    }

    ivClear.setOnClickListener(v -> etName.setText(""));

    etName.addTextChangedListener(
        new TextWatcher() {
          @Override
          public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

          @Override
          public void onTextChanged(CharSequence s, int start, int before, int count) {}

          @SuppressLint("SetTextI18n")
          @Override
          public void afterTextChanged(Editable s) {
            boolean empty = TextUtils.isEmpty(String.valueOf(s).trim());
            ivClear.setVisibility(empty ? View.GONE : View.VISIBLE);
            tvSave.setEnabled(!empty);
            tvSave.setAlpha(empty ? 0.5f : 1f);
            tvCount.setText(String.valueOf(s).length() + "/" + getMaxNameLength());
          }
        });

    // ivBack 由使用自定义标题行的子类赋值（Normal 版）
    if (ivBack != null) {
      ivBack.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
    }
    tvSave.setOnClickListener(v -> onSaveClick());
    etName.requestFocus();
    getWindow()
        .setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
  }

  /** 子类实现：inflate 布局、setContentView，并给所有 View 字段赋值 */
  protected abstract void initViews();

  /** 子类可覆写定制标题栏（使用 BackTitleBar 的子类覆写此方法） */
  protected void configTitle(BackTitleBar bar) {
    if (bar != null) {
      bar.setOnBackIconClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
    }
  }

  private void onSaveClick() {
    String newName = etName.getText().toString().trim();
    if (TextUtils.isEmpty(newName)) return;
    Intent intent = new Intent();
    intent.putExtra(BaseRobotEditActivity.KEY_RESULT_NAME, newName);
    setResult(RESULT_OK, intent);
    finish();
  }
}
