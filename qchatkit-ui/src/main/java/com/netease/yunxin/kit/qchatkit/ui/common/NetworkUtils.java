/*
 * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.kit.qchatkit.ui.common;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.netease.yunxin.kit.qchatkit.ui.R;

import java.util.ArrayList;
import java.util.List;

/**
 * The utils for network.
 */
public final class NetworkUtils {
    private static final int STATE_NETWORK_AVAILABLE = 1;
    private static final int STATE_NETWORK_LOST = 2;
    private static final Handler handler = new Handler(Looper.getMainLooper());

    private static int currentState = STATE_NETWORK_LOST;
    @SuppressLint("StaticFieldLeak")
    private static Context context;
    private static ConnectivityManager manager;

    private static NetworkInfo currentNetworkInfo = null;
    private static volatile boolean isInited = false;

    private static final List<NetworkStateListener> networkStateListenerList = new ArrayList<>();
    private static final ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
        @Override
        public void onAvailable(@NonNull Network network) {
            currentNetworkInfo = getManager().getNetworkInfo(network);
            if (currentState == STATE_NETWORK_AVAILABLE) {
                return;
            }
            currentState = STATE_NETWORK_AVAILABLE;
            handler.post(() -> {
                for (NetworkStateListener listener : networkStateListenerList) {
                    if (listener != null) {
                        listener.onAvailable(currentNetworkInfo);
                    }
                }
            });
        }

        @Override
        public void onLost(@NonNull Network network) {
            currentNetworkInfo = getManager().getNetworkInfo(network);
            if (currentState == STATE_NETWORK_LOST) {
                return;
            }
            currentState = STATE_NETWORK_LOST;
            handler.post(() -> {
                for (NetworkStateListener listener : networkStateListenerList) {
                    if (listener != null) {
                        listener.onLost(currentNetworkInfo);
                    }
                }
            });
        }
    };

    /**
     * the current network is connected or not
     */
    public static boolean isConnected(Context context) {
        NetworkInfo networkInfo = getActiveNetworkInfo(context);
        return networkInfo != null && networkInfo.isConnected();
    }

    public static void isConnectedToastAndRun(Context context, String toast, Runnable runnable) {
        isConnectedToastAndRun(context, runnable, () -> Toast.makeText(context, toast, Toast.LENGTH_SHORT).show());
    }

    public static void isConnectedToastAndRun(Context context, Runnable SuccessRunnable, Runnable failRunnable) {
        if (isConnected(context)) {
            if (SuccessRunnable != null) {
                SuccessRunnable.run();
            }
        } else {
            if (failRunnable != null) {
                failRunnable.run();
            }
        }
    }

    public static void isConnectedToastAndRun(Context context, Runnable runnable) {
        isConnectedToastAndRun(context, context.getString(R.string.qchat_network_error), runnable);
    }

    private static NetworkInfo getActiveNetworkInfo(Context context) {
        ConnectivityManager cm = getManager(context);
        if (cm == null) return null;
        return cm.getActiveNetworkInfo();
    }

    /**
     * register network listener.
     */
    public synchronized static void registerStateListener(NetworkStateListener listener) {
        if (listener == null) {
            return;
        }
        networkStateListenerList.add(listener);
        if (currentState == STATE_NETWORK_AVAILABLE) {
            listener.onAvailable(currentNetworkInfo);
        } else if (currentState == STATE_NETWORK_LOST) {
            listener.onLost(currentNetworkInfo);
        }
    }

    public synchronized static void unregisterStateListener(NetworkStateListener listener) {
        if (listener == null) {
            return;
        }
        networkStateListenerList.remove(listener);
    }

    /**
     * init to listen network.
     */
    public synchronized static void init(Context context) {
        if (isInited) {
            return;
        }
        NetworkUtils.context = context.getApplicationContext();
        NetworkRequest request = new NetworkRequest.Builder().build();
        getManager(context).registerNetworkCallback(request, networkCallback);
        currentState = getManager(context).isDefaultNetworkActive() ? STATE_NETWORK_AVAILABLE : STATE_NETWORK_LOST;
        currentNetworkInfo = getActiveNetworkInfo(context);
        isInited = true;
    }

    public synchronized static void unInit() {
        if (!isInited) {
            return;
        }
        getManager(context).unregisterNetworkCallback(networkCallback);
        currentState = STATE_NETWORK_LOST;
        context = null;
        isInited = true;
    }

    private static ConnectivityManager getManager() {
        if (manager != null) {
            return manager;
        }
        manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return manager;
    }

    private static ConnectivityManager getManager(Context context) {
        if (manager != null) {
            return manager;
        }
        manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return manager;
    }

    public interface NetworkStateListener {
        void onAvailable(NetworkInfo networkInfo);

        void onLost(NetworkInfo networkInfo);
    }
}
