// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.teamkit.ui.viewmodel;

import static com.netease.yunxin.kit.teamkit.ui.utils.TeamUIKitConstant.KEY_EXTENSION_AT_ALL;

import android.text.TextUtils;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import com.netease.nimlib.sdk.Observer;
import com.netease.nimlib.sdk.msg.MessageBuilder;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.CustomMessageConfig;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.msg.model.StickTopSessionInfo;
import com.netease.nimlib.sdk.team.constant.TeamInviteModeEnum;
import com.netease.nimlib.sdk.team.constant.TeamTypeEnum;
import com.netease.nimlib.sdk.team.constant.TeamUpdateModeEnum;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.nimlib.sdk.team.model.TeamMember;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.chatkit.model.TeamWithCurrentMember;
import com.netease.yunxin.kit.chatkit.model.UserInfoWithTeam;
import com.netease.yunxin.kit.chatkit.repo.ChatRepo;
import com.netease.yunxin.kit.chatkit.repo.ConversationRepo;
import com.netease.yunxin.kit.chatkit.repo.TeamObserverRepo;
import com.netease.yunxin.kit.chatkit.repo.TeamRepo;
import com.netease.yunxin.kit.common.ui.utils.ToastX;
import com.netease.yunxin.kit.common.ui.viewmodel.BaseViewModel;
import com.netease.yunxin.kit.common.ui.viewmodel.FetchResult;
import com.netease.yunxin.kit.common.ui.viewmodel.LoadStatus;
import com.netease.yunxin.kit.corekit.event.EventCenter;
import com.netease.yunxin.kit.corekit.im.IMKitClient;
import com.netease.yunxin.kit.corekit.im.custom.TeamEvent;
import com.netease.yunxin.kit.corekit.im.custom.TeamEventAction;
import com.netease.yunxin.kit.corekit.im.provider.FetchCallback;
import com.netease.yunxin.kit.corekit.model.ErrorMsg;
import com.netease.yunxin.kit.corekit.model.ResultInfo;
import com.netease.yunxin.kit.teamkit.ui.R;
import com.netease.yunxin.kit.teamkit.ui.utils.TeamUIKitConstant;
import com.netease.yunxin.kit.teamkit.ui.utils.TeamUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.json.JSONObject;

/** 群相关业务逻辑 提供数据查询接口 提供群变更监听 */
public class TeamSettingViewModel extends BaseViewModel {

  private static final String TAG = "TeamSettingViewModel";
  private static final String LIB_TAG = "TeamKit-UI";

  private final MutableLiveData<ResultInfo<TeamWithCurrentMember>> teamWithMemberData =
      new MutableLiveData<>();
  private final MutableLiveData<ResultInfo<List<UserInfoWithTeam>>> userInfoData =
      new MutableLiveData<>();
  private final MutableLiveData<ResultInfo<List<TeamMember>>> memberUpdateData =
      new MutableLiveData<>();
  private final MutableLiveData<ResultInfo<Team>> teamUpdateData = new MutableLiveData<>();
  private final MutableLiveData<ResultInfo<String>> nameData = new MutableLiveData<>();
  private final MutableLiveData<ResultInfo<String>> introduceData = new MutableLiveData<>();
  private final MutableLiveData<ResultInfo<String>> nicknameData = new MutableLiveData<>();
  private final MutableLiveData<ResultInfo<String>> iconData = new MutableLiveData<>();
  private final MutableLiveData<ResultInfo<Void>> quitTeamData = new MutableLiveData<>();
  private final MutableLiveData<ResultInfo<Void>> dismissTeamData = new MutableLiveData<>();
  private final MutableLiveData<ResultInfo<Integer>> updateInvitePrivilegeData =
      new MutableLiveData<>();
  private final MutableLiveData<ResultInfo<Integer>> updateInfoPrivilegeData =
      new MutableLiveData<>();
  private final MutableLiveData<ResultInfo<Boolean>> notifyData = new MutableLiveData<>();
  private final MutableLiveData<ResultInfo<Boolean>> stickData = new MutableLiveData<>();
  private final MutableLiveData<FetchResult<List<String>>> addRemoveMembersData =
      new MutableLiveData<>();
  private final MutableLiveData<FetchResult<List<TeamMember>>> addRemoveManagerLiveData =
      new MutableLiveData<>();
  private final MutableLiveData<ResultInfo<Boolean>> muteTeamAllMemberData =
      new MutableLiveData<>();
  private final MutableLiveData<ResultInfo<String>> updateNotifyAllPrivilegeData =
      new MutableLiveData<>();

  private String teamId;

  // 群信息变更监听
  private final Observer<List<Team>> observerForTeamUpdate =
      teams -> {
        if (teams == null) {
          return;
        }
        for (Team team : teams) {
          if (!TextUtils.equals(team.getId(), teamId)) {
            continue;
          }
          teamUpdateData.setValue(new ResultInfo<>(team));
        }
      };

