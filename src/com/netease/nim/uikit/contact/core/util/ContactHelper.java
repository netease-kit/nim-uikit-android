package com.netease.nim.uikit.contact.core.util;

import com.netease.nim.uikit.NimUIKit;
import com.netease.nim.uikit.contact.core.model.IContact;
import com.netease.nimlib.sdk.uinfo.UserInfoProvider;

/**
 * Created by huangjun on 2015/9/8.
 */
public class ContactHelper {
    public static IContact makeContactFromUserInfo(final UserInfoProvider.UserInfo userInfo) {
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
                return NimUIKit.getContactProvider().getUserDisplayName(userInfo.getAccount());
            }
        };
    }
}
