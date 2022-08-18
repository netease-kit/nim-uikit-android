// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.qchatkit.ui.message;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.common.ui.activities.CommonActivity;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.qchatkit.repo.model.QChatChannelInfo;
import com.netease.yunxin.kit.qchatkit.repo.model.QChatServerMemberInfo;
import com.netease.yunxin.kit.qchatkit.ui.channel.setting.ChannelSettingActivity;
import com.netease.yunxin.kit.qchatkit.ui.common.CommonRecyclerViewAdapter;
import com.netease.yunxin.kit.qchatkit.ui.databinding.QChatChannelMemberStatusViewHolderBinding;
import com.netease.yunxin.kit.qchatkit.ui.databinding.QChatChannelMessageActivityBinding;
import com.netease.yunxin.kit.qchatkit.ui.message.model.ChannelMemberStatusBean;
import com.netease.yunxin.kit.qchatkit.ui.message.view.MemberProfileDialog;
import com.netease.yunxin.kit.qchatkit.ui.message.viewholder.ChannelMemberViewHolder;
import com.netease.yunxin.kit.qchatkit.ui.model.QChatBaseBean;
import com.netease.yunxin.kit.qchatkit.ui.model.QChatConstant;
import com.netease.yunxin.kit.qchatkit.ui.model.QChatViewType;
import java.util.List;

/** channel message page include Message Page and Member list */
public class QChatChannelMessageActivity extends CommonActivity {

  private static final String TAG = "QChatChannelMessageActivity";
  private QChatChannelMessageActivityBinding viewBinding;
  private QChatChannelMessageFragment messageFragment;
  protected CommonRecyclerViewAdapter recyclerViewAdapter;
  private ChannelMemberViewModel viewModel;
  private MemberProfileDialog profileDialog;
  private QChatChannelInfo channelInfo;
  private long serverId;
  private long channelId;
  private String channelName;
  private String channelTopic;
  private final int LOAD_MORE_DIFF = 4;

  @Override
  protected void initViewModel() {
    ALog.d(TAG, "initViewModel");
    viewModel = new ViewModelProvider(this).get(ChannelMemberViewModel.class);
    viewModel
        .getResultLiveData()
        .observe(
            this,
            result -> {
              if (result.getLoadStatus() == LoadStatus.Success) {
                setData(result.getData());
              } else if (result.getLoadStatus() == LoadStatus.Finish) {
                if (result.getType() == FetchResult.FetchType.Add) {
                  addData(result.getTypeIndex(), result.getData());
                } else if (result.getType() == FetchResult.FetchType.Remove) {
                  removeData(result.getTypeIndex());
                }
              } else if (result.getLoadStatus() == LoadStatus.Error) {
                Toast.makeText(
                        this,
                        getResources().getString(result.errorMsg().getRes()),
                        Toast.LENGTH_SHORT)
                    .show();
              }
            });

    viewModel
        .getChannelLiveData()
        .observe(
            this,
            result -> {
              if (result.getType() == FetchResult.FetchType.Remove) {
                ALog.d(TAG, "ChannelLiveData", "Remove");
                finish();
              }
            });

    viewModel
        .getChannelInfoLiveData()
        .observe(
            this,
            result -> {
              if (result.getLoadStatus() == LoadStatus.Success && result.getData() != null) {
                channelInfo = result.getData();
                ALog.d(TAG, "ChannelInfoLiveData", "Success:" + channelInfo.toString());
                loadChannelInfo();
              }
            });

    viewModel
        .getMemberRoleLiveData()
        .observe(
            this,
            result -> {
              if (result.getLoadStatus() == LoadStatus.Success) {
                if (profileDialog.isVisible()) {
                  ALog.d(TAG, "MemberRoleLiveData", "Success:");
                  profileDialog.updateData(result.getData());
                }
              }
            });
  }

  @Nullable
  @Override
  protected View getContentView() {
    viewBinding = QChatChannelMessageActivityBinding.inflate(getLayoutInflater());
    return viewBinding.getRoot();
  }

