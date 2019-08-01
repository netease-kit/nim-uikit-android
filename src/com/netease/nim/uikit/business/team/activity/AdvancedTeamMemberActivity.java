package com.netease.nim.uikit.business.team.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;

import com.netease.nim.uikit.R;
import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.api.model.SimpleCallback;
import com.netease.nim.uikit.api.model.user.UserInfoObserver;
import com.netease.nim.uikit.api.wrapper.NimToolBarOptions;
import com.netease.nim.uikit.business.team.adapter.TeamMemberAdapter;
import com.netease.nim.uikit.business.team.adapter.TeamMemberAdapter.TeamMemberItem;
import com.netease.nim.uikit.business.team.helper.TeamHelper;
import com.netease.nim.uikit.business.team.ui.TeamInfoGridView;
import com.netease.nim.uikit.business.team.viewholder.TeamMemberHolder;
import com.netease.nim.uikit.common.activity.ToolBarOptions;
import com.netease.nim.uikit.common.activity.UI;
import com.netease.nim.uikit.common.adapter.TAdapterDelegate;
import com.netease.nim.uikit.common.adapter.TViewHolder;
import com.netease.nimlib.sdk.team.constant.TeamMemberType;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.nimlib.sdk.team.model.TeamMember;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 群成员列表界面
 * Created by hzxuwen on 2015/3/17.
 */
