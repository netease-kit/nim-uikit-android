package com.netease.nim.uikit.business.team.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.netease.nim.uikit.R;
import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.api.model.SimpleCallback;
import com.netease.nim.uikit.api.model.team.TeamDataChangedObserver;
import com.netease.nim.uikit.api.model.team.TeamMemberDataChangedObserver;
import com.netease.nim.uikit.api.model.user.UserInfoObserver;
import com.netease.nim.uikit.api.wrapper.NimToolBarOptions;
import com.netease.nim.uikit.business.contact.core.item.ContactIdFilter;
import com.netease.nim.uikit.business.contact.selector.activity.ContactSelectActivity;
import com.netease.nim.uikit.business.team.adapter.TeamMemberAdapter;
import com.netease.nim.uikit.business.team.helper.TeamHelper;
import com.netease.nim.uikit.business.team.model.TeamExtras;
import com.netease.nim.uikit.business.team.model.TeamRequestCode;
import com.netease.nim.uikit.business.team.ui.TeamInfoGridView;
import com.netease.nim.uikit.business.team.viewholder.TeamMemberHolder;
import com.netease.nim.uikit.common.activity.ToolBarOptions;
import com.netease.nim.uikit.common.activity.UI;
import com.netease.nim.uikit.common.adapter.TAdapterDelegate;
import com.netease.nim.uikit.common.adapter.TViewHolder;
import com.netease.nim.uikit.common.ui.dialog.DialogMaker;
import com.netease.nim.uikit.common.ui.widget.SwitchButton;
import com.netease.nim.uikit.common.util.sys.NetworkUtil;
import com.netease.nim.uikit.impl.NimUIKitImpl;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.team.TeamService;
import com.netease.nimlib.sdk.team.constant.TeamFieldEnum;
import com.netease.nimlib.sdk.team.constant.TeamMemberType;
import com.netease.nimlib.sdk.team.constant.TeamMessageNotifyTypeEnum;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.nimlib.sdk.team.model.TeamMember;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 讨论组资料页
 * <p/>
 * Created by huangjun on 2015/3/3.
 */
