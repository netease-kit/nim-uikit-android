package com.netease.nim.uikit.session;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.netease.nim.uikit.session.actions.BaseAction;
import com.netease.nimlib.sdk.msg.attachment.MsgAttachment;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * 聊天界面定制化参数。 可定制：<br>
 * 1. 聊天背景 <br>
 * 2. 加号展开后的按钮和动作 <br>
 * 3. ActionBar右侧按钮。
 */
public class SessionCustomization implements Serializable {

    /**
     * 聊天背景。优先使用uri，如果没有提供uri，使用color。如果没有color，使用默认。uri暂时支持以下格式：<br>
     *     drawable: android.resource://包名/drawable/资源名
     *     assets: file:///android_asset/{asset文件路径}
     *     file: file:///文件绝对路径
     */
    public String backgroundUri;
    public int backgroundColor;

    // UIKit
    public boolean withSticker;

    /**
     * 加号展开后的action list。
     * 默认已包含图片，视频和地理位置
     */
    public ArrayList<BaseAction> actions;

    /**
     * ActionBar右侧可定制按钮。默认为空。
     */
    public ArrayList<OptionsButton> buttons;

    /**
     * 如果OptionsButton的点击响应中需要startActivityForResult，可在此函数中处理结果。
     * 需要注意的是，由于加号中的Action的限制，RequestCode只能使用int的最低8位。
     *
     * @param activity 当前的聊天Activity
     * @param requestCode 请求码
     * @param resultCode 结果码
     * @param data 返回的结果数据
     */
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {}

    // uikit内建了对贴图消息的输入和管理展示，并和emoji表情整合在了一起，但贴图消息的附件定义开发者需要根据自己的扩展
    public MsgAttachment createStickerAttachment(String category, String item) {
        return null;
    }

    /**
     * ActionBar 右侧按钮，可定制icon和点击事件
     */
    public static abstract class OptionsButton implements Serializable {

        // 图标drawable id
        public int iconId;

        // 响应事件
        public abstract void onClick(Context context, View view, String sessionId);
    }
}
