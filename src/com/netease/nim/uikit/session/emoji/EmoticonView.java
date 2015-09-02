package com.netease.nim.uikit.session.emoji;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.netease.nim.uikit.R;
import com.netease.nim.uikit.common.util.log.LogUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 贴图显示viewpager
 */
public class EmoticonView {

    private ViewPager emotPager;
    private LinearLayout pageNumberLayout;
    /**
     * 总页数.
     */
    private int pageCount;

    /**
     * 每页显示的数量，Adapter保持一致.
     */
    public static final int EMOJI_PER_PAGE = 27; // 最后一个是删除键
    public static final int STICKER_PER_PAGE = 8;

    private Context context;
    private IEmoticonSelectedListener listener;
    private EmoticonViewPaperAdapter pagerAdapter = new EmoticonViewPaperAdapter();

    /**
     * 所有表情贴图支持横向滑动切换
     */
    private int categoryIndex;                           // 当套贴图的在picker中的索引
    private boolean isDataInitialized = false;             // 数据源只需要初始化一次,变更时再初始化
    private List<StickerCategory> categoryDataList;       // 表情贴图数据源
    private List<Integer> categoryPageNumberList;           // 每套表情贴图对应的页数
    private int[] pagerIndexInfo = new int[2];           // 0：category index；1：pager index in category
    private IEmoticonCategoryChanged categoryChangedCallback; // 横向滑动切换时回调picker

    public EmoticonView(Context context, IEmoticonSelectedListener mlistener,
                        ViewPager mCurPage, LinearLayout pageNumberLayout) {
        this.context = context.getApplicationContext();
        this.listener = mlistener;
        this.pageNumberLayout = pageNumberLayout;
        this.emotPager = mCurPage;

        emotPager.setOnPageChangeListener(new OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                if (categoryDataList != null) {
                    // 显示所有贴图表情
                    setCurStickerPage(position);
                    if (categoryChangedCallback != null) {
                        int currentCategoryChecked = pagerIndexInfo[0];// 当前那种类别被选中
                        categoryChangedCallback.onCategoryChanged(currentCategoryChecked);
                    }
                } else {
                    // 只显示表情
                    setCurEmotionPage(position);
                }
            }

            @Override
            public void onPageScrolled(int position, float positionOffset,
                                       int positionOffsetPixels) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        emotPager.setAdapter(pagerAdapter);
        emotPager.setOffscreenPageLimit(1);
    }

    public void setCategoryDataReloadFlag() {
        isDataInitialized = false;
    }

    public void showStickers(int index) {
        // 判断是否需要变化
        if (isDataInitialized && getPagerInfo(emotPager.getCurrentItem()) != null
                && pagerIndexInfo[0] == index && pagerIndexInfo[1] == 0) {
            return;
        }

        this.categoryIndex = index;
        showStickerGridView();
    }

    public void showEmojis() {
        showEmojiGridView();
    }

    private int getCategoryPageCount(StickerCategory category) {
        if (category == null) {
            return (int) Math.ceil(EmojiManager.getDisplayCount() / (float) EMOJI_PER_PAGE);
        } else {
            if (category.hasStickers()) {
                List<StickerItem> stickers = category.getStickers();
                return (int) Math.ceil(stickers.size() / (float) STICKER_PER_PAGE);
            } else {
                return 1;
            }
        }
    }

    private void setCurPage(int page, int pageCount) {
        int hasCount = pageNumberLayout.getChildCount();
        int forMax = Math.max(hasCount, pageCount);

        ImageView imgCur = null;
        for (int i = 0; i < forMax; i++) {
            if (pageCount <= hasCount) {
                if (i >= pageCount) {
                    pageNumberLayout.getChildAt(i).setVisibility(View.GONE);
                    continue;
                } else {
                    imgCur = (ImageView) pageNumberLayout.getChildAt(i);
                }
            } else {
                if (i < hasCount) {
                    imgCur = (ImageView) pageNumberLayout.getChildAt(i);
                } else {
                    imgCur = new ImageView(context);
                    imgCur.setBackgroundResource(R.drawable.nim_view_pager_indicator_selector);
                    pageNumberLayout.addView(imgCur);
                }
            }

            imgCur.setId(i);
            imgCur.setSelected(i == page); // 判断当前页码来更新
            imgCur.setVisibility(View.VISIBLE);
        }
    }

    /**
     * ******************************** 表情  *******************************
     */
    private void showEmojiGridView() {
        pageCount = (int) Math.ceil(EmojiManager.getDisplayCount() / (float) EMOJI_PER_PAGE);
        pagerAdapter.notifyDataSetChanged();
        resetEmotionPager();
    }

    private void resetEmotionPager() {
        setCurEmotionPage(0);
        emotPager.setCurrentItem(0, false);
    }

    private void setCurEmotionPage(int position) {
        setCurPage(position, pageCount);
    }

    public OnItemClickListener emojiListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            int position = emotPager.getCurrentItem();
            int pos = position; // 如果只有表情，那么用默认方式计算
            if (categoryDataList != null && categoryPageNumberList != null) {
                // 包含贴图
                getPagerInfo(position);
                pos = pagerIndexInfo[1];
            }

            int index = arg2 + pos * EMOJI_PER_PAGE;

