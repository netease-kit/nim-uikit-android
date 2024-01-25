// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.common;

import static com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant.CHAT_FORWARD_USER_LIMIT;
import static com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant.CHAT_MULTI_FORWARD_DEEP_LIMIT;
import static com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant.LIB_TAG;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;
import androidx.activity.result.ActivityResultLauncher;
import com.netease.nimlib.sdk.msg.attachment.FileAttachment;
import com.netease.nimlib.sdk.msg.attachment.VideoAttachment;
import com.netease.nimlib.sdk.msg.constant.AttachStatusEnum;
import com.netease.nimlib.sdk.msg.constant.MsgStatusEnum;
import com.netease.nimlib.sdk.msg.constant.MsgTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.team.constant.TeamMemberType;
import com.netease.nimlib.sdk.team.constant.TeamTypeEnum;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.model.IMMessageInfo;
import com.netease.yunxin.kit.chatkit.model.UserInfoWithTeam;
import com.netease.yunxin.kit.chatkit.repo.ChatRepo;
import com.netease.yunxin.kit.chatkit.ui.ChatKitClient;
import com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.custom.MultiForwardAttachment;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.chatkit.ui.page.WatchImageActivity;
import com.netease.yunxin.kit.chatkit.ui.page.WatchVideoActivity;
import com.netease.yunxin.kit.common.utils.CommonFileProvider;
import com.netease.yunxin.kit.common.utils.FileUtils;
import com.netease.yunxin.kit.corekit.im.IMKitClient;
import com.netease.yunxin.kit.corekit.im.utils.IMKitConstant;
import com.netease.yunxin.kit.corekit.im.utils.RouterConstant;
import com.netease.yunxin.kit.corekit.route.XKitRouter;
import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

/** 聊天工具类，提供一些常用的方法 */
public class ChatUtils {

  public static final String TAG = "ChatUtils";

  // 使用手机应用打开文件
  public static void openFileWithApp(Context context, IMMessage message) {
    FileAttachment fileAttachment = (FileAttachment) message.getAttachment();
    File openFile = new File(fileAttachment.getPath());
    if (openFile.exists()) {
      Uri fileUri = CommonFileProvider.Companion.getUriForFile(context, openFile);
      String fileExtension = fileAttachment.getExtension();
      if (TextUtils.isEmpty(fileExtension)) {
        fileExtension = FileUtils.getFileExtension(openFile.getPath());
      }
      String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension);
      Intent fileIntent = new Intent(Intent.ACTION_VIEW);
      fileIntent.setDataAndType(fileUri, mimeType);
      fileIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
      fileIntent.addFlags(
          Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
      fileIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      context.startActivity(fileIntent);
    }
  }

  // 是否为讨论组
  public static boolean isTeamGroup(Team teamInfo) {
    String teamExtension = teamInfo.getExtension();
    if ((teamExtension != null && teamExtension.contains(IMKitConstant.TEAM_GROUP_TAG))
        || teamInfo.getType() == TeamTypeEnum.Normal) {
      return true;
    }

    return false;
  }

  public static String formatFileSize(long fileS) {
    DecimalFormat df = new DecimalFormat("#.00");
    String fileSizeString;
    String wrongSize = "0B";
    if (fileS == 0) {
      return wrongSize;
    }
    if (fileS < 1000) {
      fileSizeString = df.format((double) fileS) + "B";
    } else if (fileS < 1000000) {
      fileSizeString = df.format((double) fileS / 1000) + "KB";
    } else if (fileS < 1000000000) {
      fileSizeString = df.format((double) fileS / 1000000) + "MB";
    } else {
      fileSizeString = df.format((double) fileS / 1000000000) + "GB";
    }
    return fileSizeString;
  }

  public static boolean fileSizeLimit(long fileS) {
    long limit = getFileLimitSize();
    if (limit < 0) {
      return false;
    }
    long limitSize = limit * 1000000L;
    return fileS > limitSize;
  }

