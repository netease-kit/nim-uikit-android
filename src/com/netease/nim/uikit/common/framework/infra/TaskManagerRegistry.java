package com.netease.nim.uikit.common.framework.infra;

import android.content.Context;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class TaskManagerRegistry {
    private static List<WeakReference<TaskManager>> managers = new ArrayList<WeakReference<TaskManager>>();

    public static void register(TaskManager manager) {
        synchronized (managers) {
            managers.add(new WeakReference<TaskManager>(manager));
        }
    }

    public static void waitAll(final Context context, final Runnable done, final int max, final int interval) {
        cancelAll(true);

        Handlers.sharedHandler(context).postDelayed(new Runnable() {
            int count;

            @Override
            public void run() {
                if (!idle() && count++ < max) {
                    Handlers.sharedHandler(context).postDelayed(this, interval);
                } else {
                    done.run();
                }
            }
        }, interval);
    }

    private static void cancelAll(boolean clear) {
        synchronized (managers) {
            for (int i = 0; i < managers.size(); ++i) {
                TaskManager manager = managers.get(i).get();
                if (manager != null) {
                    manager.shutdown();
                } else {
                    managers.remove(i--);
                }
            }

            if (clear) {
                managers.clear();
            }
        }
    }

    private static boolean idle() {
        synchronized (managers) {
            for (int i = 0; i < managers.size(); ++i) {
                TaskManager manager = managers.get(i).get();
                if (manager != null) {
                    if (manager.count() > 0) {
                        return false;
                    }
                } else {
                    managers.remove(i--);
                }
            }
        }
        return true;
    }
}
