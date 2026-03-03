// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.im.utils;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.v2.V2NIMError;
import com.netease.nimlib.sdk.v2.V2NIMFailureCallback;
import com.netease.nimlib.sdk.v2.V2NIMSuccessCallback;
import com.netease.nimlib.sdk.v2.conversation.V2NIMConversationService;
import com.netease.nimlib.sdk.v2.conversation.enums.V2NIMConversationType;
import com.netease.nimlib.sdk.v2.conversation.model.V2NIMConversation;
import com.netease.nimlib.sdk.v2.conversation.model.V2NIMLocalConversation;
import com.netease.nimlib.sdk.v2.message.V2NIMMessage;
import com.netease.nimlib.sdk.v2.message.V2NIMMessageService;
import com.netease.nimlib.sdk.v2.message.enums.V2NIMMessageType;
import com.netease.nimlib.sdk.v2.message.params.V2NIMSendMessageParams;
import com.netease.nimlib.v2.builder.V2NIMMessageBuilder;
import com.netease.yunxin.app.im.R;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.repo.ConversationRepo;
import com.netease.yunxin.kit.chatkit.repo.LocalConversationRepo;
import com.netease.yunxin.kit.chatkit.utils.ConversationIdUtils;
import com.netease.yunxin.kit.corekit.im2.IMKitClient;
import com.netease.yunxin.kit.corekit.im2.extend.FetchCallback;

/**
 * OpenClaw 智能体工具类
 * 用于处理OpenClaw AI智能体相关的消息操作
 */
public class OpenClawUtils {
    
    private static final String TAG = "OpenClawUtils";


    /**
     * 检查并创建 OpenClaw 智能体会话
     * 仅在用户配置了 OpenClaw 账号时执行
     */
    public static void checkAndCreateOpenClawSession(Context context,boolean cloud) {
        try {
            // 获取用户配置的 OpenClaw 账号
            String openClawAccount = ConfigDataUtils.getOpenClawAccount(context);

            // 如果没有配置 OpenClaw 账号，跳过
            if (TextUtils.isEmpty(openClawAccount)) {
                ALog.i(Constant.PROJECT_TAG, "checkAndCreateOpenClawSession", "No OpenClaw account " +
                        "configured, skipping");
                return;
            }
            String conversationId = ConversationIdUtils.conversationId(openClawAccount,
                    V2NIMConversationType.V2NIM_CONVERSATION_TYPE_P2P);
            if (cloud){
                ConversationRepo.getConversation(
                        conversationId,
                        new FetchCallback<V2NIMConversation>() {
                            @Override
                            public void onError(int i, @Nullable String s) {
                                ALog.e(Constant.PROJECT_TAG, "checkAndCreateOpenClawSession", "Failed to get conversation: " + s);
                                // 创建欢迎消息
                                OpenClawUtils.createOpenClawWelcomeMessage(context, openClawAccount);
                            }

                            @Override
                            public void onSuccess(@Nullable V2NIMConversation v2NIMConversation) {
                                // 创建欢迎消息
                                if (v2NIMConversation == null) {
                                    OpenClawUtils.createOpenClawWelcomeMessage(context, openClawAccount);
                                }
                            }
                        }
                );
            }else {
                LocalConversationRepo.getConversation(conversationId, new FetchCallback<V2NIMLocalConversation>() {
                    @Override
                    public void onError(int i, @Nullable String s) {
                        ALog.e(Constant.PROJECT_TAG, "checkAndCreateOpenClawSession", "Failed to get conversation: " + s);
                        // 创建欢迎消息
                        OpenClawUtils.createOpenClawWelcomeMessage(context, openClawAccount);
                    }

                    @Override
                    public void onSuccess(@Nullable V2NIMLocalConversation v2NIMConversation) {
                        // 创建欢迎消息
                        if (v2NIMConversation == null) {
                            OpenClawUtils.createOpenClawWelcomeMessage(context, openClawAccount);
                        }
                    }

                });
            }




        } catch (Exception e) {
            // 静默处理异常，不影响主流程
            ALog.e(Constant.PROJECT_TAG, "checkAndCreateOpenClawSession", "Error occurred");
        }
    }


    /**
     * 创建 OpenClaw 智能体欢迎消息
     * @param context 上下文对象
     * @param openClawAccount OpenClaw 账号
     */
    public static void createOpenClawWelcomeMessage(Context context, String openClawAccount) {
        if (context == null || TextUtils.isEmpty(openClawAccount)) {
            ALog.w(TAG, "createOpenClawWelcomeMessage", "Invalid parameters");
            return;
        }
        
        // 根据当前语言环境确定欢迎消息内容
        String welcomeMessage = getOpenClawWelcomeMessage(context);
        
        ALog.d(TAG, "createOpenClawWelcomeMessage", 
            "Creating welcome message for: " + openClawAccount + ", content: " + welcomeMessage);
        
        // 发送本地消息给OpenClaw智能体
        try {
            // 使用NIM SDK的V2接口创建并发送文本消息
            V2NIMMessageBuilder messageBuilder = V2NIMMessageBuilder.builder();
            
            messageBuilder.messageType(V2NIMMessageType.V2NIM_MESSAGE_TYPE_TIPS);
            messageBuilder.text(welcomeMessage);

            V2NIMMessage message = messageBuilder.build();
            
            // 设置发送参数
            V2NIMSendMessageParams params = V2NIMSendMessageParams
                    .V2NIMSendMessageParamsBuilder.builder().build();
            
            String conversationId = ConversationIdUtils.conversationId(openClawAccount,
                    V2NIMConversationType.V2NIM_CONVERSATION_TYPE_P2P);
            
            // 通过MessageService发送消息
            if (NIMClient.getService(V2NIMMessageService.class) != null) {
                NIMClient.getService(V2NIMMessageService.class)
                        .insertMessageToLocal(
                                message, conversationId, IMKitClient.account(),
                                System.currentTimeMillis(),
                                new V2NIMSuccessCallback<V2NIMMessage>() {
                                    @Override
                                    public void onSuccess(V2NIMMessage v2NIMMessage) {
                                        ALog.i(TAG, "createOpenClawWelcomeMessage " +
                                                "insertMessageToLocal onSuccess");
                                    }
                                },
                                new V2NIMFailureCallback() {
                                    @Override
                                    public void onFailure(V2NIMError error) {
                                        ALog.e(
                                                TAG,
                                                "createOpenClawWelcomeMessage insertMessageToLocal",
                                                error.getDesc()
                                        );
                                    }
                                }
                        );
                ALog.i(TAG, "createOpenClawWelcomeMessage",
                    "Welcome message sent to OpenClaw account: " + openClawAccount);
            } else {
                ALog.i(TAG, "createOpenClawWelcomeMessage",
                    "MessageService not available");
            }
            
        } catch (Exception e) {
            ALog.e(TAG, "createOpenClawWelcomeMessage", 
                "Failed to create welcome message");
        }
    }
    
    /**
     * 根据当前语言设置获取欢迎消息
     * @param context 上下文对象
     * @return 本地化的欢迎消息内容
     */
    private static String getOpenClawWelcomeMessage(Context context) {
        return context.getString(R.string.openclaw_welcome_message);
    }
    
    /**
     * 构建OpenClaw会话ID
     * @param openClawAccount OpenClaw账号
     * @return 会话ID
     */
    public static String buildOpenClawConversationId(String openClawAccount) {
        if (TextUtils.isEmpty(openClawAccount)) {
            return null;
        }
        return "p2p|" + openClawAccount;
    }
}