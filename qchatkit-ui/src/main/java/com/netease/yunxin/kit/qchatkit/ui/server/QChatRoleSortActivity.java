// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.qchatkit.ui.server;

import static com.netease.yunxin.kit.qchatkit.repo.QChatRoleRepoKt.MAX_ROLE_PAGE_SIZE;
import static com.netease.yunxin.kit.qchatkit.ui.model.QChatConstant.ROLE_EVERYONE_TYPE;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.netease.yunxin.kit.common.ui.activities.CommonActivity;
import com.netease.yunxin.kit.common.ui.dialog.ChoiceListener;
import com.netease.yunxin.kit.common.ui.dialog.CommonChoiceDialog;
import com.netease.yunxin.kit.corekit.im.IMKitClient;
import com.netease.yunxin.kit.qchatkit.repo.QChatRoleRepo;
import com.netease.yunxin.kit.qchatkit.repo.model.QChatServerInfo;
import com.netease.yunxin.kit.qchatkit.repo.model.QChatServerRoleInfo;
import com.netease.yunxin.kit.qchatkit.repo.model.ServerRoleResult;
import com.netease.yunxin.kit.qchatkit.ui.R;
import com.netease.yunxin.kit.qchatkit.ui.common.QChatCallback;
import com.netease.yunxin.kit.qchatkit.ui.databinding.QChatRoleSortActivityLayoutBinding;
import com.netease.yunxin.kit.qchatkit.ui.model.QChatConstant;
import com.netease.yunxin.kit.qchatkit.ui.server.adapter.QChatServerRolesAdapter;
import com.netease.yunxin.kit.qchatkit.ui.server.viewholder.QChatServerRoleViewHolder;
import java.util.List;
import java.util.Set;

public class QChatRoleSortActivity extends CommonActivity {

  private QChatRoleSortActivityLayoutBinding binding;

  private QChatServerInfo serverInfo;

  private QChatServerRolesAdapter rolesAdapter;

  private int firstPos = 0;

  private long topPriority;

  boolean isServerOwner;

  @NonNull
  @Override
  public View getContentView() {
    binding = QChatRoleSortActivityLayoutBinding.inflate(getLayoutInflater());
    return binding.getRoot();
  }

  @Override
  public void initView() {
    binding
        .title
        .setOnBackIconClickListener(v -> onBackPressed())
        .setTitle(R.string.qchat_role_sort)
        .setLeftText(R.string.qchat_cancel)
        .setActionText(R.string.qchat_save)
        .setActionTextColor(getResources().getColor(R.color.color_337eff))
        .setActionEnable(false)
        .setActionListener(
            v -> {
              //no roles will return directly
              if (rolesAdapter.getItemCount() == 0) {
                finish();
                return;
              }

              //topPriority is 0 will return directly
              topPriority = rolesAdapter.getTopPriority();
              if (topPriority == 0) {
                finish();
                return;
              }
              List<QChatServerRoleInfo> list =
                  rolesAdapter.getDataList().subList(firstPos, rolesAdapter.getItemCount());
              //only one role will return directly
              if (list.size() <= 1) {
                finish();
                return;
              }
              QChatRoleRepo.updateRolesPriorities(
                  serverInfo.getServerId(),
                  topPriority,
                  list,
                  new QChatCallback<Void>(getApplicationContext()) {
                    @Override
                    public void onSuccess(@Nullable Void param) {
                      super.onSuccess(param);
                      finish();
                    }
                  });
            });
    initSort();
  }

  private void initSort() {
    ItemTouchHelper.Callback touchCallback =
        new ItemTouchHelper.Callback() {
          @Override
          public int getMovementFlags(
              @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
            QChatServerRoleViewHolder qChatServerRoleViewHolder =
                (QChatServerRoleViewHolder) viewHolder;
            final int dragFlags =
                qChatServerRoleViewHolder.isDisableSort()
                    ? 0
                    : (ItemTouchHelper.UP | ItemTouchHelper.DOWN);
            final int swipeFlags = 0;
            return makeMovementFlags(dragFlags, swipeFlags);
          }

          @Override
          public boolean onMove(
              @NonNull RecyclerView recyclerView,
              @NonNull RecyclerView.ViewHolder viewHolder,
              @NonNull RecyclerView.ViewHolder target) {
            return true;
          }

          @Override
          public void onMoved(
              @NonNull RecyclerView recyclerView,
              @NonNull RecyclerView.ViewHolder viewHolder,
              int fromPos,
              @NonNull RecyclerView.ViewHolder target,
              int toPos,
              int x,
              int y) {
            rolesAdapter.onMove(fromPos, toPos);
            super.onMoved(recyclerView, viewHolder, fromPos, target, toPos, x, y);
          }

          @Override
          public boolean canDropOver(
              @NonNull RecyclerView recyclerView,
              @NonNull RecyclerView.ViewHolder current,
              @NonNull RecyclerView.ViewHolder target) {
            QChatServerRoleViewHolder currentHolder = (QChatServerRoleViewHolder) current;
            QChatServerRoleViewHolder targetHolder = (QChatServerRoleViewHolder) target;
            return !currentHolder.isDisableSort() && !targetHolder.isDisableSort();
          }

          @Override
          public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {}
        };
    ItemTouchHelper itemTouchHelper = new ItemTouchHelper(touchCallback);
    itemTouchHelper.attachToRecyclerView(binding.rvMembers);
  }

