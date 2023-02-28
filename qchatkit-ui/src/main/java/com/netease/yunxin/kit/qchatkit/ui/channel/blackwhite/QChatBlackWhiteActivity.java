// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.qchatkit.ui.channel.blackwhite;

import static com.netease.yunxin.kit.qchatkit.ui.model.QChatConstant.REQUEST_MEMBER_FILTER_CHANNEL;
import static com.netease.yunxin.kit.qchatkit.ui.model.QChatConstant.REQUEST_MEMBER_SELECTOR_KEY;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.common.ui.dialog.ChoiceListener;
import com.netease.yunxin.kit.common.ui.dialog.CommonChoiceDialog;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.qchatkit.repo.model.QChatChannelModeEnum;
import com.netease.yunxin.kit.qchatkit.repo.model.QChatServerMemberInfo;
import com.netease.yunxin.kit.qchatkit.repo.model.QChatServerRoleMemberInfo;
import com.netease.yunxin.kit.qchatkit.ui.R;
import com.netease.yunxin.kit.qchatkit.ui.channel.blackwhite.viewholder.AddMemberViewHolder;
import com.netease.yunxin.kit.qchatkit.ui.channel.blackwhite.viewholder.MemberViewHolder;
import com.netease.yunxin.kit.qchatkit.ui.channel.permission.QChatMemberPermissionActivity;
import com.netease.yunxin.kit.qchatkit.ui.common.CommonListActivity;
import com.netease.yunxin.kit.qchatkit.ui.common.CommonViewHolder;
import com.netease.yunxin.kit.qchatkit.ui.databinding.QChatChannelNameListMemberViewHolderBinding;
import com.netease.yunxin.kit.qchatkit.ui.databinding.QChatNameListAddMemberViewHolderBinding;
import com.netease.yunxin.kit.qchatkit.ui.model.QChatBaseBean;
import com.netease.yunxin.kit.qchatkit.ui.model.QChatChannelMemberBean;
import com.netease.yunxin.kit.qchatkit.ui.model.QChatConstant;
import com.netease.yunxin.kit.qchatkit.ui.model.QChatServerMemberBean;
import com.netease.yunxin.kit.qchatkit.ui.model.QChatViewType;
import com.netease.yunxin.kit.qchatkit.ui.server.QChatMemberSelectorActivity;
import java.util.ArrayList;
import java.util.List;

/**
 * black and white name list page show the member list in channel,when the channel type is public it
 * is black list if the channel type is private,it is white list
 */
public class QChatBlackWhiteActivity extends CommonListActivity {

  private static final String TAG = "QChatBlackWhiteActivity";
  private BlackWhiteViewModel viewModel;
  private ActivityResultLauncher<Intent> selectorListLauncher;
  private boolean editStatus = false;
  private long channelId;
  private long serverId;
  private QChatChannelModeEnum channelType = QChatChannelModeEnum.Public;

  @Override
  public void initView() {
    super.initView();
    ALog.d(TAG, "initView");
    setBackgroundColor(R.color.color_white);
    changeStatusBarColor(R.color.color_white);
    viewBinding.commonActTitleView.setActionText(R.string.qchat_edit);
    viewBinding.commonActTitleView.setActionTextColor(
        getResources().getColor(R.color.color_337eff));
  }

