// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.page;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
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
import com.netease.yunxin.kit.chatkit.ui.page.viewmodel.ChatSearchKeywordViewModel;
import com.netease.yunxin.kit.chatkit.utils.ConversationIdUtils;
import com.netease.yunxin.kit.chatkit.utils.ErrorUtils;
import com.netease.yunxin.kit.common.ui.activities.BaseLocalActivity;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.common.ui.widgets.BackTitleBar;
import com.netease.yunxin.kit.corekit.im2.model.IMMessageProgress;
import com.netease.yunxin.kit.corekit.im2.utils.RouterConstant;
import java.util.List;

/**
 * History message search page for Team chat search history message and jump back to the team chat
 * page
 */
public class ChatSearchBaseActivity extends BaseLocalActivity {
  private static final String TAG = "ChatSearchActivity";
  protected RecyclerView searchRV;

  protected BackTitleBar messageSearchTitleBar;

  protected EditText searchET;
  protected View clearIV;

  protected View emptyLayout;

  protected View defaultView;

  protected View loadingView;

  protected ChatSearchKeywordViewModel viewModel;
  protected String accountId;
  protected V2NIMConversationType conversationType;
  protected String conversationId;
  protected ChatSearchAdapter messageAdapter;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    initViewAndSetContentView(savedInstanceState);
    bindingView();
    initData();
  }

  protected void bindingView() {
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

    searchRV.addOnScrollListener(
        new RecyclerView.OnScrollListener() {
          @Override
          public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            RecyclerView.LayoutManager lm = recyclerView.getLayoutManager();
            if (lm instanceof LinearLayoutManager) {
              int lastVisible = ((LinearLayoutManager) lm).findLastVisibleItemPosition();
              int total = messageAdapter != null ? messageAdapter.getItemCount() : 0;
              if (lastVisible >= total - 5) {
                if (viewModel.hasMoreLocal()) {
                  viewModel.searchNextPageByKeyword();
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

    if (messageSearchTitleBar != null) {
      messageSearchTitleBar.setOnBackIconClickListener(v -> onBackPressed());
    }

    if (clearIV != null) {
      clearIV.setOnClickListener(v -> searchET.setText(""));
    }

    if (searchET != null) {
      searchET.addTextChangedListener(
          new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
              if (TextUtils.isEmpty(s)) {
                //如果为空，清空搜索结果
                viewModel.searchMessageByKeyword(null);
              }
              if (clearIV != null) {
                if (TextUtils.isEmpty(String.valueOf(s))) {
                  clearIV.setVisibility(View.GONE);
                } else {
                  clearIV.setVisibility(View.VISIBLE);
                }
              }
            }
          });
      searchET.setOnEditorActionListener(
          (textView, i, keyEvent) -> {
            String searchKey = String.valueOf(searchET.getEditableText());
            if (!TextUtils.isEmpty(searchKey)) {
              showLoading();
            }
            viewModel.searchMessageByKeyword(searchKey);
            return true;
          });
    }
  }

  protected void initViewAndSetContentView(@Nullable Bundle savedInstanceState) {}

  protected void initData() {
    accountId = getIntent().getStringExtra(RouterConstant.CHAT_ID_KRY);
    conversationType =
        V2NIMConversationType.typeOfValue(
            getIntent().getIntExtra(RouterConstant.CHAT_CONVERSATION_TYPE_KRY, 0));
    if (TextUtils.isEmpty(accountId) || conversationType == null) {
      finish();
    }
    conversationId = ConversationIdUtils.conversationId(accountId, conversationType);
    viewModel = new ViewModelProvider(this).get(ChatSearchKeywordViewModel.class);
    viewModel.setConversationInfo(accountId, conversationType);
    viewModel.getSearchMessagesLiveData().observeForever(this::onMessageLoad);
    viewModel.getMessageDeletedLiveData().observeForever(this::onDeleteOrRevokeMessage);
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
    if (defaultView != null) {
      defaultView.setVisibility(View.GONE);
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
      ErrorUtils.showErrorCodeToast(ChatSearchBaseActivity.this, result.getError().getCode());
    }
    // 延时200ms切换结果视图，避免切换过快导致界面刷新看到上次搜索结果
    new Handler(Looper.getMainLooper()).postDelayed(() -> switchResultView(), 200);
  }

  protected void onAttachmentUpdateProgress(FetchResult<IMMessageProgress> fetchResult) {
    messageAdapter.updateMessageProgress(fetchResult.getData());
  }

  private void onDeleteOrRevokeMessage(FetchResult<List<String>> result) {
    if (result.getLoadStatus() == LoadStatus.Success && result.getData() != null) {
      for (String clientId : result.getData()) {
        messageAdapter.removeMessageByClientId(clientId);
      }
      switchResultView();
    }
  }

  protected void switchResultView() {
    boolean hasSearchResult = messageAdapter.getMessageList().size() > 0;
    boolean hasEditText = !TextUtils.isEmpty(searchET.getText());
    if (hasSearchResult || hasEditText) {
      if (defaultView != null) {
        defaultView.setVisibility(View.GONE);
      }
      if (hasSearchResult) {
        searchRV.setVisibility(View.VISIBLE);
        emptyLayout.setVisibility(View.GONE);
      } else {
        emptyLayout.setVisibility(View.VISIBLE);
        searchRV.setVisibility(View.GONE);
      }
    } else {
      if (defaultView != null) {
        defaultView.setVisibility(View.VISIBLE);
      }
      emptyLayout.setVisibility(View.GONE);
      searchRV.setVisibility(View.GONE);
    }
  }
}
