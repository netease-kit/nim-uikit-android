// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.model;

import android.text.TextUtils;
import com.netease.nimlib.sdk.v2.message.V2NIMCollection;
import com.netease.nimlib.sdk.v2.message.V2NIMMessage;
import com.netease.nimlib.sdk.v2.message.V2NIMMessageConverter;
import com.netease.nimlib.sdk.v2.message.enums.V2NIMMessageType;
import com.netease.yunxin.kit.chatkit.ChatCustomMsgFactory;
import com.netease.yunxin.kit.chatkit.model.CustomAttachment;
import com.netease.yunxin.kit.chatkit.model.IMMessageInfo;
import java.io.Serializable;
import org.json.JSONException;
import org.json.JSONObject;

/** 收藏列表数据 包含收藏信息，以及解析转换之后的消息信息 */
public class CollectionBean implements Serializable {

  // 收藏类型，消息类型+1000
  public int type;
  // 收藏消息的发送者名称
  public String senderName;
  // 收藏消息的发送者头像
  public String senderAvatar;
  // 收藏消息的会话名称
  public String conversationName;

  // 收藏信息
  public V2NIMCollection collectionData;

  // 消息信息
  protected IMMessageInfo messageData;

  // 需要下载的消息，通过该参数来传递消息下载进度
  protected float loadProgress;

  public CollectionBean(V2NIMCollection collection) {
    this.collectionData = collection;
    String collectionData = collection.getCollectionData();
    if (!TextUtils.isEmpty(collectionData)) {
      try {
        JSONObject jsonData = new JSONObject(collectionData);
        type = jsonData.optInt("type");
        senderName = jsonData.optString("senderName", "");
        senderAvatar = jsonData.optString("senderAvatar", "");
        conversationName = jsonData.optString("conversationName", "");
        String data = jsonData.optString("data", "");
        if (!TextUtils.isEmpty(data)) {
          V2NIMMessage message = V2NIMMessageConverter.messageDeserialization(data);
          messageData = new IMMessageInfo(message);
          if (message != null
              && message.getMessageType() == V2NIMMessageType.V2NIM_MESSAGE_TYPE_CUSTOM) {
            messageData.setAttachment(
                ChatCustomMsgFactory.INSTANCE
                    .getCustomParse()
                    .parse(message.getAttachment().getRaw()));
          }
        }

      } catch (JSONException e) {
        e.printStackTrace();
      }
    }
  }

  public int getMessageType() {
    return messageData != null ? messageData.getMessage().getMessageType().getValue() : -1;
  }

  public int getCollectionType() {
    return type;
  }

  public String getSenderAvatar() {
    return senderAvatar;
  }

  public String getConversationName() {
    return conversationName;
  }

  public long getCreateTime() {
    return collectionData.getCreateTime();
  }

  public boolean isSame(CollectionBean data) {
    return data != null
        && data.collectionData != null
        && collectionData != null
        && collectionData.getCollectionId().equals(data.collectionData.getCollectionId());
  }

  public String getCollectionId() {
    return collectionData.getCollectionId();
  }

  public String getUserName() {
    return senderName;
  }

  public V2NIMMessage getMessageData() {
    return messageData != null ? messageData.getMessage() : null;
  }

  public IMMessageInfo getMessageInfo() {
    return messageData;
  }

  public CustomAttachment getCustomAttachment() {
    return messageData != null && messageData.getAttachment() != null
        ? messageData.getAttachment()
        : null;
  }

  public float getLoadProgress() {
    return loadProgress;
  }

  public void setLoadProgress(float loadProgress) {
    this.loadProgress = loadProgress;
  }
}
