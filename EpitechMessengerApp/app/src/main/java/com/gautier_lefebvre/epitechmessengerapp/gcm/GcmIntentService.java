package com.gautier_lefebvre.epitechmessengerapp.gcm;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.gautier_lefebvre.epitechmessengerapp.R;
import com.gautier_lefebvre.epitechmessengerapp.app.Config;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

public class GcmIntentService extends IntentService {
    public GcmIntentService() {
        super(GcmIntentService.class.getSimpleName());
    }

    public GcmIntentService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        this.registerGCM();
    }

    private void registerGCM() {
        Intent registrationComplete = new Intent(Config.REGISTRATION_COMPLETE);

        try {
            InstanceID instanceID = InstanceID.getInstance(this);
            String token = instanceID.getToken(getString(R.string.gcm_defaultSenderId),
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);

            registrationComplete.putExtra("success", true);
            registrationComplete.putExtra("token", token);
        } catch (Exception e) {
            registrationComplete.putExtra("success", false);
        }

        LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
    }
}
