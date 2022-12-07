// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.page.fragment;

import static com.netease.yunxin.kit.chatkit.ui.view.message.MessageProperties.INT_NULL;
import static com.netease.yunxin.kit.corekit.im.utils.RouterConstant.REQUEST_CONTACT_SELECTOR_KEY;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import com.netease.nimlib.sdk.msg.attachment.MsgAttachment;
import com.netease.nimlib.sdk.msg.attachment.VideoAttachment;
import com.netease.nimlib.sdk.msg.constant.AttachStatusEnum;
import com.netease.nimlib.sdk.msg.constant.MsgStatusEnum;
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.AttachmentProgress;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.msg.model.MsgPinOption;
import com.netease.nimlib.sdk.msg.model.QueryDirectionEnum;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.storage.StorageType;
import com.netease.yunxin.kit.chatkit.storage.StorageUtil;
import com.netease.yunxin.kit.chatkit.ui.ChatKitClient;
import com.netease.yunxin.kit.chatkit.ui.ChatUIConfig;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.builder.IChatViewCustom;
import com.netease.yunxin.kit.chatkit.ui.common.MessageHelper;
import com.netease.yunxin.kit.chatkit.ui.databinding.ChatLayoutFragmentBinding;
import com.netease.yunxin.kit.chatkit.ui.dialog.ChatMessageForwardConfirmDialog;
import com.netease.yunxin.kit.chatkit.ui.dialog.ChatMessageForwardSelectDialog;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.chatkit.ui.page.WatchImageActivity;
import com.netease.yunxin.kit.chatkit.ui.page.WatchVideoActivity;
import com.netease.yunxin.kit.chatkit.ui.page.viewmodel.ChatBaseViewModel;
import com.netease.yunxin.kit.chatkit.ui.page.viewmodel.ChatP2PViewModel;
import com.netease.yunxin.kit.chatkit.ui.page.viewmodel.ChatTeamViewModel;
import com.netease.yunxin.kit.chatkit.ui.view.ait.AitManager;
import com.netease.yunxin.kit.chatkit.ui.view.interfaces.IMessageItemClickListener;
import com.netease.yunxin.kit.chatkit.ui.view.interfaces.IMessageLoadHandler;
import com.netease.yunxin.kit.chatkit.ui.view.interfaces.IMessageProxy;
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
import com.netease.yunxin.kit.corekit.im.IMKitClient;
import com.netease.yunxin.kit.corekit.im.model.UserInfo;
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** BaseFragment for Chat include P2P and Team chat page */
public abstract class ChatBaseFragment extends BaseFragment {

  private static final String LOG_TAG = "ChatBaseFragment";

  private final int REQUEST_PERMISSION = 0;
  private final int REQUEST_CAMERA_PERMISSION = 1;
  private final int REQUEST_VIDEO_PERMISSION = 2;
  private final int REQUEST_READ_EXTERNAL_STORAGE_PERMISSION = 3;

  private int currentRequest = 0;

  ChatBaseViewModel viewModel;
  AitManager aitManager;

  SessionTypeEnum sessionType = SessionTypeEnum.P2P;

  ChatMessageBean forwardMessage;

  ChatLayoutFragmentBinding binding;
  private ActivityResultLauncher<String> pickMediaLauncher;
  private String captureTempImagePath = "";
  private ActivityResultLauncher<Uri> takePictureLauncher;
  private String captureTempVideoPath = "";
  private ActivityResultLauncher<Intent> captureVideoLauncher;

  private ActivityResultLauncher<Intent> forwardP2PLauncher;

  private ActivityResultLauncher<Intent> forwardTeamLauncher;

  private ActivityResultLauncher<String[]> permissionLauncher;

  private Observer<FetchResult<List<ChatMessageBean>>> messageLiveDataObserver;
  private Observer<FetchResult<ChatMessageBean>> sendLiveDataObserver;
  private Observer<FetchResult<ChatMessageBean>> revokeLiveDataObserver;
  private Observer<FetchResult<AttachmentProgress>> attachLiveDataObserver;
  private Observer<FetchResult<List<UserInfo>>> userInfoLiveDataObserver;
  private Observer<Pair<String, MsgPinOption>> addPinLiveDataObserver;
  private Observer<String> removePinLiveDataObserver;

