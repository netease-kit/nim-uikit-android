package com.netease.nim.uikit.common.util.sys;

import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.netease.nim.uikit.common.util.log.LogUtil;

public class NetworkUtil {

    public static final String TAG = "NetworkUtil";

    public static final byte CURRENT_NETWORK_TYPE_NONE = 0;

    /*
     * 根据APN区分网络类型
     */
    public static final byte CURRENT_NETWORK_TYPE_WIFI = 1;// wifi

    public static final byte CURRENT_NETWORK_TYPE_CTNET = 2;// ctnet

    public static final byte CURRENT_NETWORK_TYPE_CTWAP = 3;// ctwap

    public static final byte CURRENT_NETWORK_TYPE_CMWAP = 4;// cmwap

    public static final byte CURRENT_NETWORK_TYPE_UNIWAP = 5;// uniwap,3gwap

    public static final byte CURRENT_NETWORK_TYPE_CMNET = 6;// cmnet

    public static final byte CURRENT_NETWORK_TYPE_UNIET = 7;// uninet,3gnet

    /**
     * 根据运营商区分网络类型
     */
    public static final byte CURRENT_NETWORK_TYPE_CTC = 10;// ctwap,ctnet

    public static final byte CURRENT_NETWORK_TYPE_CUC = 11;// uniwap,3gwap,uninet,3gnet

    public static final byte CURRENT_NETWORK_TYPE_CM = 12;// cmwap,cmnet

    /**
     * apn值
     */
    private static final String CONNECT_TYPE_WIFI = "wifi";

    private static final String CONNECT_TYPE_CTNET = "ctnet";

    private static final String CONNECT_TYPE_CTWAP = "ctwap";

    private static final String CONNECT_TYPE_CMNET = "cmnet";

    private static final String CONNECT_TYPE_CMWAP = "cmwap";

    private static final String CONNECT_TYPE_UNIWAP = "uniwap";

    private static final String CONNECT_TYPE_UNINET = "uninet";

    private static final String CONNECT_TYPE_UNI3GWAP = "3gwap";

    private static final String CONNECT_TYPE_UNI3GNET = "3gnet";

    private static final Uri PREFERRED_APN_URI = Uri.parse("content://telephony/carriers/preferapn");

    public static byte curNetworkType = CURRENT_NETWORK_TYPE_NONE;

    /*
     *
     * 获取网络类型
     *
     */
    public static int getNetType(Context context) {
        NetworkInfo networkInfo = getActiveNetworkInfo(context);
        if (networkInfo == null) {
            return -1;
        } else
            return networkInfo.getType();
    }


