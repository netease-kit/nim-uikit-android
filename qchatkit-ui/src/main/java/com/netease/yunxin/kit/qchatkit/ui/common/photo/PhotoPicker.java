// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.qchatkit.ui.common.photo;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.common.utils.NetworkUtils;
import com.netease.yunxin.kit.corekit.im.provider.FetchCallback;
import com.netease.yunxin.kit.corekit.im.utils.TransHelper;
import com.netease.yunxin.kit.corekit.model.ErrorMsg;
import com.netease.yunxin.kit.corekit.model.ResultInfo;
import com.netease.yunxin.kit.qchatkit.repo.QChatServerRepo;
import com.netease.yunxin.kit.qchatkit.ui.R;
import com.netease.yunxin.kit.qchatkit.ui.common.photo.crop.CropImage;
import com.netease.yunxin.kit.qchatkit.ui.common.photo.crop.CropImageView;
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

  public void takePhotoCorpAndUpload(Context context, FetchCallback<String> callback) {
    takePhoto(
        context,
        new FetchCallback<File>() {
          @Override
          public void onSuccess(@Nullable File param) {
            cropPhoto(
                context,
                param,
                new FetchCallback<File>() {
                  @Override
                  public void onSuccess(@Nullable File param) {
                    NetworkUtils.isConnectedToastAndRun(
                        context,
                        () -> uploadPhoto(param, callback),
                        () -> {
                          callback.onException(new IllegalStateException("Network error."));
                          Toast.makeText(context, R.string.qchat_network_error, Toast.LENGTH_SHORT)
                              .show();
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

  public void getAPhotoFromAlbumCropAndUpload(Context context, FetchCallback<String> callback) {
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
              new FetchCallback<File>() {
                @Override
                public void onSuccess(@Nullable File param) {
                  NetworkUtils.isConnectedToastAndRun(
                      context,
                      () -> uploadPhoto(param, callback),
                      () -> {
                        callback.onException(new IllegalStateException("Network error."));
                        Toast.makeText(context, R.string.qchat_network_error, Toast.LENGTH_SHORT)
                            .show();
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
          return null;
        });
  }

  public void takePhoto(Context context, FetchCallback<File> callback) {
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
            uri =
                FileProvider.getUriForFile(
                    context, activity.getPackageName() + ".FileProvider", file);
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

  public void cropPhoto(Context context, File inputFile, FetchCallback<File> callback) {
    cropPhoto(
        context,
        FileProvider.getUriForFile(context, context.getPackageName() + ".FileProvider", inputFile),
        callback);
  }

  public void cropPhoto(Context context, Uri inputFileUri, FetchCallback<File> callback) {
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

  public void uploadPhoto(File file, FetchCallback<String> callback) {
    QChatServerRepo.uploadServerIcon(file, callback);
  }

  private <T, R> void inform(
      ResultInfo<T> result,
      ConvertType<T, R> converter,
      R defaultValue,
      FetchCallback<R> callback) {
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
                + Constants.FILE_DIR);
    if (!parentFileDir.exists()) {
      ALog.d(TAG, "mkdirs result is " + parentFileDir.mkdirs());
    }
    File file = new File(parentFileDir, System.currentTimeMillis() + ".jpg");
    if (!file.exists()) {
      try {
        ALog.e(TAG, "createNewFile result is " + file.createNewFile());
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
