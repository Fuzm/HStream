package com.stream.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hippo.easyrecyclerview.EasyRecyclerView;
import com.hippo.easyrecyclerview.FastScroller;
import com.hippo.easyrecyclerview.HandlerDrawable;
import com.hippo.easyrecyclerview.LayoutManagerUtils;
import com.hippo.refreshlayout.RefreshLayout;
import com.hippo.yorozuya.IntIdGenerator;
import com.hippo.yorozuya.ResourcesUtils;
import com.hippo.yorozuya.collect.IntList;
import com.stream.hstream.GetText;
import com.stream.hstream.R;
import com.stream.util.DrawableManager;
import com.stream.util.ExceptionUtils;
import com.stream.widget.view.ViewTransition;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Fuzm on 2017/3/26 0026.
 */

public class ContentLayout extends FrameLayout{

    private ProgressView mProgressView;
    private TextView mTipView;
    private ViewGroup mContentView;

    private RefreshLayout mRefreshLayout;
    private EasyRecyclerView mRecyclerView;
    private FastScroller mFastScroller;

    private ContentHelper mContentHelper;

    private int mRecyclerViewOriginTop;
    private int mRecyclerViewOriginBottom;

    public ContentLayout(Context context) {
        super(context);
        init(context);
    }

    public ContentLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ContentLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.widget_content_layout, this);

        mProgressView = (ProgressView) findViewById(R.id.progress);
        mTipView = (TextView) findViewById(R.id.tip);
        mContentView = (ViewGroup) findViewById(R.id.content_view);

        mRefreshLayout = (RefreshLayout) mContentView.findViewById(R.id.refresh_layout);
        mFastScroller = (FastScroller) mContentView.findViewById(R.id.fast_scroller);
        mRecyclerView = (EasyRecyclerView) mRefreshLayout.findViewById(R.id.recycler_view);

        mFastScroller.attachToRecyclerView(mRecyclerView);
        HandlerDrawable drawable = new HandlerDrawable();
        drawable.setColor(ResourcesUtils.getAttrColor(context, R.attr.colorAccent));
        mFastScroller.setHandlerDrawable(drawable);

        mRefreshLayout.setHeaderColorSchemeResources(
                R.color.loading_indicator_red,
                R.color.loading_indicator_purple,
                R.color.loading_indicator_blue,
                R.color.loading_indicator_cyan,
                R.color.loading_indicator_green,
                R.color.loading_indicator_yellow);
        mRefreshLayout.setFooterColorSchemeResources(
                R.color.loading_indicator_red,
                R.color.loading_indicator_blue,
                R.color.loading_indicator_green,
                R.color.loading_indicator_orange);

        mRecyclerViewOriginTop = mRecyclerView.getPaddingTop();
        mRecyclerViewOriginBottom = mRecyclerView.getPaddingBottom();
    }

    public EasyRecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    public RefreshLayout getRefreshLayout() {
        return mRefreshLayout;
    }

    public void setHelper(ContentHelper helper) {
        mContentHelper = helper;
        helper.init(this);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        return mContentHelper.saveInstanceState(super.onSaveInstanceState());
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        super.onRestoreInstanceState(mContentHelper.restoreInstanceState(state));
    }

    public abstract static class ContentHelper <E extends Parcelable> implements ViewTransition.OnShowViewListener {

        private static final String TAG = ContentHelper.class.getSimpleName();

        private static final String KEY_SUPER = "super";
        private static final String KEY_SHOWN_VIEW = "shown_view";
        private static final String KEY_TIP = "tip";
        private static final String KEY_DATA = "data";
        private static final String KEY_NEXT_ID = "next_id";
        private static final String KEY_PAGE_DIVIDER = "page_divider";
        private static final String KEY_START_PAGE = "start_page";
        private static final String KEY_END_PAGE = "end_page";
        private static final String KEY_PAGES = "pages";

        public static final int TYPE_REFRESH = 0;
        public static final int TYPE_PRE_PAGE = 1;
        public static final int TYPE_PRE_PAGE_KEEP_POS = 2;
        public static final int TYPE_NEXT_PAGE = 3;
        public static final int TYPE_NEXT_PAGE_KEEP_POS = 4;
        public static final int TYPE_SOMEWHERE = 5;

        private ArrayList<E> mData = new ArrayList<>();

        private IntIdGenerator mIdGenerator = new IntIdGenerator();

        private int mPages;
        private int mStartPage;
        private int mEndPage;

        private int mCurrentTaskPage;
        private int mCurrentTaskId;
        private int mCurrentTaskType;

        private ProgressView mProgressView;
        private TextView mTipView;
        private ViewGroup mContentView;

        private RefreshLayout mRefreshLayout;
        private EasyRecyclerView mRecyclerView;

        private ViewTransition mViewTransition;
        private IntList mPageDivider = new IntList();

        private String mEmptyString = "No hint";

        private final RecyclerView.OnScrollListener mOnScrollListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if(!mRefreshLayout.isRefreshing() && mRefreshLayout.isAlmostBottom() && mEndPage < mPages) {
                    mRefreshLayout.setFooterRefreshing(true);
                    mOnRefreshListener.onFooterRefresh();
                }
            }
        };

        private final RefreshLayout.OnRefreshListener mOnRefreshListener = new RefreshLayout.OnRefreshListener() {
            @Override
            public void onHeaderRefresh() {
                if(mStartPage > 1) {
                    mCurrentTaskId = mIdGenerator.nextId();
                    mCurrentTaskType = TYPE_PRE_PAGE_KEEP_POS;
                    mCurrentTaskPage = mStartPage - 1;
                    getPageData(mCurrentTaskId, mCurrentTaskPage, mCurrentTaskType);
                } else {
                    doRefresh();
                }
            }

            @Override
            public void onFooterRefresh() {
                if(mEndPage <= mPages) {
                    mCurrentTaskId = mIdGenerator.nextId();
                    mCurrentTaskType = TYPE_NEXT_PAGE_KEEP_POS;
                    mCurrentTaskPage = mEndPage;
                    getPageData(mCurrentTaskId, mCurrentTaskPage, mCurrentTaskType);
                } else {
                    Log.e(TAG, "END PAGE");
                    Toast.makeText(getContext(), GetText.getString(R.string.msg_last_page), Toast.LENGTH_SHORT).show();
                    mRefreshLayout.setHeaderRefreshing(false);
                    mRefreshLayout.setFooterRefreshing(false);
                }
            }
        };

        private void init(ContentLayout contentLayout) {
            mProgressView = contentLayout.mProgressView;
            mTipView = contentLayout.mTipView;
            mContentView = contentLayout.mContentView;

            mRefreshLayout = contentLayout.mRefreshLayout;
            mRecyclerView = contentLayout.mRecyclerView;

            Drawable drawable = DrawableManager.getDrawable(getContext(), R.drawable.big_weird_face);
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            mTipView.setCompoundDrawables(null, drawable, null, null);

            mViewTransition = new ViewTransition(mContentView, mProgressView, mTipView);
            mViewTransition.setOnShowViewListener(this);

            mRefreshLayout.setOnRefreshListener(mOnRefreshListener);
            mRecyclerView.setOnScrollListener(mOnScrollListener);

            mTipView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    refresh();
                }
            });
        }

        protected abstract Context getContext();

        public abstract void getPageData(int taskId, int page, int type);

        protected abstract void notifyDataSetChanged();

        protected abstract void notifyItemRangeInserted(int positionStart, int itemCount);

        @Override
        public void onShowView(View hiddenView, View shownView) {
        }

        public void showProgressBar() {
            showProgressBar(true);
        }

        public void showProgressBar(boolean animation) {
            mViewTransition.showView(1, animation);
        }

        public void showContent() {
            mViewTransition.showView(0);
        }

        public void showText(CharSequence text) {
            mTipView.setText(text);
            mViewTransition.showView(2);
        }

        public void showEmptyString() {
            showText(mEmptyString);
        }

        public void onGetPageData(int taskId, List<E> data) {
            if(mCurrentTaskId == taskId) {
                int datasize = 0;
                switch (mCurrentTaskType) {
                    case TYPE_REFRESH :
                        mStartPage = 1;
                        mEndPage = 2;

                        mPageDivider.clear();
                        mPageDivider.add(data.size());

                        if(data.isEmpty()) {
                            mData.clear();
                            notifyDataSetChanged();
                            showEmptyString();
                        } else {
                            mData.clear();
                            mData.addAll(data);
                            notifyDataSetChanged();
                            showContent();
                        }

                        break;
                    case TYPE_PRE_PAGE :
                    case TYPE_PRE_PAGE_KEEP_POS :
                        datasize = data.size();
                        for(int i=0, n=mPageDivider.size(); i<n; i++) {
                            mPageDivider.set(i, mPageDivider.get(i)+datasize);
                        }
                        mPageDivider.add(0, datasize);
                        mStartPage--;

                        if(data.isEmpty()) {
                            if(mData.isEmpty()) {
                                showEmptyString();
                            } else {
                                showContent();
                            }
                        } else {
                            mData.addAll(0, data);
                            notifyItemRangeInserted(0, data.size());
                            showContent();
                        }

                        break;
                    case TYPE_NEXT_PAGE :
                    case TYPE_NEXT_PAGE_KEEP_POS :
                        datasize = data.size();
                        int oldDataSize = mData.size();
                        mPageDivider.add(oldDataSize + datasize);
                        mEndPage++;

                        if(data.isEmpty()) {
                            if(mData.isEmpty()) {
                                showEmptyString();
                            } else {
                                showContent();
                            }

                        } else {
                            mData.addAll(oldDataSize, data);
                            notifyItemRangeInserted(oldDataSize, data.size());

                            showContent();
                        }

                        break;
                    case TYPE_SOMEWHERE:
                        mStartPage = mCurrentTaskPage;
                        mEndPage = mCurrentTaskPage + 1;

                        mPageDivider.clear();
                        mPageDivider.add(data.size());

                        if(data.isEmpty()) {
                            mData.clear();
                            notifyDataSetChanged();
                            showEmptyString();
                        } else {
                            mData.clear();
                            mData.addAll(data);
                            notifyDataSetChanged();
                            showContent();
                        }

                        LayoutManagerUtils.scrollToPositionWithOffset(mRecyclerView.getLayoutManager(), 0, 0);
                        break;
                }

                mRefreshLayout.setHeaderRefreshing(false);
                mRefreshLayout.setFooterRefreshing(false);
            }
        }

        private int getPageStart(int page) {
            if(mStartPage == page) {
                return 0;
            } else {
                return mPageDivider.get(page - mStartPage - 1);
            }
        }

        public void goTo(int page) {
           if(page < 0 || page > mPages) {
               throw new IndexOutOfBoundsException("Page count is " + mPages + ", page is " + page);
           } else {
               if(page >= mStartPage && page < mEndPage) {
                   int position = getPageStart(page);
                   LayoutManagerUtils.scrollToPositionWithOffset(mRecyclerView.getLayoutManager(), position, 0);
               } else if(page == mEndPage) {
                   mRefreshLayout.setFooterRefreshing(false);
                   mRefreshLayout.setHeaderRefreshing(true);

                   mCurrentTaskId = mIdGenerator.nextId();
                   mCurrentTaskPage = page;
                   mCurrentTaskType = TYPE_SOMEWHERE;
                   getPageData(mCurrentTaskId, mCurrentTaskPage, mCurrentTaskType);

               } else {
                   mRefreshLayout.setFooterRefreshing(false);
                   mRefreshLayout.setHeaderRefreshing(true);

                   mCurrentTaskId = mIdGenerator.nextId();
                   mCurrentTaskPage = page;
                   mCurrentTaskType = TYPE_SOMEWHERE;
                   getPageData(mCurrentTaskId, mCurrentTaskPage, mCurrentTaskType);
               }
           }
        }

        public int getPageForPosition(int position) {
            if(position < 0) {
                return -1;
            }

            IntList pageDivider = mPageDivider;
            for (int i = 0, n = pageDivider.size(); i < n; i++) {
                if (position < pageDivider.get(i)) {
                    return i + mStartPage;
                }
            }

            return -1;
        }

        public int getPageForTop() {
            return getPageForPosition(LayoutManagerUtils.getFirstVisibleItemPosition(mRecyclerView.getLayoutManager()));
        }

        public boolean isCurrentTask(int taskId) {
            return mCurrentTaskId == taskId;
        }

        public E getDataAt(int location){
            return mData.get(location);
        }

        public int size() {
            return mData.size();
        }

        public int getPages() {
            return mPages;
        }

        public void setPages(int taskId, int pages) {
            if(mCurrentTaskId == taskId) {
                mPages = pages;
            }
        }

        private void doRefresh() {
            mCurrentTaskId = mIdGenerator.nextId();
            mCurrentTaskType = TYPE_REFRESH;
            mCurrentTaskPage = 0;

            getPageData(mCurrentTaskId, mCurrentTaskPage, mCurrentTaskType);
        }

        public void firstRefresh() {
            showProgressBar(false);
            doRefresh();
        }

        public void refresh() {
            showProgressBar();
            doRefresh();
        }

        public void onGetException(int taskId, Exception e) {
            if (mCurrentTaskId == taskId) {
                mRefreshLayout.setHeaderRefreshing(false);
                mRefreshLayout.setFooterRefreshing(false);

                String readableError;
                if (e != null) {
                    e.printStackTrace();
                    readableError = ExceptionUtils.getReadableString(e);
                } else {
                    readableError = getContext().getString(R.string.error_unknown);
                }

                if (mViewTransition.getShownViewIndex() == 0) {
                    Toast.makeText(getContext(), readableError, Toast.LENGTH_SHORT).show();
                } else {
                    showText(readableError);
                }
            }
        }

        private Parcelable saveInstanceState(Parcelable superState) {
            Bundle bundle = new Bundle();
            bundle.putParcelable(KEY_SUPER, superState);

            // TODO What if data is large
            bundle.putParcelableArrayList(KEY_DATA, mData);
            bundle.putInt(KEY_NEXT_ID, mIdGenerator.nextId());
            bundle.putInt(KEY_PAGES, mPages);
            bundle.putInt(KEY_START_PAGE, mStartPage);
            bundle.putInt(KEY_END_PAGE, mEndPage);
            return bundle;
        }

        private Parcelable restoreInstanceState(Parcelable state) {
            if (state instanceof Bundle) {
                Bundle bundle = (Bundle) state;
                mData = bundle.getParcelableArrayList(KEY_DATA);
                mIdGenerator.setNextId(bundle.getInt(KEY_NEXT_ID));
                mPages = bundle.getInt(KEY_PAGES);
                mStartPage = bundle.getInt(KEY_START_PAGE);
                mEndPage = bundle.getInt(KEY_END_PAGE);
                return bundle.getParcelable(KEY_SUPER);
            } else {
                return state;
            }
        }
    }
}
