package com.netease.nim.uikit.common.ui.liv;

import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Map;

/**
 * 字母导航，点击字母，列表滑动到指定字母集合上。
 *
 * @author huangjun
 */
public class LivIndex {

    private final ListView lvContacts;

    private final LetterIndexView livIndex;

    private final TextView lblLetterHit;

    private final ImageView imgBackLetter;

    private final Map<String, Integer> mapABC; // 字母:所在的行的index

    public LivIndex(ListView contactsListView, LetterIndexView letterIndexView, TextView letterHit, ImageView
            imgBackLetter,
                    Map<String, Integer> abcMap) {
        this.lvContacts = contactsListView;
        this.livIndex = letterIndexView;
        this.lblLetterHit = letterHit;
        this.imgBackLetter = imgBackLetter;
        this.mapABC = abcMap;
        this.livIndex.setOnTouchingLetterChangedListener(new LetterChangedListener());
    }

    /**
     * 显示
     */
    public void show() {
        this.livIndex.setVisibility(View.VISIBLE);
    }

    /**
     * 隐藏
     */
    public void hide() {
        this.livIndex.setVisibility(View.GONE);
    }

    private class LetterChangedListener implements LetterIndexView.OnTouchingLetterChangedListener {

        @Override
        public void onHit(String letter) {
            lblLetterHit.setVisibility(View.VISIBLE);
            imgBackLetter.setVisibility(View.VISIBLE);
            lblLetterHit.setText(letter);

            int index = -1;
            if ("↑".equals(letter)) {
                index = 0;
            } else if (mapABC.containsKey(letter)) {
                index = mapABC.get(letter);
            }
            if (index < 0) {
                return;
            }
            index += lvContacts.getHeaderViewsCount();
            if (index >= 0 && index < lvContacts.getCount()) {
                lvContacts.setSelectionFromTop(index, 0);
            }
        }

        @Override
        public void onCancel() {
            lblLetterHit.setVisibility(View.INVISIBLE);
            imgBackLetter.setVisibility(View.INVISIBLE);
        }
    }

}
