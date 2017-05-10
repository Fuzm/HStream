package com.stream.hstream;

import android.support.annotation.IdRes;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;

import com.stream.scene.Announcer;
import com.stream.scene.SceneFragment;
import com.stream.scene.StageActivity;

public class MainActivity extends StageActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;

    static {
        registerLaunchMode(VideoListFragment.class, SceneFragment.LAUNCH_MODE_SINGLE_TOP);
        registerLaunchMode(VideoDetailFragment.class, SceneFragment.LAUNCH_MODE_STANDARD);
        registerLaunchMode(VideoFavoriteFragment.class, SceneFragment.LAUNCH_MODE_STANDARD);
        registerLaunchMode(VideoFavoriteFragment.class, SceneFragment.LAUNCH_MODE_STANDARD);
        registerLaunchMode(VideoDownloadFragment.class, SceneFragment.LAUNCH_MODE_STANDARD);
    }

    @Override
    protected void onCreate2() {
        setContentView(R.layout.activity_main);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });
//
//        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
//        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
//                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
//        drawer.setDrawerListener(toggle);
//        toggle.syncState();

        mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public int getContainerViewId() {
        return R.id.fragment_container;
    }

    @Override
    public Announcer getLaunchAnnouncer() {
        return new Announcer(VideoListFragment.class);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void setNavCheckedItem(@IdRes int resId) {
        if(mNavigationView != null) {
            if(resId != 0) {
                mNavigationView.setCheckedItem(resId);
            } else {
                mNavigationView.setCheckedItem(R.id.nav_stub);
            }
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if(id == R.id.nav_home) {
            startScene(new Announcer(VideoListFragment.class));
        } else if (id == R.id.nav_favorite) {
            startScene(new Announcer(VideoFavoriteFragment.class));
            // Handle the camera action
        } else if (id == R.id.nav_download) {
            startScene(new Announcer(VideoDownloadFragment.class));
        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void setDrawerLockMode(int lockMode, int edgeGravity) {
        mDrawerLayout.setDrawerLockMode(lockMode, edgeGravity);
    }

    public void toggleDrawer(int edgeGravity) {
        mDrawerLayout.openDrawer(edgeGravity);
    }
}
