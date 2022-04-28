package com.netease.yunxin.kit.chatkit.ui.page;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.netease.yunxin.kit.chatkit.ui.databinding.ActivityWatchImageVideoBinding;
import com.netease.yunxin.kit.chatkit.ui.page.viewmodel.WatchImageVideoViewModel;
import com.netease.yunxin.kit.common.ui.activities.BaseActivity;

/**
 * BaseActivity for Watch picture or video
 */
public abstract class WatchBaseActivity extends BaseActivity {

    WatchImageVideoViewModel viewModel;
    ActivityWatchImageVideoBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWatchImageVideoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initData(getIntent());
        initViewModel();
        initDataObserver();
        initView();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }

    private void hideSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    public void initData(Intent intent) {

    }

    public void initViewModel() {
        viewModel = new ViewModelProvider(this).get(WatchImageVideoViewModel.class);
    }

    public void initView() {
        binding.mediaClose.setOnClickListener(v -> finish());
        binding.mediaDownload.setOnClickListener(v -> saveMedia());
        binding.mediaContainer.addView(initMediaView());
    }

    public void initDataObserver() {

    }

    public abstract View initMediaView();

    public abstract void saveMedia();
}
