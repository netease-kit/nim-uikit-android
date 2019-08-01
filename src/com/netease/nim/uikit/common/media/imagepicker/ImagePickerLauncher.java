package com.netease.nim.uikit.common.media.imagepicker;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import com.netease.nim.uikit.R;
import com.netease.nim.uikit.common.media.imagepicker.option.DefaultImagePickerOption;
import com.netease.nim.uikit.common.media.imagepicker.option.ImagePickerOption;
import com.netease.nim.uikit.common.media.imagepicker.ui.ImageGridActivity;
import com.netease.nim.uikit.common.media.imagepicker.ui.ImagePreviewActivity;
import com.netease.nim.uikit.common.media.imagepicker.ui.ImageTakeActivity;
import com.netease.nim.uikit.common.media.model.GLImage;
import com.netease.nim.uikit.common.ui.dialog.CustomAlertDialog;

import java.util.ArrayList;


/**
 *
 */

public class ImagePickerLauncher {

    private static final String TAG = ImagePickerLauncher.class.getName();


    /**
     * 打开图片选择器
     */
    public static void pickImage(final Activity context, final int requestCode, int titleResId) {
        if (context == null) {
            return;
        }
        CustomAlertDialog dialog = new CustomAlertDialog(context);
        dialog.setTitle(titleResId);
        dialog.addItem(context.getString(R.string.input_panel_take),
                       new CustomAlertDialog.onSeparateItemClickListener() {

                           @Override
                           public void onClick() {
                               takePhoto(context, requestCode);
                           }
                       });
        dialog.addItem(context.getString(R.string.choose_from_photo_album),
                       new CustomAlertDialog.onSeparateItemClickListener() {

                           @Override
                           public void onClick() {
                               selectImageFromAlbum(context, requestCode);
                           }
                       });
        dialog.show();
    }

    public static void selectImage(final Activity context, final int requestCode, @NonNull final ImagePickerOption option, int titleResId) {
        if (context == null) {
            return;
        }
        CustomAlertDialog dialog = new CustomAlertDialog(context);
        dialog.setTitle(titleResId);
        dialog.addItem(context.getString(R.string.input_panel_take),
                       new CustomAlertDialog.onSeparateItemClickListener() {

                           @Override
                           public void onClick() {
                               takePhoto(context, requestCode);
                           }
                       });
        dialog.addItem(context.getString(R.string.choose_from_photo_album),
                       new CustomAlertDialog.onSeparateItemClickListener() {

                           @Override
                           public void onClick() {
                               selectImage(context, requestCode, option);
                           }
                       });
        dialog.show();
    }

    private static void takePhoto(Activity activity, int requestCode) {
        ImagePickerOption option = DefaultImagePickerOption.getInstance();
        option.setPickType(ImagePickerOption.PickType.Image).setShowCamera(true).setMultiMode(false).setSelectMax(1)
              .setCrop(true);
        ImagePicker.getInstance().setOption(option);
        Intent takePictureIntent = new Intent(activity, ImageTakeActivity.class);
        activity.startActivityForResult(takePictureIntent, requestCode);
    }

    protected static void selectImageFromAlbum(Activity activity, int requestCode) {
        ImagePickerOption option = DefaultImagePickerOption.getInstance().setCrop(true);
        option.setPickType(ImagePickerOption.PickType.Image).setMultiMode(false).setSelectMax(1).setShowCamera(false);
        ImagePickerLauncher.selectImage(activity, requestCode, option);
    }

    public static void selectImage(Activity activity, int requestCode, @NonNull ImagePickerOption option) {
        ImagePicker.getInstance().setOption(option);
        Intent intent = new Intent(activity, ImageGridActivity.class);
        activity.startActivityForResult(intent, requestCode);
    }

    public static void selectImage(Fragment fragment, int requestCode, @NonNull ImagePickerOption option) {
        ImagePicker.getInstance().setOption(option);
        Intent intent = new Intent(fragment.getActivity(), ImageGridActivity.class);
        fragment.startActivityForResult(intent, requestCode);
    }

    /**
     * 拍照的方法
     */
    public static void takePicture(Activity activity, int requestCode, @NonNull ImagePickerOption option) {
        ImagePicker.getInstance().setOption(option);
        Intent takePictureIntent = new Intent(activity, ImageTakeActivity.class);
        activity.startActivityForResult(takePictureIntent, requestCode);
    }

    /**
     * 拍照的方法
     */
    public static void takePicture(Fragment fragment, int requestCode, @NonNull ImagePickerOption option) {
        ImagePicker.getInstance().setOption(option);
        Intent takePictureIntent = new Intent(fragment.getActivity(), ImageTakeActivity.class);
        fragment.startActivityForResult(takePictureIntent, requestCode);
    }

    public static void previewImage(Activity activity, ArrayList<GLImage> images, int requestCode) {
        Intent intent = new Intent(activity, ImagePreviewActivity.class);
        intent.putExtra(Constants.EXTRA_IMAGE_ITEMS, images);
        intent.putExtra(Constants.EXTRA_SELECTED_IMAGE_POSITION, 0);
        activity.startActivityForResult(intent, requestCode);
    }
}
