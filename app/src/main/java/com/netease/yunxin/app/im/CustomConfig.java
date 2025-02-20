// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.im;

import android.content.Context;
import com.netease.yunxin.kit.chatkit.ui.ChatKitClient;
import com.netease.yunxin.kit.chatkit.ui.ChatUIConfig;
import com.netease.yunxin.kit.contactkit.ui.ContactUIConfig;
import com.netease.yunxin.kit.conversationkit.local.ui.LocalConversationKitClient;
import com.netease.yunxin.kit.conversationkit.local.ui.LocalConversationUIConfig;

public class CustomConfig {

  // 个性化配置会话消息页面
  public static void configChatKit(Context context) {
    // test
    ChatUIConfig chatUIConfig = new ChatUIConfig();

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
    //
    //            @Override
    //            public boolean onMessageClick(View view, int position, ChatMessageBean messageInfo) {
    //                ToastX.showShortToast("会话消息点击事件"+messageInfo.getMessageData().getMessage().getContent());
    //                return false;
    //            }
    //
    //            @Override
    //            public boolean onReplyMessageClick(View view, int position, IMMessageInfo messageInfo) {
    //                ToastX.showShortToast("onReplyMessageClick"+messageInfo.getMessage().getContent());
    //                return false;
    //            }
    //
    //            @Override
    //            public boolean onReEditRevokeMessage(
    //                    View view,
    //                    int position,
    //                    ChatMessageBean messageInfo
    //            ) {
    //                ToastX.showShortToast("onReEditRevokeMessage"+messageInfo.getMessageData().getMessage().getContent());
    //                return false;
    //            }
    //        };
    // 设置是否展示标题栏、右侧按钮图片和右侧按钮点击事件
    //    chatUIConfig.messageProperties = new MessageProperties();

    //    chatUIConfig.messageProperties.showTitleBar = false;
    //    chatUIConfig.messageProperties.titleBarRightRes = R.drawable.ic_user_setting;
    //      chatUIConfig.messageProperties.avatarCornerRadius = 30f;
    //      chatUIConfig.messageProperties.messageTextColor = Color.GREEN;
    //    chatUIConfig.messageProperties.titleBarRightClick =
    //        new View.OnClickListener() {
    //          @Override
    //          public void onClick(View v) {
    //            ToastX.showShortToast("会话页面标题栏右侧点击事件");
    //          }
    //        };

    //    chatUIConfig.messageProperties.selfMessageBg =
    //        new ColorDrawable(context.getResources().getColor(R.color.color_blue_3a9efb));
    //    chatUIConfig.messageProperties.receiveMessageBg =
    //        new ColorDrawable(context.getResources().getColor(R.color.color_666666));
    //
    //    chatUIConfig.messageProperties.chatViewBackground =
    //        new ColorDrawable(context.getResources().getColor(R.color.red));
    // 个性化配置会话消息页面视图
    //    chatUIConfig.chatViewCustom =
    //        new IChatViewCustom() {
    //          @Override
    //          public void customizeChatLayout(IChatView layout) {
    //            if (layout instanceof FunChatView) {
    //              FunChatView chatLayout = (FunChatView) layout;
    //              FrameLayout frameLayout = chatLayout.getChatBodyBottomLayout();
    //              TextView textView = new TextView(context, null);
    //              textView.setText("hahhahhhh");
    //              FrameLayout.LayoutParams layoutParams =
    //                  new FrameLayout.LayoutParams(
    //                      ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    //              layoutParams.gravity = Gravity.BOTTOM;
    //              frameLayout.addView(textView, layoutParams);
    //            } else if (layout instanceof ChatView) {
    //              ChatView chatLayout = (ChatView) layout;
    //              FrameLayout frameLayout = chatLayout.getChatBodyBottomLayout();
    //              TextView textView = new TextView(context, null);
    //              textView.setText("hahhahhhh");
    //              FrameLayout.LayoutParams layoutParams =
    //                  new FrameLayout.LayoutParams(
    //                      ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    //              layoutParams.gravity = Gravity.BOTTOM;
    //              frameLayout.addView(textView, layoutParams);
    //            }
    //          }
    //        };
    // 个信号配置输入框下面输入按钮
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

    // 个性化配置会话消息中消息长按弹窗菜单
    //    chatUIConfig.chatPopMenu =
    //        new IChatPopMenu() {
    //          @NonNull
    //          @Override
    //          public List<ChatPopMenuAction> customizePopMenu(
    //              List<ChatPopMenuAction> menuList, ChatMessageBean messageBean) {
    //              if(menuList != null){
    //                  for (int index = menuList.size() - 1;index >=0;index--){
    //                      if
    // (TextUtils.equals(menuList.get(index).getAction(),ActionConstants.POP_ACTION_PIN)){
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
    //    ChatKitClient.addCustomAttach(ChatMessageType.CUSTOM_STICKER, StickerAttachment.class);
    //    ChatKitClient.addCustomViewHolder(ChatMessageType.CUSTOM_STICKER, ChatStickerViewHolder.class);
  }

