package com.netease.nim.uikit.common.adapter;

import android.view.View;
import android.view.ViewGroup;

public class HybridAdapter extends BaseAdapter {
    private final Hybrid[] hybrids;

    public static abstract class Hybrid<T> {
        protected static final int VIEW_TYPE_INVALID = -1;

        private OnItemClickListener listener;

        private boolean disabled;

        public Hybrid() {
            this(null);
        }

        public Hybrid(OnItemClickListener listener) {
            this.listener = listener;
        }

        public abstract DataFreeViewHolder<T> onCreateViewHolder(ViewGroup parent, int viewType);

        public abstract void onBindViewHolder(DataFreeViewHolder<T> holder, int position);

        public abstract int getItemCount();

        public abstract int getItemViewType(int position);

        public abstract T getData(int position);

        protected void bindViewHolder(DataFreeViewHolder<T> holder, T data) {
            holder.bindViewHolder(data);
        }

        public boolean isEmpty() {
            return getItemCount() == 0;
        }

        public final void setOnItemClickListener(OnItemClickListener onItemClickListener) {
            this.listener = onItemClickListener;
        }

        public final void enable(boolean enable) {
            this.disabled = !enable;
        }

        private void handleClick(boolean longClick, View v, int pos, Object data) {
            if (listener == null) {
                return;
            }
            if (longClick) {
                listener.onLongClick(v, pos, data);
            } else {
                listener.onClick(v, pos, data);
            }
        }
    }

    protected static Hybrid[] make(Hybrid main, Hybrid[] before, Hybrid[] after) {
        int count = 1 + (before != null ? before.length : 0) + (after != null ? after.length : 0);
        Hybrid[] hybrids = new Hybrid[count];
        int index = 0;
        if (before != null) {
            System.arraycopy(before, 0, hybrids, 0, before.length);
            index += before.length;
        }
        hybrids[index++] = main;
        if (after != null) {
            System.arraycopy(after, 0, hybrids, index, after.length);
        }
        return hybrids;
    }

    public HybridAdapter(final Hybrid... hybrids) {
        super(null);

        super.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onClick(View v, int pos, Object data) {
                handleClick(false, v, pos, data);
            }

            @Override
            public boolean onLongClick(View v, int pos, Object data) {
                handleClick(true, v, pos, data);
                return false;
            }
        });

        this.hybrids = hybrids;

        setDelegate(new BaseDelegate() {
            @Override
            public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                int index = viewType >> 16;
                viewType = viewType & 0xffff;
                if (index < 0 || index >= hybrids.length) {
                    return null;
                }
                Hybrid hybrid = hybrids[index];
                return hybrid.onCreateViewHolder(parent, viewType);
            }

            @Override
            public int getItemViewType(Object data, int pos) {
                return 0;
            }
        });
    }

    @Override
    public final void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        // disable
    }

    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        for (Hybrid hybrid : hybrids) {
            if (hybrid.disabled) {
                continue;
            }
            int count = hybrid.getItemCount();
            if (position < count) {
                hybrid.onBindViewHolder((DataFreeViewHolder) holder, position);
                listenClick(holder);
                return;
            }
            position -= count;
        }
        super.onBindViewHolder(holder, position);
    }

    @Override
    public final int getItemCount() {
        int count = 0;
        for (Hybrid hybrid : hybrids) {
            if (hybrid.disabled) {
                continue;
            }
            count += hybrid.getItemCount();
        }
        return count;
    }

    @Override
    public final int getItemViewType(int position) {
        for (int i = 0; i < hybrids.length; i++) {
            Hybrid hybrid = hybrids[i];
            if (hybrid.disabled) {
                continue;
            }
            int count = hybrid.getItemCount();
            if (position < count) {
                return i << 16 | hybrid.getItemViewType(position) & 0xffff;
            }
            position -= count;
        }
        return Hybrid.VIEW_TYPE_INVALID;
    }

    @Override
    protected final Object getData(int position) {
        for (Hybrid hybrid : hybrids) {
            if (hybrid.disabled) {
                continue;
            }
            int count = hybrid.getItemCount();
            if (position < count) {
                return hybrid.getData(position);
            }
            position -= count;
        }
        return null;
    }

    protected final int getHybridOffset(Hybrid h) {
        int count = 0;
        for (Hybrid hybrid : hybrids) {
            if (hybrid.disabled) {
                continue;
            }
            if (hybrid == h) {
                return count;
            }
            count += hybrid.getItemCount();
        }
        return -1;
    }

    private void handleClick(boolean longClick, View v, int pos, Object data) {
        for (Hybrid hybrid : hybrids) {
            if (hybrid.disabled) {
                continue;
            }
            int count = hybrid.getItemCount();
            if (pos < count) {
                hybrid.handleClick(longClick, v, pos, data);
                return;
            }
            pos -= count;
        }
    }
}
