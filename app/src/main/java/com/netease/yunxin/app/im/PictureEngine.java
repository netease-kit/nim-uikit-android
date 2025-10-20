// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.im;

import static android.app.Activity.RESULT_OK;

import android.content.Context;
import android.content.Intent;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import com.luck.picture.lib.basic.PictureSelector;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.entity.MediaExtraInfo;
import com.luck.picture.lib.utils.MediaUtils;
import com.netease.yunxin.kit.chatkit.listener.MediaChooseConfig;
import com.netease.yunxin.kit.chatkit.ui.IPictureChooseEngine;
import com.netease.yunxin.kit.common.picturechoose.ImageCallback;
import com.netease.yunxin.kit.common.picturechoose.LanguageType;
import com.netease.yunxin.kit.common.picturechoose.MediaInfo;
import com.netease.yunxin.kit.common.picturechoose.PictureChoose;
import com.netease.yunxin.kit.common.picturechoose.PictureChooseClient;
import com.netease.yunxin.kit.common.ui.utils.AppLanguageConfig;
import com.netease.yunxin.kit.common.utils.model.LocalFileInfo;
import com.netease.yunxin.kit.corekit.im2.IMKitClient;
import com.netease.yunxin.kit.corekit.im2.extend.FetchCallback;
import java.util.ArrayList;
import org.jetbrains.annotations.NotNull;

/** 图片选择器Engine, 用于选择图片和视频,使用common-picturechoose库实现，可以切换您需要的图片选择库 1. 支持选择图片 2. 支持选择视频 */
public class PictureEngine implements IPictureChooseEngine {
  @Override
  public void onStartPictureChoose(
      @NotNull Context context,
      @NotNull ActivityResultLauncher<Intent> activityResultLauncher,
      @NotNull MediaChooseConfig config,
      @NotNull FetchCallback<@NotNull ArrayList<@NotNull LocalFileInfo>> callback) {
    // 图片选择器
    String currentLanguage = AppLanguageConfig.getInstance().getAppLanguage(context);
    LanguageType languageType = LanguageType.ENGLISH;
    if (currentLanguage.equals(AppLanguageConfig.APP_LANG_CHINESE)) {
      languageType = LanguageType.CHINESE;
    }
    PictureChoose pictureChoose = PictureChooseClient.createPictureChoose(
            context,
            languageType,
            config.getOnlyImage(),
            config.getMaxSelectCount(),
            config.getOriginalImage());
    if (callback != null) {
      pictureChoose.forResult(
                              new ImageCallback() {
                                  @Override
                                  public void onResult(ArrayList<MediaInfo> result) {

                                      if (result.isEmpty()) {
                                          return;
                                      }
                                      ArrayList<LocalFileInfo> localFileInfos = new ArrayList<>();
                                      for (MediaInfo localMedia : result) {
                                          if (localMedia.getPath() == null) {
                                              continue;
                                          }
                                          localFileInfos.add(convertToLocalFileInfo(localMedia));
                                      }
                                      callback.onSuccess(localFileInfos);
                                  }

                                  @Override
                                  public void onCancel() {
                                      callback.onError(-1, "onCancel");
                                  }
                              });
    }else if (activityResultLauncher != null) {
      pictureChoose
              .forResult(activityResultLauncher);
    }
  }

  @Override
  public ArrayList<LocalFileInfo> onIntentResult(@NotNull ActivityResult result) {
    int resultCode = result.getResultCode();
    ArrayList<LocalFileInfo> localFileInfos = new ArrayList<>();
    if (resultCode == RESULT_OK) {
      ArrayList<LocalMedia> selectList = PictureSelector.obtainSelectorList(result.getData());
      for (LocalMedia localMedia : selectList) {
        if (localMedia.getPath() == null) {
          continue;
        }
        localFileInfos.add(convertToLocalFileInfo(localMedia));
      }
    }
    return localFileInfos;
  }

  private LocalFileInfo convertToLocalFileInfo(MediaInfo mediaInfo) {
    LocalFileInfo localFileInfo = new LocalFileInfo();
    localFileInfo.setPath(mediaInfo.getAvailablePath());
    localFileInfo.setHeight(mediaInfo.getHeight());
    localFileInfo.setWidth(mediaInfo.getWidth());
    localFileInfo.setName(mediaInfo.getFileName());
    localFileInfo.setMimeType(mediaInfo.getMimeType());
    localFileInfo.setDuration((int) mediaInfo.getDuration());
    return localFileInfo;
  }

  private LocalFileInfo convertToLocalFileInfo(LocalMedia localMedia) {
    LocalFileInfo localFileInfo = new LocalFileInfo();
    localFileInfo.setPath(localMedia.getAvailablePath());
    if (localMedia.getWidth() == 0 || localMedia.getHeight() == 0) {
      if (PictureMimeType.isHasImage(localMedia.getMimeType())) {
        MediaExtraInfo imageExtraInfo =
            MediaUtils.getImageSize(IMKitClient.getApplicationContext(), localMedia.getPath());
        localMedia.setWidth(imageExtraInfo.getWidth());
        localMedia.setHeight(imageExtraInfo.getHeight());
      } else if (PictureMimeType.isHasVideo(localMedia.getMimeType())) {
        MediaExtraInfo videoExtraInfo =
            MediaUtils.getVideoSize(IMKitClient.getApplicationContext(), localMedia.getPath());
        localMedia.setWidth(videoExtraInfo.getWidth());
        localMedia.setHeight(videoExtraInfo.getHeight());
      }
    } else {
      localFileInfo.setHeight(localMedia.getHeight());
      localFileInfo.setWidth(localMedia.getWidth());
    }
    localFileInfo.setName(localMedia.getFileName());
    localFileInfo.setMimeType(localMedia.getMimeType());
    localFileInfo.setDuration((int) localMedia.getDuration());
    return localFileInfo;
  }
}
