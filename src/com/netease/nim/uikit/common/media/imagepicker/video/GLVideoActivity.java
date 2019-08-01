package com.netease.nim.uikit.common.media.imagepicker.video;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.netease.nim.uikit.R;

/**

 */

public class GLVideoActivity extends AppCompatActivity implements GLVideoModel.ModelObserver {
    protected static final String KEY_Duration = "duration";

    public static void start(Context context, Uri uri, long duration) {
        Intent intent = new Intent(context, GLVideoActivity.class);
        intent.setData(uri);
        intent.putExtra(KEY_Duration, duration);
        context.startActivity(intent);
    }

    public static void start(Context context, String url, long duration) {
        start(context, Uri.parse(url), duration);
    }

    private GLMediaPlayerManager mediaPlayerManager = new GLMediaPlayerManager();

    private GLVideoPlaceholder placeholder;

    private GLVideoModel videoModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nim_activity_gl_video);
        initView();
        bindData();
    }

    private void initView() {
        placeholder = findViewById(R.id.placeholder);

        findViewById(R.id.btn_back).setVisibility(View.VISIBLE);
        findViewById(R.id.retake).setVisibility(View.GONE);
        findViewById(R.id.confirm).setVisibility(View.GONE);

        findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void bindData() {
        Uri uri = getIntent().getData();
        long duration = getIntent().getLongExtra(KEY_Duration, 0);
        videoModel = new GLVideoModel(uri, duration);
        videoModel.attachVideoView(placeholder.getVideoView());
        videoModel.removeObservers();
        videoModel.addObserver(this);
        videoModel.fireAttachSurface();
        placeholder.setFullScreenEnabled(false);
        placeholder.bind(videoModel, true);

        View.OnClickListener playListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayerManager.attach(v.getContext(), videoModel);
                videoModel.userPlay();
            }
        };
        placeholder.setOnPlayClicked(playListener);
        placeholder.setOnPauseClicked(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                videoModel.userPause();
            }
        });
        playListener.onClick(placeholder);
    }


    @Override
    public void onModelChanged(GLVideoModel model) {
        placeholder.bind(model, false);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mediaPlayerManager.detach();
    }
}
