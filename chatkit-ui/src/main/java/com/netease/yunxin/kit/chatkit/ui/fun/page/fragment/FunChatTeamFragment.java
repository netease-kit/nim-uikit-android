// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.fun.page.fragment;

import static com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant.LIB_TAG;
import static com.netease.yunxin.kit.corekit.im2.utils.RouterConstant.KEY_TEAM_ID;
import static com.netease.yunxin.kit.corekit.im2.utils.RouterConstant.PATH_FUN_TEAM_SETTING_PAGE;

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
import com.netease.nimlib.sdk.v2.team.model.V2NIMTeam;
import com.netease.nimlib.sdk.v2.team.model.V2NIMTeamMember;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.IMKitConfigCenter;
import com.netease.yunxin.kit.chatkit.model.IMMessageInfo;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.cache.TeamUserManager;
import com.netease.yunxin.kit.chatkit.ui.common.ChatUtils;
import com.netease.yunxin.kit.chatkit.ui.common.MessageHelper;
import com.netease.yunxin.kit.chatkit.ui.fun.view.MessageBottomLayout;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
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

/**
 * Fun皮肤群聊聊天界面Fragment，继承自FunChatFragment
 * 单聊和群聊有一些差异，为了方便维护，将差异化的部分抽象到FunChatP2PFragment和FunChatTeamFragment中
 */
public class FunChatTeamFragment extends FunChatFragment {
  private static final String TAG = "ChatTeamFragment";
  // 群信息
  V2NIMTeam teamInfo;
  // 当前用户在群中的群成员信息
  V2NIMTeamMember currentMember;
  // 加载群聊需要定位的消息
  IMMessageInfo anchorMessage;
  private AlertDialog alertDialog = null;

