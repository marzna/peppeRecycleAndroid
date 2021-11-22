package com.example.pepperecycle;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MultipartBody;

public class PlayPepperTurnActivity extends RobotActivity implements RobotLifecycleCallbacks, View.OnTouchListener, CameraBridgeViewBase.CvCameraViewListener2 {
    private static String TAG = "PlayPepperTurnActivity";

    // Indirizzo del server
    private String postUrl = "http://efdc-193-204-189-14.ngrok.io/handle_request"; //http://127.0.0.1:5000/handle_request";

    //Parte relativa alla fotocamera
    private JavaCameraView javaCameraView;
    private Mat mRGBA, mRGBAT;
    private ImageView imageViewPepperPhoto;
    private static final int CAMERA_REQUEST = 1888;
    private static final int MY_CAMERA_REQUEST_CODE = 100; //Per la richiesta dei permessi della fotocamera //TODO Togliere?
    private boolean touched = false;
    private Bitmap mRGBATbitmap;
    private int activeCamera = CameraBridgeViewBase.CAMERA_ID_FRONT; //Attiva la fotocamera frontale (?)
    private String garbageType; //static
    byte wasteType = -1; //TODO Gestisci meglio la cosa dei tipi di spazzatura, magari con una lista
    static byte pepperScore, userScore;
    private boolean tutorialEnabled;
    byte trialState;

    boolean roundTutorial;
    boolean restartGame;
    ProgressBar pbSavePhoto;

    private boolean isThreadStarted = false;
    private String photoName = "PhotoPeppeRecycle.jpg"; // nome con cui la foto sarà salvata temporaneamente in memoria
    private String photoPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString() ;

    private TextView responseText;

    static final byte TYPE_ORGANIC = 0;
    static final byte TYPE_PAPER_CARDBOARD = 1;
    static final byte TYPE_PLASTIC_METAL = 2;
    static final byte TYPE_GLASS = 3;
    static final byte TYPE_PAPER = 4;
    static final byte TYPE_CARDBOARD = 5;
    static final byte TYPE_PLASTIC = 6;
    static final byte TYPE_METAL = 7;
    static final byte TYPE_WASTE = -1;
    static final byte CLASSIFICATION_ERROR = -1;

    static final String STRING_ORGANIC = "Organico";
    static final String STRING_PAPER_CARDBOARD = "Carta e cartone";
    static final String STRING_PLASTIC_METAL = "Plastica e metalli";
    static final String STRING_GLASS = "Vetro";
    static final String STRING_CLASSIFICATION_ERROR = "Si è verificato un errore";
    String wasteTypeString;

    TextView textViewUserScore, textViewPepperScore, tvTutorialPepper;
    boolean isPepperTurn = true;
    boolean canTakePhoto, photoTaken, takePictureSaid;

    QiContext qiContxt;
    Map<String, Byte> scores = new HashMap<String, Byte>();
    /*
    private Mat mRGBA, mRGBAT, mGrey;
    private TextView responseText;
    private boolean loaded = false;
    */
    byte round;
    byte currentRound;
    boolean classified=false;
    // Store the Animate action.
    private Animate animate;
    Button buttonTakePicture;
    boolean endOfTutorial;
    byte tutorialState;

    MediaPlayer mediaPlayer;

    ImageView imageViewUserScore,imageViewPepperScore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        QiSDK.register(this, this);
        setContentView(R.layout.activity_play_pepper_turn);
        //Per far sparire la barra grigia sopra
        setSpeechBarDisplayStrategy(SpeechBarDisplayStrategy.IMMERSIVE);
        setSpeechBarDisplayPosition(SpeechBarDisplayPosition.TOP);

        buttonTakePicture = (Button) findViewById(R.id.buttonTakePicture);
        javaCameraView = (JavaCameraView) findViewById(R.id.my_camera_view);
        responseText = (TextView) findViewById(R.id.responseText); //Conterrà i mex che verranno stampati sotto al pulsante di connessione al server
        imageViewPepperPhoto = (ImageView) findViewById(R.id.imageViewPepperPhoto);
        textViewUserScore = findViewById(R.id.textViewUserScore);
        textViewPepperScore = findViewById(R.id.textViewPepperScore);
        tvTutorialPepper = findViewById(R.id.tvTutorialPepper);
        imageViewUserScore = findViewById(R.id.imageViewUserScore);
        imageViewPepperScore = findViewById(R.id.imageViewPepperScore);
        pbSavePhoto = findViewById(R.id.pbSavePhoto);

