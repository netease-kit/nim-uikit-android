// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.view.ait;

import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import androidx.annotation.Nullable;
import com.netease.nimlib.sdk.v2.team.enums.V2NIMTeamMemberRole;
import com.netease.nimlib.sdk.v2.team.model.V2NIMTeam;
import com.netease.nimlib.sdk.v2.team.model.V2NIMTeamMember;
import com.netease.nimlib.sdk.v2.user.V2NIMUser;
import com.netease.yunxin.kit.chatkit.model.TeamMemberWithUserInfo;
import com.netease.yunxin.kit.chatkit.repo.TeamRepo;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.common.ChatUserCache;
import com.netease.yunxin.kit.chatkit.ui.common.ChatUtils;
import com.netease.yunxin.kit.chatkit.ui.model.ait.AitBlock;
import com.netease.yunxin.kit.chatkit.ui.model.ait.AtContactsModel;
import com.netease.yunxin.kit.corekit.im2.IMKitClient;
import com.netease.yunxin.kit.corekit.im2.extend.FetchCallback;
import java.util.List;
import org.json.JSONObject;

/** Team member @ manager */
public class AitManager implements TextWatcher {

  // UI风格，协同版
  public static final int STYLE_NORMAL = 0;
  // UI风格，通用版
  public static final int STYLE_FUN = 1;
  private final Context mContext;
  // 群id
  private final String tid;
  // @信息实体类
  private final AtContactsModel atContactsModel;
  // @文本输入监听
  private AitTextChangeListener aitTextChangeListener;
  // 当前光标位置
  private int curPos;
  // 是否忽略文本变化
  private boolean ignoreTextChange = false;
  // 文本输入开始位置
  private int editTextStart;
  // 文本输入数量
  private int editTextCount;
  // 文本输入前位置
  private int editTextBefore;
  // 是否删除操作
  private boolean delete;
  // 是否显示@所有成员
  private boolean showAll = true;
  // UI风格，默认协同版
  private int uiStyle = STYLE_NORMAL;
  //全局dialog
  AitContactSelectorDialog aitDialog;

  public AitManager(Context context, String teamId) {
    this.mContext = context;
    this.tid = teamId;
    atContactsModel = new AtContactsModel();
  }

  // 设置UI风格
  public void setUIStyle(int style) {
    uiStyle = style;
  }

  // 设置成员列表数据
  public void setTeamMembers(List<TeamMemberWithUserInfo> userInfoWithTeams) {
    ChatUserCache.getInstance().clearTeamMemberCache();
    for (TeamMemberWithUserInfo member : userInfoWithTeams) {
      if (TextUtils.equals(IMKitClient.account(), member.getAccountId())) {
        ChatUserCache.getInstance().setCurTeamMember(member.getTeamMember());
      }
    }
    ChatUserCache.getInstance().addTeamMembersCache(userInfoWithTeams);
    ChatUserCache.getInstance().haveLoadAllTeamMembers = true;
  }

  // 更新群信息
  public void updateTeamInfo(V2NIMTeam team) {
    this.showAll = ChatUtils.teamAllowAllMemberAt(team);
  }

  // 设置@文本输入监听
  public void setAitTextChangeListener(AitTextChangeListener listener) {
    this.aitTextChangeListener = listener;
  }

  // 获取群id
  public String getTid() {
    return tid;
  }

  // 获取@成员名称列表
  public List<String> getAitTeamMember() {
    List<String> aitMembers = atContactsModel.getAtTeamMember();
    for (String account : aitMembers) {
      if (TextUtils.equals(AtContactsModel.ACCOUNT_ALL, account)) {
        aitMembers.clear();
        aitMembers.add(AtContactsModel.ACCOUNT_ALL);
        return aitMembers;
      }
    }
    return aitMembers;
  }

  // 重置
  public void reset() {
    atContactsModel.reset();
    ignoreTextChange = false;
    curPos = 0;
  }

  // 设置@数据
  public void setAitContactsModel(AtContactsModel model) {
    if (model != null) {
      List<String> accountList = model.getAtTeamMember();
      for (String account : accountList) {
        atContactsModel.addAtBlock(account, model.getAtBlock(account));
      }
    }
  }

  // 获取@数据，转换为Json格式，放在消息体扩展字段中
  public JSONObject getAitData() {
    return atContactsModel.getBlockJson();
  }

  // 设置@数据，从消息体扩展字段中解析
  public AtContactsModel getAitContactsModel() {
    return atContactsModel;
  }

