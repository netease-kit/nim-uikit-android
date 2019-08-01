package com.netease.nim.uikit.impl.customization;

import android.content.Context;

import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.api.model.contact.ContactEventListener;

/**
 * ContactEventListener 通讯录联系人列表一些点击事件的响应处理
 * <p>
 * DefaultContactEventListener 提供了默认处理，其中点击Item 和 Avatar 响应为打开P2P聊天界面
 * <p>
 * Created by hzchenkang on 2016/12/21.
 */

public class DefaultContactEventListener implements ContactEventListener {

    @Override
    public void onItemClick(Context context, String account) {
        // 点击联系人之后，可以选择打开个人信息页面或者聊天页面
        NimUIKit.startP2PSession(context, account);
    }

    @Override
    public void onItemLongClick(Context context, String account) {
        // 长按联系人
    }

    @Override
    public void onAvatarClick(Context context, String account) {
        // 点击联系人之后，可以选择打开个人信息页面或者聊天页面
        NimUIKit.startP2PSession(context, account);
    }
}
