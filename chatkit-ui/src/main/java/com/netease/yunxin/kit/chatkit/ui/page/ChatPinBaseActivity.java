// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.page;

import static com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant.LIB_TAG;
import static com.netease.yunxin.kit.chatkit.ui.view.input.ActionConstants.PAYLOAD_REFRESH_AUDIO_ANIM;

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
import com.netease.nimlib.sdk.v2.conversation.enums.V2NIMConversationType;
import com.netease.nimlib.sdk.v2.message.enums.V2NIMMessageType;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.model.IMMessageInfo;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.common.ChatUtils;
import com.netease.yunxin.kit.chatkit.ui.common.MessageHelper;
import com.netease.yunxin.kit.chatkit.ui.common.WatchTextMessageDialog;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatPinActivityBinding;
import com.netease.yunxin.kit.chatkit.ui.interfaces.IItemClickListener;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.chatkit.ui.page.adapter.PinMessageAdapter;
import com.netease.yunxin.kit.chatkit.ui.page.viewmodel.ChatPinViewModel;
import com.netease.yunxin.kit.chatkit.ui.view.message.audio.ChatMessageAudioControl;
import com.netease.yunxin.kit.chatkit.utils.ChatKitConstant;
import com.netease.yunxin.kit.common.ui.action.ActionItem;
import com.netease.yunxin.kit.common.ui.activities.BaseActivity;
import com.netease.yunxin.kit.common.ui.dialog.BaseBottomChoiceDialog;
import com.netease.yunxin.kit.common.ui.dialog.BottomChoiceDialog;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.common.utils.NetworkUtils;
import com.netease.yunxin.kit.corekit.im2.model.IMMessageProgress;
import com.netease.yunxin.kit.corekit.im2.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import java.util.ArrayList;

public abstract class ChatPinBaseActivity extends BaseActivity {

  public static final String TAG = "ChatPinBaseActivity";
  protected ChatPinActivityBinding viewBinding;
  protected ChatPinViewModel viewModel;
  protected String mSessionId;
  protected String mSessionName;
  protected V2NIMConversationType mSessionType;
  protected PinMessageAdapter pinAdapter;
  protected ChatMessageBean forwardMessage;

  // 转发Launcher
  protected ActivityResultLauncher<Intent> forwardLauncher;

  // 跳转到聊天页面
  public static final String ACTION_CHECK_PIN = "check_pin";
  // 标记页面弹窗 取消置顶
  public static final String ACTION_CANCEL_PIN = "cancel_pin";
  // 标记页面弹窗 转发消息
  public static final String ACTION_TRANSMIT_PIN = "message_transmit";
  // 标记页面弹窗 复制消息
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
  protected void onStop() {
    super.onStop();
    ChatMessageAudioControl.getInstance().stopAudio();
  }

  // 初始化页面View
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

