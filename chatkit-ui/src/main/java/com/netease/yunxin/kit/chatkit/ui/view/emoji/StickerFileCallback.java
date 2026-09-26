package com.netease.yunxin.kit.chatkit.ui.view.emoji;

import androidx.annotation.NonNull;
import java.io.File;

public interface StickerFileCallback {
  void onSuccess(@NonNull File file, int width, int height);

  void onError(@NonNull Throwable error);
}
