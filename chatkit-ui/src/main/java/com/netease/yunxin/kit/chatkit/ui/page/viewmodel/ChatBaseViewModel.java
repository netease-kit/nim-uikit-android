/*
 * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.kit.chatkit.ui.page.viewmodel;

import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Pair;

import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;

import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.msg.MessageBuilder;
import com.netease.nimlib.sdk.msg.attachment.FileAttachment;
import com.netease.nimlib.sdk.msg.attachment.MsgAttachment;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.AttachmentProgress;
import com.netease.nimlib.sdk.msg.model.CollectInfo;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.msg.model.MemberPushOption;
import com.netease.nimlib.sdk.msg.model.MsgPinOption;
import com.netease.nimlib.sdk.msg.model.MsgPinSyncResponseOption;
import com.netease.nimlib.sdk.msg.model.QueryDirectionEnum;
import com.netease.nimlib.sdk.msg.model.ShowNotificationWhenRevokeFilter;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.model.IMMessageInfo;
import com.netease.yunxin.kit.chatkit.repo.ChatMessageRepo;
import com.netease.yunxin.kit.chatkit.repo.ChatServiceObserverRepo;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.common.ChatCallback;
import com.netease.yunxin.kit.chatkit.ui.model.ChatConstants;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.chatkit.ui.model.ait.AitContactsModel;
import com.netease.yunxin.kit.common.ui.viewmodel.BaseViewModel;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.common.utils.file.FileUtil;
import com.netease.yunxin.kit.common.utils.media.ImageUtil;
import com.netease.yunxin.kit.common.utils.media.SendMediaHelper;
import com.netease.yunxin.kit.common.utils.storage.RealPathUtil;
import com.netease.yunxin.kit.corekit.im.XKitImClient;
import com.netease.yunxin.kit.corekit.im.model.EventObserver;
import com.netease.yunxin.kit.corekit.im.provider.FetchCallback;
import com.netease.yunxin.kit.corekit.im.repo.ConfigRepo;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * chat info view model
 * fetch and send messages for chat page
 */
public abstract class ChatBaseViewModel extends BaseViewModel {
    public static final String TAG = "ChatViewModel";
    private final MutableLiveData<FetchResult<List<ChatMessageBean>>> messageLiveData = new MutableLiveData<>();
    private final FetchResult<List<ChatMessageBean>> messageFetchResult = new FetchResult<>(LoadStatus.Finish);
    private final MutableLiveData<FetchResult<ChatMessageBean>> sendMessageLiveData = new MutableLiveData<>();
    private final FetchResult<ChatMessageBean> sendMessageFetchResult = new FetchResult<>(LoadStatus.Finish);
    private final MutableLiveData<FetchResult<AttachmentProgress>> attachmentProgressMutableLiveData = new MutableLiveData<>();
    private final MutableLiveData<FetchResult<ChatMessageBean>> revokeMessageLiveData = new MutableLiveData<>();

    protected String mSessionId;
    private SessionTypeEnum mSessionType;

    private long credibleTimestamp = -1;
    private final int messagePageSize = 100;

    private final EventObserver<List<IMMessageInfo>> receiveMessageObserver = new EventObserver<List<IMMessageInfo>>() {
        @Override
        public void onEvent(@Nullable List<IMMessageInfo> event) {
            ALog.d(TAG, "receive msg -->> " + (event == null ? "null" : event.size()));
            messageFetchResult.setLoadStatus(LoadStatus.Finish);
            messageFetchResult.setData(convert(event));
            messageFetchResult.setType(FetchResult.FetchType.Add);
            messageFetchResult.setTypeIndex(-1);
            messageLiveData.setValue(messageFetchResult);
        }
    };

