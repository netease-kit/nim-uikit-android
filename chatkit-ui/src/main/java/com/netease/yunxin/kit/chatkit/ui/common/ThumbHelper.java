// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.common;

import android.text.TextUtils;
import android.util.DisplayMetrics;
import com.netease.yunxin.kit.corekit.coexist.im2.IMKitClient;

public class ThumbHelper {

  /**
   * 生成图片缩略图url
   *
   * @param url 图片url
   * @param originW 图片原始宽度
   * @param originH 图片原始高度
   * @return 缩略图url
   */
  public static String makeImageThumbUrl(String url, int originW, int originH) {
    Thumb thumb = Thumb.Internal;
    if (originH > 0 && originW > 0) {
      int ration = (originW > originH ? originW / originH : originH / originW);
      thumb = ration > 4 ? Thumb.External : Thumb.Internal;
    }

    int width = IMKitClient.getOptions().thumbnailSize;
    if (width <= 0) {
      DisplayMetrics dm = IMKitClient.getApplicationContext().getResources().getDisplayMetrics();
      width = Math.min(dm.widthPixels, dm.heightPixels) / 2;
    }
    return appendQueryParams(url, toImageThumbParams(thumb, width, width));
  }

  public static String appendQueryParams(String url, String params) {
    if (url == null) {
      return null;
    }
    String connectChar = url.contains("?") ? "&" : "?";

    return url + connectChar + params;
  }

  private static String toImageThumbParams(Thumb thumb, int width, int height) {
    if (!checkImageThumb(thumb, width, height)) {
      throw new IllegalArgumentException("width=" + width + ", height=" + height);
    }

    StringBuilder sb = new StringBuilder();

    sb.append("thumbnail=");
    sb.append(width);
    sb.append(toImageThumbMethod(thumb));
    sb.append(height);

    sb.append("&imageView");

    String gifThumb = gifThumbParams();
    if (!TextUtils.isEmpty(gifThumb)) {
      sb.append(gifThumb);
    }
    return sb.toString();
  }

  private static boolean checkImageThumb(Thumb thumb, int width, int height) {
    // not allow negative
    if (width < 0 || height < 0) {
      return false;
    }

    switch (thumb) {
      case Internal:
        // not allow both zero
        return width > 0 || height > 0;
      case Crop:
      case External:
        // not allow either zero
        return width > 0 && height > 0;
    }

    return false;
  }

  private static String toImageThumbMethod(Thumb thumb) {
    switch (thumb) {
      case Internal:
        return "x";
      case Crop:
        return "y";
      case External:
        return "z";
    }

    throw new IllegalArgumentException("thumb: " + thumb);
  }

  private static String gifThumbParams() {
    return IMKitClient.getOptions().animatedImageThumbnailEnabled ? "&tostatic=0" : null;
  }

  enum Thumb {
    Internal,
    Crop,
    External,
  }

  /**
   * 生成视频缩略图url
   *
   * @param url 视频url
   * @return 缩略图url
   */
  public static String makeVideoThumbUrl(String url) {
    return appendQueryParams(url, toVideoThumbParams());
  }

  /** 生成视频缩略图url,取第一帧 */
  private static String toVideoThumbParams() {
    return "vframe=" + 1;
  }
}
