// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.page;

import static com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant.LIB_TAG;
import static com.netease.yunxin.kit.corekit.im.utils.RouterConstant.REQUEST_CONTACT_SELECTOR_KEY;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.common.ChatUtils;
import com.netease.yunxin.kit.chatkit.ui.common.MessageDialog;
import com.netease.yunxin.kit.chatkit.ui.common.MessageHelper;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatPinActivityBinding;
import com.netease.yunxin.kit.chatkit.ui.dialog.ChatMessageForwardConfirmDialog;
import com.netease.yunxin.kit.chatkit.ui.dialog.ChatMessageForwardSelectDialog;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.chatkit.ui.page.adapter.PinMessageAdapter;
import com.netease.yunxin.kit.chatkit.ui.page.viewmodel.ChatPinViewModel;
import com.netease.yunxin.kit.chatkit.ui.view.interfaces.IPinMessageClickListener;
import com.netease.yunxin.kit.common.ui.action.ActionItem;
import com.netease.yunxin.kit.common.ui.activities.BaseActivity;
import com.netease.yunxin.kit.common.ui.dialog.BottomChoiceDialog;
import com.netease.yunxin.kit.common.utils.NetworkUtils;
import com.netease.yunxin.kit.common.utils.SizeUtils;
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import java.util.ArrayList;

public class ChatPinActivity extends BaseActivity {

  public static final String TAG = "ChatPinActivity";
  private ChatPinActivityBinding viewBinding;
  private ChatPinViewModel viewModel;
  private String mSessionId;
  private SessionTypeEnum mSessionType;
  private PinMessageAdapter pinAdapter;
  ChatMessageBean forwardMessage;

  private ActivityResultLauncher<Intent> forwardP2PLauncher;
  private ActivityResultLauncher<Intent> forwardTeamLauncher;

