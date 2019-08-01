package com.netease.nim.uikit.common.media.imagepicker.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.netease.nim.uikit.R;
import com.netease.nim.uikit.common.ToastHelper;
import com.netease.nim.uikit.common.adapter.BaseAdapter;
import com.netease.nim.uikit.common.adapter.BaseDelegate;
import com.netease.nim.uikit.common.adapter.BaseViewHolder;
import com.netease.nim.uikit.common.adapter.ItemClickListerAdapter;
import com.netease.nim.uikit.common.media.imagepicker.Constants;
import com.netease.nim.uikit.common.media.imagepicker.ImagePicker;
import com.netease.nim.uikit.common.media.imagepicker.view.SuperCheckBox;
import com.netease.nim.uikit.common.media.model.GLImage;
import com.netease.nim.uikit.common.util.sys.NetworkUtil;
import com.netease.nim.uikit.common.util.sys.ScreenUtil;

/**
 *
 */
public class ImagePreviewActivity extends ImagePreviewBaseActivity implements ImagePicker.OnImageSelectedListener,
        View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private boolean isOrigin;                      //是否选中原图

    private TextView mCbCheck;                     //是否选中当前图片的CheckBox

    private SuperCheckBox mCbOrigin;               //原图

    private TextView mBtnOk;                       //确认图片的选择

    private View bottomBar;

    private RecyclerView recyclerView;

    private int imageSize;

    private BaseAdapter chooseAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getIntentData();
        imagePicker.addOnImageSelectedListener(this);
        mBtnOk = findViewById(R.id.btn_ok);
        mBtnOk.setOnClickListener(this);
        bottomBar = findViewById(R.id.bottom_bar);
        bottomBar.setVisibility(View.VISIBLE);

        mCbCheck = findViewById(R.id.cb_check);
        mCbOrigin = findViewById(R.id.cb_origin);
        recyclerView = findViewById(R.id.choose_list);
        mCbOrigin.setOnCheckedChangeListener(this);
        mCbOrigin.setChecked(isOrigin);
        imageSize = ScreenUtil.dip2px(55);
        syncButtonText();
        init();
        initRv();
        updateCheckState();
    }

    private void initRv() {
        chooseAdapter = new BaseAdapter(imagePicker.getSelectedImages(), new ItemClickListerAdapter<GLImage>() {
            @Override
            public void onClick(View v, int pos, GLImage data) {
                int pos1 = findPos(data);
                if (pos1 != -1) {
                    mViewPager.setCurrentItem(pos1, false);
                }
            }
        });
        chooseAdapter.setDelegate(new BaseDelegate<GLImage>() {

            @Override
            public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                return new ChooseViewHolder(parent);
            }

            @Override
            public int getItemViewType(GLImage data, int pos) {
                return 0;
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setAdapter(chooseAdapter);
    }

    private int findPos(GLImage data) {
        int pos = 0;
        for (GLImage image : mGLImages) {
            if (image.equals(data)) {
                return pos;
            }
            pos++;
        }
        return -1;
    }

    private class ChooseViewHolder extends BaseViewHolder<GLImage> {

        private ImageView chooseItem;

        private View maskItem;

        public ChooseViewHolder(ViewGroup parent) {
            super(parent, R.layout.nim_image_preview_item);
        }

        @Override
        public void findViews() {
            chooseItem = itemView.findViewById(R.id.choose_item);
            maskItem = itemView.findViewById(R.id.mask_item);
        }

        @Override
        protected void onBindViewHolder(GLImage data) {
            if (data.equals(mGLImages.get(mCurrentPosition))) {
                maskItem.setVisibility(View.VISIBLE);
            } else {
                maskItem.setVisibility(View.GONE);
            }
            imagePicker.getImageLoader().displayImage(ImagePreviewActivity.this, data.getPath(), chooseItem, imageSize,
                    imageSize);
        }
    }


    /**
     * 初始化当前页面的状态
     */
    private void init() {
        initTitle();
        onImageSelected(null, false);
        //滑动ViewPager的时候，根据外界的数据改变当前的选中状态和当前的图片的位置描述文本
        mViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                mCurrentPosition = position;
                initTitle();
                updateCheckState();
            }
        });
        //当点击当前选中按钮的时候，需要根据当前的选中状态添加和移除图片
        mCbCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GLImage GLImage = mGLImages.get(mCurrentPosition);
                mCbCheck.setSelected(!mCbCheck.isSelected());
                if (mCbCheck.isSelected()) {
                    String error = imagePicker.isSelectEnable(v.getContext(), GLImage);
                    if (!TextUtils.isEmpty(error)) {
                        mCbCheck.setSelected(false);
                        Toast.makeText(ImagePreviewActivity.this, error, Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                imagePicker.addSelectedImageItem(GLImage, mCbCheck.isSelected());
                updateCheckState();
            }
        });
    }

    private void updateCheckState() {
        GLImage item = mGLImages.get(mCurrentPosition);
        int selectOrder = imagePicker.selectOrder(item);
        mCbCheck.setSelected(selectOrder > 0);
        mCbCheck.setText(selectOrder > 0 ? selectOrder + "" : "");
        chooseAdapter.notifyDataSetChanged();
        if (selectOrder > 0) {
            int pos = 0;
            for (GLImage GLImage : imagePicker.getSelectedImages()) {
                if (GLImage.equals(item)) {
                    break;
                }
                pos++;
            }
            recyclerView.scrollToPosition(pos);
        }
    }

    private void initTitle() {
        mTitleCount.setText(getString(R.string.preview_image_count, mCurrentPosition + 1, mGLImages.size()));
    }

    @Override
    protected void getIntentData() {
        super.getIntentData();
        isOrigin = getIntent().getBooleanExtra(Constants.IS_ORIGIN, false);
    }

    @Override
    protected void onImageLongTap(View view, String url) {

    }

    /**
     * 图片添加成功后，修改当前图片的选中数量
     * 当调用 addSelectedImageItem 或 deleteSelectedImageItem 都会触发当前回调
     */
    @Override
    public void onImageSelected(GLImage item, boolean isAdd) {
        if (imagePicker.getSelectImageCount() > imagePicker.getSelectMin()) {
            mBtnOk.setText(R.string.complete);
            setOkButtonEnable(true);
        } else {
            mBtnOk.setText(R.string.complete);
            setOkButtonEnable(false);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_ok) {
            if (imagePicker.needCheckNetwork()) {
                if (!NetworkUtil.isNetAvailable(this)) {
                    ToastHelper.showToast(this, R.string.network_unavailable);
                    return;
                }
            }
            Intent intent = new Intent();
            intent.putExtra(Constants.EXTRA_RESULT_ITEMS, imagePicker.getSelectedImages());
            setResult(RESULT_OK, intent);
            finish();
        } else if (id == R.id.btn_back) {
            Intent intent = new Intent();
            intent.putExtra(Constants.IS_ORIGIN, isOrigin);
            setResult(RESULT_FIRST_USER, intent);
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra(Constants.IS_ORIGIN, isOrigin);
        setResult(RESULT_FIRST_USER, intent);
        finish();
        super.onBackPressed();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        int id = buttonView.getId();
        if (id == R.id.cb_origin) {
            isOrigin = isChecked;
        }
    }

    @Override
    protected void onDestroy() {
        imagePicker.removeOnImageSelectedListener(this);
        super.onDestroy();
    }

    /**
     * 单击时，隐藏头和尾
     */
    @Override
    public void onImageSingleTap() {

    }

    private void setOkButtonEnable(boolean enabled) {
        if (enabled) {
            mBtnOk.setEnabled(true);
        } else {
            mBtnOk.setEnabled(false);
        }
        syncButtonText();
    }

    private void syncButtonText() {
        if (imagePicker == null) {
            return;
        }
        int count = imagePicker.getSelectImageCount();
        if (count == 0) {
            mBtnOk.setText(R.string.send);
        } else {
            mBtnOk.setText(mBtnOk.getContext().getString(R.string.send_d, count));
        }
    }
}
