package com.netease.nim.uikit.business.team.activity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
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
import com.netease.nim.uikit.business.session.actions.PickImageAction;
import com.netease.nim.uikit.business.session.constant.Extras;
import com.netease.nim.uikit.business.team.adapter.TeamMemberAdapter;
import com.netease.nim.uikit.business.team.adapter.TeamMemberAdapter.TeamMemberItem;
import com.netease.nim.uikit.business.team.helper.AnnouncementHelper;
import com.netease.nim.uikit.business.team.helper.TeamHelper;
import com.netease.nim.uikit.business.team.model.Announcement;
import com.netease.nim.uikit.business.team.ui.TeamInfoGridView;
import com.netease.nim.uikit.business.team.viewholder.TeamMemberHolder;
import com.netease.nim.uikit.common.activity.ToolBarOptions;
import com.netease.nim.uikit.common.activity.UI;
import com.netease.nim.uikit.common.adapter.TAdapterDelegate;
import com.netease.nim.uikit.common.adapter.TViewHolder;
import com.netease.nim.uikit.common.media.picker.PickImageHelper;
import com.netease.nim.uikit.common.ui.dialog.DialogMaker;
import com.netease.nim.uikit.common.ui.dialog.MenuDialog;
import com.netease.nim.uikit.common.ui.imageview.HeadImageView;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.netease.nim.uikit.common.util.sys.TimeUtil;
import com.netease.nimlib.sdk.AbortableFuture;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.RequestCallbackWrapper;
import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.nos.NosService;
import com.netease.nimlib.sdk.team.TeamService;
import com.netease.nimlib.sdk.team.constant.TeamBeInviteModeEnum;
import com.netease.nimlib.sdk.team.constant.TeamFieldEnum;
import com.netease.nimlib.sdk.team.constant.TeamInviteModeEnum;
import com.netease.nimlib.sdk.team.constant.TeamMemberType;
import com.netease.nimlib.sdk.team.constant.TeamMessageNotifyTypeEnum;
import com.netease.nimlib.sdk.team.constant.TeamUpdateModeEnum;
import com.netease.nimlib.sdk.team.constant.VerifyTypeEnum;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.nimlib.sdk.team.model.TeamMember;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 高级群群资料页
 * Created by huangjun on 2015/3/17.
 */