    /**
     * 判断当前网络类型。WIFI,NET,WAP
     *
     * @param context
     * @return
     */
    public static byte getCurrentNetType(Context context) {
        NetworkInfo networkInfo = getActiveNetworkInfo(context);
        byte type = CURRENT_NETWORK_TYPE_NONE;
        if (networkInfo != null) {
            // String typeName = networkInfo.getTypeName();
            // XT800
            String typeName = networkInfo.getExtraInfo();
            if (TextUtils.isEmpty(typeName)) {
                typeName = networkInfo.getTypeName();
            }
            if (!TextUtils.isEmpty(typeName)) {
                String temp = typeName.toLowerCase();
                if (temp.indexOf(CONNECT_TYPE_WIFI) > -1) {// wifi
                    type = CURRENT_NETWORK_TYPE_WIFI;
                } else if (temp.indexOf(CONNECT_TYPE_CTNET) > -1) {// ctnet
                    type = CURRENT_NETWORK_TYPE_CTNET;
                } else if (temp.indexOf(CONNECT_TYPE_CTWAP) > -1) {// ctwap
                    type = CURRENT_NETWORK_TYPE_CTWAP;
                } else if (temp.indexOf(CONNECT_TYPE_CMNET) > -1) {// cmnet
                    type = CURRENT_NETWORK_TYPE_CMNET;
                } else if (temp.indexOf(CONNECT_TYPE_CMWAP) > -1) {// cmwap
                    type = CURRENT_NETWORK_TYPE_CMWAP;
                } else if (temp.indexOf(CONNECT_TYPE_UNIWAP) > -1) {// uniwap
                    type = CURRENT_NETWORK_TYPE_UNIWAP;
                } else if (temp.indexOf(CONNECT_TYPE_UNI3GWAP) > -1) {// 3gwap
                    type = CURRENT_NETWORK_TYPE_UNIWAP;
                } else if (temp.indexOf(CONNECT_TYPE_UNINET) > -1) {// uninet
                    type = CURRENT_NETWORK_TYPE_UNIET;
                } else if (temp.indexOf(CONNECT_TYPE_UNI3GNET) > -1) {// 3gnet
                    type = CURRENT_NETWORK_TYPE_UNIET;
                }
            }
        }

        if (type == CURRENT_NETWORK_TYPE_NONE) {
            String apnType = getApnType(context);
            if (apnType != null && apnType.equals(CONNECT_TYPE_CTNET)) {// ctnet
                type = CURRENT_NETWORK_TYPE_CTNET;
            } else if (apnType != null && apnType.equals(CONNECT_TYPE_CTWAP)) {// ctwap
                type = CURRENT_NETWORK_TYPE_CTWAP;
            } else if (apnType != null && apnType.equals(CONNECT_TYPE_CMWAP)) {// cmwap
                type = CURRENT_NETWORK_TYPE_CMWAP;
            } else if (apnType != null && apnType.equals(CONNECT_TYPE_CMNET)) {// cmnet
                type = CURRENT_NETWORK_TYPE_CMNET;
            } else if (apnType != null && apnType.equals(CONNECT_TYPE_UNIWAP)) {// uniwap
                type = CURRENT_NETWORK_TYPE_UNIWAP;
            } else if (apnType != null && apnType.equals(CONNECT_TYPE_UNI3GWAP)) {// 3gwap
                type = CURRENT_NETWORK_TYPE_UNIWAP;
            } else if (apnType != null && apnType.equals(CONNECT_TYPE_UNINET)) {// uninet
                type = CURRENT_NETWORK_TYPE_UNIET;
            } else if (apnType != null && apnType.equals(CONNECT_TYPE_UNI3GNET)) {// 3gnet
                type = CURRENT_NETWORK_TYPE_UNIET;
            }
        }
        curNetworkType = type;

        return type;
    }

    /**
     * 判断APNTYPE
     *
     * @param context
     * @return
     */
    /**
     * @deprecated 4.0
     * doc:
     * Since the DB may contain corp passwords, we should secure it. Using the same permission as writing to the DB as the read is potentially as damaging as a write
     */
    public static String getApnType(Context context) {

        String apntype = "nomatch";
        Cursor c = context.getContentResolver().query(PREFERRED_APN_URI, null, null, null, null);
        if (c != null) {
            if (c.moveToFirst()) {
                String user = c.getString(c.getColumnIndex("user"));
                if (user != null && user.startsWith(CONNECT_TYPE_CTNET)) {
                    apntype = CONNECT_TYPE_CTNET;
                } else if (user != null && user.startsWith(CONNECT_TYPE_CTWAP)) {
                    apntype = CONNECT_TYPE_CTWAP;
                } else if (user != null && user.startsWith(CONNECT_TYPE_CMWAP)) {
                    apntype = CONNECT_TYPE_CMWAP;
                } else if (user != null && user.startsWith(CONNECT_TYPE_CMNET)) {
                    apntype = CONNECT_TYPE_CMNET;
                } else if (user != null && user.startsWith(CONNECT_TYPE_UNIWAP)) {
                    apntype = CONNECT_TYPE_UNIWAP;
                } else if (user != null && user.startsWith(CONNECT_TYPE_UNINET)) {
                    apntype = CONNECT_TYPE_UNINET;
                } else if (user != null && user.startsWith(CONNECT_TYPE_UNI3GWAP)) {
                    apntype = CONNECT_TYPE_UNI3GWAP;
                } else if (user != null && user.startsWith(CONNECT_TYPE_UNI3GNET)) {
                    apntype = CONNECT_TYPE_UNI3GNET;
                }
            }
            c.close();
            c = null;
        }

        return apntype;
    }

