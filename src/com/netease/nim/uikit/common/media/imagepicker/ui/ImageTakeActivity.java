package com.netease.nim.uikit.common.media.imagepicker.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;

import com.netease.nim.uikit.R;
import com.netease.nim.uikit.common.media.imagepicker.Constants;
import com.netease.nim.uikit.common.media.imagepicker.ImagePicker;
import com.netease.nim.uikit.common.media.imagepicker.ImagePickerLauncher;
import com.netease.nim.uikit.common.media.imagepicker.Utils;
import com.netease.nim.uikit.common.media.model.GLImage;
import com.netease.nim.uikit.common.media.model.GenericFileProvider;
import com.netease.nim.uikit.common.util.sys.TimeUtil;

import java.io.File;

import static com.netease.nim.uikit.common.media.imagepicker.Constants.REQUEST_CODE_CROP;
import static com.netease.nim.uikit.common.media.imagepicker.Constants.REQUEST_PERMISSION_CAMERA;


/**
 *
 */

public class ImageTakeActivity extends ImageBaseActivity {

    private ImagePicker imagePicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nim_activity_image_crop);
        imagePicker = ImagePicker.getInstance();
        if (savedInstanceState == null) {
            if (!(checkPermission(Manifest.permission.CAMERA))) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                                                  REQUEST_PERMISSION_CAMERA);
            } else {
                takePicture();
            }
        }
    }

    @Override
    public void clearRequest() {
    }

    @Override
    public void clearMemoryCache() {
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_CAMERA) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                ImagePickerLauncher.takePicture(this, Constants.REQUEST_CODE_TAKE, imagePicker.getOption());
            } else {
                showToast("权限被禁止，无法打开相机");
                finish();
            }
        }
    }

    private void takePicture() {
        File takeImageFile = null;
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            if (Utils.existSDCard()) {
                takeImageFile = new File(Environment.getExternalStorageDirectory(), "/DCIM/camera/");
            } else {
                takeImageFile = Environment.getDataDirectory();
            }
            takeImageFile = Utils.createFile(takeImageFile, "IMG_", ".jpg");
            if (takeImageFile != null) {
                // 默认情况下，即不需要指定intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                // 照相机有自己默认的存储路径，拍摄的照片将返回一个缩略图。如果想访问原始图片，
                // 可以通过dat extra能够得到原始图片位置。即，如果指定了目标uri，data就没有数据，
                // 如果没有指定uri，则data就返回有数据！
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    //Android N必须使用这种方式
                    Uri photoURI = GenericFileProvider.getUriForFile(this, getApplicationContext().getPackageName() +
                                                                           ".generic.file.provider", takeImageFile);
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                } else {
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(takeImageFile));
                }
            }
        }
        if (takeImageFile == null) {
            //TODO
            return;
        }
        //FIXME
        imagePicker.setTakeImageFile(takeImageFile);
        startActivityForResult(takePictureIntent, Constants.REQUEST_CODE_TAKE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.REQUEST_CODE_TAKE) {
            if (resultCode == RESULT_OK) {
                //发送广播通知图片增加了
                Utils.galleryAddPic(this, imagePicker.getTakeImageFile());
                GLImage glImage = GLImage.Builder.newBuilder().setAddTime(TimeUtil.getNow_millisecond()).setPath(
                        imagePicker.getTakeImageFile().getAbsolutePath()).setMimeType("image/jpeg").build();
                imagePicker.clearSelectedImages();
                imagePicker.addSelectedImageItem(glImage, true);
                if (imagePicker.isCrop()) {
                    Intent intent = new Intent(this, ImageCropActivity.class);
                    startActivityForResult(intent, REQUEST_CODE_CROP);  //单选需要裁剪，进入裁剪界面
                    return;
                } else {
                    Intent intent = new Intent();
                    intent.putExtra(Constants.EXTRA_RESULT_ITEMS, imagePicker.getSelectedImages());
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
            finish();
        } else if (requestCode == REQUEST_CODE_CROP) {
            if (resultCode == RESULT_OK) {
                setResult(RESULT_OK, data);
                finish();
            }
            finish();
        }
    }
}
