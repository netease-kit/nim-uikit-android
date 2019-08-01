package com.netease.nim.uikit.support.permission;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class BaseMPermission {

    private static final String TAG = "MPermission";

    public enum MPermissionResultEnum {
        GRANTED, DENIED, DENIED_NEVER_ASK_AGAIN
    }

    static boolean isOverMarshmallow() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    static Activity getActivity(Object object) {
        if (object instanceof Fragment) {
            return ((Fragment) object).getActivity();
        } else if (object instanceof Activity) {
            return (Activity) object;
        }
        return null;
    }

    /**
     * 获取权限请求结果
     */
    public static List<MPermissionResultEnum> getPermissionResult(Activity activity, String[] permissions) {
        return findPermissionResult(activity, permissions);
    }

    public static List<MPermissionResultEnum> getPermissionResult(Fragment fragment, String[] permissions) {
        return findPermissionResult(fragment.getActivity(), permissions);
    }

    @TargetApi(value = Build.VERSION_CODES.M)
    private static List<MPermissionResultEnum> findPermissionResult(Activity activity, String... permissions) {
        boolean overM = isOverMarshmallow();
        List<MPermissionResultEnum> result = new ArrayList<>();
        for (String p : permissions) {
            if (overM) {
                if (activity.checkSelfPermission(p) == PackageManager.PERMISSION_GRANTED) {
                    result.add(MPermissionResultEnum.GRANTED);
                } else {
                    if (!activity.shouldShowRequestPermissionRationale(p)) {
                        result.add(MPermissionResultEnum.DENIED_NEVER_ASK_AGAIN);
                    } else {
                        result.add(MPermissionResultEnum.DENIED);
                    }
                }
            } else {
                result.add(MPermissionResultEnum.GRANTED);
            }
        }

        return result;
    }

    /**
     * 获取所有被未被授权的权限
     */
    public static List<String> getDeniedPermissions(Activity activity, String[] permissions) {
        return findDeniedPermissions(activity, permissions);
    }

    public static List<String> getDeniedPermissions(Fragment fragment, String[] permissions) {
        return findDeniedPermissions(fragment.getActivity(), permissions);
    }

    @TargetApi(value = Build.VERSION_CODES.M)
    static List<String> findDeniedPermissions(Activity activity, String... permissions) {
        if (!isOverMarshmallow()) {
            return null;
        }

        List<String> denyPermissions = new ArrayList<>();
        for (String value : permissions) {
            if (activity.checkSelfPermission(value) != PackageManager.PERMISSION_GRANTED) {
                denyPermissions.add(value);
            }
        }
        return denyPermissions;
    }

    /**
     * 获取被拒绝且勾选不再询问的权限
     * 请在请求权限结果回调中使用，因为从未请求过的权限也会被认为是该结果集
     */
    public static List<String> getNeverAskAgainPermissions(Activity activity, String[] permissions) {
        return findNeverAskAgainPermissions(activity, permissions);
    }

    public static List<String> getNeverAskAgainPermissions(Fragment fragment, String[] permissions) {
        return findNeverAskAgainPermissions(fragment.getActivity(), permissions);
    }

    @TargetApi(value = Build.VERSION_CODES.M)
    private static List<String> findNeverAskAgainPermissions(Activity activity, String... permissions) {
        if (!isOverMarshmallow()) {
            return null;
        }

        List<String> neverAskAgainPermission = new ArrayList<>();
        for (String value : permissions) {
            if (activity.checkSelfPermission(value) != PackageManager.PERMISSION_GRANTED &&
                    !activity.shouldShowRequestPermissionRationale(value)) {
                // 拒绝&不要需要解释了（用户勾选了不再询问）
                // 坑爹：第一次不做任何设置，返回值也是false。建议在权限授权结果里判断！！！
                neverAskAgainPermission.add(value);
            }
        }

        return neverAskAgainPermission;
    }

    @TargetApi(value = Build.VERSION_CODES.M)
    static boolean hasNeverAskAgainPermission(Activity activity, List<String> permission) {
        if (!isOverMarshmallow()) {
            return false;
        }

        for (String value : permission) {
            if (activity.checkSelfPermission(value) != PackageManager.PERMISSION_GRANTED &&
                    !activity.shouldShowRequestPermissionRationale(value)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 获取被拒绝但没有勾选不再询问的权限（可以继续申请，会继续弹框）
     */
    public static List<String> getDeniedPermissionsWithoutNeverAskAgain(Activity activity, String[] permissions) {
        return findDeniedPermissionWithoutNeverAskAgain(activity, permissions);
    }

    public static List<String> getDeniedPermissionsWithoutNeverAskAgain(Fragment fragment, String[] permissions) {
        return findDeniedPermissionWithoutNeverAskAgain(fragment.getActivity(), permissions);
    }

    @TargetApi(value = Build.VERSION_CODES.M)
    private static List<String> findDeniedPermissionWithoutNeverAskAgain(Activity activity, String... permission) {
        if (!isOverMarshmallow()) {
            return null;
        }

        List<String> denyPermissions = new ArrayList<>();
        for (String value : permission) {
            if (activity.checkSelfPermission(value) != PackageManager.PERMISSION_GRANTED &&
                    activity.shouldShowRequestPermissionRationale(value)) {
                denyPermissions.add(value); // 上次申请被用户拒绝了
            }
        }

        return denyPermissions;
    }

    /**
     * Log专用
     */
    public static void printMPermissionResult(boolean preRequest, Activity activity, String[] permissions) {
        Log.i(TAG, "----- MPermission result " + (preRequest ? "before" : "after") + " request：");
        List<BaseMPermission.MPermissionResultEnum> result = getPermissionResult(activity, permissions);
        int i = 0;
        for (BaseMPermission.MPermissionResultEnum p : result) {
            Log.i(TAG, "* MPermission=" + permissions[i++] + ", result=" + p);
        }
    }

    static String toString(List<String> permission) {
        if (permission == null || permission.isEmpty()) {
            return "";
        }

        return toString(permission.toArray(new String[permission.size()]));
    }

    private static String toString(String[] permission) {
        if (permission == null || permission.length <= 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (String p : permission) {
            sb.append(p.replaceFirst("android.permission.", ""));
            sb.append(",");
        }

        sb.deleteCharAt(sb.length() - 1);

        return sb.toString();
    }
}