  // 群成员变更监听
  private final Observer<List<TeamMember>> observerForTeamMemberUpdate =
      teamMembers -> {
        if (teamMembers == null) {
          return;
        }
        ArrayList<TeamMember> updateTeamMembers = new ArrayList<>();
        for (TeamMember item : teamMembers) {
          if (!TextUtils.equals(item.getTid(), teamId)) {
            continue;
          }
          updateTeamMembers.add(item);
        }
        if (updateTeamMembers.size() > 0) {
          memberUpdateData.setValue(new ResultInfo<>(updateTeamMembers));
        }
      };

  // 群成员移除监听
  private final Observer<List<TeamMember>> observerForTeamMemberRemove =
      teamMembers -> {
        if (teamMembers == null) {
          return;
        }
        ArrayList<String> removeList = new ArrayList<>();
        for (TeamMember item : teamMembers) {
          if (!TextUtils.equals(item.getTid(), teamId)) {
            continue;
          }
          removeList.add(item.getAccount());
        }
        if (removeList.size() > 0) {
          FetchResult<List<String>> result = new FetchResult<>(LoadStatus.Success, removeList);
          result.setType(FetchResult.FetchType.Remove);
          addRemoveMembersData.setValue(result);
        }
      };

  /** 构造函数。注册监听 */
  public TeamSettingViewModel() {
    TeamObserverRepo.registerTeamUpdateObserver(observerForTeamUpdate);
    TeamObserverRepo.registerTeamMemberUpdateObserver(observerForTeamMemberUpdate);
    TeamObserverRepo.registerTeamMemberRemoveObserver(observerForTeamMemberRemove);
  }

  /**
   * 配置群ID，在使用之前必须要调用，业务逻辑中teamId是必要参数
   *
   * @param teamId 群ID
   */
  public void configTeamId(String teamId) {
    this.teamId = teamId;
  }

  @Override
  protected void onCleared() {
    super.onCleared();
    TeamObserverRepo.unregisterTeamUpdateObserver(observerForTeamUpdate);
    TeamObserverRepo.unregisterTeamMemberUpdateObserver(observerForTeamMemberUpdate);
    TeamObserverRepo.unregisterTeamMemberRemoveObserver(observerForTeamMemberRemove);
  }

  /**
   * 获取群信息，包含群信息和当前用户在群里的成员信息
   *
   * @param teamId 群ID
   */
  public void requestTeamData(String teamId) {
    ALog.d(LIB_TAG, TAG, "requestTeamData:" + teamId);
    TeamRepo.queryTeamWithMember(
        teamId,
        Objects.requireNonNull(IMKitClient.account()),
        new FetchCallback<TeamWithCurrentMember>() {
          @Override
          public void onSuccess(@Nullable TeamWithCurrentMember param) {
            ALog.d(LIB_TAG, TAG, "requestTeamData,onSuccess:" + (param == null));
            teamWithMemberData.setValue(new ResultInfo<>(param));
          }

          @Override
          public void onFailed(int code) {
            ALog.d(LIB_TAG, TAG, "requestTeamData,onFailed:" + code);
            teamWithMemberData.setValue(new ResultInfo<>(null, false, new ErrorMsg(code)));
          }

          @Override
          public void onException(@Nullable Throwable exception) {
            ALog.d(LIB_TAG, TAG, "requestTeamData,onException");
            teamWithMemberData.setValue(
                new ResultInfo<>(null, false, new ErrorMsg(-1, "", exception)));
          }
        });
  }

  /**
   * 获取群成员列表信息
   *
   * @param teamId 群ID
   */
  public void requestTeamMembers(String teamId) {
    ALog.d(LIB_TAG, TAG, "requestTeamMembers:" + teamId);
    TeamRepo.getMemberList(
        teamId,
        new FetchCallback<List<UserInfoWithTeam>>() {
          @Override
          public void onSuccess(@Nullable List<UserInfoWithTeam> param) {
            ALog.d(
                LIB_TAG,
                TAG,
                "requestTeamMembers,onSuccess:" + (param == null ? "null" : param.size()));
            userInfoData.setValue(new ResultInfo<>(param));
          }

          @Override
          public void onFailed(int code) {
            ALog.d(LIB_TAG, TAG, "requestTeamMembers,onFailed:" + code);
            userInfoData.setValue(new ResultInfo<>(null, false, new ErrorMsg(code)));
          }

          @Override
          public void onException(@Nullable Throwable exception) {
            ALog.d(LIB_TAG, TAG, "requestTeamMembers,onException");
            userInfoData.setValue(new ResultInfo<>(null, false, new ErrorMsg(-1, "", exception)));
          }
        });
  }

