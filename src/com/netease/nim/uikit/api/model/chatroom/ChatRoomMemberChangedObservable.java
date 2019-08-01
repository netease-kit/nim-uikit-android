package com.netease.nim.uikit.api.model.chatroom;

import android.content.Context;
import android.os.Handler;

import com.netease.nimlib.sdk.chatroom.model.ChatRoomMember;

import java.util.ArrayList;
import java.util.List;

/**
 * UIKit 与 app 聊天室成员变化监听接口
 */

public class ChatRoomMemberChangedObservable {

    private List<RoomMemberChangedObserver> observers = new ArrayList<>();
    private Handler uiHandler;

    public ChatRoomMemberChangedObservable(Context context) {
        uiHandler = new Handler(context.getMainLooper());
    }

    public synchronized void registerObserver(RoomMemberChangedObserver observer, boolean register) {
        if (observer == null) {
            return;
        }
        if (register) {
            observers.add(observer);
        } else {
            observers.remove(observer);
        }
    }

    public synchronized void notifyMemberChange(final ChatRoomMember member, final boolean in) {
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                if (in) {
                    for (RoomMemberChangedObserver o : observers) {
                        o.onRoomMemberIn(member);
                    }
                } else {
                    for (RoomMemberChangedObserver o : observers) {
                        o.onRoomMemberExit(member);
                    }
                }
            }
        });
    }
}
