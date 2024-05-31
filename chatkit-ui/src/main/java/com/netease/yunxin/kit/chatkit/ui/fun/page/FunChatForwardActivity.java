// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.fun.page;

import android.os.Bundle;
import androidx.annotation.Nullable;
import com.netease.nimlib.sdk.v2.message.enums.V2NIMMessageType;
import com.netease.yunxin.kit.chatkit.model.IMMessageInfo;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.custom.MultiForwardAttachment;
import com.netease.yunxin.kit.chatkit.ui.page.ChatForwardBaseActivity;
import com.netease.yunxin.kit.corekit.im2.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;

/**
 * Fun皮肤合并转发消息的详情页面，继承自ChatForwardBaseActivity
 * Fun皮肤差异化的部分抽象到FunChatForwardActivity中，通用功能抽象到ChatForwardBaseActivity中
 */
public class FunChatForwardActivity extends ChatForwardBaseActivity {

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    changeStatusBarColor(R.color.fun_chat_page_bg_color);
    viewBinding.forwardPageRoot.setBackgroundResource(R.color.fun_chat_page_bg_color);
  }

  @Override
  protected void clickMessage(IMMessageInfo messageInfo) {
    if (messageInfo.getMessage().getMessageType() == V2NIMMessageType.V2NIM_MESSAGE_TYPE_CUSTOM) {
      if (messageInfo.getAttachment() instanceof MultiForwardAttachment) {
        XKitRouter.withKey(RouterConstant.PATH_FUN_CHAT_FORWARD_PAGE)
            .withContext(this)
            .withParam(RouterConstant.KEY_MESSAGE, messageInfo)
            .navigate();
        return;
      }
    }
    super.clickMessage(messageInfo);
  }
}
