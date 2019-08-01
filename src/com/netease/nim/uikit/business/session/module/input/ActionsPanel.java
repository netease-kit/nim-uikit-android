package com.netease.nim.uikit.business.session.module.input;

import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.netease.nim.uikit.R;
import com.netease.nim.uikit.business.session.actions.BaseAction;

import java.util.List;

/**
 * 更多操作模块
 * Created by hzxuwen on 2015/6/17.
 */
public class ActionsPanel {

    // 初始化更多布局adapter
    public static void init(View view, List<BaseAction> actions) {
        final ViewPager viewPager = (ViewPager) view.findViewById(R.id.viewPager);
        final ViewGroup indicator = (ViewGroup) view.findViewById(R.id.actions_page_indicator);

        ActionsPagerAdapter adapter = new ActionsPagerAdapter(viewPager, actions);
        viewPager.setAdapter(adapter);
        initPageListener(indicator, adapter.getCount(), viewPager);
    }

    // 初始化更多布局PageListener
    private static void initPageListener(final ViewGroup indicator, final int count, final ViewPager viewPager) {
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                setIndicator(indicator, count, position);
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        setIndicator(indicator, count, 0);
    }

    /**
     * 设置页码
     */
    private static void setIndicator(ViewGroup indicator, int total, int current) {
        if (total <= 1) {
            indicator.removeAllViews();
        } else {
            indicator.removeAllViews();
            for (int i = 0; i < total; i++) {
                ImageView imgCur = new ImageView(indicator.getContext());
                imgCur.setId(i);
                // 判断当前页码来更新
                if (i == current) {
                    imgCur.setBackgroundResource(R.drawable.nim_moon_page_selected);
                } else {
                    imgCur.setBackgroundResource(R.drawable.nim_moon_page_unselected);
                }

                indicator.addView(imgCur);
            }
        }
    }
}
