// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.normal.page.fragment;

import static com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant.LIB_TAG;
import static com.netease.yunxin.kit.corekit.im2.utils.RouterConstant.KEY_TEAM_ID;
import static com.netease.yunxin.kit.corekit.im2.utils.RouterConstant.PATH_TEAM_SETTING_PAGE;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import com.netease.nimlib.sdk.v2.conversation.enums.V2NIMConversationType;
import com.netease.nimlib.sdk.v2.message.V2NIMMessage;
import com.netease.nimlib.sdk.v2.message.V2NIMTeamMessageReadReceipt;
import com.netease.nimlib.sdk.v2.team.enums.V2NIMTeamChatBannedMode;
import com.netease.nimlib.sdk.v2.team.enums.V2NIMTeamMemberRole;
import com.netease.nimlib.sdk.v2.team.model.V2NIMTeam;
import com.netease.nimlib.sdk.v2.team.model.V2NIMTeamMember;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.model.IMMessageInfo;
import com.netease.yunxin.kit.chatkit.model.TeamMemberWithUserInfo;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.common.MessageHelper;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.chatkit.ui.normal.view.MessageBottomLayout;
import com.netease.yunxin.kit.chatkit.ui.page.viewmodel.ChatTeamViewModel;
import com.netease.yunxin.kit.chatkit.ui.view.ait.AitManager;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.corekit.im2.IMKitClient;
import com.netease.yunxin.kit.corekit.im2.IMKitConstant;
import com.netease.yunxin.kit.corekit.im2.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import java.util.ArrayList;
import java.util.List;

/** 标准皮肤，群聊会话页面Fragment。 */
public class ChatTeamFragment extends NormalChatFragment {
  private static final String TAG = "ChatTeamFragment";
  // 群信息
  V2NIMTeam teamInfo;
  // 当前用户在群中的群成员信息
  V2NIMTeamMember currentMember;
  // 群聊定位消息
  IMMessageInfo anchorMessage;
  // 是否展示删除对话框
  private boolean showDeleteDialog = false;
  // 群聊消息已读回执观察者
  Observer<FetchResult<List<V2NIMTeamMessageReadReceipt>>> teamReceiptObserver;

  @Override
  protected void initData(Bundle bundle) {
    ALog.d(LIB_TAG, TAG, "initData");
    conversationType = V2NIMConversationType.V2NIM_CONVERSATION_TYPE_TEAM;
    teamInfo = (V2NIMTeam) bundle.getSerializable(RouterConstant.CHAT_KRY);
    accountId = (String) bundle.getSerializable(RouterConstant.CHAT_ID_KRY);
    // 如果群信息为空，且accountId为空，则直接关闭页面
    if (teamInfo == null && TextUtils.isEmpty(accountId)) {
      requireActivity().finish();
      return;
    }
    if (TextUtils.isEmpty(accountId)) {
      accountId = teamInfo.getTeamId();
    }
    // 群聊定位消息
    anchorMessage = (IMMessageInfo) bundle.getSerializable(RouterConstant.KEY_MESSAGE_INFO);
    if (anchorMessage == null) {
      V2NIMMessage message = (V2NIMMessage) bundle.getSerializable(RouterConstant.KEY_MESSAGE);
      if (message != null) {
        anchorMessage = new IMMessageInfo(message);
      }
    }
    // 初始化AitManager
    aitManager = new AitManager(getContext(), accountId);
    aitManager.updateTeamInfo(teamInfo);
    chatView.setAitManager(aitManager);
    refreshView();
  }

  @Override
  protected void initView() {
    super.initView();
    // 设置标题栏
    chatView
        .getTitleBar()
        .setOnBackIconClickListener(v -> requireActivity().onBackPressed())
        .setActionImg(R.drawable.ic_more_point)
        .setActionListener(
            v -> {
              // go to team setting
              chatView.hideCurrentInput();
              XKitRouter.withKey(PATH_TEAM_SETTING_PAGE)
                  .withContext(requireContext())
                  .withParam(KEY_TEAM_ID, accountId)
                  .navigate();
            });
  }

  // 刷新页面
  private void refreshView() {
    if (teamInfo != null) {
      chatView.getTitleBar().setTitle(teamInfo.getName());
      chatView.updateInputHintInfo(teamInfo.getName());
      chatView.getMessageListView().updateTeamInfo(teamInfo);
    }
    if (currentMember != null && teamInfo != null) {
      if (currentMember.getMemberRole() != V2NIMTeamMemberRole.V2NIM_TEAM_MEMBER_ROLE_OWNER
          && currentMember.getMemberRole() != V2NIMTeamMemberRole.V2NIM_TEAM_MEMBER_ROLE_MANAGER) {
        chatView.setInputMute(
            teamInfo.getChatBannedMode()
                != V2NIMTeamChatBannedMode.V2NIM_TEAM_CHAT_BANNED_MODE_UNBAN);
      } else {
        chatView.setInputMute(false);
      }
    }
  }

