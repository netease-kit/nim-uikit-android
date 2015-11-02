package com.netease.nim.uikit.session.emoji;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.util.Log;

import com.netease.nim.uikit.NimUIKit;
import com.netease.nim.uikit.R;
import com.netease.nim.uikit.common.util.file.FileUtil;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.download.ImageDownloader;
import com.nostra13.universalimageloader.core.process.BitmapProcessor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 贴图管理类
 */
public class StickerManager {
    private final String TAG = "StickerManager";

    private static StickerManager instance;
    private static final String CATEGORY_AJMD = "ajmd";
    private static final String CATEGORY_XXY = "xxy";
    private static final String CATEGORY_LT = "lt";

    /**
     * 数据源
     */
    private List<StickerCategory> stickerCategories = new ArrayList<>();
    private Map<String, StickerCategory> stickerCategoryMap = new HashMap<>();
    private Map<String, Integer> stickerOrder = new HashMap<>(3);

    /**
     * ImageLoader
     */
    private Map<Integer, DisplayImageOptions> stickerImageOptions = new HashMap<>(2);

    public static StickerManager getInstance() {
        if (instance == null) {
            instance = new StickerManager();
        }

        return instance;
    }

    public StickerManager() {
        initStickerOrder();
        loadStickerCategory();
    }

    public void init() {
        Log.i(TAG, "Sticker Manager init...");
    }

    private void initStickerOrder() {
        // 默认贴图顺序
        stickerOrder.put(CATEGORY_AJMD, 1);
        stickerOrder.put(CATEGORY_XXY, 2);
        stickerOrder.put(CATEGORY_LT, 3);
    }

    private boolean isSystemSticker(String category) {
        return CATEGORY_XXY.equals(category) ||
                CATEGORY_AJMD.equals(category) ||
                CATEGORY_LT.equals(category);
    }

    private int getStickerOrder(String categoryName) {
        if (stickerOrder.containsKey(categoryName)) {
            return stickerOrder.get(categoryName);
        } else {
            return 100;
        }
    }

    private void loadStickerCategory() {
        AssetManager assetManager = NimUIKit.getContext().getResources().getAssets();
        try {
            String[] files = assetManager.list("sticker");
            StickerCategory category;
            for (String name : files) {
                if (!FileUtil.hasExtentsion(name)) {
                    category = new StickerCategory(name, name, true, getStickerOrder(name));
                    stickerCategories.add(category);
                    stickerCategoryMap.put(name, category);
                }
            }
            // 排序
            Collections.sort(stickerCategories, new Comparator<StickerCategory>() {
                @Override
                public int compare(StickerCategory l, StickerCategory r) {
                    return l.getOrder() - r.getOrder();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized List<StickerCategory> getCategories() {
        return stickerCategories;
    }

    public synchronized StickerCategory getCategory(String name) {
        return stickerCategoryMap.get(name);
    }

    public String getStickerBitmapUri(String categoryName, String stickerName) {
        StickerManager manager = StickerManager.getInstance();
        StickerCategory category = manager.getCategory(categoryName);
        if (category == null) {
            return null;
        }

        if (isSystemSticker(categoryName)) {
            if (!stickerName.contains(".png")) {
                stickerName += ".png";
            }

            String path = "sticker/" + category.getName() + "/" + stickerName;
            return ImageDownloader.Scheme.ASSETS.wrap(path);
        }

        return null;
    }

    /**
     * **************************** StickerImageLoader ****************************
     */

    public DisplayImageOptions getStickerImageOptions(int resize) {
        if (resize < 0) {
            resize = 0;
        }

        if (!stickerImageOptions.containsKey(resize)) {
            stickerImageOptions.put(resize, createStickerImageOption(resize));
        }

        return stickerImageOptions.get(resize);
    }

    private DisplayImageOptions createStickerImageOption(int resize) {
        int defaultIcon = R.drawable.nim_default_img_failed;
        return new DisplayImageOptions.Builder()
                .showImageOnFail(defaultIcon)
                .resetViewBeforeLoading(true)
                .cacheInMemory(true)
                .cacheOnDisk(false)
                .preProcessor(new StickerBitmapResizeProcessor(resize))
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();
    }

    private class StickerBitmapResizeProcessor implements BitmapProcessor {
        private int resize = 0;

        public StickerBitmapResizeProcessor(int resize) {
            this.resize = resize;
        }

        @Override
        public Bitmap process(Bitmap bitmap) {
            return resize(bitmap, resize);
        }
    }

    private Bitmap resize(Bitmap source, int size) {
        if (source == null) {
            return null;
        }
        int scale = 1;
        if (size < source.getWidth() / 4) {
            scale = 4;
        } else if (size < source.getWidth() * 3 / 4) {
            scale = 2;
        } else if (size < source.getWidth()) {
            scale = 1;
        }
        int width = source.getWidth() / scale;
        int height = source.getHeight() / scale;

        if (width >= source.getWidth() && height >= source.getHeight()) {
            return source;
        } else {
            return ThumbnailUtils.extractThumbnail(source, width, height, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        }
    }
}
