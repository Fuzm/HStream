package com.stream.util;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Parcelable;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

/**
 * Created by Fuzm on 2017/4/1 0001.
 */

public class AppHelper {

    public static void hideSoftInput(Activity activity) {
        View view = activity.getCurrentFocus();
        if(view != null) {
            InputMethodManager manager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            manager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public static void hideSoftInput(Dialog dialog) {
        View view = dialog.getCurrentFocus();
        if(view != null) {
            InputMethodManager manager = (InputMethodManager) dialog.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            manager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
