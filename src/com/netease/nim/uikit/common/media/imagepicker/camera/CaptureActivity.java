package com.netease.nim.uikit.common.media.imagepicker.camera;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.netease.nim.uikit.R;
import com.netease.nim.uikit.common.ToastHelper;
import com.netease.nim.uikit.common.media.imagepicker.Constants;
import com.netease.nim.uikit.common.media.imagepicker.ui.ImagePreviewRetakeActivity;
import com.netease.nim.uikit.common.media.imagepicker.video.GLVideoConfirmActivity;
import com.netease.nim.uikit.common.media.model.GLImage;
import com.netease.nim.uikit.common.util.media.ImageUtil;
import com.netease.nim.uikit.common.util.sys.TimeUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;

public class CaptureActivity extends AppCompatActivity {

    private static final String TAG = CaptureActivity.class.getSimpleName();

    public static final String[] VIDEO_PERMISSIONS = {Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO,};

    public static final int VIDEO_PERMISSIONS_REQUEST_CODE = 1;

    private Handler mainHandler;

    private Camera mCamera;

    private int cameraId;

    private CameraPreview mPreview;

    private MediaRecorder mMediaRecorder;

    private LongPressRunnable longPressRunnable;

    private boolean isAction = false;

    private boolean isRecording = false;

    private boolean isCameraFront = false;//当前是否是前置摄像头

    private int currentTime;

    private View captureButton;

    private TextView tvBalanceTime;//录制剩余时间

    private ImageView ivSwitchCamera;//切换前后摄像头

    private ImageView ivClose;//关闭该Activity

    private CircleProgressView mProgressView;//录像进度条

    private TextView cameraTip;

    private ImageView capturebg;

    private OrientationEventListener mOrientationListener;

    private String pictureSavePath;

    private String videoSavePath;

    private int videoWidth;

    private int videoHeight;

    public static int RECORD_MAX_TIME = 15;//录制的总时长秒数，单位秒，默认15秒

    public static int RECORD_MIN_TIME = 1;//最小录制时长，单位秒，默认1秒

    public static void start(Activity activity) {
        start(activity, Constants.RESULT_CODE_RECORD_VIDEO);
    }

