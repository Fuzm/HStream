package com.stream.drawable;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import com.stream.hstream.R;

/**
 * Created by Fuzm on 2017/4/11 0011.
 */

public class PlayPauseDrawable extends Drawable {

    private final Paint mPaint = new Paint();
    private final Path mPath = new Path();

    private float mSize;

    public PlayPauseDrawable(Context context) {
        Resources resources = context.getResources();

        mPaint.setAntiAlias(true);
        mPaint.setColor(resources.getColor(R.color.primary_drawable_light));

        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);

        mSize = resources.getDimensionPixelSize(R.dimen.ppd_drawable_size);
    }

    @Override
    public void draw(Canvas canvas) {
        Rect bounds = getBounds();

        float halfSize = mSize / 2;
        float middleSize = (float) Math.sqrt(Math.pow(halfSize, 2) / 5);
        float distanceSize = middleSize * 2;

        mPath.rewind();
        mPath.moveTo(distanceSize, 0);
        mPath.lineTo(-middleSize, -halfSize);
        mPath.lineTo(-middleSize, halfSize);
        mPath.close();

        canvas.save();
        canvas.translate(bounds.centerX(), bounds.centerY());
        canvas.drawPath(mPath, mPaint);
        canvas.restore();
    }

    @Override
    public void setAlpha(int alpha) {

    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {

    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }
}
