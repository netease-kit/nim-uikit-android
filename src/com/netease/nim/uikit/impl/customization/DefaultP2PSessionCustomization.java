package com.netease.nim.uikit.impl.customization;

import android.app.Activity;
import android.content.Intent;

import com.netease.nim.uikit.api.model.session.SessionCustomization;
import com.netease.nimlib.sdk.msg.attachment.MsgAttachment;

/**
 * SessionCustomization 可以实现聊天界面定制项：
 * 1. 聊天背景 <br>
 * 2. 加号展开后的按钮和动作，如自定义消息 <br>
 * 3. ActionBar右侧按钮。
 * <p>
 * Created by hzchenkang on 2016/12/19.
 */

public class DefaultP2PSessionCustomization extends SessionCustomization {

    @Override
    public void onActivityResult(final Activity activity, int requestCode, int resultCode, Intent data) {
        super.onActivityResult(activity, requestCode, resultCode, data);
    }

    @Override
    public MsgAttachment createStickerAttachment(String category, String item) {
        return null;
    }

}
