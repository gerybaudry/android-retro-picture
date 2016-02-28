package com.gautier_lefebvre.epitechmessengerapp.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;

import com.gautier_lefebvre.epitechmessengerapp.app.Config;
import com.gautier_lefebvre.epitechmessengerapp.business.ProtocolService;
import com.gautier_lefebvre.epitechmessengerapp.entity.ApplicationData;
import com.gautier_lefebvre.epitechmessengerapp.entity.protocol.GetServerKey;
import com.gautier_lefebvre.epitechmessengerapp.exception.HTTPRequestFailedException;
import com.gautier_lefebvre.epitechmessengerapp.exception.RSAException;
import com.gautier_lefebvre.epitechmessengerapp.gcm.GcmIntentService;
import com.gautier_lefebvre.epitechmessengerapp.helper.AESHelper;
import com.gautier_lefebvre.epitechmessengerapp.helper.RSAHelper;
import com.gautier_lefebvre.epitechmessengerapp.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

public class MainActivity extends AppActivity {
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private Handler _handler = new Handler();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // override attribute from AppActivity
        _gcmRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Config.REGISTRATION_COMPLETE)) {
                    ApplicationData.gcmRegistrationToken = intent.getStringExtra("token");

                    ((MainActivity)_context)._handler.post(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if (ApplicationData.aesKey == null) {
                                    ApplicationData.aesKey = AESHelper.generateKey();
                                }

                                if (ApplicationData.serverKey == null) {
                                    GetServerKey.Request request = new GetServerKey.Request();
                                    request.activity = _context;
                                    new MainActivity.GetServerKeyTask().execute(request);
                                } else {
                                    _context.finish();
                                    _context.startActivity(_context.getHomeIntent());
                                }
                            } catch (Exception e) {
                                _context.showDialog("error", "error initializing the app");
                            }
                        }
                    });
                }
            }
        };

        if (checkPlayServices()) {
            registerGCM();
        } else {
            this.showDialog("error", "Google Play Services are required");
        }
    }

    private void registerGCM() {
        Intent intent = new Intent(this, GcmIntentService.class);
        startService(intent);
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                finish();
            }
            return false;
        }
        return true;
    }

    public void onGetServerKeySuccess(String keyStr) {
        try {
            ApplicationData.serverKey = RSAHelper.getKeyFromString(keyStr);
            this.finish();
            this.startActivity(this.getHomeIntent());
        } catch (RSAException e) {
            this.goToErrorActivity(e);
        }
    }

    public static class GetServerKeyTask extends AsyncTask<GetServerKey.Request, Void, Void> {
        GetServerKey.Request request = null;
        String keyStr = null;
        HTTPRequestFailedException exception = null;

        @Override
        protected Void doInBackground(GetServerKey.Request... params) {
            this.request = params[0];
            try {
                this.keyStr = ProtocolService.getInstance().getServerKey().key;
            } catch (HTTPRequestFailedException e) {
                this.exception = e;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void _) {
            MainActivity activity = (MainActivity)this.request.activity;
            if (this.exception == null) {
                activity.onGetServerKeySuccess(this.keyStr);
            } else {
                activity.onNetworkError(this.exception);
            }
        }
    }
}