public class AdvancedTeamInfoActivity extends UI implements
        TAdapterDelegate, TeamMemberAdapter.AddMemberCallback, TeamMemberHolder.TeamMemberHolderEventListener {

    private static final int REQUEST_CODE_TRANSFER = 101;
    private static final int REQUEST_CODE_MEMBER_LIST = 102;
    private static final int REQUEST_CODE_CONTACT_SELECT = 103;
    private static final int REQUEST_PICK_ICON = 104;

    private static final int ICON_TIME_OUT = 30000;

    // constant
    private static final String TAG = "RegularTeamInfoActivity";

    private static final String EXTRA_ID = "EXTRA_ID";
    public static final String RESULT_EXTRA_REASON = "RESULT_EXTRA_REASON";
    public static final String RESULT_EXTRA_REASON_QUIT = "RESULT_EXTRA_REASON_QUIT";
    public static final String RESULT_EXTRA_REASON_DISMISS = "RESULT_EXTRA_REASON_DISMISS";

    private static final int TEAM_MEMBERS_SHOW_LIMIT = 5;

    // data
    private TeamMemberAdapter adapter;
    private String teamId;
    private Team team;
    private String creator;
    private List<String> memberAccounts;
    private List<TeamMember> members;
    private List<TeamMemberAdapter.TeamMemberItem> dataSource;
    private MenuDialog dialog;
    private MenuDialog authenDialog;
    private MenuDialog inviteDialog;
    private MenuDialog teamInfoUpdateDialog;
    private MenuDialog teamInviteeDialog;
    private MenuDialog teamNotifyDialog;
    private List<String> managerList;
    private UserInfoObserver userInfoObserver;
    private AbortableFuture<String> uploadFuture;

    // view
    private View headerLayout;
    private HeadImageView teamHeadImage;
    private TextView teamNameText;
    private TextView teamIdText;
    private TextView teamCreateTimeText;

    private TextView teamBusinessCard; // 我的群名片

    private View layoutMime;
    private View layoutTeamMember;
    private TeamInfoGridView gridView;
    private View layoutTeamName;
    private View layoutTeamIntroduce;
    private View layoutTeamAnnouncement;
    private View layoutTeamExtension;
    private View layoutAuthentication;
    private View layoutNotificationConfig;
    // 邀请他人权限
    private View layoutInvite;
    private TextView inviteText;
    // 群资料修改权限
    private View layoutInfoUpdate;
    private TextView infoUpdateText;
    // 被邀请人身份验证权限
    private View layoutInviteeAuthen;
    private TextView inviteeAutenText;

    private TextView memberCountText;
    private TextView introduceEdit;
    private TextView announcementEdit;
    private TextView extensionTextView;
    private TextView authenticationText;
    private TextView notificationConfigText;

    // state
    private boolean isSelfAdmin = false;
    private boolean isSelfManager = false;

    public static void start(Context context, String tid) {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_ID, tid);
        intent.setClass(context, AdvancedTeamInfoActivity.class);
        context.startActivity(intent);
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

        setContentView(R.layout.nim_advanced_team_info_activity);

        ToolBarOptions options = new NimToolBarOptions();
        setToolBar(R.id.toolbar, options);

        parseIntentData();
        findViews();
        initActionbar();
        initAdapter();
        loadTeamInfo();
        requestMembers();
        registerObservers(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        switch (requestCode) {
            case REQUEST_CODE_CONTACT_SELECT:
                final ArrayList<String> selected = data.getStringArrayListExtra(ContactSelectActivity.RESULT_DATA);
                if (selected != null && !selected.isEmpty()) {
                    inviteMembers(selected);
                }
                break;
            case REQUEST_CODE_TRANSFER:
                final ArrayList<String> target = data.getStringArrayListExtra(ContactSelectActivity.RESULT_DATA);
                if (target != null && !target.isEmpty()) {
                    transferTeam(target.get(0));
                }
                break;
            case AdvancedTeamNicknameActivity.REQ_CODE_TEAM_NAME:
                setBusinessCard(data.getStringExtra(AdvancedTeamNicknameActivity.EXTRA_NAME));
                break;
            case AdvancedTeamMemberInfoActivity.REQ_CODE_REMOVE_MEMBER:
                boolean isSetAdmin = data.getBooleanExtra(AdvancedTeamMemberInfoActivity.EXTRA_ISADMIN, false);
                boolean isRemoveMember = data.getBooleanExtra(AdvancedTeamMemberInfoActivity.EXTRA_ISREMOVE, false);
                String account = data.getStringExtra(EXTRA_ID);
                refreshAdmin(isSetAdmin, account);
                if (isRemoveMember) {
                    removeMember(account);
                }
                break;
            case REQUEST_CODE_MEMBER_LIST:
                boolean isMemberChange = data.getBooleanExtra(AdvancedTeamMemberActivity.EXTRA_DATA, false);
                if (isMemberChange) {
                    requestMembers();
                }
                break;
            case REQUEST_PICK_ICON:
                String path = data.getStringExtra(Extras.EXTRA_FILE_PATH);
                updateTeamIcon(path);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        if (dialog != null) {
            dialog.dismiss();
        }

        if (authenDialog != null) {
            authenDialog.dismiss();
        }

        registerObservers(false);

        super.onDestroy();
    }

    private void parseIntentData() {
        teamId = getIntent().getStringExtra(EXTRA_ID);
    }

    private void findViews() {
        headerLayout = findViewById(R.id.team_info_header);
        headerLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSelector(R.string.set_head_image, REQUEST_PICK_ICON);
            }
        });

        teamHeadImage = (HeadImageView) findViewById(R.id.team_head_image);
        teamNameText = (TextView) findViewById(R.id.team_name);
        teamIdText = (TextView) findViewById(R.id.team_id);
        teamCreateTimeText = (TextView) findViewById(R.id.team_create_time);

        layoutMime = findViewById(R.id.team_mime_layout);
        ((TextView) layoutMime.findViewById(R.id.item_title)).setText(R.string.my_team_card);
        teamBusinessCard = (TextView) layoutMime.findViewById(R.id.item_detail);
        layoutMime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AdvancedTeamNicknameActivity.start(AdvancedTeamInfoActivity.this, teamBusinessCard.getText().toString());
            }
        });

        layoutTeamMember = findViewById(R.id.team_memeber_layout);
        ((TextView) layoutTeamMember.findViewById(R.id.item_title)).setText(R.string.team_member);
        memberCountText = (TextView) layoutTeamMember.findViewById(R.id.item_detail);
        gridView = (TeamInfoGridView) findViewById(R.id.team_member_grid_view);
        layoutTeamMember.setVisibility(View.GONE);
        gridView.setVisibility(View.GONE);
        memberCountText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AdvancedTeamMemberActivity.startActivityForResult(AdvancedTeamInfoActivity.this, teamId, REQUEST_CODE_MEMBER_LIST);
            }
        });

        layoutTeamName = findViewById(R.id.team_name_layout);
        ((TextView) layoutTeamName.findViewById(R.id.item_title)).setText(R.string.team_name);
        layoutTeamName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TeamPropertySettingActivity.start(AdvancedTeamInfoActivity.this, teamId, TeamFieldEnum.Name, team.getName());
            }
        });

        layoutTeamIntroduce = findViewById(R.id.team_introduce_layout);
        ((TextView) layoutTeamIntroduce.findViewById(R.id.item_title)).setText(R.string.team_introduce);
        introduceEdit = ((TextView) layoutTeamIntroduce.findViewById(R.id.item_detail));
        introduceEdit.setHint(R.string.team_introduce_hint);
        layoutTeamIntroduce.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TeamPropertySettingActivity.start(AdvancedTeamInfoActivity.this, teamId, TeamFieldEnum.Introduce, team.getIntroduce());
            }
        });

        layoutTeamAnnouncement = findViewById(R.id.team_announcement_layout);
        ((TextView) layoutTeamAnnouncement.findViewById(R.id.item_title)).setText(R.string.team_annourcement);
        announcementEdit = ((TextView) layoutTeamAnnouncement.findViewById(R.id.item_detail));
        announcementEdit.setHint(R.string.team_announce_hint);
        layoutTeamAnnouncement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AdvancedTeamAnnounceActivity.start(AdvancedTeamInfoActivity.this, teamId);
            }
        });

        layoutTeamExtension = findViewById(R.id.team_extension_layout);
        ((TextView) layoutTeamExtension.findViewById(R.id.item_title)).setText(R.string.team_extension);
        extensionTextView = ((TextView) layoutTeamExtension.findViewById(R.id.item_detail));
        extensionTextView.setHint(R.string.team_extension_hint);
        layoutTeamExtension.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TeamPropertySettingActivity.start(AdvancedTeamInfoActivity.this, teamId, TeamFieldEnum.Extension, team.getExtension());
            }
        });

        // 群消息提醒设置
        initNotify();
        // 身份验证
        findLayoutAuthentication();
        // 邀请他人权限
        findLayoutInvite();
        // 群资料修改权限
        findLayoutInfoUpdate();
        // 被邀请人身份验证
        findLayoutInviteeAuthen();
    }

    /**
     * 打开图片选择器
     */
    private void showSelector(int titleId, final int requestCode) {
        PickImageHelper.PickImageOption option = new PickImageHelper.PickImageOption();
        option.titleResId = titleId;
        option.multiSelect = false;
        option.crop = true;
        option.cropOutputImageWidth = 720;
        option.cropOutputImageHeight = 720;

        PickImageHelper.pickImage(AdvancedTeamInfoActivity.this, requestCode, option);
    }

    /**
     * 群消息提醒设置
     */
    private void initNotify() {
        layoutNotificationConfig = findViewById(R.id.team_notification_config_layout);
        ((TextView) layoutNotificationConfig.findViewById(R.id.item_title)).setText(R.string.team_notification_config);
        notificationConfigText = (TextView) layoutNotificationConfig.findViewById(R.id.item_detail);
        layoutNotificationConfig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTeamNotifyMenu();
            }
        });
    }


    /**
     * 身份验证布局初始化
     */
    private void findLayoutAuthentication() {
        layoutAuthentication = findViewById(R.id.team_authentication_layout);
        layoutAuthentication.setVisibility(View.GONE);
        ((TextView) layoutAuthentication.findViewById(R.id.item_title)).setText(R.string.team_authentication);
        authenticationText = ((TextView) layoutAuthentication.findViewById(R.id.item_detail));
        layoutAuthentication.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTeamAuthenMenu();
            }
        });
    }

    /**
     * 邀请他人权限布局初始化
     */
    private void findLayoutInvite() {
        layoutInvite = findViewById(R.id.team_invite_layout);
        layoutInvite.setVisibility(View.GONE);
        ((TextView) layoutInvite.findViewById(R.id.item_title)).setText(R.string.team_invite);
        inviteText = ((TextView) layoutInvite.findViewById(R.id.item_detail));
        layoutInvite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTeamInviteMenu();
            }
        });
    }

    /**
     * 群资料修改权限布局初始化
     */
    private void findLayoutInfoUpdate() {
        layoutInfoUpdate = findViewById(R.id.team_info_update_layout);
        layoutInfoUpdate.setVisibility(View.GONE);
        ((TextView) layoutInfoUpdate.findViewById(R.id.item_title)).setText(R.string.team_info_update);
        infoUpdateText = ((TextView) layoutInfoUpdate.findViewById(R.id.item_detail));
        layoutInfoUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTeamInfoUpdateMenu();
            }
        });
    }


    /**
     * 被邀请人身份验证布局初始化
     */
    private void findLayoutInviteeAuthen() {
        layoutInviteeAuthen = findViewById(R.id.team_invitee_authen_layout);
        layoutInviteeAuthen.setVisibility(View.GONE);
        ((TextView) layoutInviteeAuthen.findViewById(R.id.item_title)).setText(R.string.team_invitee_authentication);
        inviteeAutenText = ((TextView) layoutInviteeAuthen.findViewById(R.id.item_detail));
        layoutInviteeAuthen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTeamInviteeAuthenMenu();
            }
        });
    }

    private void initActionbar() {
        TextView toolbarView = findView(R.id.action_bar_right_clickable_textview);
        toolbarView.setText(R.string.menu);
        toolbarView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRegularTeamMenu();
            }
        });
    }

    private void initAdapter() {
        memberAccounts = new ArrayList<>();
        members = new ArrayList<>();
        dataSource = new ArrayList<>();
        managerList = new ArrayList<>();
        adapter = new TeamMemberAdapter(this, dataSource, this, null, this);
        adapter.setEventListener(this);

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
        gridView.setAdapter(adapter);
    }

    /**
     * 初始化群组基本信息
     */
    private void loadTeamInfo() {
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

    private void onGetTeamInfoFailed() {
        Toast.makeText(this, getString(R.string.team_not_exist), Toast.LENGTH_SHORT).show();
        finish();
    }

    /**
     * 更新群信息
     *
     * @param t
     */
    private void updateTeamInfo(final Team t) {
        this.team = t;

        if (team == null) {
            Toast.makeText(this, getString(R.string.team_not_exist), Toast.LENGTH_SHORT).show();
            finish();
            return;
        } else {
            creator = team.getCreator();
            if (creator.equals(NimUIKit.getAccount())) {
                isSelfAdmin = true;
            }

            setTitle(team.getName());
        }

        teamHeadImage.loadTeamIconByTeam(team);
        teamNameText.setText(team.getName());
        teamIdText.setText(team.getId());
        teamCreateTimeText.setText(TimeUtil.getTimeShowString(team.getCreateTime(), true));

        ((TextView) layoutTeamName.findViewById(R.id.item_detail)).setText(team.getName());
        introduceEdit.setText(team.getIntroduce());
        extensionTextView.setText(team.getExtension());
        memberCountText.setText(String.format("共%d人", team.getMemberCount()));

        setAnnouncement(team.getAnnouncement());
        setAuthenticationText(team.getVerifyType());
        updateTeamNotifyText(team.getMessageNotifyType());
        updateInviteText(team.getTeamInviteMode());
        updateInfoUpateText(team.getTeamUpdateMode());
        updateBeInvitedText(team.getTeamBeInviteMode());
    }

    /**
     * 更新群成员信息
     *
     * @param m
     */
    private void updateTeamMember(final List<TeamMember> m) {
        if (m != null && m.isEmpty()) {
            return;
        }

        updateTeamBusinessCard(m);
        addTeamMembers(m, true);
    }

    /**
     * 更新我的群名片
     *
     * @param m
     */
    private void updateTeamBusinessCard(List<TeamMember> m) {
        for (TeamMember teamMember : m) {
            if (teamMember != null && teamMember.getAccount().equals(NimUIKit.getAccount())) {
                teamBusinessCard.setText(teamMember.getTeamNick() != null ? teamMember.getTeamNick() : "");
            }
        }
    }

    /**
     * 添加群成员到列表
     *
     * @param m     群成员列表
     * @param clear 是否清除
     */
    private void addTeamMembers(final List<TeamMember> m, boolean clear) {
        if (m == null || m.isEmpty()) {
            return;
        }

        isSelfManager = false;
        isSelfAdmin = false;

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
            if (tm == null) {
                continue;
            }
            if (tm.getType() == TeamMemberType.Manager) {
                managerList.add(tm.getAccount());
            }
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

        updateAuthenView();
        updateTeamMemberDataSource();
    }

    /**
     * 更新身份验证是否显示
     */
    private void updateAuthenView() {
        if (isSelfAdmin || isSelfManager) {
            layoutAuthentication.setVisibility(View.VISIBLE);
            layoutInvite.setVisibility(View.VISIBLE);
            layoutInfoUpdate.setVisibility(View.VISIBLE);
            layoutInviteeAuthen.setVisibility(View.VISIBLE);
            announcementEdit.setHint(R.string.without_content);
        } else {
            layoutAuthentication.setVisibility(View.GONE);
            layoutInvite.setVisibility(View.GONE);
            layoutInfoUpdate.setVisibility(View.GONE);
            layoutInviteeAuthen.setVisibility(View.GONE);
            introduceEdit.setHint(R.string.without_content);
            announcementEdit.setHint(R.string.without_content);
        }
    }

    /**
     * 更新成员信息
     */
    private void updateTeamMemberDataSource() {
        if (members.size() > 0) {
            gridView.setVisibility(View.VISIBLE);
            layoutTeamMember.setVisibility(View.VISIBLE);
        } else {
            gridView.setVisibility(View.GONE);
            layoutTeamMember.setVisibility(View.GONE);
            return;
        }

        dataSource.clear();

        // add item
        if (team.getTeamInviteMode() == TeamInviteModeEnum.All || isSelfAdmin || isSelfManager) {
            dataSource.add(new TeamMemberAdapter.TeamMemberItem(TeamMemberAdapter.TeamMemberItemTag.ADD, null, null,
                    null));
        }

        // member item
        int count = 0;
        String identity = null;
        for (String account : memberAccounts) {
            int limit = TEAM_MEMBERS_SHOW_LIMIT;
            if (team.getTeamInviteMode() == TeamInviteModeEnum.All || isSelfAdmin || isSelfManager) {
                limit = TEAM_MEMBERS_SHOW_LIMIT - 1;
            }
            if (count < limit) {
                identity = getIdentity(account);
                dataSource.add(new TeamMemberAdapter.TeamMemberItem(TeamMemberAdapter.TeamMemberItemTag
                        .NORMAL, teamId, account, identity));
            }
            count++;
        }

        // refresh
        adapter.notifyDataSetChanged();
        memberCountText.setText(String.format("共%d人", count));
    }

    private String getIdentity(String account) {
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

    /**
     * *************************** 加载&变更数据源 ********************************
     */
    private void requestMembers() {
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
     * ************************** 群信息变更监听 **************************
     */
    /**
     * 注册群信息更新监听
     *
     * @param register
     */
    private void registerObservers(boolean register) {
        NimUIKit.getTeamChangedObservable().registerTeamMemberDataChangedObserver(teamMemberObserver, register);
        NimUIKit.getTeamChangedObservable().registerTeamDataChangedObserver(teamDataObserver, register);
        registerUserInfoChangedObserver(register);
    }

    TeamMemberDataChangedObserver teamMemberObserver = new TeamMemberDataChangedObserver() {

        @Override
        public void onUpdateTeamMember(List<TeamMember> m) {
            for (TeamMember mm : m) {
                for (TeamMember member : members) {
                    if (mm.getAccount().equals(member.getAccount())) {
                        members.set(members.indexOf(member), mm);
                        break;
                    }
                }
            }
            addTeamMembers(m, false);
        }

        @Override
        public void onRemoveTeamMember(List<TeamMember> members) {
            for (TeamMember member : members) {
                removeMember(member.getAccount());
            }
        }
    };

    TeamDataChangedObserver teamDataObserver = new TeamDataChangedObserver() {
        @Override
        public void onUpdateTeams(List<Team> teams) {
            for (Team team : teams) {
                if (team.getId().equals(teamId)) {
                    updateTeamInfo(team);
                    updateTeamMemberDataSource();
                    break;
                }
            }
        }

        @Override
        public void onRemoveTeam(Team team) {
            if (team.getId().equals(teamId)) {
                AdvancedTeamInfoActivity.this.team = team;
                finish();
            }
        }
    };

    /**
     * ******************************* Action *********************************
     */

    /**
     * 从联系人选择器发起邀请成员
     */
    @Override
    public void onAddMember() {
        ContactSelectActivity.Option option = TeamHelper.getContactSelectOption(memberAccounts);
        NimUIKit.startContactSelector(AdvancedTeamInfoActivity.this, option, REQUEST_CODE_CONTACT_SELECT);
    }

    /**
     * 从联系人选择器选择群转移对象
     */
    private void onTransferTeam() {
        if (memberAccounts.size() <= 1) {
            Toast.makeText(AdvancedTeamInfoActivity.this, R.string.team_transfer_without_member, Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        ContactSelectActivity.Option option = new ContactSelectActivity.Option();
        option.title = "选择群转移的对象";
        option.type = ContactSelectActivity.ContactSelectType.TEAM_MEMBER;
        option.teamId = teamId;
        option.multi = false;
        option.maxSelectNum = 1;
        ArrayList<String> includeAccounts = new ArrayList<>();
        includeAccounts.addAll(memberAccounts);
        option.itemFilter = new ContactIdFilter(includeAccounts, false);
        NimUIKit.startContactSelector(this, option, REQUEST_CODE_TRANSFER);
        dialog.dismiss();
    }

    /**
     * 邀请群成员
     *
     * @param accounts 邀请帐号
     */
    private void inviteMembers(ArrayList<String> accounts) {
        NIMClient.getService(TeamService.class).addMembers(teamId, accounts).setCallback(new RequestCallback<List<String>>() {
            @Override
            public void onSuccess(List<String> failedAccounts) {
                if (failedAccounts == null || failedAccounts.isEmpty()) {
                    Toast.makeText(AdvancedTeamInfoActivity.this, "添加群成员成功", Toast.LENGTH_SHORT).show();
                } else {
                    TeamHelper.onMemberTeamNumOverrun(failedAccounts, AdvancedTeamInfoActivity.this);
                }
            }

            @Override
            public void onFailed(int code) {
                if (code == ResponseCode.RES_TEAM_INVITE_SUCCESS) {
                    Toast.makeText(AdvancedTeamInfoActivity.this, R.string.team_invite_members_success, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(AdvancedTeamInfoActivity.this, "invite members failed, code=" + code, Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "invite members failed, code=" + code);
                }
            }

            @Override
            public void onException(Throwable exception) {

            }
        });
    }

    /**
     * 转让群
     *
     * @param account 转让的帐号
     */
    private void transferTeam(final String account) {
        TeamMember member = NimUIKit.getTeamProvider().getTeamMember(teamId, account);
        if (member == null) {
            Toast.makeText(AdvancedTeamInfoActivity.this, "成员不存在", Toast.LENGTH_SHORT).show();
            return;
        }
        if (member.isMute()) {
            Toast.makeText(AdvancedTeamInfoActivity.this, "该成员已被禁言，请先取消禁言", Toast.LENGTH_LONG).show();
            return;
        }
        NIMClient.getService(TeamService.class).transferTeam(teamId, account, false)
                .setCallback(new RequestCallback<List<TeamMember>>() {
                    @Override
                    public void onSuccess(List<TeamMember> members) {
                        creator = account;
                        updateTeamMember(NimUIKit.getTeamProvider().getTeamMemberList(teamId));
                        Toast.makeText(AdvancedTeamInfoActivity.this, R.string.team_transfer_success, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailed(int code) {
                        Toast.makeText(AdvancedTeamInfoActivity.this, R.string.team_transfer_failed, Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "team transfer failed, code=" + code);
                    }

                    @Override
                    public void onException(Throwable exception) {

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
                Toast.makeText(AdvancedTeamInfoActivity.this, R.string.quit_team_success, Toast.LENGTH_SHORT).show();
                setResult(Activity.RESULT_OK, new Intent().putExtra(RESULT_EXTRA_REASON, RESULT_EXTRA_REASON_QUIT));
                finish();
            }

            @Override
            public void onFailed(int code) {
                DialogMaker.dismissProgressDialog();
                Toast.makeText(AdvancedTeamInfoActivity.this, R.string.quit_team_failed, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onException(Throwable exception) {
                DialogMaker.dismissProgressDialog();
            }
        });
    }

    /**
     * 群主解散群(直接退出)
     */
    private void dismissTeam() {
        DialogMaker.showProgressDialog(this, getString(R.string.empty), true);
        NIMClient.getService(TeamService.class).dismissTeam(teamId).setCallback(new RequestCallback<Void>() {
            @Override
            public void onSuccess(Void param) {
                DialogMaker.dismissProgressDialog();
                setResult(Activity.RESULT_OK, new Intent().putExtra(RESULT_EXTRA_REASON, RESULT_EXTRA_REASON_DISMISS));
                Toast.makeText(AdvancedTeamInfoActivity.this, R.string.dismiss_team_success, Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailed(int code) {
                DialogMaker.dismissProgressDialog();
                Toast.makeText(AdvancedTeamInfoActivity.this, R.string.dismiss_team_failed, Toast.LENGTH_SHORT).show();
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
    /**
     * 显示菜单
     */
    private void showRegularTeamMenu() {
        List<String> btnNames = new ArrayList<>();
        if (isSelfAdmin) {
            btnNames.add(getString(R.string.dismiss_team));
            btnNames.add(getString(R.string.transfer_team));
            btnNames.add(getString(R.string.cancel));
        } else {
            btnNames.add(getString(R.string.quit_team));
            btnNames.add(getString(R.string.cancel));
        }
        dialog = new MenuDialog(this, btnNames, new MenuDialog.MenuDialogOnButtonClickListener() {
            @Override
            public void onButtonClick(String name) {
                if (name.equals(getString(R.string.quit_team))) {
                    quitTeam();
                } else if (name.equals(getString(R.string.dismiss_team))) {
                    dismissTeam();
                } else if (name.equals(getString(R.string.transfer_team))) {
                    onTransferTeam();
                }
                dialog.dismiss();
            }
        });

        dialog.show();
    }


    private void showTeamNotifyMenu() {
        if (teamNotifyDialog == null) {
            List<String> btnNames = TeamHelper.createNotifyMenuStrings();
            int type = team.getMessageNotifyType().getValue();
            teamNotifyDialog = new MenuDialog(AdvancedTeamInfoActivity.this, btnNames, type, 3, new MenuDialog
                    .MenuDialogOnButtonClickListener() {
                @Override
                public void onButtonClick(String name) {
                    teamNotifyDialog.dismiss();

                    TeamMessageNotifyTypeEnum type = TeamHelper.getNotifyType(name);
                    if (type == null) {
                        return;
                    }
                    DialogMaker.showProgressDialog(AdvancedTeamInfoActivity.this, getString(R.string.empty), true);
                    NIMClient.getService(TeamService.class).muteTeam(teamId, type).setCallback(new RequestCallback<Void>() {
                        @Override
                        public void onSuccess(Void param) {
                            DialogMaker.dismissProgressDialog();
                            updateTeamNotifyText(team.getMessageNotifyType());
                        }

                        @Override
                        public void onFailed(int code) {
                            DialogMaker.dismissProgressDialog();
                            teamNotifyDialog.undoLastSelect();
                            Log.d(TAG, "muteTeam failed code:" + code);
                        }

                        @Override
                        public void onException(Throwable exception) {
                            DialogMaker.dismissProgressDialog();
                        }
                    });
                }
            });
        }
        teamNotifyDialog.show();
    }

    /**
     * 显示验证菜单
     */
    private void showTeamAuthenMenu() {
        if (authenDialog == null) {
            List<String> btnNames = TeamHelper.createAuthenMenuStrings();

            int type = team.getVerifyType().getValue();
            authenDialog = new MenuDialog(AdvancedTeamInfoActivity.this, btnNames, type, 3, new MenuDialog
                    .MenuDialogOnButtonClickListener() {
                @Override
                public void onButtonClick(String name) {
                    authenDialog.dismiss();

                    if (name.equals(getString(R.string.cancel))) {
                        return; // 取消不处理
                    }
                    VerifyTypeEnum type = TeamHelper.getVerifyTypeEnum(name);
                    if (type != null) {
                        setAuthen(type);
                    }

                }
            });
        }
        authenDialog.show();
    }

    /**
     * 显示邀请他人权限菜单
     */
    private void showTeamInviteMenu() {
        if (inviteDialog == null) {
            List<String> btnNames = TeamHelper.createInviteMenuStrings();

            int type = team.getTeamInviteMode().getValue();
            inviteDialog = new MenuDialog(AdvancedTeamInfoActivity.this, btnNames, type, 2, new MenuDialog
                    .MenuDialogOnButtonClickListener() {
                @Override
                public void onButtonClick(String name) {
                    inviteDialog.dismiss();

                    if (name.equals(getString(R.string.cancel))) {
                        return; // 取消不处理
                    }
                    TeamInviteModeEnum type = TeamHelper.getInviteModeEnum(name);
                    if (type != null) {
                        updateInviteMode(type);
                    }
                }
            });
        }
        inviteDialog.show();
    }

    // 显示群资料修改权限菜单
    private void showTeamInfoUpdateMenu() {
        if (teamInfoUpdateDialog == null) {
            List<String> btnNames = TeamHelper.createTeamInfoUpdateMenuStrings();

            int type = team.getTeamUpdateMode().getValue();
            teamInfoUpdateDialog = new MenuDialog(AdvancedTeamInfoActivity.this, btnNames, type, 2, new MenuDialog
                    .MenuDialogOnButtonClickListener() {
                @Override
                public void onButtonClick(String name) {
                    teamInfoUpdateDialog.dismiss();

                    if (name.equals(getString(R.string.cancel))) {
                        return; // 取消不处理
                    }
                    TeamUpdateModeEnum type = TeamHelper.getUpdateModeEnum(name);
                    if (type != null) {
                        updateInfoUpdateMode(type);
                    }
                }
            });
        }
        teamInfoUpdateDialog.show();
    }

    // 显示被邀请人身份验证菜单
    private void showTeamInviteeAuthenMenu() {
        if (teamInviteeDialog == null) {
            List<String> btnNames = TeamHelper.createTeamInviteeAuthenMenuStrings();

            int type = team.getTeamBeInviteMode().getValue();
            teamInviteeDialog = new MenuDialog(AdvancedTeamInfoActivity.this, btnNames, type, 2, new MenuDialog
                    .MenuDialogOnButtonClickListener() {
                @Override
                public void onButtonClick(String name) {
                    teamInviteeDialog.dismiss();

                    if (name.equals(getString(R.string.cancel))) {
                        return; // 取消不处理
                    }
                    TeamBeInviteModeEnum type = TeamHelper.getBeInvitedModeEnum(name);
                    if (type != null) {
                        updateBeInvitedMode(type);
                    }
                }
            });
        }
        teamInviteeDialog.show();
    }


    /**
     * 设置我的名片
     *
     * @param nickname 群昵称
     */
    private void setBusinessCard(final String nickname) {
        DialogMaker.showProgressDialog(this, getString(R.string.empty), true);
        NIMClient.getService(TeamService.class).updateMemberNick(teamId, NimUIKit.getAccount(), nickname).setCallback(new RequestCallback<Void>() {
            @Override
            public void onSuccess(Void param) {
                DialogMaker.dismissProgressDialog();
                teamBusinessCard.setText(nickname);
                Toast.makeText(AdvancedTeamInfoActivity.this, R.string.update_success, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailed(int code) {
                DialogMaker.dismissProgressDialog();
                Toast.makeText(AdvancedTeamInfoActivity.this, String.format(getString(R.string.update_failed), code),
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onException(Throwable exception) {
                DialogMaker.dismissProgressDialog();
            }
        });
    }

    @Override
    public void onHeadImageViewClick(String account) {
        // 打开群成员信息详细页面
        AdvancedTeamMemberInfoActivity.startActivityForResult(AdvancedTeamInfoActivity.this, account, teamId);
    }

    /**
     * 设置群公告
     *
     * @param announcement 群公告
     */
    private void setAnnouncement(String announcement) {
        Announcement a = AnnouncementHelper.getLastAnnouncement(teamId, announcement);
        if (a == null) {
            announcementEdit.setText("");
        } else {
            announcementEdit.setText(a.getTitle());
        }
    }

    /**
     * 设置验证模式
     *
     * @param type 验证类型
     */
    private void setAuthen(final VerifyTypeEnum type) {
        DialogMaker.showProgressDialog(this, getString(R.string.empty));
        NIMClient.getService(TeamService.class).updateTeam(teamId, TeamFieldEnum.VerifyType, type).setCallback(new RequestCallback<Void>() {
            @Override
            public void onSuccess(Void param) {
                DialogMaker.dismissProgressDialog();
                setAuthenticationText(type);
                Toast.makeText(AdvancedTeamInfoActivity.this, R.string.update_success, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailed(int code) {
                authenDialog.undoLastSelect(); // 撤销选择
                DialogMaker.dismissProgressDialog();
                Toast.makeText(AdvancedTeamInfoActivity.this, String.format(getString(R.string.update_failed), code), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onException(Throwable exception) {
                DialogMaker.dismissProgressDialog();
            }
        });
    }

    /**
     * 设置验证模式detail显示
     *
     * @param type 验证类型
     */
    private void setAuthenticationText(VerifyTypeEnum type) {
        authenticationText.setText(TeamHelper.getVerifyString(type));
    }

    private void updateTeamNotifyText(TeamMessageNotifyTypeEnum typeEnum) {
        if (typeEnum == TeamMessageNotifyTypeEnum.All) {
            notificationConfigText.setText(getString(R.string.team_notify_all));
        } else if (typeEnum == TeamMessageNotifyTypeEnum.Manager) {
            notificationConfigText.setText(getString(R.string.team_notify_manager));
        } else if (typeEnum == TeamMessageNotifyTypeEnum.Mute) {
            notificationConfigText.setText(getString(R.string.team_notify_mute));
        }
    }

    /**
     * 更新邀请他人权限
     *
     * @param type 邀请他人类型
     */
    private void updateInviteMode(final TeamInviteModeEnum type) {
        DialogMaker.showProgressDialog(this, getString(R.string.empty));
        NIMClient.getService(TeamService.class).updateTeam(teamId, TeamFieldEnum.InviteMode, type).setCallback(new RequestCallback<Void>() {
            @Override
            public void onSuccess(Void param) {
                DialogMaker.dismissProgressDialog();
                updateInviteText(type);
                Toast.makeText(AdvancedTeamInfoActivity.this, R.string.update_success, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailed(int code) {
                inviteDialog.undoLastSelect(); // 撤销选择
                DialogMaker.dismissProgressDialog();
                Toast.makeText(AdvancedTeamInfoActivity.this, String.format(getString(R.string.update_failed), code), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onException(Throwable exception) {
                DialogMaker.dismissProgressDialog();
            }
        });
    }

    /**
     * 更新邀请他人detail显示
     *
     * @param type 邀请他人类型
     */
    private void updateInviteText(TeamInviteModeEnum type) {
        inviteText.setText(TeamHelper.getInviteModeString(type));
    }

    /**
     * 更新群资料修改权限
     *
     * @param type 群资料修改类型
     */
    private void updateInfoUpdateMode(final TeamUpdateModeEnum type) {
        DialogMaker.showProgressDialog(this, getString(R.string.empty));
        NIMClient.getService(TeamService.class).updateTeam(teamId, TeamFieldEnum.TeamUpdateMode, type).setCallback(new RequestCallback<Void>() {
            @Override
            public void onSuccess(Void param) {
                DialogMaker.dismissProgressDialog();
                updateInfoUpateText(type);
                Toast.makeText(AdvancedTeamInfoActivity.this, R.string.update_success, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailed(int code) {
                teamInfoUpdateDialog.undoLastSelect(); // 撤销选择
                DialogMaker.dismissProgressDialog();
                Toast.makeText(AdvancedTeamInfoActivity.this, String.format(getString(R.string.update_failed), code), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onException(Throwable exception) {
                DialogMaker.dismissProgressDialog();
            }
        });
    }

    /**
     * 更新群资料修改detail显示
     *
     * @param type 群资料修改类型
     */
    private void updateInfoUpateText(TeamUpdateModeEnum type) {
        infoUpdateText.setText(TeamHelper.getInfoUpdateModeString(type));
    }

    /**
     * 更新被邀请人权限
     *
     * @param type 被邀请人类型
     */
    private void updateBeInvitedMode(final TeamBeInviteModeEnum type) {
        DialogMaker.showProgressDialog(this, getString(R.string.empty));
        NIMClient.getService(TeamService.class).updateTeam(teamId, TeamFieldEnum.BeInviteMode, type).setCallback(new RequestCallback<Void>() {
            @Override
            public void onSuccess(Void param) {
                DialogMaker.dismissProgressDialog();
                updateBeInvitedText(type);
                Toast.makeText(AdvancedTeamInfoActivity.this, R.string.update_success, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailed(int code) {
                teamInviteeDialog.undoLastSelect(); // 撤销选择
                DialogMaker.dismissProgressDialog();
                Toast.makeText(AdvancedTeamInfoActivity.this, String.format(getString(R.string.update_failed), code), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onException(Throwable exception) {
                DialogMaker.dismissProgressDialog();
            }
        });
    }

    /**
     * 更新被邀请人detail显示
     *
     * @param type 被邀请人类型
     */
    private void updateBeInvitedText(TeamBeInviteModeEnum type) {
        inviteeAutenText.setText(TeamHelper.getBeInvitedModeString(type));
    }

    /**
     * 移除群成员成功后，删除列表中的群成员
     *
     * @param account 被删除成员帐号
     */
    private void removeMember(String account) {
        if (TextUtils.isEmpty(account)) {
            return;
        }

        memberAccounts.remove(account);

        for (TeamMember m : members) {
            if (m.getAccount().equals(account)) {
                members.remove(m);
                break;
            }
        }

        memberCountText.setText(String.format("共%d人", members.size()));

        for (TeamMemberItem item : dataSource) {
            if (item.getAccount() != null && item.getAccount().equals(account)) {
                dataSource.remove(item);
                break;
            }
        }
        adapter.notifyDataSetChanged();
    }

    /**
     * 是否设置了管理员刷新界面
     *
     * @param isSetAdmin
     * @param account
     */
    private void refreshAdmin(boolean isSetAdmin, String account) {
        if (isSetAdmin) {
            if (managerList.contains(account)) {
                return;
            }
            managerList.add(account);
            updateTeamMemberDataSource();
        } else {
            if (managerList.contains(account)) {
                managerList.remove(account);
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

    /**
     * 更新头像
     */
    private void updateTeamIcon(final String path) {
        if (TextUtils.isEmpty(path)) {
            return;
        }

        File file = new File(path);
        if (file == null) {
            return;
        }
        DialogMaker.showProgressDialog(this, null, null, true, new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                cancelUpload(R.string.team_update_cancel);
            }
        }).setCanceledOnTouchOutside(true);

        LogUtil.i(TAG, "start upload icon, local file path=" + file.getAbsolutePath());
        new Handler().postDelayed(outimeTask, ICON_TIME_OUT);
        uploadFuture = NIMClient.getService(NosService.class).upload(file, PickImageAction.MIME_JPEG);
        uploadFuture.setCallback(new RequestCallbackWrapper<String>() {
            @Override
            public void onResult(int code, String url, Throwable exception) {
                if (code == ResponseCode.RES_SUCCESS && !TextUtils.isEmpty(url)) {
                    LogUtil.i(TAG, "upload icon success, url =" + url);

                    NIMClient.getService(TeamService.class).updateTeam(teamId, TeamFieldEnum.ICON, url).setCallback(new RequestCallback<Void>() {
                        @Override
                        public void onSuccess(Void param) {
                            DialogMaker.dismissProgressDialog();
                            Toast.makeText(AdvancedTeamInfoActivity.this, R.string.update_success, Toast.LENGTH_SHORT).show();
                            onUpdateDone();
                        }

                        @Override
                        public void onFailed(int code) {
                            DialogMaker.dismissProgressDialog();
                            Toast.makeText(AdvancedTeamInfoActivity.this, String.format(getString(R.string.update_failed), code), Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onException(Throwable exception) {
                            DialogMaker.dismissProgressDialog();
                        }
                    }); // 更新资料
                } else {
                    Toast.makeText(AdvancedTeamInfoActivity.this, R.string.team_update_failed, Toast
                            .LENGTH_SHORT).show();
                    onUpdateDone();
                }
            }
        });
    }

    private void cancelUpload(int resId) {
        if (uploadFuture != null) {
            uploadFuture.abort();
            Toast.makeText(AdvancedTeamInfoActivity.this, resId, Toast.LENGTH_SHORT).show();
            onUpdateDone();
        }
    }

    private Runnable outimeTask = new Runnable() {
        @Override
        public void run() {
            cancelUpload(R.string.team_update_failed);
        }
    };

    private void onUpdateDone() {
        uploadFuture = null;
        DialogMaker.dismissProgressDialog();
    }
}
