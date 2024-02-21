// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.page.fragment;

import static com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant.LIB_TAG;
import static com.netease.yunxin.kit.chatkit.ui.view.input.ActionConstants.PAYLOAD_REFRESH_AUDIO_ANIM;
import static com.netease.yunxin.kit.corekit.im.utils.RouterConstant.REQUEST_CONTACT_SELECTOR_KEY;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.StatusCode;
import com.netease.nimlib.sdk.auth.AuthServiceObserver;
import com.netease.nimlib.sdk.msg.attachment.MsgAttachment;
import com.netease.nimlib.sdk.msg.attachment.NetCallAttachment;
import com.netease.nimlib.sdk.msg.constant.MsgStatusEnum;
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.AttachmentProgress;
import com.netease.nimlib.sdk.msg.model.GetMessageDirectionEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.msg.model.MsgPinOption;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.map.ChatLocationBean;
import com.netease.yunxin.kit.chatkit.model.IMMessageInfo;
import com.netease.yunxin.kit.chatkit.repo.ChatRepo;
import com.netease.yunxin.kit.chatkit.ui.ChatKitClient;
import com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant;
import com.netease.yunxin.kit.chatkit.ui.ChatUIConfig;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.builder.IChatViewCustom;
import com.netease.yunxin.kit.chatkit.ui.common.ChatMsgCache;
import com.netease.yunxin.kit.chatkit.ui.common.ChatUtils;
import com.netease.yunxin.kit.chatkit.ui.common.MessageHelper;
import com.netease.yunxin.kit.chatkit.ui.common.WatchTextMessageDialog;
import com.netease.yunxin.kit.chatkit.ui.custom.RichTextAttachment;
import com.netease.yunxin.kit.chatkit.ui.dialog.ChatBaseForwardSelectDialog;
import com.netease.yunxin.kit.chatkit.ui.interfaces.IChatView;
import com.netease.yunxin.kit.chatkit.ui.interfaces.IMessageItemClickListener;
import com.netease.yunxin.kit.chatkit.ui.interfaces.IMessageLoadHandler;
import com.netease.yunxin.kit.chatkit.ui.interfaces.IMessageProxy;
import com.netease.yunxin.kit.chatkit.ui.model.AnchorScrollInfo;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.chatkit.ui.model.ait.AitContactsModel;
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
import com.netease.yunxin.kit.common.ui.fragments.BaseFragment;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.common.utils.NetworkUtils;
import com.netease.yunxin.kit.common.utils.PermissionUtils;
import com.netease.yunxin.kit.common.utils.storage.StorageType;
import com.netease.yunxin.kit.common.utils.storage.StorageUtil;
import com.netease.yunxin.kit.corekit.im.IMKitClient;
import com.netease.yunxin.kit.corekit.im.model.UserInfo;
import com.netease.yunxin.kit.corekit.im.provider.FetchCallback;
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** BaseFragment for Chat include P2P and Team chat page */
public abstract class ChatBaseFragment extends BaseFragment {

  private static final String LOG_TAG = "ChatBaseFragment";

  private static final int REQUEST_PERMISSION = 0;
  private static final int REQUEST_CAMERA_PERMISSION = 1;
  private static final int REQUEST_VIDEO_PERMISSION = 2;
  private static final int REQUEST_READ_EXTERNAL_STORAGE_PERMISSION_ALBUM = 3;
  private static final int REQUEST_READ_EXTERNAL_STORAGE_PERMISSION_FILE = 4;
  private static final int AUDIO_MESSAGE_MIN_LENGTH = 1000;

  private int currentRequest = 0;

  protected ChatBaseViewModel viewModel;
  protected AitManager aitManager;

  protected SessionTypeEnum sessionType = SessionTypeEnum.P2P;

  protected String sessionID;

  protected Handler mHandler;

  protected ChatMessageBean forwardMessage;

  protected ActivityResultLauncher<String> pickMediaLauncher;
  protected ActivityResultLauncher<String[]> pickFileLauncher;
  private String captureTempImagePath = "";
  protected ActivityResultLauncher<Uri> takePictureLauncher;
  private String captureTempVideoPath = "";
  protected ActivityResultLauncher<Intent> captureVideoLauncher;

