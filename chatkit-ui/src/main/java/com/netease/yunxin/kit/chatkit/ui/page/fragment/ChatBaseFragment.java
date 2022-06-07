/*
 * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.kit.chatkit.ui.page.fragment;

import static com.netease.yunxin.kit.corekit.im.utils.RouterConstant.REQUEST_CONTACT_SELECTOR_KEY;

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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.netease.nimlib.sdk.msg.attachment.MsgAttachment;
import com.netease.nimlib.sdk.msg.attachment.VideoAttachment;
import com.netease.nimlib.sdk.msg.constant.AttachStatusEnum;
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.msg.model.QueryDirectionEnum;
import com.netease.yunxin.kit.alog.ALog;
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
import com.netease.yunxin.kit.chatkit.ui.view.message.ChatMessageViewHolderFactory;
import com.netease.yunxin.kit.chatkit.ui.view.popmenu.ChatPopMenu;
import com.netease.yunxin.kit.chatkit.ui.view.popmenu.ChatPopMenuActionListener;
import com.netease.yunxin.kit.common.ui.dialog.ChoiceListener;
import com.netease.yunxin.kit.common.ui.dialog.CommonChoiceDialog;
import com.netease.yunxin.kit.common.ui.fragments.BaseFragment;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.common.utils.NetworkUtils;
import com.netease.yunxin.kit.chatkit.utils.SendMediaHelper;
import com.netease.yunxin.kit.common.utils.storage.StorageType;
import com.netease.yunxin.kit.common.utils.storage.StorageUtil;
import com.netease.yunxin.kit.corekit.im.XKitImClient;
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * BaseFragment for Chat
 * include P2P and Team chat page
 */
public abstract class ChatBaseFragment extends BaseFragment {

    private static final String LOG_TAG = "ChatBaseFragment";

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

    ChatPopMenu popMenu;

    private ChatMessageViewHolderFactory factory;

