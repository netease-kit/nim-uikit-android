package com.netease.nim.uikit.api.model.contact;

import android.content.Context;
import android.os.Handler;

import java.util.ArrayList;
import java.util.List;

/**
 * 好友关系变动观察者管理
 */

public class ContactChangedObservable {

    private List<ContactChangedObserver> observers = new ArrayList<>();
    private Handler uiHandler;

    public ContactChangedObservable(Context context) {
        uiHandler = new Handler(context.getMainLooper());
    }

    public synchronized void registerObserver(ContactChangedObserver observer, boolean register) {
        if (observer == null) {
            return;
        }
        if (register) {
            observers.add(observer);
        } else {
            observers.remove(observer);
        }
    }

    public synchronized void notifyAddedOrUpdated(final List<String> accounts) {
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                for (ContactChangedObserver observer : observers) {
                    observer.onAddedOrUpdatedFriends(accounts);
                }
            }
        });
    }

    public synchronized void notifyDelete(final List<String> accounts) {
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                for (ContactChangedObserver observer : observers) {
                    observer.onDeletedFriends(accounts);
                }
            }
        });
    }

    public synchronized void notifyAddToBlackList(final List<String> accounts) {
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                for (ContactChangedObserver observer : observers) {
                    observer.onAddUserToBlackList(accounts);
                }
            }
        });
    }

    public synchronized void notifyRemoveFromBlackList(final List<String> accounts) {
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                for (ContactChangedObserver observer : observers) {
                    observer.onRemoveUserFromBlackList(accounts);
                }
            }
        });
    }
}
