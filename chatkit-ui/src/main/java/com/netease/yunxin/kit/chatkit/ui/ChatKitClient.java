package com.netease.yunxin.kit.chatkit.ui;

public class ChatKitClient {

    private static ChatUIConfig chatConfig;

    public static void setChatUIConfig(ChatUIConfig config){
        chatConfig = config;
    }

    public static ChatUIConfig getChatUIConfig(){
        return chatConfig;
    }
}