  @Override
  public void initData() {
    serverInfo = (QChatServerInfo) getIntent().getSerializableExtra(QChatConstant.SERVER_INFO);
    LinearLayoutManager layoutManager = new LinearLayoutManager(this);
    binding.rvMembers.setLayoutManager(layoutManager);
    rolesAdapter = new QChatServerRolesAdapter();
    rolesAdapter.setDeleteListener(this::showDeleteConfirmDialog);
    rolesAdapter.setSort(true);
    binding.rvMembers.setAdapter(rolesAdapter);
    getRolesList();
  }

  private void getRolesList() {
    QChatRoleRepo.fetchServerRoles(
        serverInfo.getServerId(),
        0,
        MAX_ROLE_PAGE_SIZE,
        new QChatCallback<ServerRoleResult>(getApplicationContext()) {
          @Override
          public void onSuccess(@Nullable ServerRoleResult param) {
            if (param != null && param.getRoleList() != null && !param.getRoleList().isEmpty()) {
              List<QChatServerRoleInfo> roleInfoList = param.getRoleList();
              Set<Long> isMemberList = param.isMemberRoleList();
              QChatServerRoleInfo roleInfo = roleInfoList.get(0);
              if (roleInfo != null && roleInfo.getType() == ROLE_EVERYONE_TYPE) {
                roleInfoList.remove(0);
              }
              isServerOwner = TextUtils.equals(serverInfo.getOwner(), IMKitClient.account());
              long myPriority = Long.MAX_VALUE;
              if (isMemberList != null) {
                for (QChatServerRoleInfo role : roleInfoList) {
                  firstPos++;
                  if (isMemberList.contains(role.getRoleId())) {
                    myPriority = role.getPriority();
                    break;
                  }
                }
              }
              if (isServerOwner) {
                firstPos = 0;
              }
              //only priority lower than self's can be sort
              if (firstPos < roleInfoList.size()) {
                topPriority = roleInfoList.get(firstPos).getPriority();
                binding.title.setActionEnable(true);
              }
              rolesAdapter.setMyPriority(myPriority);
              rolesAdapter.setServerOwner(isServerOwner);
              rolesAdapter.refresh(roleInfoList);
            }
          }
        });
  }

  private void showDeleteConfirmDialog(QChatServerRoleInfo roleInfo) {
    CommonChoiceDialog dialog = new CommonChoiceDialog();
    dialog
        .setNegativeStr(getResources().getString(R.string.qchat_cancel))
        .setTitleStr(getResources().getString(R.string.qchat_role_delete))
        .setContentStr(
            String.format(
                getResources().getString(R.string.qchat_role_delete_one_role), roleInfo.getName()))
        .setPositiveStr(getResources().getString(R.string.qchat_sure))
        .setConfirmListener(
            new ChoiceListener() {
              @Override
              public void onPositive() {
                deleteRole(roleInfo);
              }

              @Override
              public void onNegative() {}
            })
        .show(getSupportFragmentManager());
  }

  private void deleteRole(QChatServerRoleInfo roleInfo) {
    QChatRoleRepo.deleteServerRole(
        roleInfo.getServerId(),
        roleInfo.getRoleId(),
        new QChatCallback<Void>(getApplicationContext()) {
          @Override
          public void onSuccess(@Nullable Void param) {
            rolesAdapter.deleteItem(roleInfo);
          }
        });
  }

  @Override
  protected void initViewModel() {}

  public static void launch(Activity activity, QChatServerInfo serverInfo) {
    Intent intent = new Intent(activity, QChatRoleSortActivity.class);
    intent.putExtra(QChatConstant.SERVER_INFO, serverInfo);
    activity.startActivity(intent);
  }
}
