// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.custom;

import static com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant.KEY_RICH_TEXT_BODY;
import static com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant.KEY_RICH_TEXT_TITLE;

import android.text.TextUtils;
import androidx.annotation.Nullable;
import com.netease.yunxin.kit.chatkit.ui.ChatMessageType;
import com.netease.yunxin.kit.corekit.im.custom.CustomAttachment;
import org.json.JSONObject;

/** 富文本消息自定义类型，102 { "type"：102, "data":{ "title":"我是标题XXX", "body":"我是内容XXX" } } */
public class RichTextAttachment extends CustomAttachment {

  public String title;
  public String body;

  public RichTextAttachment() {
    super(ChatMessageType.RICH_TEXT_ATTACHMENT);
  }

  @Override
  protected void parseData(@Nullable JSONObject data) {

    if (data == null) {
      return;
    }
    try {
      title = data.optString(KEY_RICH_TEXT_TITLE, "");
      body = data.optString(KEY_RICH_TEXT_BODY, "");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Nullable
  @Override
  protected JSONObject packData() {

    JSONObject jsonObject = new JSONObject();
    try {
      jsonObject.put(KEY_RICH_TEXT_TITLE, title);
      jsonObject.put(KEY_RICH_TEXT_BODY, body);

    } catch (Exception e) {
      e.printStackTrace();
    }
    return jsonObject;
  }

  @Nullable
  @Override
  public String getContent() {
    return !TextUtils.isEmpty(title) ? title : body;
  }
}
