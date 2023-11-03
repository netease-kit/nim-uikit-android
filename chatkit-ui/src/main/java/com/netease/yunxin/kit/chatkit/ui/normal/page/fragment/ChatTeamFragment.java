// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.normal.page.fragment;

import static com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant.LIB_TAG;
import static com.netease.yunxin.kit.corekit.im.utils.RouterConstant.KEY_TEAM_ID;
import static com.netease.yunxin.kit.corekit.im.utils.RouterConstant.PATH_TEAM_SETTING_PAGE;

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
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.model.IMMessageInfo;
import com.netease.yunxin.kit.chatkit.model.IMTeamMessageReceiptInfo;
import com.netease.yunxin.kit.chatkit.model.UserInfoWithTeam;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.chatkit.ui.normal.view.MessageBottomLayout;
import com.netease.yunxin.kit.chatkit.ui.page.viewmodel.ChatTeamViewModel;
import com.netease.yunxin.kit.chatkit.ui.view.ait.AitManager;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.corekit.im.IMKitClient;
import com.netease.yunxin.kit.corekit.im.utils.IMKitConstant;
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import java.util.ArrayList;
import java.util.List;

/** Team chat page */
public class ChatTeamFragment extends NormalChatFragment {
  private static final String TAG = "ChatTeamFragment";
  Team teamInfo;
  IMMessage anchorMessage;
  private boolean showDeleteDialog = false;
  Observer<FetchResult<List<IMTeamMessageReceiptInfo>>> teamReceiptObserver;

  @Override
  protected void initData(Bundle bundle) {
    ALog.d(LIB_TAG, TAG, "initData");
    sessionType = SessionTypeEnum.Team;
    teamInfo = (Team) bundle.getSerializable(RouterConstant.CHAT_KRY);
    sessionID = (String) bundle.getSerializable(RouterConstant.CHAT_ID_KRY);
    if (teamInfo == null && TextUtils.isEmpty(sessionID)) {
      getActivity().finish();
      return;
    }
    if (TextUtils.isEmpty(sessionID)) {
      sessionID = teamInfo.getId();
    }
    anchorMessage = (IMMessage) bundle.getSerializable(RouterConstant.KEY_MESSAGE);
    aitManager = new AitManager(getContext(), sessionID);
    chatView.setAitManager(aitManager);
    refreshView();
  }

  @Override
  protected void initView() {
    super.initView();
    chatView
        .getTitleBar()
        .setOnBackIconClickListener(v -> requireActivity().onBackPressed())
        .setActionImg(R.drawable.ic_more_point)
        .setActionListener(
            v -> {
              //go to team setting
              chatView.hideCurrentInput();
              XKitRouter.withKey(PATH_TEAM_SETTING_PAGE)
                  .withContext(requireContext())
                  .withParam(KEY_TEAM_ID, sessionID)
                  .navigate();
            });
  }

  private void refreshView() {
    if (teamInfo != null) {
      chatView.getTitleBar().setTitle(teamInfo.getName());
      chatView.updateInputHintInfo(teamInfo.getName());
      if (!TextUtils.equals(teamInfo.getCreator(), IMKitClient.account())) {
        chatView.setInputMute(teamInfo.isAllMute());
      }
      chatView.getMessageListView().updateTeamInfo(teamInfo);
    }
  }

  @Override
  protected void initViewModel() {
    ALog.d(LIB_TAG, TAG, "initViewModel");
    viewModel = new ViewModelProvider(this).get(ChatTeamViewModel.class);
    viewModel.init(sessionID, SessionTypeEnum.Team);
  }

  @Override
  protected void initToFetchData() {
    if (viewModel instanceof ChatTeamViewModel) {
      // query team info
      ((ChatTeamViewModel) viewModel).requestTeamInfo(sessionID);
      // init team members
      ((ChatTeamViewModel) viewModel).requestTeamMembers(sessionID);
      //fetch history message
      viewModel.initFetch(anchorMessage, false);
    }
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    if (chatConfig != null && chatConfig.chatListener != null) {
      chatConfig.chatListener.onSessionChange(sessionID, sessionType);
    }
    if (chatConfig != null && chatConfig.messageProperties != null) {
      viewModel.setShowReadStatus(chatConfig.messageProperties.showTeamMessageStatus);
    }
  }

  @Override
  public void onStart() {
    super.onStart();
    ALog.d(LIB_TAG, TAG, "onStart:" + showDeleteDialog);
    if (showDeleteDialog) {
      showDialogToFinish();
    }
  }

