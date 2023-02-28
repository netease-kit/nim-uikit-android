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
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.common.ui.activities.BaseActivity;
import com.netease.yunxin.kit.corekit.im.provider.FetchCallback;
import com.netease.yunxin.kit.qchatkit.repo.QChatServerRepo;
import com.netease.yunxin.kit.qchatkit.repo.model.QChatSearchResultInfo;
import com.netease.yunxin.kit.qchatkit.ui.R;
import com.netease.yunxin.kit.qchatkit.ui.databinding.QChatJoinOtherServerActivityBinding;
import com.netease.yunxin.kit.qchatkit.ui.message.QChatChannelMessageActivity;
import com.netease.yunxin.kit.qchatkit.ui.server.adapter.QChatSearchResultAdapter;
import com.netease.yunxin.kit.qchatkit.ui.utils.QChatUtils;
import java.util.Collections;
import java.util.List;

/** In the page, user can search a existing server and join it. */
public class QChatJoinOtherServerActivity extends BaseActivity {
  private static final String TAG = "QChatJoinOtherServerActivity";
  private QChatJoinOtherServerActivityBinding binding;
  private QChatSearchResultAdapter adapter;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = QChatJoinOtherServerActivityBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());

    initView();
  }

  private void initView() {
    binding.ivBack.setOnClickListener(v -> finish());
    binding.etServerID.addTextChangedListener(
        new TextWatcher() {
          @Override
          public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

          @Override
          public void onTextChanged(CharSequence s, int start, int before, int count) {}

          @Override
          public void afterTextChanged(Editable s) {
            if (TextUtils.isEmpty(String.valueOf(s))) {
              binding.ivClear.setVisibility(View.GONE);
            } else {
              binding.ivClear.setVisibility(View.VISIBLE);
            }
          }
        });
    // da search action.
    binding.etServerID.setOnEditorActionListener(
        (v, actionId, event) -> {
          if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            InputMethodManager manager =
                (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            manager.hideSoftInputFromWindow(v.getWindowToken(), 0);
            Long serverId = getServerIdFromEdit();
            if (serverId == null) {
              binding.groupNoServerTip.setVisibility(View.VISIBLE);
              adapter.addDataList(Collections.emptyList(), true);
            } else {
              QChatUtils.isConnectedToastAndRun(
                  this,
                  () ->
                      QChatServerRepo.searchServerById(
                          serverId,
                          new FetchCallback<List<QChatSearchResultInfo>>() {
                            @Override
                            public void onSuccess(@Nullable List<QChatSearchResultInfo> param) {
                              if (param != null && !param.isEmpty()) {
                                binding.groupNoServerTip.setVisibility(View.GONE);
                                adapter.addDataList(param, true);
                              } else {
                                binding.groupNoServerTip.setVisibility(View.VISIBLE);
                                adapter.addDataList(Collections.emptyList(), true);
                              }
                            }

                            public void onFailed(int code) {
                              Toast.makeText(
                                      getApplicationContext(),
                                      getString(R.string.qchat_server_request_fail) + code,
                                      Toast.LENGTH_SHORT)
                                  .show();
                            }

                            @Override
                            public void onException(@Nullable Throwable exception) {
                              Toast.makeText(
                                      getApplicationContext(),
                                      getString(R.string.qchat_server_request_fail) + exception,
                                      Toast.LENGTH_SHORT)
                                  .show();
                            }
                          }));
            }
            return true;
          }
          return false;
        });

    binding.ivClear.setOnClickListener(v -> binding.etServerID.setText(null));

    adapter = new QChatSearchResultAdapter(this);
    // if user click the item of the searching result that user had joined,
    // user can launching the message page of the first channel in the server.
    adapter.setItemClickListener(
        (data, holder) -> {
          if (data.channelInfo != null) {
            QChatChannelMessageActivity.launch(
                QChatJoinOtherServerActivity.this,
                data.channelInfo.getServerId(),
                data.channelInfo.getChannelId(),
                data.channelInfo.getName(),
                data.channelInfo.getTopic());
            setResult(RESULT_OK);
            finish();
          }
        });
    binding.ryServerList.setAdapter(adapter);
    binding.ryServerList.setLayoutManager(
        new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
  }

  private Long getServerIdFromEdit() {
    Editable editable = binding.etServerID.getText();
    if (editable == null) {
      return null;
    }
    Long result = null;
    try {
      result = Long.parseLong(editable.toString());
    } catch (NumberFormatException exception) {
      ALog.e(TAG, "getServerIdFromEdit", exception);
    }
    return result;
  }

  @Override
  public void finish() {
    super.finish();
    overridePendingTransition(R.anim.anim_empty_with_time, R.anim.anim_from_start_to_end);
  }
}