  ChatPopMenu popMenu;

  private IChatViewCustom chatViewCustom;

  private ChatUIConfig chatConfig;

  private IMessageItemClickListener delegateListener;

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    binding = ChatLayoutFragmentBinding.inflate(inflater, container, false);
    if (getArguments() != null) {
      initData(getArguments());
    }
    initView();
    initViewModel();
    initDataObserver();
    loadConfig();
    NetworkUtils.registerNetworkStatusChangedListener(networkStateListener);
    initCustom();
    return binding.getRoot();
  }

  public void setIMessageItemClickListener(IMessageItemClickListener clickListener) {
    delegateListener = clickListener;
  }

  protected void initView() {
    ALog.d(LOG_TAG, "initView");
    binding.chatView.getMessageListView().setPopActionListener(actionListener);
    binding.chatView.setMessageProxy(messageProxy);
    binding.chatView.setLoadHandler(loadHandler);
    binding.chatView.setMessageReader(message -> viewModel.sendReceipt(message.getMessage()));
    binding.chatView.setItemClickListener(itemClickListener);
    permissionLauncher =
        registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            result -> {
              if (result != null) {
                for (Map.Entry entry : result.entrySet()) {
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
                        permission, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                      startPickMedia();
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
  }

  private void loadConfig() {
    ChatUIConfig config = this.chatConfig;
    if (config == null) {
      config = ChatKitClient.getChatUIConfig();
      this.chatConfig = config;
    }
    if (config != null) {
      if (config.messageProperties != null) {
        binding.chatView.setMessageProperties(config.messageProperties);
        int titleBarVisible = config.messageProperties.showTitleBar ? View.VISIBLE : View.GONE;
        binding.chatView.getTitleBar().setVisibility(titleBarVisible);
        int settingVisible =
            config.messageProperties.showTitleBarRightIcon ? View.VISIBLE : View.GONE;
        binding.chatView.getTitleBar().setRightImageViewVisible(settingVisible);
        if (config.messageProperties.titleBarRightRes != INT_NULL) {
          binding
              .chatView
              .getTitleBar()
              .getRightImageView()
              .setImageResource(config.messageProperties.titleBarRightRes);
        }
        if (config.messageProperties.titleBarRightClick != null) {
          binding
              .chatView
              .getTitleBar()
              .setActionListener(config.messageProperties.titleBarRightClick);
        }

        if (config.messageProperties.chatViewBackground != null) {
          binding.chatView.setMessageBackground(config.messageProperties.chatViewBackground);
        }
      }

      if (config.chatFactory != null) {
        binding.chatView.setMessageViewHolderFactory(config.chatFactory);
      }

      if (config.chatViewCustom != null) {
        binding.chatView.setLayoutCustom(config.chatViewCustom);
      }

      if (config.chatPopMenu != null) {
        binding.chatView.getMessageListView().setChatPopMenu(config.chatPopMenu);
      }

      delegateListener = config.messageItemClickListener;
    }
  }

  protected void initCustom() {
    ALog.d(LOG_TAG, "initCustom");
    if (chatViewCustom != null) {
      binding.chatView.setLayoutCustom(chatViewCustom);
    }
    binding.chatView.setShowReadStatus(viewModel.isShowReadStatus());
  }

  private final IMessageProxy messageProxy =
      new IMessageProxy() {
        @Override
        public boolean sendTextMessage(String msg, ChatMessageBean replyMsg) {
          List<String> pushList = null;
          if (aitManager != null && sessionType == SessionTypeEnum.Team) {
            pushList = aitManager.getAitTeamMember();
          }
          if (replyMsg == null) {
            viewModel.sendTextMessage(msg, pushList);
          } else {
            viewModel.replyTextMessage(msg, replyMsg.getMessageData().getMessage(), pushList);
          }
          if (aitManager != null) {
            aitManager.reset();
          }
          return true;
        }

        @Override
        public void pickMedia() {
          if (PermissionUtils.hasPermissions(
              ChatBaseFragment.this.getContext(), Manifest.permission.READ_EXTERNAL_STORAGE)) {
            startPickMedia();
          } else {
            requestCameraPermission(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                REQUEST_READ_EXTERNAL_STORAGE_PERMISSION);
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
          //todo send file
          return false;
        }

        @Override
        public boolean sendAudio(File audioFile, long audioLength, ChatMessageBean replyMsg) {
          //audio not support reply
          viewModel.sendAudioMessage(audioFile, audioLength);
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
      };

  private void requestCameraPermission(String permission, int request) {
    currentRequest = request;
    permissionLauncher.launch(new String[] {permission});
  }

  public String getPermissionText(String permission) {
    String text = this.getContext().getString(R.string.permission_default);
    if (TextUtils.equals(permission, Manifest.permission.CAMERA)) {
      text = this.getContext().getString(R.string.permission_camera);
    } else if (TextUtils.equals(permission, Manifest.permission.READ_EXTERNAL_STORAGE)) {
      text = this.getContext().getString(R.string.permission_storage);
    } else if (TextUtils.equals(permission, Manifest.permission.RECORD_AUDIO)) {
      text = this.getContext().getString(R.string.permission_audio);
    }
    return text;
  }

  private void startTakePicture() {
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

  private void startPickMedia() {
    pickMediaLauncher.launch("image/*;video/*");
  }

  private void startCaptureVideo() {
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

  private final IMessageItemClickListener itemClickListener =
      new IMessageItemClickListener() {
        @Override
        public boolean onMessageLongClick(View view, int position, ChatMessageBean messageBean) {
          if (delegateListener == null
              || !delegateListener.onMessageLongClick(view, position, messageBean)) {
            if (messageBean.isRevoked()) {
              return false;
            }
            //show pop menu
            if (popMenu == null) {
              popMenu = new ChatPopMenu();
            }
            if (popMenu.isShowing()) {
              return true;
            }
            int[] location = new int[2];
            binding.chatView.getMessageListView().getLocationOnScreen(location);
            popMenu.show(view, messageBean, location[1]);
          }
          return true;
        }

        @Override
        public boolean onMessageClick(View view, int position, ChatMessageBean messageBean) {
          if (delegateListener == null
              || !delegateListener.onMessageClick(view, position, messageBean)) {

            if (messageBean.getViewType() == MsgTypeEnum.image.getValue()) {
              watchImage(
                  messageBean,
                  binding
                      .chatView
                      .getMessageListView()
                      .filterMessagesByType(messageBean.getViewType()));
            } else if (messageBean.getViewType() == MsgTypeEnum.video.getValue()) {
              watchVideo(messageBean.getMessageData().getMessage());
            }
          }
          return true;
        }

        @Override
        public boolean onUserIconClick(View view, int position, ChatMessageBean messageBean) {
          if (delegateListener == null
              || !delegateListener.onUserIconClick(view, position, messageBean)) {
            XKitRouter.withKey(RouterConstant.PATH_USER_INFO_PAGE)
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
            //todo
          }
          return true;
        }

        @Override
        public boolean onSelfIconLongClick(View view, int position, ChatMessageBean messageInfo) {
          if (delegateListener == null
              || !delegateListener.onSelfIconLongClick(view, position, messageInfo)) {
            //todo
          }
          return true;
        }

        @Override
        public boolean onReEditRevokeMessage(View view, int position, ChatMessageBean messageBean) {
          //only support text message
          if (delegateListener == null
              || !delegateListener.onReEditRevokeMessage(view, position, messageBean)) {
            if (messageBean.getMessageData().getMessage().getMsgType() == MsgTypeEnum.text) {
              binding
                  .chatView
                  .getInputView()
                  .setReEditMessage(messageBean.getMessageData().getMessage().getContent());
            }
          }
          return true;
        }

        @Override
        public boolean onReplyMessageClick(View view, int position, String replyUUid) {
          //scroll to the message position
          if (delegateListener == null
              || !delegateListener.onReplyMessageClick(view, position, replyUUid)) {
            binding.chatView.getMessageListView().scrollToMessage(replyUUid);
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
          if (delegateListener == null
              || !delegateListener.onTextSelected(view, position, messageInfo)) {
            //todo
          }
          return false;
        }
      };

  private final IMessageLoadHandler loadHandler =
      new IMessageLoadHandler() {
        @Override
        public void loadMoreForward(ChatMessageBean messageBean) {
          viewModel.fetchMoreMessage(
              messageBean.getMessageData().getMessage(), QueryDirectionEnum.QUERY_OLD);
        }

        @Override
        public void loadMoreBackground(ChatMessageBean messageBean) {
          viewModel.fetchMoreMessage(
              messageBean.getMessageData().getMessage(), QueryDirectionEnum.QUERY_NEW);
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
          ClipboardManager cmb =
              (ClipboardManager)
                  IMKitClient.getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
          ClipData clipData = null;
          if (messageBean.getMessageData().getMessage().getMsgType() == MsgTypeEnum.text) {
            clipData =
                ClipData.newPlainText(null, messageBean.getMessageData().getMessage().getContent());
          }
          cmb.setPrimaryClip(clipData);
          ToastX.showShortToast(R.string.chat_message_action_copy_success);
          return true;
        }

        @Override
        public boolean onReply(ChatMessageBean messageBean) {
          if (chatConfig != null
              && chatConfig.popMenuClickListener != null
              && chatConfig.popMenuClickListener.onReply(messageBean)) {
            return true;
          }
          if (aitManager != null && sessionType == SessionTypeEnum.Team) {
            String account = messageBean.getMessageData().getMessage().getFromAccount();
            if (!TextUtils.equals(account, IMKitClient.account())) {
              String name =
                  MessageHelper.getTeamAitName(
                      aitManager.getTid(), messageBean.getMessageData().getFromUser());
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
          binding.chatView.getInputView().setReplyMessage(messageBean);
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
          ChatMessageForwardSelectDialog dialog = new ChatMessageForwardSelectDialog();
          dialog.setSelectedCallback(
              new ChatMessageForwardSelectDialog.ForwardTypeSelectedCallback() {
                @Override
                public void onTeamSelected() {
                  startTeamList();
                }

                @Override
                public void onP2PSelected() {
                  startP2PSelector();
                }
              });
          dialog.show(getParentFragmentManager(), ChatMessageForwardSelectDialog.TAG);
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
          //todo
          if (chatConfig != null
              && chatConfig.popMenuClickListener != null
              && chatConfig.popMenuClickListener.onMultiSelected(messageBean)) {
            return true;
          }
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
          showDeleteConfirmDialog(message);
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
          if (chatConfig != null
              && chatConfig.popMenuClickListener != null
              && chatConfig.popMenuClickListener.onCustom(view, messageInfo, action)) {
            return true;
          }
          return false;
        }
      };

  private void showDeleteConfirmDialog(ChatMessageBean message) {
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
                viewModel.deleteMessage(message.getMessageData());
                binding.chatView.getMessageListView().deleteMessage(message);
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

  private void watchImage(ChatMessageBean messageBean, ArrayList<ChatMessageBean> imageMessages) {
    int index = 0;
    ArrayList<IMMessage> messages = new ArrayList<>();
    for (int i = 0; i < imageMessages.size(); ++i) {
      if (messageBean.equals(imageMessages.get(i))) {
        index = i;
      }
      messages.add(imageMessages.get(i).getMessageData().getMessage());
    }
    WatchImageActivity.launch(getContext(), messages, index);
  }

  private void watchVideo(IMMessage message) {
    if (message.getAttachStatus() == AttachStatusEnum.transferred
        && !TextUtils.isEmpty(((VideoAttachment) message.getAttachment()).getPath())) {
      WatchVideoActivity.launch(getContext(), message);
    } else if (message.getAttachStatus() != AttachStatusEnum.transferring) {
      viewModel.downloadMessageAttachment(message);
    }
  }

  protected abstract void initViewModel();

  protected void initDataObserver() {
    ALog.d(LOG_TAG, "initDataObserver");

    messageLiveDataObserver =
        listFetchResult -> {
          boolean hasMore = listFetchResult.getLoadStatus() != LoadStatus.Finish;
          if (listFetchResult.getTypeIndex() == 0) {
            ALog.d(LOG_TAG, "message observe older forward has more:" + hasMore);
            binding.chatView.getMessageListView().setHasMoreForwardMessages(hasMore);
            binding.chatView.addMessageListForward(listFetchResult.getData());
          } else {
            ALog.d(LOG_TAG, "message observe newer load has more:" + hasMore);
            binding.chatView.getMessageListView().setHasMoreNewerMessages(hasMore);
            binding.chatView.appendMessageList(listFetchResult.getData());
          }
        };
    viewModel.getQueryMessageLiveData().observeForever(messageLiveDataObserver);

    sendLiveDataObserver =
        chatMessageBeanFetchResult -> {
          if (chatMessageBeanFetchResult.getType() == FetchResult.FetchType.Add) {
            ALog.d(LOG_TAG, "send message add");
            if (binding.chatView.getMessageListView().hasMoreNewerMessages()) {
              binding.chatView.clearMessageList();
              binding.chatView.appendMessage(chatMessageBeanFetchResult.getData());
              if (chatMessageBeanFetchResult.getData() != null) {
                binding.chatView.getMessageListView().setHasMoreNewerMessages(false);
                viewModel.fetchMoreMessage(
                    chatMessageBeanFetchResult.getData().getMessageData().getMessage(),
                    QueryDirectionEnum.QUERY_OLD);
              }
            } else {
              binding.chatView.appendMessage(chatMessageBeanFetchResult.getData());
            }
          } else {
            binding.chatView.updateMessage(chatMessageBeanFetchResult.getData());
          }
        };
    viewModel.getSendMessageLiveData().observeForever(sendLiveDataObserver);

    attachLiveDataObserver =
        attachmentProgressFetchResult -> {
          binding.chatView.updateProgress(attachmentProgressFetchResult.getData());
        };
    viewModel.getAttachmentProgressMutableLiveData().observeForever(attachLiveDataObserver);

    revokeLiveDataObserver =
        messageResult -> {
          if (messageResult.getLoadStatus() == LoadStatus.Success) {
            binding.chatView.getMessageListView().revokeMessage(messageResult.getData());
          } else if (messageResult.getLoadStatus() == LoadStatus.Error) {
            FetchResult.ErrorMsg errorMsg = messageResult.getError();
            if (errorMsg != null) {
              ToastX.showShortToast(errorMsg.getRes());
            }
          }
        };
    viewModel.getRevokeMessageLiveData().observeForever(revokeLiveDataObserver);

    userInfoLiveDataObserver =
        userResult -> {
          if (userResult.getLoadStatus() == LoadStatus.Finish
              && userResult.getType() == FetchResult.FetchType.Update) {
            binding.chatView.getMessageListView().updateUserInfo(userResult.getData());
          }
        };
    viewModel.getUserInfoLiveData().observeForever(userInfoLiveDataObserver);

    addPinLiveDataObserver =
        responseOption ->
            binding
                .chatView
                .getMessageListView()
                .addPinMessage(responseOption.first, responseOption.second);
    viewModel.getAddPinMessageLiveData().observeForever(addPinLiveDataObserver);

    removePinLiveDataObserver =
        uuid -> {
          binding.chatView.getMessageListView().removePinMessage(uuid);
        };
    viewModel.getRemovePinMessageLiveData().observeForever(removePinLiveDataObserver);

    pickMediaLauncher =
        registerForActivityResult(
            new ActivityResultContracts.GetMultipleContents(),
            result -> {
              for (int i = 0; i < result.size(); ++i) {
                Uri uri = result.get(i);
                ALog.d(LOG_TAG, "pick media result uri(" + i + ") -->> " + uri);
                viewModel.sendImageOrVideoMessage(uri);
              }
            });
    takePictureLauncher =
        registerForActivityResult(
            new ActivityResultContracts.TakePicture(),
            result -> {
              if (result && !TextUtils.isEmpty(captureTempImagePath)) {
                File f = new File(captureTempImagePath);
                Uri contentUri = Uri.fromFile(f);
                ALog.d(LOG_TAG, "take picture contentUri -->> " + contentUri);
                viewModel.sendImageOrVideoMessage(contentUri);
                captureTempImagePath = "";
              }
            });
    captureVideoLauncher =
        registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
              if (result.getResultCode() == Activity.RESULT_OK
                  && !TextUtils.isEmpty(captureTempVideoPath)) {
                File f = new File(captureTempVideoPath);
                Uri contentUri = Uri.fromFile(f);
                ALog.d(LOG_TAG, "capture video contentUri -->> " + contentUri);
                viewModel.sendImageOrVideoMessage(contentUri);
                captureTempVideoPath = "";
              }
            });

    forwardP2PLauncher =
        registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
              if (result.getResultCode() != Activity.RESULT_OK || forwardMessage == null) {
                return;
              }
              ALog.d(LOG_TAG, "forward P2P result");
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
              ALog.d(LOG_TAG, "forward Team result");
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
  }

  private void showForwardConfirmDialog(SessionTypeEnum type, ArrayList<String> sessionIds) {
    ChatMessageForwardConfirmDialog confirmDialog = new ChatMessageForwardConfirmDialog();
    Bundle bundle = new Bundle();
    bundle.putInt(ChatMessageForwardConfirmDialog.FORWARD_TYPE, type.getValue());
    bundle.putStringArrayList(ChatMessageForwardConfirmDialog.FORWARD_SESSION_LIST, sessionIds);
    String sendName =
        forwardMessage.getMessageData().getFromUser() == null
            ? forwardMessage.getMessageData().getMessage().getFromAccount()
            : forwardMessage.getMessageData().getFromUser().getName();
    bundle.putString(ChatMessageForwardConfirmDialog.FORWARD_MESSAGE_SEND, sendName);
    confirmDialog.setArguments(bundle);
    confirmDialog.setCallback(
        () -> {
          if (forwardMessage != null) {
            for (String accId : sessionIds) {
              viewModel.sendForwardMessage(
                  forwardMessage.getMessageData().getMessage(), accId, type);
            }
          }
        });
    confirmDialog.show(getParentFragmentManager(), ChatMessageForwardConfirmDialog.TAG);
  }

  private void startTeamList() {
    XKitRouter.withKey(RouterConstant.PATH_MY_TEAM_PAGE)
        .withParam(RouterConstant.KEY_TEAM_LIST_SELECT, true)
        .withContext(requireContext())
        .navigate(forwardTeamLauncher);
  }

  private void startP2PSelector() {
    ArrayList<String> filterList = new ArrayList<>();
    if (sessionType == SessionTypeEnum.P2P) {
      filterList.add(viewModel.getSessionId());
    }
    XKitRouter.withKey(RouterConstant.PATH_CONTACT_SELECTOR_PAGE)
        .withParam(RouterConstant.KEY_CONTACT_SELECTOR_MAX_COUNT, 6)
        .withContext(requireContext())
        .withParam(RouterConstant.SELECTOR_CONTACT_FILTER_KEY, filterList)
        .navigate(forwardP2PLauncher);
  }

  protected abstract void initData(Bundle bundle);

  @Override
  public void onResume() {
    super.onResume();
    ALog.d(LOG_TAG, "onResume");
    viewModel.setChattingAccount();
  }

  @Override
  public void onPause() {
    super.onPause();
    ALog.d(LOG_TAG, "onPause");
    viewModel.clearChattingAccount();
  }

  public void onNewIntent(Intent intent) {
    ALog.d(LOG_TAG, "onNewIntent");
  }

  private final NetworkUtils.NetworkStateListener networkStateListener =
      new NetworkUtils.NetworkStateListener() {
        @Override
        public void onAvailable(NetworkInfo network) {
          binding.chatView.setNetWorkState(true);
        }

        @Override
        public void onLost(NetworkInfo network) {
          binding.chatView.setNetWorkState(false);
        }
      };

  @Override
  public void onDestroyView() {
    ALog.d(LOG_TAG, "onDestroyView");
    super.onDestroyView();
    NetworkUtils.unregisterNetworkStatusChangedListener(networkStateListener);
    if (popMenu != null) {
      popMenu.hide();
    }
    viewModel.getUserInfoLiveData().removeObserver(userInfoLiveDataObserver);
    viewModel.getQueryMessageLiveData().removeObserver(messageLiveDataObserver);
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
}
