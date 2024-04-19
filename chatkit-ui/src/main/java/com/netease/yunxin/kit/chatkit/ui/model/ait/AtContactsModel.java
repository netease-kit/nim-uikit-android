// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.model.ait;

import static com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant.LIB_TAG;

import android.text.TextUtils;
import com.netease.yunxin.kit.alog.ALog;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 功能实现：@功能实体类，用来保存消息中的@信息 通过标记消息中@信息的起始位置和结束位置，来实现对@信息的高亮显示
 * 记录消息中所有@内容的位置，在消息发送的时候，将@信息转换为json数据，放在消息的扩展字段中发送 在消息接收的时候，解析json数据，将@信息解析出来，用来显示@信息的高亮显示
 */
public class AtContactsModel {
  // @所有人时，@的标识
  public static final String ACCOUNT_ALL = "ait_all";
  private static final String TAG = "AtContactsModel";

  // 消息中@信息的集合
  private final Map<String, AitBlock> aitBlocks = new HashMap<>();

  // 重置@信息
  public void reset() {
    aitBlocks.clear();
  }

  // 添加@信息
  public void addAtMember(String account, String name, int start) {
    AitBlock aitBlock = aitBlocks.get(account);
    if (aitBlock == null) {
      aitBlock = new AitBlock(name);
      aitBlocks.put(account, aitBlock);
    }
    int end = start + name.length();
    aitBlock.addSegment(start, end);
  }

  // 获取已经@的成员
  public List<String> getAtTeamMember() {
    List<String> teamMembers = new ArrayList<>();
    for (String account : aitBlocks.keySet()) {
      AitBlock block = aitBlocks.get(account);
      if (block != null && block.valid()) {
        teamMembers.add(account);
      }
    }
    return teamMembers;
  }

  // 获取@信息
  public AitBlock getAtBlock(String account) {
    return aitBlocks.get(account);
  }

  // 添加@信息
  public void addAtBlock(String account, AitBlock block) {
    if (!TextUtils.isEmpty(account) && block != null) {
      aitBlocks.put(account, block);
    }
  }

  // 获取@信息列表
  public List<AitBlock> getAtBlockList() {
    List<AitBlock> blockList = new ArrayList<>();
    for (String account : aitBlocks.keySet()) {
      AitBlock block = aitBlocks.get(account);
      if (block != null && block.valid()) {
        blockList.add(block);
      }
    }
    return blockList;
  }

  // 根据起始位置获取消息中@信息的
  public AitBlock.AitSegment findAtSegmentByEndPos(int start) {
    for (String account : aitBlocks.keySet()) {
      AitBlock block = aitBlocks.get(account);
      AitBlock.AitSegment segment = null;
      if (block != null) {
        segment = block.findLastSegmentByEnd(start);
      }
      if (segment != null) {
        return segment;
      }
    }
    return null;
  }

  // 插入文本内容是，修改@信息的位置
  public void onInsertText(int start, String changeText) {
    Iterator<String> iterator = aitBlocks.keySet().iterator();
    while (iterator.hasNext()) {
      String account = iterator.next();
      AitBlock block = aitBlocks.get(account);
      if (block != null) {
        block.moveRight(start, changeText);
        if (!block.valid()) {
          iterator.remove();
        }
      }
    }
  }

  // 删除文本内容时，修改@信息的位置
  public void onDeleteText(int start, int length) {
    Iterator<String> iterator = aitBlocks.keySet().iterator();
    while (iterator.hasNext()) {
      String account = iterator.next();
      AitBlock block = aitBlocks.get(account);
      if (block != null) {
        block.moveLeft(start, length);
        if (!block.valid()) {
          iterator.remove();
        }
      }
    }
  }

  // 将@信息转换为json数据，消息发送的时候需要将@信息转换为json数据放在消息的扩展字段中发送
  public JSONObject getBlockJson() {
    JSONObject data = new JSONObject();
    try {
      for (String account : aitBlocks.keySet()) {
        AitBlock block = aitBlocks.get(account);
        if (block != null) {
          data.put(account, block.toJson());
        }
      }
    } catch (JSONException e) {
      ALog.e(LIB_TAG, TAG, "getBlockJson error: " + e.getMessage());
    }

    return data;
  }

  // 从json数据中解析@信息
  public static AtContactsModel parseFromJson(JSONObject jsonObject) {
    if (jsonObject == null) {
      return null;
    }
    AtContactsModel model = new AtContactsModel();
    try {
      JSONArray jsonArray = jsonObject.names();
      for (int index = 0; jsonArray != null && index < jsonArray.length(); index++) {
        String name = jsonArray.getString(index);
        JSONObject block = jsonObject.getJSONObject(name);
        model.aitBlocks.put(name, AitBlock.parseFromJson(block));
      }
    } catch (JSONException e) {
      ALog.e(LIB_TAG, TAG, "parseFromJson error: " + e.getMessage());
    }
    return model;
  }
}
