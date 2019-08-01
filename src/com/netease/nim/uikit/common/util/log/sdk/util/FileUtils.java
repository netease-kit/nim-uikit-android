package com.netease.nim.uikit.common.util.log.sdk.util;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * Created by huangjun on 2017/3/7.
 */

public class FileUtils {

    private static final String TAG = "FileUtils";

    public static File getFile(final String path) {
        try {
            File file = new File(path);
            File dir = file.getParentFile();
            if (dir == null) {
                Log.e(TAG, "file's parent dir is null, path=" + file.getCanonicalPath());
                return null;
            }

            if (!dir.exists()) {
                if (dir.getParentFile().exists()) {
                    dir.mkdir(); // dir父目录存在用mkDir
                } else {
                    dir.mkdirs(); // dir父目录不存在用mkDirs
                }
            }

            if (!file.exists() && !file.createNewFile()) {
                Log.e(TAG, "can not create dest file, path=" + path);
                return null;
            }
            return file;
        } catch (Throwable e) {
            Log.e(TAG, "create dest file error, path=" + path, e);
        }

        return null;
    }

    public static boolean appendFile(final String message, final String path) {
        if (TextUtils.isEmpty(message)) {
            return false;
        }

        if (TextUtils.isEmpty(path)) {
            return false;
        }

        boolean written = false;
        try {
            BufferedWriter fw = new BufferedWriter(new FileWriter(path, true));
            fw.write(message);
            fw.flush();
            fw.close();

            written = true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return written;
    }

    public static boolean appendFile(final byte[] message, final String path) {
        if (message == null || message.length <= 0) {
            return false;
        }

        if (TextUtils.isEmpty(path)) {
            return false;
        }

        boolean written = false;
        try {
            FileOutputStream fw = new FileOutputStream(path, true);
            fw.write(message);
            fw.close();

            written = true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return written;
    }

    public static synchronized void shrink(final String logPath, final int maxLength, final int baseLength) {
        File file = new File(logPath);
        if (file.length() < maxLength) {
            return;
        } else if (file.length() > Integer.MAX_VALUE) {
            file.delete();
            return;
        }

        File out = new File(logPath + "_tmp");
        FileInputStream fis = null;
        FileOutputStream fos = null;
        try {
            fis = new FileInputStream(file);
            fos = new FileOutputStream(out);
            FileChannel input = fis.getChannel();

            input.position(file.length() - baseLength);
            FileChannel output = fos.getChannel();
            output.transferFrom(fis.getChannel(), 0, baseLength);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close(fis);
            close(fos);
        }

        if (out.exists()) {
            if (file.delete()) {
                out.renameTo(file);
            }
        }
    }

    public static void close(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getFilePath(final String dirPath, final String fileName) {
        File dir = new File(dirPath);

        if (!dir.exists()) {
            if (dir.getParentFile().exists()) {
                dir.mkdir(); // dir父目录存在用mkDir
            } else {
                dir.mkdirs(); // dir父目录不存在用mkDirs
            }
        }

        return dirPath + File.separator + fileName;
    }

    // 是否包含扩展名
    public static boolean hasExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        return ((dot > -1) && (dot < (filename.length() - 1)));
    }

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

    // 获取文件名
    public static String getFileNameFromPath(String filepath) {
        if ((filepath != null) && (filepath.length() > 0)) {
            int sep = filepath.lastIndexOf('/');
            if ((sep > -1) && (sep < filepath.length() - 1)) {
                return filepath.substring(sep + 1);
            }
        }
        return filepath;
    }

    // 获取不带扩展名的文件名
    public static String getFileNameNoEx(String filename) {
        if ((filename != null) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            if ((dot > -1) && (dot < (filename.length()))) {
                return filename.substring(0, dot);
            }
        }
        return filename;
    }

    public static String getExternalPackageDirectory(Context context) {
        String externalPath = Environment.getExternalStorageDirectory().getPath();
        return externalPath + File.separator + context.getPackageName();
    }
}
