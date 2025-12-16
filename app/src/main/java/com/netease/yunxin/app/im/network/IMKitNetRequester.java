// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.im.network;

import android.text.TextUtils;
import com.netease.yunxin.app.im.utils.Constant;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.corekit.coexist.im2.extend.FetchCallback;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class IMKitNetRequester {
  private final Map<String, String> headerMap = new HashMap<>();
  private Retrofit retrofit;
  private String appKey;
  private AIChatHelperAPI aichatHelperAPI;

  private IMKitNetRequester() {}

  private static final class Holder {
    private static final IMKitNetRequester INSTANCE = new IMKitNetRequester();
  }

  public static IMKitNetRequester getInstance() {
    return Holder.INSTANCE;
  }

  public void setup(String url, String appKey, String accountId, String signature) {
    if (TextUtils.isEmpty(url)) {
      return;
    }
    this.appKey = appKey;
    headerMap.put("appKey", appKey);
    headerMap.put("accountId", accountId);
    headerMap.put("accessToken", signature);
    headerMap.put("Content-Type", "application/json;charset=utf-8");

    OkHttpClient httpClient =
        new OkHttpClient.Builder()
            .connectTimeout(10L, TimeUnit.SECONDS)
            .readTimeout(5L, TimeUnit.SECONDS)
            .writeTimeout(5L, TimeUnit.SECONDS)
            .build();
    retrofit =
        new Retrofit.Builder()
            .baseUrl(url)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build();
    aichatHelperAPI = retrofit.create(AIChatHelperAPI.class);
  }

  public void requestAIChatHelperTest(JSONObject query, FetchCallback<AIHelperAnswer> callback) {
    if (callback == null || retrofit == null) {
      return;
    }
    JSONArray styleArray = new JSONArray();
    try {
      styleArray.put("warm");
      styleArray.put("hot");
      styleArray.put("smart");
      query.put("style_list", styleArray);
    } catch (JSONException e) {
      e.printStackTrace();
    }

    String bodyContent = query.toString().replace("\\", "").replace("\"[", "[").replace("]\"", "]");
    RequestBody body = RequestBody.create(MediaType.parse("application/json"), bodyContent);
    Call<IMKItNetworkResponse<AIHelperAnswer>> call =
        aichatHelperAPI.requestAIChatHelper(headerMap, body);
    call.enqueue(
        new Callback<IMKItNetworkResponse<AIHelperAnswer>>() {
          @Override
          public void onResponse(
              Call<IMKItNetworkResponse<AIHelperAnswer>> call,
              Response<IMKItNetworkResponse<AIHelperAnswer>> response) {
            if (response.isSuccessful()) {
              IMKItNetworkResponse<AIHelperAnswer> body = response.body();
              if (body != null && body.data != null) {
                callback.onSuccess(body.data);
              } else {
                callback.onSuccess(null);
              }
            } else {
              callback.onError(response.code(), response.message());
            }
          }

          @Override
          public void onFailure(Call<IMKItNetworkResponse<AIHelperAnswer>> call, Throwable t) {
            callback.onError(-1, t.getMessage());
          }
        });
  }

  public void requestAIChatHelper(JSONObject query, FetchCallback<AIHelperAnswer> callback) {
    if (callback == null || retrofit == null) {
      return;
    }
    JSONArray styleArray = new JSONArray();
    try {
      styleArray.put("warm");
      styleArray.put("hot");
      styleArray.put("smart");
      query.put("style_list", styleArray);
    } catch (JSONException e) {
      e.printStackTrace();
    }

    String bodyContent = query.toString().replace("\\", "").replace("\"[", "[").replace("]\"", "]");
    ALog.d(Constant.PROJECT_TAG, "AI helper requestAIChatHelper,body:", bodyContent);
    RequestBody body = RequestBody.create(MediaType.parse("application/json"), bodyContent);
    retrofit
        .create(AIChatHelperAPI.class)
        .requestAIChatHelper(headerMap, body)
        .enqueue(
            new Callback<IMKItNetworkResponse<AIHelperAnswer>>() {
              @Override
              public void onResponse(
                  Call<IMKItNetworkResponse<AIHelperAnswer>> call,
                  Response<IMKItNetworkResponse<AIHelperAnswer>> response) {
                if (response.isSuccessful()) {
                  IMKItNetworkResponse<AIHelperAnswer> body = response.body();
                  if (body != null && body.data != null) {
                    callback.onSuccess(body.data);
                  } else {
                    callback.onSuccess(null);
                  }
                } else {
                  callback.onError(response.code(), response.message());
                }
              }

              @Override
              public void onFailure(Call<IMKItNetworkResponse<AIHelperAnswer>> call, Throwable t) {
                callback.onError(-1, t.getMessage());
              }
            });
  }
}
