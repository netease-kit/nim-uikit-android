package com.netease.nim.uikit.common.media.picker.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.netease.nim.uikit.R;
import com.netease.nim.uikit.common.activity.TActionBarActivity;
import com.netease.nim.uikit.common.media.picker.adapter.PickerPreviewPagerAdapter;
import com.netease.nim.uikit.common.media.picker.model.PhotoInfo;
import com.netease.nim.uikit.common.media.picker.model.PickerContract;
import com.netease.nim.uikit.common.media.picker.util.PickerUtil;
import com.netease.nim.uikit.common.ui.imageview.BaseZoomableImageView;
import com.netease.nim.uikit.common.util.media.BitmapDecoder;
import com.netease.nim.uikit.common.util.media.ImageUtil;
import com.netease.nim.uikit.common.util.sys.ActionBarUtil;
import com.netease.nim.uikit.session.constant.Extras;
import com.netease.nim.uikit.session.constant.RequestCode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PickerAlbumPreviewActivity extends TActionBarActivity implements OnClickListener, OnPageChangeListener {
	
	public static final int RESULT_FROM_USER  = RESULT_FIRST_USER + 1;
	
	public static void start(Activity activity, List<PhotoInfo> photos, int position, boolean supportOrig, 
			boolean isOrig, List<PhotoInfo> selectPhotoList, int mutiSelectLimitSize) {
		Intent intent = PickerContract.makePreviewDataIntent(photos, selectPhotoList);
		intent.setClass(activity, PickerAlbumPreviewActivity.class);
		intent.putExtra(Extras.EXTRA_PREVIEW_CURRENT_POS, position);
		intent.putExtra(Extras.EXTRA_SUPPORT_ORIGINAL, supportOrig);
		intent.putExtra(Extras.EXTRA_IS_ORIGINAL, isOrig);
		intent.putExtra(Extras.EXTRA_MUTI_SELECT_SIZE_LIMIT, mutiSelectLimitSize);
		activity.startActivityForResult(intent, RequestCode.PICKER_IMAGE_PREVIEW);
	}
	
	public static void start(Fragment fragment, List<PhotoInfo> photos, int position, boolean supportOrig, 
			boolean isOrig, List<PhotoInfo> selectPhotoList, int mutiSelectLimitSize) {
		Intent intent = PickerContract.makePreviewDataIntent(photos, selectPhotoList);
		intent.setClass(fragment.getActivity(), PickerAlbumPreviewActivity.class);
		intent.putExtra(Extras.EXTRA_PREVIEW_CURRENT_POS, position);
		intent.putExtra(Extras.EXTRA_SUPPORT_ORIGINAL, supportOrig);
		intent.putExtra(Extras.EXTRA_IS_ORIGINAL, isOrig);
		intent.putExtra(Extras.EXTRA_MUTI_SELECT_SIZE_LIMIT, mutiSelectLimitSize);
		fragment.startActivityForResult(intent, RequestCode.PICKER_IMAGE_PREVIEW);
	}
	
	private ViewPager imageViewPager;
	
	private PickerPreviewPagerAdapter imageViewPagerAdapter;
	
	private List<PhotoInfo> selectPhotoList = new ArrayList<PhotoInfo>();
	
	private List<PhotoInfo> photoLists = new ArrayList<PhotoInfo>();
	
	private int firstDisplayImageIndex = 0;
	
	private int currentPosition = -1;
	
	private int totalSize;
	
	private BaseZoomableImageView currentImageView;
	
	private int tempIndex = -1;
	
	@SuppressWarnings("unused")
	private LinearLayout previewOperationBar;
	
	private ImageButton originalImage;
	
	private boolean isSupportOriginal;
	
	private boolean isSendOriginalImage;
	
	private TextView originalImageSizeTip;
	
	private TextView previewSendBtn;
	
	private ImageButton previewSelectBtn;

	private int mutiSelectLimitSize;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.nim_picker_image_preview_activity);
		proceedExtras();
		initActionBar();
		initUI();
	}
	
	private void proceedExtras(){
		Intent intent = getIntent();
		isSupportOriginal = intent.getBooleanExtra(Extras.EXTRA_SUPPORT_ORIGINAL, false);
		isSendOriginalImage = intent.getBooleanExtra(Extras.EXTRA_IS_ORIGINAL, false);
		firstDisplayImageIndex = intent.getIntExtra(Extras.EXTRA_PREVIEW_CURRENT_POS, 0);
		mutiSelectLimitSize = intent.getIntExtra(Extras.EXTRA_MUTI_SELECT_SIZE_LIMIT, 9);
		
		photoLists.addAll(PickerContract.getPhotos(intent));
		totalSize = photoLists.size();
		
		selectPhotoList.clear();
		selectPhotoList.addAll(PickerContract.getSelectPhotos(intent));
	}
	
	private void initActionBar(){
		View barView = ActionBarUtil.addRightCustomViewOnActionBar(this, R.layout.nim_action_bar_right_picker_preview);
		previewSelectBtn = (ImageButton) barView.findViewById(R.id.picker_image_preview_photos_select);
		previewSelectBtn.setOnClickListener(this);
	}
	
	private void initUI(){		
		previewOperationBar = (LinearLayout) findViewById(R.id.picker_image_preview_operator_bar);
		originalImage = (ImageButton) findViewById(R.id.picker_image_preview_orignal_image);
		originalImage.setOnClickListener(this);
		originalImageSizeTip = (TextView) findViewById(R.id.picker_image_preview_orignal_image_tip);
		if(!isSupportOriginal){
			originalImage.setVisibility(View.INVISIBLE);
			originalImageSizeTip.setVisibility(View.INVISIBLE);
		}
		previewSendBtn = (TextView) findViewById(R.id.picker_image_preview_send);
		previewSendBtn.setOnClickListener(this);
		updateSelectBtnStatus();
		updateOriImageSizeTip(isSendOriginalImage);
				
		imageViewPager = (ViewPager) findViewById(R.id.picker_image_preview_viewpager);
		imageViewPager.setOnPageChangeListener(this);
		imageViewPager.setOffscreenPageLimit(2);
		imageViewPagerAdapter = new PickerPreviewPagerAdapter(this, photoLists, getLayoutInflater(),
				imageViewPager.getLayoutParams().width, imageViewPager.getLayoutParams().height, this);
		imageViewPager.setAdapter(imageViewPagerAdapter);
		
		setTitleIndex(firstDisplayImageIndex);
		updateTitleSelect(firstDisplayImageIndex);
		imageViewPager.setCurrentItem(firstDisplayImageIndex);
	}
	
	private void updateTitleSelect(int index){
		if (photoLists == null || index >= photoLists.size())
			return;
		
		PhotoInfo photo = photoLists.get(index);
		if(photo.isChoose()){
			previewSelectBtn.setImageResource(R.drawable.nim_picker_image_selected);
		}else{
			previewSelectBtn.setImageResource(R.drawable.nim_picker_preview_unselected);
		}
	}
	
	private void setTitleIndex(int index) {
		if (totalSize <= 0) {
			setTitle("");
		}else {
			index++;
			setTitle(index + "/" + totalSize);
		}
	}
	
	public void updateCurrentImageView(final int position) {
		if (photoLists == null 
				|| (position > 0
				&& position >= photoLists.size()))
			return;

		if (currentPosition == position) {
			return;
		} else {
			currentPosition = position;
		}
		
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

		setImageView(photoLists.get(position));
	}
	
	public void setImageView(PhotoInfo info) {
		if (info == null) {
			return;
		}
		
		if(info.getAbsolutePath() == null){
			return;
		}

		Bitmap bitmap = BitmapDecoder.decodeSampledForDisplay(info.getAbsolutePath());
		if (bitmap == null) {
			currentImageView.setImageBitmap(ImageUtil.getDefaultBitmapWhenGetFail());
			Toast.makeText(this, R.string.picker_image_error, Toast.LENGTH_LONG).show();
		} else {			
			try{
				bitmap = ImageUtil.rotateBitmapInNeeded(info.getAbsolutePath(), bitmap);
			} catch (OutOfMemoryError e) {
				e.printStackTrace();
			}
			currentImageView.setImageBitmap(bitmap);
		}
	}
	
	private void restoreList() {
		if (tempIndex != -1) {
			imageViewPager.setAdapter(imageViewPagerAdapter);
			setTitleIndex(tempIndex);
			imageViewPager.setCurrentItem(tempIndex);
			tempIndex = -1;
		}
	}
	
	private void updateSelectBtnStatus(){
		int selectSize = selectPhotoList.size();
		if(selectSize > 0){
			previewSendBtn.setEnabled(true);
			previewSendBtn.setText(String.format(this.getResources().getString(
					R.string.picker_image_send_select), selectSize));
		}else{
			previewSendBtn.setEnabled(true);
			previewSendBtn.setText(R.string.picker_image_send);
		}
	}
	
	private void updatePreviewSelectBtnStatus(boolean isChoose){
		if(isChoose){
			previewSelectBtn.setImageResource(R.drawable.nim_picker_image_selected);
		}else{
			previewSelectBtn.setImageResource(R.drawable.nim_picker_preview_unselected);
		}
	}
	
	private void updateOriImageSizeTip(boolean isOri){
		if(selectPhotoList == null)
			return;
		if(isOri){
			long totalSize = 0;
			for(int i = 0; i < selectPhotoList.size(); i++){
				PhotoInfo pi = selectPhotoList.get(i);
				totalSize += pi.getSize();
			}
			originalImageSizeTip.setText(String.format(this.getResources().getString(
					R.string.picker_image_preview_original_select), PickerUtil.getFileSizeString(totalSize)));
			originalImage.setImageResource(R.drawable.nim_picker_orignal_checked);
		}else{
			originalImageSizeTip.setText(R.string.picker_image_preview_original);
			originalImage.setImageResource(R.drawable.nim_picker_orignal_normal);
		}
	}
	
	private boolean checkSelectPhoto(PhotoInfo photo){
		boolean isSelect = false;
		for(int i = 0; i < selectPhotoList.size(); i++){
			PhotoInfo select = selectPhotoList.get(i);
			if(select.getImageId() == photo.getImageId()){
				isSelect = true;
				break;
			}
		}
		
		return isSelect;
	}
	
	private void removeSelectPhoto(PhotoInfo photo){
		Iterator<PhotoInfo> lIterator = selectPhotoList.iterator();
		while (lIterator.hasNext()) {
			PhotoInfo select = lIterator.next();
            if(select.getImageId() == photo.getImageId()) {
            	lIterator.remove();
            } 
        }
	}
	
	@Override
	public void onResume() {
		// restore the data source
		restoreList();

		super.onResume();
	}

	@Override
	public void onPause() {
		// save the data source and recycle all bitmaps
		imageViewPager.setAdapter(null);
		tempIndex = currentPosition;
		currentPosition = -1;

		super.onPause();
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.picker_image_preview_photos_select) {
            if (photoLists == null || currentPosition >= photoLists.size())
                return;

            PhotoInfo selectPhoto = photoLists.get(currentPosition);
            boolean isChoose = selectPhoto.isChoose();
            if (selectPhotoList != null && selectPhotoList.size() >= mutiSelectLimitSize && !isChoose) {
                Toast.makeText(this, String.format(getResources().getString(R.string.picker_image_exceed_max_image_select)
                        , mutiSelectLimitSize), Toast.LENGTH_SHORT).show();
                return;
            }
            selectPhoto.setChoose(!isChoose);
            updatePreviewSelectBtnStatus(!isChoose);

            // check
            if (!isChoose) {
                boolean isSelect = checkSelectPhoto(selectPhoto);
                if (!isSelect) {
                    selectPhotoList.add(selectPhoto);
                }
            } else {
                removeSelectPhoto(selectPhoto);
            }
            // update bottom bar
            updateSelectBtnStatus();
            // 如果没有选择，则自动取消原图发送
            if (selectPhotoList.size() == 0 && isSendOriginalImage) {
                isSendOriginalImage = false;
            }
            updateOriImageSizeTip(isSendOriginalImage);
        } else if (v.getId() == R.id.picker_image_preview_send) {
            if (selectPhotoList != null && selectPhotoList.size() == 0) { // 没有选择，点击发送则发送当前图片
                PhotoInfo current = photoLists.get(currentPosition);
                current.setChoose(true);
                selectPhotoList.add(current);
            }
            setResult(RESULT_OK, PickerContract.makeDataIntent(selectPhotoList, isSendOriginalImage));
            finish();
        } else if (v.getId() == R.id.picker_image_preview_orignal_image) {
            if (!isSendOriginalImage) {
                isSendOriginalImage = true;
                // 如果已选图片小于mutiSelectLimitSize，点击发送原图，自动选择当前页面
                int selectSize = selectPhotoList != null ? selectPhotoList.size() : 0;
                if (selectSize < mutiSelectLimitSize) {
                    PhotoInfo cur = photoLists.get(currentPosition);
                    if (!cur.isChoose()) {
                        cur.setChoose(true);
                        selectPhotoList.add(cur);
                        updateSelectBtnStatus();
                        updatePreviewSelectBtnStatus(true);
                    }
                }
            } else {
                isSendOriginalImage = false;
            }
            updateOriImageSizeTip(isSendOriginalImage);
        }
	}
	
	@Override
	public void onBackPressed(){
		setResult(RESULT_FROM_USER, PickerContract.makePreviewDataIntent(photoLists, selectPhotoList,
				isSendOriginalImage));
		finish();
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
	}

	@Override
	public void onPageSelected(int arg0) {
		setTitleIndex(arg0);
		updateTitleSelect(arg0);
	}
}
