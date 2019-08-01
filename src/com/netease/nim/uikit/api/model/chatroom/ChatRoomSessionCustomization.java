package com.netease.nim.uikit.api.model.chatroom;

import com.netease.nim.uikit.business.session.actions.BaseAction;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * 聊天室聊天界面定制化参数。 可定制：<br>
 * 1. 加号展开后的按钮和动作 <br>
 */
public class ChatRoomSessionCustomization implements Serializable {

    /**
     * 加号展开后的action list。
     * 默认已包含图片，视频和地理位置
     */
    public ArrayList<BaseAction> actions;
}