  public static Long getFileLimitSize() {
    long limit = ChatKitUIConstant.FILE_LIMIT;
    if (ChatKitClient.getChatUIConfig() != null
        && ChatKitClient.getChatUIConfig().messageProperties != null) {
      limit = ChatKitClient.getChatUIConfig().messageProperties.sendFileLimit;
    }
    return limit;
  }

  private static final Map<String, Integer> fileRes = getFileTypeMap();

  public static int getFileIcon(String type) {
    if (!TextUtils.isEmpty(type)) {
      String lowerType = type.toLowerCase(Locale.getDefault());
      Integer result = fileRes.get(lowerType);
      return result != null ? result : R.drawable.ic_unknown_file;
    }
    return R.drawable.ic_unknown_file;
  }

  public static String getUrlFileName(Context context, Uri uri) {
    Cursor resolver = context.getContentResolver().query(uri, null, null, null, null, null);
    resolver.moveToFirst();
    int nameIndex = resolver.getColumnIndex(OpenableColumns.DISPLAY_NAME);
    String displayName = "";
    if (nameIndex >= 0) {
      displayName = resolver.getString(nameIndex);
      ALog.d("ChatUtils", "pick file result uri(" + displayName + ") -->> " + uri);
    }
    resolver.close();

    return displayName;
  }

  public static String getUrlFileSize(Context context, Uri uri) {
    Cursor resolver = context.getContentResolver().query(uri, null, null, null, null, null);
    resolver.moveToFirst();
    int sizeIndex = resolver.getColumnIndex(OpenableColumns.SIZE);
    String displaySize = "0";
    if (sizeIndex >= 0) {
      displaySize = resolver.getString(sizeIndex);
      ALog.d("ChatUtils", "pick file result uri(" + displaySize + ") -->> " + uri);
    }
    resolver.close();

    return displaySize;
  }

  private static Map<String, Integer> getFileTypeMap() {
    Map<String, Integer> fileRes = new HashMap<>();
    fileRes.put("doc", R.drawable.ic_word_file);
    fileRes.put("docx", R.drawable.ic_word_file);
    fileRes.put("xls", R.drawable.ic_excel_file);
    fileRes.put("xlsx", R.drawable.ic_excel_file);
    fileRes.put("ppt", R.drawable.ic_ppt_file);
    fileRes.put("pptx", R.drawable.ic_ppt_file);
    //    fileRes.put("keynote", R.drawable.ic_ppt_file);
    fileRes.put("jpg", R.drawable.ic_image_file);
    fileRes.put("png", R.drawable.ic_image_file);
    fileRes.put("jpeg", R.drawable.ic_image_file);
    //    fileRes.put("psd", R.drawable.ic_image_file);
    fileRes.put("tiff", R.drawable.ic_image_file);
    fileRes.put("gif", R.drawable.ic_image_file);
    fileRes.put("zip", R.drawable.ic_rar_file);
    fileRes.put("7z", R.drawable.ic_rar_file);
    fileRes.put("tar", R.drawable.ic_rar_file);
    fileRes.put("rar", R.drawable.ic_rar_file);
    fileRes.put("pdf", R.drawable.ic_pdf_file);
    fileRes.put("rtf", R.drawable.ic_pdf_file);
    fileRes.put("txt", R.drawable.ic_text_file);
    fileRes.put("csv", R.drawable.ic_excel_file);
    fileRes.put("html", R.drawable.ic_html_file);
    fileRes.put("mp4", R.drawable.ic_video_file);
    fileRes.put("avi", R.drawable.ic_video_file);
    fileRes.put("wmv", R.drawable.ic_video_file);
    fileRes.put("mpeg", R.drawable.ic_video_file);
    fileRes.put("m4v", R.drawable.ic_video_file);
    fileRes.put("mov", R.drawable.ic_video_file);
    fileRes.put("asf", R.drawable.ic_video_file);
    fileRes.put("flv", R.drawable.ic_video_file);
    fileRes.put("f4v", R.drawable.ic_video_file);
    fileRes.put("rmvb", R.drawable.ic_video_file);
    fileRes.put("rm", R.drawable.ic_video_file);
    fileRes.put("3gp", R.drawable.ic_video_file);
    //    fileRes.put("vob", R.drawable.ic_video_file);
    fileRes.put("mp3", R.drawable.ic_mp3_file);
    fileRes.put("aac", R.drawable.ic_mp3_file);
    fileRes.put("wav", R.drawable.ic_mp3_file);
    fileRes.put("wma", R.drawable.ic_mp3_file);
    //    fileRes.put("cda", R.drawable.ic_mp3_file);
    fileRes.put("flac", R.drawable.ic_mp3_file);
    fileRes.put("unknown", R.drawable.ic_unknown_file);
    fileRes.put("", R.drawable.ic_unknown_file);
    return fileRes;
  }

