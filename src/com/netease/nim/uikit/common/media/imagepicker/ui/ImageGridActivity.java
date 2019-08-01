package com.netease.nim.uikit.common.media.imagepicker.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.netease.nim.uikit.R;
import com.netease.nim.uikit.common.ToastHelper;
import com.netease.nim.uikit.common.media.imagepicker.Constants;
import com.netease.nim.uikit.common.media.imagepicker.ImagePicker;
import com.netease.nim.uikit.common.media.imagepicker.ImagePickerLauncher;
import com.netease.nim.uikit.common.media.imagepicker.adapter.ImageFolderAdapter;
import com.netease.nim.uikit.common.media.imagepicker.adapter.ImageItemDecoration;
import com.netease.nim.uikit.common.media.imagepicker.adapter.ImageSectionAdapter;
import com.netease.nim.uikit.common.media.imagepicker.adapter.vh.ImageItemViewHolder;
import com.netease.nim.uikit.common.media.imagepicker.data.AbsDataSource;
import com.netease.nim.uikit.common.media.imagepicker.data.DataSourceFactory;
import com.netease.nim.uikit.common.media.imagepicker.data.ImageFolder;
import com.netease.nim.uikit.common.media.imagepicker.option.ImagePickerOption;
import com.netease.nim.uikit.common.media.imagepicker.video.GLVideoActivity;
import com.netease.nim.uikit.common.media.imagepicker.view.FolderPopUpWindow;
import com.netease.nim.uikit.common.media.model.GLImage;
import com.netease.nim.uikit.common.ui.dialog.DialogMaker;
import com.netease.nim.uikit.common.util.sys.NetworkUtil;

import java.io.File;
import java.util.List;


