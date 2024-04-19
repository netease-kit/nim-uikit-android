// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.custom;

import static com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant.KEY_MULTI_TRANSMIT_ABSTRACTS;
import static com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant.KEY_MULTI_TRANSMIT_DEPTH;
import static com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant.KEY_MULTI_TRANSMIT_MD5;
import static com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant.KEY_MULTI_TRANSMIT_SEND_CONTENT;
import static com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant.KEY_MULTI_TRANSMIT_SEND_ID;
import static com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant.KEY_MULTI_TRANSMIT_SEND_NICK;
import static com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant.KEY_MULTI_TRANSMIT_SESSION_ID;
import static com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant.KEY_MULTI_TRANSMIT_SESSION_NAME;
import static com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant.KEY_MULTI_TRANSMIT_URL;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.netease.yunxin.kit.chatkit.model.CustomAttachment;
import com.netease.yunxin.kit.chatkit.ui.ChatMessageType;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.corekit.im2.IMKitClient;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * 合并转发消息自定义类型，101 { "type":101, "data":{ "sessionID":"sessionID",//被合并消息的sessionId
 * "sessionName":"sessionName",//被合并消息的会话名称 "url":"url", "md5":"md5", "depth":1,//深度
 * "abstracts":{//摘要，默认三条 { "senderNick":"senderNick",//消息展示的nick，只取fromNick，没有就accId
 * "content":"content",//内容，不是Text的显示缩略 "userAccId":"userAccid" } } } }
 */
public class MultiForwardAttachment extends CustomAttachment {

  public String sessionID;
  public String sessionName;
  public String url;
  public String md5;
  public int depth;
  public List<Abstracts> abstractsList;

  public MultiForwardAttachment() {
    super(ChatMessageType.MULTI_FORWARD_ATTACHMENT);
  }

  @Override
  protected void parseData(@Nullable JSONObject data) {

    if (data == null) {
      return;
    }
    try {

      sessionID = data.optString(KEY_MULTI_TRANSMIT_SESSION_ID);
      sessionName = data.optString(KEY_MULTI_TRANSMIT_SESSION_NAME);
      url = data.optString(KEY_MULTI_TRANSMIT_URL);
      md5 = data.optString(KEY_MULTI_TRANSMIT_MD5);
      depth = data.optInt(KEY_MULTI_TRANSMIT_DEPTH);
      abstractsList = new ArrayList<>();
      JSONArray abArray = data.getJSONArray(KEY_MULTI_TRANSMIT_ABSTRACTS);
      if (abArray != null && abArray.length() > 0) {

        for (int i = 0; i < abArray.length(); i++) {
          JSONObject jsonObject = abArray.getJSONObject(i);
          if (jsonObject == null) {
            continue;
          }
          String userAccId = jsonObject.optString(KEY_MULTI_TRANSMIT_SEND_ID, "");
          String senderNick = jsonObject.optString(KEY_MULTI_TRANSMIT_SEND_NICK, userAccId);
          String content = jsonObject.optString(KEY_MULTI_TRANSMIT_SEND_CONTENT, "");
          abstractsList.add(new Abstracts(senderNick, content, userAccId));
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Nullable
  @Override
  protected JSONObject packData() {

    JSONObject jsonObject = new JSONObject();
    try {
      jsonObject.put(KEY_MULTI_TRANSMIT_SESSION_ID, sessionID);
      jsonObject.put(KEY_MULTI_TRANSMIT_SESSION_NAME, sessionName);
      jsonObject.put(KEY_MULTI_TRANSMIT_URL, url);
      jsonObject.put(KEY_MULTI_TRANSMIT_MD5, md5);
      jsonObject.put(KEY_MULTI_TRANSMIT_DEPTH, depth);
      JSONArray jsonArray = new JSONArray();
      for (Abstracts abstracts : abstractsList) {
        jsonArray.put(abstracts.toJson());
      }
      jsonObject.put(KEY_MULTI_TRANSMIT_ABSTRACTS, jsonArray);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return jsonObject;
  }

  @NonNull
  @Override
  public String toJsonStr() {
    try {
      JSONObject map = new JSONObject();
      map.put("type", ChatMessageType.MULTI_FORWARD_ATTACHMENT);
      map.put("data", packData());
      return map.toString();
    } catch (Exception e) {
      e.printStackTrace();
      return "";
    }
  }

  public static class Abstracts implements Serializable {
    public String senderNick;
    public String content;
    public String userAccId;

    public Abstracts(String senderNick, String content, String userAccId) {
      this.senderNick = senderNick;
      this.content = content;
      this.userAccId = userAccId;
    }

    public JSONObject toJson() {
      JSONObject jsonObject = new JSONObject();
      try {
        jsonObject.put(KEY_MULTI_TRANSMIT_SEND_NICK, senderNick);
        jsonObject.put(KEY_MULTI_TRANSMIT_SEND_CONTENT, content);
        jsonObject.put(KEY_MULTI_TRANSMIT_SEND_ID, userAccId);
      } catch (Exception e) {
        e.printStackTrace();
      }
      return jsonObject;
    }
  }

  @Override
  public String getContent() {
    return IMKitClient.getApplicationContext().getString(R.string.msg_type_multi_tips);
  }
}
