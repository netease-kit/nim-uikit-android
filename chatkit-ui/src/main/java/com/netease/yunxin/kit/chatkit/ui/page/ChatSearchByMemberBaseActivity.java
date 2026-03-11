// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.page;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.netease.nimlib.sdk.v2.conversation.enums.V2NIMConversationType;
import com.netease.nimlib.sdk.v2.message.enums.V2NIMMessageType;
import com.netease.yunxin.kit.chatkit.model.IMMessageInfo;
import com.netease.yunxin.kit.chatkit.ui.ChatViewHolderDefaultFactory;
import com.netease.yunxin.kit.chatkit.ui.IChatDefaultFactory;
import com.netease.yunxin.kit.chatkit.ui.common.MessageHelper;
import com.netease.yunxin.kit.chatkit.ui.interfaces.IMessageItemClickListener;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.chatkit.ui.page.adapter.ChatSearchAdapter;
import com.netease.yunxin.kit.chatkit.ui.page.viewmodel.ChatSearchMemberViewModel;
import com.netease.yunxin.kit.chatkit.utils.ConversationIdUtils;
import com.netease.yunxin.kit.chatkit.utils.ErrorUtils;
import com.netease.yunxin.kit.common.ui.activities.BaseLocalActivity;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.common.ui.widgets.BackTitleBar;
import com.netease.yunxin.kit.corekit.im2.model.IMMessageProgress;
import com.netease.yunxin.kit.corekit.im2.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import java.util.List;

public abstract class ChatSearchByMemberBaseActivity extends BaseLocalActivity {

  protected ChatSearchMemberViewModel viewModel;
  protected String teamId;
  protected String conversationId;
  protected RecyclerView searchRV;
  protected BackTitleBar messageSearchTitleBar;
  protected View emptyLayout;
  protected View loadingView;
  protected ChatSearchAdapter messageAdapter;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    initViewAndSetContentView(savedInstanceState);
    bindingView();
    initData();
  }

  protected void initViewAndSetContentView(@Nullable Bundle savedInstanceState) {}

  protected void bindingView() {
    setupCommonUI();
  }

  protected void initData() {
    initMemberSearchData();
    observeSearchResults();
  }

  protected void initMemberSearchData() {
    teamId = getIntent().getStringExtra(RouterConstant.CHAT_ID_KRY);
    if (teamId == null) {
      finish();
      return;
    }
    viewModel = new ViewModelProvider(this).get(ChatSearchMemberViewModel.class);
    viewModel.setConversationInfo(teamId, V2NIMConversationType.V2NIM_CONVERSATION_TYPE_TEAM);
    conversationId =
        ConversationIdUtils.conversationId(
            teamId, V2NIMConversationType.V2NIM_CONVERSATION_TYPE_TEAM);
  }

  protected void setupCommonUI() {
    if (searchRV != null) {
      LinearLayoutManager layoutManager = new LinearLayoutManager(this);
      searchRV.setLayoutManager(layoutManager);
      messageAdapter = new ChatSearchAdapter();
      searchRV.setAdapter(messageAdapter);
      messageAdapter.setViewHolderFactory(getChatFactory());
      messageAdapter.setItemClickListener(
          new IMessageItemClickListener() {
            @Override
            public boolean onMessageLongClick(
                View view, int position, ChatMessageBean messageBean) {
              if (messageBean.getMessageData().getMessage().getMessageType()
                  == V2NIMMessageType.V2NIM_MESSAGE_TYPE_TEXT) {
                MessageHelper.copyTextMessage(messageBean.getMessageData(), true);
              }
              return true;
            }

            @Override
            public boolean onMessageClick(View view, int position, ChatMessageBean messageInfo) {
              clickMessage(messageInfo.getMessageData());
              return true;
            }
          });
    }
    if (messageSearchTitleBar != null) {
      messageSearchTitleBar.setOnBackIconClickListener(v -> onBackPressed());
    }
  }

  protected void observeSearchResults() {
    if (viewModel == null) {
      return;
    }
    viewModel.getSearchMessagesLiveData().observeForever(this::onMessageLoad);
    viewModel.getUserChangeLiveData().observeForever(this::onUserChange);
    viewModel.getMessageDeletedLiveData().observeForever(this::onDeleteOrRevokeMessage);
  }

  protected void onUserChange(FetchResult<List<String>> result) {
    if (result.getLoadStatus() == LoadStatus.Success && result.getData() != null) {
      messageAdapter.notifyUserInfoChange(result.getData());
    }
  }

  protected void onDeleteOrRevokeMessage(FetchResult<List<String>> result) {
    if (result.getLoadStatus() == LoadStatus.Success && result.getData() != null) {
      for (String clientId : result.getData()) {
        messageAdapter.removeMessageByClientId(clientId);
      }
      switchResultView();
    }
  }

  protected void switchResultView() {
    if (emptyLayout == null || searchRV == null) {
      return;
    }
    if (messageAdapter.getItemCount() < 1) {
      emptyLayout.setVisibility(View.VISIBLE);
      searchRV.setVisibility(View.GONE);
    } else {
      emptyLayout.setVisibility(View.GONE);
      searchRV.setVisibility(View.VISIBLE);
    }
  }

  protected void startMemberSelector(ActivityResultLauncher<Intent> launcher) {
    if (teamId == null || launcher == null) {
      return;
    }
    XKitRouter.withKey(RouterConstant.PATH_TEAM_MEMBER_LIST_PAGE)
        .withParam(RouterConstant.KEY_TEAM_ID, teamId)
        .withParam(
            RouterConstant.KEY_TEAM_MEMBER_SELECT_MODE, RouterConstant.TEAM_MEMBER_MODE_SELECT)
        .withContext(this)
        .navigate(launcher);
  }

  protected IChatDefaultFactory getChatFactory() {
    return ChatViewHolderDefaultFactory.getInstance();
  }

  protected void clickMessage(IMMessageInfo messageInfo) {}

  public void showLoading() {
    if (loadingView != null) {
      loadingView.setVisibility(View.VISIBLE);
    }
    if (searchRV != null) {
      searchRV.setVisibility(View.GONE);
    }
    if (emptyLayout != null) {
      emptyLayout.setVisibility(View.GONE);
    }
  }

  public void hideLoading() {
    if (loadingView != null) {
      loadingView.setVisibility(View.GONE);
    }
  }

  private void onMessageLoad(FetchResult<List<ChatMessageBean>> result) {
    hideLoading();
    if (result.getLoadStatus() == LoadStatus.Success) {
      if (result.getType() == FetchResult.FetchType.Add) {
        messageAdapter.setFooterLoading(false);
        messageAdapter.setShowFooter(false);
        messageAdapter.appendMessages(result.getData());
      } else {
        messageAdapter.setMessagesData(result.getData());
      }
    } else if (result.getLoadStatus() == LoadStatus.Error && result.getError() != null) {
      messageAdapter.setShowFooter(false);
      ErrorUtils.showErrorCodeToast(
          ChatSearchByMemberBaseActivity.this, result.getError().getCode());
    }
    switchResultView();
  }

  protected void onAttachmentUpdateProgress(FetchResult<IMMessageProgress> fetchResult) {
    messageAdapter.updateMessageProgress(fetchResult.getData());
  }
}
