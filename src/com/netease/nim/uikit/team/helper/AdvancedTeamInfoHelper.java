package com.netease.nim.uikit.team.helper;

import android.util.Log;
import android.widget.Toast;

import com.netease.nim.uikit.NimUIKit;
import com.netease.nim.uikit.R;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.team.TeamService;

import java.util.ArrayList;

/**
 * Created by hzxuwen on 2016/9/21.
 */
public class AdvancedTeamInfoHelper {
    private static final String TAG = AdvancedTeamInfoHelper.class.getSimpleName();

    /**
     * 邀请群成员
     */
    public static void inviteMembers(String teamId, ArrayList<String> accounts) {
        NIMClient.getService(TeamService.class).addMembers(teamId, accounts).setCallback(new RequestCallback<Void>() {
            @Override
            public void onSuccess(Void param) {
                Toast.makeText(NimUIKit.getContext(), "添加群成员成功", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailed(int code) {
                if (code == ResponseCode.RES_TEAM_INVITE_SUCCESS) {
                    Toast.makeText(NimUIKit.getContext(), R.string.team_invite_members_success, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(NimUIKit.getContext(), "invite members failed, code=" + code, Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "invite members failed, code=" + code);
                }
            }

            @Override
            public void onException(Throwable exception) {

            }
        });
    }
}
