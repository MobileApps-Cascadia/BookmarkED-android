package edu.cascadia.bookmarked;

/*
 * Copyright Google Inc. All Rights Reserved.
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



 /*       import android.app.Application;

        import com.google.android.gms.analytics.GoogleAnalytics;
        import com.google.android.gms.analytics.Logger;
        import com.google.android.gms.analytics.Tracker;

/**
 * This is a subclass of {@link Application} used to provide shared objects for this app, such as
 * the {@link Tracker}.
 */
/*public class AnalyticsApplication extends Application {
    private Tracker mTracker;

    /**
     * Gets the default {@link Tracker} for this {@link Application}.
     * @return tracker
     */
  /*  synchronized public Tracker getDefaultTracker() {
        if (mTracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
            mTracker = analytics.newTracker(R.xml.global_tracker);
            mTracker.enableAutoActivityTracking(true);
            mTracker.enableExceptionReporting(true);
        }
        return mTracker;
    }
}*/


import android.app.Application;

import com.google.android.gms.analytics.GoogleAnalytics;

import com.google.android.gms.analytics.Tracker;

import java.util.HashMap;

public class AnalyticsApplication extends Application {

// The following line should be changed to include the correct property id.

    private static final String PROPERTY_ID = "UA-71290756-1";

//Logging TAG

    private static final String TAG = "MyApp";

    public static int GENERAL_TRACKER = 0;

    public enum TrackerName {

        APP_TRACKER, // Tracker used only in this app.

        GLOBAL_TRACKER, // Tracker used by all the apps from a company. eg: roll-up tracking.

       // ECOMMERCE_TRACKER, // Tracker used by all ecommerce transactions from a company.


    }

    HashMap<TrackerName, Tracker> mTrackers = new HashMap<TrackerName, Tracker>();

    public AnalyticsApplication() {

        super();

    }

    synchronized Tracker getTracker(TrackerName trackerId) {

        if (!mTrackers.containsKey(trackerId)) {

            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);

            if (trackerId == TrackerName.APP_TRACKER) {
                Tracker t = analytics.newTracker(R.xml.app_tracker);
                mTrackers.put(trackerId, t);
            }
            else if (trackerId == TrackerName.GLOBAL_TRACKER) {
                Tracker t = analytics.newTracker(PROPERTY_ID);
                mTrackers.put(trackerId, t);
            }
            else {
                //Tracker t = analytics.newTracker(R.xml.ecommerce_tracker);
            }

        }

        return mTrackers.get(trackerId);

    }

}
