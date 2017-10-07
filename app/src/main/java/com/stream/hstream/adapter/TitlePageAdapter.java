package com.stream.hstream.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.stream.hstream.fragments.tab.ListFragment;

import java.util.List;

/**
 * Created by Seven-one on 2017/9/26.
 */

public class TitlePageAdapter extends FragmentPagerAdapter{

    private List<ListFragment> mFragments;

    public TitlePageAdapter(FragmentManager fm, List<ListFragment> fragments) {
        super(fm);
        mFragments = fragments;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return super.getPageTitle(position);
    }

    @Override
    public Fragment getItem(int position) {
        return mFragments.get(position);
    }

    @Override
    public int getCount() {
        return mFragments.size();
    }
}
