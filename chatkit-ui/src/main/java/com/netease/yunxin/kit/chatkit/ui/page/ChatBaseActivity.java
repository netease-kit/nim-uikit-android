package com.netease.yunxin.kit.chatkit.ui.page;

import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.annotation.Nullable;

import com.netease.yunxin.kit.chatkit.ui.databinding.ChatActivityBinding;
import com.netease.yunxin.kit.chatkit.ui.view.message.audio.ChatMessageAudioControl;
import com.netease.yunxin.kit.common.ui.activities.BaseActivity;

/**
 * BaseActivity for Chat
 * include P2P chat page and Team chat page
 */
public abstract class ChatBaseActivity extends BaseActivity {

    ChatActivityBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ChatActivityBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());
        initChat();
    }

    protected abstract void initChat();

    @Override
    protected void onStop() {
        super.onStop();
        //stop message audio
        ChatMessageAudioControl.getInstance().stopAudio();
    }
}
