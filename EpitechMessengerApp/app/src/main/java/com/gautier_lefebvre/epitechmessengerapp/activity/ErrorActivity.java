package com.gautier_lefebvre.epitechmessengerapp.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.widget.TextView;

import com.gautier_lefebvre.epitechmessengerapp.R;

public class ErrorActivity extends AppActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_error);

        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setDisplayHomeAsUpEnabled(true);
        }

        TextView t = (TextView)findViewById(R.id.errorMessage);
        String errorMessage = getIntent().getStringExtra("error");
        t.setText(errorMessage);
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
}
