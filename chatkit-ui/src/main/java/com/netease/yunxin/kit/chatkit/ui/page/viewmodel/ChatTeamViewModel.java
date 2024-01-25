// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.page.viewmodel;

import static com.netease.yunxin.kit.chatkit.ui.ChatKitUIConstant.LIB_TAG;

import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.nimlib.sdk.team.model.TeamMember;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.model.IMTeamMessageReceiptInfo;
import com.netease.yunxin.kit.chatkit.model.UserInfoWithTeam;
import com.netease.yunxin.kit.chatkit.repo.ChatObserverRepo;
import com.netease.yunxin.kit.chatkit.repo.ChatRepo;
import com.netease.yunxin.kit.chatkit.repo.TeamObserverRepo;
import com.netease.yunxin.kit.chatkit.repo.TeamRepo;
import com.netease.yunxin.kit.chatkit.ui.common.ChatUserCache;
import com.netease.yunxin.kit.chatkit.ui.model.ChatMessageBean;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.corekit.event.EventCenter;
import com.netease.yunxin.kit.corekit.event.EventNotify;
import com.netease.yunxin.kit.corekit.im.custom.TeamEvent;
import com.netease.yunxin.kit.corekit.im.custom.TeamEventAction;
import com.netease.yunxin.kit.corekit.im.model.EventObserver;
import com.netease.yunxin.kit.corekit.im.provider.FetchCallback;
import com.netease.yunxin.kit.corekit.model.ErrorMsg;
import com.netease.yunxin.kit.corekit.model.ResultInfo;
import java.util.ArrayList;
import java.util.List;

/** Team chat info view model team message receipt, team member info for Team chat page */
public class ChatTeamViewModel extends ChatBaseViewModel {

  private static final String TAG = "ChatTeamViewModel";

  private final MutableLiveData<FetchResult<List<IMTeamMessageReceiptInfo>>>
      teamMessageReceiptLiveData = new MutableLiveData<>();

  private final MutableLiveData<Team> teamLiveData = new MutableLiveData<>();
  private final MutableLiveData<Team> teamRemoveLiveData = new MutableLiveData<>();
  private final MutableLiveData<ResultInfo<List<UserInfoWithTeam>>> teamMemberData =
      new MutableLiveData<>();
  private final MutableLiveData<FetchResult<List<UserInfoWithTeam>>> teamMemberChangeData =
      new MutableLiveData<>();
  private boolean myDismiss = false;

  private final EventObserver<List<IMTeamMessageReceiptInfo>> teamMessageReceiptObserver =
      new EventObserver<List<IMTeamMessageReceiptInfo>>() {
        @Override
        public void onEvent(@Nullable List<IMTeamMessageReceiptInfo> event) {
          ALog.d(LIB_TAG, TAG, "messageReceipt:" + (event == null ? "null" : event.size()));
          FetchResult<List<IMTeamMessageReceiptInfo>> receiptResult =
              new FetchResult<>(LoadStatus.Finish);
          receiptResult.setData(event);
          receiptResult.setType(FetchResult.FetchType.Update);
          receiptResult.setTypeIndex(-1);
          teamMessageReceiptLiveData.setValue(receiptResult);
        }
      };

  private final Observer<List<Team>> teamObserver =
      event -> {
        ALog.d(LIB_TAG, TAG, "teamObserver:" + (event == null ? "null" : event.size()));
        if (event == null) return;
        for (Team team : event) {
          if (TextUtils.equals(team.getId(), mSessionId)) {
            teamLiveData.setValue(team);
          }
        }
      };

  private final Observer<Team> teamRemoveObserver =
      team -> {
        ALog.d(LIB_TAG, TAG, "teamRemoveObserver");
        if (team != null && TextUtils.equals(team.getId(), mSessionId)) {
          teamRemoveLiveData.setValue(team);
        }
      };

  private final Observer<List<TeamMember>> teamMemberUpdateObserver =
      teamMemberList -> {
        ALog.d(LIB_TAG, TAG, "teamMemberUpdateObserver:" + teamMemberList.size());
        TeamRepo.fillTeamMemberList(
            teamMemberList,
            new FetchCallback<List<UserInfoWithTeam>>() {
              @Override
              public void onSuccess(@Nullable List<UserInfoWithTeam> param) {
                ChatUserCache.addUserCache(param);
                if (param != null && param.size() > 0) {
                  FetchResult<List<UserInfoWithTeam>> result = new FetchResult<>(LoadStatus.Finish);
                  result.setData(param);
                  result.setType(FetchResult.FetchType.Update);
                  teamMemberChangeData.setValue(result);
                }
              }

              @Override
              public void onFailed(int code) {
                ALog.d(LIB_TAG, TAG, "fillTeamMemberList,onFailed:" + code);
              }

              @Override
              public void onException(@Nullable Throwable exception) {
                ALog.d(LIB_TAG, TAG, "fillTeamMemberList,onException");
              }
            });
      };

