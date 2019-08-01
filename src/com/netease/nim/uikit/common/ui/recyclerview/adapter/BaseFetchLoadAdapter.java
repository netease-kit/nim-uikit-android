package com.netease.nim.uikit.common.ui.recyclerview.adapter;

import android.animation.Animator;
import android.content.Context;
import android.support.annotation.IntDef;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.LayoutParams;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;

import com.netease.nim.uikit.common.ui.recyclerview.animation.AlphaInAnimation;
import com.netease.nim.uikit.common.ui.recyclerview.animation.BaseAnimation;
import com.netease.nim.uikit.common.ui.recyclerview.animation.ScaleInAnimation;
import com.netease.nim.uikit.common.ui.recyclerview.animation.SlideInBottomAnimation;
import com.netease.nim.uikit.common.ui.recyclerview.animation.SlideInLeftAnimation;
import com.netease.nim.uikit.common.ui.recyclerview.animation.SlideInRightAnimation;
import com.netease.nim.uikit.common.ui.recyclerview.holder.BaseViewHolder;
import com.netease.nim.uikit.common.ui.recyclerview.loadmore.LoadMoreView;
import com.netease.nim.uikit.common.ui.recyclerview.loadmore.SimpleLoadMoreView;
import com.netease.nim.uikit.common.ui.recyclerview.util.RecyclerViewUtil;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

public abstract class BaseFetchLoadAdapter<T, K extends BaseViewHolder> extends RecyclerView.Adapter<K> implements IRecyclerView {

    private static final String TAG = BaseFetchLoadAdapter.class.getSimpleName();

    // fetch more
    public interface RequestFetchMoreListener {
        void onFetchMoreRequested();
    }

    protected RecyclerView mRecyclerView;

    private boolean mFetching = false;
    private boolean mFetchMoreEnable = false;
    private boolean mNextFetchEnable = false;
    private boolean mFirstFetchSuccess = true;
    private int mAutoFetchMoreSize = 1; // 距离顶部多少条就开始拉取数据了
    private RequestFetchMoreListener mRequestFetchMoreListener;
    private LoadMoreView mFetchMoreView = new SimpleLoadMoreView();

    //load more
    public interface RequestLoadMoreListener {
        void onLoadMoreRequested();
    }

    private boolean mLoading = false;
    private boolean mNextLoadEnable = false;
    private boolean mLoadMoreEnable = false;
    private boolean mFirstLoadSuccess = true;
    private int mAutoLoadMoreSize = 1; // 距离底部多少条就开始加载数据了
    private RequestLoadMoreListener mRequestLoadMoreListener;
    private LoadMoreView mLoadMoreView = new SimpleLoadMoreView();

    // animation
    private boolean mAnimationShowFirstOnly = true;
    private boolean mOpenAnimationEnable = false;
    private Interpolator mInterpolator = new LinearInterpolator();
    private int mAnimationDuration = 200;
    private int mLastPosition = -1;

    // @AnimationType
    private BaseAnimation mCustomAnimation;
    private BaseAnimation mSelectAnimation = new AlphaInAnimation();

    // empty
    private FrameLayout mEmptyView;
    private boolean mIsUseEmpty = true;

    // basic
    protected Context mContext;
    protected int mLayoutResId;
    protected LayoutInflater mLayoutInflater;
    protected List<T> mData;
    private boolean isScrolling = false;

    /**
     * Implement this method and use the helper to adapt the view to the given item.
     *
     * @param helper      A fully initialized helper.
     * @param item        the item that needs to be displayed.
     * @param position    the item position
     * @param isScrolling RecyclerView is scrolling
     */
    protected abstract void convert(K helper, T item, int position, boolean isScrolling);


    @IntDef({ALPHAIN, SCALEIN, SLIDEIN_BOTTOM, SLIDEIN_LEFT, SLIDEIN_RIGHT})
    @Retention(RetentionPolicy.SOURCE)
    public @interface AnimationType {
    }