  // 初始化ViewModel
  @Override
  protected void initViewModel() {
    ALog.d(LIB_TAG, TAG, "initViewModel");
    viewModel = new ViewModelProvider(this).get(ChatTeamViewModel.class);
    viewModel.init(accountId, V2NIMConversationType.V2NIM_CONVERSATION_TYPE_TEAM);
  }

  @Override
  protected void initData() {
    if (viewModel instanceof ChatTeamViewModel) {
      // 请求群信息
      ((ChatTeamViewModel) viewModel).requestTeamInfo();
      // 请求群聊消息
      if (anchorMessage != null) {
        viewModel.getMessageList(anchorMessage.getMessage(), false);
      } else {
        viewModel.getMessageList(null, false);
      }
    }
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    if (chatConfig != null && chatConfig.chatListener != null) {
      chatConfig.chatListener.onConversationChange(accountId, conversationType);
    }
    if (chatConfig != null && chatConfig.messageProperties != null) {
      viewModel.setShowReadStatus(chatConfig.messageProperties.showTeamMessageStatus);
    }
  }

  @Override
  public void onStart() {
    super.onStart();
    ALog.d(LIB_TAG, TAG, "onStart:" + showDeleteDialog);
    // 如果是自己解散群聊，则直接关闭页面,否则弹出提示框
    if (((ChatTeamViewModel) viewModel).isMyDismiss()) {
      requireActivity().finish();
    } else if (showDeleteDialog) {
      showDialogToFinish(null);
    }
  }

  // 获取会话名称
  @Override
  public String getConversationName() {
    if (teamInfo != null) {
      return teamInfo.getName();
    }
    return super.getConversationName();
  }

  // 获取页面底部输入框布局
  public MessageBottomLayout getMessageBottomLayout() {
    return viewBinding.chatView.getBottomInputLayout();
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    ((ChatTeamViewModel) viewModel)
        .getTeamMessageReceiptLiveData()
        .removeObserver(teamReceiptObserver);
  }

  @Override
  protected void initDataObserver() {
    super.initDataObserver();
    ALog.d(LIB_TAG, TAG, "initDataObserver");
    // 群聊消息已读回执初始化
    teamReceiptObserver =
        listFetchResult -> {
          ALog.d(LIB_TAG, TAG, "TeamMessageReceiptLiveData,observer");
          if (listFetchResult == null || listFetchResult.getData() == null) return;
          List<ChatMessageBean> messageList = chatView.getMessageList();
          List<ChatMessageBean> updateMessage = new ArrayList<>();
          for (V2NIMTeamMessageReadReceipt receiptInfo : listFetchResult.getData()) {
            String msgId = receiptInfo.getMessageClientId();
            for (ChatMessageBean messageBean : messageList) {
              if (messageBean != null
                  && messageBean.getMessageData() != null
                  && TextUtils.equals(
                      messageBean.getMessageData().getMessage().getMessageClientId(), msgId)) {
                messageBean.getMessageData().setReadCount(receiptInfo.getReadCount());
                messageBean.getMessageData().setUnReadCount(receiptInfo.getUnreadCount());
                updateMessage.add(messageBean);
                break;
              }
            }
          }
          ALog.d(LIB_TAG, TAG, "TeamMessageReceiptLiveData,observer,msgList:" + messageList.size());
          for (ChatMessageBean message : updateMessage) {
            chatView.getMessageListView().updateMessageStatus(message);
          }
        };
    // 监听群聊消息已读回执
    ((ChatTeamViewModel) viewModel)
        .getTeamMessageReceiptLiveData()
        .observeForever(teamReceiptObserver);

    // 监听群信息
    ((ChatTeamViewModel) viewModel)
        .getTeamLiveData()
        .observe(
            getViewLifecycleOwner(),
            team -> {
              ALog.d(LIB_TAG, TAG, "TeamLiveData,observe");
              if (team != null) {
                teamInfo = team;
                if (!team.isValidTeam()) {
                  ALog.d(LIB_TAG, TAG, "TeamLiveData,observe invalid team");
                  showDialogToFinish(getString(R.string.chat_team_invalid_content));
                  return;
                }
                if (!TextUtils.isEmpty(team.getServerExtension())
                    && team.getServerExtension().contains(IMKitConstant.TEAM_GROUP_TAG)) {
                  viewModel.setTeamGroup(true);
                }
                aitManager.updateTeamInfo(team);
                refreshView();
              }
            });
    // 监听群成员数量变化
    ((ChatTeamViewModel) viewModel)
        .getTeamMemberChangeData()
        .observe(
            getViewLifecycleOwner(),
            result -> {
              ALog.d(LIB_TAG, TAG, "TeamMemberChangeData,observe");
              if (result.getLoadStatus() == LoadStatus.Finish && result.getData() != null) {
                List<String> accIdList = new ArrayList<>();
                for (TeamMemberWithUserInfo user : result.getData()) {
                  if (TextUtils.equals(user.getAccountId(), IMKitClient.account())) {
                    currentMember = user.getTeamMember();
                    refreshView();
                  }
                  accIdList.add(user.getAccountId());
                }
                chatView.getMessageListView().notifyUserInfoChanged(accIdList);
              }
            });
  }

