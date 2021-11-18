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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.builder.AnimateBuilder;
import com.aldebaran.qi.sdk.builder.AnimationBuilder;
import com.aldebaran.qi.sdk.builder.ListenBuilder;
import com.aldebaran.qi.sdk.builder.PhraseSetBuilder;
import com.aldebaran.qi.sdk.builder.SayBuilder;
import com.aldebaran.qi.sdk.design.activity.RobotActivity;
import com.aldebaran.qi.sdk.design.activity.conversationstatus.SpeechBarDisplayPosition;
import com.aldebaran.qi.sdk.design.activity.conversationstatus.SpeechBarDisplayStrategy;
import com.aldebaran.qi.sdk.object.actuation.Animate;
import com.aldebaran.qi.sdk.object.actuation.Animation;
import com.aldebaran.qi.sdk.object.conversation.Listen;
import com.aldebaran.qi.sdk.object.conversation.ListenResult;
import com.aldebaran.qi.sdk.object.conversation.PhraseSet;
import com.aldebaran.qi.sdk.object.conversation.Say;
import com.aldebaran.qi.sdk.util.PhraseSetUtil;

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
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MultipartBody;

public class PepperClassifyingActivity extends RobotActivity implements RobotLifecycleCallbacks, View.OnTouchListener {//, CameraBridgeViewBase.CvCameraViewListener2 {

    private static String TAG = "PlayPepperClassifyingActivity";

    //Parte relativa alla fotocamera
    private Mat mRGBA, mRGBAT;
    private ImageView imageViewPepperPhoto;
    private static final int CAMERA_REQUEST = 1888;
    private static final int MY_CAMERA_REQUEST_CODE = 100; //Per la richiesta dei permessi della fotocamera //TODO Togliere?
    private boolean touched = false;
    private Bitmap mRGBATbitmap;
    private int activeCamera = CameraBridgeViewBase.CAMERA_ID_FRONT; //Attiva la fotocamera frontale (?)
    private String garbageType = null; //static
    byte wasteType = -1; //TODO Gestisci meglio la cosa dei tipi di spazzatura, magari con una lista
    static byte pepperScore, userScore;
    private String postUrl = "http://f43e-193-204-189-14.ngrok.io/handle_request"; //http://127.0.0.1:5000/handle_request";


    private boolean isThreadStarted = false;
    private String photoName = "PhotoPeppeRecycle.jpg";
    private String photoPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString() ;

    private TextView responseText;

    String wasteTypeString;
    boolean isPepperTurn = true;

    Map<String, Byte> scores = new HashMap<String, Byte>();
    /*
    private Mat mRGBA, mRGBAT, mGrey;
    private TextView responseText;
    private boolean loaded = false;
    */
    byte round;

    // Store the Animate action.
    private Animate animate;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        QiSDK.register(this, this);

        setContentView(R.layout.activity_pepper_classifying);

        //Per far sparire la barra grigia sopra
        setSpeechBarDisplayStrategy(SpeechBarDisplayStrategy.IMMERSIVE);
        setSpeechBarDisplayPosition(SpeechBarDisplayPosition.TOP);

        responseText = (TextView) findViewById(R.id.responseText); //Conterrà i mex che verranno stampati sotto al pulsante di connessione al server

        imageViewPepperPhoto = (ImageView) findViewById(R.id.imageViewPepperPhoto);
        /*textViewUserScore = findViewById(R.id.textViewUserScore);
        textViewPepperScore = findViewById(R.id.textViewPepperScore);*/

       /* if (checkPermissions()) {
            Log.d(TAG, "Permissions granted");
        } else {
            // prompt system dialog
            Log.d(TAG, "Permission prompt");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, MY_CAMERA_REQUEST_CODE); //RequestCode?
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 2);
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.MANAGE_EXTERNAL_STORAGE}, 3);
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 4); //??
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_NETWORK_STATE}, 5);//??
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, 6);
        }*/
        /* TODO
        cardboard cartone
        glass vetro
        metal metallo
        organic organico
        paper carta
        plastic plastica        */
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            round = extras.getByte("round");
            //scores = (Map<String, Byte>) getIntent().getSerializableExtra("scores");          //TODO Serializable(?)
            pepperScore = extras.getByte("pepperScore");
            userScore = extras.getByte("userScore");
            wasteType = extras.getByte("wasteType");
            wasteTypeString = extras.getString("wasteTypeString");
            isPepperTurn = extras.getBoolean("isPepperTurn");
        }
        //showScore();