            if (listener != null) {
                int count = EmojiManager.getDisplayCount();
                if (arg2 == EMOJI_PER_PAGE || index >= count) {
                    listener.onEmojiSelected("/DEL");
                } else {
                    String text = EmojiManager.getDisplayText((int) arg3);
                    if (!TextUtils.isEmpty(text)) {
                        listener.onEmojiSelected(text);
                    }
                }
            }
        }
    };

    /**
     * ******************************** 贴图  *******************************
     */

    private void showStickerGridView() {
        initData();
        pagerAdapter.notifyDataSetChanged();

        // 计算起始的pager index
        int position = 0;
        for (int i = 0; i < categoryPageNumberList.size(); i++) {
            if (i == categoryIndex) {
                break;
            }
            position += categoryPageNumberList.get(i);
        }

        setCurStickerPage(position);
        emotPager.setCurrentItem(position, false);
    }

    private void initData() {
        if (isDataInitialized) {//数据已经初始化，未变动不重新加载数据
            return;
        }

        if (categoryDataList == null) {
            categoryDataList = new ArrayList<>();
        }

        if (categoryPageNumberList == null) {
            categoryPageNumberList = new ArrayList<>();
        }

        categoryDataList.clear();
        categoryPageNumberList.clear();

        final StickerManager manager = StickerManager.getInstance();

        categoryDataList.add(null); // 表情
        categoryPageNumberList.add(getCategoryPageCount(null));

        List<StickerCategory> categories = manager.getCategories();

        categoryDataList.addAll(categories); // 贴图
        for (StickerCategory c : categories) {
            categoryPageNumberList.add(getCategoryPageCount(c));
        }

        pageCount = 0;//总页数
        for (Integer count : categoryPageNumberList) {
            pageCount += count;
        }

        isDataInitialized = true;
    }

    // 给定pager中的索引，返回categoryIndex和positionInCategory
    private int[] getPagerInfo(int position) {
        if (categoryDataList == null || categoryPageNumberList == null) {
            return pagerIndexInfo;
        }

        int cIndex = categoryIndex;
        int startIndex = 0;
        int pageNumberPerCategory = 0;
        for (int i = 0; i < categoryPageNumberList.size(); i++) {
            pageNumberPerCategory = categoryPageNumberList.get(i);
            if (position < startIndex + pageNumberPerCategory) {
                cIndex = i;
                break;
            }
            startIndex += pageNumberPerCategory;
        }

        this.pagerIndexInfo[0] = cIndex;
        this.pagerIndexInfo[1] = position - startIndex;

        return pagerIndexInfo;
    }

    private void setCurStickerPage(int position) {
        getPagerInfo(position);
        int categoryIndex = pagerIndexInfo[0];
        int pageIndexInCategory = pagerIndexInfo[1];
        int categoryPageCount = categoryPageNumberList.get(categoryIndex);

        setCurPage(pageIndexInCategory, categoryPageCount);
    }

    public void setCategoryChangCheckedCallback(IEmoticonCategoryChanged callback) {
        this.categoryChangedCallback = callback;
    }

    private OnItemClickListener stickerListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            int position = emotPager.getCurrentItem();
            getPagerInfo(position);
            int cIndex = pagerIndexInfo[0];
            int pos = pagerIndexInfo[1];
            StickerCategory c = categoryDataList.get(cIndex);
            int index = arg2 + pos * STICKER_PER_PAGE; // 在category中贴图的index

            if (index >= c.getStickers().size()) {
                LogUtil.i("sticker", "index " + index + " larger than size " + c.getStickers().size());
                return;
            }

            if (listener != null) {
                StickerManager manager = StickerManager.getInstance();
                List<StickerItem> stickers = c.getStickers();
                StickerItem sticker = stickers.get(index);
                StickerCategory real = manager.getCategory(sticker.getCategory());

                if (real == null) {
                    return;
                }

                listener.onStickerSelected(sticker.getCategory(), sticker.getName());
            }
        }
    };


    /**
     * ***************************** PagerAdapter ****************************
     */
    private class EmoticonViewPaperAdapter extends PagerAdapter {
        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public int getCount() {
            return pageCount == 0 ? 1 : pageCount;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            StickerCategory category;
            int pos;
            if (categoryDataList != null && categoryDataList.size() > 0 && categoryPageNumberList != null
                    && categoryPageNumberList.size() > 0) {
                // 显示所有贴图&表情
                getPagerInfo(position);
                int cIndex = pagerIndexInfo[0];
                category = categoryDataList.get(cIndex);
                pos = pagerIndexInfo[1];
            } else {
                // 只显示表情
                category = null;
                pos = position;
            }

            if (category == null) {
                pageNumberLayout.setVisibility(View.VISIBLE);
                GridView gridView = new GridView(context);
                gridView.setOnItemClickListener(emojiListener);
                gridView.setAdapter(new EmojiAdapter(context, pos * EMOJI_PER_PAGE));
                gridView.setNumColumns(7);
                gridView.setHorizontalSpacing(5);
                gridView.setVerticalSpacing(5);
                gridView.setGravity(Gravity.CENTER);
                gridView.setSelector(R.drawable.nim_emoji_item_selector);
                container.addView(gridView);
                return gridView;
            } else {
                pageNumberLayout.setVisibility(View.VISIBLE);
                GridView gridView = new GridView(context);
                gridView.setPadding(10, 0, 10, 0);
                gridView.setOnItemClickListener(stickerListener);
                gridView.setAdapter(new StickerAdapter(context, category, pos * STICKER_PER_PAGE));
                gridView.setNumColumns(4);
                gridView.setHorizontalSpacing(5);
                gridView.setGravity(Gravity.CENTER);
                gridView.setSelector(R.drawable.nim_emoji_item_selector);
                container.addView(gridView);
                return gridView;
            }
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            View layout = (View) object;
            container.removeView(layout);
        }

        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }
    }
}
