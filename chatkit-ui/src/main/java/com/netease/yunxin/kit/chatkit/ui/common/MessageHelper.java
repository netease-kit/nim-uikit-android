// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.common;

import static com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant.LIB_TAG;
import static com.netease.yunxin.kit.corekit.im.utils.RouterConstant.KEY_REVOKE_CONTENT_TAG;
import static com.netease.yunxin.kit.corekit.im.utils.RouterConstant.KEY_REVOKE_TAG;
import static com.netease.yunxin.kit.corekit.im.utils.RouterConstant.KEY_REVOKE_TIME_TAG;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.view.View;
import android.widget.TextView;
import com.netease.nimlib.sdk.msg.MessageBuilder;
import com.netease.nimlib.sdk.msg.constant.MsgDirectionEnum;
import com.netease.nimlib.sdk.msg.constant.MsgStatusEnum;
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.CustomMessageConfig;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.model.IMMessageInfo;
import com.netease.yunxin.kit.chatkit.model.IMMessageRecord;
import com.netease.yunxin.kit.chatkit.repo.ChatRepo;
import com.netease.yunxin.kit.chatkit.ui.ChatCustom;
import com.netease.yunxin.kit.chatkit.ui.ChatKitClient;
import com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.chatkit.ui.model.ait.AitBlock;
import com.netease.yunxin.kit.chatkit.ui.model.ait.AitContactsModel;
import com.netease.yunxin.kit.chatkit.ui.view.emoji.EmojiManager;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import com.netease.yunxin.kit.corekit.im.IMKitClient;
import com.netease.yunxin.kit.corekit.im.model.UserInfo;
import com.netease.yunxin.kit.corekit.im.provider.FetchCallback;
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import org.json.JSONObject;

public class MessageHelper {

  public static final int REVOKE_TIME_INTERVAL = 2 * 60 * 1000;

  public static final float DEF_SCALE = 0.6f;
  public static final float SMALL_SCALE = 0.4F;
  private static final String TAG = "MessageUtil";
  private static final ChatCustom chatCustom = new ChatCustom();
  /**
   * get nickName display
   *
   * @param tid team id
   * @param account user accId
   */
  public static String getTeamMemberDisplayNameYou(String tid, String account) {
    if (account.equals(IMKitClient.account())) {
      return IMKitClient.getApplicationContext().getString(R.string.chat_you);
    }
    return ChatUserCache.getName(tid, account);
  }

  //获取会话中的用户名称
  public static void getChatDisplayNameYou(
      String tid, String account, FetchCallback<String> callback) {
    String nick = null;
    if (account.equals(IMKitClient.account())) {
      nick = IMKitClient.getApplicationContext().getString(R.string.chat_you);
      callback.onSuccess(nick);
      return;
    }

    ChatUserCache.getName(account);
    callback.onSuccess(account);
  }

  public static String getChatDisplayNameYou(String tid, String account) {
    if (account.equals(IMKitClient.account())) {
      return IMKitClient.getApplicationContext().getString(R.string.chat_you);
    }

    return ChatUserCache.getName(tid, account);
  }

  public static String getTeamNick(String tid, String account) {
    return ChatUserCache.getName(tid, account);
  }

  /**
   * get nickName display
   *
   * @param tid team id
   * @param user UserInfo
   */
  public static String getTeamMemberDisplayName(String tid, UserInfo user) {
    if (TextUtils.equals(IMKitClient.account(), user.getAccount())) {
      return IMKitClient.getApplicationContext().getString(R.string.chat_you);
    }
    return ChatUserCache.getName(tid, user.getAccount());
  }

  public static String getTeamAitName(String tid, UserInfo user) {
    if (TextUtils.equals(IMKitClient.account(), user.getAccount())) {
      return "";
    }

    String memberNick = ChatUserCache.getAitName(tid, user.getAccount());
    if (!TextUtils.isEmpty(memberNick)) {
      return memberNick;
    }
    return (TextUtils.isEmpty(user.getName()) ? user.getAccount() : user.getName());
  }

