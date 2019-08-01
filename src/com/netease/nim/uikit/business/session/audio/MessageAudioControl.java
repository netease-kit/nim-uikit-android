package com.netease.nim.uikit.business.session.audio;

import android.content.Context;
import com.netease.nim.uikit.common.ToastHelper;

import com.netease.nim.uikit.R;
import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.common.media.audioplayer.BaseAudioControl;
import com.netease.nim.uikit.common.media.audioplayer.Playable;
import com.netease.nim.uikit.common.ui.recyclerview.adapter.BaseMultiItemFetchLoadAdapter;
import com.netease.nim.uikit.common.util.storage.StorageUtil;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallbackWrapper;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.attachment.AudioAttachment;
import com.netease.nimlib.sdk.msg.constant.AttachStatusEnum;
import com.netease.nimlib.sdk.msg.constant.MsgDirectionEnum;
import com.netease.nimlib.sdk.msg.constant.MsgStatusEnum;
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;

import java.io.File;
import java.util.List;

public class MessageAudioControl extends BaseAudioControl<IMMessage> {
    private static MessageAudioControl mMessageAudioControl = null;

    private boolean mIsNeedPlayNext = false;

    private BaseMultiItemFetchLoadAdapter mAdapter;

    private IMMessage mItem = null;

    private MessageAudioControl(Context context) {
        super(context, true);
    }

    public static MessageAudioControl getInstance(Context context) {
        if (mMessageAudioControl == null) {
            synchronized (MessageAudioControl.class) {
                if (mMessageAudioControl == null) {
                    mMessageAudioControl = new MessageAudioControl(NimUIKit.getContext());
                }
            }
        }

        return mMessageAudioControl;
    }

    @Override
    protected void setOnPlayListener(Playable playingPlayable, AudioControlListener audioControlListener) {
        this.audioControlListener = audioControlListener;

        BasePlayerListener basePlayerListener = new BasePlayerListener(currentAudioPlayer, playingPlayable) {

            @Override
            public void onInterrupt() {
                if (!checkAudioPlayerValid()) {
                    return;
                }

                super.onInterrupt();
                cancelPlayNext();
            }

            @Override
            public void onError(String error) {
                if (!checkAudioPlayerValid()) {
                    return;
                }

                super.onError(error);
                cancelPlayNext();
            }

            @Override
            public void onCompletion() {
                if (!checkAudioPlayerValid()) {
                    return;
                }

                resetAudioController(listenerPlayingPlayable);

                boolean isLoop = false;
                if (mIsNeedPlayNext) {
                    if (mAdapter != null && mItem != null) {
                        isLoop = playNextAudio(mAdapter, mItem);
                    }
                }

                if (!isLoop) {
                    if (audioControlListener != null) {
                        audioControlListener.onEndPlay(currentPlayable);
                    }

                    playSuffix();
                }
            }
        };

        basePlayerListener.setAudioControlListener(audioControlListener);
        currentAudioPlayer.setOnPlayListener(basePlayerListener);
    }

    @Override
    public IMMessage getPlayingAudio() {
        if (isPlayingAudio() && AudioMessagePlayable.class.isInstance(currentPlayable)) {
            return ((AudioMessagePlayable) currentPlayable).getMessage();
        } else {
            return null;
        }
    }

    @Override
    public void startPlayAudioDelay(
            final long delayMillis,
            final IMMessage message,
            final AudioControlListener audioControlListener, final int audioStreamType) {
        // 如果不存在则下载
        AudioAttachment audioAttachment = (AudioAttachment) message.getAttachment();
        File file = new File(audioAttachment.getPathForSave());
        if (!file.exists()) {
            NIMClient.getService(MsgService.class).downloadAttachment(message, false).setCallback(new RequestCallbackWrapper() {
                @Override
                public void onResult(int code, Object result, Throwable exception) {
                    startPlayAudio(message, audioControlListener, audioStreamType, true, delayMillis);
                }
            });
            return;
        }
        startPlayAudio(message, audioControlListener, audioStreamType, true, delayMillis);
    }

    //连续播放时不需要resetOrigAudioStreamType
    private void startPlayAudio(
            IMMessage message,
            AudioControlListener audioControlListener,
            int audioStreamType,
            boolean resetOrigAudioStreamType,
            long delayMillis) {
        if (StorageUtil.isExternalStorageExist()) {
            if (startAudio(new AudioMessagePlayable(message), audioControlListener, audioStreamType, resetOrigAudioStreamType, delayMillis)) {
                // 将未读标识去掉,更新数据库
                if (isUnreadAudioMessage(message)) {
                    message.setStatus(MsgStatusEnum.read);
                    NIMClient.getService(MsgService.class).updateIMMessageStatus(message);
                }
            }
        } else {
            ToastHelper.showToast(mContext, R.string.sdcard_not_exist_error);
        }
    }

    private boolean playNextAudio(BaseMultiItemFetchLoadAdapter tAdapter, IMMessage messageItem) {
        final List<?> list = tAdapter.getData();
        int index = 0;
        int nextIndex = -1;
        //找到当前已经播放的
        for (int i = 0; i < list.size(); ++i) {
            IMMessage item = (IMMessage) list.get(i);
            if (item.equals(messageItem)) {
                index = i;
                break;
            }
        }
        //找到下一个将要播放的
        for (int i = index; i < list.size(); ++i) {
            IMMessage item = (IMMessage) list.get(i);
            IMMessage message = item;
            if (isUnreadAudioMessage(message)) {
                nextIndex = i;
                break;
            }
        }

        if (nextIndex == -1) {
            cancelPlayNext();
            return false;
        }
        IMMessage message = (IMMessage) list.get(nextIndex);
        AudioAttachment attach = (AudioAttachment) message.getAttachment();
        if (mMessageAudioControl != null && attach != null) {
            if (message.getAttachStatus() != AttachStatusEnum.transferred) {
                cancelPlayNext();
                return false;
            }
            if (message.getStatus() != MsgStatusEnum.read) {
                message.setStatus(MsgStatusEnum.read);
                NIMClient.getService(MsgService.class).updateIMMessageStatus(message);
            }
            //不是直接通过点击ViewHolder开始的播放，不设置AudioControlListener
            //notifyDataSetChanged会触发ViewHolder刷新，对应的ViewHolder会把AudioControlListener设置上去
            //连续播放 1.继续使用playingAudioStreamType 2.不需要resetOrigAudioStreamType
            mMessageAudioControl.startPlayAudio(message, null, getCurrentAudioStreamType(), false, 0);
            mItem = (IMMessage) list.get(nextIndex);
            tAdapter.notifyDataSetChanged();
            return true;
        }
        return false;
    }

    private void cancelPlayNext() {
        setPlayNext(false, null, null);
    }

    public void setPlayNext(boolean isPlayNext, BaseMultiItemFetchLoadAdapter adapter, IMMessage item) {
        mIsNeedPlayNext = isPlayNext;
        mAdapter = adapter;
        mItem = item;
    }

    public void stopAudio() {
        super.stopAudio();
    }

    public boolean isUnreadAudioMessage(IMMessage message) {
        if ((message.getMsgType() == MsgTypeEnum.audio)
                && message.getDirect() == MsgDirectionEnum.In
                && message.getAttachStatus() == AttachStatusEnum.transferred
                && message.getStatus() != MsgStatusEnum.read) {
            return true;
        } else {
            return false;
        }
    }
}