  @Override
  public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    delete = count > after;
  }

  @Override
  public void onTextChanged(CharSequence s, int start, int before, int count) {
    this.editTextStart = start;
    this.editTextCount = count;
    this.editTextBefore = before;
  }

  @Override
  public void afterTextChanged(Editable s) {
    afterTextChanged(s, editTextStart, delete ? editTextBefore : editTextCount, delete);
  }

  private void afterTextChanged(Editable editable, int start, int count, boolean delete) {
    curPos = delete ? start : count + start;
    if (ignoreTextChange) {
      return;
    }
    if (delete) {
      int before = start + count;
      if (deleteSegment(before, count)) {
        return;
      }
      atContactsModel.onDeleteText(before, count);
    } else {
      if (count <= 0 || editable.length() < start + count) {
        return;
      }
      CharSequence s = editable.subSequence(start, start + count);
      // 输入@符号，拉起@成员选择器
      if (s.toString().equals("@") && !TextUtils.isEmpty(tid)) {
        if (aitDialog == null) {
          aitDialog = new AitContactSelectorDialog(mContext);
        }
        aitDialog.setUIStyle(uiStyle);
        aitDialog.setOnItemListener(
            new AitContactSelectorDialog.ItemListener() {
              // 选择@成员
              @Override
              public void onSelect(TeamMemberWithUserInfo item) {
                if (item == null) {
                  // ait all
                  insertAitMemberInner(
                      AtContactsModel.ACCOUNT_ALL,
                      mContext.getString(R.string.chat_team_ait_all),
                      curPos,
                      false);
                } else {
                  V2NIMUser userInfo = item.getUserInfo();
                  if (userInfo != null) {
                    insertAitMemberInner(
                        userInfo.getAccountId(),
                        ChatUserCache.getInstance().getAitName(item),
                        curPos,
                        false);
                  }
                }
              }

              // 加载更多
              @Override
              public void onLoadMore() {}
            });
        if (!aitDialog.isShowing()) {
          aitDialog.show();
        }
        // 加载数据，则请求数据进行异步加载
        loadData(aitDialog);
      }
      atContactsModel.onInsertText(start, s.toString());
    }
  }

  // 加载数据，请求群成员列表
  public void loadData(AitContactSelectorDialog dialog) {
    //如果已经拉取了所有成员，则使用缓存，直接展示
    if (ChatUserCache.getInstance().haveLoadAllTeamMembers) {
      if (dialog.isShowing()) {
        dialog.setData(
            ChatUserCache.getInstance().getAllMemberWithoutCurrentUser(), true, canAtAll());
      }
      return;
    }
    //拉取所有成员并展示
    TeamRepo.queryAllTeamMemberListWithUserInfo(
        tid,
        new FetchCallback<>() {
          @Override
          public void onSuccess(@Nullable List<TeamMemberWithUserInfo> data) {
            if (data != null && !data.isEmpty()) {
              setTeamMembers(data);
            }
            if (dialog.isShowing()) {
              dialog.setData(
                  ChatUserCache.getInstance().getAllMemberWithoutCurrentUser(), true, canAtAll());
            }
          }

          @Override
          public void onError(int errorCode, @Nullable String errorMsg) {}
        });
  }

  // 插入@成员
  public void insertReplyAit(String account, String name) {
    insertAitMemberInner(account, name, curPos, true);
  }

  private void insertAitMemberInner(
      String account, String name, int start, boolean needInsertAitInText) {
    name = name + " ";
    String content = needInsertAitInText ? "@" + name : name;
    ignoreTextChange = true;
    if (aitTextChangeListener != null) {
      aitTextChangeListener.onTextAdd(content, start, content.length(), needInsertAitInText);
    }
    ignoreTextChange = false;

    atContactsModel.onInsertText(start, content);

    int index = needInsertAitInText ? start : start - 1;
    atContactsModel.addAtMember(account, name, index);
  }

  private boolean deleteSegment(int start, int count) {
    if (count != 1) {
      return false;
    }
    boolean result = false;
    AitBlock.AitSegment segment = atContactsModel.findAtSegmentByEndPos(start);
    if (segment != null) {
      int length = start - segment.start;
      ignoreTextChange = true;
      if (aitTextChangeListener != null) {
        aitTextChangeListener.onTextDelete(segment.start, length);
      }
      ignoreTextChange = false;
      atContactsModel.onDeleteText(start, length);
      result = true;
    }
    return result;
  }

  private boolean canAtAll() {
    V2NIMTeamMember teamMember =
        ChatUserCache.getInstance().getTeamMemberOnly(IMKitClient.account());
    if (teamMember == null) {
      teamMember = ChatUserCache.getInstance().getCurTeamMember();
    }
    return showAll
        || teamMember.getMemberRole() == V2NIMTeamMemberRole.V2NIM_TEAM_MEMBER_ROLE_OWNER
        || teamMember.getMemberRole() == V2NIMTeamMemberRole.V2NIM_TEAM_MEMBER_ROLE_MANAGER;
  }
}
