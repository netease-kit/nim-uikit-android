package com.netease.yunxin.app.im;

import android.graphics.drawable.ColorDrawable;
import android.view.View;

import androidx.annotation.NonNull;

import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.chatkit.ui.view.ChatView;
import com.netease.yunxin.kit.chatkit.ui.view.input.MessageBottomLayout;
import com.netease.yunxin.kit.chatkit.ui.view.interfaces.IMessageItemClickListener;
import com.netease.yunxin.kit.chatkit.ui.view.message.ChatMessageListView;
import com.netease.yunxin.kit.chatkit.ui.view.message.MessageProperties;
import com.netease.yunxin.kit.chatkit.ui.view.popmenu.ChatActionFactory;
import com.netease.yunxin.kit.chatkit.ui.view.popmenu.ChatPopMenuAction;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import com.netease.yunxin.kit.common.ui.widgets.BackTitleBar;

import java.util.ArrayList;
import java.util.List;

public class KitCustomConfig {

    //************custom config for chat ui *******************

    public static void initChatUICustom() {
        // for example
//        ChatUIConfig uiConfig = ChatUIConfig.getInstance();
//        P2PChatFragmentBuilder p2PChatFragmentBuilder = new P2PChatFragmentBuilder();
//        p2PChatFragmentBuilder.setChatMessageViewHolderFactory(new ChatMessageViewHolderFactory() {
//            @Nullable
//            @Override
//            public ChatBaseMessageViewHolder getViewHolderCustom(@NonNull ViewGroup parent, int viewType) {
//                return null;
//            }
//        });
//        p2PChatFragmentBuilder.setChatViewCustom(KitCustomConfig::setChatView);
//        uiConfig.setP2PChatFragmentBuilder(p2PChatFragmentBuilder);
    }

    /**
     * for example code
     */
    private static void setChatView(ChatView chatView) {
        //you can set your custom layout for ChatView Here if need

        //set titleBar here
        BackTitleBar titleBar = chatView.getTitleBar();
        titleBar.setTitle("custom title");


        //====== example for ChatMessageListView ======//
        ChatMessageListView messageListView = chatView.getMessageListView();

//        ////// set background for message list view //////
        messageListView.setBackground(new ColorDrawable(0xFFEFE5D4));
//        ////// set properties for message view,this will useful for default message view holder//////
        MessageProperties properties = new MessageProperties();
        properties.setMessageTextSize(13);
        properties.setReceiveMessageBg(new ColorDrawable(0x666666));
        properties.setSelfMessageBg(new ColorDrawable(0x123456));
        properties.setUserNickColor(0x654321);

        messageListView.setMessageProperties(properties);

        ///////set pop actions here///////
        ChatActionFactory.getInstance().setCustomPopAction(new ChatActionFactory.ICustomPopAction() {


            @NonNull
            @Override
            public List<ChatPopMenuAction> getCustomPopAction() {
                return new ArrayList<>();
            }

            /**
             * false will show default actions
             * true will show custom actions only
             */
            @Override
            public boolean abandonDefaultAction() {
                return true;
            }
        });

        final IMessageItemClickListener listener = messageListView.getItemClickListener();
        messageListView.setItemClickListener(new IMessageItemClickListener() {

            @Override
            public boolean onMessageLongClick(View view, ChatMessageBean messageInfo) {
                ToastX.showShortToast("custom onMessageLongClick");
                return listener.onMessageLongClick(view, messageInfo);
            }

            @Override
            public void onMessageClick(View view, int position, ChatMessageBean messageInfo) {
                listener.onMessageClick(view, position, messageInfo);
            }

            @Override
            public void onUserIconClick(View view, ChatMessageBean messageInfo) {
                listener.onUserIconClick(view, messageInfo);
            }

            @Override
            public void onSelfIconClick(View view) {
                listener.onSelfIconClick(view);
            }

            @Override
            public void onUserIconLongClick(View view, ChatMessageBean messageInfo) {
                //same as above
            }

            @Override
            public void onReEditRevokeMessage(View view, ChatMessageBean messageInfo) {
                //same as above
            }

            @Override
            public void onReplyMessageClick(View view, String messageUuid) {
                //same as above
            }

            @Override
            public void onSendFailBtnClick(View view, ChatMessageBean messageInfo) {
                //same as above
            }

            @Override
            public void onTextSelected(View view, int position, ChatMessageBean messageInfo) {
                //same as above
            }
        });


        //set inputView here
        MessageBottomLayout inputView = chatView.getInputView();

    }

    //************custom config for contact ui *******************
    public static void initContactUICustom() {
        //code for example
//        ContactUIConfig contactUIConfig = ContactUIConfig.getInstance();
//        ContactFragment.Builder contactBuilder = new ContactFragment.Builder();
//        contactBuilder.setTitle("custom contact");
//        contactUIConfig.setContactBuilder(contactBuilder);
    }
}
