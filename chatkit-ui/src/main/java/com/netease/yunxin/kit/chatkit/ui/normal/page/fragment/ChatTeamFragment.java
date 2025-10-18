// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.normal.page.fragment;

import static com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant.LIB_TAG;
import static com.netease.yunxin.kit.corekit.im2.utils.RouterConstant.KEY_TEAM_ID;
import static com.netease.yunxin.kit.corekit.im2.utils.RouterConstant.PATH_TEAM_SETTING_PAGE;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.res.ResourcesCompat;
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
import com.netease.yunxin.kit.chatkit.ui.ChatKitClient;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.cache.TeamUserManager;
import com.netease.yunxin.kit.chatkit.ui.common.ChatUtils;
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
  private AlertDialog alertDialog = null;
  // 是否展示删除对话框
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
      chatView.updateInputHintInfo(teamInfo.getName());
      chatView.getMessageListView().updateTeamInfo(teamInfo);
      chatView.getTitleBar().getActionImageView().setVisibility(View.VISIBLE);
      String name = teamInfo.getName();
      if (ChatKitClient.isEarphoneMode()) {
        SpannableString spannable = new SpannableString(name + "v");
        Drawable icon =
            ResourcesCompat.getDrawable(getResources(), R.drawable.ic_chat_audio_earphone, null);
        int iconSize = getResources().getDimensionPixelSize(R.dimen.chat_earphone_icon_size);
        if (icon != null) {
          icon.setBounds(0, 0, iconSize, iconSize); // 宽高根据需求调整
          ImageSpan imageSpan = new ImageSpan(icon, ImageSpan.ALIGN_BASELINE); // 图标与文字基线对齐
          int start = name.length();
          spannable.setSpan(imageSpan, start, start + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        chatView.getTitleBar().setTitle(spannable);
      } else {
        chatView.getTitleBar().setTitle(name);
      }
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
      // 请求群聊消息
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
    // 如果是自己解散群聊，则直接关闭页面,否则弹出提示框
    if (((ChatTeamViewModel) viewModel).isMyDismiss()) {
      requireActivity().finish();
    } else if (showDeleteDialog) {
      showDialogToFinish(deleteDialogContent);
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
          ALog.d(LIB_TAG, TAG, "TeamMessageReceiptLiveData,observer,msgList:" + messageList.size());
          for (ChatMessageBean message : updateMessage) {
            chatView.updateMessageStatus(message);
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
              if (team != null) {
                ALog.d(LIB_TAG, TAG, "TeamLiveData,observe:" + team.getTeamId());
                teamInfo = team;
                if (!team.isValidTeam()) {
                  ALog.d(LIB_TAG, TAG, "TeamLiveData,observe invalid team");
                  handleTeamDismiss(getString(R.string.chat_team_invalid_content));
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
  }

  @Override
  protected void onUserInfoChanged(FetchResult<List<String>> fetchResult) {
    super.onUserInfoChanged(fetchResult);
    if (fetchResult.getLoadStatus() == LoadStatus.Finish && fetchResult.getData() != null) {
      for (String userId : fetchResult.getData()) {
        if (TextUtils.equals(userId, IMKitClient.account())) {
          currentMember = TeamUserManager.getInstance().getCurTeamMember();
          refreshView();
        }
      }
    }
  }

  // 处理群聊解散，如果配置不删除会话，则不进行弹窗，输入隐藏即可
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

  // 收到群解散或退出，处理解散群聊逻辑
  private void startTeamDismiss(String dialogContent) {
    if (ChatTeamFragment.this.isResumed()) {
      if (((ChatTeamViewModel) viewModel).isMyDismiss()) {
        ChatTeamFragment.this.requireActivity().finish();
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
    if (ChatTeamFragment.this.isResumed() && alertDialog != null) {
      alertDialog.dismiss();
    }
    deleteDialogContent = "";
    showDeleteDialog = false;
    viewBinding.chatView.getBottomInputLayout().setVisibility(View.VISIBLE);
    viewBinding.chatInvalidTipLayout.setVisibility(View.GONE);
  }

  // 被踢出群聊、解散群聊、群不存在弹窗
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
    // 设置不可取消
    builder.setCancelable(false);
    builder.setView(dialogView);
    alertDialog = builder.create();
    positiveBut.setOnClickListener(
        v -> {
          //在点击弹窗的时候，发送UIKit层群解散事件，会话列表页面监听到事件，
          ((ChatTeamViewModel) viewModel).sendTeamDismissEvent();
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
    ALog.d(LIB_TAG, TAG, "onReceiveMessage");
    if (listFetchResult.getData() != null) {
      List<ChatMessageBean> messageList = listFetchResult.getData();
      for (ChatMessageBean message : messageList) {
        if (MessageHelper.isDismissTeamMsg(message.getMessageData())) {
          ALog.d(LIB_TAG, TAG, "onReceiveMessage:isDismissTeamMsg");
          handleTeamDismiss(null);
          return;
        } else if (MessageHelper.isKickMsg(message.getMessageData())) {
          handleTeamDismiss(getString(R.string.chat_team_be_kick_content));
          ALog.d(LIB_TAG, TAG, "onReceiveMessage:isKickMsg");
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
