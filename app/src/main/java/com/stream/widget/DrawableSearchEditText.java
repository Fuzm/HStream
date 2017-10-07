package com.stream.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatTextView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

import com.stream.hstream.R;
import com.stream.util.DrawableManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Fuzm on 2017/4/9 0009.
 */

public class DrawableSearchEditText extends AppCompatEditText implements TextWatcher, View.OnTouchListener{

    private OnTouchListener mTouchListener;

    private Drawable mLeftDrawable;
    private Drawable mRightDrawable;

    public DrawableSearchEditText(Context context) {
        this(context, null);
    }

    public DrawableSearchEditText(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DrawableSearchEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        initView();
    }

    private void initView() {
        setBackgroundResource(R.drawable.ic_round_edittext_light);

        mLeftDrawable = DrawableManager.getDrawable(getContext(), R.drawable.ic_search_black_24dp);
        mLeftDrawable.setBounds(0, 0, mLeftDrawable.getMinimumWidth(), mLeftDrawable.getMinimumHeight());
        mRightDrawable = DrawableManager.getDrawable(getContext(), R.drawable.ic_search_clear_black_24dp);
        mRightDrawable.setBounds(0, 0, mRightDrawable.getMinimumWidth(), mRightDrawable.getMinimumHeight());

        setLeftAndRightDrawable();
        addTextChangedListener(this);
        super.setOnTouchListener(this);
    }

    private void setLeftAndRightDrawable() {
        if(!TextUtils.isEmpty(getText().toString().trim())) {
            setCompoundDrawables(mLeftDrawable, null, mRightDrawable, null);
        } else {
            setCompoundDrawables(mLeftDrawable, null, null, null);
        }
        setCompoundDrawablePadding(10);
    }

    @Override
    public void setOnTouchListener(OnTouchListener l) {
        mTouchListener = l;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        setLeftAndRightDrawable();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        if(mTouchListener != null) {
            mTouchListener.onTouch(v, event);
        }

        if(v instanceof DrawableSearchEditText) {
            DrawableSearchEditText editText = (DrawableSearchEditText) v;
            Drawable rightDrawable = editText.getCompoundDrawables()[2];
            if (rightDrawable != null) {

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    int drawablePosx = editText.getWidth() - editText.getPaddingRight() - rightDrawable.getIntrinsicWidth();
                    if (event.getX() > drawablePosx) {
                        editText.setText("");
                        setLeftAndRightDrawable();
                    }
                }
            }
        }

        return false;
    }
}
