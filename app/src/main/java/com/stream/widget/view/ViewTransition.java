package com.stream.widget.view;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.view.View;

import com.hippo.yorozuya.SimpleAnimatorListener;
import com.stream.client.parser.VideoUrlParser;

/**
 * Created by Fuzm on 2017/4/9 0009.
 */

public class ViewTransition {

    private static final long ANIMATE_TIME = 300L;

    private final View[] mViews;
    private int mShownView = -1;

    public ViewTransition(View... views) {
        mViews = views;

        showView(0, false);
    }

    public boolean showView(int shownView, boolean animate) {
        View[] views = mViews;
        int length = views.length;
        if(shownView >= length || shownView < 0) {
            throw new IndexOutOfBoundsException("Only " + length + " view(s) in " +
                    "the ViewTransition, but attempt to show " + shownView);
        }

        if(mShownView != shownView) {
            int oldShownView = mShownView;
            mShownView = shownView;

            if(animate) {
                for(int i=0; i<length; i++) {
                    if(i != shownView && i != oldShownView) {
                        View v = views[i];
                        v.setAlpha(1f);
                        v.setVisibility(View.GONE);
                    }
                }

                startAnimations(views[oldShownView], views[shownView]);
            } else {
                for(int i=0; i<length; i++) {
                    View v = views[i];
                    if(i == shownView) {
                        v.setAlpha(1f);
                        v.setVisibility(View.VISIBLE);
                    } else {
                        v.setAlpha(0f);
                        v.setVisibility(View.GONE);
                    }
                }
            }

            return true;
        } else {
            return false;
        }
    }

    private void startAnimations(final View hiddenView, final View showView) {
        ObjectAnimator oa1 = ObjectAnimator.ofFloat(hiddenView, "alpha", 0f);
        oa1.setDuration(ANIMATE_TIME);
        oa1.addListener(new SimpleAnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                hiddenView.setVisibility(View.GONE);
            }
        });
        oa1.start();

        showView.setVisibility(View.VISIBLE);
        ObjectAnimator oa2 = ObjectAnimator.ofFloat(showView, "alpha", 1f);
        oa2.setDuration(ANIMATE_TIME);
        oa2.addListener(new SimpleAnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                //showView.setVisibility(View.VISIBLE);
            }
        });
        oa2.start();
    }

}
