// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.normal.page;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.netease.nimlib.sdk.v2.conversation.enums.V2NIMConversationType;
import com.netease.nimlib.sdk.v2.topic.V2NIMTopic;
import com.netease.nimlib.sdk.v2.utils.V2NIMConversationIdUtil;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.common.BotSubSessionMoreActionHelper;
import com.netease.yunxin.kit.chatkit.ui.common.BotSubSessionUtils;
import com.netease.yunxin.kit.chatkit.ui.common.ChatUserCache;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatBotSubSessionActionDialogBinding;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatBotSubSessionActivityBinding;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatBotSubSessionRenameDialogBinding;
import com.netease.yunxin.kit.chatkit.ui.page.adapter.BotSubSessionListAdapter;
import com.netease.yunxin.kit.chatkit.ui.page.viewmodel.ChatBotSubSessionListViewModel;
import com.netease.yunxin.kit.common.ui.activities.BaseLocalActivity;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.common.utils.NetworkUtils;
import com.netease.yunxin.kit.corekit.im2.extend.FetchCallback;
import com.netease.yunxin.kit.corekit.im2.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;

public class ChatBotSubSessionActivity extends BaseLocalActivity {

  private static final long CREATE_TOPIC_DEBOUNCE_MS = 500L;
  private static final int TOPIC_NAME_MAX_LENGTH = 20;

