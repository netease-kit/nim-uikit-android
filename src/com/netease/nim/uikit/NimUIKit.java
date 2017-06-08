package com.netease.nim.uikit;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.netease.nim.uikit.cache.DataCacheManager;
import com.netease.nim.uikit.cache.TeamDataCache;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.netease.nim.uikit.common.util.storage.StorageType;
import com.netease.nim.uikit.common.util.storage.StorageUtil;
import com.netease.nim.uikit.common.util.sys.ScreenUtil;
import com.netease.nim.uikit.contact.ContactEventListener;
import com.netease.nim.uikit.contact.ContactProvider;
import com.netease.nim.uikit.contact_selector.activity.ContactSelectActivity;
import com.netease.nim.uikit.custom.DefalutContactEventListener;
import com.netease.nim.uikit.custom.DefalutP2PSessionCustomization;
import com.netease.nim.uikit.custom.DefalutTeamSessionCustomization;
import com.netease.nim.uikit.custom.DefaultUserInfoProvider;
import com.netease.nim.uikit.custom.DefaultContactProvider;
import com.netease.nim.uikit.glide.ImageLoaderKit;
import com.netease.nim.uikit.session.SessionCustomization;
import com.netease.nim.uikit.session.SessionEventListener;
import com.netease.nim.uikit.session.activity.P2PMessageActivity;
import com.netease.nim.uikit.session.activity.TeamMessageActivity;
import com.netease.nim.uikit.session.emoji.StickerManager;
import com.netease.nim.uikit.session.module.MsgForwardFilter;
import com.netease.nim.uikit.session.module.MsgRevokeFilter;
import com.netease.nim.uikit.session.viewholder.MsgViewHolderBase;
import com.netease.nim.uikit.session.viewholder.MsgViewHolderFactory;
import com.netease.nim.uikit.team.activity.AdvancedTeamInfoActivity;
import com.netease.nim.uikit.team.activity.NormalTeamInfoActivity;
import com.netease.nim.uikit.uinfo.UserInfoHelper;
import com.netease.nimlib.sdk.AbortableFuture;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.auth.AuthService;
import com.netease.nimlib.sdk.auth.LoginInfo;
import com.netease.nimlib.sdk.msg.attachment.MsgAttachment;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.team.constant.TeamTypeEnum;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.nimlib.sdk.uinfo.UserInfoProvider;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * UIKit能力输出类。
 */
public final class NimUIKit {

    // context
    private static Context context;

    // 自己的用户帐号
    private static String account;

    // 用户信息提供者
    private static UserInfoProvider userInfoProvider;

    // 通讯录信息提供者
    private static ContactProvider contactProvider;

    // 地理位置信息提供者
    private static LocationProvider locationProvider;

    // 图片加载、缓存与管理组件
    private static ImageLoaderKit imageLoaderKit;

    // 会话窗口消息列表一些点击事件的响应处理函数
    private static SessionEventListener sessionListener;

    // 通讯录列表一些点击事件的响应处理函数
    private static ContactEventListener contactEventListener;

    // 转发消息过滤器
    private static MsgForwardFilter msgForwardFilter;

    // 撤回消息过滤器
    private static MsgRevokeFilter msgRevokeFilter;

    // 自定义推送配置
    private static CustomPushContentProvider customPushContentProvider;

    // 单聊界面定制
    private static SessionCustomization commonP2PSessionCustomization;

    // 群聊界面定制
    private static SessionCustomization commonTeamSessionCustomization;

    // 在线状态展示内容
    private static OnlineStateContentProvider onlineStateContentProvider;

    // 在线状态变化监听
    private static List<OnlineStateChangeListener> onlineStateChangeListeners;

    /**
     * 初始化UIKit, 用户信息、联系人信息使用 {@link DefaultUserInfoProvider}，{@link DefaultContactProvider}
     * 若用户自行提供 userInfoProvider，contactProvider，请使用 {@link NimUIKit#init(Context, UserInfoProvider, ContactProvider)}
     *
     * @param context
     */
    public static void init(Context context) {
        init(context, null, null);
    }

