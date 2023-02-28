// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.qchatkit.ui.message;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import com.netease.nimlib.sdk.msg.attachment.ImageAttachment;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.common.ui.dialog.CommonAlertDialog;
import com.netease.yunxin.kit.common.ui.fragments.BaseFragment;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.common.utils.CommonFileProvider;
import com.netease.yunxin.kit.common.utils.ImageUtils;
import com.netease.yunxin.kit.common.utils.NetworkUtils;
import com.netease.yunxin.kit.common.utils.PermissionUtils;
import com.netease.yunxin.kit.corekit.im.IMKitClient;
import com.netease.yunxin.kit.corekit.im.provider.FetchCallback;
import com.netease.yunxin.kit.qchatkit.repo.model.QChatChannelInfo;
import com.netease.yunxin.kit.qchatkit.repo.model.QChatMessageInfo;
import com.netease.yunxin.kit.qchatkit.ui.R;
import com.netease.yunxin.kit.qchatkit.ui.databinding.QChatChannelMessageFragmentBinding;
import com.netease.yunxin.kit.qchatkit.ui.message.audio.QChatMessageAudioControl;
import com.netease.yunxin.kit.qchatkit.ui.message.interfaces.IMessageLoadHandler;
import com.netease.yunxin.kit.qchatkit.ui.message.interfaces.IMessageOptionCallBack;
import com.netease.yunxin.kit.qchatkit.ui.message.interfaces.IMessageProxy;
import com.netease.yunxin.kit.qchatkit.ui.message.utils.SendImageHelper;
import com.netease.yunxin.kit.qchatkit.ui.message.view.PhotoPickerDialog;
import com.netease.yunxin.kit.qchatkit.ui.message.view.QChatMessageListView;
import com.netease.yunxin.kit.qchatkit.ui.model.QChatConstant;
import com.netease.yunxin.kit.qchatkit.ui.utils.FileUtils;
import java.io.File;
import java.util.List;
import java.util.Map;

/** channel message page to send text message and image message */
public class QChatChannelMessageFragment extends BaseFragment {

  public static final String TAG = "QChatChannelMessageFragment";
  private final int REQUEST_PERMISSION = 0;
  private int currentRequest = 0;

  private QChatChannelMessageFragmentBinding viewBinding;
  private MessageViewModel viewModel;
  private ActivityResultLauncher<Intent> activityResultLauncher;
  private File tempFile;
  private PhotoPickerDialog photoPickerDialog;
  private long serverId;
  private long channelId;
  private ActivityResultLauncher<String[]> permissionLauncher;
  private static final int COPY_SHOW_TIME = 1000;
  private static final int AUDIO_MIN_TIME = 1000;
  private final Observer<FetchResult<List<QChatMessageInfo>>> queryMessageObserver =
      result -> {
        if (result.getLoadStatus() == LoadStatus.Success) {
          viewBinding.qChatMessageListRecyclerView.appendMessages(result.getData());
        } else if (result.getLoadStatus() == LoadStatus.Finish) {
          if (result.getType() == FetchResult.FetchType.Add) {
            if (result.getTypeIndex() == -1) {
              viewBinding.qChatMessageListRecyclerView.appendMessages(result.getData());
            } else {
              viewBinding.qChatMessageListRecyclerView.addMessagesForward(result.getData());
              viewBinding.qChatMessageListRecyclerView.setHasMoreForwardMessages(
                  viewModel.isHasForward());
            }
          } else if (result.getType() == FetchResult.FetchType.Remove) {
            //todo delete message
          } else if (result.getType() == FetchResult.FetchType.Update) {

          }
        }
      };

