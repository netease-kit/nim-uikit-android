// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.common;

import static com.netease.nimlib.sdk.ResponseCode.RES_IN_BLACK_LIST;
import static com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant.LIB_TAG;
import static com.netease.yunxin.kit.chatkit.ui.model.CollectionBean.COLLECTION_TYPE_START;
import static com.netease.yunxin.kit.corekit.im2.utils.RouterConstant.KEY_REVOKE_CONTENT_TAG;
import static com.netease.yunxin.kit.corekit.im2.utils.RouterConstant.KEY_REVOKE_RICH_CONTENT_TAG;
import static com.netease.yunxin.kit.corekit.im2.utils.RouterConstant.KEY_REVOKE_TAG;
import static com.netease.yunxin.kit.corekit.im2.utils.RouterConstant.KEY_REVOKE_TIME_TAG;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.v2.ai.model.V2NIMAIUser;
import com.netease.nimlib.sdk.v2.ai.params.V2NIMAIModelCallContent;
import com.netease.nimlib.sdk.v2.conversation.enums.V2NIMConversationType;
import com.netease.nimlib.sdk.v2.message.V2NIMMessage;
import com.netease.nimlib.sdk.v2.message.V2NIMMessageConverter;
import com.netease.nimlib.sdk.v2.message.V2NIMMessageCreator;
import com.netease.nimlib.sdk.v2.message.V2NIMMessageRefer;
import com.netease.nimlib.sdk.v2.message.V2NIMMessageRevokeNotification;
import com.netease.nimlib.sdk.v2.message.attachment.V2NIMMessageFileAttachment;
import com.netease.nimlib.sdk.v2.message.attachment.V2NIMMessageNotificationAttachment;
import com.netease.nimlib.sdk.v2.message.config.V2NIMMessageAIConfig;
import com.netease.nimlib.sdk.v2.message.config.V2NIMMessageConfig;
import com.netease.nimlib.sdk.v2.message.enums.V2NIMMessageAIStatus;
import com.netease.nimlib.sdk.v2.message.enums.V2NIMMessageNotificationType;
import com.netease.nimlib.sdk.v2.message.enums.V2NIMMessageType;
import com.netease.nimlib.sdk.v2.message.params.V2NIMAddCollectionParams;
import com.netease.nimlib.sdk.v2.message.params.V2NIMMessageAIConfigParams;
import com.netease.nimlib.sdk.v2.message.params.V2NIMSendMessageParams;
import com.netease.nimlib.sdk.v2.message.result.V2NIMSendMessageResult;
import com.netease.nimlib.sdk.v2.team.enums.V2NIMTeamChatBannedMode;
import com.netease.nimlib.sdk.v2.team.model.V2NIMUpdatedTeamInfo;
import com.netease.nimlib.sdk.v2.utils.V2NIMConversationIdUtil;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.ChatCustomMsgFactory;
import com.netease.yunxin.kit.chatkit.emoji.ChatEmojiManager;
import com.netease.yunxin.kit.chatkit.manager.AIUserManager;
import com.netease.yunxin.kit.chatkit.model.CustomAttachment;
import com.netease.yunxin.kit.chatkit.model.IMMessageInfo;
import com.netease.yunxin.kit.chatkit.model.RecentForward;
import com.netease.yunxin.kit.chatkit.repo.ChatRepo;
import com.netease.yunxin.kit.chatkit.repo.SettingRepo;
import com.netease.yunxin.kit.chatkit.ui.ChatCustom;
import com.netease.yunxin.kit.chatkit.ui.ChatKitClient;
import com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.cache.TeamUserManager;
import com.netease.yunxin.kit.chatkit.ui.custom.MultiForwardAttachment;
import com.netease.yunxin.kit.chatkit.ui.custom.RichTextAttachment;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.chatkit.ui.model.ait.AitBlock;
import com.netease.yunxin.kit.chatkit.ui.model.ait.AtContactsModel;
import com.netease.yunxin.kit.chatkit.utils.AIErrorCode;
import com.netease.yunxin.kit.chatkit.utils.ErrorUtils;
import com.netease.yunxin.kit.chatkit.utils.MessageExtensionHelper;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import com.netease.yunxin.kit.corekit.im2.IMKitClient;
import com.netease.yunxin.kit.corekit.im2.extend.FetchCallback;
import com.netease.yunxin.kit.corekit.im2.extend.ProgressFetchCallback;
import com.netease.yunxin.kit.corekit.im2.utils.RouterConstant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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
   * 获取群通知中的操作者名称
   *
   * @param account 操作者账号ID
   * @return UI展示昵称
   */
  public static String getTeamNotifyDisplayName(String account) {
    if (account.equals(IMKitClient.account())) {
      return IMKitClient.getApplicationContext().getString(R.string.chat_you);
    }
    return TeamUserManager.getInstance().getNickname(account, true);
  }

  /**
   * 获取标记中的昵称展示
   *
   * @param account 操作者账号ID
   * @param type 会话类型
   * @return UI展示昵称
   */
  public static String getChatPinDisplayName(String account, V2NIMConversationType type) {
    if (account.equals(IMKitClient.account())) {
      return IMKitClient.getApplicationContext().getString(R.string.chat_you);
    }

    return ChatUserCache.getInstance().getNickname(account, type);
  }

  /**
   * 获取群消息已读未读中的操作者名称
   *
   * @param account 操作者账号ID
   */
  public static String getTeamReaderDisplayName(String account) {
    if (TextUtils.equals(IMKitClient.account(), account)) {
      return IMKitClient.getApplicationContext().getString(R.string.chat_you);
    }

    //数字人直接返回
    if (AIUserManager.getAIUserById(account) != null) {
      return AIUserManager.getAIUserById(account).getName();
    }

    return TeamUserManager.getInstance().getNickname(account, true);
  }

  /**
   * 获取群@功能中展示的昵称
   *
   * @param user 用户ID
   * @return UI展示昵称
   */
  public static String getTeamAtName(String user) {
    if (TextUtils.equals(IMKitClient.account(), user)) {
      return "";
    }
    //数字人直接返回
    if (AIUserManager.getAIUserById(user) != null) {
      return AIUserManager.getAIUserById(user).getName();
    }
    return ChatUserCache.getInstance().getAitName(user);
  }

  /**
   * 获取消息发送者昵称
   *
   * @param account 发送者账号ID
   * @param type 会话类型
   * @return UI展示昵称
   */
  public static String getChatMessageUserNameByAccount(String account, V2NIMConversationType type) {
    //数字人直接返回
    if (AIUserManager.getAIUserById(account) != null) {
      V2NIMAIUser aiUser = AIUserManager.getAIUserById(account);
      return TextUtils.isEmpty(aiUser.getName()) ? account : aiUser.getName();
    }
    return ChatUserCache.getInstance().getNickname(account, type);
  }

  /**
   * 获取消息发送者昵称，只显示user 信息中的nick，忽略备注，群昵称等
   *
   * @param account 发送者账号ID
   * @param type 会话类型
   * @return UI展示昵称
   */
  public static String getUserNickByAccount(String account, V2NIMConversationType type) {
    //数字人直接返回
    if (AIUserManager.getAIUserById(account) != null) {
      V2NIMAIUser aiUser = AIUserManager.getAIUserById(account);
      return TextUtils.isEmpty(aiUser.getName()) ? account : aiUser.getName();
    }
    return ChatUserCache.getInstance().getUserNick(account, type);
  }

  /**
   * 获取消息发送者头像
   *
   * @param account 发送者账号ID
   * @return 头像地址
   */
  public static String getChatCacheAvatar(String account, V2NIMConversationType type) {
    if (account == null) {
      return null;
    }
    //数字人直接返回
    if (AIUserManager.getAIUserById(account) != null) {
      return AIUserManager.getAIUserById(account).getAvatar();
    }
    if (account.equals(IMKitClient.account()) && IMKitClient.currentUser() != null) {
      return IMKitClient.currentUser().getAvatar();
    }
    return ChatUserCache.getInstance().getAvatar(account, type);
  }

  /**
   * 获取消息发送者头像名称，用于当用户头像不存在时展示
   *
   * @param account 发送者账号ID
   * @param type 会话类型
   * @return 头像名称
   */
  public static String getChatCacheAvatarName(String account, V2NIMConversationType type) {
    //数字人直接返回
    if (AIUserManager.getAIUserById(account) != null) {
      V2NIMAIUser aiUser = AIUserManager.getAIUserById(account);
      return TextUtils.isEmpty(aiUser.getName()) ? account : aiUser.getName();
    }
    if (account.equals(IMKitClient.account())) {
      return TextUtils.isEmpty(IMKitClient.currentUser().getName())
          ? account
          : IMKitClient.currentUser().getName();
    }
    return ChatUserCache.getInstance().getNickname(account, type);
  }

  /**
   * 消息搜素结果页面，获取消息昵称
   *
   * @param message 消息体
   * @return UI展示昵称
   */
  public static String getChatSearchMessageUserName(V2NIMMessage message) {
    String name = null;
    if (message != null) {
      name = getChatMessageUserNameByAccount(message.getSenderId(), message.getConversationType());
    }
    return name;
  }

  /**
   * 获取消回复消息时，展示被回复消息内容。在输入框回复消息时展示
   *
   * @param messageInfo 消息体
   * @return UI展示昵称
   */
  public static String getReplyMessageTips(IMMessageInfo messageInfo) {
    if (messageInfo == null) {
      return "...";
    }
    String nickName =
        getChatMessageUserNameByAccount(
            getRealMessageSenderId(messageInfo.getMessage()),
            messageInfo.getMessage().getConversationType());
    String content = getMsgBrief(messageInfo, false);
    return nickName + ": " + content;
  }

  /**
   * 查询回复消息消息体，异步方法
   *
   * @param replyMessage 回复
   */
  public static void getReplyMessageInfo(
      V2NIMMessageRefer replyMessage, FetchCallback<IMMessageInfo> callback) {
    if (replyMessage == null) {
      callback.onSuccess(null);
      return;
    }
    List<V2NIMMessageRefer> refers = new ArrayList<>();
    refers.add(replyMessage);
    ChatRepo.getMessageListByRefers(
        refers,
        new FetchCallback<List<IMMessageInfo>>() {
          @Override
          public void onError(int errorCode, @Nullable String errorMsg) {
            callback.onError(errorCode, errorMsg);
          }

          @Override
          public void onSuccess(@Nullable List<IMMessageInfo> data) {
            if (data != null && !data.isEmpty()) {
              callback.onSuccess(data.get(0));
            } else {
              callback.onSuccess(null);
            }
          }
        });
  }

  /**
   * 获取回复消息内容，在消息列表中展示。将消息内容和发送者进行拼接展示
   *
   * @param messageInfo 消息体
   * @return UI展示昵称
   */
  public static String getReplyContent(IMMessageInfo messageInfo) {
    String result;
    if (messageInfo == null) {
      result = IMKitClient.getApplicationContext().getString(R.string.chat_message_removed_tip);
    } else {
      String nickName = messageInfo.getFromUserName();
      String content = getMsgBrief(messageInfo, false);
      result = nickName + ": " + content;
    }
    return result;
  }

  /**
   * 获取回复消息需要展示消息的内容，支持自定义配置
   *
   * @param messageInfo 消息体
   * @return UI展示消息体内容
   */
  public static String getMsgBrief(IMMessageInfo messageInfo, boolean showLocationDetail) {
    if (ChatKitClient.getChatUIConfig() != null
        && ChatKitClient.getChatUIConfig().chatCustom != null) {
      return ChatKitClient.getChatUIConfig()
          .chatCustom
          .getMessageBrief(messageInfo, showLocationDetail);
    }
    return chatCustom.getMessageBrief(messageInfo, showLocationDetail);
  }

  /**
   * 获取撤回消息，消息的内容
   *
   * @param messageInfo 消息体
   * @return UI展示消息体内容
   */
  public static String getMessageRevokeContent(IMMessageInfo messageInfo) {
    Map<String, Object> localExtension =
        MessageExtensionHelper.parseJsonStringToMap(messageInfo.getMessage().getLocalExtension());
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

  /**
   * 获取撤回富文本消息，消息的内容
   *
   * @param messageInfo 消息体，消息必须为富文本消息
   * @return UI展示消息体内容
   */
  public static Map<String, String> getRichMessageRevokeContent(IMMessageInfo messageInfo) {

    Map<String, Object> localExtension =
        MessageExtensionHelper.parseJsonStringToMap(messageInfo.getMessage().getLocalExtension());
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

  /**
   * 识别消息内容中的表情，并将表情符替换为表情图片，设置到TextView中
   *
   * @param context 上下文
   * @param textView TextView
   * @param value 消息内容
   * @param align 图片对齐方式
   */
  public static void identifyFaceExpression(
      Context context, View textView, String value, int align) {
    identifyFaceExpression(context, textView, value, align, DEF_SCALE);
  }

  /**
   * 识别消息内容中的表情，将表情符替换为表情图片，识别文本消息内容中的@信息并高亮，设置到TextView中 用于普通的文本消息
   *
   * @param context 上下文
   * @param textView TextView
   * @param message 消息体
   */
  public static void identifyExpression(Context context, View textView, V2NIMMessage message) {
    identifyExpression(context, textView, message.getText(), message);
  }

  /**
   * 识别消息内容中的表情，将表情符替换为表情图片，识别文本消息内容中的@信息并高亮，设置到TextView中 用于富文本自定义消息，需要从自定义内容中解析出文本内容，再进行表情和@信息识别
   *
   * @param context 上下文
   * @param textView TextView
   * @param content 消息内容
   * @param message 消息体
   */
  public static void identifyExpression(
      Context context, View textView, String content, V2NIMMessage message) {
    if (message != null && textView != null) {
      SpannableString spannableString =
          replaceEmoticons(context, content, DEF_SCALE, ImageSpan.ALIGN_BOTTOM);
      int color = context.getResources().getColor(AT_HIGHLIGHT);
      // 如果是AI消息，不需要高亮@
      if (!MessageHelper.isReceivedMessageFromAi(message)) {
        identifyAtExpression(context, spannableString, color, content, message);
      }
      viewSetText(textView, spannableString);
    }
  }

  /**
   * 识别文本消息内容中的@信息并高亮
   *
   * @param context 上下文
   * @param spannableString 设置高亮的文本内容
   * @param color 高亮部分颜色值
   * @param content 消息内容
   * @param message 消息体
   */
  public static void identifyAtExpression(
      Context context,
      SpannableString spannableString,
      int color,
      String content,
      V2NIMMessage message) {
    AtContactsModel atContactsModel = getAitBlockFromMsg(message);
    if (atContactsModel != null && !TextUtils.isEmpty(content)) {
      List<AitBlock> blockList = atContactsModel.getAtBlockList();
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

  /**
   * 识别富文本消息内容中的@信息并高亮，输入中的消息，此时还没有产生V2NIMMessage
   *
   * @param context 上下文
   * @param textView TextView
   * @param content 消息内容
   * @param atContactsModel @信息
   */
  public static void identifyExpressionForRichTextMsg(
      Context context, View textView, String content, AtContactsModel atContactsModel) {
    if (textView != null) {
      SpannableString spannableString =
          replaceEmoticons(context, content, DEF_SCALE, ImageSpan.ALIGN_BOTTOM);
      int color = context.getResources().getColor(AT_HIGHLIGHT);
      identifyAtExpression(context, spannableString, color, content, atContactsModel);
      viewSetText(textView, spannableString);
    }
  }

  /**
   * 将文本消息和@信息进行解析，并设置高亮色到SpannableString中
   *
   * @param context 上下文
   * @param spannableString 设置高亮的文本内容
   * @param color 高亮部分颜色值
   * @param content 消息内容
   * @param atContactsModel @信息
   */
  public static void identifyAtExpression(
      Context context,
      SpannableString spannableString,
      int color,
      String content,
      AtContactsModel atContactsModel) {
    if (atContactsModel != null && !TextUtils.isEmpty(content)) {
      List<AitBlock> blockList = atContactsModel.getAtBlockList();
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

  /**
   * 生成@信息高亮的SpannableString
   *
   * @param content 消息内容
   * @return 高亮的SpannableString
   */
  public static SpannableString generateAtSpanString(String content) {
    SpannableString spannableString = new SpannableString(content);
    int color = IMKitClient.getApplicationContext().getResources().getColor(AT_HIGHLIGHT);
    ForegroundColorSpan colorSpan = new ForegroundColorSpan(color);
    spannableString.setSpan(colorSpan, 0, content.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    return spannableString;
  }

  /**
   * 获取消息内容中的@信息
   *
   * @param message 消息体
   * @return @信息
   */
  public static AtContactsModel getAitBlockFromMsg(V2NIMMessage message) {
    if (message != null && message.getServerExtension() != null) {

      Map<String, Object> remoteExt =
          MessageExtensionHelper.parseJsonStringToMap(message.getServerExtension());
      if (remoteExt != null && remoteExt.containsKey(ChatKitUIConstant.AIT_REMOTE_EXTENSION_KEY)) {
        Object aitData = remoteExt.get(ChatKitUIConstant.AIT_REMOTE_EXTENSION_KEY);
        if (aitData instanceof Map) {
          JSONObject aitJson = new JSONObject((Map) aitData);
          return AtContactsModel.parseFromJson(aitJson);
        }
      }
    }
    return null;
  }

  // 设置TextView的内容
  private static void viewSetText(View textView, SpannableString mSpannableString) {
    if (textView instanceof TextView) {
      ((TextView) textView).setText(mSpannableString);
    }
  }

  /**
   * 判断撤回的消息是否可以重新编辑（撤回两分钟允许重新编辑）
   *
   * @param data 消息体
   * @return 是否可以重新编辑，可以则将内容展示在输入框中
   */
  public static boolean revokeMsgIsEdit(ChatMessageBean data) {
    V2NIMMessage message = data.getMessageData().getMessage();
    return !isReceivedMessage(data)
        && canRevokeEdit(data.getMessageData().getMessage())
        && (System.currentTimeMillis() - message.getCreateTime() < REVOKE_TIME_INTERVAL)
        && data.revokeMsgEdit;
  }

  /**
   * 判断消息是否为接受到的消息（非自己发送消息） 如果是AI消息，也认为是接受到的消息
   *
   * @param message 消息体
   * @return 是否为接受到的消息
   */
  public static boolean isReceivedMessage(ChatMessageBean message) {
    if (isReceivedMessageFromAi(message.getMessageData().getMessage())) {
      return true;
    }
    return !message.getMessageData().getMessage().isSelf()
        && !TextUtils.isEmpty(message.getSenderId());
  }

  /**
   * 是否是数字人发送的消息
   *
   * @param message 消息
   * @return 是否是数字人发送的消息
   */
  public static boolean isReceivedMessageFromAi(V2NIMMessage message) {
    V2NIMMessageAIConfig aiConfig = message.getAIConfig();
    if (aiConfig != null) {
      return aiConfig.getAIStatus() == V2NIMMessageAIStatus.V2NIM_MESSAGE_AI_STATUS_RESPONSE
          && !TextUtils.isEmpty(aiConfig.getAccountId());
    }
    return false;
  }

  /**
   * 获取真正的消息发送ID
   *
   * @param message 消息体
   * @return 消息发送ID
   */
  public static String getRealMessageSenderId(V2NIMMessage message) {
    V2NIMMessageAIConfig aiConfig = message.getAIConfig();
    if (aiConfig != null
        && aiConfig.getAIStatus() == V2NIMMessageAIStatus.V2NIM_MESSAGE_AI_STATUS_RESPONSE
        && !TextUtils.isEmpty(aiConfig.getAccountId())) {
      return aiConfig.getAccountId();
    }
    return message.getSenderId();
  }

  /**
   * 判断消息是否是Thread回复消息
   *
   * @param message 消息体
   * @return 是否可以撤回编辑
   */
  public static boolean isThreadReplayInfo(ChatMessageBean message) {
    return message != null
        && message.getMessageData().getMessage().getThreadReply() != null
        && !TextUtils.isEmpty(
            message.getMessageData().getMessage().getThreadReply().getMessageClientId());
  }

  /**
   * 识别emoji表情（发送过程中，emoji表情是按照编码发送），并替换成图片设置到TextView中
   *
   * @param context 上下文
   * @param textView TextView
   * @param value 消息内容
   * @param align emoji表情图片对齐方式
   * @param scale emoji表情图片缩放比例
   */
  public static void identifyFaceExpression(
      Context context, View textView, String value, int align, float scale) {
    SpannableString spannableString = replaceEmoticons(context, value, scale, align);
    viewSetText(textView, spannableString);
  }

  /**
   * 替换emoji表情（发送过程中，emoji表情是按照编码发送）
   *
   * @param context 上下文
   * @param value 消息内容
   * @param align emoji表情图片对齐方式
   */
  public static SpannableString replaceEmoticons(
      Context context, String value, float scale, int align) {
    if (TextUtils.isEmpty(value)) {
      value = "";
    }

    SpannableString mSpannableString = new SpannableString(value);
    Matcher matcher = ChatEmojiManager.INSTANCE.getPattern().matcher(value);
    while (matcher.find()) {
      int start = matcher.start();
      int end = matcher.end();
      String emote = value.substring(start, end);
      Drawable d = ChatEmojiManager.INSTANCE.getEmoteDrawable(emote, scale);
      if (d != null) {
        ImageSpan span = new ImageSpan(d, align);
        mSpannableString.setSpan(span, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
      }
    }
    return mSpannableString;
  }

  /**
   * 替换emoji表情（发送过程中，emoji表情是按照编码发送）
   *
   * @param context 上下文
   * @param spannableString 消息内容
   * @param start 开始位置
   * @param count 替换长度
   * @return 是否替换成功
   */
  public static boolean replaceEmoticons(
      Context context, SpannableString spannableString, int start, int count) {
    if (count <= 0 || spannableString.length() < start + count) return false;

    boolean result = false;
    CharSequence s = spannableString.subSequence(start, start + count);
    Matcher matcher = ChatEmojiManager.INSTANCE.getPattern().matcher(s);
    while (matcher.find()) {
      int from = start + matcher.start();
      int to = start + matcher.end();
      String emote = spannableString.subSequence(from, to).toString();
      Drawable d = ChatEmojiManager.INSTANCE.getEmoteDrawable(emote, SMALL_SCALE);
      if (d != null) {
        ImageSpan span = new ImageSpan(d, ImageSpan.ALIGN_CENTER);
        spannableString.setSpan(span, from, to, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        result = true;
      }
    }
    return result;
  }

  public static Map<String, Object> createReplyExtension(
      Map<String, Object> remote, V2NIMMessage replyMsg) {
    if (replyMsg != null) {

      if (remote == null) {
        remote = new HashMap<>();
      }
      Map<String, Object> replyInfo = new HashMap<>();
      replyInfo.put(ChatKitUIConstant.REPLY_UUID_KEY, replyMsg.getMessageClientId());
      replyInfo.put(ChatKitUIConstant.REPLY_TYPE_KEY, replyMsg.getConversationType().getValue());
      replyInfo.put(ChatKitUIConstant.REPLY_FROM_KEY, replyMsg.getSenderId());
      replyInfo.put(ChatKitUIConstant.REPLY_TO_KEY, replyMsg.getConversationId());
      replyInfo.put(ChatKitUIConstant.REPLY_RECEIVE_ID_KEY, replyMsg.getReceiverId());
      replyInfo.put(ChatKitUIConstant.REPLY_SERVER_ID_KEY, replyMsg.getMessageServerId());
      replyInfo.put(ChatKitUIConstant.REPLY_TIME_KEY, replyMsg.getCreateTime());
      remote.put(ChatKitUIConstant.REPLY_REMOTE_EXTENSION_KEY, replyInfo);
    }
    return remote;
  }

  public static void clearAitAndReplyInfo(V2NIMMessage message) {
    if (message != null && message.getServerExtension() != null) {
      Map<String, Object> remote =
          MessageExtensionHelper.parseJsonStringToMap(message.getServerExtension());
      if (remote != null) {
        remote.remove(ChatKitUIConstant.REPLY_REMOTE_EXTENSION_KEY);
        remote.remove(ChatKitUIConstant.AIT_REMOTE_EXTENSION_KEY);
        message.setServerExtension(new JSONObject(remote).toString());
      }
    }
  }

  /**
   * 复制消息内容到剪切板
   *
   * @param messageInfo 消息体
   * @param showToast 是否显示Toast提示
   */
  public static void copyTextMessage(IMMessageInfo messageInfo, boolean showToast) {
    String text = null;
    if (messageInfo.getMessage().getMessageType() == V2NIMMessageType.V2NIM_MESSAGE_TYPE_TEXT) {
      text = messageInfo.getMessage().getText();
    } else if (messageInfo.getMessage().getMessageType()
        == V2NIMMessageType.V2NIM_MESSAGE_TYPE_CUSTOM) {
      // 复制换行消息
      CustomAttachment attachment = messageInfo.getAttachment();
      if (attachment instanceof RichTextAttachment) {
        text = ((RichTextAttachment) attachment).body;
        if (TextUtils.isEmpty(text)) {
          text = ((RichTextAttachment) attachment).title;
        }
      }
    }
    if (text != null) {
      copyText(text, showToast);
    }
  }

  /**
   * 复制文本到剪切板
   *
   * @param text 文本内容
   * @param showToast 是否显示Toast提示
   */
  public static void copyText(String text, boolean showToast) {
    if (TextUtils.isEmpty(text)) {
      return;
    }
    ClipboardManager cmb =
        (ClipboardManager)
            IMKitClient.getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
    ClipData clipData = ClipData.newPlainText(null, text);
    if (clipData != null) {
      cmb.setPrimaryClip(clipData);
      if (showToast) {
        ToastX.showShortToast(R.string.chat_message_action_copy_success);
      }
    }
  }

  public static void saveLocalMessageForOthersRevokeMessage(
      V2NIMMessageRevokeNotification revokeNotification) {
    V2NIMMessage revokeMessage =
        V2NIMMessageCreator.createTextMessage(
            IMKitClient.getApplicationContext()
                .getResources()
                .getString(R.string.chat_message_revoke_content));
    Map<String, Object> map = new HashMap<>(4);
    map.put(KEY_REVOKE_TAG, true);
    map.put(KEY_REVOKE_TIME_TAG, SystemClock.elapsedRealtime());
    map.put(RouterConstant.KEY_REVOKE_EDIT_TAG, false);
    revokeMessage.setLocalExtension(new JSONObject(map).toString());
    ChatRepo.insertMessageToLocal(
        revokeMessage,
        revokeNotification.getMessageRefer().getConversationId(),
        revokeNotification.getRevokeAccountId(),
        revokeNotification.getMessageRefer().getCreateTime(),
        null);
    ALog.d(LIB_TAG, TAG, "saveLocalMessageForOthersRevokeMessage");
  }

  public static void saveLocalRevokeMessage(
      V2NIMMessage message, boolean canRevokeEdit, String revokerId) {
    V2NIMMessage revokeMessage =
        V2NIMMessageCreator.createTextMessage(
            IMKitClient.getApplicationContext()
                .getResources()
                .getString(R.string.chat_message_revoke_content));
    revokeMessage.setServerExtension(message.getServerExtension());
    Map<String, Object> map = new HashMap<>(4);
    map.put(KEY_REVOKE_TAG, true);
    map.put(KEY_REVOKE_TIME_TAG, SystemClock.elapsedRealtime());
    if (message.getMessageType() == V2NIMMessageType.V2NIM_MESSAGE_TYPE_TEXT) {
      map.put(KEY_REVOKE_CONTENT_TAG, message.getText());
    } else if (message.getMessageType() == V2NIMMessageType.V2NIM_MESSAGE_TYPE_CUSTOM) {
      RichTextAttachment attachment = isRichTextMsg(message);
      if (attachment != null) {
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
    revokeMessage.setLocalExtension(new JSONObject(map).toString());
    // 本地插入消息要保证时间顺序，所以+1，会话列表才会刷新最新的消息内容
    long createTime = message.getCreateTime() + 1;
    ChatRepo.insertMessageToLocal(
        revokeMessage, message.getConversationId(), IMKitClient.account(), createTime, null);
    ALog.d(LIB_TAG, TAG, "saveLocalRevokeMessage:" + message.getCreateTime());
  }

  public static void saveLocalBlackTipMessage(String conversationId, String sendId) {
    V2NIMMessage tipMessage =
        V2NIMMessageCreator.createTipsMessage(
            IMKitClient.getApplicationContext()
                .getString(R.string.chat_message_send_message_when_in_black));
    ChatRepo.insertMessageToLocal(
        tipMessage, conversationId, sendId, tipMessage.getCreateTime(), null);
    ALog.d(LIB_TAG, TAG, "saveLocalBlackTipMessage:" + tipMessage.getCreateTime());
  }

  /**
   * 保存本地消息，用于发送音视频通话时，对方不是好友，提示消息
   *
   * @param conversationId 会话ID
   * @param senderId 发送者ID
   */
  public static void saveLocalNotFriendCallTipMessageAndNotify(
      String conversationId, String senderId) {
    V2NIMMessage tipMessage =
        V2NIMMessageCreator.createTipsMessage(
            IMKitClient.getApplicationContext()
                .getString(R.string.chat_message_not_friend_send_call));
    ChatRepo.insertMessageToLocal(
        tipMessage, conversationId, senderId, tipMessage.getCreateTime(), null);
    ALog.d(LIB_TAG, TAG, "saveLocalNotFriendCallTipMessageAndNotify:" + tipMessage.getCreateTime());
  }

  // 创建合并转发消息体内容
  public static String createMultiForwardMsg(List<IMMessageInfo> msgList) {
    if (msgList == null || msgList.isEmpty()) {
      return "";
    }

    V2NIMConversationType conversationType = msgList.get(0).getMessage().getConversationType();

    //    生成合并转发消息Str
    List<V2NIMMessage> messageList = new ArrayList<>();
    Map<String, Object> atMap = new HashMap<>();
    Map<String, Object> replyMap = new HashMap<>();
    for (int index = 0; index < msgList.size(); index++) {
      IMMessageInfo info = msgList.get(index);
      //  去除转发消息中的回复消息 和 @消息
      Map<String, Object> extension =
          MessageExtensionHelper.parseJsonStringToMap(info.getMessage().getServerExtension());
      if (extension != null) {
        if (extension.containsKey(ChatKitUIConstant.REPLY_REMOTE_EXTENSION_KEY)) {
          Object replyContent = extension.remove(ChatKitUIConstant.REPLY_REMOTE_EXTENSION_KEY);
          replyMap.put(info.getMessage().getMessageClientId(), replyContent);
        }
        if (extension.containsKey(ChatKitUIConstant.AIT_REMOTE_EXTENSION_KEY)) {
          Object atContent = extension.remove(ChatKitUIConstant.AIT_REMOTE_EXTENSION_KEY);
          atMap.put(info.getMessage().getMessageClientId(), atContent);
        }
      } else {
        extension = new HashMap<>();
      }
      String name = null;
      String avatar = null;
      if (MessageHelper.getRealMessageSenderId(info.getMessage()).equals(IMKitClient.account())
          && IMKitClient.currentUser() != null) {
        //自己
        name = IMKitClient.currentUser().getName();
        avatar = IMKitClient.currentUser().getAvatar();
      } else {
        name =
            MessageHelper.getUserNickByAccount(
                MessageHelper.getRealMessageSenderId(info.getMessage()), conversationType);
        avatar =
            MessageHelper.getChatCacheAvatar(
                MessageHelper.getRealMessageSenderId(info.getMessage()), conversationType);
      }

      extension.put(
          ChatKitUIConstant.KEY_MERGE_REMOTE_EXTENSION_NICK,
          name != null ? name : info.getMessage().getSenderId());
      extension.put(
          ChatKitUIConstant.KEY_MERGE_REMOTE_EXTENSION_AVATAR, avatar != null ? avatar : "");
      info.getMessage().setServerExtension(new JSONObject(extension).toString());
      messageList.add(info.getMessage());
    }
    String messageListStr = createForwardMessageListFileDetail(messageList);
    for (int index = 0; index < msgList.size(); index++) {
      IMMessageInfo info = msgList.get(index);
      // 去除转发消息中的回复消息 和 @消息
      Map<String, Object> extMap =
          MessageExtensionHelper.parseJsonStringToMap(info.getMessage().getServerExtension());
      if (replyMap.containsKey(info.getMessage().getMessageClientId())) {
        if (extMap == null) {
          extMap = new HashMap<>();
        }
        extMap.put(
            ChatKitUIConstant.REPLY_REMOTE_EXTENSION_KEY,
            replyMap.get(info.getMessage().getMessageClientId()));
      }
      if (atMap.containsKey(info.getMessage().getMessageClientId())) {
        if (extMap == null) {
          extMap = new HashMap<>();
        }
        extMap.put(
            ChatKitUIConstant.AIT_REMOTE_EXTENSION_KEY,
            atMap.get(info.getMessage().getMessageClientId()));
      }
      if (extMap != null) {
        extMap.remove(ChatKitUIConstant.KEY_MERGE_REMOTE_EXTENSION_NICK);
        extMap.remove(ChatKitUIConstant.KEY_MERGE_REMOTE_EXTENSION_AVATAR);
        info.getMessage().setServerExtension(new JSONObject(extMap).toString());
      }
    }

    return messageListStr;
  }

  /**
   * 创建合并转发消息体内容
   *
   * @param messages
   * @return
   */
  public static String createForwardMessageListFileDetail(List<V2NIMMessage> messages) {
    if (messages == null || messages.isEmpty()) {
      return "";
    }
    final String conversationId = messages.get(0).getConversationId();
    if (conversationId == null) {
      return "";
    }
    List<V2NIMMessage> msgList = new ArrayList<>(messages.size());
    for (V2NIMMessage msg : messages) {
      // 不是来自同一会话，不添加
      if (!conversationId.equals(msg.getConversationId())) {
        continue;
      }
      // 不是可以转发的类型，不添加
      if (msg.getMessageType() == V2NIMMessageType.V2NIM_MESSAGE_TYPE_NOTIFICATION
          || msg.getMessageType() == V2NIMMessageType.V2NIM_MESSAGE_TYPE_AVCHAT
          || msg.getMessageType() == V2NIMMessageType.V2NIM_MESSAGE_TYPE_ROBOT) {
        continue;
      }
      msgList.add(msg);
    }
    return buildHeader(0, msgList.size()) + "\n" + buildBody(msgList);
  }

  /**
   * 序列化文件的头部，占一行
   *
   * @param version 文件格式版本
   * @param message_count 消息数目
   * @return 头部字符串
   */
  private static String buildHeader(int version, int message_count) {
    JSONObject obj = new JSONObject();
    try {
      obj.put("version", version);
      //终端类型：Android
      obj.put("terminal", 1);
      obj.put("sdk_version", NIMClient.getSDKVersion());
      obj.put("message_count", message_count);
    } catch (JSONException e) {
      e.printStackTrace();
    }
    return obj.toString();
  }

  /**
   * 把需要进行上传的数据 按照约定的“数据字段字典”转换后的json格式字符串，每条记录一行，存储在文本文件中，并从第二行开始存储(因为第一行存的是head信息)
   *
   * @param msgList 要合并的消息列表
   * @return 合并后的字符串
   */
  private static String buildBody(@NonNull List<V2NIMMessage> msgList) {
    String enter = "\n";
    if (msgList.isEmpty()) {
      return "";
    }
    StringBuilder stringBuilder = new StringBuilder();
    for (V2NIMMessage msg : msgList) {
      stringBuilder.append(enter).append(V2NIMMessageConverter.messageSerialization(msg));
    }
    return stringBuilder.substring(enter.length());
  }

  /**
   * 创建合并转发消息体内容
   *
   * @param displayName 显示会话名称
   * @param fromSessionID 来源会话ID
   * @param url 跳转链接
   * @param msgList 消息列表
   * @return 合并转发消息体附件
   */
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
      if (info.getAttachment() instanceof MultiForwardAttachment) {
        if (depth < ((MultiForwardAttachment) info.getAttachment()).depth) {
          depth = ((MultiForwardAttachment) info.getAttachment()).depth;
        }
      }
      if (abstractsList.size() < ChatKitUIConstant.CHAT_FORWARD_ABSTRACTS_LIMIT) {
        String nick =
            MessageHelper.getUserNickByAccount(
                MessageHelper.getRealMessageSenderId(info.getMessage()),
                info.getMessage().getConversationType());
        MultiForwardAttachment.Abstracts abstracts =
            new MultiForwardAttachment.Abstracts(
                nick, getMsgBrief(info, false), info.getMessage().getSenderId());
        abstractsList.add(abstracts);
      }
    }
    attachment.depth = depth + 1;
    attachment.abstractsList = abstractsList;
    return attachment;
  }

  /**
   * 创建富文本消息
   *
   * @param title 标题
   * @param content 内容
   * @return 富文本消息
   */
  public static V2NIMMessage createRichTextMessage(String title, String content) {
    ALog.d(LIB_TAG, TAG, "createRichTextMessage:" + (content != null ? content.length() : "null"));
    RichTextAttachment attachment = new RichTextAttachment();
    attachment.body = content;
    attachment.title = title;
    if (!TextUtils.isEmpty(attachment.toJsonStr())) {
      return V2NIMMessageCreator.createCustomMessage(null, attachment.toJsonStr());
    }
    return null;
  }

  /** 获取pushList中的群成员账号列表 */
  public static List<String> getTeamMemberPush(List<String> pushList) {
    if (pushList != null
        && pushList.size() == 1
        && pushList.get(0).equals(AtContactsModel.ACCOUNT_ALL)) {
      return ChatUserCache.getInstance().getAllTeamMemberAccounts();
    }
    return pushList;
  }

  /**
   * 判断是否为富文本消息
   *
   * @param message 消息体
   * @return 是否为富文本消息
   */
  public static boolean isRichTextMsg(IMMessageInfo message) {
    return message != null && message.getAttachment() instanceof RichTextAttachment;
  }

  /**
   * 获取富文本消息内容附件
   *
   * @param message 消息体
   * @return 富文本消息内容
   */
  public static RichTextAttachment isRichTextMsg(V2NIMMessage message) {
    if (message == null
        || message.getAttachment() == null
        || ChatCustomMsgFactory.INSTANCE.getCustomParse() == null) {
      return null;
    }
    CustomAttachment attachment =
        ChatCustomMsgFactory.INSTANCE.getCustomParse().parse(message.getAttachment().getRaw());
    if (attachment instanceof RichTextAttachment) {
      return (RichTextAttachment) attachment;
    }
    return null;
  }

  /**
   * 是否可以撤回编辑
   *
   * @param message 消息体
   * @return 是否可以撤回编辑
   */
  public static boolean canRevokeEdit(V2NIMMessage message) {
    return message.getMessageType() == V2NIMMessageType.V2NIM_MESSAGE_TYPE_TEXT
        || isRichTextMsg(message) != null;
  }

  /**
   * 是否为离开群的消息，包括群解散和个人退出
   *
   * @param messageInfo 消息体
   * @return 是否为离开群的消息
   */
  public static boolean isDismissTeamMsg(IMMessageInfo messageInfo) {
    if (messageInfo != null
        && messageInfo.getMessage().getMessageType()
            == V2NIMMessageType.V2NIM_MESSAGE_TYPE_NOTIFICATION) {
      V2NIMMessageNotificationAttachment attachment =
          (V2NIMMessageNotificationAttachment) messageInfo.getMessage().getAttachment();
      ALog.d(LIB_TAG, TAG, "isDismissTeamMsg:" + attachment.getType());
      boolean result =
          attachment.getType()
              == V2NIMMessageNotificationType.V2NIM_MESSAGE_NOTIFICATION_TYPE_TEAM_DISMISS;
      if (attachment.getType()
              == V2NIMMessageNotificationType.V2NIM_MESSAGE_NOTIFICATION_TYPE_TEAM_LAVE
          && TextUtils.equals(messageInfo.getMessage().getSenderId(), IMKitClient.account())) {
        result = true;
      }
      return result;
    }
    return false;
  }

  /**
   * 是否为被踢出群的消息
   *
   * @param messageInfo 消息体
   * @return 是否为被踢出群的消息
   */
  public static boolean isKickMsg(IMMessageInfo messageInfo) {
    if (messageInfo != null
        && messageInfo.getMessage().getMessageType()
            == V2NIMMessageType.V2NIM_MESSAGE_TYPE_NOTIFICATION) {
      V2NIMMessageNotificationAttachment attachment =
          (V2NIMMessageNotificationAttachment) messageInfo.getMessage().getAttachment();
      if (attachment.getType()
              == V2NIMMessageNotificationType.V2NIM_MESSAGE_NOTIFICATION_TYPE_TEAM_KICK
          && attachment.getTargetIds() != null) {
        return attachment.getTargetIds().contains(IMKitClient.account());
      }
    }
    return false;
  }

  /**
   * 是否为被邀请入群的消息
   *
   * @param messageInfo 消息体
   * @return 是否为被踢出群的消息
   */
  public static boolean isTeamJoinedMsg(IMMessageInfo messageInfo) {
    if (messageInfo != null
        && messageInfo.getMessage().getMessageType()
            == V2NIMMessageType.V2NIM_MESSAGE_TYPE_NOTIFICATION) {
      V2NIMMessageNotificationAttachment attachment =
          (V2NIMMessageNotificationAttachment) messageInfo.getMessage().getAttachment();
      if (attachment.getType()
              == V2NIMMessageNotificationType.V2NIM_MESSAGE_NOTIFICATION_TYPE_TEAM_INVITE
          && attachment.getTargetIds() != null) {
        return attachment.getTargetIds().contains(IMKitClient.account());
      }
    }
    return false;
  }

  /**
   * 获取消息附件路径
   *
   * @param message 消息
   * @return 消息附件路径
   */
  public static String getMessageAttachPath(V2NIMMessage message) {
    if (message.getAttachment() instanceof V2NIMMessageFileAttachment) {
      V2NIMMessageFileAttachment attachment = (V2NIMMessageFileAttachment) message.getAttachment();
      if (!TextUtils.isEmpty(attachment.getPath())) {
        return attachment.getPath();
      }
      String subPath;
      if (message.getMessageType() == V2NIMMessageType.V2NIM_MESSAGE_TYPE_IMAGE) {
        subPath = "image/";
      } else if (message.getMessageType() == V2NIMMessageType.V2NIM_MESSAGE_TYPE_VIDEO) {
        subPath = "video/";
      } else if (message.getMessageType() == V2NIMMessageType.V2NIM_MESSAGE_TYPE_AUDIO) {
        subPath = "audio/";
      } else {
        subPath = "file/";
      }
      String ext = attachment.getExt();
      if (ext != null && !ext.startsWith(".")) {
        ext = "." + ext;
      }
      return IMKitClient.getSDKStorageDirPath() + subPath + message.getMessageClientId() + ext;
    }
    return null;
  }

  /**
   * 是否为同一个消息
   *
   * @param messageInfo 消息体
   * @param messageInfo2 消息体
   * @return 是否为同一个消息
   */
  public static boolean isSameMessage(IMMessageInfo messageInfo, IMMessageInfo messageInfo2) {
    if (messageInfo == null || messageInfo2 == null) {
      return false;
    }
    return messageInfo.equals(messageInfo2);
  }

  /** 将消息V2IMMessageInfo转换为消息体V2ChatMessageBean，V2ChatMessageBean为UI展示使用 */
  public static List<ChatMessageBean> convertToChatMessageBean(List<IMMessageInfo> messageList) {
    if (messageList == null) {
      return null;
    }
    Collections.sort(
        messageList,
        (o1, o2) -> {
          long time = o1.getMessage().getCreateTime() - o2.getMessage().getCreateTime();
          return time == 0L ? 0 : (time > 0 ? -1 : 1);
        });
    ArrayList<ChatMessageBean> result = new ArrayList<>(messageList.size());
    for (IMMessageInfo message : messageList) {
      result.add(new ChatMessageBean(message));
    }
    return result;
  }

  public static boolean isMuteNotification(V2NIMMessage message) {
    if (message != null && message.getAttachment() instanceof V2NIMMessageNotificationAttachment) {
      V2NIMMessageNotificationAttachment attachment =
          (V2NIMMessageNotificationAttachment) message.getAttachment();
      V2NIMUpdatedTeamInfo field = attachment.getUpdatedTeamInfo();

      if (field != null && field.getChatBannedMode() != null) {
        if (field.getChatBannedMode()
            == V2NIMTeamChatBannedMode.V2NIM_TEAM_CHAT_BANNED_MODE_UNBAN) {
          return false;
        } else {
          return true;
        }
      }
    }
    return false;
  }

  public static V2NIMAddCollectionParams createCollectionParams(
      String conversationName, V2NIMMessage message) {
    int type = message.getMessageType().getValue() + COLLECTION_TYPE_START;
    String data = V2NIMMessageConverter.messageSerialization(message);
    String senderName =
        getChatMessageUserNameByAccount(
            getRealMessageSenderId(message), message.getConversationType());
    String senderAvatar =
        getChatCacheAvatar(getRealMessageSenderId(message), message.getConversationType());
    JSONObject paramJson = new JSONObject();
    try {
      paramJson.put("message", data);
      paramJson.put("senderName", senderName);
      paramJson.put("avatar", senderAvatar);
      paramJson.put("conversationName", conversationName);
    } catch (JSONException e) {
      e.printStackTrace();
    }
    V2NIMAddCollectionParams params =
        V2NIMAddCollectionParams.V2NIMAddCollectionParamsBuilder.builder(type, paramJson.toString())
            .withUniqueId(message.getMessageServerId())
            .build();

    return params;
  }

  /**
   * 获取消息文本，用于AI内容识别
   *
   * @param message 消息体
   * @return 消息文本
   */
  public static String getAIContentMsg(V2NIMMessage message) {
    if (message == null) {
      return null;
    }
    if (message.getMessageType() == V2NIMMessageType.V2NIM_MESSAGE_TYPE_TEXT) {
      return message.getText();
    }
    if (message.getMessageType() == V2NIMMessageType.V2NIM_MESSAGE_TYPE_CUSTOM) {
      RichTextAttachment attachment = isRichTextMsg(message);
      if (attachment != null) {
        return attachment.title + attachment.body;
      }
    }
    return null;
  }

  /**
   * 获取Tips消息提示内容
   *
   * @param context 上下文
   * @param message 消息体
   * @return 提示内容
   */
  public static String getTipsMessageContent(Context context, V2NIMMessage message) {
    switch (message.getMessageStatus().getErrorCode()) {
      case AIErrorCode.AI_ERROR_TIPS_CODE:
        return context.getString(R.string.chat_ai_message_type_unsupport);
      case AIErrorCode.V2NIM_ERROR_CODE_FAILED_TO_REQUEST_LLM:
        return context.getString(R.string.chat_ai_error_failed_request_to_the_llm);
      case AIErrorCode.V2NIM_ERROR_CODE_AI_MESSAGES_FUNCTION_DISABLED:
        return context.getString(R.string.chat_ai_error_ai_messages_function_disabled);
      case AIErrorCode.V2NIM_ERROR_CODE_IS_NOT_AI_ACCOUNT:
        return context.getString(R.string.chat_ai_error_not_an_ai_account);
      case AIErrorCode.V2NIM_ERROR_CODE_AI_ACCOUNT_BLOCKLIST_OPERATION_NOT_ALLOWED:
        return context.getString(R.string.chat_ai_error_cannot_blocklist_an_ai_account);
      case AIErrorCode.V2NIM_ERROR_CODE_PARAMETER_ERROR:
        return context.getString(R.string.chat_ai_error_parameter);
      case AIErrorCode.V2NIM_ERROR_CODE_ACCOUNT_NOT_EXIST:
      case AIErrorCode.V2NIM_ERROR_CODE_FRIEND_NOT_EXIST:
        return context.getString(R.string.chat_ai_error_user_not_exist);
      case AIErrorCode.V2NIM_ERROR_CODE_ACCOUNT_BANNED:
        return context.getString(R.string.chat_ai_error_user_banned);
      case AIErrorCode.V2NIM_ERROR_CODE_ACCOUNT_CHAT_BANNED:
        return context.getString(R.string.chat_ai_error_user_chat_banned);
      case AIErrorCode.V2NIM_ERROR_CODE_MESSAGE_HIT_ANTISPAM:
        return context.getString(R.string.chat_ai_error_message_hit_antispam);
      case AIErrorCode.V2NIM_ERROR_CODE_TEAM_MEMBER_NOT_EXIST:
        return context.getString(R.string.chat_ai_error_team_member_not_exist);
      case AIErrorCode.V2NIM_ERROR_CODE_TEAM_NORMAL_MEMBER_CHAT_BANNED:
        return context.getString(R.string.chat_ai_error_team_normal_member_chat_banned);
      case AIErrorCode.V2NIM_ERROR_CODE_TEAM_MEMBER_CHAT_BANNED:
        return context.getString(R.string.chat_ai_error_team_member_chat_banned);
      case AIErrorCode.V2NIM_ERROR_CODE_RATE_LIMIT:
        return context.getString(R.string.chat_ai_error_rate_limit_exceeded);
      default:
        break;
    }
    return message.getText();
  }

  /**
   * 发送附言消息
   *
   * @param inputMsg 输入的脚本消息
   * @param conversationIds 会话ID列表
   */
  public static void sendNoteMessage(
      String inputMsg, List<String> conversationIds, boolean readReceiptEnabled) {
    if (TextUtils.isEmpty(inputMsg) || TextUtils.getTrimmedLength(inputMsg) <= 0) {
      return;
    }
    //delay 500ms 发送
    new Handler(Looper.getMainLooper())
        .postDelayed(
            () -> {
              for (String sessionId : conversationIds) {
                V2NIMMessage textMessage = V2NIMMessageCreator.createTextMessage(inputMsg);
                forwardMessageImpl(textMessage, sessionId, false, readReceiptEnabled);
              }
            },
            500);
  }

  /**
   * 转发消息实现
   *
   * @param message 消息体
   * @param conversationId 会话ID
   * @param showToasts 是否显示提示
   * @param readReceiptEnabled 是否开启已读回执
   */
  public static void forwardMessageImpl(
      V2NIMMessage message, String conversationId, boolean showToasts, boolean readReceiptEnabled) {
    if (message != null) {

      V2NIMMessageConfig.V2NIMMessageConfigBuilder configBuilder =
          V2NIMMessageConfig.V2NIMMessageConfigBuilder.builder();
      configBuilder.withReadReceiptEnabled(readReceiptEnabled);
      V2NIMSendMessageParams.V2NIMSendMessageParamsBuilder paramsBuilder =
          V2NIMSendMessageParams.V2NIMSendMessageParamsBuilder.builder()
              .withMessageConfig(configBuilder.build());

      //@ AI机器人代理设置
      V2NIMMessageAIConfigParams aiConfigParams = null;
      String chatId = V2NIMConversationIdUtil.conversationTargetId(conversationId);
      V2NIMAIUser aiAgent = AIUserManager.getAIUserById(chatId);
      if (aiAgent != null) {
        aiConfigParams = new V2NIMMessageAIConfigParams(aiAgent.getAccountId());
        if (!TextUtils.isEmpty(MessageHelper.getAIContentMsg(message))) {
          V2NIMAIModelCallContent content =
              new V2NIMAIModelCallContent(
                  MessageHelper.getAIContentMsg(message),
                  V2NIMMessageType.V2NIM_MESSAGE_TYPE_TEXT.getValue());
          aiConfigParams.setContent(content);
        }
      }
      if (aiConfigParams != null) {
        paramsBuilder.withAIConfig(aiConfigParams);
      }
      ChatRepo.sendMessage(
          message,
          conversationId,
          paramsBuilder.build(),
          new ProgressFetchCallback<V2NIMSendMessageResult>() {

            @Override
            public void onProgress(int progress) {
              // do nothing
            }

            @Override
            public void onSuccess(@Nullable V2NIMSendMessageResult data) {
              ALog.d(LIB_TAG, TAG, "sendMessage onSuccess -->> ");
              if (showToasts) {
                ToastX.showShortToast(R.string.chat_message_forward_success_tips);
              }
            }

            @Override
            public void onError(int errorCode, @Nullable String errorMsg) {
              ALog.d(
                  LIB_TAG, TAG, "sendMessage onError -->> " + errorCode + " errorMsg:" + errorMsg);
              if (showToasts) {
                ErrorUtils.showErrorCodeToast(IMKitClient.getApplicationContext(), errorCode);
              }
              if (errorCode == RES_IN_BLACK_LIST) {
                MessageHelper.saveLocalBlackTipMessage(conversationId, IMKitClient.account());
              }
            }
          });
    }
  }

  // 发送转发消息(单条转发)
  public static void sendForwardMessage(
      ChatMessageBean message,
      String inputMsg,
      List<String> conversationIds,
      boolean showToasts,
      boolean readReceiptEnabled) {
    ALog.d(LIB_TAG, TAG, "sendForwardMessage:" + conversationIds.size());
    if (message != null && !message.isRevoked()) {
      List<RecentForward> recentForwards = new ArrayList<>();
      for (String conversationId : conversationIds) {
        // 转发消息
        V2NIMMessage forwardMessage =
            V2NIMMessageCreator.createForwardMessage(message.getMessageData().getMessage());
        MessageHelper.clearAitAndReplyInfo(forwardMessage);
        forwardMessageImpl(forwardMessage, conversationId, showToasts, readReceiptEnabled);
        sendNoteMessage(inputMsg, Collections.singletonList(conversationId), readReceiptEnabled);
        //保存到最新转发
        String sessionId = V2NIMConversationIdUtil.conversationTargetId(conversationId);
        V2NIMConversationType sessionType =
            V2NIMConversationIdUtil.conversationType(conversationId);
        recentForwards.add(new RecentForward(sessionId, sessionType));
      }
      SettingRepo.saveRecentForward(recentForwards);
    } else {
      ToastX.showShortToast(R.string.chat_message_removed_tip);
    }
  }
  // 发送转发消息（逐条转发）
  public static void sendForwardMessages(
      String inputMsg,
      List<String> conversationIds,
      List<ChatMessageBean> messages,
      boolean showToasts,
      boolean readReceiptEnabled) {
    if (conversationIds == null
        || conversationIds.isEmpty()
        || messages == null
        || messages.isEmpty()) {
      return;
    }

    boolean hasError = false;

    //记录转发的最新会话
    List<RecentForward> recentForwards = new ArrayList<>();
    // 合并转发消息需要逆序的，所以获取消息列表是逆序。逐条转发需要正序，所以要倒序遍历
    for (int index = messages.size() - 1; index >= 0; index--) {
      ChatMessageBean message = messages.get(index);
      if (message.isRevoked()) {
        continue;
      }
      if (message.getMessageData().getMessage().getMessageType()
          == V2NIMMessageType.V2NIM_MESSAGE_TYPE_AUDIO) {
        hasError = true;
        continue;
      }
      for (String conversationId : conversationIds) {
        //转发
        V2NIMMessage forwardMessage =
            V2NIMMessageCreator.createForwardMessage(message.getMessageData().getMessage());
        MessageHelper.clearAitAndReplyInfo(forwardMessage);
        forwardMessageImpl(forwardMessage, conversationId, showToasts, readReceiptEnabled);
        if (recentForwards.isEmpty()) {
          String sessionId = V2NIMConversationIdUtil.conversationTargetId(conversationId);
          V2NIMConversationType sessionType =
              V2NIMConversationIdUtil.conversationType(conversationId);
          recentForwards.add(new RecentForward(sessionId, sessionType));
        }
      }
    }
    SettingRepo.saveRecentForward(recentForwards);
    if (hasError) {
      ToastX.showLongToast(R.string.msg_multi_forward_error_tips);
    }
    sendNoteMessage(inputMsg, conversationIds, readReceiptEnabled);
  }
}
