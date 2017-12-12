package com.stream.widget;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Build;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.hippo.yorozuya.AnimationUtils;
import com.hippo.yorozuya.MathUtils;
import com.hippo.yorozuya.SimpleAnimatorListener;
import com.stream.dao.Suggestion;
import com.stream.hstream.HStreamDB;
import com.stream.hstream.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Fuzm on 2017/4/8 0008.
 */

public class DrawableSearchBar extends FrameLayout implements TextView.OnEditorActionListener, TextWatcher, View.OnClickListener{

    public static final int STATE_NORMAL = 0;
    public static final int STATE_SEARCH = 1;

    private static final long ANIMATE_TIME = 300L;

    private int mState = STATE_NORMAL;

    private Helper mHelper;
    private DrawableSearchEditText mEditText;
    private ImageView mSearchBack;
    private TextView mSearchAction;
    private LinearLayout mListContainer;

    private List<String> mSuggestionList;
    private ArrayAdapter mSuggestionAdapter;

    public DrawableSearchBar(Context context) {
        super(context);
        init(context);
    }

    public DrawableSearchBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public DrawableSearchBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void init(Context context) {
        //setBackgroundResource(R.drawable.card_white_no_padding_2dp);

        View view = LayoutInflater.from(context).inflate(R.layout.widget_drawable_search_bar, this);
        mEditText = (DrawableSearchEditText) view.findViewById(R.id.drawable_search_edit_text);
        mSearchAction = (TextView) view.findViewById(R.id.search_action);
        mSearchBack = (ImageView) view.findViewById(R.id.search_back);

        mEditText.setOnEditorActionListener(this);
        mEditText.addTextChangedListener(this);
        mEditText.setOnClickListener(this);
//        mEditText.setSearchEditTextListener(new SearchEditText.SearchEditTextListener() {
//            @Override
//            public void onClick() {
//                mHelper.onClickEditText();
//            }
//
//            @Override
//            public void onBackPressed() {
//                if(mState != STATE_NORMAL) {
//                    setState(DrawableSearchBar.STATE_NORMAL);
//                }
//            }
//        });

        mListContainer = (LinearLayout) view.findViewById(R.id.list_container);
        ListView list = (ListView) mListContainer.findViewById(R.id.search_bar_list);

        mSuggestionList = new ArrayList<>();
        mSuggestionAdapter = new ArrayAdapter<>(context, R.layout.item_simple_list, mSuggestionList);
        list.setAdapter(mSuggestionAdapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String suggestion = mSuggestionList.get(MathUtils.clamp(position, 0, mSuggestionList.size() - 1));
                mEditText.setText(suggestion);
                mEditText.setSelection(mEditText.getText().length());
            }
        });
        list.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                String query = mEditText.getText().toString();
                HStreamDB.deleteSuggestionByQuery(query);
                updateSuggestions();
                return false;
            }
        });

        mSearchAction.setOnClickListener(this);
        mSearchBack.setOnClickListener(this);

        setSearchActionState();
        //mEditText.requestFocus();
        //showImeAndSuggestionsList(true);
    }

    public void setHelper(Helper helper) {
        mHelper = helper;
    }

    public float getEditTextTextSize() {
        return mEditText.getTextSize();
    }

    public void setEditTextHint(CharSequence hint) {
        mEditText.setHint(hint);
    }

    public void setText(CharSequence text) {
        mEditText.setText(text);
    }

    public int getState() {
        return mState;
    }

    public void setState(int state) {
        setState(state, true);
    }

    public void setState(int state, boolean animate) {
        if(mState != state) {
            int oldState = mState;
            mState = state;

            switch (oldState) {
                case STATE_NORMAL:
                    mEditText.requestFocus();
                    showImeAndSuggestionsList(animate);

                    break;
                case STATE_SEARCH:
                    hideImeAndSuggestionList(animate);
                    break;
            }
        }
    }

    public void showImeAndSuggestionsList(boolean animation) {
        // Show ime
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(mEditText, 0);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);

        updateSuggestions();

        if(animation) {
            ObjectAnimator animator = ObjectAnimator.ofFloat(this, "progress", 1f);
            animator.setDuration(ANIMATE_TIME);
            animator.setInterpolator(AnimationUtils.FAST_SLOW_INTERPOLATOR);
            animator.addListener(new SimpleAnimatorListener() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mListContainer.setVisibility(VISIBLE);
                }
            });

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                animator.setAutoCancel(true);
            }
            animator.start();
        } else {
            mListContainer.setVisibility(VISIBLE);
        }
    }

    public void hideImeAndSuggestionList(boolean animation) {
        // Hide ime
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(this.getWindowToken(), 0);

        if(animation) {
            ObjectAnimator animator = ObjectAnimator.ofFloat(this, "progress", 0f);
            animator.setDuration(ANIMATE_TIME);
            animator.setInterpolator(AnimationUtils.SLOW_FAST_INTERPOLATOR);
            animator.addListener(new SimpleAnimatorListener() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mListContainer.setVisibility(GONE);
                }
            });

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                animator.setAutoCancel(true);
            }
            animator.start();
        } else {
            mListContainer.setVisibility(GONE);
        }
    }

    public void updateSuggestions() {
        String prefix = mEditText.getText().toString();
        List<Suggestion> suggestionList = HStreamDB.searchSuggestionByPrefit(prefix);
        String[] suggestions = new String[suggestionList.size()];
        for(int i=0; i<suggestionList.size(); i++) {
            suggestions[i] = suggestionList.get(i).getQuery();
        }

        mSuggestionList.clear();
        Collections.addAll(mSuggestionList, suggestions);

        mSuggestionAdapter.notifyDataSetChanged();
    }

    public void applySearch() {
        hideImeAndSuggestionList(true);
        String query = mEditText.getText().toString().trim();
        //if(TextUtils.isEmpty(query)) {
        //    return;
        //}
        Suggestion suggestion = new Suggestion();
        suggestion.setQuery(query);
        suggestion.setDate(System.currentTimeMillis());
        HStreamDB.addSuggestion(suggestion);
        mHelper.onApplySearch(query);
    }

    private void setSearchActionState() {
        String query = mEditText.getText().toString();
        if(!TextUtils.isEmpty(query)) {
            mSearchAction.setClickable(true);
            mSearchAction.setTextColor(getResources().getColor(R.color.cyan_600));
        } else {
            mSearchAction.setClickable(false);
            mSearchAction.setTextColor(getResources().getColor(R.color.grey_500));
        }
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if(v == mEditText) {
            if(actionId == EditorInfo.IME_ACTION_SEARCH) {
                applySearch();
                return true;
            }
        }
        return false;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        updateSuggestions();
        setSearchActionState();
    }

    @Override
    public void onClick(View v) {
        if(v == mSearchBack) {
            mHelper.onClickLeftIcon();
        } else if(v == mSearchAction) {
            applySearch();
        } else if(v == mEditText) {
            mHelper.onClickEditText();
        }
    }

    public interface Helper {
        void onClickLeftIcon();

        void onClickEditText();

        void onApplySearch(String query);
    }
}
