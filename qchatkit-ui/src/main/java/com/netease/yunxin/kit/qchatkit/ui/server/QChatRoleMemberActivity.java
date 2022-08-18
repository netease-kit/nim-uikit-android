// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.qchatkit.ui.server;

import static com.netease.yunxin.kit.qchatkit.ui.model.QChatConstant.REQUEST_MEMBER_SELECTOR_KEY;
import static com.netease.yunxin.kit.qchatkit.ui.model.QChatConstant.REQUEST_MEMBER_SIZE_KEY;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.netease.yunxin.kit.common.ui.activities.CommonActivity;
import com.netease.yunxin.kit.common.ui.activities.adapter.CommonMoreRecyclerViewDecorator;
import com.netease.yunxin.kit.common.ui.dialog.ChoiceListener;
import com.netease.yunxin.kit.common.ui.dialog.CommonChoiceDialog;
import com.netease.yunxin.kit.qchatkit.repo.QChatRoleRepo;
import com.netease.yunxin.kit.qchatkit.repo.model.QChatServerRoleInfo;
import com.netease.yunxin.kit.qchatkit.repo.model.QChatServerRoleMemberInfo;
import com.netease.yunxin.kit.qchatkit.ui.R;
import com.netease.yunxin.kit.qchatkit.ui.common.QChatCallback;
import com.netease.yunxin.kit.qchatkit.ui.databinding.QChatRoleMemberActivityLayoutBinding;
import com.netease.yunxin.kit.qchatkit.ui.model.QChatConstant;
import com.netease.yunxin.kit.qchatkit.ui.server.adapter.QChatServerMemberListAdapter;
import java.util.ArrayList;
import java.util.List;

/** manager member in server role */
public class QChatRoleMemberActivity extends CommonActivity {

  private static final int PAGE_SIZE = 20;

  private QChatRoleMemberActivityLayoutBinding binding;

  private QChatServerRoleInfo roleInfo;

  private QChatServerMemberListAdapter memberAdapter;

  private ActivityResultLauncher<Intent> selectorListLauncher;

  @NonNull
  @Override
  public View getContentView() {
    binding = QChatRoleMemberActivityLayoutBinding.inflate(getLayoutInflater());
    return binding.getRoot();
  }

  @Override
  public void initView() {
    registerResult();
    binding
        .title
        .setOnBackIconClickListener(v -> onBackPressed())
        .setTitle(R.string.qchat_member_manager);
    binding.rlyMemberModify.setOnClickListener(
        v -> {
          Intent intent =
              new Intent(QChatRoleMemberActivity.this, QChatMemberSelectorActivity.class);
          intent.putExtra(QChatConstant.SERVER_ID, roleInfo.getServerId());
          intent.putExtra(
              QChatConstant.REQUEST_MEMBER_FILTER_KEY, QChatConstant.REQUEST_MEMBER_FILTER_ROLE);
          intent.putExtra(QChatConstant.SERVER_ROLE_ID, roleInfo.getRoleId());
          selectorListLauncher.launch(intent);
        });
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
                  addMembers(accIds);
                }
              }
            });
  }

  @Override
  public void initData() {
    roleInfo =
        (QChatServerRoleInfo) getIntent().getSerializableExtra(QChatConstant.SERVER_ROLE_INFO);
    if (roleInfo == null) return;
    binding.tvNumber.setText(String.valueOf(roleInfo.getMemberCount()));
    LinearLayoutManager layoutManager = new LinearLayoutManager(this);
    binding.rvMembers.setLayoutManager(layoutManager);
    memberAdapter = new QChatServerMemberListAdapter(this::showDeleteConfirmDialog);
    binding.rvMembers.setAdapter(memberAdapter);
    CommonMoreRecyclerViewDecorator<QChatServerRoleMemberInfo> decorator =
        new CommonMoreRecyclerViewDecorator<>(binding.rvMembers, layoutManager, memberAdapter);
    decorator.setLoadMoreListener(
        data ->
            getMembers(
                data == null ? 0 : data.getCreateTime(), data == null ? null : data.getAccId()));
  }

  @Override
  protected void onResume() {
    super.onResume();
    getMembers();
  }

  private void showDeleteConfirmDialog(QChatServerRoleMemberInfo item) {
    String nick = TextUtils.isEmpty(item.getNick()) ? item.getImNickname() : item.getNick();
    CommonChoiceDialog dialog = new CommonChoiceDialog();
    dialog
        .setTitleStr(getResources().getString(R.string.qchat_delete_member))
        .setContentStr(
            String.format(getResources().getString(R.string.qchat_delete_some_member), nick))
        .setNegativeStr(getResources().getString(R.string.qchat_cancel))
        .setPositiveStr(getResources().getString(R.string.qchat_sure))
        .setConfirmListener(
            new ChoiceListener() {
              @Override
              public void onPositive() {
                deleteMember(item);
              }

              @Override
              public void onNegative() {
                //do nothing
              }
            })
        .show(getSupportFragmentManager());
  }

  private void deleteMember(QChatServerRoleMemberInfo item) {
    List<String> accIds = new ArrayList<>();
    accIds.add(item.getAccId());
    QChatRoleRepo.removeServerRoleMember(
        roleInfo.getServerId(),
        roleInfo.getRoleId(),
        accIds,
        new QChatCallback<List<String>>(getApplicationContext()) {
          @Override
          public void onSuccess(@Nullable List<String> param) {
            if (param != null && !param.isEmpty()) {
              memberAdapter.deleteItem(item);
              roleInfo.setMemberCount(roleInfo.getMemberCount() - 1);
              binding.tvNumber.setText(String.valueOf(roleInfo.getMemberCount()));
            }
            super.onSuccess(param);
          }
        });
  }

  private void addMembers(List<String> accIds) {
    QChatRoleRepo.addServerRoleMember(
        roleInfo.getServerId(),
        roleInfo.getRoleId(),
        accIds,
        new QChatCallback<List<String>>(getApplicationContext()) {
          @Override
          public void onSuccess(@Nullable List<String> param) {
            if (param != null && !param.isEmpty()) {
              getMembers();
              roleInfo.setMemberCount(roleInfo.getMemberCount() + accIds.size());
              binding.tvNumber.setText(String.valueOf(roleInfo.getMemberCount()));
            }
          }
        });
  }

  private void getMembers() {
    getMembers(0, null);
  }

  private void getMembers(long timeTag, String accId) {
    QChatRoleRepo.fetchServerRoleMember(
        roleInfo.getServerId(),
        roleInfo.getRoleId(),
        timeTag,
        PAGE_SIZE,
        accId,
        new QChatCallback<List<QChatServerRoleMemberInfo>>(getApplicationContext()) {
          @Override
          public void onSuccess(@Nullable List<QChatServerRoleMemberInfo> param) {
            if (timeTag == 0 && param != null) {
              memberAdapter.refresh(param);
              roleInfo.setMemberCount(memberAdapter.getItemCount());
              binding.tvNumber.setText(String.valueOf(memberAdapter.getItemCount()));
            }
          }
        });
  }

  @Override
  public void onBackPressed() {
    Intent result = new Intent();
    result.putExtra(REQUEST_MEMBER_SIZE_KEY, memberAdapter.getItemCount());
    setResult(RESULT_OK, result);
    super.onBackPressed();
  }

  @Override
  protected void initViewModel() {}

  public static void launch(Activity activity, QChatServerRoleInfo data) {
    Intent intent = new Intent(activity, QChatRoleMemberActivity.class);
    intent.putExtra(QChatConstant.SERVER_ROLE_INFO, data);
    activity.startActivity(intent);
  }
}
