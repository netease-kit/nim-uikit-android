// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.teamkit.ui.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import androidx.annotation.NonNull;
import com.netease.yunxin.kit.alog.ALog;
import java.util.Objects;

public abstract class BaseTeamIdentifyDialog extends Dialog {
  private static final String TAG = "TeamIdentifyDialog";
  public static final int TYPE_TEAM_OWNER = 0;
  public static final int TYPE_TEAM_ALL_MEMBER = 1;

  protected TeamChoiceListener callback;
  private final View rootView;
  protected View tvTeamAllMember;
  protected View tvTeamOwner;
  protected View tvCancel;

  public BaseTeamIdentifyDialog(@NonNull Activity activity, int themeResId) {
    super(activity, themeResId);
    rootView = initViewAndGetRootView();
    checkViews();
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Window window = getWindow();
    if (window != null) {
      WindowManager.LayoutParams wlp = window.getAttributes();
      wlp.gravity = Gravity.BOTTOM;
      wlp.flags |= WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
      wlp.width = WindowManager.LayoutParams.MATCH_PARENT;
      wlp.height = WindowManager.LayoutParams.WRAP_CONTENT;
      window.setAttributes(wlp);
    }
    setContentView(rootView);
    setCanceledOnTouchOutside(true);
    setCancelable(true);

    setOnDismissListener(dialog -> callback = null);
  }

  protected abstract View initViewAndGetRootView();

  protected void checkViews() {
    Objects.requireNonNull(rootView);
    Objects.requireNonNull(tvTeamAllMember);
    Objects.requireNonNull(tvTeamOwner);
    Objects.requireNonNull(tvCancel);
  }

  /** render page */
  private void renderRootView() {
    tvTeamAllMember.setOnClickListener(
        v -> {
          if (callback != null) {
            callback.onTypeChoice(TYPE_TEAM_ALL_MEMBER);
          }
          dismiss();
        });
    tvTeamOwner.setOnClickListener(
        v -> {
          if (callback != null) {
            callback.onTypeChoice(TYPE_TEAM_OWNER);
          }
          dismiss();
        });
    tvCancel.setOnClickListener(v -> dismiss());
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