public class AdvancedTeamMemberActivity extends UI implements TAdapterDelegate,
        TeamMemberAdapter.RemoveMemberCallback, TeamMemberAdapter.AddMemberCallback, TeamMemberHolder.TeamMemberHolderEventListener {

    // constant
    private static final String EXTRA_ID = "EXTRA_ID";
    public static final String EXTRA_DATA = "EXTRA_DATA";

    // data source
    private String teamId;
    private List<TeamMember> members;
    private TeamMemberAdapter adapter;
    private List<String> memberAccounts;
    private List<TeamMemberAdapter.TeamMemberItem> dataSource;
    private String creator;
    private List<String> managerList;

    // state
    private boolean isSelfAdmin = false;
    private boolean isSelfManager = false;
    private boolean isMemberChange = false;
    private UserInfoObserver userInfoObserver;

    public static void startActivityForResult(Activity context, String tid, int resCode) {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_ID, tid);
        intent.setClass(context, AdvancedTeamMemberActivity.class);
        context.startActivityForResult(intent, resCode);
    }

    /**
     * *********************************lifeCycle*******************************************
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nim_team_member_grid_layout);

        ToolBarOptions options = new NimToolBarOptions();
        options.titleId = R.string.team_member;
        setToolBar(R.id.toolbar, options);

        parseIntentData();
        loadTeamInfo();
        initAdapter();
        findViews();
        registerUserInfoChangedObserver(true);
        requestData();
    }

    @Override
    protected void onDestroy() {
        registerUserInfoChangedObserver(false);

        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_DATA, isMemberChange);
        setResult(Activity.RESULT_OK, intent);
        super.onBackPressed();
    }

    private void parseIntentData() {
        teamId = getIntent().getStringExtra(EXTRA_ID);
    }

    private void loadTeamInfo() {
        Team team = NimUIKit.getTeamProvider().getTeamById(teamId);
        if (team != null) {
            creator = team.getCreator();
        }
    }

    private void findViews() {
        TeamInfoGridView teamInfoGridView = (TeamInfoGridView) findViewById(R.id.team_member_grid);
        teamInfoGridView.setSelector(R.color.transparent);
        teamInfoGridView.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == 0) {
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });
        teamInfoGridView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP && adapter.getMode() == TeamMemberAdapter.Mode.DELETE) {
                    adapter.setMode(TeamMemberAdapter.Mode.NORMAL);
                    adapter.notifyDataSetChanged();
                    return true;
                }
                return false;
            }
        });
        teamInfoGridView.setAdapter(adapter);
    }

    private void initAdapter() {
        memberAccounts = new ArrayList<>();
        members = new ArrayList<>();
        dataSource = new ArrayList<>();
        managerList = new ArrayList<>();
        adapter = new TeamMemberAdapter(this, dataSource, this, this, this);
        adapter.setEventListener(this);
    }

    private void updateTeamMember(final List<TeamMember> members) {
        if (members != null && members.isEmpty()) {
            return;
        }

        addTeamMembers(members, true);
    }

    private void addTeamMembers(final List<TeamMember> m, boolean clear) {
        if (m == null || m.isEmpty()) {
            return;
        }

        if (clear) {
            this.members.clear();
            this.memberAccounts.clear();
        }

        // add
        if (this.members.isEmpty()) {
            this.members.addAll(m);
        } else {
            for (TeamMember tm : m) {
                if (!this.memberAccounts.contains(tm.getAccount())) {
                    this.members.add(tm);
                }
            }
        }

        // sort
        Collections.sort(this.members, TeamHelper.teamMemberComparator);

        // accounts, manager, creator
        this.memberAccounts.clear();
        this.managerList.clear();
        for (TeamMember tm : members) {
            initManagerList(tm);
            if (tm.getAccount().equals(NimUIKit.getAccount())) {
                if (tm.getType() == TeamMemberType.Manager) {
                    isSelfManager = true;
                } else if (tm.getType() == TeamMemberType.Owner) {
                    isSelfAdmin = true;
                    creator = NimUIKit.getAccount();
                }
            }
            this.memberAccounts.add(tm.getAccount());
        }

        updateTeamMemberDataSource();
    }

    /**
     * 初始化管理员列表
     *
     * @param tm 群成员
     */
    private void initManagerList(TeamMember tm) {
        if (tm.getType() == TeamMemberType.Manager) {
            managerList.add(tm.getAccount());
        }
    }

    private void updateTeamMemberDataSource() {
        if (members.size() <= 0) {
            return;
        }

        dataSource.clear();

        // member item
        for (String account : memberAccounts) {
            dataSource.add(new TeamMemberItem(TeamMemberAdapter.TeamMemberItemTag
                    .NORMAL, teamId, account, initMemberIdentity(account)));
        }

        // refresh
        adapter.notifyDataSetChanged();
    }

    /**
     * 初始化成员身份
     *
     * @param account 帐号
     * @return String
     */
    private String initMemberIdentity(String account) {
        String identity;
        if (creator.equals(account)) {
            identity = TeamMemberHolder.OWNER;
        } else if (managerList.contains(account)) {
            identity = TeamMemberHolder.ADMIN;
        } else {
            identity = null;
        }
        return identity;
    }


    @Override
    public void onAddMember() {

    }

    @Override
    public void onRemoveMember(String account) {

    }

    /**
     * ******************************加载数据*******************************
     */
    private void requestData() {
        NimUIKit.getTeamProvider().fetchTeamMemberList(teamId, new SimpleCallback<List<TeamMember>>() {
            @Override
            public void onResult(boolean success, List<TeamMember> members, int code) {
                if (success && members != null && !members.isEmpty()) {
                    updateTeamMember(members);
                }
            }
        });
    }

    /**
     * ************************ TAdapterDelegate **************************
     */

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public Class<? extends TViewHolder> viewHolderAtPosition(int position) {
        return TeamMemberHolder.class;
    }

    @Override
    public boolean enabled(int position) {
        return false;
    }

    @Override
    public void onHeadImageViewClick(String account) {
        AdvancedTeamMemberInfoActivity.startActivityForResult(AdvancedTeamMemberActivity.this, account, teamId);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case AdvancedTeamMemberInfoActivity.REQ_CODE_REMOVE_MEMBER:
                    boolean isSetAdmin = data.getBooleanExtra(AdvancedTeamMemberInfoActivity.EXTRA_ISADMIN, false);
                    boolean isRemoveMember = data.getBooleanExtra(AdvancedTeamMemberInfoActivity.EXTRA_ISREMOVE, false);
                    String account = data.getStringExtra(EXTRA_ID);
                    refreshAdmin(isSetAdmin, account);
                    if (isRemoveMember) {
                        removeMember(account);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 移除群成员成功后，删除列表中的群成员
     */
    private void removeMember(String account) {
        if (TextUtils.isEmpty(account)) {
            return;
        }
        for (TeamMemberItem item : dataSource) {
            if (item.getAccount() != null && item.getAccount().equals(account)) {
                dataSource.remove(item);
                isMemberChange = true;
                break;
            }
        }
        adapter.notifyDataSetChanged();
    }

    /**
     * 是否设置了管理员刷新界面
     *
     * @param isSetAdmin 是否设置为管理员
     * @param account    帐号
     */
    private void refreshAdmin(boolean isSetAdmin, String account) {
        if (isSetAdmin) {
            if (managerList.contains(account)) {
                return;
            }
            managerList.add(account);
            isMemberChange = true;
            updateTeamMemberDataSource();
        } else {
            if (managerList.contains(account)) {
                managerList.remove(account);
                isMemberChange = true;
                updateTeamMemberDataSource();
            }
        }
    }

    private void registerUserInfoChangedObserver(boolean register) {
        if (register) {
            if (userInfoObserver == null) {
                userInfoObserver = new UserInfoObserver() {
                    @Override
                    public void onUserInfoChanged(List<String> accounts) {
                        adapter.notifyDataSetChanged();
                    }
                };
            }
            NimUIKit.getUserInfoObservable().registerObserver(userInfoObserver, true);
        } else {
            NimUIKit.getUserInfoObservable().registerObserver(userInfoObserver, false);
        }
    }
}
