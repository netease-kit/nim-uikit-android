// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.qchatkit.ui.channel.add;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.qchatkit.repo.model.QChatChannelRoleInfo;
import com.netease.yunxin.kit.qchatkit.repo.model.QChatServerRoleInfo;
import com.netease.yunxin.kit.qchatkit.ui.R;
import com.netease.yunxin.kit.qchatkit.ui.channel.add.viewholder.RoleViewHolder;
import com.netease.yunxin.kit.qchatkit.ui.channel.permission.QChatRolePermissionActivity;
import com.netease.yunxin.kit.qchatkit.ui.common.CommonListActivity;
import com.netease.yunxin.kit.qchatkit.ui.common.CommonViewHolder;
import com.netease.yunxin.kit.qchatkit.ui.databinding.QChatAddRoleViewHolderBinding;
import com.netease.yunxin.kit.qchatkit.ui.model.QChatArrowBean;
import com.netease.yunxin.kit.qchatkit.ui.model.QChatBaseBean;
import com.netease.yunxin.kit.qchatkit.ui.model.QChatConstant;
import com.netease.yunxin.kit.qchatkit.ui.model.QChatViewType;

/** server role list that used to add role to channel */
public class QChatChannelAddRoleActivity extends CommonListActivity {

  private static final String TAG = "QChatChannelAddRoleActivity";
  private AddRoleViewModel viewModel;
  private long serverId;
  private long channelId;

  @Override
  public void initData() {
    super.initData();
    serverId = getIntent().getLongExtra(QChatConstant.SERVER_ID, 0);
    channelId = getIntent().getLongExtra(QChatConstant.CHANNEL_ID, 0);
    ALog.d(TAG, "initData", "info:" + serverId + "," + channelId);
    viewModel = new ViewModelProvider(this).get(AddRoleViewModel.class);
    viewModel
        .getRoleLiveData()
        .observe(
            this,
            result -> {
              if (result.getLoadStatus() == LoadStatus.Success) {
                ALog.d(TAG, "RoleLiveData", "Success");
                showEmptyView(result.getData() == null || result.getData().size() < 1);
                setData(result.getData());
              } else if (result.getLoadStatus() == LoadStatus.Finish) {
                if (result.getType() == FetchResult.FetchType.Add) {
                  ALog.d(TAG, "RoleLiveData", "Add");
                  addData(result.getTypeIndex(), result.getData());
                } else if (result.getType() == FetchResult.FetchType.Remove) {
                  ALog.d(TAG, "RoleLiveData", "Remove");
                  removeData(result.getTypeIndex());
                }
              }
            });

    viewModel
        .getAddLiveData()
        .observe(
            this,
            result -> {
              if (result.getLoadStatus() == LoadStatus.Success) {
                ALog.d(TAG, "AddLiveData", "Success");
                QChatChannelRoleInfo param = result.getData();
                Intent intent = new Intent(this, QChatRolePermissionActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable(QChatConstant.CHANNEL_ROLE, param);
                intent.putExtras(bundle);
                startActivity(intent);
              } else if (result.getLoadStatus() == LoadStatus.Error) {
                ALog.d(TAG, "AddLiveData", "Error");
                Toast.makeText(
                        this,
                        getResources().getString(result.errorMsg().getRes()),
                        Toast.LENGTH_SHORT)
                    .show();
              }
            });
  }

  @Override
  protected void onResume() {
    super.onResume();
    ALog.d(TAG, "onResume");
    viewModel.fetchRoleList(serverId, channelId);
  }

  /** show page empty view */
  private void showEmptyView(boolean isShow) {
    String content = "";
    if (isShow) {
      String memberText = getResources().getString(R.string.qchat_roles);
      String tipsText = getResources().getString(R.string.qchat_empty_text);
      content = String.format(tipsText, memberText);
    }
    showEmptyView(content, isShow);
  }

  @Override
  public void loadMore(QChatBaseBean bean) {
    viewModel.loadMore(serverId, channelId);
  }

  @Override
  public void onTitleActionClick(View view) {}

  @Override
  public String getTitleText() {
    return getResources().getString(R.string.qchat_channel_add_role_title);
  }

  @Override
  public boolean isLoadMore() {
    return viewModel.hasMore();
  }

  @Override
  public CommonViewHolder<QChatBaseBean> onCreateViewHolder(
      @NonNull ViewGroup parent, int viewType) {
    if (viewType == QChatViewType.ARROW_VIEW_TYPE) {
      QChatAddRoleViewHolderBinding viewBinding =
          QChatAddRoleViewHolderBinding.inflate(getLayoutInflater(), parent, false);
      RoleViewHolder roleViewHolder = new RoleViewHolder(viewBinding);
      roleViewHolder.setItemOnClickListener(
          (data, position) -> {
            if (data instanceof QChatArrowBean) {
              Object param = ((QChatArrowBean) data).param;
              if (param instanceof QChatServerRoleInfo) {
                viewModel.addChannelRole(channelId, (QChatServerRoleInfo) param);
              }
            }
          });
      return roleViewHolder;
    }
    return null;
  }

  public static void launch(Activity activity, long serverId, long channelId) {
    Intent intent = new Intent(activity, QChatChannelAddRoleActivity.class);
    intent.putExtra(QChatConstant.SERVER_ID, serverId);
    intent.putExtra(QChatConstant.CHANNEL_ID, channelId);
    activity.startActivity(intent);
  }
}