    private final EventObserver<IMMessageInfo> msgStatusObserver = new EventObserver<IMMessageInfo>() {
        @Override
        public void onEvent(@Nullable IMMessageInfo event) {
            ALog.d(TAG, "msg status change -->> " + (event == null ? "null" : event.getMessage().getStatus()));
            sendMessageFetchResult.setLoadStatus(LoadStatus.Finish);
            sendMessageFetchResult.setData(new ChatMessageBean(event));
            sendMessageFetchResult.setType(FetchResult.FetchType.Update);
            sendMessageFetchResult.setTypeIndex(-1);
            sendMessageLiveData.setValue(sendMessageFetchResult);
        }
    };

    private final Observer<AttachmentProgress> attachmentProgressObserver = attachmentProgress -> {
        ALog.d(TAG, "attachment progress update -->> " + attachmentProgress.getTransferred() + "/" +
                attachmentProgress.getTotal());
        FetchResult<AttachmentProgress> result = new FetchResult<>(LoadStatus.Finish);
        result.setData(attachmentProgress);
        result.setType(FetchResult.FetchType.Update);
        result.setTypeIndex(-1);
        attachmentProgressMutableLiveData.postValue(result);
    };

    private final ShowNotificationWhenRevokeFilter revokeFilter = notification -> {
        ALog.i(TAG, notification.getMessage().toString());
        FetchResult<ChatMessageBean> fetchResult = new FetchResult<>(LoadStatus.Success);
        fetchResult.setData(new ChatMessageBean(new IMMessageInfo(notification.getMessage())));
        revokeMessageLiveData.postValue(fetchResult);
        return true;
    };

    /**
     * chat message revoke live data
     */
    public MutableLiveData<FetchResult<ChatMessageBean>> getRevokeMessageLiveData() {
        return revokeMessageLiveData;
    }

    /**
     * query chat message list
     */
    public MutableLiveData<FetchResult<List<ChatMessageBean>>> getQueryMessageLiveData() {
        return messageLiveData;
    }

    public void deleteMessage(IMMessageInfo messageInfo) {
        ChatMessageRepo.deleteChattingHistory(messageInfo);
    }

    public void revokeMessage(ChatMessageBean messageBean) {
        ChatMessageRepo.revokeMessage(messageBean.getMessageData(), new FetchCallback<Void>() {
            @Override
            public void onSuccess(@Nullable Void param) {
                FetchResult<ChatMessageBean> fetchResult = new FetchResult<>(LoadStatus.Success);
                fetchResult.setData(messageBean);
                revokeMessageLiveData.postValue(fetchResult);
            }

            @Override
            public void onFailed(int code) {
                FetchResult<ChatMessageBean> fetchResult = new FetchResult<>(LoadStatus.Error);
                fetchResult.setError(code, code == ResponseCode.RES_OVERDUE ? R.string.chat_message_revoke_over_time
                        : R.string.chat_message_revoke_error);
                revokeMessageLiveData.postValue(fetchResult);
            }

            @Override
            public void onException(@Nullable Throwable exception) {
                FetchResult<ChatMessageBean> fetchResult = new FetchResult<>(LoadStatus.Error);
                fetchResult.setError(-1, exception == null ? "" : exception.getMessage());
                revokeMessageLiveData.postValue(fetchResult);
            }
        });
    }

    /**
     * send new message or chat message status change live data
     */
    public MutableLiveData<FetchResult<ChatMessageBean>> getSendMessageLiveData() {
        return sendMessageLiveData;
    }

    /**
     * message attachment load progress live data
     */
    public MutableLiveData<FetchResult<AttachmentProgress>> getAttachmentProgressMutableLiveData() {
        return attachmentProgressMutableLiveData;
    }

    public void init(String sessionId, SessionTypeEnum sessionType) {
        ALog.d(TAG, "init sessionId:" + sessionId + " sessionType:" + sessionType);
        this.mSessionId = sessionId;
        this.mSessionType = sessionType;
    }

    public void setChattingAccount() {
        ChatMessageRepo.setChattingAccount(mSessionId, mSessionType);
    }

    public String getmSessionId() {
        return mSessionId;
    }

