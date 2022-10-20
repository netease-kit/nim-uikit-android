// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.qchatkit.ui;

import android.content.Context;
import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.netease.yunxin.kit.common.ui.photo.PhotoChoiceDialog;
import com.netease.yunxin.kit.common.ui.utils.CommonCallback;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import com.netease.yunxin.kit.common.utils.NetworkUtils;
import com.netease.yunxin.kit.corekit.im.provider.FetchCallback;
import com.netease.yunxin.kit.corekit.im.repo.CommonRepo;
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.im.utils.TransHelper;
import com.netease.yunxin.kit.corekit.model.ErrorMsg;
import com.netease.yunxin.kit.corekit.model.ResultInfo;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import com.netease.yunxin.kit.qchatkit.QChatService;
import java.io.File;

@Keep
public class QChatUIService extends QChatService {

  @NonNull
  @Override
  public String getServiceName() {
    return "QChatUIService";
  }

  @NonNull
  @Override
  public QChatService create(@NonNull Context context) {
    XKitRouter.registerRouter(
        RouterConstant.PATH_QCHAT_PICKING_PHOTO_ACTION,
        new XKitRouter.RouterValue(
            "",
            (value, params, observer) -> {
              int requestCode = 95201;
              TransHelper.launchTask(
                  context,
                  requestCode,
                  (activity, integer) -> {
                    PhotoChoiceDialog dialog = new PhotoChoiceDialog(activity);
                    dialog.show(
                        new CommonCallback<File>() {
                          @Override
                          public void onSuccess(@Nullable File param) {
                            if (NetworkUtils.isConnected()) {
                              CommonRepo.uploadImage(
                                  param,
                                  new FetchCallback<String>() {
                                    @Override
                                    public void onSuccess(@Nullable String urlParam) {
                                      if (observer != null) {
                                        observer.onResult(new ResultInfo<>(param));
                                      }
                                    }

                                    @Override
                                    public void onFailed(int code) {
                                      if (observer != null) {
                                        observer.onResult(
                                            new ResultInfo<>(null, false, new ErrorMsg(code)));
                                      }
                                    }

                                    @Override
                                    public void onException(@Nullable Throwable exception) {
                                      if (observer != null) {
                                        observer.onResult(
                                            new ResultInfo<>(
                                                null, false, new ErrorMsg(-1, "", exception)));
                                      }
                                    }
                                  });
                            } else {
                              ToastX.showShortToast(R.string.qchat_network_error_tip);
                            }
                          }

                          @Override
                          public void onFailed(int code) {
                            if (observer != null) {
                              observer.onResult(new ResultInfo<>(null, false, new ErrorMsg(code)));
                            }
                          }

                          @Override
                          public void onException(@Nullable Throwable exception) {
                            if (observer != null) {
                              observer.onResult(
                                  new ResultInfo<>(null, false, new ErrorMsg(-1, "", exception)));
                            }
                          }
                        });
                    dialog.setOnDismissListener(
                        dialog1 -> {
                          TransHelper.removeTransferKey(requestCode);
                          activity.finish();
                        });
                    return null;
                  },
                  intentResultInfo -> null);
              return false;
            }));
    return this;
  }
}
