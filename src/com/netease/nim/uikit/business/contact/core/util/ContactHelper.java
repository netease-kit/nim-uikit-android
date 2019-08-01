package com.netease.nim.uikit.business.contact.core.util;

import com.netease.nim.uikit.business.contact.core.model.IContact;
import com.netease.nim.uikit.business.team.helper.TeamHelper;
import com.netease.nim.uikit.business.uinfo.UserInfoHelper;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.search.model.MsgIndexRecord;
import com.netease.nimlib.sdk.uinfo.model.UserInfo;

/**
 * Created by huangjun on 2015/9/8.
 */
public class ContactHelper {
    public static IContact makeContactFromUserInfo(final UserInfo userInfo) {
        return new IContact() {
            @Override
            public String getContactId() {
                return userInfo.getAccount();
            }

            @Override
            public int getContactType() {
                return Type.Friend;
            }

            @Override
            public String getDisplayName() {
                return UserInfoHelper.getUserDisplayName(userInfo.getAccount());
            }
        };
    }

    public static IContact makeContactFromMsgIndexRecord(final MsgIndexRecord record) {
        return new IContact() {
            @Override
            public String getContactId() {
                return record.getSessionId();
            }

            @Override
            public int getContactType() {
                return Type.Msg;
            }

            @Override
            public String getDisplayName() {
                String sessionId = record.getSessionId();
                SessionTypeEnum sessionType = record.getSessionType();

                if (sessionType == SessionTypeEnum.P2P) {
                    return UserInfoHelper.getUserDisplayName(sessionId);
                } else if (sessionType == SessionTypeEnum.Team) {
                    return TeamHelper.getTeamName(sessionId);
                }

                return "";
            }
        };
    }
}
