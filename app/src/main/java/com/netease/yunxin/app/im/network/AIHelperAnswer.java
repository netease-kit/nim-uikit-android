// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.im.network;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class AIHelperAnswer {
  @SerializedName("items")
  public List<AIHelperAnswerItem> items;
}
