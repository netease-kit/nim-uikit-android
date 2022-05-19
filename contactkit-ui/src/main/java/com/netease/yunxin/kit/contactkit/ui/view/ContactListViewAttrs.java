/*
 * Copyright (c) 2022 NetEase, Inc.  All rights reserved.
 * Use of this source code is governed by a MIT license that can be found in the LICENSE file.
 */

package com.netease.yunxin.kit.contactkit.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.annotation.ColorInt;

import com.netease.yunxin.kit.contactkit.ui.R;

/**
 * attrs for {@link ContactListView}
 * all attrs should be object
 */
public class ContactListViewAttrs {

    private Integer nameTextColor;

    private Boolean showIndexBar;

    private Boolean showSelector;

    public ContactListViewAttrs() {

    }

    public void parseAttrs(Context context, AttributeSet attrs) {
        if (attrs == null) {
            return;
        }
        TypedArray t;
        t = context.getTheme().obtainStyledAttributes(attrs,
                R.styleable.ContactListView, 0, 0);
        nameTextColor = t.getColor(R.styleable.ContactListView_nameTextColor,
                context.getResources().getColor(R.color.color_14131b));
        showIndexBar = t.getBoolean(R.styleable.ContactListView_showIndexBar, true);

        showSelector = t.getBoolean(R.styleable.ContactListView_showSelector, false);
        t.recycle();
    }

    public ContactListViewAttrs setAll(ContactListViewAttrs other) {
        if (other.nameTextColor != null) {
            this.nameTextColor = other.nameTextColor;
            this.showIndexBar = other.showIndexBar;
            this.showSelector = other.showSelector;
        }
        return this;
    }

    public void setNameTextColor(@ColorInt int nameTextColor) {
        this.nameTextColor = nameTextColor;
    }

    public int getNameTextColor() {
        return nameTextColor;
    }

    public Boolean getShowIndexBar() {
        return showIndexBar;
    }

    public void setShowIndexBar(Boolean showIndexBar) {
        this.showIndexBar = showIndexBar;
    }

    public Boolean getShowSelector() {
        return showSelector;
    }

    public void setShowSelector(Boolean showSelector) {
        this.showSelector = showSelector;
    }
}