  public static String getChatMessageUserName(IMMessageInfo message) {
    String account = message.getMessage().getFromAccount();
    String tid = null;
    if (message.getMessage().getSessionType() == SessionTypeEnum.Team) {
      tid = message.getMessage().getSessionId();
    }
    String name = ChatUserCache.getName(tid, message.getMessage().getFromAccount());
    if (TextUtils.equals(account, IMKitClient.account()) && TextUtils.equals(name, account)) {
      UserInfo userInfo = IMKitClient.getUserInfo();
      name = userInfo != null ? userInfo.getName() : account;
    }

    return name;
  }

  public static String getChatCacheAvatar(String account) {
    UserInfo userInfo = ChatUserCache.getUserInfo(account);
    if (userInfo != null && !TextUtils.isEmpty(userInfo.getAvatar())) {
      return userInfo.getAvatar();
    }
    return null;
  }

  public static UserInfo getChatMessageUserInfo(String account) {
    return ChatUserCache.getUserInfo(account);
  }

  public static String getChatSearchMessageUserName(IMMessageRecord message) {
    String name = null;
    if (message != null) {
      message.getIndexRecord();
      name = ChatUserCache.getName(message.getIndexRecord().getMessage().getFromAccount());
    }
    return name;
  }

  public static String getReplyMessageTips(IMMessageInfo messageInfo) {
    if (messageInfo == null) {
      return "...";
    }
    String nickName = getChatMessageUserName(messageInfo);
    String content = getReplyMsgBrief(messageInfo);
    return nickName + ": " + content;
  }

  public static void getReplyMessageInfo(String uuid, FetchCallback<List<IMMessageInfo>> callback) {
    if (TextUtils.isEmpty(uuid)) {
      callback.onSuccess(null);
    }
    List<String> uuidList = new ArrayList<>(1);
    uuidList.add(uuid);
    ChatRepo.queryMessageListByUuid(uuidList, callback);
  }

  public static String getReplyContent(IMMessageInfo messageInfo) {
    String result;
    if (messageInfo == null) {
      result = IMKitClient.getApplicationContext().getString(R.string.chat_message_removed_tip);
    } else {
      String nickName = getChatMessageUserName(messageInfo);
      String content = getReplyMsgBrief(messageInfo);
      result = nickName + ": " + content;
    }
    return result;
  }

  public static String getReplyMsgBrief(IMMessageInfo messageInfo) {
    if (ChatKitClient.getChatUIConfig() != null
        && ChatKitClient.getChatUIConfig().chatCustom != null) {
      return ChatKitClient.getChatUIConfig().chatCustom.getReplyMsgBrief(messageInfo);
    }
    return chatCustom.getReplyMsgBrief(messageInfo);
  }

  public static String getMessageRevokeContent(IMMessageInfo messageInfo) {

    Map<String, Object> localExtension = messageInfo.getMessage().getLocalExtension();
    if (localExtension != null
        && localExtension.containsKey(RouterConstant.KEY_REVOKE_CONTENT_TAG)) {
      Object content = localExtension.get(RouterConstant.KEY_REVOKE_CONTENT_TAG);
      if (content instanceof String) {
        return (String) content;
      }
    }
    return null;
  }

  public static void identifyFaceExpression(
      Context context, View textView, String value, int align) {
    identifyFaceExpression(context, textView, value, align, DEF_SCALE);
  }

  public static void identifyExpression(Context context, View textView, IMMessage message) {
    if (message != null && textView != null) {
      SpannableString spannableString =
          replaceEmoticons(context, message.getContent(), DEF_SCALE, ImageSpan.ALIGN_BOTTOM);
      identifyAitExpression(context, spannableString, message);
      viewSetText(textView, spannableString);
    }
  }

