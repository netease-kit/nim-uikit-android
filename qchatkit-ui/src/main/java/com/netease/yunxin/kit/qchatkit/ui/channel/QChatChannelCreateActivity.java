// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.qchatkit.ui.channel;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.common.ui.activities.BaseActivity;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.qchatkit.repo.model.QChatChannelInfo;
import com.netease.yunxin.kit.qchatkit.repo.model.QChatChannelModeEnum;
import com.netease.yunxin.kit.qchatkit.ui.R;
import com.netease.yunxin.kit.qchatkit.ui.databinding.QChatChannelCreateActivityBinding;
import com.netease.yunxin.kit.qchatkit.ui.model.QChatConstant;
import java.util.ArrayList;

/** channel create activity */
public class QChatChannelCreateActivity extends BaseActivity {

  private static final String TAG = "QChatChannelCreateActivity";

  /** 创建频道超出数量限制 */
  private static final int ERROR_CODE_OVER_LIMIT = 419;

  private QChatChannelCreateActivityBinding viewBinding;
  private ChannelCreateViewModel viewModel;
  private ActivityResultLauncher<Intent> activityResultLauncher;
  private final ArrayList<String> channelTypeList = new ArrayList<>(2);
  private int selectIndex = 0;
  private long serverId;
  private boolean inputTitle = false;
  private boolean isCreate = false;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    ALog.d(TAG, "onCreate");
    changeStatusBarColor(R.color.color_eff1f4);
    viewBinding = QChatChannelCreateActivityBinding.inflate(LayoutInflater.from(this));
    viewModel = new ViewModelProvider(this).get(ChannelCreateViewModel.class);
    setContentView(viewBinding.getRoot());
    initView();
    initData();
  }

  private void initView() {

    activityResultLauncher =
        registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
              selectIndex = result.getResultCode();
              if (selectIndex >= 0 && selectIndex < channelTypeList.size()) {
                viewBinding.channelCreateTypeRtv.setText(channelTypeList.get(selectIndex));
              } else {
                selectIndex = 0;
              }
              ALog.d(TAG, "activityResultLauncher", "channel type select:" + selectIndex);
            });

    viewBinding.channelCreateLeftTv.setOnClickListener(view -> finish());

    viewBinding.channelCreateRightTv.setOnClickListener(
        view -> {
          ALog.d(TAG, "OnClickListener", "channelCreateRightTv:inputTitle=" + inputTitle);
          if (viewBinding.channelCreateRightCover.getVisibility() == View.GONE) {
            createChannel();
          }
        });

    viewBinding.channelCreateNameEt.addTextChangedListener(
        new TextWatcher() {
          @Override
          public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

          @Override
          public void onTextChanged(CharSequence s, int start, int before, int count) {}

          @Override
          public void afterTextChanged(Editable s) {
            boolean hasContent = s.length() > 0;
            if (hasContent != inputTitle) {
              inputTitle = hasContent;
              updateCreateUI();
            }
          }
        });

    /** choice channel type. default type is public */
    viewBinding.channelCreateTypeRtv.setOnClickListener(
        view -> {
          ALog.d(TAG, "channelCreateTypeRtv:choice type");
          Intent intent =
              new Intent(QChatChannelCreateActivity.this, QChatChannelTypeSelectActivity.class);
          intent.putStringArrayListExtra(QChatConstant.CHOICE_LIST, channelTypeList);
          intent.putExtra(QChatConstant.CHOICE_LIST, channelTypeList);
          intent.putExtra(QChatConstant.SELECTED_INDEX, selectIndex);
          intent.putExtra(
              QChatConstant.TITLE, getResources().getString(R.string.qchat_channel_type));
          activityResultLauncher.launch(intent);
        });

    updateCreateUI();
  }

  private void updateCreateUI() {
    if (inputTitle && !isCreate) {
      viewBinding.channelCreateRightCover.setVisibility(View.GONE);
    } else {
      viewBinding.channelCreateRightCover.setVisibility(View.VISIBLE);
    }
  }

  private void initData() {
    channelTypeList.add(getResources().getString(R.string.qchat_channel_type_public));
    channelTypeList.add(getResources().getString(R.string.qchat_channel_type_private));
    serverId = getIntent().getLongExtra(QChatConstant.SERVER_ID, 0);
    if (serverId < 1) {
      Toast.makeText(
              this, getResources().getString(R.string.qchat_server_empty_error), Toast.LENGTH_SHORT)
          .show();
      finish();
    }
    viewModel
        .getFetchResult()
        .observe(
            this,
            (result) -> {
              if (result.getLoadStatus() == LoadStatus.Success) {
                ALog.d(TAG, "viewModel:observe Success");
                Intent intent = new Intent();
                QChatChannelInfo chatChannelInfo = result.getData();
                intent.putExtra(QChatConstant.CHANNEL_ID, chatChannelInfo.getChannelId());
                intent.putExtra(QChatConstant.CHANNEL_NAME, chatChannelInfo.getName());
                intent.putExtra(QChatConstant.CHANNEL_TOPIC, chatChannelInfo.getTopic());
                setResult(RESULT_OK, intent);
                finish();
              } else if (result.getLoadStatus() == LoadStatus.Error) {
                FetchResult.ErrorMsg msg = result.getError();
                int errorCode = -1;
                if (msg != null) {
                  errorCode = msg.getCode();
                  ALog.d(TAG, "viewModel:observe Error" + errorCode);
                }
                if (errorCode == ERROR_CODE_OVER_LIMIT) {
                  Toast.makeText(
                          this, R.string.qchat_channel_create_error_count_limit, Toast.LENGTH_SHORT)
                      .show();
                } else {
                  Toast.makeText(this, result.getErrorMsg(this), Toast.LENGTH_SHORT).show();
                }

                isCreate = false;
                updateCreateUI();
              }
            });
  }

  private void createChannel() {

    String channelName = viewBinding.channelCreateNameEt.getText();
    if (TextUtils.isEmpty(channelName)) {
      Toast.makeText(
              this,
              getResources().getString(R.string.qchat_channel_name_empty_error),
              Toast.LENGTH_SHORT)
          .show();
      return;
    }
    if (isCreate) {
      return;
    }
    String channelTopic = viewBinding.channelCreateTopicEt.getText();
    ALog.d(TAG, "createChannel", "channelInfo:" + channelName + "," + channelTopic);
    QChatChannelModeEnum type =
        selectIndex == 0 ? QChatChannelModeEnum.Public : QChatChannelModeEnum.Private;
    isCreate = true;
    updateCreateUI();
    viewModel.createChannel(serverId, channelName, channelTopic, type);
  }

  public static void launch(Activity activity, long serverId) {
    Intent intent = new Intent(activity, QChatChannelCreateActivity.class);
    intent.putExtra(QChatConstant.SERVER_ID, serverId);
    activity.startActivity(intent);
  }
}
