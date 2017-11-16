package com.netease.nim.uikit.common.media.picker.activity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.netease.nim.uikit.R;
import com.netease.nim.uikit.api.wrapper.NimToolBarOptions;
import com.netease.nim.uikit.business.session.constant.Extras;
import com.netease.nim.uikit.business.session.constant.RequestCode;
import com.netease.nim.uikit.common.activity.ToolBarOptions;
import com.netease.nim.uikit.common.activity.UI;
import com.netease.nim.uikit.common.util.file.AttachmentStore;
import com.netease.nim.uikit.common.util.media.BitmapDecoder;
import com.netease.nim.uikit.common.util.storage.StorageType;
import com.netease.nim.uikit.common.util.storage.StorageUtil;

import java.io.File;
import java.util.ArrayList;

public class PreviewImageFromCameraActivity extends UI {

    public static final String RESULT_RETAKE = "RESULT_RETAKE";
    public static final String RESULT_SEND = "RESULT_SEND";

    private ImageView previewImageView;

    private File imageFile;

    private Button sendButton;

    private String origImageFilePath;

    private String btnText;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nim_preview_image_from_camera_activity);

        ToolBarOptions options = new NimToolBarOptions();
        setToolBar(R.id.toolbar, options);

        initActionBar();
        getIntentData();
        findViews();
        initSendBtn();
        showPicture();
    }

    private void findViews() {
        sendButton = (Button) findViewById(R.id.buttonSend);
        previewImageView = (ImageView) findViewById(R.id.imageViewPreview);
    }

    private void getIntentData() {
        String imageFilePathString = getIntent().getExtras().getString("ImageFilePath");
        origImageFilePath = getIntent().getExtras().getString("OrigImageFilePath");
        btnText = getIntent().getExtras().getString(Extras.EXTRA_PREVIEW_IMAGE_BTN_TEXT);
        imageFile = new File(imageFilePathString);
    }

    private void initSendBtn() {
        if (!TextUtils.isEmpty(btnText)) {
            sendButton.setText(btnText);
        }
        sendButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                ArrayList<String> imageList = new ArrayList<String>();
                ArrayList<String> origImageList = new ArrayList<String>();
                imageList.add(imageFile.getPath());
                origImageList.add(origImageFilePath);

                boolean isOrig = false;
                if (!isOrig) {
                    // 拍照不是原图发送的话，原图需要手动删掉
                    AttachmentStore.delete(origImageFilePath);
                }

                Intent intent = PreviewImageFromLocalActivity.initPreviewImageIntent(imageList, origImageList, isOrig);
                intent.setClass(PreviewImageFromCameraActivity.this, getIntent().getClass());
                intent.putExtra(RESULT_SEND, true);
                setResult(RESULT_OK, intent);
                PreviewImageFromCameraActivity.this.finish();
            }
        });
    }


    private void initActionBar() {
        TextView toolbarView = findView(R.id.action_bar_right_clickable_textview);
        toolbarView.setText(R.string.recapture);
        toolbarView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteTempFile();
                Intent intent = new Intent();
                intent.setClass(PreviewImageFromCameraActivity.this, getIntent().getClass());
                intent.putExtra(RESULT_RETAKE, true);
                setResult(RESULT_OK, intent);
                PreviewImageFromCameraActivity.this.finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        deleteTempFile();

        Intent intent = new Intent();
        intent.setClass(PreviewImageFromCameraActivity.this, getIntent().getClass());
        setResult(RESULT_CANCELED, intent);
        PreviewImageFromCameraActivity.this.finish();
    }

    @Override
    public void onDestroy() {
        Drawable dr = previewImageView.getDrawable();
        previewImageView.setImageBitmap(null);

        if (dr != null) {
            Bitmap bitmap = getBitmap(dr);
            if (bitmap != null) {
                bitmap.recycle();
            }
        }

        super.onDestroy();
    }

    public static final Bitmap getBitmap(Drawable dr) {
        if (dr == null) {
            return null;
        }

        if (dr instanceof BitmapDrawable) {
            return ((BitmapDrawable) dr).getBitmap();
        }

        return null;
    }

    private void showPicture() {
        try {
            Bitmap bitmap = BitmapDecoder.decodeSampledForDisplay(imageFile.getAbsolutePath());

            if (bitmap != null) {
                previewImageView.setImageBitmap(bitmap);
            } else {
                Toast.makeText(this, R.string.image_show_error, Toast.LENGTH_LONG).show();
            }
        } catch (OutOfMemoryError e) {
            Toast.makeText(this, R.string.memory_out, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * 获取本地图片
     */
    protected void choosePictureFromLocal() {
        if (!StorageUtil.hasEnoughSpaceForWrite(PreviewImageFromCameraActivity.this, StorageType.TYPE_IMAGE, true)) {
            return;
        }

        new AsyncTask<String, Integer, Boolean>() {

            @Override
            protected void onPreExecute() {
                Toast.makeText(PreviewImageFromCameraActivity.this, R.string.waitfor_image_local, Toast.LENGTH_LONG).show();
            }

            @Override
            protected Boolean doInBackground(String... params) {
                return true;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                if (Build.VERSION.SDK_INT >= 11) {
                    intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                }
                try {
                    PreviewImageFromCameraActivity.this.startActivityForResult(intent, RequestCode.GET_LOCAL_IMAGE);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(PreviewImageFromCameraActivity.this, R.string.gallery_invalid, Toast.LENGTH_LONG).show();
                }
            }
        }.execute();
    }

    private void deleteTempFile() {
        if (imageFile != null) {
            imageFile.delete();
        }

        AttachmentStore.delete(origImageFilePath);
    }
}
