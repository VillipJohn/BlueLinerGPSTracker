package com.villip.bluelinergpstracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class GPSTrackerAlarmReceiver extends BroadcastReceiver {
    //private static final String TAG = "GpsTrackerAlarmReceiver";

    private SharedPreferences mSharedPreferences;
    private static final String APP_PREFERENCES = "mySettings";
    private static final String APP_PREFERENCES_CURRENTLY_TRACKING = "currentlyTracking";
    private boolean currentlyTracking;

    @Override
    public void onReceive(Context context, Intent intent) {

        mSharedPreferences = context.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        currentlyTracking = mSharedPreferences.getBoolean(APP_PREFERENCES_CURRENTLY_TRACKING, false);

        if(currentlyTracking){
            context.startService(new Intent(context, LocationService.class));
        }
    }
}
