// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.qchatkit.ui.channel.setting;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.common.ui.activities.BaseActivity;
import com.netease.yunxin.kit.common.ui.dialog.BottomConfirmDialog;
import com.netease.yunxin.kit.common.ui.dialog.ConfirmListener;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.qchatkit.repo.model.QChatChannelInfo;
import com.netease.yunxin.kit.qchatkit.repo.model.QChatChannelModeEnum;
import com.netease.yunxin.kit.qchatkit.ui.R;
import com.netease.yunxin.kit.qchatkit.ui.channel.blackwhite.QChatBlackWhiteActivity;
import com.netease.yunxin.kit.qchatkit.ui.channel.permission.QChatChannelPermissionActivity;
import com.netease.yunxin.kit.qchatkit.ui.databinding.QChatChannelSettingActivityBinding;
import com.netease.yunxin.kit.qchatkit.ui.model.QChatConstant;

/** channel setting activity you can modify channel name,channel topic */
public class ChannelSettingActivity extends BaseActivity {

  private static final String TAG = "ChannelSettingActivity";
  private QChatChannelSettingActivityBinding viewBinding;
  private ChannelSettingViewModel viewModel;
  private QChatChannelInfo channelInfo;
  private long channelId;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    ALog.d(TAG, "onCreate");
    changeStatusBarColor(R.color.color_eff1f4);
    viewBinding = QChatChannelSettingActivityBinding.inflate(LayoutInflater.from(this));
    viewModel = new ViewModelProvider(this).get(ChannelSettingViewModel.class);
    setContentView(viewBinding.getRoot());
    initView();
    initData();
  }

  private void initView() {

    viewBinding.channelSettingTitleLeftTv.setOnClickListener(view -> finish());

    viewBinding.channelSettingTitleRightTv.setOnClickListener(view -> updateChannelSetting());

    viewBinding.channelSettingPermissionRtv.setOnClickListener(
        view -> {
          ALog.d(TAG, "OnClickListener", "channelSettingPermissionRtv" + checkParamValid());
          if (checkParamValid()) {
            QChatChannelPermissionActivity.launch(this, channelInfo.getServerId(), channelId);
          } else {
            Toast.makeText(
                    this,
                    getResources().getString(R.string.qchat_channel_empty_error),
                    Toast.LENGTH_SHORT)
                .show();
          }
        });

    viewBinding.channelSettingWhiteNameListRtv.setOnClickListener(
        view -> {
          ALog.d(TAG, "OnClickListener", "channelSettingWhiteNameListRtv" + checkParamValid());
          if (checkParamValid()) {
            QChatBlackWhiteActivity.launch(
                this,
                channelInfo.getServerId(),
                channelId,
                this.channelInfo.getViewMode().ordinal());
          } else {
            Toast.makeText(
                    this,
                    getResources().getString(R.string.qchat_channel_empty_error),
                    Toast.LENGTH_SHORT)
                .show();
          }
        });

    viewBinding.channelSettingDeleteRtv.setOnClickListener(view -> showDeleteDialog());
  }

  /** show delete channel dialog */
  private void showDeleteDialog() {
    ALog.d(TAG, "showDeleteDialog");
    BottomConfirmDialog bottomConfirmDialog = new BottomConfirmDialog();
    bottomConfirmDialog
        .setTitleStr(getString(R.string.qchat_channel_delete_dialog_title))
        .setPositiveStr(getString(R.string.qchat_sure))
        .setNegativeStr(getString(R.string.qchat_cancel))
        .setConfirmListener(
            new ConfirmListener() {
              @Override
              public void onNegative() {
                ALog.d(TAG, "showDeleteDialog", "onNegative");
                bottomConfirmDialog.dismiss();
              }

              @Override
              public void onPositive() {
                ALog.d(TAG, "showDeleteDialog", "onPositive:" + channelId);
                bottomConfirmDialog.dismiss();
                viewModel.deleteChannel(channelId);
              }
            })
        .show(getSupportFragmentManager());
  }

  private void initData() {

    channelId = getIntent().getLongExtra(QChatConstant.CHANNEL_ID, 0);

    viewModel
        .getFetchResult()
        .observe(
            this,
            result -> {
              if (result.getLoadStatus() == LoadStatus.Success) {
                loadData(result.getData());

              } else if (result.getLoadStatus() == LoadStatus.Error) {
                Toast.makeText(this, result.getErrorMsg(this), Toast.LENGTH_SHORT).show();
              } else if (result.getLoadStatus() == LoadStatus.Finish) {
                if (result.getType() == FetchResult.FetchType.Remove) {
                  finish();
                }
              }
            });
    viewModel.fetchChannelInfo(channelId);
    ALog.d(TAG, "initData", "channelId:" + channelId);
  }

  private void loadData(QChatChannelInfo channelInfo) {
    if (channelInfo == null) {
      return;
    }
    ALog.d(TAG, "loadData", "channelInfo:" + channelInfo.getChannelId());
    this.channelInfo = channelInfo;
    viewBinding.channelSettingNameEt.setText(channelInfo.getName());
    viewBinding.channelSettingTopicEt.setText(channelInfo.getTopic());
    if (this.channelInfo.getViewMode() == QChatChannelModeEnum.Public) {
      viewBinding.channelSettingWhiteNameListRtv.setText(R.string.qchat_channel_black_name_list);
    } else {
      viewBinding.channelSettingWhiteNameListRtv.setText(R.string.qchat_channel_white_name_list);
    }
  }

  private void updateChannelSetting() {
    if (channelInfo != null) {
      ALog.d(TAG, "updateChannelSetting", "channelInfo:" + channelInfo.getChannelId());
      String channelName = viewBinding.channelSettingNameEt.getText();
      String topic = viewBinding.channelSettingTopicEt.getText();
      if (!TextUtils.isEmpty(channelName)) {
        viewModel.updateChannel(channelId, channelName, topic);
      } else {
        Toast.makeText(
                this, getString(R.string.qchat_channel_update_empty_error), Toast.LENGTH_SHORT)
            .show();
      }
    }
  }

  private boolean checkParamValid() {
    return channelInfo != null && channelId > 0;
  }
}
