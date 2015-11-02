package com.netease.nim.uikit.session.module.list;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.netease.nim.uikit.R;
import com.netease.nim.uikit.common.ui.imageview.HeadImageView;
import com.netease.nim.uikit.common.ui.listview.ListViewUtil;
import com.netease.nim.uikit.common.ui.listview.MessageListView;
import com.netease.nim.uikit.session.emoji.MoonUtil;
import com.netease.nim.uikit.session.helper.TeamNotificationHelper;
import com.netease.nimlib.sdk.msg.model.IMMessage;

/**
 * 新消息提醒模块
 * Created by hzxuwen on 2015/6/17.
 */
public class IncomingMsgPrompt {
    // 底部新消息提示条
    private View newMessageTipLayout;
    private TextView newMessageTipTextView;
    private HeadImageView newMessageTipHeadImageView;

    private Context context;
    private View view;
    private MessageListView messageListView;
    private Handler uiHandler;

    public IncomingMsgPrompt(Context context, View view, MessageListView messageListView, Handler uiHandler) {
        this.context = context;
        this.view = view;
        this.messageListView = messageListView;
        this.uiHandler = uiHandler;
    }

    // 显示底部新信息提示条
    public void show(IMMessage newMessage) {
        if (newMessageTipLayout == null) {
            init();
        }

        if (!TextUtils.isEmpty(newMessage.getFromAccount())) {
            newMessageTipHeadImageView.loadBuddyAvatar(newMessage.getFromAccount());
        } else {
            newMessageTipHeadImageView.resetImageView();
        }

        MoonUtil.identifyFaceExpression(context, newMessageTipTextView, TeamNotificationHelper.getMsgShowText(newMessage),
                ImageSpan.ALIGN_BOTTOM);
        newMessageTipLayout.setVisibility(View.VISIBLE);
        uiHandler.removeCallbacks(showNewMessageTipLayoutRunnable);
        uiHandler.postDelayed(showNewMessageTipLayoutRunnable, 5 * 1000);
    }

    public void onBackPressed() {
        removeHandlerCallback();
    }

    // 初始化底部新信息提示条
    private void init() {
        ViewGroup containerView = (ViewGroup) view.findViewById(R.id.message_activity_list_view_container);
        View.inflate(context, R.layout.nim_new_message_tip_layout, containerView);
        newMessageTipLayout = containerView.findViewById(R.id.new_message_tip_layout);
        newMessageTipLayout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                ListViewUtil.scrollToBottom(messageListView);
                newMessageTipLayout.setVisibility(View.GONE);
            }
        });
        newMessageTipTextView = (TextView) newMessageTipLayout.findViewById(R.id.new_message_tip_text_view);
        newMessageTipHeadImageView = (HeadImageView) newMessageTipLayout.findViewById(R.id.new_message_tip_head_image_view);
    }

    private Runnable showNewMessageTipLayoutRunnable = new Runnable() {

        @Override
        public void run() {
            newMessageTipLayout.setVisibility(View.GONE);
        }
    };

    private void removeHandlerCallback() {
        if (showNewMessageTipLayoutRunnable != null) {
            uiHandler.removeCallbacks(showNewMessageTipLayoutRunnable);
        }
    }
}