  /**
   * 更新群名称
   *
   * @param teamId 群ID
   * @param name 群名称
   */
  public void updateName(String teamId, String name) {
    ALog.d(LIB_TAG, TAG, "updateName:" + teamId);
    TeamRepo.updateTeamName(
        teamId,
        name,
        new FetchCallback<Void>() {
          @Override
          public void onSuccess(@Nullable Void param) {
            ALog.d(LIB_TAG, TAG, "updateName,onSuccess");
            nameData.setValue(new ResultInfo<>(name));
          }

          @Override
          public void onFailed(int code) {
            ALog.d(LIB_TAG, TAG, "updateName,onFailed:" + code);
            nameData.setValue(new ResultInfo<>(null, false, new ErrorMsg(code)));
          }

          @Override
          public void onException(@Nullable Throwable exception) {
            ALog.d(LIB_TAG, TAG, "updateName,onException");
            nameData.setValue(new ResultInfo<>(null, false, new ErrorMsg(-1, "", exception)));
          }
        });
  }

  /**
   * 更新群介绍
   *
   * @param teamId 群ID
   * @param introduce 群介绍
   */
  public void updateIntroduce(String teamId, String introduce) {
    ALog.d(LIB_TAG, TAG, "updateIntroduce:" + teamId);
    TeamRepo.updateTeamIntroduce(
        teamId,
        introduce,
        new FetchCallback<Void>() {
          @Override
          public void onSuccess(@Nullable Void param) {
            ALog.d(LIB_TAG, TAG, "updateIntroduce,onSuccess");
            introduceData.setValue(new ResultInfo<>(introduce));
          }

          @Override
          public void onFailed(int code) {
            ALog.d(LIB_TAG, TAG, "updateIntroduce,onFailed:" + code);
            introduceData.setValue(new ResultInfo<>(null, false, new ErrorMsg(code)));
          }

          @Override
          public void onException(@Nullable Throwable exception) {
            ALog.d(LIB_TAG, TAG, "updateIntroduce,onException");
            introduceData.setValue(new ResultInfo<>(null, false, new ErrorMsg(-1, "", exception)));
          }
        });
  }

  /**
   * 更新群昵称
   *
   * @param teamId 群ID
   * @param nickname 我的群昵称
   */
  public void updateNickname(String teamId, String nickname) {
    ALog.d(LIB_TAG, TAG, "updateNickname:" + teamId);
    TeamRepo.updateMemberNick(
        teamId,
        Objects.requireNonNull(IMKitClient.account()),
        nickname,
        new FetchCallback<Void>() {
          @Override
          public void onSuccess(@Nullable Void param) {
            ALog.d(LIB_TAG, TAG, "updateNickname,onSuccess");
            nicknameData.setValue(new ResultInfo<>(nickname));
          }

          @Override
          public void onFailed(int code) {
            ALog.d(LIB_TAG, TAG, "updateNickname,onFailed:" + code);
            nicknameData.setValue(new ResultInfo<>(null, false, new ErrorMsg(code)));
          }

          @Override
          public void onException(@Nullable Throwable exception) {
            ALog.d(LIB_TAG, TAG, "updateNickname,onException");
            nicknameData.setValue(new ResultInfo<>(null, false, new ErrorMsg(-1, "", exception)));
          }
        });
  }

  /**
   * 更新群头像
   *
   * @param teamId 群ID
   * @param iconUrl 群头像URL
   */
  public void updateIcon(String teamId, String iconUrl) {
    ALog.d(LIB_TAG, TAG, "updateIcon:" + teamId);
    TeamRepo.updateTeamIcon(
        teamId,
        iconUrl,
        new FetchCallback<Void>() {
          @Override
          public void onSuccess(@Nullable Void param) {
            ALog.d(LIB_TAG, TAG, "updateIcon,onSuccess");
            iconData.setValue(new ResultInfo<>(iconUrl));
          }

          @Override
          public void onFailed(int code) {
            ALog.d(LIB_TAG, TAG, "updateIcon,onFailed:" + code);
            iconData.setValue(new ResultInfo<>(null, false, new ErrorMsg(code)));
          }

          @Override
          public void onException(@Nullable Throwable exception) {
            ALog.d(LIB_TAG, TAG, "updateIcon,onException");
            iconData.setValue(new ResultInfo<>(null, false, new ErrorMsg(-1, "", exception)));
          }
        });
  }

  /**
   * 退出群
   *
   * @param team 群信息
   */
  public void quitTeam(Team team) {
    ALog.d(LIB_TAG, TAG, "quitTeam:" + team.getId());
    if (team.getType() == TeamTypeEnum.Advanced
        && TextUtils.equals(team.getCreator(), IMKitClient.account())) {
      creatorQuitTeam(team.getId());
    } else {
      quitTeam(team.getId());
    }
  }

