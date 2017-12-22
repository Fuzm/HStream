package com.stream.hstream.fragments.tab;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hippo.easyrecyclerview.EasyRecyclerView;
import com.stream.client.HsClient;
import com.stream.client.HsRequest;
import com.stream.client.data.ListUrlBuilder;
import com.stream.client.data.VideoInfo;
import com.stream.client.parser.VideoListParser;
import com.stream.drawable.AddDeleteDrawable;
import com.stream.enums.GenreEnum;
import com.stream.hstream.HStreamApplication;
import com.stream.client.HsCallback;
import com.stream.hstream.R;
import com.stream.scene.SceneFragment;
import com.stream.util.AppHelper;
import com.stream.videoplayerlibrary.tv.TuMediaPlayerManager;
import com.stream.videoplayerlibrary.tv.TuVideoPlayer;
import com.stream.widget.ContentLayout;
import com.stream.widget.EditTextDialogBuilder;
import com.stream.widget.FabLayout;
import com.stream.hstream.adapter.VideoAdapter;

import junit.framework.Assert;

/**
 * Created by Seven-one on 2017/9/26.
 */

public class ListFragment extends SceneFragment implements
        FabLayout.OnClickFabListener, FabLayout.OnExpandListener {

    /**
     * get fragment for gener list
     */
    private GenreEnum mGenreEnum;
    private String mKeyword;

    public static ListFragment getInstance(GenreEnum genreEnum) {
        Assert.assertNotNull("genre type not null for list", genreEnum);
        ListFragment fragment = new ListFragment();
        fragment.mGenreEnum = genreEnum;
        return fragment;
    }

    /**
     * get fragment for search
     * @param genreEnum
     * @param keyword
     * @return
     */
    public static ListFragment getSearchInstance(GenreEnum genreEnum, String keyword) {
        Assert.assertNotNull("keyword cannot be not for search", keyword);
        Assert.assertTrue("genre enum must MuchoSearch or StreamSearch",
                (genreEnum == GenreEnum.MochuSearch || genreEnum == GenreEnum.StreamSearch));

        ListFragment fragment = new ListFragment();
        fragment.mGenreEnum = genreEnum;
        fragment.mKeyword = keyword;
        return fragment;
    }

    public final static String KEY_LIST_URL_BUILDER = "list_url_builder";
    public final static String KEY_HAS_FIRST_REFRESH = "has_first_refresh";
    public final static String KEY_GENER_ENUM = "gener_enum";

    private static final long ANIMATE_TIME = 300L;

    private ContentLayout mContentLayout;
    private EasyRecyclerView mRecyclerView;
    private VideoListAdapter mAdapter;
    private VideoListHelper mHelper;
    private HsClient mClient;
    private ListUrlBuilder mUrlBuilder;
    private FloatingActionButton mSearchFab;
    private FabLayout mFabLayout;
    private AddDeleteDrawable mActionFabDrawable;

    private TuVideoPlayer.FullScreenListener mFullScreenListener;

    private boolean mHasFirstRefresh = false;

    private final RecyclerView.OnScrollListener mOnScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Context context = getContext();
        mClient = HStreamApplication.getHsClient(context);
        mUrlBuilder = new ListUrlBuilder(mGenreEnum);

        if(!TextUtils.isEmpty(mKeyword)) {
            mUrlBuilder.setKeyword(mKeyword);
        }

        if (savedInstanceState != null) {
            onRestore(savedInstanceState);
        }

        mFullScreenListener = new TuVideoPlayer.FullScreenListener(context);
    }

    private void onRestore(Bundle savedInstanceState) {
        mHasFirstRefresh = savedInstanceState.getBoolean(KEY_HAS_FIRST_REFRESH);
        mUrlBuilder = savedInstanceState.getParcelable(KEY_LIST_URL_BUILDER);
        mGenreEnum = (GenreEnum) savedInstanceState.getSerializable(KEY_GENER_ENUM);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(KEY_HAS_FIRST_REFRESH, mHasFirstRefresh);
        outState.putParcelable(KEY_LIST_URL_BUILDER, mUrlBuilder);
        outState.putSerializable(KEY_GENER_ENUM, mGenreEnum);
    }

    @Override
    public void onResume() {
        super.onResume();
        mFullScreenListener.registerListener();
    }

    @Override
    public int getNavCheckedItem() {
        return R.id.nav_home;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_tab, container, false);
        Resources resources = getContext().getResources();

        mHelper = new VideoListHelper();

        mContentLayout = (ContentLayout) view.findViewById(R.id.fragment_content_layout);
        mContentLayout.setHelper(mHelper);

        mRecyclerView = mContentLayout.getRecyclerView();
        mAdapter = new VideoListAdapter(inflater, resources, mRecyclerView, getContext());
        //mRecyclerView.setSelector(Ripple.generateRippleDrawable(context, false));
        mRecyclerView.setDrawSelectorOnTop(true);
        mRecyclerView.hasFixedSize();
        mRecyclerView.setClipToPadding(false);
        //mRecyclerView.setOnItemClickListener(this);

        mFabLayout = (FabLayout) view.findViewById(R.id.fab_layout);
        mFabLayout.setAutoCancel(true);
        mFabLayout.setExpanded(false);
        mFabLayout.setHidePrimaryFab(false);
        mFabLayout.setOnClickFabListener(this);
        mFabLayout.setOnExpandListener(this);
        //addAboveSnackView(mFabLayout);

        mActionFabDrawable = new AddDeleteDrawable(getContext());
        mActionFabDrawable.setColor(resources.getColor(R.color.primary_drawable_dark));
        mFabLayout.getPrimaryFab().setImageDrawable(mActionFabDrawable);

        mSearchFab = (FloatingActionButton) view.findViewById(R.id.search_fab);

        if(!mHasFirstRefresh) {
            mHasFirstRefresh = true;
            mHelper.firstRefresh();
        }

        return view;
    }

    private void onGetVideoListSuccess(int taskId, VideoListParser.Result result) {
        if(mHelper != null && mHelper.isCurrentTask(taskId)) {
            mHelper.setPages(taskId, result.pages);
            mHelper.onGetPageData(taskId, result.mVideoInfoList);
        }
    }

    private void onGetVideoListFail(int taskId, Exception e) {
        mHelper.onGetException(taskId, e);
    }

    @Override
    public void onClickPrimaryFab(FabLayout view, FloatingActionButton fab) {
        view.toggle();
    }

    public void showGoToDialog() {
        Context context = getContext();
        if(null == context) {
            return;
        }

        final int page = mHelper.getPageForTop();
        final int pages = mHelper.getPages();
        String hint = getString(R.string.go_to_hint, page, pages);
        final EditTextDialogBuilder builder = new EditTextDialogBuilder(context, hint);
        final AlertDialog dialog = builder.setTitle(R.string.go_to)
                .setPositiveButton(R.string.ok, null)
                .show();

        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mHelper == null) {
                    dialog.dismiss();
                    return;
                }

                String text = builder.getText();
                int goTo = 0;
                try {
                    goTo = Integer.parseInt(text);
                } catch (Exception e) {
                    builder.setError(getString(R.string.error_invalid_number));
                    return;
                }

                if(goTo < 0 || goTo > pages) {
                    builder.setError(getString(R.string.error_out_of_range));
                    return;
                }

                builder.setError(null);
                mHelper.goTo(goTo);
                AppHelper.hideSoftInput(dialog);
                dialog.dismiss();
            }
        });
    }

    @Override
    public void onClickSecondaryFab(FabLayout view, FloatingActionButton fab, int position) {
        if(null == mHelper) {
            return;
        }

        switch (position) {
            case 0:
                showGoToDialog();
                break;
            case 1:
                mHelper.refresh();
                break;
        }

        view.setExpanded(false);
    }

    @Override
    public void onExpand(boolean expanded) {
        if(null == mActionFabDrawable) {
            return;
        }

        if(expanded) {
            setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, Gravity.LEFT);
            setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, Gravity.RIGHT);
            mActionFabDrawable.setDelete(ANIMATE_TIME);
        } else {
            setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, Gravity.LEFT);
            setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, Gravity.RIGHT);
            mActionFabDrawable.setAdd(ANIMATE_TIME);
        }
    }

    //execute search
    public void applySearch(String query) {
        mUrlBuilder.reset();
        mUrlBuilder.setKeyword(query);
        mHelper.refresh();
    }

    public class VideoListHelper extends ContentLayout.ContentHelper<VideoInfo> {

        @Override
        protected Context getContext() {
            return ListFragment.this.getContext();
        }

        @Override
        public void getPageData(int taskId, int page, int type) {
            //Log.d(TAG, "query page :" + page);
            mUrlBuilder.setPageIndex(page);
            String url = mUrlBuilder.build();
            HsRequest request = new HsRequest();
            request.setMethod(HsClient.METHOD_GET_VIDEO_LIST);
            request.setCallback(new ListFragment.VideoListListener(getContext(), ListFragment.this, taskId));
            request.setArgs(url);
            mClient.execute(request);
        }

        @Override
        public void notifyDataSetChanged() {
            if(mAdapter != null) {
                mAdapter.notifyDataSetChanged();
            }
        }

        @Override
        protected void notifyItemRangeInserted(int positionStart, int itemCount) {
            if (null != mAdapter) {
                mAdapter.notifyItemRangeInserted(positionStart, itemCount);
            }
        }
    }

    public class VideoListAdapter extends VideoAdapter {

        public VideoListAdapter(@NonNull LayoutInflater inflater, @NonNull Resources resources, @NonNull RecyclerView recyclerView, Context context) {
            super(inflater, resources, recyclerView, context);
        }

        @Override
        public VideoInfo getDataAt(int position) {
            return null != mHelper ? mHelper.getDataAt(position) : null;
        }

        @Override
        public int getItemCount() {
            return null != mHelper ? mHelper.size() : 0;
        }
    }

    public class VideoListListener extends HsCallback<ListFragment, VideoListParser.Result> {

        private final int mTaskId;

        public VideoListListener(Context context, ListFragment fragment, int taskId) {
            super(fragment);
            mTaskId = taskId;
        }

        @Override
        public void onSuccess(VideoListParser.Result result) {
            getFragment().onGetVideoListSuccess(mTaskId, result);
        }

        @Override
        public void onFailure(Exception e) {
            getFragment().onGetVideoListFail(mTaskId, e);
        }

        @Override
        public void onCancel() {

        }
    }

    @Override
    public void onPause() {
        super.onPause();

        TuMediaPlayerManager.pauseManager();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        TuMediaPlayerManager.releaseManager();
        mFullScreenListener.unregistetListener();

        if(mClient != null) {
            mClient = null;
        }
    }

}
