package com.netease.nim.uikit.business.ait.selector;

import android.content.Context;
import android.support.v7.widget.RecyclerView;

import com.netease.nim.uikit.common.ui.recyclerview.decoration.DividerItemDecoration;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by hzchenkang on 2017/6/22.
 */

public class AitContactDecoration extends DividerItemDecoration {

    // 不需要分割线
    private Set<Integer> ignoreDecorations;

    public AitContactDecoration(Context context, int orientation, List<Integer> ignoreDecorations) {
        super(context, orientation);
        if (ignoreDecorations != null) {
            this.ignoreDecorations = new HashSet<>(ignoreDecorations);
        }
    }

    @Override
    protected boolean needDrawDecoration(RecyclerView parent, int position) {
        if (ignoreDecorations != null) {
            int viewType = parent.getAdapter().getItemViewType(position);
            if (ignoreDecorations.contains(viewType)) {
                return false;
            }
        }
        return true;
    }
}
