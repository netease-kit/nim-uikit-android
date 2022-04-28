package com.netease.yunxin.kit.chatkit.ui.view.emoji;

public interface IEmojiSelectedListener {
    void onEmojiSelected(String key);

    void onStickerSelected(String categoryName, String stickerName);

    void onEmojiSendClick();
}
