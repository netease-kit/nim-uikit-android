package com.netease.yunxin.kit.chatkit.ui.view.popmenu;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

import com.netease.yunxin.kit.corekit.im.XKitImClient;

public class ChatPopMenuAction {
    private String actionName;
    private @DrawableRes
    int actionIcon;
    private OnClickListener actionClickListener;

    public ChatPopMenuAction(String actionName, @DrawableRes int actionIcon) {
        this(actionName, actionIcon, null);
    }

    public ChatPopMenuAction(@StringRes int nameRes, @DrawableRes int actionIcon, OnClickListener actionClickListener) {
        this(XKitImClient.getApplicationContext().getString(nameRes), actionIcon, actionClickListener);
    }

    public ChatPopMenuAction(String actionName, @DrawableRes int actionIcon, OnClickListener actionClickListener) {
        this.actionName = actionName;
        this.actionIcon = actionIcon;
        this.actionClickListener = actionClickListener;
    }

    public void setActionName(String actionName) {
        this.actionName = actionName;
    }

    public String getActionName() {
        return actionName;
    }

    public void setActionIcon(@DrawableRes int actionIcon) {
        this.actionIcon = actionIcon;
    }

    public @DrawableRes
    int getActionIcon() {
        return actionIcon;
    }

    public void setActionClickListener(OnClickListener actionClickListener) {
        this.actionClickListener = actionClickListener;
    }

    public OnClickListener getActionClickListener() {
        return actionClickListener;
    }

    @FunctionalInterface
    public interface OnClickListener {
        void onClick();
    }
}
