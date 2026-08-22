// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.conversationkit.ui.common;

import android.text.TextUtils;
import com.netease.yunxin.kit.common.utils.SPUtils;
import com.netease.yunxin.kit.conversationkit.ui.model.ConversationGroupLocalConfig;
import com.netease.yunxin.kit.conversationkit.ui.model.ConversationGroupType;
import com.netease.yunxin.kit.corekit.im2.IMKitClient;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public final class ConversationGroupLocalConfigHelper {

  public interface OnConfigChangedListener {
    void onConfigChanged();
  }

  private static final String SP_NAME = "conversation_group_config";
  private static final String KEY_PREFIX = "conversation_group_config_";
  private static final String KEY_GROUP_ID = "groupId";
  private static final String KEY_TYPE = "type";
  private static final String KEY_VISIBLE = "visible";
  private static final String KEY_SORT_ORDER = "sortOrder";
  private static final Set<OnConfigChangedListener> CONFIG_CHANGED_LISTENERS =
      new CopyOnWriteArraySet<>();

  private ConversationGroupLocalConfigHelper() {}

  public static Map<String, ConversationGroupLocalConfig> getConfigMap() {
    Map<String, ConversationGroupLocalConfig> result = new HashMap<>();
    for (ConversationGroupLocalConfig config : getConfigList()) {
      result.put(config.getGroupId(), config);
    }
    return result;
  }

  public static List<ConversationGroupLocalConfig> getConfigList() {
    List<ConversationGroupLocalConfig> result = new ArrayList<>();
    String json = SPUtils.getInstance(SP_NAME).getString(getAccountKey(), "");
    if (TextUtils.isEmpty(json)) {
      return result;
    }
    try {
      JSONArray array = new JSONArray(json);
      for (int index = 0; index < array.length(); index++) {
        JSONObject object = array.optJSONObject(index);
        if (object == null) {
          continue;
        }
        String groupId = object.optString(KEY_GROUP_ID);
        String typeValue = object.optString(KEY_TYPE);
        if (TextUtils.isEmpty(groupId) || TextUtils.isEmpty(typeValue)) {
          continue;
        }
        ConversationGroupType type = ConversationGroupType.valueOf(typeValue);
        result.add(
            new ConversationGroupLocalConfig(
                groupId, type, object.optBoolean(KEY_VISIBLE), object.optInt(KEY_SORT_ORDER)));
      }
    } catch (JSONException | IllegalArgumentException ignored) {
      return new ArrayList<>();
    }
    return result;
  }

  public static void saveConfigList(List<ConversationGroupLocalConfig> configs) {
    JSONArray array = new JSONArray();
    if (configs != null) {
      for (ConversationGroupLocalConfig config : configs) {
        JSONObject object = new JSONObject();
        try {
          object.put(KEY_GROUP_ID, config.getGroupId());
          object.put(KEY_TYPE, config.getType().name());
          object.put(KEY_VISIBLE, config.isVisible());
          object.put(KEY_SORT_ORDER, config.getSortOrder());
          array.put(object);
        } catch (JSONException ignored) {
          // Ignore invalid item and keep remaining config entries.
        }
      }
    }
    SPUtils.getInstance(SP_NAME).put(getAccountKey(), array.toString());
    notifyConfigChanged();
  }

  public static void addConfigChangedListener(OnConfigChangedListener listener) {
    if (listener != null) {
      CONFIG_CHANGED_LISTENERS.add(listener);
    }
  }

  public static void removeConfigChangedListener(OnConfigChangedListener listener) {
    if (listener != null) {
      CONFIG_CHANGED_LISTENERS.remove(listener);
    }
  }

  public static void saveConfig(ConversationGroupLocalConfig config) {
    if (config == null || TextUtils.isEmpty(config.getGroupId())) {
      return;
    }
    Map<String, ConversationGroupLocalConfig> map = getConfigMap();
    map.put(config.getGroupId(), config);
    saveConfigList(new ArrayList<>(map.values()));
  }

  public static void removeConfig(String groupId) {
    if (TextUtils.isEmpty(groupId)) {
      return;
    }
    Map<String, ConversationGroupLocalConfig> map = getConfigMap();
    map.remove(groupId);
    saveConfigList(new ArrayList<>(map.values()));
  }

  private static void notifyConfigChanged() {
    for (OnConfigChangedListener listener : CONFIG_CHANGED_LISTENERS) {
      listener.onConfigChanged();
    }
  }

  private static String getAccountKey() {
    String account = IMKitClient.account();
    if (TextUtils.isEmpty(account)) {
      account = "anonymous";
    }
    return KEY_PREFIX + account;
  }
}
