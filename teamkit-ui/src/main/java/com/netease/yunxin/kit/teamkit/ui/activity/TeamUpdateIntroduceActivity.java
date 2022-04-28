/*
 * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.kit.teamkit.ui.activity;

import static com.netease.yunxin.kit.corekit.im.utils.RouterConstant.KEY_TEAM_ID;
import static com.netease.yunxin.kit.teamkit.ui.activity.TeamInfoActivity.KEY_TEAM_UPDATE_INFO_PRIVILEGE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.Nullable;

import com.netease.yunxin.kit.common.ui.activities.BaseActivity;
import com.netease.yunxin.kit.teamkit.ui.R;
import com.netease.yunxin.kit.teamkit.ui.databinding.TeamUpdateIntroduceActivityBinding;
import com.netease.yunxin.kit.teamkit.ui.viewmodel.TeamSettingViewModel;

/**
 * set team introduce activity
 */
public class TeamUpdateIntroduceActivity extends BaseActivity {
    public static final String KEY_TEAM_INTRODUCE = "team/teamIntroduce";
    private static final String MAX_COUNT_STR = "/100";
    private final TeamSettingViewModel model = new TeamSettingViewModel();
    private boolean canUpdate = false;
    private String lastTeamIntroduce;
    private String teamId;

    private String teamIntroduce;
    private boolean hasPrivilege;

    private TeamUpdateIntroduceActivityBinding binding;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = TeamUpdateIntroduceActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        changeStatusBarColor(R.color.color_eff1f4);
        hasPrivilege = getIntent().getBooleanExtra(KEY_TEAM_UPDATE_INFO_PRIVILEGE, false);
        teamId = getIntent().getStringExtra(KEY_TEAM_ID);
        lastTeamIntroduce = getIntent().getStringExtra(KEY_TEAM_INTRODUCE);
        teamIntroduce = lastTeamIntroduce;
        binding.tvCancel.setOnClickListener(v -> finish());
        if (!TextUtils.isEmpty(lastTeamIntroduce)) {
            binding.etIntroduce.setText(lastTeamIntroduce);
            binding.ivClear.setVisibility(View.VISIBLE);
            binding.tvFlag.setText(lastTeamIntroduce.length() + MAX_COUNT_STR);
        }
        if (!hasPrivilege) {
            binding.tvSave.setVisibility(View.GONE);
            binding.etIntroduce.setEnabled(false);
            binding.ivClear.setVisibility(View.GONE);
        }
        binding.etIntroduce.requestFocus();
        binding.ivClear.setOnClickListener(v -> binding.etIntroduce.setText(""));
        binding.etIntroduce.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (TextUtils.isEmpty(String.valueOf(s))) {
                    binding.ivClear.setVisibility(View.GONE);
                } else {
                    binding.ivClear.setVisibility(View.VISIBLE);
                }
                binding.tvFlag.setText(String.valueOf(s).length() + MAX_COUNT_STR);
            }
        });
        binding.tvSave.setOnClickListener(v -> model.updateIntroduce(teamId,String.valueOf(binding.etIntroduce.getText())));
        model.getIntroduceData().observe(this, stringResultInfo -> {
            if (!stringResultInfo.getSuccess()){
                return;
            }
            if (!TextUtils.equals(lastTeamIntroduce, String.valueOf(binding.etIntroduce.getText()))) {
                canUpdate = true;
            }
            teamIntroduce = String.valueOf(binding.etIntroduce.getText());
            finish();
        });
    }

    @Override
    public void finish() {
        if (hasPrivilege && canUpdate) {
            Intent intent = new Intent();
            intent.putExtra(KEY_TEAM_INTRODUCE, teamIntroduce);
            setResult(RESULT_OK, intent);
        }
        super.finish();
    }

    public static void launch(Context context, boolean hasPrivilege, String teamId, String introduce, ActivityResultLauncher<Intent> launcher) {
        Intent intent = new Intent(context, TeamUpdateIntroduceActivity.class);
        intent.putExtra(KEY_TEAM_UPDATE_INFO_PRIVILEGE, hasPrivilege);
        intent.putExtra(KEY_TEAM_INTRODUCE, introduce);
        intent.putExtra(KEY_TEAM_ID, teamId);
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        launcher.launch(intent);
    }
}
