// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.common;

import static com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant.LIB_TAG;
import static com.netease.yunxin.kit.corekit.im.utils.RouterConstant.KEY_REVOKE_CONTENT_TAG;
import static com.netease.yunxin.kit.corekit.im.utils.RouterConstant.KEY_REVOKE_RICH_CONTENT_TAG;
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
import com.netease.nimlib.sdk.msg.constant.NotificationType;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.CustomMessageConfig;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.msg.model.MemberPushOption;
import com.netease.nimlib.sdk.team.model.DismissAttachment;
import com.netease.nimlib.sdk.team.model.MemberChangeAttachment;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.model.IMMessageInfo;
import com.netease.yunxin.kit.chatkit.model.IMMessageRecord;
import com.netease.yunxin.kit.chatkit.repo.ChatRepo;
import com.netease.yunxin.kit.chatkit.ui.ChatBriefUtils;
import com.netease.yunxin.kit.chatkit.ui.ChatCustom;
import com.netease.yunxin.kit.chatkit.ui.ChatKitClient;
import com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.custom.MultiForwardAttachment;
import com.netease.yunxin.kit.chatkit.ui.custom.RichTextAttachment;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.chatkit.ui.model.ait.AitBlock;
import com.netease.yunxin.kit.chatkit.ui.model.ait.AitContactsModel;
import com.netease.yunxin.kit.chatkit.ui.view.emoji.EmojiManager;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import com.netease.yunxin.kit.corekit.im.IMKitClient;
import com.netease.yunxin.kit.corekit.im.custom.CustomAttachment;
import com.netease.yunxin.kit.corekit.im.model.UserInfo;
import com.netease.yunxin.kit.corekit.im.provider.FetchCallback;
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import org.json.JSONException;
import org.json.JSONObject;

/** 消息相关工具类，主要用于创建消息，消息内容解析等 */
public class MessageHelper {

  public static final int REVOKE_TIME_INTERVAL = 2 * 60 * 1000;

  public static final float DEF_SCALE = 0.6f;
  public static final float SMALL_SCALE = 0.4F;
  private static final String TAG = "MessageUtil";

  // @信息高亮颜色值
  private static final int AT_HIGHLIGHT = R.color.color_007aff;
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

