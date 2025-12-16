// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui;

import android.content.Context;
import android.content.Intent;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import com.netease.yunxin.kit.chatkit.listener.MediaChooseConfig;
import com.netease.yunxin.kit.common.utils.model.LocalFileInfo;
import com.netease.yunxin.kit.corekit.coexist.im2.extend.FetchCallback;
import java.util.ArrayList;
import org.jetbrains.annotations.NotNull;

public interface IPictureChooseEngine {

  public void onStartPictureChoose(
      @NotNull Context context,
      @NotNull ActivityResultLauncher<Intent> activityResultLauncher,
      @NotNull MediaChooseConfig config,
      @NotNull FetchCallback<@NotNull ArrayList<@NotNull LocalFileInfo>> callback);

  public ArrayList<LocalFileInfo> onIntentResult(@NotNull ActivityResult result);
}
