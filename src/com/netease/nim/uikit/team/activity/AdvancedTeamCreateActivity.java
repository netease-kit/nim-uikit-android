package com.netease.nim.uikit.team.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.netease.nim.uikit.NimUIKit;
import com.netease.nim.uikit.R;
import com.netease.nim.uikit.common.activity.TActionBarActivity;
import com.netease.nim.uikit.common.adapter.TAdapterDelegate;
import com.netease.nim.uikit.common.adapter.TViewHolder;
import com.netease.nim.uikit.common.ui.dialog.DialogMaker;
import com.netease.nim.uikit.common.ui.dialog.MenuDialog;
import com.netease.nim.uikit.common.util.sys.ActionBarUtil;
import com.netease.nim.uikit.contact_selector.activity.ContactSelectActivity;
import com.netease.nim.uikit.cache.TeamDataCache;
import com.netease.nim.uikit.team.adapter.TeamMemberAdapter;
import com.netease.nim.uikit.team.helper.TeamHelper;
import com.netease.nim.uikit.team.ui.TeamInfoGridView;
import com.netease.nim.uikit.team.viewholder.TeamMemberHolder;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.team.TeamService;
import com.netease.nimlib.sdk.team.constant.TeamFieldEnum;
import com.netease.nimlib.sdk.team.constant.TeamTypeEnum;
import com.netease.nimlib.sdk.team.constant.VerifyTypeEnum;
import com.netease.nimlib.sdk.team.model.Team;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/**
 * 创建固定群界面
 * Created by hzxuwen on 2015/3/21.
 */
public class AdvancedTeamCreateActivity extends TActionBarActivity implements TAdapterDelegate, TeamMemberAdapter.AddMemberCallback {

    private final static String TAG = "TeamCreateActivity";
    private static final int CONTACT_SELECT_REQUEST_CODE = 0x01;

    // view
    private EditText teamNameText;
    private EditText teamIntroduceText;
    private TeamInfoGridView gridView;
    private TextView authenText;
    private MenuDialog authenDialog;

    // data
    private TeamMemberAdapter adapter;
    private String creator;
    private List<String> memberAccounts;
    private List<TeamMemberAdapter.TeamMemberItem> dataSource;
    private VerifyTypeEnum verifyType = VerifyTypeEnum.Apply;

    // state: init -1, failed 0, success 1
    private Team team;

    private int teamCapacity = 50; // 群人数上限，暂定

    public static void startActivity(Context context) {
        Intent intent = new Intent();
        intent.setClass(context, AdvancedTeamCreateActivity.class);
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
     * ************************ Life cycle **************************
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nim_advanced_team_create_layout);
        setTitle(R.string.create_advanced_team);

