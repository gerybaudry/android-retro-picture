package com.gautier_lefebvre.epitechmessengerapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.gautier_lefebvre.epitechmessengerapp.R;

public class HomeDisconnectedActivity extends AppActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_disconnected);
    }

    public void SignUpButton_onClick(View v) {
        Intent i = new Intent(this, SignUpActivity.class);
        this.finish();
        this.startActivity(i);
    }

    public void SignInButton_onClick(View v) {
        Intent i = new Intent(this, SignInActivity.class);
        this.finish();
        this.startActivity(i);
    }
}
