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

public abstract class BaseMultiItemFetchLoadAdapter<T, K extends BaseViewHolder> extends BaseFetchLoadAdapter<T, K> {

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

    public BaseMultiItemFetchLoadAdapter(RecyclerView recyclerView, List<T> data) {
        super(recyclerView, 0, data);
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

    protected RecyclerViewHolder getViewHolder(int viewType, String viewHolderKey) {
        if (multiTypeViewHolders.containsKey(viewType)) {
            return multiTypeViewHolders.get(viewType).get(viewHolderKey);
        }

        return null;
    }
}


