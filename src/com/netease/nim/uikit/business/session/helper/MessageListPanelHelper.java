package com.netease.nim.uikit.business.session.helper;

import com.netease.nimlib.sdk.msg.model.IMMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * 聊天消息列表辅助类
 * 主要用于某些需求场景下需要往聊天消息列表中手动添加消息、删除消息、或者清空消息列表。
 * Created by huangjun on 2015/7/8.
 */
public class MessageListPanelHelper {

    private static MessageListPanelHelper instance;

    private List<LocalMessageObserver> observers = new ArrayList<>();

    public static MessageListPanelHelper getInstance() {
        if (instance == null) {
            instance = new MessageListPanelHelper();
        }

        return instance;
    }

    public interface LocalMessageObserver {
        void onAddMessage(IMMessage message);

        void onClearMessages(String account);
    }

    public void registerObserver(LocalMessageObserver o, boolean register) {
        if (register) {
            observers.add(o);
        } else {
            observers.remove(o);
        }
    }

    public void notifyAddMessage(IMMessage msg) {
        for (LocalMessageObserver o : observers) {
            o.onAddMessage(msg);
        }
    }

    public void notifyClearMessages(String account) {
        for (LocalMessageObserver o : observers) {
            o.onClearMessages(account);
        }
    }
}
