/*
 * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.app.im.main;

import android.Manifest;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.ColorRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.netease.yunxin.app.im.R;
import com.netease.yunxin.app.im.databinding.ActivityMainBinding;
import com.netease.yunxin.app.im.main.mine.MineFragment;
import com.netease.yunxin.kit.contactkit.ui.contact.ContactFragment;
import com.netease.yunxin.kit.conversationkit.ui.page.ConversationFragment;
import com.netease.yunxin.kit.qchatkit.ui.common.NetworkUtils;
import com.netease.yunxin.kit.qchatkit.ui.server.QChatServerFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * IM  Main Page
 * include four tab , message/contact/live/profile
 */
public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding activityMainBinding;
    private static final int START_INDEX = 0;
    private View mCurrentTab;

    ContactFragment.Builder contactBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityMainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(activityMainBinding.getRoot());
        grantPermission();
        initView();
        // init network state listen
        NetworkUtils.init(getApplicationContext());
    }

    private void grantPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.CAMERA,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.READ_PHONE_STATE}, 0x10);
    }

    private void initView() {
        List<Fragment> fragments = new ArrayList<>();
        ConversationFragment conversationFragment = new ConversationFragment();
        initConversationFragment(conversationFragment);
        fragments.add(conversationFragment);
        QChatServerFragment qChatServerFragment = new QChatServerFragment();
        fragments.add(qChatServerFragment);
        //Contact
        contactBuilder = new ContactFragment.Builder();
        ContactFragment contactFragment = contactBuilder.build();
        initContactFragment(contactFragment);
        fragments.add(contactFragment);

        fragments.add(new MineFragment());

        FragmentAdapter fragmentAdapter = new FragmentAdapter(this);
        fragmentAdapter.setFragmentList(fragments);
        activityMainBinding.viewPager.setUserInputEnabled(false);
        activityMainBinding.viewPager.setAdapter(fragmentAdapter);
        activityMainBinding.viewPager.setCurrentItem(START_INDEX, false);
        activityMainBinding.viewPager.setOffscreenPageLimit(4);
        mCurrentTab = activityMainBinding.conversationBtnGroup;
        changeStatusBarColor(R.color.color_white);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public void tabClick(View view) {

        if (mCurrentTab != null && mCurrentTab == view) {
            return;
        }
        resetTabStyle();
        mCurrentTab = view;
        if (mCurrentTab == activityMainBinding.contactBtnGroup) {
            activityMainBinding.viewPager.setCurrentItem(2, false);
            activityMainBinding.contact.setTextColor(getResources().getColor(R.color.tab_checked_color));
            activityMainBinding.contact.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.mipmap.ic_contact_tab_checked), null, null);
            changeStatusBarColor(R.color.color_white);
        }else if (mCurrentTab == activityMainBinding.myselfBtnGroup) {
            activityMainBinding.viewPager.setCurrentItem(3, false);
            activityMainBinding.mine.setTextColor(getResources().getColor(R.color.tab_checked_color));
            activityMainBinding.mine.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.mipmap.ic_mine_tab_checked), null, null);
            changeStatusBarColor(R.color.color_white);
        } else if (mCurrentTab == activityMainBinding.qchatBtnGroup) {
            activityMainBinding.viewPager.setCurrentItem(1, false);
            activityMainBinding.qchat.setTextColor(getResources().getColor(R.color.tab_checked_color));
            activityMainBinding.qchat.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.ic_qchat_checked), null, null);
            changeStatusBarColor(R.color.color_e9eff5);
        }else if (mCurrentTab == activityMainBinding.conversationBtnGroup){
            activityMainBinding.viewPager.setCurrentItem(0, false);
            activityMainBinding.conversation.setTextColor(getResources().getColor(R.color.tab_checked_color));
            activityMainBinding.conversation.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.mipmap.ic_conversation_tab_checked), null, null);
            changeStatusBarColor(R.color.color_white);
        }
    }

    private void initConversationFragment(ConversationFragment conversationFragment){
        if (conversationFragment != null){
            conversationFragment.setConversationCallback(count -> {
                if(count > 0){
                    activityMainBinding.conversationDot.setVisibility(View.VISIBLE);
                }else {
                    activityMainBinding.conversationDot.setVisibility(View.GONE);
                }
            });
        }
    }

    private void initContactFragment(ContactFragment contactFragment){
        if (contactFragment != null){
            contactFragment.setContactCallback( count -> {
                if(count > 0){
                    activityMainBinding.contactDot.setVisibility(View.VISIBLE);
                }else {
                    activityMainBinding.contactDot.setVisibility(View.GONE);
                }
            });
        }
    }

    private void changeStatusBarColor(@ColorRes int colorResId){
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, colorResId));
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void resetTabStyle(){

        activityMainBinding.conversation.setTextColor(getResources().getColor(R.color.tab_unchecked_color));
        activityMainBinding.conversation.setCompoundDrawablesWithIntrinsicBounds(null,getResources().getDrawable(R.mipmap.ic_conversation_tab_unchecked),null,null);

        activityMainBinding.contact.setTextColor(getResources().getColor(R.color.tab_unchecked_color));
        activityMainBinding.contact.setCompoundDrawablesWithIntrinsicBounds(null,getResources().getDrawable(R.mipmap.ic_contact_tab_unchecked),null,null);

        activityMainBinding.mine.setTextColor(getResources().getColor(R.color.tab_unchecked_color));
        activityMainBinding.mine.setCompoundDrawablesWithIntrinsicBounds(null,getResources().getDrawable(R.mipmap.ic_mine_tab_unchecked),null,null);

        activityMainBinding.qchat.setTextColor(getResources().getColor(R.color.tab_unchecked_color));
        activityMainBinding.qchat.setCompoundDrawablesWithIntrinsicBounds(null,getResources().getDrawable(R.drawable.ic_qchat_unchecked),null,null);

    }

}