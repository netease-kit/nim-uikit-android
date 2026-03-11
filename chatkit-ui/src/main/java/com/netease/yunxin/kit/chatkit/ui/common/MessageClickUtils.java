// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.common;

import static android.net.MailTo.MAILTO_SCHEME;
import static com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant.TEL_SCHEME;
import static com.netease.yunxin.kit.chatkit.ui.view.input.ActionConstants.POP_ACTION_COPY;
import static com.netease.yunxin.kit.chatkit.ui.view.input.ActionConstants.POP_ACTION_TEL;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import androidx.annotation.NonNull;
import com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.common.ui.dialog.BottomChoiceDialog;
import com.netease.yunxin.kit.common.ui.dialog.BottomHeaderChoiceDialog;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import com.netease.yunxin.kit.common.utils.NetworkUtils;

public class MessageClickUtils {

  /**
   * 打开url链接
   *
   * @param context 上下文
   * @param url 链接
   * @return 是否成功打开
   */
  public static boolean handleClickableSpanClick(Context context, String url) {
    if (url.startsWith(TEL_SCHEME)) {
      String phone = url.substring(TEL_SCHEME.length());
      BottomHeaderChoiceDialog dialog =
          new BottomHeaderChoiceDialog(context, ChatDialogUtils.assembleMessageTelActions());
      dialog.setTitle(
          String.format(context.getResources().getString(R.string.chat_tel_tips_title), phone));
      dialog.setOnChoiceListener(
          new BottomChoiceDialog.OnChoiceListener() {
            @Override
            public void onChoice(@NonNull String type) {
              boolean hasNetwork = NetworkUtils.isConnected();
              switch (type) {
                case POP_ACTION_TEL:
                  if (hasNetwork) {
                    Intent intent = new Intent(Intent.ACTION_DIAL); // 仅打开拨号界面
                    intent.setData(Uri.parse("tel:" + phone)); // 自动填充电话号码
                    context.startActivity(intent);
                  } else {
                    ToastX.showLongToast(R.string.chat_network_error_tip);
                  }
                  break;
                case POP_ACTION_COPY:
                  if (hasNetwork) {
                    MessageHelper.copyText(phone, true);
                  } else {
                    ToastX.showLongToast(R.string.chat_network_error_tip);
                  }
                  break;
                default:
                  break;
              }
            }

            @Override
            public void onCancel() {}
          });
      dialog.show();
      return true;
    } else if (url.startsWith(MAILTO_SCHEME)) {
      try {
        Intent intentEmail = new Intent(Intent.ACTION_VIEW);
        intentEmail.setData(Uri.parse(url));
        context.startActivity(intentEmail);
      } catch (ActivityNotFoundException e) {
        return false;
      }
      return true;
    } else {
      for (String scheme : ChatKitUIConstant.WEB_SCHEME) {
        if (url.startsWith(scheme)) {
          try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            context.startActivity(intent);
          } catch (ActivityNotFoundException e) {
            return false;
          }
          return true;
        }
      }
    }

    return false;
  }
}
