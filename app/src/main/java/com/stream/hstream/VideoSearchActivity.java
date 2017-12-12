package com.stream.hstream;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.stream.enums.GenreEnum;
import com.stream.hstream.fragments.tab.ListFragment;
import com.stream.hstream.fragments.tab.TabFragment;
import com.stream.util.AppHelper;
import com.stream.widget.DrawableSearchBar;
import com.stream.widget.DrawableSearchEditText;

/**
 * Created by Seven-one on 2017/9/28.
 */

public class VideoSearchActivity extends AppCompatActivity {

    private static final String FRAGMENT_SEARCH_TAG = "search_tag";

    private DrawableSearchBar mSearchBar;

    public static Intent newIntent(Context context) {
        return new Intent(context, VideoSearchActivity.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_search_main);

        //init component
        mSearchBar = (DrawableSearchBar) findViewById(R.id.search_bar);
        mSearchBar.setHelper(new DrawableSearchBar.Helper() {
            @Override
            public void onClickLeftIcon() {
                finish();
            }

            @Override
            public void onClickEditText() {
                mSearchBar.setState(DrawableSearchBar.STATE_SEARCH);
            }

            @Override
            public void onApplySearch(String query) {
                applySearch(query);
            }
        });

        mSearchBar.setState(DrawableSearchBar.STATE_SEARCH, true);

        Log.d("VideoSearchActivity", "Search Bar Measured Height: " + mSearchBar.getMeasuredHeight());
        Log.d("VideoSearchActivity", "Search Bar Height: " + mSearchBar.getMeasuredHeight());
    }

    private void applySearch(String query) {
        if (!TextUtils.isEmpty(query)) {
            TabFragment fragment = (TabFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_SEARCH_TAG);
            if (fragment == null) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .add(R.id.search_container, TabFragment.getSearchInstance(query), FRAGMENT_SEARCH_TAG)
                        .commit();
            } else {
                fragment.applySearch(query);
            }

            mSearchBar.setState(DrawableSearchBar.STATE_NORMAL);
        }
    }

    @Override
    public void onBackPressed() {
        int state = mSearchBar.getState();
        if(state != DrawableSearchBar.STATE_NORMAL) {
            mSearchBar.setState(DrawableSearchBar.STATE_NORMAL);
        } else {
            super.onBackPressed();
        }
    }
}