  @Override
  public void initData() {
    super.initData();
    channelId = getIntent().getLongExtra(QChatConstant.CHANNEL_ID, 0);
    serverId = getIntent().getLongExtra(QChatConstant.SERVER_ID, 0);
    int type = getIntent().getIntExtra(QChatConstant.CHANNEL_TYPE, 0);
    if (type == QChatChannelModeEnum.Private.ordinal()) {
      channelType = QChatChannelModeEnum.Private;
    }
    ALog.d(TAG, "initData", "info:" + channelId + "," + serverId);
    viewModel = new ViewModelProvider(this).get(BlackWhiteViewModel.class);
    //observe the member list
    viewModel
        .getResultLiveData()
        .observe(
            this,
            result -> {
              if (result.getLoadStatus() == LoadStatus.Success) {
                addData(result.getData());
                ALog.d(TAG, "ResultLiveData", "Success");
              } else if (result.getLoadStatus() == LoadStatus.Finish) {
                if (result.getType() == FetchResult.FetchType.Add) {
                  addData(result.getTypeIndex(), result.getData());
                  ALog.d(TAG, "ResultLiveData", "Add" + result.getTypeIndex());
                } else if (result.getType() == FetchResult.FetchType.Remove) {
                  removeData(result.getTypeIndex());
                  ALog.d(TAG, "ResultLiveData", "Remove" + result.getTypeIndex());
                }
              } else if (result.getLoadStatus() == LoadStatus.Error) {
                FetchResult.ErrorMsg errorMsg = result.errorMsg();
                if (errorMsg != null
                    && errorMsg.getCode() != QChatConstant.ERROR_CODE_IM_NO_PERMISSION) {
                  Toast.makeText(
                          this, getResources().getString(errorMsg.getRes()), Toast.LENGTH_SHORT)
                      .show();
                }

                ALog.d(TAG, "ResultLiveData", "Error");
              }
            });
    //add member live data
    viewModel
        .getAddLiveData()
        .observe(
            this,
            result -> {
              if (result.getLoadStatus() == LoadStatus.Success) {
                loadData();
                ALog.d(TAG, "AddLiveData", "Success");
              } else if (result.getLoadStatus() == LoadStatus.Error) {
                ALog.d(TAG, "AddLiveData", "Error");
                Toast.makeText(
                        this,
                        getResources().getString(result.errorMsg().getRes()),
                        Toast.LENGTH_SHORT)
                    .show();
              }
            });

    viewModel
        .getRemoveLiveData()
        .observe(
            this,
            result -> {
              if (result.getLoadStatus() == LoadStatus.Error) {
                ALog.d(TAG, "AddLiveData", "Error");
                Toast.makeText(
                        this,
                        getResources().getString(result.errorMsg().getRes()),
                        Toast.LENGTH_SHORT)
                    .show();
              } else if (result.getLoadStatus() == LoadStatus.Finish) {
                if (result.getType() == FetchResult.FetchType.Remove) {
                  ALog.d(TAG, "AddLiveData", "Remove");
                  removeData(result.getTypeIndex());
                }
              }
            });

    registerResult();
    loadData();
  }

  private void loadData() {
    setData(viewModel.loadHeader());
    viewModel.fetchMemberList(serverId, channelId, channelType);
    ALog.d(TAG, "loadData");
  }

  @Override
  protected void onResume() {
    super.onResume();
    ALog.d(TAG, "onResume");
    //    loadData();
  }

  @Override
  public void onTitleActionClick(View view) {
    if (editStatus) {
      viewBinding.commonActTitleView.setActionText(R.string.qchat_edit);
    } else {
      viewBinding.commonActTitleView.setActionText(R.string.qchat_complete);
    }
    editStatus = !editStatus;
    ALog.d(TAG, "onTitleActionClick", "info:" + editStatus);
    recyclerViewAdapter.setEditStatus(editStatus);
  }

  @Override
  public String getTitleText() {
    return channelType == QChatChannelModeEnum.Public
        ? getResources().getString(R.string.qchat_channel_black_name_list)
        : getResources().getString(R.string.qchat_channel_white_name_list);
  }

  @Override
  public boolean isLoadMore() {
    return viewModel.hasMore();
  }

