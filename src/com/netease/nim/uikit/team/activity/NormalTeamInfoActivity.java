package com.netease.nim.uikit.team.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.netease.nim.uikit.NimUIKit;
import com.netease.nim.uikit.R;
import com.netease.nim.uikit.common.activity.TActionBarActivity;
import com.netease.nim.uikit.common.adapter.TAdapterDelegate;
import com.netease.nim.uikit.common.adapter.TViewHolder;
import com.netease.nim.uikit.common.ui.dialog.DialogMaker;
import com.netease.nim.uikit.common.ui.widget.SwitchButton;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.netease.nim.uikit.contact.core.item.ContactIdFilter;
import com.netease.nim.uikit.contact_selector.activity.ContactSelectActivity;
import com.netease.nim.uikit.team.TeamDataCache;
import com.netease.nim.uikit.team.adapter.TeamMemberAdapter;
import com.netease.nim.uikit.team.model.TeamExtras;
import com.netease.nim.uikit.team.model.TeamRequestCode;
import com.netease.nim.uikit.team.ui.TeamInfoGridView;
import com.netease.nim.uikit.team.viewholder.TeamMemberHolder;
import com.netease.nim.uikit.uinfo.UserInfoHelper;
import com.netease.nim.uikit.uinfo.UserInfoObservable;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.team.TeamService;
import com.netease.nimlib.sdk.team.constant.TeamFieldEnum;
import com.netease.nimlib.sdk.team.constant.TeamMemberType;
import com.netease.nimlib.sdk.team.constant.TeamTypeEnum;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.nimlib.sdk.team.model.TeamMember;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 普通群群资料页(TARGET_CREATE_NORMAL_TEAM) / 创建普通群(TARGET_TEAM_INFO)
 * <p/>
 * Created by huangjun on 2015/3/3.
 */