  public static void startTeamList(
      Context context, String pagePath, ActivityResultLauncher<Intent> launcher) {
    XKitRouter.withKey(pagePath)
        .withParam(RouterConstant.KEY_TEAM_LIST_SELECT, true)
        .withContext(context)
        .navigate(launcher);
  }

  public static void startP2PSelector(
      Context context, String pagePath, String filterId, ActivityResultLauncher<Intent> launcher) {
    ArrayList<String> filterList = new ArrayList<>();
    if (!TextUtils.isEmpty(filterId)) {
      filterList.add(filterId);
    }
    XKitRouter.withKey(pagePath)
        .withParam(RouterConstant.KEY_CONTACT_SELECTOR_MAX_COUNT, CHAT_FORWARD_USER_LIMIT)
        .withContext(context)
        .withParam(RouterConstant.SELECTOR_CONTACT_FILTER_KEY, filterList)
        .navigate(launcher);
  }

  public static void startVideoCall(Context context, String sessionId) {
    XKitRouter.withKey(RouterConstant.PATH_CALL_SINGLE_PAGE)
        .withContext(context)
        .withParam(RouterConstant.KEY_CALLER_ACC_ID, IMKitClient.account())
        .withParam(RouterConstant.KEY_CALLED_ACC_ID, sessionId)
        .withParam(RouterConstant.KEY_CALL_TYPE, RouterConstant.KEY_CALL_TYPE_VIDEO)
        .navigate();
  }

  public static void startAudioCall(Context context, String sessionId) {
    XKitRouter.withKey(RouterConstant.PATH_CALL_SINGLE_PAGE)
        .withContext(context)
        .withParam(RouterConstant.KEY_CALLER_ACC_ID, IMKitClient.account())
        .withParam(RouterConstant.KEY_CALLED_ACC_ID, sessionId)
        .withParam(RouterConstant.KEY_CALL_TYPE, RouterConstant.KEY_CALL_TYPE_AUDIO)
        .navigate();
  }

  public static void watchImage(
      Context context, IMMessageInfo messageInfo, ArrayList<IMMessageInfo> imageMessages) {
    int index = 0;
    if (messageInfo.getMessage().getAttachStatus() != AttachStatusEnum.transferred
        && messageInfo.getMessage().getAttachStatus() != AttachStatusEnum.transferring) {
      ChatRepo.downloadAttachment(messageInfo.getMessage(), false, null);
    }
    ArrayList<IMMessage> messages = new ArrayList<>();
    int maxLimit = 100;
    int halfLimit = 50;
    int arraySize = imageMessages.size();
    for (int i = 0; i < arraySize; ++i) {
      if (messageInfo.equals(imageMessages.get(i))) {
        index = i;
      }
      messages.add(imageMessages.get(i).getMessage());
    }
    // 防止消息数量过多造成传递数据超限，设置消息最多100个
    if (arraySize > maxLimit) {
      int start = 0;
      int end = arraySize;
      if (index > halfLimit) {
        if (index + halfLimit >= arraySize) {
          start = arraySize - maxLimit;
          index = index - start;
        } else if (index + halfLimit < arraySize) {
          start = index - halfLimit;
          end = index + halfLimit;
          index = halfLimit;
        }
      } else {
        end = maxLimit;
      }
      messages = new ArrayList<>(messages.subList(start, end));
    }
    WatchImageActivity.launch(context, messages, index);
  }

