// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.chatkit.ui.view.emoji;

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
import com.netease.yunxin.kit.chatkit.emoji.ChatEmojiManager;
import com.netease.yunxin.kit.chatkit.ui.R;
import java.util.ArrayList;
import java.util.List;

/** emoji and stick viewpager */
public class EmojiView {

  private final ViewPager emojiPager;
  private final LinearLayout pageNumberLayout;
  private int pageCount;

  /** emoji page count，keep with Adapter same.last on is delete */
  public static final int EMOJI_PER_PAGE = 20;

  private final Context context;
  private final IEmojiSelectedListener listener;
  private final EmoticonViewPaperAdapter pagerAdapter = new EmoticonViewPaperAdapter();

  private int categoryIndex;
  private boolean isDataInitialized = false;
  private List<Integer> categoryPageNumberList;
  private final int[] pagerIndexInfo = new int[2];

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
            setCurEmotionPage(position);
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
    initData();
    pageCount =
        (int) Math.ceil(ChatEmojiManager.INSTANCE.getDisplayCount() / (float) EMOJI_PER_PAGE);
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

  private int[] getPagerInfo(int position) {
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
    getPagerInfo(position);
    int categoryIndex = pagerIndexInfo[0];
    int pageIndexInCategory = pagerIndexInfo[1];
    int categoryPageCount = categoryPageNumberList.get(categoryIndex);

    setCurPage(pageIndexInCategory, categoryPageCount);
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
      int pos;
      if (categoryPageNumberList != null && categoryPageNumberList.size() > 0) {
        getPagerInfo(position);
        pos = pagerIndexInfo[1];
      } else {
        pos = position;
      }

      pageNumberLayout.setVisibility(View.VISIBLE);
      GridView gridView = new GridView(context);

      gridView.setOnItemClickListener(emojiListener);
      gridView.setAdapter(new EmojiAdapter(context, pos * EMOJI_PER_PAGE));
      gridView.setNumColumns(7);
      gridView.setHorizontalSpacing(5);
      gridView.setVerticalSpacing(5);

      gridView.setGravity(Gravity.CENTER);
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
