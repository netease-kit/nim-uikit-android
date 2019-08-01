package com.netease.nim.uikit.common.media.imagepicker.video;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.netease.nim.uikit.R;
import com.netease.nim.uikit.common.media.imagepicker.loader.GlideImageLoader;
import com.netease.nim.uikit.common.util.sys.TimeUtil;


/**
 */

public class GLVideoPlaceholder extends FrameLayout {

    private GLVideoView videoView;

    private ImageView cover;

    private View icon;

    private View indicator;

    private View pause;

    private View full;

    private TextView time;

    private View mask;

    private float ratioWidth;

    private float ratioHeight;

    private int layout;

    private boolean fullScreenEnabled;

    private GLVideoModel model;

    private int videoWidth = -1;

    private int videoHeight = -1;

    private int placeholderWidth = -1;

    private int placeholderHeight = -1;

    // 0 for width
    // 1 for height
    private int standard;

    private static final int sEnumNop = -1;
    private static final int sEnumWidth = 0;
    private static final int sEnumHeight = 1;

    private OnClickListener onPlayClicked;

    private OnClickListener onPlayClickedListener = new OnClickListener() {
        @Override
        public void onClick(final View v) {
            if (model == null) {
                return;
            }
            triggerPlayClicked(v);
        }
    };

    public GLVideoPlaceholder(Context context) {
        this(context, null);
    }

    public GLVideoPlaceholder(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GLVideoPlaceholder(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setClickable(true);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.VideoPlaceholder, defStyleAttr, 0);
        if (ta != null) {
            ratioWidth = ta.getFloat(R.styleable.VideoPlaceholder_vp_ratio_width, 1);
            ratioHeight = ta.getFloat(R.styleable.VideoPlaceholder_vp_ratio_height, 1);
            standard = ta.getInt(R.styleable.VideoPlaceholder_vp_standard, sEnumNop);
            layout = ta.getResourceId(R.styleable.VideoPlaceholder_vp_layout, R.layout.nim_widget_video_view_default);
            ta.recycle();
        }

        LayoutInflater.from(getContext()).inflate(layout, this, true);

        videoView = findViewById(R.id.widget_video_view_texture);
        cover = findViewById(R.id.widget_video_view_cover);
        icon = findViewById(R.id.widget_video_view_icon);
        pause = findViewById(R.id.widget_video_view_pause);
        indicator = findViewById(R.id.widget_video_view_indicator);
        full = findViewById(R.id.widget_video_view_full);
        time = findViewById(R.id.widget_video_view_time);
        mask = findViewById(R.id.widget_video_view_mask);
        icon.setOnClickListener(onPlayClickedListener);
    }

