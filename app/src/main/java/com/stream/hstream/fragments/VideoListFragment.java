package com.stream.hstream.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IntegerRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.flyco.tablayout.CommonTabLayout;
import com.flyco.tablayout.listener.CustomTabEntity;
import com.flyco.tablayout.listener.OnTabSelectListener;
import com.stream.drawable.DrawerArrowDrawable;
import com.stream.enums.GenreEnum;
import com.stream.hstream.R;
import com.stream.hstream.Setting;
import com.stream.hstream.VideoSearchActivity;
import com.stream.hstream.adapter.TitlePageAdapter;
import com.stream.hstream.entity.TabEntity;
import com.stream.hstream.fragments.tab.ListFragment;
import com.stream.hstream.fragments.tab.TabFragment;
import com.stream.scene.SceneFragment;
import com.stream.videoplayerlibrary.tv.TuVideoPlayer;
import com.stream.widget.DrawableSearchEditText;

import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Fuzm on 2017/3/24 0024.
 */

public class VideoListFragment extends SceneFragment {

    private static final String TAG = VideoListFragment.class.getSimpleName();

    private static final int BACK_PRESSED_INTERVAL = 2000;
    private static final String FRAGMENT_NORMAL_TAG = "normal_tag";

    private DrawerArrowDrawable mLeftDrawable;

    private long mPressBackTime = 0;

    private ImageView mRightButton;
    private DrawableSearchEditText mSearchText;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public int getNavCheckedItem() {
        return R.id.nav_home;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_content_main, container, false);

        TabFragment fragment = (TabFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_NORMAL_TAG);
        if (fragment == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.content_main_container, TabFragment.getNormalInstance(), FRAGMENT_NORMAL_TAG)
                    .commit();
        }

        mRightButton = (ImageView) view.findViewById(R.id.right_button);
        mLeftDrawable = new DrawerArrowDrawable(getContext());
        mRightButton.setImageDrawable(mLeftDrawable);
        mRightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleDrawer(Gravity.LEFT);
            }
        });

        //search text
        mSearchText = (DrawableSearchEditText) view.findViewById(R.id.search_text);
        mSearchText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(VideoSearchActivity.newIntent(getActivity()));
            }
        });

        return view;
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

}
