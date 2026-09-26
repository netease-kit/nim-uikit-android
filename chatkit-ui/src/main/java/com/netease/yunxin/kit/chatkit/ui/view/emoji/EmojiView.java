// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.view.emoji;

import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
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
import com.netease.yunxin.kit.chatkit.emoji.ChatEmojiManager;
import com.netease.yunxin.kit.chatkit.ui.R;
import java.util.ArrayList;
import java.util.List;

/** emoji and stick viewpager */
public class EmojiView {

  private final ViewPager emojiPager;
  private final LinearLayout pageNumberLayout;
  private int pageCount;
  private int emojiPageCount;

  /** emoji page count，keep with Adapter same.last on is delete */
  public static final int EMOJI_PER_PAGE = 20;

  private final Context context;
  private final IEmojiSelectedListener listener;
  private final IStickerSelectedListener stickerListener;
  private final StickerAssetManager stickerAssetManager;
  private final IEmojiCategoryChanged categoryChangedListener;
  private final EmoticonViewPaperAdapter pagerAdapter = new EmoticonViewPaperAdapter();
  private OnPageChangeListener pageChangeListener;
  private boolean released;

  private int categoryIndex;
  private boolean isDataInitialized = false;
  private List<Integer> categoryPageNumberList;
  private final int[] pagerIndexInfo = new int[2];
  private List<StickerPage> stickerPages = new ArrayList<>();
  private List<Integer> stickerPackFirstPagePositions = new ArrayList<>();
  private boolean stickerPagesInitialized;
  private boolean showingStickers;
  private int pageIndicatorSelectorResId = R.drawable.chat_emoji_page_indicator_selector;

  public EmojiView(
      Context context,
      IEmojiSelectedListener listener,
      ViewPager mCurPage,
      LinearLayout pageNumberLayout) {
    this(context, listener, null, null, null, mCurPage, pageNumberLayout);
  }

  public EmojiView(
      Context context,
      IEmojiSelectedListener listener,
      IStickerSelectedListener stickerListener,
      StickerAssetManager stickerAssetManager,
      IEmojiCategoryChanged categoryChangedListener,
      ViewPager mCurPage,
      LinearLayout pageNumberLayout) {
    this.context = context.getApplicationContext();
    this.listener = listener;
    this.stickerListener = stickerListener;
    this.stickerAssetManager = stickerAssetManager;
    this.categoryChangedListener = categoryChangedListener;
    this.pageNumberLayout = pageNumberLayout;
    this.emojiPager = mCurPage;

    pageChangeListener =
        new OnPageChangeListener() {

          @Override
          public void onPageSelected(int position) {
            boolean stickerPosition = isStickerPosition(position);
            showingStickers = stickerPosition;
            setCurEmotionPage(position);
            if (categoryChangedListener != null) {
              if (stickerPosition && !stickerPages.isEmpty()) {
                int pagePosition = Math.min(position - emojiPageCount, stickerPages.size() - 1);
                categoryChangedListener.onCategoryChanged(
                    stickerPages.get(pagePosition).packIndex + 1);
              } else if (!stickerPosition) {
                categoryChangedListener.onCategoryChanged(0);
              }
            }
          }

          @Override
          public void onPageScrolled(
              int position, float positionOffset, int positionOffsetPixels) {}

          @Override
          public void onPageScrollStateChanged(int state) {}
        };
    emojiPager.addOnPageChangeListener(pageChangeListener);
    emojiPager.setAdapter(pagerAdapter);
    emojiPager.setOffscreenPageLimit(1);
  }

  /** Releases callbacks registered by this view from the host ViewPager. */
  public void release() {
    if (released) {
      return;
    }
    released = true;
    if (pageChangeListener != null) {
      emojiPager.removeOnPageChangeListener(pageChangeListener);
      pageChangeListener = null;
    }
  }

  public void setCategoryDataReloadFlag() {
    isDataInitialized = false;
  }

  public void showStickers(int index) {
    initData();
    emojiPageCount = getCategoryPageCount();
    ensureStickerPagesInitialized();
    pageCount = emojiPageCount + stickerPages.size();
    if (index < 0 || index >= stickerPackFirstPagePositions.size()) return;
    int stickerPosition = stickerPackFirstPagePositions.get(index);

    boolean wasShowingStickers = showingStickers;
    showingStickers = true;
    if (wasShowingStickers && isDataInitialized && emojiPager.getCurrentItem() == stickerPosition) {
      return;
    }

    this.categoryIndex = index;
    pagerAdapter.notifyDataSetChanged();
    setCurStickerPage(stickerPosition - emojiPageCount);
    emojiPager.setCurrentItem(stickerPosition, false);
  }

  public void setPageIndicatorSelectorResource(int selectorResId) {
    pageIndicatorSelectorResId = selectorResId;
    for (int i = 0; i < pageNumberLayout.getChildCount(); i++) {
      pageNumberLayout.getChildAt(i).setBackgroundResource(selectorResId);
    }
  }

  public void showEmojis() {
    showingStickers = false;
    showEmojiGridView();
  }

