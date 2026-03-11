// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.normal.search;

import android.content.Intent;
import android.os.Bundle;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.netease.yunxin.kit.chatkit.model.IMMessageInfo;
import com.netease.yunxin.kit.chatkit.ui.IChatDefaultFactory;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatSearchMemberActivityBinding;
import com.netease.yunxin.kit.chatkit.ui.normal.factory.ChatViewHolderFactory;
import com.netease.yunxin.kit.chatkit.ui.page.ChatSearchByMemberBaseActivity;
import com.netease.yunxin.kit.common.utils.KeyboardUtils;
import com.netease.yunxin.kit.corekit.im2.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;

/** 只保留“按群成员查找”的历史消息页面，不含搜索框。 */
public class ChatSearchMemberActivity extends ChatSearchByMemberBaseActivity {

  private static final String TAG = "ChatSearchMemberActivity";

  private ChatSearchMemberActivityBinding binding;
  private ActivityResultLauncher<Intent> memberSelectLauncher;
  private String selectedAccountId;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    changeStatusBarColor(R.color.color_white);
  }

  @Override
  protected void initViewAndSetContentView(@Nullable Bundle savedInstanceState) {
    binding = ChatSearchMemberActivityBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());
    searchRV = binding.searchRv;
    emptyLayout = binding.emptyLayout;
    loadingView = binding.loadingView;
    messageSearchTitleBar = binding.searchTitleBar;
  }

  @Override
  protected void bindingView() {
    setupCommonUI();
    if (messageSearchTitleBar != null) {
      messageSearchTitleBar.setTitle(R.string.chat_search_member_by_user);
      messageSearchTitleBar.setOnBackIconClickListener(v -> onBackPressed());
    }
    searchRV.addOnScrollListener(
        new RecyclerView.OnScrollListener() {
          @Override
          public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            RecyclerView.LayoutManager lm = recyclerView.getLayoutManager();
            if (lm instanceof LinearLayoutManager) {
              int lastVisible = ((LinearLayoutManager) lm).findLastVisibleItemPosition();
              int total = messageAdapter != null ? messageAdapter.getItemCount() : 0;
              if (lastVisible >= total - 1) {
                if (viewModel.hasMoreLocal()) {
                  viewModel.searchNextPageBySender();
                  messageAdapter.setFooterLoading(true);
                } else {
                  messageAdapter.setFooterLoading(false);
                  messageAdapter.setShowFooter(total > 0);
                }
              } else if (viewModel.hasMoreLocal()) {
                messageAdapter.setShowFooter(false);
              }
            }
          }
        });
  }

  @Override
  protected void initData() {
    super.initData();
    memberSelectLauncher =
        registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
              Intent data = result.getData();
              if (result.getResultCode() == RESULT_OK && data != null && teamId != null) {
                String accountId =
                    data.getStringExtra(RouterConstant.KEY_TEAM_MEMBER_RESULT_SELECTED_ACCOUNT_ID);
                if (accountId != null) {
                  showLoading();
                  viewModel.searchMessageBySender(accountId);
                } else {
                  finish();
                }
              } else {
                finish();
              }
            });
    startSelectMember();
  }

  @Override
  protected void clickMessage(IMMessageInfo messageInfo) {
    KeyboardUtils.hideKeyboard(this);
    String routerPath = RouterConstant.PATH_CHAT_TEAM_PAGE;
    XKitRouter.withKey(routerPath)
        .withParam(RouterConstant.KEY_MESSAGE_INFO, messageInfo)
        .withParam(RouterConstant.CHAT_ID_KRY, teamId)
        .withContext(this)
        .navigate();
    finish();
  }

  protected IChatDefaultFactory getChatFactory() {
    return ChatViewHolderFactory.getInstance();
  }

  private void startSelectMember() {
    if (teamId == null) {
      return;
    }
    startMemberSelector(memberSelectLauncher);
  }
}
