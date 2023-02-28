// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.qchatkit.ui.server;

import static com.netease.yunxin.kit.qchatkit.ui.model.QChatConstant.REQUEST_MEMBER_FILTER_CHANNEL;
import static com.netease.yunxin.kit.qchatkit.ui.model.QChatConstant.REQUEST_MEMBER_FILTER_ROLE;
import static com.netease.yunxin.kit.qchatkit.ui.model.QChatConstant.REQUEST_MEMBER_SELECTOR_KEY;

import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.netease.yunxin.kit.common.ui.activities.CommonActivity;
import com.netease.yunxin.kit.common.ui.activities.adapter.CommonMoreAdapter;
import com.netease.yunxin.kit.common.ui.activities.adapter.CommonMoreRecyclerViewDecorator;
import com.netease.yunxin.kit.common.ui.activities.viewholder.BaseMoreViewHolder;
import com.netease.yunxin.kit.common.ui.utils.AvatarColor;
import com.netease.yunxin.kit.qchatkit.repo.QChatServerRepo;
import com.netease.yunxin.kit.qchatkit.repo.model.QChatPageResult;
import com.netease.yunxin.kit.qchatkit.repo.model.QChatServerRoleMemberInfo;
import com.netease.yunxin.kit.qchatkit.ui.R;
import com.netease.yunxin.kit.qchatkit.ui.common.QChatCallback;
import com.netease.yunxin.kit.qchatkit.ui.databinding.QChatSelectorViewHolderBinding;
import com.netease.yunxin.kit.qchatkit.ui.databinding.QChatServerMemberSelectorActivityLayoutBinding;
import com.netease.yunxin.kit.qchatkit.ui.model.QChatConstant;
import com.netease.yunxin.kit.qchatkit.ui.server.adapter.QChatServerMemberListAdapter;
import java.util.ArrayList;
import java.util.List;

/** show member list and return selected members */
public class QChatMemberSelectorActivity extends CommonActivity {

  private static final int PAGE_SIZE = 20;

  private static final int MAX_SELECTED_NUM = 10;

  QChatServerMemberSelectorActivityLayoutBinding binding;

  CommonMoreAdapter<QChatServerRoleMemberInfo, QChatSelectorViewHolderBinding> selectorAdapter;

  private long serverId;
  private long channelId;
  private long roleId;
  private int channelType;

  private List<String> filterList;

  QChatServerMemberListAdapter memberAdapter;

  CommonMoreRecyclerViewDecorator<QChatServerRoleMemberInfo> decorator;

  @Override
  protected void initViewModel() {}

  @Nullable
  @Override
  protected View getContentView() {
    binding = QChatServerMemberSelectorActivityLayoutBinding.inflate(getLayoutInflater());
    return binding.getRoot();
  }

  @Override
  protected void initView() {
    binding
        .title
        .setTitle(R.string.qchat_select)
        .setOnBackIconClickListener(v -> onBackPressed())
        .setActionTextColor(getResources().getColor(R.color.color_337eff))
        .setActionText(R.string.qchat_sure)
        .setActionListener(v -> selectedAndBack());
    LinearLayoutManager selectorLayoutManager =
        new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
    binding.rvSelected.setLayoutManager(selectorLayoutManager);
    selectorAdapter =
        new CommonMoreAdapter<QChatServerRoleMemberInfo, QChatSelectorViewHolderBinding>() {
          @NonNull
          @Override
          public BaseMoreViewHolder<QChatServerRoleMemberInfo, QChatSelectorViewHolderBinding>
              getViewHolder(@NonNull ViewGroup parent, int viewType) {
            QChatSelectorViewHolderBinding binding =
                QChatSelectorViewHolderBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false);
            return new BaseMoreViewHolder<
                QChatServerRoleMemberInfo, QChatSelectorViewHolderBinding>(binding) {
              @Override
              public void bind(QChatServerRoleMemberInfo item) {
                String nick =
                    TextUtils.isEmpty(item.getNick()) ? item.getImNickname() : item.getNick();
                binding.avatar.setData(
                    item.getAvatarUrl(), nick, AvatarColor.avatarColor(item.getAccId()));
                binding.getRoot().setOnClickListener(v -> unselectedItem(item));
              }
            };
          }
        };
    binding.rvSelected.setAdapter(selectorAdapter);

