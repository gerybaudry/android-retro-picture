package com.example.gery.retropicture.home.camera;

import android.app.Fragment;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.example.gery.retropicture.R;
import com.example.gery.retropicture.home.utils.CameraPreview;

/**
 * Created by Gery on 06/02/2016.
 */
public class CameraFragment extends Fragment {

    //region Attributes
    private Camera mCamera;
    private CameraPreview mPreview;
    private View mView;
    //endregion

    //region Override Methods
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_camera, container, false);
        // Inflate the layout for this fragment
        // Create an instance of Camera
        mCamera = getCameraInstance();

        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(mView.getContext(), mCamera);
        FrameLayout preview = (FrameLayout) mView.findViewById(R.id.camera_preview);
        preview.addView(mPreview);

        return mView;
    }

    //endregion

    //region Methods
    /** Check if this device has a camera */
    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    /** A safe way to get an instance of the Camera object. */
    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }
    //endregion
}
