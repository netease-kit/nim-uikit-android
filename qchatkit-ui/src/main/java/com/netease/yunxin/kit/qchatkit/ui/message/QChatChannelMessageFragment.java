/*
 * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.kit.qchatkit.ui.message;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.netease.nimlib.sdk.msg.attachment.ImageAttachment;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.common.ui.dialog.CommonAlertDialog;
import com.netease.yunxin.kit.common.ui.fragments.BaseFragment;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.corekit.im.IMKitClient;
import com.netease.yunxin.kit.corekit.im.provider.FetchCallback;
import com.netease.yunxin.kit.qchatkit.repo.model.QChatChannelInfo;
import com.netease.yunxin.kit.qchatkit.repo.model.QChatMessageInfo;
import com.netease.yunxin.kit.qchatkit.ui.R;
import com.netease.yunxin.kit.qchatkit.ui.common.NetworkUtils;
import com.netease.yunxin.kit.qchatkit.ui.databinding.QChatChannelMessageFragmentBinding;
import com.netease.yunxin.kit.qchatkit.ui.message.interfaces.IMessageLoadHandler;
import com.netease.yunxin.kit.qchatkit.ui.message.interfaces.IMessageOptionCallBack;
import com.netease.yunxin.kit.qchatkit.ui.message.interfaces.IMessageProxy;
import com.netease.yunxin.kit.qchatkit.ui.message.utils.SendImageHelper;
import com.netease.yunxin.kit.qchatkit.ui.message.view.MessageInputLayout;
import com.netease.yunxin.kit.qchatkit.ui.message.view.PhotoPickerDialog;
import com.netease.yunxin.kit.qchatkit.ui.model.QChatConstant;
import com.netease.yunxin.kit.qchatkit.ui.utils.FileUtils;

import java.io.File;

/**
 * channel message page
 * to send text message and image message
 */
public class QChatChannelMessageFragment extends BaseFragment {

    public static final String TAG = "QChatChannelMessageFragment";
    private QChatChannelMessageFragmentBinding viewBinding;
    private MessageViewModel viewModel;
    private MessageInputLayout bottomBarLayout;
    private ActivityResultLauncher<Intent> activityResultLauncher;
    private File tempFile;
    private PhotoPickerDialog photoPickerDialog;
    private long serverId;
    private long channelId;
    private static final int COPY_SHOW_TIME = 1000;

    private final NetworkUtils.NetworkStateListener networkStateListener = new NetworkUtils.NetworkStateListener() {
        @Override
        public void onAvailable(NetworkInfo network) {
            if (viewBinding == null) {
                return;
            }
            viewBinding.networkTip.getRoot().setVisibility(View.GONE);

        }

        @Override
        public void onLost(NetworkInfo network) {
            if (viewBinding == null) {
                return;
            }
            viewBinding.networkTip.getRoot().setVisibility(View.VISIBLE);
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
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
            String hintText = String.format(getResources().getString(R.string.qchat_channel_message_send_hint), channelInfo.getName());
            viewBinding.qChatMessageBottomLayout.qChatMessageInputEt.setHint(hintText);
        }
    }

