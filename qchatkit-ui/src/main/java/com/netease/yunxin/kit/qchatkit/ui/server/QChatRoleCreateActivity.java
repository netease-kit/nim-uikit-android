// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.qchatkit.ui.server;

import static com.netease.yunxin.kit.qchatkit.ui.model.QChatConstant.REQUEST_MEMBER_SELECTOR_KEY;

import android.app.Activity;
import android.content.Intent;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.netease.yunxin.kit.common.ui.activities.CommonActivity;
import com.netease.yunxin.kit.qchatkit.repo.model.QChatServerRoleMemberInfo;
import com.netease.yunxin.kit.qchatkit.ui.R;
import com.netease.yunxin.kit.qchatkit.ui.common.QChatCallback;
import com.netease.yunxin.kit.qchatkit.ui.databinding.QChatRoleGroupCreatorActivityLayoutBinding;
import com.netease.yunxin.kit.qchatkit.ui.model.QChatConstant;
import com.netease.yunxin.kit.qchatkit.ui.server.adapter.QChatServerMemberListAdapter;
import com.netease.yunxin.kit.qchatkit.ui.server.viewmodel.QChatRoleCreateViewModel;
import java.util.ArrayList;
import java.util.List;

/** create a role in server */
public class QChatRoleCreateActivity extends CommonActivity {

  private QChatRoleGroupCreatorActivityLayoutBinding binding;

  private QChatRoleCreateViewModel viewModel;

  private long serverId;

  private ActivityResultLauncher<Intent> selectorListLauncher;

  private QChatServerMemberListAdapter memberListAdapter;

  @NonNull
  @Override
  public View getContentView() {
    binding = QChatRoleGroupCreatorActivityLayoutBinding.inflate(getLayoutInflater());
    return binding.getRoot();
  }

  @Override
  public void initView() {
    registerResult();
    binding
        .title
        .setOnBackIconClickListener(v -> onBackPressed())
        .setTitle(R.string.qchat_create_new_role_group)
        .setActionText(R.string.qchat_create)
        .setActionEnable(false)
        .setActionTextColor(getResources().getColor(R.color.color_337eff))
        .setActionListener(
            v -> viewModel.createRole(serverId, binding.chatRoleName.getText().toString().trim()));

    binding.rlyMemberAdd.setOnClickListener(
        v -> {
          Intent intent =
              new Intent(QChatRoleCreateActivity.this, QChatMemberSelectorActivity.class);
          intent.putExtra(QChatConstant.SERVER_ID, serverId);
          intent.putExtra(
              QChatConstant.REQUEST_MEMBER_FILTER_LIST,
              new ArrayList<>(viewModel.getSelectedUsers()));
          selectorListLauncher.launch(intent);
        });

    binding.rvMember.setLayoutManager(new LinearLayoutManager(this));
    memberListAdapter =
        new QChatServerMemberListAdapter(
            item -> {
              memberListAdapter.deleteItem(item);
              viewModel.deleteSelectMember(item.getAccId());
            });
    binding.rvMember.setAdapter(memberListAdapter);
    binding.chatRoleName.addTextChangedListener(
        new TextWatcher() {
          @Override
          public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

          @Override
          public void onTextChanged(CharSequence s, int start, int before, int count) {
            binding.title.setActionEnable(!TextUtils.isEmpty(s));
          }

          @Override
          public void afterTextChanged(Editable s) {}
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
                  viewModel.addSelectMember(accIds);
                  memberListAdapter.append(members);
                }
              }
            });
  }

  @Override
  public void initData() {
    serverId = getIntent().getLongExtra(QChatConstant.SERVER_ID, 0);
    viewModel.getCreateResult().observe(this, aBoolean -> finish());

    viewModel
        .getErrorLiveData()
        .observe(
            this, errorMsg -> QChatCallback.showToast(errorMsg.getCode(), getApplicationContext()));
  }

  @Override
  protected void initViewModel() {
    viewModel = new ViewModelProvider(this).get(QChatRoleCreateViewModel.class);
  }

  public static void launch(Activity activity, long serverId) {
    Intent intent = new Intent(activity, QChatRoleCreateActivity.class);
    intent.putExtra(QChatConstant.SERVER_ID, serverId);
    activity.startActivity(intent);
  }
}
