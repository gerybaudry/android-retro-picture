package com.gautier_lefebvre.epitechmessengerapp.gcm;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import com.gautier_lefebvre.epitechmessengerapp.app.Config;
import com.google.android.gms.gcm.GcmListenerService;

public class MyGcmPushReceiver extends GcmListenerService {
    @Override
    public void onMessageReceived(String from, Bundle bundle) {
        Intent pushNotification = new Intent(Config.PUSH_NOTIFICATION);
        LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification);
    }
}
