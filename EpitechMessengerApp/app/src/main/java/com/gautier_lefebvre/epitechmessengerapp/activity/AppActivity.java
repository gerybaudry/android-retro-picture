package com.gautier_lefebvre.epitechmessengerapp.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;

import com.gautier_lefebvre.epitechmessengerapp.app.Config;
import com.gautier_lefebvre.epitechmessengerapp.app.MyApplication;
import com.gautier_lefebvre.epitechmessengerapp.business.ProtocolService;
import com.gautier_lefebvre.epitechmessengerapp.entity.ApplicationData;
import com.gautier_lefebvre.epitechmessengerapp.entity.protocol.UpdateRegistrationToken;
import com.gautier_lefebvre.epitechmessengerapp.exception.HTTPRequestFailedException;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Base class of all activities
 */
public class AppActivity extends AppCompatActivity {
    // the application
    protected MyApplication _myApp;

    // the activity
    protected AppActivity _context = this;

    // broadcast receiver for registration token update
    protected BroadcastReceiver _gcmRegistrationBroadcastReceiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _myApp = (MyApplication)this.getApplicationContext();

        // when gcm registration token updates (push notifications) -> send new token to server
        _gcmRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Config.REGISTRATION_COMPLETE)) {
                    ApplicationData.gcmRegistrationToken = intent.getStringExtra("token");

                    if (ApplicationData.currentUser != null) {
                        UpdateRegistrationToken.Request request = new UpdateRegistrationToken.Request();
                        request.activity = _context;
                        request.userId = ApplicationData.currentUser.userId;
                        request.auth_token = ApplicationData.currentUser.authToken;
                        new UpdateRegistrationTokenTask().execute(request);
                    }
                }
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        _myApp.setCurrentActivity(this);

        LocalBroadcastManager.getInstance(this).registerReceiver(_gcmRegistrationBroadcastReceiver,
                new IntentFilter(Config.REGISTRATION_COMPLETE));
    }

    @Override
    protected void onPause() {
        clearReferences();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        clearReferences();
        super.onDestroy();
    }

    private void clearReferences() {
        Activity currentActivity = _myApp.getCurrentActivity();
        if (this.equals(currentActivity)) {
            _myApp.setCurrentActivity(null);
        }

        LocalBroadcastManager.getInstance(this).unregisterReceiver(_gcmRegistrationBroadcastReceiver);
    }

    /**
     * Called after a called to the server failed.
     * @param e the exception caught
     */
    public void onNetworkError(HTTPRequestFailedException e) {
        this.goToErrorActivity(e);
    }

    public void goToErrorActivity(String error) {
        Intent i = new Intent(this, ErrorActivity.class);
        i.putExtra("error", error);
        this.finish();
        this.startActivity(i);
    }

    public void goToErrorActivity(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        this.goToErrorActivity(sw.toString());
    }

    public Intent getHomeIntent() {
        return new Intent(this, ApplicationData.currentUser != null ? HomeActivity.class : HomeDisconnectedActivity.class);
    }

    public void showDialog(String title, String content) {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(title)
                .setCancelable(true)
                .setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setMessage(content)
                .create();

        dialog.show();
    }

    protected static class UpdateRegistrationTokenTask extends AsyncTask<UpdateRegistrationToken.Request, Void, Void> {
        UpdateRegistrationToken.Response response = null;
        UpdateRegistrationToken.Request request = null;
        HTTPRequestFailedException exception = null;

        @Override
        protected Void doInBackground(UpdateRegistrationToken.Request... params) {
            this.request = params[0];
            try {
                UpdateRegistrationToken.Response response = new UpdateRegistrationToken.Response();
                ProtocolService.getInstance().sendEncryptedRequest(this.request, response);
                this.response = response;
            } catch (HTTPRequestFailedException e) {
                this.exception = e;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void _) {
            if (this.response != null) {
                this.request.activity.onUpdateRegistrationTokenTaskSuccess(this.response);
            } else {
                this.request.activity.onNetworkError(this.exception);
            }
        }
    }

    private void onUpdateRegistrationTokenTaskSuccess(UpdateRegistrationToken.Response response) {
        if (!response.success) {
            this.showDialog("error", "failed updating token: " + response.reason);
        }
    }
}
