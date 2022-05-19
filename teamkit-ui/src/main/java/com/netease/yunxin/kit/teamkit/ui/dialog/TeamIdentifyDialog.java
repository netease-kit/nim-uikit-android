/*
 * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.kit.teamkit.ui.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.teamkit.ui.R;
import com.netease.yunxin.kit.teamkit.ui.databinding.TeamIdentifyChoiceDialogBinding;

public class TeamIdentifyDialog extends Dialog {
    private static final String TAG = "TeamIdentifyDialog";
    public static final int TYPE_TEAM_OWNER = 0;
    public static final int TYPE_TEAM_ALL_MEMBER = 1;

    private TeamIdentifyChoiceDialogBinding binding;
    private TeamChoiceListener callback;


    public TeamIdentifyDialog(@NonNull Activity activity) {
        super(activity, R.style.BottomDialogTheme);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        if (window != null) {
            WindowManager.LayoutParams wlp = window.getAttributes();
            wlp.gravity = Gravity.BOTTOM;
            wlp.width = WindowManager.LayoutParams.MATCH_PARENT;
            wlp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(wlp);
        }
        setContentView(binding.getRoot());
        setCanceledOnTouchOutside(true);
        setCancelable(true);

        setOnDismissListener(dialog -> callback = null);
    }

    /**
     * render page
     */
    private void renderRootView() {
        binding = TeamIdentifyChoiceDialogBinding.inflate(getLayoutInflater());
        binding.tvTeamAllMember.setOnClickListener(v -> {
            if (callback != null) {
                callback.onTypeChoice(TYPE_TEAM_ALL_MEMBER);
            }
            dismiss();
        });
        binding.tvTeamOwner.setOnClickListener(v -> {
            if (callback != null) {
                callback.onTypeChoice(TYPE_TEAM_OWNER);
            }
            dismiss();
        });
        binding.tvCancel.setOnClickListener(v -> dismiss());
    }

    public void show(TeamChoiceListener callback) {
        if (isShowing()) {
            return;
        }
        this.callback = callback;
        renderRootView();
        try {
            super.show();
        } catch (Throwable throwable) {
            ALog.e(TAG, "show TeamIdentifyDialog", throwable);
        }
    }

    public void dismiss() {
        if (!isShowing()) {
            return;
        }
        try {
            super.dismiss();
        } catch (Throwable throwable) {
            ALog.e(TAG, "dismiss TeamIdentifyDialog", throwable);
        }
    }

    public interface TeamChoiceListener {
        void onTypeChoice(int type);
    }
}
