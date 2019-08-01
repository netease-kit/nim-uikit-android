package com.netease.nim.uikit.common.media.picker.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import com.netease.nim.uikit.R;
import com.netease.nim.uikit.api.wrapper.NimToolBarOptions;
import com.netease.nim.uikit.business.session.constant.Extras;
import com.netease.nim.uikit.common.activity.ToolBarOptions;
import com.netease.nim.uikit.common.activity.UI;
import com.netease.nim.uikit.common.ui.imageview.CropImageView;
import com.netease.nim.uikit.common.util.media.BitmapDecoder;
import com.netease.nim.uikit.common.util.media.ImageUtil;

public class CropImageActivity extends UI {

    public static final int PICKER_IMAGE_EDIT = 0x1040;

    private boolean returnData;

    private String filePath;

    private CropImageView cropImageView;

    public static void startForData(Activity activity, String srcFile, int outputX, int outputY, int requestCode) {
        Intent intent = new Intent(activity, CropImageActivity.class);
        intent.putExtra(Extras.EXTRA_SRC_FILE, srcFile);
        intent.putExtra(Extras.EXTRA_OUTPUTX, outputX);
        intent.putExtra(Extras.EXTRA_OUTPUTY, outputY);
        intent.putExtra(Extras.EXTRA_RETURN_DATA, true);
        activity.startActivityForResult(intent, requestCode);
    }

    public static void startForFile(Activity activity, String srcFile, int outputX, int outputY, String path,
                                    int requestCode) {
        Intent intent = new Intent(activity, CropImageActivity.class);
        intent.putExtra(Extras.EXTRA_SRC_FILE, srcFile);
        intent.putExtra(Extras.EXTRA_OUTPUTX, outputX);
        intent.putExtra(Extras.EXTRA_OUTPUTY, outputY);
        intent.putExtra(Extras.EXTRA_FILE_PATH, path);
        activity.startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nim_crop_image_activity);

        ToolBarOptions options = new NimToolBarOptions();
        options.titleId = R.string.crop;
        setToolBar(R.id.toolbar, options);

        processIntent();

        initCropImageView();
    }

    @Override
    protected void onDestroy() {
        cropImageView.clear();
        super.onDestroy();
    }

    private void processIntent() {
        Intent intent = getIntent();
        returnData = intent.getBooleanExtra(Extras.EXTRA_RETURN_DATA, false);
        filePath = intent.getStringExtra(Extras.EXTRA_FILE_PATH);
    }

    private void initCropImageView() {
        cropImageView = (CropImageView) findViewById(R.id.cropable_image_view);
        Intent intent = getIntent();
        final String srcFile = intent.getStringExtra(Extras.EXTRA_SRC_FILE);
        int outputX = intent.getIntExtra(Extras.EXTRA_OUTPUTX, 0);
        int outputY = intent.getIntExtra(Extras.EXTRA_OUTPUTY, 0);
        cropImageView.setOutput(outputX, outputY);

        // 抛到下一个UI循环，等到我们的activity真正到了前台
        // 否则可能会获取不到openGL的最大texture size，导致解出的bitmap过大，显示不了
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                Bitmap src = BitmapDecoder.decodeSampledForDisplay(srcFile);
                src = ImageUtil.rotateBitmapInNeeded(srcFile, src);
                cropImageView.setImageBitmap(src);
            }
        });
    }

    public void onClick(View v) {
        if (v.getId() == R.id.ok_btn) {
            if (returnData) {
                byte[] data = cropImageView.getCroppedImage();
                if (data != null) {
                    Intent intent = new Intent();
                    intent.putExtra(Extras.EXTRA_DATA, data);
                    setResult(RESULT_OK, intent);
                }
                finish();
            } else {
                if (cropImageView.saveCroppedImage(filePath)) {
                    setResult(RESULT_OK);
                }
                finish();
            }
        } else if (v.getId() == R.id.cancel_btn) {
            finish();
        }
    }
}