    public static void start(Activity activity, int requestCode) {
        if (Build.VERSION.SDK_INT < 21) {
            ToastHelper.showToast(activity, "当前系统版本暂不支持视频拍摄功能");
            return;
        }
        Intent intent = new Intent(activity, CaptureActivity.class);
        activity.startActivityForResult(intent, requestCode);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //全屏模式
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //透明导航栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        setContentView(R.layout.nim_activity_camera);
        mainHandler = new Handler();
        // Add a listener to the Capture button
        captureButton = findViewById(R.id.button_capture);
        setupTouchListener();
        mProgressView = findViewById(R.id.progressView);
        tvBalanceTime = findViewById(R.id.tv_balanceTime);
        ivSwitchCamera = findViewById(R.id.iv_switchCamera);
        cameraTip = findViewById(R.id.camera_tip);
        ivClose = findViewById(R.id.iv_close);
        capturebg = findViewById(R.id.button_capture_bg);
        ivSwitchCamera.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                switchCamera();
            }
        });
        ivClose.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!hasPermissionsGranted(VIDEO_PERMISSIONS)) {
            requestVideoPermissions();
            return;
        } else {
            setupSurfaceIfNeeded();
            setupCamera();
        }
    }

    private void setupSurfaceIfNeeded() {
        if (mPreview != null) {
            return;
        }
        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);
    }

    private void setupCamera() {
        // Create an instance of Camera
        Pair<Camera, Integer> pair = CameraUtils.getCameraInstance(isCameraFront);
        mCamera = pair.first;
        cameraId = pair.second;
        if (mCamera == null) {
            ToastHelper.showToast(this, "设备异常");
            finish();
        }
        // get Camera parameters
        Camera.Parameters params = mCamera.getParameters();
        List<String> focusModes = params.getSupportedFocusModes();
        if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            // Autofocus mode is supported
            // set the focus mode
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }
        CameraUtils.Size choosePictureSize = CameraUtils.choosePictureSize(params.getSupportedPictureSizes());
        params.setPictureSize(choosePictureSize.getWidth(), choosePictureSize.getHeight());
        params.setPictureFormat(ImageFormat.JPEG);
        params.setRotation(CameraUtils.getPictureRotation(this, cameraId));
        int displayOrientation = CameraUtils.getDisplayOrientation(this, cameraId, mCamera, false);
        mCamera.setDisplayOrientation(displayOrientation);
        // set Camera parameters
        mCamera.setParameters(params);
        mPreview.setCamera(mCamera, isCameraFront);
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseMediaRecorder();       // if you are using MediaRecorder, release it first
        releaseCamera();              // release the camera immediately on pause event
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.RESULT_CODE_CONFIRM_VIDEO) {
            if (resultCode == Activity.RESULT_OK) {
                GLImage yixinVideo = GLImage.Builder.newBuilder().setAddTime(TimeUtil.getNow_millisecond()).setDuration(
                        currentTime * 1000).setSize(new File(videoSavePath).length()).setHeight(videoHeight).setWidth(
                        videoWidth).setMimeType("video/mp4") // FIXME
                                                    .setPath(videoSavePath).build();
                ArrayList<GLImage> selectedVideos = new ArrayList<>(1);
                selectedVideos.add(yixinVideo);
                Intent intent = new Intent();
                intent.putExtra(Constants.EXTRA_RESULT_ITEMS, selectedVideos);
                setResult(RESULT_OK, intent);
                finish();
            } else {
                new File(videoSavePath).delete();
            }
        } else if (requestCode == Constants.RESULT_CODE_CONFIRM_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {
                ArrayList<GLImage> GLImages = (ArrayList<GLImage>) data.getSerializableExtra(
                        Constants.RESULT_EXTRA_CONFIRM_IMAGES);
                Intent intent = new Intent();
                intent.putExtra(Constants.EXTRA_RESULT_ITEMS, GLImages);
                setResult(RESULT_OK, intent);
                finish();
            } else {
                new File(pictureSavePath).delete();
            }
        }
    }

    private void takePicture() {
        // get an image from the camera
        mCamera.takePicture(null, null, mPicture);
    }

    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            long now = TimeUtil.getNow_millisecond();
            File pictureFile = CameraUtils.getOutputMediaFile(MEDIA_TYPE_IMAGE, String.valueOf(now));
            if (pictureFile == null) {
                Log.d(TAG, "Error creating media file, check storage permissions");
                return;
            }
            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
                pictureSavePath = pictureFile.getAbsolutePath();
                String pictureName = pictureFile.getName();
                BitmapFactory.Options options = ImageUtil.getOptions(pictureSavePath);
                GLImage image = GLImage.Builder.newBuilder().setWidth(options.outWidth).setHeight(options.outHeight)
                                               .setMimeType(options.outMimeType).setPath(pictureSavePath).setName(
                                pictureName).setSize(pictureFile.length()).setAddTime(now).build();
                ImagePreviewRetakeActivity.start(CaptureActivity.this, image);
            } catch (FileNotFoundException e) {
                Log.d(TAG, "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d(TAG, "Error accessing file: " + e.getMessage());
            }
        }
    };

    private void switchCamera() {
        mCamera.stopPreview();
        releaseCamera();
        isCameraFront = !isCameraFront;
        setupCamera();
        mCamera.startPreview();
    }

    private void setupProfile() {
        if (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_720P)) {
            CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_720P);
            mMediaRecorder.setProfile(profile);
            // default 12 * 1000 * 1000
            mMediaRecorder.setVideoEncodingBitRate(profile.videoBitRate / 8);
            //                mMediaRecorder.setOutputFormat(profile.fileFormat);
            //                mMediaRecorder.setVideoFrameRate(profile.videoFrameRate);
            //                mMediaRecorder.setVideoSize(profile.videoFrameWidth, profile.videoFrameHeight);
            //                mMediaRecorder.setVideoEncodingBitRate((int) (1.5 * 1000 * 1000));
            //                mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.HEVC);
            //                mMediaRecorder.setAudioEncodingBitRate(profile.audioBitRate);
            //                mMediaRecorder.setAudioChannels(profile.audioChannels);
            //                mMediaRecorder.setAudioSamplingRate(profile.audioSampleRate);
            //                mMediaRecorder.setAudioEncoder(profile.audioCodec);
            videoWidth = profile.videoFrameWidth;
            videoHeight = profile.videoFrameHeight;
        } else if (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_480P)) {
            CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_480P);
            mMediaRecorder.setProfile(profile);
            mMediaRecorder.setVideoEncodingBitRate(profile.videoBitRate / 8);
            videoWidth = profile.videoFrameWidth;
            videoHeight = profile.videoFrameHeight;
        } else if (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_QVGA)) {
            CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_QVGA);
            mMediaRecorder.setProfile(profile);
            mMediaRecorder.setVideoEncodingBitRate(profile.videoBitRate / 8);
            videoWidth = profile.videoFrameWidth;
            videoHeight = profile.videoFrameHeight;
        } else if (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_CIF)) {
            CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_CIF);
            mMediaRecorder.setProfile(profile);
            mMediaRecorder.setVideoEncodingBitRate(profile.videoBitRate / 8);
            videoWidth = profile.videoFrameWidth;
            videoHeight = profile.videoFrameHeight;
        } else {
            videoWidth = 960;
            videoHeight = 540;
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
            mMediaRecorder.setVideoFrameRate(30);
            mMediaRecorder.setVideoSize(videoWidth, videoHeight);
            mMediaRecorder.setVideoEncodingBitRate((int) (1.5 * 1000 * 1000));
            mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);
            mMediaRecorder.setAudioEncodingBitRate(96000);
            mMediaRecorder.setAudioChannels(1);
            mMediaRecorder.setAudioSamplingRate(48000);
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
        }
    }

    private void setupTouchListener() {
        longPressRunnable = new LongPressRunnable();
        captureButton.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        isAction = true;
                        isRecording = false;
                        mainHandler.postDelayed(longPressRunnable, 500);//同时延长500启动长按后处理的逻辑Runnable
                        break;
                    case MotionEvent.ACTION_MOVE:
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        if (isAction) {
                            isAction = false;
                            handleActionUpByState();
                        }
                        break;
                }
                return true;
            }
        });
    }

    private void handleActionUpByState() {
        mainHandler.removeCallbacks(longPressRunnable);//移除长按逻辑的Runnable
        //根据当前状态处理
        if (isRecording) {
            stopMediaRecorder();
        } else {
            takePicture();
        }
    }

    private boolean prepareVideoRecorder() {
        mMediaRecorder = new MediaRecorder();
        // Step 1: Unlock and set camera to MediaRecorder
        mCamera.unlock();
        mMediaRecorder.setCamera(mCamera);
        // Step 2: Set sources
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
        setupProfile();
        // Step 4: Set output file
        long now = TimeUtil.getNow_millisecond();
        videoSavePath = CameraUtils.getOutputMediaFile(MEDIA_TYPE_VIDEO, String.valueOf(now)).getAbsolutePath();
        mMediaRecorder.setOutputFile(videoSavePath);
        // Step 5: Set the preview output
        mMediaRecorder.setPreviewDisplay(mPreview.getHolder().getSurface());
        int degrees = CameraUtils.getDisplayOrientation(this, cameraId, mCamera, true);
        mMediaRecorder.setOrientationHint(degrees);
        if (degrees == degrees || degrees == 270) {
            int temp = videoWidth;
            videoWidth = videoHeight;
            videoHeight = temp;
        }
        // Step 6: Prepare configured MediaRecorder
        try {
            mMediaRecorder.prepare();
        } catch (IllegalStateException e) {
            Log.d(TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            Log.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        }
        return true;
    }

    private void startMediaRecorder() {
        // Camera is available and unlocked, MediaRecorder is prepared,
        // now you can start recording
        mMediaRecorder.start();
        isRecording = true;
        startButtonAnimation();
        currentTime = 0;
        mainHandler.postDelayed(progressRunnable, 0);
    }

    private void stopMediaRecorder() {
        // stop recording and release camera
        try {
            mMediaRecorder.stop();   // stop the recording
        } catch (RuntimeException stopException) {
        }
        releaseMediaRecorder(); // release the MediaRecorder object
        mCamera.lock();         // take camera access back from MediaRecorder
        isRecording = false;
        mainHandler.removeCallbacks(progressRunnable);
        stopButtonAnimation();
        tvBalanceTime.setVisibility(View.GONE);
        mProgressView.reset();
        Log.i(TAG, "stopMediaRecorder currentTime:" + currentTime);
        if (currentTime <= RECORD_MIN_TIME) {
            Toast.makeText(this, "录制时间过短", Toast.LENGTH_LONG).show();
        } else {
            GLVideoConfirmActivity.start(this, Uri.fromFile(new File(videoSavePath)), currentTime * 1000);
        }
    }

    private void releaseMediaRecorder() {
        if (mMediaRecorder != null) {
            mMediaRecorder.reset();   // clear recorder configuration
            mMediaRecorder.release(); // release the recorder object
            mMediaRecorder = null;
            mCamera.lock();           // lock camera for later use
        }
    }

    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }

    private class LongPressRunnable implements Runnable {

        @Override
        public void run() {
            // initialize video camera
            if (prepareVideoRecorder()) {
                startMediaRecorder();
            } else {
                // prepare didn't work, release the camera
                releaseMediaRecorder();
                // inform user
            }
        }
    }

    private Runnable progressRunnable = new Runnable() {

        @Override
        public void run() {
            currentTime++;
            Log.i(TAG, "recordRunnable currentTime:" + currentTime);
            //开始显示进度条
            mProgressView.setVisibility(View.VISIBLE);
            mProgressView.setIsStart(true);
            //显示时间
            tvBalanceTime.setVisibility(View.VISIBLE);
            tvBalanceTime.setText(RECORD_MAX_TIME - currentTime + "s");
            //如果超过最大录制时长则自动结束
            if (currentTime > RECORD_MAX_TIME) {
                isAction = false;
                stopMediaRecorder();
            } else {
                mainHandler.postDelayed(this, 1000);
            }
        }
    };

    //开始按下按钮动画
    public void startButtonAnimation() {
        AnimatorSet animatorSet = new AnimatorSet();//组合动画
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(capturebg, "scaleX", 1f, 1.4f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(capturebg, "scaleY", 1f, 1.4f);
        animatorSet.setDuration(100);
        animatorSet.setInterpolator(new LinearInterpolator());
        animatorSet.play(scaleX).with(scaleY);//两个动画同时开始
        animatorSet.addListener(new Animator.AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                capturebg.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        animatorSet.start();
        cameraTip.setVisibility(View.INVISIBLE);
    }

    //停止按下按钮动画
    public void stopButtonAnimation() {
        AnimatorSet animatorSet = new AnimatorSet();//组合动画
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(capturebg, "scaleX", 1.4f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(capturebg, "scaleY", 1.4f, 1f);
        //        mProgressView.setVisibility(View.INVISIBLE);
        capturebg.setVisibility(View.VISIBLE);
        animatorSet.setDuration(100);
        animatorSet.setInterpolator(new LinearInterpolator());
        animatorSet.play(scaleX).with(scaleY);//两个动画同时开始
        animatorSet.start();
        cameraTip.setVisibility(View.VISIBLE);
    }


    public static final int REQUEST_VIDEO_PERMISSIONS = 1;

    public static final String PERMISSIONS_FRAGMENT_DIALOG = "permission_dialog";

    private void requestVideoPermissions() {
        if (shouldShowRequestPermissionRationale(VIDEO_PERMISSIONS)) {
            new ConfirmationDialog().show(getFragmentManager(), PERMISSIONS_FRAGMENT_DIALOG);
        } else {
            ActivityCompat.requestPermissions(this, VIDEO_PERMISSIONS, REQUEST_VIDEO_PERMISSIONS);
        }
    }

    private boolean hasPermissionsGranted(String[] permissions) {
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private boolean shouldShowRequestPermissionRationale(String[] permissions) {
        for (String permission : permissions) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult");
        if (requestCode == REQUEST_VIDEO_PERMISSIONS) {
            if (grantResults.length == VIDEO_PERMISSIONS.length) {
                for (int result : grantResults) {
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        ErrorDialog.newInstance(getString(R.string.permission_request)).show(getFragmentManager(),
                                                                                             PERMISSIONS_FRAGMENT_DIALOG);
                        break;
                    }
                }
            } else {
                ErrorDialog.newInstance(getString(R.string.permission_request)).show(getFragmentManager(),
                                                                                     PERMISSIONS_FRAGMENT_DIALOG);
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
