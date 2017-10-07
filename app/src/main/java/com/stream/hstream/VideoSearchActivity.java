package com.stream.hstream;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.TextView;

import com.stream.enums.GenreEnum;
import com.stream.hstream.fragments.tab.ListFragment;
import com.stream.hstream.fragments.tab.TabFragment;
import com.stream.util.AppHelper;
import com.stream.widget.DrawableSearchEditText;

/**
 * Created by Seven-one on 2017/9/28.
 */

public class VideoSearchActivity extends AppCompatActivity implements View.OnClickListener, TextView.OnEditorActionListener{

    private static final String FRAGMENT_SEARCH_TAG = "search_tag";

    private ImageView mSearchBack;
    private DrawableSearchEditText mSearchEditText;
    private TextView mSearchAction;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_search_main);

        //init component
        mSearchBack = (ImageView) findViewById(R.id.search_back);
        mSearchEditText = (DrawableSearchEditText) findViewById(R.id.search_edit_text);
        mSearchAction = (TextView) findViewById(R.id.search_action);

        //registe click event
        mSearchBack.setOnClickListener(this);
        mSearchAction.setOnClickListener(this);
        mSearchEditText.setOnEditorActionListener(this);
        mSearchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                setSearchActionState(s.toString());
            }
        });

        //other
        setSearchActionState(null);

        mSearchEditText.requestFocus();
        AppHelper.showKeyBord(mSearchEditText, this);
    }

    private void setSearchActionState(String query) {
        if(!TextUtils.isEmpty(query)) {
            mSearchAction.setClickable(true);
            mSearchAction.setTextColor(getResources().getColor(R.color.cyan_600));
        } else {
            mSearchAction.setClickable(false);
            mSearchAction.setTextColor(getResources().getColor(R.color.grey_500));
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.search_back) {
            onBackPressed();
        } else if (v.getId() == R.id.search_action) {
            applySearch();
        }
    }

    private void applySearch() {
        String query = mSearchEditText.getText().toString();
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

            AppHelper.hideKeyBord(mSearchEditText, this);
        }
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if(v == mSearchEditText) {
            if(actionId == EditorInfo.IME_ACTION_SEARCH) {
                applySearch();
                return true;
            }
        }
        return false;
    }
}
