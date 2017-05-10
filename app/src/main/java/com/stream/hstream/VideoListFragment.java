package com.stream.hstream;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.hippo.easyrecyclerview.EasyRecyclerView;
import com.stream.client.HsClient;
import com.stream.client.HsRequest;
import com.stream.client.data.ListUrlBuilder;
import com.stream.client.data.VideoInfo;
import com.stream.client.data.VideoSourceInfo;
import com.stream.client.parser.VideoListParser;
import com.stream.client.parser.VideoSourceParser;
import com.stream.download.DownloadService;
import com.stream.drawable.AddDeleteDrawable;
import com.stream.drawable.DrawerArrowDrawable;
import com.stream.scene.Announcer;
import com.stream.scene.SceneFragment;
import com.stream.util.AppHelper;
import com.stream.util.DrawableManager;
import com.stream.videoplayerlibrary.tv.TuVideoPlayer;
import com.stream.widget.ContentLayout;
import com.stream.widget.EditTextDialogBuilder;
import com.stream.widget.FabLayout;
import com.stream.widget.SearchBar;
import com.stream.widget.VideoAdapter;

/**
 * Created by Fuzm on 2017/3/24 0024.
 */

public class VideoListFragment extends SceneFragment implements EasyRecyclerView.OnItemClickListener,
        FabLayout.OnClickFabListener, FabLayout.OnExpandListener, SearchBar.Helper,
        SearchBar.OnStateChangeListener {

    private static final String TAG = VideoListFragment.class.getSimpleName();

    public final static String KEY_LIST_URL_BUILDER = "list_url_builder";
    public final static String KEY_HAS_FIRST_REFRESH = "has_first_refresh";

    private static final long ANIMATE_TIME = 300L;
    private static final int BACK_PRESSED_INTERVAL = 2000;

    private ContentLayout mContentLayout;
    private SearchBar mSearchBar;
    private EasyRecyclerView mRecyclerView;
    private VideoListAdapter mAdapter;
    private VideoListHelper mHelper;
    private HsClient mClient;
    private ListUrlBuilder mUrlBuilder;
    private FloatingActionButton mSearchFab;
    private FabLayout mFabLayout;
    private AddDeleteDrawable mActionFabDrawable;
    private DrawerArrowDrawable mLeftDrawable;
    private AddDeleteDrawable mRightDrawable;

    private boolean mHasFirstRefresh = false;
    private VideoInfo mClickVideo;
    private long mPressBackTime = 0;

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
        mUrlBuilder = new ListUrlBuilder();

        if (savedInstanceState != null) {
            onRestore(savedInstanceState);
        }
    }

    private void onRestore(Bundle savedInstanceState) {
        mHasFirstRefresh = savedInstanceState.getBoolean(KEY_HAS_FIRST_REFRESH);
        mUrlBuilder = savedInstanceState.getParcelable(KEY_LIST_URL_BUILDER);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(KEY_HAS_FIRST_REFRESH, mHasFirstRefresh);
        outState.putParcelable(KEY_LIST_URL_BUILDER, mUrlBuilder);
    }

    @Override
    public int getNavCheckedItem() {
        return R.id.nav_home;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_content_main, container, false);
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
        mRecyclerView.setOnItemClickListener(this);

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
        mSearchBar = (SearchBar) view.findViewById(R.id.search_tool_bar);

        mLeftDrawable = new DrawerArrowDrawable(getContext());
        mRightDrawable = new AddDeleteDrawable(getContext());
        mSearchBar.setLeftDrawable(mLeftDrawable);
        mSearchBar.setRightDrawable(mRightDrawable);
        mSearchBar.setHelper(this);
        mSearchBar.setTitle(getTitleForUrlBuilder());
        setSearchBarHint(getContext(), mSearchBar);
        mSearchBar.setOnStateChangeListener(this);

        if(!mHasFirstRefresh) {
            mHasFirstRefresh = true;
            mHelper.firstRefresh();
        }

        return view;
    }

    private void setSearchBarHint(Context context, SearchBar searchBar) {
        Resources resources = context.getResources();
        Drawable searchImage = DrawableManager.getDrawable(context, R.drawable.v_magnify_x24);
        SpannableStringBuilder ssb = new SpannableStringBuilder("   ");
        ssb.append(resources.getString(R.string.gallery_list_search_bar_hint_exhentai));
        int textSize = (int) (searchBar.getEditTextTextSize() * 1.25);
        if (searchImage != null) {
            searchImage.setBounds(0, 0, textSize, textSize);
            ssb.setSpan(new ImageSpan(searchImage), 1, 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        searchBar.setEditTextHint(ssb);
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
    public boolean onItemClick(EasyRecyclerView parent, View view, int position, long id) {
        if (null == mHelper || null == mRecyclerView) {
            return false;
        }

        VideoInfo videoInfo = mHelper.getDataAt(position);
        if(view.getId() == R.id.download_button) {
            Intent intent = new Intent(getActivity(), DownloadService.class);
            intent.putExtra(DownloadService.KEY_VIDEO_INFO, videoInfo);
            getActivity().startService(intent);
        }

        return true;
    }

    private void requiredDetailInfo() {
        HsRequest request = new HsRequest();
        request.setMethod(HsClient.METHOD_GET_VIDEO_DETAIL);
        request.setCallback(new VideoListFragment.VideoDetailListener(this));
        request.setArgs(mClickVideo.url);
        mClient.execute(request);
    }

    private void onRequiredDetailSuccess(VideoSourceParser.Result result) {
        VideoSourceInfo videoSourceInfo = result.mVideoSourceInfoList.get(0);
        if(videoSourceInfo != null) {
//            Intent intent = new Intent(getActivity(), VideoPlayActivity.class);
//            intent.putExtra(VideoPlayActivity.KEY_VIDEO_TITLE, mClickVideo.title);
//            intent.putExtra(VideoPlayActivity.KEY_VIDEO_THUMB, mClickVideo.thumb);
//            intent.putExtra(VideoPlayActivity.KEY_VIDEO_URL, videoSourceInfo.videoUrl);
//            startActivity(intent);
        }
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
            //阻止展开是测试菜单
            setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, Gravity.LEFT);
            setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, Gravity.RIGHT);
            mActionFabDrawable.setDelete(ANIMATE_TIME);
        } else {
            setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, Gravity.LEFT);
            setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, Gravity.RIGHT);
            mActionFabDrawable.setAdd(ANIMATE_TIME);
        }
    }

    @Override
    public void onClickLeftIcon() {
        if(null == mSearchBar) {
            return;
        }

        if(mSearchBar.getState() == SearchBar.STATE_NORMAL) {
            toggleDrawer(Gravity.LEFT);
        } else {
            mSearchBar.setState(SearchBar.STATE_NORMAL, true);
        }
    }

    @Override
    public void onClickRightIcon() {
        if(mSearchBar.getState() == SearchBar.STATE_SEARCH) {
            mSearchBar.setText("");
        }
    }

    @Override
    public void onClickTitle() {
        mSearchBar.setState(SearchBar.STATE_SEARCH, true);
    }

    @Override
    public void onApplySearch(String query) {
        mUrlBuilder.reset();
        mUrlBuilder.setKeyword(query);
        mHelper.refresh();

        mSearchBar.setTitle(getTitleForUrlBuilder());
        mSearchBar.setState(SearchBar.STATE_NORMAL, true);
    }

    private String getTitleForUrlBuilder() {
        String keyword = mUrlBuilder.getKeyword();

        if(TextUtils.isEmpty(keyword)) {
            return getResources().getString(R.string.app_name);
        } else {
            return keyword;
        }
    }

    @Override
    public void onStateChange(SearchBar searchBar, int newState, int oldState, boolean animation) {
        if(mLeftDrawable == null || mRightDrawable == null) {
            return;
        }

        switch (oldState) {
            case SearchBar.STATE_NORMAL:
                mLeftDrawable.setArrow(animation ? ANIMATE_TIME : 0);
                mRightDrawable.setDelete(animation ? ANIMATE_TIME : 0);
                break;
            case SearchBar.STATE_SEARCH:
                mLeftDrawable.setMenu(animation ? ANIMATE_TIME : 0);
                mRightDrawable.setAdd(animation ? ANIMATE_TIME : 0);
                break;
        }
    }

    public class VideoListHelper extends ContentLayout.ContentHelper<VideoInfo> {

        @Override
        public void getPageData(int taskId, int page, int type) {
            //Log.d(TAG, "query page :" + page);
            mUrlBuilder.setPageIndex(page);
            String url = mUrlBuilder.build();
            HsRequest request = new HsRequest();
            request.setMethod(HsClient.METHOD_GET_VIDEO_LIST);
            request.setCallback(new VideoListListener(getContext(), VideoListFragment.this, taskId));
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

        @Override
        public void onItemClick(View view, int position) {
            VideoInfo videoInfo = mHelper.getDataAt(position);
            if(view.getId() == R.id.download_button) {
                Intent intent = new Intent(getActivity(), DownloadService.class);
                intent.setAction(DownloadService.ACTION_START);
                intent.putExtra(DownloadService.KEY_VIDEO_INFO, videoInfo);
                getActivity().startService(intent);
            }
        }
    }

    public class VideoListListener extends HsCallback<VideoListFragment, VideoListParser.Result> {

        private final int mTaskId;

        public VideoListListener(Context context, VideoListFragment fragment, int taskId) {
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

    public class VideoDetailListener extends HsCallback<VideoListFragment, VideoSourceParser.Result> {

        public VideoDetailListener(VideoListFragment fragment) {
            super(fragment);
        }

        @Override
        public void onSuccess(VideoSourceParser.Result result) {
            getFragment().onRequiredDetailSuccess(result);
        }

        @Override
        public void onFailure(Exception e) {

        }

        @Override
        public void onCancel() {

        }
    }

    private boolean checkDoubleClickExit() {
        long time = System.currentTimeMillis();
        if (time - mPressBackTime > BACK_PRESSED_INTERVAL) {
            // It is the last scene
            mPressBackTime = time;
            showTip(R.string.press_twice_exit, LENGTH_SHORT);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onBackPressed() {
        if(TuVideoPlayer.backPress()) {
            return;
        }

        boolean handle = checkDoubleClickExit();
        if(!handle) {
            finish();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if(mClient != null) {
            mClient = null;
        }
    }


}