  public static boolean watchVideo(Context context, IMMessageInfo messageInfo) {
    if (messageInfo == null) {
      return false;
    }
    IMMessage message = messageInfo.getMessage();
    if ((message.getAttachStatus() == AttachStatusEnum.transferred
            || message.getAttachStatus() == AttachStatusEnum.def)
        && !TextUtils.isEmpty(((VideoAttachment) message.getAttachment()).getPath())) {
      WatchVideoActivity.launch(context, message);
      return true;
    } else if (message.getAttachStatus() != AttachStatusEnum.transferring
        && message.getAttachment() instanceof FileAttachment) {
      ALog.d(LIB_TAG, TAG, "downloadMessageAttachment:" + message.getUuid());
      ChatRepo.downloadAttachment(message, false, null);
    }
    return false;
  }

  public static boolean openFile(Context context, IMMessageInfo messageInfo) {
    if (messageInfo == null) {
      return false;
    }
    IMMessage message = messageInfo.getMessage();
    if ((message.getAttachStatus() == AttachStatusEnum.transferred
            || message.getAttachStatus() == AttachStatusEnum.def)
        && !TextUtils.isEmpty(((FileAttachment) message.getAttachment()).getPath())) {
      ChatUtils.openFileWithApp(context, message);
      return true;
    } else if (message.getAttachStatus() != AttachStatusEnum.transferring
        && message.getAttachment() instanceof FileAttachment) {
      ALog.d(LIB_TAG, TAG, "downloadMessageAttachment:" + message.getUuid());
      ChatRepo.downloadAttachment(message, false, null);
    }
    return false;
  }

  public static boolean openForwardFile(Context context, IMMessageInfo messageInfo) {
    if (messageInfo == null) {
      return false;
    }
    IMMessage message = messageInfo.getMessage();
    if (!TextUtils.isEmpty(((FileAttachment) message.getAttachment()).getPath())) {
      ChatUtils.openFileWithApp(context, message);
      return true;
    } else if (message.getAttachStatus() != AttachStatusEnum.transferring
        && message.getAttachment() instanceof FileAttachment) {
      ALog.d(LIB_TAG, TAG, "downloadMessageAttachment:" + message.getUuid());
      ChatRepo.downloadAttachment(message, false, null);
    }
    return false;
  }

  public static Boolean teamAllowAllMemberAt(Team teamInfo) {
    if (teamInfo == null || TextUtils.isEmpty(teamInfo.getExtension())) {
      return true;
    }
    String result = ChatKitUIConstant.TYPE_EXTENSION_ALLOW_ALL;
    try {
      JSONObject obj = new JSONObject(teamInfo.getExtension());
      result =
          obj.optString(
              ChatKitUIConstant.KEY_EXTENSION_AT_ALL, ChatKitUIConstant.TYPE_EXTENSION_ALLOW_ALL);
    } catch (JSONException e) {
      ALog.e(TAG, "getTeamNotifyAllMode", e);
    }
    return TextUtils.equals(result, ChatKitUIConstant.TYPE_EXTENSION_ALLOW_ALL);
  }

  public static String getEllipsizeMiddleNick(String content) {
    return getEllipsizeMiddleStr(content, 4, 2);
  }

  public static String getEllipsizeMiddleStr(String content, int maxStartLength, int maxEndLength) {
    if (TextUtils.isEmpty(content)) {
      return "";
    }
    if (content.length() <= maxStartLength + maxEndLength) {
      return content;
    } else {
      return truncateString(content, maxStartLength) + "..." + subEndString(content, maxEndLength);
    }
  }

  // 前向截取字符串，如果包含表情则完整截取，防止不出现因为截取出现乱码
  public static String truncateString(String input, int maxLength) {
    if (input.length() <= maxLength) {
      return input;
    } else {
      StringBuilder truncated = new StringBuilder();
      int currentLength = 0;
      for (int i = 0; i < input.length(); i++) {
        char currentChar = input.charAt(i);
        if (Character.isHighSurrogate(currentChar)) {
          if (currentLength + 2 > maxLength) {
            break;
          }
          truncated.append(currentChar);
          truncated.append(input.charAt(i + 1));
          i++;
          currentLength += 2;
        } else {
          if (currentLength + 1 > maxLength) {
            break;
          }
          truncated.append(currentChar);
          currentLength += 1;
        }
      }
      return truncated.toString();
    }
  }

