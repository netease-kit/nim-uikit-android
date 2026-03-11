// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.normal.search;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.netease.nimlib.sdk.v2.conversation.enums.V2NIMConversationType;
import com.netease.nimlib.sdk.v2.message.V2NIMMessage;
import com.netease.nimlib.sdk.v2.message.attachment.V2NIMMessageImageAttachment;
import com.netease.nimlib.sdk.v2.message.attachment.V2NIMMessageVideoAttachment;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.model.IMMessageInfo;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.common.ChatUserCache;
import com.netease.yunxin.kit.chatkit.ui.common.ChatUtils;
import com.netease.yunxin.kit.chatkit.ui.common.MessageHelper;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatSearchImageListActivityBinding;
import com.netease.yunxin.kit.chatkit.ui.interfaces.IItemClickListener;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.chatkit.ui.normal.ChatMessageForwardConfirmDialog;
import com.netease.yunxin.kit.chatkit.ui.page.adapter.ChatSearchImageAdapter;
import com.netease.yunxin.kit.chatkit.ui.page.viewmodel.ChatSearchImageViewModel;
import com.netease.yunxin.kit.chatkit.ui.view.input.ActionConstants;
import com.netease.yunxin.kit.chatkit.utils.ConversationIdUtils;
import com.netease.yunxin.kit.chatkit.utils.ErrorUtils;
import com.netease.yunxin.kit.common.ui.action.ActionItem;
import com.netease.yunxin.kit.common.ui.activities.BaseLocalActivity;
import com.netease.yunxin.kit.common.ui.dialog.BaseBottomChoiceDialog;
import com.netease.yunxin.kit.common.ui.dialog.BottomChoiceDialog;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.corekit.im2.model.IMMessageProgress;
import com.netease.yunxin.kit.corekit.im2.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jetbrains.annotations.NotNull;

/** 按日期分组的图片列表页面 根据图片的时间信息，将相同日期的图片分组展示 */
public class ChatSearchImageActivity extends BaseLocalActivity {
  private static final String TAG = "ChatSearchImageListActivity";
  private ChatSearchImageListActivityBinding binding;
  private ChatSearchImageAdapter groupAdapter;
  private ChatSearchImageViewModel searchImageViewModel;
  private String conversationId;
  private String targetId;
  private V2NIMConversationType conversationType;
  private int searchMode = MODE_IMAGE;
  public static final String EXTRA_MODE = "search_mode";
  public static final int MODE_IMAGE = 1;
  public static final int MODE_VIDEO = 2;
  public static final int MODE_ALL = 3;
  private static final String DIALOG_TAG_FORWARD_CONFIRM = "file_forward_confirm";

