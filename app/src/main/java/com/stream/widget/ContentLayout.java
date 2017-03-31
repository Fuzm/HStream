package com.stream.widget;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.TabLayout;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.hippo.easyrecyclerview.EasyRecyclerView;
import com.hippo.refreshlayout.RefreshLayout;
import com.hippo.yorozuya.IntIdGenerator;
import com.stream.hstream.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Fuzm on 2017/3/26 0026.
 */

public class ContentLayout extends FrameLayout{

    private EasyRecyclerView mRecyclerView;
    private ContentHelper mContentHelper;
    private RefreshLayout mRefreshLayout;

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
        LayoutInflater.from(context).inflate(R.layout.content_video_list, this);

        mRefreshLayout = (RefreshLayout) findViewById(R.id.refresh_layout);
        mRecyclerView = (EasyRecyclerView) mRefreshLayout.findViewById(R.id.recycler_view);
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

    public abstract static class ContentHelper <E extends Parcelable> {

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

        private ArrayList<E> mData = new ArrayList<>();

        private IntIdGenerator mIdGenerator = new IntIdGenerator();

        private int mPages;
        private int mStartPage;
        private int mEndPage;

        private int mCurrentTaskPage;
        private int mCurrentTaskId;
        private int mCurrentTaskType;

        private RefreshLayout mRefreshLayout;
        private EasyRecyclerView mRecyclerView;

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
                if(mStartPage > 0) {
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
                if(mEndPage < mPages) {
                    mCurrentTaskId = mIdGenerator.nextId();
                    mCurrentTaskType = TYPE_NEXT_PAGE_KEEP_POS;
                    mCurrentTaskPage = mEndPage;
                    getPageData(mCurrentTaskId, mCurrentTaskPage, mCurrentTaskType);
                } else if(mEndPage == mPages) {
                    //TO DO
                } else {
                    Log.e(TAG, "END PAGE");
                }
            }
        };

        private void init(ContentLayout contentLayout) {
            mRefreshLayout = contentLayout.getRefreshLayout();
            mRecyclerView = contentLayout.getRecyclerView();

            mRefreshLayout.setOnRefreshListener(mOnRefreshListener);
            mRecyclerView.setOnScrollListener(mOnScrollListener);
        }

        public abstract void getPageData(int taskId, int page, int type);

        protected abstract void notifyDataSetChanged();

        protected abstract void notifyItemRangeInserted(int positionStart, int itemCount);

        public void onGetPageData(int taskId, List<E> data) {

            switch (mCurrentTaskType) {
                case TYPE_REFRESH :
                    mStartPage = 0;
                    mEndPage = 2;

                    mData.clear();
                    mData.addAll(data);
                    notifyDataSetChanged();

                    break;
                case TYPE_PRE_PAGE :
                case TYPE_PRE_PAGE_KEEP_POS :
                    mStartPage--;

                    mData.addAll(0, data);
                    notifyItemRangeInserted(0, data.size());

                    break;
                case TYPE_NEXT_PAGE :
                case TYPE_NEXT_PAGE_KEEP_POS :
                    mEndPage++;

                    int oldSize = mData.size();
                    mData.addAll(oldSize, data);
                    notifyItemRangeInserted(oldSize, data.size());

                    break;
            }

            mRefreshLayout.setHeaderRefreshing(false);
            mRefreshLayout.setFooterRefreshing(false);
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

        public void setPages(int pages) {
            mPages = pages;
        }

        private void doRefresh() {
            mCurrentTaskId = mIdGenerator.nextId();
            mCurrentTaskType = TYPE_REFRESH;
            mCurrentTaskPage = 0;

            getPageData(mCurrentTaskId, mCurrentTaskPage, mCurrentTaskType);
        }

        public void firstRefresh() {
            doRefresh();
        }

        public void onGetException(int taskId, Exception e) {
            if (mCurrentTaskId == taskId) {
                mRefreshLayout.setHeaderRefreshing(false);
                mRefreshLayout.setFooterRefreshing(false);
            }
        }

        private Parcelable saveInstanceState(Parcelable superState) {
            Bundle bundle = new Bundle();
            bundle.putParcelable(KEY_SUPER, superState);

            // TODO What if data is large
            bundle.putParcelableArrayList(KEY_DATA, mData);
            bundle.putInt(KEY_NEXT_ID, mIdGenerator.nextId());
            bundle.putInt(KEY_PAGES, mPages);
            return bundle;
        }

        private Parcelable restoreInstanceState(Parcelable state) {
            if (state instanceof Bundle) {
                Bundle bundle = (Bundle) state;
                mData = bundle.getParcelableArrayList(KEY_DATA);
                mIdGenerator.setNextId(bundle.getInt(KEY_NEXT_ID));
                mPages = bundle.getInt(KEY_PAGES);
                return bundle.getParcelable(KEY_SUPER);
            } else {
                return state;
            }
        }
    }
}
