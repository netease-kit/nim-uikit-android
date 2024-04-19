// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.common;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant;
import com.netease.yunxin.kit.corekit.im2.IMKitClient;
import com.netease.yunxin.kit.corekit.im2.custom.AitInfo;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** @数据库Helper类，用于会话列表中[有人@我]功能 提供@信息的数据库操作，增删改查 */
public class AitDBHelper extends SQLiteOpenHelper {

  private static final String TAG = "AitDBHelper";
  public static AitDBHelper aitDBHelper = null;
  private SQLiteDatabase aitDatabase = null;
  public static final String DB_NAME = "nim_kit_ait.db";
  public static final String TABLE_NAME = "ait_message";
  public static final String DB_COLUMN_SESSION = "session_id";
  public static final String DB_COLUMN_ROW_ID = "_id";
  public static final String DB_COLUMN_MSG_ID = "msg_uuid";
  public static final String DB_COLUMN_USER_ID = "account_id";
  public static int Version = 1;

  public AitDBHelper(Context context) {
    super(context, DB_NAME, null, Version);
  }

  public AitDBHelper(Context context, int version) {
    super(context, DB_NAME, null, version);
  }

  public static AitDBHelper getInstance(Context context) {
    return getInstance(context, Version);
  }

  public static AitDBHelper getInstance(Context context, int version) {
    if (aitDBHelper == null && version > 0) {
      aitDBHelper = new AitDBHelper(context, version);
    } else if (aitDBHelper == null) {
      aitDBHelper = new AitDBHelper(context);
    }
    return aitDBHelper;
  }

  public SQLiteDatabase openWrite() {
    ALog.d(ChatKitUIConstant.LIB_TAG, TAG, "openWrite");
    if (aitDatabase == null || !aitDatabase.isOpen()) {
      aitDatabase = aitDBHelper.getWritableDatabase();
    }
    return aitDatabase;
  }

  public SQLiteDatabase openRead() {
    ALog.d(ChatKitUIConstant.LIB_TAG, TAG, "openRead");
    if (aitDatabase == null || !aitDatabase.isOpen()) {
      aitDatabase = aitDBHelper.getReadableDatabase();
    }
    return aitDatabase;
  }

  public void closeDataBase() {
    if (aitDatabase != null && aitDatabase.isOpen()) {
      aitDatabase.close();
      aitDatabase = null;
    }
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    ALog.d(ChatKitUIConstant.LIB_TAG, TAG, "onCreate");
    String drop_sql = "DROP TABLE IF EXISTS " + TABLE_NAME + ";";
    db.execSQL(drop_sql);
    String create_sql =
        "CREATE TABLE IF NOT EXISTS "
            + TABLE_NAME
            + "("
            + DB_COLUMN_ROW_ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + DB_COLUMN_SESSION
            + " VARCHAR NOT NULL,"
            + DB_COLUMN_MSG_ID
            + " VARCHAR NOT NULL,"
            + DB_COLUMN_USER_ID
            + " VARCHAR NOT NULL"
            + ");";
    db.execSQL(create_sql);
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}

  public int delete(String condition) {
    // 执行删除记录动作，该语句返回删除记录的数目
    ALog.d(ChatKitUIConstant.LIB_TAG, TAG, "delete:" + condition);
    if (aitDatabase == null || !aitDatabase.isOpen()) {
      return -1;
    }
    return aitDatabase.delete(TABLE_NAME, condition, null);
  }

  public int deleteWithConversationId(String[] sessionId) {
    // 执行删除记录动作，该语句返回删除记录的数目
    ALog.d(ChatKitUIConstant.LIB_TAG, TAG, "deleteWithSessionId");
    if (sessionId == null
        || sessionId.length == 0
        || aitDatabase == null
        || !aitDatabase.isOpen()) {
      return -1;
    }
    return aitDatabase.delete(
        TABLE_NAME,
        DB_COLUMN_SESSION
            + " IN ("
            + TextUtils.join(",", Collections.nCopies(sessionId.length, "?"))
            + ")",
        sessionId);
  }

  // 删除该表所有记录
  public int deleteAll() {
    // 执行删除记录动作，该语句返回删除记录的数目
    if (aitDatabase == null || !aitDatabase.isOpen()) {
      return -1;
    }
    return aitDatabase.delete(TABLE_NAME, "1=1", null);
  }

  // 往该表添加一条记录
  public long insert(AitInfo info) {
    if (info == null) {
      return -1;
    }
    ALog.d(ChatKitUIConstant.LIB_TAG, TAG, "insert:" + info.getConversationId());
    List<AitInfo> aitInfo = new ArrayList<>();
    aitInfo.add(info);
    return insert(aitInfo);
  }

