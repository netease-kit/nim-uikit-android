// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.custom;

import androidx.annotation.Nullable;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.corekit.coexist.im2.IMKitClient;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

public class NERTCCallAttachment {

  public static final int NrtcCallStatusComplete = 1;
  public static final int NrtcCallStatusCanceled = 2;
  public static final int NrtcCallStatusRejected = 3;
  public static final int NrtcCallStatusTimeout = 4;
  public static final int NrtcCallStatusBusy = 5;

  // 1:音频 2:视频 类型通话
  public int callType = 0;
  // 话单类型
  public int callStatus = 0;

  public String msgId;

  public List<Duration> durationList = new ArrayList<>();

  public NERTCCallAttachment(String msgId, String data) {
    try {
      JSONObject jsonData = new JSONObject(data);
      parseData(jsonData);
      this.msgId = msgId;
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void parseData(@Nullable JSONObject data) {
    if (data == null) {
      return;
    }
    durationList.clear();
    try {
      // 音频/视频 类型通话
      callType = data.getInt("type");
      // 话单类型
      callStatus = data.getInt("status");
      JSONArray durationArray = data.getJSONArray("durations");
      for (int i = 0; i < durationArray.length(); i++) {
        JSONObject durationJson = durationArray.getJSONObject(i);
        NERTCCallAttachment.Duration duration = new NERTCCallAttachment.Duration();
        duration.accid = durationJson.getString("accid");
        duration.duration = durationJson.getInt("duration");
        durationList.add(duration);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public String getContent() {
    if (callType == 1) {
      return IMKitClient.getApplicationContext().getString(R.string.msg_type_rtc_audio);
    } else if (callType == 2) {
      return IMKitClient.getApplicationContext().getString(R.string.msg_type_rtc_video);
    } else {
      return IMKitClient.getApplicationContext().getString(R.string.msg_type_no_tips);
    }
  }

  public static class Duration {
    public int duration;
    public String accid;
  }
}
