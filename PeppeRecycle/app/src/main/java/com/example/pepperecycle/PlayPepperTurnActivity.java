package com.example.pepperecycle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.design.activity.RobotActivity;
import com.aldebaran.qi.sdk.design.activity.conversationstatus.SpeechBarDisplayPosition;
import com.aldebaran.qi.sdk.design.activity.conversationstatus.SpeechBarDisplayStrategy;

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

import okhttp3.MultipartBody;

public class PlayPepperTurnActivity extends RobotActivity implements RobotLifecycleCallbacks, View.OnTouchListener, CameraBridgeViewBase.CvCameraViewListener2 {
    private static String TAG = "PlayPepperTurnActivity";

    //Parte relativa alla fotocamera
    private JavaCameraView javaCameraView;
    private Mat mRGBA, mRGBAT;
    private ImageView imageViewPepperPhoto;
    private static final int CAMERA_REQUEST = 1888;
    private static final int MY_CAMERA_REQUEST_CODE = 100; //Per la richiesta dei permessi della fotocamera //TODO Togliere?
    private boolean touched = false;
    private Bitmap mRGBATbitmap;
    private int activeCamera = CameraBridgeViewBase.CAMERA_ID_FRONT; //Attiva la fotocamera frontale (?)
    private String garbageType = null; //static

    private String postUrl = "http://538c-193-204-189-14.ngrok.io/handle_request"; //http://127.0.0.1:5000/handle_request";

    private boolean isThreadStarted = false;
    private String photoName = "PhotoPeppeRecycle.jpg";
    private String photoPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString() ;

    private TextView responseText;
    /*
    private Mat mRGBA, mRGBAT, mGrey;
    private TextView responseText;
    private boolean loaded = false;
    */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_pepper_turn);

        //Per far sparire la barra grigia sopra
        setSpeechBarDisplayStrategy(SpeechBarDisplayStrategy.IMMERSIVE);
        setSpeechBarDisplayPosition(SpeechBarDisplayPosition.TOP);

        javaCameraView = (JavaCameraView) findViewById(R.id.my_camera_view);
        responseText = (TextView) findViewById(R.id.responseText); //Conterrà i mex che verranno stampati sotto al pulsante di connessione al server
        imageViewPepperPhoto = (ImageView) findViewById(R.id.imageViewPepperPhoto);

        if (checkPermissions()) {
            Log.d(TAG, "Permissions granted");
            initializeCamera((JavaCameraView) javaCameraView, activeCamera);
        } else {
            // prompt system dialog
            Log.d(TAG, "Permission prompt");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, MY_CAMERA_REQUEST_CODE); //RequestCode?
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 2);
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.MANAGE_EXTERNAL_STORAGE}, 3);
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 4); //??
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_NETWORK_STATE}, 5);//??
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, 6);
        }
        /* TODO
        cardboard cartone
        glass vetro
        metal metallo
        organic organico
        paper carta
        plastic plastica        */
    }

    public void buttonHome(View v) { //Pressione tasto "torna alla Home" TODO Togli perché è un duplicato? [???]
        Intent activity2Intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(activity2Intent); //Per andare alla pagina principale
        finish();
    }

    public void buttonClose(View v) { //Pressione tasto "Chiudi" TODO Togli perché è un duplicato? [???]
        finish();/*Intent activity2Intent = new Intent(getApplicationContext(), TodoActivity.class);
        startActivity(activity2Intent); //TODO Chiudi gioco*/
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        return false;
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
        javaCameraView.setCvCameraViewListener(PlayPepperTurnActivity.this);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_REQUEST
                && resultCode == Activity.RESULT_OK
                // && requestCode == RESULT_OK
                && data != null) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            imageViewPepperPhoto.setImageBitmap(photo);


            Uri uri = data.getData();

        }
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mRGBA = new Mat(height, width, CvType.CV_8UC4);
    }

    @Override
    public void onCameraViewStopped() {
        if (mRGBA != null)
            mRGBA.release();
        if (mRGBAT != null)
            mRGBAT.release(); //TODO???
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRGBA = inputFrame.rgba();
        mRGBAT = mRGBA.t();

        Core.flip(mRGBA, mRGBAT, 1);
        Imgproc.resize(mRGBAT, mRGBAT, mRGBA.size());

        /*if (touched) {*/
        Log.e("TAG", "Immagine premuta, foto scattata.");

        //Potrebbe servire qui, prima del try...catch? -> mRGBATbitmap = Bitmap.createBitmap(javaCameraView.getWidth()/4,javaCameraView.getHeight()/4, Bitmap.Config.ARGB_8888);
        try {
            mRGBATbitmap = Bitmap.createBitmap(mRGBAT.cols(), mRGBAT.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(mRGBAT, mRGBATbitmap);
            imageViewPepperPhoto.setImageBitmap(mRGBATbitmap);
            imageViewPepperPhoto.invalidate();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
            /* touched = false;

        }else {
            Log.e("TAG", "Immagine non premuta. No scatto.");
        }*/

        return mRGBAT;
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

    /*@Override
    public boolean onTouch(View v, MotionEvent event) {
        touched = true;
        return true;
    }*/

    public boolean cliccato(View v) {
        touched = true;
        return true;
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
    public void classify(View v) {
        responseText.setText("Classificazione in corso...");

        responseText.setText(postUrl);

        MultipartBody.Builder multipartBodyBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888; //options.inPreferredConfig = Bitmap.Config.RGB_565;

        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        // Read BitMap by file path.
        Bitmap bitmap = mRGBATbitmap;
        try {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream); //TODO Rimuovi (o no?)
            //checkIfPhotoExists();
            savePhoto(mRGBATbitmap);
            responseText.setText("L'immagine dovrebbe esser stata salvata in:" + photoPath);
        } catch (Exception e) {
            responseText.setText("Errore. L'immagine non è stata catturata in modo corretto.");
            return;
        }
        javaCameraView.disableView(); //setVisibility(View.INVISIBLE); // Rende la cam invisibile
        loadPhoto(imageViewPepperPhoto); // Carica l'immagine nell'ImageView passata come parametro

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
        photoPath = "storage/emulated/0/DCIM/" + photoName; //pictureFile.toString(); ///storage/emulated/0/DCIM/PhotoPepper0.jpg
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

    private void loadPhoto(ImageView imageViewPepperPhoto) {
        File imgFile = new File(photoPath);

        if( imgFile.exists() ) {
            //Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            Bitmap myBitmap = BitmapFactory.decodeFile(photoPath);
            //TODO DECOMMENTA SE LO TOGLI DALL'ONCREATE ImageView myImage = (ImageView) findViewById(R.id.imageViewPepperPhoto);

            imageViewPepperPhoto.setImageBitmap(myBitmap);
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

    private BaseLoaderCallback baseLoaderCallback = new BaseLoaderCallback(PlayPepperTurnActivity.this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case BaseLoaderCallback.SUCCESS: {
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

}