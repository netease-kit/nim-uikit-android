// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.im.network;

import com.netease.yunxin.app.im.AppConfig;
import java.util.Map;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.HeaderMap;
import retrofit2.http.POST;

public interface AIChatHelperAPI {

  @POST(AppConfig.AIHelperUrl)
  Call<IMKItNetworkResponse<AIHelperAnswer>> requestAIChatHelper(
      @HeaderMap Map<String, String> header, @Body RequestBody body);
}