  // 往该表添加多条记录
  public long insert(List<AitInfo> aitInfoList) {
    long result = -1;
    if (aitInfoList == null
        || aitInfoList.isEmpty()
        || aitDatabase == null
        || !aitDatabase.isOpen()) {
      return result;
    }
    ALog.d(ChatKitUIConstant.LIB_TAG, TAG, "list insert:" + aitInfoList.size());
    for (AitInfo aitInfo : aitInfoList) {
      if (!TextUtils.isEmpty(aitInfo.getConversationId()) && aitInfo.getMsgUidList() != null) {
        ALog.d(
            ChatKitUIConstant.LIB_TAG, TAG, "list insert aitinfo:" + aitInfo.getConversationId());
        ContentValues cv = new ContentValues();
        cv.put(DB_COLUMN_SESSION, aitInfo.getConversationId());
        cv.put(DB_COLUMN_MSG_ID, aitInfo.getMsgUidString());
        cv.put(DB_COLUMN_USER_ID, aitInfo.getAccountId());
        // 执行插入记录动作，该语句返回插入记录的行号
        // 参数二：参数未设置为NULL,参数提供可空列名称的名称，以便在 cv 为空的情况下显式插入 NULL。
        // 参数三：values 此映射包含行的初始列值。键应该是列名，值应该是列值
        result = aitDatabase.insert(TABLE_NAME, "", cv);
      }
      // 添加成功则返回行号，添加失败则返回-1
      if (result == -1) {
        return result;
      }
    }
    ALog.d(ChatKitUIConstant.LIB_TAG, TAG, "list insert result:" + result);
    return result;
  }

  // 根据条件更新指定的表记录
  public int update(AitInfo aitInfo, String condition) {
    ALog.d(ChatKitUIConstant.LIB_TAG, TAG, "update:" + condition);
    if (aitInfo == null
        || TextUtils.isEmpty(aitInfo.getConversationId())
        || aitDatabase == null
        || !aitDatabase.isOpen()) {
      return -1;
    }
    ALog.d(ChatKitUIConstant.LIB_TAG, TAG, "update:" + aitInfo.getConversationId());
    ContentValues cv = new ContentValues();
    cv.put(DB_COLUMN_SESSION, aitInfo.getConversationId());
    cv.put(DB_COLUMN_MSG_ID, aitInfo.getMsgUidString());
    cv.put(DB_COLUMN_USER_ID, aitInfo.getAccountId());
    return aitDatabase.update(TABLE_NAME, cv, condition, null);
  }

  public int update(AitInfo aitInfo) {
    // 执行更新记录动作，该语句返回更新的记录数量
    return update(aitInfo, DB_COLUMN_ROW_ID + "=" + aitInfo.getRowId());
  }

  @SuppressLint("Range")
  public List<AitInfo> queryAll() {
    return query(DB_COLUMN_USER_ID + "='" + IMKitClient.account() + "'");
  }

  @SuppressLint("Range")
  public List<AitInfo> query(String condition) {
    ALog.d(ChatKitUIConstant.LIB_TAG, TAG, "query:" + condition);
    String sql =
        String.format(
            "select "
                + DB_COLUMN_ROW_ID
                + ","
                + DB_COLUMN_SESSION
                + ","
                + DB_COLUMN_MSG_ID
                + ","
                + DB_COLUMN_USER_ID
                + " from %s where %s;",
            TABLE_NAME,
            condition);
    List<AitInfo> infoList = new ArrayList<>();
    // 执行记录查询动作，该语句返回结果集的游标
    if (aitDatabase != null && aitDatabase.isOpen()) {

      Cursor cursor = aitDatabase.rawQuery(sql, null);
      // 循环取出游标指向的每条记录
      while (cursor != null && cursor.moveToNext()) {
        AitInfo aitInfo = new AitInfo();
        aitInfo.setConversationId(cursor.getString(cursor.getColumnIndex(DB_COLUMN_SESSION)));
        aitInfo.setRowId(cursor.getInt(cursor.getColumnIndex(DB_COLUMN_ROW_ID)));
        String msgId = cursor.getString(cursor.getColumnIndex(DB_COLUMN_MSG_ID));
        String accountID = cursor.getString(cursor.getColumnIndex(DB_COLUMN_USER_ID));
        if (!TextUtils.isEmpty(msgId)) {
          String[] idStrings = msgId.split(",");
          aitInfo.addMsgUid(Arrays.asList(idStrings));
        }
        aitInfo.setAccountId(accountID);
        ALog.d(ChatKitUIConstant.LIB_TAG, TAG, "query AitInfo:" + aitInfo.getConversationId());
        infoList.add(aitInfo);
      }
      // 查询完毕，关闭数据库游标
      cursor.close();
    }
    ALog.d(ChatKitUIConstant.LIB_TAG, TAG, "query result:" + infoList.size());
    return infoList;
  }
}