        photoTaken = false;
        classified = false;
        canTakePhoto = false;
        roundTutorial = false;
        restartGame = false;
        takePictureSaid = false;
        wasteTypeString = "";

        mediaPlayer = MediaPlayer.create(this, R.raw.ticking_clock);

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
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            round = extras.getByte("round");
            //scores = (Map<String, Byte>) getIntent().getSerializableExtra("scores");          //TODO Serializable(?)
            pepperScore = extras.getByte("pepperScore");
            userScore = extras.getByte("userScore");
            tutorialEnabled = extras.getBoolean("tutorialEnabled");
            roundTutorial = extras.getBoolean("roundTutorial");
            currentRound = extras.getByte("currentRound");
            trialState = extras.getByte("trialState");
            restartGame = extras.getBoolean("restartGame");
            endOfTutorial = extras.getBoolean("endOfTutorial");
            tutorialState = extras.getByte("tutorialState");
            Log.d(TAG, "Ricevuto trialState: "+ trialState);




        } else {
            Log.d(TAG, "NON ricevuto trialState: " + trialState);
        }


        if(trialState == 1) {
            tvTutorialPepper.setVisibility(View.VISIBLE);
            textViewUserScore.setVisibility(View.INVISIBLE);
            textViewPepperScore.setVisibility(View.INVISIBLE);
            imageViewUserScore.setVisibility(View.INVISIBLE);
            imageViewPepperScore.setVisibility(View.INVISIBLE);
        /*}  else if (trialState == 0) {
             textViewUserScore.setVisibility(View.INVISIBLE);
                textViewPepperScore.setVisibility(View.INVISIBLE);
                imageViewUserScore.setVisibility(View.INVISIBLE);
                imageViewPepperScore.setVisibility(View.INVISIBLE);
            tvTutorialPepper.setVisibility(View.INVISIBLE);
            */
        } else {
            tvTutorialPepper.setVisibility(View.INVISIBLE);
            textViewUserScore.setVisibility(View.VISIBLE);
            textViewPepperScore.setVisibility(View.VISIBLE);
            imageViewUserScore.setVisibility(View.VISIBLE);
            imageViewPepperScore.setVisibility(View.VISIBLE);
        }

        showScore();

