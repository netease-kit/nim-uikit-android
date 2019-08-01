package com.netease.nim.uikit.common.media.imagepicker.camera;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.os.Environment;
import android.util.Log;
import android.util.Pair;
import android.view.Surface;
import android.view.WindowManager;

import com.netease.nim.uikit.api.NimUIKit;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;

public class CameraUtils {
    private static final String TAG = CameraUtils.class.getSimpleName();

    public static File getOutputMediaFile(int mediaType, String name) {
        String filePath;

        if (mediaType == MEDIA_TYPE_IMAGE) {
            String dir = NimUIKit.getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath();
            filePath = dir + "/" + "IMG_" + name + ".jpg";
        } else if (mediaType == MEDIA_TYPE_VIDEO) {
            String dir = NimUIKit.getContext().getExternalFilesDir(Environment.DIRECTORY_MOVIES).getAbsolutePath();
            filePath = dir + "/" + "VID_" + name + ".mp4";
        } else {
            String dir = NimUIKit.getContext().getExternalCacheDir().getAbsolutePath();
            filePath = dir + "/" + name;
        }

        return new File(filePath);
    }

    /** A safe way to get an instance of the Camera object. */
    public static Pair<Camera, Integer> getCameraInstance(boolean front) {
        Camera c = null;

        try {
            int numberOfCameras = Camera.getNumberOfCameras();
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            for (int i = 0; i < numberOfCameras; i++) {
                Camera.getCameraInfo(i, cameraInfo);
                if (front && cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    return new Pair<>(Camera.open(i), i);
                }

                if (!front && cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    return new Pair<>(Camera.open(i), i);
                }
            }
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }

        return new Pair<>(c, 0); // returns null if camera is unavailable
    }

    public static Camera.Size choosePreviewSize(List<Camera.Size> supportSizes, int width, int height) {
        if (supportSizes == null || supportSizes.size() == 0) {
            return null;
        }

        if (width == 0 || height == 0) {
            return supportSizes.get(0);
        }

        float ratio = (float) width / (float) height;

        List<Camera.Size> meetRatioAndSizeList = new ArrayList<>();
        List<Camera.Size> meetRatioList = new ArrayList<>();
        for (Camera.Size size : supportSizes) {
            if (((float) size.width / (float) size.height) == ratio) {
                meetRatioList.add(size);

                if ((size.width >= width) && (size.height >= height)) {
                    meetRatioAndSizeList.add(size);
                }
            }
        }

        Comparator<Camera.Size> sizeComparator = new Comparator<Camera.Size>() {
            @Override
            public int compare(Camera.Size o1, Camera.Size o2) {
                // We cast here to ensure the multiplications won't overflow
                return Long.signum((long) o1.width * o1.height -
                        (long) o2.width * o2.height);
            }
        };

        if (meetRatioAndSizeList.size() == 0 && meetRatioList.size() == 0) {
            return supportSizes.get(0);
        } else if (meetRatioAndSizeList.size() > 0) {
            return Collections.max(meetRatioList, sizeComparator);
        } else {
            return Collections.min(meetRatioAndSizeList, sizeComparator);
        }
    }

    private static float DEFAULT_IMAGE_SIZE = 1920 * 1080;
    public static Size choosePictureSize(List<Camera.Size> choices) {
        float diff = Float.MAX_VALUE;
        Size chosenSize = null;

        for (Camera.Size size : choices) {
            float value = Math.abs(((float) size.width * size.height) / DEFAULT_IMAGE_SIZE - 1);
            if (value < diff) {
                chosenSize = new CameraUtils.Size(size.width, size.height);
                diff = value;
            }
        }

        Log.i(TAG, "Select size:" + chosenSize);
        return chosenSize;
    }

    public static int getDisplayOrientation(
            Activity activity, int cameraId, Camera camera, boolean forVideoRecorder) {
        Camera.CameraInfo info =
                new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        // fixme
        if (!forVideoRecorder && info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }

        return result;
    }

    public static int getPictureRotation(Context context, int cameraId) {
        final WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        int orientation = windowManager != null ? windowManager.getDefaultDisplay().getRotation() : Surface.ROTATION_0;

        Camera.CameraInfo info =
                new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        orientation = (orientation + 45) / 90 * 90;
        int rotation = 0;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            rotation = (info.orientation - orientation + 360) % 360;
        } else {  // back-facing camera
            rotation = (info.orientation + orientation) % 360;
        }

        return rotation;
    }

    public static class Size{
        public int width;
        public int height;
        public Size(int w, int h) {
            width = w;
            height = h;
        }
        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }
    }
}
