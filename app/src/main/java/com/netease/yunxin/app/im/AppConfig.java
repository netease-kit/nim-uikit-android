// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.im;

/**
 * IM UIKit 配置信息
 * 包括登录信息、离线推送信息、AI助聊接口信息等
 */
public class AppConfig {

  // IM 登录

  // IM UIKit和IM SDK登录账号ID
  public static final String account = "";
  // IM UIKit和IM SDK登录token
  public static final String token = "";


  // AI助聊 HTTP请求接口信息
  // 助聊接口Host
  public static final String AIHelperHost_ONLINE = "";
  // 助聊URL地址
  public static final String AIHelperUrl = "";
  // 助聊应用服务器需要的权限校验token，由自己业务决定和实现
  public static final String accessToken = "";

  // 离线推送信息
  // 小米配置信息
  public static final String xmAppId = "";
  public static final String xmAppKey = "";
  public static final String xmCertificateName = "";

  // 华为推送配置
  public static final String hwAppId = "";
  public static final String hwCertificateName = "";

  // 魅族推送配置
  public static final String mzAppId = "";
  public static final String mzAppKey = "";
  public static final String mzCertificateName = "";

  // vivo离线推送配置
  public static final String vivoCertificateName = "";

  // oppo离线推送配置
  public static final String oppoAppId = "";
  public static final String oppoAppKey = "";
  public static final String oppoAppSercet = "";
  public static final String oppoCertificateName = "";
}