    private void triggerPlayClicked(View v) {
        if (onPlayClicked != null) {
            onPlayClicked.onClick(v);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (standard != sEnumNop) {
            int width = getMeasuredWidth();
            int height = getMeasuredHeight();

            switch (standard) {
                case sEnumWidth:
                    // 以width为准
                    height = (int) (width / ratioWidth * ratioHeight);
                    break;
                case sEnumHeight:
                    // 以height为准
                    width = (int) (height / ratioHeight * ratioWidth);
                    break;
            }

            setMeasuredDimension(width, height);
            measureChildren(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                            MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
        }

        resize();
    }

    public GLVideoView getVideoView() {
        return videoView;
    }

    public void bind(GLVideoModel model, boolean force) {
        this.model = model;

        if (model.isPlayerPlay()) {
            icon.setVisibility(View.GONE);
            indicator.setVisibility(View.GONE);
            pause.setVisibility(View.VISIBLE);
            time.setVisibility(View.VISIBLE);
            setCoverVisible(false);
            setFullVisible(false);
        } else if (model.isPlayerLoading()) {
            icon.setVisibility(View.GONE);
            indicator.setVisibility(View.VISIBLE);
            pause.setVisibility(View.GONE);
            time.setVisibility(View.GONE);
            setCoverVisible(false);
            setFullVisible(false);
        } else if (model.isPlayerPaused()) {
            icon.setVisibility(View.VISIBLE);
            indicator.setVisibility(View.GONE);
            pause.setVisibility(View.GONE);
            time.setVisibility(View.VISIBLE);
            setCoverVisible(!model.isSurfaceAvailable());
            setFullVisible(true);
        } else if (model.isPlayerStopped()) {
            icon.setVisibility(View.VISIBLE);
            indicator.setVisibility(View.GONE);
            pause.setVisibility(View.GONE);
            time.setVisibility(View.GONE);
            setCoverVisible(true);
            setFullVisible(true);
        } else if (model.isPlayerComplete()) {
            icon.setVisibility(View.VISIBLE);
            indicator.setVisibility(View.GONE);
            pause.setVisibility(View.GONE);
            time.setVisibility(View.GONE);
            setCoverVisible(true);
            setFullVisible(true);
        } else if (model.isPlayerError()) {
            icon.setVisibility(View.VISIBLE);
            indicator.setVisibility(View.GONE);
            pause.setVisibility(View.GONE);
            time.setVisibility(View.GONE);
            setCoverVisible(true);
            setFullVisible(true);
        }

        time.setText(String.format("%s/%s", TimeUtil.secToTime(model.getCurrent() / 1000),
                                   TimeUtil.secToTime((int) (model.getDuration() / 1000))));

        if (force) {
            GlideImageLoader.displayVideo(cover, model.getUri());
        }

        resize();
    }

    private void resize() {
        if (model == null) {
            return;
        }

        if (model.getVideoWidth() != videoWidth || model.getViewHeight() != videoHeight ||
            placeholderWidth != getMeasuredWidth() || placeholderWidth != getMeasuredHeight()) {

            videoWidth = model.getVideoWidth();
            videoHeight = model.getViewHeight();
            placeholderWidth = getMeasuredWidth();
            placeholderHeight = getMeasuredHeight();

            if (placeholderWidth <= 0 || placeholderHeight <= 0 || videoWidth <= 0 || videoHeight <= 0) {
                return;
            }

            float ratio = placeholderWidth * 1.0f / placeholderHeight;
            float currentRatio = videoWidth * 1.0f / videoHeight;
            // float scale;
            int targetWidth, targetHeight;
            if (ratio < currentRatio) {
                // 宽固定, 高适应
                targetWidth = placeholderWidth;
                targetHeight = (int) (targetWidth * 1.0f / videoWidth * videoHeight);
                // scale = parentHeight * 1.0f / targetHeight;
            } else {
                // 高固定, 宽适应
                targetHeight = placeholderHeight;
                targetWidth = (int) (targetHeight * 1.0f / videoHeight * videoWidth);
                // scale = parentWidth * 1.0f / targetWidth;
            }

            setLayout(videoView, targetWidth, targetHeight);
            setLayout(cover, targetWidth, targetHeight);
        }
    }

    private void setLayout(View view, int width, int height) {
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.width = width;
        layoutParams.height = height;
        view.setLayoutParams(layoutParams);
    }

    private void setFullVisible(boolean visible) {
        full.setVisibility(fullScreenEnabled && visible ? View.VISIBLE : View.GONE);
    }

    private void setCoverVisible(boolean visible) {
        cover.setVisibility(visible ? View.VISIBLE : View.GONE);
        mask.setVisibility(cover.getVisibility());
    }

    public void setOnPlayClicked(OnClickListener onPlayClicked) {
        this.onPlayClicked = onPlayClicked;
    }

    public void setOnPauseClicked(OnClickListener clickListener) {
        pause.setOnClickListener(clickListener);
    }

    public void setFullScreenEnabled(boolean fullScreenEnabled) {
        this.fullScreenEnabled = fullScreenEnabled;
    }

    public void setOnFullClicked(OnClickListener clickListener) {
        full.setOnClickListener(clickListener);
    }
}
