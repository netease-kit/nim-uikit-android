package com.netease.nim.uikit.business.session.helper;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.netease.nim.uikit.R;
import com.netease.nim.uikit.business.session.activity.CaptureVideoActivity;
import com.netease.nim.uikit.common.ToastHelper;
import com.netease.nim.uikit.common.ui.dialog.CustomAlertDialog;
import com.netease.nim.uikit.common.util.C;
import com.netease.nim.uikit.common.util.file.AttachmentStore;
import com.netease.nim.uikit.common.util.file.FileUtil;
import com.netease.nim.uikit.common.util.storage.StorageType;
import com.netease.nim.uikit.common.util.storage.StorageUtil;
import com.netease.nim.uikit.common.util.string.MD5;
import com.netease.nim.uikit.common.util.string.StringUtil;

import java.io.File;

/**
 * Created by hzxuwen on 2015/4/10.
 */
public class VideoMessageHelper {
    private File videoFile;
    private String videoFilePath;

    private Activity activity;
    private VideoMessageHelperListener listener;

    private int localRequestCode;
    private int captureRequestCode;

    public VideoMessageHelper(Activity activity, VideoMessageHelperListener listener) {
        this.activity = activity;
        this.listener = listener;
    }

    public interface VideoMessageHelperListener {
        void onVideoPicked(File file, String md5);
    }

    /**
     * 显示视频拍摄或从本地相册中选取
     */
    public void showVideoSource(int local, int capture) {
        this.localRequestCode = local;
        this.captureRequestCode = capture;
        CustomAlertDialog dialog = new CustomAlertDialog(activity);
        dialog.setTitle(activity.getString(R.string.input_panel_video));
        dialog.addItem("拍摄视频", new CustomAlertDialog.onSeparateItemClickListener() {
            @Override
            public void onClick() {
                chooseVideoFromCamera();
            }
        });
        dialog.addItem("从相册中选择视频", new CustomAlertDialog.onSeparateItemClickListener() {
            @Override
            public void onClick() {
                chooseVideoFromLocal();
            }
        });
        dialog.show();
    }

    /************************************************* 视频操作S *******************************************/

    /**
     * 拍摄视频
     */
    protected void chooseVideoFromCamera() {
        if (!StorageUtil.hasEnoughSpaceForWrite(activity, StorageType.TYPE_VIDEO, true)) {
            return;
        }
        videoFilePath = StorageUtil.getWritePath(activity, StringUtil.get36UUID() + C.FileSuffix.MP4,
                                                 StorageType.TYPE_TEMP);
        videoFile = new File(videoFilePath);
        // 启动视频录制
        CaptureVideoActivity.start(activity, videoFilePath, captureRequestCode);
    }

    /**
     * 从本地相册中选择视频
     */
    protected void chooseVideoFromLocal() {
        if (Build.VERSION.SDK_INT >= 19) {
            chooseVideoFromLocalKitKat();
        } else {
            chooseVideoFromLocalBeforeKitKat();
        }
    }

    /**
     * API19 之后选择视频
     */
    protected void chooseVideoFromLocalKitKat() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        try {
            activity.startActivityForResult(intent, localRequestCode);
        } catch (ActivityNotFoundException e) {
            ToastHelper.showToast(activity, R.string.gallery_invalid);
        } catch (SecurityException e) {

        }
    }

    /**
     * API19 之前选择视频
     */
    protected void chooseVideoFromLocalBeforeKitKat() {
        Intent mIntent = new Intent(Intent.ACTION_GET_CONTENT);
        mIntent.setType(C.MimeType.MIME_VIDEO_ALL);
        mIntent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        try {
            activity.startActivityForResult(mIntent, localRequestCode);
        } catch (ActivityNotFoundException e) {
            ToastHelper.showToast(activity, R.string.gallery_invalid);
        }
    }

    /****************************视频选中后回调操作********************************************/

    /**
     * 获取本地相册视频回调操作
     */
    public void onGetLocalVideoResult(final Intent data) {
        if (data == null) {
            return;
        }

        String filePath = filePathFromIntent(data);
        if (StringUtil.isEmpty(filePath) || !checkVideoFile(filePath)) {
            return;
        }

        String md5 = MD5.getStreamMD5(filePath);
        String filename = md5 + "." + FileUtil.getExtensionName(filePath);
        String md5Path = StorageUtil.getWritePath(filename, StorageType.TYPE_VIDEO);

        if (AttachmentStore.copy(filePath, md5Path) != -1) {
            if (listener != null) {
                listener.onVideoPicked(new File(md5Path), md5);
            }
        } else {
            ToastHelper.showToast(activity, R.string.video_exception);
        }
    }

    /**
     * 拍摄视频后回调操作
     */
    public void onCaptureVideoResult(Intent data) {

        if (videoFile == null || !videoFile.exists()) {
            //activity 可能会销毁重建，所以从这取一下
            String dataFilePath = data.getStringExtra(CaptureVideoActivity.EXTRA_DATA_FILE_NAME);
            if (!TextUtils.isEmpty(dataFilePath)) {
                videoFile = new File(dataFilePath);
            }
        }

        if (videoFile == null || !videoFile.exists()) {
            return;
        }

        //N930拍照取消也产生字节为0的文件
        if (videoFile.length() <= 0) {
            videoFile.delete();
            return;
        }

        String videoPath = videoFile.getPath();
        String md5 = MD5.getStreamMD5(videoPath);
        String md5Path = StorageUtil.getWritePath(md5 + ".mp4", StorageType.TYPE_VIDEO);

        if (AttachmentStore.move(videoPath, md5Path)) {
            if (listener != null) {
                listener.onVideoPicked(new File(md5Path), md5);
            }
        }
    }

    /**
     * 获取文件路径
     *
     * @param data intent数据
     * @return
     */
    private String filePathFromIntent(Intent data) {
        Uri uri = data.getData();

        try {
            Cursor cursor = activity.getContentResolver().query(uri, null, null, null, null);
            if (cursor == null) {
                //miui 2.3 有可能为null
                return uri.getPath();
            } else {
                cursor.moveToFirst();
                return cursor.getString(cursor.getColumnIndex("_data")); // 文件路径
            }
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 检查文件
     *
     * @param file 视频文件
     * @return boolean
     */
    private boolean checkVideoFile(String file) {
        if (!AttachmentStore.isFileExist(file)) {
            return false;
        }

        if (new File(file).length() > C.MAX_LOCAL_VIDEO_FILE_SIZE) {
            ToastHelper.showToast(activity, R.string.im_choose_video_file_size_too_large);
            return false;
        }

        if (!StorageUtil.isInvalidVideoFile(file)) {
            ToastHelper.showToast(activity, R.string.im_choose_video);
            return false;
        }
        return true;
    }

}
