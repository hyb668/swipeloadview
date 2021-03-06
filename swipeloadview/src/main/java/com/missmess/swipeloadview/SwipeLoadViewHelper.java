package com.missmess.swipeloadview;

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ListView;

import com.missmess.swipeloadview.gridview.GridViewHandler;
import com.missmess.swipeloadview.listview.ExpandableListViewHandler;
import com.missmess.swipeloadview.listview.ListViewHandler;
import com.missmess.swipeloadview.recyclerview.RecyclerViewHandler;

import in.srain.cube.views.GridViewWithHeaderAndFooter;

/**
 * 在SwipeRefreshLayout中使用listview，RecyclerView，GridView等，并让这些view支持上拉加载更多的helper类。
 *
 * @author wl
 * @since 2015/12/02 09:57
 */
public class SwipeLoadViewHelper<V extends View> {
    private static final int SWIPE_ANIM_DELAY = 500;
    private SwipeRefreshLayout mRefreshLayout;
    private V mRefreshView;
    private ILoadViewFactory.ILoadMoreView mLoadMoreView;
    private ILoadViewHandler loadHandler;
    private OnRefreshLoadListener onRefreshLoadListener;
    private OnScrollBottomListener onScrollBottomListener;
    private View.OnClickListener onLoadMoreBtnClickListener;
    private boolean hasInitLoadMoreView = false;
    private boolean mHasMore = true;
    private boolean hasError = false;
    private boolean isLoading = false;
    private boolean isRefreshing = false;

    /**
     * 构造一个支持SwipeRefreshLayout+下拉加载的helper，使用默认 {@link DefaultLoadViewFactory} 显示加载更多，加载失败等界面。
     *
     * @param refreshLayout SwipeRefreshLayout
     * @param mRefreshView  需要支持下拉刷新，上拉加载的view。ListView、RecyclerView、{@link GridViewWithHeaderAndFooter}、ExpandableListView中的一个
     */
    public SwipeLoadViewHelper(SwipeRefreshLayout refreshLayout, V mRefreshView) {
        this(refreshLayout, mRefreshView, new DefaultLoadViewFactory());
    }

    /**
     * 构造一个支持SwipeRefreshLayout+下拉加载的helper
     *
     * @param refreshLayout   SwipeRefreshLayout
     * @param mRefreshView    需要支持下拉刷新，上拉加载的view。ListView、RecyclerView、{@link GridViewWithHeaderAndFooter}、ExpandableListView中的一个
     * @param loadViewFactory 布局factory
     */
    public SwipeLoadViewHelper(SwipeRefreshLayout refreshLayout, V mRefreshView, ILoadViewFactory loadViewFactory) {
        this.mRefreshLayout = refreshLayout;
        this.mRefreshView = mRefreshView;
        this.mLoadMoreView = loadViewFactory.madeLoadMoreView();

        if (mRefreshView instanceof ExpandableListView) {
            loadHandler = new ExpandableListViewHandler();
        } else if (mRefreshView instanceof ListView) {
            loadHandler = new ListViewHandler();
        } else if (mRefreshView instanceof RecyclerView) {
            loadHandler = new RecyclerViewHandler();
        } else if (mRefreshView instanceof GridViewWithHeaderAndFooter) {
            loadHandler = new GridViewHandler();
        } else {
            throw new IllegalArgumentException("this view do not support Load-More function");
        }

        onScrollBottomListener = new OnScrollBottomListener() {
            @Override
            public void onScrollBottom() {
                if (!hasError) {
                    judgeToLoadMore();
                }
            }
        };
        onLoadMoreBtnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                judgeToLoadMore();
            }
        };

        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!isRefreshing() && !isLoading()) { //不在刷新,加载中
                    mHasMore = true;
                    hasError = false;
                    isRefreshing = true;
                    if (onRefreshLoadListener != null) {
                        onRefreshLoadListener.onRefresh();
                    }
                }
            }
        });
    }

    private void judgeToLoadMore() {
        if (mHasMore && !isRefreshing() && !isLoading()) { //有数据，并且不在刷新,加载中
            hasError = false;
            isLoading = true;
            mLoadMoreView.showLoading();
            if (onRefreshLoadListener != null) {
                onRefreshLoadListener.onLoad();
            }
        }
    }

    public void setOnRefreshLoadListener(OnRefreshLoadListener onRefreshLoadListener) {
        this.onRefreshLoadListener = onRefreshLoadListener;
    }

    /**
     * 设置适配器。需要与设置的refreshView匹配。
     *
     * @param adapter 适配器
     */
    public void setAdapter(Object adapter) {
        loadHandler.setUpListener(mRefreshView, onScrollBottomListener);
        hasInitLoadMoreView = loadHandler.handleSetAdapter(mRefreshView, adapter, mLoadMoreView, onLoadMoreBtnClickListener);
    }

    /**
     * 显示刷新动画。（仅显示动画）
     */
    public void animRefresh() {
        mRefreshLayout.setRefreshing(true);
    }

    /**
     * 立即结束显示刷新动画。（仅关闭动画）
     */
    public void endAnimRefresh() {
        if (mRefreshLayout != null)
            mRefreshLayout.setRefreshing(false);
    }

    /**
     * 是否有更多数据
     *
     * @param hasMoreData true-还有数据；false-没有更多数据了
     */
    public void setHasMoreData(boolean hasMoreData) {
        this.mHasMore = hasMoreData;
        if (hasInitLoadMoreView && mLoadMoreView != null) {
            if (hasMoreData) {
                mLoadMoreView.showNormal();
            } else {
                mLoadMoreView.showNomore();
            }
        }
    }

    /**
     * 下拉刷新结束
     */
    public void completeRefresh() {
        isRefreshing = false;
        //延长动画
        mRefreshLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                mRefreshLayout.setRefreshing(false);
            }
        }, SWIPE_ANIM_DELAY);
    }

    /**
     * 加载更多结束
     */
    public void completeLoadmore() {
        isLoading = false;
        if (mHasMore && !hasError) //没错误，并且有更多数据
            mLoadMoreView.showNormal();
    }

    /**
     * 是否正在刷新
     *
     * @return true-是
     */
    public boolean isRefreshing() {
        return isRefreshing;
    }

    /**
     * 是否正在加载
     *
     * @return true-是
     */
    public boolean isLoading() {
        return isLoading;
    }

    /**
     * 加载更多出错了，显示错误信息
     *
     * @param msg 错误信息
     */
    public void setLoadMoreError(String msg) {
        hasError = true;
        mLoadMoreView.showFail(new Exception(msg));
    }

    /**
     * 刷新加载监听器
     */
    public interface OnRefreshLoadListener {
        /**
         * 刷新时
         */
        void onRefresh();

        /**
         * 加载更多时
         */
        void onLoad();
    }

    public void setOnListScrollListener(OnListScrollListener<V> scrollListener) {
        loadHandler.setOnScrollListener(scrollListener);
    }

    public static abstract class OnListScrollListener<G> {
        public OnListScrollListener() {
        }

        public void onScrollStateChanged(G view, int scrollState) {
        }

        public void onScroll(G view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        }
    }

    public interface OnScrollBottomListener {
        void onScrollBottom();
    }
}
