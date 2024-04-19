// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.page.viewmodel;

import static com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant.LIB_TAG;

import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import com.netease.nimlib.sdk.v2.conversation.enums.V2NIMConversationType;
import com.netease.nimlib.sdk.v2.message.V2NIMMessage;
import com.netease.nimlib.sdk.v2.message.V2NIMTeamMessageReadReceipt;
import com.netease.nimlib.sdk.v2.team.V2NIMTeamListener;
import com.netease.nimlib.sdk.v2.team.model.V2NIMTeam;
import com.netease.nimlib.sdk.v2.team.model.V2NIMTeamMember;
import com.netease.nimlib.sdk.v2.user.V2NIMUser;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.impl.TeamListenerImpl;
import com.netease.yunxin.kit.chatkit.model.IMMessageInfo;
import com.netease.yunxin.kit.chatkit.model.TeamMemberWithUserInfo;
import com.netease.yunxin.kit.chatkit.repo.ChatRepo;
import com.netease.yunxin.kit.chatkit.repo.ContactRepo;
import com.netease.yunxin.kit.chatkit.repo.TeamRepo;
import com.netease.yunxin.kit.chatkit.ui.common.ChatUserCache;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.corekit.event.EventCenter;
import com.netease.yunxin.kit.corekit.event.EventNotify;
import com.netease.yunxin.kit.corekit.im2.IMKitClient;
import com.netease.yunxin.kit.corekit.im2.custom.TeamEvent;
import com.netease.yunxin.kit.corekit.im2.custom.TeamEventAction;
import com.netease.yunxin.kit.corekit.im2.extend.FetchCallback;
import com.netease.yunxin.kit.corekit.im2.listener.V2UserListener;
import com.netease.yunxin.kit.corekit.im2.model.V2UserInfo;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/** Team chat info view model team message receipt, team member info for Team chat page */
public class ChatTeamViewModel extends ChatBaseViewModel {

  private static final String TAG = "ChatTeamViewModel";

  private final MutableLiveData<FetchResult<List<V2NIMTeamMessageReadReceipt>>>
      teamMessageReceiptLiveData = new MutableLiveData<>();

  private final MutableLiveData<V2NIMTeam> teamLiveData = new MutableLiveData<>();
  private final MutableLiveData<FetchResult<List<TeamMemberWithUserInfo>>> teamMemberChangeData =
      new MutableLiveData<>();
  private boolean myDismiss = false;

  private final V2UserListener userListener =
      new V2UserListener() {
        @Override
        public void onUserChanged(@NonNull List<V2UserInfo> users) {
          if (users.size() < 1) {
            return;
          }
          List<V2NIMUser> userList = new ArrayList<>();
          for (V2UserInfo userInfo : users) {
            userList.add(userInfo.getNIMUserInfo());
          }
          List<TeamMemberWithUserInfo> members =
              ChatUserCache.getInstance().fillUserWithTeamMember(userList);
          FetchResult<List<TeamMemberWithUserInfo>> result = new FetchResult<>(LoadStatus.Finish);
          result.setData(members);
          result.setType(FetchResult.FetchType.Update);
          teamMemberChangeData.setValue(result);
        }
      };

