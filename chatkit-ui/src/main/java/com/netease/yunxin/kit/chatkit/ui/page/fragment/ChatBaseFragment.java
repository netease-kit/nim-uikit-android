// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.page.fragment;

import static androidx.core.content.PermissionChecker.PERMISSION_GRANTED;
import static com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant.LIB_TAG;
import static com.netease.yunxin.kit.chatkit.ui.view.input.ActionConstants.PAYLOAD_REFRESH_AUDIO_ANIM;
import static com.netease.yunxin.kit.corekit.im2.utils.RouterConstant.KEY_FORWARD_SELECTED_CONVERSATIONS;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.netease.nimlib.sdk.v2.V2NIMError;
import com.netease.nimlib.sdk.v2.ai.enums.V2NIMAIModelRoleType;
import com.netease.nimlib.sdk.v2.ai.model.V2NIMAIUser;
import com.netease.nimlib.sdk.v2.ai.params.V2NIMAIModelCallMessage;
import com.netease.nimlib.sdk.v2.auth.enums.V2NIMDataSyncState;
import com.netease.nimlib.sdk.v2.auth.enums.V2NIMDataSyncType;
import com.netease.nimlib.sdk.v2.conversation.enums.V2NIMConversationType;
import com.netease.nimlib.sdk.v2.message.V2NIMMessage;
import com.netease.nimlib.sdk.v2.message.V2NIMMessagePin;
import com.netease.nimlib.sdk.v2.message.V2NIMMessageRefer;
import com.netease.nimlib.sdk.v2.message.attachment.V2NIMMessageImageAttachment;
import com.netease.nimlib.sdk.v2.message.attachment.V2NIMMessageVideoAttachment;
import com.netease.nimlib.sdk.v2.message.config.V2NIMMessageAIConfig;
import com.netease.nimlib.sdk.v2.message.enums.V2NIMMessageAIStatus;
import com.netease.nimlib.sdk.v2.message.enums.V2NIMMessageQueryDirection;
import com.netease.nimlib.sdk.v2.message.enums.V2NIMMessageType;
import com.netease.nimlib.sdk.v2.utils.V2NIMConversationIdUtil;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.IMKitConfigCenter;
import com.netease.yunxin.kit.chatkit.impl.LoginDetailListenerImpl;
import com.netease.yunxin.kit.chatkit.listener.MessageUpdateType;
import com.netease.yunxin.kit.chatkit.manager.AIUserManager;
import com.netease.yunxin.kit.chatkit.map.ChatLocationBean;
import com.netease.yunxin.kit.chatkit.model.IMMessageInfo;
import com.netease.yunxin.kit.chatkit.model.MessagePinInfo;
import com.netease.yunxin.kit.chatkit.repo.ChatRepo;
import com.netease.yunxin.kit.chatkit.ui.ChatKitClient;
import com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant;
import com.netease.yunxin.kit.chatkit.ui.ChatUIConfig;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.builder.IChatViewCustom;
import com.netease.yunxin.kit.chatkit.ui.common.ChatMsgCache;
import com.netease.yunxin.kit.chatkit.ui.common.ChatUserCache;
import com.netease.yunxin.kit.chatkit.ui.common.ChatUtils;
import com.netease.yunxin.kit.chatkit.ui.common.MessageHelper;
import com.netease.yunxin.kit.chatkit.ui.common.ThumbHelper;
import com.netease.yunxin.kit.chatkit.ui.common.WatchTextMessageDialog;
import com.netease.yunxin.kit.chatkit.ui.custom.ChatConfigManager;
import com.netease.yunxin.kit.chatkit.ui.custom.NERTCCallAttachment;
import com.netease.yunxin.kit.chatkit.ui.custom.RichTextAttachment;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatTopMessageLayoutBinding;
import com.netease.yunxin.kit.chatkit.ui.dialog.ChatBaseForwardSelectDialog;
import com.netease.yunxin.kit.chatkit.ui.fun.view.message.viewholder.ChatAudioMessageViewHolder;
import com.netease.yunxin.kit.chatkit.ui.interfaces.IChatView;
import com.netease.yunxin.kit.chatkit.ui.interfaces.IMessageItemClickListener;
import com.netease.yunxin.kit.chatkit.ui.interfaces.IMessageLoadHandler;
import com.netease.yunxin.kit.chatkit.ui.interfaces.IMessageProxy;
import com.netease.yunxin.kit.chatkit.ui.model.AnchorScrollInfo;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.chatkit.ui.model.MessageRevokeInfo;
import com.netease.yunxin.kit.chatkit.ui.model.ait.AtContactsModel;
import com.netease.yunxin.kit.chatkit.ui.page.viewmodel.ChatBaseViewModel;
import com.netease.yunxin.kit.chatkit.ui.page.viewmodel.ChatP2PViewModel;
import com.netease.yunxin.kit.chatkit.ui.page.viewmodel.ChatTeamViewModel;
import com.netease.yunxin.kit.chatkit.ui.view.ait.AitManager;
import com.netease.yunxin.kit.chatkit.ui.view.input.ActionConstants;
import com.netease.yunxin.kit.chatkit.ui.view.message.ChatMessageListView;
import com.netease.yunxin.kit.chatkit.ui.view.message.adapter.ChatMessageAdapter;
import com.netease.yunxin.kit.chatkit.ui.view.message.audio.ChatMessageAudioControl;
import com.netease.yunxin.kit.chatkit.ui.view.popmenu.ChatPopMenu;
import com.netease.yunxin.kit.chatkit.ui.view.popmenu.IChatPopMenuClickListener;
import com.netease.yunxin.kit.chatkit.utils.SendMediaHelper;
import com.netease.yunxin.kit.common.ui.dialog.ChoiceListener;
import com.netease.yunxin.kit.common.ui.dialog.CommonChoiceDialog;
import com.netease.yunxin.kit.common.ui.dialog.TopPopupWindow;
import com.netease.yunxin.kit.common.ui.fragments.BaseFragment;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.common.utils.NetworkUtils;
import com.netease.yunxin.kit.common.utils.PermissionUtils;
import com.netease.yunxin.kit.common.utils.storage.StorageType;
import com.netease.yunxin.kit.common.utils.storage.StorageUtil;
import com.netease.yunxin.kit.corekit.im2.IMKitClient;
import com.netease.yunxin.kit.corekit.im2.extend.FetchCallback;
import com.netease.yunxin.kit.corekit.im2.model.IMMessageProgress;
import com.netease.yunxin.kit.corekit.im2.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** 聊天页面基础Fragment 页面交互、消息相关功能 */
public abstract class ChatBaseFragment extends BaseFragment {

  private static final String LOG_TAG = "ChatBaseFragment";

  //权限请求
  private static final int REQUEST_PERMISSION = 0;
  // 相机权限申请
  private static final int REQUEST_CAMERA_PERMISSION = 1;
  // 视频权限申请
  private static final int REQUEST_VIDEO_PERMISSION = 2;
  // 相册权限申请
  private static final int REQUEST_READ_EXTERNAL_STORAGE_PERMISSION_ALBUM = 3;
  // 文件权限申请
  private static final int REQUEST_READ_EXTERNAL_STORAGE_PERMISSION_FILE = 4;
  // 地理位置权限申请
  private static final int REQUEST_LOCATION_PERMISSION = 5;
  // 音频消息最小长度(单位ms)
  private static final int AUDIO_MESSAGE_MIN_LENGTH = 1000;

  //AI 消息的上下文长度
  private static final int AI_MESSAGE_SIZE = 30;

  //当前申请的权限
  private int currentRequest = 0;

  // 聊天页面ViewModel 消息发送、接受、变更等
  protected ChatBaseViewModel viewModel;
  // @功能管理器
  protected AitManager aitManager;

  // 聊天页面类型单聊或者群聊 默认为单聊
  protected V2NIMConversationType conversationType =
      V2NIMConversationType.V2NIM_CONVERSATION_TYPE_P2P;

  // 聊天对象ID 单聊为对方ID 群聊为群ID
  protected String accountId;

  protected Handler mHandler;

  // 当前要转发的消息
  protected ChatMessageBean forwardMessage;

  protected TopPopupWindow permissionPop;

  // 多媒体文件选择Launcher
  ActivityResultLauncher<PickVisualMediaRequest> pickMediaLauncher;
  // 文件选择Launcher
  protected ActivityResultLauncher<String[]> pickFileLauncher;
  // 拍照图片存储地址
  private String captureTempImagePath = "";
  // 拍照Launcher
  protected ActivityResultLauncher<Uri> takePictureLauncher;
  private String captureTempVideoPath = "";
  // 视频拍摄Launcher
  protected ActivityResultLauncher<Uri> captureVideoLauncher;

  // 转发选择Launcher
  protected ActivityResultLauncher<Intent> forwardLauncher;

  // 系统权限请求Launcher
  protected ActivityResultLauncher<String[]> permissionLauncher;
  // 位置选择Launcher
  protected ActivityResultLauncher<Intent> locationLauncher;
  // 消息拉取观察者
  private Observer<FetchResult<List<ChatMessageBean>>> messageLiveDataObserver;
  // 消息接受观察者
  private Observer<FetchResult<List<ChatMessageBean>>> messageRecLiveDataObserver;
  // 消息状态更新观察者
  private Observer<FetchResult<Pair<MessageUpdateType, List<ChatMessageBean>>>>
      messageUpdateLiveDataObserver;

  // 消息PIN列表变化
  private Observer<FetchResult<List<V2NIMMessagePin>>> pinedMessageLiveDataObserver;

  // 消息发送观察者
  private Observer<FetchResult<ChatMessageBean>> sendLiveDataObserver;
  // 消息撤回观察者
  private Observer<FetchResult<List<MessageRevokeInfo>>> revokeLiveDataObserver;
  // 消息附件上传下载观察者
  private Observer<FetchResult<IMMessageProgress>> attachLiveDataObserver;
  // 用户信息变更观察者
  private Observer<FetchResult<List<String>>> userInfoLiveDataObserver;

  // 消息标记观察者
  private Observer<FetchResult<Map<String, V2NIMMessagePin>>> msgPinLiveDataObserver;
  // 添加消息标记观察者
  private Observer<Pair<String, V2NIMMessagePin>> addPinLiveDataObserver;
  // 移除消息标记观察者
  private Observer<String> removePinLiveDataObserver;
  // 删除消息观察者
  private Observer<FetchResult<List<V2NIMMessageRefer>>> deleteLiveDataObserver;
  // 登录状态变更监听
  private final LoginDetailListenerImpl loginListener =
      new LoginDetailListenerImpl() {
        @Override
        public void onDataSync(
            @Nullable V2NIMDataSyncType type,
            @Nullable V2NIMDataSyncState state,
            @Nullable V2NIMError error) {
          ALog.d(
              LIB_TAG,
              LOG_TAG,
              "onDataSync type:" + type != null
                  ? type.name()
                  : "null" + " state:" + state != null ? state.name() : "");
          if (type == V2NIMDataSyncType.V2NIM_DATA_SYNC_MAIN) {
            if (state == V2NIMDataSyncState.V2NIM_DATA_SYNC_STATE_COMPLETED) {
              updateDataWhenLogin();
              viewModel.getPinedMessageList();
              if (ChatBaseFragment.this.chatView != null
                  && ChatBaseFragment.this.chatView.getMessageListView() != null) {
                if (ChatBaseFragment.this.chatView.getMessageListView().getMessageAdapter() != null
                    && ChatBaseFragment.this
                            .chatView
                            .getMessageListView()
                            .getMessageAdapter()
                            .getItemCount()
                        > 0) {
                  //如果已经有数据了，不再重新拉取
                  return;
                }
                ChatBaseFragment.this.chatView.getMessageListView().clearMessageList();
              }
              initData();
            }
          }
          //群成员数据同步完成后，更新群成员信息
          if (type == V2NIMDataSyncType.V2NIM_DATA_SYNC_TEAM_MEMBER
              && state == V2NIMDataSyncState.V2NIM_DATA_SYNC_STATE_COMPLETED) {
            //            updateMyTeamMember();
          }
        }
      };

