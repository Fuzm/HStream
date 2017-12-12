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

    private DrawerArrowDrawable mLeftDrawable;

    private long mPressBackTime = 0;

    //tab layout
    private CommonTabLayout mTabLayout;
    private ViewPager mViewPager;

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

        List<ListFragment> fragments = new ArrayList<>();
        ArrayList<CustomTabEntity> entities = new ArrayList<>();
        for(GenreEnum genreEnum: GenreEnum.listForGener()) {
            fragments.add(ListFragment.getInstance(genreEnum));
            entities.add(new TabEntity(genreEnum.getTitle(), 0, 0));
        }

        //view pager
        mViewPager = (ViewPager) view.findViewById(R.id.tab_page);
        mViewPager.setAdapter(new TitlePageAdapter(getChildFragmentManager(), fragments));
        mViewPager.setOffscreenPageLimit(1);
        mViewPager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });

        //tab layout
        mTabLayout = (CommonTabLayout) view.findViewById(R.id.title_tab);
        mTabLayout.setTabData(entities);
        //when only one tab, do not show tablayout
        if(entities != null && entities.size() == 1) {
            mTabLayout.setVisibility(View.GONE);
        }
        mTabLayout.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelect(int position) {
                mViewPager.setCurrentItem(position);
            }

            @Override
            public void onTabReselect(int position) {

            }
        });
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mTabLayout.setCurrentTab(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

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
