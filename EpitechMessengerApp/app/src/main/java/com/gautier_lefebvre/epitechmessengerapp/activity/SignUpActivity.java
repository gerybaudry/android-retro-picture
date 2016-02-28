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
import com.gautier_lefebvre.epitechmessengerapp.entity.protocol.SignUp;
import com.gautier_lefebvre.epitechmessengerapp.exception.HTTPRequestFailedException;
import com.gautier_lefebvre.epitechmessengerapp.helper.PasswordHelper;
import com.gautier_lefebvre.epitechmessengerapp.R;

import java.util.Objects;

public class SignUpActivity extends AppActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

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
        EditText nameField = (EditText)findViewById(R.id.nameField);
        EditText emailField = (EditText)findViewById(R.id.emailField);
        EditText passwordField = (EditText)findViewById(R.id.passwordField);
        EditText passwordConfirmField = (EditText)findViewById(R.id.passwordConfirmField);

        // trim name and email
        String name = nameField.getText().toString().trim();
        String email = emailField.getText().toString().trim();

        // keep passwords untouched
        String password = passwordField.getText().toString();
        String passwordConfirm = passwordConfirmField.getText().toString();

        // check errors
        if (name.length() == 0) {
            this.showDialog("Error", "Nickname cannot be empty");
        } else if (email.length() == 0) {
            this.showDialog("Error", "Email address cannot be empty");
        } else if (!PasswordHelper.isPasswordValid(password)) {
            this.showDialog("Error", "Invalid password (must be at least 8 characters long, and contain a maj, a min and a number)");
        } else if (!Objects.equals(password, passwordConfirm)) {
            this.showDialog("Error", "Passwords don't match");
        } else {
            // disable button
            Button submitButton = (Button)this.findViewById(R.id.submitButton);
            submitButton.setEnabled(false);

            // send sign up request
            SignUp.Request request = new SignUp.Request();
            request.nickname = name;
            request.email = email;
            request.password = password;
            request.activity = this;
            request.key = ApplicationData.aesKey;
            new SignUpActivity.SignUpTask().execute(request);
        }
    }

    private void onSignUpTaskSuccess(SignUp.Response response) {
        if (response.success) {
            // set current user object
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
            // display error
            this.showDialog("Error", response.reason);

            // enable button
            Button submitButton = (Button)this.findViewById(R.id.submitButton);
            submitButton.setEnabled(true);
        }
    }

    public static class SignUpTask extends AsyncTask<SignUp.Request, Void, Void> {
        SignUp.Request request = null;
        SignUp.Response response = null;
        HTTPRequestFailedException exception = null;

        @Override
        protected Void doInBackground(SignUp.Request... params) {
            this.request = params[0];
            try {
                this.response = ProtocolService.getInstance().signUp(this.request);
            } catch (HTTPRequestFailedException e) {
                this.exception = e;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void _) {
            if (this.response != null) {
                this.request.activity.onSignUpTaskSuccess(this.response);
            } else {
                this.request.activity.onNetworkError(this.exception);
            }
        }
    }
}