  public static final String ACTION_CHECK_PIN = "check_pin";
  public static final String ACTION_CANCEL_PIN = "cancel_pin";
  public static final String ACTION_TRANSMIT_PIN = "message_transmit";
  public static final String ACTION_COPY_PIN = "message_copy";

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    changeStatusBarColor(R.color.color_eef1f4);
    viewBinding = ChatPinActivityBinding.inflate(getLayoutInflater());
    setContentView(viewBinding.getRoot());
    initView();
    initData();
  }

  private void initView() {
    viewBinding.pinTitleBar.setOnBackIconClickListener(view -> finish());
    LinearLayoutManager layoutManager = new LinearLayoutManager(this);
    viewBinding.pinRecyclerView.setLayoutManager(layoutManager);
    int lRPadding = SizeUtils.dp2px(20);
    int topPadding = SizeUtils.dp2px(12);
    RecyclerView.ItemDecoration itemDecoration =
        new RecyclerView.ItemDecoration() {
          @Override
          public void getItemOffsets(
              @NonNull Rect outRect,
              @NonNull View view,
              @NonNull RecyclerView parent,
              @NonNull RecyclerView.State state) {
            outRect.set(lRPadding, topPadding, lRPadding, 0);
          }
        };
    viewBinding.pinRecyclerView.addItemDecoration(itemDecoration);
    pinAdapter = new PinMessageAdapter();
    pinAdapter.setViewHolderClickListener(pinClickListener);
    viewBinding.pinRecyclerView.setAdapter(pinAdapter);
  }

  private void initData() {
    viewModel = new ViewModelProvider(this).get(ChatPinViewModel.class);
    mSessionId = getIntent().getStringExtra(RouterConstant.KEY_SESSION_ID);
    mSessionType =
        SessionTypeEnum.typeOfValue(getIntent().getIntExtra(RouterConstant.KEY_SESSION_TYPE, -1));

    viewModel
        .getMessageLiveData()
        .observe(
            this,
            result -> {
              showEmptyView(result.getData() == null || result.getData().size() < 1);
              pinAdapter.setData(result.getData());
            });

    viewModel
        .getRemovePinLiveData()
        .observe(
            this,
            result -> {
              pinAdapter.removeDataWithUuId(result.getData());
              showEmptyView(pinAdapter.getItemCount() < 1);
            });
    viewModel
        .getAddPinLiveData()
        .observe(
            this,
            result -> {
              pinAdapter.addData(result.getData());
              showEmptyView(pinAdapter.getItemCount() < 1);
            });

    forwardP2PLauncher =
        registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
              if (result.getResultCode() != Activity.RESULT_OK || forwardMessage == null) {
                return;
              }
              ALog.d(LIB_TAG, TAG, "forward P2P result");
              Intent data = result.getData();
              if (data != null) {
                ArrayList<String> friends =
                    data.getStringArrayListExtra(REQUEST_CONTACT_SELECTOR_KEY);
                if (friends != null && !friends.isEmpty()) {
                  showForwardConfirmDialog(SessionTypeEnum.P2P, friends);
                }
              }
            });

    forwardTeamLauncher =
        registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
              if (result.getResultCode() != Activity.RESULT_OK || forwardMessage == null) {
                return;
              }
              ALog.d(LIB_TAG, TAG, "forward Team result");
              Intent data = result.getData();
              if (data != null) {
                String tid = data.getStringExtra(RouterConstant.KEY_TEAM_ID);
                if (!TextUtils.isEmpty(tid)) {
                  ArrayList<String> sessionIds = new ArrayList<>();
                  sessionIds.add(tid);
                  showForwardConfirmDialog(SessionTypeEnum.Team, sessionIds);
                }
              }
            });

    if (TextUtils.isEmpty(mSessionId) || mSessionType == SessionTypeEnum.None) {
      showEmptyView(true);
    } else {
      showEmptyView(false);
      viewModel.init(mSessionId, mSessionType);
      viewModel.fetchPinMsg();
    }
  }

  private void showEmptyView(boolean show) {
    if (show) {
      viewBinding.pinEmptyView.setVisibility(View.VISIBLE);
    } else {
      viewBinding.pinEmptyView.setVisibility(View.GONE);
    }
  }

  private final IPinMessageClickListener pinClickListener =
      new IPinMessageClickListener() {
        @Override
        public boolean onMessageClick(View view, int position, ChatMessageBean messageInfo) {
          jumpToChat(messageInfo);
          return true;
        }

        @Override
        public boolean onViewClick(View view, int position, ChatMessageBean messageInfo) {
          if (view.getId() == R.id.iv_more_action) {
            showMoreActionDialog(messageInfo);
          } else if (view.getId() == R.id.messageText) {
            MessageDialog.launchDialog(
                getSupportFragmentManager(), TAG, messageInfo.getMessageData());
          } else {
            jumpToChat(messageInfo);
          }
          return true;
        }
      };

  private void jumpToChat(ChatMessageBean messageInfo) {
    String router = RouterConstant.PATH_CHAT_TEAM_PAGE;
    if (mSessionType == SessionTypeEnum.P2P) {
      router = RouterConstant.PATH_CHAT_P2P_PAGE;
    }

    XKitRouter.withKey(router)
        .withParam(RouterConstant.KEY_MESSAGE_BEAN, messageInfo)
        .withParam(RouterConstant.CHAT_KRY, mSessionId)
        .withContext(ChatPinActivity.this)
        .navigate();
    finish();
  }

  private void showMoreActionDialog(ChatMessageBean messageInfo) {
    BottomChoiceDialog dialog = new BottomChoiceDialog(this, assembleActions(messageInfo));
    dialog.setOnChoiceListener(
        new BottomChoiceDialog.OnChoiceListener() {
          @Override
          public void onChoice(@NonNull String type) {
            boolean hasNetwork = NetworkUtils.isConnected();
            switch (type) {
              case ACTION_TRANSMIT_PIN:
                if (hasNetwork) {
                  onTransmit(messageInfo);
                } else {
                  Toast.makeText(
                          ChatPinActivity.this,
                          R.string.chat_network_error_tips,
                          Toast.LENGTH_SHORT)
                      .show();
                }
                break;
              case ACTION_CANCEL_PIN:
                if (hasNetwork) {
                  viewModel.removePin(messageInfo.getMessageData());
                } else {
                  Toast.makeText(
                          ChatPinActivity.this,
                          R.string.chat_network_error_tips,
                          Toast.LENGTH_SHORT)
                      .show();
                }
                break;
              case ACTION_COPY_PIN:
                MessageHelper.copyTextMessage(messageInfo.getMessageData(), true);
                break;
              case ACTION_CHECK_PIN:
                jumpToChat(messageInfo);
                break;
              default:
                break;
            }
          }

          @Override
          public void onCancel() {}
        });
    dialog.show();
  }

  private ArrayList<ActionItem> assembleActions(ChatMessageBean messageInfo) {
    ArrayList<ActionItem> actions = new ArrayList<>();
    //    actions.add(
    //        new ActionItem(ACTION_CHECK_PIN, 0, R.string.chat_pin_more_check_action)
    //            .setTitleColorResId(R.color.color_333333));
    actions.add(
        new ActionItem(ACTION_CANCEL_PIN, 0, R.string.chat_message_action_pin_cancel)
            .setTitleColorResId(R.color.color_333333));
    if (messageInfo.getViewType() == MsgTypeEnum.text.getValue()) {
      actions.add(
          new ActionItem(ACTION_COPY_PIN, 0, R.string.chat_message_action_copy)
              .setTitleColorResId(R.color.color_333333));
    }
    if (messageInfo.getViewType() != MsgTypeEnum.audio.getValue()) {
      actions.add(
          new ActionItem(ACTION_TRANSMIT_PIN, 0, R.string.chat_message_action_transmit)
              .setTitleColorResId(R.color.color_333333));
    }

    return actions;
  }

  private void onTransmit(ChatMessageBean messageBean) {
    forwardMessage = messageBean;
    ChatMessageForwardSelectDialog dialog = new ChatMessageForwardSelectDialog();
    dialog.setSelectedCallback(
        new ChatMessageForwardSelectDialog.ForwardTypeSelectedCallback() {
          @Override
          public void onTeamSelected() {
            ChatUtils.startTeamList(ChatPinActivity.this, forwardTeamLauncher);
          }

          @Override
          public void onP2PSelected() {
            ChatUtils.startP2PSelector(
                ChatPinActivity.this,
                mSessionType == SessionTypeEnum.P2P ? mSessionId : null,
                forwardP2PLauncher);
          }
        });
    dialog.show(getSupportFragmentManager(), TAG);
  }

  private void showForwardConfirmDialog(SessionTypeEnum type, ArrayList<String> sessionIds) {
    ChatMessageForwardConfirmDialog confirmDialog =
        ChatMessageForwardConfirmDialog.createForwardConfirmDialog(
            type, sessionIds, forwardMessage.getMessageData());
    confirmDialog.setCallback(
        () -> {
          if (forwardMessage != null) {
            for (String accId : sessionIds) {
              viewModel.sendForwardMessage(
                  forwardMessage.getMessageData().getMessage(), accId, type);
            }
          }
        });
    confirmDialog.show(getSupportFragmentManager(), TAG);
  }
}
