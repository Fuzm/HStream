package com.stream.widget;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.Editable;
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
import com.stream.data.StreamDataBase;
import com.stream.hstream.R;
import com.stream.widget.view.ViewTransition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Fuzm on 2017/4/8 0008.
 */

public class SearchBar extends FrameLayout implements View.OnClickListener,
        TextView.OnEditorActionListener, TextWatcher{

    public static final int STATE_NORMAL = 0;
    public static final int STATE_SEARCH = 1;

    private static final long ANIMATE_TIME = 300L;

    private int mState = STATE_NORMAL;

    private Helper mHelper;
    private ImageView mSearchMenu;
    private ImageView mSearchAction;
    private TextView mTitleView;
    private SearchEditText mEditText;
    private LinearLayout mListContainer;

    private ViewTransition mViewTransition;
    private OnStateChangeListener mOnStateChangeListener;

    private List<String> mSuggestionList;
    private ArrayAdapter mSuggestionAdapter;

    private StreamDataBase mDataBase;

    public SearchBar(Context context) {
        super(context);
        init(context);
    }

    public SearchBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SearchBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void init(Context context) {
        setBackgroundResource(R.drawable.card_white_no_padding_2dp);

        mDataBase = StreamDataBase.getInstance(context);

        View view = LayoutInflater.from(context).inflate(R.layout.widget_search_bar, this);
        mSearchMenu = (ImageView) view.findViewById(R.id.search_menu);
        mSearchAction = (ImageView) view.findViewById(R.id.search_action);
        mTitleView = (TextView) view.findViewById(R.id.search_title);
        mEditText = (SearchEditText) view.findViewById(R.id.search_edit_text);

        mViewTransition = new ViewTransition(mTitleView, mEditText);

        mSearchMenu.setOnClickListener(this);
        mTitleView.setOnClickListener(this);
        mSearchAction.setOnClickListener(this);
        mEditText.setOnEditorActionListener(this);
        mEditText.addTextChangedListener(this);

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
                String suggestion = mEditText.getText().toString();
                mDataBase.deleteSuggestion(suggestion);
                updateSuggestions();
                return false;
            }
        });
    }

    public void setHelper(Helper helper) {
        mHelper = helper;
    }

    public void setLeftDrawable(Drawable drawable) {
        mSearchMenu.setImageDrawable(drawable);
    }

    public void setRightDrawable(Drawable drawable) {
        mSearchAction.setImageDrawable(drawable);
    }

    public void setTitle(String title) {
        mTitleView.setText(title);
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

    public void setOnStateChangeListener(OnStateChangeListener listener) {
        mOnStateChangeListener = listener;
    }

    public int getState() {
        return mState;
    }

    public void setState(int state, boolean animate) {
        if(mState != state) {
            int oldState = mState;
            mState = state;

            switch (oldState) {
                case STATE_NORMAL:
                    mViewTransition.showView(1, animate);
                    mEditText.requestFocus();

                    showImeAndSuggestionsList(animate);

                    if(mOnStateChangeListener != null) {
                        mOnStateChangeListener.onStateChange(this, state, oldState, animate);
                    }

                    break;
                case STATE_SEARCH:
                    hideImeAndSuggestionList(animate);

                    mViewTransition.showView(0, animate);
                    if(mOnStateChangeListener != null) {
                        mOnStateChangeListener.onStateChange(this, state, oldState, animate);
                    }
                    break;
            }
        }
    }

    public void showImeAndSuggestionsList(boolean animation) {
        // Show ime
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(mEditText, 0);

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
        String[] suggestions = mDataBase.querySuggestions(prefix);
        mSuggestionList.clear();
        Collections.addAll(mSuggestionList, suggestions);

        mSuggestionAdapter.notifyDataSetChanged();
    }


    @Override
    public void onClick(View v) {
        if(v == mSearchMenu) {
            mHelper.onClickLeftIcon();
        } else if(v == mTitleView) {
            mHelper.onClickTitle();
        } else if(v == mSearchAction) {
            mHelper.onClickRightIcon();
        }
    }

    private void applySearch() {
        String query = mEditText.getText().toString().trim();
        //if(TextUtils.isEmpty(query)) {
        //    return;
        //}
        mDataBase.addSuggestion(query);
        mHelper.onApplySearch(query);
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
    }

    public interface Helper {
        void onClickLeftIcon();
        void onClickRightIcon();
        void onClickTitle();
        void onApplySearch(String query);
    }

    public interface OnStateChangeListener {
        void onStateChange(SearchBar searchBar, int newState, int oldState, boolean animation);
    }
}