  // 个性化定制联系人页面
  public static void configContactKit(Context context) {
    ContactUIConfig contactUIConfig = new ContactUIConfig();
    //    contactUIConfig.showHeader = false;
    //    contactUIConfig.showTitleBar = false;
    //    contactUIConfig.showTitleBarRightIcon = false;
    //    contactUIConfig.showTitleBarRight2Icon = false;

    //    contactUIConfig.title = "我的通讯录";
    //    contactUIConfig.titleColor = Color.GREEN;
    //    contactUIConfig.titleBarRight2Res = R.drawable.ic_about;
    //    contactUIConfig.titleBarRightRes = R.drawable.ic_about;
    //    contactUIConfig.titleBarRightClick = v -> ToastX.showShortToast("titleBarRightClick");
    //    contactUIConfig.titleBarRight2Click = v -> ToastX.showShortToast("titleBarRight2Click");

    //      contactUIConfig.contactAttrs = new ContactListViewAttrs();
    //      contactUIConfig.contactAttrs.setShowIndexBar(false);
    //      contactUIConfig.contactAttrs.setNameTextColor(Color.GREEN);
    //      contactUIConfig.contactAttrs.setIndexTextColor(Color.RED);
    //      contactUIConfig.itemClickListeners.put(2, (position, data) -> {
    //          ToastX.showShortToast("itemClickListeners:2");
    //      });

    //    contactUIConfig.customLayout =
    //        new IContactViewLayout() {
    //          @Override
    //          public void customizeContactLayout(ContactLayout layout) {
    //                        layout
    //                            .getBodyLayout()
    //
    // .setBackgroundColor(context.getResources().getColor(R.color.color_a6adb6));
    //              TextView textView = new TextView(context);
    //                                textView.setText("this is contact");
    //                        layout.getBodyTopLayout().addView(textView);
    //          }
    //        };
    //        ContactKitClient.setContactUIConfig(contactUIConfig);
  }

  // 个性化配置会话列表页面
  public static void configConversation(Context context) {
    LocalConversationUIConfig conversationUIConfig = new LocalConversationUIConfig();
    //    conversationUIConfig.showTitleBarLeftIcon = false;
    //    conversationUIConfig.showTitleBarRight2Icon = false;
    //    conversationUIConfig.showTitleBarRightIcon = false;
    //    conversationUIConfig.showTitleBar = false;
    //    conversationUIConfig.titleBarLeftRes = R.drawable.ic_more_point;
    //    conversationUIConfig.titleBarRightRes = R.drawable.ic_more_point;
    //    conversationUIConfig.titleBarRight2Res = R.drawable.ic_more_point;
    //    conversationUIConfig.titleBarLeftClick = v -> {
    //
    //        ToastX.showShortToast("titleBarLeftClick");
    //    };
    //      conversationUIConfig.titleBarRightClick = v -> {
    //
    //          ToastX.showShortToast("titleBarRightClick");
    //      };
    //      conversationUIConfig.titleBarRight2Click = v -> {
    //
    //          ToastX.showShortToast("titleBarRight2Click");
    //      };

    //      conversationUIConfig.titleBarTitle = "会话列表";
    //      conversationUIConfig.titleBarTitleColor = Color.GREEN;
    //
    //
    //      conversationUIConfig.itemTitleColor = Color.GREEN;
    //      conversationUIConfig.itemTitleSize = 42;
    //      conversationUIConfig.itemBackground = new ColorDrawable(Color.BLACK);
    //      conversationUIConfig.itemContentColor = Color.GREEN;
    //      conversationUIConfig.itemContentSize = 32;
    //      conversationUIConfig.itemDateColor = Color.GREEN;
    //      conversationUIConfig.itemDateSize = 32;
    //      conversationUIConfig.itemStickTopBackground = new ColorDrawable(Color.RED);
    //      conversationUIConfig.avatarCornerRadius = 30f;

    //      conversationUIConfig.itemClickListener =
    //        new ItemClickListener() {
    //          @Override
    //          public boolean onClick(Context context, ConversationBean data, int position) {
    //
    //            ToastX.showShortToast("onClick");
    //            return false;
    //          }
    //
    //          @Override
    //          public boolean onAvatarClick(Context context, ConversationBean data, int position) {
    //            ToastX.showShortToast("onAvatarClick");
    //            return false;
    //          }
    //
    //            @Override
    //            public boolean onLongClick(Context context, ConversationBean data, int position) {
    //                ToastX.showShortToast("onLongClick");
    //                return false;
    //            }
    //
    //            @Override
    //            public boolean onAvatarLongClick(Context context, ConversationBean data, int
    // position) {
    //                ToastX.showShortToast("onAvatarLongClick");
    //                return false;
    //            }
    //        };

    //      conversationUIConfig.conversationCustom = new ConversationCustom(){
    //          @Override
    //          public String customContentText(Context context, ConversationInfo conversationInfo)
    // {
    //              String test = super.customContentText(context,conversationInfo);
    //              return test+"test";
    //          }
    //      };

    //      conversationUIConfig.customLayout = new IConversationViewLayout() {
    //          @Override
    //          public void customizeConversationLayout(ConversationBaseFragment fragment) {
    //
    //              if (fragment instanceof ConversationFragment){
    //                  ConversationFragment conversationFragment = (ConversationFragment) fragment;
    //                  TextView textView = new TextView(conversationFragment.getContext());
    //                  textView.setText("this is conversationFragment");
    //                  conversationFragment.getBodyTopLayout().addView(textView);
    //              }else if (fragment instanceof FunConversationFragment){
    //                  FunConversationFragment conversationFragment = (FunConversationFragment)
    // fragment;
    //                  TextView textView = new TextView(conversationFragment.getContext());
    //                  textView.setText("this is FunConversationFragment");
    //                  conversationFragment.getBodyTopLayout().addView(textView);
    //              }
    //
    //          }
    //      };

    LocalConversationKitClient.setConversationUIConfig(conversationUIConfig);
  }
}
