package com.netease.yunxin.kit.conversationkit.ui;

import android.view.View;

import com.netease.yunxin.kit.conversationkit.model.ConversationInfo;

import java.util.Comparator;

public class ConversationUIConfig {

    public static int INT_DEFAULT_NULL = -1;

    public boolean showTitleBar = true;
    public boolean showTitleBarLeftIcon  = true;
    public boolean showTitleBarRightIcon = true;
    public boolean showTitleBarRight2Icon = true;

    public int titleBarLeftRes = INT_DEFAULT_NULL;
    public int titleBarRightRes = INT_DEFAULT_NULL;
    public int titleBarRight2Res = INT_DEFAULT_NULL;

    public String titleBarTitle;
    public int titleBarTitleColor = INT_DEFAULT_NULL;

    public int itemTitleColor = INT_DEFAULT_NULL;
    public int itemTitleSize = INT_DEFAULT_NULL;
    public int itemContentColor = INT_DEFAULT_NULL;
    public int itemContentSize = INT_DEFAULT_NULL;
    public int itemDateColor = INT_DEFAULT_NULL;
    public int itemDateSize = INT_DEFAULT_NULL;

    public float avatarCornerRadius = INT_DEFAULT_NULL;

    public View.OnClickListener titleBarRightClick;
    public View.OnClickListener titleBarRight2Click;
    public ItemClickListener itemClickListener;
    public Comparator<ConversationInfo> conversationComparator;
    public IConversationFactory conversationFactory;

}
