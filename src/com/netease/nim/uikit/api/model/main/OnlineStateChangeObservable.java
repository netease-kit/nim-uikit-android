package com.netease.nim.uikit.api.model.main;

import android.content.Context;
import android.os.Handler;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * 在线状态事件变更通知接口
 */

public class OnlineStateChangeObservable {

    // 在线状态变化监听
    private List<OnlineStateChangeObserver> onlineStateChangeObservers;
    private Handler uiHandler;

    public OnlineStateChangeObservable(Context context) {
        onlineStateChangeObservers = new LinkedList<>();
        uiHandler = new Handler(context.getMainLooper());
    }

    public synchronized void registerOnlineStateChangeListeners(OnlineStateChangeObserver onlineStateChangeObserver, boolean register) {
        if (register) {
            onlineStateChangeObservers.add(onlineStateChangeObserver);
        }else {
            onlineStateChangeObservers.remove(onlineStateChangeObserver);
        }
    }

    public synchronized void notifyOnlineStateChange(final Set<String> accounts) {
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                if (onlineStateChangeObservers != null) {
                    for (OnlineStateChangeObserver listener : onlineStateChangeObservers) {
                        listener.onlineStateChange(accounts);
                    }
                }
            }
        });
    }
}
