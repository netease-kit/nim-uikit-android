// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.im.main.mine.setting;

import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.netease.nimlib.push.net.lbs.IPVersion;
import com.netease.nimlib.push.packet.asymmetric.AsymmetricType;
import com.netease.nimlib.push.packet.symmetry.SymmetryType;
import com.netease.nimlib.sdk.NimHandshakeType;
import com.netease.nimlib.sdk.ServerAddresses;
import java.util.HashSet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ServerConfigUtils {

  /// BASIC
  private static final String KEY_APP_KEY = "appkey";

  private static final String KEY_MODULE = "module";

  private static final String KEY_VERSION = "version";

  private static final String KEY_HAND_SHAKE_TYPE = "hand_shake_type";

  /// MAIN LINK
  private static final String KEY_LBS = "lbs";

  private static final String KEY_LINK = "link";

  /// NOS UPLOAD
  private static final String KEY_HTTPS_ENABLED = "https_enabled";

  private static final String KEY_NOS_LBS = "nos_lbs";

  private static final String KEY_NOS_UPLOADER = "nos_uploader";

  private static final String KEY_NOS_UPLOADER_HOST = "nos_uploader_host";

  /// NOS DOWNLOAD
  private static final String KEY_NOS_DOWNLOADER = "nos_downloader";

  private static final String KEY_NOS_ACCELERATE = "nos_accelerate";

  private static final String KEY_NOS_ACCELERATE_HOST = "nos_accelerate_host";

  private static final String KEY_NOS_ACCELERATE_HOST_LIST = "nos_accelerate_host_list";

  private static final String KEY_NOS_CDN_ENABLE = "nos_cdn_enable";

  /// SERVER
  private static final String KEY_NT_SERVER = "nt_server";

  // 握手协议(国密)
  private static final String KEY_DEDICATED_CLUSTE_FLAG = "dedicated_cluste_flag";
  private static final String KEY_NEGO_KEY_NECA = "nego_key_neca";
  private static final String KEY_NEGO_KEY_ENCA_KEY_VERSION = "nego_key_enca_key_version";
  private static final String KEY_NEGO_KEY_ENCA_KEY_PARTA = "nego_key_enca_key_parta";
  private static final String KEY_NEGO_KEY_ENCA_KEY_PARTB = "nego_key_enca_key_partb";
  private static final String KEY_COMM_ENCA = "comm_enca";

  // IM IPv6
  private static final String KEY_LINK_IPV6 = "link_ipv6";
  private static final String KEY_IP_PROTOCOL_VERSION = "ip_protocol_version";
  private static final String KEY_PROBE_IPV4_URL = "probe_ipv4_url";
  private static final String KEY_PROBE_IPV6_URL = "probe_ipv6_url";

  private static final String SHARE_NAME = "nim_demo_private_config";

  private static final String KEY_CONFIG_ENABLE = "private_config_enable";

  private static final String KEY_CONFIG_JSON = "private_config_json";

  private static final String KEY_CHAT_ROOM_LIST_URL = "chatroomDemoListUrl";

  private static final String BUCKET_NAME_PLACE_HOLDER = "{bucket}";

  private static final String OBJECT_PLACE_HOLDER = "{object}";

  private static final String CONFIG_URL = "config_private_url";

  private static final String YSF_DEFALUT_URL_LABEL = "ysf_defalut_url_label";

  private static final String YSF_DA_URL_LABEL = "ysf_da_url_label";

  private static String appKey;

  public static ServerAddresses parseAddresses(String serverConfig) {
    if (TextUtils.isEmpty(serverConfig)) {
      return null;
    }
    try {
      JSONObject jsonObject = new JSONObject(serverConfig);
      return parseAddresses(jsonObject);
    } catch (JSONException e) {
      e.printStackTrace();
    }
    return null;
  }

  public static ServerAddresses parseAddresses(JSONObject jsonObject) {
    if (jsonObject == null) {
      return null;
    }
    ServerAddresses addresses = new ServerAddresses();
    addresses.handshakeType =
        NimHandshakeType.value(
            jsonObject.optInt(KEY_HAND_SHAKE_TYPE, NimHandshakeType.V1.getValue()));
    addresses.module = jsonObject.optString(KEY_MODULE);
    addresses.publicKeyVersion = jsonObject.optInt(KEY_VERSION, 0);
    addresses.lbs = jsonObject.optString(KEY_LBS);
    addresses.defaultLink = jsonObject.optString(KEY_LINK);
    addresses.nosUploadLbs = jsonObject.optString(KEY_NOS_LBS);
    addresses.nosUploadDefaultLink = jsonObject.optString(KEY_NOS_UPLOADER);
    addresses.nosUpload = jsonObject.optString(KEY_NOS_UPLOADER_HOST);
    addresses.nosSupportHttps = jsonObject.optBoolean(KEY_HTTPS_ENABLED, true);
    addresses.nosDownloadUrlFormat = jsonObject.optString(KEY_NOS_DOWNLOADER);
    addresses.nosDownload = jsonObject.optString(KEY_NOS_ACCELERATE_HOST);
    addresses.nosDownloadSet =
        createStringSetFromJSONArray(jsonObject.optJSONArray(KEY_NOS_ACCELERATE_HOST_LIST));
    addresses.nosCdnEnable = jsonObject.optBoolean(KEY_NOS_CDN_ENABLE);
    addresses.nosAccess = jsonObject.optString(KEY_NOS_ACCELERATE);
    addresses.ntServerAddress = jsonObject.optString(KEY_NT_SERVER);
    addresses.dedicatedClusteFlag = jsonObject.optInt(KEY_DEDICATED_CLUSTE_FLAG);
    addresses.negoKeyNeca =
        AsymmetricType.value(jsonObject.optInt(KEY_NEGO_KEY_NECA, AsymmetricType.RSA.getValue()));
    addresses.negoKeyEncaKeyVersion = jsonObject.optInt(KEY_NEGO_KEY_ENCA_KEY_VERSION);
    addresses.negoKeyEncaKeyParta = jsonObject.optString(KEY_NEGO_KEY_ENCA_KEY_PARTA);
    addresses.negoKeyEncaKeyPartb = jsonObject.optString(KEY_NEGO_KEY_ENCA_KEY_PARTB);
    addresses.commEnca =
        SymmetryType.value(jsonObject.optInt(KEY_COMM_ENCA, SymmetryType.RC4.getValue()));
    addresses.linkIpv6 = jsonObject.optString(KEY_LINK_IPV6);
    addresses.ipProtocolVersion =
        IPVersion.value(jsonObject.optInt(KEY_IP_PROTOCOL_VERSION, IPVersion.IPV4.getValue()));
    addresses.probeIpv4Url = jsonObject.optString(KEY_PROBE_IPV4_URL);
    addresses.probeIpv6Url = jsonObject.optString(KEY_PROBE_IPV6_URL);
    appKey = jsonObject.optString(KEY_APP_KEY);
    autoAdjust(addresses);
    return addresses;
  }

  private static void autoAdjust(@NonNull ServerAddresses addresses) {
    addresses.module = TextUtils.isEmpty(addresses.module) ? null : addresses.module;
    addresses.lbs = TextUtils.isEmpty(addresses.lbs) ? null : addresses.lbs;
    addresses.defaultLink = TextUtils.isEmpty(addresses.defaultLink) ? null : addresses.defaultLink;
    addresses.nosUploadLbs =
        TextUtils.isEmpty(addresses.nosUploadLbs) ? null : addresses.nosUploadLbs;
    addresses.nosUploadDefaultLink =
        TextUtils.isEmpty(addresses.nosUploadDefaultLink) ? null : addresses.nosUploadDefaultLink;
    addresses.nosUpload = TextUtils.isEmpty(addresses.nosUpload) ? null : addresses.nosUpload;
    addresses.nosDownloadUrlFormat =
        TextUtils.isEmpty(addresses.nosDownloadUrlFormat) ? null : addresses.nosDownloadUrlFormat;
    addresses.nosDownload = TextUtils.isEmpty(addresses.nosDownload) ? null : addresses.nosDownload;
    addresses.nosAccess = TextUtils.isEmpty(addresses.nosAccess) ? null : addresses.nosAccess;
    addresses.ntServerAddress =
        TextUtils.isEmpty(addresses.ntServerAddress) ? null : addresses.ntServerAddress;
    addresses.negoKeyEncaKeyParta =
        TextUtils.isEmpty(addresses.negoKeyEncaKeyParta) ? null : addresses.negoKeyEncaKeyParta;
    addresses.negoKeyEncaKeyPartb =
        TextUtils.isEmpty(addresses.negoKeyEncaKeyPartb) ? null : addresses.negoKeyEncaKeyPartb;
    addresses.linkIpv6 = TextUtils.isEmpty(addresses.linkIpv6) ? null : addresses.linkIpv6;
    addresses.probeIpv4Url =
        TextUtils.isEmpty(addresses.probeIpv4Url) ? null : addresses.probeIpv4Url;
    addresses.probeIpv6Url =
        TextUtils.isEmpty(addresses.probeIpv6Url) ? null : addresses.probeIpv6Url;
    appKey = TextUtils.isEmpty(appKey) ? null : appKey;
  }

  public static HashSet<String> createStringSetFromJSONArray(@Nullable JSONArray array) {
    int len;
    if (array == null || (len = array.length()) == 0) {
      return new HashSet<>(0);
    }
    HashSet<String> set = new HashSet<>(len);
    String elem;
    for (int i = 0; i < len; ++i) {
      try {
        elem = array.getString(i);
      } catch (JSONException e) {
        e.printStackTrace();
        continue;
      }
      set.add(elem);
    }
    return set;
  }
}
