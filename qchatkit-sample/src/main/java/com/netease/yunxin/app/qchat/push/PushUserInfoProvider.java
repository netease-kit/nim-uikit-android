// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.qchat.push;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallbackWrapper;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.nos.NosService;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.nimlib.sdk.uinfo.UserInfoProvider;
import com.netease.nimlib.sdk.uinfo.model.UserInfo;
import com.netease.yunxin.app.qchat.R;
import com.netease.yunxin.kit.corekit.im.provider.TeamProvider;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class PushUserInfoProvider implements UserInfoProvider {

  private Context context;

  public PushUserInfoProvider(Context context) {
    this.context = context;
  }

  @Override
  public UserInfo getUserInfo(String account) {
    return com.netease.yunxin.kit.corekit.im.provider.UserInfoProvider.getUserInfoLocal(account);
  }

  @Override
  public String getDisplayNameForMessageNotifier(
      String account, String sessionId, SessionTypeEnum sessionType) {
    return null;
  }

  @Override
  public Bitmap getAvatarForMessageNotifier(SessionTypeEnum sessionType, String sessionId) {
    /*
     * get from cache
     */
    Bitmap bm = null;
    int defResId = R.mipmap.ic_notification_avatar_default;
    CountDownLatch countDownLatch = new CountDownLatch(1);
    final String[] originUrl = new String[1];
    if (SessionTypeEnum.P2P == sessionType) {
      UserInfo user = getUserInfo(sessionId);
      originUrl[0] = user != null ? user.getAvatar() : null;
    } else if (SessionTypeEnum.Team == sessionType) {
      Team team = TeamProvider.INSTANCE.getTeamById(sessionId);
      originUrl[0] = team != null ? team.getIcon() : null;
    }
    NIMClient.getService(NosService.class)
        .getOriginUrlFromShortUrl(originUrl[0])
        .setCallback(
            new RequestCallbackWrapper<String>() {

              @Override
              public void onResult(int code, String result, Throwable exception) {
                originUrl[0] = result;
                countDownLatch.countDown();
              }
            });
    try {
      countDownLatch.await(200, TimeUnit.MILLISECONDS);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    if (!TextUtils.isEmpty(originUrl[0])) {
      bm = getNotificationBitmapFromCache(originUrl[0]);
    }
    if (bm == null) {
      if (SessionTypeEnum.Team == sessionType || SessionTypeEnum.SUPER_TEAM == sessionType) {
        defResId = R.mipmap.ic_notification_avatar_group;
      }
      Drawable drawable = context.getResources().getDrawable(defResId);
      if (drawable instanceof BitmapDrawable) {
        bm = ((BitmapDrawable) drawable).getBitmap();
      }
    }
    return bm;
  }

  @Override
  public String getDisplayTitleForMessageNotifier(IMMessage message) {
    return null;
  }

  public Bitmap getNotificationBitmapFromCache(String url) {
    if (TextUtils.isEmpty(url)) {
      return null;
    }
    final int imageSize = (int) context.getResources().getDimension(R.dimen.dimen_48_dp);
    Bitmap cachedBitmap = null;
    try {
      cachedBitmap =
          Glide.with(context)
              .asBitmap()
              .load(url)
              .apply(new RequestOptions().centerCrop().override(imageSize, imageSize))
              .submit()
              .get(200, TimeUnit.MILLISECONDS) // 最大等待200ms
      ;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return cachedBitmap;
  }
}
