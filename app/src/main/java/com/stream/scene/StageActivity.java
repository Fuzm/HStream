package com.stream.scene;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;

import com.stream.hstream.R;

import junit.framework.Assert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Fuzm on 2017/3/24 0024.
 */

public abstract class StageActivity extends AppCompatActivity {

    public static final String TAG = StageActivity.class.getSimpleName();

    public static final String ACTION_START_SCENE = "start_scene";
    public static final String KEY_SCENE_NAME = "stage_activity_scene_name";
    public static final String KEY_SCENE_ARGS = "stage_activity_scene_args";

    private final ArrayList<String> mSceneTagList = new ArrayList<>();
    private final AtomicInteger mIdGenerator = new AtomicInteger();


    private static final Map<Class<?>, Integer> sLaunchModeMap = new HashMap<>();

    public static void registerLaunchMode(Class<?> clazz, @SceneFragment.LaunchMode int launchMode) {
        if (launchMode != SceneFragment.LAUNCH_MODE_STANDARD &&
                launchMode != SceneFragment.LAUNCH_MODE_SINGLE_TOP &&
                launchMode != SceneFragment.LAUNCH_MODE_SINGLE_TASK) {
            throw new IllegalStateException("Invalid launch mode: " + launchMode);
        }
        sLaunchModeMap.put(clazz, launchMode);
    }

