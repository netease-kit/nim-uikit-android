package com.netease.yunxin.kit.contactkit.ui.contact;

import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;

import com.netease.yunxin.kit.common.ui.activities.BaseActivity;
import com.netease.yunxin.kit.contactkit.ui.R;
import com.netease.yunxin.kit.contactkit.ui.databinding.ContactActivityBinding;

public class ContactActivity extends BaseActivity {

    private ContactActivityBinding viewBinding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding = ContactActivityBinding.inflate(LayoutInflater.from(this));
        setContentView(viewBinding.getRoot());
        FragmentManager fragmentManager = getSupportFragmentManager();
        ContactFragment fragment = new ContactFragment();
        fragmentManager
                .beginTransaction()
                .add(R.id.contact_fragment_container, fragment)
                .commitAllowingStateLoss();
    }
}