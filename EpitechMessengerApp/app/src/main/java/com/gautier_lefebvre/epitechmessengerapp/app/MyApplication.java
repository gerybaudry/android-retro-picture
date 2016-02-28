package com.gautier_lefebvre.epitechmessengerapp.app;

import android.app.Activity;
import android.app.Application;

public class MyApplication extends Application {
    private Activity _currentActivity = null;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public Activity getCurrentActivity() {
        return _currentActivity;
    }

    public void setCurrentActivity(Activity currentActivity) {
        this._currentActivity = currentActivity;
    }
}