  /**
   * 群主退群，需要将群转让给他人
   *
   * @param teamId 群ID
   */
  public void creatorQuitTeam(String teamId) {
    ALog.d(LIB_TAG, TAG, "creatorQuitTeam:" + teamId);
    if (TextUtils.isEmpty(teamId)) {
      return;
    }

    TeamRepo.getMemberList(
        teamId,
        new FetchCallback<List<UserInfoWithTeam>>() {
          @Override
          public void onSuccess(@Nullable List<UserInfoWithTeam> param) {
            String account = IMKitClient.account();
            if (param == null || param.size() <= 1) {
              dismissTeam(teamId);
              return;
            }
            for (UserInfoWithTeam user : param) {
              if (user != null && !TextUtils.equals(account, user.getTeamInfo().getAccount())) {
                account = user.getTeamInfo().getAccount();
                break;
              }
            }
            TeamRepo.transferTeam(
                teamId,
                account,
                true,
                new FetchCallback<List<TeamMember>>() {
                  @Override
                  public void onSuccess(@Nullable List<TeamMember> param) {
                    ALog.d(LIB_TAG, TAG, "creatorQuitTeam transferTeam,onSuccess");
                    quitTeam(teamId);
                  }

                  @Override
                  public void onFailed(int code) {
                    ALog.d(LIB_TAG, TAG, "creatorQuitTeam transferTeam,onFailed:" + code);
                    quitTeamData.setValue(new ResultInfo<>(null, false, new ErrorMsg(code)));
                  }

                  @Override
                  public void onException(@Nullable Throwable exception) {
                    ALog.d(LIB_TAG, TAG, "creatorQuitTeam transferTeam,onException");
                    quitTeamData.setValue(
                        new ResultInfo<>(null, false, new ErrorMsg(-1, "", exception)));
                  }
                });
          }

          @Override
          public void onFailed(int code) {
            ALog.d(LIB_TAG, TAG, "quitTeam  getMemberList,onFailed:" + code);
            quitTeamData.setValue(new ResultInfo<>(null, false, new ErrorMsg(code)));
          }

          @Override
          public void onException(@Nullable Throwable exception) {
            ALog.d(LIB_TAG, TAG, "quitTeam getMemberList,onException");
            quitTeamData.setValue(new ResultInfo<>(null, false, new ErrorMsg(-1, "", exception)));
          }
        });
  }

  /**
   * 退出群
   *
   * @param teamId 群ID
   */
  public void quitTeam(String teamId) {
    ALog.d(LIB_TAG, TAG, "quitTeam,teamId:" + teamId);
    EventCenter.notifyEvent(new TeamEvent(teamId, TeamEventAction.ACTION_DISMISS));
    TeamRepo.quitTeam(
        teamId,
        new FetchCallback<Void>() {
          @Override
          public void onSuccess(@Nullable Void param) {
            ALog.d(LIB_TAG, TAG, "quitTeam,onSuccess");
            clearNotify(teamId);
            removeStickTop(teamId);
            quitTeamData.setValue(new ResultInfo<>(param));
          }

          @Override
          public void onFailed(int code) {
            ALog.d(LIB_TAG, TAG, "quitTeam,onFailed:" + code);
            if (code == TeamUIKitConstant.QUIT_TEAM_ERROR_CODE_NO_MEMBER) {
              dismissTeam(teamId);
              return;
            }
            quitTeamData.setValue(new ResultInfo<>(null, false, new ErrorMsg(code)));
          }

          @Override
          public void onException(@Nullable Throwable exception) {
            ALog.d(LIB_TAG, TAG, "quitTeam,onException");
            quitTeamData.setValue(new ResultInfo<>(null, false, new ErrorMsg(-1, "", exception)));
          }
        });
  }

  /**
   * 取消置顶
   *
   * @param teamId 群ID
   */
  public void removeStickTop(String teamId) {
    ALog.d(LIB_TAG, TAG, "removeStickTop,teamId:" + teamId);
    if (ConversationRepo.isStickTop(teamId, SessionTypeEnum.Team)) {
      ALog.d(LIB_TAG, TAG, "removeStickTop,isStickTop:" + teamId);
      ConversationRepo.removeStickTop(
          teamId,
          SessionTypeEnum.Team,
          "",
          new FetchCallback<Void>() {
            @Override
            public void onSuccess(@Nullable Void param) {
              ALog.d(LIB_TAG, TAG, "removeStickTop,onSuccess");
            }

            @Override
            public void onFailed(int code) {
              ALog.d(LIB_TAG, TAG, "removeStickTop,onFailed:" + code);
            }

            @Override
            public void onException(@Nullable Throwable exception) {
              ALog.d(LIB_TAG, TAG, "removeStickTop,onException");
            }
          });
    }
  }

