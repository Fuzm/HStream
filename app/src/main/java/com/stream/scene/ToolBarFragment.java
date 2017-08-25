package com.stream.scene;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.health.PackageHealthStats;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.stream.hstream.R;

/**
 * Created by Fuzm on 2017/5/9 0009.
 */

public class ToolBarFragment extends SceneFragment{

    private Toolbar mToolbar;
    private CharSequence mTempTitle;

    @Nullable
    @Override
    public final View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_toolbar, container, false);
        FrameLayout contentPanel  = (FrameLayout) view.findViewById(R.id.content_panel);

        View contentView = onCreateView2(inflater, container, savedInstanceState);
        if(contentView != null) {
            mToolbar  = (Toolbar) view.findViewById(R.id.toolbar);
            contentPanel.addView(contentView);
            return view;
        } else {
            return null;
        }
    }

    public View onCreateView2(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return null;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (mToolbar != null) {
            if (mTempTitle != null) {
                mToolbar.setTitle(mTempTitle);
                mTempTitle = null;
            }

            int menuResId = getMenuResId();
            if (menuResId != 0) {
                mToolbar.inflateMenu(menuResId);
                mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        return ToolBarFragment.this.onMenuItemClick(item);
                    }
                });
                onMenuCreated(mToolbar.getMenu());
            }
            mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onNavigationClick();
                }
            });
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mToolbar = null;
    }

    public int getMenuResId() {
        return 0;
    }

    public boolean onMenuItemClick(MenuItem item) {
        return false;
    }

    public void onMenuCreated(Menu menu) {
    }

    public void onNavigationClick(){
    }

    public void setNavigationIcon(@DrawableRes int resId) {
        if(mToolbar != null) {
            mToolbar.setNavigationIcon(resId);
        }
    }

    public void setNavigationIcon(@Nullable Drawable icon) {
        if(mToolbar != null) {
            mToolbar.setNavigationIcon(icon);
        }
    }

    public void setTitle(CharSequence title) {
        if(mToolbar != null) {
            mToolbar.setTitle(title);
        } else {
            mTempTitle = title;
        }
    }

}