  protected ChatBotSubSessionActivityBinding binding;
  private ChatBotSubSessionListViewModel viewModel;
  protected BotSubSessionListAdapter adapter;
  private String accountId;
  private String conversationId;
  private long lastCreateTopicClickTime;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    changeStatusBarColor(R.color.color_white);
    binding = ChatBotSubSessionActivityBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());
    accountId = getIntent().getStringExtra(RouterConstant.CHAT_ID_KRY);
    conversationId = getIntent().getStringExtra(RouterConstant.KEY_BOT_SUB_SESSION_CONVERSATION_ID);
    if (TextUtils.isEmpty(accountId)) {
      finish();
      return;
    }
    if (TextUtils.isEmpty(conversationId)) {
      conversationId = V2NIMConversationIdUtil.p2pConversationId(accountId);
    }
    initView();
    initViewModel();
  }

  private void initView() {
    binding.titleBarView.setTitle(R.string.chat_bot_sub_session_title);
    binding.titleBarView.setOnBackIconClickListener(v -> onBackPressed());
    binding.createTopicButton.setOnClickListener(v -> createTopicChat());
    binding.settingButton.setOnClickListener(v -> openSettingPage());

    adapter = new BotSubSessionListAdapter();
    adapter.setOnTopicActionListener(
        new BotSubSessionListAdapter.OnTopicActionListener() {
          @Override
          public void onTopicClick(V2NIMTopic topic) {
            openTopicChat(topic);
          }

          @Override
          public void onTopicLongClick(V2NIMTopic topic) {
            showTopicActionDialog(topic);
          }
        });
    binding.topicRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    binding.topicRecyclerView.setAdapter(adapter);
    binding.topicRecyclerView.addOnScrollListener(
        new RecyclerView.OnScrollListener() {
          @Override
          public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            LinearLayoutManager manager = (LinearLayoutManager) recyclerView.getLayoutManager();
            if (manager != null
                && dy > 0
                && viewModel != null
                && viewModel.hasMore()
                && manager.findLastVisibleItemPosition() >= adapter.getItemCount() - 3) {
              viewModel.loadMoreTopics();
            }
          }
        });
    binding.searchContainer.setOnClickListener(v -> showKeyboard());
    binding.emptyView.setOnClickListener(
        v -> {
          if (viewModel != null) {
            viewModel.loadTopics();
          }
        });
    binding.emptyActionButton.setOnClickListener(v -> createTopicChat());
    binding.searchClearButton.setOnClickListener(
        v -> {
          binding.searchEditText.setText("");
          showKeyboard();
        });
    binding.searchEditText.setOnEditorActionListener(
        (v, actionId, event) -> {
          if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            if (viewModel != null) {
              viewModel.search(binding.searchEditText.getText().toString());
            }
            hideKeyboard();
            return true;
          }
          return false;
        });
    binding.searchEditText.addTextChangedListener(
        new TextWatcher() {
          @Override
          public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

          @Override
          public void onTextChanged(CharSequence s, int start, int before, int count) {
            binding.searchClearButton.setVisibility(
                TextUtils.isEmpty(s) ? View.GONE : View.VISIBLE);
            if (viewModel != null) {
              viewModel.search(s == null ? null : s.toString());
            }
          }

          @Override
          public void afterTextChanged(Editable s) {}
        });
    onListViewReady();
  }

  protected void onListViewReady() {}

  protected int getDefaultEmptyImageRes() {
    return R.drawable.ic_chat_empty;
  }

  protected int getSearchEmptyImageRes() {
    return R.drawable.ic_list_empty;
  }

  private void initViewModel() {
    viewModel = new ViewModelProvider(this).get(ChatBotSubSessionListViewModel.class);
    viewModel.init(conversationId);
    viewModel
        .getTopicListLiveData()
        .observe(
            this,
            result -> {
              if (result == null) {
                return;
              }
              if (result.getLoadStatus() == LoadStatus.Loading) {
                return;
              }
              if (result.getLoadStatus() == LoadStatus.Error
                  && result.getType() == FetchResult.FetchType.Add) {
                ToastX.showShortToast(R.string.chat_bot_sub_session_load_more_failed);
                return;
              }
              boolean empty = result.getData() == null || result.getData().isEmpty();
              boolean searchEmpty =
                  empty
                      && result.getLoadStatus() == LoadStatus.Success
                      && !TextUtils.isEmpty(binding.searchEditText.getText());
              if (searchEmpty) {
                binding.emptyImage.setImageResource(getSearchEmptyImageRes());
                binding.emptyText.setText(R.string.chat_bot_sub_session_search_empty);
                binding.emptyActionButton.setVisibility(View.GONE);
              } else if (result.getLoadStatus() == LoadStatus.Error) {
                binding.emptyImage.setImageResource(getDefaultEmptyImageRes());
                binding.emptyText.setText(R.string.chat_bot_sub_session_load_failed);
                binding.emptyActionButton.setText(R.string.chat_bot_sub_session_retry);
                binding.emptyActionButton.setVisibility(View.VISIBLE);
                binding.emptyActionButton.setOnClickListener(
                    v -> {
                      if (viewModel != null) {
                        viewModel.loadTopics();
                      }
                    });
              } else {
                binding.emptyImage.setImageResource(getDefaultEmptyImageRes());
                binding.emptyText.setText(R.string.chat_bot_sub_session_empty);
                binding.emptyActionButton.setText(
                    R.string.chat_bot_sub_session_create_conversation);
                binding.emptyActionButton.setVisibility(View.VISIBLE);
                binding.emptyActionButton.setOnClickListener(v -> createTopicChat());
              }
              adapter.submitList(result.getData());
              binding.emptyView.setVisibility(empty ? View.VISIBLE : View.GONE);
            });
    viewModel.loadTopics();
    viewModel.refreshUnreadState();
  }

  @Override
  protected void onResume() {
    super.onResume();
    if (viewModel != null) {
      viewModel.refreshUnreadState();
      viewModel.refreshSummaries();
    }
  }

  private void createTopicChat() {
    long now = SystemClock.elapsedRealtime();
    if (now - lastCreateTopicClickTime < CREATE_TOPIC_DEBOUNCE_MS) {
      return;
    }
    lastCreateTopicClickTime = now;
    if (!NetworkUtils.isConnected()) {
      ToastX.showShortToast(R.string.chat_bot_sub_session_offline_create);
      return;
    }
    openTopicChat(null);
  }

  private void openTopicChat(@Nullable V2NIMTopic topic) {
    XKitRouter.withKey(getTopicChatRoute())
        .withParam(RouterConstant.CHAT_ID_KRY, accountId)
        .withParam(
            RouterConstant.KEY_SESSION_NAME,
            ChatUserCache.getInstance()
                .getUserNick(accountId, V2NIMConversationType.V2NIM_CONVERSATION_TYPE_P2P))
        .withParam(RouterConstant.KEY_BOT_SUB_SESSION_CONVERSATION_ID, conversationId)
        .withParam(RouterConstant.KEY_BOT_SUB_SESSION_TOPIC, topic)
        .withContext(this)
        .navigate();
  }

  protected String getTopicChatRoute() {
    return RouterConstant.PATH_CHAT_BOT_SUB_SESSION_CHAT_PAGE;
  }

  protected String getSettingRoute() {
    return RouterConstant.PATH_CHAT_SETTING_PAGE;
  }

  private void openSettingPage() {
    XKitRouter.withKey(getSettingRoute())
        .withParam(RouterConstant.CHAT_ID_KRY, accountId)
        .withParam(RouterConstant.KEY_FROM_BOT_SUB_SESSION, true)
        .withContext(this)
        .navigate();
  }

  private void showTopicActionDialog(V2NIMTopic topic) {
    ChatBotSubSessionActionDialogBinding dialogBinding =
        ChatBotSubSessionActionDialogBinding.inflate(LayoutInflater.from(this));
    Dialog dialog = new Dialog(this);
    dialog.setContentView(dialogBinding.getRoot());
    if (dialog.getWindow() != null) {
      dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
      dialog.getWindow().setGravity(Gravity.BOTTOM);
      dialog
          .getWindow()
          .setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }
    dialog.setCanceledOnTouchOutside(true);
    dialogBinding.topicTitle.setText(BotSubSessionUtils.getTopicTitle(this, topic));
    dialogBinding.renameButton.setOnClickListener(
        v -> {
          dialog.dismiss();
          showRenameDialog(topic);
        });
    dialogBinding.deleteButton.setOnClickListener(
        v -> {
          dialog.dismiss();
          showDeleteDialog(topic);
        });
    dialogBinding.cancelButton.setOnClickListener(v -> dialog.dismiss());
    dialog.show();
  }

  private void showDeleteDialog(V2NIMTopic topic) {
    BotSubSessionMoreActionHelper.showDeleteConfirmDialog(
        this,
        BotSubSessionUtils.getTopicTitle(this, topic),
        () ->
            viewModel.deleteTopic(
                topic,
                new FetchCallback<Void>() {
                  @Override
                  public void onError(int errorCode, @Nullable String errorMsg) {
                    ToastX.showShortToast(R.string.chat_bot_sub_session_delete_failed);
                  }

                  @Override
                  public void onSuccess(@Nullable Void data) {}
                }));
  }

  private void showRenameDialog(V2NIMTopic topic) {
    ChatBotSubSessionRenameDialogBinding dialogBinding =
        ChatBotSubSessionRenameDialogBinding.inflate(LayoutInflater.from(this));
    EditText input = dialogBinding.nameInput;
    input.setSingleLine(true);
    input.setImeOptions(EditorInfo.IME_ACTION_DONE);
    input.setFilters(new InputFilter[] {new InputFilter.LengthFilter(TOPIC_NAME_MAX_LENGTH)});
    input.setHint(R.string.chat_bot_sub_session_input_name);
    input.setText(BotSubSessionUtils.getTopicTitle(this, topic));
    input.setSelectAllOnFocus(true);
    dialogBinding.saveButton.setBackgroundResource(getRenameConfirmButtonBackgroundRes());
    AlertDialog dialog = new AlertDialog.Builder(this).setView(dialogBinding.getRoot()).create();
    dialogBinding.cancelButton.setOnClickListener(v -> dialog.dismiss());
    View.OnClickListener saveListener =
        v -> {
          String newName = input.getText() == null ? "" : input.getText().toString().trim();
          if (TextUtils.isEmpty(newName)) {
            ToastX.showShortToast(R.string.chat_bot_sub_session_input_name);
            return;
          }
          if (!NetworkUtils.isConnected()) {
            ToastX.showShortToast(R.string.chat_network_error_tip);
            return;
          }
          viewModel.renameTopic(
              topic,
              newName,
              new FetchCallback<V2NIMTopic>() {
                @Override
                public void onError(int errorCode, @Nullable String errorMsg) {
                  ToastX.showShortToast(R.string.chat_bot_sub_session_rename_failed);
                }

                @Override
                public void onSuccess(@Nullable V2NIMTopic data) {
                  dialog.dismiss();
                }
              });
        };
    dialogBinding.saveButton.setOnClickListener(saveListener);
    input.setOnEditorActionListener(
        (v, actionId, event) -> {
          if (actionId == EditorInfo.IME_ACTION_DONE) {
            saveListener.onClick(dialogBinding.saveButton);
            return true;
          }
          return false;
        });
    dialog.setOnShowListener(
        shownDialog -> {
          if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog
                .getWindow()
                .setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.7f),
                    ViewGroup.LayoutParams.WRAP_CONTENT);
          }
          input.requestFocus();
          input.post(
              () -> {
                InputMethodManager imm =
                    (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                  imm.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT);
                }
              });
        });
    dialog.show();
  }

  public int getRenameConfirmButtonBackgroundRes() {
    return R.drawable.chat_bot_sub_session_rename_confirm_bg;
  }

  private void showKeyboard() {
    binding.searchEditText.requestFocus();
    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
    if (imm != null) {
      imm.showSoftInput(binding.searchEditText, InputMethodManager.SHOW_IMPLICIT);
    }
  }

  private void hideKeyboard() {
    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
    if (imm != null) {
      imm.hideSoftInputFromWindow(binding.searchEditText.getWindowToken(), 0);
    }
  }
}
