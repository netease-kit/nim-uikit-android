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
import com.netease.yunxin.kit.qchatkit.repo.model.QChatChannelMember;
import com.netease.yunxin.kit.qchatkit.ui.R;
import com.netease.yunxin.kit.qchatkit.ui.channel.add.viewholder.ServerMemberViewHolder;
import com.netease.yunxin.kit.qchatkit.ui.channel.permission.QChatMemberPermissionActivity;
import com.netease.yunxin.kit.qchatkit.ui.common.CommonListActivity;
import com.netease.yunxin.kit.qchatkit.ui.common.CommonViewHolder;
import com.netease.yunxin.kit.qchatkit.ui.databinding.QChatChannelMemberViewHolderBinding;
import com.netease.yunxin.kit.qchatkit.ui.model.QChatBaseBean;
import com.netease.yunxin.kit.qchatkit.ui.model.QChatConstant;
import com.netease.yunxin.kit.qchatkit.ui.model.QChatServerMemberBean;
import com.netease.yunxin.kit.qchatkit.ui.model.QChatViewType;

/** add member to channel permission page show the server member list */
public class QChatChannelAddMemberActivity extends CommonListActivity {

  private static final String TAG = "QChatChannelAddMemberActivity";

  private AddMemberViewModel viewModel;
  private long serverId;
  private long channelId;

  @Override
  public void initView() {
    super.initView();
    ALog.d(TAG, "initView");
    setBackgroundColor(R.color.color_white);
    changeStatusBarColor(R.color.color_white);
  }

  @Override
  public void initData() {
    super.initData();
    serverId = getIntent().getLongExtra(QChatConstant.SERVER_ID, 0);
    channelId = getIntent().getLongExtra(QChatConstant.CHANNEL_ID, 0);
    ALog.d(TAG, "initData", "info:" + serverId + "," + channelId);
    viewModel = new ViewModelProvider(this).get(AddMemberViewModel.class);
    viewModel
        .getResultLiveData()
        .observe(
            this,
            result -> {
              if (result.getLoadStatus() == LoadStatus.Success) {
                showEmptyView(result.getData() == null || result.getData().size() < 1);
                setData(result.getData());
                ALog.d(TAG, "ResultLiveData", "Success");
              } else if (result.getLoadStatus() == LoadStatus.Finish) {
                if (result.getType() == FetchResult.FetchType.Add) {
                  addData(result.getTypeIndex(), result.getData());
                  ALog.d(TAG, "ResultLiveData", "Add");
                } else if (result.getType() == FetchResult.FetchType.Remove) {
                  removeData(result.getTypeIndex());
                  ALog.d(TAG, "ResultLiveData", "Remove:" + result.getTypeIndex());
                }
              } else if (result.getLoadStatus() == LoadStatus.Error) {
                Toast.makeText(
                        this,
                        getResources().getString(result.errorMsg().getRes()),
                        Toast.LENGTH_SHORT)
                    .show();
                ALog.d(TAG, "ResultLiveData", "Error");
              }
            });
    viewModel
        .getAddLiveData()
        .observe(
            this,
            result -> {
              if (result.getLoadStatus() == LoadStatus.Success) {
                ALog.d(TAG, "AddLiveData", "Success");
                QChatChannelMember param = result.getData();
                Intent intent = new Intent(this, QChatMemberPermissionActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable(QChatConstant.CHANNEL_MEMBER, param);
                intent.putExtras(bundle);
                startActivity(intent);
              } else if (result.getLoadStatus() == LoadStatus.Error) {
                Toast.makeText(
                        this,
                        getResources().getString(result.errorMsg().getRes()),
                        Toast.LENGTH_SHORT)
                    .show();
                ALog.d(TAG, "AddLiveData", "Error");
              }
            });
  }

  @Override
  protected void onResume() {
    super.onResume();
    ALog.d(TAG, "onResume");
    viewModel.fetchMemberList(serverId, channelId);
  }

  private void showEmptyView(boolean isShow) {
    ALog.d(TAG, "showEmptyView");
    String content = "";
    if (isShow) {
      String memberText = getResources().getString(R.string.qchat_member);
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
    return getResources().getString(R.string.qchat_channel_add_member_title);
  }

  @Override
  public boolean isLoadMore() {
    return viewModel.hasMore();
  }

  @Override
  public CommonViewHolder<QChatBaseBean> onCreateViewHolder(
      @NonNull ViewGroup parent, int viewType) {
    if (viewType == QChatViewType.SERVER_MEMBER_VIEW_TYPE) {
      QChatChannelMemberViewHolderBinding viewBinding =
          QChatChannelMemberViewHolderBinding.inflate(getLayoutInflater(), parent, false);
      ServerMemberViewHolder addRoleViewHolder = new ServerMemberViewHolder(viewBinding);
      addRoleViewHolder.setItemOnClickListener(
          (data, position) -> {
            if (data instanceof QChatServerMemberBean) {
              viewModel.addMemberToChannel(
                  serverId, channelId, ((QChatServerMemberBean) data).serverMember.getAccId());
            }
          });
      return addRoleViewHolder;
    }
    return null;
  }

  public static void launch(Activity activity, long serverId, long channelId) {
    Intent intent = new Intent(activity, QChatChannelAddMemberActivity.class);
    intent.putExtra(QChatConstant.SERVER_ID, serverId);
    intent.putExtra(QChatConstant.CHANNEL_ID, channelId);
    activity.startActivity(intent);
  }
}