  // 是否展示退出群聊弹窗
  private boolean showDeleteDialog = false;
  private String deleteDialogContent = "";
  // 群聊消息已读回执观察者
  Observer<FetchResult<List<V2NIMTeamMessageReadReceipt>>> teamReceiptObserver;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    ALog.d(LIB_TAG, TAG, "onCreate");
    if (getArguments() != null) {
      conversationType = V2NIMConversationType.V2NIM_CONVERSATION_TYPE_TEAM;
      teamInfo = (V2NIMTeam) getArguments().getSerializable(RouterConstant.CHAT_KRY);
      accountId = (String) getArguments().getSerializable(RouterConstant.CHAT_ID_KRY);
    }
  }

  @Override
  protected void initData(Bundle bundle) {
    ALog.d(LIB_TAG, TAG, "initData");
    // 如果群信息为空，且accountId为空，直接关闭页面
    if (teamInfo == null && TextUtils.isEmpty(accountId)) {
      getActivity().finish();
      return;
    }
    if (TextUtils.isEmpty(accountId)) {
      accountId = teamInfo.getTeamId();
    }
    anchorMessage = (IMMessageInfo) bundle.getSerializable(RouterConstant.KEY_MESSAGE_INFO);
    if (anchorMessage == null) {
      V2NIMMessage message = (V2NIMMessage) bundle.getSerializable(RouterConstant.KEY_MESSAGE);
      if (message != null) {
        anchorMessage = new IMMessageInfo(message);
      }
    }
    // 初始化AitManager 用于@功能
    aitManager = new AitManager(getContext(), accountId);
    aitManager.setUIStyle(AitManager.STYLE_FUN);
    aitManager.updateTeamInfo(teamInfo);
    chatView.setAitManager(aitManager);
    refreshView();
  }

  @Override
  protected void initView() {
    super.initView();
    // 设置titleBar上按钮的点击事件
    chatView
        .getTitleBar()
        .setOnBackIconClickListener(v -> requireActivity().onBackPressed())
        .setActionImg(R.drawable.ic_more_point)
        .setActionListener(
            v -> {
              // go to team setting
              chatView.hideCurrentInput();
              XKitRouter.withKey(PATH_FUN_TEAM_SETTING_PAGE)
                  .withContext(requireContext())
                  .withParam(KEY_TEAM_ID, accountId)
                  .navigate();
            });
  }

  // 刷新界面
  private void refreshView() {
    if (teamInfo != null) {
      chatView.getTitleBar().setTitle(teamInfo.getName());
      chatView.getMessageListView().updateTeamInfo(teamInfo);
      chatView.getTitleBar().getActionImageView().setVisibility(View.VISIBLE);
    } else {
      chatView.getTitleBar().getActionImageView().setVisibility(View.GONE);
    }
    boolean isMute = ChatUtils.isMute(currentMember, teamInfo);
    ALog.d(LIB_TAG, TAG, "refreshView isMute:" + isMute);
    if (isMute) {
      chatView.setInputMute(true);
    } else {
      chatView.setInputMute(false);
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
      ((ChatTeamViewModel) viewModel).getTeamInfo();
      // 请求消息列表
      if (anchorMessage != null) {
        viewModel.getMessageList(anchorMessage.getMessage(), false);
      } else {
        viewModel.getMessageList(null, false);
      }
    }
  }

  @Override
  protected void updateDataWhenLogin() {
    if (viewModel instanceof ChatTeamViewModel) {
      // 请求群信息
      ((ChatTeamViewModel) viewModel).getTeamInfo();
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
    if (((ChatTeamViewModel) viewModel).isMyDismiss()) {
      requireActivity().finish();
    } else if (showDeleteDialog) {
      showDialogToFinish(deleteDialogContent);
    }
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
    // 群聊消息已读回执观察者,更新消息已读未读状态
    teamReceiptObserver =
        listFetchResult -> {
          ALog.d(LIB_TAG, TAG, "TeamMessageReceiptLiveData,observer");
          if (listFetchResult == null || listFetchResult.getData() == null) return;
          List<ChatMessageBean> messageList = chatView.getMessageList();
          if (messageList == null || messageList.isEmpty()) return;
          List<ChatMessageBean> updateMessage = new ArrayList<>();
          for (V2NIMTeamMessageReadReceipt receiptInfo : listFetchResult.getData()) {
            String msgId = receiptInfo.getMessageClientId();
            for (ChatMessageBean messageBean : messageList) {
              if (messageBean != null
                  && messageBean.getMessageData() != null
                  && TextUtils.equals(
                      messageBean.getMessageData().getMessage().getMessageClientId(), msgId)) {
                // 更新消息已读状态，判断已读书的变化，过滤已读数变小的case
                if (receiptInfo.getReadCount() >= messageBean.getMessageData().getReadCount()) {
                  messageBean.getMessageData().setReadCount(receiptInfo.getReadCount());
                  messageBean.getMessageData().setUnReadCount(receiptInfo.getUnreadCount());
                }
                updateMessage.add(messageBean);
                break;
              }
            }
          }
          ALog.d(LIB_TAG, TAG, "TeamMessageReceiptLiveData,observer");
          for (ChatMessageBean message : updateMessage) {
            chatView.getMessageListView().updateMessageStatus(message);
          }
        };
    // 监听群已读未读回执
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
                  handleTeamDismiss(getString(R.string.chat_team_invalid_content));
                  return;
                }
                if (!TextUtils.isEmpty(team.getServerExtension())
                    && team.getServerExtension().contains(IMKitConstant.TEAM_GROUP_TAG)) {
                  viewModel.setTeamGroup(true);
                }
                aitManager.updateTeamInfo(teamInfo);
                refreshView();
              }
            });
  }

  @Override
  protected void onUserInfoChanged(FetchResult<List<String>> fetchResult) {
    super.onUserInfoChanged(fetchResult);
    ALog.d(LIB_TAG, TAG, "onUserInfoChanged");
    if (fetchResult.getLoadStatus() == LoadStatus.Finish && fetchResult.getData() != null) {
      for (String userId : fetchResult.getData()) {
        if (TextUtils.equals(userId, IMKitClient.account())) {
          currentMember = TeamUserManager.getInstance().getCurTeamMember();
          refreshView();
        }
      }
    }
  }

  //  处理群聊解散，如果配置不删除会话，则不进行弹窗，输入隐藏即可
  protected void handleTeamDismiss(String dialogContent) {
    if (IMKitConfigCenter.getEnableDismissTeamDeleteConversation()) {
      startTeamDismiss(dialogContent);
      viewBinding.chatView.getBottomInputLayout().setVisibility(View.VISIBLE);
      viewBinding.chatInvalidTipLayout.setVisibility(View.GONE);
    } else {
      viewBinding.chatView.getBottomInputLayout().setVisibility(View.GONE);
      viewBinding.chatInvalidTipLayout.setVisibility(View.VISIBLE);
    }
  }

  // 收到解散群聊通知，处理逻辑。如果是主动退出则直接关闭页面，否则弹窗提示
  private void startTeamDismiss(String dialogContent) {
    ALog.d(LIB_TAG, TAG, "startTeamDismiss");
    if (FunChatTeamFragment.this.isResumed()) {
      if (((ChatTeamViewModel) viewModel).isMyDismiss()) {
        FunChatTeamFragment.this.requireActivity().finish();
      } else {
        showDialogToFinish(dialogContent);
        showDeleteDialog = false;
      }
    } else {
      deleteDialogContent = dialogContent;
      showDeleteDialog = true;
    }
  }

  /** 清除解散群聊弹窗 如果踢出群，在邀请入群可能出现该场景，此时不需要弹窗也不需要关闭页面 */
  protected void clearTeamDismiss() {
    if (FunChatTeamFragment.this.isResumed() && alertDialog != null) {
      alertDialog.dismiss();
    }
    deleteDialogContent = "";
    showDeleteDialog = false;
    viewBinding.chatView.getBottomInputLayout().setVisibility(View.VISIBLE);
    viewBinding.chatInvalidTipLayout.setVisibility(View.GONE);
  }

  // 被踢出群聊、解散群聊、群不存在弹窗提示
  protected void showDialogToFinish(String contentText) {
    AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
    LayoutInflater layoutInflater = LayoutInflater.from(requireContext());
    View dialogView = layoutInflater.inflate(R.layout.chat_alert_dialog_layout, null);
    TextView title = dialogView.findViewById(R.id.tv_dialog_title);
    TextView content = dialogView.findViewById(R.id.tv_dialog_content);
    TextView positiveBut = dialogView.findViewById(R.id.tv_dialog_positive);
    if (TextUtils.isEmpty(contentText)) {
      contentText = getString(R.string.chat_team_be_removed_content);
    }
    content.setText(contentText);
    title.setText(getString(R.string.chat_team_be_removed_title));
    positiveBut.setText(getString(R.string.chat_dialog_sure));
    positiveBut.setTextColor(getResources().getColor(R.color.fun_chat_color));
    // 设置不可取消
    builder.setCancelable(false);
    builder.setView(dialogView);
    alertDialog = builder.create();
    positiveBut.setOnClickListener(
        v -> {
          if (alertDialog != null) {
            ((ChatTeamViewModel) viewModel).sendTeamDismissEvent();
            alertDialog.dismiss();
          }
          requireActivity().finish();
        });
    alertDialog.show();
  }

  // 收到定位消息，需要重新拉取消息并定位到该消息
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

  // 获取会话名称，展示使用
  @Override
  public String getConversationName() {
    if (teamInfo != null) {
      return teamInfo.getName();
    }
    return super.getConversationName();
  }

  // 获取会话底部布局Layout
  public MessageBottomLayout getMessageBottomLayout() {
    return viewBinding.chatView.getBottomInputLayout();
  }

  // 接受消息
  @Override
  protected void onReceiveMessage(FetchResult<List<ChatMessageBean>> listFetchResult) {
    super.onReceiveMessage(listFetchResult);
    if (listFetchResult != null && listFetchResult.getData() != null) {
      List<ChatMessageBean> messageList = listFetchResult.getData();
      for (ChatMessageBean message : messageList) {
        if (MessageHelper.isDismissTeamMsg(message.getMessageData())) {
          handleTeamDismiss(null);
          return;
        } else if (MessageHelper.isKickMsg(message.getMessageData())) {
          handleTeamDismiss(getString(R.string.chat_team_be_kick_content));
          return;
        } else if (MessageHelper.isTeamJoinedMsg(message.getMessageData())) {
          clearTeamDismiss();
          ALog.d(LIB_TAG, TAG, "onReceiveMessage:isTeamJoinedMsg");
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