  private final V2NIMTeamListener teamListener =
      new TeamListenerImpl() {

        @Override
        public void onTeamInfoUpdated(V2NIMTeam team) {
          ALog.d(LIB_TAG, TAG, "onTeamInfoUpdated:");
          if (team != null && TextUtils.equals(team.getTeamId(), mChatAccountId)) {
            teamLiveData.setValue(team);
          }
        }

        @Override
        public void onTeamMemberJoined(List<V2NIMTeamMember> teamMembers) {
          teamMembers = filterTeamMembers(teamMembers);
          if (teamMembers.size() < 1) {
            return;
          }
          ALog.d(LIB_TAG, TAG, "onTeamMemberJoined:" + teamMembers.size());
          ChatUserCache.getInstance()
              .fillUserInfoToTeamMember(
                  teamMembers,
                  new FetchCallback<List<TeamMemberWithUserInfo>>() {
                    @Override
                    public void onError(int errorCode, @Nullable String errorMsg) {
                      ALog.d(LIB_TAG, TAG, "onTeamMemberJoined,onError:" + errorCode);
                    }

                    @Override
                    public void onSuccess(@Nullable List<TeamMemberWithUserInfo> members) {
                      FetchResult<List<TeamMemberWithUserInfo>> result =
                          new FetchResult<>(LoadStatus.Finish);
                      result.setData(members);
                      result.setType(FetchResult.FetchType.Add);
                      teamMemberChangeData.setValue(result);
                    }
                  });
        }

        @Override
        public void onTeamMemberKicked(
            String operatorAccountId, List<V2NIMTeamMember> teamMembers) {
          teamMembers = filterTeamMembers(teamMembers);
          if (teamMembers.size() < 1) {
            return;
          }
          clearMemberCache(teamMembers);
        }

        @Override
        public void onTeamMemberLeft(List<V2NIMTeamMember> teamMembers) {
          teamMembers = filterTeamMembers(teamMembers);
          if (teamMembers.size() < 1) {
            return;
          }
          clearMemberCache(teamMembers);
        }

        @Override
        public void onTeamMemberInfoUpdated(List<V2NIMTeamMember> teamMembers) {
          teamMembers = filterTeamMembers(teamMembers);
          if (teamMembers.size() < 1) {
            return;
          }
          ALog.d(LIB_TAG, TAG, "teamMemberUpdateObserver:" + teamMembers.size());
          ChatUserCache.getInstance()
              .fillUserInfoToTeamMember(
                  teamMembers,
                  new FetchCallback<>() {
                    @Override
                    public void onError(int errorCode, @Nullable String errorMsg) {
                      ALog.d(LIB_TAG, TAG, "onTeamMemberInfoUpdated,onError:" + errorCode);
                    }

                    @Override
                    public void onSuccess(@Nullable List<TeamMemberWithUserInfo> members) {
                      FetchResult<List<TeamMemberWithUserInfo>> result =
                          new FetchResult<>(LoadStatus.Finish);
                      result.setData(members);
                      result.setType(FetchResult.FetchType.Update);
                      teamMemberChangeData.setValue(result);
                    }
                  });
        }
      };

  /**
   * 过滤出当前会话的群组成员
   *
   * @param teamMembers 群组成员列表
   * @return 当前会话的群组成员
   */
  private List<V2NIMTeamMember> filterTeamMembers(List<V2NIMTeamMember> teamMembers) {
    List<V2NIMTeamMember> members = new ArrayList<>();
    if (teamMembers == null || teamMembers.size() < 1) {
      return members;
    }
    for (V2NIMTeamMember teamMember : teamMembers) {
      if (teamMember != null && TextUtils.equals(teamMember.getTeamId(), mChatAccountId)) {
        members.add(teamMember);
      }
    }
    return members;
  }

  private void clearMemberCache(List<V2NIMTeamMember> teamMembers) {
    ChatUserCache.getInstance()
        .fillUserInfoToTeamMember(
            teamMembers,
            new FetchCallback<>() {
              @Override
              public void onError(int errorCode, @Nullable String errorMsg) {
                ALog.d(LIB_TAG, TAG, "clearMemberCache,onError:" + errorCode);
              }

              @Override
              public void onSuccess(@Nullable List<TeamMemberWithUserInfo> members) {
                FetchResult<List<TeamMemberWithUserInfo>> result =
                    new FetchResult<>(LoadStatus.Finish);
                result.setData(members);
                result.setType(FetchResult.FetchType.Remove);
                teamMemberChangeData.setValue(result);
              }
            });
  }

  @Override
  public void init(String accountId, V2NIMConversationType sessionType) {
    super.init(accountId, sessionType);
    if (IMKitClient.account() != null) {
      //查询自己在群里的信息
      TeamRepo.getTeamMember(
          mChatAccountId,
          Objects.requireNonNull(IMKitClient.account()),
          new FetchCallback<>() {
            @Override
            public void onError(int errorCode, @Nullable String errorMsg) {
              ALog.d(LIB_TAG, TAG, "getTeamMember,onError:" + errorCode);
            }

            @Override
            public void onSuccess(@Nullable V2NIMTeamMember data) {
              ALog.d(LIB_TAG, TAG, "getTeamMember,onSuccess:" + (data == null));
              ChatUserCache.getInstance().setCurTeamMember(data);
            }
          });
    }
  }

  @Override
  protected void onTeamMessageReadReceipts(List<V2NIMTeamMessageReadReceipt> readReceipts) {
    super.onTeamMessageReadReceipts(readReceipts);
    ALog.d(LIB_TAG, TAG, "messageReceipt:" + (readReceipts == null ? "null" : readReceipts.size()));
    FetchResult<List<V2NIMTeamMessageReadReceipt>> receiptResult =
        new FetchResult<>(LoadStatus.Finish);
    receiptResult.setData(readReceipts);
    receiptResult.setType(FetchResult.FetchType.Update);
    receiptResult.setTypeIndex(-1);
    teamMessageReceiptLiveData.setValue(receiptResult);
  }

