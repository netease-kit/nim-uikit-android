// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.im.utils;

import android.text.TextUtils;
import com.netease.nimlib.sdk.v2.conversation.enums.V2NIMConversationType;
import com.netease.nimlib.sdk.v2.message.enums.V2NIMMessageSendingState;
import com.netease.nimlib.sdk.v2.message.enums.V2NIMMessageType;
import com.netease.yunxin.app.im.R;
import com.netease.yunxin.app.im.network.AIHelperAnswer;
import com.netease.yunxin.app.im.network.AIHelperAnswerItem;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.model.IMMessageInfo;
import com.netease.yunxin.kit.chatkit.repo.ChatRepo;
import com.netease.yunxin.kit.chatkit.ui.common.MessageHelper;
import com.netease.yunxin.kit.chatkit.ui.normal.view.AIHelperView;
import com.netease.yunxin.kit.chatkit.utils.ConversationIdUtils;
import com.netease.yunxin.kit.corekit.im2.IMKitClient;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MessageUtils {

  private static final int MAX_COUNT = 5;
  private static final int[] aiStyleList =
      new int[] {
        R.drawable.app_bg_corner_pink,
        R.drawable.app_bg_corner_pink2,
        R.drawable.app_bg_corner_blue,
      };

  private static final int[] aiStyleTextColorList =
      new int[] {R.color.color_F159A2, R.color.color_E75257, R.color.color_598CF1};

  public static JSONObject generateAIHelperInfo(List<IMMessageInfo> msgList) {
    String queryStr = "";
    String hostName = IMKitClient.currentUser().getName();
    String chatName =
        MessageHelper.getChatMessageUserNameByAccount(
            ConversationIdUtils.conversationTargetId(ChatRepo.getConversationId()),
            V2NIMConversationType.V2NIM_CONVERSATION_TYPE_P2P);
    if (msgList == null || msgList.isEmpty()) {
      return new JSONObject();
    }
    int count = 0;
    List<JSONObject> historyList = new ArrayList<>();
    for (int index = msgList.size() - 1; index >= 0; index--) {
      if (count > MAX_COUNT && !TextUtils.isEmpty(queryStr)) {
        break;
      }
      IMMessageInfo info = msgList.get(index);
      if (info.getMessage().getMessageType() == V2NIMMessageType.V2NIM_MESSAGE_TYPE_TEXT
          && info.getSendingState()
              == V2NIMMessageSendingState.V2NIM_MESSAGE_SENDING_STATE_SUCCEEDED) {

        if (count < MAX_COUNT) {
          JSONObject jsonObject = new JSONObject();
          try {
            jsonObject.put("sender_id", info.getSenderId());
            jsonObject.put("text", info.getMessage().getText());
          } catch (JSONException e) {
            throw new RuntimeException(e);
          }
          historyList.add(jsonObject);
          count++;
        }
        if (TextUtils.isEmpty(queryStr) && !info.getMessage().isSelf()) {
          queryStr = info.getMessage().getText();
        }
      }
    }
    JSONArray historyArray = new JSONArray();
    for (int index = historyList.size() - 1; index >= 0; index--) {
      historyArray.put(historyList.get(index));
    }
    ALog.d(
        "IMAPP",
        "MessageUtils,generateAIHelperInfo queryStr=" + queryStr + "," + "history=" + historyArray);
    JSONObject jsonObject = new JSONObject();
    String conversationId = ChatRepo.getConversationId();
    try {
      jsonObject.put("receiver_last_message", queryStr);
      jsonObject.put("history", historyArray);
      jsonObject.put("sender_id", ConversationIdUtils.conversationTargetId(conversationId));
      jsonObject.put("receiver_id", IMKitClient.account());
    } catch (JSONException exception) {

    }
    return jsonObject;
  }

  public static List<AIHelperView.AIHelperItem> convertToAIHelperItem(AIHelperAnswer data) {
    List<AIHelperView.AIHelperItem> itemList = new ArrayList<>();
    if (data != null && data.items != null) {
      for (int index = 0; index < data.items.size(); index++) {
        AIHelperAnswerItem answerItem = data.items.get(index);
        AIHelperView.AIHelperItem helperItem =
            new AIHelperView.AIHelperItem(answerItem.answer, answerItem.styleName);
        int styleIndex = index % 3;
        helperItem.tagBgDrawable = aiStyleList[styleIndex];
        helperItem.tagTextColor = aiStyleTextColorList[styleIndex];
        itemList.add(helperItem);
      }
    }
    return itemList;
  }
}