  // 初始化页面数据
  protected void initData() {
    viewModel = new ViewModelProvider(this).get(ChatPinViewModel.class);
    mSessionId = getIntent().getStringExtra(RouterConstant.KEY_SESSION_ID);
    mSessionName = getIntent().getStringExtra(RouterConstant.KEY_SESSION_NAME);
    mSessionType =
        V2NIMConversationType.typeOfValue(
            getIntent().getIntExtra(RouterConstant.KEY_SESSION_TYPE, -1));

    // 监听标记列表查询结果
    viewModel
        .getMessageLiveData()
        .observe(
            this,
            result -> {
              if (result.getLoadStatus() == LoadStatus.Error
                  && result.getError() != null
                  && result.getError().getCode() == ChatKitConstant.ERROR_CODE_PARAM_INVALID) {
                Toast.makeText(this, R.string.chat_team_error_tip_content, Toast.LENGTH_SHORT)
                    .show();
                finish();
              } else {
                showEmptyView(result.getData() == null || result.getData().size() < 1);
                pinAdapter.setData(result.getData());
              }
            });

    // 监听标记列表移除
    viewModel
        .getRemovePinLiveData()
        .observe(
            this,
            result -> {
              if (ChatMessageAudioControl.getInstance().isPlayingAudio()
                  && TextUtils.equals(
                      result.getData(),
                      ChatMessageAudioControl.getInstance()
                          .getPlayingAudio()
                          .getMessage()
                          .getMessageClientId())) {
                ChatMessageAudioControl.getInstance().stopAudio();
              }
              pinAdapter.removeDataWithClientId(result.getData());
              showEmptyView(pinAdapter.getItemCount() < 1);
            });
    // 监听标记列表添加
    viewModel
        .getAddPinLiveData()
        .observe(
            this,
            result -> {
              pinAdapter.addData(result.getData());
              showEmptyView(pinAdapter.getItemCount() < 1);
            });
    //监听用户信息变更
    viewModel
        .getUserChangeLiveData()
        .observe(
            this,
            result -> {
              if (result.isSuccess() && result.getData() != null) {
                pinAdapter.updateUserList(result.getData());
              }
            });
    // 监听标记列表消息删除
    viewModel
        .getDeleteMessageLiveData()
        .observe(
            this,
            result -> {
              if (result.isSuccess() && result.getData() != null) {
                pinAdapter.removeDataWithClientIds(result.getData());
              }
              showEmptyView(pinAdapter.getItemCount() < 1);
            });

    // 转发Launcher
    forwardLauncher =
        registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
              if (result.getResultCode() != Activity.RESULT_OK || forwardMessage == null) {
                return;
              }
              ALog.d(LIB_TAG, TAG, "forward result");
              Intent data = result.getData();
              if (data != null) {
                ArrayList<String> conversationIds =
                    data.getStringArrayListExtra(RouterConstant.KEY_FORWARD_SELECTED_CONVERSATIONS);
                if (conversationIds != null && !conversationIds.isEmpty()) {
                  showForwardConfirmDialog(conversationIds);
                }
              }
            });

    // 监听附件下载进度
    viewModel.getAttachmentProgressLiveData().observeForever(this::onAttachmentUpdateProgress);

    if (TextUtils.isEmpty(mSessionId)
        || mSessionType == V2NIMConversationType.V2NIM_CONVERSATION_TYPE_UNKNOWN) {
      showEmptyView(true);
      finish();
    } else {
      showEmptyView(false);
      viewModel.init(mSessionId, mSessionType);
      viewModel.getPinMessageList();
    }
  }

  // 显示空页面
  private void showEmptyView(boolean show) {
    if (show) {
      viewBinding.pinEmptyView.setVisibility(View.VISIBLE);
    } else {
      viewBinding.pinEmptyView.setVisibility(View.GONE);
    }
  }

  // 附件下载进度更新
  protected void onAttachmentUpdateProgress(FetchResult<IMMessageProgress> fetchResult) {
    if (fetchResult.isSuccess() && fetchResult.getData() != null) {
      pinAdapter.updateMessageProgress(fetchResult.getData());
    }
  }

  // 获取RecyclerView的ItemDecoration 普通版和娱乐版UI差异
  public RecyclerView.ItemDecoration getItemDecoration() {
    return null;
  }

  // 标记列表消息点击事件监听
  private final IItemClickListener pinClickListener =
      new IItemClickListener<ChatMessageBean>() {
        @Override
        public boolean onMessageClick(View view, int position, ChatMessageBean messageInfo) {
          jumpToChat(messageInfo);
          return true;
        }

        @Override
        public boolean onViewClick(View view, int position, ChatMessageBean messageInfo) {
          if (view.getId() == R.id.iv_more_action) {
            showMoreActionDialog(messageInfo);
          } else {
            clickMsg(messageInfo);
          }
          return true;
        }
      };

  // 标记列表，消息体点击
  private void clickMsg(ChatMessageBean messageInfo) {
    // 文本消息和富文本消息点击进入查看页面
    if (messageInfo.getMessageData().getMessage().getMessageType()
            == V2NIMMessageType.V2NIM_MESSAGE_TYPE_TEXT
        || MessageHelper.isRichTextMsg(messageInfo.getMessageData())) {
      WatchTextMessageDialog.launchDialog(
          getSupportFragmentManager(), TAG, messageInfo.getMessageData(), getPageBackgroundColor());
    } else if (messageInfo.getMessageData().getMessage().getMessageType()
        == V2NIMMessageType.V2NIM_MESSAGE_TYPE_IMAGE) {
      // 图片消息点击查看大图
      ArrayList<IMMessageInfo> messageList = new ArrayList<>();
      messageList.add(messageInfo.getMessageData());
      ChatUtils.watchImage(ChatPinBaseActivity.this, messageInfo.getMessageData(), messageList);
    } else if (messageInfo.getMessageData().getMessage().getMessageType()
        == V2NIMMessageType.V2NIM_MESSAGE_TYPE_VIDEO) {
      // 视频消息点击查看视频
      ChatUtils.watchVideo(ChatPinBaseActivity.this, messageInfo.getMessageData());
    } else if (messageInfo.getMessageData().getMessage().getMessageType()
        == V2NIMMessageType.V2NIM_MESSAGE_TYPE_FILE) {
      // 文件消息点击查看文件,打开系统查看器
      ChatUtils.openFile(ChatPinBaseActivity.this, messageInfo.getMessageData());
    } else if (messageInfo.getMessageData().getMessage().getMessageType()
        == V2NIMMessageType.V2NIM_MESSAGE_TYPE_LOCATION) {
      // 位置消息点击查看地图
      XKitRouter.withKey(RouterConstant.PATH_CHAT_LOCATION_PAGE)
          .withContext(this)
          .withParam(RouterConstant.KEY_MESSAGE, messageInfo.getMessageData().getMessage())
          .withParam(RouterConstant.KEY_LOCATION_PAGE_TYPE, RouterConstant.KEY_LOCATION_TYPE_DETAIL)
          .navigate();
    } else if (messageInfo.getMessageData().getMessage().getMessageType()
        == V2NIMMessageType.V2NIM_MESSAGE_TYPE_AUDIO) {
      // 音频消息点击播放音频
      pinAdapter.updateMessage(messageInfo, PAYLOAD_REFRESH_AUDIO_ANIM);
    } else {
      // 自定义消息点击，实现在子类中
      clickCustomMessage(messageInfo);
    }
  }

  // 跳转到聊天页面，并定位到指定消息
  public void jumpToChat(ChatMessageBean messageInfo) {
    String router = RouterConstant.PATH_CHAT_TEAM_PAGE;
    if (mSessionType == V2NIMConversationType.V2NIM_CONVERSATION_TYPE_P2P) {
      router = RouterConstant.PATH_CHAT_P2P_PAGE;
    }

    XKitRouter.withKey(router)
        .withParam(RouterConstant.KEY_MESSAGE, messageInfo.getMessageData())
        .withParam(RouterConstant.CHAT_KRY, mSessionId)
        .withContext(ChatPinBaseActivity.this)
        .navigate();
    finish();
  }

  //   显示更多操作弹窗
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
                          R.string.chat_network_error_tip,
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
                          R.string.chat_network_error_tip,
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

  // 组装更多操作弹窗按钮
  public ArrayList<ActionItem> assembleActions(ChatMessageBean messageInfo) {
    ArrayList<ActionItem> actions = new ArrayList<>();
    actions.add(
        new ActionItem(ACTION_CANCEL_PIN, 0, R.string.chat_message_action_pin_cancel)
            .setTitleColorResId(R.color.color_333333));
    if (messageInfo.getViewType() == V2NIMMessageType.V2NIM_MESSAGE_TYPE_TEXT.getValue()) {
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

  // 点击自定义消息，子类实现后续操作
  protected void clickCustomMessage(ChatMessageBean messageBean) {}

  // 转发消息，如果配置不支持群，则直接到P2P转发
  protected void onTransmit(ChatMessageBean messageBean) {
    forwardMessage = messageBean;
    goToForwardPage();
  }

  // 跳转到转发页面，子类实现
  protected abstract void goToForwardPage();

  // 获取页面背景颜色，子类可以复写
  protected int getPageBackgroundColor() {
    return R.color.color_eef1f4;
  }

  // 显示转发确认弹窗，子类实现
  protected void showForwardConfirmDialog(ArrayList<String> sessionIds) {}
}
