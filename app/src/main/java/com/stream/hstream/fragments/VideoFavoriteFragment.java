package com.stream.hstream.fragments;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.daimajia.swipe.SwipeLayout;
import com.stream.dao.Favorite;
import com.stream.hstream.HStreamDB;
import com.stream.hstream.R;
import com.stream.hstream.adapter.SimpleAdapter;
import com.stream.hstream.adapter.SimpleHolder;
import com.stream.scene.ToolBarFragment;
import com.stream.util.DrawableManager;
import com.stream.util.LoadImageHelper;
import com.stream.videoplayerlibrary.common.VideoUtils;
import com.stream.videoplayerlibrary.tv.TuIjkMediaPlayerManager;
import com.stream.videoplayerlibrary.tv.TuVideoPlayer;
import com.stream.widget.view.ViewTransition;

import java.util.List;

/**
 * Created by Fuzm on 2017/4/27 0027.
 */

public class VideoFavoriteFragment extends ToolBarFragment {

    private ListView mListView;
    private FavoriteListAdapter mAdapter;
    private ViewTransition mViewTransition;
    private List<Favorite> mData;

    @Nullable
    @Override
    public View onCreateView2(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.fragment_favorite_main, container, false);

        mListView = (ListView) view.findViewById(R.id.list_favorites);
        TextView tip = (TextView) view.findViewById(R.id.tip);

        mData = HStreamDB.queryAllFavorite();
        mAdapter = new FavoriteListAdapter(LayoutInflater.from(getContext()), getContext());
        mListView.setAdapter(mAdapter);

        Drawable drawable = DrawableManager.getDrawable(getContext(), R.drawable.ic_big_favorite);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        tip.setCompoundDrawables(null, drawable, null, null);

        mViewTransition = new ViewTransition(mListView, tip);
        updateView();

        return view;
    }

    public void updateView() {
        if(mData != null && mData.size() > 0) {
            mViewTransition.showView(0, true);
        } else {
            mViewTransition.showView(1, true);
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setTitle(getResources().getString(R.string.menu_favorite));
        setNavigationIcon(R.drawable.v_arrow_left_dark_x24);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mListView = null;
        mViewTransition = null;
        mData = null;

        TuIjkMediaPlayerManager.releaseManager();
    }

    @Override
    public int getNavCheckedItem() {
        return R.id.nav_favorite;
    }

    @Override
    public void onNavigationClick() {
        onBackPressed();
    }

    private class FavoriteListAdapter extends com.stream.hstream.adapter.SimpleAdapter<VideoHolder> {

        private final LayoutInflater mInflater;
        private final Context mContext;

        public FavoriteListAdapter(@NonNull LayoutInflater inflater, Context context) {
            mContext = context;
            mInflater = inflater;
        }

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public Object getItem(int position) {
            return mData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public VideoHolder onCreateViewHolder(ViewGroup parent) {
            return new VideoHolder(mInflater.inflate(R.layout.item_favorite_video_list, parent, false));
        }

        @Override
        public void onBindViewHolder(VideoHolder holder, int position) {
            Favorite favorite = mData.get(position);
            holder.mVideoPlayer.setUp(favorite.getVideoUrl(), favorite.getTitle(), TuVideoPlayer.MODE_NORMAL_SCREEN);

            if(favorite.getThumb().startsWith("http://") || favorite.getThumb().startsWith("https://")) {
                holder.setThumb(mContext, favorite.getToken(), favorite.getThumb());
            } else {
                holder.mVideoPlayer.setThumb(VideoUtils.getDrawableFromPath(mContext, favorite.getThumb()));
            }
        }
    }

    private class VideoHolder extends SimpleHolder implements View.OnClickListener{

        private View mItemView;
        private TuVideoPlayer mVideoPlayer;
        private LoadImageHelper mImageHelper;
        private Button mDelete;

        public VideoHolder(View itemView) {
            super(itemView);
            mItemView = itemView;

            mVideoPlayer = (TuVideoPlayer) itemView.findViewById(R.id.list_favorite_video);
            mDelete = (Button) itemView.findViewById(R.id.delete);
            mDelete.setOnClickListener(this);
        }

        public void setThumb(Context context, String token, String thumb) {
            mImageHelper = LoadImageHelper.with(context)
                    .load(token, thumb)
                    .into(mVideoPlayer.getThumb());
        }

        @Override
        public void onClick(View v) {
            int index = mListView.getPositionForView(mItemView);
            if(index == -1) {
                return;
            }

            if(v == mDelete) {
                Favorite favorite = mData.get(index);
                HStreamDB.removeFavorite(favorite.getToken());

                mVideoPlayer.release();
                mData.remove(index);
                mAdapter.notifyDataSetChanged();
            }
        }
    }
}
