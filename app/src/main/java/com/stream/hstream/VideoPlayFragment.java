package com.stream.hstream;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.stream.scene.SceneFragment;
import com.stream.widget.StreamVideoView;

/**
 * Created by Fuzm on 2017/4/16 0016.
 */

public class VideoPlayFragment extends SceneFragment {

    private String mVideoTitle;
    private String mVideoThumb;
    private String mVideoUrl;

    private StreamVideoView mStreamVideoView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_play_main, container, false);

        mStreamVideoView = (StreamVideoView) view.findViewById(R.id.fragment_video_view);
        mStreamVideoView.setTitle(mVideoTitle);
        mStreamVideoView.loadBackground(mVideoTitle, mVideoThumb);
        mStreamVideoView.setVideoPath(mVideoUrl);
        mStreamVideoView.start();

        return view;
    }
}