    public void clearChattingAccount() {
        ChatMessageRepo.clearChattingAccount();
    }

    public void registerObservers(boolean register) {
        ALog.i(TAG, "registerObservers " + register);
        ChatServiceObserverRepo.observeReceiveMessage(mSessionId, receiveMessageObserver, register);
        ChatServiceObserverRepo.observeMsgStatus(msgStatusObserver, register);
        ChatServiceObserverRepo.observeAttachmentProgress(attachmentProgressObserver, register);
        ChatMessageRepo.registerShouldShowNotificationWhenRevokeFilter(revokeFilter);
        ChatServiceObserverRepo.observeAddMessagePin(msgPinAddObserver, register);
        ChatServiceObserverRepo.observeRemoveMessagePin(msgPinRemoveObserver, register);
    }

    public void sendTextMessage(String content, List<String> pushList) {
        IMMessage textMsg = MessageBuilder.createTextMessage(mSessionId, mSessionType, content);
        appendTeamMemberPush(textMsg, pushList);
        sendMessage(textMsg, false, true);
    }

    public void addMsgCollection(IMMessageInfo messageInfo) {
        ChatMessageRepo.collectMessage(messageInfo.getMessage(), new ChatCallback<CollectInfo>().setShowSuccess(true));
    }

    public void sendAudioMessage(File audio, long audioLength) {
        IMMessage audioMsg = MessageBuilder.createAudioMessage(mSessionId, mSessionType, audio, audioLength);
        sendMessage(audioMsg, false, true);
    }

    public void sendImageMessage(File imageFile) {
        IMMessage imageMsg = MessageBuilder.createImageMessage(mSessionId, mSessionType, imageFile);
        sendMessage(imageMsg, false, true);
    }

    public void sendCustomMessage(MsgAttachment attachment, String content) {
        IMMessage customMessage = MessageBuilder.createCustomMessage(mSessionId, mSessionType, content, attachment);
        sendMessage(customMessage, false, true);
    }

    public void sendForwardMessage(IMMessage message, String sessionId, SessionTypeEnum sessionType) {
        IMMessage forwardMessage = MessageBuilder.createForwardMessage(message, sessionId, sessionType);
        sendMessage(forwardMessage, false, TextUtils.equals(sessionId, mSessionId));
    }

    public void replyImageMessage(File imageFile, IMMessage message) {
        IMMessage imageMsg = MessageBuilder.createImageMessage(mSessionId, mSessionType, imageFile);
        replyMessage(imageMsg, message, false);
    }

    public void sendVideoMessage(File videoFile, long duration, int width, int height, String displayName) {
        IMMessage message =
                MessageBuilder.createVideoMessage(mSessionId, mSessionType, videoFile, duration, width, height,
                        displayName);
        sendMessage(message, false, true);
    }

    public void downloadMessageAttachment(IMMessage message) {
        if (message.getAttachment() instanceof FileAttachment) {
            ChatMessageRepo.downloadAttachment(message, false, null);
        }
    }

    public void sendImageOrVideoMessage(Uri uri) {
        String mimeType = FileUtil.getExtensionName(uri.getPath());
        if (TextUtils.isEmpty(mimeType)) {
            String realPath = RealPathUtil.getRealPath(uri);
            mimeType = FileUtil.getExtensionName(realPath);
        }
        if (ImageUtil.isValidPictureFile(mimeType)) {
            SendMediaHelper.handleImage(uri, false, this::sendImageMessage);
        } else if (ImageUtil.isValidVideoFile(mimeType)) {
            SendMediaHelper.handleVideo(uri, file -> {
                MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                try {
                    mmr.setDataSource(file.getPath());
                    String duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                    String width = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
                    String height = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
                    sendVideoMessage(file, Long.parseLong(duration), Integer.parseInt(width), Integer.parseInt(height),
                            file.getName());
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    mmr.release();
                }
            });
        } else {
            ALog.e(TAG, "invalid file type");
        }
    }

