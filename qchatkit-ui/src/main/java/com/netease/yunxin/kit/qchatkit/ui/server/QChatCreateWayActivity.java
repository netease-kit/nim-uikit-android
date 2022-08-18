// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.qchatkit.ui.server;

import android.content.Intent;
import android.os.Bundle;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import com.netease.yunxin.kit.common.ui.activities.BaseActivity;
import com.netease.yunxin.kit.qchatkit.ui.R;
import com.netease.yunxin.kit.qchatkit.ui.databinding.QChatCreateServerWayActivityBinding;

/** The entrance of creating server. */
public class QChatCreateWayActivity extends BaseActivity {

  private QChatCreateServerWayActivityBinding binding;
  private ActivityResultLauncher<Intent> launcher;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = QChatCreateServerWayActivityBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());

    launcher =
        registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
              if (result.getResultCode() == RESULT_OK) {
                finish();
              }
            });
    initView();
  }

  private void initView() {
    binding.tvClose.setOnClickListener(v -> finish());
    // creating a server by self.
    binding.tvCreateSelf.setOnClickListener(
        v -> {
          launcher.launch(new Intent(QChatCreateWayActivity.this, QChatCreateBySelfActivity.class));
          overridePendingTransition(R.anim.anim_from_end_to_start, R.anim.anim_empty_with_time);
        });
    // search and join a server.
    binding.tvJoinOther.setOnClickListener(
        v -> {
          launcher.launch(
              new Intent(QChatCreateWayActivity.this, QChatJoinOtherServerActivity.class));
          overridePendingTransition(R.anim.anim_from_end_to_start, R.anim.anim_empty_with_time);
        });
  }

  @Override
  public void finish() {
    super.finish();
    overridePendingTransition(R.anim.anim_empty_with_time, R.anim.anim_from_top_to_bottom);
  }
}
