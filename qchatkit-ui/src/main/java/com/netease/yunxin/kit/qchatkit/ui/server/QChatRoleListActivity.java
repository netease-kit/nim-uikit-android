// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.qchatkit.ui.server;

import static com.netease.yunxin.kit.qchatkit.ui.model.QChatConstant.ROLE_EVERYONE_TYPE;

import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.netease.yunxin.kit.common.ui.activities.CommonActivity;
import com.netease.yunxin.kit.common.ui.activities.adapter.CommonMoreRecyclerViewDecorator;
import com.netease.yunxin.kit.qchatkit.repo.QChatRoleRepo;
import com.netease.yunxin.kit.qchatkit.repo.model.QChatServerInfo;
import com.netease.yunxin.kit.qchatkit.repo.model.QChatServerRoleInfo;
import com.netease.yunxin.kit.qchatkit.repo.model.ServerRoleResult;
import com.netease.yunxin.kit.qchatkit.ui.R;
import com.netease.yunxin.kit.qchatkit.ui.common.QChatCallback;
import com.netease.yunxin.kit.qchatkit.ui.databinding.QChatRoleListActivityLayoutBinding;
import com.netease.yunxin.kit.qchatkit.ui.model.QChatConstant;
import com.netease.yunxin.kit.qchatkit.ui.server.adapter.QChatServerRolesAdapter;
import java.util.List;

public class QChatRoleListActivity extends CommonActivity {

  private QChatRoleListActivityLayoutBinding binding;

  private QChatServerRoleInfo everyOneRole;

  private QChatServerInfo serverInfo;

  private static final int PAGE_SIZE = 100;

  private QChatServerRolesAdapter rolesAdapter;

  @NonNull
  @Override
  public View getContentView() {
    binding = QChatRoleListActivityLayoutBinding.inflate(getLayoutInflater());
    return binding.getRoot();
  }

  @Override
  public void initView() {
    binding
        .title
        .setOnBackIconClickListener(v -> onBackPressed())
        .setTitle(R.string.qchat_roles)
        .setActionImg(R.drawable.ic_add)
        .setActionListener(
            v -> {
              QChatRoleCreateActivity.launch(this, serverInfo.getServerId());
            });
    binding.clEveryone.setOnClickListener(
        v -> {
          if (everyOneRole != null) {
            gotoRoleInfo(everyOneRole);
          }
        });
    binding.tvSort.setOnClickListener(
        v -> {
          if (rolesAdapter.getItemCount() > 0) {
            QChatRoleSortActivity.launch(this, serverInfo);
          }
        });
  }

  @Override
  public void initData() {
    serverInfo = (QChatServerInfo) getIntent().getSerializableExtra(QChatConstant.SERVER_INFO);
    LinearLayoutManager layoutManager = new LinearLayoutManager(this);
    binding.rvRoles.setLayoutManager(layoutManager);
    rolesAdapter = new QChatServerRolesAdapter();
    rolesAdapter.setItemClickListener((item, position) -> gotoRoleInfo(item));
    binding.rvRoles.setAdapter(rolesAdapter);
    CommonMoreRecyclerViewDecorator<QChatServerRoleInfo> decorator =
        new CommonMoreRecyclerViewDecorator<>(binding.rvRoles, layoutManager, rolesAdapter);
    decorator.setLoadMoreListener(data -> getRolesList(data == null ? 0 : data.getCreateTime()));
  }

  private void gotoRoleInfo(QChatServerRoleInfo info) {
    QChatRoleSettingActivity.launch(this, info);
  }

  @Override
  protected void onResume() {
    super.onResume();
    getRolesList(0);
  }

  private void getRolesList(long timeTag) {
    QChatRoleRepo.fetchServerRoles(
        serverInfo.getServerId(),
        timeTag,
        PAGE_SIZE,
        new QChatCallback<ServerRoleResult>(getApplicationContext()) {
          @Override
          public void onSuccess(@Nullable ServerRoleResult param) {
            if (param != null && param.getRoleList() != null && !param.getRoleList().isEmpty()) {
              List<QChatServerRoleInfo> roleInfoList = param.getRoleList();
              QChatServerRoleInfo roleInfo = roleInfoList.get(0);
              if (roleInfo != null && roleInfo.getType() == ROLE_EVERYONE_TYPE) {
                binding.tvName.setText(roleInfo.getName());
                everyOneRole = roleInfo;
                roleInfoList.remove(0);
              }
              if (timeTag == 0) {
                rolesAdapter.refresh(roleInfoList);
                if (!roleInfoList.isEmpty()) {
                  binding.rlyRoleTitle.setVisibility(View.VISIBLE);
                  binding.tvRoleCount.setText(
                      getString(R.string.qchat_roles_count, roleInfoList.size()));
                } else {
                  binding.rlyRoleTitle.setVisibility(View.GONE);
                }
              } else {
                rolesAdapter.append(roleInfoList);
              }
            }
            super.onSuccess(param);
          }

          @Override
          public void onFailed(int code) {
            super.onFailed(code);
            binding.tvRoleCount.setText(getString(R.string.qchat_roles_count, 0));
          }

          @Override
          public void onException(@Nullable Throwable exception) {
            super.onException(exception);
            binding.tvRoleCount.setText(getString(R.string.qchat_roles_count, 0));
          }
        });
  }

  @Override
  protected void initViewModel() {}
}
