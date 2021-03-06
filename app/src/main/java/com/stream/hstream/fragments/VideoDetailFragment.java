package com.stream.hstream.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.stream.client.HsClient;
import com.stream.client.HsRequest;
import com.stream.client.data.VideoSourceInfo;
import com.stream.client.data.VideoInfo;
import com.stream.client.parser.VideoSourceParser;
import com.stream.hstream.HStreamApplication;
import com.stream.client.HsCallback;
import com.stream.hstream.R;
import com.stream.scene.SceneFragment;
import com.stream.widget.LoadImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Fuzm on 2017/3/25 0025.
 */

public class VideoDetailFragment extends SceneFragment implements AdapterView.OnItemClickListener{

    public static final String TAG = VideoDetailFragment.class.getSimpleName();

    public static final String KEY_DETAIL_INFO = "video_detail_info";

    private VideoInfo mVideoInfo;
    private HsClient mClient;
    private LoadImageView mThumb;
    private TextView mTitleText;
    private ListView mListView;
    private SimpleAdapter mSimpleAdapter;
    private List<Map<String, Object>> mData = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Context context = getContext();
        mClient = HStreamApplication.getHsClient(context);
        handleArgs(getArguments());
    }

    public void handleArgs(Bundle args) {
        if (args == null) {
            return;
        }

        mVideoInfo = args.getParcelable(KEY_DETAIL_INFO);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detail_main, container, false);

        mThumb = (LoadImageView) view.findViewById(R.id.detail_thumb);
        mThumb.load(mVideoInfo.token, mVideoInfo.thumb);

        mTitleText = (TextView) view.findViewById(R.id.detial_title);
        mTitleText.setText(mVideoInfo.title);

        mListView = (ListView) view.findViewById(R.id.source_list);
        mSimpleAdapter = new SimpleAdapter(
                getContext(),
                mData,
                R.layout.item_detail_list,
                new String[]{"name"},
                new int[]{R.id.name});
        mListView.setAdapter(mSimpleAdapter);

        mListView.setOnItemClickListener(this);

        requiredDetailInfo();
        return view;
    }

    private void requiredDetailInfo() {
        HsRequest request = new HsRequest();
        request.setMethod(HsClient.METHOD_GET_VIDEO_DETAIL);
        request.setCallback(new VideoDetailFragment.VideoDetailListener(this));
        request.setArgs(mVideoInfo.url);
        mClient.execute(request);
    }

    private void onRequiredDetailSuccess(VideoSourceParser.Result result) {
        mData.clear();
        for(VideoSourceInfo info : result.mVideoSourceInfoList) {
            mData.add(info.parseMap());
        }
        mSimpleAdapter.notifyDataSetChanged();
    }

//    private void requiredVideoUrl(String infoUrl) {
//        HsRequest request = new HsRequest();
//        request.setMethod(HsClient.METHOD_GET_VIDEO_URL);
//        request.setCallback(new VideoDetailFragment.VideoUrlListener());
//        request.setArgs(infoUrl);
//        mClient.execute(request);
//    }

//    private void onRequiredUrlSuccess(VideoUrlParser.Result result) {
//        Intent intent = new Intent(getActivity(), VideoPlayActivity.class);
//        intent.putExtra(VideoPlayActivity.KEY_VIDEO_TITLE, mVideoInfo.title);
//        intent.putExtra(VideoPlayActivity.KEY_VIDEO_THUMB, mVideoInfo.thumb);
//        intent.putExtra(VideoPlayActivity.KEY_VIDEO_URL, result.url);
//        startActivity(intent);
//    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Map info = (Map) mData.get(position);
        //requiredVideoUrl((String) info.get("url"));

//        Intent intent = new Intent(getActivity(), VideoPlayActivity.class);
//        intent.putExtra(VideoPlayActivity.KEY_VIDEO_TITLE, mVideoInfo.title);
//        intent.putExtra(VideoPlayActivity.KEY_VIDEO_THUMB, mVideoInfo.thumb);
//        intent.putExtra(VideoPlayActivity.KEY_VIDEO_URL, (String) info.get("videoUrl"));
//        startActivity(intent);
    }

    public class VideoDetailListener extends HsCallback<VideoDetailFragment, VideoSourceParser.Result> {

        public VideoDetailListener(VideoDetailFragment fragment) {
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

//    public class VideoUrlListener extends HsCallback<VideoDetailFragment, VideoUrlParser.Result> {
//
//        @Override
//        public void onSuccess(VideoUrlParser.Result result) {
//            VideoDetailFragment.this.onRequiredUrlSuccess(result);
//        }
//
//        @Override
//        public void onFailure(Exception e) {
//
//        }
//
//        @Override
//        public void onCancel() {
//
//        }
//    }
}
