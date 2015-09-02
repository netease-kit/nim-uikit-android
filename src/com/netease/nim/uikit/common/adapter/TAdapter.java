package com.netease.nim.uikit.common.adapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.netease.nim.uikit.common.util.log.LogUtil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TAdapter<T> extends BaseAdapter implements IViewReclaimer {
	
	protected final Context context;

	private final List<T> items;
	
	private final TAdapterDelegate delegate;

	private final LayoutInflater inflater;

	private final Map<Class<?>, Integer> viewTypes;

	private Object tag;

    private boolean mutable;

    private Set<IScrollStateListener> listeners;

	public TAdapter(Context context, List<T> items, TAdapterDelegate delegate) {
        this.context = context;
		this.items = items;
		this.delegate = delegate;
		this.inflater = LayoutInflater.from(context);
		this.viewTypes = new HashMap<Class<?>, Integer>(getViewTypeCount());
        this.listeners = new HashSet<IScrollStateListener>();
	}

	@Override
	public int getCount() {
		return items == null ? 0 : items.size();
	}

	public T getItem(int position) {
		return position < getCount() ? items.get(position) : null;
	}

	public long getItemId(int position) {
		return position;
	}

	public boolean isEnabled(int position) {
		return delegate.enabled(position);
	}

	public List<T> getItems() {
		return items;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		return getView(position, convertView, parent, true);
	}

	public View getView(final int position, View convertView, ViewGroup parent, boolean needRefresh) {
		if (convertView == null) {
			convertView = viewAtPosition(position);
		}

		TViewHolder holder = (TViewHolder) convertView.getTag();
		holder.setPosition(position);
		if (needRefresh) {
            try {
                holder.refresh(getItem(position));
            } catch (RuntimeException e) {
                LogUtil.e("TAdapter", "refresh viewholder error. " + e.getMessage());
            }
		}

        if (holder instanceof IScrollStateListener) {
            listeners.add(holder);
        }
		
		return convertView;
	}

	@Override
	public int getViewTypeCount() {
		return delegate.getViewTypeCount();
	}

	@Override
	public int getItemViewType(int position) {
		if (getViewTypeCount() == 1) {
			return 0;
		}

		Class<?> clazz = delegate.viewHolderAtPosition(position);
		if (viewTypes.containsKey(clazz)) {
			return viewTypes.get(clazz);
		} else {
			int type = viewTypes.size();
			if (type < getViewTypeCount()) {
				viewTypes.put(clazz, type);
				return type;
			}
			return 0;
		}
	}

	@Override
	public void reclaimView(View view) {
		if (view == null) {
			return;
		}

		TViewHolder holder = (TViewHolder) view.getTag();
		if (holder != null) {
			holder.reclaim();
            listeners.remove(holder);
		}
	}

    public void onMutable(boolean mutable) {
        boolean becomeImmutable = this.mutable && !mutable;
        this.mutable = mutable;
        if (becomeImmutable) {
            for (IScrollStateListener listener : listeners) {
                listener.onImmutable();
            }
            listeners.clear();
        }
    }

    public boolean isMutable() {
        return mutable;
    }

	public View viewAtPosition(int position) {
		TViewHolder holder = null;
		View view = null;
		try {
			Class<?> viewHolder = delegate.viewHolderAtPosition(position);
			holder = (TViewHolder) viewHolder.newInstance();
			holder.setAdapter(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
		view = holder.getView(inflater);
		view.setTag(holder);
		holder.setContext(view.getContext());
		return view;
	}

	public Object getTag() {
		return tag;
	}

	public void setTag(Object tag) {
		this.tag = tag;
	}
}