  // 用于处理转发操作的ActivityResultLauncher
  private ActivityResultLauncher<Intent> forwardLauncher;
  private V2NIMMessage forwardMessage;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // 使用ViewBinding
    binding = ChatSearchImageListActivityBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());
    // 初始化UI
    initViews();
    // 初始化ViewModel
    initViewModel();
    // 初始化RecyclerView
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
    loadView();
  }

  /** 初始化ViewModel */
  private void initViewModel() {
    // 获取conversationId并开始搜索
    conversationId = getIntent().getStringExtra(RouterConstant.KEY_SESSION_ID);
    searchMode = getIntent().getIntExtra(EXTRA_MODE, MODE_IMAGE);
    if (TextUtils.isEmpty(conversationId)) {
      finish();
    }
    searchImageViewModel = new ChatSearchImageViewModel();
    conversationType = ConversationIdUtils.conversationType(conversationId);
    targetId = ConversationIdUtils.conversationTargetId(conversationId);
    searchImageViewModel.init(conversationId, searchMode);
    searchImageViewModel.getSearchImageLiveData().observeForever(this::loadFetchResult);
    // 附件下载监听，文件消息、视频消息等下载进度更新
    searchImageViewModel
        .getAttachmentProgressLiveData()
        .observeForever(this::onAttachmentUpdateProgress);
    searchImageViewModel
        .getMessageDeletedOrRevokedLiveData()
        .observeForever(this::onMessageDeletedOrRevoked);
    showLoading();
    searchImageViewModel.searchImageMessages();
  }

  public void showLoading() {
    binding.loadingView.setVisibility(View.VISIBLE);
    binding.swipeRefresh.setVisibility(View.GONE);
    binding.searchEmptyView.setVisibility(View.GONE);
  }

  public void hideLoading() {
    binding.loadingView.setVisibility(View.GONE);
    binding.swipeRefresh.setVisibility(View.VISIBLE);
  }

  /** 初始化UI控件 */
  private void initViews() {
    // 通过ViewBinding获取RecyclerView
    // 不需要单独初始化，直接在initRecyclerView中使用binding.rvImages
    binding.searchTitleBar.setTitle(R.string.message_search_image);
    binding.searchTitleBar.setOnBackIconClickListener(v -> finish());
    binding.swipeRefresh.setOnRefreshListener(
        () -> {
          if (searchImageViewModel.hasMore()) {
            searchImageViewModel.searchImageMessages();
          } else {
            binding.swipeRefresh.setRefreshing(false);
            Toast.makeText(
                    ChatSearchImageActivity.this, R.string.chat_no_more_images, Toast.LENGTH_SHORT)
                .show();
            boolean empty = groupAdapter.isGroupsEmpty();
            groupAdapter.setShowTopTips(!empty);
          }
        });
  }

  private void loadView() {
    if (searchMode == MODE_VIDEO) {
      binding.searchTitleBar.setTitle(R.string.message_search_video);
      binding.searchEmptyTips.setText(R.string.chat_search_video_empty_tips);
    } else {
      binding.searchTitleBar.setTitle(R.string.message_search_image);
      binding.searchEmptyTips.setText(R.string.chat_search_image_empty_tips);
    }
  }

  private void loadFetchResult(FetchResult<List<V2NIMMessage>> result) {
    hideLoading();
    binding.swipeRefresh.setRefreshing(false);
    if (result.getData() != null && result.getLoadStatus() == LoadStatus.Success) {
      List<V2NIMMessage> messages = result.getData();
      int prevCount = groupAdapter.getItemCount();
      boolean scrollEnd = prevCount < 1;
      LinearLayoutManager lm = (LinearLayoutManager) binding.rvImages.getLayoutManager();
      int firstVisible = lm != null ? lm.findFirstVisibleItemPosition() : 0;
      View firstChild =
          (lm != null && firstVisible != RecyclerView.NO_POSITION)
              ? lm.findViewByPosition(firstVisible)
              : null;
      int offsetTop = firstChild != null ? firstChild.getTop() : 0;
      groupAdapter.addData(messages);
      boolean hasMoreNext = searchImageViewModel.hasMore();
      // 先根据 hasMore 切换顶部提示，确保统计数量稳定
      if (hasMoreNext) {
        groupAdapter.setShowTopTips(false);
      } else {
        boolean emptyGroups = groupAdapter.isGroupsEmpty();
        groupAdapter.setShowTopTips(!emptyGroups);
      }
      binding.swipeRefresh.setEnabled(hasMoreNext);
      if (scrollEnd) {
        scrollToEnd();
      } else {
        int newCount = groupAdapter.getItemCount();
        int inserted = Math.max(0, newCount - prevCount);
        if (inserted > 0 && lm != null && firstVisible >= 0) {
          lm.scrollToPositionWithOffset(firstVisible + inserted, offsetTop);
        }
      }
      updateEmptyView();
    } else {
      if (result.getLoadStatus() == LoadStatus.Error && result.getError() != null) {
        ErrorUtils.showErrorCodeToast(ChatSearchImageActivity.this, result.getError().getCode());
      }
      boolean hasMoreNext = searchImageViewModel.hasMore();
      binding.swipeRefresh.setEnabled(hasMoreNext);
      boolean empty = groupAdapter.isGroupsEmpty();
      binding.searchEmptyView.setVisibility(
          empty ? android.view.View.VISIBLE : android.view.View.GONE);
      if (hasMoreNext) {
        groupAdapter.setShowTopTips(false);
      } else {
        groupAdapter.setShowTopTips(!empty);
      }
    }
  }

  /**
   * 附件下载进度更新监听
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
   * 消息删除或撤回监听
   *
   * @param fetchResult
   */
  private void onMessageDeletedOrRevoked(FetchResult<List<String>> fetchResult) {
    if (fetchResult.getData() == null || fetchResult.getLoadStatus() != LoadStatus.Success) {
      return;
    }
    ALog.i(TAG, "onMessageDeletedOrRevoked, messageIds: ");
    groupAdapter.removeData(fetchResult.getData());
    updateEmptyView();
  }

  private void updateEmptyView() {
    boolean empty = groupAdapter.isGroupsEmpty();
    binding.searchEmptyView.setVisibility(empty ? View.VISIBLE : View.GONE);
  }

  /** 初始化RecyclerView */
  private void initRecyclerView() {
    // 设置线性布局管理器
    LinearLayoutManager layoutManager = new LinearLayoutManager(this);
    binding.rvImages.setLayoutManager(layoutManager);

    // 创建并设置分组适配器
    groupAdapter = new ChatSearchImageAdapter(this);

    // 设置点击监听器，在activity中处理点击事件
    groupAdapter.setItemClickListener(
        new IItemClickListener<V2NIMMessage>() {
          @Override
          public boolean onMessageClick(
              android.view.View view, int position, V2NIMMessage message) {
            handleImageClick(message, position);
            return true;
          }

          @Override
          public boolean onCustomViewClick(
              android.view.View view, int position, V2NIMMessage message) {
            return false;
          }

          @Override
          public boolean onMessageLongClick(View view, int position, V2NIMMessage messageInfo) {
            ArrayList<ActionItem> actions = new ArrayList<>();
            actions.add(
                new ActionItem(
                        ActionConstants.POP_ACTION_MESSAGE_LOCATION,
                        0,
                        R.string.chat_message_action_location)
                    .setTitleColorResId(R.color.color_333333));
            actions.add(
                new ActionItem(
                        ActionConstants.POP_ACTION_TRANSMIT,
                        0,
                        R.string.chat_message_action_transmit)
                    .setTitleColorResId(R.color.color_333333));
            BottomChoiceDialog dialog =
                new BottomChoiceDialog(ChatSearchImageActivity.this, actions);
            dialog.setOnChoiceListener(
                new BaseBottomChoiceDialog.OnChoiceListener() {
                  @Override
                  public void onCancel() {
                    dialog.dismiss();
                  }

                  @Override
                  public void onChoice(@NotNull String action) {
                    if (ActionConstants.POP_ACTION_MESSAGE_LOCATION.equals(action)) {
                      String routerPath = RouterConstant.PATH_CHAT_TEAM_PAGE;
                      if (conversationType == V2NIMConversationType.V2NIM_CONVERSATION_TYPE_P2P) {
                        routerPath = RouterConstant.PATH_CHAT_P2P_PAGE;
                      }
                      XKitRouter.withKey(routerPath)
                          .withParam(RouterConstant.KEY_MESSAGE, messageInfo)
                          .withParam(RouterConstant.CHAT_ID_KRY, targetId)
                          .withContext(ChatSearchImageActivity.this)
                          .navigate();
                    } else if (ActionConstants.POP_ACTION_TRANSMIT.equals(action)) {
                      // 处理收藏操作
                      forwardMessage = messageInfo;
                      ChatUtils.startForwardSelector(
                          ChatSearchImageActivity.this,
                          RouterConstant.PATH_FORWARD_SELECTOR_PAGE,
                          false,
                          forwardLauncher);
                    }
                    dialog.dismiss();
                  }
                });
            dialog.show();
            return true;
          }
        });

    binding.rvImages.setAdapter(groupAdapter);

    // 自动滚动到列表底部，显示最近日期的图片
    if (groupAdapter.getItemCount() > 0) {
      binding.rvImages.post(
          new Runnable() {
            @Override
            public void run() {
              binding.rvImages.scrollToPosition(groupAdapter.getItemCount() - 1);
            }
          });
    }

    // 顶部下拉加载，不使用底部触发逻辑
  }

  public void scrollToEnd() {
    if (groupAdapter != null) {
      int itemCount = groupAdapter.getItemCount();
      if (itemCount > 0) {
        binding.rvImages.post(() -> binding.rvImages.scrollToPosition(itemCount - 1));
      }
    }
  }

  /**
   * 处理图片点击事件
   *
   * @param message 被点击的图片消息
   * @param position 图片在列表中的位置
   */
  private void handleImageClick(V2NIMMessage message, int position) {
    if (message.getAttachment() instanceof V2NIMMessageImageAttachment) {
      ArrayList<V2NIMMessage> imgs = groupAdapter.getAllImageMessages();
      ChatUtils.watchImage(this, message, imgs);
    } else if (message.getAttachment() instanceof V2NIMMessageVideoAttachment) {
      ChatUtils.watchVideo(this, message);
    }
  }

  private void showForwardConfirmDialog(ArrayList<String> conversationIds) {
    if (forwardMessage == null) {
      return;
    }
    String sendName = ChatUserCache.getInstance().getConversationInfo(conversationId);
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
}
