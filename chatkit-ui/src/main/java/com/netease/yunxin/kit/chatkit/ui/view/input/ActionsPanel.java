/*
 * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.kit.chatkit.ui.view.input;

import android.content.pm.PackageManager;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.viewpager2.widget.ViewPager2;

import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.view.interfaces.IMessageProxy;
import com.netease.yunxin.kit.common.ui.action.ActionItem;
import com.netease.yunxin.kit.common.ui.dialog.BottomChoiceDialog;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import com.netease.yunxin.kit.common.utils.AppInfo;

import java.util.ArrayList;

/**
 * more action panel in input view
 */
public class ActionsPanel implements ActionsPanelAdapter.OnActionItemClick {
    private ViewPager2 viewPager2;
    private ActionsPanelAdapter adapter;
    private IMessageProxy messageProxy;

    public void init(ViewPager2 viewPager2, ArrayList<ActionItem> actionItems, IMessageProxy proxy) {
        this.viewPager2 = viewPager2;
        this.adapter = new ActionsPanelAdapter(viewPager2.getContext(), actionItems);
        this.adapter.setOnActionItemClick(this);
        this.viewPager2.setAdapter(adapter);
        this.messageProxy = proxy;
    }

    @Override
    public void onClick(ActionItem item) {
        if (TextUtils.equals(item.getType(), ActionConstants.ACTION_MORE_SHOOT)) {
            BottomChoiceDialog dialog = new BottomChoiceDialog(viewPager2.getContext(),
                    ActionFactory.assembleTakeShootActions());
            dialog.setOnChoiceListener(new BottomChoiceDialog.OnChoiceListener() {
                @Override
                public void onChoice(@NonNull String type) {
                    if (!AppInfo.applicationContext.getPackageManager()
                            .hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
                        ToastX.showShortToast(R.string.chat_message_camera_unavailable);
                        return;
                    }
                    switch (type) {
                        case ActionConstants.ACTION_TYPE_TAKE_PHOTO:
                            messageProxy.takePicture();
                            break;
                        case ActionConstants.ACTION_TYPE_TAKE_VIDEO:
                            messageProxy.captureVideo();
                            break;
                        default:
                            break;
                    }
                }

                @Override
                public void onCancel() {

                }
            });
            dialog.show();
        }
    }
}
