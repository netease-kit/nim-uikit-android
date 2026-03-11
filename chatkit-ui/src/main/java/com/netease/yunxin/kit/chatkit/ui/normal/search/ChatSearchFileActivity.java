// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.normal.search;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.netease.nimlib.sdk.v2.message.V2NIMMessage;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.model.IMMessageInfo;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.common.ChatUserCache;
import com.netease.yunxin.kit.chatkit.ui.common.ChatUtils;
import com.netease.yunxin.kit.chatkit.ui.common.MessageHelper;
import com.netease.yunxin.kit.chatkit.ui.common.MessageSearchUtils;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatSearchFileListActivityBinding;
import com.netease.yunxin.kit.chatkit.ui.interfaces.IItemClickListener;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.chatkit.ui.model.MessageGroup;
import com.netease.yunxin.kit.chatkit.ui.normal.ChatMessageForwardConfirmDialog;
import com.netease.yunxin.kit.chatkit.ui.page.viewmodel.ChatSearchFileViewModel;
import com.netease.yunxin.kit.chatkit.ui.view.input.ActionConstants;
import com.netease.yunxin.kit.chatkit.utils.ErrorUtils;
import com.netease.yunxin.kit.common.ui.action.ActionItem;
import com.netease.yunxin.kit.common.ui.activities.BaseLocalActivity;
import com.netease.yunxin.kit.common.ui.dialog.BottomChoiceDialog;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.corekit.im2.model.IMMessageProgress;
import com.netease.yunxin.kit.corekit.im2.utils.RouterConstant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChatSearchFileActivity extends BaseLocalActivity {

  private static final String TAG = "ChatSearchFileActivity";
  private ChatSearchFileListActivityBinding binding;
  private ChatSearchFileAdapter groupAdapter;
  private ChatSearchFileViewModel searchFileViewModel;
  private String conversationId;
  private List<MessageGroup> fileGroups = new ArrayList<>();
  private ActivityResultLauncher<Intent> forwardLauncher;
  private V2NIMMessage forwardMessage;
  private static final String DIALOG_TAG_FORWARD_CONFIRM = "file_forward_confirm";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = ChatSearchFileListActivityBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());
    initViews();
    initViewModel();
    initRecyclerView();
    forwardLauncher =
        registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
              if (result.getResultCode() != android.app.Activity.RESULT_OK
                  || forwardMessage == null) {
                return;
              }
              Intent data = result.getData();
              if (data != null) {
                ArrayList<String> conversationIds =
                    data.getStringArrayListExtra(RouterConstant.KEY_FORWARD_SELECTED_CONVERSATIONS);
                if (conversationIds != null && !conversationIds.isEmpty()) {
                  showForwardConfirmDialog(conversationIds);
                }
              }
            });
  }

  private void initViewModel() {
    conversationId = getIntent().getStringExtra(RouterConstant.KEY_SESSION_ID);
    if (TextUtils.isEmpty(conversationId)) {
      ALog.e(TAG, "conversationId is empty");
      finish();
      return;
    }
    searchFileViewModel = new ChatSearchFileViewModel();
    searchFileViewModel.init(conversationId);
    searchFileViewModel.getSearchFileLiveData().observeForever(this::loadFetchResult);
    // 附件下载监听，文件消息、视频消息等下载进度更新
    searchFileViewModel
        .getAttachmentProgressLiveData()
        .observeForever(this::onAttachmentUpdateProgress);
    searchFileViewModel
        .getMessageDeletedOrRevokedLiveData()
        .observeForever(this::onMessageDeletedOrRevoked);
    showLoading();
    searchFileViewModel.searchFileMessages();
  }

  public void showLoading() {
    binding.loadingView.setVisibility(View.VISIBLE);
    binding.rvFiles.setVisibility(View.GONE);
    binding.searchEmptyView.setVisibility(View.GONE);
  }

  private void hideLoading() {
    binding.loadingView.setVisibility(View.GONE);
    binding.rvFiles.setVisibility(View.VISIBLE);
  }

  /**
   * 附件下载监听，文件消息、视频消息等下载进度更新
   *
   * @param fetchResult
   */
  private void onAttachmentUpdateProgress(FetchResult<IMMessageProgress> fetchResult) {
    if (fetchResult.getData() == null || fetchResult.getLoadStatus() != LoadStatus.Success) {
      return;
    }
    IMMessageProgress progress = fetchResult.getData();
    ALog.i(TAG, "onAttachmentUpdateProgress, progress: " + progress);
    if (groupAdapter != null) {
      groupAdapter.updateProgress(progress);
    }
  }

  /**
   * 搜索文件消息结果回调
   *
   * @param result
   */
  protected void loadFetchResult(FetchResult<List<V2NIMMessage>> result) {
    hideLoading();
    if (result.getData() != null && result.getLoadStatus() == LoadStatus.Success) {
      List<V2NIMMessage> messages = result.getData();
      MessageSearchUtils.groupMessageByMonth(ChatSearchFileActivity.this, fileGroups, messages);
      groupAdapter.notifyDataSetChanged();
      boolean empty = isGroupsEmpty();
      groupAdapter.setShowFooter(!empty && !searchFileViewModel.hasMore());
    } else {
      if (result.getLoadStatus() == LoadStatus.Error && result.getError() != null) {
        ErrorUtils.showErrorCodeToast(ChatSearchFileActivity.this, result.getError().getCode());
      }
      groupAdapter.setShowFooter(false);
    }
    groupAdapter.setFooterLoading(false);
    updateEmptyView();
  }

  /**
   * 消息删除或撤回监听
   *
   * @param fetchResult
   */
  private void onMessageDeletedOrRevoked(FetchResult<List<String>> fetchResult) {
    if (fetchResult.getData() == null || fetchResult.getLoadStatus() != LoadStatus.Success) {
      return;
    }
    List<String> messageIds = fetchResult.getData();
    ALog.i(TAG, "onMessageDeletedOrRevoked, messageIds: " + messageIds);
    if (!fileGroups.isEmpty()) {
      // 遍历文件消息分组，删除包含在删除或撤回消息列表中的消息
      for (MessageGroup group : fileGroups) {
        if (group.getMessageList() != null && !group.getMessageList().isEmpty()) {
          List<V2NIMMessage> toRemove = new ArrayList<>();
          for (V2NIMMessage message : group.getMessageList()) {
            if (messageIds.contains(message.getMessageClientId())) {
              toRemove.add(message);
            }
          }
          group.getMessageList().removeAll(toRemove);
        }
      }
      // 当MessageGroup中没有消息时，直接删除该分组
      List<MessageGroup> emptyGroups = new ArrayList<>();
      for (MessageGroup group : fileGroups) {
        if (group.getMessageList() == null || group.getMessageList().isEmpty()) {
          emptyGroups.add(group);
        }
      }
      fileGroups.removeAll(emptyGroups);
      groupAdapter.notifyDataSetChanged();
      updateEmptyView();
    }
  }

  private boolean isGroupsEmpty() {
    if (fileGroups == null || fileGroups.isEmpty()) {
      return true;
    }
    for (MessageGroup g : fileGroups) {
      if (g.getMessageList() != null && !g.getMessageList().isEmpty()) {
        return false;
      }
    }
    return true;
  }

  private void initViews() {
    binding.searchTitleBar.setOnBackIconClickListener(v -> finish());
  }

  private void initRecyclerView() {
    LinearLayoutManager layoutManager = new LinearLayoutManager(this);
    binding.rvFiles.setLayoutManager(layoutManager);
    groupAdapter = new ChatSearchFileAdapter(this, fileGroups);
    groupAdapter.setItemClickListener(
        new IItemClickListener<V2NIMMessage>() {
          @Override
          public boolean onMessageClick(View view, int position, V2NIMMessage message) {
            ChatUtils.openFile(ChatSearchFileActivity.this, message);
            return true;
          }

          @Override
          public boolean onCustomViewClick(View view, int position, V2NIMMessage message) {
            showBottomActionPop(message);
            return true;
          }
        });
    binding.rvFiles.setAdapter(groupAdapter);

    binding.rvFiles.addOnScrollListener(
        new RecyclerView.OnScrollListener() {
          @Override
          public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            int lastVisible = layoutManager.findLastVisibleItemPosition();
            int total = groupAdapter.getItemCount();
            if (total > 0
                && lastVisible >= total - 1
                && conversationId != null
                && searchFileViewModel.hasMore()) {
              recyclerView.post(
                  () -> {
                    groupAdapter.setShowFooter(true);
                    groupAdapter.setFooterLoading(true);
                    searchFileViewModel.searchFileMessages();
                  });
            } else if (total > 0 && lastVisible >= total - 1 && !searchFileViewModel.hasMore()) {
              boolean empty = isGroupsEmpty();
              recyclerView.post(
                  () -> {
                    groupAdapter.setShowFooter(!empty);
                    groupAdapter.setFooterLoading(false);
                  });
            }
          }
        });
  }

  private void updateEmptyView() {
    boolean empty = isGroupsEmpty();
    binding.searchEmptyView.setVisibility(empty ? View.VISIBLE : View.GONE);
  }

  public void showBottomActionPop(V2NIMMessage message) {
    ArrayList<ActionItem> actions = new ArrayList<>();
    actions.add(
        new ActionItem(
                ActionConstants.POP_ACTION_TRANSMIT, 0, R.string.chat_message_action_transmit)
            .setTitleColorResId(R.color.color_333333));
    actions.add(
        new ActionItem(
                ActionConstants.POP_ACTION_COLLECTION, 0, R.string.chat_message_action_collection)
            .setTitleColorResId(R.color.color_333333));
    BottomChoiceDialog dialog = new BottomChoiceDialog(ChatSearchFileActivity.this, actions);
    dialog.setOnChoiceListener(
        new BottomChoiceDialog.OnChoiceListener() {
          @Override
          public void onChoice(@NonNull String type) {
            if (ActionConstants.POP_ACTION_TRANSMIT.equals(type)) {
              forwardMessage = message;
              ChatUtils.startForwardSelector(
                  ChatSearchFileActivity.this,
                  RouterConstant.PATH_FORWARD_SELECTOR_PAGE,
                  false,
                  forwardLauncher);
            } else if (ActionConstants.POP_ACTION_COLLECTION.equals(type)) {
              searchFileViewModel.addMsgCollection(message);
            }
          }

          @Override
          public void onCancel() {}
        });
    dialog.show();
  }

  private void showForwardConfirmDialog(ArrayList<String> conversationIds) {
    if (forwardMessage == null) {
      return;
    }
    String sendName =
        ChatUserCache.getInstance().getConversationInfo(forwardMessage.getConversationId());
    ChatMessageForwardConfirmDialog confirmDialog =
        ChatMessageForwardConfirmDialog.createForwardConfirmDialog(
            conversationIds, sendName, true, ActionConstants.POP_ACTION_TRANSMIT);
    confirmDialog.setCallback(
        input -> {
          for (String conversationId : conversationIds) {
            MessageHelper.sendForwardMessage(
                new ChatMessageBean(new IMMessageInfo(forwardMessage)),
                input,
                Collections.singletonList(conversationId),
                true,
                true);
          }
        });
    confirmDialog.show(getSupportFragmentManager(), DIALOG_TAG_FORWARD_CONFIRM);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
  }
}