  protected ActivityResultLauncher<Intent> forwardP2PLauncher;

  protected ActivityResultLauncher<Intent> forwardTeamLauncher;

  protected ActivityResultLauncher<String[]> permissionLauncher;

  protected ActivityResultLauncher<Intent> locationLauncher;

  private Observer<FetchResult<List<ChatMessageBean>>> messageLiveDataObserver;
  private Observer<FetchResult<List<ChatMessageBean>>> messageRecLiveDataObserver;
  private Observer<FetchResult<ChatMessageBean>> sendLiveDataObserver;
  private Observer<FetchResult<ChatMessageBean>> revokeLiveDataObserver;
  private Observer<FetchResult<AttachmentProgress>> attachLiveDataObserver;
  private Observer<FetchResult<List<String>>> userInfoLiveDataObserver;
  private Observer<FetchResult<Map<String, MsgPinOption>>> msgPinLiveDataObserver;
  private Observer<Pair<String, MsgPinOption>> addPinLiveDataObserver;
  private Observer<String> removePinLiveDataObserver;
  private Observer<FetchResult<List<ChatMessageBean>>> deleteLiveDataObserver;
  private final com.netease.nimlib.sdk.Observer<StatusCode> loginObserver =
      statusCode -> {
        if (statusCode == StatusCode.LOGINED) {
          NIMClient.getService(AuthServiceObserver.class)
              .observeOnlineStatus(this.loginObserver, false);
          if (this.chatView != null && this.chatView.getMessageListView() != null) {
            this.chatView.getMessageListView().clearMessageList();
          }
          initToFetchData();
        }
      };

  protected ChatPopMenu popMenu;

  protected IChatViewCustom chatViewCustom;

  protected ChatUIConfig chatConfig;

  protected IMessageItemClickListener delegateListener;

  public IChatView chatView;

  public View rootView;

