/*
 * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.kit.teamkit.ui.viewmodel;

import android.text.TextUtils;

import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;

import com.netease.nimlib.sdk.msg.model.StickTopSessionInfo;
import com.netease.nimlib.sdk.team.constant.TeamInviteModeEnum;
import com.netease.nimlib.sdk.team.constant.TeamUpdateModeEnum;
import com.netease.yunxin.kit.common.ui.viewmodel.BaseViewModel;
import com.netease.yunxin.kit.corekit.im.XKitImClient;
import com.netease.yunxin.kit.corekit.im.provider.FetchCallback;
import com.netease.yunxin.kit.corekit.model.ErrorMsg;
import com.netease.yunxin.kit.corekit.model.ResultInfo;
import com.netease.yunxin.kit.teamkit.model.TeamWithCurrentMember;
import com.netease.yunxin.kit.teamkit.model.UserInfoWithTeam;
import com.netease.yunxin.kit.teamkit.repo.TeamRepo;

import java.util.List;
import java.util.Objects;

public class TeamSettingViewModel extends BaseViewModel {
    private final MutableLiveData<ResultInfo<TeamWithCurrentMember>> teamWithMemberData = new MutableLiveData<>();
    private final MutableLiveData<ResultInfo<List<UserInfoWithTeam>>> userInfoData = new MutableLiveData<>();
    private final MutableLiveData<ResultInfo<String>> nameData = new MutableLiveData<>();
    private final MutableLiveData<ResultInfo<String>> introduceData = new MutableLiveData<>();
    private final MutableLiveData<ResultInfo<String>> nicknameData = new MutableLiveData<>();
    private final MutableLiveData<ResultInfo<String>> iconData = new MutableLiveData<>();
    private final MutableLiveData<ResultInfo<Void>> quitTeamData = new MutableLiveData<>();
    private final MutableLiveData<ResultInfo<Void>> dismissTeamData = new MutableLiveData<>();
    private final MutableLiveData<ResultInfo<Integer>> updateInvitePrivilegeData = new MutableLiveData<>();
    private final MutableLiveData<ResultInfo<Integer>> updateInfoPrivilegeData = new MutableLiveData<>();
    private final MutableLiveData<ResultInfo<Boolean>> muteTeamData = new MutableLiveData<>();
    private final MutableLiveData<ResultInfo<Boolean>> beInvitedNeedAgreedData = new MutableLiveData<>();
    private final MutableLiveData<ResultInfo<Boolean>> stickData = new MutableLiveData<>();
    private final MutableLiveData<ResultInfo<List<String>>> addMembersData = new MutableLiveData<>();
    private final MutableLiveData<ResultInfo<Boolean>> muteTeamAllMemberData = new MutableLiveData<>();

    public void requestTeamData(String teamId) {
        TeamRepo.queryTeamWithMember(teamId, Objects.requireNonNull(XKitImClient.account()), new FetchCallback<TeamWithCurrentMember>() {
            @Override
            public void onSuccess(@Nullable TeamWithCurrentMember param) {
                teamWithMemberData.postValue(new ResultInfo<>(param));
            }

            @Override
            public void onFailed(int code) {
                teamWithMemberData.postValue(new ResultInfo<>(null, false, new ErrorMsg(code)));
            }

            @Override
            public void onException(@Nullable Throwable exception) {
                teamWithMemberData.postValue(new ResultInfo<>(null, false, new ErrorMsg(-1, "", exception)));
            }
        });
    }

    public void requestTeamMembers(String teamId) {
        TeamRepo.queryMemberWithBasicInfoList(teamId, new FetchCallback<List<UserInfoWithTeam>>() {
            @Override
            public void onSuccess(@Nullable List<UserInfoWithTeam> param) {
                userInfoData.postValue(new ResultInfo<>(param));
            }

            @Override
            public void onFailed(int code) {
                userInfoData.postValue(new ResultInfo<>(null, false, new ErrorMsg(code)));
            }

            @Override
            public void onException(@Nullable Throwable exception) {
                userInfoData.postValue(new ResultInfo<>(null, false, new ErrorMsg(-1, "", exception)));
            }
        });
    }

    public void updateName(String teamId, String name) {
        TeamRepo.updateName(teamId, name, new FetchCallback<Void>() {
            @Override
            public void onSuccess(@Nullable Void param) {
                nameData.postValue(new ResultInfo<>(name));
            }

            @Override
            public void onFailed(int code) {
                nameData.postValue(new ResultInfo<>(null, false, new ErrorMsg(code)));
            }

            @Override
            public void onException(@Nullable Throwable exception) {
                nameData.postValue(new ResultInfo<>(null, false, new ErrorMsg(-1, "", exception)));
            }
        });
    }

    public void updateIntroduce(String teamId, String introduce) {
        TeamRepo.updateIntroduce(teamId, introduce, new FetchCallback<Void>() {
            @Override
            public void onSuccess(@Nullable Void param) {
                introduceData.postValue(new ResultInfo<>(introduce));
            }

            @Override
            public void onFailed(int code) {
                introduceData.postValue(new ResultInfo<>(null, false, new ErrorMsg(code)));
            }

            @Override
            public void onException(@Nullable Throwable exception) {
                introduceData.postValue(new ResultInfo<>(null, false, new ErrorMsg(-1, "", exception)));
            }
        });
    }

    public void updateNickname(String teamId, String nickname) {
        TeamRepo.updateMemberNick(teamId, Objects.requireNonNull(XKitImClient.account()), nickname, new FetchCallback<Void>() {
            @Override
            public void onSuccess(@Nullable Void param) {
                nicknameData.postValue(new ResultInfo<>(nickname));
            }

            @Override
            public void onFailed(int code) {
                nicknameData.postValue(new ResultInfo<>(null, false, new ErrorMsg(code)));
            }

            @Override
            public void onException(@Nullable Throwable exception) {
                nicknameData.postValue(new ResultInfo<>(null, false, new ErrorMsg(-1, "", exception)));
            }
        });
    }

    public void updateIcon(String teamId, String iconUrl) {
        TeamRepo.updateIcon(teamId, iconUrl, new FetchCallback<Void>() {
            @Override
            public void onSuccess(@Nullable Void param) {
                iconData.postValue(new ResultInfo<>(iconUrl));
            }

            @Override
            public void onFailed(int code) {
                iconData.postValue(new ResultInfo<>(null, false, new ErrorMsg(code)));
            }

            @Override
            public void onException(@Nullable Throwable exception) {
                iconData.postValue(new ResultInfo<>(null, false, new ErrorMsg(-1, "", exception)));
            }
        });
    }

    public void quitTeam(String teamId) {
        TeamRepo.quitTeam(teamId, new FetchCallback<Void>() {
            @Override
            public void onSuccess(@Nullable Void param) {
                quitTeamData.postValue(new ResultInfo<>(param));
            }

            @Override
            public void onFailed(int code) {
                quitTeamData.postValue(new ResultInfo<>(null, false, new ErrorMsg(code)));
            }

            @Override
            public void onException(@Nullable Throwable exception) {
                quitTeamData.postValue(new ResultInfo<>(null, false, new ErrorMsg(-1, "", exception)));
            }
        });
    }

    public void dismissTeam(String teamId) {
        TeamRepo.dismissTeam(teamId, new FetchCallback<Void>() {
            @Override
            public void onSuccess(@Nullable Void param) {
                dismissTeamData.postValue(new ResultInfo<>(param));
            }

            @Override
            public void onFailed(int code) {
                dismissTeamData.postValue(new ResultInfo<>(null, false, new ErrorMsg(code)));
            }

            @Override
            public void onException(@Nullable Throwable exception) {
                dismissTeamData.postValue(new ResultInfo<>(null, false, new ErrorMsg(-1, "", exception)));
            }
        });
    }

    public void muteTeam(String teamId, boolean mute) {
        TeamRepo.muteTeam(teamId, mute, new FetchCallback<Void>() {
            @Override
            public void onSuccess(@Nullable Void param) {
                muteTeamData.postValue(new ResultInfo<>(mute));
            }

            @Override
            public void onFailed(int code) {
                muteTeamData.postValue(new ResultInfo<>(null, false, new ErrorMsg(code)));
            }

            @Override
            public void onException(@Nullable Throwable exception) {
                muteTeamData.postValue(new ResultInfo<>(null, false, new ErrorMsg(-1, "", exception)));
            }
        });
    }

    public boolean isStick(String sessionId) {
        if (TextUtils.isEmpty(sessionId)) {
            return false;
        }
        return TeamRepo.isStick(sessionId);
    }

    public void configStick(String sessionId, boolean stick) {
        if (TextUtils.isEmpty(sessionId)) {
            stickData.postValue(new ResultInfo<>(null, false, new ErrorMsg(-1)));
            return;
        }
        if (stick) {
            TeamRepo.addStick(sessionId, new FetchCallback<StickTopSessionInfo>() {
                @Override
                public void onSuccess(@Nullable StickTopSessionInfo param) {
                    stickData.postValue(new ResultInfo<>(true));
                    TeamRepo.notifyStickNotify(sessionId);
                }

                @Override
                public void onFailed(int code) {
                    stickData.postValue(new ResultInfo<>(null, false, new ErrorMsg(code)));
                }

                @Override
                public void onException(@Nullable Throwable exception) {
                    stickData.postValue(new ResultInfo<>(null, false, new ErrorMsg(-1, "", exception)));
                }
            });
        } else {
            TeamRepo.removeStick(sessionId, new FetchCallback<Void>() {
                @Override
                public void onSuccess(@Nullable Void param) {
                    stickData.postValue(new ResultInfo<>(false));
                    TeamRepo.notifyStickNotify(sessionId);
                }

                @Override
                public void onFailed(int code) {
                    stickData.postValue(new ResultInfo<>(null, false, new ErrorMsg(code)));
                }

                @Override
                public void onException(@Nullable Throwable exception) {
                    stickData.postValue(new ResultInfo<>(null, false, new ErrorMsg(-1, "", exception)));
                }
            });
        }
    }

    public void addMembers(String teamId, List<String> members) {
        TeamRepo.addMembers(teamId, members, new FetchCallback<List<String>>() {
            @Override
            public void onSuccess(@Nullable List<String> param) {
                addMembersData.postValue(new ResultInfo<>(param));
            }

            @Override
            public void onFailed(int code) {
                addMembersData.postValue(new ResultInfo<>(null, false, new ErrorMsg(code)));
            }

            @Override
            public void onException(@Nullable Throwable exception) {
                addMembersData.postValue(new ResultInfo<>(null, false, new ErrorMsg(-1, "", exception)));
            }
        });
    }

    public void muteTeamAllMember(String teamId, boolean mute) {
        TeamRepo.muteTeamAllMember(teamId, mute, new FetchCallback<Void>() {
            @Override
            public void onSuccess(@Nullable Void param) {
                muteTeamAllMemberData.postValue(new ResultInfo<>(mute));
            }

            @Override
            public void onFailed(int code) {
                muteTeamAllMemberData.postValue(new ResultInfo<>(null, false, new ErrorMsg(code)));
            }

            @Override
            public void onException(@Nullable Throwable exception) {
                muteTeamAllMemberData.postValue(new ResultInfo<>(null, false, new ErrorMsg(-1, "", exception)));
            }
        });
    }

    public void updateBeInviteMode(String teamId, boolean needAgree) {
        TeamRepo.updateBeInviteMode(teamId, needAgree, new FetchCallback<Void>() {
            @Override
            public void onSuccess(@Nullable Void param) {
                beInvitedNeedAgreedData.postValue(new ResultInfo<>(needAgree));
            }

            @Override
            public void onFailed(int code) {
                beInvitedNeedAgreedData.postValue(new ResultInfo<>(null, false, new ErrorMsg(code)));
            }

            @Override
            public void onException(@Nullable Throwable exception) {
                beInvitedNeedAgreedData.postValue(new ResultInfo<>(null, false, new ErrorMsg(-1, "", exception)));
            }
        });
    }

    public void updateInvitePrivilege(String teamId, int type) {
        TeamRepo.updateInvitePrivilege(teamId, TeamInviteModeEnum.typeOfValue(type), new FetchCallback<Void>() {
            @Override
            public void onSuccess(@Nullable Void param) {
                updateInvitePrivilegeData.postValue(new ResultInfo<>(type));
            }

            @Override
            public void onFailed(int code) {
                updateInvitePrivilegeData.postValue(new ResultInfo<>(null, false, new ErrorMsg(code)));
            }

            @Override
            public void onException(@Nullable Throwable exception) {
                updateInvitePrivilegeData.postValue(new ResultInfo<>(null, false, new ErrorMsg(-1, "", exception)));
            }
        });
    }

    public void updateInfoPrivilege(String teamId, int type) {
        TeamRepo.updateInfoPrivilege(teamId, TeamUpdateModeEnum.typeOfValue(type), new FetchCallback<Void>() {
            @Override
            public void onSuccess(@Nullable Void param) {
                updateInfoPrivilegeData.postValue(new ResultInfo<>(type));
            }

            @Override
            public void onFailed(int code) {
                updateInfoPrivilegeData.postValue(new ResultInfo<>(null, false, new ErrorMsg(code)));
            }

            @Override
            public void onException(@Nullable Throwable exception) {
                updateInfoPrivilegeData.postValue(new ResultInfo<>(null, false, new ErrorMsg(-1, "", exception)));
            }
        });
    }

    public MutableLiveData<ResultInfo<Boolean>> getMuteTeamAllMemberData() {
        return muteTeamAllMemberData;
    }

    public MutableLiveData<ResultInfo<Boolean>> getBeInvitedNeedAgreedData() {
        return beInvitedNeedAgreedData;
    }

    public MutableLiveData<ResultInfo<TeamWithCurrentMember>> getTeamWithMemberData() {
        return teamWithMemberData;
    }

    public MutableLiveData<ResultInfo<List<UserInfoWithTeam>>> getUserInfoData() {
        return userInfoData;
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

    public MutableLiveData<ResultInfo<Boolean>> getMuteTeamData() {
        return muteTeamData;
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

    public MutableLiveData<ResultInfo<List<String>>> getAddMembersData() {
        return addMembersData;
    }
}