    private final IMessageProxy messageProxy = new IMessageProxy() {
        @Override
        public boolean sendTextMessage(String msg) {
            ALog.d(TAG, "sendTextMessage", "info:" + msg);
            QChatMessageInfo messageInfo = viewModel.sendTextMessage(msg);
            viewBinding.qChatChannelMemberListRecyclerView.appendMessage(messageInfo);
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
            Toast.makeText(getActivityContext(), R.string.qchat_develop_text, Toast.LENGTH_SHORT).show();
            return false;
        }

        @Override
        public boolean sendEmoji() {
            Toast.makeText(getActivityContext(), R.string.qchat_develop_text, Toast.LENGTH_SHORT).show();
            return false;
        }

        @Override
        public boolean sendVoice() {
            Toast.makeText(getActivityContext(), R.string.qchat_develop_text, Toast.LENGTH_SHORT).show();
            return false;
        }

        @Override
        public void onInputPanelExpand() {
            Toast.makeText(getActivityContext(), R.string.qchat_develop_text, Toast.LENGTH_SHORT).show();

        }

        @Override
        public void shouldCollapseInputPanel() {

        }

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
        viewBinding.qChatChannelMemberListRecyclerView.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            if (bottom < oldBottom) {
                viewBinding.qChatChannelMemberListRecyclerView.scrollBy(0, oldBottom - bottom);
            }
        });
        viewBinding.qChatChannelMemberListRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (viewBinding.qChatMessageBottomLayout.qChatMessageInputEt.hasFocus()) {
                    InputMethodManager imm = (InputMethodManager) QChatChannelMessageFragment.this.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(viewBinding.qChatMessageBottomLayout.qChatMessageInputEt.getWindowToken(), 0);
                    }
                    viewBinding.qChatMessageBottomLayout.qChatMessageInputEt.setFocusable(false);
                    viewBinding.qChatMessageBottomLayout.qChatMessageInputEt.setFocusableInTouchMode(true);
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });
        bottomBarLayout = new MessageInputLayout(viewBinding.qChatMessageBottomLayout, messageProxy);
        viewBinding.qChatChannelMemberListRecyclerView.setLoadHandler(new IMessageLoadHandler() {
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
        viewBinding.qChatChannelMemberListRecyclerView.setOptionCallback(new IMessageOptionCallBack() {
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
        viewModel.getQueryMessageLiveData().observe(this.getViewLifecycleOwner(), result -> {

            if (result.getLoadStatus() == LoadStatus.Success) {
                viewBinding.qChatChannelMemberListRecyclerView.appendMessages(result.getData());
            } else if (result.getLoadStatus() == LoadStatus.Finish) {
                if (result.getType() == FetchResult.FetchType.Add) {
                    if (result.getTypeIndex() == -1) {
                        viewBinding.qChatChannelMemberListRecyclerView.appendMessages(result.getData());
                    } else {
                        viewBinding.qChatChannelMemberListRecyclerView.addMessagesForward(result.getData());
                        viewBinding.qChatChannelMemberListRecyclerView.setHasMoreForwardMessages(viewModel.isHasForward());
                    }
                } else if (result.getType() == FetchResult.FetchType.Remove) {
                    //todo delete message
                } else if (result.getType() == FetchResult.FetchType.Update) {

                }
            }
        });

        viewModel.getSendMessageLiveData().observe(this.getViewLifecycleOwner(), result -> {
            if (result.getLoadStatus() == LoadStatus.Success) {
                ALog.d(TAG, "SendMessageLiveData", "Success");
                viewBinding.qChatChannelMemberListRecyclerView.updateMessageStatus(result.getData());
            }else if (result.getLoadStatus() == LoadStatus.Error) {
                if(result.getError() != null && result.getError().getCode() == QChatConstant.ERROR_CODE_IM_NO_PERMISSION){
                    ALog.d(TAG, "SendMessageLiveData", "Error 403");
                    showPermissionErrorDialog();
                    if (result.getData() != null) {
                        viewBinding.qChatChannelMemberListRecyclerView.deleteMessage(result.getData());
                    }
                }else {
                    if (result.getData() != null) {
                        viewBinding.qChatChannelMemberListRecyclerView.updateMessageStatus(result.getData());
                    }
                }
                ALog.d(TAG, "SendMessageLiveData", "Error");
            }
        });
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                String path = null;
                if (result.getData() == null) {
                    path = tempFile.getPath();
                    ALog.d(TAG, "activityResultLauncher", "info:" + path);
                    QChatMessageInfo messageInfo = viewModel.sendImageMessage(createImageMessage(path));
                    viewBinding.qChatChannelMemberListRecyclerView.appendMessage(messageInfo);
                } else {
                    Uri imageUri = result.getData().getData();
                    ALog.d(TAG, "activityResultLauncher", "intent info:" + imageUri.getPath());
                    new SendImageHelper.SendImageTask(getActivity(), imageUri, (filePath, isOrig) -> {
                        QChatMessageInfo messageInfo = viewModel.sendImageMessage(createImageMessage(filePath));
                        viewBinding.qChatChannelMemberListRecyclerView.appendMessage(messageInfo);
                    }).execute();
                }
            }
        });

        viewModel.fetchMessageList();

        NetworkUtils.registerStateListener(networkStateListener);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        NetworkUtils.unregisterStateListener(networkStateListener);
    }

    private void photoPicker() {

        if (photoPickerDialog == null) {
            photoPickerDialog = new PhotoPickerDialog(getActivity());
        }

        photoPickerDialog.show(new FetchCallback<Integer>() {
            @Override
            public void onSuccess(@Nullable Integer param) {
                if (param == 0) {
                    File file = FileUtils.getTempFile(getActivity(), null);
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    Uri uri;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        uri = FileProvider.getUriForFile(getActivity(), getActivity().getPackageName() + ".FileProvider", file);
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
                    Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
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
        ALog.d(TAG, "createImageMessage", "info:" + path);
        return imageAttachment;
    }

    private void showCopyTip() {
        viewBinding.cvCopyTip.setVisibility(View.VISIBLE);
        viewBinding.cvCopyTip.postDelayed(() -> {
            viewBinding.cvCopyTip.setVisibility(View.GONE);
        }, COPY_SHOW_TIME);
    }

    private void showPermissionErrorDialog(){
        CommonAlertDialog commonDialog = new CommonAlertDialog();
        commonDialog.setContentStr(getString(R.string.qchat_no_permission_content))
                .setPositiveStr(getString(R.string.qchat_ensure))
                .setConfirmListener(() -> {

                })
                .show(getActivity().getSupportFragmentManager());
    }
}
