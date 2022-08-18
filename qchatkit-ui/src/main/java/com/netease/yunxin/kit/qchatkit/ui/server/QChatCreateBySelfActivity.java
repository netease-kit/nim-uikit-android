// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.qchatkit.ui.server;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;
import androidx.annotation.Nullable;
import com.bumptech.glide.Glide;
import com.netease.yunxin.kit.common.ui.activities.BaseActivity;
import com.netease.yunxin.kit.common.utils.NetworkUtils;
import com.netease.yunxin.kit.corekit.im.provider.FetchCallback;
import com.netease.yunxin.kit.qchatkit.repo.QChatServerRepo;
import com.netease.yunxin.kit.qchatkit.repo.model.QChatChannelInfo;
import com.netease.yunxin.kit.qchatkit.repo.model.QChatServerWithSingleChannel;
import com.netease.yunxin.kit.qchatkit.ui.R;
import com.netease.yunxin.kit.qchatkit.ui.common.photo.PhotoChoiceDialog;
import com.netease.yunxin.kit.qchatkit.ui.databinding.QChatCreateBySelfActivityBinding;
import com.netease.yunxin.kit.qchatkit.ui.message.QChatChannelMessageActivity;

public class QChatCreateBySelfActivity extends BaseActivity {

  private QChatCreateBySelfActivityBinding binding;
  private String iconUrl = null;
  private InputMethodManager manager;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = QChatCreateBySelfActivityBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());

    manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

    initView();
  }

  private void initView() {
    binding.ivBack.setOnClickListener(v -> finish());
    binding.ivPortrait.setOnClickListener(
        v -> {
          manager.hideSoftInputFromWindow(v.getWindowToken(), 0);
          new PhotoChoiceDialog(QChatCreateBySelfActivity.this)
              .show(
                  new FetchCallback<String>() {
                    @Override
                    public void onSuccess(@Nullable String param) {
                      iconUrl = param;
                      Glide.with(getApplicationContext())
                          .load(param)
                          .circleCrop()
                          .into(binding.ivPortrait);
                    }

                    @Override
                    public void onFailed(int code) {
                      if (code != 0) {
                        Toast.makeText(
                                getApplicationContext(),
                                getString(R.string.qchat_server_request_fail) + " " + code,
                                Toast.LENGTH_SHORT)
                            .show();
                      }
                    }

                    @Override
                    public void onException(@Nullable Throwable exception) {
                      Toast.makeText(
                              getApplicationContext(),
                              getString(R.string.qchat_server_request_fail),
                              Toast.LENGTH_SHORT)
                          .show();
                    }
                  });
        });
    binding.etServerName.addTextChangedListener(
        new TextWatcher() {
          @Override
          public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

          @Override
          public void onTextChanged(CharSequence s, int start, int before, int count) {}

          @Override
          public void afterTextChanged(Editable s) {
            if (TextUtils.isEmpty(String.valueOf(s))) {
              binding.ivClear.setVisibility(View.GONE);
              binding.tvCreate.setEnabled(false);
              binding.tvCreate.setAlpha(0.5f);
            } else {
              binding.ivClear.setVisibility(View.VISIBLE);
              binding.tvCreate.setEnabled(true);
              binding.tvCreate.setAlpha(1f);
            }
          }
        });

    binding.ivClear.setOnClickListener(v -> binding.etServerName.setText(null));
    binding.tvCreate.setEnabled(false);
    binding.tvCreate.setAlpha(0.5f);
    binding.tvCreate.setOnClickListener(
        v ->
            NetworkUtils.isConnectedToastAndRun(
                this,
                () -> {
                  binding.tvCreate.setEnabled(false);
                  QChatServerRepo.createServerAndCreateChannel(
                      binding.etServerName.getText().toString(),
                      getString(R.string.qchat_server_channel_name_fix, "1"),
                      getString(R.string.qchat_server_channel_name_fix, "2"),
                      iconUrl,
                      new FetchCallback<QChatServerWithSingleChannel>() {
                        @Override
                        public void onSuccess(@Nullable QChatServerWithSingleChannel param) {
                          // loading create channel and finish
                          if (param == null) {
                            binding.tvCreate.setEnabled(true);
                            return;
                          }
                          QChatChannelInfo channelInfo = param.getChannelInfo();
                          if (channelInfo != null) {
                            QChatChannelMessageActivity.launch(
                                QChatCreateBySelfActivity.this,
                                channelInfo.getServerId(),
                                channelInfo.getChannelId(),
                                channelInfo.getName(),
                                channelInfo.getTopic());
                          }
                          setResult(RESULT_OK);
                          finish();
                          Toast.makeText(
                                  getApplicationContext(), "create success", Toast.LENGTH_SHORT)
                              .show();
                        }

                        public void onFailed(int code) {
                          binding.tvCreate.setEnabled(true);
                          Toast.makeText(
                                  getApplicationContext(),
                                  getString(R.string.qchat_server_request_fail) + code,
                                  Toast.LENGTH_SHORT)
                              .show();
                        }

                        @Override
                        public void onException(@Nullable Throwable exception) {
                          binding.tvCreate.setEnabled(true);
                          Toast.makeText(
                                  getApplicationContext(),
                                  getString(R.string.qchat_server_request_fail) + exception,
                                  Toast.LENGTH_SHORT)
                              .show();
                        }
                      });
                }));
  }

  @Override
  public void finish() {
    super.finish();
    overridePendingTransition(R.anim.anim_empty_with_time, R.anim.anim_from_start_to_end);
  }
}