  private int getCategoryPageCount() {
    return (int) Math.ceil(ChatEmojiManager.INSTANCE.getDisplayCount() / (float) EMOJI_PER_PAGE);
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
          imgCur.setBackgroundResource(pageIndicatorSelectorResId);
          pageNumberLayout.addView(imgCur);
        }
      }

      LinearLayout.LayoutParams indicatorParams =
          new LinearLayout.LayoutParams(
              getDimensionPixelSize(R.dimen.chat_emoji_page_indicator_width),
              getDimensionPixelSize(R.dimen.chat_emoji_page_indicator_height));
      indicatorParams.rightMargin =
          i < pageCount - 1 ? getDimensionPixelSize(R.dimen.chat_emoji_page_indicator_spacing) : 0;
      imgCur.setLayoutParams(indicatorParams);
      imgCur.setId(i);
      imgCur.setSelected(i == page);
      imgCur.setVisibility(View.VISIBLE);
    }
  }

  private int getDimensionPixelSize(int dimensionResId) {
    return context.getResources().getDimensionPixelSize(dimensionResId);
  }

  private void showEmojiGridView() {
    initData();
    emojiPageCount =
        (int) Math.ceil(ChatEmojiManager.INSTANCE.getDisplayCount() / (float) EMOJI_PER_PAGE);
    ensureStickerPagesInitialized();
    pageCount = emojiPageCount + stickerPages.size();
    pagerAdapter.notifyDataSetChanged();
    resetEmotionPager();
  }

  private void resetEmotionPager() {
    setCurEmotionPage(0);
    emojiPager.setCurrentItem(0, false);
  }

  private void setCurEmotionPage(int position) {
    if (isStickerPosition(position)) {
      setCurStickerPage(position - emojiPageCount);
      return;
    }
    setCurPage(position, emojiPageCount);
  }

  public OnItemClickListener emojiListener =
      new OnItemClickListener() {
        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
          int position = emojiPager.getCurrentItem();
          int pos = position;
          if (categoryPageNumberList != null) {
            getPagerInfo(position);
            pos = pagerIndexInfo[1];
          }

          int index = arg2 + pos * EMOJI_PER_PAGE;

          if (listener != null) {
            int count = ChatEmojiManager.INSTANCE.getDisplayCount();
            if (arg2 == EMOJI_PER_PAGE || index >= count) {
              listener.onEmojiSelected("/DEL");
            } else {
              String text = ChatEmojiManager.INSTANCE.getDisplayText((int) arg3);
              if (!TextUtils.isEmpty(text)) {
                listener.onEmojiSelected(text);
              }
            }
          }
        }
      };

  private void initData() {
    if (isDataInitialized) {
      return;
    }

    if (categoryPageNumberList == null) {
      categoryPageNumberList = new ArrayList<>();
    }

    categoryPageNumberList.clear();

    categoryPageNumberList.add(getCategoryPageCount());

    pageCount = 0;
    for (Integer count : categoryPageNumberList) {
      pageCount += count;
    }

    isDataInitialized = true;
  }

  private void initStickerPages() {
    stickerPages.clear();
    stickerPackFirstPagePositions.clear();
    if (stickerAssetManager == null) return;
    List<StickerPack> packs = stickerAssetManager.getPacks();
    for (int packIndex = 0; packIndex < packs.size(); packIndex++) {
      List<StickerItem> items = packs.get(packIndex).items;
      int pageCount = (int) Math.ceil(items.size() / 8.0);
      if (pageCount == 0) continue;
      stickerPackFirstPagePositions.add(emojiPageCount + stickerPages.size());
      for (int page = 0; page < pageCount; page++) {
        int start = page * 8;
        int end = Math.min(start + 8, items.size());
        stickerPages.add(
            new StickerPage(
                packIndex, page, pageCount, new ArrayList<>(items.subList(start, end))));
      }
    }
    pageCount = stickerPages.size();
  }

  private void ensureStickerPagesInitialized() {
    if (stickerPagesInitialized) {
      return;
    }
    initStickerPages();
    stickerPagesInitialized = true;
  }

  private int[] getPagerInfo(int position) {
    if (isStickerPosition(position)) {
      StickerPage page = stickerPages.get(position - emojiPageCount);
      pagerIndexInfo[0] = page.packIndex;
      pagerIndexInfo[1] = page.pageIndexInPack;
      return pagerIndexInfo;
    }
    if (categoryPageNumberList == null) {
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
    if (stickerPages.isEmpty()) return;
    StickerPage page = stickerPages.get(Math.max(0, Math.min(position, stickerPages.size() - 1)));
    setCurPage(page.pageIndexInPack, page.packPageCount);
  }

  private boolean isStickerPosition(int position) {
    return position >= emojiPageCount && position < emojiPageCount + stickerPages.size();
  }

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
      boolean stickerPosition = isStickerPosition(position);
      int pos;
      if (!stickerPosition && categoryPageNumberList != null && categoryPageNumberList.size() > 0) {
        getPagerInfo(position);
        pos = pagerIndexInfo[1];
      } else {
        pos = position;
      }

      pageNumberLayout.setVisibility(View.VISIBLE);
      GridView gridView =
          (GridView)
              LayoutInflater.from(container.getContext())
                  .inflate(R.layout.chat_emoji_page_layout, container, false);

      if (stickerPosition) {
        StickerPage page = stickerPages.get(position - emojiPageCount);
        gridView.setOnItemClickListener(
            (parent, view, itemPosition, id) -> {
              if (stickerListener != null && itemPosition < page.items.size()) {
                stickerListener.onStickerSelected(page.items.get(itemPosition));
              }
            });
        gridView.setAdapter(new StickerAdapter(stickerAssetManager, page.items));
      } else {
        gridView.setOnItemClickListener(emojiListener);
        gridView.setAdapter(new EmojiAdapter(context, pos * EMOJI_PER_PAGE));
      }
      gridView.setClickable(true);
      gridView.setNumColumns(stickerPosition ? 4 : 7);

      gridView.setGravity(stickerPosition ? Gravity.CENTER : Gravity.BOTTOM);
      gridView.setSelector(R.drawable.emoji_item_selector);
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
