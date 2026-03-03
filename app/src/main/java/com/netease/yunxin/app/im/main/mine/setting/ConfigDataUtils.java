package com.netease.yunxin.app.im.main.mine.setting;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Base64;

/**
 * OpenClaw配置数据管理工具类
 * 提供AppKey、Account、Token、OpenClaw Account等配置数据的安全存储和读取功能
 */
public class ConfigDataUtils {
    
    private static final String CONFIG_PREFS = "openclaw_config";
    private static final String KEY_APP_KEY = "app_key";
    private static final String KEY_ACCOUNT = "account";
    private static final String KEY_TOKEN = "token";  // Base64编码存储
    private static final String KEY_OPENCLAW_ACCOUNT = "openclaw_account";
    private static final String KEY_CONFIG_SAVED = "config_saved";
    
    /**
     * 获取SharedPreferences实例
     */
    private static SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences(CONFIG_PREFS, Context.MODE_PRIVATE);
    }
    
    /**
     * 保存AppKey配置
     * @param context 上下文
     * @param appKey 应用AppKey
     */
    public static void saveAppKey(Context context, String appKey) {
        SharedPreferences.Editor editor = getPreferences(context).edit();
        editor.putString(KEY_APP_KEY, appKey);
        editor.apply();
    }
    
    /**
     * 获取保存的AppKey
     * @param context 上下文
     * @return AppKey，如果未配置则返回null
     */
    public static String getAppKey(Context context) {
        return getPreferences(context).getString(KEY_APP_KEY, null);
    }
    
    /**
     * 保存账号信息
     * @param context 上下文
     * @param account 用户账号
     */
    public static void saveAccount(Context context, String account) {
        SharedPreferences.Editor editor = getPreferences(context).edit();
        editor.putString(KEY_ACCOUNT, account);
        editor.apply();
    }
    
    /**
     * 获取保存的账号
     * @param context 上下文
     * @return 账号，如果未配置则返回null
     */
    public static String getAccount(Context context) {
        return getPreferences(context).getString(KEY_ACCOUNT, null);
    }
    
    /**
     * 保存Token（会进行Base64编码）
     * @param context 上下文
     * @param token 用户Token
     */
    public static void saveToken(Context context, String token) {
        SharedPreferences.Editor editor = getPreferences(context).edit();
        if (!TextUtils.isEmpty(token)) {
            String encodedToken = Base64.encodeToString(token.getBytes(), Base64.DEFAULT);
            editor.putString(KEY_TOKEN, encodedToken);
        } else {
            editor.putString(KEY_TOKEN, null);
        }
        editor.apply();
    }
    
    /**
     * 获取保存的Token（会自动进行Base64解码）
     * @param context 上下文
     * @return Token，如果未配置则返回null
     */
    public static String getToken(Context context) {
        String encodedToken = getPreferences(context).getString(KEY_TOKEN, null);
        if (!TextUtils.isEmpty(encodedToken)) {
            try {
                return new String(Base64.decode(encodedToken, Base64.DEFAULT));
            } catch (Exception e) {
                // 解码失败，返回null
                return null;
            }
        }
        return null;
    }
    
    /**
     * 保存OpenClaw账号
     * @param context 上下文
     * @param openClawAccount OpenClaw智能体账号
     */
    public static void saveOpenClawAccount(Context context, String openClawAccount) {
        SharedPreferences.Editor editor = getPreferences(context).edit();
        editor.putString(KEY_OPENCLAW_ACCOUNT, openClawAccount);
        editor.apply();
    }
    
    /**
     * 获取保存的OpenClaw账号
     * @param context 上下文
     * @return OpenClaw账号，如果未配置则返回null
     */
    public static String getOpenClawAccount(Context context) {
        return getPreferences(context).getString(KEY_OPENCLAW_ACCOUNT, null);
    }
    
    /**
     * 保存完整的配置信息
     * @param context 上下文
     * @param appKey AppKey
     * @param account 账号
     * @param token Token
     * @param openClawAccount OpenClaw账号
     */
    public static void saveAllConfig(Context context, String appKey, String account, String token, String openClawAccount) {
        SharedPreferences.Editor editor = getPreferences(context).edit();
        editor.putString(KEY_APP_KEY, appKey);
        editor.putString(KEY_ACCOUNT, account);
        
        // Token进行Base64编码
        if (!TextUtils.isEmpty(token)) {
            String encodedToken = Base64.encodeToString(token.getBytes(), Base64.DEFAULT);
            editor.putString(KEY_TOKEN, encodedToken);
        } else {
            editor.putString(KEY_TOKEN, null);
        }
        
        editor.putString(KEY_OPENCLAW_ACCOUNT, openClawAccount);
        editor.putBoolean(KEY_CONFIG_SAVED, true);
        editor.apply();
    }
    
    /**
     * 检查是否已保存配置
     * @param context 上下文
     * @return true表示已保存配置，false表示未保存
     */
    public static boolean isConfigSaved(Context context) {
        return getPreferences(context).getBoolean(KEY_CONFIG_SAVED, false);
    }
    
    /**
     * 重置所有配置到默认状态
     * @param context 上下文
     */
    public static void resetConfig(Context context) {
        SharedPreferences.Editor editor = getPreferences(context).edit();
        editor.clear();
        editor.apply();
    }
    
    /**
     * 检查基本登录配置是否完整（AppKey + Account + Token）
     * @param context 上下文
     * @return true表示配置完整，false表示缺少必要配置
     */
    public static boolean hasValidLoginConfig(Context context) {
        String appKey = getAppKey(context);
        String account = getAccount(context);
        String token = getToken(context);
        
        return !TextUtils.isEmpty(appKey) && !TextUtils.isEmpty(account) && !TextUtils.isEmpty(token);
    }
    
    /**
     * 验证AppKey格式（简单验证）
     * @param appKey 待验证的AppKey
     * @return true表示格式正确，false表示格式错误
     */
    public static boolean isValidAppKey(String appKey) {
        // AppKey通常为32位长度的字符串
        return !TextUtils.isEmpty(appKey) && appKey.length() >= 20;
    }
}