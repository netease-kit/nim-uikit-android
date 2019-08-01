package com.netease.nim.uikit.common.media.imagepicker.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.netease.nim.uikit.R;
import com.netease.nim.uikit.common.media.imagepicker.Constants;
import com.netease.nim.uikit.common.media.imagepicker.ImagePicker;
import com.netease.nim.uikit.common.media.imagepicker.adapter.ImagePageAdapter;
import com.netease.nim.uikit.common.media.imagepicker.adapter.LocalImagePageAdapter;
import com.netease.nim.uikit.common.media.imagepicker.view.ViewPagerFixed;
import com.netease.nim.uikit.common.media.model.GLImage;

import java.util.ArrayList;

import uk.co.senab.photoview.PhotoView;

public abstract class ImagePreviewBaseActivity extends ImageBaseActivity {

    protected ImagePicker imagePicker;

    protected ArrayList<GLImage> mGLImages;      //跳转进ImagePreviewFragment的图片文件夹

    protected int mCurrentPosition = 0;              //跳转进ImagePreviewFragment时的序号，第几个图片

    protected TextView mTitleCount;                  //显示当前图片的位置  例如  5/31

    protected ViewPagerFixed mViewPager;

    protected ImagePageAdapter mAdapter;

    public int getLayoutResId() {
        return R.layout.nim_activity_image_preview;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResId());
        getIntentData();

        findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mTitleCount = findViewById(R.id.tv_des);

        mViewPager = findViewById(R.id.viewpager);
        mAdapter = new LocalImagePageAdapter(this, mGLImages);
        mAdapter.setPhotoViewClickListener(new ImagePageAdapter.PhotoViewClickListener() {
            @Override
            public void OnPhotoTapListener(View view, float v, float v1) {
                onImageSingleTap();
            }

            @Override
            public void onPhotoLongListener(PhotoView view, String url) {
                onImageLongTap(view, url);
            }
        });
        mViewPager.setAdapter(mAdapter);
        mViewPager.setCurrentItem(mCurrentPosition, false);

        //初始化当前页面的状态
        mTitleCount.setText(getString(R.string.preview_image_count, mCurrentPosition + 1, mGLImages.size()));
    }

    protected void getIntentData() {
        mCurrentPosition = getIntent().getIntExtra(Constants.EXTRA_SELECTED_IMAGE_POSITION, 0);
        imagePicker = ImagePicker.getInstance();
        if (getIntent().getBooleanExtra(Constants.EXTRA_IMAGE_PREVIEW_FROM_PICKER, false)) {
            mGLImages = imagePicker.getCurrentImageFolderItems();
        } else {
            mGLImages = (ArrayList<GLImage>) getIntent().getSerializableExtra(Constants.EXTRA_IMAGE_ITEMS);
        }
    }

    protected abstract void onImageLongTap(View view, String url);

    /**
     * 单击时，隐藏头和尾
     */
    public abstract void onImageSingleTap();

    @Override
    public void clearRequest() {
    }

    @Override
    public void clearMemoryCache() {
        imagePicker.getImageLoader().clearMemoryCache();
    }
}