    private IChatViewCustom chatViewCustom;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = ChatLayoutFragmentBinding.inflate(inflater, container, false);
        if (getArguments() != null) {
            initData(getArguments());
        }
        initView();
        initViewModel();
        initDataObserver();
        NetworkUtils.registerStateListener(networkStateListener);
        initCustom();
        return binding.getRoot();
    }

    protected void initView() {
        ALog.i(LOG_TAG, "initView");

        binding.chatView.setMessageViewHolderFactory(factory);

        binding.chatView.getMessageListView().setPopActionListener(actionListener);

        binding.chatView.setMessageProxy(new IMessageProxy() {
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
                pickMediaLauncher.launch("image/*;video/*");
            }

            @Override
            public void takePicture() {
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

            @Override
            public void captureVideo() {
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
                    Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE)
                            .putExtra(MediaStore.EXTRA_OUTPUT, videoUri);
                    captureVideoLauncher.launch(intent);
                }
            }

            @Override
            public boolean sendFile(ChatMessageBean replyMsg) {
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
                if (sessionType == SessionTypeEnum.P2P &&
                        viewModel instanceof ChatP2PViewModel) {
                    ((ChatP2PViewModel) viewModel).sendInputNotification(isTyping);
                }
            }
        });
        binding.chatView.setLoadHandler(new IMessageLoadHandler() {
            @Override
            public void loadMoreForward(ChatMessageBean messageBean) {
                viewModel.fetchMoreMessage(messageBean.getMessageData().getMessage(), QueryDirectionEnum.QUERY_OLD);
            }

            @Override
            public void loadMoreBackground(ChatMessageBean messageBean) {
                viewModel.fetchMoreMessage(messageBean.getMessageData().getMessage(), QueryDirectionEnum.QUERY_NEW);
            }

            @Override
            public void onVisibleItemChange(List<ChatMessageBean> messages) {
                if (sessionType == SessionTypeEnum.Team &&
                        viewModel instanceof ChatTeamViewModel) {
                    ((ChatTeamViewModel) viewModel).refreshTeamMessageReceipt(messages);
                }
            }
        });

        binding.chatView.setMessageReader(message -> viewModel.sendReceipt(message.getMessage())
        );

        binding.chatView.setItemClickListener(new IMessageItemClickListener() {
            @Override
            public boolean onMessageLongClick(View view, ChatMessageBean messageBean) {
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
                return true;
            }

            @Override
            public void onMessageClick(View view, int position, ChatMessageBean messageBean) {
                if (messageBean.getViewType() == MsgTypeEnum.image.getValue()) {
                    watchImage(messageBean,
                            binding.chatView.getMessageListView().filterMessagesByType(messageBean.getViewType()));
                } else if (messageBean.getViewType() == MsgTypeEnum.video.getValue()) {
                    watchVideo(messageBean.getMessageData().getMessage());
                }
            }

            @Override
            public void onUserIconClick(View view, ChatMessageBean messageBean) {
                XKitRouter.withKey(RouterConstant.PATH_USER_INFO_ACTIVITY)
                        .withContext(view.getContext())
                        .withParam(RouterConstant.KEY_ACCOUNT_ID_KEY,
                                messageBean.getMessageData().getMessage().getFromAccount())
                        .navigate();
            }

            @Override
            public void onSelfIconClick(View view) {
                XKitRouter.withKey(RouterConstant.PATH_MINE_USER_INFO)
                        .withContext(view.getContext())
                        .navigate();
            }

            @Override
            public void onUserIconLongClick(View view, ChatMessageBean messageBean) {
                //todo
            }

            @Override
            public void onReEditRevokeMessage(View view, ChatMessageBean messageBean) {
                //only support text message
                if (messageBean.getMessageData().getMessage().getMsgType() == MsgTypeEnum.text) {
                    binding.chatView.getInputView()
                            .setReEditMessage(messageBean.getMessageData().getMessage().getContent());
                }
            }

            @Override
            public void onReplyMessageClick(View view, String messageUuid) {
                //scroll to the message position
                binding.chatView.getMessageListView().scrollToMessage(messageUuid);
            }

            @Override
            public void onSendFailBtnClick(View view, ChatMessageBean messageBean) {
                IMessageItemClickListener.super.onSendFailBtnClick(view, messageBean);
                viewModel.sendMessage(messageBean.getMessageData().getMessage(), true, true);
            }
        });
    }

    protected void initCustom() {
        ALog.i(LOG_TAG, "initCustom");
        if (chatViewCustom != null) {
            binding.chatView.setLayoutCustom(chatViewCustom);
        }
    }

    private final ChatPopMenuActionListener actionListener = new ChatPopMenuActionListener() {
        @Override
        public void onCopy(ChatMessageBean messageBean) {
            ClipboardManager cmb = (ClipboardManager) XKitImClient.getApplicationContext()
                    .getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clipData = null;
            if (messageBean.getMessageData().getMessage().getMsgType() == MsgTypeEnum.text) {
                clipData = ClipData.newPlainText(null, messageBean.getMessageData().getMessage().getContent());
            }
            cmb.setPrimaryClip(clipData);
            ToastX.showShortToast(R.string.chat_message_action_copy_success);
        }

        @Override
        public void onReply(ChatMessageBean messageBean) {
            if (aitManager != null && sessionType == SessionTypeEnum.Team) {
                String account = messageBean.getMessageData().getMessage().getFromAccount();
                if (!TextUtils.equals(account, XKitImClient.account())) {
                    String name = MessageHelper.getTeamNick(aitManager.getTid(), account);
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
        }

        @Override
        public void onForward(ChatMessageBean messageBean) {
            forwardMessage = messageBean;
            ChatMessageForwardSelectDialog dialog = new ChatMessageForwardSelectDialog();
            dialog.setSelectedCallback(new ChatMessageForwardSelectDialog.ForwardTypeSelectedCallback() {
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
        }

        @Override
        public void onSignal(ChatMessageBean messageBean, boolean cancel) {
            if (cancel) {
                viewModel.removeMsgPin(messageBean.getMessageData());
            } else {
                viewModel.addMessagePin(messageBean.getMessageData(), "");
            }
        }

        @Override
        public void onMultiSelected(ChatMessageBean messageBean) {
            //todo
        }

        @Override
        public void onCollection(ChatMessageBean messageBean) {
            viewModel.addMsgCollection(messageBean.getMessageData());
        }

        @Override
        public void onDelete(ChatMessageBean message) {
            showDeleteConfirmDialog(message);
        }

        @Override
        public void onRecall(ChatMessageBean messageBean) {
            showRevokeConfirmDialog(messageBean);
        }
    };

    private void showDeleteConfirmDialog(ChatMessageBean message) {
        CommonChoiceDialog dialog = new CommonChoiceDialog();
        dialog.setTitleStr(getString(R.string.chat_message_action_delete))
                .setContentStr(getString(R.string.chat_message_action_delete_this_message))
                .setPositiveStr(getString(R.string.chat_message_delete))
                .setNegativeStr(getString(R.string.cancel))
                .setConfirmListener(new ChoiceListener() {
                    @Override
                    public void onPositive() {
                        viewModel.deleteMessage(message.getMessageData());
                        binding.chatView.getMessageListView().deleteMessage(message);
                    }

                    @Override
                    public void onNegative() {

                    }
                }).show(getParentFragmentManager());
    }

    private void showRevokeConfirmDialog(ChatMessageBean messageBean) {
        CommonChoiceDialog dialog = new CommonChoiceDialog();
        dialog.setTitleStr(getString(R.string.chat_message_action_recall))
                .setContentStr(getString(R.string.chat_message_action_revoke_this_message))
                .setPositiveStr(getString(R.string.chat_message_action_recall))
                .setNegativeStr(getString(R.string.cancel))
                .setConfirmListener(new ChoiceListener() {
                    @Override
                    public void onPositive() {
                        viewModel.revokeMessage(messageBean);
                    }

                    @Override
                    public void onNegative() {

                    }
                }).show(getParentFragmentManager());
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
        if (message.getAttachStatus() == AttachStatusEnum.transferred &&
                !TextUtils.isEmpty(((VideoAttachment) message.getAttachment()).getPath())) {
            WatchVideoActivity.launch(getContext(), message);
        } else if (message.getAttachStatus() != AttachStatusEnum.transferring) {
            viewModel.downloadMessageAttachment(message);
        }
    }

    protected abstract void initViewModel();

    protected void initDataObserver() {
        ALog.i(LOG_TAG, "initDataObserver");
        viewModel.getQueryMessageLiveData().observe(getViewLifecycleOwner(), listFetchResult -> {
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
        });

        viewModel.getSendMessageLiveData().observe(getViewLifecycleOwner(), chatMessageBeanFetchResult -> {
            if (chatMessageBeanFetchResult.getType() == FetchResult.FetchType.Add) {
                ALog.i(LOG_TAG, "send message add");
                if (binding.chatView.getMessageListView().hasMoreNewerMessages()) {
                    binding.chatView.clearMessageList();
                    binding.chatView.appendMessage(chatMessageBeanFetchResult.getData());
                    if (chatMessageBeanFetchResult.getData() != null) {
                        binding.chatView.getMessageListView().setHasMoreNewerMessages(false);
                        viewModel.fetchMoreMessage(chatMessageBeanFetchResult.getData().getMessageData().getMessage(),
                                QueryDirectionEnum.QUERY_OLD);
                    }
                } else {
                    binding.chatView.appendMessage(chatMessageBeanFetchResult.getData());
                }
            } else {
                binding.chatView.updateMessage(chatMessageBeanFetchResult.getData());
            }
        });

        viewModel.getAttachmentProgressMutableLiveData()
                .observe(getViewLifecycleOwner(), attachmentProgressFetchResult -> binding.chatView
                        .updateProgress(attachmentProgressFetchResult.getData()));

        viewModel.getRevokeMessageLiveData().observe(getViewLifecycleOwner(), messageResult -> {
            if (messageResult.getLoadStatus() == LoadStatus.Success) {
                binding.chatView.getMessageListView().revokeMessage(messageResult.getData());
            }
        });

        viewModel.getAddPinMessageLiveData().observe(getViewLifecycleOwner(),
                responseOption -> binding.chatView.getMessageListView()
                        .addPinMessage(responseOption.first, responseOption.second));

        viewModel.getRemovePinMessageLiveData().observe(getViewLifecycleOwner(),
                uuid -> binding.chatView.getMessageListView().removePinMessage(uuid));

        pickMediaLauncher = registerForActivityResult(new ActivityResultContracts.GetMultipleContents(), result -> {
            for (int i = 0; i < result.size(); ++i) {
                Uri uri = result.get(i);
                ALog.i(LOG_TAG, "pick media result uri(" + i + ") -->> " + uri);
                viewModel.sendImageOrVideoMessage(uri);
            }
        });
        takePictureLauncher = registerForActivityResult(new ActivityResultContracts.TakePicture(), result -> {
            if (result && !TextUtils.isEmpty(captureTempImagePath)) {
                File f = new File(captureTempImagePath);
                Uri contentUri = Uri.fromFile(f);
                ALog.i(LOG_TAG, "take picture contentUri -->> " + contentUri);
                viewModel.sendImageOrVideoMessage(contentUri);
                captureTempImagePath = "";
            }
        });
        captureVideoLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && !TextUtils.isEmpty(captureTempVideoPath)) {
                        File f = new File(captureTempVideoPath);
                        Uri contentUri = Uri.fromFile(f);
                        ALog.i(LOG_TAG, "capture video contentUri -->> " + contentUri);
                        viewModel.sendImageOrVideoMessage(contentUri);
                        captureTempVideoPath = "";
                    }
                });

        forwardP2PLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() != Activity.RESULT_OK || forwardMessage == null) {
                return;
            }
            ALog.i(LOG_TAG, "forward P2P result");
            Intent data = result.getData();
            if (data != null) {
                ArrayList<String> friends = data.getStringArrayListExtra(REQUEST_CONTACT_SELECTOR_KEY);
                if (friends != null && !friends.isEmpty()) {
                    showForwardConfirmDialog(SessionTypeEnum.P2P, friends);
                }
            }
        });

        forwardTeamLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() != Activity.RESULT_OK || forwardMessage == null) {
                        return;
                    }
                    ALog.i(LOG_TAG, "forward Team result");
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
        String sendName = forwardMessage.getMessageData().getFromUser() == null ?
                forwardMessage.getMessageData().getMessage().getFromAccount()
                : forwardMessage.getMessageData().getFromUser().getName();
        bundle.putString(ChatMessageForwardConfirmDialog.FORWARD_MESSAGE_SEND, sendName);
        confirmDialog.setArguments(bundle);
        confirmDialog.setCallback(() -> {
            if (forwardMessage != null) {
                for (String accId : sessionIds) {
                    viewModel.sendForwardMessage(forwardMessage.getMessageData().getMessage(),
                            accId, type);
                }
            }
        });
        confirmDialog.show(getParentFragmentManager(), ChatMessageForwardConfirmDialog.TAG);
    }

    private void startTeamList() {
        XKitRouter.withKey(RouterConstant.PATH_TEAM_LIST)
                .withParam(RouterConstant.KEY_TEAM_LIST_SELECT, true)
                .withContext(requireContext())
                .navigate(forwardTeamLauncher);
    }

    private void startP2PSelector() {
        ArrayList<String> filterList = new ArrayList<>();
        if (sessionType == SessionTypeEnum.P2P) {
            filterList.add(viewModel.getmSessionId());
        }
        XKitRouter.withKey(RouterConstant.PATH_SELECTOR_ACTIVITY)
                .withParam(RouterConstant.KEY_CONTACT_SELECTOR_MAX_COUNT, 6)
                .withContext(requireContext())
                .withParam(RouterConstant.SELECTOR_CONTACT_FILTER_KEY, filterList)
                .navigate(forwardP2PLauncher);
    }

    protected abstract void initData(Bundle bundle);

    @Override
    public void onResume() {
        super.onResume();
        ALog.i(LOG_TAG, "onResume");
        viewModel.setChattingAccount();
    }

    @Override
    public void onPause() {
        super.onPause();
        ALog.i(LOG_TAG, "onPause");
        viewModel.clearChattingAccount();
    }

    public void onNewIntent(Intent intent) {
        ALog.i(LOG_TAG, "onNewIntent");

    }

    private final NetworkUtils.NetworkStateListener networkStateListener = new NetworkUtils.NetworkStateListener() {
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
        ALog.i(LOG_TAG, "onDestroyView");
        NetworkUtils.unregisterStateListener(networkStateListener);
        if (popMenu != null) {
            popMenu.hide();
        }
        super.onDestroyView();
    }

    /**
     * for extension message view holder
     */
    public void setMessageViewHolderFactory(ChatMessageViewHolderFactory factory) {
        if (binding != null) {
            binding.chatView.setMessageViewHolderFactory(factory);
        }
        this.factory = factory;
    }

    /**
     * for custom layout for ChatView
     */
    public void setChatViewCustom(IChatViewCustom chatViewCustom) {
        this.chatViewCustom = chatViewCustom;
    }
}
