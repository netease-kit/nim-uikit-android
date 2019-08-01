package com.netease.nim.uikit.common.ui.listview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.BaseAdapter;

import com.netease.nim.uikit.common.adapter.IViewReclaimer;

public class MessageListView extends AutoRefreshListView {

    private IViewReclaimer viewReclaimer;

    private GestureDetector gestureDetector;

    private OnListViewEventListener listener;

    private RecyclerListener recyclerListener = new RecyclerListener() {

        @Override
        public void onMovedToScrapHeap(View view) {
            if (viewReclaimer != null) {
                viewReclaimer.reclaimView(view);
            }
        }
    };

    public MessageListView(Context context) {
        super(context);
        init(context);
    }

    public MessageListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MessageListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        setRecyclerListener(recyclerListener);

        gestureDetector = new GestureDetector(context, new GestureListener());
    }

    private boolean isScroll = false;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);

        if (event.getAction() == MotionEvent.ACTION_CANCEL || event.getAction() == MotionEvent.ACTION_UP) {
            isScroll = false;
        }

        return super.onTouchEvent(event);
    }

    public void setListViewEventListener(OnListViewEventListener listener) {
        this.listener = listener;
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            if (!isScroll) {
                if (listener != null) {
                    listener.onListViewStartScroll();
                    isScroll = true;
                }
            }

            return true;
        }

        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (!isScroll) {
                if (listener != null) {
                    listener.onListViewStartScroll();
                    isScroll = true;
                }
            }

            return true;
        }
    }

    public void setAdapter(BaseAdapter adapter) {
        // view reclaimer
        viewReclaimer = adapter != null && adapter instanceof IViewReclaimer ? (IViewReclaimer) adapter : null;

        super.setAdapter(adapter);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    public interface OnListViewEventListener {
        public void onListViewStartScroll();
    }
}
