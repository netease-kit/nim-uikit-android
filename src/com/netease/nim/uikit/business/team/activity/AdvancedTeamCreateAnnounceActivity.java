package com.netease.nim.uikit.business.team.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import com.netease.nim.uikit.common.ToastHelper;

import com.netease.nim.uikit.R;
import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.api.model.SimpleCallback;
import com.netease.nim.uikit.api.wrapper.NimToolBarOptions;
import com.netease.nim.uikit.business.team.helper.AnnouncementHelper;
import com.netease.nim.uikit.common.activity.ToolBarOptions;
import com.netease.nim.uikit.common.activity.UI;
import com.netease.nim.uikit.common.ui.dialog.DialogMaker;
import com.netease.nim.uikit.common.util.sys.NetworkUtil;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.team.TeamService;
import com.netease.nimlib.sdk.team.constant.TeamFieldEnum;
import com.netease.nimlib.sdk.team.model.Team;

/**
 * 创建群公告界面
 * Created by hzxuwen on 2015/3/18.
 */
public class AdvancedTeamCreateAnnounceActivity extends UI {

    // constant
    private final static String EXTRA_TID = "EXTRA_TID";

    // data
    private String teamId;
    private String announce;

    // view
    private EditText teamAnnounceTitle;
    private EditText teamAnnounceContent;
    private TextView toolbarView;

    public static void startActivityForResult(Activity activity, String teamId, int requestCode) {
        Intent intent = new Intent();
        intent.setClass(activity, AdvancedTeamCreateAnnounceActivity.class);
        intent.putExtra(EXTRA_TID, teamId);
        activity.startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nim_advanced_team_create_announce);

        ToolBarOptions options = new NimToolBarOptions();
        options.titleId = R.string.team_annourcement;
        setToolBar(R.id.toolbar, options);

        parseIntentData();
        findViews();
        initActionbar();
    }

    private void parseIntentData() {
        teamId = getIntent().getStringExtra(EXTRA_TID);
    }

    private void findViews() {
        teamAnnounceTitle = (EditText) findViewById(R.id.team_announce_title);
        teamAnnounceContent = (EditText) findViewById(R.id.team_announce_content);
        teamAnnounceTitle.setFilters(new InputFilter[]{new InputFilter.LengthFilter(64)});
        teamAnnounceContent.setFilters(new InputFilter[]{new InputFilter.LengthFilter(1024)});
    }

    private void initActionbar() {
        toolbarView = findView(R.id.action_bar_right_clickable_textview);
        toolbarView.setText(R.string.save);
        toolbarView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestAnnounceData();
            }
        });
    }

    private void requestAnnounceData() {
        if (!NetworkUtil.isNetAvailable(this)) {
            ToastHelper.showToast(this, R.string.network_is_not_available);
            return;
        }

        if (TextUtils.isEmpty(teamAnnounceTitle.getText().toString())) {
            ToastHelper.showToast(AdvancedTeamCreateAnnounceActivity.this, R.string.team_announce_notice);
            return;
        }

        toolbarView.setEnabled(false);
        // 请求群信息
        Team t = NimUIKit.getTeamProvider().getTeamById(teamId);
        if (t != null) {
            updateTeamData(t);
            updateAnnounce();
        } else {
            NimUIKit.getTeamProvider().fetchTeamById(teamId, new SimpleCallback<Team>() {
                @Override
                public void onResult(boolean success, Team result, int code) {
                    if (success && result != null) {
                        updateTeamData(result);
                        updateAnnounce();
                    } else {
                        toolbarView.setEnabled(true);
                    }
                }
            });
        }
    }

    /**
     * 获得最新公告内容
     *
     * @param team 群
     */
    private void updateTeamData(Team team) {
        if (team == null) {
            ToastHelper.showToast(this, getString(R.string.team_not_exist));
            showKeyboard(false);
            finish();
        } else {
            announce = team.getAnnouncement();
        }
    }

    /**
     * 创建公告更新到服务器
     */
    private void updateAnnounce() {
        String announcement = AnnouncementHelper.makeAnnounceJson(announce, teamAnnounceTitle.getText().toString(),
                teamAnnounceContent.getText().toString());
        NIMClient.getService(TeamService.class).updateTeam(teamId, TeamFieldEnum.Announcement, announcement).setCallback(new RequestCallback<Void>() {
            @Override
            public void onSuccess(Void param) {
                DialogMaker.dismissProgressDialog();
                setResult(Activity.RESULT_OK);
                showKeyboard(false);
                finish();
                ToastHelper.showToast(AdvancedTeamCreateAnnounceActivity.this, R.string.update_success);
            }

            @Override
            public void onFailed(int code) {
                DialogMaker.dismissProgressDialog();
                toolbarView.setEnabled(true);
                ToastHelper.showToast(AdvancedTeamCreateAnnounceActivity.this, String.format(getString(R.string.update_failed), code));
            }

            @Override
            public void onException(Throwable exception) {
                DialogMaker.dismissProgressDialog();
                toolbarView.setEnabled(true);
            }
        });
    }

    @Override
    public void onBackPressed() {
        showKeyboard(false);
        super.onBackPressed();
    }
}