/*        textViewPepperScore.setText(scores.get("score_pepper").toString());
        textViewUserScore.setText(scores.get("score_user1").toString());*/
    }

    /* void showScore () {*//*
        textViewPepperScore.setText(pepperScore);
        textViewUserScore.setText(userScore);*//*
        textViewPepperScore.setText("" + pepperScore);
        textViewUserScore.setText("" + userScore);
    }*/

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {

        return false;
    }

    @Override
    public void onRobotFocusGained(QiContext qiContext) {
        /*Say sayPepperThinking= SayBuilder.with(qiContext) // Create the builder with the context. //TODO scelta di una fra più frasi
                .withText("Grazie! Uhm, lasciami un attimo per pensare...") // Set the text to say.
                .build(); // Build the say action.
        //TODO Help ... in una dialog
        Animation pepperThinks = AnimationBuilder.with(qiContext)
                .withResources(R.raw.scratch_top_of_head_right_b001)
                .build();
        Animate animatePepperThinks = AnimateBuilder.with(qiContext)
                .withAnimation(pepperThinks).build();

        sayPepperThinking.run();
        animatePepperThinks.run();*/
        Say sayPepperSelectBin = SayBuilder.with(qiContext) // Create the builder with the context. //TODO scelta di una fra più frasi
                .withText("Ci sono!. \\rspd=95\\" + wasteTypeString) // Set the text to say.
                .build(); // Build the say action.
        Animation pepperSelectsBin = AnimationBuilder.with(qiContext)
                .withResources(R.raw.scratch_top_of_head_right_b001)
                .build();
        Animate animatePepperSelectBin = AnimateBuilder.with(qiContext)
                .withAnimation(pepperSelectsBin).build();

        sayPepperSelectBin.run();
        animatePepperSelectBin.run();

        askForConfirm();

    }

    @Override
    public void onRobotFocusLost() {

    }

    @Override
    public void onRobotFocusRefused(String reason) {

    }
/*

    // callback to be executed after the user has given approval or rejection via system prompt
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // camera can be turned on
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();

            } else {
                // camera will stay off
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
        //todo mancano storage e internet -> https://github.com/ahmedfgad/AndroidFlask/blob/master/Part%201/AndroidClient/app/src/main/java/gad/heartbeat/androidflask/easyupload/MainActivity.java
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
*/

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Unregister the RobotLifecycleCallbacks for this Activity.
        QiSDK.unregister(this, this);

    }

    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    void askForConfirm() {
        Intent activity2Intent = new Intent(PepperClassifyingActivity.this, JudgeConfirmActivity.class);
        activity2Intent.putExtra("wasteType", wasteType);
        activity2Intent.putExtra("round", round);
        activity2Intent.putExtra("isPepperTurn", isPepperTurn);
        //activity2Intent.putExtra("scores", (Serializable) scores); //TODO Serializable(?)
        activity2Intent.putExtra("pepperScore", pepperScore);
        activity2Intent.putExtra("userScore", userScore);

        startActivity(activity2Intent);
        finish();
        /* TODO Turno dell'utente:
        Schermata con i bidoni. L'utente deve selezionare il bidone corretto.
        Successivamente, c'è la schermata di richiesta conferma per il giudice
        Se la risposta è affermativa, l'utente guadagna un punto.
        */
    }
    /*private BaseLoaderCallback baseLoaderCallback = new BaseLoaderCallback(PepperClassifyingActivity.this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case BaseLoaderCallback.SUCCESS: {
                    break;
                }
                default: {
                    super.onManagerConnected(status);
                    break;
                }
            }
            super.onManagerConnected(status);
        }
    };*/

}