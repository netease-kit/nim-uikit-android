// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.qchatkit.ui.message.emoji;

import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager.widget.ViewPager.OnPageChangeListener;
import com.netease.yunxin.kit.qchatkit.ui.R;
import java.util.ArrayList;
import java.util.List;

/** emoji and stick viewpager */
public class EmojiView {

  private final ViewPager emojiPager;
  private final LinearLayout pageNumberLayout;
  private int pageCount;

  /** emoji page count，keep with Adapter same.last on is delete */
  public static final int EMOJI_PER_PAGE = 20;

  public static final int STICKER_PER_PAGE = 8;

  private final Context context;
  private final IEmojiSelectedListener listener;
  private final EmoticonViewPaperAdapter pagerAdapter = new EmoticonViewPaperAdapter();

  private int categoryIndex;
  private boolean isDataInitialized = false;
  private List<StickerCategory> categoryDataList;
  private List<Integer> categoryPageNumberList;
  private final int[] pagerIndexInfo = new int[2];
  private IEmojiCategoryChanged categoryChangedCallback;

  public EmojiView(
      Context context,
      IEmojiSelectedListener listener,
      ViewPager mCurPage,
      LinearLayout pageNumberLayout) {
    this.context = context.getApplicationContext();
    this.listener = listener;
    this.pageNumberLayout = pageNumberLayout;
    this.emojiPager = mCurPage;

    emojiPager.addOnPageChangeListener(
        new OnPageChangeListener() {

          @Override
          public void onPageSelected(int position) {
            if (categoryDataList != null) {
              setCurStickerPage(position);
              if (categoryChangedCallback != null) {
                int currentCategoryChecked = pagerIndexInfo[0];
                categoryChangedCallback.onCategoryChanged(currentCategoryChecked);
              }
            } else {
              setCurEmotionPage(position);
            }
          }

          @Override
          public void onPageScrolled(
              int position, float positionOffset, int positionOffsetPixels) {}

          @Override
          public void onPageScrollStateChanged(int state) {}
        });
    emojiPager.setAdapter(pagerAdapter);
    emojiPager.setOffscreenPageLimit(1);
  }

  public void setCategoryDataReloadFlag() {
    isDataInitialized = false;
  }

  public void showStickers(int index) {
    if (isDataInitialized
        && getPagerInfo(emojiPager.getCurrentItem()) != null
        && pagerIndexInfo[0] == index
        && pagerIndexInfo[1] == 0) {
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

    ImageView imgCur;
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
          imgCur.setBackgroundResource(R.drawable.view_pager_indicator_selector);
          pageNumberLayout.addView(imgCur);
        }
      }

      imgCur.setId(i);
      imgCur.setSelected(i == page);
      imgCur.setVisibility(View.VISIBLE);
    }
  }

  private void showEmojiGridView() {
    pageCount = (int) Math.ceil(EmojiManager.getDisplayCount() / (float) EMOJI_PER_PAGE);
    pagerAdapter.notifyDataSetChanged();
    resetEmotionPager();
  }

  private void resetEmotionPager() {
    setCurEmotionPage(0);
    emojiPager.setCurrentItem(0, false);
  }

  private void setCurEmotionPage(int position) {
    setCurPage(position, pageCount);
  }

  public OnItemClickListener emojiListener =
      new OnItemClickListener() {
        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
          int position = emojiPager.getCurrentItem();
          int pos = position;
          if (categoryDataList != null && categoryPageNumberList != null) {
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

  private void showStickerGridView() {
    initData();
    pagerAdapter.notifyDataSetChanged();

    int position = 0;
    for (int i = 0; i < categoryPageNumberList.size(); i++) {
      if (i == categoryIndex) {
        break;
      }
      position += categoryPageNumberList.get(i);
    }

    setCurStickerPage(position);
    emojiPager.setCurrentItem(position, false);
  }

  private void initData() {
    if (isDataInitialized) {
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

    categoryDataList.add(null);
    categoryPageNumberList.add(getCategoryPageCount(null));

    List<StickerCategory> categories = manager.getCategories();

    categoryDataList.addAll(categories);
    for (StickerCategory c : categories) {
      categoryPageNumberList.add(getCategoryPageCount(c));
    }

    pageCount = 0;
    for (Integer count : categoryPageNumberList) {
      pageCount += count;
    }

    isDataInitialized = true;
  }

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

  public void setCategoryChangCheckedCallback(IEmojiCategoryChanged callback) {
    this.categoryChangedCallback = callback;
  }

  private final OnItemClickListener stickerListener =
      new OnItemClickListener() {
        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
          int position = emojiPager.getCurrentItem();
          getPagerInfo(position);
          int cIndex = pagerIndexInfo[0];
          int pos = pagerIndexInfo[1];
          StickerCategory c = categoryDataList.get(cIndex);
          int index = arg2 + pos * STICKER_PER_PAGE;

          if (index >= c.getStickers().size()) {
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

  private class EmoticonViewPaperAdapter extends PagerAdapter {
    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
      return view == object;
    }

    @Override
    public int getCount() {
      return pageCount == 0 ? 1 : pageCount;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
      StickerCategory category;
      int pos;
      if (categoryDataList != null
          && categoryDataList.size() > 0
          && categoryPageNumberList != null
          && categoryPageNumberList.size() > 0) {
        getPagerInfo(position);
        int cIndex = pagerIndexInfo[0];
        category = categoryDataList.get(cIndex);
        pos = pagerIndexInfo[1];
      } else {
        category = null;
        pos = position;
      }

      pageNumberLayout.setVisibility(View.VISIBLE);
      GridView gridView = new GridView(context);
      if (category == null) {
        gridView.setOnItemClickListener(emojiListener);
        gridView.setAdapter(new EmojiAdapter(context, pos * EMOJI_PER_PAGE));
        gridView.setNumColumns(7);
        gridView.setHorizontalSpacing(5);
        gridView.setVerticalSpacing(5);
      } else {
        gridView.setPadding(10, 0, 10, 0);
        gridView.setOnItemClickListener(stickerListener);
        gridView.setAdapter(new StickerAdapter(context, category, pos * STICKER_PER_PAGE));
        gridView.setNumColumns(4);
        gridView.setHorizontalSpacing(5);
      }
      gridView.setGravity(Gravity.CENTER);
      gridView.setSelector(R.drawable.bg_emoji_item_selector);
      container.addView(gridView);
      return gridView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, @NonNull Object object) {
      View layout = (View) object;
      container.removeView(layout);
    }

    public int getItemPosition(@NonNull Object object) {
      return POSITION_NONE;
    }
  }
}
