package com.netease.nim.uikit.common.media.picker.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.netease.nim.uikit.R;
import com.netease.nim.uikit.business.session.constant.Extras;
import com.netease.nim.uikit.business.session.constant.RequestCode;
import com.netease.nim.uikit.common.activity.UI;
import com.netease.nim.uikit.common.media.picker.adapter.ImagePagerAdapterInImageSwitch;
import com.netease.nim.uikit.common.ui.dialog.EasyAlertDialog;
import com.netease.nim.uikit.common.ui.imageview.BaseZoomableImageView;
import com.netease.nim.uikit.common.util.file.AttachmentStore;
import com.netease.nim.uikit.common.util.file.FileUtil;
import com.netease.nim.uikit.common.util.media.BitmapDecoder;
import com.netease.nim.uikit.common.util.media.ImageUtil;
import com.netease.nim.uikit.common.util.storage.StorageType;
import com.netease.nim.uikit.common.util.storage.StorageUtil;

import java.io.File;
import java.util.ArrayList;

public class PreviewImageFromLocalActivity extends UI {

    private boolean needShowSendOriginal = false;

    private ImageButton originalImage;

    private boolean isSendOriginalImage;

    private TextView originalImageSizeTip;

    private File imageFile;

    private ArrayList<String> selectImageList;

    private ArrayList<String> origImageList;

    protected ViewPager imageViewPager;

    private ImagePagerAdapterInImageSwitch imageViewPagerAdapter;

    protected BaseZoomableImageView currentImageView;

    protected int currentPostion = -1;

    private int tempIndex = -1;

    private View sendButton;

    public static Intent initPreviewImageIntent(
            ArrayList<String> thumbnailImageList,
            ArrayList<String> orignialImageList,
            boolean isOrignial
    ) {
        Intent intent = new Intent();
        intent.putStringArrayListExtra(Extras.EXTRA_SCALED_IMAGE_LIST, thumbnailImageList);
        intent.putStringArrayListExtra(Extras.EXTRA_ORIG_IMAGE_LIST, orignialImageList);
        intent.putExtra(Extras.EXTRA_IS_ORIGINAL, isOrignial);
        return intent;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nim_preview_image_from_local_activity);