  /**
   * 设置消息提醒
   *
   * @param teamId 群ID
   */
  public void clearNotify(String teamId) {
    ALog.d(LIB_TAG, TAG, "clearNotify,teamId:" + teamId);
    ConversationRepo.setNotify(
        teamId,
        SessionTypeEnum.Team,
        true,
        new FetchCallback<Void>() {
          @Override
          public void onSuccess(@Nullable Void param) {
            ALog.d(LIB_TAG, TAG, "clearNotify,onSuccess");
          }

          @Override
          public void onFailed(int code) {
            ALog.d(LIB_TAG, TAG, "clearNotify,onFailed:" + code);
          }

          @Override
          public void onException(@Nullable Throwable exception) {
            ALog.d(LIB_TAG, TAG, "clearNotify,onException");
          }
        });
  }

  /**
   * 解散群
   *
   * @param teamId 群ID
   */
  public void dismissTeam(String teamId) {
    ALog.d(LIB_TAG, TAG, "dismissTeam:" + teamId);
    EventCenter.notifyEvent(new TeamEvent(teamId, TeamEventAction.ACTION_DISMISS));
    TeamRepo.dismissTeam(
        teamId,
        new FetchCallback<Void>() {
          @Override
          public void onSuccess(@Nullable Void param) {
            ALog.d(LIB_TAG, TAG, "dismissTeam,onSuccess");
            dismissTeamData.setValue(new ResultInfo<>(param));
          }

          @Override
          public void onFailed(int code) {
            ALog.d(LIB_TAG, TAG, "dismissTeam,onFailed:" + code);
            dismissTeamData.setValue(new ResultInfo<>(null, false, new ErrorMsg(code)));
          }

          @Override
          public void onException(@Nullable Throwable exception) {
            ALog.d(LIB_TAG, TAG, "dismissTeam,onException");
            dismissTeamData.setValue(
                new ResultInfo<>(null, false, new ErrorMsg(-1, "", exception)));
          }
        });
  }

  /**
   * 群消息提醒
   *
   * @param teamId 群ID
   * @param notify 是否提醒
   */
  public void setTeamNotify(String teamId, boolean notify) {
    ALog.d(LIB_TAG, TAG, "setTeamNotify:" + teamId);
    ConversationRepo.setNotify(
        teamId,
        SessionTypeEnum.Team,
        !notify,
        new FetchCallback<Void>() {
          @Override
          public void onSuccess(@Nullable Void param) {
            ALog.d(LIB_TAG, TAG, "setTeamNotify,onSuccess");
            notifyData.setValue(new ResultInfo<>(notify));
          }

          @Override
          public void onFailed(int code) {
            ALog.d(LIB_TAG, TAG, "setTeamNotify,onFailed:" + code);
            notifyData.setValue(new ResultInfo<>(null, false, new ErrorMsg(code)));
          }

          @Override
          public void onException(@Nullable Throwable exception) {
            ALog.d(LIB_TAG, TAG, "muteTeam,onException");
            notifyData.setValue(new ResultInfo<>(null, false, new ErrorMsg(-1, "", exception)));
          }
        });
  }

  /**
   * 置顶
   *
   * @param sessionId 会话ID
   * @param stick 是否置顶
   */
  public void configStick(String sessionId, boolean stick) {
    ALog.d(LIB_TAG, TAG, "configStick:" + sessionId + "," + stick);
    if (TextUtils.isEmpty(sessionId)) {
      stickData.setValue(new ResultInfo<>(null, false, new ErrorMsg(-1)));
      return;
    }
    if (stick) {
      ConversationRepo.addStickTop(
          sessionId,
          SessionTypeEnum.Team,
          "",
          new FetchCallback<StickTopSessionInfo>() {
            @Override
            public void onSuccess(@Nullable StickTopSessionInfo param) {
              ALog.d(LIB_TAG, TAG, "configStick,onSuccess");
              stickData.setValue(new ResultInfo<>(true));
              ConversationRepo.notifyStickTop(sessionId, SessionTypeEnum.Team);
            }

            @Override
            public void onFailed(int code) {
              ALog.d(LIB_TAG, TAG, "configStick,onFailed:" + code);
              stickData.setValue(new ResultInfo<>(null, false, new ErrorMsg(code)));
            }

            @Override
            public void onException(@Nullable Throwable exception) {
              ALog.d(LIB_TAG, TAG, "configStick,onException");
              stickData.setValue(new ResultInfo<>(null, false, new ErrorMsg(-1, "", exception)));
            }
          });
    } else {
      ConversationRepo.removeStickTop(
          sessionId,
          SessionTypeEnum.Team,
          "",
          new FetchCallback<Void>() {
            @Override
            public void onSuccess(@Nullable Void param) {
              ALog.d(LIB_TAG, TAG, "configStick,onSuccess");
              stickData.setValue(new ResultInfo<>(false));
              ConversationRepo.notifyStickTop(sessionId, SessionTypeEnum.Team);
            }

            @Override
            public void onFailed(int code) {
              ALog.d(LIB_TAG, TAG, "configStick,onFailed:" + code);
              stickData.setValue(new ResultInfo<>(null, false, new ErrorMsg(code)));
            }

            @Override
            public void onException(@Nullable Throwable exception) {
              ALog.d(LIB_TAG, TAG, "configStick,onException");
              stickData.setValue(new ResultInfo<>(null, false, new ErrorMsg(-1, "", exception)));
            }
          });
    }
  }