public class ImageGridActivity extends ImageBaseActivity implements AbsDataSource.OnImagesLoadedListener,
        ImageSectionAdapter.OnImageClickListener, ImagePicker.OnImageSelectedListener, OnClickListener {

    private static final String KEY_PICKER_OPTION = "picker_option";

    private ImagePicker imagePicker;

    private TextView title;

    private boolean isOrigin = false;  //是否选中原图

    private RecyclerView mGridView;  //图片展示控件

    private View mFooterBar;     //底部栏

    private TextView mBtnOk;       //确定按钮

    private ImageFolderAdapter mImageFolderAdapter;    //图片文件夹的适配器

    private FolderPopUpWindow mFolderPopupWindow;  //ImageSet的PopupWindow

    private List<ImageFolder> mImageFolders;   //所有的图片文件夹

    private AbsDataSource dataSource;

    private ImageSectionAdapter sectionAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nim_activity_image_grid);
        initOption(savedInstanceState);
        findViews();
        setViewsListener();
        initView();
        initRV();
    }

    private void initOption(Bundle savedInstanceState) {
        imagePicker = ImagePicker.getInstance();
        imagePicker.clear();
        imagePicker.addOnImageSelectedListener(this);
        if (savedInstanceState != null) {
            ImagePickerOption option = (ImagePickerOption) savedInstanceState.getSerializable(KEY_PICKER_OPTION);
            imagePicker.setOption(option);
        }
    }

    private void findViews() {
        title = findViewById(R.id.tv_des);
        mBtnOk = findViewById(R.id.btn_ok);
        mFooterBar = findViewById(R.id.footer_bar);
        mGridView = findViewById(R.id.gridview);
    }

    private void setViewsListener() {
        findViewById(R.id.btn_cancel).setOnClickListener(this);
        mBtnOk.setOnClickListener(this);
        title.setOnClickListener(this);
    }

    private void initView() {
        if (!TextUtils.isEmpty(imagePicker.getTitle())) {
            title.setText(imagePicker.getTitle());
        } else {
            title.setText(imagePicker.getPickType().getTitle());
        }
        if (imagePicker.isMultiMode()) {
            setOkButtonVisible(true);
        } else {
            mFooterBar.setVisibility(View.GONE);
        }
    }

    private void initRV() {
        GridLayoutManager glm = new GridLayoutManager(this, Constants.GRIDVIEW_COLUMN);
        glm.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {

            @Override
            public int getSpanSize(int position) {
                return sectionAdapter.getSpanSize(position);
            }
        });
        mGridView.setLayoutManager(glm);
        mGridView.addItemDecoration(new ImageItemDecoration());
        mGridView.setRecyclerListener(new RecyclerView.RecyclerListener() {

            @Override
            public void onViewRecycled(RecyclerView.ViewHolder holder) {
                if (holder instanceof ImageItemViewHolder) {
                    ((ImageItemViewHolder) holder).clearRequest();
                }
            }
        });
        sectionAdapter = new ImageSectionAdapter(this);
        mGridView.setAdapter(sectionAdapter);
        sectionAdapter.setOnImageItemClickListener(this);
        mImageFolderAdapter = new ImageFolderAdapter(this, null);
        onImageSelected(null, false);
        if (checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            loadImageData(imagePicker.getOption());
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                              Constants.REQUEST_PERMISSION_STORAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Constants.REQUEST_PERMISSION_STORAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadImageData(imagePicker.getOption());
            } else {
                showToast("权限被禁止，无法选择本地图片");
            }
        } else if (requestCode == Constants.REQUEST_PERMISSION_CAMERA) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                ImagePickerLauncher.takePicture(this, Constants.REQUEST_CODE_TAKE, imagePicker.getOption());
            } else {
                showToast("权限被禁止，无法打开相机");
            }
        }
    }

    @Override
    protected void onDestroy() {
        imagePicker.removeOnImageSelectedListener(this);
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_ok) {
            if (imagePicker.getSelectImageCount() < imagePicker.getSelectMin()) {
                ToastHelper.showToast(this, getString(R.string.choose_min_num, imagePicker.getSelectMin()));
                return;
            }
            if (imagePicker.needCheckNetwork()) {
                if (!NetworkUtil.isNetAvailable(this)) {
                    ToastHelper.showToast(this, R.string.network_unavailable);
                    return;
                }
            }
            Intent intent = new Intent();
            intent.putExtra(Constants.EXTRA_RESULT_ITEMS, imagePicker.getSelectedImages());
            setResult(RESULT_OK, intent);  //多选不允许裁剪裁剪，返回数据
            finish();
        } else if (id == R.id.tv_des) {
            if (mImageFolders == null) {
                Log.i("ImageGridActivity", "您的手机没有图片");
                return;
            }
            //点击文件夹按钮
            createPopupFolderList();
            mImageFolderAdapter.refreshData(mImageFolders);  //刷新数据
            if (mFolderPopupWindow.isShowing()) {
                mFolderPopupWindow.dismiss();
            } else {
                title.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.up_icon, 0);
                mFolderPopupWindow.showAsDropDown(title, Gravity.TOP, 0, 0);
                //默认选择当前选择的上一个，当目录很多时，直接定位到已选中的条目
                int index = mImageFolderAdapter.getSelectIndex();
                index = index == 0 ? index : index - 1;
                mFolderPopupWindow.setSelection(index);
            }
        } else if (id == R.id.btn_cancel) {
            switch (ImagePicker.getInstance().getPickType()) {
                case All:
                    break;
                case Video:
                    break;
                case Image:
                    break;
            }
            finish();
        }
    }

    /**
     * 创建弹出的ListView
     */
    private void createPopupFolderList() {
        if (mFolderPopupWindow != null) {
            return;
        }
        mFolderPopupWindow = new FolderPopUpWindow(this, mImageFolderAdapter);
        mFolderPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {

            @Override
            public void onDismiss() {
                title.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.down_icon, 0);
            }
        });
        mFolderPopupWindow.setOnItemClickListener(new FolderPopUpWindow.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                mImageFolderAdapter.setSelectIndex(position);
                imagePicker.setCurrentImageFolderPosition(position);
                mFolderPopupWindow.dismiss();
                ImageFolder imageFolder = (ImageFolder) adapterView.getAdapter().getItem(position);
                if (null != imageFolder) {
                    sectionAdapter.refreshData(imageFolder.images);
                }
                updateImageFolderTitle();
                mGridView.smoothScrollToPosition(0);//滑动到顶部
            }
        });
    }

    private void updateImageFolderTitle() {
        ImageFolder imageFolder = imagePicker.getCurrentImageFolder();
        if (imageFolder != null) {
            title.setText(imageFolder.name);
        }
    }

    private void loadImageData(ImagePickerOption option) {
        dataSource = DataSourceFactory.create(this, null, option.getPickType());
        dataSource.setLoadedListener(this);
        if (dataSource != null) {
            dataSource.reload();
        }
    }

    @Override
    public void onImagesLoaded(List<ImageFolder> imageFolders) {
        this.mImageFolders = imageFolders;
        imagePicker.setImageFolders(imageFolders);
        if (imageFolders.size() == 0) {
            sectionAdapter.refreshData(null);
        } else {
            sectionAdapter.refreshData(imageFolders.get(imagePicker.getCurrentImageFolderPosition()).images);
            //mGridView.scrollToPosition(sectionAdapter.getItemCount() - 1);
        }
        mImageFolderAdapter.refreshData(imageFolders);
        updateImageFolderTitle();
    }

    @Override
    public void onImageItemClick(View view, GLImage GLImage, int position) {
        if (GLImage.isVideo()) {
            onVideoItemClick(GLImage, position);
        } else {
            onImageItemClick(GLImage, position);
        }
    }

    private void onImageItemClick(GLImage GLImage, int position) {
        if (imagePicker.isMultiMode()) {
            Intent intent = new Intent(ImageGridActivity.this, ImagePreviewActivity.class);
            intent.putExtra(Constants.EXTRA_SELECTED_IMAGE_POSITION, position);
            intent.putExtra(Constants.EXTRA_IMAGE_PREVIEW_FROM_PICKER, true);
            intent.putExtra(Constants.IS_ORIGIN, isOrigin);
            startActivityForResult(intent, Constants.REQUEST_CODE_PREVIEW);  //如果是多选，点击图片进入预览界面
        } else {
            imagePicker.clearSelectedImages();
            imagePicker.addSelectedImageItem(GLImage, true);
            if (imagePicker.isCrop()) {
                Intent intent = new Intent(ImageGridActivity.this, ImageCropActivity.class);
                startActivityForResult(intent, Constants.REQUEST_CODE_CROP);  //单选需要裁剪，进入裁剪界面
            } else {
                Intent intent = new Intent();
                intent.putExtra(Constants.EXTRA_RESULT_ITEMS, imagePicker.getSelectedImages());
                setResult(RESULT_OK, intent);   //单选不需要裁剪，返回数据
                finish();
            }
        }
    }

    private void onVideoItemClick(GLImage GLImage, int position) {
        GLVideoActivity.start(this, Uri.fromFile(new File(GLImage.getPath())), GLImage.getDuration());
    }

    @Override
    public void onImageSelected(GLImage item, boolean isAdd) {
        if (imagePicker.getSelectImageCount() > imagePicker.getSelectMin()) {
            mBtnOk.setText(R.string.send);
            setOkButtonEnable(true);
        } else {
            mBtnOk.setText(getString(R.string.send));
            setOkButtonEnable(false);
        }
        sectionAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.REQUEST_CODE_TAKE) {
            if (resultCode == RESULT_OK) {
                handleResultData(data);
            }
        } else if (requestCode == Constants.REQUEST_CODE_CROP) {
            if (resultCode == RESULT_OK) {
                handleResultData(data);
            }
        } else if (requestCode == Constants.REQUEST_CODE_PREVIEW) {
            if (resultCode == RESULT_FIRST_USER) {
                if (data != null) {
                    isOrigin = data.getBooleanExtra(Constants.IS_ORIGIN, false);
                }
            } else if (resultCode == RESULT_OK) {
                handleResultData(data);
            }
        } else if (requestCode == Constants.RESULT_CODE_RECORD_VIDEO) {
            if (resultCode == RESULT_OK) {
                handleResultData(data);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //跳转前dismiss，如果在onActivityResult返回的时候在dismiss可能会出现崩溃(当Activity实例已经被销毁，但Dialog实例是static的)！
        DialogMaker.dismissProgressDialog();
    }

    private void handleResultData(Intent data) {
        setResult(RESULT_OK, data);
        finish();
    }

    private void setOkButtonEnable(boolean enabled) {
        if (enabled) {
            mBtnOk.setEnabled(true);
        } else {
            mBtnOk.setEnabled(false);
        }
        syncButtonText();
    }

    private void setOkButtonVisible(boolean visible) {
        if (visible) {
            mBtnOk.setVisibility(View.VISIBLE);
        } else {
            mBtnOk.setVisibility(View.INVISIBLE);
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

    @Override
    public void clearRequest() {
    }

    @Override
    public void clearMemoryCache() {
        imagePicker.getImageLoader().clearMemoryCache();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(KEY_PICKER_OPTION, imagePicker.getOption());
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}