        initCreateTeam();
        findViews();
        initActionbar();
        initAdapter();
        requestData();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CONTACT_SELECT_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            final ArrayList<String> selected = data.getStringArrayListExtra(ContactSelectActivity.RESULT_DATA);
            if (selected != null && !selected.isEmpty()) {
                addMember(selected, false);
            }
        }
    }

    @Override
    public void onAddMember() {
        // 限制群成员数量在群容量范围内
        int capacity = teamCapacity - memberAccounts.size();
        ContactSelectActivity.Option option = TeamHelper.getContactSelectOption(memberAccounts);
        option.maxSelectNum = capacity;
        option.maxSelectedTip = getString(R.string.reach_team_member_capacity, teamCapacity);
        NimUIKit.startContactSelect(AdvancedTeamCreateActivity.this, option, CONTACT_SELECT_REQUEST_CODE);
    }

    private void initCreateTeam() {
        creator = NimUIKit.getAccount();
    }

    private void findViews() {
        gridView = (TeamInfoGridView) findViewById(R.id.team_member_grid_view);
        teamNameText = (EditText) findViewById(R.id.team_name);
        teamIntroduceText = (EditText) findViewById(R.id.team_introduce);

        teamNameText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(32)});
        teamIntroduceText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(32)});

        View authenLayout = findViewById(R.id.team_authentication_layout);
        ((TextView) authenLayout.findViewById(R.id.item_title)).setText(R.string.team_authentication);
        authenText = (TextView) authenLayout.findViewById(R.id.item_detail);
        authenText.setHint(R.string.team_need_authentication);
        authenLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAuthenMenu();
            }
        });
    }

    private void initActionbar() {
        ActionBarUtil.addRightClickableTextViewOnActionBar(this, R.string.create_team, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCreateRegularTeam();
            }
        });
    }

    private void initAdapter() {
        memberAccounts = new ArrayList<>();
        dataSource = new ArrayList<>();
        adapter = new TeamMemberAdapter(this, dataSource, this, null, this);

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

    private void requestData() {
        memberAccounts.clear();
        List<String> accounts = new ArrayList<>();
        accounts.add(creator);
        addMember(accounts, true);
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
        for (String account : memberAccounts) {
            dataSource.add(new TeamMemberAdapter.TeamMemberItem(TeamMemberAdapter.TeamMemberItemTag
                    .NORMAL, null, account, creator.equals(account) ? TeamMemberHolder.OWNER : null));
        }

        // add item
        dataSource.add(new TeamMemberAdapter.TeamMemberItem(TeamMemberAdapter.TeamMemberItemTag.ADD,
                null, null, null));

        // refresh
        adapter.notifyDataSetChanged();
    }

    /**
     * ************************ protocol **************************
     */

    /**
     * 开始创建群
     */
    private void onCreateRegularTeam() {
        ArrayList<String> accounts = new ArrayList<>();
        accounts.addAll(memberAccounts);
        accounts.remove(creator);

        String teamName = teamNameText.getText().toString();
        if (TextUtils.isEmpty(teamName)) {
            teamName = "固定群";
        }

        final String teamIntroduce = teamIntroduceText.getText().toString();

        DialogMaker.showProgressDialog(this, getString(R.string.empty), true);
        // 创建群
        TeamTypeEnum type = TeamTypeEnum.Advanced;
        HashMap<TeamFieldEnum, Serializable> fields = new HashMap<>();
        fields.put(TeamFieldEnum.Name, teamName);
        fields.put(TeamFieldEnum.Introduce, teamIntroduce);
        fields.put(TeamFieldEnum.VerifyType, verifyType);
        NIMClient.getService(TeamService.class).createTeam(fields, type, "",
                accounts).setCallback(
                new RequestCallback<Team>() {
                    @Override
                    public void onSuccess(Team t) {
                        Log.i(TAG, "create team success, team id =" + t.getId() + ", now begin to update property...");
                        team = t;
                        onCreateSuccess();
                    }

                    @Override
                    public void onFailed(int code) {
                        DialogMaker.dismissProgressDialog();
                        if (code == 801) {
                            String tip = getString(R.string.over_team_member_capacity, teamCapacity);
                            Toast.makeText(AdvancedTeamCreateActivity.this, tip,
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(AdvancedTeamCreateActivity.this, R.string.create_team_failed,
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

    /**
     * 创建成功回调
     */
    private void onCreateSuccess() {
        if (team == null) {
            Log.e(TAG, "onCreateSuccess exception: team is null");
            return;
        }
        Log.i(TAG, "create and update team success");

        TeamDataCache.getInstance().addOrUpdateTeam(team);

        DialogMaker.dismissProgressDialog();
        Toast.makeText(AdvancedTeamCreateActivity.this, R.string.create_team_success,
                Toast.LENGTH_SHORT).show();
        finish();
    }

    /**
     * 显示身份验证菜单
     */
    private void showAuthenMenu() {
        if (authenDialog == null) {
            List<String> btnNames = TeamHelper.createAuthenMenuStrings();

            authenDialog = new MenuDialog(AdvancedTeamCreateActivity.this, btnNames, 1, 3, new MenuDialog
                    .MenuDialogOnButtonClickListener() {
                @Override
                public void onButtonClick(String name) {
                    authenDialog.dismiss();
                    if (name.equals(getString(R.string.cancel))) {
                        return; // 取消不处理
                    }

                    verifyType = TeamHelper.getVerifyTypeEnum(name);
                    if (verifyType != null) {
                        updateAuthenText(verifyType);
                    }
                }
            });
        }
        authenDialog.show();
    }

    /**
     * authenText显示
     *
     * @param verifyTypeEnum 验证类型枚举
     */
    private void updateAuthenText(VerifyTypeEnum verifyTypeEnum) {
        authenText.setText(TeamHelper.getVerifyString(verifyTypeEnum));
    }
}