  // 消息长按菜单
  protected ChatPopMenu popMenu;
  // 消息UI布局个性化配置接口，页面加载时会调用customizeChatLayout方法
  protected IChatViewCustom chatViewCustom;
  // 消息页面UI的个性化配置接口
  protected ChatUIConfig chatConfig;
  // 消息点击事件
  protected IMessageItemClickListener delegateListener;
  // 消息列表View
  public IChatView chatView;
  //  页面根View
  public View rootView;
  // 转发类型，转发到单聊或者群聊
  public String forwardAction;

  //置顶消息View
  ChatTopMessageLayoutBinding topMessageViewBinding;

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    rootView = initViewAndGetRootView(inflater, container);
    topMessageViewBinding = ChatTopMessageLayoutBinding.inflate(inflater, container, false);
    initView();
    loadConfig();
    NetworkUtils.registerNetworkStatusChangedListener(networkStateListener);
    initCustom();
    return rootView;
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    if (getArguments() != null) {
      initData(getArguments());
    }
    mHandler = new Handler();
    initViewModel();
    handleTopMessage();
    initDataObserver();
    //增加登录监听器，如果客户没有登录，则监听登录成功后，再去拉取数据
    IMKitClient.addLoginDetailListener(loginListener);
    //如果客户已经登录，则直接拉取数据
    if (!TextUtils.isEmpty(IMKitClient.account())) {
      initData();
    }
  }

  private void handleTopMessage() {
    if (IMKitConfigCenter.getEnableTopMessage() && viewModel instanceof ChatTeamViewModel) {
      ((ChatTeamViewModel) viewModel)
          .getTopMessagePermissionLiveData()
          .observeForever(
              s -> {
                if (ChatUtils.havePermissionForTopSticky()) {
                  topMessageViewBinding.ivTopClose.setVisibility(View.VISIBLE);
                } else {
                  topMessageViewBinding.ivTopClose.setVisibility(View.GONE);
                }
              });
      chatView.addTopView(topMessageViewBinding.getRoot());
      topMessageViewBinding.getRoot().setVisibility(View.GONE);
      ((ChatTeamViewModel) viewModel)
          .getTopMessageLiveData()
          .observe(
              getViewLifecycleOwner(),
              topMessage -> {
                if (topMessage != null) {
                  topMessageViewBinding.getRoot().setVisibility(View.VISIBLE);
                  //处理音视频
                  if (topMessage.getMessage().getMessageType()
                          == V2NIMMessageType.V2NIM_MESSAGE_TYPE_IMAGE
                      || topMessage.getMessage().getMessageType()
                          == V2NIMMessageType.V2NIM_MESSAGE_TYPE_VIDEO) {
                    topMessageViewBinding.rlyTopThumb.setVisibility(View.VISIBLE);
                    String thumbUrl;
                    if (topMessage.getMessage().getMessageType()
                        == V2NIMMessageType.V2NIM_MESSAGE_TYPE_VIDEO) {
                      topMessageViewBinding.ivTopVideo.setVisibility(View.VISIBLE);
                      V2NIMMessageVideoAttachment videoAttachment =
                          (V2NIMMessageVideoAttachment) topMessage.getMessage().getAttachment();
                      String videoUrl = videoAttachment.getUrl();
                      thumbUrl = ThumbHelper.makeVideoThumbUrl(videoUrl);
                    } else {
                      topMessageViewBinding.ivTopVideo.setVisibility(View.GONE);
                      V2NIMMessageImageAttachment imageAttachment =
                          (V2NIMMessageImageAttachment) topMessage.getMessage().getAttachment();
                      thumbUrl =
                          ThumbHelper.makeImageThumbUrl(
                              imageAttachment.getUrl(),
                              imageAttachment.getWidth(),
                              imageAttachment.getHeight());
                    }

                    Glide.with(this).load(thumbUrl).into(topMessageViewBinding.ivTopThumb);
                  } else {
                    topMessageViewBinding.rlyTopThumb.setVisibility(View.GONE);
                  }
                  String text =
                      ChatUtils.getEllipsizeMiddleNick(getTopMessageNick(topMessage)) + ":";
                  topMessageViewBinding.tvNickname.setText(text);
                  MessageHelper.identifyFaceExpression(
                      getContext(),
                      topMessageViewBinding.tvTopContent,
                      MessageHelper.getMsgBrief(topMessage, true),
                      ImageSpan.ALIGN_BOTTOM,
                      MessageHelper.SMALL_SCALE);
                  if (ChatUtils.havePermissionForTopSticky()) {
                    topMessageViewBinding.ivTopClose.setVisibility(View.VISIBLE);
                  } else {
                    topMessageViewBinding.ivTopClose.setVisibility(View.GONE);
                  }
                  topMessageViewBinding.ivTopClose.setOnClickListener(
                      v -> {
                        if (!NetworkUtils.isConnected()) {
                          ToastX.showShortToast(R.string.chat_network_error_tip);
                          return;
                        }
                        ((ChatTeamViewModel) viewModel).removeStickyMessage();
                      });

                  topMessageViewBinding
                      .getRoot()
                      .setOnClickListener(
                          view -> {
                            scrollToMessage(topMessage);
                          });

                } else {
                  topMessageViewBinding.getRoot().setVisibility(View.GONE);
                }
              });
      //删除消息则隐藏
      viewModel
          .getDeleteMessageLiveData()
          .observeForever(
              fetchResult -> {
                if (ChatUserCache.getInstance().getTopMessage() != null
                    && fetchResult.getData() != null
                    && !fetchResult.getData().isEmpty()) {
                  for (V2NIMMessageRefer refer : fetchResult.getData()) {
                    if (Objects.equals(
                        refer.getMessageClientId(),
                        ChatUserCache.getInstance()
                            .getTopMessage()
                            .getMessage()
                            .getMessageClientId())) {
                      topMessageViewBinding.getRoot().setVisibility(View.GONE);
                      break;
                    }
                  }
                }
              });
      //撤回消息则隐藏
      viewModel
          .getRevokeMessageLiveData()
          .observeForever(
              fetchResult -> {
                if (ChatUserCache.getInstance().getTopMessage() != null
                    && fetchResult.getData() != null
                    && !fetchResult.getData().isEmpty()) {
                  for (MessageRevokeInfo revokeInfo : fetchResult.getData()) {
                    if (Objects.equals(
                        revokeInfo.getRevokeMessageClientId(),
                        ChatUserCache.getInstance()
                            .getTopMessage()
                            .getMessage()
                            .getMessageClientId())) {
                      topMessageViewBinding.getRoot().setVisibility(View.GONE);
                      break;
                    }
                  }
                }
              });
      //置顶消息昵称
      viewModel
          .getUserChangeLiveData()
          .observeForever(
              fetchResult -> {
                if (ChatUserCache.getInstance().getTopMessage() != null
                    && fetchResult.getData() != null
                    && !fetchResult.getData().isEmpty()) {
                  for (String userId : fetchResult.getData()) {
                    if (Objects.equals(
                        userId,
                        MessageHelper.getRealMessageSenderId(
                            ChatUserCache.getInstance().getTopMessage().getMessage()))) {
                      String text =
                          ChatUtils.getEllipsizeMiddleNick(
                                  getTopMessageNick(ChatUserCache.getInstance().getTopMessage()))
                              + ":";
                      topMessageViewBinding.tvNickname.setText(text);
                      break;
                    }
                  }
                }
              });
      //置顶消息昵称监听群成员变化
      ((ChatTeamViewModel) viewModel)
          .getUserChangeLiveData()
          .observeForever(
              fetchResult -> {
                if (ChatUserCache.getInstance().getTopMessage() != null
                    && fetchResult.getData() != null
                    && !fetchResult.getData().isEmpty()) {
                  for (String userId : fetchResult.getData()) {
                    if (Objects.equals(
                        userId,
                        ChatUserCache.getInstance().getTopMessage().getMessage().getSenderId())) {
                      String text =
                          ChatUtils.getEllipsizeMiddleNick(
                                  getTopMessageNick(ChatUserCache.getInstance().getTopMessage()))
                              + ":";
                      topMessageViewBinding.tvNickname.setText(text);
                      break;
                    }
                  }
                }
              });
    }
  }

  //获取指定消息昵称，优先取缓存
  private String getTopMessageNick(IMMessageInfo messageInfo) {
    if (ChatUserCache.getInstance()
            .getUserInfo(
                MessageHelper.getRealMessageSenderId(messageInfo.getMessage()),
                V2NIMConversationType.V2NIM_CONVERSATION_TYPE_TEAM)
        != null) {
      return MessageHelper.getChatMessageUserNameByAccount(
          MessageHelper.getRealMessageSenderId(messageInfo.getMessage()),
          V2NIMConversationType.V2NIM_CONVERSATION_TYPE_TEAM);
    }
    return messageInfo.getFromUserName();
  }

  /**
   * 滚动到指定消息
   *
   * @param messageInfo 消息信息
   */
  protected void scrollToMessage(IMMessageInfo messageInfo) {
    int position =
        chatView
            .getMessageListView()
            .searchMessagePosition(messageInfo.getMessage().getMessageClientId());
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
      chatView.appendMessage(new ChatMessageBean(messageInfo));
      viewModel.getMessageList(messageInfo.getMessage(), false);
    }
  }

  // 设置消息点击事件
  public void setIMessageItemClickListener(IMessageItemClickListener clickListener) {
    delegateListener = clickListener;
  }

  // 子类实现，初始化页面布局和UI元素
  public abstract View initViewAndGetRootView(
      @NonNull LayoutInflater inflater, @Nullable ViewGroup container);

  // 回复消息的背景Resource
  public Integer getReplayMessageClickPreviewDialogBgRes() {
    return null;
  }

  // 返回用户信息路由，点击用户头像跳转使用
  public String getUserInfoRoutePath() {
    return null;
  }

  protected void initView() {
    ALog.d(LIB_TAG, LOG_TAG, "initView");

    chatView.getMessageListView().setPopActionListener(actionListener);
    chatView.setMessageProxy(messageProxy);
    chatView.setLoadHandler(loadHandler);
    chatView.setMessageReader(message -> viewModel.sendReceipt(message.getMessage()));
    chatView.setItemClickListener(itemClickListener);
    chatView
        .getTitleBar()
        .getRightTextView()
        .setTextColor(getResources().getColor(R.color.color_333333));
    // 权限申请Launcher
    permissionLauncher =
        registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            result -> {
              if (permissionPop != null) {
                permissionPop.dismiss();
              }
              if (result != null) {
                boolean hasPermission = true;
                String permission = "";
                if (currentRequest == REQUEST_CAMERA_PERMISSION) {
                  if (PermissionUtils.hasPermissions(
                      ChatBaseFragment.this.getContext(), Manifest.permission.CAMERA)) {
                    startTakePicture();
                  } else {
                    hasPermission = false;
                    permission = Manifest.permission.CAMERA;
                  }
                } else if (currentRequest == REQUEST_VIDEO_PERMISSION) {
                  if (PermissionUtils.hasPermissions(
                      ChatBaseFragment.this.getContext(), Manifest.permission.CAMERA)) {
                    startCaptureVideo();
                  } else {
                    hasPermission = false;
                    permission = Manifest.permission.CAMERA;
                  }
                } else if (currentRequest == REQUEST_READ_EXTERNAL_STORAGE_PERMISSION_ALBUM) {
                  if (this.checkImageOrFilePermission()) {
                    startPickMedia();
                  } else {
                    hasPermission = false;
                    permission = Manifest.permission.READ_MEDIA_IMAGES;
                  }
                } else if (currentRequest == REQUEST_READ_EXTERNAL_STORAGE_PERMISSION_FILE) {
                  if (this.checkImageOrFilePermission()) {
                    startPickFile();
                  } else {
                    hasPermission = false;
                    permission = Manifest.permission.READ_EXTERNAL_STORAGE;
                  }
                } else if (currentRequest == REQUEST_LOCATION_PERMISSION) {
                  if (PermissionUtils.hasPermissions(
                      ChatBaseFragment.this.getContext(),
                      Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    startLocationPage();
                  } else {
                    LocationManager locationManager =
                        (LocationManager)
                            ChatBaseFragment.this
                                .getContext()
                                .getSystemService(Context.LOCATION_SERVICE);
                    try {
                      int locationMode =
                          Settings.Secure.getInt(
                              ChatBaseFragment.this.getContext().getContentResolver(),
                              Settings.Secure.LOCATION_MODE);
                      // 位置服务关闭无法使用
                      if (locationMode == Settings.Secure.LOCATION_MODE_OFF) {
                        ToastX.showShortToast(
                            getResources().getString(R.string.permission_location_deny_tips));
                        return;
                      }
                    } catch (Exception e) {

                    }
                    hasPermission = false;
                    permission = Manifest.permission.ACCESS_COARSE_LOCATION;
                  }
                }

                if (!hasPermission) {
                  if (shouldShowRequestPermissionRationale(permission)) {
                    if (chatConfig == null
                        || chatConfig.permissionListener == null
                        || !chatConfig.permissionListener.requestPermissionDenied(
                            ChatBaseFragment.this.getActivity(), permission)) {
                      ToastX.showShortToast(
                          getResources().getString(R.string.permission_deny_tips));
                    }
                  } else {
                    if (chatConfig == null
                        || chatConfig.permissionListener == null
                        || !chatConfig.permissionListener.permissionDeniedForever(
                            ChatBaseFragment.this.getActivity(), permission)) {
                      ToastX.showShortToast(getPermissionText(permission));
                    }
                  }
                }
              }
            });
    chatView
        .getMessageListView()
        .addOnScrollListener(
            new RecyclerView.OnScrollListener() {
              @Override
              public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (popMenu != null && popMenu.isShowing()) {
                  popMenu.hide();
                }
              }
            });
  }

  /** 检查是否有本地图片、视频或者文件读取权限 适配 Android不同版本的权限 */
  private boolean checkImageOrFilePermission() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE
        && ContextCompat.checkSelfPermission(
                ChatBaseFragment.this.requireContext(),
                Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
            == PERMISSION_GRANTED) {
      // Android 14及以上部分照片和视频访问权限
      return true;
    } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.TIRAMISU
        && (ContextCompat.checkSelfPermission(
                    ChatBaseFragment.this.requireContext(), Manifest.permission.READ_MEDIA_IMAGES)
                == PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(
                    ChatBaseFragment.this.requireContext(), Manifest.permission.READ_MEDIA_VIDEO)
                == PERMISSION_GRANTED)) {
      // Android 13及以上完整照片和视频访问权限
      return true;
    } else if (ContextCompat.checkSelfPermission(
            ChatBaseFragment.this.requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
        == PERMISSION_GRANTED) {
      // Android 12及以下完整本地读写访问权限
      return true;
    } else {
      // 无本地读写访问权限
      return false;
    }
  }

  // 加载UI的个性化配置
  private void loadConfig() {
    ChatUIConfig config = this.chatConfig;
    if (config == null) {
      config = ChatKitClient.getChatUIConfig();
      this.chatConfig = config;
    }
    if (config != null) {
      chatView.setChatConfig(config);
      if (config.messageProperties != null) {
        int titleBarVisible = config.messageProperties.showTitleBar ? View.VISIBLE : View.GONE;
        chatView.setTitleBarVisible(titleBarVisible);
        int settingVisible =
            config.messageProperties.showTitleBarRightIcon ? View.VISIBLE : View.GONE;
        chatView.getTitleBar().setRightImageViewVisible(settingVisible);
        if (config.messageProperties.titleBarRightRes != null) {
          chatView
              .getTitleBar()
              .getRightImageView()
              .setImageResource(config.messageProperties.titleBarRightRes);
        }
        if (config.messageProperties.titleBarRightClick != null) {
          chatView.getTitleBar().setActionListener(config.messageProperties.titleBarRightClick);
        }

        if (config.messageProperties.chatViewBackground != null) {
          chatView.setMessageBackground(config.messageProperties.chatViewBackground);
        }
      }

      if (config.chatFactory != null) {
        chatView.setMessageViewHolderFactory(config.chatFactory);
      }

      if (config.chatViewCustom != null) {
        chatView.setLayoutCustom(config.chatViewCustom);
      }

      if (config.chatPopMenu != null) {
        chatView.getMessageListView().setChatPopMenu(config.chatPopMenu);
      }

      delegateListener = config.messageItemClickListener;
    }
  }

  protected void initCustom() {
    ALog.d(LIB_TAG, LOG_TAG, "initCustom");
    if (chatViewCustom != null) {
      chatView.setLayoutCustom(chatViewCustom);
    }
  }

  protected void checkMultiSelectView() {
    chatView.setMultiSelectEnable(ChatMsgCache.getMessageList().size() > 0);
  }

  @Override
  public void onStop() {
    super.onStop();
    InputMethodManager imm =
        (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
    imm.hideSoftInputFromWindow(rootView.getWindowToken(), 0);
    ChatMessageAudioControl.getInstance().stopAudio();
  }

  private final IMessageProxy messageProxy =
      new IMessageProxy() {
        @Override
        public boolean sendTextMessage(String msg, ChatMessageBean replyMsg) {
          List<String> pushList = null;
          Map<String, Object> extension = null;
          if (TextUtils.isEmpty(msg) || TextUtils.getTrimmedLength(msg) < 1) {
            Toast.makeText(
                    ChatBaseFragment.this.getContext(),
                    R.string.chat_send_null_message_tips,
                    Toast.LENGTH_SHORT)
                .show();
            return false;
          }
          if (aitManager != null) {
            pushList = aitManager.getAitTeamMember();
            if (pushList != null && pushList.size() > 0) {
              extension = new HashMap<>();
              extension.put(ChatKitUIConstant.AIT_REMOTE_EXTENSION_KEY, aitManager.getAitData());
            }
          }
          if (replyMsg == null) {
            viewModel.sendTextMessage(msg, pushList, extension, getAIUser(), getAIMessage());
          } else {
            viewModel.replyTextMessage(
                msg, replyMsg.getMessageData().getMessage(), pushList, extension, getAIUser());
          }
          if (aitManager != null) {
            aitManager.reset();
          }
          return true;
        }

        @Override
        public boolean sendRichTextMessage(String title, String content, ChatMessageBean replyMsg) {
          List<String> pushList = new ArrayList<>();
          Map<String, Object> extension = null;
          if (TextUtils.isEmpty(title) || TextUtils.getTrimmedLength(title) < 1) {
            Toast.makeText(
                    ChatBaseFragment.this.getContext(),
                    R.string.chat_send_null_title_tips,
                    Toast.LENGTH_SHORT)
                .show();
            return false;
          }
          String msgContent = TextUtils.getTrimmedLength(content) < 1 ? null : content;
          if (aitManager != null) {
            pushList = aitManager.getAitTeamMember();
            if (pushList != null && pushList.size() > 0) {
              extension = new HashMap<>();
              extension.put(ChatKitUIConstant.AIT_REMOTE_EXTENSION_KEY, aitManager.getAitData());
            }
          }
          //标题中不允许包含回车
          String replaceTitle = title.replaceAll("\r|\n", "");
          V2NIMMessage sendMsg = MessageHelper.createRichTextMessage(replaceTitle, msgContent);
          pushList = MessageHelper.getTeamMemberPush(pushList);
          if (replyMsg == null) {
            viewModel.sendMessage(sendMsg, pushList, extension, getAIUser(), getAIMessage());
          } else {
            viewModel.replyMessage(
                sendMsg, replyMsg.getMessageData().getMessage(), pushList, extension, getAIUser());
          }
          if (aitManager != null) {
            aitManager.reset();
          }
          return true;
        }

        @Override
        public void pickMedia() {
          String[] permission = new String[] {Manifest.permission.READ_EXTERNAL_STORAGE};

          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            permission =
                new String[] {
                  Manifest.permission.READ_MEDIA_IMAGES,
                  Manifest.permission.READ_MEDIA_VIDEO,
                  Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
                };
            // 根据系统版本判断，如果是Android13则采用Manifest.permission.READ_MEDIA_IMAGES
          } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.TIRAMISU) {
            permission =
                new String[] {
                  Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO
                };
          }
          if (PermissionUtils.hasPermissions(ChatBaseFragment.this.getContext(), permission)) {
            startPickMedia();
          } else {
            requestSystemPermission(
                permission,
                REQUEST_READ_EXTERNAL_STORAGE_PERMISSION_ALBUM,
                R.string.chat_permission_storage_title,
                R.string.chat_permission_storage_content);
          }
        }

        @Override
        public void takePicture() {
          if (PermissionUtils.hasPermissions(
              ChatBaseFragment.this.getContext(), Manifest.permission.CAMERA)) {
            startTakePicture();
          } else {
            requestSystemPermission(
                Manifest.permission.CAMERA,
                REQUEST_CAMERA_PERMISSION,
                R.string.chat_permission_camera_title,
                R.string.chat_permission_camera_content);
          }
        }

        @Override
        public void captureVideo() {
          if (PermissionUtils.hasPermissions(
              ChatBaseFragment.this.getContext(), Manifest.permission.CAMERA)) {
            startCaptureVideo();
          } else {
            requestSystemPermission(
                Manifest.permission.CAMERA,
                REQUEST_VIDEO_PERMISSION,
                R.string.chat_permission_camera_title,
                R.string.chat_permission_camera_content);
          }
        }

        @Override
        public boolean sendFile() {
          String[] permission = new String[] {Manifest.permission.READ_EXTERNAL_STORAGE};
          // 根据系统版本判断，如果是Android13则采用Manifest.permission.READ_MEDIA_IMAGES
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permission =
                new String[] {
                  Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO
                };
          }
          if (PermissionUtils.hasPermissions(ChatBaseFragment.this.getContext(), permission)) {
            startPickFile();
          } else {
            requestSystemPermission(
                permission,
                REQUEST_READ_EXTERNAL_STORAGE_PERMISSION_FILE,
                R.string.chat_permission_storage_title,
                R.string.chat_permission_storage_content);
          }
          return true;
        }

        @Override
        public boolean sendAudio(File audioFile, int audioLength, ChatMessageBean replyMsg) {
          // audio not support reply
          if (audioLength < AUDIO_MESSAGE_MIN_LENGTH) {
            ToastX.showShortToast(R.string.chat_message_audio_to_short);
          } else {
            viewModel.sendAudioMessage(audioFile, audioLength);
          }
          return true;
        }

        @Override
        public boolean sendCustomMessage(Map<String, Object> attachment, String content) {
          viewModel.sendCustomMessage(attachment, content);
          return true;
        }

        @Override
        public void onTypeStateChange(boolean isTyping) {
          if (conversationType == V2NIMConversationType.V2NIM_CONVERSATION_TYPE_P2P
              && viewModel instanceof ChatP2PViewModel) {
            ((ChatP2PViewModel) viewModel).sendInputNotification(isTyping);
          }
        }

        @Override
        public boolean hasPermission(String[] permission) {
          if (permission == null || permission.length < 1) {
            return false;
          }
          if (PermissionUtils.hasPermissions(ChatBaseFragment.this.getContext(), permission)) {
            return true;
          } else {
            int titleRes = R.string.chat_permission_storage_title;
            int contentRes = R.string.chat_permission_storage_content;
            for (String s : permission) {
              if (s.equals(Manifest.permission.RECORD_AUDIO)) {
                titleRes = R.string.chat_permission_audio_title;
                contentRes = R.string.chat_permission_audio_content;
                break;
              } else if (s.equals(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                titleRes = R.string.chat_permission_location_title;
                contentRes = R.string.chat_permission_location_content;
                break;
              }
            }
            requestSystemPermission(permission, REQUEST_PERMISSION, titleRes, contentRes);
            return false;
          }
        }

        @Override
        public void onCustomAction(View view, String action) {
          if (chatConfig != null && chatConfig.chatInputMenu != null) {
            chatConfig.chatInputMenu.onCustomInputClick(getContext(), view, action);
          }
        }

        @Override
        public void sendLocationLaunch() {
          String[] permissions = {
            Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION
          };
          if (PermissionUtils.hasPermissions(ChatBaseFragment.this.getContext(), permissions)) {
            startLocationPage();
          } else {
            requestSystemPermission(
                permissions,
                REQUEST_LOCATION_PERMISSION,
                R.string.chat_permission_storage_title,
                R.string.chat_permission_storage_content);
          }
        }

        @Override
        public boolean onActionClick(View view, String action) {
          if (chatConfig != null && chatConfig.chatInputMenu != null) {
            return chatConfig.chatInputMenu.onInputClick(getContext(), view, action);
          }
          return false;
        }

        @Override
        public boolean onMultiActionClick(View view, String action) {
          if (!NetworkUtils.isConnected()) {
            Toast.makeText(getContext(), R.string.chat_network_error_tip, Toast.LENGTH_SHORT)
                .show();
            return true;
          }
          if (TextUtils.equals(action, ActionConstants.ACTION_TYPE_MULTI_FORWARD)) {
            onMultiForward();
          } else if (TextUtils.equals(action, ActionConstants.ACTION_TYPE_SINGLE_FORWARD)) {
            onSingleForward();
          } else if (TextUtils.equals(action, ActionConstants.ACTION_TYPE_MULTI_DELETE)) {
            onMultiDelete();
          }
          return true;
        }

        @Override
        public void videoCall() {
          startCall(2);
        }

        @Override
        public void audioCall() {
          startCall(1);
        }

        @Override
        public void onTranslateAction() {}

        @Override
        public String getConversationId() {
          return V2NIMConversationIdUtil.conversationId(accountId, conversationType);
        }

        @Override
        public V2NIMConversationType getConversationType() {
          return conversationType;
        }
      };

  /**
   * 获取AI用户
   *
   * @return AI数字人
   */
  protected V2NIMAIUser getAIUser() {
    if (AIUserManager.getAIUserById(accountId) != null) {
      return AIUserManager.getAIUserById(accountId);
    }
    if (aitManager == null) {
      return null;
    }
    String accId = aitManager.getFirstAIMember();
    if (!TextUtils.isEmpty(accId)) {
      return AIUserManager.getAIUserById(accId);
    }
    return null;
  }

  /**
   * 获取AI聊的上下文
   *
   * @return 上下文
   */
  protected List<V2NIMAIModelCallMessage> getAIMessage() {
    //上下文
    List<V2NIMAIModelCallMessage> aiMessages = new ArrayList<>();
    if (AIUserManager.getAIUserById(accountId) != null) {
      int size = Math.min(chatView.getMessageList().size(), AI_MESSAGE_SIZE);
      //第一条消息不能是数字人消息
      // 标记是否已经设置过第一条消息
      boolean firstSet = false;
      for (int i = size; i > 0; i--) {
        int index = chatView.getMessageList().size() - i;
        ChatMessageBean chatMessageBean = chatView.getMessageList().get(index);
        boolean isFromAIUser =
            AIUserManager.isAIUser(chatMessageBean.getMessageData().getMessage().getSenderId());
        //1 如果第一条是数字人消息，则不再添加
        //2 如果消息已经撤回，则不再添加
        //3 如果消息没有服务器ID，说明不是发出去的消息，则不再添加
        if ((!firstSet && isFromAIUser)
            || chatMessageBean.isRevoked()
            || TextUtils.isEmpty(
                chatMessageBean.getMessageData().getMessage().getMessageServerId())) {
          continue;
        }
        firstSet = true;
        if (!TextUtils.isEmpty(
            MessageHelper.getAIContentMsg(chatMessageBean.getMessageData().getMessage()))) {
          aiMessages.add(
              new V2NIMAIModelCallMessage(
                  isFromAIUser
                      ? V2NIMAIModelRoleType.V2NIM_AI_MODEL_ROLE_TYPE_ASSISTANT
                      : V2NIMAIModelRoleType.V2NIM_AI_MODEL_ROLE_TYPE_USER,
                  MessageHelper.getAIContentMsg(chatMessageBean.getMessageData().getMessage()),
                  V2NIMMessageType.V2NIM_MESSAGE_TYPE_TEXT.getValue()));
        }
      }
    }
    return aiMessages;
  }

  private void requestSystemPermission(
      String permission, int request, int permissionTitleRes, int permissionContentRes) {
    currentRequest = request;
    if (chatConfig != null && chatConfig.permissionListener != null) {
      chatConfig.permissionListener.onPermissionRequest(
          this.getActivity(), new String[] {permission});
    }
    if (chatConfig == null
        || chatConfig.showPermissionPop == null
        || chatConfig.showPermissionPop == Boolean.TRUE) {
      permissionPop =
          new TopPopupWindow(
              ChatBaseFragment.this.requireContext(), permissionTitleRes, permissionContentRes);
      permissionPop.showAtLocation(
          ChatBaseFragment.this.chatView.getMessageListView(), Gravity.TOP, 0, 100);
    }

    permissionLauncher.launch(new String[] {permission});
  }

  private void requestSystemPermission(
      String[] permission, int request, int permissionTitleRes, int permissionContentRes) {
    currentRequest = request;
    if (chatConfig != null && chatConfig.permissionListener != null) {
      chatConfig.permissionListener.onPermissionRequest(this.getActivity(), permission);
    }
    if (chatConfig == null
        || chatConfig.showPermissionPop == null
        || chatConfig.showPermissionPop == Boolean.TRUE) {

      permissionPop =
          new TopPopupWindow(
              ChatBaseFragment.this.requireContext(), permissionTitleRes, permissionContentRes);
      permissionPop.showAtLocation(
          ChatBaseFragment.this.chatView.getMessageListView(), Gravity.TOP, 0, 100);
    }
    permissionLauncher.launch(permission);
  }

  public String getPermissionText(String permission) {
    String text = this.requireContext().getString(R.string.permission_default);
    if (TextUtils.equals(permission, Manifest.permission.CAMERA)) {
      text = this.requireContext().getString(R.string.permission_camera);
    } else if (TextUtils.equals(permission, Manifest.permission.READ_EXTERNAL_STORAGE)) {
      text = this.requireContext().getString(R.string.permission_storage);
    } else if (TextUtils.equals(permission, Manifest.permission.RECORD_AUDIO)) {
      text = this.requireContext().getString(R.string.permission_audio);
    }
    return text;
  }

  protected void startTakePicture() {
    File tempImageFile = null;
    try {
      tempImageFile = SendMediaHelper.createImageFile();
      captureTempImagePath = tempImageFile.getAbsolutePath();
    } catch (IOException e) {
      e.printStackTrace();
    }
    if (tempImageFile != null) {
      Uri pictureUri = SendMediaHelper.getUriForFile(tempImageFile);
      takePictureLauncher.launch(pictureUri);
    }
  }

  protected void startPickMedia() {
    pickMediaLauncher.launch(
        new PickVisualMediaRequest.Builder()
            .setMediaType(ActivityResultContracts.PickVisualMedia.ImageAndVideo.INSTANCE)
            .build());
  }

  protected void startPickFile() {
    pickFileLauncher.launch(new String[] {"*/*"});
  }

  protected void startLocationPage() {
    XKitRouter.withKey(RouterConstant.PATH_CHAT_LOCATION_PAGE)
        .withContext(requireContext())
        .navigate(locationLauncher);
  }

  protected void startCaptureVideo() {
    if (!StorageUtil.hasEnoughSpaceForWrite(StorageType.TYPE_VIDEO)) {
      return;
    }
    File tempVideoFile = null;
    try {
      tempVideoFile = SendMediaHelper.createVideoFile();
      captureTempVideoPath = tempVideoFile.getAbsolutePath();
    } catch (IOException e) {
      e.printStackTrace();
    }
    if (tempVideoFile != null) {
      Uri videoUri = SendMediaHelper.getUriForFile(tempVideoFile);
      captureVideoLauncher.launch(videoUri);
    }
  }

  protected void checkAudioPlayAndStop(V2NIMMessageRefer message) {
    if (message != null
        && ChatMessageAudioControl.getInstance().isPlayingAudio()
        && message.getMessageClientId()
            == ChatMessageAudioControl.getInstance()
                .getPlayingAudio()
                .getMessage()
                .getMessageClientId()) {
      ChatMessageAudioControl.getInstance().stopAudio();
    }
  }

  private final IMessageItemClickListener itemClickListener =
      new IMessageItemClickListener() {
        @Override
        public boolean onMessageLongClick(View view, int position, ChatMessageBean messageBean) {
          if (delegateListener == null
              || !delegateListener.onMessageLongClick(view, position, messageBean)) {
            if (messageBean.isRevoked()) {
              return false;
            }
            // show pop menu
            if (popMenu != null && popMenu.isShowing()) {
              popMenu.hide();
            }
            popMenu = new ChatPopMenu();
            //            popMenu.setDismissListener(() -> SelectableTextHelper.getInstance().dismiss());
            int[] location = new int[2];
            chatView.getMessageListView().getLocationOnScreen(location);
            popMenu.show(getContext(), view, messageBean, location[1]);
          }
          return true;
        }

        @Override
        public boolean onMessageClick(View view, int position, ChatMessageBean messageBean) {
          if (delegateListener == null
              || !delegateListener.onMessageClick(view, position, messageBean)) {
            clickMessage(messageBean.getMessageData(), false);
          }

          return true;
        }

        @Override
        public boolean onMessageSelect(
            View view, int position, ChatMessageBean messageInfo, boolean selected) {
          if (selected) {
            ChatMsgCache.addMessage(messageInfo);
          } else {
            ChatMsgCache.removeMessage(
                messageInfo.getMessageData().getMessage().getMessageClientId());
          }
          checkMultiSelectView();
          return true;
        }

        @Override
        public boolean onUserIconClick(View view, int position, ChatMessageBean messageBean) {
          if (delegateListener == null
              || !delegateListener.onUserIconClick(view, position, messageBean)) {
            String userId = messageBean.getMessageData().getMessage().getSenderId();
            //如果是AI消息，取AI的ID
            if (MessageHelper.isReceivedMessageFromAi(messageBean.getMessageData().getMessage())) {
              userId = messageBean.getMessageData().getMessage().getAIConfig().getAccountId();
            }
            XKitRouter.withKey(getUserInfoRoutePath())
                .withContext(view.getContext())
                .withParam(RouterConstant.KEY_ACCOUNT_ID_KEY, userId)
                .navigate();
          }
          return true;
        }

        @Override
        public boolean onSelfIconClick(View view, int position, ChatMessageBean messageBean) {
          if (delegateListener == null
              || !delegateListener.onSelfIconClick(view, position, messageBean)) {
            XKitRouter.withKey(RouterConstant.PATH_MINE_INFO_PAGE)
                .withContext(view.getContext())
                .navigate();
          }
          return true;
        }

        @Override
        public boolean onUserIconLongClick(View view, int position, ChatMessageBean messageBean) {
          if (delegateListener == null
              || !delegateListener.onUserIconLongClick(view, position, messageBean)) {
            String account = getMessageSendId(messageBean.getMessageData().getMessage());
            if (aitManager != null) {
              if (conversationType == V2NIMConversationType.V2NIM_CONVERSATION_TYPE_TEAM) {
                if (!TextUtils.equals(account, IMKitClient.account())) {
                  String name = MessageHelper.getTeamAtName(account);
                  aitManager.insertReplyAit(account, name);
                }
              }
            }
          }
          return true;
        }

        @Override
        public boolean onSelfIconLongClick(View view, int position, ChatMessageBean messageInfo) {
          return (delegateListener != null
              && delegateListener.onSelfIconLongClick(view, position, messageInfo));
        }

        @Override
        public boolean onReeditRevokeMessage(View view, int position, ChatMessageBean messageBean) {
          // only support text message
          if (delegateListener == null
              || !delegateListener.onReeditRevokeMessage(view, position, messageBean)) {
            if (messageBean != null && MessageHelper.revokeMsgIsEdit(messageBean)) {
              AtContactsModel aitModel =
                  MessageHelper.getAitBlockFromMsg(messageBean.getMessageData().getMessage());
              if (aitModel != null) {
                aitManager.reset();
                aitManager.setAitContactsModel(aitModel);
              }
              Map<String, String> richMap =
                  MessageHelper.getRichMessageRevokeContent(messageBean.getMessageData());
              if (MessageHelper.isRichTextMsg(messageBean.getMessageData())) {
                RichTextAttachment attachment =
                    (RichTextAttachment) messageBean.getMessageData().getMessage().getAttachment();
                chatView.setReeditRichMessage(attachment.title, attachment.body);

              } else if (richMap != null) {
                String title = richMap.get(ChatKitUIConstant.KEY_RICH_TEXT_TITLE);
                String body = richMap.get(ChatKitUIConstant.KEY_RICH_TEXT_BODY);
                chatView.setReeditRichMessage(title, body);
              } else {
                String revokeContent =
                    MessageHelper.getMessageRevokeContent(messageBean.getMessageData());
                if (TextUtils.isEmpty(revokeContent)) {
                  revokeContent = messageBean.getMessageData().getMessage().getText();
                }
                chatView.setReeditMessage(revokeContent);
              }
              if (messageBean.hasReply()) {
                loadReplyInfo(messageBean.getReplyMessageRefer(), false);
              }

            } else {
              Toast.makeText(
                      ChatBaseFragment.this.getContext(),
                      R.string.chat_message_revoke_eidt_error,
                      Toast.LENGTH_SHORT)
                  .show();
              chatView
                  .getMessageListView()
                  .updateMessage(messageBean, ActionConstants.PAYLOAD_REVOKE_STATUS);
            }
          }
          return true;
        }

        @Override
        public boolean onReplyMessageClick(View view, int position, IMMessageInfo messageInfo) {
          // scroll to the message position
          if (delegateListener == null
              || !delegateListener.onReplyMessageClick(view, position, messageInfo)) {
            if (messageInfo != null
                && (messageInfo.getMessage().getMessageType()
                        == V2NIMMessageType.V2NIM_MESSAGE_TYPE_TEXT
                    || MessageHelper.isRichTextMsg(messageInfo))) {
              WatchTextMessageDialog.launchDialog(
                  getParentFragmentManager(),
                  "",
                  messageInfo,
                  getReplayMessageClickPreviewDialogBgRes());
            } else {
              clickMessage(messageInfo, true);
            }
          }
          return true;
        }

        @Override
        public boolean onSendFailBtnClick(View view, int position, ChatMessageBean messageBean) {
          if (delegateListener == null
              || !delegateListener.onSendFailBtnClick(view, position, messageBean)) {
            viewModel.sendMessageStrExtension(
                messageBean.getMessageData().getMessage(),
                V2NIMConversationIdUtil.conversationId(accountId, conversationType),
                messageBean.getMessageData().getMessage().getPushConfig().getForcePushAccountIds(),
                messageBean.getMessageData().getMessage().getServerExtension());
          }
          return true;
        }

        @Override
        public boolean onTextSelected(
            View view,
            int position,
            ChatMessageBean messageInfo,
            String text,
            boolean isSelectAll) {
          if (delegateListener == null
              || delegateListener.onTextSelected(view, position, messageInfo, text, isSelectAll)) {
            if (messageInfo.isRevoked()) {
              return false;
            }
            ALog.d(LIB_TAG, LOG_TAG, "onTextSelected: " + text);
            // show pop menu
            if (popMenu == null) {
              popMenu = new ChatPopMenu();
            }
            //            popMenu.setDismissListener(() -> SelectableTextHelper.getInstance().dismiss());
            int[] location = new int[2];
            chatView.getMessageListView().getLocationOnScreen(location);
            if (isSelectAll) {
              popMenu.show(getContext(), view, messageInfo, location[1]);
            } else {
              popMenu.show(
                  getContext(),
                  view,
                  text,
                  messageInfo.getMessageData().getMessage().isSelf(),
                  location[1]);
            }
          }
          return true;
        }

        @Override
        public boolean onCustomClick(View view, int position, ChatMessageBean messageInfo) {
          return (delegateListener != null
              && delegateListener.onCustomClick(view, position, messageInfo));
        }
      };

  /**
   * 获取消息发送者ID
   *
   * @param message 消息
   * @return 发送者ID，可能是ai
   */
  private String getMessageSendId(V2NIMMessage message) {
    V2NIMMessageAIConfig aiConfig = message.getAIConfig();
    if (aiConfig != null
        && aiConfig.getAIStatus() == V2NIMMessageAIStatus.V2NIM_MESSAGE_AI_STATUS_RESPONSE) {
      return aiConfig.getAccountId();
    }
    return message.getSenderId();
  }

  protected void loadReplyInfo(V2NIMMessageRefer refer, boolean addAit) {
    if (refer == null) {
      return;
    }
    List<V2NIMMessageRefer> refers = new ArrayList<>();
    refers.add(refer);
    ChatRepo.getMessageListByRefers(
        refers,
        new FetchCallback<List<IMMessageInfo>>() {
          @Override
          public void onError(int errorCode, @Nullable String errorMsg) {}

          @Override
          public void onSuccess(@Nullable List<IMMessageInfo> data) {
            if (data != null && !data.isEmpty()) {
              loadReplyView(data.get(0), addAit);
            }
          }
        });
  }

  protected void clickMessage(IMMessageInfo messageInfo, boolean isReply) {
    if (messageInfo == null) {
      return;
    }
    if (chatView.isMultiSelect()) {
      return;
    }
    if (messageInfo.getMessage().getMessageType() == V2NIMMessageType.V2NIM_MESSAGE_TYPE_IMAGE) {
      ArrayList<IMMessageInfo> messageInfoList = new ArrayList<>();
      if (!isReply) {
        List<ChatMessageBean> filterList =
            chatView
                .getMessageListView()
                .filterMessagesByType(messageInfo.getMessage().getMessageType().getValue());
        for (ChatMessageBean messageBean : filterList) {
          messageInfoList.add(messageBean.getMessageData());
        }
      } else {
        messageInfoList.add(messageInfo);
      }
      ChatUtils.watchImage(this.getContext(), messageInfo, messageInfoList);
    } else if (messageInfo.getMessage().getMessageType()
        == V2NIMMessageType.V2NIM_MESSAGE_TYPE_VIDEO) {
      boolean isOpen = ChatUtils.watchVideo(getContext(), messageInfo);
      if (!isOpen && isReply) {
        chatView
            .getMessageListView()
            .scrollToMessage(messageInfo.getMessage().getMessageClientId());
      }
    } else if (messageInfo.getMessage().getMessageType()
        == V2NIMMessageType.V2NIM_MESSAGE_TYPE_LOCATION) {
      XKitRouter.withKey(RouterConstant.PATH_CHAT_LOCATION_PAGE)
          .withContext(requireContext())
          .withParam(RouterConstant.KEY_MESSAGE, messageInfo.getMessage())
          .withParam(RouterConstant.KEY_LOCATION_PAGE_TYPE, RouterConstant.KEY_LOCATION_TYPE_DETAIL)
          .navigate();
    } else if (messageInfo.getMessage().getMessageType()
        == V2NIMMessageType.V2NIM_MESSAGE_TYPE_FILE) {
      boolean isOpen = ChatUtils.openFile(getContext(), messageInfo);
      if (!isOpen && isReply) {
        chatView
            .getMessageListView()
            .scrollToMessage(messageInfo.getMessage().getMessageClientId());
      }
    } else if (messageInfo.getMessage().getMessageType()
        == V2NIMMessageType.V2NIM_MESSAGE_TYPE_CALL) {
      if (!NetworkUtils.isConnected()) {
        Toast.makeText(getContext(), R.string.chat_network_error_tip, Toast.LENGTH_SHORT).show();
        return;
      }
      //解决取消的消息首次点击不能呼叫的问题
      NERTCCallAttachment attachment = (NERTCCallAttachment) messageInfo.getCustomData();
      if (attachment == null && messageInfo.getMessage().getAttachment() != null) {
        // 此处只处理话单消息
        String attachmentRaw = messageInfo.getMessage().getAttachment().getRaw();
        if (TextUtils.isEmpty(attachmentRaw)) {
          return;
        }
        attachment =
            new NERTCCallAttachment(messageInfo.getMessage().getMessageClientId(), attachmentRaw);
        messageInfo.setCustomData(attachment);
      }
      if (attachment != null) {
        startCall(attachment.callType);
      }
    } else if (messageInfo.getMessage().getMessageType()
        == V2NIMMessageType.V2NIM_MESSAGE_TYPE_AUDIO) {
      if (isReply) {
        ChatMessageAudioControl.getInstance()
            .startPlayAudioDelay(
                ChatAudioMessageViewHolder.CLICK_TO_PLAY_AUDIO_DELAY, messageInfo, null);

      } else {
        ChatMessageListView messageListView = chatView.getMessageListView();
        if (messageListView == null) {
          return;
        }
        int position =
            messageListView.searchMessagePosition(messageInfo.getMessage().getMessageClientId());
        ChatMessageAdapter adapter = messageListView.getMessageAdapter();
        if (adapter == null || position < 0) {
          return;
        }
        adapter.notifyItemChanged(position, PAYLOAD_REFRESH_AUDIO_ANIM);
      }
    } else {
      if (isReply) {
        chatView
            .getMessageListView()
            .scrollToMessage(messageInfo.getMessage().getMessageClientId());
      }
    }
  }

  protected void startCall(int type) {
    if (conversationType == V2NIMConversationType.V2NIM_CONVERSATION_TYPE_P2P
        && viewModel instanceof ChatP2PViewModel) {
      if (IMKitConfigCenter.getEnableOnlyFriendCall()
          && !((ChatP2PViewModel) viewModel).isFriend(accountId)) {
        ((ChatP2PViewModel) viewModel).saveNotFriendCallTips();
      } else {
        if (type == 1) {
          ChatUtils.startAudioCall(ChatBaseFragment.this.getContext(), accountId);
        } else {
          ChatUtils.startVideoCall(ChatBaseFragment.this.getContext(), accountId);
        }
      }
    }
  }

  protected void loadReplyView(IMMessageInfo messageInfo, boolean addAit) {
    if (aitManager != null
        && conversationType == V2NIMConversationType.V2NIM_CONVERSATION_TYPE_TEAM
        && addAit) {
      String account = MessageHelper.getRealMessageSenderId(messageInfo.getMessage());
      if (!TextUtils.equals(account, IMKitClient.account())) {
        String name = MessageHelper.getTeamAtName(account);
        aitManager.insertReplyAit(account, name);
      }
    }
    chatView.setReplyMessage(new ChatMessageBean(messageInfo));
  }

  private final IMessageLoadHandler loadHandler =
      new IMessageLoadHandler() {
        @Override
        public void loadMoreForward(ChatMessageBean messageBean) {
          viewModel.fetchMoreMessage(
              messageBean.getMessageData().getMessage(),
              V2NIMMessageQueryDirection.V2NIM_QUERY_DIRECTION_DESC,
              false);
        }

        @Override
        public void loadMoreBackground(ChatMessageBean messageBean) {
          viewModel.fetchMoreMessage(
              messageBean.getMessageData().getMessage(),
              V2NIMMessageQueryDirection.V2NIM_QUERY_DIRECTION_ASC,
              true);
        }

        @Override
        public void onVisibleItemChange(List<ChatMessageBean> messages) {
          if (conversationType == V2NIMConversationType.V2NIM_CONVERSATION_TYPE_TEAM
              && viewModel instanceof ChatTeamViewModel) {
            ((ChatTeamViewModel) viewModel).refreshTeamMessageReceipt(messages);
          }
        }
      };

  private final IChatPopMenuClickListener actionListener =
      new IChatPopMenuClickListener() {
        @Override
        public boolean onCopy(String content) {
          if (chatConfig != null
              && chatConfig.popMenuClickListener != null
              && chatConfig.popMenuClickListener.onCopy(content)) {
            return true;
          }
          MessageHelper.copyText(content, true);
          return true;
        }

        @Override
        public boolean onReply(ChatMessageBean messageBean) {
          if (chatConfig != null
              && chatConfig.popMenuClickListener != null
              && chatConfig.popMenuClickListener.onReply(messageBean)) {
            return true;
          }
          loadReplyView(messageBean.getMessageData(), true);
          return true;
        }

        @Override
        public boolean onForward(ChatMessageBean messageBean) {
          if (chatConfig != null
              && chatConfig.popMenuClickListener != null
              && chatConfig.popMenuClickListener.onForward(messageBean)) {
            return true;
          }
          forwardMessage = messageBean;
          onStartForward(ActionConstants.POP_ACTION_TRANSMIT);
          return true;
        }

        @Override
        public boolean onSignal(ChatMessageBean messageBean, boolean cancel) {
          if (chatConfig != null
              && chatConfig.popMenuClickListener != null
              && chatConfig.popMenuClickListener.onSignal(messageBean, cancel)) {
            return true;
          }
          if (cancel) {
            viewModel.removeMsgPin(messageBean.getMessageData());
          } else {
            viewModel.addMessagePin(messageBean.getMessageData(), "");
          }
          return true;
        }

        @Override
        public boolean onMultiSelected(ChatMessageBean messageBean) {
          if (chatConfig != null
              && chatConfig.popMenuClickListener != null
              && chatConfig.popMenuClickListener.onMultiSelected(messageBean)) {
            return true;
          }
          chatView.showMultiSelect(true);
          ChatMsgCache.addMessage(messageBean);
          chatView.getMessageListView().setMultiSelect(true);
          checkMultiSelectView();
          return true;
        }

        @Override
        public boolean onCollection(ChatMessageBean messageBean) {
          if (chatConfig != null
              && chatConfig.popMenuClickListener != null
              && chatConfig.popMenuClickListener.onCollection(messageBean)) {
            return true;
          }
          viewModel.addMsgCollection(getConversationName(), messageBean.getMessageData());
          return true;
        }

        @Override
        public boolean onTopSticky(ChatMessageBean messageInfo, boolean isAdd) {
          if (chatConfig != null
              && chatConfig.popMenuClickListener != null
              && chatConfig.popMenuClickListener.onTopSticky(messageInfo, isAdd)) {
            return true;
          }
          if (!NetworkUtils.isConnected()) {
            ToastX.showShortToast(R.string.chat_network_error_tip);
            return true;
          }
          if (!ChatUtils.havePermissionForTopSticky()) {
            ToastX.showShortToast(R.string.chat_kit_no_permission_error);
            return true;
          }

          if (viewModel instanceof ChatTeamViewModel) {
            if (isAdd) {
              ((ChatTeamViewModel) viewModel)
                  .addStickyMessage(messageInfo.getMessageData().getMessage());
            } else {
              ((ChatTeamViewModel) viewModel).removeStickyMessage();
            }
          }
          return true;
        }

        @Override
        public boolean onDelete(ChatMessageBean message) {
          if (chatConfig != null
              && chatConfig.popMenuClickListener != null
              && chatConfig.popMenuClickListener.onDelete(message)) {
            return true;
          }
          showDeleteConfirmDialog(Collections.singletonList(message));
          return true;
        }

        @Override
        public boolean onRecall(ChatMessageBean messageBean) {
          if (chatConfig != null
              && chatConfig.popMenuClickListener != null
              && chatConfig.popMenuClickListener.onRecall(messageBean)) {
            return true;
          }
          long revokeTime = 0;
          if (ChatKitClient.getChatUIConfig() != null
              && ChatKitClient.getChatUIConfig().revokeTimeGap != null) {
            // 将分钟转换为毫秒
            long timeGap = ChatKitClient.getChatUIConfig().revokeTimeGap;
            if (timeGap < ChatKitUIConstant.MESSAGE_REVOKE_TIME_MIN) {
              timeGap = ChatKitUIConstant.MESSAGE_REVOKE_TIME_MIN;
            } else if (timeGap > ChatKitUIConstant.MESSAGE_REVOKE_TIME_MAX) {
              timeGap = ChatKitUIConstant.MESSAGE_REVOKE_TIME_MAX;
            }
            revokeTime = timeGap * 6000;
          }
          if (revokeTime <= 0
              || System.currentTimeMillis()
                      - messageBean.getMessageData().getMessage().getCreateTime()
                  < revokeTime) {
            showRevokeConfirmDialog(messageBean);
          } else {
            Toast.makeText(
                    ChatBaseFragment.this.getContext(),
                    R.string.chat_message_revoke_over_time,
                    Toast.LENGTH_SHORT)
                .show();
          }
          return true;
        }

        @Override
        public boolean onTransferToText(ChatMessageBean messageBean) {
          if (chatConfig != null
              && chatConfig.popMenuClickListener != null
              && chatConfig.popMenuClickListener.onRecall(messageBean)) {
            return true;
          }
          viewModel.voiceToText(messageBean);
          return true;
        }

        @Override
        public boolean onCustom(View view, ChatMessageBean messageInfo, String action) {
          return chatConfig != null
              && chatConfig.popMenuClickListener != null
              && chatConfig.popMenuClickListener.onCustom(view, messageInfo, action);
        }
      };

  protected void onStartForward(String action) {
    forwardAction = action;
  }

  private void showDeleteConfirmDialog(List<ChatMessageBean> message) {
    CommonChoiceDialog dialog = new CommonChoiceDialog();
    dialog
        .setTitleStr(getString(R.string.chat_message_action_delete))
        .setContentStr(getString(R.string.chat_message_action_delete_this_message))
        .setPositiveStr(getString(R.string.chat_message_delete))
        .setNegativeStr(getString(R.string.cancel))
        .setConfirmListener(
            new ChoiceListener() {
              @Override
              public void onPositive() {
                if (!NetworkUtils.isConnected()) {
                  ToastX.showShortToast(R.string.chat_network_error_tip);
                  return;
                }
                viewModel.deleteMessage(message);
                clearMessageMultiSelectStatus();
              }

              @Override
              public void onNegative() {}
            })
        .show(getParentFragmentManager());
  }

  private void showRevokeConfirmDialog(ChatMessageBean messageBean) {
    CommonChoiceDialog dialog = new CommonChoiceDialog();
    dialog
        .setTitleStr(getString(R.string.chat_message_action_recall))
        .setContentStr(getString(R.string.chat_message_action_revoke_this_message))
        .setPositiveStr(getString(R.string.chat_message_positive_recall))
        .setNegativeStr(getString(R.string.cancel))
        .setConfirmListener(
            new ChoiceListener() {
              @Override
              public void onPositive() {
                viewModel.revokeMessage(messageBean);
              }

              @Override
              public void onNegative() {}
            })
        .show(getParentFragmentManager());
  }

  protected void clearMessageMultiSelectStatus() {
    chatView.showMultiSelect(false);
    ChatMsgCache.clear();
  }

  protected ChatMessageBean getForwardMessage() {
    if (forwardMessage == null) {
      return null;
    }
    return chatView
        .getMessageListView()
        .searchMessage(forwardMessage.getMessageData().getMessage().getMessageClientId());
  }

  public String getConversationName() {
    return "";
  }

  protected abstract void initViewModel();

  protected abstract ChatBaseForwardSelectDialog getForwardSelectDialog();

  protected abstract void initData();

  //登录状态变化时更新数据
  protected abstract void updateDataWhenLogin();

  protected void initDataObserver() {
    ALog.d(LIB_TAG, LOG_TAG, "initDataObserver");

    // 加载消息数据，首次进入或者加载更多
    messageLiveDataObserver = this::onLoadMessage;
    viewModel.getQueryMessageLiveData().observeForever(messageLiveDataObserver);

    // 接受消息监听
    messageRecLiveDataObserver = this::onReceiveMessage;
    viewModel.getRecMessageLiveData().observeForever(messageRecLiveDataObserver);

    messageUpdateLiveDataObserver = this::onMessageUpdate;
    viewModel.getUpdateMessageLiveData().observeForever(messageUpdateLiveDataObserver);

    pinedMessageLiveDataObserver = this::onPinedMessageListChange;
    viewModel.getPinedMessageListLiveData().observeForever(pinedMessageLiveDataObserver);

    // 发送消息监听，根据发送状态sending添加到消息列表中
    sendLiveDataObserver = this::onSentMessage;
    viewModel.getSendMessageLiveData().observeForever(sendLiveDataObserver);

    // 附件下载监听，文件消息、视频消息等下载进度更新
    attachLiveDataObserver = this::onAttachmentUpdateProgress;
    viewModel.getAttachmentProgressMutableLiveData().observeForever(attachLiveDataObserver);

    // 消息撤回监听，消息撤回（他人消息撤回或本人撤回成功）
    revokeLiveDataObserver = this::onRevokeMessage;
    viewModel.getRevokeMessageLiveData().observeForever(revokeLiveDataObserver);

    // 用户信息变化监听，更新相关用户信息。用户信息数据保存在全局静态数据中
    userInfoLiveDataObserver = this::onUserInfoChanged;
    viewModel.getUserChangeLiveData().observeForever(userInfoLiveDataObserver);

    // 查询PIN信息结果监听
    msgPinLiveDataObserver = this::onQueryPinMessage;
    viewModel.getMsgPinLiveData().observeForever(msgPinLiveDataObserver);

    // 添加PIN消息监听
    addPinLiveDataObserver = this::onAddPin;
    viewModel.getAddPinMessageLiveData().observeForever(addPinLiveDataObserver);

    // 移除PIN消息
    removePinLiveDataObserver = this::onRemovePin;
    viewModel.getRemovePinMessageLiveData().observeForever(removePinLiveDataObserver);

    // 删除消息监听
    deleteLiveDataObserver = this::onDeleteMessage;
    viewModel.getDeleteMessageLiveData().observeForever(deleteLiveDataObserver);

    // 系统图片&视频选择器，选择结果处理
    pickMediaLauncher =
        registerForActivityResult(
            new ActivityResultContracts.PickVisualMedia(),
            uri -> {
              // Callback is invoked after the user selects a media item or closes the
              // photo picker.
              if (uri != null) {
                ALog.d(LIB_TAG, LOG_TAG, "pick media result uri -->> " + uri);
                mHandler.postDelayed(
                    () -> viewModel.sendImageOrVideoMessage(uri, getContext()), 100);
              } else {
                ALog.d(LIB_TAG, LOG_TAG, "PhotoPicker No media selected");
              }
            });

    // 系统文件选择器，选择结果处理
    pickFileLauncher =
        registerForActivityResult(new ActivityResultContracts.OpenDocument(), this::onPickFile);

    // 发送拍摄的图片
    takePictureLauncher =
        registerForActivityResult(new ActivityResultContracts.TakePicture(), this::onTakePicture);

    // 发送录像的视频
    captureVideoLauncher =
        registerForActivityResult(new ActivityResultContracts.CaptureVideo(), this::onCaptureVideo);

    forwardLauncher =
        registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), this::onForwardMessage);

    locationLauncher =
        registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), this::onSelectLocation);
  }

  public void showForwardConfirmDialog(ArrayList<String> conversationIds) {}

  protected void onLoadMessage(FetchResult<List<ChatMessageBean>> listFetchResult) {
    if (listFetchResult == null) {
      return;
    }
    if (chatView.getMessageListView().getMessageAdapter().getItemCount() == 0
        && listFetchResult.getData() != null) {
      if (conversationType == V2NIMConversationType.V2NIM_CONVERSATION_TYPE_TEAM
          && viewModel instanceof ChatTeamViewModel) {
        ((ChatTeamViewModel) viewModel).refreshTeamMessageReceipt(listFetchResult.getData());
      }
    }
    boolean hasMore = listFetchResult.getLoadStatus() != LoadStatus.Finish;
    if (listFetchResult.getTypeIndex() == 0) {
      ALog.d(LIB_TAG, LOG_TAG, "message observe older forward has more:" + hasMore);
      chatView.getMessageListView().setHasMoreForwardMessages(hasMore);
      chatView.addMessageListForward(listFetchResult.getData());
    } else {
      ALog.d(LIB_TAG, LOG_TAG, "message observe newer load has more:" + hasMore);
      chatView.getMessageListView().setHasMoreNewerMessages(hasMore);
      if (listFetchResult.getExtraInfo() instanceof AnchorScrollInfo) {
        chatView.appendMessageList(listFetchResult.getData(), false);
      } else {
        chatView.appendMessageList(listFetchResult.getData());
      }
    }
  }

  protected void onReceiveMessage(FetchResult<List<ChatMessageBean>> listFetchResult) {

    if (listFetchResult.getData() == null || listFetchResult.getData().isEmpty()) {
      ALog.d(LIB_TAG, LOG_TAG, "rec message observe data is null or empty");
      return;
    }
    if (chatView.getMessageListView().hasMoreNewerMessages()) {
      chatView.clearMessageList();
      chatView.getMessageListView().appendMessageList(listFetchResult.getData());
      if (listFetchResult.getData() != null) {
        chatView.getMessageListView().setHasMoreNewerMessages(false);
        viewModel.fetchMoreMessage(
            listFetchResult
                .getData()
                .get(listFetchResult.getData().size() - 1)
                .getMessageData()
                .getMessage(),
            V2NIMMessageQueryDirection.V2NIM_QUERY_DIRECTION_DESC);
      }
    } else {
      chatView.appendMessageList(listFetchResult.getData());
    }
  }

  protected void onMessageUpdate(
      FetchResult<Pair<MessageUpdateType, List<ChatMessageBean>>> fetchResult) {
    if (fetchResult.getLoadStatus() == LoadStatus.Success && fetchResult.getData() != null) {
      String payload = null;
      if (fetchResult.getData().first == MessageUpdateType.Pin) {
        payload = ActionConstants.PAYLOAD_SIGNAL;
      } else if (fetchResult.getData().first == MessageUpdateType.VoiceToText) {
        payload = ActionConstants.PAYLOAD_VOICE_TO_TEXT;
      }
      for (ChatMessageBean messageBean : fetchResult.getData().second) {
        chatView.getMessageListView().updateMessage(messageBean, payload);
      }
    } else if (fetchResult.getLoadStatus() == LoadStatus.Error) {
      String toast = fetchResult.getErrorMsg(getContext());
      if (!TextUtils.isEmpty(toast)) {
        ToastX.showShortToast(toast);
      }
    }
  }

  /** PIN 消息列表变化处理 */
  protected void onPinedMessageListChange(FetchResult<List<V2NIMMessagePin>> pinedList) {
    if (pinedList.getLoadStatus() == LoadStatus.Success && pinedList.getData() != null) {
      for (ChatMessageBean messageBean : chatView.getMessageList()) {
        //查找消息对应的pin信息
        V2NIMMessagePin pinInfo = null;
        for (V2NIMMessagePin pin : pinedList.getData()) {
          if (TextUtils.equals(
              messageBean.getMessageData().getMessage().getMessageClientId(),
              pin.getMessageRefer().getMessageClientId())) {
            pinInfo = pin;
            break;
          }
        }
        if (pinInfo != null && messageBean.getMessageData().getPinOption() == null) {
          //更新消息的pin信息
          messageBean.getMessageData().setPinOption(new MessagePinInfo(pinInfo));
          chatView.getMessageListView().updateMessage(messageBean, ActionConstants.PAYLOAD_SIGNAL);
        } else if (pinInfo == null && messageBean.getMessageData().getPinOption() != null) {
          messageBean.getMessageData().setPinOption(null);
          chatView.getMessageListView().updateMessage(messageBean, ActionConstants.PAYLOAD_SIGNAL);
        }
      }
    }
  }

  protected void onSentMessage(FetchResult<ChatMessageBean> fetchResult) {
    if (fetchResult.getType() == FetchResult.FetchType.Add) {
      ALog.d(LIB_TAG, LOG_TAG, "send message add");
      if (chatView.getMessageListView().hasMoreNewerMessages()) {
        chatView.clearMessageList();
        chatView.getMessageListView().insertMessage(fetchResult.getData());
        if (fetchResult.getData() != null) {
          chatView.getMessageListView().setHasMoreNewerMessages(false);
          viewModel.fetchMoreMessage(
              fetchResult.getData().getMessageData().getMessage(),
              V2NIMMessageQueryDirection.V2NIM_QUERY_DIRECTION_DESC);
        }
      } else {
        ALog.d(LIB_TAG, LOG_TAG, "send message appendMessage");
        chatView.getMessageListView().insertMessage(fetchResult.getData());
      }
    } else {
      chatView.updateMessageStatus(fetchResult.getData());
    }
  }

  protected void onAttachmentUpdateProgress(FetchResult<IMMessageProgress> fetchResult) {
    ALog.d(LIB_TAG, LOG_TAG, "attachment update progress");
    chatView.updateProgress(fetchResult.getData());
  }

  protected void onRevokeMessage(FetchResult<List<MessageRevokeInfo>> fetchResult) {
    if (fetchResult.getLoadStatus() == LoadStatus.Success
        && fetchResult.getData() != null
        && fetchResult.getData().size() > 0) {
      for (MessageRevokeInfo revokeInfo : fetchResult.getData()) {
        V2NIMMessageRefer revokeRefer = revokeInfo.getRevokeRefer();
        chatView.revokeMessage(revokeRefer);
        ChatMsgCache.removeMessage(revokeRefer.getMessageClientId());
        checkMultiSelectView();
        checkAudioPlayAndStop(revokeRefer);

        if (ChatConfigManager.enableInsertLocalMsgWhenRevoke) {
          if (revokeInfo.getRevokeMessage() != null) {
            MessageHelper.saveLocalRevokeMessage(
                revokeInfo.getRevokeMessage(), true, IMKitClient.account());
          } else {
            MessageHelper.saveLocalMessageForOthersRevokeMessage(
                revokeInfo.getRevokeNotification());
          }
        }
      }

    } else if (fetchResult.getLoadStatus() == LoadStatus.Error) {
      FetchResult.ErrorMsg errorMsg = fetchResult.getError();
      if (errorMsg != null) {
        ToastX.showShortToast(errorMsg.getRes());
      }
    }
  }

  protected void onDeleteMessage(FetchResult<List<V2NIMMessageRefer>> fetchResult) {
    if (fetchResult.getLoadStatus() == LoadStatus.Success && fetchResult.getData() != null) {
      for (V2NIMMessageRefer msg : fetchResult.getData()) {
        checkAudioPlayAndStop(msg);
      }
      List<String> deleteListId = new ArrayList<>();
      for (V2NIMMessageRefer msg : fetchResult.getData()) {
        deleteListId.add(msg.getMessageClientId());
      }
      chatView.deleteMessages(deleteListId);
      ChatMsgCache.removeMessagesByClientId(deleteListId);
      checkMultiSelectView();
      if (chatView.getMessageList() == null || chatView.getMessageList().size() < 1) {
        viewModel.getMessageList(null, false);
      }
    } else if (fetchResult.getLoadStatus() == LoadStatus.Error) {
      FetchResult.ErrorMsg errorMsg = fetchResult.getError();
      if (errorMsg != null) {
        ToastX.showShortToast(errorMsg.getRes());
      }
    }
  }

  protected void onUserInfoChanged(FetchResult<List<String>> fetchResult) {
    if (fetchResult.getLoadStatus() == LoadStatus.Finish
        && fetchResult.getType() == FetchResult.FetchType.Update) {
      chatView.getMessageListView().notifyUserInfoChanged(fetchResult.getData());
      if (conversationType == V2NIMConversationType.V2NIM_CONVERSATION_TYPE_P2P
          && fetchResult.getData() != null) {
        for (String account : fetchResult.getData()) {
          if (TextUtils.equals(account, accountId)) {
            updateCurrentUserInfo();
          }
        }
      }
    }
  }

  protected void onQueryPinMessage(FetchResult<Map<String, V2NIMMessagePin>> fetchResult) {
    if (fetchResult.getLoadStatus() == LoadStatus.Finish
        && fetchResult.getType() == FetchResult.FetchType.Update) {
      chatView.getMessageListView().updateMessagePin(fetchResult.getData());
    }
  }

  protected void onAddPin(Pair<String, V2NIMMessagePin> pinOptionPair) {
    chatView.getMessageListView().addPinMessage(pinOptionPair.first, pinOptionPair.second);
  }

  protected void onRemovePin(String uuid) {
    chatView.getMessageListView().removePinMessage(uuid);
  }

  protected void onPickFile(Uri pickFile) {
    ALog.d(LIB_TAG, LOG_TAG, "pick file result uri:" + pickFile);
    if (pickFile == null) {
      return;
    }
    String size = ChatUtils.getUrlFileSize(IMKitClient.getApplicationContext(), pickFile);
    if (ChatUtils.fileSizeLimit(Long.parseLong(size))) {
      String fileSizeLimit = String.valueOf(ChatUtils.getFileLimitSize());
      String limitText =
          String.format(getString(R.string.chat_message_file_size_limit_tips), fileSizeLimit);
      Toast.makeText(ChatBaseFragment.this.getContext(), limitText, Toast.LENGTH_SHORT).show();
      return;
    }
    viewModel.sendFile(pickFile);
  }

  protected void onTakePicture(boolean sure) {
    if (sure && !TextUtils.isEmpty(captureTempImagePath)) {
      File f = new File(captureTempImagePath);
      Uri contentUri = Uri.fromFile(f);
      ALog.d(LIB_TAG, LOG_TAG, "take picture contentUri -->> " + contentUri);
      viewModel.sendImageOrVideoMessage(contentUri, getContext());
      captureTempImagePath = "";
    }
  }

  protected void onCaptureVideo(boolean sure) {
    if (sure && !TextUtils.isEmpty(captureTempVideoPath)) {
      File f = new File(captureTempVideoPath);
      Uri contentUri = Uri.fromFile(f);
      ALog.d(LIB_TAG, LOG_TAG, "capture video contentUri -->> " + contentUri);
      viewModel.sendImageOrVideoMessage(contentUri, getContext());
      captureTempVideoPath = "";
    }
  }

  protected void onForwardMessage(ActivityResult result) {
    if (result.getResultCode() != Activity.RESULT_OK) {
      return;
    }
    ALog.d(LIB_TAG, LOG_TAG, "forward Team result");
    Intent data = result.getData();
    if (data != null) {
      ArrayList<String> selectedList =
          data.getStringArrayListExtra(KEY_FORWARD_SELECTED_CONVERSATIONS);
      if (selectedList != null && selectedList.size() > 0) {
        showForwardConfirmDialog(selectedList);
      }
    }
  }

  protected void onSelectLocation(ActivityResult result) {
    if (result.getResultCode() != Activity.RESULT_OK) {
      return;
    }
    ALog.d(LIB_TAG, LOG_TAG, "send location result");
    Intent data = result.getData();
    if (data != null) {
      ChatLocationBean locationBean =
          (ChatLocationBean) data.getSerializableExtra(RouterConstant.KEY_LOCATION_SELECT_RESULT);
      if (locationBean != null) {
        viewModel.sendLocationMessage(locationBean);
      }
    }
  }

  protected abstract void initData(Bundle bundle);

  @Override
  public void onStart() {
    super.onStart();
    ALog.d(LIB_TAG, LOG_TAG, "onStart");
    viewModel.setChattingAccount();
    chatView.setNetWorkState(NetworkUtils.isConnected());
  }

  public void onNewIntent(Intent intent) {
    ALog.d(LIB_TAG, LOG_TAG, "onNewIntent");
  }

  protected void onMultiDelete() {
    if (ChatMsgCache.getMessageCount() > ChatKitUIConstant.MULTI_DELETE_MSG_LIMIT) {
      Toast.makeText(
              ChatBaseFragment.this.getContext(),
              R.string.chat_message_multi_delete_limit_tips,
              Toast.LENGTH_SHORT)
          .show();
      return;
    }
    showDeleteConfirmDialog(ChatMsgCache.getMessageList());
  }

  protected boolean onSingleForward() {
    if (ChatMsgCache.getMessageCount() > ChatKitUIConstant.SINGLE_FORWARD_MSG_LIMIT) {
      Toast.makeText(
              ChatBaseFragment.this.getContext(),
              R.string.chat_message_single_forward_limit_tips,
              Toast.LENGTH_SHORT)
          .show();
      return true;
    }

    // 如果逐条转发中包含不允许转发消息，则Toast提示,并取消选中
    List<ChatMessageBean> invalidList = ChatUtils.checkSingleForward(ChatMsgCache.getMessageList());
    if (invalidList.size() > 0) {
      CommonChoiceDialog dialog = new CommonChoiceDialog();
      dialog
          .setTitleStr(getString(R.string.msg_forward_error_dialog_title))
          .setContentStr(getString(R.string.msg_forward_error_dialog_content))
          .setPositiveStr(getString(R.string.chat_dialog_sure))
          .setNegativeStr(getString(R.string.cancel))
          .setConfirmListener(
              new ChoiceListener() {
                @Override
                public void onPositive() {
                  ChatMsgCache.removeMessages(invalidList);
                  chatView.getMessageListView().updateMultiSelectMessage(invalidList);
                  // 如果逐条转发中所有消息都是不可转发，则不弹出转发选择框
                  if (ChatMsgCache.getMessageCount() > 0) {
                    onStartForward(ActionConstants.ACTION_TYPE_SINGLE_FORWARD);
                  } else {
                    chatView.setMultiSelectEnable(false);
                  }
                }

                @Override
                public void onNegative() {}
              })
          .show(getParentFragmentManager());
    } else {
      // 如果逐条转发中所有消息都是不可转发，则不弹出转发选择框
      if (ChatMsgCache.getMessageCount() > 0) {
        onStartForward(ActionConstants.ACTION_TYPE_SINGLE_FORWARD);
      } else {
        chatView.setMultiSelectEnable(false);
      }
    }
    return true;
  }

  protected boolean onMultiForward() {
    if (ChatMsgCache.getMessageCount() > ChatKitUIConstant.MULTI_FORWARD_MSG_LIMIT) {
      Toast.makeText(
              ChatBaseFragment.this.getContext(),
              R.string.chat_message_multi_forward_limit_tips,
              Toast.LENGTH_SHORT)
          .show();
      return true;
    }
    // 如果逐条转发中包含不允许转发消息，则Toast提示,并取消选中
    List<ChatMessageBean> invalidList = ChatUtils.checkMultiForward(ChatMsgCache.getMessageList());
    if (invalidList.size() > 0) {
      CommonChoiceDialog dialog = new CommonChoiceDialog();
      dialog
          .setTitleStr(getString(R.string.msg_forward_error_dialog_title))
          .setContentStr(getString(R.string.msg_forward_error_dialog_content))
          .setPositiveStr(getString(R.string.chat_dialog_sure))
          .setNegativeStr(getString(R.string.cancel))
          .setConfirmListener(
              new ChoiceListener() {
                @Override
                public void onPositive() {
                  ChatMsgCache.removeMessages(invalidList);
                  chatView.getMessageListView().updateMultiSelectMessage(invalidList);
                  // 如果逐条转发中所有消息都是不可转发，则不弹出转发选择框
                  if (ChatMsgCache.getMessageCount() > 0) {
                    onStartForward(ActionConstants.ACTION_TYPE_MULTI_FORWARD);
                  } else {
                    chatView.setMultiSelectEnable(false);
                  }
                }

                @Override
                public void onNegative() {}
              })
          .show(getParentFragmentManager());
    } else {
      // 如果逐条转发中所有消息都是不可转发，则不弹出转发选择框
      if (ChatMsgCache.getMessageCount() > 0) {
        onStartForward(ActionConstants.ACTION_TYPE_MULTI_FORWARD);
      } else {
        chatView.setMultiSelectEnable(false);
      }
    }

    return true;
  }

  private final NetworkUtils.NetworkStateListener networkStateListener =
      new NetworkUtils.NetworkStateListener() {

        @Override
        public void onConnected(NetworkUtils.NetworkType networkType) {
          ALog.d(LIB_TAG, LOG_TAG, "onNewIntent");
          chatView.setNetWorkState(true);
          refreshTeamMessageReceiptForNetBroken();
        }

        @Override
        public void onDisconnected() {
          chatView.setNetWorkState(false);
        }
      };

  private void refreshTeamMessageReceiptForNetBroken() {
    if (conversationType != V2NIMConversationType.V2NIM_CONVERSATION_TYPE_TEAM
        || !(viewModel instanceof ChatTeamViewModel)) {
      return;
    }
    LinearLayoutManager layoutManager =
        (LinearLayoutManager) chatView.getMessageListView().getLayoutManager();
    ChatMessageAdapter messageAdapter = chatView.getMessageListView().getMessageAdapter();
    if (layoutManager == null || messageAdapter == null) {
      return;
    }
    List<ChatMessageBean> messages = messageAdapter.getMessageList();
    if (messages == null || messages.isEmpty()) {
      return;
    }
    int firstVisible = Math.max(layoutManager.findFirstVisibleItemPosition(), 0);
    int lastVisible = Math.min(layoutManager.findLastVisibleItemPosition() + 1, messages.size());
    if (firstVisible > lastVisible) {
      return;
    }
    ((ChatTeamViewModel) viewModel)
        .refreshTeamMessageReceipt(messages.subList(firstVisible, lastVisible));
  }

  @Override
  public void onDestroyView() {
    ALog.d(LIB_TAG, LOG_TAG, "onDestroyView");
    super.onDestroyView();
    IMKitClient.removeLoginDetailListener(loginListener);
    viewModel.clearChattingAccount();
    NetworkUtils.unregisterNetworkStatusChangedListener(networkStateListener);
    if (popMenu != null) {
      popMenu.hide();
    }
    viewModel.getUserChangeLiveData().removeObserver(userInfoLiveDataObserver);
    viewModel.getQueryMessageLiveData().removeObserver(messageLiveDataObserver);
    viewModel.getRecMessageLiveData().removeObserver(messageRecLiveDataObserver);
    viewModel.getAddPinMessageLiveData().removeObserver(addPinLiveDataObserver);
    viewModel.getRemovePinMessageLiveData().removeObserver(removePinLiveDataObserver);
    viewModel.getSendMessageLiveData().removeObserver(sendLiveDataObserver);
    viewModel.getRevokeMessageLiveData().removeObserver(revokeLiveDataObserver);
    viewModel.getAttachmentProgressMutableLiveData().removeObserver(attachLiveDataObserver);
    viewModel.getUpdateMessageLiveData().removeObserver(messageUpdateLiveDataObserver);
    viewModel.getPinedMessageListLiveData().removeObserver(pinedMessageLiveDataObserver);
  }

  /** for custom layout for ChatView */
  public void setChatViewCustom(IChatViewCustom chatViewCustom) {
    this.chatViewCustom = chatViewCustom;
  }

  public void setChatConfig(ChatUIConfig config) {
    if (config != null) {
      this.chatConfig = config;
    }
  }

  public void updateCurrentUserInfo() {}
}
