package com.netease.nim.uikit.common.media.picker.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.text.TextUtils;

import java.io.IOException;


public class BitmapUtil {

    public static Bitmap reviewPicRotate(Bitmap bitmap, String path) {
        int degree = 0;
        String mimeType = getImageType(path);
        if (!TextUtils.isEmpty(mimeType) && !mimeType.equals("image/png")) {
            degree = getPicRotate(path); // PNG没有旋转信息
        }
        if (degree != 0) {
            try {
                Matrix m = new Matrix();
                int width = bitmap.getWidth();
                int height = bitmap.getHeight();
                m.setRotate(degree);
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, m, true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return bitmap;
    }

    /**
     * 获取图片类型
     *
     * @param path 图片绝对路径
     * @return 图片类型image/jpeg image/png
     */
    public static String getImageType(String path) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        return options.outMimeType;
    }

    /**
     * 读取图片属性：旋转的角度
     *
     * @param path 图片绝对路径
     * @return degree旋转的角度
     */
    public static int getPicRotate(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    public static Bitmap resizeBitmap(Bitmap bitmap, int w, int h) {
        if (bitmap == null) {
            return null;
        }

        Bitmap BitmapOrg = bitmap;
        int width = BitmapOrg.getWidth();
        int height = BitmapOrg.getHeight();
        int newWidth = w;
        int newHeight = h;

        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;

        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);

        return Bitmap.createBitmap(BitmapOrg, 0, 0, width, height, matrix, true);
    }
}
