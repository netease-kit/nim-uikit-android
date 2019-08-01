package com.netease.nim.uikit.common.media.imagepicker.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.netease.nim.uikit.R;
import com.netease.nim.uikit.common.media.imagepicker.Constants;
import com.netease.nim.uikit.common.media.imagepicker.adapter.ImagePageAdapter;
import com.netease.nim.uikit.common.media.imagepicker.adapter.LocalImagePageAdapter;
import com.netease.nim.uikit.common.media.imagepicker.view.ViewPagerFixed;
import com.netease.nim.uikit.common.media.model.GLImage;

import java.util.ArrayList;
import java.util.List;

public class ImagePreviewRetakeActivity extends ImageBaseActivity implements View.OnClickListener {

    public static void start(Activity activity, GLImage GLImage) {
        List<GLImage> GLImages = new ArrayList<>(1);
        GLImages.add(GLImage);
        ImagePreviewRetakeActivity.start(activity, GLImages);
    }

    public static void start(Activity activity, List<GLImage> GLImages) {
        ArrayList<GLImage> datas = new ArrayList<>(GLImages.size());
        datas.addAll(GLImages);

        Intent intent = new Intent(activity, ImagePreviewRetakeActivity.class);
        intent.putExtra(Constants.EXTRA_SELECTED_IMAGE_POSITION, 0);
        intent.putExtra(Constants.EXTRA_IMAGE_ITEMS, datas);

        activity.startActivityForResult(intent, Constants.RESULT_CODE_CONFIRM_IMAGE);
    }

    private ImageView mBtnOk;                         //确认图片的选择
    private ImageView retake;
    protected ArrayList<GLImage> mGLImages;      //跳转进ImagePreviewFragment的图片文件夹
    protected ViewPagerFixed mViewPager;
    protected ImagePageAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nim_activity_image_preview_retake);

        mBtnOk = findViewById(R.id.btn_ok);
        mBtnOk.setOnClickListener(this);

        retake = findViewById(R.id.retake);
        retake.setOnClickListener(this);

        mGLImages = (ArrayList<GLImage>) getIntent().getSerializableExtra(Constants.EXTRA_IMAGE_ITEMS);
        mViewPager = findViewById(R.id.viewpager);
        mAdapter = new LocalImagePageAdapter(this, mGLImages);
        mViewPager.setAdapter(mAdapter);
        mViewPager.setCurrentItem(0, false);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_ok) {
            Intent intent = new Intent();
            intent.putExtra(Constants.RESULT_EXTRA_CONFIRM_IMAGES, mGLImages);
            setResult(RESULT_OK, intent);
            finish();
        } else if (id == R.id.retake) {
            finish();
        }
    }

    @Override
    public void clearRequest() {

    }

    @Override
    public void clearMemoryCache() {

    }
}
