/*
 *  Copyright 2015 Yahoo Inc.
 *  Licensed under the terms of the zLib license. Please see LICENSE file for terms.
 */
package com.flurry.samples.tumblrsharing;

import android.app.Application;
import android.util.Log;

import com.flurry.android.FlurryAgent;

/**
 * Base class to maintain global application state
 */
public class FlurryTumblrSharingAndroidApp extends Application {

    private static final String FLURRY_APIKEY = "D2K3KT94CMV8GT9YXY2T";
    public static final String LOG_TAG = FlurryTumblrSharingAndroidApp.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();

        // Init Flurry
        FlurryAgent.setLogEnabled(true);
        FlurryAgent.setLogLevel(Log.INFO);

        FlurryAgent.setVersionName("1.0");
        FlurryAgent.init(this, FLURRY_APIKEY);

        Log.i(LOG_TAG, "Initialized FLurry Agent");
    }
}