  @Override
  protected void initView() {
    ALog.d(TAG, "initView");

    viewBinding.qChatChannelMessageDrawerLayout.setScrimColor(Color.TRANSPARENT);
    viewBinding.qChatMessageTitleActionIv.setOnClickListener(
        view -> {
          viewBinding.qChatChannelMessageDrawerLayout.openDrawer(GravityCompat.END);
        });

    viewBinding.qChatMessageMemberTitleSetting.setOnClickListener(
        view -> {
          ALog.d(TAG, "OnClickListener", "qChatMessageMemberTitleSetting");
          Intent createIntent = new Intent(this, ChannelSettingActivity.class);
          createIntent.putExtra(QChatConstant.CHANNEL_ID, channelId);
          createIntent.putExtra(QChatConstant.SERVER_ID, serverId);
          startActivity(createIntent);
        });

    viewBinding.qChatMessageTitleLeftIv.setOnClickListener(
        view -> {
          ALog.d(TAG, "OnClickListener", "qChatMessageTitleLeftIv");
          finish();
        });
    LinearLayoutManager layoutManager = new LinearLayoutManager(this);
    layoutManager.setOrientation(RecyclerView.VERTICAL);
    viewBinding.qChatMessageMemberRecyclerView.setLayoutManager(layoutManager);
    recyclerViewAdapter = new CommonRecyclerViewAdapter();
    recyclerViewAdapter.setViewHolderFactory(
        (parent, viewType) -> {
          if (viewType == QChatViewType.CHANNEL_MEMBER_VIEW_TYPE) {
            QChatChannelMemberStatusViewHolderBinding viewHolderBinding =
                QChatChannelMemberStatusViewHolderBinding.inflate(
                    getLayoutInflater(), parent, false);
            ChannelMemberViewHolder viewHolder = new ChannelMemberViewHolder(viewHolderBinding);
            viewHolder.setItemOnClickListener(
                (data, position) -> {
                  if (profileDialog == null) {
                    profileDialog = new MemberProfileDialog();
                  }
                  if (profileDialog.isAdded()) {
                    profileDialog.dismiss();
                  }
                  QChatServerMemberInfo member = ((ChannelMemberStatusBean) data).channelMember;
                  profileDialog.setData(member, null);
                  viewModel.fetchMemberRoleList(serverId, member.getAccId());
                  profileDialog.show(getSupportFragmentManager(), member.getAccId());
                });
            return viewHolder;
          }
          return null;
        });
    viewBinding.qChatMessageMemberRecyclerView.addOnScrollListener(
        new RecyclerView.OnScrollListener() {
          @Override
          public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
              int position = layoutManager.findLastVisibleItemPosition();
              if (isLoadMore() && recyclerViewAdapter.getItemCount() < position + LOAD_MORE_DIFF) {
                loadMoreMember();
              }
            }
          }

          @Override
          public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
          }
        });
    viewBinding.qChatMessageMemberRecyclerView.setAdapter(recyclerViewAdapter);
    messageFragment = new QChatChannelMessageFragment();
    getSupportFragmentManager()
        .beginTransaction()
        .add(viewBinding.qChatMessageBodyLayout.getId(), messageFragment)
        .commit();
  }

  @Override
  protected void initData() {
    serverId = getIntent().getLongExtra(QChatConstant.SERVER_ID, 0);
    channelId = getIntent().getLongExtra(QChatConstant.CHANNEL_ID, 0);
    channelName = getIntent().getStringExtra(QChatConstant.CHANNEL_NAME);
    channelTopic = getIntent().getStringExtra(QChatConstant.CHANNEL_TOPIC);
    ALog.d(TAG, "initData", "info:" + channelId + "'" + channelName);
    viewModel.registerDeleteChannelObserver(serverId, channelId);
    messageFragment.init(serverId, channelId);
    loadChannelInfo();
  }

  private boolean isLoadMore() {
    return viewModel.hasMore();
  }

  private void loadMoreMember() {
    viewModel.loadMore(serverId, channelId);
  }

  @Override
  protected void onResume() {
    super.onResume();
    ALog.d(TAG, "onResume");
    viewModel.fetchChannelInfo(channelId);
    viewModel.fetchMemberList(serverId, channelId);
  }

  public void loadChannelInfo() {
    if (channelInfo != null) {
      ALog.d(TAG, "loadChannelInfo:" + channelInfo.toString());
      viewBinding.qChatMessageTitleTv.setText(channelInfo.getName());
      viewBinding.qChatMessageMemberTitleTv.setText(channelInfo.getName());
      viewBinding.qChatMessageMemberSubTitleTv.setText(channelInfo.getTopic());
      messageFragment.updateChannelInfo(channelInfo);
    } else {
      viewBinding.qChatMessageTitleTv.setText(channelName);
      viewBinding.qChatMessageMemberTitleTv.setText(channelName);
      viewBinding.qChatMessageMemberSubTitleTv.setText(channelTopic);
    }
  }

  public void setData(List<? extends QChatBaseBean> data) {
    recyclerViewAdapter.setData(data);
  }

  public void addData(int index, List<? extends QChatBaseBean> data) {
    recyclerViewAdapter.addData(index, data);
  }

  public void removeData(int index) {
    recyclerViewAdapter.removeData(index);
  }

  public static void launch(
      Activity activity, long serverId, long channelId, String channelName, String channelTopic) {
    Intent intent = new Intent(activity, QChatChannelMessageActivity.class);
    intent.putExtra(QChatConstant.SERVER_ID, serverId);
    intent.putExtra(QChatConstant.CHANNEL_ID, channelId);
    intent.putExtra(QChatConstant.CHANNEL_NAME, channelName);
    intent.putExtra(QChatConstant.CHANNEL_TOPIC, channelTopic);
    activity.startActivity(intent);
  }
}
