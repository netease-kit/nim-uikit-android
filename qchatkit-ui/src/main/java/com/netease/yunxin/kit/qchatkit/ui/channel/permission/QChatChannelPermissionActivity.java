// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.qchatkit.ui.channel.permission;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.common.ui.dialog.ChoiceListener;
import com.netease.yunxin.kit.common.ui.dialog.CommonChoiceDialog;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.qchatkit.ui.R;
import com.netease.yunxin.kit.qchatkit.ui.channel.add.QChatChannelAddMemberActivity;
import com.netease.yunxin.kit.qchatkit.ui.channel.add.QChatChannelAddRoleActivity;
import com.netease.yunxin.kit.qchatkit.ui.channel.permission.viewholder.ArrowViewHolder;
import com.netease.yunxin.kit.qchatkit.ui.channel.permission.viewholder.CornerViewHolder;
import com.netease.yunxin.kit.qchatkit.ui.channel.permission.viewholder.MemberViewHolder;
import com.netease.yunxin.kit.qchatkit.ui.channel.permission.viewholder.MoreViewHolder;
import com.netease.yunxin.kit.qchatkit.ui.channel.permission.viewholder.RoleViewHolder;
import com.netease.yunxin.kit.qchatkit.ui.channel.permission.viewholder.TitleViewHolder;
import com.netease.yunxin.kit.qchatkit.ui.common.CommonListActivity;
import com.netease.yunxin.kit.qchatkit.ui.common.CommonViewHolder;
import com.netease.yunxin.kit.qchatkit.ui.databinding.QChatArrowViewHolderBinding;
import com.netease.yunxin.kit.qchatkit.ui.databinding.QChatChannelMemberConerViewHolderBinding;
import com.netease.yunxin.kit.qchatkit.ui.databinding.QChatChannelRoleViewHolderBinding;
import com.netease.yunxin.kit.qchatkit.ui.databinding.QChatCornerViewholderLayoutBinding;
import com.netease.yunxin.kit.qchatkit.ui.databinding.QChatMoreViewholderLayoutBinding;
import com.netease.yunxin.kit.qchatkit.ui.databinding.QChatTitleViewholderLayoutBinding;
import com.netease.yunxin.kit.qchatkit.ui.model.QChatBaseBean;
import com.netease.yunxin.kit.qchatkit.ui.model.QChatChannelMemberBean;
import com.netease.yunxin.kit.qchatkit.ui.model.QChatChannelRoleBean;
import com.netease.yunxin.kit.qchatkit.ui.model.QChatConstant;
import com.netease.yunxin.kit.qchatkit.ui.model.QChatViewType;

/** channel permission setting include member and role */
public class QChatChannelPermissionActivity extends CommonListActivity {

  private static final String TAG = "QChatChannelPermissionActivity";
  private ChannelPermissionViewModel viewModel;
  private static final int ROLE_INDEX = 2;
  private long channelId;
  private long serverId;
  private boolean editStatus = false;
  //进入页面重新加载状态，0开始加载，2加载完成
  private int reloadStatus = 2;

  @Override
  public void initData() {
    ALog.d(TAG, "initData");
    viewModel = new ViewModelProvider(this).get(ChannelPermissionViewModel.class);
    channelId = getIntent().getLongExtra(QChatConstant.CHANNEL_ID, 0);
    serverId = getIntent().getLongExtra(QChatConstant.SERVER_ID, 0);

    viewModel
        .getRoleLiveData()
        .observe(
            this,
            result -> {
              if (result.getLoadStatus() == LoadStatus.Success) {
                addData(ROLE_INDEX, result.getData());
                ALog.d(TAG, "getRoleLiveData", "Success" + ROLE_INDEX);
              } else if (result.getLoadStatus() == LoadStatus.Finish) {
                if (result.getType() == FetchResult.FetchType.Add) {
                  addData(result.getTypeIndex() + ROLE_INDEX, result.getData());
                  ALog.d(
                      TAG,
                      "getRoleLiveData",
                      "Add index:"
                          + result.getTypeIndex()
                          + "role index:"
                          + ROLE_INDEX
                          + "size:"
                          + result.getData().size());
                } else if (result.getType() == FetchResult.FetchType.Remove) {
                  removeData(result.getTypeIndex());
                  checkAdapterData();
                  ALog.d(TAG, "getRoleLiveData", "Remove" + result.getTypeIndex());
                }
              } else if (result.getLoadStatus() == LoadStatus.Error) {
                String errorMsg = null;
                if (result.errorMsg() != null) {
                  errorMsg = getResources().getString(result.errorMsg().getRes());
                }
                Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
                ALog.d(TAG, "getRoleLiveData", "Error" + errorMsg);
              }
              reloadStatus++;
              updateTitleAction();
            });

    viewModel
        .getMemberLiveData()
        .observe(
            this,
            result -> {
              if (result.getLoadStatus() == LoadStatus.Success) {
                setData(result.getData());
                ALog.d(TAG, "getMemberLiveData", "Success");
              } else if (result.getLoadStatus() == LoadStatus.Finish) {
                if (result.getType() == FetchResult.FetchType.Add) {
                  addData(result.getTypeIndex(), result.getData());
                  ALog.d(TAG, "getMemberLiveData", "Add");
                } else if (result.getType() == FetchResult.FetchType.Remove) {
                  removeData(result.getTypeIndex());
                  checkAdapterData();
                  ALog.d(TAG, "getMemberLiveData", "Remove");
                }
              } else if (result.getLoadStatus() == LoadStatus.Error) {
                String errorMsg = null;
                if (result.errorMsg() != null) {
                  errorMsg = getResources().getString(result.errorMsg().getRes());
                }
                Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
                ALog.d(TAG, "getMemberLiveData", "Error");
              }
              reloadStatus++;
              updateTitleAction();
            });

    viewModel
        .getRoleMoreLiveData()
        .observe(
            this,
            result -> {
              if (result.getType() == FetchResult.FetchType.Remove) {
                removeData(result.getTypeIndex() + ROLE_INDEX);
                checkAdapterData();
                ALog.d(TAG, "getRoleMoreLiveData", "Remove");
              }
            });
  }