    /**
     * 初始化UIKit，须传入context以及用户信息提供者
     *
     * @param context          上下文
     * @param userInfoProvider 用户信息提供者
     * @param contactProvider  通讯录信息提供者
     */
    public static void init(Context context, UserInfoProvider userInfoProvider, ContactProvider contactProvider) {
        NimUIKit.context = context.getApplicationContext();

        initUserInfoProvider(userInfoProvider);
        initContactProvider(contactProvider);
        initDefalutSessionCustomization();
        initDefalutContactEventListener();

        NimUIKit.imageLoaderKit = new ImageLoaderKit(context);

        // init data cache
        LoginSyncDataStatusObserver.getInstance().registerLoginSyncDataStatus(true);  // 监听登录同步数据完成通知
        DataCacheManager.observeSDKDataChanged(true);
        if (!TextUtils.isEmpty(getAccount())) {
            DataCacheManager.buildDataCache(); // build data cache on auto login
            getImageLoaderKit().buildImageCache(); // build image cache on auto login
        }

        // init tools
        StorageUtil.init(context, null);
        ScreenUtil.init(context);
        StickerManager.getInstance().init();

        // init log
        String path = StorageUtil.getDirectoryByDirType(StorageType.TYPE_LOG);
        LogUtil.init(path, Log.DEBUG);
    }

    // 初始化用户信息提供者
    private static void initUserInfoProvider(UserInfoProvider userInfoProvider) {

        if (userInfoProvider == null) {
            userInfoProvider = new DefaultUserInfoProvider(context);
        }

        NimUIKit.userInfoProvider = userInfoProvider;
    }

    // 初始化联系人信息提供者
    private static void initContactProvider(ContactProvider contactProvider) {

        if (contactProvider == null) {
            contactProvider = new DefaultContactProvider();
        }

        NimUIKit.contactProvider = contactProvider;
    }

    // 初始化会话定制，群、P2P
    private static void initDefalutSessionCustomization() {
        if (commonP2PSessionCustomization == null) {
            commonP2PSessionCustomization = new DefalutP2PSessionCustomization();
        }
        if (commonTeamSessionCustomization == null) {
            commonTeamSessionCustomization = new DefalutTeamSessionCustomization();
        }
    }

    // 初始化联系人点击事件
    private static void initDefalutContactEventListener() {
        if (contactEventListener == null) {
            contactEventListener = new DefalutContactEventListener();
        }
    }

    /**
     * 打开单聊界面，若开发者未设置 {@link NimUIKit#setCommonP2PSessionCustomization(SessionCustomization)},
     * 则定制化信息 SessionCustomization 为{@link DefalutP2PSessionCustomization}
     * <p>
     * 若需要为目标会话提供单独定义的SessionCustomization，请使用{@link NimUIKit#startChatting(Context, String, SessionTypeEnum, SessionCustomization, IMMessage)}
     *
     * @param context 上下文
     * @param account 目标账号
     */
    public static void startP2PSession(Context context, String account) {
        startP2PSession(context, account, null);
    }

    /**
     * 同 {@link NimUIKit#startP2PSession(Context, String)},同时聊天界面打开后，列表跳转至anchor位置
     *
     * @param context 上下文
     * @param account 目标账号
     * @param anchor  跳转到指定消息的位置，不需要跳转填null
     */
    public static void startP2PSession(Context context, String account, IMMessage anchor) {
        NimUIKit.startChatting(context, account, SessionTypeEnum.P2P, commonP2PSessionCustomization, anchor);
    }

    /**
     * 打开群聊界面，若开发者未设置 {@link NimUIKit#setCommonTeamSessionCustomization(SessionCustomization)},
     * 则定制化信息 SessionCustomization 为{@link DefalutTeamSessionCustomization}
     * <p>
     * 若需要为目标会话提供单独定义的SessionCustomization，请使用{@link NimUIKit#startChatting(Context, String, SessionTypeEnum, SessionCustomization, IMMessage)}
     *
     * @param context 上下文
     * @param tid     群id
     */
    public static void startTeamSession(Context context, String tid) {
        startTeamSession(context, tid, null);
    }

    /**
     * 同 {@link NimUIKit#startTeamSession(Context, String)},同时聊天界面打开后，列表跳转至anchor位置
     *
     * @param context 上下文
     * @param tid     群id
     * @param anchor  跳转到指定消息的位置，不需要跳转填null
     */
    public static void startTeamSession(Context context, String tid, IMMessage anchor) {
        NimUIKit.startChatting(context, tid, SessionTypeEnum.Team, commonTeamSessionCustomization, anchor);
    }

