// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.conversationkit.ui.page;

import android.os.Bundle;
import android.text.TextUtils;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import com.netease.yunxin.kit.common.utils.NetworkUtils;
import com.netease.yunxin.kit.conversationkit.ui.R;
import com.netease.yunxin.kit.conversationkit.ui.common.ConversationGroupErrorHelper;
import com.netease.yunxin.kit.conversationkit.ui.model.ConversationBean;
import com.netease.yunxin.kit.conversationkit.ui.page.viewmodel.ConversationGroupAddViewModel;
import com.netease.yunxin.kit.corekit.im2.utils.RouterConstant;
import java.util.List;
import java.util.Set;

public class ConversationGroupAddConversationActivity extends BaseRecentConversationSelectActivity {

  private ConversationGroupAddViewModel viewModel;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    String groupId = getIntent().getStringExtra(RouterConstant.KEY_CONVERSATION_GROUP_ID);
    if (TextUtils.isEmpty(groupId)) {
      finish();
      return;
    }
    viewModel = new ViewModelProvider(this).get(ConversationGroupAddViewModel.class);
    setContentView(createSelectContentView());
    bindViewModel();
    viewModel.init(groupId);
  }

  private void bindViewModel() {
    viewModel
        .getConversationLiveData()
        .observe(this, data -> renderConversations(viewModel.filter(keyword)));
    viewModel.getSelectedLiveData().observe(this, this::updateSelectedConversations);
  }

  @Override
  protected String getTitleText() {
    return getString(
        R.string.conversation_group_add_title,
        viewModel == null ? 0 : viewModel.getSelectedCount(),
        ConversationGroupAddViewModel.GROUP_MEMBER_MAX_COUNT);
  }

  @Override
  protected List<ConversationBean> getFilteredConversations(String keyword) {
    return viewModel.filter(keyword);
  }

  @Override
  protected Set<String> getSelectedConversationIds() {
    return viewModel.getSelectedLiveData().getValue();
  }

  @Override
  protected boolean hasMoreConversations() {
    return viewModel.hasMore();
  }

  @Override
  protected void loadMoreConversations() {
    viewModel.loadRecentConversations(false);
  }

  @Override
  protected void toggleConversation(ConversationBean bean) {
    if (!viewModel.toggle(bean)) {
      ToastX.showShortToast(R.string.conversation_group_member_limit_tip);
    }
  }

  @Override
  protected void submitSelectedConversations() {
    Set<String> selectedIds = getSelectedConversationIds();
    if (selectedIds == null || selectedIds.isEmpty()) {
      return;
    }
    if (!NetworkUtils.isConnected()) {
      ToastX.showShortToast(R.string.conversation_network_error_tip);
      return;
    }
    viewModel.submit(
        new ConversationGroupAddViewModel.SubmitCallback() {
          @Override
          public void onError(int errorCode, @Nullable String errorMsg) {
            ConversationGroupErrorHelper.showErrorToast(
                ConversationGroupAddConversationActivity.this,
                errorCode,
                R.string.conversation_group_add_failed);
          }

          @Override
          public void onSuccess() {
            finish();
          }

          @Override
          public void onPartialSuccess() {
            ToastX.showShortToast(R.string.conversation_group_add_partial_success);
            finish();
          }
        });
  }
}
