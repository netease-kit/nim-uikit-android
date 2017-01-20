package com.netease.nim.uikit.team.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.netease.nim.uikit.NimUIKit;
import com.netease.nim.uikit.R;
import com.netease.nim.uikit.cache.SimpleCallback;
import com.netease.nim.uikit.cache.TeamDataCache;
import com.netease.nim.uikit.common.activity.UI;
import com.netease.nim.uikit.model.ToolBarOptions;
import com.netease.nim.uikit.team.adapter.TeamMemberListAdapter;
import com.netease.nim.uikit.team.model.TeamExtras;
import com.netease.nim.uikit.team.model.TeamRequestCode;
import com.netease.nim.uikit.team.ui.DividerItemDecoration;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.nimlib.sdk.team.model.TeamMember;

import java.util.List;

/**
 * Created by hzchenkang on 2016/12/2.
 */

public class TeamMemberListActivity extends UI {

    private static final String EXTRA_ID = "EXTRA_ID";

    private TeamMemberListAdapter adapter;

    private String teamId;

    public static void start(Context context, String tid) {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_ID, tid);
        intent.setClass(context, TeamMemberListActivity.class);
        ((Activity) context).startActivityForResult(intent, TeamRequestCode.REQUEST_TEAM_AIT_MEMBER);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nim_team_member_list_layout);
        parseIntent();
        initViews();
        initData();
    }

    private void initViews() {
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.member_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TeamMemberListAdapter(this);
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(
                this, DividerItemDecoration.VERTICAL_LIST));
        adapter.setListener(new TeamMemberListAdapter.ItemClickListener() {
            @Override
            public void onItemClick(TeamMember member) {
                Intent intent = new Intent();
                intent.putExtra(TeamExtras.RESULT_EXTRA_DATA, member);
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });
        ToolBarOptions options = new ToolBarOptions();
        options.titleString = "选择提醒的人";
        setToolBar(R.id.toolbar, options);
    }

    private void parseIntent() {
        teamId = getIntent().getStringExtra(EXTRA_ID);
    }

    private void initData() {
        Team t = TeamDataCache.getInstance().getTeamById(teamId);
        if (t != null) {
            updateTeamMember(t);
        } else {
            TeamDataCache.getInstance().fetchTeamById(teamId, new SimpleCallback<Team>() {
                @Override
                public void onResult(boolean success, Team result) {
                    if (success && result != null) {
                        updateTeamMember(result);
                    } else {
                        //
                    }
                }
            });
        }
    }

    private void updateTeamMember(Team team) {
        TeamDataCache.getInstance().fetchTeamMemberList(teamId, new SimpleCallback<List<TeamMember>>() {
            @Override
            public void onResult(boolean success, List<TeamMember> members) {
                if (success && members != null && !members.isEmpty()) {
                    // filter self
                    for (TeamMember member : members) {
                        if (member.getAccount().equals(NimUIKit.getAccount())) {
                            members.remove(member);
                            break;
                        }
                    }
                    adapter.updateData(members);
                }
            }
        });
    }
}
