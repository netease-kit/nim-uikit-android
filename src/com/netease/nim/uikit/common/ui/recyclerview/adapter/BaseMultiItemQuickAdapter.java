package com.netease.nim.uikit.common.ui.recyclerview.adapter;

import android.support.annotation.LayoutRes;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.ViewGroup;

import com.netease.nim.uikit.common.ui.recyclerview.holder.BaseViewHolder;
import com.netease.nim.uikit.common.ui.recyclerview.holder.RecyclerViewHolder;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BaseMultiItemQuickAdapter<T, K extends BaseViewHolder> extends BaseQuickAdapter<T, K> {

    /**
     * viewType->layoutResId
     */
    private SparseArray<Integer> layouts;

    /**
     * viewType->view holder class
     */
    private SparseArray<Class<? extends RecyclerViewHolder>> holderClasses;

    /**
     * viewType->view holder instance
     */
    private Map<Integer, Map<String, RecyclerViewHolder>> multiTypeViewHolders;

    /**
     * get view type from data item
     *
     * @param item
     * @return
     */
    protected abstract int getViewType(T item);

    /**
     * get view holder unique key from data item
     *
     * @param item
     * @return
     */
    protected abstract String getItemKey(T item);

    public BaseMultiItemQuickAdapter(RecyclerView recyclerView, List<T> data) {
        super(recyclerView, data);
    }

    /**
     * add viewType->layoutResId, viewType->ViewHolder.class
     *
     * @param type            view type
     * @param layoutResId
     * @param viewHolderClass
     */
    protected void addItemType(int type, @LayoutRes int layoutResId, Class<? extends RecyclerViewHolder> viewHolderClass) {
        // layouts
        if (layouts == null) {
            layouts = new SparseArray<>();
        }
        layouts.put(type, layoutResId);

        // view holder class
        if (holderClasses == null) {
            holderClasses = new SparseArray<>();
        }
        holderClasses.put(type, viewHolderClass);

        // view holder
        if (multiTypeViewHolders == null) {
            multiTypeViewHolders = new HashMap<>();
        }
        multiTypeViewHolders.put(type, new HashMap<String, RecyclerViewHolder>());
    }

    @Override
    protected int getDefItemViewType(int position) {
        return getViewType(mData.get(position));
    }

    @Override
    protected K onCreateDefViewHolder(ViewGroup parent, int viewType) {
        return createBaseViewHolder(parent, getLayoutId(viewType));
    }

    @Override
    protected void convert(final K baseHolder, final T item, final int position, boolean isScrolling) {
        /**
         * 注意：baseHolder对应的是某一行的View的缓存，与item不一定是一一对应的。即同一个item，在不同的行，回调的baseHolder不一样。
         * 例如：第一行：s1-> baseHolder01 第二行 s2->baseHolder02。
         * 把s2置顶，则第一行：s2->baseHolder01，第二行：s1->baseHolder02。
         *
         * 而我们的设计：item与RecyclerViewHolder是一一对应的。因此每次convert都需要拿当前回调的baseHolder进行convert，
         * RecyclerViewHolder可以从baseHolder中取出该行View所有的子View进行数据绑定（相当于需要经历inflate->refresh的过程）。
         */
        final String key = getItemKey(item);
        final int viewType = baseHolder.getItemViewType();

        RecyclerViewHolder h = multiTypeViewHolders.get(viewType).get(key);
        if (h == null) {
            // build
            try {
                Class<? extends RecyclerViewHolder> cls = holderClasses.get(viewType);
                Constructor c = cls.getDeclaredConstructors()[0]; // 第一个显式的构造函数
                c.setAccessible(true);
                h = (RecyclerViewHolder) c.newInstance(new Object[]{this});
                multiTypeViewHolders.get(viewType).put(key, h);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // convert
        if (h != null) {
            h.convert(baseHolder, item, position, isScrolling);
        }
    }

    @Override
    protected void onRemove(final T item) {
        super.onRemove(item);

        // 移除holder
        multiTypeViewHolders.get(getViewType(item)).remove(getItemKey(item));
    }

    private int getLayoutId(int viewType) {
        return layouts.get(viewType);
    }
}


