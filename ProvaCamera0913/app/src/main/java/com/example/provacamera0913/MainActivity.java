package com.example.provacamera0913;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2, View.OnTouchListener {

    private static final String TAG = "MainActivity";
    private static final int CAMERA_REQUEST = 1888;
    private static final int MY_CAMERA_REQUEST_CODE = 100; //Per la richiesta dei permessi della fotocamera
    int activeCamera = CameraBridgeViewBase.CAMERA_ID_FRONT; //Attiva la fotocamera frontale (?)
    CascadeClassifier faceDetector;
    //private CameraBridgeViewBase javaCameraView;
    private JavaCameraView javaCameraView;
    private File cascFile;
    private Mat mRGBA, mRGBAT, mGrey;
    private boolean touched = false;
    private ImageView imageView;
    private Bitmap mRGBATbitmap;
    private BaseLoaderCallback baseLoaderCallback = new BaseLoaderCallback(MainActivity.this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case BaseLoaderCallback.SUCCESS: {
                    InputStream is = getResources().openRawResource(R.raw.haarcascade_frontalface_alt2);

                    File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                    cascFile = new File(cascadeDir, "haarcascade_frontalface_alt2.xml");

                    FileOutputStream fos = null;
                    try {
                        fos = new FileOutputStream(cascFile);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                    byte[] buffer = new byte[4096];
                    int bytesRead;

                    try {
                        while (((bytesRead = is.read(buffer)) != -1)) {
                            fos.write(buffer, 0, bytesRead);
                        }
                        is.close();
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                    faceDetector = new CascadeClassifier(cascFile.getAbsolutePath());
                    if (faceDetector.empty()) {
                        faceDetector = null;
                    } else {
                        cascadeDir.delete();
                    }

                    javaCameraView.enableView();

                    break;
                }
                default: {
                    super.onManagerConnected(status);
                    break;
                }
            }
            super.onManagerConnected(status);
        }
    };

    public MainActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        javaCameraView = (JavaCameraView) findViewById(R.id.my_camera_view);

        this.imageView = (ImageView) this.findViewById(R.id.imageView);
        //Button buttonCapture = (Button) findViewById(R.id.buttonCapture); // bottone "ecco"

        // Controlla se il permesso di accesso alla fotocamera è stato già chiesto in passato
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Permissions granted");
            initializeCamera((JavaCameraView) javaCameraView, activeCamera);
        } else {
            // prompt system dialog
            Log.d(TAG, "Permission prompt");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, MY_CAMERA_REQUEST_CODE);
        }

        /*buttonCapture.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                public void onClick(View v)
                {
                    if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                    {
                        requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_REQUEST_CODE);
                    }
                    else
                    {
                        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(cameraIntent, CAMERA_REQUEST);
                    }
                }
        });*/
    }

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

    private void initializeCamera(JavaCameraView javaCameraView, int activeCamera) {
        javaCameraView.setCameraPermissionGranted();
        javaCameraView.setCameraIndex(activeCamera);

        //javaCameraView.setVisibility(CameraBridgeViewBase.VISIBLE); --> non so a cosa serva #TODO
        javaCameraView.setVisibility(SurfaceView.VISIBLE);
        javaCameraView.setCvCameraViewListener(this);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            imageView.setImageBitmap(photo);
        }
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mRGBA = new Mat(height, width, CvType.CV_8UC4);
        mGrey = new Mat();
    }

    @Override
    public void onCameraViewStopped() {
        mRGBA.release();
        mRGBAT.release(); //TODO???
        mGrey.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        //Core.flip(mRGBA.t(),  mRGBAT,1); ----> Distorce l'immagine, quindi non serve
        mRGBA = inputFrame.rgba();
        mGrey = inputFrame.gray();
        mRGBAT = mRGBA.t();

        Core.flip(mRGBA, mRGBAT, 1);
        Imgproc.resize(mRGBAT, mRGBAT, mRGBA.size());

        if (touched) {
            Log.e("TAP", "HA TAPPATO E SIAMO NELL'IF");

            //Potrebbe servire qui, prima del try...catch? -> mRGBATbitmap = Bitmap.createBitmap(javaCameraView.getWidth()/4,javaCameraView.getHeight()/4, Bitmap.Config.ARGB_8888);
            try {
                mRGBATbitmap = Bitmap.createBitmap(mRGBAT.cols(), mRGBAT.rows(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(mRGBAT, mRGBATbitmap);
                imageView.setImageBitmap(mRGBATbitmap);
                imageView.invalidate();
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
            touched = false;
        } else {
            Log.e("TAP", "NIENTE TAP");
        }

        return mRGBAT;

        /* //Face detection
        MatOfRect faceDetections = new MatOfRect();
        faceDetector.detectMultiScale(mRGBA, faceDetections);

        for (Rect rect : faceDetections.toArray()) {
            Imgproc.rectangle(
                    mRGBA,
                    new Point(rect.x, rect.y),
                    new Point(rect.x + rect.width, rect.y + rect.height),
                    new Scalar(255, 0, 0));
        }
        return mRGBA;
        */

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        touched = true;
        return true;
    }

    public boolean cliccato(View v) {
        touched = true;
        return true;
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (javaCameraView != null) {
            javaCameraView.disableView();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (javaCameraView != null) {
            javaCameraView.disableView();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (OpenCVLoader.initDebug()) {
            Log.d(TAG, "OpenCV is Configured or Connected Successfully.");
            baseLoaderCallback.onManagerConnected(BaseLoaderCallback.SUCCESS);
        } else {
            Log.d(TAG, "OpenCV not Working or Loaded");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, baseLoaderCallback);
        }
    }

}