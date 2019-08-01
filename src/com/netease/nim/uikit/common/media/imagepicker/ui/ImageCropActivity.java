package com.netease.nim.uikit.common.media.imagepicker.ui;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.netease.nim.uikit.R;
import com.netease.nim.uikit.common.media.imagepicker.Constants;
import com.netease.nim.uikit.common.media.imagepicker.ImagePicker;
import com.netease.nim.uikit.common.media.imagepicker.loader.GlideImageLoader;
import com.netease.nim.uikit.common.media.imagepicker.view.CropImageView;
import com.netease.nim.uikit.common.media.model.GLImage;

import java.io.File;
import java.util.ArrayList;


public class ImageCropActivity extends ImageBaseActivity implements View.OnClickListener,
        CropImageView.OnBitmapSaveCompleteListener {

    private CropImageView mCropImageView;
    private boolean mIsSaveRectangle;
    private int mOutputX;
    private int mOutputY;
    private ArrayList<GLImage> mGLImages;
    private ImagePicker imagePicker;
    private TextView btn_ok;
    private TextView tv_des;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nim_activity_image_crop);

        imagePicker = ImagePicker.getInstance();

        //初始化View
        findViewById(R.id.btn_back).setOnClickListener(this);
        btn_ok = findViewById(R.id.btn_ok);
        btn_ok.setText(getString(R.string.complete));
        btn_ok.setOnClickListener(this);
        tv_des = findViewById(R.id.tv_des);
        tv_des.setText(getString(R.string.photo_crop));
        mCropImageView = findViewById(R.id.cv_crop_image);
        mCropImageView.setOnBitmapSaveCompleteListener(this);

        //获取需要的参数
        mOutputX = imagePicker.getOutPutX();
        mOutputY = imagePicker.getOutPutY();
        mIsSaveRectangle = imagePicker.isSaveRectangle();
        mGLImages = imagePicker.getSelectedImages();
        String imagePath = mGLImages.get(0).getPath();

        mCropImageView.setFocusStyle(imagePicker.getStyle());
        mCropImageView.setFocusWidth(imagePicker.getFocusWidth());
        mCropImageView.setFocusHeight(imagePicker.getFocusHeight());

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        final View loadingView = findViewById(R.id.pb_loading);
        btn_ok.setEnabled(false);
        ImagePicker.getInstance().getImageLoader().displayImage(this, imagePath, mCropImageView,
                displayMetrics.widthPixels, displayMetrics.heightPixels, new GlideImageLoader.LoadListener() {
                    @Override
                    public void onLoadSuccess() {
                        loadingView.setVisibility(View.GONE);
                        btn_ok.setEnabled(true);
                    }

                    @Override
                    public void onLoadFailed() {

                    }
                });
    }

    public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        int width = options.outWidth;
        int height = options.outHeight;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            if (width > height) {
                inSampleSize = width / reqWidth;
            } else {
                inSampleSize = height / reqHeight;
            }
        }
        return inSampleSize;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_back) {
            setResult(RESULT_CANCELED);
            finish();
        } else if (id == R.id.btn_ok) {
            boolean ret = mCropImageView.saveBitmapToFile(imagePicker.getCropCacheFolder(this), mOutputX, mOutputY,
                    mIsSaveRectangle);
            if (!ret) {
                Toast.makeText(this, "裁剪失败，换一张试试", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onBitmapSaveSuccess(File file) {
        //裁剪后替换掉返回数据的内容，但是不要改变全局中的选中数据
        GLImage glImage = mGLImages.remove(0);
        GLImage newItem = GLImage.Builder.newBuilder(glImage).setPath(file.getAbsolutePath()).build();
        mGLImages.add(0, newItem);

        Intent intent = new Intent();
        intent.putExtra(Constants.EXTRA_RESULT_ITEMS, mGLImages);
        setResult(RESULT_OK, intent);   //单选不需要裁剪，返回数据
        finish();
    }

    @Override
    public void onBitmapSaveError(File file) {

    }

    @Override
    public void clearRequest() {
        // imagePicker.getImageLoader().clearRequest(mCropImageView);
    }

    @Override
    public void clearMemoryCache() {
        imagePicker.getImageLoader().clearMemoryCache();
    }
}