  /**
   * 添加成员
   *
   * @param teamId 群ID
   * @param members 成员ID列表
   */
  public void addMembers(String teamId, List<String> members) {
    if (members == null || members.size() == 0) {
      return;
    }
    ALog.d(LIB_TAG, TAG, "addMembers:" + teamId + "," + members.size());
    TeamRepo.inviteUser(
        teamId,
        members,
        new FetchCallback<List<String>>() {
          @Override
          public void onSuccess(@Nullable List<String> param) {
            ALog.d(LIB_TAG, TAG, "addMembers,onSuccess");
            addRemoveMembersData.setValue(new FetchResult<>(LoadStatus.Success, param));
          }

          @Override
          public void onFailed(int code) {
            ALog.d(LIB_TAG, TAG, "addMembers,onFailed:" + code);
            handErrorCode(code);
            addRemoveMembersData.setValue(new FetchResult<>(LoadStatus.Error));
          }

          @Override
          public void onException(@Nullable Throwable exception) {
            ALog.d(LIB_TAG, TAG, "addMembers,onException");
            addRemoveMembersData.setValue(new FetchResult<>(LoadStatus.Error));
          }
        });
  }

  /**
   * 添加管理员
   *
   * @param teamId 群ID
   * @param members 成员ID列表
   */
  public void addManager(String teamId, List<String> members) {
    if (members == null || members.size() == 0) {
      return;
    }
    ALog.d(LIB_TAG, TAG, "addManager:" + teamId + "," + members.size());
    TeamRepo.addManagers(
        teamId,
        members,
        new FetchCallback<List<TeamMember>>() {
          @Override
          public void onSuccess(@Nullable List<TeamMember> param) {
            ALog.d(LIB_TAG, TAG, "addManager,onSuccess");
            addRemoveManagerLiveData.setValue(new FetchResult<>(LoadStatus.Success, param));
          }

          @Override
          public void onFailed(int code) {
            ALog.d(LIB_TAG, TAG, "addManager,onFailed:" + code);
            handErrorCode(code);
            addRemoveManagerLiveData.setValue(new FetchResult<>(LoadStatus.Error));
          }

          @Override
          public void onException(@Nullable Throwable exception) {
            ALog.d(LIB_TAG, TAG, "addManager,onException");
            addRemoveManagerLiveData.setValue(new FetchResult<>(LoadStatus.Error));
          }
        });
  }

  /**
   * 移除管理员
   *
   * @param teamId 群ID
   * @param members 成员ID列表
   */
  public void removeManager(String teamId, List<String> members) {
    if (members == null || members.size() == 0) {
      return;
    }
    ALog.d(LIB_TAG, TAG, "removeManager:" + teamId + "," + members.size());
    TeamRepo.removeManagers(
        teamId,
        members,
        new FetchCallback<List<TeamMember>>() {
          @Override
          public void onSuccess(@Nullable List<TeamMember> param) {
            ALog.d(LIB_TAG, TAG, "removeManager,onSuccess");
            FetchResult<List<TeamMember>> result = new FetchResult<>(LoadStatus.Success, param);
            result.setType(FetchResult.FetchType.Remove);
            addRemoveManagerLiveData.setValue(result);
          }

          @Override
          public void onFailed(int code) {
            ALog.d(LIB_TAG, TAG, "removeManager,onFailed:" + code);
            handErrorCode(code);
            addRemoveManagerLiveData.setValue(new FetchResult<>(LoadStatus.Error));
          }

          @Override
          public void onException(@Nullable Throwable exception) {
            ALog.d(LIB_TAG, TAG, "removeManager,onException");
            addRemoveManagerLiveData.setValue(new FetchResult<>(LoadStatus.Error));
          }
        });
  }

