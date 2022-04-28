/*
 * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.kit.contactkit.ui.userinfo;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
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
        binding = CommentActivityLayoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initView();
    }

    private void initView() {
        String comment = getIntent().getStringExtra(REQUEST_COMMENT_NAME_KEY);
        binding.edtComment.setText(comment);
        binding.edtComment.setFilter(new InputFilter[]{new InputFilter.LengthFilter(30)});
        binding.title.setTitle(R.string.comment_name)
                .setOnBackIconClickListener(v -> onBackPressed())
                .setActionText(R.string.save)
                .setActionListener(v -> {
                    Intent intent = new Intent();
                    if (!TextUtils.isEmpty(binding.edtComment.getText())) {
                        intent.putExtra(REQUEST_COMMENT_NAME_KEY, binding.edtComment.getText());
                    }
                    setResult(RESULT_OK, intent);
                    finish();
                });
    }
}
