// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.im.network;

import com.google.gson.annotations.SerializedName;

public class IMKItNetworkResponse<T> {

  @SerializedName("code")
  public int code;

  @SerializedName("data")
  public T data;

  @SerializedName("msg")
  public String msg;

  @SerializedName("requestId")
  public String requestId;

  @Override
  public String toString() {
    return "IMKItNetworkResponse{"
        + "code="
        + code
        + "msg="
        + msg
        + ", data="
        + data
        + ", requestId='"
        + requestId
        + '\''
        + '}';
  }
}