  //用于记录是否为自己解散的群聊
  private final EventNotify<TeamEvent> teamDismissNotify =
      new EventNotify<TeamEvent>() {
        @Override
        public void onNotify(@NonNull TeamEvent event) {
          ALog.d(LIB_TAG, TAG, "teamDismissNotify:" + event.getTeamId() + event.getAction());
          myDismiss =
              TextUtils.equals(event.getTeamId(), mChatAccountId)
                  && TextUtils.equals(event.getAction(), TeamEventAction.ACTION_DISMISS);
        }

        @NonNull
        @Override
        public String getEventType() {
          return "TeamEvent";
        }
      };

  public MutableLiveData<FetchResult<List<V2NIMTeamMessageReadReceipt>>>
      getTeamMessageReceiptLiveData() {
    return teamMessageReceiptLiveData;
  }

  public MutableLiveData<FetchResult<List<TeamMemberWithUserInfo>>> getTeamMemberChangeData() {
    return teamMemberChangeData;
  }

  public boolean isMyDismiss() {
    return myDismiss;
  }

  public void refreshTeamMessageReceipt(List<ChatMessageBean> messageBeans) {
    ALog.d(
        LIB_TAG,
        TAG,
        "refreshTeamMessageReceipt:" + (messageBeans == null ? "null" : messageBeans.size()));
    if (messageBeans == null || messageBeans.size() < 1) {
      return;
    }
    List<V2NIMMessage> messages = new ArrayList<>();
    for (ChatMessageBean messageBean : messageBeans) {
      messages.add(messageBean.getMessageData().getMessage());
    }
    ChatRepo.refreshTeamMessageReceipt(messages);
  }

  /** team info change live data */
  public MutableLiveData<V2NIMTeam> getTeamLiveData() {
    return teamLiveData;
  }

  @Override
  public void addListener() {
    super.addListener();
    TeamRepo.addTeamListener(teamListener);
    EventCenter.registerEventNotify(teamDismissNotify);
    ContactRepo.addUserListener(userListener);
  }

  @Override
  public void removeListener() {
    super.removeListener();
    TeamRepo.removeTeamListener(teamListener);
    ContactRepo.removeUserListener(userListener);
    EventCenter.unregisterEventNotify(teamDismissNotify);
  }

  @Override
  public void sendReceipt(V2NIMMessage message) {
    ALog.d(
        LIB_TAG, TAG, "sendReceipt:" + (message == null ? "null" : message.getMessageClientId()));
    if (message != null && message.getMessageConfig().isReadReceiptEnabled() && showRead) {
      List<V2NIMMessage> msgList = new ArrayList<>();
      msgList.add(message);
      ChatRepo.markTeamMessagesRead(msgList);
    }
  }

  public void requestTeamInfo() {
    ALog.d(LIB_TAG, TAG, "requestTeamInfo:" + mChatAccountId);
    TeamRepo.getTeamInfo(
        mChatAccountId,
        new FetchCallback<>() {
          @Override
          public void onSuccess(@Nullable V2NIMTeam param) {
            ALog.d(LIB_TAG, TAG, "sendReceipt,onSuccess:" + (param == null));
            teamLiveData.setValue(param);
            ChatUserCache.getInstance().setCurrentTeam(param);
            ChatRepo.setCurrentTeam(param);
          }

          @Override
          public void onError(int errorCode, @Nullable String errorMsg) {
            ALog.d(LIB_TAG, TAG, "sendReceipt,onError:" + errorCode);
          }
        });
  }

  @Override
  protected void getTeamMemberInfoWithMessage(List<IMMessageInfo> messages) {
    super.getTeamMemberInfoWithMessage(messages);
    Set<String> memberIds = new HashSet<>();
    for (IMMessageInfo message : messages) {
      memberIds.add(message.getMessage().getSenderId());
    }
    List<String> userIds = new ArrayList<>(memberIds);
    userIds.add(IMKitClient.account());
    TeamRepo.getTeamMemberListWithUserIds(
        mChatAccountId,
        userIds,
        new FetchCallback<>() {
          @Override
          public void onError(int errorCode, @Nullable String errorMsg) {
            ALog.d(LIB_TAG, TAG, "fetchTeamMemberInfoWithMessage,onError:" + errorCode);
          }

          @Override
          public void onSuccess(@Nullable List<TeamMemberWithUserInfo> data) {
            ALog.d(LIB_TAG, TAG, "fetchTeamMemberInfoWithMessage,onSuccess:" + (data == null));
            ChatUserCache.getInstance().addTeamMembersCache(data);
            FetchResult<List<TeamMemberWithUserInfo>> result = new FetchResult<>(LoadStatus.Finish);
            result.setData(data);
            result.setType(FetchResult.FetchType.Update);
            teamMemberChangeData.setValue(result);
          }
        });
  }

  public boolean hasLoadMessage() {
    return hasLoadMessage;
  }
}
