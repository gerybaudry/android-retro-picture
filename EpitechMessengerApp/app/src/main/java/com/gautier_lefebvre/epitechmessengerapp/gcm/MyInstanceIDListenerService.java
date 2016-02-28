package com.gautier_lefebvre.epitechmessengerapp.gcm;

import android.content.Intent;

import com.google.android.gms.iid.InstanceIDListenerService;

public class MyInstanceIDListenerService extends InstanceIDListenerService {
    @Override
    public void onTokenRefresh() {
        Intent intent = new Intent(this, GcmIntentService.class);
        startService(intent);
    }
}