  // 获取会话中的用户名称
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
    return getChatMessageUserName(message.getMessage());
  }

  public static String getChatMessageUserName(IMMessage message) {
    String account = message.getFromAccount();
    String tid = null;
    if (message.getSessionType() == SessionTypeEnum.Team) {
      tid = message.getSessionId();
    }
    String name = ChatUserCache.getName(tid, message.getFromAccount());
    if (TextUtils.equals(account, IMKitClient.account()) && TextUtils.equals(name, account)) {
      UserInfo userInfo = IMKitClient.getUserInfo();
      name = userInfo != null ? userInfo.getName() : account;
    }
    return name;
  }

  public static String getChatMessageUserNameByAccount(String account) {
    String name = ChatUserCache.getName(null, account);
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
    if (localExtension != null) {
      if (localExtension.containsKey(RouterConstant.KEY_REVOKE_CONTENT_TAG)) {
        Object content = localExtension.get(RouterConstant.KEY_REVOKE_CONTENT_TAG);
        if (content instanceof String) {
          return (String) content;
        }
      }
    }
    return "";
  }

  public static Map<String, String> getRichMessageRevokeContent(IMMessageInfo messageInfo) {

    Map<String, Object> localExtension = messageInfo.getMessage().getLocalExtension();
    if (localExtension != null) {
      if (localExtension.containsKey(RouterConstant.KEY_REVOKE_RICH_CONTENT_TAG)) {
        Object content = localExtension.get(RouterConstant.KEY_REVOKE_RICH_CONTENT_TAG);
        if (content instanceof Map) {
          return (Map<String, String>) content;
        }
      }
    }
    return null;
  }

  public static void identifyFaceExpression(
      Context context, View textView, String value, int align) {
    identifyFaceExpression(context, textView, value, align, DEF_SCALE);
  }

  public static void identifyExpression(
      Context context, View textView, int color, IMMessage message) {
    if (message != null && textView != null) {
      SpannableString spannableString =
          replaceEmoticons(context, message.getContent(), DEF_SCALE, ImageSpan.ALIGN_BOTTOM);
      identifyAitExpression(context, spannableString, color, message);
      viewSetText(textView, spannableString);
    }
  }

  public static void identifyExpression(Context context, View textView, IMMessage message) {
    identifyExpression(context, textView, message.getContent(), message);
  }

  public static void identifyExpression(
      Context context, View textView, String content, IMMessage message) {
    if (message != null && textView != null) {
      SpannableString spannableString =
          replaceEmoticons(context, content, DEF_SCALE, ImageSpan.ALIGN_BOTTOM);
      int color = context.getResources().getColor(AT_HIGHLIGHT);
      identifyAitExpression(context, spannableString, color, content, message);
      viewSetText(textView, spannableString);
    }
  }

  public static void identifyAitExpression(
      Context context, SpannableString spannableString, int color, IMMessage message) {
    identifyAitExpression(context, spannableString, color, message.getContent(), message);
  }

  public static void identifyAitExpression(
      Context context,
      SpannableString spannableString,
      int color,
      String content,
      IMMessage message) {
    AitContactsModel aitContactsModel = getAitBlock(message);
    if (aitContactsModel != null && !TextUtils.isEmpty(content)) {
      List<AitBlock> blockList = aitContactsModel.getAitBlockList();
      for (AitBlock block : blockList) {
        for (AitBlock.AitSegment segment : block.segments) {
          if (segment.start >= 0 && segment.end > segment.start && segment.end < content.length()) {
            ForegroundColorSpan colorSpan = new ForegroundColorSpan(color);
            spannableString.setSpan(
                colorSpan, segment.start, segment.end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
          }
        }
      }
    }
  }

  public static void identifyExpressionForEditMsg(
      Context context, View textView, String content, AitContactsModel aitContactsModel) {
    if (textView != null) {
      SpannableString spannableString =
          replaceEmoticons(context, content, DEF_SCALE, ImageSpan.ALIGN_BOTTOM);
      int color = context.getResources().getColor(AT_HIGHLIGHT);
      identifyAitExpression(context, spannableString, color, content, aitContactsModel);
      viewSetText(textView, spannableString);
    }
  }

  public static void identifyAitExpression(
      Context context,
      SpannableString spannableString,
      int color,
      String content,
      AitContactsModel aitContactsModel) {
    if (aitContactsModel != null && !TextUtils.isEmpty(content)) {
      List<AitBlock> blockList = aitContactsModel.getAitBlockList();
      for (AitBlock block : blockList) {
        for (AitBlock.AitSegment segment : block.segments) {
          if (segment.start >= 0 && segment.end > segment.start && segment.end < content.length()) {
            ForegroundColorSpan colorSpan = new ForegroundColorSpan(color);
            spannableString.setSpan(
                colorSpan, segment.start, segment.end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
          }
        }
      }
    }
  }

  public static SpannableString generateAtSpanString(String content) {
    SpannableString spannableString = new SpannableString(content);
    int color = IMKitClient.getApplicationContext().getResources().getColor(AT_HIGHLIGHT);
    ForegroundColorSpan colorSpan = new ForegroundColorSpan(color);
    spannableString.setSpan(colorSpan, 0, content.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    return spannableString;
  }

  public static AitContactsModel getAitBlock(IMMessage message) {
    if (message != null && message.getRemoteExtension() != null) {
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
    IMMessage message = data.getMessageData().getMessage();
    return !isReceivedMessage(data)
        && canRevokeEdit(data.getMessageData().getMessage())
        && (System.currentTimeMillis() - message.getTime() < REVOKE_TIME_INTERVAL)
        && data.revokeMsgEdit;
  }

  public static boolean isReceivedMessage(ChatMessageBean message) {
    return message.getMessageData().getMessage().getDirect() == MsgDirectionEnum.In;
  }

  public static boolean isThreadReplayInfo(ChatMessageBean message) {
    return message != null
        && message.getMessageData().getMessage().getThreadOption() != null
        && !TextUtils.isEmpty(
            message.getMessageData().getMessage().getThreadOption().getReplyMsgIdClient());
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

  public static boolean replaceEmoticons(
      Context context, SpannableString spannableString, int start, int count) {
    if (count <= 0 || spannableString.length() < start + count) return false;

    boolean result = false;
    CharSequence s = spannableString.subSequence(start, start + count);
    Matcher matcher = EmojiManager.getPattern().matcher(s);
    while (matcher.find()) {
      int from = start + matcher.start();
      int to = start + matcher.end();
      String emot = spannableString.subSequence(from, to).toString();
      Drawable d = getEmotDrawable(context, emot, SMALL_SCALE);
      if (d != null) {
        ImageSpan span = new ImageSpan(d, ImageSpan.ALIGN_CENTER);
        spannableString.setSpan(span, from, to, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        result = true;
      }
    }
    return result;
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
    } else if (messageInfo.getMessage().getMsgType() == MsgTypeEnum.custom) {
      CustomAttachment attachment = (CustomAttachment) messageInfo.getMessage().getAttachment();
      if (attachment instanceof RichTextAttachment) {
        String data = ((RichTextAttachment) attachment).body;
        if (TextUtils.isEmpty(data)) {
          data = ((RichTextAttachment) attachment).title;
        }
        clipData = ClipData.newPlainText(null, data);
      }
    }
    cmb.setPrimaryClip(clipData);
    if (showToast) {
      ToastX.showShortToast(R.string.chat_message_action_copy_success);
    }
  }

  public static void saveLocalRevokeMessage(IMMessage message, boolean canRevokeEdit) {
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
    revokeMsg.setRemoteExtension(message.getRemoteExtension());
    CustomMessageConfig config = new CustomMessageConfig();
    config.enableUnreadCount = false;
    revokeMsg.setConfig(config);
    Map<String, Object> map = new HashMap<>(4);
    map.put(KEY_REVOKE_TAG, true);
    map.put(KEY_REVOKE_TIME_TAG, SystemClock.elapsedRealtime());
    if (message.getMsgType() == MsgTypeEnum.text) {
      map.put(KEY_REVOKE_CONTENT_TAG, message.getContent());
    } else if (isRichText(message)) {
      CustomAttachment attachment = (CustomAttachment) message.getAttachment();
      if (attachment instanceof RichTextAttachment) {
        String body = ((RichTextAttachment) attachment).body;
        String title = ((RichTextAttachment) attachment).title;
        JSONObject data = new JSONObject();
        try {
          data.put("body", body == null ? "" : body);
          data.put("title", title == null ? "" : title);
        } catch (JSONException e) {
          e.printStackTrace();
        }
        map.put(KEY_REVOKE_RICH_CONTENT_TAG, data);
      }
    }
    if (!canRevokeEdit(message) || !canRevokeEdit) {
      map.put(RouterConstant.KEY_REVOKE_EDIT_TAG, false);
    } else {
      map.put(RouterConstant.KEY_REVOKE_EDIT_TAG, true);
    }
    revokeMsg.setLocalExtension(map);
    ChatRepo.saveLocalMessageExt(revokeMsg, message.getTime());
    ALog.d(LIB_TAG, TAG, "saveLocalRevokeMessage:" + message.getTime());
  }

  public static void saveLocalBlackTipMessageAndNotify(IMMessage message) {
    IMMessage tipMsg =
        MessageBuilder.createTipMessage(message.getSessionId(), message.getSessionType());
    tipMsg.setStatus(MsgStatusEnum.success);
    tipMsg.setDirect(message.getDirect());
    tipMsg.setFromAccount(message.getFromAccount());
    String content =
        IMKitClient.getApplicationContext()
            .getString(
                R.string.chat_message_send_message_when_in_black,
                getChatMessageUserNameByAccount(message.getSessionId()));
    tipMsg.setContent(content);
    CustomMessageConfig config = new CustomMessageConfig();
    config.enableUnreadCount = false;
    tipMsg.setConfig(config);
    ChatRepo.saveLocalMessageExt(tipMsg, tipMsg.getTime(), true);
    ALog.d(LIB_TAG, TAG, "saveLocalBlackTipMessage:" + tipMsg.getTime());
  }

  // 创建合并转发消息体内容
  public static String createMultiForwardMsg(List<IMMessageInfo> msgList) {
    if (msgList == null || msgList.isEmpty()) {
      return "";
    }

    List<IMMessage> messageList = new ArrayList<>();
    Map<String, Object> atMap = new HashMap<>();
    Map<String, Object> replyMap = new HashMap<>();
    for (int index = 0; index < msgList.size(); index++) {
      IMMessageInfo info = msgList.get(index);
      // 去除转发消息中的回复消息 和 @消息
      Map<String, Object> extension = info.getMessage().getRemoteExtension();
      if (extension != null) {
        if (extension.containsKey(ChatKitUIConstant.REPLY_REMOTE_EXTENSION_KEY)) {
          Object replyContent = extension.remove(ChatKitUIConstant.REPLY_REMOTE_EXTENSION_KEY);
          replyMap.put(info.getMessage().getUuid(), replyContent);
        }
        if (extension.containsKey(ChatKitUIConstant.AIT_REMOTE_EXTENSION_KEY)) {
          Object atContent = extension.remove(ChatKitUIConstant.AIT_REMOTE_EXTENSION_KEY);
          atMap.put(info.getMessage().getUuid(), atContent);
        }
      } else {
        extension = new HashMap<>();
      }
      extension.put(
          ChatKitUIConstant.KEY_MERGE_REMOTE_EXTENSION_NICK,
          info.getFromUser() != null
              ? info.getFromUser().getName()
              : info.getMessage().getFromAccount());
      extension.put(
          ChatKitUIConstant.KEY_MERGE_REMOTE_EXTENSION_AVATAR,
          info.getFromUser() != null ? info.getFromUser().getAvatar() : "");
      info.getMessage().setRemoteExtension(extension);
      messageList.add(info.getMessage());
    }
    String messageListStr = MessageBuilder.createForwardMessageListFileDetail(messageList);
    for (int index = 0; index < msgList.size(); index++) {
      IMMessageInfo info = msgList.get(index);
      // 去除转发消息中的回复消息 和 @消息
      Map<String, Object> extMap = info.getMessage().getRemoteExtension();
      if (replyMap.containsKey(info.getMessage().getUuid())) {
        if (extMap == null) {
          extMap = new HashMap<>();
        }
        extMap.put(
            ChatKitUIConstant.REPLY_REMOTE_EXTENSION_KEY,
            replyMap.get(info.getMessage().getUuid()));
      }
      if (atMap.containsKey(info.getMessage().getUuid())) {
        if (extMap == null) {
          extMap = new HashMap<>();
        }
        extMap.put(
            ChatKitUIConstant.AIT_REMOTE_EXTENSION_KEY, atMap.get(info.getMessage().getUuid()));
      }
      if (extMap != null) {
        extMap.remove(ChatKitUIConstant.KEY_MERGE_REMOTE_EXTENSION_NICK);
        extMap.remove(ChatKitUIConstant.KEY_MERGE_REMOTE_EXTENSION_AVATAR);
        info.getMessage().setRemoteExtension(extMap);
      }
    }

    return messageListStr;
  }

  // 创建合并转发消息附件
  public static MultiForwardAttachment createMultiTransmitAttachment(
      String displayName, String fromSessionID, String url, List<IMMessageInfo> msgList) {
    if (msgList == null || msgList.isEmpty() || fromSessionID == null) {
      return null;
    }
    MultiForwardAttachment attachment = new MultiForwardAttachment();
    attachment.sessionID = fromSessionID;
    attachment.sessionName = displayName;
    attachment.url = url;
    attachment.md5 = "";
    int depth = 0;
    List<MultiForwardAttachment.Abstracts> abstractsList = new ArrayList<>();
    for (int index = 0; index < msgList.size(); index++) {
      IMMessageInfo info = msgList.get(index);
      if (info.getMessage().getAttachment() instanceof MultiForwardAttachment) {
        if (depth < ((MultiForwardAttachment) info.getMessage().getAttachment()).depth) {
          depth = ((MultiForwardAttachment) info.getMessage().getAttachment()).depth;
        }
      }
      if (abstractsList.size() < ChatKitUIConstant.CHAT_FORWARD_ABSTRACTS_LIMIT) {
        String nick =
            info.getFromUser() != null
                ? info.getFromUser().getName()
                : info.getMessage().getFromAccount();
        MultiForwardAttachment.Abstracts abstracts =
            new MultiForwardAttachment.Abstracts(
                nick,
                ChatBriefUtils.customContentText(IMKitClient.getApplicationContext(), info),
                info.getMessage().getFromAccount());
        abstractsList.add(abstracts);
      }
    }
    attachment.depth = depth + 1;
    attachment.abstractsList = abstractsList;
    return attachment;
  }

  public static IMMessage createRichTextMessage(
      String title,
      String content,
      String sessionId,
      SessionTypeEnum sessionType,
      List<String> pushList,
      Map<String, Object> remoteExtension) {
    ALog.d(LIB_TAG, TAG, "createRichTextMessage:" + (content != null ? content.length() : "null"));
    RichTextAttachment attachment = new RichTextAttachment();
    attachment.body = content;
    attachment.title = title;
    IMMessage customMsg = MessageBuilder.createCustomMessage(sessionId, sessionType, attachment);
    appendTeamMemberPush(customMsg, pushList);
    if (remoteExtension != null) {
      customMsg.setRemoteExtension(remoteExtension);
    }
    customMsg.setPushContent(title);
    return customMsg;
  }

  public static void appendTeamMemberPush(IMMessage message, List<String> pushList) {
    ALog.d(
        LIB_TAG,
        TAG,
        "appendTeamMemberPush,message" + (message == null ? "null" : message.getUuid()));
    if (message == null) {
      return;
    }
    if (message.getSessionType() == SessionTypeEnum.Team
        && pushList != null
        && !pushList.isEmpty()) {
      MemberPushOption memberPushOption = new MemberPushOption();
      memberPushOption.setForcePush(true);
      memberPushOption.setForcePushContent(message.getContent());
      if (pushList.size() == 1 && pushList.get(0).equals(AitContactsModel.ACCOUNT_ALL)) {
        memberPushOption.setForcePushList(null);
      } else {
        memberPushOption.setForcePushList(pushList);
      }
      message.setMemberPushOption(memberPushOption);
    }
  }

  public static boolean isRichText(IMMessageInfo message) {
    return message != null && message.getMessage().getAttachment() instanceof RichTextAttachment;
  }

  public static boolean isRichText(IMMessage message) {
    return message != null && message.getAttachment() instanceof RichTextAttachment;
  }

  public static boolean canRevokeEdit(IMMessage message) {
    return message.getMsgType() == MsgTypeEnum.text || isRichText(message);
  }

  public static boolean isDismissTeamMsg(IMMessageInfo messageInfo) {
    if (messageInfo != null && messageInfo.getMessage().getMsgType() == MsgTypeEnum.notification) {
      if (messageInfo.getMessage().getAttachment() instanceof DismissAttachment) {
        return true;
      }
    }
    return false;
  }

  public static boolean isKickMsg(IMMessageInfo messageInfo) {
    if (messageInfo != null && messageInfo.getMessage().getMsgType() == MsgTypeEnum.notification) {
      if (messageInfo.getMessage().getAttachment() instanceof MemberChangeAttachment) {
        MemberChangeAttachment changeAttachment =
            (MemberChangeAttachment) messageInfo.getMessage().getAttachment();
        if (changeAttachment.getType() == NotificationType.KickMember
            && changeAttachment.getTargets() != null) {
          return changeAttachment.getTargets().contains(IMKitClient.account());
        }
      }
    }
    return false;
  }

  public static boolean isSameMessage(IMMessageInfo messageInfo, IMMessageInfo messageInfo2) {
    if (messageInfo == null || messageInfo2 == null) {
      return false;
    }
    return messageInfo.getMessage().isTheSame(messageInfo2.getMessage());
  }
}
