// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.view.ait;

import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import androidx.annotation.Nullable;
import com.netease.yunxin.kit.chatkit.model.UserInfoWithTeam;
import com.netease.yunxin.kit.chatkit.repo.ChatRepo;
import com.netease.yunxin.kit.chatkit.ui.R;
import com.netease.yunxin.kit.chatkit.ui.common.ChatUserCache;
import com.netease.yunxin.kit.chatkit.ui.model.ait.AitBlock;
import com.netease.yunxin.kit.chatkit.ui.model.ait.AitContactsModel;
import com.netease.yunxin.kit.corekit.im.IMKitClient;
import com.netease.yunxin.kit.corekit.im.model.UserInfo;
import com.netease.yunxin.kit.corekit.im.provider.FetchCallbackImpl;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;

/** Team member @ manager */
public class AitManager implements TextWatcher {
  private final Context mContext;
  private final String tid;
  private final AitContactsModel aitContactsModel;
  private AitTextChangeListener aitTextChangeListener;
  private final List<UserInfoWithTeam> teamMemberList = new ArrayList<>();

  private int curPos;
  private boolean ignoreTextChange = false;

  private int editTextStart;
  private int editTextCount;
  private int editTextBefore;
  private boolean delete;
  private boolean fetchNewInfo;

  public AitManager(Context context, String teamId) {
    this(context, teamId, true);
  }

  public AitManager(Context context, String teamId, boolean fetchNewInfo) {
    this.mContext = context;
    this.tid = teamId;
    aitContactsModel = new AitContactsModel();
    this.fetchNewInfo = fetchNewInfo;
  }

  public void setTeamMembers(List<UserInfoWithTeam> userInfoWithTeams) {
    this.teamMemberList.clear();
    for (UserInfoWithTeam member : userInfoWithTeams) {
      if (!TextUtils.equals(IMKitClient.account(), member.getTeamInfo().getAccount())) {
        this.teamMemberList.add(member);
      }
    }
  }

  public void setAitTextChangeListener(AitTextChangeListener listener) {
    this.aitTextChangeListener = listener;
  }

  public String getTid() {
    return tid;
  }

  public List<String> getAitTeamMember() {
    List<String> aitMembers = aitContactsModel.getAitTeamMember();
    for (String account : aitMembers) {
      if (TextUtils.equals(AitContactsModel.ACCOUNT_ALL, account)) {
        aitMembers.clear();
        aitMembers.add(AitContactsModel.ACCOUNT_ALL);
        return aitMembers;
      }
    }
    return aitMembers;
  }

  public void reset() {
    aitContactsModel.reset();
    ignoreTextChange = false;
    curPos = 0;
  }

  public void setAitContactsModel(AitContactsModel model) {
    if (model != null) {
      List<String> accountList = model.getAitTeamMember();
      for (String account : accountList) {
        aitContactsModel.addAitBlock(account, model.getAitBlock(account));
      }
    }
  }

  public JSONObject getAitData() {
    return aitContactsModel.getBlockJson();
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
      aitContactsModel.onDeleteText(before, count);
    } else {
      if (count <= 0 || editable.length() < start + count) {
        return;
      }
      CharSequence s = editable.subSequence(start, start + count);
      if (s.toString().equals("@") && !TextUtils.isEmpty(tid)) {

        AitContactSelectorDialog dialog = new AitContactSelectorDialog(mContext);
        dialog.setData(teamMemberList, false);
        dialog.setOnItemSelectListener(
            item -> {
              if (item == null) {
                // ait all
                insertAitMemberInner(
                    AitContactsModel.ACCOUNT_ALL,
                    mContext.getString(R.string.chat_team_ait_all),
                    curPos,
                    false);
              } else {
                UserInfo userInfo = item.getUserInfo();
                if (userInfo != null) {
                  insertAitMemberInner(
                      userInfo.getAccount(), ChatUserCache.getAitName(item), curPos, false);
                }
              }
            });
        dialog.show();
        if (fetchNewInfo) {
          ChatRepo.queryTeamMemberList(
              tid,
              true,
              new FetchCallbackImpl<List<UserInfoWithTeam>>() {
                @Override
                public void onSuccess(@Nullable List<UserInfoWithTeam> param) {
                  if (param != null) {
                    setTeamMembers(param);
                  }
                  if (dialog.isShowing()) {
                    dialog.setData(teamMemberList, true);
                  }
                }
              });
        }
      }
      aitContactsModel.onInsertText(start, s.toString());
    }
  }

  public void insertReplyAit(String account, String name) {
    insertAitMemberInner(account, name, curPos, true);
  }

  private void insertAitMemberInner(
      String account, String name, int start, boolean needInsertAitInText) {
    name = name + " ";
    String content = needInsertAitInText ? "@" + name : name;
    if (aitTextChangeListener != null) {
      ignoreTextChange = true;
      aitTextChangeListener.onTextAdd(content, start, content.length());
      ignoreTextChange = false;
    }

    aitContactsModel.onInsertText(start, content);

    int index = needInsertAitInText ? start : start - 1;
    aitContactsModel.addAitMember(account, name, index);
  }

  private boolean deleteSegment(int start, int count) {
    if (count != 1) {
      return false;
    }
    boolean result = false;
    AitBlock.AitSegment segment = aitContactsModel.findAitSegmentByEndPos(start);
    if (segment != null) {
      int length = start - segment.start;
      if (aitTextChangeListener != null) {
        ignoreTextChange = true;
        aitTextChangeListener.onTextDelete(segment.start, length);
        ignoreTextChange = false;
      }
      aitContactsModel.onDeleteText(start, length);
      result = true;
    }
    return result;
  }
}
