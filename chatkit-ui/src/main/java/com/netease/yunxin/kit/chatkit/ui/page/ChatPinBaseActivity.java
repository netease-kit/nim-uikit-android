// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.page;

import static com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant.LIB_TAG;
import static com.netease.yunxin.kit.chatkit.ui.ChatUIConstants.KEY_MAP_FOR_PIN;
import static com.netease.yunxin.kit.corekit.im.utils.RouterConstant.REQUEST_CONTACT_SELECTOR_KEY;

import android.app.Activity;
import android.content.Intent;
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
import com.netease.yunxin.kit.chatkit.ui.ChatKitClient;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.common.MessageHelper;
import com.netease.yunxin.kit.chatkit.ui.common.WatchTextMessageDialog;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatPinActivityBinding;
import com.netease.yunxin.kit.chatkit.ui.dialog.ChatBaseForwardSelectDialog;
import com.netease.yunxin.kit.chatkit.ui.interfaces.IChatClickListener;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.chatkit.ui.page.adapter.PinMessageAdapter;
import com.netease.yunxin.kit.chatkit.ui.page.viewmodel.ChatPinViewModel;
import com.netease.yunxin.kit.common.ui.action.ActionItem;
import com.netease.yunxin.kit.common.ui.activities.BaseActivity;
import com.netease.yunxin.kit.common.ui.dialog.BaseBottomChoiceDialog;
import com.netease.yunxin.kit.common.ui.dialog.BottomChoiceDialog;
import com.netease.yunxin.kit.common.utils.NetworkUtils;
import com.netease.yunxin.kit.corekit.im.IMKitClient;
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import java.util.ArrayList;

public abstract class ChatPinBaseActivity extends BaseActivity {

  public static final String TAG = "ChatPinActivity";
  protected ChatPinActivityBinding viewBinding;
  protected ChatPinViewModel viewModel;
  protected String mSessionId;
  protected SessionTypeEnum mSessionType;
  protected PinMessageAdapter pinAdapter;
  protected ChatMessageBean forwardMessage;

  protected ActivityResultLauncher<Intent> forwardP2PLauncher;
  protected ActivityResultLauncher<Intent> forwardTeamLauncher;

  public static final String ACTION_CHECK_PIN = "check_pin";
  public static final String ACTION_CANCEL_PIN = "cancel_pin";
  public static final String ACTION_TRANSMIT_PIN = "message_transmit";
  public static final String ACTION_COPY_PIN = "message_copy";

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    viewBinding = ChatPinActivityBinding.inflate(getLayoutInflater());
    setContentView(viewBinding.getRoot());
    initView();
    initData();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (ChatKitClient.getMessageMapProvider() != null) {
      ChatKitClient.getMessageMapProvider().releaseAllChatMap(KEY_MAP_FOR_PIN);
    }
  }

  protected void initView() {
    viewBinding.pinTitleBar.setOnBackIconClickListener(view -> finish());
    LinearLayoutManager layoutManager = new LinearLayoutManager(this);
    viewBinding.pinRecyclerView.setLayoutManager(layoutManager);
    RecyclerView.ItemDecoration itemDecoration = getItemDecoration();
    if (itemDecoration != null) {
      viewBinding.pinRecyclerView.addItemDecoration(itemDecoration);
    }
    pinAdapter = new PinMessageAdapter();
    pinAdapter.setViewHolderClickListener(pinClickListener);
    viewBinding.pinRecyclerView.setAdapter(pinAdapter);
  }

  protected void initData() {
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
    viewModel
        .getDeleteMessageLiveData()
        .observe(
            this,
            result -> {
              pinAdapter.removeData(result.getData());
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

  public RecyclerView.ItemDecoration getItemDecoration() {
    return null;
  }

  private final IChatClickListener pinClickListener =
      new IChatClickListener() {
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
            WatchTextMessageDialog.launchDialog(
                getSupportFragmentManager(), TAG, messageInfo.getMessageData());
          } else {
            jumpToChat(messageInfo);
          }
          return true;
        }
      };

  public void jumpToChat(ChatMessageBean messageInfo) {
    String router = RouterConstant.PATH_CHAT_TEAM_PAGE;
    if (mSessionType == SessionTypeEnum.P2P) {
      router = RouterConstant.PATH_CHAT_P2P_PAGE;
    }

    XKitRouter.withKey(router)
        .withParam(RouterConstant.KEY_MESSAGE_BEAN, messageInfo)
        .withParam(RouterConstant.CHAT_KRY, mSessionId)
        .withContext(ChatPinBaseActivity.this)
        .navigate();
    finish();
  }

  public void showMoreActionDialog(ChatMessageBean messageInfo) {
    BaseBottomChoiceDialog dialog = getMoreActionDialog(messageInfo);
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
                          ChatPinBaseActivity.this,
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
                          ChatPinBaseActivity.this,
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

  public BaseBottomChoiceDialog getMoreActionDialog(ChatMessageBean messageInfo) {
    return new BottomChoiceDialog(this, assembleActions(messageInfo));
  }

  public ArrayList<ActionItem> assembleActions(ChatMessageBean messageInfo) {
    ArrayList<ActionItem> actions = new ArrayList<>();
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

  protected abstract ChatBaseForwardSelectDialog getForwardSelectDialog();

  protected abstract void toP2PSelected();

  protected abstract void toTeamSelected();

  protected void onTransmit(ChatMessageBean messageBean) {
    forwardMessage = messageBean;
    if (IMKitClient.getConfigCenter().getTeamEnable()) {
      ChatBaseForwardSelectDialog dialog = getForwardSelectDialog();
      if (dialog != null) {
        dialog.show(getSupportFragmentManager(), TAG);
      }
    } else {
      toP2PSelected();
    }
  }

  protected void showForwardConfirmDialog(SessionTypeEnum type, ArrayList<String> sessionIds) {}
}
