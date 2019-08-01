package com.netease.nim.uikit.common.ui.dialog;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;

import com.netease.nim.uikit.R;

public class EasyAlertDialogHelper {
    public static void popClearMessageConfirmDialog(final Activity activity, final OnClearMessageListener listener,
                                                    String title) {
        OnDialogActionListener actionListener = new OnDialogActionListener() {
            @Override
            public void doCancelAction() {
            }

            @Override
            public void doOkAction() {
                listener.clearAllMessage();
                // activity.finish();
            }
        };
        final EasyAlertDialog dialog = createOkCancelDiolag(activity, null, title,
                activity.getString(R.string.clear_empty), null, true, actionListener);
        dialog.show();
    }

    public interface OnClearMessageListener {
        void clearAllMessage();
    }

    public static void showOneButtonDiolag(Context mContext, int titleResId, int msgResId, int btnResId,
                                           boolean cancelable, final OnClickListener positiveListener) {
        showOneButtonDiolag(mContext, getString(mContext, titleResId), getString(mContext, msgResId),
                getString(mContext, btnResId), cancelable, positiveListener);
    }

    public static void showOneButtonDiolag(Context mContext, CharSequence titleString, CharSequence msgString,
                                           CharSequence btnString, boolean cancelable, final OnClickListener positiveListener) {
        final EasyAlertDialog dialog = new EasyAlertDialog(mContext);
        if (TextUtils.isEmpty(titleString)) {
            dialog.setTitleVisible(false);
        } else {
            dialog.setTitle(titleString);
        }
        if (TextUtils.isEmpty(msgString)) {
            dialog.setMessageVisible(false);
        } else {
            dialog.setMessage(msgString);
        }
        dialog.setCancelable(cancelable);
        dialog.addPositiveButton(TextUtils.isEmpty(btnString) ? mContext.getString(R.string.iknow) : btnString,
                EasyAlertDialog.NO_TEXT_COLOR, EasyAlertDialog.NO_TEXT_SIZE, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        if (positiveListener != null)
                            positiveListener.onClick(v);
                    }
                });
        dialog.show();
    }

    public static EasyAlertDialog createOkCancelDiolag(Context context, CharSequence title, CharSequence message,
                                                       boolean cancelable, final OnDialogActionListener listener) {
        return createOkCancelDiolag(context, title, message, null, null, cancelable, listener);
    }

    /**
     * 两个按钮的dialog
     *
     * @param context
     * @param title
     * @param message
     * @param okStr
     * @param cancelStr
     * @param cancelable
     * @param listener
     * @return
     */
    public static EasyAlertDialog createOkCancelDiolag(Context context, CharSequence title, CharSequence message,
                                                       CharSequence okStr, CharSequence cancelStr, boolean cancelable, final OnDialogActionListener listener) {
        final EasyAlertDialog dialog = new EasyAlertDialog(context);
        OnClickListener okListener = new OnClickListener() {
            public void onClick(View v) {
                dialog.dismiss();
                listener.doOkAction();
            }
        };
        OnClickListener cancelListener = new OnClickListener() {
            public void onClick(View v) {
                dialog.dismiss();
                listener.doCancelAction();
            }
        };
        if (TextUtils.isEmpty(title)) {
            dialog.setTitleVisible(false);
        } else {
            dialog.setTitle(title);
        }
        if (TextUtils.isEmpty(message)) {
            dialog.setMessageVisible(false);
        } else {
            dialog.setMessage(message);
        }
        dialog.addPositiveButton(okStr, okListener);
        dialog.addNegativeButton(cancelStr, cancelListener);
        dialog.setCancelable(cancelable);
        return dialog;
    }

    public interface OnDialogActionListener {
        void doCancelAction();

        void doOkAction();
    }

    private static String getString(Context context, int id) {
        if (id == 0) {
            return null;
        }
        return context.getString(id);
    }
}
