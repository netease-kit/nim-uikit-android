// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.common.ui.photo;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import androidx.annotation.Nullable;
import com.netease.yunxin.kit.common.ui.photo.crop.CropImage;
import com.netease.yunxin.kit.common.ui.photo.crop.CropImageView;
import com.netease.yunxin.kit.common.ui.utils.CommonCallback;
import com.netease.yunxin.kit.common.utils.CommonFileProvider;
import java.io.File;
import java.io.IOException;

public final class PhotoPicker {
  private static final String TAG = "PhotoPicker";

  private PhotoPicker() {}

  private static final class Holder {
    private static final PhotoPicker INSTANCE = new PhotoPicker();
  }

  public static PhotoPicker getInstance() {
    return Holder.INSTANCE;
  }

  public void takePhotoCorpAndUpload(Context context, CommonCallback<File> callback) {
    takePhoto(
        context,
        new CommonCallback<File>() {
          @Override
          public void onSuccess(@Nullable File param) {
            cropPhoto(
                context,
                param,
                new CommonCallback<File>() {
                  @Override
                  public void onSuccess(@Nullable File param) {
                    callback.onSuccess(param);
                  }

                  @Override
                  public void onFailed(int code) {
                    if (callback != null) {
                      callback.onFailed(code);
                    }
                  }

                  @Override
                  public void onException(@Nullable Throwable exception) {
                    if (callback != null) {
                      callback.onException(exception);
                    }
                  }
                });
          }

          @Override
          public void onFailed(int code) {
            if (callback != null) {
              callback.onFailed(code);
            }
          }

          @Override
          public void onException(@Nullable Throwable exception) {
            if (callback != null) {
              callback.onException(exception);
            }
          }
        });
  }

  public void getAPhotoFromAlbumCropAndUpload(Context context, CommonCallback<File> callback) {
    int requestId = 9529;
    File file = getTempFile(context);
    TransHelper.launchTask(
        context,
        requestId,
        (activity, integer) -> {
          CropImage.activity()
              .setCropShape(CropImageView.CropShape.RECTANGLE)
              .setGuidelines(CropImageView.Guidelines.OFF)
              .setAspectRatio(1, 1)
              .setOutputUri(Uri.fromFile(file))
              .setRequestedSize(400, 400)
              .start(activity);
          return null;
        },
        intentResultInfo -> {
          inform(
              intentResultInfo,
              value -> file,
              file,
              new CommonCallback<File>() {
                @Override
                public void onSuccess(@Nullable File param) {
                  callback.onSuccess(param);
                }

                @Override
                public void onFailed(int code) {
                  if (callback != null) {
                    callback.onFailed(code);
                  }
                }

                @Override
                public void onException(@Nullable Throwable exception) {
                  if (callback != null) {
                    callback.onException(exception);
                  }
                }
              });
          return null;
        });
  }

  public void takePhoto(Context context, CommonCallback<File> callback) {
    // The request id of taking photos.
    int requestId = 9527;
    File file = getTempFile(context);
    TransHelper.launchTask(
        context,
        requestId,
        (activity, integer) -> {
          Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
          Uri uri;
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            uri = CommonFileProvider.Companion.getUriForFile(context, file);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
          } else {
            uri = Uri.fromFile(file);
          }

          intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
          activity.startActivityForResult(intent, integer);
          return null;
        },
        intentResultInfo -> {
          inform(intentResultInfo, value -> file, file, callback);
          return null;
        });
  }

  public void cropPhoto(Context context, File inputFile, CommonCallback<File> callback) {
    cropPhoto(context, CommonFileProvider.Companion.getUriForFile(context, inputFile), callback);
  }

  public void cropPhoto(Context context, Uri inputFileUri, CommonCallback<File> callback) {
    // The request id of cropping photos.
    int requestId = 9528;
    File file = getTempFile(context);
    TransHelper.launchTask(
        context,
        requestId,
        (activity, integer) -> {
          CropImage.activity(inputFileUri)
              .setCropShape(CropImageView.CropShape.RECTANGLE)
              .setGuidelines(CropImageView.Guidelines.OFF)
              .setAspectRatio(1, 1)
              .setOutputUri(Uri.fromFile(file))
              .setRequestedSize(400, 400)
              .start(activity);
          return null;
        },
        intentResultInfo -> {
          inform(intentResultInfo, value -> file, file, callback);
          return null;
        });
  }

  private <T, R> void inform(
      ResultInfo<T> result,
      ConvertType<T, R> converter,
      R defaultValue,
      CommonCallback<R> callback) {
    if (callback == null) {
      return;
    }
    if (result == null) {
      callback.onException(new PhotoEmptyResultException("no result."));
      return;
    }

    if (result.getSuccess() == Boolean.TRUE) {
      callback.onSuccess(
          result.getValue() == null ? defaultValue : converter.convert(result.getValue()));
      return;
    }
    ErrorMsg msg = result.getMsg();
    if (msg == null) {
      callback.onException(new PhotoEmptyResultException("no error result."));
      return;
    }
    if (msg.getException() != null) {
      callback.onException(msg.getException());
      return;
    }
    callback.onFailed(msg.getCode());
  }

  private File getTempFile(Context context) {
    File parentFileDir;
    parentFileDir =
        new File(
            context.getExternalFilesDir(Environment.DIRECTORY_DCIM)
                + File.separator
                + "SyncStateContract.Constants.FILE_DIR");
    if (!parentFileDir.exists()) {
      parentFileDir.mkdirs();
    }
    File file = new File(parentFileDir, System.currentTimeMillis() + ".jpg");
    if (!file.exists()) {
      try {
        file.createNewFile();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return file;
  }

  private interface ConvertType<T, R> {
    R convert(T value);
  }

  public class PhotoEmptyResultException extends Exception {
    public PhotoEmptyResultException(String message) {
      super(message);
    }
  }
}
