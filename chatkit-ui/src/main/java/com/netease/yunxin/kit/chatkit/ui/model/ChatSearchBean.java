// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.model;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import androidx.annotation.Nullable;
import com.netease.nimlib.sdk.search.model.RecordHitInfo;
import com.netease.nimlib.sdk.v2.message.V2NIMMessage;
import com.netease.yunxin.kit.chatkit.ui.common.ChatUserCache;
import com.netease.yunxin.kit.chatkit.ui.common.MessageHelper;
import com.netease.yunxin.kit.common.ui.viewholder.BaseBean;
import com.netease.yunxin.kit.corekit.im2.model.V2UserInfo;
import com.netease.yunxin.kit.corekit.im2.utils.RouterConstant;
import java.util.ArrayList;
import java.util.List;

/** history message search bean used to locate the message */
public class ChatSearchBean extends BaseBean {
  V2NIMMessage msgRecord;

  String keyword;

  public ChatSearchBean(V2NIMMessage record, String keyword) {
    this.msgRecord = record;
    this.keyword = keyword;
    this.paramKey = RouterConstant.KEY_MESSAGE;
    this.param = getMessage();
    this.router = RouterConstant.PATH_CHAT_TEAM_PAGE;
  }

  public String getNickName() {
    return MessageHelper.getChatSearchMessageUserName(msgRecord);
  }

  public String getAccount() {
    if (msgRecord != null) {
      return msgRecord.getSenderId();
    }
    return null;
  }

  public long getTime() {
    if (msgRecord != null) {
      return msgRecord.getCreateTime();
    }
    return 0;
  }

  public String getAvatar() {
    V2UserInfo userInfo = ChatUserCache.getInstance().getUserInfo(msgRecord.getSenderId());
    if (userInfo != null) {
      return userInfo.getAvatar();
    }
    return null;
  }

  public V2NIMMessage getMessage() {
    return msgRecord;
  }

  /**
   * get the keyword
   *
   * @return the keyword
   */
  public String getKeyword() {
    return keyword;
  }

  public SpannableString getSpannableString(int color) {
    if (msgRecord != null) {
      String content = msgRecord.getText();
      SpannableString spannable = new SpannableString(content);
      List<RecordHitInfo> hitInfoList = new ArrayList<>();
      int startIndex = 0;
      while (startIndex < content.length()) {
        int foundIndex = content.indexOf(keyword, startIndex);
        if (foundIndex == -1) {
          break;
        } else {
          hitInfoList.add(new RecordHitInfo(foundIndex, foundIndex + keyword.length() - 1));
          startIndex = foundIndex + 1;
        }
      }
      if (!hitInfoList.isEmpty()) {
        for (RecordHitInfo hitInfo : hitInfoList) {
          spannable.setSpan(
              new ForegroundColorSpan(color),
              hitInfo.start,
              hitInfo.end + 1,
              Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        }
      }
      return spannable;
    }
    return null;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (obj instanceof ChatSearchBean) {
      ChatSearchBean bean = (ChatSearchBean) obj;
      return bean.msgRecord.equals(msgRecord);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return msgRecord.hashCode();
  }
}
