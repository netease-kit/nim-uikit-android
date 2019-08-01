package com.netease.nim.uikit.common.ui.dialog;

import android.content.Context;
import android.content.DialogInterface.OnCancelListener;
import android.text.TextUtils;

import com.netease.nim.uikit.common.util.log.LogUtil;

import java.lang.ref.WeakReference;

public class DialogMaker {

    private static WeakReference<EasyProgressDialog> sProgressDialogRef;

    public static EasyProgressDialog showProgressDialog(Context context, String message) {
        return showProgressDialog(context, null, message, true, null);
    }

    public static EasyProgressDialog showProgressDialog(Context context, String message, boolean cancelable) {
        return showProgressDialog(context, null, message, cancelable, null);
    }

    @Deprecated
    public static EasyProgressDialog showProgressDialog(Context context, String title, String message,
                                                        boolean canCancelable, OnCancelListener listener) {

        EasyProgressDialog dialog = getDialog();

        if (dialog != null && dialog.getContext() != context) {
            // maybe existing dialog is running in a destroyed activity cotext we should recreate one
            dismissProgressDialog();
            LogUtil.e("dialog", "there is a leaked window here,orign context: " + dialog.getContext() + " now: " + context);
            dialog = null;
        }

        if (dialog == null) {
            dialog = new EasyProgressDialog(context, message);
            sProgressDialogRef = new WeakReference<>(dialog);
        }

        if (!TextUtils.isEmpty(title)) {
            dialog.setTitle(title);
        }
        if (!TextUtils.isEmpty(message)) {
            dialog.setMessage(message);
        }
        dialog.setCancelable(canCancelable);
        dialog.setOnCancelListener(listener);
        dialog.show();
        return dialog;
    }

    public static void dismissProgressDialog() {
        EasyProgressDialog dialog = getDialog();
        if (null == dialog) {
            return;
        }
        sProgressDialogRef.clear();
        if (dialog.isShowing()) {
            try {
                dialog.dismiss();
            } catch (Exception e) {
                // maybe we catch IllegalArgumentException here.
            }
        }

    }

    public static void setMessage(String message) {
        EasyProgressDialog dialog = getDialog();
        if (null != dialog && dialog.isShowing() && !TextUtils.isEmpty(message)) {
            dialog.setMessage(message);
        }
    }

    public static void updateLoadingMessage(String message) {
        EasyProgressDialog dialog = getDialog();
        if (null != dialog && dialog.isShowing() && !TextUtils.isEmpty(message)) {
            dialog.updateLoadingMessage(message);
        }
    }

    public static boolean isShowing() {
        EasyProgressDialog dialog = getDialog();
        return (dialog != null && dialog.isShowing());
    }

    private static EasyProgressDialog getDialog() {
        return sProgressDialogRef == null ? null : sProgressDialogRef.get();
    }
}
