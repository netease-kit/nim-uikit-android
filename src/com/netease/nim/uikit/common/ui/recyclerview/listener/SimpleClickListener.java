package com.netease.nim.uikit.common.ui.recyclerview.listener;

import android.os.Build;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;

import com.netease.nim.uikit.common.ui.recyclerview.adapter.IRecyclerView;
import com.netease.nim.uikit.common.ui.recyclerview.holder.BaseViewHolder;

import java.util.Iterator;
import java.util.Set;

/**
 * This can be useful for applications that wish to implement various forms of click and longclick and childView click
 * manipulation of item views within the RecyclerView. SimpleClickListener may intercept
 * a touch interaction already in progress even if the SimpleClickListener is already handling that
 * gesture stream itself for the purposes of scrolling.
 *
 * @see RecyclerView.OnItemTouchListener
 */
public abstract class SimpleClickListener<T extends IRecyclerView> implements RecyclerView.OnItemTouchListener {
    private GestureDetectorCompat mGestureDetector;
    private RecyclerView recyclerView;
    private Set<Integer> childClickViewIds;
    private Set<Integer> longClickViewIds;
    protected T baseAdapter;
    public static String TAG = "SimpleClickListener";
    private boolean mIsPrepressed = false;
    private boolean mIsShowPress = false;
    private View mPressedView = null;
    private boolean shouldDetectGesture = true;
    private int longClickDelta = 200;

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        if (!shouldDetectGesture()) {
            return false; // 拦截手势检测
        }

        // 手势检测
        if (recyclerView == null) {
            this.recyclerView = rv;
            this.baseAdapter = (T) recyclerView.getAdapter();
            mGestureDetector = new GestureDetectorCompat(recyclerView.getContext(), new ItemTouchHelperGestureListener(recyclerView));
        }
        if (!mGestureDetector.onTouchEvent(e) && e.getActionMasked() == MotionEvent.ACTION_UP && mIsShowPress) {
            if (mPressedView != null) {
                BaseViewHolder vh = (BaseViewHolder) recyclerView.getChildViewHolder(mPressedView);
                if (vh == null || vh.getItemViewType() != IRecyclerView.LOADING_VIEW || vh.getItemViewType() != IRecyclerView.FETCHING_VIEW) {
                    mPressedView.setPressed(false);
                }
                mPressedView = null;
            }
            mIsShowPress = false;
            mIsPrepressed = false;
        }

        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {
        if (!shouldDetectGesture()) {
            return;
        }

        mGestureDetector.onTouchEvent(e);
    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
    }

    private class ItemTouchHelperGestureListener extends GestureDetector.SimpleOnGestureListener {

        private RecyclerView recyclerView;

        public ItemTouchHelperGestureListener(RecyclerView recyclerView) {
            this.recyclerView = recyclerView;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            if (!shouldDetectGesture()) {
                return false;
            }

            mIsPrepressed = true;
            mPressedView = recyclerView.findChildViewUnder(e.getX(), e.getY());

            super.onDown(e);
            return false;
        }

        @Override
        public void onShowPress(MotionEvent e) {
            if (!shouldDetectGesture()) {
                return;
            }

            if (mIsPrepressed && mPressedView != null) {
//                mPressedView.setPressed(true);
                mIsShowPress = true;
            }
            super.onShowPress(e);
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            if (!shouldDetectGesture()) {
                return false;
            }

            if (mIsPrepressed && mPressedView != null) {
                final View pressedView = mPressedView;
                BaseViewHolder vh = (BaseViewHolder) recyclerView.getChildViewHolder(pressedView);

                if (isHeaderOrFooterPosition(vh.getLayoutPosition())) {
                    return false;
                }
                childClickViewIds = vh.getChildClickViewIds();

                if (childClickViewIds != null && childClickViewIds.size() > 0) {
                    for (Iterator it = childClickViewIds.iterator(); it.hasNext(); ) {
                        View childView = pressedView.findViewById((Integer) it.next());
                        if (inRangeOfView(childView, e) && childView.isEnabled()) {
                            setPressViewHotSpot(e, childView);
                            childView.setPressed(true);
                            onItemChildClick(baseAdapter, childView, vh.getLayoutPosition() - baseAdapter.getHeaderLayoutCount());
                            resetPressedView(childView);
                            return true;
                        } else {
                            childView.setPressed(false);
                        }
                    }
                    setPressViewHotSpot(e, pressedView);
                    mPressedView.setPressed(true);
                    for (Iterator it = childClickViewIds.iterator(); it.hasNext(); ) {
                        View childView = pressedView.findViewById((Integer) it.next());
                        childView.setPressed(false);
                    }
                    onItemClick(baseAdapter, pressedView, vh.getLayoutPosition() - baseAdapter.getHeaderLayoutCount());
                } else {
                    setPressViewHotSpot(e, pressedView);
                    mPressedView.setPressed(true);
                    for (Iterator it = childClickViewIds.iterator(); it.hasNext(); ) {
                        View childView = pressedView.findViewById((Integer) it.next());
                        childView.setPressed(false);
                    }
                    onItemClick(baseAdapter, pressedView, vh.getLayoutPosition() - baseAdapter.getHeaderLayoutCount());
                }
                resetPressedView(pressedView);

            }
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            if (!shouldDetectGesture()) {
                return;
            }

            boolean isChildLongClick = false;
            if (mIsPrepressed && mPressedView != null) {
                mPressedView.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                BaseViewHolder vh = (BaseViewHolder) recyclerView.getChildViewHolder(mPressedView);
                if (!isHeaderOrFooterPosition(vh.getLayoutPosition())) {
                    longClickViewIds = vh.getItemChildLongClickViewIds();
                    if (longClickViewIds != null && longClickViewIds.size() > 0) {
                        for (Iterator it = longClickViewIds.iterator(); it.hasNext(); ) {
                            View childView = mPressedView.findViewById((Integer) it.next());
                            if (inRangeOfView(childView, e) && childView.isEnabled()) {
                                setPressViewHotSpot(e, childView);
                                onItemChildLongClick(baseAdapter, childView, vh.getLayoutPosition() - baseAdapter.getHeaderLayoutCount());
                                childView.setPressed(true);
                                mIsShowPress = true;
                                isChildLongClick = true;
                                break;
                            }
                        }
                    }
                    if (!isChildLongClick) {
                        onItemLongClick(baseAdapter, mPressedView, vh.getLayoutPosition() - baseAdapter.getHeaderLayoutCount());
                        setPressViewHotSpot(e, mPressedView);
                        mPressedView.setPressed(true);
                        for (Iterator it = longClickViewIds.iterator(); it.hasNext(); ) {
                            View childView = mPressedView.findViewById((Integer) it.next());
                            childView.setPressed(false);
                        }
                        mIsShowPress = true;
                    }
                }
            }
        }