  //用于记录是否为自己解散的群聊
  private final EventNotify<TeamEvent> teamDismissNotify =
      new EventNotify<TeamEvent>() {
        @Override
        public void onNotify(@NonNull TeamEvent event) {
          ALog.d(LIB_TAG, TAG, "teamDismissNotify:" + event.getTeamId() + event.getAction());
          if (TextUtils.equals(event.getTeamId(), mSessionId)
              && TextUtils.equals(event.getAction(), TeamEventAction.ACTION_DISMISS)) {
            myDismiss = true;
          } else {
            myDismiss = false;
          }
        }

        @NonNull
        @Override
        public String getEventType() {
          return "TeamEvent";
        }
      };

  public MutableLiveData<FetchResult<List<IMTeamMessageReceiptInfo>>>
      getTeamMessageReceiptLiveData() {
    return teamMessageReceiptLiveData;
  }

  public MutableLiveData<FetchResult<List<UserInfoWithTeam>>> getTeamMemberChangeData() {
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
    List<IMMessage> messages = new ArrayList<>();
    for (ChatMessageBean messageBean : messageBeans) {
      messages.add(messageBean.getMessageData().getMessage());
    }
    ChatRepo.refreshTeamMessageReceipt(messages);
  }

  /** team info change live data */
  public MutableLiveData<Team> getTeamLiveData() {
    return teamLiveData;
  }

  public MutableLiveData<Team> getTeamRemoveLiveData() {
    return teamRemoveLiveData;
  }

  /** team member info live data */
  public MutableLiveData<ResultInfo<List<UserInfoWithTeam>>> getTeamMemberData() {
    return teamMemberData;
  }

  @Override
  public void registerObservers() {
    super.registerObservers();
    ChatObserverRepo.registerTeamMessageReceiptObserve(teamMessageReceiptObserver);
    TeamObserverRepo.registerTeamUpdateObserver(teamObserver);
    TeamObserverRepo.registerTeamRemoveObserver(teamRemoveObserver);
    TeamObserverRepo.registerTeamMemberUpdateObserver(teamMemberUpdateObserver);
    EventCenter.registerEventNotify(teamDismissNotify);
  }

  @Override
  public void unregisterObservers() {
    super.unregisterObservers();
    ChatObserverRepo.unregisterTeamMessageReceiptObserve(teamMessageReceiptObserver);
    TeamObserverRepo.unregisterTeamUpdateObserver(teamObserver);
    TeamObserverRepo.unregisterTeamRemoveObserver(teamRemoveObserver);
    TeamObserverRepo.unregisterTeamMemberUpdateObserver(teamMemberUpdateObserver);
    EventCenter.unregisterEventNotify(teamDismissNotify);
  }

  @Override
  public void sendReceipt(IMMessage message) {
    ALog.d(LIB_TAG, TAG, "sendReceipt:" + (message == null ? "null" : message.getUuid()));
    if (message != null && message.needMsgAck() && showRead) {
      ChatRepo.markTeamMessageRead(message);
    }
  }

  public void requestTeamInfo(String teamId) {
    ALog.d(LIB_TAG, TAG, "requestTeamInfo:" + teamId);
    TeamRepo.getTeamInfo(
        teamId,
        new FetchCallback<Team>() {
          @Override
          public void onSuccess(@Nullable Team param) {
            ALog.d(LIB_TAG, TAG, "sendReceipt,onSuccess:" + (param == null));
            teamLiveData.setValue(param);
            ChatRepo.setCurrentTeam(param);
          }

          @Override
          public void onFailed(int code) {
            ALog.d(LIB_TAG, TAG, "sendReceipt,onFailed:" + code);
          }

          @Override
          public void onException(@Nullable Throwable exception) {
            ALog.d(LIB_TAG, TAG, "sendReceipt,onException");
          }
        });
  }

  public void requestTeamMembers(String teamId) {
    ALog.d(LIB_TAG, TAG, "requestTeamMembers:" + teamId);
    TeamRepo.queryTeamMemberListWithUserInfo(
        teamId,
        new FetchCallback<List<UserInfoWithTeam>>() {
          @Override
          public void onSuccess(@Nullable List<UserInfoWithTeam> param) {
            ALog.d(LIB_TAG, TAG, "requestTeamMembers,onSuccess:" + (param == null));
            ChatUserCache.addUserCache(param);
            teamMemberData.setValue(new ResultInfo<>(param));
          }

          @Override
          public void onFailed(int code) {
            ALog.d(LIB_TAG, TAG, "requestTeamMembers,onFailed:" + code);
            teamMemberData.setValue(new ResultInfo<>(null, false, new ErrorMsg(code)));
          }

          @Override
          public void onException(@Nullable Throwable exception) {
            ALog.d(LIB_TAG, TAG, "requestTeamMembers,onException");
            teamMemberData.setValue(new ResultInfo<>(null, false, new ErrorMsg(-1, "", exception)));
          }
        });
  }

  public boolean hasLoadMessage() {
    return hasLoadMessage;
  }
}