  private final Observer<FetchResult<QChatMessageInfo>> sendMessageObserver =
      result -> {
        if (result.getLoadStatus() == LoadStatus.Success) {
          ALog.d(TAG, "SendMessageLiveData", "Success");
          viewBinding.qChatMessageListRecyclerView.updateMessageStatus(result.getData());
        } else if (result.getLoadStatus() == LoadStatus.Error) {
          if (result.getError() != null
              && result.getError().getCode() == QChatConstant.ERROR_CODE_IM_NO_PERMISSION) {
            ALog.d(TAG, "SendMessageLiveData", "Error 403");
            showPermissionErrorDialog();
            if (result.getData() != null) {
              viewBinding.qChatMessageListRecyclerView.deleteMessage(result.getData());
            }
          } else {
            if (result.getData() != null) {
              viewBinding.qChatMessageListRecyclerView.updateMessageStatus(result.getData());
            }
          }
          ALog.d(TAG, "SendMessageLiveData", "Error");
        }
      };

  private final NetworkUtils.NetworkStateListener networkStateListener =
      new NetworkUtils.NetworkStateListener() {
        @Override
        public void onAvailable(NetworkInfo network) {
          if (viewBinding == null) {
            return;
          }
          viewBinding.networkTip.setVisibility(View.GONE);
        }

        @Override
        public void onLost(NetworkInfo network) {
          if (viewBinding == null) {
            return;
          }
          viewBinding.networkTip.setVisibility(View.VISIBLE);
        }
      };

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    ALog.d(TAG, "onCreateView");
    viewBinding = QChatChannelMessageFragmentBinding.inflate(inflater, container, false);
    return viewBinding.getRoot();
  }

  public void init(long serverId, long channelId) {
    ALog.d(TAG, "init", "info:" + serverId + "," + channelId);
    this.serverId = serverId;
    this.channelId = channelId;
  }

  public void updateChannelInfo(QChatChannelInfo channelInfo) {
    if (channelInfo != null) {
      ALog.d(TAG, "updateChannelInfo", "info:" + channelInfo.toString());
      String hintText =
          String.format(
              getResources().getString(R.string.qchat_channel_message_send_hint),
              channelInfo.getName());
      viewBinding.qChatMessageBottomLayout.getViewBinding().chatMessageInputEt.setHint(hintText);
    }
  }

  @Override
  public void onStop() {
    super.onStop();
    QChatMessageAudioControl.getInstance().stopAudio();
  }

  private final IMessageProxy messageProxy =
      new IMessageProxy() {
        @Override
        public boolean sendTextMessage(String msg) {
          ALog.d(TAG, "sendTextMessage", "info:" + msg);
          QChatMessageInfo messageInfo = viewModel.sendTextMessage(msg);
          viewBinding.qChatMessageListRecyclerView.appendMessage(messageInfo);
          return true;
        }

        @Override
        public boolean sendImage() {
          ALog.d(TAG, "sendImage");
          photoPicker();
          return true;
        }

        @Override
        public boolean sendFile() {
          Toast.makeText(getActivityContext(), R.string.qchat_develop_text, Toast.LENGTH_SHORT)
              .show();
          return false;
        }

        @Override
        public boolean sendEmoji() {
          Toast.makeText(getActivityContext(), R.string.qchat_develop_text, Toast.LENGTH_SHORT)
              .show();
          return false;
        }

        @Override
        public boolean sendVoice() {
          Toast.makeText(getActivityContext(), R.string.qchat_develop_text, Toast.LENGTH_SHORT)
              .show();
          return false;
        }

        @Override
        public boolean hasPermission(String permission) {
          if (TextUtils.isEmpty(permission)) {
            return false;
          }
          if (PermissionUtils.hasPermissions(
              QChatChannelMessageFragment.this.getContext(), permission)) {
            return true;
          } else {
            requestCameraPermission(permission, REQUEST_PERMISSION);
            return false;
          }
        }

        @Override
        public boolean pickMedia() {
          photoPicker();
          return false;
        }

        @Override
        public boolean takePicture() {
          return false;
        }

        @Override
        public boolean captureVideo() {
          return false;
        }

        @Override
        public void onInputPanelExpand() {
          Toast.makeText(getActivityContext(), R.string.qchat_develop_text, Toast.LENGTH_SHORT)
              .show();
        }

        @Override
        public boolean sendAudio(File audioFile, long audioLength) {
          if (audioLength < AUDIO_MIN_TIME) {
            Toast.makeText(
                    getActivityContext(),
                    R.string.qchat_pressed_audio_too_short,
                    Toast.LENGTH_SHORT)
                .show();
          } else {
            QChatMessageInfo messageInfo = viewModel.sendVoiceMessage(audioFile, audioLength);
            viewBinding.qChatMessageListRecyclerView.appendMessage(messageInfo);
          }
          return true;
        }

        @Override
        public void shouldCollapseInputPanel() {}

        @Override
        public String getAccount() {
          return IMKitClient.account();
        }

        @Override
        public Context getActivityContext() {
          return QChatChannelMessageFragment.this.getContext();
        }
      };

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    viewModel = new ViewModelProvider(this).get(MessageViewModel.class);
    viewModel.init(serverId, channelId);
    viewBinding.qChatMessageListRecyclerView.addOnLayoutChangeListener(
        (v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
          if (bottom < oldBottom) {
            viewBinding.qChatMessageListRecyclerView.scrollBy(0, oldBottom - bottom);
          }
        });
    viewBinding.qChatMessageListRecyclerView.setOnListViewEventListener(
        new QChatMessageListView.OnListViewEventListener() {
          @Override
          public void onListViewStartScroll() {
            viewBinding.qChatMessageBottomLayout.collapse(true);
          }

          @Override
          public void onListViewTouched() {
            viewBinding.qChatMessageBottomLayout.collapse(true);
          }
        });
    viewBinding.qChatMessageBottomLayout.init(messageProxy);
    viewBinding.qChatMessageListRecyclerView.setOnTouchListener(
        (v, event) -> {
          viewBinding.qChatMessageBottomLayout.collapse(true);
          return false;
        });
    viewBinding.qChatMessageListRecyclerView.setLoadHandler(
        new IMessageLoadHandler() {
          @Override
          public boolean loadMoreForward(QChatMessageInfo messageInfo) {
            viewModel.fetchForwardMessage(messageInfo);
            return true;
          }

          @Override
          public boolean loadMoreBackground(QChatMessageInfo messageInfo) {
            viewModel.fetchBackwardMessage(messageInfo);
            return true;
          }
        });
    viewBinding.qChatMessageListRecyclerView.setOptionCallback(
        new IMessageOptionCallBack() {
          @Override
          public void onRead(QChatMessageInfo message) {
            viewModel.makeMessageRead(message);
          }

          @Override
          public void reSend(QChatMessageInfo message) {
            //todo resend
          }

          @Override
          public void onCopy(QChatMessageInfo message) {
            showCopyTip();
          }
        });
    viewModel.getQueryMessageLiveData().observeForever(queryMessageObserver);

    viewModel.getSendMessageLiveData().observeForever(sendMessageObserver);
    activityResultLauncher =
        registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
              if (result.getResultCode() == Activity.RESULT_OK) {
                String path = null;
                if (result.getData() == null) {
                  path = tempFile.getPath();
                  ALog.d(TAG, "activityResultLauncher", "info:" + path);
                  QChatMessageInfo messageInfo =
                      viewModel.sendImageMessage(createImageMessage(path));
                  viewBinding.qChatMessageListRecyclerView.appendMessage(messageInfo);
                } else {
                  Uri imageUri = result.getData().getData();
                  ALog.d(TAG, "activityResultLauncher", "intent info:" + imageUri.getPath());
                  new SendImageHelper.SendImageTask(
                          getActivity(),
                          imageUri,
                          (filePath, isOrig) -> {
                            QChatMessageInfo messageInfo =
                                viewModel.sendImageMessage(createImageMessage(filePath));
                            viewBinding.qChatMessageListRecyclerView.appendMessage(messageInfo);
                          })
                      .execute();
                }
              }
            });

    permissionLauncher =
        registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            result -> {
              if (result != null) {
                for (Map.Entry entry : result.entrySet()) {
                  String permission = entry.getKey().toString();
                  boolean grant = (Boolean) entry.getValue();
                  if (!grant) {
                    if (shouldShowRequestPermissionRationale(permission)) {

                      ToastX.showShortToast(
                          getResources().getString(R.string.qchat_permission_deny_tips));
                    } else {

                      ToastX.showShortToast(getPermissionText(permission));
                    }
                  }
                }
              }
            });
    NetworkUtils.registerNetworkStatusChangedListener(networkStateListener);
    if (NetworkUtils.isConnected()) {
      viewModel.fetchMessageList();
    } else {
      viewModel.loadMessageCache();
    }
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    NetworkUtils.unregisterNetworkStatusChangedListener(networkStateListener);
    viewModel.getSendMessageLiveData().removeObserver(sendMessageObserver);
    viewModel.getQueryMessageLiveData().removeObserver(queryMessageObserver);
  }

  private void photoPicker() {

    if (photoPickerDialog == null) {
      photoPickerDialog = new PhotoPickerDialog(getActivity());
    }

    photoPickerDialog.show(
        new FetchCallback<Integer>() {
          @Override
          public void onSuccess(@Nullable Integer param) {
            if (param == 0) {
              File file = FileUtils.getTempFile(getActivity(), null);
              Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
              Uri uri;
              if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                uri = CommonFileProvider.Companion.getUriForFile(getActivity(), file);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
              } else {
                uri = Uri.fromFile(file);
              }
              tempFile = file;
              ALog.d(TAG, "photoPickerDialog", "info:" + param + "," + uri.getPath());
              intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
              activityResultLauncher.launch(intent);
            } else if (param == 1) {
              ALog.d(TAG, "photoPickerDialog", "info:" + param);
              Intent intent =
                  new Intent(
                      Intent.ACTION_PICK,
                      android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
              intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
              activityResultLauncher.launch(intent);
            }
            photoPickerDialog.dismiss();
          }

          @Override
          public void onFailed(int code) {
            photoPickerDialog.dismiss();
          }

          @Override
          public void onException(@Nullable Throwable exception) {
            photoPickerDialog.dismiss();
          }
        });
  }

  protected ImageAttachment createImageMessage(String path) {
    ImageAttachment imageAttachment = new ImageAttachment();
    imageAttachment.setPath(path);
    File imageFile = new File(path);
    if (imageFile.exists()) {
      imageAttachment.setSize(imageFile.length());
      int[] dimension = ImageUtils.getSize(path);
      imageAttachment.setWidth(dimension[0]);
      imageAttachment.setHeight(dimension[1]);
    }
    ALog.d(TAG, "createImageMessage", "info:" + path);
    return imageAttachment;
  }

  private void showCopyTip() {
    viewBinding.cvCopyTip.setVisibility(View.VISIBLE);
    viewBinding.cvCopyTip.postDelayed(
        () -> {
          viewBinding.cvCopyTip.setVisibility(View.GONE);
        },
        COPY_SHOW_TIME);
  }

  private void showPermissionErrorDialog() {
    CommonAlertDialog commonDialog = new CommonAlertDialog();
    commonDialog
        .setContentStr(getString(R.string.qchat_no_permission_content))
        .setPositiveStr(getString(R.string.qchat_ensure))
        .setConfirmListener(() -> {})
        .show(getActivity().getSupportFragmentManager());
  }

  private void requestCameraPermission(String permission, int request) {
    currentRequest = request;
    permissionLauncher.launch(new String[] {permission});
  }

  public String getPermissionText(String permission) {
    String text = this.getContext().getString(R.string.qchat_permission_default);
    if (TextUtils.equals(permission, Manifest.permission.CAMERA)) {
      text = this.getContext().getString(R.string.qchat_permission_camera);
    } else if (TextUtils.equals(permission, Manifest.permission.READ_EXTERNAL_STORAGE)) {
      text = this.getContext().getString(R.string.qchat_permission_storage);
    } else if (TextUtils.equals(permission, Manifest.permission.RECORD_AUDIO)) {
      text = this.getContext().getString(R.string.qchat_permission_audio);
    }
    return text;
  }
}