  public String forwardAction;

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    rootView = initViewAndGetRootView(inflater, container);
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
    NIMClient.getService(AuthServiceObserver.class).observeOnlineStatus(loginObserver, true);
    initViewModel();
    initDataObserver();
    if (!NetworkUtils.isConnected()) {
      initToFetchData();
    }
  }

  public void setIMessageItemClickListener(IMessageItemClickListener clickListener) {
    delegateListener = clickListener;
  }

  public abstract View initViewAndGetRootView(
      @NonNull LayoutInflater inflater, @Nullable ViewGroup container);

  public Integer getReplayMessageClickPreviewDialogBgRes() {
    return null;
  }

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
    permissionLauncher =
        registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            result -> {
              if (result != null) {
                for (Map.Entry<String, Boolean> entry : result.entrySet()) {
                  String permission = entry.getKey().toString();
                  boolean grant = (Boolean) entry.getValue();
                  if (grant) {
                    if (TextUtils.equals(permission, Manifest.permission.CAMERA)) {
                      if (currentRequest == REQUEST_CAMERA_PERMISSION) {
                        startTakePicture();
                      } else if (currentRequest == REQUEST_VIDEO_PERMISSION) {
                        startCaptureVideo();
                      }
                    } else if (TextUtils.equals(
                            permission, Manifest.permission.READ_EXTERNAL_STORAGE)
                        || TextUtils.equals(permission, Manifest.permission.READ_MEDIA_IMAGES)) {
                      if (currentRequest == REQUEST_READ_EXTERNAL_STORAGE_PERMISSION_ALBUM) {
                        startPickMedia();
                      } else if (currentRequest == REQUEST_READ_EXTERNAL_STORAGE_PERMISSION_FILE) {
                        startPickFile();
                      }
                    }
                  } else {
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
    if (ChatMsgCache.getMessageList().size() > 0) {
      chatView.setMultiSelectEnable(true);
    } else {
      chatView.setMultiSelectEnable(false);
    }
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
          if (aitManager != null && sessionType == SessionTypeEnum.Team) {
            pushList = aitManager.getAitTeamMember();
            if (pushList != null && pushList.size() > 0) {
              extension = new HashMap<>();
              extension.put(ChatKitUIConstant.AIT_REMOTE_EXTENSION_KEY, aitManager.getAitData());
            }
          }
          if (replyMsg == null) {
            viewModel.sendTextMessage(msg, pushList, extension);
          } else {
            viewModel.replyTextMessage(
                msg, replyMsg.getMessageData().getMessage(), pushList, extension);
          }
          if (aitManager != null) {
            aitManager.reset();
          }
          return true;
        }

        @Override
        public boolean sendRichTextMessage(String title, String content, ChatMessageBean replyMsg) {
          List<String> pushList = null;
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
          if (aitManager != null && sessionType == SessionTypeEnum.Team) {
            pushList = aitManager.getAitTeamMember();
            if (pushList != null && pushList.size() > 0) {
              extension = new HashMap<>();
              extension.put(ChatKitUIConstant.AIT_REMOTE_EXTENSION_KEY, aitManager.getAitData());
            }
          }
          //标题中不允许包含回车
          String replaceTitle = title.replaceAll("\r|\n", "");
          IMMessage sendMsg =
              MessageHelper.createRichTextMessage(
                  replaceTitle, msgContent, sessionID, sessionType, pushList, extension);
          if (replyMsg == null) {
            viewModel.sendMessage(sendMsg);
          } else {
            viewModel.replyMessage(sendMsg, replyMsg.getMessageData().getMessage(), true);
          }
          if (aitManager != null) {
            aitManager.reset();
          }
          return true;
        }

        @Override
        public void pickMedia() {
          String[] permission = new String[] {Manifest.permission.READ_EXTERNAL_STORAGE};
          // 根据系统版本判断，如果是Android13则采用Manifest.permission.READ_MEDIA_IMAGES
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permission =
                new String[] {
                  Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO
                };
          }
          if (PermissionUtils.hasPermissions(ChatBaseFragment.this.getContext(), permission)) {
            startPickMedia();
          } else {
            requestCameraPermission(permission, REQUEST_READ_EXTERNAL_STORAGE_PERMISSION_ALBUM);
          }
        }

        @Override
        public void takePicture() {
          if (PermissionUtils.hasPermissions(
              ChatBaseFragment.this.getContext(), Manifest.permission.CAMERA)) {
            startTakePicture();
          } else {
            requestCameraPermission(Manifest.permission.CAMERA, REQUEST_CAMERA_PERMISSION);
          }
        }

        @Override
        public void captureVideo() {
          if (PermissionUtils.hasPermissions(
              ChatBaseFragment.this.getContext(), Manifest.permission.CAMERA)) {
            startCaptureVideo();
          } else {
            requestCameraPermission(Manifest.permission.CAMERA, REQUEST_VIDEO_PERMISSION);
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
            requestCameraPermission(permission, REQUEST_READ_EXTERNAL_STORAGE_PERMISSION_FILE);
          }
          return true;
        }

        @Override
        public boolean sendAudio(File audioFile, long audioLength, ChatMessageBean replyMsg) {
          // audio not support reply
          if (audioLength < AUDIO_MESSAGE_MIN_LENGTH) {
            ToastX.showShortToast(R.string.chat_message_audio_to_short);
          } else {
            viewModel.sendAudioMessage(audioFile, audioLength);
          }
          return true;
        }

        @Override
        public boolean sendCustomMessage(MsgAttachment attachment, String content) {
          viewModel.sendCustomMessage(attachment, content);
          return true;
        }

        @Override
        public void onTypeStateChange(boolean isTyping) {
          if (sessionType == SessionTypeEnum.P2P && viewModel instanceof ChatP2PViewModel) {
            ((ChatP2PViewModel) viewModel).sendInputNotification(isTyping);
          }
        }

        @Override
        public boolean hasPermission(String permission) {
          if (TextUtils.isEmpty(permission)) {
            return false;
          }
          if (PermissionUtils.hasPermissions(ChatBaseFragment.this.getContext(), permission)) {
            return true;
          } else {
            requestCameraPermission(permission, REQUEST_PERMISSION);
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
          XKitRouter.withKey(RouterConstant.PATH_CHAT_LOCATION_PAGE)
              .withContext(requireContext())
              .navigate(locationLauncher);
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
          ChatUtils.startVideoCall(getContext(), sessionID);
        }

        @Override
        public void audioCall() {
          ChatUtils.startAudioCall(getContext(), sessionID);
        }

        @Override
        public String getSessionId() {
          return sessionID;
        }

        @Override
        public SessionTypeEnum getSessionType() {
          return sessionType;
        }
      };

  private void requestCameraPermission(String permission, int request) {
    currentRequest = request;
    permissionLauncher.launch(new String[] {permission});
  }

  private void requestCameraPermission(String[] permission, int request) {
    currentRequest = request;
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
    pickMediaLauncher.launch("image/*;video/*");
  }

  protected void startPickFile() {
    pickFileLauncher.launch(new String[] {"*/*"});
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
      Intent intent =
          new Intent(MediaStore.ACTION_VIDEO_CAPTURE).putExtra(MediaStore.EXTRA_OUTPUT, videoUri);
      captureVideoLauncher.launch(intent);
    }
  }

  protected void checkAudioPlayAndStop(ChatMessageBean messageBean) {
    if (messageBean != null
        && messageBean.getMessageData().getMessage().getMsgType() == MsgTypeEnum.audio
        && ChatMessageAudioControl.getInstance().isPlayingAudio()
        && MessageHelper.isSameMessage(
            messageBean.getMessageData(),
            ChatMessageAudioControl.getInstance().getPlayingAudio())) {
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
            if (popMenu == null) {
              popMenu = new ChatPopMenu();
            }
            if (popMenu.isShowing()) {
              return true;
            }
            int[] location = new int[2];
            chatView.getMessageListView().getLocationOnScreen(location);
            popMenu.show(view, messageBean, location[1]);
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
            ChatMsgCache.removeMessage(messageInfo.getMessageData().getMessage().getUuid());
          }
          checkMultiSelectView();
          return true;
        }

        @Override
        public boolean onUserIconClick(View view, int position, ChatMessageBean messageBean) {
          if (delegateListener == null
              || !delegateListener.onUserIconClick(view, position, messageBean)) {
            XKitRouter.withKey(getUserInfoRoutePath())
                .withContext(view.getContext())
                .withParam(
                    RouterConstant.KEY_ACCOUNT_ID_KEY,
                    messageBean.getMessageData().getMessage().getFromAccount())
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
            if (aitManager != null && sessionType == SessionTypeEnum.Team) {
              String account = messageBean.getMessageData().getMessage().getFromAccount();
              UserInfo userInfo = messageBean.getMessageData().getFromUser();
              if (!TextUtils.equals(account, IMKitClient.account())) {
                if (userInfo == null) {
                  userInfo = new UserInfo(account, account, null);
                }
                String name = MessageHelper.getTeamAitName(aitManager.getTid(), userInfo);
                if (TextUtils.isEmpty(name)) {
                  if (messageBean.getMessageData().getFromUser() != null) {
                    name = messageBean.getMessageData().getFromUser().getName();
                  } else {
                    name = account;
                  }
                }
                aitManager.insertReplyAit(account, name);
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
        public boolean onReEditRevokeMessage(View view, int position, ChatMessageBean messageBean) {
          // only support text message
          if (delegateListener == null
              || !delegateListener.onReEditRevokeMessage(view, position, messageBean)) {
            if (messageBean != null && MessageHelper.revokeMsgIsEdit(messageBean)) {
              Map<String, String> richMap =
                  MessageHelper.getRichMessageRevokeContent(messageBean.getMessageData());
              if (MessageHelper.isRichText(messageBean.getMessageData())) {
                RichTextAttachment attachment =
                    (RichTextAttachment) messageBean.getMessageData().getMessage().getAttachment();
                chatView.setReEditRichMessage(attachment.title, attachment.body);

              } else if (richMap != null) {
                String title = richMap.get(ChatKitUIConstant.KEY_RICH_TEXT_TITLE);
                String body = richMap.get(ChatKitUIConstant.KEY_RICH_TEXT_BODY);
                chatView.setReEditRichMessage(title, body);
              } else {
                String revokeContent =
                    MessageHelper.getMessageRevokeContent(messageBean.getMessageData());
                if (TextUtils.isEmpty(revokeContent)) {
                  revokeContent = messageBean.getMessageData().getMessage().getContent();
                }
                chatView.setReeditMessage(revokeContent);
              }
              if (messageBean.hasReply()) {
                loadReplyInfo(messageBean.getReplyUUid(), false);
              }
              AitContactsModel aitModel =
                  MessageHelper.getAitBlock(messageBean.getMessageData().getMessage());
              if (aitModel != null) {
                aitManager.reset();
                aitManager.setAitContactsModel(aitModel);
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
                && (messageInfo.getMessage().getMsgType() == MsgTypeEnum.text
                    || MessageHelper.isRichText(messageInfo))) {
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
            messageBean.getMessageData().getMessage().setStatus(MsgStatusEnum.sending);
            viewModel.sendMessage(messageBean.getMessageData().getMessage(), true, true);
          }
          return true;
        }

        @Override
        public boolean onTextSelected(View view, int position, ChatMessageBean messageInfo) {
          return (delegateListener != null
              && delegateListener.onTextSelected(view, position, messageInfo));
        }

        @Override
        public boolean onCustomClick(View view, int position, ChatMessageBean messageInfo) {
          return (delegateListener != null
              && delegateListener.onCustomClick(view, position, messageInfo));
        }
      };

  protected void loadReplyInfo(String uuid, boolean addAit) {
    if (TextUtils.isEmpty(uuid)) {
      return;
    }
    List<String> uuidList = new ArrayList<>(1);
    uuidList.add(uuid);
    ChatRepo.queryMessageListByUuid(
        uuidList,
        new FetchCallback<List<IMMessageInfo>>() {
          @Override
          public void onSuccess(@Nullable List<IMMessageInfo> param) {
            if (param != null && param.size() > 0) {
              loadReplyView(param.get(0), addAit);
            }
          }

          @Override
          public void onFailed(int code) {}

          @Override
          public void onException(@Nullable Throwable exception) {}
        });
  }

  protected void clickMessage(IMMessageInfo messageInfo, boolean isReply) {
    if (messageInfo == null) {
      return;
    }
    if (chatView.isMultiSelect()) {
      return;
    }
    if (messageInfo.getMessage().getMsgType() == MsgTypeEnum.image) {
      ArrayList<IMMessageInfo> messageInfoList = new ArrayList<>();
      List<ChatMessageBean> filterList =
          chatView
              .getMessageListView()
              .filterMessagesByType(messageInfo.getMessage().getMsgType().getValue());
      for (ChatMessageBean messageBean : filterList) {
        messageInfoList.add(messageBean.getMessageData());
      }
      ChatUtils.watchImage(this.getContext(), messageInfo, messageInfoList);
    } else if (messageInfo.getMessage().getMsgType() == MsgTypeEnum.video) {
      boolean isOpen = ChatUtils.watchVideo(getContext(), messageInfo);
      if (!isOpen && isReply) {
        chatView.getMessageListView().scrollToMessage(messageInfo.getMessage().getUuid());
      }
    } else if (messageInfo.getMessage().getMsgType() == MsgTypeEnum.location) {
      XKitRouter.withKey(RouterConstant.PATH_CHAT_LOCATION_PAGE)
          .withContext(requireContext())
          .withParam(RouterConstant.KEY_MESSAGE, messageInfo.getMessage())
          .withParam(RouterConstant.KEY_LOCATION_PAGE_TYPE, RouterConstant.KEY_LOCATION_TYPE_DETAIL)
          .navigate();
    } else if (messageInfo.getMessage().getMsgType() == MsgTypeEnum.file) {
      boolean isOpen = ChatUtils.openFile(getContext(), messageInfo);
      if (!isOpen && isReply) {
        chatView.getMessageListView().scrollToMessage(messageInfo.getMessage().getUuid());
      }
    } else if (messageInfo.getMessage().getMsgType() == MsgTypeEnum.nrtc_netcall) {
      if (!NetworkUtils.isConnected()) {
        Toast.makeText(getContext(), R.string.chat_network_error_tip, Toast.LENGTH_SHORT).show();
        return;
      }
      IMMessage message = messageInfo.getMessage();
      if (message.getAttachment() instanceof NetCallAttachment) {
        NetCallAttachment attachment = (NetCallAttachment) message.getAttachment();
        int type = attachment.getType();
        if (type == 1) {
          ChatUtils.startAudioCall(ChatBaseFragment.this.getContext(), sessionID);
        } else {
          ChatUtils.startVideoCall(ChatBaseFragment.this.getContext(), sessionID);
        }
      }
    } else if (messageInfo.getMessage().getMsgType() == MsgTypeEnum.audio) {
      ChatMessageListView messageListView = chatView.getMessageListView();
      if (messageListView == null) {
        return;
      }
      int position = messageListView.searchMessagePosition(messageInfo.getMessage().getUuid());
      ChatMessageAdapter adapter = messageListView.getMessageAdapter();
      if (adapter == null || position < 0) {
        return;
      }
      adapter.notifyItemChanged(position, PAYLOAD_REFRESH_AUDIO_ANIM);
    } else {
      if (isReply) {
        chatView.getMessageListView().scrollToMessage(messageInfo.getMessage().getUuid());
      }
    }
  }

  protected void loadReplyView(IMMessageInfo messageInfo, boolean addAit) {
    if (aitManager != null && sessionType == SessionTypeEnum.Team && addAit) {
      String account = messageInfo.getMessage().getFromAccount();
      if (!TextUtils.equals(account, IMKitClient.account())) {
        UserInfo userInfo = messageInfo.getFromUser();
        if (userInfo == null) {
          userInfo = new UserInfo(account, account, null);
        }
        String name = MessageHelper.getTeamAitName(aitManager.getTid(), userInfo);
        if (TextUtils.isEmpty(name)) {
          if (messageInfo.getFromUser() != null) {
            name = messageInfo.getFromUser().getName();
          } else {
            name = account;
          }
        }
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
              messageBean.getMessageData().getMessage(), GetMessageDirectionEnum.FORWARD);
        }

        @Override
        public void loadMoreBackground(ChatMessageBean messageBean) {
          viewModel.fetchMoreMessage(
              messageBean.getMessageData().getMessage(), GetMessageDirectionEnum.BACKWARD);
        }

        @Override
        public void onVisibleItemChange(List<ChatMessageBean> messages) {
          if (sessionType == SessionTypeEnum.Team && viewModel instanceof ChatTeamViewModel) {
            ((ChatTeamViewModel) viewModel).refreshTeamMessageReceipt(messages);
          }
        }
      };

  private final IChatPopMenuClickListener actionListener =
      new IChatPopMenuClickListener() {
        @Override
        public boolean onCopy(ChatMessageBean messageBean) {
          if (chatConfig != null
              && chatConfig.popMenuClickListener != null
              && chatConfig.popMenuClickListener.onCopy(messageBean)) {
            return true;
          }
          MessageHelper.copyTextMessage(messageBean.getMessageData(), true);
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
          viewModel.addMsgCollection(messageBean.getMessageData());
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
          showRevokeConfirmDialog(messageBean);
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
    if (IMKitClient.getConfigCenter().getTeamEnable()) {
      ChatBaseForwardSelectDialog dialog = getForwardSelectDialog();
      dialog.setSelectedCallback(
          new ChatBaseForwardSelectDialog.ForwardTypeSelectedCallback() {
            @Override
            public void onTeamSelected() {
              forwardTeam();
            }

            @Override
            public void onP2PSelected() {
              forwardP2P();
            }
          });
      dialog.show(getParentFragmentManager(), ChatBaseForwardSelectDialog.TAG);
    } else {
      forwardP2P();
    }
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
        .searchMessage(forwardMessage.getMessageData().getMessage().getUuid());
  }

  protected abstract void initViewModel();

  protected abstract ChatBaseForwardSelectDialog getForwardSelectDialog();

  protected abstract void initToFetchData();

  protected abstract void forwardP2P();

  protected abstract void forwardTeam();

  protected void initDataObserver() {
    ALog.d(LIB_TAG, LOG_TAG, "initDataObserver");

    // 加载消息数据，首次进入或者加载更多
    messageLiveDataObserver = this::onLoadMessage;
    viewModel.getQueryMessageLiveData().observeForever(messageLiveDataObserver);

    // 接受消息监听
    messageRecLiveDataObserver = this::onReceiveMessage;
    viewModel.getRecMessageLiveData().observeForever(messageRecLiveDataObserver);

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
    viewModel.getUserInfoLiveData().observeForever(userInfoLiveDataObserver);

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
            new ActivityResultContracts.GetMultipleContents(), this::onPickMedia);

    // 系统文件选择器，选择结果处理
    pickFileLauncher =
        registerForActivityResult(new ActivityResultContracts.OpenDocument(), this::onPickFile);

    // 发送拍摄的图片
    takePictureLauncher =
        registerForActivityResult(new ActivityResultContracts.TakePicture(), this::onTakePicture);

    // 发送录像的视频
    captureVideoLauncher =
        registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), this::onCaptureVideo);

    forwardP2PLauncher =
        registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> onForwardMessage(result, SessionTypeEnum.P2P));

    forwardTeamLauncher =
        registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> onForwardMessage(result, SessionTypeEnum.Team));

    locationLauncher =
        registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), this::onSelectLocation);
  }

  public void showForwardConfirmDialog(SessionTypeEnum type, ArrayList<String> sessionIds) {}

  protected void onLoadMessage(FetchResult<List<ChatMessageBean>> listFetchResult) {
    if (listFetchResult == null) {
      return;
    }
    if (chatView.getMessageListView().getMessageAdapter().getItemCount() == 0
        && listFetchResult.getData() != null) {
      if (sessionType == SessionTypeEnum.Team && viewModel instanceof ChatTeamViewModel) {
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
    ALog.d(
        LIB_TAG, LOG_TAG, "rec message observe newer load:" + (listFetchResult.getData() == null));
    chatView.appendMessageList(listFetchResult.getData());
  }

  protected void onSentMessage(FetchResult<ChatMessageBean> fetchResult) {
    if (fetchResult.getType() == FetchResult.FetchType.Add) {
      ALog.d(LIB_TAG, LOG_TAG, "send message add");
      if (chatView.getMessageListView().hasMoreNewerMessages()) {
        chatView.clearMessageList();
        chatView.appendMessage(fetchResult.getData());
        if (fetchResult.getData() != null) {
          chatView.getMessageListView().setHasMoreNewerMessages(false);
          viewModel.fetchMoreMessage(
              fetchResult.getData().getMessageData().getMessage(), GetMessageDirectionEnum.FORWARD);
        }
      } else {
        ALog.d(LIB_TAG, LOG_TAG, "send message appendMessage");
        chatView.appendMessage(fetchResult.getData());
      }
    } else {
      chatView.updateMessageStatus(fetchResult.getData());
    }
  }

  protected void onAttachmentUpdateProgress(FetchResult<AttachmentProgress> fetchResult) {
    chatView.updateProgress(fetchResult.getData());
  }

  protected void onRevokeMessage(FetchResult<ChatMessageBean> fetchResult) {
    if (fetchResult.getLoadStatus() == LoadStatus.Success) {
      ChatMsgCache.removeMessage(fetchResult.getData().getMessageData().getMessage().getUuid());
      chatView.revokeMessage(fetchResult.getData());
      checkMultiSelectView();
      checkAudioPlayAndStop(fetchResult.getData());
    } else if (fetchResult.getLoadStatus() == LoadStatus.Error) {
      FetchResult.ErrorMsg errorMsg = fetchResult.getError();
      if (errorMsg != null) {
        ToastX.showShortToast(errorMsg.getRes());
      }
    }
  }

  protected void onDeleteMessage(FetchResult<List<ChatMessageBean>> fetchResult) {
    if (fetchResult.getLoadStatus() == LoadStatus.Success && fetchResult.getData() != null) {
      chatView.deleteMessage(fetchResult.getData());
      ChatMsgCache.removeMessages(fetchResult.getData());
      checkMultiSelectView();
      for (ChatMessageBean messageBean : fetchResult.getData()) {
        checkAudioPlayAndStop(messageBean);
      }
      if (chatView.getMessageList() == null || chatView.getMessageList().size() < 1) {
        viewModel.initFetch(null, false);
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
      chatView.getMessageListView().notifyUserInfoChange(fetchResult.getData());
      if (sessionType == SessionTypeEnum.P2P && fetchResult.getData() != null) {
        for (String account : fetchResult.getData()) {
          if (TextUtils.equals(account, sessionID)) {
            updateCurrentUserInfo();
          }
        }
      }
    }
  }

  protected void onQueryPinMessage(FetchResult<Map<String, MsgPinOption>> fetchResult) {
    if (fetchResult.getLoadStatus() == LoadStatus.Finish
        && fetchResult.getType() == FetchResult.FetchType.Update) {
      chatView.getMessageListView().updateMessagePin(fetchResult.getData());
    }
  }

  protected void onAddPin(Pair<String, MsgPinOption> pinOptionPair) {
    chatView.getMessageListView().addPinMessage(pinOptionPair.first, pinOptionPair.second);
  }

  protected void onRemovePin(String uuid) {
    chatView.getMessageListView().removePinMessage(uuid);
  }

  protected void onPickMedia(List<Uri> pickList) {
    if (pickList == null) return;
    for (int i = 0; i < pickList.size(); ++i) {
      Uri uri = pickList.get(i);
      ALog.d(LIB_TAG, LOG_TAG, "pick media result uri(" + i + ") -->> " + uri);
      mHandler.postDelayed(() -> viewModel.sendImageOrVideoMessage(uri), 100);
    }
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
      viewModel.sendImageOrVideoMessage(contentUri);
      captureTempImagePath = "";
    }
  }

  protected void onCaptureVideo(ActivityResult result) {
    if (result.getResultCode() == Activity.RESULT_OK && !TextUtils.isEmpty(captureTempVideoPath)) {
      File f = new File(captureTempVideoPath);
      Uri contentUri = Uri.fromFile(f);
      ALog.d(LIB_TAG, LOG_TAG, "capture video contentUri -->> " + contentUri);
      viewModel.sendImageOrVideoMessage(contentUri);
      captureTempVideoPath = "";
    }
  }

  protected void onForwardMessage(ActivityResult result, SessionTypeEnum sessionType) {
    if (result.getResultCode() != Activity.RESULT_OK) {
      return;
    }
    ALog.d(LIB_TAG, LOG_TAG, "forward Team result");
    Intent data = result.getData();
    if (data != null) {
      ArrayList<String> sessionList = new ArrayList<>();
      if (sessionType == SessionTypeEnum.P2P) {
        sessionList.addAll(data.getStringArrayListExtra(REQUEST_CONTACT_SELECTOR_KEY));
      } else {
        String tid = data.getStringExtra(RouterConstant.KEY_TEAM_ID);
        if (!TextUtils.isEmpty(tid)) {
          sessionList.add(tid);
        }
      }
      if (sessionList.size() > 0) {
        showForwardConfirmDialog(sessionType, sessionList);
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
    if (NetworkUtils.isConnected()) {
      chatView.setNetWorkState(true);
    } else {
      chatView.setNetWorkState(false);
    }
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
    if (sessionType != SessionTypeEnum.Team || !(viewModel instanceof ChatTeamViewModel)) {
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
    NIMClient.getService(AuthServiceObserver.class).observeOnlineStatus(loginObserver, false);
    if (chatView != null && chatView.getMessageListView() != null) {
      chatView.getMessageListView().release();
    }
    viewModel.clearChattingAccount();
    NetworkUtils.unregisterNetworkStatusChangedListener(networkStateListener);
    if (popMenu != null) {
      popMenu.hide();
    }
    viewModel.getUserInfoLiveData().removeObserver(userInfoLiveDataObserver);
    viewModel.getQueryMessageLiveData().removeObserver(messageLiveDataObserver);
    viewModel.getRecMessageLiveData().removeObserver(messageRecLiveDataObserver);
    viewModel.getAddPinMessageLiveData().removeObserver(addPinLiveDataObserver);
    viewModel.getRemovePinMessageLiveData().removeObserver(removePinLiveDataObserver);
    viewModel.getSendMessageLiveData().removeObserver(sendLiveDataObserver);
    viewModel.getRevokeMessageLiveData().removeObserver(revokeLiveDataObserver);
    viewModel.getAttachmentProgressMutableLiveData().removeObserver(attachLiveDataObserver);
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