    /**
     * 判断是否有网络可用
     *
     * @param context
     * @return
     */
    public static boolean isNetAvailable(Context context) {
        NetworkInfo networkInfo = getActiveNetworkInfo(context);
        if (networkInfo != null) {
            return networkInfo.isAvailable();
        } else {
            return false;
        }
    }

    /**
     * 此判断不可靠
     *
     * @param context
     * @return
     */
    public static boolean isNetworkConnected(Context context) {
        NetworkInfo networkInfo = getActiveNetworkInfo(context);
        if (networkInfo != null) {
            boolean a = networkInfo.isConnected();
            return a;
        } else {
            return false;
        }
    }

    /**
     * 获取可用的网络信息
     *
     * @param context
     * @return
     */
    private static NetworkInfo getActiveNetworkInfo(Context context) {
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            return cm.getActiveNetworkInfo();
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean isWifiOr3G(Context context) {
        if (isWifi(context)) {
            return true;
        } else {
            return is3G(context);
        }
    }

    public static boolean is2G(Context context) {
        return !isWifiOr3G(context);
    }

    public static boolean is3G(Context context) {
        int type = getNetworkClass(context);
        if (type == NETWORK_CLASS_3_G || type == NETWORK_CLASS_4_G) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 当前网络是否是wifi网络
     *
     * @param context
     * @return
     */
    public static boolean isWifi(Context context) {
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo ni = cm.getActiveNetworkInfo();
            if (ni != null) {
                if (ni.getType() == ConnectivityManager.TYPE_WIFI) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean getNetworkConnectionStatus(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (manager == null) {
            return false;
        }

        NetworkInfo info = manager.getActiveNetworkInfo();
        if (info == null) {
            return false;
        }

        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (tm == null) {
            return false;
        }

        if ((tm.getDataState() == TelephonyManager.DATA_CONNECTED || tm.getDataState() == TelephonyManager.DATA_ACTIVITY_NONE)
                && info.isAvailable()) {
            return true;
        } else {
            return false;
        }
    }

    public static String getNetworkProxyInfo(Context context) {
        String proxyHost = android.net.Proxy.getDefaultHost();
        int proxyPort = android.net.Proxy.getDefaultPort();
        String szport = String.valueOf(proxyPort);
        String proxyInfo = null;

        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (manager == null) {
            return null;
        } else {
            NetworkInfo info = manager.getActiveNetworkInfo();
            if (info != null) {
                String typeName = info.getTypeName().toLowerCase();
                if (typeName != null && typeName.equals("wifi")) {
                    return null;
                }
            } else {
                return null;
            }
        }

        if (proxyHost != null && (0 < proxyPort && proxyPort < 65535)) {
            proxyInfo = proxyHost + ":" + szport;
            return proxyInfo;
        } else {
            return null;
        }
    }

    public static String getNetworkProxyUrl(Context context) {
        if (isWifi(context)) {
            return null;
        }

        String proxyHost = android.net.Proxy.getDefaultHost();
        LogUtil.e(TAG, "proxyHost:" + proxyHost);
        return proxyHost;
    }

    public static String getNetworkProxyUrl() {
        /**
         * 当网络为wifi时,直接返回空代理: 当ctwap,cmwap,uniwap,3gwap开启时同时开启wifi网络
         * ,通过下面的getDefaultHost接口将得到对应wap网络代理ip ,这是错误的,所以在此判断当前网络是否为wifi
         */
        if (curNetworkType == CURRENT_NETWORK_TYPE_WIFI) {
            return null;
        }

        String proxyHost = android.net.Proxy.getDefaultHost();
        LogUtil.e(TAG, "proxyHost:" + proxyHost);
        return proxyHost;
    }

    public static int getNetworkProxyPort() {
        int proxyPort = android.net.Proxy.getDefaultPort();
        return proxyPort;
    }

    public static boolean isCtwap(Context context) {
        if (getApnType(context).equals(CONNECT_TYPE_CTWAP)) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isUniwap(Context context) {
        if (getApnType(context).equals(CONNECT_TYPE_UNIWAP)) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isCmwap(Context context) {
        if (getApnType(context).equals(CONNECT_TYPE_CMWAP)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 判断是否是电信网络(ctwap,ctnet)
     *
     * @return
     */
    public static boolean isCtcNetwork(Context context) {
        byte type = getCurrentNetType(context);

        return isCtcNetwork(type);
    }

    public static boolean isCtcNetwork(String apnName) {
        if (apnName == null) {
            return false;
        }

        if (apnName.equals(CONNECT_TYPE_CTWAP) || apnName.equals(CONNECT_TYPE_CTNET)) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isCtcNetwork(byte type) {
        if (type == CURRENT_NETWORK_TYPE_CTWAP || type == CURRENT_NETWORK_TYPE_CTNET) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 判断是否是联通网络(uniwap,uninet,3gwap,3gnet)
     *
     * @return
     */
    public static boolean isCucNetwork(Context context) {
        byte type = getCurrentNetType(context);

        return isCucNetwork(type);
    }

    public static boolean isCucNetwork(String apnName) {
        if (apnName == null) {
            return false;
        }

        if (apnName.equals(CONNECT_TYPE_UNIWAP) || apnName.equals(CONNECT_TYPE_UNINET)
                || apnName.equals(CONNECT_TYPE_UNI3GWAP) || apnName.equals(CONNECT_TYPE_UNI3GNET)) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isCucNetwork(byte type) {
        if (type == CURRENT_NETWORK_TYPE_UNIWAP || type == CURRENT_NETWORK_TYPE_UNIET) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 判断是否是移动网络(cmwap,cmnet)
     *
     * @return
     */
    public static boolean isCmbNetwork(Context context) {
        byte type = getCurrentNetType(context);

        return isCmbNetwork(type);
    }

    public static boolean isCmbNetwork(String apnName) {
        if (apnName == null) {
            return false;
        }

        if (apnName.equals(CONNECT_TYPE_CMWAP) || apnName.equals(CONNECT_TYPE_CMNET)) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isCmbNetwork(byte type) {
        if (type == CURRENT_NETWORK_TYPE_CMWAP || type == CURRENT_NETWORK_TYPE_CMNET) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 获取网络运营商类型(中国移动,中国联通,中国电信,wifi)
     *
     * @param context
     * @return
     */
    public static byte getNetworkOperators(Context context) {
        if (isWifi(context)) {
            return CURRENT_NETWORK_TYPE_WIFI;
        } else if (isCtcNetwork(context)) {
            return CURRENT_NETWORK_TYPE_CTC;
        } else if (isCmbNetwork(context)) {
            return CURRENT_NETWORK_TYPE_CM;
        } else if (isCucNetwork(context)) {
            return CURRENT_NETWORK_TYPE_CUC;
        } else {
            return CURRENT_NETWORK_TYPE_NONE;
        }
    }

    public static byte getNetworkOperators(byte type) {
        if (type == CURRENT_NETWORK_TYPE_NONE) {
            return CURRENT_NETWORK_TYPE_NONE;
        } else if (type == CURRENT_NETWORK_TYPE_WIFI) {
            return CURRENT_NETWORK_TYPE_WIFI;
        } else if (type == CURRENT_NETWORK_TYPE_CTNET || type == CURRENT_NETWORK_TYPE_CTWAP) {
            return CURRENT_NETWORK_TYPE_CTC;
        } else if (type == CURRENT_NETWORK_TYPE_CMWAP || type == CURRENT_NETWORK_TYPE_CMNET) {
            return CURRENT_NETWORK_TYPE_CM;
        } else if (type == CURRENT_NETWORK_TYPE_UNIWAP || type == CURRENT_NETWORK_TYPE_UNIET) {
            return CURRENT_NETWORK_TYPE_CUC;
        } else {
            return CURRENT_NETWORK_TYPE_NONE;
        }
    }

    /**
     * 是否需要设置代理(网络请求,一般用于wap网络,但有些机型设置代理会导致系统异常)
     *
     * @return
     */
    public static boolean isNeedSetProxyForNetRequest() { // #00044 +
        if (Build.MODEL.equals("SCH-N719") || Build.MODEL.equals("SCH-I939D")) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * get mac address of wifi if wifi is active
     */

    public static String getActiveMacAddress(Context context) {
        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        WifiInfo info = wifi.getConnectionInfo();

        if (info != null) {
            return info.getMacAddress();
        }

        return "";
    }

    public static String getNetworkInfo(Context context) {
        String info = "";
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo activeNetInfo = connectivity.getActiveNetworkInfo();
            if (activeNetInfo != null) {
                if (activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                    info = activeNetInfo.getTypeName();
                } else {
                    StringBuilder sb = new StringBuilder();
                    TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                    sb.append(activeNetInfo.getTypeName());
                    sb.append(" [");
                    if (tm != null) {
                        // Result may be unreliable on CDMA networks
                        sb.append(tm.getNetworkOperatorName());
                        sb.append("#");
                    }
                    sb.append(activeNetInfo.getSubtypeName());
                    sb.append("]");
                    info = sb.toString();
                }
            }
        }
        return info;
    }

    public enum NetworkSpeedMode {
        LOW, NORMAL, HIGH, UNKNOWN
    }

    /**
     * 网络类型
     */
    public static final int NETWORK_CLASS_UNKNOWN = 0;

    public static final int NETWORK_CLASS_2_G = 1;

    public static final int NETWORK_CLASS_3_G = 2;

    public static final int NETWORK_CLASS_4_G = 3;

    public static final int NETWORK_CLASS_WIFI = 10;

    /**
     * 仅判断Mobile网络的慢速.蓝牙等其他网络不做判断.
     *
     * @param context
     * @return
     */
    public static NetworkSpeedMode getNetworkSpeedModeInMobile(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if (networkInfo != null) {
                if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                    switch (networkInfo.getSubtype()) {
                        case TelephonyManager.NETWORK_TYPE_IDEN: // ~25 kbps
                            return NetworkSpeedMode.LOW;
                        case TelephonyManager.NETWORK_TYPE_CDMA: // ~ 14-64 kbps
                            return NetworkSpeedMode.LOW;
                        case TelephonyManager.NETWORK_TYPE_1xRTT: // ~ 50-100 kbps
                            return NetworkSpeedMode.LOW;
                        case TelephonyManager.NETWORK_TYPE_EDGE: // ~ 50-100 kbps
                            return NetworkSpeedMode.LOW;
                        case TelephonyManager.NETWORK_TYPE_GPRS: // ~ 100 kbps
                            return NetworkSpeedMode.LOW;

                        case TelephonyManager.NETWORK_TYPE_EVDO_0: // ~ 400-1000
                            // kbps
                            return NetworkSpeedMode.NORMAL;
                        case TelephonyManager.NETWORK_TYPE_EVDO_A: // ~ 600-1400
                            // kbps
                            return NetworkSpeedMode.NORMAL;
                        case TelephonyManager.NETWORK_TYPE_HSPA: // ~ 700-1700 kbps
                            return NetworkSpeedMode.NORMAL;
                        case TelephonyManager.NETWORK_TYPE_UMTS: // ~ 400-7000 kbps
                            return NetworkSpeedMode.NORMAL;
                        case 14: // TelephonyManager.NETWORK_TYPE_EHRPD: // ~ 1-2
                            // Mbps
                            return NetworkSpeedMode.NORMAL;
                        case 12: // TelephonyManager.NETWORK_TYPE_EVDO_B: // ~ 5
                            // Mbps
                            return NetworkSpeedMode.NORMAL;

                        case TelephonyManager.NETWORK_TYPE_HSDPA: // ~ 2-14 Mbps
                            return NetworkSpeedMode.HIGH;
                        case TelephonyManager.NETWORK_TYPE_HSUPA: // ~ 1-23 Mbps
                            return NetworkSpeedMode.HIGH;
                        case 15: // TelephonyManager.NETWORK_TYPE_HSPAP: // ~ 10-20
                            // Mbps
                            return NetworkSpeedMode.HIGH;
                        case 13: // TelephonyManager.NETWORK_TYPE_LTE: // ~ 10+ Mbps
                            return NetworkSpeedMode.HIGH;

                        case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                            return NetworkSpeedMode.NORMAL;
                        default:
                            break;
                    }
                }
            }
        }
        return NetworkSpeedMode.UNKNOWN;
    }

    /**
     * 获取在Mobile网络下的网络类型. 2G,3G,4G
     *
     * @param context
     * @return
     */
    public static int getNetworkClass(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if (networkInfo != null) {
                if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                    switch (networkInfo.getSubtype()) {
                        case TelephonyManager.NETWORK_TYPE_GPRS:
                        case TelephonyManager.NETWORK_TYPE_EDGE:
                        case TelephonyManager.NETWORK_TYPE_CDMA:
                        case TelephonyManager.NETWORK_TYPE_1xRTT:
                        case TelephonyManager.NETWORK_TYPE_IDEN:
                            return NETWORK_CLASS_2_G;
                        case TelephonyManager.NETWORK_TYPE_UMTS:
                        case TelephonyManager.NETWORK_TYPE_EVDO_0:
                        case TelephonyManager.NETWORK_TYPE_EVDO_A:
                        case TelephonyManager.NETWORK_TYPE_HSDPA:
                        case TelephonyManager.NETWORK_TYPE_HSUPA:
                        case TelephonyManager.NETWORK_TYPE_HSPA:
                        case 12: // TelephonyManager.NETWORK_TYPE_EVDO_B:
                        case 14: // TelephonyManager.NETWORK_TYPE_EHRPD:
                        case 15: // TelephonyManager.NETWORK_TYPE_HSPAP:
                            return NETWORK_CLASS_3_G;
                        case 13: // TelephonyManager.NETWORK_TYPE_LTE:
                            return NETWORK_CLASS_4_G;
                        default:
                            return NETWORK_CLASS_UNKNOWN;
                    }
                } else if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                    return NETWORK_CLASS_WIFI;
                }
            }
        }
        return NETWORK_CLASS_UNKNOWN;
    }

    public static String getNetworkTypeName(Context context) {
        String networkName = "UNKNOWN";
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if (networkInfo != null) {
                networkName = getNetworkTypeName(networkInfo.getType());
                if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                    networkName += "#" + getNetworkTypeNameInMobile(networkInfo.getSubtype());
                }
            }
        }
        return networkName;
    }

    private static String getNetworkTypeNameInMobile(int type) {
        switch (type) {
            case TelephonyManager.NETWORK_TYPE_GPRS:
                return "GPRS";
            case TelephonyManager.NETWORK_TYPE_EDGE:
                return "EDGE";
            case TelephonyManager.NETWORK_TYPE_UMTS:
                return "UMTS";
            case TelephonyManager.NETWORK_TYPE_HSDPA:
                return "HSDPA";
            case TelephonyManager.NETWORK_TYPE_HSUPA:
                return "HSUPA";
            case TelephonyManager.NETWORK_TYPE_HSPA:
                return "HSPA";
            case TelephonyManager.NETWORK_TYPE_CDMA:
                return "CDMA";
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
                return "CDMA - EvDo rev. 0";
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
                return "CDMA - EvDo rev. A";
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
                return "CDMA - EvDo rev. B";
            case TelephonyManager.NETWORK_TYPE_1xRTT:
                return "CDMA - 1xRTT";
            case TelephonyManager.NETWORK_TYPE_LTE:
                return "LTE";
            case TelephonyManager.NETWORK_TYPE_EHRPD:
                return "CDMA - eHRPD";
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return "iDEN";
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                return "HSPA+";
            default:
                return "UNKNOWN";
        }
    }

    private static String getNetworkTypeName(int type) {
        switch (type) {
            case ConnectivityManager.TYPE_MOBILE:
                return "MOBILE";
            case ConnectivityManager.TYPE_WIFI:
                return "WIFI";
            case ConnectivityManager.TYPE_MOBILE_MMS:
                return "MOBILE_MMS";
            case ConnectivityManager.TYPE_MOBILE_SUPL:
                return "MOBILE_SUPL";
            case ConnectivityManager.TYPE_MOBILE_DUN:
                return "MOBILE_DUN";
            case ConnectivityManager.TYPE_MOBILE_HIPRI:
                return "MOBILE_HIPRI";
            case ConnectivityManager.TYPE_WIMAX:
                return "WIMAX";
            case ConnectivityManager.TYPE_BLUETOOTH:
                return "BLUETOOTH";
            case ConnectivityManager.TYPE_DUMMY:
                return "DUMMY";
            case ConnectivityManager.TYPE_ETHERNET:
                return "ETHERNET";
            case 10: // ConnectivityManager.TYPE_MOBILE_FOTA:
                return "MOBILE_FOTA";
            case 11: // ConnectivityManager.TYPE_MOBILE_IMS:
                return "MOBILE_IMS";
            case 12: // ConnectivityManager.TYPE_MOBILE_CBS:
                return "MOBILE_CBS";
            case 13: // ConnectivityManager.TYPE_WIFI_P2P:
                return "WIFI_P2P";
            default:
                return Integer.toString(type);
        }
    }

    //中国电信
    public static final int ISP_CTCC = 0;
    //中国联通
    public static final int ISP_CUCC = 1;
    //中国移动
    public static final int ISP_CMCC = 2;
    //中国铁通
    public static final int ISP_CTT = 3;
    //其他
    public static final int ISP_OTHERS = -1;

    public static String getSimOperator(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (tm != null) {
            return tm.getSimOperator();
        }
        return null;
    }

    public static String getNetworkOperator(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (tm != null) {
            return tm.getNetworkOperator();
        }
        return null;
    }


    public interface LinkNetWorkType {
        public static final int UNKNOWN = 0;
        public static final int WIFI = 1;
        public static final int WWAN = 2;
        public static final int _2G = 3;
        public static final int _3G = 4;
        public static final int _4G = 5;
    }

    public static int getNetworkTypeForLink(Context context) {
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo ni = cm.getActiveNetworkInfo();
            if (ni != null) {
                if (ni.getType() == ConnectivityManager.TYPE_WIFI) {
                    return LinkNetWorkType.WIFI;
                } else {
                    if (ni.getType() == ConnectivityManager.TYPE_MOBILE) {
                        switch (ni.getSubtype()) {
                            case TelephonyManager.NETWORK_TYPE_GPRS:
                            case TelephonyManager.NETWORK_TYPE_EDGE:
                            case TelephonyManager.NETWORK_TYPE_CDMA:
                            case TelephonyManager.NETWORK_TYPE_1xRTT:
                            case TelephonyManager.NETWORK_TYPE_IDEN:
                                return LinkNetWorkType._2G;
                            case TelephonyManager.NETWORK_TYPE_UMTS:
                            case TelephonyManager.NETWORK_TYPE_EVDO_0:
                            case TelephonyManager.NETWORK_TYPE_EVDO_A:
                            case TelephonyManager.NETWORK_TYPE_HSDPA:
                            case TelephonyManager.NETWORK_TYPE_HSUPA:
                            case TelephonyManager.NETWORK_TYPE_HSPA:
                            case 12: // TelephonyManager.NETWORK_TYPE_EVDO_B:
                            case 14: // TelephonyManager.NETWORK_TYPE_EHRPD:
                            case 15: // TelephonyManager.NETWORK_TYPE_HSPAP:
                                return LinkNetWorkType._3G;
                            case 13: // TelephonyManager.NETWORK_TYPE_LTE:
                                return LinkNetWorkType._4G;
                            default:
                                return LinkNetWorkType._2G;
                        }
                    }
                }
            }
        } catch (Exception e) {
            return LinkNetWorkType.UNKNOWN;
        }
        return LinkNetWorkType.UNKNOWN;
    }
}
