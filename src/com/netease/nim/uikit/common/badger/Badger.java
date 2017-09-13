package com.netease.nim.uikit.common.badger;

import android.os.Handler;
import android.util.Log;

import com.netease.nim.uikit.NimUIKit;
import com.netease.nim.uikit.common.framework.infra.Handlers;

import me.leolin.shortcutbadger.ShortcutBadger;

/**
 * APP图标未读数红点更新接口
 * https://github.com/leolin310148/ShortcutBadger
 * <p>
 * Created by huangjun on 2017/7/25.
 */

public class Badger {
    private static final String TAG = "Badger";

    private static Handler handler;

    public static void updateBadgerCount(final int unreadCount) {
        if (handler == null) {
            handler = Handlers.sharedInstance().newHandler("Badger");
        }

        handler.removeCallbacksAndMessages(null);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                int badgerCount = unreadCount;
                if (badgerCount < 0) {
                    badgerCount = 0;
                } else if (badgerCount > 99) {
                    badgerCount = 99;
                }

                boolean res = ShortcutBadger.applyCount(NimUIKit.getContext(), badgerCount);
                Log.i(TAG, "update badger count " + (res ? "success" : "failed"));
            }
        }, 200);
    }
}
