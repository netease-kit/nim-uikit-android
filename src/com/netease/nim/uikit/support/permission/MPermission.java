package com.netease.nim.uikit.support.permission;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import com.netease.nim.uikit.support.permission.annotation.OnMPermissionDenied;
import com.netease.nim.uikit.support.permission.annotation.OnMPermissionGranted;
import com.netease.nim.uikit.support.permission.annotation.OnMPermissionNeverAskAgain;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class MPermission extends BaseMPermission {

    private int requestCode;
    private String[] permissions;
    private Object object; // activity or fragment

    private MPermission(Object object) {
        this.object = object;
    }

    public static MPermission with(Activity activity) {
        return new MPermission(activity);
    }

    public static MPermission with(Fragment fragment) {
        return new MPermission(fragment);
    }

    public MPermission setRequestCode(int requestCode) {
        this.requestCode = requestCode;
        return this;
    }

    public MPermission permissions(String... permissions) {
        this.permissions = permissions;
        return this;
    }

    /**
     * ********************* request *********************
     */

    @TargetApi(value = Build.VERSION_CODES.M)
    public void request() {
        doRequestPermissions(object, requestCode, permissions);
    }

    @TargetApi(value = Build.VERSION_CODES.M)
    private static void doRequestPermissions(Object object, int requestCode, String[] permissions) {
        if (!isOverMarshmallow()) {
            doExecuteSuccess(object, requestCode);
            return;
        }

        List<String> deniedPermissions = findDeniedPermissions(getActivity(object), permissions);
        if (deniedPermissions != null && deniedPermissions.size() > 0) {
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

    public static void onRequestPermissionsResult(Activity activity, int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        dispatchResult(activity, requestCode, permissions, grantResults);
    }

    public static void onRequestPermissionsResult(Fragment fragment, int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        dispatchResult(fragment, requestCode, permissions, grantResults);
    }

    private static void dispatchResult(Object obj, int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        List<String> deniedPermissions = new ArrayList<>();
        for (int i = 0; i < grantResults.length; i++) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                deniedPermissions.add(permissions[i]);
            }
        }

        if (deniedPermissions.size() > 0) {
            if (hasNeverAskAgainPermission(getActivity(obj), deniedPermissions)) {
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
        executeMethod(activity, findMethodWithRequestCode(activity.getClass(), OnMPermissionGranted.class, requestCode));
    }

    private static void doExecuteFail(Object activity, int requestCode) {
        executeMethod(activity, findMethodWithRequestCode(activity.getClass(), OnMPermissionDenied.class, requestCode));
    }

    private static void doExecuteFailAsNeverAskAgain(Object activity, int requestCode) {
        executeMethod(activity, findMethodWithRequestCode(activity.getClass(), OnMPermissionNeverAskAgain.class, requestCode));
    }

    private static <A extends Annotation> Method findMethodWithRequestCode(Class clazz, Class<A> annotation, int
            requestCode) {
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.getAnnotation(annotation) != null &&
                    isEqualRequestCodeFromAnnotation(method, annotation, requestCode)) {
                return method;
            }
        }
        return null;
    }

    private static boolean isEqualRequestCodeFromAnnotation(Method m, Class clazz, int requestCode) {
        if (clazz.equals(OnMPermissionDenied.class)) {
            return requestCode == m.getAnnotation(OnMPermissionDenied.class).value();
        } else if (clazz.equals(OnMPermissionGranted.class)) {
            return requestCode == m.getAnnotation(OnMPermissionGranted.class).value();
        } else if (clazz.equals(OnMPermissionNeverAskAgain.class)) {
            return requestCode == m.getAnnotation(OnMPermissionNeverAskAgain.class).value();
        } else {
            return false;
        }
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
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }
}
