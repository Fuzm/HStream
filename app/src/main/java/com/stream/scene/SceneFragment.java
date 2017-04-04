/*
 * Copyright 2016 Hippo Seven
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.stream.scene;

import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

import com.hippo.yorozuya.collect.IntList;
import com.stream.hstream.MainActivity;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

public class SceneFragment extends Fragment {

    @IntDef({LAUNCH_MODE_STANDARD, LAUNCH_MODE_SINGLE_TOP, LAUNCH_MODE_SINGLE_TASK})
    @Retention(RetentionPolicy.SOURCE)
    public @interface LaunchMode {}

    public static final int LAUNCH_MODE_STANDARD = 0;
    public static final int LAUNCH_MODE_SINGLE_TOP = 1;
    public static final int LAUNCH_MODE_SINGLE_TASK = 2;

    /** Standard scene result: operation canceled. */
    public static final int RESULT_CANCELED  = 0;
    /** Standard scene result: operation succeeded. */
    public static final int RESULT_OK = -1;

    int resultCode = RESULT_CANCELED;
    Bundle result = null;

    List<String> mRequestSceneTagList = new ArrayList<>(0);
    IntList mRequestCodeList = new IntList(0);

    public void startScene(Announcer announcer) {
        FragmentActivity activity = getActivity();
        if (activity instanceof StageActivity) {
            ((StageActivity) activity).startScene(announcer);
        }
    }

    public void onNewArguments(@NonNull Bundle args) {}

    void addRequest(String requestSceneTag, int requestCode) {
        mRequestSceneTagList.add(requestSceneTag);
        mRequestCodeList.add(requestCode);
    }

    public void onBackPressed() {
        FragmentActivity activity = getActivity();
        if (activity instanceof StageActivity) {
            ((StageActivity) activity).finishScene(this);
        }
    }

    void returnResult(StageActivity stage) {
        for (int i = 0, size = Math.min(mRequestSceneTagList.size(), mRequestCodeList.size()); i < size; i++) {
            String tag = mRequestSceneTagList.get(i);
            int code = mRequestCodeList.get(i);
            SceneFragment scene = stage.findSceneByTag(tag);
            if (scene != null) {
                scene.onSceneResult(code, resultCode, result);
            }
        }
        mRequestSceneTagList.clear();
        mRequestCodeList.clear();
    }

    protected void onSceneResult(int requestCode, int resultCode, Bundle data) {
    }

    public void setDrawerLockMode(int lockMode, int edgeGravity) {
        FragmentActivity activity = getActivity();
        if (activity instanceof MainActivity) {
            ((MainActivity) activity).setDrawerLockMode(lockMode, edgeGravity);
        }
    }

}
