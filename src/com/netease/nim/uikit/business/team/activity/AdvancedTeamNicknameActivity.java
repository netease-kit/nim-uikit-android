package com.netease.nim.uikit.business.team.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.netease.nim.uikit.common.ToastHelper;

import com.netease.nim.uikit.R;
import com.netease.nim.uikit.api.wrapper.NimToolBarOptions;
import com.netease.nim.uikit.common.activity.ToolBarOptions;
import com.netease.nim.uikit.common.activity.UI;
import com.netease.nim.uikit.common.util.string.StringUtil;

/**
 * Created by hzxuwen on 2015/3/19.
 */
public class AdvancedTeamNicknameActivity extends UI implements TextWatcher, View.OnClickListener {

    // constant
    public static final String EXTRA_NAME = "EXTRA_NAME";
    public static final int REQ_CODE_TEAM_NAME = 20;
    private static final int MAX_LENGTH = 32;

    // view
    private EditText regularTeamNickname;

    // data
    private String nickName;

    public static void start(Context context, String name) {
        Intent intent = new Intent();
        intent.setClass(context, AdvancedTeamNicknameActivity.class);
        intent.putExtra(EXTRA_NAME, name);
        ((Activity) context).startActivityForResult(intent, REQ_CODE_TEAM_NAME);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nim_advanced_team_nickname_activity);

        ToolBarOptions options = new NimToolBarOptions();
        options.titleId = R.string.team_nickname;
        setToolBar(R.id.toolbar, options);

        nickName = getIntent().getStringExtra(EXTRA_NAME);
        if (nickName == null) {
            nickName = "";
        }

        TextView toolbarView = findView(R.id.action_bar_right_clickable_textview);
        toolbarView.setText(R.string.save);
        toolbarView.setOnClickListener(this);

        regularTeamNickname = (EditText) findViewById(R.id.regular_team_nickname);
        regularTeamNickname.setText(nickName);
        regularTeamNickname.addTextChangedListener(this);
        regularTeamNickname.setOnKeyListener(new View.OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP) {
                    // do nothing, just consume ACTION_UP event
                    return true;
                }
                return false;
            }

        });
        regularTeamNickname.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE
                        || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    complete();
                    return true;
                } else {
                    return false;
                }
            }
        });
        showKeyboardDelayed(regularTeamNickname);

        LinearLayout backgroundLayout = (LinearLayout) findViewById(R.id.background);
        backgroundLayout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                showKeyboard(false);
            }
        });
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        int editEnd = regularTeamNickname.getSelectionEnd();
        regularTeamNickname.removeTextChangedListener(this);
        while (StringUtil.counterChars(s.toString()) > MAX_LENGTH && editEnd > 0) {
            s.delete(editEnd - 1, editEnd);
            editEnd--;
        }
        regularTeamNickname.setSelection(editEnd);
        regularTeamNickname.addTextChangedListener(this);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.action_bar_right_clickable_textview) {
            showKeyboard(false);
            complete();
        } else {
        }
    }

    private void complete() {
        if (TextUtils.isEmpty(regularTeamNickname.getText().toString())) {
            ToastHelper.showToast(this, R.string.team_name_toast);
        } else {
            Intent intent = getIntent();
            intent.putExtra(EXTRA_NAME, regularTeamNickname.getText().toString());
            setResult(RESULT_OK, intent);
            finish();
        }

    }

    @Override
    public void onBackPressed() {
        showKeyboard(false);
        super.onBackPressed();
    }
}
