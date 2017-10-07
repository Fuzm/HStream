package com.stream.hstream.entity;

import com.flyco.tablayout.listener.CustomTabEntity;

/**
 * Created by Seven-one on 2017/9/26.
 */

public class TabEntity implements CustomTabEntity {

    private String tabTile;
    private int selectedIcon;
    private int unselectedIcon;

    public TabEntity(String title, int selectedIcon, int unselectedIcon) {
        this.tabTile = title;
        this.selectedIcon = selectedIcon;
        this.unselectedIcon = unselectedIcon;
    }

    @Override
    public String getTabTitle() {
        return tabTile;
    }

    @Override
    public int getTabSelectedIcon() {
        return selectedIcon;
    }

    @Override
    public int getTabUnselectedIcon() {
        return unselectedIcon;
    }
}
