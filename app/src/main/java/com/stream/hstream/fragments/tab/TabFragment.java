package com.stream.hstream.fragments.tab;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.flyco.tablayout.CommonTabLayout;
import com.flyco.tablayout.listener.CustomTabEntity;
import com.flyco.tablayout.listener.OnTabSelectListener;
import com.stream.enums.GenreEnum;
import com.stream.hstream.R;
import com.stream.hstream.adapter.TitlePageAdapter;
import com.stream.hstream.entity.TabEntity;
import com.stream.scene.SceneFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Seven-one on 2017/9/29.
 */

public class TabFragment extends SceneFragment {

    private static final String TAG = TabFragment.class.getSimpleName();

    public static final int MODE_SEARCH = 1;
    public static final int MODE_NORMAL = 2;

    private int mMode = MODE_NORMAL;
    private String mKeyword;

    //get search fragment
    public static TabFragment getSearchInstance(String keyword) {
        TabFragment fragment = new TabFragment();
        fragment.mMode = MODE_SEARCH;
        fragment.mKeyword = keyword;
        return fragment;
    }

    //get normal fragment
    public static TabFragment getNormalInstance() {
        TabFragment fragment = new TabFragment();
        fragment.mMode = MODE_NORMAL;
        return fragment;
    }

    private List<ListFragment> mFragments = new ArrayList<>();
    private ArrayList<CustomTabEntity> mEntities = new ArrayList<>();

    //tab layout
    private CommonTabLayout mTabLayout;
    private ViewPager mViewPager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(mMode == MODE_NORMAL) {
            for(GenreEnum genreEnum: GenreEnum.listForGener()) {
                mFragments.add(ListFragment.getInstance(genreEnum));
                mEntities.add(new TabEntity(genreEnum.getTitle(), 0, 0));
            }
        } else if(mMode == MODE_SEARCH) {
            for(GenreEnum genreEnum: GenreEnum.listForSearch()) {
                mFragments.add(ListFragment.getSearchInstance(genreEnum, mKeyword));
                mEntities.add(new TabEntity(genreEnum.getTitle(), 0, 0));
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tab_main, container, false);

        //view pager
        mViewPager = (ViewPager) view.findViewById(R.id.tab_page);
        mViewPager.setAdapter(new TitlePageAdapter(getFragmentManager(), mFragments));
        mViewPager.setOffscreenPageLimit(1);
        mViewPager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });

        //tab layout
        mTabLayout = (CommonTabLayout) view.findViewById(R.id.title_tab);
        mTabLayout.setTabData(mEntities);
        //when only one tab, do not show tablayout
        if(mEntities != null && mEntities.size() == 1) {
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

        return view;
    }

    //execute search for search mode
    public void applySearch(String keyword) {
        if(mMode != MODE_SEARCH) {
            return;
        }

        for (ListFragment fragment: mFragments) {
            fragment.applySearch(keyword);
        }
    }
}
