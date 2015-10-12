package com.netease.nim.uikit.session.helper;

import com.netease.nimlib.sdk.msg.model.IMMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by huangjun on 2015/7/8.
 */
public class LocalMessageTransfer {

    private static LocalMessageTransfer instance;

    private List<LocalMessageObserver> observers = new ArrayList<>();

    public static LocalMessageTransfer getInstance() {
        if (instance == null) {
            instance = new LocalMessageTransfer();
        }

        return instance;
    }

    public interface LocalMessageObserver {
        void onMessage(IMMessage message);
    }

    public void registerObserver(LocalMessageObserver o, boolean register) {
        if (register) {
            observers.add(o);
        } else {
            observers.remove(o);
        }
    }

    public void notify(IMMessage msg) {
        for (LocalMessageObserver o : observers) {
            o.onMessage(msg);
        }
    }
}
