package com.netease.nim.uikit.impl.provider;

import com.netease.nim.uikit.api.model.SimpleCallback;
import com.netease.nim.uikit.api.model.user.IUserInfoProvider;
import com.netease.nim.uikit.impl.cache.NimUserInfoCache;
import com.netease.nimlib.sdk.RequestCallbackWrapper;
import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.uinfo.model.NimUserInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hzchenkang on 2017/10/24.
 */

public class DefaultUserInfoProvider implements IUserInfoProvider<NimUserInfo> {

    @Override
    public NimUserInfo getUserInfo(String account) {
        NimUserInfo user = NimUserInfoCache.getInstance().getUserInfo(account);
        if (user == null) {
            NimUserInfoCache.getInstance().getUserInfoFromRemote(account, null);
        }

        return user;
    }

    @Override
    public List<NimUserInfo> getUserInfo(List<String> accounts) {
        List<NimUserInfo> users = new ArrayList<>();
        for (String account : accounts) {
            NimUserInfo userInfo = getUserInfo(account);
            if (userInfo != null) {
                users.add(userInfo);
            }
        }
        return users;
    }

    @Override
    public void getUserInfoAsync(String account, final SimpleCallback<NimUserInfo> callback) {
        NimUserInfoCache.getInstance().getUserInfoFromRemote(account, new RequestCallbackWrapper<NimUserInfo>() {
            @Override
            public void onResult(int code, NimUserInfo result, Throwable exception) {
                if (callback != null) {
                    callback.onResult(code == ResponseCode.RES_SUCCESS, result, code);
                }
            }
        });
    }

    @Override
    public void getUserInfoAsync(List<String> accounts, final SimpleCallback<List<NimUserInfo>> callback) {
        NimUserInfoCache.getInstance().getUserInfoFromRemote(accounts, new RequestCallbackWrapper<List<NimUserInfo>>() {
            @Override
            public void onResult(int code, List<NimUserInfo> result, Throwable exception) {
                if (callback != null) {
                    callback.onResult(code == ResponseCode.RES_SUCCESS, result, code);
                }
            }
        });
    }
}
