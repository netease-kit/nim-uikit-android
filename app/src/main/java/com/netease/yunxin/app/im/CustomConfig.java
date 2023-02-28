// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.im;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum;
import com.netease.yunxin.kit.chatkit.ui.ChatKitClient;
import com.netease.yunxin.kit.chatkit.ui.ChatMessageType;
import com.netease.yunxin.kit.chatkit.ui.ChatUIConfig;
import com.netease.yunxin.kit.chatkit.ui.builder.IChatViewCustom;
import com.netease.yunxin.kit.chatkit.ui.custom.ChatStickerViewHolder;
import com.netease.yunxin.kit.chatkit.ui.custom.StickerAttachment;
import com.netease.yunxin.kit.chatkit.ui.view.ChatView;
import com.netease.yunxin.kit.chatkit.ui.view.message.MessageProperties;
import com.netease.yunxin.kit.contactkit.ui.ContactUIConfig;
import com.netease.yunxin.kit.contactkit.ui.IContactViewLayout;
import com.netease.yunxin.kit.contactkit.ui.view.ContactLayout;
import com.netease.yunxin.kit.conversationkit.model.ConversationInfo;
import com.netease.yunxin.kit.conversationkit.ui.ConversationCustom;
import com.netease.yunxin.kit.conversationkit.ui.ConversationUIConfig;
import com.netease.yunxin.kit.conversationkit.ui.IConversationViewLayout;
import com.netease.yunxin.kit.conversationkit.ui.view.ConversationLayout;

public class CustomConfig {

  //个性化配置会话消息页面
  public static void configChatKit(Context context) {
    //test
    ChatUIConfig chatUIConfig = new ChatUIConfig();
    chatUIConfig.messageProperties = new MessageProperties();
    //    chatUIConfig.messageItemClickListener =
    //        new IMessageItemClickListener() {
    //          @Override
    //          public boolean onSelfIconLongClick(View view, int position, ChatMessageBean messageInfo) {
    //            ToastX.showShortToast("会话页面onSelfIconLongClick点击事件");
    //            return false;
    //          }
    //
    //          @Override
    //          public boolean onUserIconLongClick(View view, int position, ChatMessageBean messageInfo) {
    //            ToastX.showShortToast("会话页面onUserIconLongClick点击事件");
    //            return false;
    //          }
    //        };
    //设置是否展示标题栏、右侧按钮图片和右侧按钮点击事件
    //      chatUIConfig.messageProperties.showTitleBar = false;
    //      chatUIConfig.messageProperties.titleBarRightRes = R.drawable.ic_user_setting;
    //      chatUIConfig.messageProperties.titleBarRightClick = new View.OnClickListener() {
    //          @Override
    //          public void onClick(View v) {
    //              ToastX.showShortToast("会话页面标题栏右侧点击事件");
    //          }
    //      };

    //      chatUIConfig.messageProperties.selfMessageBg =new ColorDrawable(context.getResources().getColor(R.color.color_blue_3a9efb));
    //      chatUIConfig.messageProperties.receiveMessageBg =new ColorDrawable(context.getResources().getColor(R.color.color_666666));

    //      chatUIConfig.messageProperties.chatViewBackground =
    //        new ColorDrawable(context.getResources().getColor(R.color.color_blue_3a9efb));
    //个性化配置会话消息页面视图
    chatUIConfig.chatViewCustom =
        new IChatViewCustom() {
          @Override
          public void customizeChatLayout(ChatView layout) {
            //            FrameLayout frameLayout = layout.getChatBodyBottomLayout();
            //            TextView textView = new TextView(context, null);
            //            textView.setText("hahhahhhh");
            //            FrameLayout.LayoutParams layoutParams =
            //                new FrameLayout.LayoutParams(
            //                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            //            layoutParams.gravity = Gravity.BOTTOM;
            //            frameLayout.addView(textView, layoutParams);
          }
        };
    //个信号配置输入框下面输入按钮
    //    chatUIConfig.chatInputMenu =
    //        new IChatInputMenu() {
    //          @Override
    //          public List<ActionItem> customizeInputBar(List<ActionItem> actionItemList) {
    //            return actionItemList;
    //          }
    //
    //          @Override
    //          public List<ActionItem> customizeInputMore(List<ActionItem> actionItemList) {
    //            actionItemList.add(
    //                new ActionItem("custom_more_1", R.drawable.ic_user_setting, R.string.chat));
    //            return null;
    //          }
    //
    //          @Override
    //          public boolean onCustomInputClick(Context context, View view, String action) {
    //            ToastX.showShortToast(action);
    //            return true;
    //          }
    //        };

    //个性化配置会话消息中消息长按弹窗菜单
    //    chatUIConfig.chatPopMenu =
    //        new IChatPopMenu() {
    //          @NonNull
    //          @Override
    //          public List<ChatPopMenuAction> customizePopMenu(
    //              List<ChatPopMenuAction> menuList, ChatMessageBean messageBean) {
    //              if(menuList != null){
    //                  for (int index = menuList.size() - 1;index >=0;index--){
    //                      if (TextUtils.equals(menuList.get(index).getAction(),ActionConstants.POP_ACTION_PIN)){
    //                          menuList.remove(index);
    //                          break;
    //                      }
    //                  }
    //              }
    //            return menuList;
    //          }
    //
    //          @Override
    //          public boolean showDefaultPopMenu() {
    //            return true;
    //          }
    //        };
    ChatKitClient.setChatUIConfig(chatUIConfig);
    ChatKitClient.addCustomAttach(ChatMessageType.CUSTOM_STICKER, StickerAttachment.class);
    ChatKitClient.addCustomViewHolder(ChatMessageType.CUSTOM_STICKER, ChatStickerViewHolder.class);
  }

  //个性化定制联系人页面
  public static void configContactKit(Context context) {
    ContactUIConfig contactUIConfig = new ContactUIConfig();
    contactUIConfig.customLayout =
        new IContactViewLayout() {
          @Override
          public void customizeContactLayout(ContactLayout layout) {
            //            layout
            //                .getBodyLayout()
            //                .setBackgroundColor(context.getResources().getColor(R.color.color_a6adb6));
          }
        };
    //    ContactKitClient.setContactUIConfig(contactUIConfig);
  }

  //个性化配置会话列表页面
  public static void configConversation(Context context) {
    ConversationUIConfig conversationUIConfig = new ConversationUIConfig();
    conversationUIConfig.customLayout =
        new IConversationViewLayout() {
          @Override
          public void customizeConversationLayout(ConversationLayout layout) {
            //            layout
            //                .getBodyLayout()
            //                .setBackground(
            //                    new ColorDrawable(context.getResources().getColor(R.color.color_a6adb6)));
          }
        };
    conversationUIConfig.itemBackground =
        new ColorDrawable(context.getResources().getColor(R.color.color_blue_3a9efb));
    conversationUIConfig.conversationCustom =
        new ConversationCustom() {
          @Override
          public String customContentText(Context context, ConversationInfo conversationInfo) {
            if (conversationInfo != null && context != null) {
              MsgTypeEnum typeEnum = conversationInfo.getMsgType();
              if (typeEnum == MsgTypeEnum.file) {
                return "HHHHH";
              }
            }
            return super.customContentText(context, conversationInfo);
          }
        };
    //    ConversationKitClient.setConversationUIConfig(conversationUIConfig);
  }
}