        originalImageSizeTip = (TextView) findViewById(R.id.picker_image_preview_orignal_image_tip);
        originalImage = (ImageButton) findViewById(R.id.picker_image_preview_orignal_image);
        originalImage.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                isSendOriginalImage = !isSendOriginalImage;
                updateOriImageSizeTip(isSendOriginalImage);
            }
        });

        needShowSendOriginal = getIntent().getBooleanExtra(Extras.EXTRA_NEED_SHOW_SEND_ORIGINAL, false);
        if (needShowSendOriginal) {
            originalImage.setVisibility(View.VISIBLE);
            originalImageSizeTip.setVisibility(View.VISIBLE);
        } else {
            originalImage.setVisibility(View.INVISIBLE);
            originalImageSizeTip.setVisibility(View.INVISIBLE);
        }

        sendButton = findViewById(R.id.buttonSend);
        sendButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isSendOriginalImage) {
                    sendOrigImage();
                } else {
                    sendSelectedImage(false);
                }
            }
        });

        initViewPager();
    }

    private void restoreList() {
        if (tempIndex != -1) {
            imageViewPager.setAdapter(imageViewPagerAdapter);
            setTitleIndex(tempIndex);
            imageViewPager.setCurrentItem(tempIndex);
            tempIndex = -1;
        }
    }

    private void setTitleIndex(int index) {
        if (selectImageList.size() <= 0) {
            setTitle("");
        } else {
            index++;
            setTitle(index + "/" + selectImageList.size());
        }
    }

    private void sendSelectedImage(boolean isOrig) {
        Intent intent = initPreviewImageIntent(selectImageList, origImageList, isOrig);
        intent.setClass(PreviewImageFromLocalActivity.this, getIntent().getClass());
        setResult(RESULT_OK, intent);
        PreviewImageFromLocalActivity.this.finish();
    }

    @SuppressLint("NewApi")
    private void sendOrigImage() {
        final UI activity = PreviewImageFromLocalActivity.this;
        final EasyAlertDialog allAlertDialog = new EasyAlertDialog(activity);
        allAlertDialog.setTitle(getString(R.string.picker_image_preview_original));

        long fileSize = 0;
        for (String filepath : origImageList) {
            fileSize += AttachmentStore.getFileLength(filepath);
        }

        if (origImageList.size() == 1) {
            allAlertDialog.setMessage(getString(R.string.image_compressed_size, FileUtil.formatFileSize(fileSize))
                    + getString(R.string.is_send_image));
        } else {
            allAlertDialog.setMessage(getString(R.string.multi_image_compressed_size,
                    FileUtil.formatFileSize(fileSize))
                    + getString(R.string.is_send_multi_image));
        }

        allAlertDialog.addPositiveButton(getString(R.string.ok), EasyAlertDialog.NO_TEXT_COLOR, EasyAlertDialog.NO_TEXT_SIZE,
                new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        sendSelectedImage(true);
                    }
                });
        allAlertDialog.addNegativeButton(getString(R.string.cancel), EasyAlertDialog.NO_TEXT_COLOR,
                EasyAlertDialog.NO_TEXT_SIZE, new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        allAlertDialog.dismiss();
                    }
                });
        if (!activity.isDestroyedCompatible()) {
            allAlertDialog.show();
        }
    }

    private void updateOriImageSizeTip(boolean isOrig) {
        if (origImageList == null)
            return;
        if (isOrig) {
            long totalSize = 0;

            for (String filepath : origImageList) {
                totalSize += AttachmentStore.getFileLength(filepath);
            }
            originalImageSizeTip.setText(String.format(this.getResources().getString(
                    R.string.picker_image_preview_original_select), FileUtil.formatFileSize(totalSize)));
            originalImage.setImageResource(R.drawable.nim_picker_orignal_checked);
        } else {
            originalImageSizeTip.setText(R.string.picker_image_preview_original);
            originalImage.setImageResource(R.drawable.nim_picker_orignal_normal);
        }
    }

    /**
     * 初始化图片查看
     */
    private void initViewPager() {
        imageViewPager = (ViewPager) findViewById(R.id.viewPagerImage);
        imageViewPager.setOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageScrollStateChanged(int arg0) {
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageSelected(int arg0) {
                setTitleIndex(arg0);
            }
        });
        imageViewPager.setOffscreenPageLimit(2);

        String imageFilePath = getIntent().getExtras().getString("ImageFilePath");
        String origImageFilePath = getIntent().getExtras().getString("OrigImageFilePath");

        imageFile = new File(imageFilePath);

        selectImageList = new ArrayList<String>();
        selectImageList.add(imageFilePath);

        origImageList = new ArrayList<String>();
        origImageList.add(origImageFilePath);

        imageViewPagerAdapter = new ImagePagerAdapterInImageSwitch(this, selectImageList, getLayoutInflater(),
                imageViewPager.getLayoutParams().width, imageViewPager.getLayoutParams().height, this);
        imageViewPager.setAdapter(imageViewPagerAdapter);
    }

    @Override
    public void onBackPressed() {
        deleteTempFiles();

        Intent intent = new Intent();
        intent.setClass(PreviewImageFromLocalActivity.this, getIntent().getClass());
        setResult(RESULT_CANCELED, intent);
        PreviewImageFromLocalActivity.this.finish();
    }

    @Override
    public void onResume() {
        // restore the data source
        restoreList();

        super.onResume();
    }

    @Override
    public void onPause() {
        // recycleBitmap(previewImageView);

        // save the data source and recycle all bitmaps
        imageViewPager.setAdapter(null);
        tempIndex = currentPostion;
        currentPostion = -1;

        super.onPause();
    }

    /**
     * 获取本地图片
     */
    protected void choosePictureFromLocal() {
        if (!StorageUtil.hasEnoughSpaceForWrite(PreviewImageFromLocalActivity.this, StorageType.TYPE_IMAGE, true)) {
            return;
        }

        new AsyncTask<String, Integer, Boolean>() {
            @Override
            protected void onPreExecute() {
                Toast.makeText(PreviewImageFromLocalActivity.this, R.string.waitfor_image_local, Toast.LENGTH_LONG).show();
            }

            @Override
            protected Boolean doInBackground(String... params) {
                return true;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                int from = PickImageActivity.FROM_LOCAL;
                Activity thiz = PreviewImageFromLocalActivity.this;
                PickImageActivity.start(thiz, RequestCode.GET_LOCAL_IMAGE, from, "");
            }
        }.execute();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_CANCELED) {
            if (requestCode == RequestCode.GET_LOCAL_IMAGE) {
                onGetLocalPictureResult(requestCode, resultCode, data);
            }
        } else {
            if (selectImageList.size() == 0) {
                sendButton.setEnabled(false);
            }
        }
    }

    public void onGetLocalPictureResult(int requestCode, int resultCode, final Intent data) {
        if (data == null) {
            return;
        }
        final String photoPath = data.getStringExtra(Extras.EXTRA_FILE_PATH);
        if (TextUtils.isEmpty(photoPath)) {
            return;
        }

        imageFile = new File(photoPath);
        String mimeType = FileUtil.getExtensionName(photoPath);
        imageFile = ImageUtil.getScaledImageFileWithMD5(imageFile, mimeType);
        if (imageFile == null) {
            Toast.makeText(this, R.string.picker_image_error, Toast.LENGTH_LONG).show();
            return;
        } else {
            ImageUtil.makeThumbnail(PreviewImageFromLocalActivity.this,
                    imageFile);
        }

        origImageList.add(photoPath);
        selectImageList.add(imageFile.getAbsolutePath());
        imageViewPagerAdapter.notifyDataSetChanged();

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                imageViewPager.setCurrentItem(selectImageList.size() - 1);
            }
        }, 100);
        //有可用的图片
        if (selectImageList.size() >= 1) {
            sendButton.setEnabled(true);
        }
    }

    public void updateCurrentImageView(final int position) {
        if (selectImageList == null
                || (position > 0
                && position >= selectImageList.size()))
            return;

        if (currentPostion == position) {
            return;
        } else {
            currentPostion = position;
        }
        setTitleIndex(position);

        // LinearLayout currentLayout = (LinearLayout)
        // imageViewPager.getChildAt(position);
        LinearLayout currentLayout = (LinearLayout) imageViewPager.findViewWithTag(position);

        if (currentLayout == null) {
            Handler mHandler = new Handler();
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    updateCurrentImageView(position);
                }
            }, 300);
            return;
        }
        currentImageView = (BaseZoomableImageView) currentLayout.findViewById(R.id.imageView);
        currentImageView.setViewPager(imageViewPager);

        setImageView(selectImageList.get(position));
    }

    public void setImageView(String imageFilePath) {
        if (imageFilePath == null) {
            return;
        }

        Bitmap bitmap = BitmapDecoder.decodeSampledForDisplay(imageFilePath);
        if (bitmap == null) {
            currentImageView.setImageBitmap(ImageUtil.getDefaultBitmapWhenGetFail());
            Toast.makeText(this, R.string.picker_image_error, Toast.LENGTH_LONG).show();
        } else {
            currentImageView.setImageBitmap(bitmap);
        }
    }

    private void deleteTempFiles() {
        for (String file : selectImageList) {
            new File(file).delete();
        }
    }
}
