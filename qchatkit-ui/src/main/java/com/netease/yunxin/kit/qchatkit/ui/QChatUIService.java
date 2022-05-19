/*
 * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.kit.qchatkit.ui;

import android.content.Context;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.netease.yunxin.kit.common.ui.CommonUIClient;
import com.netease.yunxin.kit.corekit.im.provider.FetchCallback;
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.im.utils.TransHelper;
import com.netease.yunxin.kit.corekit.model.ErrorMsg;
import com.netease.yunxin.kit.corekit.model.ResultInfo;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import com.netease.yunxin.kit.qchatkit.QChatService;
import com.netease.yunxin.kit.qchatkit.ui.common.photo.PhotoChoiceDialog;

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
        XKitRouter.registerRouter(RouterConstant.PATH_QCHAT_PICKING_PHOTO, new XKitRouter.RouterValue("", (value, params, observer) -> {
            int requestCode = 95201;
            TransHelper.launchTask(context, requestCode, (activity, integer) -> {
                PhotoChoiceDialog dialog = new PhotoChoiceDialog(activity);
                dialog.show(new FetchCallback<String>() {
                    @Override
                    public void onSuccess(@Nullable String param) {
                        if (observer != null) {
                            observer.onResult(new ResultInfo<>(param));
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
                            observer.onResult(new ResultInfo<>(null, false, new ErrorMsg(-1, "", exception)));
                        }
                    }
                });
                dialog.setOnDismissListener(dialog1 -> {
                    TransHelper.removeTransferKey(requestCode);
                    activity.finish();
                });
                return null;
            }, intentResultInfo -> null);
            return false;
        }));
        CommonUIClient.init(context);
        return this;
    }
}
