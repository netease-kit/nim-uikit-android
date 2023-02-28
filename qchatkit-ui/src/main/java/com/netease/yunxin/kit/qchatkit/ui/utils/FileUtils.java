// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.qchatkit.ui.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.qchatkit.ui.model.QChatConstant;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileUtils {

  private static final String TAG = "FileUtils";

  // 获取文件扩展名
  public static String getExtensionName(String filename) {
    if ((filename != null) && (filename.length() > 0)) {
      int dot = filename.lastIndexOf('.');
      if ((dot > -1) && (dot < (filename.length() - 1))) {
        return filename.substring(dot + 1);
      }
    }
    return "";
  }

  public static File getTempFile(Context context, String originPath) {
    File parentFileDir;
    parentFileDir =
        new File(
            context.getExternalFilesDir(Environment.DIRECTORY_DCIM)
                + File.separator
                + QChatConstant.FILE_DIR);
    if (!parentFileDir.exists()) {
      ALog.d(TAG, "mkdirs result is " + parentFileDir.mkdirs());
    }
    String suffix = ".jpg";
    if (!TextUtils.isEmpty(originPath)) {
      String temp = getExtensionName(originPath);
      if (!TextUtils.isEmpty(temp)) {
        suffix = "." + temp;
      }
    }
    File file = new File(parentFileDir, System.currentTimeMillis() + suffix);
    if (!file.exists()) {
      try {
        ALog.e(TAG, "createNewFile result is " + file.createNewFile());
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return file;
  }

  public static void copy(Context context, Uri srcPath, String dstPath) {
    if (srcPath == null || TextUtils.isEmpty(dstPath)) {
      return;
    }
    ContentResolver resolver = context.getContentResolver();

    InputStream fcin = null;
    FileOutputStream fcout = null;
    try {
      fcin = resolver.openInputStream(srcPath);
      fcout = new FileOutputStream(create(dstPath));
      byte[] tmpBuffer = new byte[4096];
      while (fcin.read(tmpBuffer) != -1) {
        fcout.write(tmpBuffer);
      }

    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      try {
        if (fcin != null) {
          fcin.close();
        }
        if (fcout != null) {
          fcout.close();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  public static File create(String filePath) {
    if (TextUtils.isEmpty(filePath)) {
      return null;
    }

    File f = new File(filePath);
    if (!f.getParentFile().exists()) { // 如果不存在上级文件夹
      f.getParentFile().mkdirs();
    }
    try {
      f.createNewFile();
      return f;
    } catch (IOException e) {
      if (f != null && f.exists()) {
        f.delete();
      }
      return null;
    }
  }
}
