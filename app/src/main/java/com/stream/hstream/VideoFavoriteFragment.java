package com.stream.hstream;

import android.content.Context;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.stream.data.StreamDataBase;
import com.stream.data.model.Favorite;
import com.stream.hstream.adapter.SimpleHolder;
import com.stream.scene.SceneFragment;
import com.stream.util.LoadImageHelper;
import com.stream.videoplayerlibrary.common.VideoUtils;
import com.stream.videoplayerlibrary.tv.TuVideoPlayer;

import java.util.List;

/**
 * Created by Fuzm on 2017/4/27 0027.
 */

public class VideoFavoriteFragment extends SceneFragment {

    private ListView mListView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.fragment_favorite_main, container, false);

        mListView = (ListView) view.findViewById(R.id.list_favorites);

        List<Favorite> data = StreamDataBase.getInstance(getContext()).queryFavorites();
        mListView.setAdapter(new FavoriteListAdapter(LayoutInflater.from(getContext()), getContext(), data));

        return view;
    }

    private class FavoriteListAdapter extends com.stream.hstream.adapter.SimpleAdapter<VideoHolder> {

        private final LayoutInflater mInflater;
        private final Context mContext;
        private final List<Favorite> mData;

        public FavoriteListAdapter(@NonNull LayoutInflater inflater, Context context, List<Favorite> data) {
            mContext = context;
            mInflater = inflater;
            mData = data;
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
            holder.mVideoPlayer.setUp(favorite.getVideoPath(), favorite.getTitle(), TuVideoPlayer.MODE_NORMAL_SCREEN);

            if(favorite.getImagePath().startsWith("http://") || favorite.getImagePath().startsWith("https://")) {
                LoadImageHelper.with(mContext)
                        .load(favorite.getImagePath(), favorite.getImagePath())
                        .into(holder.mVideoPlayer.getThumb());
            } else {
                holder.mVideoPlayer.setThumb(VideoUtils.getDrawableFromPath(mContext, favorite.getImagePath()));
            }

        }
    }

    private class VideoHolder extends SimpleHolder {

        private TuVideoPlayer mVideoPlayer;

        public VideoHolder(View itemView) {
            super(itemView);

            mVideoPlayer = (TuVideoPlayer) itemView.findViewById(R.id.list_favorite_video);
        }
    }
}
