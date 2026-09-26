

// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.common.ui.photo.crop;

import static com.netease.yunxin.kit.common.ui.photo.crop.CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import com.netease.yunxin.kit.common.ui.activities.BaseActivity;
import com.netease.yunxin.kit.common.ui.databinding.CommonCropImageActivityBinding;
import java.io.File;
import java.io.IOException;

/**
 * Built-in activity for image cropping.<br>
 * Use {@link CropImage#activity(Uri)} to create a builder to start this activity.
 */
public class CropImageActivity extends BaseActivity
    implements CropImageView.OnSetImageUriCompleteListener,
        CropImageView.OnCropImageCompleteListener {

  /** The crop image view library widget used in the activity */
  private CropImageView mCropImageView;

  /** Persist URI image to crop URI if specific permissions are required */
  private Uri mCropImageUri;

  /** the options that were set for the crop image */
  private CropImageOptions mOptions;

  @Override
  @SuppressLint("NewApi")
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    CommonCropImageActivityBinding binding =
        CommonCropImageActivityBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());

    mCropImageView = binding.cropImageView;

    Bundle bundle = getIntent().getBundleExtra(CropImage.CROP_IMAGE_EXTRA_BUNDLE);
    mCropImageUri = bundle.getParcelable(CropImage.CROP_IMAGE_EXTRA_SOURCE);
    mOptions = bundle.getParcelable(CropImage.CROP_IMAGE_EXTRA_OPTIONS);
    if (savedInstanceState == null) {
      if (mCropImageUri == null || mCropImageUri.equals(Uri.EMPTY)) {
        Intent intent =
            new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(intent, PICK_IMAGE_CHOOSER_REQUEST_CODE);
      } else {
        // no permissions required or already grunted, can start crop image activity
        mCropImageView.setImageUriAsync(mCropImageUri);
      }
    }
    binding.tvDone.setOnClickListener(v -> cropImage());
    binding.ivBack.setOnClickListener(v -> finish());
  }

  @Override
  protected void onStart() {
    super.onStart();
    mCropImageView.setOnSetImageUriCompleteListener(this);
    mCropImageView.setOnCropImageCompleteListener(this);
  }

  @Override
  protected void onStop() {
    super.onStop();
    mCropImageView.setOnSetImageUriCompleteListener(null);
    mCropImageView.setOnCropImageCompleteListener(null);
  }

  @Override
  public void onBackPressed() {
    super.onBackPressed();
    setResultCancel();
  }

  @Override
  @SuppressLint("NewApi")
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    // handle result of pick image chooser
    if (requestCode == PICK_IMAGE_CHOOSER_REQUEST_CODE) {
      if (resultCode == Activity.RESULT_CANCELED) {
        // User cancelled the picker. We don't have anything to crop
        setResultCancel();
      }

      if (resultCode == Activity.RESULT_OK) {
        mCropImageUri = CropImage.getPickImageResultUri(this, data);

        // For API >= 23 we need to check specifically that we have permissions to read external
        // storage.
        if (CropImage.isReadExternalStoragePermissionsRequired(this, mCropImageUri)) {
          // request permissions and handle the result in onRequestPermissionsResult()
          requestPermissions(
              new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},
              CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE);
        } else {
          // no permissions required or already grunted, can start crop image activity
          mCropImageView.setImageUriAsync(mCropImageUri);
        }
      }
    }
  }

  @Override
  public void onRequestPermissionsResult(
      int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    if (requestCode == CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE) {
      if (mCropImageUri != null
          && grantResults.length > 0
          && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        // required permissions granted, start crop image activity
        mCropImageView.setImageUriAsync(mCropImageUri);
      } else {
        setResultCancel();
      }
    }
  }

  @Override
  public void onSetImageUriComplete(CropImageView view, Uri uri, Exception error) {
    if (error == null) {
      if (mOptions.initialCropWindowRectangle != null) {
        mCropImageView.setCropRect(mOptions.initialCropWindowRectangle);
      }
    } else {
      setResult(null, error, 1);
    }
  }

  @Override
  public void onCropImageComplete(CropImageView view, CropImageView.CropResult result) {
    setResult(result.getUri(), result.getError(), result.getSampleSize());
  }

  /** Execute crop image and save the result tou output uri. */
  protected void cropImage() {
    if (mOptions.noOutputImage) {
      setResult(null, null, 1);
    } else {
      Uri outputUri = getOutputUri();
      mCropImageView.saveCroppedImageAsync(
          outputUri,
          mOptions.outputCompressFormat,
          mOptions.outputCompressQuality,
          mOptions.outputRequestWidth,
          mOptions.outputRequestHeight,
          mOptions.outputRequestSizeOptions);
    }
  }

  /**
   * Get Android uri to save the cropped image into.<br>
   * Use the given in options or create a temp file.
   */
  protected Uri getOutputUri() {
    Uri outputUri = mOptions.outputUri;
    if (outputUri == null || outputUri.equals(Uri.EMPTY)) {
      try {
        String ext =
            mOptions.outputCompressFormat == Bitmap.CompressFormat.JPEG
                ? ".jpg"
                : mOptions.outputCompressFormat == Bitmap.CompressFormat.PNG ? ".png" : ".webp";
        outputUri = Uri.fromFile(File.createTempFile("cropped", ext, getCacheDir()));
      } catch (IOException e) {
        throw new RuntimeException("Failed to create temp file for output image", e);
      }
    }
    return outputUri;
  }

  /** Result with cropped image data or error if failed. */
  protected void setResult(Uri uri, Exception error, int sampleSize) {
    int resultCode = error == null ? RESULT_OK : CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE;
    setResult(resultCode, getResultIntent(uri, error, sampleSize));
    finish();
  }

  /** Cancel of cropping activity. */
  protected void setResultCancel() {
    setResult(RESULT_CANCELED);
    finish();
  }

  /** Get intent instance to be used for the result of this activity. */
  protected Intent getResultIntent(Uri uri, Exception error, int sampleSize) {
    CropImage.ActivityResult result =
        new CropImage.ActivityResult(
            mCropImageView.getImageUri(),
            uri,
            error,
            mCropImageView.getCropPoints(),
            mCropImageView.getCropRect(),
            mCropImageView.getRotatedDegrees(),
            mCropImageView.getWholeImageRect(),
            sampleSize);
    Intent intent = new Intent();
    intent.putExtras(getIntent());
    intent.putExtra(CropImage.CROP_IMAGE_EXTRA_RESULT, result);
    return intent;
  }
}
