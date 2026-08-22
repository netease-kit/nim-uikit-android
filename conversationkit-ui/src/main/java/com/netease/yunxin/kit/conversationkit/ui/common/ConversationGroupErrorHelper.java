// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.conversationkit.ui.common;

import android.content.Context;
import androidx.annotation.StringRes;
import com.netease.yunxin.kit.chatkit.utils.ErrorUtils;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import com.netease.yunxin.kit.conversationkit.ui.R;

public final class ConversationGroupErrorHelper {

  public static final int ERROR_CONVERSATION_BELONGED_GROUP_LIMIT = 110304;
  public static final int ERROR_CONVERSATION_NOT_EXIST = 110404;
  public static final int ERROR_CONVERSATION_GROUP_NOT_EXIST = 116404;
  public static final int ERROR_CONVERSATION_GROUP_LIMIT = 116435;
  public static final int ERROR_CONVERSATIONS_IN_GROUP_LIMIT = 116437;

  private ConversationGroupErrorHelper() {}

  public static void showErrorToast(Context context, int errorCode) {
    showErrorToast(context, errorCode, 0);
  }

  public static void showErrorToast(Context context, int errorCode, @StringRes int fallbackTipRes) {
    int tipRes = getErrorTipRes(errorCode);
    if (tipRes != 0) {
      ToastX.showShortToast(tipRes);
      return;
    }
    if (fallbackTipRes != 0) {
      ToastX.showShortToast(fallbackTipRes);
      return;
    }
    ErrorUtils.showErrorCodeToast(context, errorCode);
  }

  @StringRes
  private static int getErrorTipRes(int errorCode) {
    switch (errorCode) {
      case ERROR_CONVERSATION_BELONGED_GROUP_LIMIT:
        return R.string.conversation_group_belong_limit_tip;
      case ERROR_CONVERSATION_NOT_EXIST:
        return R.string.conversation_not_exist_tip;
      case ERROR_CONVERSATION_GROUP_NOT_EXIST:
        return R.string.conversation_group_not_exist_tip;
      case ERROR_CONVERSATION_GROUP_LIMIT:
        return R.string.conversation_group_limit_tip;
      case ERROR_CONVERSATIONS_IN_GROUP_LIMIT:
        return R.string.conversation_group_conversation_limit_tip;
      default:
        return 0;
    }
  }
}