    /**
     * 手动登陆，由于手动登陆完成之后，UIKit 需要设置账号、构建缓存等，使用此方法登陆 UIKit 会将这部分逻辑处理好，开发者只需要处理自己的逻辑即可
     *
     * @param loginInfo 登陆账号信息
     * @param callback  登陆结果回调
     */
    public static AbortableFuture<LoginInfo> doLogin(LoginInfo loginInfo, final RequestCallback<LoginInfo> callback) {

        AbortableFuture<LoginInfo> loginRequest = NIMClient.getService(AuthService.class).login(loginInfo);
        loginRequest.setCallback(new RequestCallback<LoginInfo>() {
            @Override
            public void onSuccess(LoginInfo loginInfo) {
                NimUIKit.setAccount(loginInfo.getAccount());
                DataCacheManager.buildDataCacheAsync();
                getImageLoaderKit().buildImageCache();
                callback.onSuccess(loginInfo);
            }

            @Override
            public void onFailed(int code) {
                callback.onFailed(code);
            }

            @Override
            public void onException(Throwable exception) {
                callback.onException(exception);
            }
        });
        return loginRequest;
    }

    /**
     * 释放缓存，一般在注销时调用
     */
    public static void clearCache() {
        DataCacheManager.clearDataCache();
        getImageLoaderKit().clear();
    }

    /**
     * 打开一个聊天窗口，开始聊天
     *
     * @param context       上下文
     * @param id            聊天对象ID（用户帐号account或者群组ID）
     * @param sessionType   会话类型
     * @param customization 定制化信息。针对不同的聊天对象，可提供不同的定制化。
     * @param anchor        跳转到指定消息的位置，不需要跳转填null
     */
    public static void startChatting(Context context, String id, SessionTypeEnum sessionType, SessionCustomization
            customization, IMMessage anchor) {
        if (sessionType == SessionTypeEnum.P2P) {
            P2PMessageActivity.start(context, id, customization, anchor);
        } else if (sessionType == SessionTypeEnum.Team) {
            TeamMessageActivity.start(context, id, customization, null, anchor);
        }
    }

    /**
     * 打开一个聊天窗口（用于从聊天信息中创建群聊时，打开群聊）
     *
     * @param context       上下文
     * @param id            聊天对象ID（用户帐号account或者群组ID）
     * @param sessionType   会话类型
     * @param customization 定制化信息。针对不同的聊天对象，可提供不同的定制化。
     * @param backToClass   返回的指定页面
     * @param anchor        跳转到指定消息的位置，不需要跳转填null
     */
    public static void startChatting(Context context, String id, SessionTypeEnum sessionType, SessionCustomization customization,
                                     Class<? extends Activity> backToClass, IMMessage anchor) {
        if (sessionType == SessionTypeEnum.Team) {
            TeamMessageActivity.start(context, id, customization, backToClass, anchor);
        }
    }

    /**
     * 打开联系人选择器
     *
     * @param context     上下文（Activity）
     * @param option      联系人选择器可选配置项
     * @param requestCode startActivityForResult使用的请求码
     */
    public static void startContactSelect(Context context, ContactSelectActivity.Option option, int requestCode) {
        ContactSelectActivity.startActivityForResult(context, option, requestCode);
    }

    /**
     * 打开讨论组或高级群资料页
     *
     * @param context 上下文
     * @param teamId  群id
     */
    public static void startTeamInfo(Context context, String teamId) {
        Team team = TeamDataCache.getInstance().getTeamById(teamId);
        if (team == null) {
            return;
        }
        if (team.getType() == TeamTypeEnum.Advanced) {
            AdvancedTeamInfoActivity.start(context, teamId); // 启动固定群资料页
        } else if (team.getType() == TeamTypeEnum.Normal) {
            NormalTeamInfoActivity.start(context, teamId); // 启动讨论组资料页
        }

    }

    public static Context getContext() {
        return context;
    }

    public static String getAccount() {
        return account;
    }


    public static UserInfoProvider getUserInfoProvider() {
        return userInfoProvider;
    }

    public static ContactProvider getContactProvider() {
        return contactProvider;
    }

    public static LocationProvider getLocationProvider() {
        return locationProvider;
    }

    public static ImageLoaderKit getImageLoaderKit() {
        return imageLoaderKit;
    }

    /**
     * 设置位置信息提供者
     *
     * @param locationProvider 位置信息提供者
     */
    public static void setLocationProvider(LocationProvider locationProvider) {
        NimUIKit.locationProvider = locationProvider;
    }

    /**
     * 设置单聊界面定制 SessionCustomization
     *
     * @param commonP2PSessionCustomization 聊天界面定制化
     */
    public static void setCommonP2PSessionCustomization(SessionCustomization commonP2PSessionCustomization) {
        NimUIKit.commonP2PSessionCustomization = commonP2PSessionCustomization;
    }

    /**
     * 设置群聊界面定制 SessionCustomization
     *
     * @param commonTeamSessionCustomization 聊天界面定制化
     */
    public static void setCommonTeamSessionCustomization(SessionCustomization commonTeamSessionCustomization) {
        NimUIKit.commonTeamSessionCustomization = commonTeamSessionCustomization;
    }