  public static void identifyAitExpression(
      Context context, SpannableString spannableString, IMMessage message) {
    AitContactsModel aitContactsModel = getAitBlock(message);
    if (aitContactsModel != null) {
      List<AitBlock> blockList = aitContactsModel.getAitBlockList();
      String text = message.getContent();
      for (AitBlock block : blockList) {
        for (AitBlock.AitSegment segment : block.segments) {
          if (segment.start >= 0 && segment.end > segment.start && segment.end < text.length()) {
            ForegroundColorSpan colorSpan =
                new ForegroundColorSpan(context.getResources().getColor(R.color.color_007aff));
            spannableString.setSpan(
                colorSpan, segment.start, segment.end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
          }
        }
      }
    }
  }

  public static AitContactsModel getAitBlock(IMMessage message) {
    if (message != null
        && message.getMsgType() == MsgTypeEnum.text
        && message.getRemoteExtension() != null) {
      Map<String, Object> remoteExt = message.getRemoteExtension();
      Object aitData = remoteExt.get(ChatKitUIConstant.AIT_REMOTE_EXTENSION_KEY);
      if (aitData instanceof Map) {
        JSONObject aitJson = new JSONObject((Map) aitData);
        return AitContactsModel.parseFromJson(aitJson);
      }
    }
    return null;
  }

  private static void viewSetText(View textView, SpannableString mSpannableString) {
    if (textView instanceof TextView) {
      TextView tv = (TextView) textView;
      tv.setText(mSpannableString);
    }
  }

  public static boolean revokeMsgIsEdit(ChatMessageBean data) {
    return !isReceivedMessage(data)
        && data.getMessageData().getMessage().getMsgType() == MsgTypeEnum.text
        && (System.currentTimeMillis() - data.getMessageData().getMessage().getTime()
            < REVOKE_TIME_INTERVAL)
        && data.revokeMsgEdit;
  }

  public static boolean isReceivedMessage(ChatMessageBean message) {
    return message.getMessageData().getMessage().getDirect() == MsgDirectionEnum.In;
  }

  public static void identifyFaceExpression(
      Context context, View textView, String value, int align, float scale) {
    SpannableString spannableString = replaceEmoticons(context, value, scale, align);
    viewSetText(textView, spannableString);
  }

  private static SpannableString replaceEmoticons(
      Context context, String value, float scale, int align) {
    if (TextUtils.isEmpty(value)) {
      value = "";
    }

    SpannableString mSpannableString = new SpannableString(value);
    Matcher matcher = EmojiManager.getPattern().matcher(value);
    while (matcher.find()) {
      int start = matcher.start();
      int end = matcher.end();
      String emot = value.substring(start, end);
      Drawable d = getEmotDrawable(context, emot, scale);
      if (d != null) {
        ImageSpan span = new ImageSpan(d, align);
        mSpannableString.setSpan(span, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
      }
    }
    return mSpannableString;
  }

  public static String formatCallTime(int time) {

    int hour = time / 3600;
    int minute = (time % 3600) / 60;
    int second = time % 60;
    if (hour == 0) {
      return String.format(Locale.CHINA, "%02d:%02d", minute, second);
    }
    return String.format(Locale.CHINA, "%02d:%02d:%02d", hour, minute, second);
  }

  public static void replaceEmoticons(Context context, Editable editable, int start, int count) {
    if (count <= 0 || editable.length() < start + count) return;

    CharSequence s = editable.subSequence(start, start + count);
    Matcher matcher = EmojiManager.getPattern().matcher(s);
    while (matcher.find()) {
      int from = start + matcher.start();
      int to = start + matcher.end();
      String emot = editable.subSequence(from, to).toString();
      Drawable d = getEmotDrawable(context, emot, SMALL_SCALE);
      if (d != null) {
        ImageSpan span = new ImageSpan(d, ImageSpan.ALIGN_CENTER);
        editable.setSpan(span, from, to, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
      }
    }
  }

  private static Drawable getEmotDrawable(Context context, String text, float scale) {
    Drawable drawable = EmojiManager.getDrawable(context, text);

    // scale
    if (drawable != null) {
      int width = (int) (drawable.getIntrinsicWidth() * scale);
      int height = (int) (drawable.getIntrinsicHeight() * scale);
      drawable.setBounds(0, 0, width, height);
    }

    return drawable;
  }

  public static Map<String, Object> createReplyExtension(
      Map<String, Object> remote, IMMessage replyMsg) {
    if (replyMsg != null) {

      if (remote == null) {
        remote = new HashMap<>();
      }
      Map<String, Object> replyInfo = new HashMap<>();
      replyInfo.put(ChatKitUIConstant.REPLY_UUID_KEY, replyMsg.getUuid());
      replyInfo.put(ChatKitUIConstant.REPLY_TYPE_KEY, replyMsg.getSessionType().toString());
      replyInfo.put(ChatKitUIConstant.REPLY_FROM_KEY, replyMsg.getFromAccount());
      replyInfo.put(ChatKitUIConstant.REPLY_TO_KEY, replyMsg.getSessionId());
      replyInfo.put(ChatKitUIConstant.REPLY_SERVER_ID_KEY, replyMsg.getServerId());
      replyInfo.put(ChatKitUIConstant.REPLY_TIME_KEY, replyMsg.getTime());
      remote.put(ChatKitUIConstant.REPLY_REMOTE_EXTENSION_KEY, replyInfo);
    }
    return remote;
  }

  public static void clearAitAndReplyInfo(IMMessage message) {
    if (message != null && message.getRemoteExtension() != null) {
      Map<String, Object> remote = message.getRemoteExtension();
      remote.remove(ChatKitUIConstant.REPLY_REMOTE_EXTENSION_KEY);
      remote.remove(ChatKitUIConstant.AIT_REMOTE_EXTENSION_KEY);
      message.setRemoteExtension(remote);
    }
  }

  public static void copyTextMessage(IMMessageInfo messageInfo, boolean showToast) {
    ClipboardManager cmb =
        (ClipboardManager)
            IMKitClient.getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
    ClipData clipData = null;
    if (messageInfo.getMessage().getMsgType() == MsgTypeEnum.text) {
      clipData = ClipData.newPlainText(null, messageInfo.getMessage().getContent());
    }
    cmb.setPrimaryClip(clipData);
    if (showToast) {
      ToastX.showShortToast(R.string.chat_message_action_copy_success);
    }
  }

  public static void saveLocalRevokeMessage(IMMessage message) {
    Map<String, Object> map = new HashMap<>(2);
    map.put(KEY_REVOKE_TAG, true);
    map.put(KEY_REVOKE_TIME_TAG, SystemClock.elapsedRealtime());
    map.put(KEY_REVOKE_CONTENT_TAG, message.getContent());
    if (message.getMsgType() != MsgTypeEnum.text) {
      map.put(RouterConstant.KEY_REVOKE_EDIT_TAG, false);
    } else {
      map.put(RouterConstant.KEY_REVOKE_EDIT_TAG, true);
    }

    IMMessage revokeMsg =
        MessageBuilder.createTextMessage(
            message.getSessionId(),
            message.getSessionType(),
            IMKitClient.getApplicationContext()
                .getResources()
                .getString(R.string.chat_message_revoke_content));
    revokeMsg.setStatus(MsgStatusEnum.success);
    revokeMsg.setDirect(message.getDirect());
    revokeMsg.setFromAccount(message.getFromAccount());
    revokeMsg.setLocalExtension(map);
    revokeMsg.setRemoteExtension(message.getRemoteExtension());
    CustomMessageConfig config = new CustomMessageConfig();
    config.enableUnreadCount = false;
    revokeMsg.setConfig(config);
    ChatRepo.saveLocalMessageExt(revokeMsg, message.getTime());
    ALog.d(LIB_TAG, TAG, "saveLocalRevokeMessage:" + message.getTime());
  }
}
