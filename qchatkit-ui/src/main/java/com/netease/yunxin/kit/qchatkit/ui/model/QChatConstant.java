// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.qchatkit.ui.model;

public class QChatConstant {

  public static final String FILE_DIR = "tempPictures";
  public static final String TITLE = "title";
  public static final String CHOICE_LIST = "choice_list";
  public static final String SELECTED_INDEX = "selected_index";
  public static final String SERVER_ID = "server_id";
  public static final String SERVER_INFO = "serverInfo";
  public static final String SERVER_MEMBER = "serverMember";
  public static final String CHANNEL_ID = "channel_id";
  public static final String CHANNEL_NAME = "channel_name";
  public static final String CHANNEL_TOPIC = "channel_topic";
  public static final String CHANNEL_TYPE = "channel_type";
  public static final String SERVER_ROLE_INFO = "serverRoleInfo";
  public static final String SERVER_ROLE_ID = "serverRoleId";
  public static final String CHANNEL_ROLE = "channel_role_info";
  public static final String CHANNEL_MEMBER = "channel_member_info";

  public static final String ROUTER_ADD_ROLE = "channel/add_role";
  public static final String ROUTER_ADD_MEMBER = "channel/add_member";
  public static final String ROUTER_MEMBER_PERMISSION = "channel/member_permission";
  public static final String ROUTER_ROLE_PERMISSION = "channel/role_permission";

  //member selector
  public static final String REQUEST_MEMBER_SELECTOR_KEY = "request_member_selector_key";

  public static final String REQUEST_MEMBER_SIZE_KEY = "request_member_size_key";

  public static final int ROLE_EVERYONE_TYPE = 1;

  public static final String REQUEST_MEMBER_FILTER_KEY = "request_member_filter_key";

  public static final String REQUEST_MEMBER_FILTER_LIST = "request_member_filter_list";

  public static final int REQUEST_MEMBER_FILTER_ROLE = 1;

  public static final int REQUEST_MEMBER_FILTER_CHANNEL = 2;

  //server member operator
  public static final String MEMBER_OPERATOR_ACCID = "operator_accId";

  public static final String MEMBER_OPERATOR_TYPE = "operator_type";

  public static final int MEMBER_OPERATOR_TYPE_UNCHANGED = 0;

  public static final int MEMBER_OPERATOR_TYPE_DELETE = 1;

  public static final int MEMBER_OPERATOR_TYPE_CHANGED = 2;

  public static final int MEMBER_TYPE_OWNER = 1;

  /** 参数常量 */
  public static final int CORNER_RADIUS_ARROW = 16;

  public static final int QCHAT_ROLE_MAX_PAGE_SIZE = 500;

  public static final int MEMBER_PAGE_SIZE = 200;

  public static final int EVERYONE_TYPE = 1;

  /** im sdk error code */
  public static final int ERROR_CODE_IM_NO_PERMISSION = 403;

  /** Server error code 10001---10199 */
  public static final int ERROR_CODE_SERVER_CREATE = 10001;

  public static final int ERROR_CODE_SERVER_JOIN_APPLIED = 10002;
  public static final int ERROR_CODE_SERVER_LOAD = 10003;
  public static final int ERROR_CODE_SERVER_GET_ITEM = 10004;
  /** Channel error code 10101---10199 */
  public static final int ERROR_CODE_CHANNEL_CREATE = 10101;

  public static final int ERROR_CODE_CHANNEL_DELETE = 10102;
  public static final int ERROR_CODE_CHANNEL_FETCH = 10103;
  public static final int ERROR_CODE_CHANNEL_ROLE_FETCH = 10104;
  public static final int ERROR_CODE_CHANNEL_ROLE_ADD = 10104;
  public static final int ERROR_CODE_CHANNEL_ROLE_DELETE = 10104;
  public static final int ERROR_CODE_CHANNEL_MEMBER_FETCH = 10104;
  public static final int ERROR_CODE_CHANNEL_MEMBER_ADD = 10104;
  public static final int ERROR_CODE_CHANNEL_MEMBER_DELETE = 10104;

  /** message error code 10201---10299 */
  public static final int ERROR_CODE_MESSAGE_FETCH = 10201;

  public static final int ERROR_CODE_SEND_MESSAGE = 10202;
}