    /**
     * 根据消息附件类型注册对应的消息项展示ViewHolder
     *
     * @param attach     附件类型
     * @param viewHolder 消息ViewHolder
     */
    public static void registerMsgItemViewHolder(Class<? extends MsgAttachment> attach, Class<? extends MsgViewHolderBase> viewHolder) {
        MsgViewHolderFactory.register(attach, viewHolder);
    }

    /**
     * 注册Tip类型消息项展示ViewHolder
     *
     * @param viewHolder Tip消息ViewHolder
     */
    public static void registerTipMsgViewHolder(Class<? extends MsgViewHolderBase> viewHolder) {
        MsgViewHolderFactory.registerTipMsgViewHolder(viewHolder);
    }

    /**
     * 设置当前登录用户的帐号
     *
     * @param account 帐号
     */
    public static void setAccount(String account) {
        NimUIKit.account = account;
    }

    /**
     * 获取聊天界面事件监听器
     *
     * @return
     */
    public static SessionEventListener getSessionListener() {
        return sessionListener;
    }

    /**
     * 设置聊天界面的事件监听器
     *
     * @param sessionListener
     */
    public static void setSessionListener(SessionEventListener sessionListener) {
        NimUIKit.sessionListener = sessionListener;
    }

    /**
     * 获取通讯录列表的事件监听器
     *
     * @return
     */
    public static ContactEventListener getContactEventListener() {
        return contactEventListener;
    }

    /**
     * 设置通讯录列表的事件监听器
     *
     * @param contactEventListener
     */
    public static void setContactEventListener(ContactEventListener contactEventListener) {
        NimUIKit.contactEventListener = contactEventListener;
    }

    /**
     * 当用户资料发生改动时，请调用此接口，通知更新UI
     *
     * @param accounts 有用户信息改动的帐号列表
     */
    public static void notifyUserInfoChanged(List<String> accounts) {
        UserInfoHelper.notifyChanged(accounts);
    }

    /**
     * 设置转发消息过滤的监听器
     *
     * @param msgForwardFilter
     */
    public static void setMsgForwardFilter(MsgForwardFilter msgForwardFilter) {
        NimUIKit.msgForwardFilter = msgForwardFilter;
    }

    /**
     * 获取转发消息过滤的监听器
     *
     * @return
     */
    public static MsgForwardFilter getMsgForwardFilter() {
        return msgForwardFilter;
    }

    /**
     * 设置消息撤回的监听器
     *
     * @param msgRevokeFilter
     */
    public static void setMsgRevokeFilter(MsgRevokeFilter msgRevokeFilter) {
        NimUIKit.msgRevokeFilter = msgRevokeFilter;
    }

    /**
     * 获取消息撤回的监听器
     *
     * @return
     */
    public static MsgRevokeFilter getMsgRevokeFilter() {
        return msgRevokeFilter;
    }

    /**
     * @return
     */
    public static CustomPushContentProvider getCustomPushContentProvider() {
        return customPushContentProvider;
    }

    /**
     * 注册自定义推送文案
     *
     * @return
     */
    public static void CustomPushContentProvider(CustomPushContentProvider mixPushCustomConfig) {
        NimUIKit.customPushContentProvider = mixPushCustomConfig;
    }

    public static OnlineStateContentProvider getOnlineStateContentProvider() {
        return onlineStateContentProvider;
    }

    public static void setOnlineStateContentProvider(OnlineStateContentProvider onlineStateContentProvider) {
        NimUIKit.onlineStateContentProvider = onlineStateContentProvider;
    }

    public static void addOnlineStateChangeListeners(OnlineStateChangeListener onlineStateChangeListener) {
        if (onlineStateChangeListeners == null) {
            onlineStateChangeListeners = new LinkedList<>();
        }
        onlineStateChangeListeners.add(onlineStateChangeListener);
    }

    public static void removeOnlineStateChangeListeners(OnlineStateChangeListener onlineStateChangeListener) {
        if (onlineStateChangeListeners == null) {
            return;
        }
        onlineStateChangeListeners.remove(onlineStateChangeListener);
    }

    public static void notifyOnlineStateChange(Set<String> accounts) {
        if (onlineStateChangeListeners != null) {
            for (OnlineStateChangeListener listener : onlineStateChangeListeners) {
                listener.onlineStateChange(accounts);
            }
        }
    }

    /**
     * 设置了 onlineStateContentProvider 则表示UIKit需要展示在线状态
     *
     * @return
     */
    public static boolean enableOnlineState() {
        return onlineStateContentProvider != null;
    }
}