  // 截取字符串末尾，如果包含表情则完整截取，防止不出现因为截取出现乱码
  public static String subEndString(String input, int maxLength) {
    if (input.length() <= maxLength) {
      return input;
    } else {
      int index = input.length() - maxLength;
      if (index > 1) {
        char currentChar = input.charAt(index - 1);
        if (Character.isHighSurrogate(currentChar)) {
          index++;
        }
      }
      return input.substring(index);
    }
  }

  // 按时间排序，最新的在最后
  public static List<IMMessageInfo> sortMsgByTime(List<IMMessageInfo> msgList) {
    if (msgList == null || msgList.size() < 2) {
      return msgList;
    }
    Collections.sort(
        msgList,
        (o1, o2) -> {
          if (o1 == null || o2 == null) {
            return 0;
          }
          return (int) (o1.getMessage().getTime() - o2.getMessage().getTime());
        });
    return msgList;
  }

  /**
   * 检查单条转发消息是否符合条件
   *
   * @param msgBeanList
   * @return
   */
  public static List<ChatMessageBean> checkSingleForward(List<ChatMessageBean> msgBeanList) {
    List<ChatMessageBean> limitList = new ArrayList<>();
    if (msgBeanList != null && !msgBeanList.isEmpty()) {
      for (ChatMessageBean bean : msgBeanList) {
        if (bean.getMessageData().getMessage().getMsgType() == MsgTypeEnum.audio
            || bean.getMessageData().getMessage().getMsgType() == MsgTypeEnum.nrtc_netcall
            || bean.getMessageData().getMessage().getStatus() == MsgStatusEnum.fail) {
          limitList.add(bean);
        }
      }
    }
    return limitList;
  }

  /**
   * 检查合并转发消息是否符合条件
   *
   * @param msgBeanList
   * @return
   */
  public static List<ChatMessageBean> checkMultiForward(List<ChatMessageBean> msgBeanList) {
    List<ChatMessageBean> limitList = new ArrayList<>();
    if (msgBeanList != null && !msgBeanList.isEmpty()) {
      for (ChatMessageBean bean : msgBeanList) {
        if (bean.getMessageData().getMessage().getStatus() == MsgStatusEnum.fail) {
          limitList.add(bean);
        } else if (bean.getMessageData().getMessage().getMsgType() == MsgTypeEnum.custom
            && bean.getMessageData().getMessage().getAttachment()
                instanceof MultiForwardAttachment) {
          MultiForwardAttachment attachment =
              (MultiForwardAttachment) bean.getMessageData().getMessage().getAttachment();
          if (attachment.depth >= CHAT_MULTI_FORWARD_DEEP_LIMIT) {
            limitList.add(bean);
          }
        }
      }
    }
    return limitList;
  }

  /**
   * 群成员类型排序 群主排在第一个 管理员按照加入时间排序，并排在群主之后 普通成员按照加入时间排序，并排在管理员和群主之后
   *
   * @return
   */
  public static Comparator<UserInfoWithTeam> teamManagerComparator() {
    return (o1, o2) -> {
      if (o1 == null || o2 == null) {
        return 0;
      }
      if (o1.getTeamInfo().getType() == TeamMemberType.Owner) {
        return -1;
      } else if (o2.getTeamInfo().getType() == TeamMemberType.Owner) {
        return 1;
      } else if (o1.getTeamInfo().getType() == o2.getTeamInfo().getType()) {
        return o1.getTeamInfo().getJoinTime() < o2.getTeamInfo().getJoinTime() ? -1 : 1;
      } else {
        if (o1.getTeamInfo().getType() == TeamMemberType.Manager) {
          return -1;
        } else {
          return 1;
        }
      }
    };
  }
}