public class NormalTeamInfoActivity extends UI implements OnClickListener, TAdapterDelegate,
        TeamMemberAdapter.RemoveMemberCallback, TeamMemberAdapter.AddMemberCallback, TeamMemberHolder.TeamMemberHolderEventListener {

    // constant
    private static final String TAG = "TeamInfoActivity";

    private static final int REQUEST_CODE_NAME = 101;

    private static final int REQUEST_CODE_CONTACT_SELECT = 102;

    private static final String EXTRA_ID = "EXTRA_ID";

    private static final String KEY_MSG_NOTICE = "msg_notice";

    // adapter & data source

    private TeamMemberAdapter adapter;

    private String teamId;

    private Team team;

    private String creator;

    private List<String> memberAccounts;

    private List<TeamMemberAdapter.TeamMemberItem> dataSource;

    private UserInfoObserver userInfoObserver;

    // view
    private TextView teamNameTextView;

    private TeamInfoGridView gridView;

    private ViewGroup toggleLayout;

    private SwitchButton noticeBtn;

    // state
    private boolean isSelfAdmin = false;

    private int teamCapacity = 200; // 群人数上限，暂定

    /**
     * 启动群资料页
     *
     * @param context 调用方Activity
     * @param id      讨论组ID
     */
    public static void start(Context context, String id) {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_ID, id);
        intent.setClass(context, NormalTeamInfoActivity.class);
        ((Activity) context).startActivityForResult(intent, TeamRequestCode.REQUEST_CODE);
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

    /**
     * ***************************** Life cycle *****************************
     */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nim_team_info_activity);

        ToolBarOptions options = new NimToolBarOptions();
        setToolBar(R.id.toolbar, options);

        parseIntentData();
        initToggleBtn();
        loadTeamInfo();
        initAdapter();
        findViews();
        requestMembers();

        registerObservers(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        registerObservers(false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CONTACT_SELECT && resultCode == Activity.RESULT_OK) {
            final ArrayList<String> selected = data.getStringArrayListExtra(ContactSelectActivity.RESULT_DATA);
            if (selected != null && !selected.isEmpty()) {
                addMembersToTeam(selected);
            }
        }
    }

    /**
     * ************************** 群信息变更监听 **************************
     */
    /**
     * 注册群信息更新监听
     *
     * @param register
     */
    private void registerObservers(boolean register) {
        NimUIKit.getTeamChangedObservable().registerTeamDataChangedObserver(teamDataObserver, register);
        NimUIKit.getTeamChangedObservable().registerTeamMemberDataChangedObserver(teamMemberObserver, register);
        registerUserInfoChangedObserver(register);
    }

    TeamDataChangedObserver teamDataObserver = new TeamDataChangedObserver() {
        @Override
        public void onUpdateTeams(List<Team> teams) {
            for (Team team : teams) {
                if (team.getId().equals(teamId)) {
                    updateTeamInfo(team);
                    break;
                }
            }
        }

        @Override
        public void onRemoveTeam(Team team) {
            if (team.getId().equals(teamId)) {
                NormalTeamInfoActivity.this.team = team;
            }
        }
    };

    TeamMemberDataChangedObserver teamMemberObserver = new TeamMemberDataChangedObserver() {

        @Override
        public void onUpdateTeamMember(List<TeamMember> members) {
            List<String> accounts = new ArrayList<>();
            for (TeamMember m : members) {
                if (m.getTid().equals(teamId)) {
                    accounts.add(m.getAccount());
                }
            }

            if (!accounts.isEmpty()) {
                addMember(accounts, null, false);
            }
        }

        @Override
        public void onRemoveTeamMember(List<TeamMember> members) {
            for (TeamMember member : members) {
                if (member.getTid().equals(teamId)) {
                    removeMember(member.getAccount());
                }
            }
        }
    };

    private void refreshMembers(List<TeamMember> members) {
        gridView.setVisibility(View.VISIBLE);
        List<String> accounts = new ArrayList<>();
        for (TeamMember member : members) {
            // 标记创建者（群主）
            if (member.getType() == TeamMemberType.Owner) {
                creator = member.getAccount();
                if (creator.equals(NimUIKit.getAccount())) {
                    isSelfAdmin = true;
                }
            }
            accounts.add(member.getAccount());
        }
        addMember(accounts, null, true);
    }

    private void parseIntentData() {
        teamId = getIntent().getStringExtra(EXTRA_ID);
    }

    private void initToggleBtn() {
        toggleLayout = findView(R.id.toggle_layout);
        noticeBtn = addToggleItemView(KEY_MSG_NOTICE, R.string.team_notification_config, true);
    }

    private void setToggleBtn(Team team) {
        if (noticeBtn != null) {
            noticeBtn.setCheck(team.getMessageNotifyType() == TeamMessageNotifyTypeEnum.All);
        }
    }

    private SwitchButton addToggleItemView(String key, int titleResId, boolean initState) {
        ViewGroup vp = (ViewGroup) getLayoutInflater().inflate(R.layout.nim_user_profile_toggle_item, null);
        ViewGroup.LayoutParams vlp = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, (int) getResources().getDimension(R.dimen.isetting_item_height));
        vp.setLayoutParams(vlp);

        TextView titleText = ((TextView) vp.findViewById(R.id.user_profile_title));
        titleText.setText(titleResId);

        SwitchButton switchButton = (SwitchButton) vp.findViewById(R.id.user_profile_toggle);
        switchButton.setCheck(initState);
        switchButton.setOnChangedListener(onChangedListener);
        switchButton.setTag(key);

        toggleLayout.addView(vp);

        return switchButton;
    }

    private SwitchButton.OnChangedListener onChangedListener = new SwitchButton.OnChangedListener() {
        @Override
        public void OnChanged(View v, final boolean checkState) {
            if (!NetworkUtil.isNetAvailable(NormalTeamInfoActivity.this)) {
                Toast.makeText(NormalTeamInfoActivity.this, R.string.network_is_not_available, Toast.LENGTH_SHORT).show();
                noticeBtn.setCheck(!checkState);
                return;
            }
            TeamMessageNotifyTypeEnum typeEnum = checkState ? TeamMessageNotifyTypeEnum.All : TeamMessageNotifyTypeEnum.Mute;
            NIMClient.getService(TeamService.class).muteTeam(team.getId(), typeEnum).setCallback(new RequestCallback<Void>() {
                @Override
                public void onSuccess(Void param) {
                    if (checkState) {
                        Toast.makeText(NormalTeamInfoActivity.this, "开启消息提醒", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(NormalTeamInfoActivity.this, "关闭消息提醒", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailed(int code) {
                    if (code == 408) {
                        Toast.makeText(NormalTeamInfoActivity.this, R.string.network_is_not_available, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(NormalTeamInfoActivity.this, "on failed:" + code, Toast.LENGTH_SHORT).show();
                    }
                    noticeBtn.setCheck(!checkState);
                }

                @Override
                public void onException(Throwable exception) {

                }
            });
        }
    };

    private void loadTeamInfo() {
        creator = "";
        Team t = NimUIKit.getTeamProvider().getTeamById(teamId);
        if (t != null) {
            updateTeamInfo(t);
        } else {
            NimUIKit.getTeamProvider().fetchTeamById(teamId, new SimpleCallback<Team>() {
                @Override
                public void onResult(boolean success, Team result, int code) {
                    if (success && result != null) {
                        updateTeamInfo(result);
                    } else {
                        onGetTeamInfoFailed();
                    }
                }
            });
        }

    }

    private void updateTeamInfo(Team t) {
        if (t == null) {
            return;
        }

        team = t;

        // title
        String teamName = team.getName();
        setTitle(teamName);

        // team name
        View nameView = findViewById(R.id.settings_item_name);
        teamNameTextView = (TextView) nameView.findViewById(R.id.item_detail);
        teamNameTextView.setText(teamName);
        teamNameTextView.setEnabled(isSelfAdmin);

        setToggleBtn(team);
    }

    private void onGetTeamInfoFailed() {
        Toast.makeText(this, getString(R.string.normal_team_not_exist), Toast.LENGTH_SHORT).show();
        finish();
    }

    private void initAdapter() {
        memberAccounts = new ArrayList<>();
        dataSource = new ArrayList<>();
        adapter = new TeamMemberAdapter(this, dataSource, this, this, this);
        adapter.setEventListener(this);
    }

    private void findViews() {
        // grid view
        gridView = (TeamInfoGridView) findViewById(R.id.team_members_grid_view);
        gridView.setSelector(R.color.transparent);
        gridView.setOnScrollListener(new AbsListView.OnScrollListener() {

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
        gridView.setOnTouchListener(new View.OnTouchListener() {

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
        gridView.setAdapter(adapter);

        // discussion name view
        View nameView = findViewById(R.id.settings_item_name);
        nameView.setOnClickListener(this);
        TextView nameLabel = (TextView) nameView.findViewById(R.id.item_title);
        nameLabel.setText(R.string.normal_team_name);

        // talk button
        Button quitBtn = (Button) findViewById(R.id.quit_team);
        quitBtn.setOnClickListener(this);
    }

    /**
     * *************************** 加载&变更数据源 ********************************
     */
    private void requestMembers() {
        gridView.setVisibility(View.GONE);
        memberAccounts.clear();
        if (team != null) {
            NimUIKit.getTeamProvider().fetchTeamMemberList(teamId, new SimpleCallback<List<TeamMember>>() {
                @Override
                public void onResult(boolean success, List<TeamMember> members, int code) {
                    if (success && members != null && !members.isEmpty()) {
                        refreshMembers(members);
                    } else {
                        Toast.makeText(NormalTeamInfoActivity.this, "获取成员列表失败", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void addMember(List<String> accounts, List<String> failed, boolean clear) {
        if (accounts == null || accounts.isEmpty()) {
            return;
        }

        if (clear) {
            this.memberAccounts.clear();
        }

        // add
        if (this.memberAccounts.isEmpty()) {
            this.memberAccounts.addAll(accounts);
        } else {
            for (String account : accounts) {
                if (!this.memberAccounts.contains(account) && (failed == null || !failed.contains(account))) {
                    this.memberAccounts.add(account);
                }
            }
        }

        // sort
        Collections.sort(this.memberAccounts, new Comparator<String>() {
            @Override
            public int compare(String l, String r) {
                if (creator == null) {
                    return 0;
                }

                if (creator.equals(l)) {
                    return -1;
                }
                if (creator.equals(r)) {
                    return 1;
                }

                return l.compareTo(r);
            }
        });

        updateDataSource();
    }

    private void updateDataSource() {
        dataSource.clear();

        // member item
        String identity;
        for (String account : memberAccounts) {
            if (creator.equals(account)) {
                identity = TeamMemberHolder.OWNER;
            } else {
                identity = null;
            }
            dataSource.add(new TeamMemberAdapter.TeamMemberItem(TeamMemberAdapter.TeamMemberItemTag
                    .NORMAL, teamId, account, identity));
        }

        // add item
        dataSource.add(new TeamMemberAdapter.TeamMemberItem(TeamMemberAdapter.TeamMemberItemTag.ADD, null, null, null));

        // remove item
        if (isSelfAdmin) {
            dataSource.add(new TeamMemberAdapter.TeamMemberItem(TeamMemberAdapter.TeamMemberItemTag.DELETE, null, null,
                    null));
        }

        // refresh
        if (adapter.getMode() != TeamMemberAdapter.Mode.DELETE) {
            adapter.notifyDataSetChanged();
        }
    }

    private void removeMember(String account) {
        memberAccounts.remove(account);
        for (TeamMemberAdapter.TeamMemberItem item : dataSource) {
            if (account.equals(item.getAccount())) {
                dataSource.remove(item);
                break;
            }
        }
        // 为了解决2.3系统，移除用户后刷新界面不显示的问题
        if (Build.VERSION.SDK_INT < 11) {
            adapter.setMode(TeamMemberAdapter.Mode.NORMAL);
        }

        adapter.notifyDataSetChanged();
    }

    /**
     * ******************************* Action *********************************
     */

    /**
     * 邀请群成员
     */
    @Override
    public void onAddMember() {
        ContactSelectActivity.Option option = new ContactSelectActivity.Option();
        option.title = "邀请成员";
        ArrayList<String> disableAccounts = new ArrayList<>();
        disableAccounts.addAll(memberAccounts);
        option.itemDisableFilter = new ContactIdFilter(disableAccounts);

        // 限制群成员数量在群容量范围内
        int capacity = teamCapacity - memberAccounts.size();
        option.maxSelectNum = capacity;
        option.maxSelectedTip = getString(R.string.reach_team_member_capacity, teamCapacity);
        NimUIKit.startContactSelector(NormalTeamInfoActivity.this, option, REQUEST_CODE_CONTACT_SELECT);
    }

    /**
     * 移除群成员
     */
    @Override
    public void onRemoveMember(final String account) {
        DialogMaker.showProgressDialog(this, getString(R.string.empty), true);
        NIMClient.getService(TeamService.class).removeMember(teamId, account).setCallback(new RequestCallback<Void>
                () {
            @Override
            public void onSuccess(Void param) {
                DialogMaker.dismissProgressDialog();
                removeMember(account);
                Toast.makeText(NormalTeamInfoActivity.this, R.string.remove_member_success, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailed(int code) {
                DialogMaker.dismissProgressDialog();
                Toast.makeText(NormalTeamInfoActivity.this, R.string.remove_member_failed, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onException(Throwable exception) {
                DialogMaker.dismissProgressDialog();
            }
        });
    }


    /**
     * 添加群成员
     */
    private void addMembersToTeam(final ArrayList<String> selected) {
        // add members
        DialogMaker.showProgressDialog(this, getString(R.string.empty), true);
        NIMClient.getService(TeamService.class).addMembers(teamId, selected).setCallback(new RequestCallback<List<String>>() {
            @Override
            public void onSuccess(List<String> failedAccounts) {
                DialogMaker.dismissProgressDialog();
                addMember(selected, failedAccounts, false);
                if (failedAccounts != null && !failedAccounts.isEmpty()) {
                    TeamHelper.onMemberTeamNumOverrun(failedAccounts, NormalTeamInfoActivity.this);
                } else {
                    Toast.makeText(NormalTeamInfoActivity.this, R.string.invite_member_success, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailed(int code) {
                DialogMaker.dismissProgressDialog();
                Toast.makeText(NormalTeamInfoActivity.this, R.string.invite_member_failed, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onException(Throwable exception) {
                DialogMaker.dismissProgressDialog();
            }
        });
    }

    /**
     * 非群主退出群
     */
    private void quitTeam() {
        DialogMaker.showProgressDialog(this, getString(R.string.empty), true);
        NIMClient.getService(TeamService.class).quitTeam(teamId).setCallback(new RequestCallback<Void>() {
            @Override
            public void onSuccess(Void param) {
                DialogMaker.dismissProgressDialog();
                Toast.makeText(NormalTeamInfoActivity.this, R.string.quit_normal_team_success, Toast.LENGTH_SHORT).show();
                setResult(Activity.RESULT_OK, new Intent().putExtra(TeamExtras.RESULT_EXTRA_REASON, TeamExtras.RESULT_EXTRA_REASON_QUIT));

                NIMClient.getService(MsgService.class).deleteRecentContact2(teamId, SessionTypeEnum.Team);
                finish();
            }

            @Override
            public void onFailed(int code) {
                DialogMaker.dismissProgressDialog();
                Toast.makeText(NormalTeamInfoActivity.this, R.string.quit_normal_team_failed, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onException(Throwable exception) {
                DialogMaker.dismissProgressDialog();
            }
        });
    }

    /**
     * ******************************* Event *********************************
     */

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.quit_team) {
            quitTeam();

        } else if (i == R.id.settings_item_name) {
            TeamPropertySettingActivity.start(NormalTeamInfoActivity.this, teamId, TeamFieldEnum.Name, teamNameTextView.getText().toString(), REQUEST_CODE_NAME);

        } else {
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && adapter.switchMode()) {
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        if (adapter.switchMode()) {
            return;
        }

        super.onBackPressed();
    }

    @Override
    public void onHeadImageViewClick(String account) {
        if (NimUIKitImpl.getContactEventListener() != null) {
            NimUIKitImpl.getContactEventListener().onAvatarClick(this, account);
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
