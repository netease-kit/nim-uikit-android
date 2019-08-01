package com.netease.nim.uikit.common;

import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.Toast;


public class ToastHelper {

    private static Toast sToast ;


    private ToastHelper(){

    }

    public static void showToast(Context context , String text){
        showToastInner(context,text,Toast.LENGTH_SHORT);
    }

    public static void showToast(Context context , int stringId){
        showToastInner(context,context.getString(stringId),Toast.LENGTH_SHORT);
    }


    public static void showToastLong(Context context , String text ){
        showToastInner(context,text,Toast.LENGTH_LONG);
    }

    public static void showToastLong(Context context , int stringId){
        showToastInner(context,context.getString(stringId),Toast.LENGTH_LONG);
    }


    private static void showToastInner(Context context , String text , int duration){
        ensureToast( context);
        sToast.setText(text);
        sToast.setDuration(duration);
        sToast.show();
    }


    @SuppressLint("ShowToast")
    private static void ensureToast(Context context) {
        if(sToast!=null){
            return;
        }
        synchronized (ToastHelper.class){
            if(sToast!=null) {
                return;
            }
            sToast =Toast.makeText(context.getApplicationContext()," ",Toast.LENGTH_SHORT);
        }
    }
}