        private final void resetPressedView(final View pressedView) {
            if (pressedView != null) {
                pressedView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (pressedView != null) {
                            pressedView.setPressed(false);
                        }
                    }
                }, longClickDelta);
            }

            mIsPrepressed = false;
            mPressedView = null;
        }
    }

    private void setPressViewHotSpot(final MotionEvent e, final View mPressedView) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            /**
             * when   click   Outside the region  ,mPressedView is null
             */
            if (mPressedView != null && mPressedView.getBackground() != null) {
                mPressedView.getBackground().setHotspot(e.getRawX(), e.getY() - mPressedView.getY());
            }
        }
    }

    /**
     * Callback method to be invoked when an item in this AdapterView has
     * been clicked.
     *
     * @param view     The view within the AdapterView that was clicked (this
     *                 will be a view provided by the adapter)
     * @param position The position of the view in the adapter.
     */
    public abstract void onItemClick(T adapter, View view, int position);

    /**
     * callback method to be invoked when an item in this view has been
     * click and held
     *
     * @param view     The view whihin the AbsListView that was clicked
     * @param position The position of the view int the adapter
     * @return true if the callback consumed the long click ,false otherwise
     */
    public abstract void onItemLongClick(T adapter, View view, int position);

    public abstract void onItemChildClick(T adapter, View view, int position);

    public abstract void onItemChildLongClick(T adapter, View view, int position);

    public boolean inRangeOfView(View view, MotionEvent ev) {
        int[] location = new int[2];
        if (view.getVisibility() != View.VISIBLE) {
            return false;
        }
        view.getLocationOnScreen(location);
        int x = location[0];
        int y = location[1];
        if (ev.getRawX() < x
                || ev.getRawX() > (x + view.getWidth())
                || ev.getRawY() < y
                || ev.getRawY() > (y + view.getHeight())) {
            return false;
        }
        return true;
    }

    private boolean isHeaderOrFooterPosition(int position) {
        /**
         *  have a headview and EMPTY_VIEW FOOTER_VIEW LOADING_VIEW
         */
        if (baseAdapter == null) {
            if (recyclerView != null) {
                baseAdapter = (T) recyclerView.getAdapter();
            } else {
                return false;
            }
        }
        int type = baseAdapter.getItemViewType(position);
        return (type == IRecyclerView.EMPTY_VIEW || type == IRecyclerView.HEADER_VIEW || type == IRecyclerView.FOOTER_VIEW
                || type == IRecyclerView.LOADING_VIEW || type == IRecyclerView.FETCHING_VIEW);
    }

    public void setShouldDetectGesture(boolean shouldDetectGesture) {
        this.shouldDetectGesture = shouldDetectGesture;
    }

    private boolean shouldDetectGesture() {
        if (!shouldDetectGesture) {
            mIsPrepressed = false;
            mPressedView = null;
        }

        return shouldDetectGesture;
    }

    public void setLongClickDelta(int longClickDelta) {
        if (longClickDelta <= 0 || longClickDelta > 2000) {
            longClickDelta = 200;
        }

        this.longClickDelta = longClickDelta;
    }
}