  /**
   * 移除成员
   *
   * @param teamId 群ID
   * @param members 成员ID列表
   */
  public void removeMember(String teamId, List<String> members) {
    if (members == null || members.size() == 0) {
      return;
    }
    ALog.d(LIB_TAG, TAG, "removeMember:" + teamId + "," + members.size());
    TeamRepo.removeMembers(
        teamId,
        members,
        new FetchCallback<Void>() {
          @Override
          public void onSuccess(@Nullable Void param) {
            ALog.d(LIB_TAG, TAG, "removeManager,onSuccess");
            FetchResult<List<String>> result = new FetchResult<>(LoadStatus.Success, members);
            result.setType(FetchResult.FetchType.Remove);
            addRemoveMembersData.setValue(result);
          }

          @Override
          public void onFailed(int code) {
            ALog.d(LIB_TAG, TAG, "removeManager,onFailed:" + code);
            handErrorCode(code);
            addRemoveManagerLiveData.setValue(new FetchResult<>(LoadStatus.Error));
          }

          @Override
          public void onException(@Nullable Throwable exception) {
            ALog.d(LIB_TAG, TAG, "removeManager,onException");
            addRemoveManagerLiveData.setValue(new FetchResult<>(LoadStatus.Error));
          }
        });
  }

  /**
   * 禁言
   *
   * @param teamId 群ID
   * @param mute 是否禁言
   */
  public void muteTeamAllMember(String teamId, boolean mute) {
    ALog.d(LIB_TAG, TAG, "muteTeamAllMember:" + teamId + "," + mute);
    TeamRepo.muteAllMembers(
        teamId,
        mute,
        new FetchCallback<Void>() {
          @Override
          public void onSuccess(@Nullable Void param) {
            ALog.d(LIB_TAG, TAG, "muteTeamAllMember,onSuccess");
            muteTeamAllMemberData.setValue(new ResultInfo<>(mute));
          }

          @Override
          public void onFailed(int code) {
            ALog.d(LIB_TAG, TAG, "muteTeamAllMember,onFailed:" + code);
            muteTeamAllMemberData.setValue(new ResultInfo<>(null, false, new ErrorMsg(code)));
          }

          @Override
          public void onException(@Nullable Throwable exception) {
            ALog.d(LIB_TAG, TAG, "muteTeamAllMember,onException");
            muteTeamAllMemberData.setValue(
                new ResultInfo<>(null, false, new ErrorMsg(-1, "", exception)));
          }
        });
  }

  /**
   * 更新添加成员权限
   *
   * @param teamId 群ID
   * @param type 权限类型
   */
  public void updateInvitePrivilege(String teamId, int type) {
    ALog.d(LIB_TAG, TAG, "updateInvitePrivilege:" + teamId + "," + type);
    TeamRepo.updateInviteMode(
        teamId,
        TeamInviteModeEnum.typeOfValue(type),
        new FetchCallback<Void>() {
          @Override
          public void onSuccess(@Nullable Void param) {
            ALog.d(LIB_TAG, TAG, "updateInvitePrivilege,onSuccess");
            updateInvitePrivilegeData.setValue(new ResultInfo<>(type));
          }

          @Override
          public void onFailed(int code) {
            ALog.d(LIB_TAG, TAG, "updateInvitePrivilege,onFailed:" + code);
            updateInvitePrivilegeData.setValue(new ResultInfo<>(null, false, new ErrorMsg(code)));
          }

          @Override
          public void onException(@Nullable Throwable exception) {
            ALog.d(LIB_TAG, TAG, "updateInvitePrivilege,onException");
            updateInvitePrivilegeData.setValue(
                new ResultInfo<>(null, false, new ErrorMsg(-1, "", exception)));
          }
        });
  }

  /**
   * 更新群信息权限
   *
   * @param teamId 群ID
   * @param type 权限类型
   */
  public void updateInfoPrivilege(String teamId, int type) {
    ALog.d(LIB_TAG, TAG, "updateInfoPrivilege:" + teamId + "," + type);
    TeamRepo.updateTeamInfoPrivilege(
        teamId,
        TeamUpdateModeEnum.typeOfValue(type),
        new FetchCallback<Void>() {
          @Override
          public void onSuccess(@Nullable Void param) {
            ALog.d(LIB_TAG, TAG, "updateInfoPrivilege,onSuccess");
            updateInfoPrivilegeData.setValue(new ResultInfo<>(type));
          }

          @Override
          public void onFailed(int code) {
            ALog.d(LIB_TAG, TAG, "updateInfoPrivilege,onFailed:" + code);
            updateInfoPrivilegeData.setValue(new ResultInfo<>(null, false, new ErrorMsg(code)));
          }

          @Override
          public void onException(@Nullable Throwable exception) {
            ALog.d(LIB_TAG, TAG, "updateInfoPrivilege,onException");
            updateInfoPrivilegeData.setValue(
                new ResultInfo<>(null, false, new ErrorMsg(-1, "", exception)));
          }
        });
  }

