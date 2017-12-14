package com.stream.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.stream.hstream.R;

import java.math.BigDecimal;
import java.security.InvalidParameterException;

/**
 * Created by Seven-one on 2017/12/13 0013.
 */

public class NumberAjustBar extends FrameLayout implements View.OnClickListener {

    private ImageView mSubImageView;
    private ImageView mAddImageView;
    private TextView mNumberTextView;

    private float mDistance = 1;
    private float mCurrentValue = 0;

    private NumberAjustListener mListener;

    public NumberAjustBar(@NonNull Context context) {
        super(context);
        init(context);
    }

    public NumberAjustBar(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public NumberAjustBar(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.widget_number_ajust_bar, this);

        mSubImageView = (ImageView) view.findViewById(R.id.action_sub);
        mAddImageView = (ImageView) view.findViewById(R.id.action_add);
        mNumberTextView = (TextView) view.findViewById(R.id.number_text);

        //init text view
        mNumberTextView.setText("0");
        mSubImageView.setOnClickListener(this);
        mAddImageView.setOnClickListener(this);
    }

    /**
     * set distance for component
     * @param distance distance >= 0
     */
    public void setDistance(float distance) {
        if(distance > Float.MAX_VALUE || distance < Float.MIN_VALUE) {
            throw new InvalidParameterException("distance invalid");
        }

        mDistance = Math.abs(distance);
    }

    /**
     * get distance
     * @return
     */
    public float getDistance() {
        return mDistance;
    }

    /**
     * set number ajust listener
     * @param listener
     */
    public void setNumberAjustListener(NumberAjustListener listener) {
        mListener = listener;
    }

    @Override
    public void onClick(View v) {
        float value = 0;
        if(v == mSubImageView) {
            value = sub(mCurrentValue, mDistance);
        } else if(v == mAddImageView) {
            value = add(mCurrentValue, mDistance);
        }

        setValue(value);
    }

    private float sub(float value1, float value2) {
        BigDecimal v1 = new BigDecimal(value1);
        BigDecimal v2 = new BigDecimal(value2);

        return v1.subtract(v2).floatValue();
    }

    private float add(float value1, float value2) {
        BigDecimal v1 = new BigDecimal(value1);
        BigDecimal v2 = new BigDecimal(value2);

        return v1.add(v2).floatValue();
    }

    /**
     * set value for textview
     * @param value
     */
    private void setValue(float value) {
        float oldValue = mCurrentValue;
        mCurrentValue = value;
        mNumberTextView.setText(String.valueOf(value));

        if(mListener != null) {
            mListener.ajust(value, oldValue);
        }
    }

    public interface NumberAjustListener {

        void ajust(float newValue, float oldValue);
    }

}