    /**
     * Use with {@link #openLoadAnimation}
     */
    public static final int ALPHAIN = 0x00000001;
    /**
     * Use with {@link #openLoadAnimation}
     */
    public static final int SCALEIN = 0x00000002;
    /**
     * Use with {@link #openLoadAnimation}
     */
    public static final int SLIDEIN_BOTTOM = 0x00000003;
    /**
     * Use with {@link #openLoadAnimation}
     */
    public static final int SLIDEIN_LEFT = 0x00000004;
    /**
     * Use with {@link #openLoadAnimation}
     */
    public static final int SLIDEIN_RIGHT = 0x00000005;


    /**
     * Same as QuickAdapter#QuickAdapter(Context,int) but with
     * some initialization data.
     *
     * @param layoutResId The layout resource id of each item.
     * @param data        A new list is created out of this one to avoid mutable list
     */
    public BaseFetchLoadAdapter(RecyclerView recyclerView, int layoutResId, List<T> data) {
        this.mRecyclerView = recyclerView;
        this.mData = data == null ? new ArrayList<T>() : data;
        if (layoutResId != 0) {
            this.mLayoutResId = layoutResId;
        }

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                isScrolling = newState != RecyclerView.SCROLL_STATE_IDLE;
            }
        });

        /**
         * 关闭默认viewholder item动画
         */
        RecyclerViewUtil.changeItemAnimation(recyclerView, false);
    }

    @Override
    public int getHeaderLayoutCount() {
        return getFetchMoreViewCount();
    }

    /**
     * *********************************** fetch more 顶部下拉加载 ***********************************
     */

    public void setOnFetchMoreListener(RequestFetchMoreListener requestFetchMoreListener) {
        this.mRequestFetchMoreListener = requestFetchMoreListener;
        mNextFetchEnable = true;
        mFetchMoreEnable = true;
        mFetching = false;
    }

    public void setAutoFetchMoreSize(int autoFetchMoreSize) {
        if (autoFetchMoreSize > 1) {
            mAutoFetchMoreSize = autoFetchMoreSize;
        }
    }

    public void setFetchMoreView(LoadMoreView fetchMoreView) {
        this.mFetchMoreView = fetchMoreView; // 自定义View
    }

    private int getFetchMoreViewCount() {
        if (mRequestFetchMoreListener == null || !mFetchMoreEnable) {
            return 0;
        }
        if (!mNextFetchEnable && mFetchMoreView.isLoadEndMoreGone()) {
            return 0;
        }

        return 1;
    }

    /**
     * 列表滑动时自动拉取数据
     *
     * @param position
     */
    private void autoRequestFetchMoreData(int position) {
        if (getFetchMoreViewCount() == 0) {
            return;
        }

        if (position > mAutoFetchMoreSize - 1) {
            return;
        }

        if (mFetchMoreView.getLoadMoreStatus() != LoadMoreView.STATUS_DEFAULT) {
            return;
        }

        if (mData.size() == 0 && mFirstFetchSuccess) {
            return; // 都还没有数据，不自动触发加载，等外部塞入数据后再加载
        }

        Log.d(TAG, "auto fetch, pos=" + position);

        mFetchMoreView.setLoadMoreStatus(LoadMoreView.STATUS_LOADING);
        if (!mFetching) {
            mFetching = true;
            mRequestFetchMoreListener.onFetchMoreRequested();
        }
    }

    /**
     * fetch complete
     */
    public void fetchMoreComplete(final List<T> newData) {
        addFrontData(newData); // notifyItemRangeInserted从顶部向下加入View，顶部View并没有改变

        if (getFetchMoreViewCount() == 0) {
            return;
        }

        fetchMoreComplete(newData.size());
    }

    public void fetchMoreComplete(int newDataSize) {
        if (getFetchMoreViewCount() == 0) {
            return;
        }

        mFetching = false;
        mFetchMoreView.setLoadMoreStatus(LoadMoreView.STATUS_DEFAULT);
        notifyItemChanged(0);

        // 定位到insert新消息前的top消息位置。必须移动，否则还在顶部，会继续fetch!!!
        if (mRecyclerView != null) {
            RecyclerView.LayoutManager layoutManager = mRecyclerView.getLayoutManager();
            // 只有LinearLayoutManager才有查找第一个和最后一个可见view位置的方法
            if (layoutManager instanceof LinearLayoutManager) {
                LinearLayoutManager linearManager = (LinearLayoutManager) layoutManager;
                //获取第一个可见view的位置
                int firstItemPosition = linearManager.findFirstVisibleItemPosition();
                if (firstItemPosition == 0) {
                    // 最顶部可见的View已经是FetchMoreView了，那么add数据&局部刷新后，要进行定位到上次的最顶部消息。
                    mRecyclerView.scrollToPosition(newDataSize + getFetchMoreViewCount());
                }
            } else {
                mRecyclerView.scrollToPosition(newDataSize);
            }
        }
    }

    /**
     * fetch end, no more data
     *
     * @param data last load data to add
     * @param gone if true gone the fetch more view
     */
    public void fetchMoreEnd(List<T> data, boolean gone) {
        addFrontData(data);

        if (getFetchMoreViewCount() == 0) {
            return;
        }
        mFetching = false;
        mNextFetchEnable = false;
        mFetchMoreView.setLoadMoreEndGone(gone);
        if (gone) {
            notifyItemRemoved(0);
        } else {
            mFetchMoreView.setLoadMoreStatus(LoadMoreView.STATUS_END);
            notifyItemChanged(0);
        }
    }

    /**
     * fetch failed
     */
    public void fetchMoreFailed() {
        if (getFetchMoreViewCount() == 0) {
            return;
        }
        mFetching = false;
        if (mData.size() == 0) {
            mFirstFetchSuccess = false; // 首次加载失败
        }
        mFetchMoreView.setLoadMoreStatus(LoadMoreView.STATUS_FAIL);

        notifyItemChanged(0);
    }

    /**
     * *********************************** load more 底部上拉加载 ***********************************
     */

    public void setLoadMoreView(LoadMoreView loadingView) {
        this.mLoadMoreView = loadingView; // 自定义View
    }

    public void setOnLoadMoreListener(RequestLoadMoreListener requestLoadMoreListener) {
        this.mRequestLoadMoreListener = requestLoadMoreListener;
        mNextLoadEnable = true;
        mLoadMoreEnable = true;
        mLoading = false;
    }

    public void setAutoLoadMoreSize(int autoLoadMoreSize) {
        if (autoLoadMoreSize > 1) {
            mAutoLoadMoreSize = autoLoadMoreSize;
        }
    }

    private int getLoadMoreViewCount() {
        if (mRequestLoadMoreListener == null || !mLoadMoreEnable) {
            return 0;
        }
        if (!mNextLoadEnable && mLoadMoreView.isLoadEndMoreGone()) {
            return 0;
        }
        if (mData.size() == 0) {
            return 0;
        }
        return 1;
    }

    /**
     * 列表滑动时自动加载数据
     *
     * @param position
     */
    private void autoRequestLoadMoreData(int position) {
        if (getLoadMoreViewCount() == 0) {
            return;
        }

        if (position < getItemCount() - mAutoLoadMoreSize) {
            return;
        }

        if (mLoadMoreView.getLoadMoreStatus() != LoadMoreView.STATUS_DEFAULT) {
            return;
        }

        if (mData.size() == 0 && mFirstLoadSuccess) {
            return; // 都还没有数据，不自动触发加载，等外部塞入数据后再加载
        }

        Log.d(TAG, "auto load, pos=" + position);
        mLoadMoreView.setLoadMoreStatus(LoadMoreView.STATUS_LOADING);
        if (!mLoading) {
            mLoading = true;
            mRequestLoadMoreListener.onLoadMoreRequested();
        }
    }

    /**
     * load complete
     */
    public void loadMoreComplete(final List<T> newData) {
        appendData(newData);

        loadMoreComplete();
    }

    public void loadMoreComplete() {
        if (getLoadMoreViewCount() == 0) {
            return;
        }
        mLoading = false;
        mLoadMoreView.setLoadMoreStatus(LoadMoreView.STATUS_DEFAULT);
        notifyItemChanged(getFetchMoreViewCount() + mData.size());
    }

    /**
     * load end, no more data
     *
     * @param data last data to append
     * @param gone if true gone the load more view
     */
    public void loadMoreEnd(List<T> data, boolean gone) {
        appendData(data);

        if (getLoadMoreViewCount() == 0) {
            return;
        }
        mLoading = false;
        mNextLoadEnable = false;
        mLoadMoreView.setLoadMoreEndGone(gone);
        if (gone) {
            notifyItemRemoved(getFetchMoreViewCount() + mData.size());
        } else {
            mLoadMoreView.setLoadMoreStatus(LoadMoreView.STATUS_END);
            notifyItemChanged(getFetchMoreViewCount() + mData.size());
        }
    }

    /**
     * load failed
     */
    public void loadMoreFail() {
        if (getLoadMoreViewCount() == 0) {
            return;
        }
        mLoading = false;
        if (mData.size() == 0) {
            mFirstLoadSuccess = false; // 首次加载失败
        }
        mLoadMoreView.setLoadMoreStatus(LoadMoreView.STATUS_FAIL);
        notifyItemChanged(getFetchMoreViewCount() + mData.size());
    }


    /**
     * Set the enabled state of load more.
     *
     * @param enable True if load more is enabled, false otherwise.
     */
    public void setEnableLoadMore(boolean enable) {
        int oldLoadMoreCount = getLoadMoreViewCount();
        mLoadMoreEnable = enable;
        int newLoadMoreCount = getLoadMoreViewCount();

        if (oldLoadMoreCount == 1) {
            if (newLoadMoreCount == 0) {
                notifyItemRemoved(getFetchMoreViewCount() + mData.size());
            }
        } else {
            if (newLoadMoreCount == 1) {
                mLoadMoreView.setLoadMoreStatus(LoadMoreView.STATUS_DEFAULT);
                notifyItemInserted(getFetchMoreViewCount() + mData.size());
            }
        }
    }

    /**
     * Returns the enabled status for load more.
     *
     * @return True if load more is enabled, false otherwise.
     */
    public boolean isLoadMoreEnable() {
        return mLoadMoreEnable;
    }

    /**
     * *********************************** 数据源管理 ***********************************
     */

    /**
     * setting up a new instance to data;
     *
     * @param data
     */
    public void setNewData(List<T> data) {
        this.mData = data == null ? new ArrayList<T>() : data;
        if (mRequestLoadMoreListener != null) {
            mNextLoadEnable = true;
            mLoadMoreEnable = true;
            mLoading = false;
            mLoadMoreView.setLoadMoreStatus(LoadMoreView.STATUS_DEFAULT);
        }
        if (mRequestFetchMoreListener != null) {
            mNextFetchEnable = true;
            mFetchMoreEnable = true;
            mFetching = false;
            mFetchMoreView.setLoadMoreStatus(LoadMoreView.STATUS_DEFAULT);
        }

        mLastPosition = -1;
        notifyDataSetChanged();
    }

    /**
     * clear data before reload
     */
    public void clearData() {
        this.mData.clear();
        if (mRequestLoadMoreListener != null) {
            mNextLoadEnable = true;
            mLoading = false;
            mLoadMoreView.setLoadMoreStatus(LoadMoreView.STATUS_DEFAULT);
        }
        if (mRequestFetchMoreListener != null) {
            mNextFetchEnable = true;
            mFetching = false;
            mFetchMoreView.setLoadMoreStatus(LoadMoreView.STATUS_DEFAULT);
        }

        mLastPosition = -1;
        notifyDataSetChanged();
    }

    /**
     * insert  a item associated with the specified position of adapter
     *
     * @param position
     * @param item
     */
    public void add(int position, T item) {
        mData.add(position, item);
        notifyItemInserted(position + getFetchMoreViewCount());
    }

    /**
     * add new data in to certain location
     *
     * @param position
     */
    public void addData(int position, List<T> data) {
        if (0 <= position && position < mData.size()) {
            mData.addAll(position, data);
            notifyItemRangeInserted(getFetchMoreViewCount() + position, data.size());
        } else {
            throw new ArrayIndexOutOfBoundsException("inserted position most greater than 0 and less than data size");
        }
    }

    /**
     * remove the item associated with the specified position of adapter
     *
     * @param position
     */
    public void remove(int position) {
        final T item = mData.get(position);
        mData.remove(position);
        notifyItemRemoved(position + getHeaderLayoutCount());
        onRemove(item);
    }

    protected void onRemove(T item) {

    }

    /**
     * add new data to head location
     */
    public void addFrontData(List<T> data) {
        if (data == null || data.isEmpty()) {
            return;
        }

        mData.addAll(0, data);
        notifyItemRangeInserted(getFetchMoreViewCount(), data.size()); // add到FetchMoreView之下，保持FetchMoreView在顶部
    }

    /**
     * additional data;
     *
     * @param newData
     */
    public void appendData(List<T> newData) {
        if (newData == null || newData.isEmpty()) {
            return;
        }

        this.mData.addAll(newData);
        notifyItemRangeInserted(mData.size() - newData.size() + getFetchMoreViewCount(), newData.size());
    }

    public void appendData(T newData) {
        List<T> data = new ArrayList<>(1);
        data.add(newData);
        appendData(data);
    }

    /**
     * Get the data of list
     *
     * @return
     */
    public List<T> getData() {
        return mData;
    }

    /**
     * Get the data item associated with the specified position in the data set.
     *
     * @param position Position of the item whose data we want within the adapter's
     *                 data set.
     * @return The data at the specified position.
     */
    public T getItem(int position) {
        return mData.get(position);
    }

    public int getDataSize() {
        return mData == null ? 0 : mData.size();
    }

    public int getBottomDataPosition() {
        return getHeaderLayoutCount() + mData.size() - 1;
    }

    public void notifyDataItemChanged(int dataIndex) {
        notifyItemChanged(getHeaderLayoutCount() + dataIndex);
    }

    /**
     * *********************************** ViewHolder/ViewType ***********************************
     */

    @Override
    public int getItemCount() {
        int count;
        if (getEmptyViewCount() == 1) {
            count = 1;
        } else {
            count = getFetchMoreViewCount() + mData.size() + getLoadMoreViewCount();
        }
        return count;
    }

    @Override
    public int getItemViewType(int position) {
        if (getEmptyViewCount() == 1) {
            return EMPTY_VIEW;
        }

        // fetch
        autoRequestFetchMoreData(position);
        // load
        autoRequestLoadMoreData(position);
        int fetchMoreCount = getFetchMoreViewCount();
        if (position < fetchMoreCount) {
            Log.d(TAG, "FETCH pos=" + position);
            return FETCHING_VIEW;
        } else {
            int adjPosition = position - fetchMoreCount;
            int adapterCount = mData.size();
            if (adjPosition < adapterCount) {
                Log.d(TAG, "DATA pos=" + position);
                return getDefItemViewType(adjPosition);
            } else {
                Log.d(TAG, "LOAD pos=" + position);
                return LOADING_VIEW;
            }
        }
    }

    /**
     * To bind different types of holder and solve different the bind events
     *
     * @param holder
     * @param positions
     * @see #getDefItemViewType(int)
     */
    @Override
    public void onBindViewHolder(K holder, int positions) {
        int viewType = holder.getItemViewType();

        switch (viewType) {
            case LOADING_VIEW:
                mLoadMoreView.convert(holder);
                break;
            case FETCHING_VIEW:
                mFetchMoreView.convert(holder);
                break;
            case EMPTY_VIEW:
                break;
            default:
                convert(holder, mData.get(holder.getLayoutPosition() - getFetchMoreViewCount()), positions, isScrolling);
                break;
        }
    }

    protected K onCreateDefViewHolder(ViewGroup parent, int viewType) {
        return createBaseViewHolder(parent, mLayoutResId);
    }

    protected K createBaseViewHolder(ViewGroup parent, int layoutResId) {
        return createBaseViewHolder(getItemView(layoutResId, parent));
    }

    /**
     * @param layoutResId ID for an XML layout resource to load
     * @param parent      Optional view to be the parent of the generated hierarchy or else simply an object that
     *                    provides a set of LayoutParams values for root of the returned
     *                    hierarchy
     * @return view will be return
     */
    protected View getItemView(int layoutResId, ViewGroup parent) {
        return mLayoutInflater.inflate(layoutResId, parent, false);
    }

    /**
     * if you want to use subclass of BaseViewHolder in the adapter,
     * you must override the method to create new ViewHolder.
     *
     * @param view view
     * @return new ViewHolder
     */
    protected K createBaseViewHolder(View view) {
        return (K) new BaseViewHolder(view);
    }

    protected int getDefItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public K onCreateViewHolder(ViewGroup parent, int viewType) {
        K baseViewHolder;
        this.mContext = parent.getContext();
        this.mLayoutInflater = LayoutInflater.from(mContext);
        switch (viewType) {
            case FETCHING_VIEW:
                baseViewHolder = getFetchingView(parent);
                break;
            case LOADING_VIEW:
                baseViewHolder = getLoadingView(parent);
                break;
            case EMPTY_VIEW:
                baseViewHolder = createBaseViewHolder(mEmptyView);
                break;
            default:
                baseViewHolder = onCreateDefViewHolder(parent, viewType);
        }
        return baseViewHolder;

    }

    private K getLoadingView(ViewGroup parent) {
        View view = getItemView(mLoadMoreView.getLayoutId(), parent);
        K holder = createBaseViewHolder(view);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mLoadMoreView.getLoadMoreStatus() == LoadMoreView.STATUS_FAIL) {
                    mLoadMoreView.setLoadMoreStatus(LoadMoreView.STATUS_DEFAULT);
                    notifyItemChanged(getFetchMoreViewCount() + mData.size());
                }
            }
        });
        return holder;
    }

    private K getFetchingView(ViewGroup parent) {
        View view = getItemView(mFetchMoreView.getLayoutId(), parent);
        K holder = createBaseViewHolder(view);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mFetchMoreView.getLoadMoreStatus() == LoadMoreView.STATUS_FAIL) {
                    mFetchMoreView.setLoadMoreStatus(LoadMoreView.STATUS_DEFAULT);
                    notifyItemChanged(0);
                }
            }
        });
        return holder;
    }

    /**
     * Called when a view created by this adapter has been attached to a window.
     * simple to solve item will layout using all
     * {@link #setFullSpan(RecyclerView.ViewHolder)}
     *
     * @param holder
     */
    @Override
    public void onViewAttachedToWindow(K holder) {
        super.onViewAttachedToWindow(holder);
        int type = holder.getItemViewType();
        if (type == EMPTY_VIEW || type == LOADING_VIEW || type == FETCHING_VIEW) {
            setFullSpan(holder);
        } else {
            addAnimation(holder);
        }
    }

    /**
     * When set to true, the item will layout using all span area. That means, if orientation
     * is vertical, the view will have full width; if orientation is horizontal, the view will
     * have full height.
     * if the hold view use StaggeredGridLayoutManager they should using all span area
     *
     * @param holder True if this item should traverse all spans.
     */
    protected void setFullSpan(RecyclerView.ViewHolder holder) {
        if (holder.itemView.getLayoutParams() instanceof StaggeredGridLayoutManager.LayoutParams) {
            StaggeredGridLayoutManager.LayoutParams params = (StaggeredGridLayoutManager.LayoutParams) holder.itemView.getLayoutParams();
            params.setFullSpan(true);
        }
    }

    @Override
    public void onAttachedToRecyclerView(final RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
        if (manager instanceof GridLayoutManager) {
            final GridLayoutManager gridManager = ((GridLayoutManager) manager);
            gridManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    int type = getItemViewType(position);
                    if (mSpanSizeLookup == null) {
                        return (type == EMPTY_VIEW || type == LOADING_VIEW || type == FETCHING_VIEW) ? gridManager.getSpanCount() : 1;
                    } else {
                        return (type == EMPTY_VIEW || type == LOADING_VIEW || type == FETCHING_VIEW) ? gridManager
                                .getSpanCount() : mSpanSizeLookup.getSpanSize(gridManager, position - getFetchMoreViewCount());
                    }
                }
            });
        }
    }

    private SpanSizeLookup mSpanSizeLookup;

    public interface SpanSizeLookup {
        int getSpanSize(GridLayoutManager gridLayoutManager, int position);
    }

    /**
     * *********************************** EmptyView ***********************************
     */

    /**
     * if mEmptyView will be return 1 or not will be return 0
     *
     * @return
     */
    public int getEmptyViewCount() {
        if (mEmptyView == null || mEmptyView.getChildCount() == 0) {
            return 0;
        }
        if (!mIsUseEmpty) {
            return 0;
        }
        if (mData.size() != 0) {
            return 0;
        }
        return 1;
    }

    public void setEmptyView(View emptyView) {
        boolean insert = false;
        if (mEmptyView == null) {
            mEmptyView = new FrameLayout(emptyView.getContext());
            mEmptyView.setLayoutParams(new LayoutParams(MATCH_PARENT, MATCH_PARENT));
            insert = true;
        }
        mEmptyView.removeAllViews();
        mEmptyView.addView(emptyView);
        mIsUseEmpty = true;
        if (insert) {
            if (getEmptyViewCount() == 1) {
                notifyItemInserted(0);
            }
        }
    }

    /**
     * Set whether to use empty view
     *
     * @param isUseEmpty
     */
    public void isUseEmpty(boolean isUseEmpty) {
        mIsUseEmpty = isUseEmpty;
    }

    /**
     * When the current adapter is empty, the BaseQuickAdapter can display a special view
     * called the empty view. The empty view is used to provide feedback to the user
     * that no data is available in this AdapterView.
     *
     * @return The view to show if the adapter is empty.
     */
    public View getEmptyView() {
        return mEmptyView;
    }

    /**
     * *********************************** 动画 ***********************************
     */

    /**
     * Set the view animation type.
     *
     * @param animationType One of {@link #ALPHAIN}, {@link #SCALEIN}, {@link #SLIDEIN_BOTTOM}, {@link #SLIDEIN_LEFT}, {@link #SLIDEIN_RIGHT}.
     */
    public void openLoadAnimation(@AnimationType int animationType) {
        this.mOpenAnimationEnable = true;
        mCustomAnimation = null;
        switch (animationType) {
            case ALPHAIN:
                mSelectAnimation = new AlphaInAnimation();
                break;
            case SCALEIN:
                mSelectAnimation = new ScaleInAnimation();
                break;
            case SLIDEIN_BOTTOM:
                mSelectAnimation = new SlideInBottomAnimation();
                break;
            case SLIDEIN_LEFT:
                mSelectAnimation = new SlideInLeftAnimation();
                break;
            case SLIDEIN_RIGHT:
                mSelectAnimation = new SlideInRightAnimation();
                break;
            default:
                break;
        }
    }

    /**
     * Set Custom ObjectAnimator
     *
     * @param animation ObjectAnimator
     */
    public void openLoadAnimation(BaseAnimation animation) {
        this.mOpenAnimationEnable = true;
        this.mCustomAnimation = animation;
    }

    /**
     * To open the animation when loading
     */
    public void openLoadAnimation() {
        this.mOpenAnimationEnable = true;
    }

    /**
     * To close the animation when loading
     */
    public void closeLoadAnimation() {
        this.mOpenAnimationEnable = false;
        this.mSelectAnimation = null;
        this.mCustomAnimation = null;
        this.mAnimationDuration = 0;
    }

    /**
     * {@link #addAnimation(RecyclerView.ViewHolder)}
     *
     * @param firstOnly true just show anim when first loading false show anim when load the data every time
     */
    public void setAnimationShowFirstOnly(boolean firstOnly) {
        this.mAnimationShowFirstOnly = firstOnly;
    }

    /**
     * Sets the duration of the animation.
     *
     * @param duration The length of the animation, in milliseconds.
     */
    public void setAnimationDuration(int duration) {
        mAnimationDuration = duration;
    }

    /**
     * add animation when you want to show time
     *
     * @param holder
     */
    private void addAnimation(RecyclerView.ViewHolder holder) {
        if (mOpenAnimationEnable) {
            if (!mAnimationShowFirstOnly || holder.getLayoutPosition() > mLastPosition) {
                BaseAnimation animation;
                if (mCustomAnimation != null) {
                    animation = mCustomAnimation;
                } else {
                    animation = mSelectAnimation;
                }
                for (Animator anim : animation.getAnimators(holder.itemView)) {
                    startAnim(anim, holder.getLayoutPosition());
                }
                mLastPosition = holder.getLayoutPosition();
            }
        }
    }

    /**
     * set anim to start when loading
     *
     * @param anim
     * @param index
     */
    protected void startAnim(Animator anim, int index) {
        anim.setDuration(mAnimationDuration).start();
        anim.setInterpolator(mInterpolator);
    }
}