/*        textViewPepperScore.setText(scores.get("score_pepper").toString());
        textViewUserScore.setText(scores.get("score_user1").toString());*/
    }

    void showScore () {
        textViewPepperScore.setText("" + pepperScore);
        textViewUserScore.setText("" + userScore);
    }
    public void buttonHome(View v) { //Pressione tasto "torna alla Home" TODO Togli perché è un duplicato? [???]
        Intent activity2Intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(activity2Intent); //Per andare alla pagina principale
        finish();
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {

        return false;
    }
    @Override
    public void onRobotFocusGained(QiContext qiContext) {
        setQiContext(qiContext);

        Say sayPepperTurn = SayBuilder.with(qiContext) // Create the builder with the context. //TODO scelta di una fra più frasi
                .withText("Iniziamo dal mio turno!") // Set the text to say.
                .build(); // Build the say action.

        Say sayPepperTurnTutorial = SayBuilder.with(qiContext) // Create the builder with the context.
                .withText("Aspetto che mi mostri il rifiuto e prema il pulsante sul mio tablet o pronunci la parola ECCO, così da permettermi di vedere il rifiuto." ) // Set the text to say.
                .build(); // Build the say action.;

        Say showGarbage = SayBuilder.with(qiContext) // Create the builder with the context. //TODO scelta di una fra più frasi
                .withText("Per piacere, posso vedere il rifiuto da riciclare?") // Set the text to say.
                .build(); // Build the say action.

        //TODO Help ... in una dialog
        Animation pepperTurn = AnimationBuilder.with(qiContext)
                .withResources(R.raw.show_self_a001)
                .build();
        Animate animatePepperTurn = AnimateBuilder.with(qiContext)
                .withAnimation(pepperTurn).build();

        PhraseSet phraseSetYes = PhraseSetBuilder.with(qiContext)
                .withTexts("Sì Pepper", "Si Pepper", "Ecco", "Ecco qui", "Ecco Pepper",
                        "Ecco qui Pepper", "Tieni", "Tieni Pepper", "Pepper tieni",
                        "Sì", "Si", "okay", "ok", "va bene", "pepper ecco", "pepper si")
                .build();

        PhraseSet phraseSetNo = PhraseSetBuilder.with(qiContext)
                .withTexts("No", "Non voglio", "No Pepper", "Non mi va").build();

        PhraseSet phraseSetRepeat = PhraseSetBuilder.with(qiContext)
                .withTexts("Ripeti", "Ricominciamo", "Ricomincia", "Da capo",
                        "Non ho capito", "Puoi ripetere")
                .build();

        PhraseSet phraseSetHome = PhraseSetBuilder.with(qiContext)
                .withTexts("Torna", "Indietro", "Home")
                .build();

        PhraseSet phraseSetClose = PhraseSetBuilder.with(qiContext)
                .withTexts("Chiudi il gioco", "Esci", "Basta", "Stop")
                .build();

        if(currentRound==0 && tutorialState==-1)
            sayPepperTurn.run();
        if (trialState == 1) { // if (tutorialEnabled) {
            sayPepperTurnTutorial.run();
        }
        showGarbage.run();
        Log.d(TAG, "TrialState: " + trialState);
        //animatePepperTurn.run(); //rimosso perché sennò ci sono troppi movimenti

        Listen listenPlay = ListenBuilder
                .with(qiContext)
                .withPhraseSets(phraseSetYes, phraseSetNo, phraseSetRepeat, phraseSetClose, phraseSetHome)
                .build();
        ListenResult listenResult = listenPlay.run();
        PhraseSet matchedPhraseSet = listenResult.getMatchedPhraseSet();

        if (PhraseSetUtil.equals(matchedPhraseSet, phraseSetYes)) {             // Utente mostra l'oggetto a Pepper

            Log.d(TAG, "OnRobotFocusGained: matched yes");
            mediaPlayer.start();
            takePictureSaid = true;

            savePhoto(mRGBATbitmap);


            /*SavePhoto savePhoto = new SavePhoto(mRGBATbitmap);
            savePhoto.execute();*/


//            buttonTakePicture.performClick();

            Log.d(TAG, "after phraseSetYes: takePictureSaid: " + true + "\tphotoTaken: " + photoTaken);
            try {
                Log.d(TAG, "onrobotfocusgained clientManager lanciato.");
                ClientManager clientManager = new ClientManager(photoPath, postUrl, garbageType);
                Thread thread = new Thread(clientManager);
                thread.start();
                thread.join();
                garbageType = clientManager.getGarbageType();
                responseText.setText("Tipo rifiuto:" + garbageType);
                Log.d(TAG, "onrobotfocusgained. Ottenuto garbagetype: " + garbageType);
                classified = true;
                Log.d(TAG, "onrobotfocusgained. setwastetype");
                setWasteType();
                Log.d(TAG, "onrobotfocusgained. checkifphotoexists");
                checkIfPhotoExists();
                Log.d(TAG, "onrobotfocusgained. askforconfirm");
                askForConfirm();
                mediaPlayer.stop();
            /*else {
                    restartActivity();
                }*/
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
                        /*responseText.setText("Tipo rifiuto:" + garbageType);
                setWasteType();
                checkIfPhotoExists();
            */


           /* if(!classified) {
                classify();
                askForConfirm();
            } else {
                Log.e("CLASSIF", "ERRORE di classificazione.");
            }*/
            /*Say sayCapture = SayBuilder.with(qiContext) // Create the builder with the context.
                    .withText("Bene, allora aspetto che il giudice mi mostri il rifiuto e prema il pulsante sul mio tablet, così da permettermi di vedere il rifiuto! ." ) // Set the text to say.
                    .build(); // Build the say action.;
            sayCapture.run();*/
            //            setQiContext(qiContext);
            /*Say sayPepperThinks = SayBuilder.with(qiContext) // Create the builder with the context. //TODO scelta di una fra più frasi
                    .withText("Grazie!. Uhm, fammi pensare...") // Set the text to say.
                    .build();
            Animation pepperThinks = AnimationBuilder.with(qiContext)
                    .withResources(R.raw.scratch_top_of_head_right_b001)
                    .build();
            Animate animatePepperThinks = AnimateBuilder.with(qiContext)
                    .withAnimation(pepperThinks).build();

            animatePepperThinks.run();
            sayPepperThinks.run();*/
//            this.buttonTakePicture.performClick();
            /*if(!classified) {
                classify();
                askForConfirm();
            } else {
                Log.e("CLASSIF", "ERRORE di classificazione.");
            }*/


            /*Say sayPepperSelectBin = SayBuilder.with(qiContext) // Create the builder with the context. //TODO scelta di una fra più frasi
                    .withText("Ci sono!" + wasteTypeString) // Set the text to say.
                    .build(); // Build the say action.
            Animation pepperSelectsBin = AnimationBuilder.with(qiContext)
                    .withResources(R.raw.scratch_top_of_head_right_b001)
                    .build();
            Animate animatePepperSelectBin = AnimateBuilder.with(qiContext)
                    .withAnimation(pepperSelectsBin).build();

            sayPepperSelectBin.run();
            animatePepperSelectBin.run();*/

            /*goToClassAct();*/

        } else if (PhraseSetUtil.equals(matchedPhraseSet, phraseSetNo)) {      // Utente non vuole mostrare l'oggetto a Pepper

            Log.d(TAG, "OnRobotFocusGained: matched no");
            // TODO chiede se si vuole interrompere il gioco
            // Say sayPepperStopGame= SayBuilder.with(qiContext) // Create the builder with the context. //TODO scelta di una fra più frasi
            Say sayRepeat = SayBuilder.with(qiContext) // Create the builder with the context. //TODO scelta di una fra più frasi
                    .withText("Mi dispiace, ma senza questo procedimento, io non posso giocare. ") // Set the text to say.
                    .build(); // Build the say action.
            // sayPepperStopGame.run();
            sayRepeat.run();
            restartActivity();

        } else if (PhraseSetUtil.equals(matchedPhraseSet, phraseSetRepeat)) {   // Richiesta utente di ripetere il gioco dall'inizio
            Log.d(TAG, "OnRobotFocusGained: matched repeat");

            Animation correctAnswer = AnimationBuilder.with(qiContext)
                    .withResources(R.raw.coughing_left_b001).build();
            Animate animateCorrect = AnimateBuilder.with(qiContext)
                    .withAnimation(correctAnswer).build();
            animateCorrect.run();
            Intent activity2Intent = new Intent(getApplicationContext(), PlayIntroActivity.class);
            startActivity(activity2Intent); //Per ripetere
            finish();

        } else if (PhraseSetUtil.equals(matchedPhraseSet, phraseSetHome)) {     // Torna alla home
            Log.d(TAG, "OnRobotFocusGained: matched home");

            Animation correctAnswer = AnimationBuilder.with(qiContext)
                    .withResources(R.raw.affirmation_a002).build();
            Animate animateCorrect = AnimateBuilder.with(qiContext)
                    .withAnimation(correctAnswer).build();
            animateCorrect.run();
            Intent activity2Intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(activity2Intent); //Per iniziare il gioco.
            finish();

        } else if (PhraseSetUtil.equals(matchedPhraseSet, phraseSetClose)) {    // Chiude il gioco
            Log.d(TAG, "OnRobotFocusGained: matched close");

            Animation correctAnswer = AnimationBuilder.with(qiContext)
                    .withResources(R.raw.hello_a004).build();
            animate = AnimateBuilder.with(qiContext)
                    .withAnimation(correctAnswer).build();

            Say sayGoodbye = SayBuilder.with(qiContext) // Create the builder with the context.
                    .withText("Va bene, sto chiudendo il gioco. Spero di rivederti presto!") // Set the text to say.
                    .build(); // Build the say action.

            sayGoodbye.run();
            animate.run();

            finishAffinity();

        }
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
//        Log.e("TAG", "Immagine premuta, foto scattata.");

        /*if (touched) {*/

        //Potrebbe servire qui, prima del try...catch? -> mRGBATbitmap = Bitmap.createBitmap(javaCameraView.getWidth()/4,javaCameraView.getHeight()/4, Bitmap.Config.ARGB_8888);
        try {
            mRGBATbitmap = Bitmap.createBitmap(mRGBAT.cols(), mRGBAT.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(mRGBAT, mRGBATbitmap);
            imageViewPepperPhoto.setImageBitmap(mRGBATbitmap);

            if(takePictureSaid && !photoTaken) { //Se l'utente ha espresso il comando vocale e la foto non è stata scattata
                Log.d(TAG, "(takePictureSaid && !photoTaken)==true");
                savePhoto(mRGBATbitmap);
//                takePictureSaid=false; TODO ELIMINA RIGA
                photoTaken = true;
                Log.d(TAG, "Foto scattata nell'oncameraframe");
//                javaCameraView.disableView();
            }
            //imageViewPepperPhoto.invalidate();
//                photoTaken=true;
            //javaCameraView.disableView();
//                canTakePhoto=false;
//
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

       /* if(mediaPlayer != null) {
            mediaPlayer.release();
        }*/

    }

    @Override
    public void onPause() {
        super.onPause();

        if (javaCameraView != null) {
            javaCameraView.disableView();
        }

       /* if(mediaPlayer != null) {
            if(mediaPlayer.isPlaying()) {

                mediaPlayer.release();
                mediaPlayer.stop();
            }
        }*/

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
    QiContext qiContext;

    public QiContext getQiContext() {
        return qiContext;
    }

    public void setQiContext(QiContext qiContext) {
        this.qiContext = qiContext;
    }

    public void buttonClassify(View v) {
        Log.e(TAG, "ButtonClassify cliccato");
        buttonTakePicture.setClickable(false);
        mediaPlayer.start();
        buttonTakePicture.setVisibility(View.INVISIBLE);
        pbSavePhoto.setVisibility(View.VISIBLE);
        if(!classified) {
            Log.e("CLASSIF", "Classificazione in corso...");
            classify();
            askForConfirm();
            Log.e("CLASSIF", "Classificazione avvenuta con successo.");
        } else {
            Log.e("CLASSIF", "ERRORE di classificazione.");
        }
    }

    /*public void classify() { //Dovrebbe funzionare ugualmente... Usa questo se l'altro dà problemi
        responseText.setText("Classificazione in corso...");
        Log.e("CLASSIF","Entrato in classify");
        responseText.setText(postUrl);
        MultipartBody.Builder multipartBodyBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888; //options.inPreferredConfig = Bitmap.Config.RGB_565;

        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        // Read BitMap by file path.
        Bitmap bitmap = mRGBATbitmap;
        try {
            // SCATTO FOTO
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream); //TODO Rimuovi (o no?)
            if(!photoTaken) {
                savePhoto(mRGBATbitmap);
            }
            //photoTaken=true;
            responseText.setText("L'immagine dovrebbe esser stata salvata in:" + photoPath);
//            Log.e("CLASSIF","L'immagine dovrebbe esser stata salvata in:" + photoPath);


            //CONNESSIONE SERVER
            try {
                Log.d(TAG, "Sto passando al clientManager photopath ==" + photoPath);
                ClientManager clientManager = new ClientManager(photoPath, postUrl, garbageType);
                Thread thread = new Thread(clientManager);
                thread.start();
                thread.join();
                garbageType = clientManager.getGarbageType();
                responseText.setText("Tipo rifiuto:" + garbageType);
                classified = true;
                setWasteType();
                checkIfPhotoExists();
            *//*else {
                    restartActivity();
                }*//*
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            responseText.setText("Errore. L'immagine non è stata catturata in modo corretto.");
            Log.e("CLASSIF","Errore. L'immagine non è stata catturata in modo corretto");
//            restartActivity();
            return;
        }
        try {
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        javaCameraView.disableView(); //setVisibility(View.INVISIBLE); // Rende la cam invisibile

    }*/
    public void classify() {
        responseText.setText("Classificazione in corso...");
        Log.e("CLASSIF","Entrato in classify");
        responseText.setText(postUrl);
        MultipartBody.Builder multipartBodyBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888; //options.inPreferredConfig = Bitmap.Config.RGB_565;

        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        // Read BitMap by file path.
        Bitmap bitmap = mRGBATbitmap;
        try {
            // SCATTO FOTO
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream); //TODO Rimuovi (o no?)
            if(!photoTaken) {
                savePhoto(mRGBATbitmap);
            }
            //photoTaken=true;
            responseText.setText("L'immagine dovrebbe esser stata salvata in:" + photoPath);
            Log.e("CLASSIF","L'immagine dovrebbe esser stata salvata in:" + photoPath);

            stream.close();
        } catch (Exception e) {
            responseText.setText("Errore. L'immagine non è stata catturata in modo corretto.");
            Log.e("CLASSIF","Errore. L'immagine non è stata catturata in modo corretto");
//            restartActivity();
            return;
        } finally {

            //CONNESSIONE SERVER
            try {
                ClientManager clientManager = new ClientManager(photoPath, postUrl, garbageType);
                Thread thread = new Thread(clientManager);
                thread.start();
                thread.join();
                garbageType = clientManager.getGarbageType();
                responseText.setText("Tipo rifiuto:" + garbageType);
                classified = true;
                setWasteType();
                checkIfPhotoExists();
            /*else {
                    restartActivity();
                }*/
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
//        javaCameraView.disableView(); //setVisibility(View.INVISIBLE); // Rende la cam invisibile

    }

    void restartActivity() {
        Intent activity2Intent = new Intent(getApplicationContext(), PlayPepperTurnActivity.class);
        activity2Intent.putExtra("wasteType", wasteType);
        activity2Intent.putExtra("round", round);
        activity2Intent.putExtra("tutorialEnabled", tutorialEnabled);
        activity2Intent.putExtra("isPepperTurn", isPepperTurn);
        activity2Intent.putExtra("roundTutorial", isPepperTurn);
        activity2Intent.putExtra("wasteTypeString", wasteTypeString);
        //activity2Intent.putExtra("scores", (Serializable) scores); //TODO Serializable(?)
        activity2Intent.putExtra("pepperScore", pepperScore);
        activity2Intent.putExtra("userScore", userScore);
        activity2Intent.putExtra("currentRound", currentRound);
        activity2Intent.putExtra("trialState", trialState);
        startActivity(activity2Intent); //Per ripetere
        finish();
    }
    void setWasteType() {
        switch (garbageType) {
            case "organic":
                wasteType = TYPE_ORGANIC;
                wasteTypeString = STRING_ORGANIC;
                break;
            case "plastic":
                wasteType = TYPE_PLASTIC;
                wasteTypeString = STRING_PLASTIC_METAL;
                break;
            case "metal":
                wasteType = TYPE_METAL;
                wasteTypeString = STRING_PLASTIC_METAL;
                break;
            case "paper":
                wasteType = TYPE_PAPER;
                wasteTypeString = STRING_PAPER_CARDBOARD;
                break;
            case "cardboard":
                wasteType = TYPE_CARDBOARD;
                wasteTypeString = STRING_PAPER_CARDBOARD;
                break;
            case "glass":
                wasteType = TYPE_GLASS;
                wasteTypeString = STRING_GLASS;
                break;
            default:
                /*wasteType = TYPE_ORGANIC;
                wasteTypeString = STRING_ORGANIC;*/
                wasteType = CLASSIFICATION_ERROR;
                wasteTypeString = STRING_CLASSIFICATION_ERROR;
                break;
        }
    }/* void setWasteType() {
        switch (garbageType) {
            case "organic":
                wasteType = TYPE_ORGANIC;
                wasteTypeString = STRING_ORGANIC;
                break;
            case "plastic":
            case "metal":
                wasteType = TYPE_PLASTIC_METAL;
                wasteTypeString = STRING_PLASTIC_METAL;
                break;
            case "cardboard":
            case "paper":
                wasteType = TYPE_PAPER_CARDBOARD;
                wasteTypeString = STRING_PAPER_CARDBOARD;
                break;
            case "glass":
                wasteType = TYPE_GLASS;
                wasteTypeString = STRING_GLASS;
                break;
            default:
                *//*wasteType = TYPE_ORGANIC;
                wasteTypeString = STRING_ORGANIC;*//*

                wasteType = CLASSIFICATION_ERROR;
                wasteTypeString = STRING_CLASSIFICATION_ERROR;
                break;
        }
    }*/

    void goToClassAct(){
        Intent activity2Intent = new Intent(PlayPepperTurnActivity.this, PepperClassifyingActivity.class);
        activity2Intent.putExtra("wasteType", wasteType);
        activity2Intent.putExtra("round", round);
        activity2Intent.putExtra("isPepperTurn", isPepperTurn);
        activity2Intent.putExtra("wasteTypeString", wasteTypeString);
        activity2Intent.putExtra("tutorialEnabled", tutorialEnabled);
        //activity2Intent.putExtra("scores", (Serializable) scores); //TODO Serializable(?)
        activity2Intent.putExtra("pepperScore", pepperScore);
        activity2Intent.putExtra("userScore", userScore);
        activity2Intent.putExtra("roundTutorial", isPepperTurn);
        activity2Intent.putExtra("currentRound", currentRound);
        activity2Intent.putExtra("trialState", trialState);

        startActivity(activity2Intent);
        finish();
    }
    void askForConfirm() {
//        Intent activity2Intent = new Intent(PlayPepperTurnActivity.this, JudgeConfirmActivity.class);
        Intent activity2Intent = new Intent(PlayPepperTurnActivity.this, PlayJudgeTurnActivity.class);
        activity2Intent.putExtra("wasteType", wasteType);
        activity2Intent.putExtra("round", round);
        activity2Intent.putExtra("tutorialEnabled", tutorialEnabled);
        activity2Intent.putExtra("isPepperTurn", isPepperTurn);
        activity2Intent.putExtra("wasteTypeString", wasteTypeString);
        //activity2Intent.putExtra("scores", (Serializable) scores); //TODO Serializable(?)
        activity2Intent.putExtra("pepperScore", pepperScore);
        activity2Intent.putExtra("userScore", userScore);
        activity2Intent.putExtra("currentRound", currentRound);
        activity2Intent.putExtra("trialState", trialState);
        activity2Intent.putExtra("roundTutorial", isPepperTurn);

//        Log.d(TAG, "trialstate passato da qui a judgeconfirm: " + trialState);
        Log.d(TAG, "trialstate passato da qui a judgeturn: " + trialState);
        startActivity(activity2Intent);
        mediaPlayer.stop();
        mediaPlayer.release();
        finish();
        /* TODO Turno dell'utente:
        Schermata con i bidoni. L'utente deve selezionare il bidone corretto.
        Successivamente, c'è la schermata di richiesta conferma per il giudice
        Se la risposta è affermativa, l'utente guadagna un punto.
        */
    }

    //Elimina la foto, se esistente
    private void checkIfPhotoExists() {
        Log.d(TAG, "checkifphotoexists");
        File myFile = new File(photoPath);

        if(myFile.exists()) {
            myFile.delete();

            Log.e("CLASSIF","File eliminato dal path " + photoPath);
        }
    }
    /*private void savePhoto(Bitmap bmp) { //https://stackoverflow.com/questions/15662258/how-to-save-a-bitmap-on-internal-storage
        File pictureFile = getOutputMediaFile();

        Log.d(TAG, "Entrata in savePhoto: takePictureSaid: " + true + "\tphotoTaken: " + photoTaken);
        if (pictureFile == null) {
            Log.d(TAG,
                    "Error creating media file, check storage permissions: ");// e.getMessage());
            return;
        }
        try {
            FileOutputStream fos = new FileOutputStream(pictureFile, false); //il "false" dovrebbe permettere di sovrascrivere l'immagine, se esiste
            bmp = Bitmap.createScaledBitmap(bmp, 224, 224, true);
            bmp.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.close();
            photoTaken = true;
        } catch (FileNotFoundException e) {
            Log.d(TAG, "Impossibile salvare l'immagine. " + e.getMessage());
        } catch (IOException e) {
            Log.d(TAG, "Impossibile accedere al file: " + e.getMessage());
        }
        photoPath = "storage/emulated/0/DCIM/" + photoName; //pictureFile.toString(); ///storage/emulated/0/DCIM/PhotoPepper0.jpg

        Log.e("CLASSIF","SavePhoto eseguita");
    }*/
    private void savePhoto(Bitmap bmp) { //https://stackoverflow.com/questions/15662258/how-to-save-a-bitmap-on-internal-storage
        File pictureFile = getOutputMediaFile();

        Log.d(TAG, "Entrata in savePhoto:"); //+ " takePictureSaid: " + true + "\tphotoTaken: " + photoTaken);
        if (pictureFile == null) {
            Log.d(TAG, "Error creating media file, check storage permissions: ");// e.getMessage());
            return;
        }
        try {
            FileOutputStream fos = new FileOutputStream(pictureFile, false); //il "false" dovrebbe permettere di sovrascrivere l'immagine, se esiste
            bmp = Bitmap.createScaledBitmap(bmp, 224, 224, true);
            bmp.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.close();
            photoTaken = true;
        } catch (FileNotFoundException e) {
            Log.d(TAG, "Impossibile salvare l'immagine. " + e.getMessage());
        } catch (IOException e) {
            Log.d(TAG, "Impossibile accedere al file: " + e.getMessage());
        } finally {
            photoPath = "storage/emulated/0/DCIM/" + photoName; //pictureFile.toString(); ///storage/emulated/0/DCIM/PhotoPepper0.jpg
            Log.e("CLASSIF","SavePhoto eseguita");
        }

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

    public void buttonClose(View v) { //Pressione tasto "Chiudi"
        CommonUtils.showDialogExit(this);
        //finish();
    }


   /* class SavePhoto extends AsyncTask<Object, Void, Bitmap>{
        private Bitmap bmp;

        public SavePhoto(Bitmap bmp) {
            this.bmp = bmp;
        }
        @Override
        protected Bitmap doInBackground(Object... params) {
            *//*File file = new File(
                    Environment.getExternalStorageDirectory().getAbsolutePath() + path);

            if(file.exists()){
                bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            }
*//*          if(!photoTaken)
                pbSavePhoto.setVisibility(View.VISIBLE);
                savePhoto(bmp);
            return bmp; //Inutile ma boh non so come altro fare
        }
        *//*@Override
        protected Bitmap doInBackground(Object... params) {
            Bitmap bitmap = null;
            *//**//*File file = new File(
                    Environment.getExternalStorageDirectory().getAbsolutePath() + path);

            if(file.exists()){
                bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            }
*//**//*
            savePhoto(bmp);
            return bitmap;
        }*//*
        @Override
        protected void onPostExecute(Bitmap result) {
            Log.d(TAG, "after phraseSetYes: takePictureSaid: " + true + "\tphotoTaken: " + photoTaken);
            try {
                Log.d(TAG, "onrobotfocusgained clientManager lanciato.");
                ClientManager clientManager = new ClientManager(photoPath, postUrl, garbageType);
                Thread thread = new Thread(clientManager);
                thread.start();
                thread.join();
                garbageType = clientManager.getGarbageType();
                responseText.setText("Tipo rifiuto:" + garbageType);
                Log.d(TAG, "onrobotfocusgained. Ottenuto garbagetype: " + garbageType);
                classified = true;
                Log.d(TAG, "onrobotfocusgained. setwastetype");
                setWasteType();
                Log.d(TAG, "onrobotfocusgained. checkifphotoexists");
                checkIfPhotoExists();
                Log.d(TAG, "onrobotfocusgained. askforconfirm");
                askForConfirm();
                mediaPlayer.stop();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            pbSavePhoto.setVisibility(View.INVISIBLE);
            *//* if (!imv.getTag().toString().equals(path)) {
     *//**//* The path is not same. This means that this
                  image view is handled by some other async task.
                  We don't do anything and return. *//**//*
                return;
            }

            if(result != null && imv != null){
                imv.setVisibility(View.VISIBLE);
                imv.setImageBitmap(result);
            }else{
                imv.setVisibility(View.GONE);
            }*//*
        }

    }*/

}