    private void onMessageSend(IMMessage message, boolean resend) {
        ALog.i(TAG, "sendMessage", "sending");
        sendMessageFetchResult.setLoadStatus(LoadStatus.Loading);
        if (resend) {
            sendMessageFetchResult.setType(FetchResult.FetchType.Update);
        } else {
            sendMessageFetchResult.setType(FetchResult.FetchType.Add);
        }
        sendMessageFetchResult.setData(new ChatMessageBean(new IMMessageInfo(message)));
        sendMessageLiveData.postValue(sendMessageFetchResult);
    }

    public void sendMessage(IMMessage message, boolean resend, boolean needSendMessage) {
        if (ConfigRepo.getShowReadStatus()) {
            message.setMsgAck();
        }
        if (needSendMessage) {
            onMessageSend(message, resend);
        }
        ChatMessageRepo.sendMessage(message, resend, null);
    }

    public abstract void sendReceipt(IMMessage message);

    /**
     * called when entering the chat page
     */
    public void initFetch(IMMessage anchor) {
        ALog.i(TAG, "initFetch");
        registerObservers(true);

        queryRoamMsgHasMoreTime(new FetchCallback<Long>() {
            @Override
            public void onSuccess(@Nullable Long param) {
                credibleTimestamp = param == null ? 0 : param;
                ALog.i(TAG, "initFetch", "queryRoamMsgHasMoreTime -->> credibleTimestamp:" + credibleTimestamp);
                if (anchor == null) {
                    fetchMoreMessage(
                            MessageBuilder.createEmptyMessage(mSessionId, mSessionType, System.currentTimeMillis()),
                            QueryDirectionEnum.QUERY_OLD);
                } else {
                    fetchMessageListBothDirect(anchor);
                }
            }

            @Override
            public void onFailed(int code) {

            }

            @Override
            public void onException(@Nullable Throwable exception) {

            }
        });
    }

    public void fetchMoreMessage(IMMessage anchor, QueryDirectionEnum direction) {
        if (!isMessageCredible(anchor)) {
            ALog.i(TAG, "fetchMoreMessage anchor is not credible");
            if (direction == QueryDirectionEnum.QUERY_NEW) {
                fetchMessageRemoteNewer(anchor);
            } else {
                fetchMessageRemoteOlder(anchor, false);
            }
            return;
        }
        ALog.i(TAG, "fetch local anchor time:" + anchor.getTime() + " direction:" + direction);
        ChatMessageRepo.fetchHistoryMessageLocal(anchor, direction, messagePageSize,
                new FetchCallback<List<IMMessageInfo>>() {
                    @Override
                    public void onSuccess(@Nullable List<IMMessageInfo> param) {
                        if (param == null || param.isEmpty()) {
                            // no more local messages
                            if (direction == QueryDirectionEnum.QUERY_OLD && credibleTimestamp > 0) {
                                ALog.i(TAG, "fetch local no more messages -->> try remote");
                                fetchMessageRemoteOlder(anchor, true);
                            } else {
                                onListFetchSuccess(param, direction);
                            }
                            return;
                        }
                        if (direction == QueryDirectionEnum.QUERY_OLD) {
                            if (isMessageCredible(param.get(0).getMessage())) {
                                onListFetchSuccess(param, direction);
                            } else {
                                fetchMessageRemoteOlder(anchor, true);
                            }
                        } else {
                            onListFetchSuccess(param, direction);
                        }
                    }

                    @Override
                    public void onFailed(int code) {
                        onListFetchFailed(code);
                    }

                    @Override
                    public void onException(@Nullable Throwable exception) {
                        onListFetchFailed(ChatConstants.ERROR_CODE_FETCH_MSG);
                    }
                });
    }

    public void fetchMessageListBothDirect(IMMessage anchor) {
        ALog.i(TAG, "fetchMessageListBothDirect");
        fetchMoreMessage(anchor, QueryDirectionEnum.QUERY_OLD);
        fetchMoreMessage(anchor, QueryDirectionEnum.QUERY_NEW);
    }

