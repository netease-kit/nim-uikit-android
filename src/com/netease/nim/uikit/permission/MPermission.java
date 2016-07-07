package com.netease.nim.uikit.permission;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.Fragment;

import com.netease.nim.uikit.permission.annotation.OnMPermissionDenied;
import com.netease.nim.uikit.permission.annotation.OnMPermissionGranted;
import com.netease.nim.uikit.permission.annotation.OnMPermissionNeverAskAgain;
import com.netease.nim.uikit.permission.util.MPermissionUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static com.netease.nim.uikit.permission.util.MPermissionUtil.getActivity;

public class MPermission {
    private String[] permissions;
    private int requestCode;
    private Object object; // activity or fragment

    /**
     * ********************* util *********************
     */

    public static List<String> getDeniedPermissions(Activity activity, String[] permissions) {
        return getDeniedPermissions((Object) activity, permissions);
    }

    public static List<String> getDeniedPermissions(Fragment fragment, String[] permissions) {
        return getDeniedPermissions((Object) fragment, permissions);
    }

    private static List<String> getDeniedPermissions(Object activity, String[] permissions) {
        if (permissions == null || permissions.length <= 0) {
            return null;
        }

        return MPermissionUtil.findDeniedPermissions(getActivity(activity), permissions);
    }

    public static List<String> getNeverAskAgainPermissions(Activity activity, String[] permissions) {
        return getNeverAskAgainPermissions((Object) activity, permissions);
    }

    public static List<String> getNeverAskAgainPermissions(Fragment fragment, String[] permissions) {
        return getNeverAskAgainPermissions((Object) fragment, permissions);
    }

    private static List<String> getNeverAskAgainPermissions(Object activity, String[] permissions) {
        if (permissions == null || permissions.length <= 0) {
            return null;
        }

        return MPermissionUtil.findNeverAskAgainPermissions(getActivity(activity), permissions);
    }

    public static List<String> getDeniedPermissionsWithoutNeverAskAgain(Activity activity, String[] permissions) {
        return getDeniedPermissionsWithoutNeverAskAgain((Object) activity, permissions);
    }

    public static List<String> getDeniedPermissionsWithoutNeverAskAgain(Fragment fragment, String[] permissions) {
        return getDeniedPermissionsWithoutNeverAskAgain((Object) fragment, permissions);
    }

    private static List<String> getDeniedPermissionsWithoutNeverAskAgain(Object activity, String[] permissions) {
        if (permissions == null || permissions.length <= 0) {
            return null;
        }

        return MPermissionUtil.findDeniedPermissionWithoutNeverAskAgain(getActivity(activity), permissions);
    }

    /**
     * ********************* init *********************
     */

    private MPermission(Object object) {
        this.object = object;
    }

    public static MPermission with(Activity activity) {
        return new MPermission(activity);
    }

    public static MPermission with(Fragment fragment) {
        return new MPermission(fragment);
    }

    public MPermission permissions(String... permissions) {
        this.permissions = permissions;
        return this;
    }

    public MPermission addRequestCode(int requestCode) {
        this.requestCode = requestCode;
        return this;
    }

    /**
     * ********************* request *********************
     */

    @TargetApi(value = Build.VERSION_CODES.M)
    public void request() {
        requestPermissions(object, requestCode, permissions);
    }

    public static void needPermission(Activity activity, int requestCode, String[] permissions) {
        requestPermissions(activity, requestCode, permissions);
    }

    public static void needPermission(Fragment fragment, int requestCode, String[] permissions) {
        requestPermissions(fragment, requestCode, permissions);
    }

    public static void needPermission(Activity activity, int requestCode, String permission) {
        needPermission(activity, requestCode, new String[]{permission});
    }

    public static void needPermission(Fragment fragment, int requestCode, String permission) {
        needPermission(fragment, requestCode, new String[]{permission});
    }

    @TargetApi(value = Build.VERSION_CODES.M)
    private static void requestPermissions(Object object, int requestCode, String[] permissions) {
        if (!MPermissionUtil.isOverMarshmallow()) {
            doExecuteSuccess(object, requestCode);
            return;
        }
        List<String> deniedPermissions = MPermissionUtil.findDeniedPermissions(getActivity(object), permissions);

        if (deniedPermissions.size() > 0) {
            if (object instanceof Activity) {
                ((Activity) object).requestPermissions(deniedPermissions.toArray(new String[deniedPermissions.size()]), requestCode);
            } else if (object instanceof Fragment) {
                ((Fragment) object).requestPermissions(deniedPermissions.toArray(new String[deniedPermissions.size()]), requestCode);
            } else {
                throw new IllegalArgumentException(object.getClass().getName() + " is not supported");
            }
        } else {
            doExecuteSuccess(object, requestCode);
        }
    }

    /**
     * ********************* on result *********************
     */

    public static void onRequestPermissionsResult(Activity activity, int requestCode, String[] permissions, int[] grantResults) {
        requestResult(activity, requestCode, permissions, grantResults);
    }

    public static void onRequestPermissionsResult(Fragment fragment, int requestCode, String[] permissions, int[] grantResults) {
        requestResult(fragment, requestCode, permissions, grantResults);
    }

    private static void requestResult(Object obj, int requestCode, String[] permissions, int[] grantResults) {
        List<String> deniedPermissions = new ArrayList<>();
        for (int i = 0; i < grantResults.length; i++) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                deniedPermissions.add(permissions[i]);
            }
        }

        if (deniedPermissions.size() > 0) {
            if (MPermissionUtil.hasNeverAskAgainPermission(getActivity(obj), deniedPermissions)) {
                doExecuteFailAsNeverAskAgain(obj, requestCode);
            } else {
                doExecuteFail(obj, requestCode);
            }
        } else {
            doExecuteSuccess(obj, requestCode);
        }
    }

    /**
     * ********************* reflect execute result *********************
     */

    private static void doExecuteSuccess(Object activity, int requestCode) {
        executeMethod(activity, MPermissionUtil.findMethodWithRequestCode(activity.getClass(), OnMPermissionGranted.class, requestCode));
    }

    private static void doExecuteFail(Object activity, int requestCode) {
        executeMethod(activity, MPermissionUtil.findMethodWithRequestCode(activity.getClass(), OnMPermissionDenied.class, requestCode));
    }

    private static void doExecuteFailAsNeverAskAgain(Object activity, int requestCode) {
        executeMethod(activity, MPermissionUtil.findMethodWithRequestCode(activity.getClass(), OnMPermissionNeverAskAgain.class, requestCode));
    }

    /**
     * ********************* reflect execute method *********************
     */

    private static void executeMethod(Object activity, Method executeMethod) {
        executeMethodWithParam(activity, executeMethod, new Object[]{});
    }

    private static void executeMethodWithParam(Object activity, Method executeMethod, Object... args) {
        if (executeMethod != null) {
            try {
                if (!executeMethod.isAccessible()) {
                    executeMethod.setAccessible(true);
                }
                executeMethod.invoke(activity, args);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }
}