    public int getSceneLaunchMode(Class<?> clazz) {
        Integer integer = sLaunchModeMap.get(clazz);
        if (integer == null) {
            throw new RuntimeException("Not register " + clazz.getName());
        } else {
            return integer;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        onCreate2();

        Intent intent = getIntent();
        if (savedInstanceState == null) {
            if (intent != null) {
                String action = intent.getAction();
                if (Intent.ACTION_MAIN.equals(action)) {
                    Announcer announcer = getLaunchAnnouncer();
                    if (announcer != null) {
                        startScene(announcer);
                        return;
                    }
                } else if(ACTION_START_SCENE.equals(action)) {
                    startSceneFromIntent(intent);
                }
            }
        }
    }

    protected abstract void onCreate2();

    public abstract int getContainerViewId();

    public abstract Announcer getLaunchAnnouncer();

    private SceneFragment newSceneInstance(Class<?> clazz) {
        try {
            return (SceneFragment) clazz.newInstance();
        } catch (InstantiationException e) {
            throw new IllegalStateException("Can't instance " + clazz.getName(), e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("The constructor of " +
                    clazz.getName() + " is not visible", e);
        } catch (ClassCastException e) {
            throw new IllegalStateException(clazz.getName() + " can not cast to scene", e);
        }
    }

    protected boolean startSceneFromIntent(Intent intent) {
        String sceneName = intent.getStringExtra(KEY_SCENE_NAME);
        if (null == sceneName) {
            return false;
        }

        Class clazz;
        try {
            clazz = Class.forName(sceneName);
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "Can't find class " + sceneName, e);
            return false;
        }

        Bundle args = intent.getBundleExtra(KEY_SCENE_ARGS);

        Announcer announcer = new Announcer(clazz);
        announcer.setArgs(args);
        startScene(announcer);
        return true;
    }

    protected void startScene(Announcer announcer) {
        Class<?> clazz = announcer.clazz;
        Bundle args = announcer.args;

        FragmentManager fragmentManager = getSupportFragmentManager();
        int launchMode = getSceneLaunchMode(clazz);

        // Check LAUNCH_MODE_SINGLE_TASK
        if (launchMode == SceneFragment.LAUNCH_MODE_SINGLE_TASK) {
            for (int i = 0, n = mSceneTagList.size(); i < n; i++) {
                String tag = mSceneTagList.get(i);
                Fragment fragment = fragmentManager.findFragmentByTag(tag);
                if (fragment == null) {
                    Log.e(TAG, "Can't find fragment with tag: " + tag);
                    continue;
                }

                if (clazz.isInstance(fragment)) { // Get it
                    FragmentTransaction transaction = fragmentManager.beginTransaction();

                    // Use default animation
                    transaction.setCustomAnimations(R.anim.scene_open_enter, R.anim.scene_open_exit);

                    // Remove top fragments
                    for (int j = i + 1; j < n; j++) {
                        String topTag = mSceneTagList.get(j);
                        Fragment topFragment = fragmentManager.findFragmentByTag(topTag);
                        if (null == topFragment) {
                            Log.e(TAG, "Can't find fragment with tag: " + topTag);
                            continue;
                        }
                        // Clear shared element
                        topFragment.setSharedElementEnterTransition(null);
                        topFragment.setSharedElementReturnTransition(null);
                        topFragment.setEnterTransition(null);
                        topFragment.setExitTransition(null);
                        // Remove it
                        transaction.remove(topFragment);
                    }

                    // Remove tag from index i+1
                    mSceneTagList.subList(i + 1, mSceneTagList.size()).clear();

                    // Attach fragment
                    if (fragment.isDetached()) {
                        transaction.attach(fragment);
                    }

                    // Commit
                    transaction.commitAllowingStateLoss();

                    // New arguments
                    if (args != null && fragment instanceof SceneFragment) {
                        // TODO Call onNewArguments when view created ?
                        ((SceneFragment) fragment).onNewArguments(args);
                    }

                    return;
                }
            }
        }

        // Get current fragment
        SceneFragment currentScene = null;
        if (mSceneTagList.size() > 0) {
            // Get last tag
            String tag = mSceneTagList.get(mSceneTagList.size() - 1);
            Fragment fragment = fragmentManager.findFragmentByTag(tag);
            if (fragment != null) {
                Assert.assertTrue(SceneFragment.class.isInstance(fragment));
                currentScene = (SceneFragment) fragment;
            }
        }

        // Check LAUNCH_MODE_SINGLE_TASK
        if (clazz.isInstance(currentScene) && launchMode == SceneFragment.LAUNCH_MODE_SINGLE_TOP) {
            if (args != null) {
                currentScene.onNewArguments(args);
            }
            return;
        }

        // Create new scene
        SceneFragment newScene = newSceneInstance(clazz);
        newScene.setArguments(args);

        // Create new scene tag
        String newTag = Integer.toString(mIdGenerator.getAndIncrement());

        // Add new tag to list
        mSceneTagList.add(newTag);

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        // Animation
        if (currentScene != null) {
            /*if (tranHelper == null || !tranHelper.onTransition(
                    this, transaction, currentScene, newScene)) {
                // Clear shared item
                currentScene.setSharedElementEnterTransition(null);
                currentScene.setSharedElementReturnTransition(null);
                currentScene.setEnterTransition(null);
                currentScene.setExitTransition(null);
                newScene.setSharedElementEnterTransition(null);
                newScene.setSharedElementReturnTransition(null);
                newScene.setEnterTransition(null);
                newScene.setExitTransition(null);
                // Set default animation
                transaction.setCustomAnimations(R.anim.scene_open_enter, R.anim.scene_open_exit);
            }*/
            // Detach current scene
            if (!currentScene.isDetached()) {
                transaction.detach(currentScene);
            } else {
                Log.e(TAG, "Current scene is detached");
            }
        }

        // Add new scene
        transaction.add(getContainerViewId(), newScene, newTag);

        // Commit
        transaction.commitAllowingStateLoss();

        // Check request
        if (announcer.requestFrom != null) {
            newScene.addRequest(announcer.requestFrom.getTag(), announcer.requestCode);
        }
    }

    @Override
    public void onBackPressed() {
        int size = mSceneTagList.size();
        String tag = mSceneTagList.get(size - 1);
        SceneFragment scene;
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(tag);
        if (fragment == null) {
            Log.e(TAG, "onBackPressed: Can't find scene by tag: " + tag);
            return;
        }
        if (!(fragment instanceof SceneFragment)) {
            Log.e(TAG, "onBackPressed: The fragment is not SceneFragment");
            return;
        }

        scene = (SceneFragment) fragment;
        scene.onBackPressed();
    }

    public void finishScene(SceneFragment scene) {
        finishScene(scene.getTag());
    }

    private void finishScene(String tag) {
        FragmentManager fragmentManager = getSupportFragmentManager();

        // Get scene
        Fragment scene = fragmentManager.findFragmentByTag(tag);
        if (scene == null) {
            Log.e(TAG, "finishScene: Can't find scene by tag: " + tag);
            return;
        }

        // Get scene index
        int index = mSceneTagList.indexOf(tag);
        if (index < 0) {
            Log.e(TAG, "finishScene: Can't find the tag in tag list: " + tag);
            return;
        }

        if (mSceneTagList.size() == 1) {
            // It is the last fragment, finish Activity now
            Log.i(TAG, "finishScene: It is the last scene, finish activity now");
            finish();
            return;
        }

        Fragment next = null;
        if (index == mSceneTagList.size() - 1) {
            // It is first fragment, show the next one
            next = fragmentManager.findFragmentByTag(mSceneTagList.get(index - 1));
        }

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        if (next != null) {
//            if (transitionHelper == null || !transitionHelper.onTransition(
//                    this, transaction, scene, next)) {
//                // Clear shared item
//                scene.setSharedElementEnterTransition(null);
//                scene.setSharedElementReturnTransition(null);
//                scene.setEnterTransition(null);
//                scene.setExitTransition(null);
//                next.setSharedElementEnterTransition(null);
//                next.setSharedElementReturnTransition(null);
//                next.setEnterTransition(null);
//                next.setExitTransition(null);
//                // Do not show animate if it is not the first fragment
//                transaction.setCustomAnimations(R.anim.scene_close_enter, R.anim.scene_close_exit);
//            }
            // Attach fragment
            transaction.attach(next);
        }
        transaction.remove(scene);
        transaction.commitAllowingStateLoss();

        // Remove tag
        mSceneTagList.remove(index);

        // Return result
        if (scene instanceof SceneFragment) {
            ((SceneFragment) scene).returnResult(this);
        }
    }

    public SceneFragment findSceneByTag(String tag) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentByTag(tag);
        if (fragment != null) {
            return (SceneFragment) fragment;
        } else {
            return null;
        }
    }
}