  // 收到群解散或退出，处理解散群聊逻辑
  private void startTeamDismiss() {
    if (ChatTeamFragment.this.isResumed()) {
      if (((ChatTeamViewModel) viewModel).isMyDismiss()) {
        ChatTeamFragment.this.requireActivity().finish();
      } else {
        showDialogToFinish(null);
        showDeleteDialog = false;
      }
    } else {
      showDeleteDialog = true;
    }
  }

  // 被踢出群聊、解散群聊、群不存在弹窗
  protected void showDialogToFinish(String contentText) {
    AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
    LayoutInflater layoutInflater = LayoutInflater.from(requireContext());
    View dialogView = layoutInflater.inflate(R.layout.chat_alert_dialog_layout, null);
    TextView title = dialogView.findViewById(R.id.tv_dialog_title);
    TextView content = dialogView.findViewById(R.id.tv_dialog_content);
    TextView positiveBut = dialogView.findViewById(R.id.tv_dialog_positive);
    if (contentText != null) {
      contentText = getString(R.string.chat_team_be_removed_content);
    }
    content.setText(contentText);
    title.setText(getString(R.string.chat_team_be_removed_title));
    positiveBut.setText(getString(R.string.chat_dialog_sure));
    // 设置不可取消
    builder.setCancelable(false);
    builder.setView(dialogView);
    final AlertDialog alertDialog = builder.create();
    positiveBut.setOnClickListener(
        v -> {
          alertDialog.dismiss();
          requireActivity().finish();
        });

    alertDialog.show();
  }

  // 处理页面收到定位消息，重新拉取并定位该消息
  @Override
  public void onNewIntent(Intent intent) {
    ALog.d(LIB_TAG, TAG, "onNewIntent");
    anchorMessage = (IMMessageInfo) intent.getSerializableExtra(RouterConstant.KEY_MESSAGE_INFO);
    if (anchorMessage == null) {
      V2NIMMessage message = (V2NIMMessage) intent.getSerializableExtra(RouterConstant.KEY_MESSAGE);
      if (message != null) {
        anchorMessage = new IMMessageInfo(message);
      }
    }
    ChatMessageBean anchorMessageBean = null;
    if (anchorMessage != null) {
      anchorMessageBean = new ChatMessageBean(anchorMessage);
    }
    if (anchorMessage != null) {
      int position =
          chatView
              .getMessageListView()
              .searchMessagePosition(anchorMessage.getMessage().getMessageClientId());
      if (position >= 0) {
        chatView
            .getRootView()
            .getViewTreeObserver()
            .addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                  @Override
                  public void onGlobalLayout() {
                    chatView.getRootView().getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    chatView
                        .getRootView()
                        .post(() -> chatView.getMessageListView().scrollToPosition(position));
                  }
                });
        chatView.getMessageListView().scrollToPosition(position);
      } else {
        chatView.clearMessageList();
        // need to add anchor message to list panel
        chatView.appendMessage(anchorMessageBean);
        viewModel.getMessageList(anchorMessage.getMessage(), false);
      }
    }
  }

  // 处理页面收到新消息
  @Override
  protected void onReceiveMessage(FetchResult<List<ChatMessageBean>> listFetchResult) {
    super.onReceiveMessage(listFetchResult);
    if (listFetchResult.getData() != null) {
      List<ChatMessageBean> messageList = listFetchResult.getData();
      for (ChatMessageBean message : messageList) {
        if (MessageHelper.isDismissTeamMsg(message.getMessageData())) {
          startTeamDismiss();
          return;
        } else if (MessageHelper.isKickMsg(message.getMessageData())) {
          showDialogToFinish(getString(R.string.chat_team_be_kick_content));
          return;
        }
      }
    }
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    ALog.d(LIB_TAG, TAG, "onDestroy");
    if (aitManager != null) {
      aitManager.reset();
    }
  }
}
