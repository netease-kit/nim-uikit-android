package com.netease.nim.uikit.common.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import java.util.ArrayList;
import java.util.List;

/**
 */
public class BaseAdapter<T> extends RecyclerView.Adapter<BaseViewHolder> {

    /**
     * data source
     */
    public List<T> dataList;

    /**
     * onClick onLongClick callback
     */
    public OnItemClickListener listener;

    /**
     * constructor view holder delegate
     */
    private BaseDelegate delegate;

    /**
     * constructor
     *
     * @param dataList
     */
    public BaseAdapter(List<T> dataList) {
        this(dataList, null);
    }

    /**
     * constructor
     *
     * @param dataList
     * @param listener
     */
    public BaseAdapter(List<T> dataList, OnItemClickListener listener) {
        checkData(dataList);
        this.listener = listener;
    }

    /**
     * just is empty
     *
     * @param delegate
     */
    public void setDelegate(BaseDelegate delegate) {
        this.delegate = delegate;
    }

    /**
     * just is empty
     *
     * @param dataList
     */
    private void checkData(List<T> dataList) {
        if (dataList == null) {
            dataList = new ArrayList<>(0);
        }
        this.dataList = dataList;
    }

    /**
     * set onclick & onLongClick callback
     *
     * @param listener
     */
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    /**
     * create view holder
     *
     * @param parent
     * @param viewType
     * @return
     */
    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        BaseViewHolder viewHolder = delegate.createViewHolder(this, parent, viewType);
        viewHolder.findViews();
        return viewHolder;
    }

    /**
     * bind view holder
     *
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(final BaseViewHolder holder, final int position) {
        holder.bindViewHolder(dataList.get(position));
        listenClick(holder);
    }

    protected void listenClick(BaseViewHolder holder) {
        if (listener != null && holder.isClickable()) {
            holder.itemView.setOnClickListener(mClickListenerMediator);
            holder.itemView.setOnLongClickListener(mLongClickListenerMediator);
        }
    }

    public void updateDataAndNotify(List<? extends T> list) {
        dataList.clear();
        if (list != null && list.size() > 0) {
            dataList.addAll(list);
        }
        delegate.onDataSetChanged();
        this.notifyDataSetChanged();
    }

    public void appendDataAndNotify(T t) {
        if (t == null) {
            return;
        }
        dataList.add(t);
        delegate.onDataSetChanged();
        this.notifyDataSetChanged();
    }

    public void insertHeadDataAndNotify(T t) {
        if (t == null) {
            return;
        }
        dataList.add(0, t);
        delegate.onDataSetChanged();
        this.notifyDataSetChanged();
    }

    public void appendDataAndNotify(List<? extends T> list) {
        dataList.addAll(list);
        delegate.onDataSetChanged();
        this.notifyDataSetChanged();
    }

    public boolean isEmpty() {
        return dataList.size() == 0;
    }

    /**
     * get item count
     *
     * @return
     */
    @Override
    public int getItemCount() {
        return dataList.size();
    }

    /**
     * get item view type
     *
     * @param position
     * @return
     */
    @Override
    public int getItemViewType(int position) {
        return delegate.getItemViewType(dataList.get(position), position);
    }

    private View.OnClickListener mClickListenerMediator = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (listener != null) {
                int pos = getViewHolderAdapterPosition(v);
                if (pos < 0) {
                    return;
                }
                listener.onClick(v, pos, getData(pos));
            }
        }
    };

    private View.OnLongClickListener mLongClickListenerMediator = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            if (listener != null) {
                int pos = getViewHolderAdapterPosition(v);
                if (pos < 0) {
                    return false;
                }
                return listener.onLongClick(v, pos, getData(pos));
            }
            return false;
        }
    };

    protected T getData(int pos) {
        return pos >= 0 ? dataList.get(pos) : null;
    }

    static int getViewHolderAdapterPosition(View v) {
        if (v != null) {
            ViewParent parent = v.getParent();
            if (parent instanceof RecyclerView) {
                return ((RecyclerView) parent).getChildAdapterPosition(v);
            }
        }
        return -1;
    }

    public void removeItem(int position) {
        dataList.remove(position);
        // notify the item removed by position
        // to perform recycler view delete animations
        // NOTE: don't call notifyDataSetChanged()
        delegate.onDataSetChanged();
        notifyItemRemoved(position);
    }

    @Override
    public void onViewAttachedToWindow(@NonNull BaseViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        if (holder instanceof DetachAwareViewHolder) {
            ((DetachAwareViewHolder) holder).onViewAttachedToWindow();
        }
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull BaseViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        if (holder instanceof DetachAwareViewHolder) {
            ((DetachAwareViewHolder) holder).onViewDetachedFromWindow();
        }
    }

    @Override
    public void onViewRecycled(@NonNull BaseViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder instanceof RecyclerView.RecyclerListener) {
            ((RecyclerView.RecyclerListener) holder).onViewRecycled(holder);
        }
    }

    public T getLastData() {
        if (dataList != null && dataList.size() > 0) {
            return dataList.get(dataList.size() - 1);
        }

        return null;
    }
}
