package com.example.pepperecycleandroid;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
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

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

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
    //private static final String[] PERMISSIONS =
    private static final int MY_CAMERA_REQUEST_CODE = 100; //Per la richiesta dei permessi della fotocamera
    private int activeCamera = CameraBridgeViewBase.CAMERA_ID_FRONT; //Attiva la fotocamera frontale (?)

    private static final int CAMERA_REQUEST = 1888;
    // CascadeClassifier faceDetector;
    // private CameraBridgeViewBase javaCameraView;
    private File cascFile;
    private Mat mRGBA, mRGBAT, mGrey;
    private boolean touched = false;
    private ImageView imageView, imageLoadedView;
    private TextView responseText;
    private Bitmap mRGBATbitmap;
    // private boolean useTopCamera = true; //TODO ->  FAI PARTE topcamera/frontcamera in modo che vada messo a false per la camera tablet

    private boolean loaded = false;
   // protected static String garbageType = null; //static
    private String garbageType = null; //static

    private String postUrl = "http://09ba-193-204-189-14.ngrok.io/handle_request"; //http://127.0.0.1:5000/handle_request";

    private boolean isThreadStarted = false;
    private String photoName = "PhotoPepper0.jpg";
    private String photoPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString() ;



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

        responseText = findViewById(R.id.responseText); //Conterrà i mex che verranno stampati sotto al pulsante di connessione al server

        //Parte relativa alla fotocamera
        javaCameraView = (JavaCameraView) findViewById(R.id.my_camera_view);

        this.imageView = (ImageView) this.findViewById(R.id.imageView);
        this.imageLoadedView = (ImageView) this.findViewById(R.id.imageLoadedView);
        // Button buttonCapture = (Button) findViewById(R.id.buttonCapture); // bottone "ecco"

        // Controlla se il permesso di accesso alla fotocamera è stato già chiesto in passato
        // if (useTopCamera || TODO capisci il funzionamento della fotocamera frontale/tablet di Pepper
       /* if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) == PackageManager.PERMISSION_GRANTED
        )*/ if (checkPermissions()) {
            Log.d(TAG, "Permissions granted");
            initializeCamera((JavaCameraView) javaCameraView, activeCamera);
        } else {
            // prompt system dialog
            Log.d(TAG, "Permission prompt");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, MY_CAMERA_REQUEST_CODE);
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.MANAGE_EXTERNAL_STORAGE}, 1);
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 4); //??
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_NETWORK_STATE}, 3);//??
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.INTERNET}, 2);
        }
    }

    private boolean checkPermissions(){
        Log.d(TAG, "Controllo permessi...");

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1); //TODO Come si sceglie il requestCode?
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.MANAGE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.MANAGE_EXTERNAL_STORAGE}, 1);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, 1);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_NETWORK_STATE}, 1);
        }
        return true;
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
            Log.e("TAG", "Immagine premuta, foto scattata.");

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

        }/* else {
            Log.e("TAG", "Immagine non premuta. No scatto.");
        }*/

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
        /*
        if (isThreadStarted) {
            thread.interrupt();
            isThreadStarted=false;
        }*/
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
        responseText.setText("Classificazione in corso...");

        responseText.setText(postUrl);

        MultipartBody.Builder multipartBodyBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888; //options.inPreferredConfig = Bitmap.Config.RGB_565;

        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        // Read BitMap by file path.
        Bitmap bitmap = mRGBATbitmap;
        try {
            //bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            //checkIfPhotoExists();
            savePhoto(mRGBATbitmap);
            //savePhotoRN7(mRGBATbitmap);
            responseText.setText("L'immagine dovrebbe esser stata salvata in:" + photoPath);
        } catch (Exception e) {
            responseText.setText("Errore. L'immagine non è stata catturata in modo corretto.");
            return;
        }

        loadPhoto();
        //loadPhotoRN7();

        try {
            //postRequest();
            /*if (//!thread.isAlive() &&
                    !isThreadStarted) { //Controllo se il thread è stato già attivato
                thread.start();
                isThreadStarted=true;
                thread.join();
            }*/

            ClientManager clientManager = new ClientManager(photoPath, postUrl,garbageType);
            Thread thread = new Thread(clientManager);
            thread.start();
            thread.join();
            garbageType = clientManager.getGarbageType();
            checkIfPhotoExists();

        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        responseText.setText("Tipo rifiuto:" + garbageType);
/*
        //TODO capisci dove mettere questo if
        if(garbageClassified) {
            responseText.setText("Tipo rifiuto:" + garbageType);
        }
*/

    }

    private void checkIfPhotoExists() {
        File myFile = new File(photoPath);

        if(myFile.exists()) {
            myFile.delete();
        }
    }

    private void savePhoto(Bitmap bmp) { //https://stackoverflow.com/questions/15662258/how-to-save-a-bitmap-on-internal-storage
        File pictureFile = getOutputMediaFile();

        if (pictureFile == null) {
            Log.d(TAG,
                    "Error creating media file, check storage permissions: ");// e.getMessage());
            return;
        }
        try {
            FileOutputStream fos = new FileOutputStream(pictureFile, false); //il "false" dovrebbe permettere di sovrascrivere l'immagine, se esiste
            bmp.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.close();
        } catch (FileNotFoundException e) {
            Log.d(TAG, "Impossibile salvare l'immagine. " + e.getMessage());
        } catch (IOException e) {
            Log.d(TAG, "Impossibile accedere al file: " + e.getMessage());
        }
        photoPath = "storage/emulated/0/DCIM/PhotoPepper0.jpg" ;//pictureFile.toString(); ///storage/emulated/0/DCIM/PhotoPepper0.jpg
    }

    //Create a File for saving an image or video
    private  File getOutputMediaFile(){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString());

        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                return null;
            }
        }
        // Create a media file name
        File mediaFile = new File(mediaStorageDir.getPath() + File.separator + photoName);
        return mediaFile;
    }

    private void loadPhoto() {
        File imgFile = new File(photoPath);

        if( imgFile.exists() ) {

            //Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            Bitmap myBitmap = BitmapFactory.decodeFile(photoPath);
            //TODO DECOMMENTA SE LO TOGLI DALL'ONCREATE ImageView myImage = (ImageView) findViewById(R.id.imageLoadedView);

            imageLoadedView.setImageBitmap(myBitmap);
            Log.d(TAG, "Immagine caricata dal path: " + photoPath);
        } else {
            Log.d(TAG, "Non è stata trovata nessuna immagine nel path." + photoPath);
        }
    }


    //Elimina tutta la cartella di PeppeRecycle contenente le foto scattate.
    private boolean deleteDir(File dir) { //File dir = new File(imagesDir);
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i=0; i<children.length; i++) {
                boolean success = deleteDir(dir);
                if (!success) {
                    return false;
                }
            }
        }

        return dir.delete();
    }
    private boolean garbageClassified = false;

    /*private Thread thread = new Thread() {
        @Override
        public void run() {
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();

            MediaType mediaType = MediaType.parse("text/plain");

            RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("image",photoPath,
                            RequestBody.create(MediaType.parse("application/octet-stream"),
                                    new File(photoPath)))
                    .build();

            Request request = new Request.Builder()
                    .url(postUrl)
                    .method("POST", body)
                    .build();

            try {
                Response response = client.newCall(request).execute();
                garbageType =  response.body().string();
                System.out.println("Tipo di rifiuto: " + garbageType);
                //responseText.setText("Tipo rifiuto:" + garbageType); TODO È nel posto sbagliato
                garbageClassified = true;
                Log.d("Classificazione", "Classificazione riuscita.");

            } catch (IOException e) {
                e.printStackTrace();
                Log.d("Classificazione", "Classificazione non riuscita.");
            }
        }
    };*/
    /*private Thread threadProvaInvioStringa = new Thread() {
        @Override
        public void run() {
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            MediaType mediaType = MediaType.parse("text/plain");
            RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("String", "Pinco Pallino")
                    .build();
            Request request = new Request.Builder()
                    .url("postUrl")
                    .method("POST", body)
                    .build();
            try {
                Response response = client.newCall(request).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };*/

    void postReqText() {
        OkHttpClient client = new OkHttpClient();//.newBuilder().build();

        RequestBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                //.addFormDataPart("image",Environment.getExternalStorageDirectory().toString() + photoName,
                .addFormDataPart("image", photoName,
                        //RequestBody.create(MediaType.parse("application/octet-stream"),
                        RequestBody.create(
                                MediaType.parse("image/jpeg"),
                                new File(photoPath)))
                .build();

        Request request = new Request.Builder()
                .url(postUrl)
                .post(body)
                //.method("POST", body)
                .build();

        try {
            Response response = client.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void postRequest() {
        OkHttpClient client = new OkHttpClient().newBuilder().build();

        // TODO è normale che non sia ? ->  MediaType mediaType = MediaType.parse("image/jpeg");
        MediaType mediaType = MediaType.parse("text/plain");

        RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                //.addFormDataPart("image",Environment.getExternalStorageDirectory().toString() + photoName,
                .addFormDataPart("image",photoPath,
                        //RequestBody.create(MediaType.parse("application/octet-stream"),
                        RequestBody.create(MediaType.parse("image/*jpg"),
                                //new File(Environment.getExternalStorageDirectory().toString() + photoName)))
                                new File(photoPath)))
                .build();

        Request request = new Request.Builder()
                .url(postUrl)
                .method("POST", body)
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
        /* //TODO parte funzionante... rimettila se togli da "client.newcall..."
                try {
                    Response response = client.newCall(request).execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        */
    }
        /*TODO Si può eliminare, non dovrebbe più servire.
    void postRequest(String postUrl, RequestBody postBody) {
        OkHttpClient client = new OkHttpClient().newBuilder() .build();

        MediaType mediaType = MediaType.parse("text/plain");

        RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("image",Environment.getExternalStorageDirectory().getAbsolutePath() + "/PeppeRecycleAndroid/PhotoAndroid.jpg",
                        RequestBody.create(MediaType.parse("application/octet-stream"),
                                new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/PeppeRecycleAndroid/PhotoAndroid.jpg")))
                .build();

        Request request = new Request.Builder()
                .url(postUrl)
                .method("POST", body)
                .build();

        try {
            Response response = client.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        *//*OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(postUrl)
                .post(postBody)
                .build();*//*

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
*/

    //TODO: Da qui in poi ci sono problemi, credo si possa eliminare tutto ma tengo perché non si sa mai
    String imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM + "/PeppeRecycle").toString()
            /*+ File.separator
            + "PeppeRecycle"*/;
    //getting real path from uri
    private String getFilePath(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};

        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(projection[0]);
            String picturePath = cursor.getString(columnIndex); // returns null
            cursor.close();
            return picturePath;
        }
        return null;
    }

    private void savePhotoRN7(Bitmap bmp) {

        //Creazione cartella PeppeRecycle in DCIM, se non esiste
        File dir = new File(imagesDir);
        if(dir.mkdir()) {
            System.out.println("Directory created");
        } else {
            System.out.println("Directory is not created");
        }

        boolean saved;
        OutputStream fos = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Uri imageUri = Uri.parse(imagesDir);
            File fdelete = new File(getFilePath(imageUri));

            if (fdelete.exists()) {
                if (fdelete.delete()) {
                    System.out.println("file Deleted :" );
                } else {
                    System.out.println("file not Deleted :");
                }
            }

            ContentResolver resolver = this.getContentResolver();
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, photoName);
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, "DCIM/PeppeRecycle");
            /*Uri */imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
            try {
                fos = resolver.openOutputStream(imageUri);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else {


            //Environment.getExternalStoragePublicDirectory( Environment.DIRECTORY_DCIM).toString(); //+ File.separator
            //File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString());;//new File(imagesDir);
            /*File file = new File(imagesDir); //directory dove si troveranno le foto scattate

            if (!file.exists()) { //Crea la directory se non esiste
                file.mkdir();
                // If you require it to make the entire directory path including parents,
                // use directory.mkdirs(); here instead.
            }
            //File image = new File(this.getCacheDir(), photoName);
            */
            File image = new File(dir, photoName);
            //Controlla se esiste già un file con lo stesso nome. Se sì, lo elimina
            if(!image.exists()){
                try {
                    image.createNewFile();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }else{
                image.delete();
                try {
                    image.createNewFile();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            try {
                fos = new FileOutputStream(image, false); //il false dovrebbe permettere di sovrascrivere l'immagine, se esiste già
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

        }
        saved = bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        try {
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadPhotoRN7() {

        File imgFile = new File(imagesDir);
        if (imgFile.exists())
        {
            Bitmap bitmap = BitmapFactory.decodeFile(imagesDir);//imgFile.getAbsolutePath());
            imageLoadedView.setImageBitmap(bitmap);
            Log.d("TAG", "File trovato!");
        }
        else
        {
            Log.d("TAG", "Non è stato trovato il file nel path:" + imagesDir);
        }


    }
}