  public MessageBottomLayout getMessageBottomLayout() {
    return viewBinding.chatView.getInputView();
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
    teamReceiptObserver =
        listFetchResult -> {
          ALog.d(LIB_TAG, TAG, "TeamMessageReceiptLiveData,observer");
          if (listFetchResult == null || listFetchResult.getData() == null) return;
          List<ChatMessageBean> messageList = new ArrayList<>();
          for (IMTeamMessageReceiptInfo receiptInfo : listFetchResult.getData()) {
            String msgId = receiptInfo.getTeamMessageReceipt().getMsgId();
            ChatMessageBean msg = null;
            for (ChatMessageBean messageBean : messageList) {
              if (messageBean != null
                  && messageBean.getMessageData() != null
                  && TextUtils.equals(messageBean.getMessageData().getMessage().getUuid(), msgId)) {
                msg = messageBean;
                break;
              }
            }
            if (msg == null) {
              msg = chatView.getMessageListView().searchMessage(msgId);
              if (msg != null) {
                messageList.add(msg);
              }
            }
          }
          ALog.d(LIB_TAG, TAG, "TeamMessageReceiptLiveData,observer,msgList:" + messageList.size());
          for (ChatMessageBean message : messageList) {
            chatView.getMessageListView().updateMessageStatus(message);
          }
        };
    ((ChatTeamViewModel) viewModel)
        .getTeamMessageReceiptLiveData()
        .observeForever(teamReceiptObserver);

    ((ChatTeamViewModel) viewModel)
        .getTeamLiveData()
        .observe(
            getViewLifecycleOwner(),
            team -> {
              ALog.d(LIB_TAG, TAG, "TeamLiveData,observe");
              if (team != null) {
                teamInfo = team;
                if (!team.isMyTeam()) {
                  requireActivity().finish();
                }
                if (!TextUtils.isEmpty(team.getExtension())
                    && team.getExtension().contains(IMKitConstant.TEAM_GROUP_TAG)) {
                  viewModel.setTeamGroup(true);
                }
                refreshView();
              }
            });

    ((ChatTeamViewModel) viewModel)
        .getTeamRemoveLiveData()
        .observeForever(
            team -> {
              ALog.d(LIB_TAG, TAG, "TeamRemoveLiveData,observe");
              if (((ChatTeamViewModel) viewModel).isMyDismiss()) {
                ChatTeamFragment.this.requireActivity().finish();
              } else {
                if (ChatTeamFragment.this.isResumed()) {
                  showDialogToFinish();
                  showDeleteDialog = false;
                } else {
                  showDeleteDialog = true;
                }
              }
            });

    ((ChatTeamViewModel) viewModel)
        .getTeamMemberData()
        .observe(
            getViewLifecycleOwner(),
            teamMembers -> {
              ALog.d(LIB_TAG, TAG, "TeamMemberData,observe");

              if (teamMembers.getSuccess() && teamMembers.getValue() != null) {
                aitManager.setTeamMembers(teamMembers.getValue());

                if (((ChatTeamViewModel) viewModel).hasLoadMessage()) {
                  List<String> accIdList = new ArrayList<>();
                  for (UserInfoWithTeam user : teamMembers.getValue()) {
                    accIdList.add(user.getTeamInfo().getAccount());
                  }
                  if (accIdList.size() > 0) {
                    chatView.getMessageListView().notifyUserInfoChange(accIdList);
                  }
                }
              }
            });

    ((ChatTeamViewModel) viewModel)
        .getTeamMemberChangeData()
        .observe(
            getViewLifecycleOwner(),
            result -> {
              ALog.d(LIB_TAG, TAG, "TeamMemberChangeData,observe");
              if (result.getLoadStatus() == LoadStatus.Finish) {
                chatView.getMessageListView().notifyUserInfoChange(result.getData());
              }
            });
  }

  private void showDialogToFinish() {
    AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
    LayoutInflater layoutInflater = LayoutInflater.from(requireContext());
    View dialogView = layoutInflater.inflate(R.layout.chat_alert_dialog_layout, null);
    TextView title = dialogView.findViewById(R.id.tv_dialog_title);
    TextView content = dialogView.findViewById(R.id.tv_dialog_content);
    TextView positiveBut = dialogView.findViewById(R.id.tv_dialog_positive);
    content.setText(getString(R.string.chat_team_be_removed_content));
    title.setText(getString(R.string.chat_team_be_removed_title));
    positiveBut.setText(getString(R.string.chat_dialog_sure));
    // 设置不可取消
    builder.setCancelable(false);
    positiveBut.setOnClickListener(v -> ChatTeamFragment.this.requireActivity().finish());
    AlertDialog alertDialog = builder.create();
    alertDialog.show();
    alertDialog.getWindow().setContentView(dialogView);
  }

  @Override
  public void onNewIntent(Intent intent) {
    ALog.d(LIB_TAG, TAG, "onNewIntent");
    anchorMessage = (IMMessage) intent.getSerializableExtra(RouterConstant.KEY_MESSAGE);
    ChatMessageBean anchorMessageBean =
        (ChatMessageBean) intent.getSerializableExtra(RouterConstant.KEY_MESSAGE_BEAN);
    if (anchorMessageBean != null) {
      anchorMessage = anchorMessageBean.getMessageData().getMessage();
    } else if (anchorMessage != null) {
      anchorMessageBean = new ChatMessageBean(new IMMessageInfo(anchorMessage));
    }
    if (anchorMessage != null) {
      int position = chatView.getMessageListView().searchMessagePosition(anchorMessage.getUuid());
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
        viewModel.initFetch(anchorMessage, false);
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
