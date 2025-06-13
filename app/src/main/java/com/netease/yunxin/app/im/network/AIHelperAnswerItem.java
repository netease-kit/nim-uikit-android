// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.im.network;

import com.google.gson.annotations.SerializedName;

public class AIHelperAnswerItem {
  @SerializedName("style")
  public String style;

  @SerializedName("style_name")
  public String styleName;

  @SerializedName("answer")
  public String answer;
}
