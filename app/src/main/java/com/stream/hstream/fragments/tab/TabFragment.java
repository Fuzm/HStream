package com.stream.hstream.fragments.tab;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.flyco.tablayout.SlidingTabLayout;
import com.stream.dao.GenreInfo;
import com.stream.enums.GenreEnum;
import com.stream.hstream.R;
import com.stream.hstream.adapter.TitlePageAdapter;
import com.stream.hstream.dialog.GenreDialog;
import com.stream.scene.SceneFragment;
import com.stream.util.GenreManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Seven-one on 2017/9/29.
 */

public class TabFragment extends SceneFragment implements View.OnClickListener{

    private static final String TAG = TabFragment.class.getSimpleName();

    public static final int MODE_SEARCH = 1;
    public static final int MODE_NORMAL = 2;

    private int mMode = MODE_NORMAL;
    private String mKeyword;

    private GenreDialog mDialog;

    /**
     * Get search fragment
     */
    public static TabFragment getSearchInstance(String keyword) {
        TabFragment fragment = new TabFragment();
        fragment.mMode = MODE_SEARCH;
        fragment.mKeyword = keyword;
        return fragment;
    }

    /**
     *  Get normal fragment
     */
    public static TabFragment getNormalInstance() {
        TabFragment fragment = new TabFragment();
        fragment.mMode = MODE_NORMAL;
        return fragment;
    }

    //private List<ListFragment> mFragments = new ArrayList<>();
    //private ArrayList<CustomTabEntity> mEntities = new ArrayList<>();
    //private List<String> mTitles = new ArrayList<>();
    private List<TitlePageAdapter.TitlePageInfo> mPageInfoList = new ArrayList<>();

    //tab layout
    private SlidingTabLayout mTabLayout;
    private ViewPager mViewPager;
    private ImageView mAddTabView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initTab();
    }

    /**
     * init tab info
     */
    private void initTab() {
        if(mMode == MODE_NORMAL) {
            List<GenreInfo> list = GenreManager.getUsedGenreInfo();
            for(GenreInfo info : list) {
                mPageInfoList.add(new TitlePageAdapter.TitlePageInfo(info.getGenre_id(), info.getGenre_name(), ListFragment.getInstance(info)));
            }

        } else if(mMode == MODE_SEARCH) {
            for(GenreEnum genreEnum: GenreEnum.listForSearch()) {
                //mFragments.add(ListFragment.getSearchInstance(genreEnum, mKeyword));
                //mEntities.add(new TabEntity(genreEnum.getTitle(), 0, 0));
                //mTitles.add(genreEnum.getTitle());
                mPageInfoList.add(new TitlePageAdapter.TitlePageInfo(genreEnum.getValue(), genreEnum.getTitle(), ListFragment.getSearchInstance(genreEnum, mKeyword)));
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tab_main, container, false);

        //view pager
        mViewPager = (ViewPager) view.findViewById(R.id.tab_page);
        mViewPager.setAdapter(new TitlePageAdapter(getFragmentManager(), mPageInfoList));
        mViewPager.setOffscreenPageLimit(1);
        mViewPager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });

        //tab layout
        mTabLayout = (SlidingTabLayout) view.findViewById(R.id.title_tab);
        mTabLayout.setViewPager(mViewPager);

        if(mPageInfoList.size() == 1) {
            mTabLayout.setVisibility(View.GONE);
        }

        mAddTabView = (ImageView) view.findViewById(R.id.add_tab);
        mAddTabView.setOnClickListener(this);
        showAddButton();

        return view;
    }

    /**
     * update all page info
     */
    private void updateAllPageInfo() {
//        FragmentTransaction transition = getFragmentManager().beginTransaction();
//        for(TitlePageAdapter.TitlePageInfo info : mPageInfoList) {
//            transition.remove(info.getFragment());
//        }
//        transition.commitNow();
//        mPageInfoList.clear();

        FragmentTransaction transition = getFragmentManager().beginTransaction();
        List<GenreInfo> list = GenreManager.getUsedGenreInfo();
        for(TitlePageAdapter.TitlePageInfo pageInfo: mPageInfoList) {
            boolean checked = false;
            for (GenreInfo info : list) {
                if(info.getGenre_id().equals(pageInfo.getKey())) {
                    checked = true;
                    //remove same in list
                    list.remove(info);
                    break;
                }
            }

            if(!checked) {
                //remove when it not in list;
                transition.remove(pageInfo.getFragment());
                mPageInfoList.remove(pageInfo);
            }
        }
        transition.commitNow();

        //genre of adding in list
        if(list.size() > 0) {
            for (GenreInfo info : list) {
                mPageInfoList.add(new TitlePageAdapter.TitlePageInfo(info.getGenre_id(),
                        info.getGenre_name(), ListFragment.getInstance(info)));
            }
        }

        mViewPager.getAdapter().notifyDataSetChanged();
        mTabLayout.notifyDataSetChanged();

        //update current tab when this tab is deleted
        if(mTabLayout.getCurrentTab() >= mPageInfoList.size()) {
            mTabLayout.setCurrentTab(mPageInfoList.size()-1);
        }
    }

    /**
     * Execute search for search mode
     */
    public void applySearch(String keyword) {
        if(mMode != MODE_SEARCH) {
            return;
        }

        for (TitlePageAdapter.TitlePageInfo pageInfo: mPageInfoList) {
            pageInfo.getFragment().applySearch(keyword);
        }
    }

    /**
     * Set add tab button show or hide;
     */
    private void showAddButton() {
        if(mMode == MODE_SEARCH) {
            mAddTabView.setVisibility(View.GONE);
        } else {
            mAddTabView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClick(View v) {

        if(v == mAddTabView) {
            if(mDialog == null) {
                mDialog = new GenreDialog(getContext(), R.style.BottomDialog);
                mDialog.setOnCloseListener(new GenreDialog.OnCloseListener() {
                    @Override
                    public void onClose(GenreDialog dialog) {
                        updateAllPageInfo();
                    }
                });
            }
            mDialog.show();
        }
    }
}
