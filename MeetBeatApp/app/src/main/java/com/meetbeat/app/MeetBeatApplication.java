package com.meetbeat.app;

import android.app.Application;
import android.content.Context;

import com.meetbeat.app.util.ThemePreference;

public class MeetBeatApplication extends Application {

    private static Context appContext;

    @Override
    public void onCreate() {
        super.onCreate();
        appContext = getApplicationContext();
        ThemePreference.aplicarSalva(this);
    }

    public static Context getAppContext() {
        return appContext;
    }
}
