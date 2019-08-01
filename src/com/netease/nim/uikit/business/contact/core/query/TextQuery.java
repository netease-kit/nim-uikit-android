package com.netease.nim.uikit.business.contact.core.query;

import android.annotation.SuppressLint;
import android.text.TextUtils;

@SuppressLint("DefaultLocale")
public final class TextQuery {
    public final String text;

    public final boolean t9;

    public boolean digit;

    public boolean letter;

    public boolean pinyin;

    public Object[] extra;

    public TextQuery(String text) {
        this(text, false);
    }

    public TextQuery(String text, boolean t9) {
        this.text = !TextUtils.isEmpty(text) ? text.toLowerCase() : text;
        this.t9 = t9;

        init();
    }

    private void init() {
        if (TextUtils.isEmpty(text)) {
            return;
        }

        int digits = 0;
        int letters = 0;
        int pinyins = 0;

        for (int i = 0; i < text.length(); i++) {
            char chr = text.charAt(i);

            if ('0' <= chr && chr <= '9') {
                digits++;
            } else if ('a' <= chr && chr <= 'z') {
                letters++;
            } else if (PinYin.getIndex(chr) != -1) {
                pinyins++;
            }
        }

        digit = digits == text.length();
        letter = letters == text.length();
        pinyin = pinyins == text.length();
    }
}