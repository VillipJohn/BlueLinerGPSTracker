package com.villip.bluelinergpstracker;

import android.app.Application;

import com.facebook.stetho.Stetho;

/**
 * Created by villip on 25.07.2017.
 */

public class MyApplication extends Application {
    public void onCreate() {
        super.onCreate();
        Stetho.initializeWithDefaults(this);
    }
}
