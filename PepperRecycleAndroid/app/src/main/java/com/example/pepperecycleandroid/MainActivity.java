package com.example.pepperecycleandroid;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.design.activity.RobotActivity;
import com.example.pepperrecycleandroid.R;

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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends RobotActivity implements RobotLifecycleCallbacks, CameraBridgeViewBase.CvCameraViewListener2, View.OnTouchListener  {
    // Store the Animate action.
    // private Animate animate;
    private static String TAG = "MainActivity";

    //private CameraBridgeViewBase javaCameraView;
    private JavaCameraView javaCameraView;

    private static final int MY_CAMERA_REQUEST_CODE = 100; //Per la richiesta dei permessi della fotocamera
    int activeCamera = CameraBridgeViewBase.CAMERA_ID_FRONT; //Attiva la fotocamera frontale (?)

    private static final int CAMERA_REQUEST = 1888;
    CascadeClassifier faceDetector;
    //private CameraBridgeViewBase javaCameraView;
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
                    /*InputStream is = getResources().openRawResource(R.raw.haarcascade_frontalface_alt2);

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
                    }*/ //Per il test di face detection. Non funzionerà perché manca il classificatore negli assets.

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Register the RobotLifecycleCallbacks to this Activity.
        QiSDK.register(this, this);

        setContentView(R.layout.activity_main);
        //Parte relativa alla fotocamera
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

        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.INTERNET}, 2);
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);

        //TODO Da eliminare, non serve (?)
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
        //todo mancano storage e internet -> https://github.com/ahmedfgad/AndroidFlask/blob/master/Part%201/AndroidClient/app/src/main/java/gad/heartbeat/androidflask/easyupload/MainActivity.java
    }

    private void initializeCamera(JavaCameraView javaCameraView, int activeCamera) {
        javaCameraView.setCameraPermissionGranted();
        javaCameraView.setCameraIndex(activeCamera);

        javaCameraView.setVisibility(CameraBridgeViewBase.VISIBLE); // --> non so a cosa serva #TODO
        //javaCameraView.setVisibility(SurfaceView.INVISIBLE); //TODO Era SurfaceView.VISIBLE
        javaCameraView.setCvCameraViewListener(this);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_REQUEST
                && resultCode == Activity.RESULT_OK
                // && requestCode == RESULT_OK
                && data != null) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            imageView.setImageBitmap(photo);


            Uri uri = data.getData();

            //Commentato perché non serve caricare un'immagine
            /*selectedImagePath = getPath(getApplicationContext(), uri);
            EditText imgPath = findViewById(R.id.imgPath);
            imgPath.setText(selectedImagePath);
            Toast.makeText(getApplicationContext(), selectedImagePath, Toast.LENGTH_LONG).show();*/

        }
        /*if(requestCode == RESULT_OK && data != null) {
            Uri uri = data.getData();

            selectedImagePath = getPath(getApplicationContext(), uri);
            EditText imgPath = findViewById(R.id.imgPath);
            imgPath.setText(selectedImagePath);
            Toast.makeText(getApplicationContext(), selectedImagePath, Toast.LENGTH_LONG).show();
        }*/
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

                //Salvo l'immagine:
                try (FileOutputStream out = new FileOutputStream("Android_Flask")) {
                    mRGBATbitmap.compress(Bitmap.CompressFormat.JPEG, 100, out); // bmp is your Bitmap instance
                    // PNG is a lossless format, the compression factor (100) is ignored
                } catch (IOException e) {
                    e.printStackTrace();
                }

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

    public void savePhoto(Bitmap bmp) {
        String file_path = Environment.getExternalStorageDirectory().getAbsolutePath() +
                "/PeppeRecycleAndroid";
        File dir = new File(file_path);
        if(!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(dir, "PhotoPepper.jpg");
        try (FileOutputStream fOut = new FileOutputStream(file)) {
            bmp.compress(Bitmap.CompressFormat.JPEG, 90, fOut);
            fOut.flush();
            fOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        // Unregister the RobotLifecycleCallbacks for this Activity.
        QiSDK.unregister(this, this);

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

    @Override
    public void onRobotFocusGained(QiContext qiContext) {

    }

    @Override
    public void onRobotFocusLost() {

    }

    @Override
    public void onRobotFocusRefused(String reason) {

    }

    public void connectServer(View v) {
        TextView responseText = findViewById(R.id.responseText); //Conterrà i mex che verranno stampati sotto al pulsante di connessione al server

        responseText.setText("Classificazione in corso...");

        String postUrl = "http://127.0.0.1:5000/handle_request/";
        responseText.setText(postUrl);

        MultipartBody.Builder multipartBodyBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888; //options.inPreferredConfig = Bitmap.Config.RGB_565;

        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        // Read BitMap by file path.
        Bitmap bitmap = mRGBATbitmap;
        try {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        } catch(Exception e){
            responseText.setText("Errore. L'immagine non è stata catturata in modo corretto.");
            return;
        }
        byte[] byteArray = stream.toByteArray();

        multipartBodyBuilder.addFormDataPart(
                "image",
                "Android_Flask.jpg",
                RequestBody.create(MediaType.parse("image/jpg"), byteArray)); //RequestBody.create(MediaType.parse("image/*jpg"), byteArray));


        RequestBody postBodyImage = multipartBodyBuilder.build();

        postRequest(postUrl, postBodyImage);
    }

    void postRequest(String postUrl, RequestBody postBody) {

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(postUrl)
                .post(postBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Cancel the post on failure.
                call.cancel();
                Log.d("FAIL", e.getMessage());

                // In order to access the TextView inside the UI thread, the code is executed inside runOnUiThread()
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView responseText = findViewById(R.id.responseText);
                        responseText.setText("Connessione al server fallita. Per favore, riprova.");
                    }
                });
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                // In order to access the TextView inside the UI thread, the code is executed inside runOnUiThread()
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView responseText = findViewById(R.id.responseText);
                        try {
                            responseText.setText("Risposta del server:\n" + response.body().string());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

}