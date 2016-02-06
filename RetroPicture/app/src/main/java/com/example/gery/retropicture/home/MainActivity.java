package com.example.gery.retropicture.home;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.gery.retropicture.R;
import com.example.gery.retropicture.home.camera.CameraFragment;

public class MainActivity extends AppCompatActivity {


    //region Attributes

    //endregion

    //region Override Methods
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Fragment newFragment = new CameraFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, newFragment).addToBackStack(null);
        transaction.commit();
    }
    //endregion

    //region Methods

    //endregion
}