  @Override
  public CommonViewHolder<QChatBaseBean> onCreateViewHolder(
      @NonNull ViewGroup parent, int viewType) {
    if (viewType == QChatViewType.SERVER_MEMBER_VIEW_TYPE) {
      QChatChannelNameListMemberViewHolderBinding viewBinding =
          QChatChannelNameListMemberViewHolderBinding.inflate(getLayoutInflater(), parent, false);
      MemberViewHolder memberViewHolder = new MemberViewHolder(viewBinding);
      memberViewHolder.setOnClickListener(
          (data, position) -> {
            if (data instanceof QChatChannelMemberBean && !editStatus) {
              Intent intent = new Intent(this, QChatMemberPermissionActivity.class);
              Bundle bundle = new Bundle();
              bundle.putSerializable(
                  QChatConstant.CHANNEL_MEMBER, ((QChatChannelMemberBean) data).channelMember);
              intent.putExtras(bundle);
              startActivity(intent);
            }
          });
      memberViewHolder.setDeleteClickListener(
          (data, position) -> {
            if (data instanceof QChatServerMemberBean) {
              QChatServerMemberBean bean = (QChatServerMemberBean) data;
              int count = recyclerViewAdapter.getItemCount();
              for (int index = 1; index < count; index++) {
                if (bean.equals(recyclerViewAdapter.getData(index))) {
                  position = index;
                  break;
                }
              }
              showDeleteDialog(bean.serverMember, position);
            }
          });
      return memberViewHolder;
    } else if (viewType == QChatViewType.ARROW_VIEW_TYPE) {
      QChatNameListAddMemberViewHolderBinding viewBinding =
          QChatNameListAddMemberViewHolderBinding.inflate(getLayoutInflater(), parent, false);
      AddMemberViewHolder addMemberViewHolder = new AddMemberViewHolder(viewBinding);
      addMemberViewHolder.setItemOnClickListener(
          (data, position) -> {
            Intent intent =
                new Intent(QChatBlackWhiteActivity.this, QChatMemberSelectorActivity.class);
            intent.putExtra(QChatConstant.SERVER_ID, serverId);
            intent.putExtra(QChatConstant.CHANNEL_ID, channelId);
            intent.putExtra(QChatConstant.CHANNEL_TYPE, channelType.ordinal());
            intent.putExtra(QChatConstant.REQUEST_MEMBER_FILTER_KEY, REQUEST_MEMBER_FILTER_CHANNEL);
            selectorListLauncher.launch(intent);
          });
      return addMemberViewHolder;
    }
    return null;
  }

  private void registerResult() {
    selectorListLauncher =
        registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
              if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                ArrayList<QChatServerRoleMemberInfo> members =
                    result.getData().getParcelableArrayListExtra(REQUEST_MEMBER_SELECTOR_KEY);
                if (members != null && members.size() > 0) {
                  List<String> accIds = new ArrayList<>();
                  for (QChatServerRoleMemberInfo member : members) {
                    accIds.add(member.getAccId());
                  }
                  ALog.d(TAG, "registerResult", "addMember:" + accIds);
                  viewModel.addMember(serverId, channelId, accIds, channelType);
                }
              }
            });
  }

  private void showDeleteDialog(QChatServerMemberInfo data, int position) {
    if (data == null) {
      return;
    }

    String content =
        String.format(
            getResources().getString(R.string.qchat_channel_name_list_delete_content),
            data.getNick());
    CommonChoiceDialog commonConfirmDialog = new CommonChoiceDialog();
    commonConfirmDialog
        .setTitleStr(getString(R.string.qchat_channel_name_list_delete_title))
        .setContentStr(content)
        .setPositiveStr(getString(R.string.qchat_ensure))
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
                ALog.d(TAG, "showDeleteDialog", "onPositive:" + data.getAccId());
                viewModel.deleteMember(
                    serverId, channelId, channelType.ordinal(), data.getAccId(), position);
              }
            })
        .show(getSupportFragmentManager());
  }

  @Override
  public void loadMore(QChatBaseBean bean) {
    viewModel.loadMore(serverId, channelId, channelType);
  }

  public static void launch(Activity activity, long serverId, long channelId, int channelType) {
    Intent intent = new Intent(activity, QChatBlackWhiteActivity.class);
    intent.putExtra(QChatConstant.SERVER_ID, serverId);
    intent.putExtra(QChatConstant.CHANNEL_ID, channelId);
    intent.putExtra(QChatConstant.CHANNEL_TYPE, channelType);
    activity.startActivity(intent);
  }
}
