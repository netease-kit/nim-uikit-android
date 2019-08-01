package com.netease.nim.uikit.api.model.main;

import android.os.Handler;

import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.auth.AuthServiceObserver;
import com.netease.nimlib.sdk.auth.constant.LoginSyncStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * 登录
 * Created by huangjun on 2015/10/9.
 */
public class LoginSyncDataStatusObserver {

    private static final String TAG = LoginSyncDataStatusObserver.class.getSimpleName();

    private static final int TIME_OUT_SECONDS = 10;

    private Handler uiHandler;

    private Runnable timeoutRunnable;

    /**
     * 状态
     */
    private LoginSyncStatus syncStatus = LoginSyncStatus.NO_BEGIN;

    /**
     * 监听
     */
    private List<Observer<Void>> observers = new ArrayList<>();

    /**
     * 注销时清除状态&监听
     */
    public void reset() {
        syncStatus = LoginSyncStatus.NO_BEGIN;
        observers.clear();
    }

    /**
     * 在App启动时向SDK注册登录后同步数据过程状态的通知
     * 调用时机：主进程Application onCreate中
     */
    public void registerLoginSyncDataStatus(boolean register) {
        LogUtil.i(TAG, "observe login sync data completed event on Application create");
        NIMClient.getService(AuthServiceObserver.class).observeLoginSyncDataStatus(loginSyncStatusObserver, register);
        NIMClient.getService(AuthServiceObserver.class).observeLoginSyncTeamMembersCompleteResult(syncTeamMemberObserver, register);
    }

    private Observer<LoginSyncStatus> loginSyncStatusObserver = new Observer<LoginSyncStatus>() {
        @Override
        public void onEvent(LoginSyncStatus status) {
            syncStatus = status;
            if (status == LoginSyncStatus.BEGIN_SYNC) {
                LogUtil.i(TAG, "login sync data begin");
            } else if (status == LoginSyncStatus.SYNC_COMPLETED) {
                LogUtil.i(TAG, "login sync data completed");
                onLoginSyncDataCompleted(false);
            }
        }
    };
    private Observer<Boolean> syncTeamMemberObserver = new Observer<Boolean>() {
        @Override
        public void onEvent(Boolean result) {
            LogUtil.i(TAG, "login sync all team members result = " + result);
        }
    };

    /**
     * 监听登录后同步数据完成事件，缓存构建完成后自动取消监听
     * 调用时机：登录成功后
     *
     * @param observer 观察者
     * @return 返回true表示数据同步已经完成或者不进行同步，返回false表示正在同步数据
     */
    public boolean observeSyncDataCompletedEvent(Observer<Void> observer) {
        if (syncStatus == LoginSyncStatus.NO_BEGIN || syncStatus == LoginSyncStatus.SYNC_COMPLETED) {
            /*
             * NO_BEGIN 如果登录后未开始同步数据，那么可能是自动登录的情况:
             * PUSH进程已经登录同步数据完成了，此时UI进程启动后并不知道，这里直接视为同步完成
             */
            return true;
        }

        // 正在同步
        if (!observers.contains(observer)) {
            observers.add(observer);
        }

        // 超时定时器
        if (uiHandler == null) {
            uiHandler = new Handler(NimUIKit.getContext().getMainLooper());
        }

        if (timeoutRunnable == null) {
            timeoutRunnable = new Runnable() {
                @Override
                public void run() {
                    // 如果超时还处于开始同步的状态，模拟结束
                    if (syncStatus == LoginSyncStatus.BEGIN_SYNC) {
                        onLoginSyncDataCompleted(true);
                    }
                }
            };
        }

        uiHandler.removeCallbacks(timeoutRunnable);
        uiHandler.postDelayed(timeoutRunnable, TIME_OUT_SECONDS * 1000);

        return false;
    }

    /**
     * 登录同步数据完成处理
     */
    private void onLoginSyncDataCompleted(boolean timeout) {
        LogUtil.i(TAG, "onLoginSyncDataCompleted, timeout=" + timeout);

        // 移除超时任务（有可能完成包到来的时候，超时任务都还没创建）
        if (timeoutRunnable != null) {
            uiHandler.removeCallbacks(timeoutRunnable);
        }

        // 通知上层
        for (Observer<Void> o : observers) {
            o.onEvent(null);
        }

        // 重置状态
        reset();
    }


    /**
     * 单例
     */
    public static LoginSyncDataStatusObserver getInstance() {
        return InstanceHolder.instance;
    }

    static class InstanceHolder {
        final static LoginSyncDataStatusObserver instance = new LoginSyncDataStatusObserver();
    }
}
