// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.normal.page;

import android.os.Bundle;
import androidx.annotation.Nullable;
import com.netease.nimlib.sdk.v2.message.enums.V2NIMMessageType;
import com.netease.yunxin.kit.chatkit.model.IMMessageInfo;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.custom.MultiForwardAttachment;
import com.netease.yunxin.kit.chatkit.ui.page.ChatForwardBaseActivity;
import com.netease.yunxin.kit.corekit.im2.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;

/** 标准皮肤，合并转发消息详情页面。 */
public class ChatForwardActivity extends ChatForwardBaseActivity {

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    changeStatusBarColor(R.color.color_white);
    viewBinding.forwardPageRoot.setBackgroundResource(R.color.color_white);
  }

  @Override
  protected void clickMessage(IMMessageInfo messageInfo) {
    if (messageInfo.getMessage().getMessageType() == V2NIMMessageType.V2NIM_MESSAGE_TYPE_CUSTOM) {
      if (messageInfo.getAttachment() instanceof MultiForwardAttachment) {
        XKitRouter.withKey(RouterConstant.PATH_CHAT_FORWARD_PAGE)
            .withContext(this)
            .withParam(RouterConstant.KEY_MESSAGE, messageInfo)
            .navigate();
        return;
      }
    }
    super.clickMessage(messageInfo);
  }
}
