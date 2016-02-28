package com.gautier_lefebvre.epitechmessengerapp.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.gautier_lefebvre.epitechmessengerapp.business.ProtocolService;
import com.gautier_lefebvre.epitechmessengerapp.entity.ApplicationData;
import com.gautier_lefebvre.epitechmessengerapp.entity.CurrentUser;
import com.gautier_lefebvre.epitechmessengerapp.entity.protocol.SignIn;
import com.gautier_lefebvre.epitechmessengerapp.exception.HTTPRequestFailedException;
import com.gautier_lefebvre.epitechmessengerapp.R;

public class SignInActivity extends AppActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                this.finish();
                this.startActivity(this.getHomeIntent());
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void SubmitButton_onClick(View v) {
        EditText emailField = (EditText)findViewById(R.id.emailField);
        EditText passwordField = (EditText)findViewById(R.id.passwordField);

        // trim email
        String email = emailField.getText().toString().trim();

        // keep passwords untouched
        String password = passwordField.getText().toString();

        // check errors
        if (email.length() == 0) {
            this.showDialog("Error", "Email cannot be empty");
        } else if (password.length() == 0) {
            this.showDialog("Error", "Password cannot be empty");
        } else {
            // disable button
            Button submitButton = (Button)this.findViewById(R.id.submitButton);
            submitButton.setEnabled(false);

            // send sign up request
            SignIn.Request request = new SignIn.Request();
            request.email = email;
            request.password = password;
            request.activity = this;
            request.key = ApplicationData.aesKey;
            new SignInActivity.SignInTask().execute(request);
        }
    }

    /**
     * Called after a successful call to the server
     * @param response the response from the server
     */
    private void onSignInTaskSuccess(SignIn.Response response) {
        if (response.success) {
            // set current user
            ApplicationData.currentUser = new CurrentUser(
                    response.userId,
                    response.nickname,
                    response.email,
                    response.authToken);

            // go to home connected
            Intent i = new Intent(this, HomeActivity.class);
            this.finish();
            this.startActivity(i);
        } else {
            this.showDialog("Error", response.reason);

            // enable button
            Button submitButton = (Button)this.findViewById(R.id.submitButton);
            submitButton.setEnabled(true);
        }
    }

    public static class SignInTask extends AsyncTask<SignIn.Request, Void, Void> {
        SignIn.Request request = null;
        SignIn.Response response = null;
        HTTPRequestFailedException exception = null;

        @Override
        protected Void doInBackground(SignIn.Request... params) {
            this.request = params[0];
            try {
                this.response = ProtocolService.getInstance().signIn(this.request);
            } catch (HTTPRequestFailedException e) {
                this.exception = e;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void _) {
            if (this.response != null) {
                this.request.activity.onSignInTaskSuccess(this.response);
            } else {
                this.request.activity.onNetworkError(this.exception);
            }
        }
    }
}
