package com.stream.hstream;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.stream.videoplayerlibrary.tv.TuVideoPlayer;

import java.util.List;

/**
 * Created by Fuzm on 2017/4/27 0027.
 */

public class ListVideoAdapter<E> extends BaseAdapter {

    private Context mContext;
    private List<E> mData;

    public ListVideoAdapter(Context context, List data) {
        mContext = context;
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
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = null;
        if(convertView == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.item_favorite_video_list, parent, false);
        } else {
            view = convertView;
        }

        return view;
    }

    class ViewHolder {

        private TuVideoPlayer mVideoPlayer;

        private ViewHolder(View itemView) {
            mVideoPlayer = (TuVideoPlayer) itemView.findViewById(R.id.list_favorite_video);
        }
    }
}