  /**
   * 更新@所有人权限，需要在群扩展中添加KEY_EXTENSION_AT_ALL字段实现
   *
   * @param team 群信息
   * @param type 权限类型
   */
  public void updateNotifyAllPrivilege(Team team, String type) {
    if (team == null) {
      return;
    }
    ALog.d(LIB_TAG, TAG, "updateNotifyAllPrivilege:" + team + "," + type);
    JSONObject obj;
    try {
      obj = new JSONObject(team.getExtension());
    } catch (Exception e) {
      ALog.e(TAG, "updateNotifyAllPrivilege-parseExtension", e);
      obj = new JSONObject();
    }
    if (Objects.equals(obj.optString(KEY_EXTENSION_AT_ALL), type)) {
      return;
    }
    try {
      obj.putOpt(KEY_EXTENSION_AT_ALL, type);
    } catch (Exception e) {
      ALog.e(TAG, "updateNotifyAllPrivilege-putOpt", e);
    }
    String extension = obj.toString();
    TeamRepo.updateTeamExtension(
        team.getId(),
        extension,
        new FetchCallback<Void>() {
          @Override
          public void onSuccess(@Nullable Void param) {
            ALog.d(LIB_TAG, TAG, "updateNotifyAllPrivilege,onSuccess");
            team.setExtension(extension);
            sendTipsMessage(TeamUtils.buildAtNotificationText(type));
            updateNotifyAllPrivilegeData.setValue(new ResultInfo<>(type));
          }

          @Override
          public void onFailed(int code) {
            ALog.d(LIB_TAG, TAG, "updateNotifyAllPrivilege,onFailed:" + code);
            updateNotifyAllPrivilegeData.setValue(
                new ResultInfo<>(null, false, new ErrorMsg(code)));
          }

          @Override
          public void onException(@Nullable Throwable exception) {
            ALog.d(LIB_TAG, TAG, "updateNotifyAllPrivilege,onException");
            updateNotifyAllPrivilegeData.setValue(
                new ResultInfo<>(null, false, new ErrorMsg(-1, "", exception)));
          }
        });
  }

  /**
   * 发送Tips消息
   *
   * @param content 消息内容
   */
  public void sendTipsMessage(String content) {
    IMMessage msg = MessageBuilder.createTipMessage(teamId, SessionTypeEnum.Team);
    msg.setContent(content);
    CustomMessageConfig messageConfig = new CustomMessageConfig();
    messageConfig.enableUnreadCount = false;
    msg.setConfig(messageConfig);
    ChatRepo.sendMessage(msg, null);
  }

  public MutableLiveData<ResultInfo<Boolean>> getMuteTeamAllMemberData() {
    return muteTeamAllMemberData;
  }

  public MutableLiveData<ResultInfo<TeamWithCurrentMember>> getTeamWithMemberData() {
    return teamWithMemberData;
  }

  public MutableLiveData<ResultInfo<List<UserInfoWithTeam>>> getUserInfoData() {
    return userInfoData;
  }

  public MutableLiveData<ResultInfo<List<TeamMember>>> getTeamMemberUpdateData() {
    return memberUpdateData;
  }

  public MutableLiveData<ResultInfo<String>> getNameData() {
    return nameData;
  }

  public MutableLiveData<ResultInfo<String>> getIntroduceData() {
    return introduceData;
  }

  public MutableLiveData<ResultInfo<String>> getNicknameData() {
    return nicknameData;
  }

  public MutableLiveData<ResultInfo<String>> getIconData() {
    return iconData;
  }

  public MutableLiveData<ResultInfo<Void>> getQuitTeamData() {
    return quitTeamData;
  }

  public MutableLiveData<ResultInfo<Void>> getDismissTeamData() {
    return dismissTeamData;
  }

  public MutableLiveData<ResultInfo<Boolean>> getNotifyData() {
    return notifyData;
  }

  public MutableLiveData<ResultInfo<Boolean>> getStickData() {
    return stickData;
  }

  public MutableLiveData<ResultInfo<Integer>> getUpdateInvitePrivilegeData() {
    return updateInvitePrivilegeData;
  }

  public MutableLiveData<ResultInfo<Integer>> getUpdateInfoPrivilegeData() {
    return updateInfoPrivilegeData;
  }

  public MutableLiveData<ResultInfo<String>> getUpdateNotifyAllPrivilegeData() {
    return updateNotifyAllPrivilegeData;
  }

  public MutableLiveData<FetchResult<List<String>>> getAddRemoveMembersData() {
    return addRemoveMembersData;
  }

  public MutableLiveData<FetchResult<List<TeamMember>>> getAddRemoveManagerLiveData() {
    return addRemoveManagerLiveData;
  }

  public MutableLiveData<ResultInfo<Team>> getTeamUpdateData() {
    return teamUpdateData;
  }

  private void handErrorCode(int code) {
    if (code == TeamUIKitConstant.QUIT_TEAM_ERROR_CODE_NO_MEMBER
        || code == TeamUIKitConstant.REMOVE_MEMBER_ERROR_CODE_NO_PERMISSION) {
      ToastX.showLongToast(R.string.team_operate_no_permission_tip);
    }
  }
}