    LinearLayoutManager memberLayoutManager = new LinearLayoutManager(this);
    binding.rvMembers.setLayoutManager(memberLayoutManager);
    memberAdapter =
        new QChatServerMemberListAdapter(
            (item, selected) -> {
              if (selected) {
                if (selectorAdapter.getItemCount() >= MAX_SELECTED_NUM) {
                  Toast.makeText(this, R.string.qchat_member_selector_max_count, Toast.LENGTH_LONG)
                      .show();
                  item.setSelected(false);
                  memberAdapter.update(item);
                } else {
                  selectorAdapter.append(item);
                }
              } else {
                selectorAdapter.deleteItem(item);
              }
            });
    binding.rvMembers.setAdapter(memberAdapter);
    decorator =
        new CommonMoreRecyclerViewDecorator<>(
            binding.rvMembers, memberLayoutManager, memberAdapter);
    decorator.setLoadMoreListener(data -> getMembers(data == null ? 0 : data.getCreateTime()));
  }

  private void selectedAndBack() {
    Intent result = new Intent();
    if (!selectorAdapter.getDataList().isEmpty()) {
      result.putExtra(REQUEST_MEMBER_SELECTOR_KEY, selectorAdapter.getDataList());
    }
    setResult(RESULT_OK, result);
    finish();
  }

  private void unselectedItem(QChatServerRoleMemberInfo item) {
    selectorAdapter.deleteItem(item);
    memberAdapter.setSelect(item, false);
  }

  @Override
  protected void initData() {
    serverId = getIntent().getLongExtra(QChatConstant.SERVER_ID, 0);
    channelId = getIntent().getLongExtra(QChatConstant.CHANNEL_ID, 0);
    channelType = getIntent().getIntExtra(QChatConstant.CHANNEL_TYPE, 0);
    roleId = getIntent().getLongExtra(QChatConstant.SERVER_ROLE_ID, 0);
    filterList = getIntent().getStringArrayListExtra(QChatConstant.REQUEST_MEMBER_FILTER_LIST);
    getMembers(0);
  }

  private void getMembers(long timeTag) {
    int filterType = getIntent().getIntExtra(QChatConstant.REQUEST_MEMBER_FILTER_KEY, 0);
    QChatCallback<QChatPageResult<QChatServerRoleMemberInfo>> memberCallback =
        new QChatCallback<QChatPageResult<QChatServerRoleMemberInfo>>(this) {
          @Override
          public void onSuccess(@Nullable QChatPageResult<QChatServerRoleMemberInfo> param) {
            if (param != null) {
              if (param.getDataList() != null && param.getDataList().size() > 0) {
                List<QChatServerRoleMemberInfo> memberList = new ArrayList<>(param.getDataList());
                if (filterList != null && !filterList.isEmpty()) {
                  for (QChatServerRoleMemberInfo member : param.getDataList()) {
                    if (filterList.contains(member.getAccId())) {
                      memberList.remove(member);
                    }
                  }
                }
                if (timeTag == 0) {
                  memberAdapter.refresh(memberList);
                  binding.emptyLayout.setVisibility(View.GONE);
                } else {
                  memberAdapter.append(memberList);
                }
              } else {
                if (timeTag == 0) {
                  binding.emptyLayout.setVisibility(View.VISIBLE);
                }
              }
              decorator.setHasMore(param.getHasMore());
              decorator.setNextTimeTag(param.getNextTimeTag());
            }
          }
        };
    switch (filterType) {
      case REQUEST_MEMBER_FILTER_ROLE:
        QChatServerRepo.fetchServerMembersWithRolesFilter(
            serverId, roleId, timeTag, PAGE_SIZE, memberCallback);
        break;
      case REQUEST_MEMBER_FILTER_CHANNEL:
        QChatServerRepo.fetchServerMembersWithWhiteBlackFilter(
            serverId, channelId, channelType, timeTag, PAGE_SIZE, memberCallback);
        break;
      default:
        //no filter
        QChatServerRepo.getServerMembersWithFilter(serverId, timeTag, PAGE_SIZE, memberCallback);
        break;
    }
  }
}