public class NormalTeamInfoActivity extends TActionBarActivity implements OnClickListener, TAdapterDelegate,
        TeamMemberAdapter.RemoveMemberCallback, TeamMemberAdapter.AddMemberCallback, TeamMemberHolder.TeamMemberHolderEventListener {

    // constant
    private static final String TAG = "TeamInfoActivity";

    private static final int TARGET_TEAM_INFO = 0x00;

    private static final int TARGET_CREATE_NORMAL_TEAM = 0x01;

    private static final int REQUEST_CODE_NAME = 101;

    private static final int REQUEST_CODE_CONTACT_SELECT = 102;

    private static final String EXTRA_ID = "EXTRA_ID";

    private static final String EXTRA_FIRST_MEMBER_ACCOUNT = "EXTRA_FIRST_MEMBER_ACCOUNT";

    private static final String EXTRA_TARGET = "EXTRA_TARGET";

    private static final String KEY_MSG_NOTICE = "msg_notice";

    // adapter & data source

    private TeamMemberAdapter adapter;

    private String teamId;

    private Team team;

    private String creator;

    private List<String> memberAccounts;

    private List<TeamMemberAdapter.TeamMemberItem> dataSource;

    private String firstMemberAccount;

    private Map<String, Boolean> toggleStateMap;

    private UserInfoObservable.UserInfoObserver userInfoObserver;

    // view
    private TextView teamNameTextView;

    private TeamInfoGridView gridView;

    private View layoutNotificationConfig;

    private View notifyLayout;

    private TextView notificationConfigText;

    private ViewGroup toggleLayout;

    private SwitchButton noticeBtn;

    // state
    private boolean isSelfAdmin = false;

    private int teamCapacity = 50; // 群人数上限，暂定

    private int target = TARGET_TEAM_INFO;

    /**
     * 启动群资料页
     *
     * @param context 调用方Activity
     * @param id      讨论组ID
     */
    public static void startForTeamInfo(Context context, String id) {
        start(context, id, TARGET_TEAM_INFO);
    }

    /**
     * 创建普通群
     *
     * @param context 调用方Activity
     */
    public static void startForCreateNormalTeam(Context context) {
        start(context, null, TARGET_CREATE_NORMAL_TEAM);
    }

    /**
     * 创建普通群
     *
     * @param context       调用方Activity
     * @param firstMemberId 点对点聊天对象作为多人会话第一个成员
     */
    public static void startForCreateNormalTeam(Context context, String firstMemberId) {
        start(context, firstMemberId, TARGET_CREATE_NORMAL_TEAM);
    }

    private static void start(Context context, String id, int target) {
        Intent intent = new Intent();
        if (!TextUtils.isEmpty(id)) {
            intent.putExtra(target == TARGET_TEAM_INFO ? EXTRA_ID : EXTRA_FIRST_MEMBER_ACCOUNT, id);
        }
        intent.putExtra(EXTRA_TARGET, target);
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

        parseIntentData();
        initNotifyLayout();
        if (target == TARGET_CREATE_NORMAL_TEAM) {
            initCreateTeam();
        } else if (target == TARGET_TEAM_INFO) {
            loadTeamInfo();
        }

        initAdapter();
        findViews();
        requestMembers();

        registerObservers(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (target == TARGET_CREATE_NORMAL_TEAM) {
            setToggleBtn();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        registerObservers(false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_NAME && resultCode == Activity.RESULT_OK) {
            final String teamName = data.getStringExtra(TeamPropertySettingActivity.EXTRA_DATA);
            if (target == TARGET_CREATE_NORMAL_TEAM) {
                setItemDetail(R.id.settings_item_name, teamName);
            }
        }
        if (requestCode == REQUEST_CODE_CONTACT_SELECT && resultCode == Activity.RESULT_OK) {
            final ArrayList<String> selected = data.getStringArrayListExtra(ContactSelectActivity.RESULT_DATA);
            if (selected != null && !selected.isEmpty()) {
                if (target == TARGET_TEAM_INFO) {
                    addMembersToTeam(selected);
                    // 修复VH复用bug by xuwen
                    getHandler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            adapter.notifyDataSetChanged();
                        }
                    }, 300);
                } else if (target == TARGET_CREATE_NORMAL_TEAM) {
                    addMember(selected, false);
                }
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
        if (register) {
            TeamDataCache.getInstance().registerTeamDataChangedObserver(teamDataObserver);
        } else {
            TeamDataCache.getInstance().unregisterTeamDataChangedObserver(teamDataObserver);
        }

        registerUserInfoChangedObserver(register);
    }

    TeamDataCache.TeamDataChangedObserver teamDataObserver = new TeamDataCache.TeamDataChangedObserver() {
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

    private void parseIntentData() {
        teamId = getIntent().getStringExtra(EXTRA_ID);
        firstMemberAccount = getIntent().getStringExtra(EXTRA_FIRST_MEMBER_ACCOUNT);
        target = getIntent().getIntExtra(EXTRA_TARGET, TARGET_TEAM_INFO);
    }

    private void initNotifyLayout() {
        notifyLayout = findViewById(R.id.notify_layout);
        notifyLayout.setVisibility(View.GONE);
    }

    private void initCreateTeam() {
        View nameView = findViewById(R.id.settings_item_name);
        teamNameTextView = (TextView) nameView.findViewById(R.id.item_detail);
        if (target == TARGET_CREATE_NORMAL_TEAM) {
            teamNameTextView.setText("普通群");
            setTitle(R.string.chat_setting);
        }
        toggleLayout = findView(R.id.toggle_layout);
        initToggleBtn();

        creator = NimUIKit.getAccount();
    }

    private void initToggleBtn() {
        noticeBtn = addToggleItemView(KEY_MSG_NOTICE, R.string.team_notification_config, true);
    }

    private void setToggleBtn() {
        if (noticeBtn != null && toggleStateMap != null) {
            noticeBtn.setCheck(toggleStateMap.get(KEY_MSG_NOTICE));
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

        if (toggleStateMap == null) {
            toggleStateMap = new HashMap<>();
        }
        toggleStateMap.put(key, initState);
        return switchButton;
    }

    private SwitchButton.OnChangedListener onChangedListener = new SwitchButton.OnChangedListener() {
        @Override
        public void OnChanged(View v, boolean checkState) {
            String key = (String) v.getTag();
            if (toggleStateMap.containsKey(key)) {
                toggleStateMap.put(key, checkState);  // update state
                Log.i(TAG, "toggle " + key + "to " + checkState);
            }
        }
    };

    private void loadTeamInfo() {
        creator = "";
        Team t = TeamDataCache.getInstance().getTeamById(teamId);
        if (t != null) {
            updateTeamInfo(t);
        } else {
            NIMClient.getService(TeamService.class).queryTeam(teamId).setCallback(new RequestCallback<Team>() {
                @Override
                public void onSuccess(Team t) {
                    TeamDataCache.getInstance().addOrUpdateTeam(t);
                    updateTeamInfo(t);
                }

                @Override
                public void onFailed(int code) {
                    onGetTeamInfoFailed("" + code);
                }

                @Override
                public void onException(Throwable exception) {
                    onGetTeamInfoFailed(exception.getMessage().toString());
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
        // team notify
        notifyLayout.setVisibility(View.VISIBLE);
        setNotifyConfig();
        notificationConfigText.setText(team.mute() ? getString(R.string.close) : getString(R.string.open));
    }

    private void setNotifyConfig() {
        layoutNotificationConfig = findViewById(R.id.team_notification_config_layout);
        ((TextView) layoutNotificationConfig.findViewById(R.id.item_title)).setText(R.string.team_notification_config);
        notificationConfigText = (TextView) layoutNotificationConfig.findViewById(R.id.item_detail);
        layoutNotificationConfig.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                NIMClient.getService(TeamService.class).muteTeam(teamId, !team.mute()).setCallback(new RequestCallback<Void>() {
                    @Override
                    public void onSuccess(Void param) {
                        notificationConfigText.setText(team.mute() ? getString(R.string.close) : getString(R.string.open));
                    }

                    @Override
                    public void onFailed(int code) {
                        Log.d(TAG, "muteTeam failed code:" + code);
                    }

                    @Override
                    public void onException(Throwable exception) {

                    }
                });
            }
        });
    }

    private void onGetTeamInfoFailed(String errorMsg) {
        Toast.makeText(this, getString(R.string.team_not_exist) + ", errorMsg=" + errorMsg, Toast.LENGTH_SHORT).show();
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
        nameLabel.setText(R.string.team_settings_name);

        // talk button
        if (target == TARGET_TEAM_INFO) {
            Button quitBtn = (Button) findViewById(R.id.quit_team);
            quitBtn.setText(R.string.quit_team);
            quitBtn.setOnClickListener(this);
            findViewById(R.id.create_team).setVisibility(View.GONE);
        } else if (target == TARGET_CREATE_NORMAL_TEAM) {
            Button btn = (Button) findViewById(R.id.create_team);
            btn.setOnClickListener(this);
            btn.setVisibility(View.VISIBLE);
            btn.setText(R.string.create_team);
            findViewById(R.id.quit_team).setVisibility(View.GONE);
        }

    }

    private void setItemDetail(int id, String detail) {
        // 设置群名称
        View view = findViewById(id);
        if (view != null) {
            TextView detailLabel = (TextView) view.findViewById(R.id.item_detail);
            if (detailLabel != null) {
                detailLabel.setText(detail);
            }
        }
    }

    /**
     * *************************** 加载&变更数据源 ********************************
     */
    private void requestMembers() {
        if (target == TARGET_TEAM_INFO) {
            gridView.setVisibility(View.GONE);
            memberAccounts.clear();
            if (team != null) {
                NIMClient.getService(TeamService.class).queryMemberList
                        (teamId).setCallback(new RequestCallback<List<TeamMember>>() {
                    @Override
                    public void onSuccess(List<TeamMember> members) {
                        if (members == null || members.isEmpty()) {
                            String errorMsg = "queryMemberList empty";
                            LogUtil.e(TAG, errorMsg);
                            Toast.makeText(NormalTeamInfoActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                            return;
                        }

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
                        addMember(accounts, true);
                    }

                    @Override
                    public void onFailed(int code) {
                        String errorMsg = "request team member list failed :" + code;
                        Toast.makeText(NormalTeamInfoActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                        LogUtil.e(TAG, errorMsg);
                    }

                    @Override
                    public void onException(Throwable exception) {
                        String errorMsg = "request team member list exception :" + exception.getMessage();
                        Toast.makeText(NormalTeamInfoActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                        LogUtil.e(TAG, errorMsg);
                    }
                });
            }
        } else if (target == TARGET_CREATE_NORMAL_TEAM) {
            List<String> accounts = new ArrayList<>();
            accounts.add(creator);
            if (!TextUtils.isEmpty(firstMemberAccount)) {
                accounts.add(firstMemberAccount);
            }
            addMember(accounts, true);
        }

    }

    private void addMember(List<String> accounts, boolean clear) {
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
                if (!this.memberAccounts.contains(account)) {
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
        if (isSelfAdmin && target == TARGET_TEAM_INFO) {
            dataSource.add(new TeamMemberAdapter.TeamMemberItem(TeamMemberAdapter.TeamMemberItemTag.DELETE, null, null,
                    null));
        }

        // refresh
        adapter.notifyDataSetChanged();
    }

    private void removeMember(String account) {
        memberAccounts.remove(account);
        for (TeamMemberAdapter.TeamMemberItem item : dataSource) {
            if (account.equals(item.getAccount())) {
                dataSource.remove(item);
                break;
            }
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
        option.title = "邀请群成员";
        ArrayList<String> disableAccounts = new ArrayList<>();
        disableAccounts.addAll(memberAccounts);
        option.itemDisableFilter = new ContactIdFilter(disableAccounts);

        // 限制群成员数量在群容量范围内
        int capacity = teamCapacity - memberAccounts.size();
        option.maxSelectNum = capacity;
        option.maxSelectedTip = getString(R.string.reach_team_member_capacity, teamCapacity);
        NimUIKit.startContactSelect(NormalTeamInfoActivity.this, option, REQUEST_CODE_CONTACT_SELECT);
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
        NIMClient.getService(TeamService.class).addMembers(teamId, selected).setCallback(new RequestCallback<Void>() {
            @Override
            public void onSuccess(Void param) {
                DialogMaker.dismissProgressDialog();
                addMember(selected, false);
                Toast.makeText(NormalTeamInfoActivity.this, R.string.invite_member_success, Toast.LENGTH_SHORT).show();
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
                Toast.makeText(NormalTeamInfoActivity.this, R.string.quit_team_success, Toast.LENGTH_SHORT).show();
                setResult(Activity.RESULT_OK, new Intent().putExtra(TeamExtras.RESULT_EXTRA_REASON, TeamExtras.RESULT_EXTRA_REASON_QUIT));

                NIMClient.getService(MsgService.class).deleteRecentContact2(teamId, SessionTypeEnum.Team);
                finish();
            }

            @Override
            public void onFailed(int code) {
                DialogMaker.dismissProgressDialog();
                Toast.makeText(NormalTeamInfoActivity.this, R.string.quit_team_failed, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onException(Throwable exception) {
                DialogMaker.dismissProgressDialog();
            }
        });
    }

    /**
     * 创建群
     */
    private void createTeam() {
        ArrayList<String> accounts = new ArrayList<>();
        accounts.addAll(memberAccounts);
        accounts.remove(creator);
        if (accounts.isEmpty()) {
            Toast.makeText(NormalTeamInfoActivity.this, R.string.team_create_notice, Toast.LENGTH_SHORT).show();
            return;
        }

        String teamName = teamNameTextView.getText().toString();
        if (TextUtils.isEmpty(teamName)) {
            teamName = "普通群";
        }

        DialogMaker.showProgressDialog(this, getString(R.string.empty), true);
        // 创建群
        HashMap<TeamFieldEnum, Serializable> fields = new HashMap<TeamFieldEnum, Serializable>();
        fields.put(TeamFieldEnum.Name, teamName);
        NIMClient.getService(TeamService.class).createTeam(fields, TeamTypeEnum.Normal, "",
                accounts).setCallback(
                new RequestCallback<Team>() {
                    @Override
                    public void onSuccess(Team team) {
                        DialogMaker.dismissProgressDialog();
                        TeamDataCache.getInstance().addOrUpdateTeam(team);
                        Toast.makeText(NormalTeamInfoActivity.this, R.string.create_team_success,
                                Toast.LENGTH_SHORT).show();
                        setResult(Activity.RESULT_OK, new Intent().putExtra(TeamExtras.RESULT_EXTRA_REASON,
                                TeamExtras.RESULT_EXTRA_REASON_CREATE).putExtra(TeamExtras.RESULT_EXTRA_DATA, team.getId()));
                        updateTeamMute(team);
                        finish();
                    }

                    @Override
                    public void onFailed(int code) {
                        DialogMaker.dismissProgressDialog();
                        if (code == 801) {
                            String tip = getString(R.string.over_team_member_capacity, teamCapacity);
                            Toast.makeText(NormalTeamInfoActivity.this, tip,
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(NormalTeamInfoActivity.this, R.string.create_team_failed,
                                    Toast.LENGTH_SHORT).show();
                        }

                        Log.e(TAG, "create team error: " + code);
                    }

                    @Override
                    public void onException(Throwable exception) {
                        DialogMaker.dismissProgressDialog();
                    }
                }
        );
    }

    private void updateTeamMute(Team team) {
        if (toggleStateMap != null) {
            NIMClient.getService(TeamService.class).muteTeam(team.getId(), !toggleStateMap.get(KEY_MSG_NOTICE));
        }

    }

    /**
     * ******************************* Event *********************************
     */

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.quit_team) {
            quitTeam();

        } else if (i == R.id.create_team) {
            createTeam();

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
    public void onHeadImageViewClick(String uid) {
        NimUIKit.getContactEventListener().onAvatarClick(this, uid);
    }

    private void registerUserInfoChangedObserver(boolean register) {
        if (register) {
            if (userInfoObserver == null) {
                userInfoObserver = new UserInfoObservable.UserInfoObserver() {
                    @Override
                    public void onUserInfoChanged(List<String> accounts) {
                        adapter.notifyDataSetChanged();
                    }
                };
            }
            UserInfoHelper.registerObserver(userInfoObserver);
        } else {
            UserInfoHelper.unregisterObserver(userInfoObserver);
        }
    }
}