    private void fetchMessageRemoteOlder(IMMessage anchor, boolean updateCredible) {
        ALog.i(TAG, "fetch remote old anchor time:" + anchor.getTime() + " need update:" + updateCredible);
        ChatMessageRepo.fetchHistoryMessageRemote(anchor, 0, messagePageSize, QueryDirectionEnum.QUERY_OLD,
                new FetchCallback<List<IMMessageInfo>>() {
                    @Override
                    public void onSuccess(@Nullable List<IMMessageInfo> param) {
                        if (param != null) {
                            Collections.reverse(param);
                        }
                        if (updateCredible && param != null && param.size() > 0) {
                            credibleTimestamp = param.get(0).getMessage().getTime();
                            ALog.i(TAG, "updateCredible time:" + credibleTimestamp);
                            updateRoamMsgHasMoreTag(param.get(0).getMessage());
                        }
                        onListFetchSuccess(param, QueryDirectionEnum.QUERY_OLD);
                    }

                    @Override
                    public void onFailed(int code) {
                        onListFetchFailed(code);
                    }

                    @Override
                    public void onException(@Nullable Throwable exception) {
                        onListFetchFailed(ChatConstants.ERROR_CODE_FETCH_MSG);
                    }
                });
    }

    private void fetchMessageRemoteNewer(IMMessage anchor) {
        ALog.i(TAG, "fetch remote newer anchor time:" + anchor.getTime());
        ChatMessageRepo.fetchHistoryMessageRemote(anchor, System.currentTimeMillis(), messagePageSize,
                QueryDirectionEnum.QUERY_NEW, new FetchCallback<List<IMMessageInfo>>() {
                    @Override
                    public void onSuccess(@Nullable List<IMMessageInfo> param) {
                        // no need to update credible time, because all messages behind this
                        onListFetchSuccess(param, QueryDirectionEnum.QUERY_NEW);
                    }

                    @Override
                    public void onFailed(int code) {
                        onListFetchFailed(code);
                    }

                    @Override
                    public void onException(@Nullable Throwable exception) {
                        onListFetchFailed(ChatConstants.ERROR_CODE_FETCH_MSG);
                    }
                });
    }

    private boolean isMessageCredible(IMMessage message) {
        ALog.i(TAG, "isMessageCredible -->> credibleTimestamp:" + credibleTimestamp + " msgTime:" + message.getTime());
        return credibleTimestamp <= 0 || message.getTime() >= credibleTimestamp;
    }

    private void onListFetchSuccess(List<IMMessageInfo> param, QueryDirectionEnum direction) {
        ALog.i(TAG,
                "onListFetchSuccess -->> size:" + (param == null ? "null" : param.size()) + " direction:" + direction);

        LoadStatus loadStatus =
                (param == null || param.size() < messagePageSize) ? LoadStatus.Finish : LoadStatus.Success;
        messageFetchResult.setLoadStatus(loadStatus);
        messageFetchResult.setData(convert(param));
        messageFetchResult.setTypeIndex(direction == QueryDirectionEnum.QUERY_OLD ? 0 : -1);
        messageLiveData.postValue(messageFetchResult);
    }

    private void onListFetchFailed(int code) {
        ALog.i(TAG, "onListFetchFailed code:" + code);
        messageFetchResult.setError(code, R.string.chat_message_fetch_error);
        messageFetchResult.setData(null);
        messageFetchResult.setTypeIndex(-1);
        messageLiveData.postValue(messageFetchResult);
    }

    public void queryRoamMsgHasMoreTime(FetchCallback<Long> callback) {
        ChatMessageRepo.queryRoamMsgHasMoreTime(mSessionId, mSessionType, callback);
    }

    public void updateRoamMsgHasMoreTag(IMMessage newTag) {
        ChatMessageRepo.updateRoamMsgHasMoreTag(newTag);
    }