  /** if title type is last item ,it should be remove */
  private void checkAdapterData() {
    while (recyclerViewAdapter.getItemViewType(recyclerViewAdapter.getItemCount() - 1)
            == QChatViewType.CORNER_VIEW_TYPE
        && recyclerViewAdapter.getItemViewType(recyclerViewAdapter.getItemCount() - 2)
            == QChatViewType.TITLE_VIEW_TYPE) {
      recyclerViewAdapter.removeData(recyclerViewAdapter.getItemCount() - 2);
      recyclerViewAdapter.removeData(recyclerViewAdapter.getItemCount() - 1);
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
    ALog.d(TAG, "onResume");
    loadHeaderData();
    reloadStatus = 0;
    updateTitleAction();
    viewModel.fetchData(serverId, channelId);
  }

  @Override
  public void initView() {
    super.initView();
    viewBinding.commonActTitleView.setActionText(R.string.qchat_edit);
    viewBinding.commonActTitleView.setActionTextColor(
        getResources().getColor(R.color.color_337eff));
  }

  @Override
  public void onTitleActionClick(View view) {
    if (editStatus) {
      viewBinding.commonActTitleView.setActionText(R.string.qchat_edit);
    } else {
      viewBinding.commonActTitleView.setActionText(R.string.qchat_complete);
      viewModel.loadMoreRole();
    }
    editStatus = !editStatus;
    recyclerViewAdapter.setEditStatus(editStatus);
  }

  private void updateTitleAction() {
    if (reloadStatus < 2) {
      viewBinding.commonActTitleView.getRightTextView().setVisibility(View.GONE);
    } else {
      viewBinding.commonActTitleView.getRightTextView().setVisibility(View.VISIBLE);
    }
  }

  private void loadHeaderData() {
    ALog.d(TAG, "loadHeaderData");
    String[] titleArray =
        new String[] {
          getResources().getString(R.string.qchat_channel_add_role_title),
          getResources().getString(R.string.qchat_channel_add_member_title)
        };
    String[] titleRouter =
        new String[] {QChatConstant.ROUTER_ADD_ROLE, QChatConstant.ROUTER_ADD_MEMBER};
    setData(viewModel.getHeaderData(titleArray, titleRouter));
  }

  @Override
  public String getTitleText() {
    return getResources().getString(R.string.qchat_channel_permission_setting);
  }

  @Override
  public CommonViewHolder<QChatBaseBean> onCreateViewHolder(
      @NonNull ViewGroup parent, int viewType) {
    ALog.d(TAG, "onCreateViewHolder", "viewType" + viewType);
    if (viewType == QChatViewType.TITLE_VIEW_TYPE) {
      QChatTitleViewholderLayoutBinding viewBinding =
          QChatTitleViewholderLayoutBinding.inflate(getLayoutInflater(), parent, false);
      return new TitleViewHolder(viewBinding);
    } else if (viewType == QChatViewType.ARROW_VIEW_TYPE) {
      QChatArrowViewHolderBinding viewBinding =
          QChatArrowViewHolderBinding.inflate(getLayoutInflater(), parent, false);
      ArrowViewHolder arrowViewHolder = new ArrowViewHolder(viewBinding);
      arrowViewHolder.setItemOnClickListener(
          (data, position) -> {
            if (QChatConstant.ROUTER_ADD_ROLE.equals(data.router)) {
              QChatChannelAddRoleActivity.launch(this, serverId, channelId);
            } else if (QChatConstant.ROUTER_ADD_MEMBER.equals(data.router)) {
              QChatChannelAddMemberActivity.launch(this, serverId, channelId);
            }
          });
      return arrowViewHolder;
    } else if (viewType == QChatViewType.MORE_VIEW_TYPE) {
      QChatMoreViewholderLayoutBinding viewBinding =
          QChatMoreViewholderLayoutBinding.inflate(getLayoutInflater(), parent, false);
      MoreViewHolder moreViewHolder = new MoreViewHolder(viewBinding);
      moreViewHolder.setItemOnClickListener(
          (data, position) -> {
            viewModel.loadMoreRole();
          });
      return moreViewHolder;
    } else if (viewType == QChatViewType.CHANNEL_MEMBER_VIEW_TYPE) {
      QChatChannelMemberConerViewHolderBinding viewHolderBinding =
          QChatChannelMemberConerViewHolderBinding.inflate(getLayoutInflater(), parent, false);
      MemberViewHolder viewHolder = new MemberViewHolder(viewHolderBinding);
      viewHolder.setItemOnClickListener(
          (data, position) -> {;
            QChatMemberPermissionActivity.launch(
                this, ((QChatChannelMemberBean) data).channelMember);
          });
      viewHolder.setOnDeleteClickListener(this::showDeleteDialog);
      return viewHolder;
    } else if (viewType == QChatViewType.CHANNEL_ROLE_VIEW_TYPE) {
      QChatChannelRoleViewHolderBinding viewBinding =
          QChatChannelRoleViewHolderBinding.inflate(getLayoutInflater(), parent, false);
      RoleViewHolder roleViewHolder = new RoleViewHolder(viewBinding);
      roleViewHolder.setItemOnClickListener(
          (data, position) -> {
            if (data instanceof QChatChannelRoleBean) {
              QChatRolePermissionActivity.launch(this, ((QChatChannelRoleBean) data).channelRole);
            }
          });
      roleViewHolder.setOnDeleteClickListener(this::showDeleteDialog);
      return roleViewHolder;
    } else {
      QChatCornerViewholderLayoutBinding viewBinding =
          QChatCornerViewholderLayoutBinding.inflate(getLayoutInflater(), parent, false);
      CornerViewHolder cornerViewHolder = new CornerViewHolder(viewBinding);
      return cornerViewHolder;
    }
  }

  @Override
  public boolean isLoadMore() {
    ALog.d(TAG, "isLoadMore", "isLoadMore" + viewModel.hasMore());
    return viewModel.hasMore();
  }

  @Override
  public void loadMore(QChatBaseBean bean) {
    ALog.d(TAG, "loadMore");
    viewModel.loadMoreMember();
  }

  private void showDeleteDialog(QChatBaseBean data, int position) {
    if (data == null) {
      return;
    }
    ALog.d(TAG, "showDeleteDialog");
    String title = "";
    String content = "";
    if (data instanceof QChatChannelRoleBean) {
      content =
          String.format(
              getResources().getString(R.string.qchat_channel_role_delete_content),
              ((QChatChannelRoleBean) data).channelRole.getName());
      title = getString(R.string.qchat_channel_role_delete_title);
    } else if (data instanceof QChatChannelMemberBean) {
      content =
          String.format(
              getResources().getString(R.string.qchat_channel_member_delete_content),
              ((QChatChannelMemberBean) data).channelMember.getNick());
      title = getString(R.string.qchat_channel_member_delete_title);
    }

    CommonChoiceDialog commonConfirmDialog = new CommonChoiceDialog();
    commonConfirmDialog
        .setTitleStr(title)
        .setContentStr(content)
        .setPositiveStr(getString(R.string.qchat_delete))
        .setNegativeStr(getString(R.string.qchat_cancel))
        .setConfirmListener(
            new ChoiceListener() {
              @Override
              public void onNegative() {
                //do nothing
                ALog.d(TAG, "showDeleteDialog", "onNegative");
              }

              @Override
              public void onPositive() {
                ALog.d(TAG, "showDeleteDialog", "onPositive");
                viewModel.delete(data, findAdapterIndex(data));
              }
            })
        .show(getSupportFragmentManager());
  }

  private int findAdapterIndex(QChatBaseBean bean) {
    int count = recyclerViewAdapter.getItemCount();
    for (int index = 0; index < count; index++) {
      if (recyclerViewAdapter.getData(index) == bean) {
        return index;
      }
    }

    return -1;
  }

  public static void launch(Activity activity, long serverId, long channelId) {
    Intent intent = new Intent(activity, QChatChannelPermissionActivity.class);
    intent.putExtra(QChatConstant.SERVER_ID, serverId);
    intent.putExtra(QChatConstant.CHANNEL_ID, channelId);
    activity.startActivity(intent);
  }
}
