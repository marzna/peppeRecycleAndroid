package com.example.pepperecycleandroid;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.design.activity.RobotActivity;
import com.aldebaran.qi.sdk.object.camera.Camera;
import com.example.pepperrecycleandroid.R;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class MainActivity extends RobotActivity implements RobotLifecycleCallbacks, CameraBridgeViewBase.CvCameraViewListener2  {

    // Store the Animate action.
//    private Animate animate;
    private static String TAG = "MainActivity";

    //private CameraBridgeViewBase javaCameraView;
    private JavaCameraView javaCameraView;

    private Mat mRGBA, mRGBAT;

    private static final int MY_CAMERA_REQUEST_CODE = 100; //Per la richiesta dei permessi della fotocamera
    int activeCamera = CameraBridgeViewBase.CAMERA_ID_FRONT; //Attiva la fotocamera frontale (?)

    private BaseLoaderCallback baseLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch(status) {
                case BaseLoaderCallback.SUCCESS: {
                    javaCameraView.enableView();
                    break;}
                default: {
                    super.onManagerConnected(status);
                    break;
                }
            }
            super.onManagerConnected(status);
        }
    };
    // callback to be executed after the user has given approval or rejection via system prompt
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // camera can be turned on
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
                initializeCamera((JavaCameraView) javaCameraView, activeCamera);
            } else {
                // camera will stay off
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void initializeCamera(JavaCameraView javaCameraView, int activeCamera){
        javaCameraView.setCameraPermissionGranted();
        javaCameraView.setCameraIndex(activeCamera);

        //javaCameraView.setVisibility(CameraBridgeViewBase.VISIBLE); --> non so a cosa serva #TODO
        javaCameraView.setVisibility(SurfaceView.VISIBLE);
        javaCameraView.setCvCameraViewListener(this);
    }


    @Override
    public void onCameraViewStarted(int width, int height) {
        mRGBA = new Mat(height, width, CvType.CV_8UC4);
    }

    @Override
    public void onCameraViewStopped() {
        mRGBA.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRGBA = inputFrame.rgba();
        mRGBAT = mRGBA.t();
        //Core.flip(mRGBA.t(),  mRGBAT,1); ----> Distorce l'immagine, quindi non serve
        Core.flip(mRGBA,  mRGBAT,1);
        Imgproc.resize(mRGBAT, mRGBAT, mRGBA.size());


        // releasing what's not anymore needed
        mRGBA.release();

        return mRGBAT;
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Register the RobotLifecycleCallbacks to this Activity.
        QiSDK.register(this, this);
        setContentView(R.layout.activity_main);

        //Parte relativa alla fotocamera
        javaCameraView = (JavaCameraView) findViewById(R.id.my_camera_view);

        // Controlla se il permesso di accesso alla fotocamera è stato già chiesto in passato
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Permissions granted");
            initializeCamera((JavaCameraView) javaCameraView, activeCamera);
        } else {
            // prompt system dialog
            Log.d(TAG, "Permission prompt");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, MY_CAMERA_REQUEST_CODE);
        }

    }

    @Override
    protected void onDestroy() {
        // Unregister the RobotLifecycleCallbacks for this Activity.
        QiSDK.unregister(this, this);
        super.onDestroy();

        if(javaCameraView != null) {
            javaCameraView.disableView();
        }
    }
    @Override
    protected void onPause() {
        super.onPause();

        if(javaCameraView != null) {
            javaCameraView.disableView();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (OpenCVLoader.initDebug()) {
            Log.d(TAG, "OpenCV is Configured or Connected Successfully.");
            baseLoaderCallback.onManagerConnected(BaseLoaderCallback.SUCCESS);
        } else{
            Log.d(TAG, "OpenCV not Working or Loaded");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this,baseLoaderCallback);
        }
    }

    @Override
    public void onRobotFocusGained(QiContext qiContext) {

    }

    @Override
    public void onRobotFocusLost() {

    }

    @Override
    public void onRobotFocusRefused(String reason) {

    }

}