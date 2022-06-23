package com.netease.yunxin.kit.conversationkit.ui;


public class ConversationKitClient {

    private static ConversationUIConfig sConversationConfig;

    public static void setConversationUIConfig(ConversationUIConfig config){
        sConversationConfig = config;
    }

    public static ConversationUIConfig getConversationUIConfig(){
        return sConversationConfig;
    }
}
