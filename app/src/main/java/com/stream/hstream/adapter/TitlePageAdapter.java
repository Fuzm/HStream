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

    private List<TitlePageInfo> mPageInfoList;

    public TitlePageAdapter(FragmentManager fm, List<TitlePageInfo> pageInfoList) {
        super(fm);
        mPageInfoList = pageInfoList;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mPageInfoList.get(position).mTitle;
    }

    @Override
    public Fragment getItem(int position) {
        return mPageInfoList.get(position).mFragment;
    }

    @Override
    public int getCount() {
        return mPageInfoList.size();
    }

    public static class TitlePageInfo {

        private String mKey;
        private String mTitle;
        private ListFragment mFragment;

        public TitlePageInfo(String mKey, String title, ListFragment fragment) {
            mTitle = title;
            mFragment = fragment;
        }

        public String getKey() {
            return mKey;
        }

        public String getTitle() {
            return mTitle;
        }

        public ListFragment getFragment() {
            return mFragment;
        }

        public void clear() {
            if(mFragment != null) {
                mFragment.finish();
                mFragment = null;
            }
        }
    }
}
