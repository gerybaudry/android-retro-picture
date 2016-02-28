package com.gautier_lefebvre.epitechmessengerapp.activity;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import com.gautier_lefebvre.epitechmessengerapp.app.Config;

public class MessageHandlingActivity extends AppActivity {
    protected BroadcastReceiver _gcmNotificationBroadcastReceiver;

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (this._gcmNotificationBroadcastReceiver != null) {
            LocalBroadcastManager.getInstance(this).registerReceiver(
                    this._gcmNotificationBroadcastReceiver,
                    new IntentFilter(Config.PUSH_NOTIFICATION));
        }
    }

    @Override
    public void onPause() {
        this.unregisterNotificationReceiver();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        this.unregisterNotificationReceiver();
        super.onDestroy();
    }

    private void unregisterNotificationReceiver() {
        if (this._gcmNotificationBroadcastReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(this._gcmNotificationBroadcastReceiver);
        }
    }
}
