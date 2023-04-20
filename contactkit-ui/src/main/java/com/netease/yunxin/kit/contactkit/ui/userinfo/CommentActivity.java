// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.contactkit.ui.userinfo;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import androidx.annotation.Nullable;
import com.netease.yunxin.kit.common.ui.activities.BaseActivity;
import com.netease.yunxin.kit.contactkit.ui.R;
import com.netease.yunxin.kit.contactkit.ui.databinding.CommentActivityLayoutBinding;

public class CommentActivity extends BaseActivity {

  public static final String REQUEST_COMMENT_NAME_KEY = "comment";

  private CommentActivityLayoutBinding binding;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    changeStatusBarColor(R.color.color_eff1f4);
    binding = CommentActivityLayoutBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());
    initView();
  }

  private void initView() {
    String comment = getIntent().getStringExtra(REQUEST_COMMENT_NAME_KEY);
    comment = comment == null ? "" : comment;
    binding.edtComment.setText(comment);
    int textLength = binding.edtComment.getBinding().editText.length();
    binding.edtComment.getBinding().editText.setSelection(textLength);
    binding.edtComment.getBinding().editText.requestFocus();
    binding
        .title
        .setTitle(R.string.comment_name)
        .setOnBackIconClickListener(v -> onBackPressed())
        .setActionText(R.string.save)
        .setActionListener(
            v -> {
              Intent intent = new Intent();
              if (!TextUtils.isEmpty(binding.edtComment.getText())) {
                intent.putExtra(REQUEST_COMMENT_NAME_KEY, binding.edtComment.getText());
              }
              setResult(RESULT_OK, intent);
              finish();
            });
  }
}
