package com.netease.nim.uikit.business.session.emoji;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.netease.nim.uikit.R;
import com.netease.nim.uikit.common.ui.imageview.CheckedImageButton;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.netease.nim.uikit.common.util.media.BitmapDecoder;
import com.netease.nim.uikit.common.util.sys.ScreenUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * 贴图表情选择控件
 */
public class EmoticonPickerView extends LinearLayout implements IEmoticonCategoryChanged {

    private Context context;

    private IEmoticonSelectedListener listener;

    private boolean loaded = false;

    private boolean withSticker;

    private EmoticonView gifView;

    private ViewPager currentEmojiPage;

    private LinearLayout pageNumberLayout;//页面布局

    private HorizontalScrollView scrollView;

    private LinearLayout tabView;

    private int categoryIndex;

    private Handler uiHandler;

    public EmoticonPickerView(Context context) {
        super(context);

        init(context);
    }

    public EmoticonPickerView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(context);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public EmoticonPickerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        init(context);
    }

    private void init(Context context) {
        this.context = context;
        this.uiHandler = new Handler(context.getMainLooper());

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.nim_emoji_layout, this);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        setupEmojView();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    public void show(IEmoticonSelectedListener listener) {
        setListener(listener);

        if (loaded)
            return;
        loadStickers();
        loaded = true;

        show();
    }

    public void setListener(IEmoticonSelectedListener listener) {
        if (listener != null) {
            this.listener = listener;
        } else {
            LogUtil.i("sticker", "listener is null");
        }
    }

    protected void setupEmojView() {
        currentEmojiPage = (ViewPager) findViewById(R.id.scrPlugin);
        pageNumberLayout = (LinearLayout) findViewById(R.id.layout_scr_bottom);
        tabView = (LinearLayout) findViewById(R.id.emoj_tab_view);
        scrollView = (HorizontalScrollView) findViewById(R.id.emoj_tab_view_container);

        findViewById(R.id.top_divider_line).setVisibility(View.VISIBLE);
    }

    // 添加各个tab按钮
    OnClickListener tabCheckListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            onEmoticonBtnChecked(v.getId());
        }
    };

    private void loadStickers() {
        if (!withSticker) {
            scrollView.setVisibility(View.GONE);
            return;
        }

        final StickerManager manager = StickerManager.getInstance();

        tabView.removeAllViews();

        int index = 0;

        // emoji表情
        CheckedImageButton btn = addEmoticonTabBtn(index++, tabCheckListener);
        btn.setNormalImageId(R.drawable.nim_emoji_icon_inactive);
        btn.setCheckedImageId(R.drawable.nim_emoji_icon);

        // 贴图
        List<StickerCategory> categories = manager.getCategories();
        for (StickerCategory category : categories) {
            btn = addEmoticonTabBtn(index++, tabCheckListener);
            setCheckedButtomImage(btn, category);
        }
    }


    private CheckedImageButton addEmoticonTabBtn(int index, OnClickListener listener) {
        CheckedImageButton emotBtn = new CheckedImageButton(context);
        emotBtn.setNormalBkResId(R.drawable.nim_sticker_button_background_normal_layer_list);
        emotBtn.setCheckedBkResId(R.drawable.nim_sticker_button_background_pressed_layer_list);
        emotBtn.setId(index);
        emotBtn.setOnClickListener(listener);
        emotBtn.setScaleType(ImageView.ScaleType.FIT_CENTER);
        emotBtn.setPaddingValue(ScreenUtil.dip2px(7));

        final int emojiBtnWidth = ScreenUtil.dip2px(50);
        final int emojiBtnHeight = ScreenUtil.dip2px(44);

        tabView.addView(emotBtn);

        ViewGroup.LayoutParams emojBtnLayoutParams = emotBtn.getLayoutParams();
        emojBtnLayoutParams.width = emojiBtnWidth;
        emojBtnLayoutParams.height = emojiBtnHeight;
        emotBtn.setLayoutParams(emojBtnLayoutParams);

        return emotBtn;
    }

    private void setCheckedButtomImage(CheckedImageButton btn, StickerCategory category) {
        try {
            InputStream is = category.getCoverNormalInputStream(context);
            if (is != null) {
                Bitmap bmp = BitmapDecoder.decode(is);
                btn.setNormalImage(bmp);
                is.close();
            }
            is = category.getCoverPressedInputStream(context);
            if (is != null) {
                Bitmap bmp = BitmapDecoder.decode(is);
                btn.setCheckedImage(bmp);
                is.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void onEmoticonBtnChecked(int index) {
        updateTabButton(index);
        showEmotPager(index);
    }

    private void updateTabButton(int index) {
        for (int i = 0; i < tabView.getChildCount(); ++i) {
            View child = tabView.getChildAt(i);
            if (child instanceof FrameLayout) {
                child = ((FrameLayout) child).getChildAt(0);
            }

            if (child != null && child instanceof CheckedImageButton) {
                CheckedImageButton tabButton = (CheckedImageButton) child;
                if (tabButton.isChecked() && i != index) {
                    tabButton.setChecked(false);
                } else if (!tabButton.isChecked() && i == index) {
                    tabButton.setChecked(true);
                }
            }
        }
    }

    private void showEmotPager(int index) {
        if (gifView == null) {
            gifView = new EmoticonView(context, listener, currentEmojiPage, pageNumberLayout);
            gifView.setCategoryChangCheckedCallback(this);
        }

        gifView.showStickers(index);
    }

    private void showEmojiView() {
        if (gifView == null) {
            gifView = new EmoticonView(context, listener, currentEmojiPage, pageNumberLayout);
        }
        gifView.showEmojis();
    }

    private void show() {
        if (listener == null) {
            LogUtil.i("sticker", "show picker view when listener is null");
        }
        if (!withSticker) {
            showEmojiView();
        } else {
            onEmoticonBtnChecked(0);
            setSelectedVisible(0);
        }
    }


    private void setSelectedVisible(final int index) {
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (scrollView.getChildAt(0).getWidth() == 0) {
                    uiHandler.postDelayed(this, 100);
                }
                int x = -1;
                View child = tabView.getChildAt(index);
                if (child != null) {
                    if (child.getRight() > scrollView.getWidth()) {
                        x = child.getRight() - scrollView.getWidth();
                    }
                }
                if (x != -1) {
                    scrollView.smoothScrollTo(x, 0);
                }
            }
        };
        uiHandler.postDelayed(runnable, 100);
    }


    @Override
    public void onCategoryChanged(int index) {
        if (categoryIndex == index) {
            return;
        }

        categoryIndex = index;
        updateTabButton(index);
    }

    public void setWithSticker(boolean withSticker) {
        this.withSticker = withSticker;
    }
}