    private List<ChatMessageBean> convert(List<IMMessageInfo> messageList) {
        if (messageList == null) {
            return null;
        }
        ArrayList<ChatMessageBean> result = new ArrayList<>(messageList.size());
        for (IMMessageInfo message : messageList) {
            result.add(new ChatMessageBean(message));
        }
        return result;
    }

    //**********reply message**************
    public void replyMessage(IMMessage message, IMMessage replyMsg, boolean resend) {
        message.setThreadOption(replyMsg);
        message.setMsgAck();
        onMessageSend(message, resend);
        ChatMessageRepo.replyMessage(message, replyMsg, resend, null);
    }

    public void replyTextMessage(String content, IMMessage message, List<String> pushList) {
        IMMessage textMsg = MessageBuilder.createTextMessage(mSessionId, mSessionType, content);
        appendTeamMemberPush(textMsg, pushList);
        replyMessage(textMsg, message, false);
    }

    //**********Message Pin****************

    private void appendTeamMemberPush(IMMessage message, List<String> pushList) {
        if (mSessionType == SessionTypeEnum.Team && pushList != null && !pushList.isEmpty()) {
            MemberPushOption memberPushOption = new MemberPushOption();
            memberPushOption.setForcePush(true);
            memberPushOption.setForcePushContent(message.getContent());
            if (pushList.size() == 1 && pushList.get(0).equals(AitContactsModel.ACCOUNT_ALL)) {
                memberPushOption.setForcePushList(null);
            } else {
                memberPushOption.setForcePushList(pushList);
            }
            message.setMemberPushOption(memberPushOption);
        }
    }

    //********************Message Pin********************

    private final MutableLiveData<Pair<String, MsgPinOption>> addPinMessageLiveData = new MutableLiveData<>();

    private final MutableLiveData<String> removePinMessageLiveData = new MutableLiveData<>();

    public MutableLiveData<Pair<String, MsgPinOption>> getAddPinMessageLiveData() {
        return addPinMessageLiveData;
    }

    public MutableLiveData<String> getRemovePinMessageLiveData() {
        return removePinMessageLiveData;
    }

    private final Observer<MsgPinSyncResponseOption> msgPinAddObserver = msgPinSyncResponseOption -> {
        Pair<String, MsgPinOption> pinInfo = new Pair<>(msgPinSyncResponseOption.getKey().getUuid(),
                msgPinSyncResponseOption.getPinOption());
        addPinMessageLiveData.setValue(pinInfo);
    };

    private final Observer<MsgPinSyncResponseOption> msgPinRemoveObserver = responseOption -> removePinMessageLiveData
            .setValue(responseOption.getKey().getUuid());

    public void addMessagePin(IMMessageInfo messageInfo, String ext) {
        ChatMessageRepo.addMessagePin(messageInfo.getMessage(), ext, new ChatCallback<Long>() {
            @Override
            public void onSuccess(@Nullable Long param) {
                super.onSuccess(param);
                MsgPinOption pinOption = new MsgPinOption() {
                    @Override
                    public String getAccount() {
                        return XKitImClient.account();
                    }

                    @Override
                    public String getExt() {
                        return ext;
                    }

                    @Override
                    public long getCreateTime() {
                        return System.currentTimeMillis();
                    }

                    @Override
                    public long getUpdateTime() {
                        return System.currentTimeMillis();
                    }
                };
                Pair<String, MsgPinOption> pinInfo = new Pair<>(messageInfo.getMessage().getUuid(),
                        pinOption);
                addPinMessageLiveData.setValue(pinInfo);
            }
        });
    }

    public void removeMsgPin(IMMessageInfo messageInfo) {
        ChatMessageRepo.removeMessagePin(messageInfo.getMessage(), new ChatCallback<Long>() {
            @Override
            public void onSuccess(@Nullable Long param) {
                super.onSuccess(param);
                removePinMessageLiveData.postValue(messageInfo.getMessage().getUuid());
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        registerObservers(false);
    }
}
