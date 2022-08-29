// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.teamkit.ui;

import static com.netease.yunxin.kit.corekit.im.utils.RouterConstant.PATH_TEAM_SETTING_PAGE;

import android.content.Context;
import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.netease.yunxin.kit.corekit.XKitService;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import com.netease.yunxin.kit.corekit.startup.Initializer;
import com.netease.yunxin.kit.teamkit.TeamService;
import com.netease.yunxin.kit.teamkit.ui.activity.TeamSettingActivity;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/** launch service when app start the TeamUIService will be created it need to config in manifest */
@Keep
public class TeamUIService extends TeamService {
  @NonNull
  @Override
  public String getServiceName() {
    return "TeamUIService";
  }

  @NonNull
  @Override
  public String getVersionName() {
    return BuildConfig.versionName;
  }

  @Nullable
  @Override
  public Object onMethodCall(@NonNull String method, @Nullable Map<String, ?> param) {
    return null;
  }

  @Override
  public XKitService create(@NonNull Context context) {
    XKitRouter.registerRouter(PATH_TEAM_SETTING_PAGE, TeamSettingActivity.class);
    return this;
  }

  @NonNull
  @Override
  public List<Class<? extends Initializer<?>>> dependencies() {
    return Collections.emptyList();
  }
}
