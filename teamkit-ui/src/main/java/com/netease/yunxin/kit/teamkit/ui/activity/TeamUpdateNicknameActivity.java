/*
 * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.kit.teamkit.ui.activity;

import static com.netease.yunxin.kit.corekit.im.utils.RouterConstant.KEY_TEAM_ID;

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
import com.netease.yunxin.kit.teamkit.ui.databinding.TeamUpdateNicknameActivityBinding;
import com.netease.yunxin.kit.teamkit.ui.viewmodel.TeamSettingViewModel;

/**
 * set nick name activity
 */
public class TeamUpdateNicknameActivity extends BaseActivity {
    public static final String KEY_TEAM_MY_NICKNAME = "my_team_nickname";
    private static final String MAX_COUNT_STR = "/30";
    private TeamUpdateNicknameActivityBinding binding;
    private final TeamSettingViewModel model = new TeamSettingViewModel();
    private boolean canUpdate = false;
    private String lastTeamNickname;
    private String teamId;

    private String teamNickname;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = TeamUpdateNicknameActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        changeStatusBarColor(R.color.color_eff1f4);
        teamId = getIntent().getStringExtra(KEY_TEAM_ID);
        lastTeamNickname = getIntent().getStringExtra(KEY_TEAM_MY_NICKNAME);
        teamNickname = lastTeamNickname;
        binding.tvCancel.setOnClickListener(v -> finish());
        if (!TextUtils.isEmpty(lastTeamNickname)) {
            binding.etNickname.setText(lastTeamNickname);
            binding.ivClear.setVisibility(View.VISIBLE);
            binding.tvFlag.setText(lastTeamNickname.length() + MAX_COUNT_STR);
        }
        binding.etNickname.requestFocus();
        binding.ivClear.setOnClickListener(v -> binding.etNickname.setText(""));
        binding.etNickname.addTextChangedListener(new TextWatcher() {
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
        binding.tvSave.setOnClickListener(v -> model.updateNickname(teamId, String.valueOf(binding.etNickname.getText())));
        model.getNicknameData().observe(this, stringResultInfo -> {
            if (!stringResultInfo.getSuccess()){
                return;
            }
            if (!TextUtils.equals(lastTeamNickname, stringResultInfo.getValue())) {
                canUpdate = true;
            }
            teamNickname = String.valueOf(binding.etNickname.getText());
            finish();
        });
    }

    @Override
    public void finish() {
        if (canUpdate) {
            Intent intent = new Intent();
            intent.putExtra(KEY_TEAM_MY_NICKNAME, teamNickname);
            setResult(RESULT_OK, intent);
        }
        super.finish();
    }

    public static void launch(Context context, String teamId, String teamNickname, ActivityResultLauncher<Intent> launcher) {
        Intent intent = new Intent(context, TeamUpdateNicknameActivity.class);
        intent.putExtra(KEY_TEAM_MY_NICKNAME, teamNickname);
        intent.putExtra(KEY_TEAM_ID, teamId);
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        launcher.launch(intent);
    }
}
