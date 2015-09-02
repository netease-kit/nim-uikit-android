package com.netease.nim.uikit.session.actions;

import android.content.Intent;
import android.text.TextUtils;
import android.widget.Toast;

import com.netease.nim.uikit.R;
import com.netease.nim.uikit.common.media.picker.activity.PreviewImageFromCameraActivity;
import com.netease.nim.uikit.common.ui.dialog.CustomAlertDialog;
import com.netease.nim.uikit.common.util.file.AttachmentStore;
import com.netease.nim.uikit.common.util.media.ImageUtil;
import com.netease.nim.uikit.common.util.storage.StorageType;
import com.netease.nim.uikit.common.util.storage.StorageUtil;
import com.netease.nim.uikit.common.util.string.StringUtil;
import com.netease.nim.uikit.session.constant.Extras;
import com.netease.nim.uikit.session.activity.PickImageActivity;
import com.netease.nim.uikit.session.helper.SendImageHelper;
import com.netease.nim.uikit.session.constant.RequestCode;

import java.io.File;

/**
 * Created by zhoujianghua on 2015/7/31.
 */
public abstract class PickImageAction extends BaseAction {

    private static final int PICK_IMAGE_COUNT = 9;

    public static final String MIME_JPEG = "image/jpeg";
    public static final String JPG = ".jpg";

    private boolean mutiSelect;

    protected abstract void onPicked(File file);

    protected PickImageAction(int iconResId, int titleId, boolean mutiSelect) {
        super(iconResId, titleId);
        this.mutiSelect = mutiSelect;
    }

    @Override
    public void onClick() {
        int requestCode = makeRequestCode(RequestCode.PICK_IMAGE);
        showSelector(getTitleId(), requestCode, mutiSelect, tempFile());
    }

    /**
     * 打开图片选择器
     */
    private void showSelector(int titleId, final int requestCode, final boolean mutiSelect, final String outPath) {
        if (getActivity() == null) {
            return;
        }

        CustomAlertDialog dialog = new CustomAlertDialog(getActivity());
        dialog.setTitle(titleId);

        dialog.addItem(getActivity().getString(R.string.input_panel_take),new CustomAlertDialog.onSeparateItemClickListener() {
            @Override
            public void onClick() {
                int from = PickImageActivity.FROM_CAMERA;
                PickImageActivity.start(getActivity(), requestCode, from, outPath, mutiSelect, 1,
                        false, false, 0, 0);
            }
        });
        dialog.addItem(getActivity().getString(R.string.choose_from_photo_album), new CustomAlertDialog.onSeparateItemClickListener() {
            @Override
            public void onClick() {
                int from = PickImageActivity.FROM_LOCAL;
                PickImageActivity.start(getActivity(), requestCode, from, outPath, mutiSelect, PICK_IMAGE_COUNT,
                        false, false, 0, 0);
            }
        });
        dialog.show();

    }

    private String tempFile() {
        String filename = StringUtil.get32UUID() + JPG;
        return StorageUtil.getWritePath(filename, StorageType.TYPE_TEMP);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
        case RequestCode.PICK_IMAGE:
            onPickImageActivityResult(requestCode, data);
            break;
        case RequestCode.PREVIEW_IMAGE_FROM_CAMERA:
            onPreviewImageActivityResult(requestCode, data);
            break;
        }
    }

    // 图片选取回调
    private void onPickImageActivityResult(int requestCode, Intent data) {
        if (data == null) {
            Toast.makeText(getActivity(), R.string.picker_image_error, Toast.LENGTH_LONG).show();
            return;
        }

        boolean local = data.getBooleanExtra(Extras.EXTRA_FROM_LOCAL, false);
        if (local) {    // 本地相册
            sendImageAfterSelfImagePicker(data);
        } else {        // 拍照
            Intent intent = new Intent();
            if (!handleImagePath(intent, data)) {
                return;
            }

            intent.setClass(getActivity(), PreviewImageFromCameraActivity.class);
            getActivity().startActivityForResult(intent, makeRequestCode(RequestCode.PREVIEW_IMAGE_FROM_CAMERA));
        }
    }

    /**
     * 是否可以获取图片
     */
    private boolean handleImagePath(Intent intent, Intent data) {
        String photoPath = data.getStringExtra(Extras.EXTRA_FILE_PATH);
        if (TextUtils.isEmpty(photoPath)) {
            Toast.makeText(getActivity(), R.string.picker_image_error, Toast.LENGTH_LONG).show();
            return false;
        }

        File imageFile = new File(photoPath);
        intent.putExtra("OrigImageFilePath", photoPath);
        File scaledImageFile = ImageUtil.getScaledImageFileWithMD5(imageFile, MIME_JPEG);

        boolean local = data.getExtras().getBoolean(Extras.EXTRA_FROM_LOCAL, true);
        if (!local) {
            // 删除拍照生成的临时文件
            AttachmentStore.delete(photoPath);
        }

        if (scaledImageFile == null) {
            Toast.makeText(getActivity(), R.string.picker_image_error, Toast.LENGTH_LONG).show();
            return false;
        } else {
            ImageUtil.makeThumbnail(getActivity(), scaledImageFile);
        }
        intent.putExtra("ImageFilePath", scaledImageFile.getAbsolutePath());
        return true;
    }

    /**
     * 从预览界面点击发送图片
     */
    private void sendImageAfterPreviewPhotoActivityResult(Intent data) {
        SendImageHelper.sendImageAfterPreviewPhotoActivityResult(data, new SendImageHelper.Callback() {

            @Override
            public void sendImage(File file, boolean isOrig) {
                onPicked(file);
            }
        });
    }

    /**
     * 发送图片
     */
    private void sendImageAfterSelfImagePicker(final Intent data) {
        SendImageHelper.sendImageAfterSelfImagePicker(getActivity(), data, new SendImageHelper.Callback() {

            @Override
            public void sendImage(File file, boolean isOrig) {
                onPicked(file);

            }
        });
    }

    // 拍摄回调
    private void onPreviewImageActivityResult(int requestCode, Intent data) {
        if (data.getBooleanExtra(PreviewImageFromCameraActivity.RESULT_SEND, false)) {
            sendImageAfterPreviewPhotoActivityResult(data);
        } else if (data.getBooleanExtra(PreviewImageFromCameraActivity.RESULT_RETAKE, false)) {
            String filename = StringUtil.get32UUID() + JPG;
            String path = StorageUtil.getWritePath(filename, StorageType.TYPE_TEMP);

            if (requestCode == RequestCode.PREVIEW_IMAGE_FROM_CAMERA) {
                PickImageActivity.start(getActivity(), makeRequestCode(RequestCode.PICK_IMAGE), PickImageActivity.FROM_CAMERA, path);
            }
        }
    }
}
