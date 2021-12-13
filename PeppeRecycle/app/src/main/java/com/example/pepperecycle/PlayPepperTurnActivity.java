package com.example.pepperecycle;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
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
import com.aldebaran.qi.sdk.object.conversation.BodyLanguageOption;
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

// Activity relativa alla gestione del turno di Pepper
// TODO Importante: cambiare ngrokUrl con quello aggiornato

public class PlayPepperTurnActivity extends RobotActivity implements RobotLifecycleCallbacks, View.OnTouchListener, CameraBridgeViewBase.CvCameraViewListener2 {
    private static String TAG = "PlayPepperTurnActivity";

    // Indirizzo del server
    private String ngrokUrl = "http://8c98-193-204-189-14.ngrok.io";
    private String postUrl = ngrokUrl + "/handle_request"; //http://127.0.0.1:5000/handle_request";

    //Parte relativa alla fotocamera
    private JavaCameraView javaCameraView;
    private Mat mRGBA, mRGBAT;
    private Bitmap mRGBATbitmap;
    private int activeCamera = CameraBridgeViewBase.CAMERA_ID_FRONT; //Attiva la fotocamera frontale
    private static final int CAMERA_REQUEST = 1888;
    private static final int MY_CAMERA_REQUEST_CODE = 100; //Per la richiesta dei permessi della fotocamera

    QiContext qiContext;
    private ImageView imageViewPepperPhoto;
    private boolean touched = false;
    private String garbageType;
    byte wasteType = -1;
    static byte pepperScore, userScore;
    private boolean tutorialEnabled;
    byte trialState;

    boolean roundTutorial;
    boolean restartGame;
    ProgressBar pbSavePhoto;

    private boolean isThreadStarted = false;
    private String photoName = "PhotoPeppeRecycle.jpg"; // nome con cui la foto sarà salvata temporaneamente in memoria
    private String photoPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString();

    private TextView responseText;

    static final byte TYPE_ORGANIC = 0;
    static final byte TYPE_PAPER_CARDBOARD = 1;
    static final byte TYPE_PLASTIC_METAL = 2;
    static final byte TYPE_GLASS = 3;
    static final byte TYPE_PAPER = 4;
    static final byte TYPE_CARDBOARD = 5;
    static final byte TYPE_PLASTIC = 6;
    static final byte TYPE_METAL = 7;
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

    Mat dst;

    byte round;
    byte currentTurn;
    boolean classified=false;
    private Animate animate;    // Store the Animate action.
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

        buttonTakePicture = findViewById(R.id.buttonTakePicture);
        javaCameraView = findViewById(R.id.my_camera_view);
        responseText = findViewById(R.id.responseText); //Conterrà i messaggi che verranno stampati sotto al pulsante di connessione al server
        imageViewPepperPhoto = findViewById(R.id.imageViewPepperPhoto);
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
            Log.d(TAG, "Permission prompt");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, MY_CAMERA_REQUEST_CODE);
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 2);
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.MANAGE_EXTERNAL_STORAGE}, 3);
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 4); //??
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_NETWORK_STATE}, 5);//??
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, 6);
        }


        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            round = extras.getByte("round");
            pepperScore = extras.getByte("pepperScore");
            userScore = extras.getByte("userScore");
            tutorialEnabled = extras.getBoolean("tutorialEnabled");
            roundTutorial = extras.getBoolean("roundTutorial");
            currentTurn = extras.getByte("currentTurn");
            trialState = extras.getByte("trialState");
            restartGame = extras.getBoolean("restartGame");
            endOfTutorial = extras.getBoolean("endOfTutorial");
            tutorialState = extras.getByte("tutorialState");
        }

        if(trialState == 1) {
            tvTutorialPepper.setVisibility(View.VISIBLE);
            textViewUserScore.setVisibility(View.INVISIBLE);
            textViewPepperScore.setVisibility(View.INVISIBLE);
            imageViewUserScore.setVisibility(View.INVISIBLE);
            imageViewPepperScore.setVisibility(View.INVISIBLE);
        } else {
            tvTutorialPepper.setVisibility(View.INVISIBLE);
            textViewUserScore.setVisibility(View.VISIBLE);
            textViewPepperScore.setVisibility(View.VISIBLE);
            imageViewUserScore.setVisibility(View.VISIBLE);
            imageViewPepperScore.setVisibility(View.VISIBLE);
        }

        showScore();

    }

    void showScore () {
        textViewPepperScore.setText("" + pepperScore);
        textViewUserScore.setText("" + userScore);
    }
    public void buttonHome(View v) { //Pressione tasto "torna alla Home"
        Intent activity2Intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(activity2Intent);
        finish();
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
                .withText("Per piacere, posso vedere il rifiuto da riciclare? Ricòrdati di dire ecco per mostrarmi il rifiuto.") // Set the text to say.
                .withBodyLanguageOption(BodyLanguageOption.DISABLED) //Movimento disabilitato per velocizzare lo scatto e farlo più stabile (?)
                .build(); // Build the say action.

        Animation pepperTurn = AnimationBuilder.with(qiContext)
                .withResources(R.raw.show_self_a001)
                .build();
        Animate animatePepperTurn = AnimateBuilder.with(qiContext)
                .withAnimation(pepperTurn).build();

        PhraseSet phraseSetYes = PhraseSetBuilder.with(qiContext)
                .withTexts("Sì Pepper", "Si Pepper", "Ecco", "Ecco qui", "Ecco Pepper",
                        "Ecco qui Pepper", "Tieni", "Tieni Pepper", "Pepper tieni",
                        "Sì", "Si", "okay", "ok", "va bene", "pepper ecco", "pepper si",
                        "guarda", "guarda qui", "pepper guarda", "guarda pepper")
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

        if(currentTurn==0 && trialState==-1)
            sayPepperTurn.run();
        if (trialState == 1) {
            sayPepperTurnTutorial.run();
        }
        showGarbage.run();
        Log.d(TAG, "TrialState: " + trialState);
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
            try {
                ClientManager clientManager = new ClientManager(photoPath, postUrl, garbageType);
                Thread thread = new Thread(clientManager);
                thread.start();
                thread.join();
                garbageType = clientManager.getGarbageType();
                responseText.setText("Tipo rifiuto:" + garbageType);
                //Log.d(TAG, "onrobotfocusgained. Ottenuto garbagetype: " + garbageType);
                classified = true;
                setWasteType();
                checkIfPhotoExists();
                askForConfirm();
                mediaPlayer.stop();
                mediaPlayer.release();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else if (PhraseSetUtil.equals(matchedPhraseSet, phraseSetNo)) {      // Utente non vuole mostrare l'oggetto a Pepper

            Log.d(TAG, "OnRobotFocusGained: matched no");
            Say sayRepeat = SayBuilder.with(qiContext) // Create the builder with the context.
                    .withText("Mi dispiace, ma senza questo procedimento, io non posso giocare. ") // Set the text to say.
                    .build(); // Build the say action.
            sayRepeat.run();
            restartActivity();

        } else if (PhraseSetUtil.equals(matchedPhraseSet, phraseSetRepeat)) {   // Richiesta utente di ripetere il gioco dall'inizio
            Animation correctAnswer = AnimationBuilder.with(qiContext)
                    .withResources(R.raw.coughing_left_b001).build();
            Animate animateCorrect = AnimateBuilder.with(qiContext)
                    .withAnimation(correctAnswer).build();
            animateCorrect.run();
            Intent activity2Intent = new Intent(getApplicationContext(), PlayIntroActivity.class);
            startActivity(activity2Intent); //Per ripetere
            finish();

        } else if (PhraseSetUtil.equals(matchedPhraseSet, phraseSetHome)) {     // Torna alla home
            Animation correctAnswer = AnimationBuilder.with(qiContext)
                    .withResources(R.raw.affirmation_a002).build();
            Animate animateCorrect = AnimateBuilder.with(qiContext)
                    .withAnimation(correctAnswer).build();
            animateCorrect.run();
            Intent activity2Intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(activity2Intent); //Per iniziare il gioco.
            finish();

        } else if (PhraseSetUtil.equals(matchedPhraseSet, phraseSetClose)) {    // Chiude il gioco
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
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
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
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show(); // camera can be turned on
                initializeCamera(javaCameraView, activeCamera);
            } else {
                // camera will stay off
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void initializeCamera(JavaCameraView javaCameraView, int activeCamera) {
        javaCameraView.setCameraPermissionGranted();
        javaCameraView.setCameraIndex(activeCamera);
        javaCameraView.setVisibility(CameraBridgeViewBase.VISIBLE);
        javaCameraView.setCvCameraViewListener(PlayPepperTurnActivity.this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_REQUEST
                && resultCode == Activity.RESULT_OK
                && data != null) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            imageViewPepperPhoto.setImageBitmap(photo);
        }
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mRGBA = new Mat(height, width, CvType.CV_8UC4);
        mRGBAT = new Mat();
        dst = new Mat();
    }

    @Override
    public void onCameraViewStopped() {
        if (mRGBA != null)
            mRGBA.release();
        if (mRGBAT != null)
            mRGBAT.release();
    }


    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) { //https://stackoverflow.com/questions/64488713/opencv-application-android-camera-crashes-after-10-seconds/64493151#64493151
        mRGBA = inputFrame.rgba();

        mRGBAT = mRGBA.t();
        Core.transpose(mRGBA, mRGBAT);
        Core.flip(mRGBA, mRGBAT, 1);
        Imgproc.resize(mRGBAT, dst, mRGBA.size());
//        Log.e("TAG", "Immagine premuta, foto scattata.");

        try {
            mRGBATbitmap = Bitmap.createBitmap(mRGBAT.cols(), mRGBAT.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(mRGBAT, mRGBATbitmap);
            imageViewPepperPhoto.setImageBitmap(mRGBATbitmap);

            if(takePictureSaid && !photoTaken) { //Se l'utente ha espresso il comando vocale e la foto non è stata scattata
                savePhoto(mRGBATbitmap);
                photoTaken = true;
                //Log.d(TAG, "Foto scattata nell'oncameraframe");
                //javaCameraView.disableView();
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        mRGBA.release();
        mRGBAT.release();
        return dst;
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

    }

    @Override
    public void onResume() {
        super.onResume();

        if (OpenCVLoader.initDebug()) {
            //Log.d(TAG, "OpenCV is Configured or Connected Successfully.");
            baseLoaderCallback.onManagerConnected(BaseLoaderCallback.SUCCESS);
        } else {
            //Log.d(TAG, "OpenCV not Working or Loaded");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, baseLoaderCallback);
        }
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    public QiContext getQiContext() {
        return qiContext;
    }

    public void setQiContext(QiContext qiContext) {
        this.qiContext = qiContext;
    }

    public void buttonClassify(View v) {
        //Log.d(TAG, "ButtonClassify cliccato");
        buttonTakePicture.setClickable(false);
        mediaPlayer.start();
        buttonTakePicture.setVisibility(View.INVISIBLE);
        pbSavePhoto.setVisibility(View.VISIBLE);
        if(!classified) {
            classify();
            askForConfirm();
            Log.d(TAG, "Classificazione avvenuta con successo.");
        } else {
            Log.e(TAG, "ERRORE di classificazione.");
        }
    }

    public void classify() {
        responseText.setText("Classificazione in corso...");
        //Log.d(TAG,"Classificazione in corso...");
        responseText.setText(postUrl);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888; //options.inPreferredConfig = Bitmap.Config.RGB_565;

        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        Bitmap bitmap = mRGBATbitmap;
        try {
            // SCATTO FOTO
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            if(!photoTaken) {
                savePhoto(mRGBATbitmap);
            }
            responseText.setText("L'immagine dovrebbe esser stata salvata in:" + photoPath);
            //Log.d(TAG,"L'immagine dovrebbe esser stata salvata in:" + photoPath);
            stream.close();
        } catch (Exception e) {
            responseText.setText("Errore. L'immagine non è stata catturata in modo corretto.");
            //Log.d(TAG,"Errore. L'immagine non è stata catturata in modo corretto");
            //restartActivity();
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
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    void restartActivity() { //Ripete la stessa activity
        Intent activity2Intent = new Intent(getApplicationContext(), PlayPepperTurnActivity.class);
        activity2Intent.putExtra("wasteType", wasteType);
        activity2Intent.putExtra("round", round);
        activity2Intent.putExtra("tutorialEnabled", tutorialEnabled);
        activity2Intent.putExtra("isPepperTurn", isPepperTurn);
        activity2Intent.putExtra("roundTutorial", isPepperTurn);
        activity2Intent.putExtra("wasteTypeString", wasteTypeString);
        activity2Intent.putExtra("pepperScore", pepperScore);
        activity2Intent.putExtra("userScore", userScore);
        activity2Intent.putExtra("currentTurn", currentTurn);
        activity2Intent.putExtra("trialState", trialState);
        startActivity(activity2Intent);
        finish();
    }
    void setWasteType() {
        if(garbageType != null) {
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
                    /* wasteType = TYPE_ORGANIC;
                    wasteTypeString = STRING_ORGANIC;*/
                    wasteType = CLASSIFICATION_ERROR;
                    wasteTypeString = STRING_CLASSIFICATION_ERROR;
                    break;
            }
        } else {
            wasteType = TYPE_ORGANIC;
            wasteTypeString = STRING_ORGANIC;
        }
    }

    void askForConfirm() {
        Intent activity2Intent = new Intent(PlayPepperTurnActivity.this, PlayJudgeTurnActivity.class);
        activity2Intent.putExtra("wasteType", wasteType);
        activity2Intent.putExtra("round", round);
        activity2Intent.putExtra("tutorialEnabled", tutorialEnabled);
        activity2Intent.putExtra("isPepperTurn", isPepperTurn);
        activity2Intent.putExtra("wasteTypeString", wasteTypeString);
        activity2Intent.putExtra("pepperScore", pepperScore);
        activity2Intent.putExtra("userScore", userScore);
        activity2Intent.putExtra("currentTurn", currentTurn);
        activity2Intent.putExtra("trialState", trialState);
        activity2Intent.putExtra("roundTutorial", isPepperTurn);
        startActivity(activity2Intent);
        mediaPlayer.stop();
        mediaPlayer.release();
        finish();
    }

    //Elimina la foto, se esistente
    private void checkIfPhotoExists() {
        File myFile = new File(photoPath);

        if(myFile.exists()) {
            myFile.delete();
            //Log.d(TAG,"File eliminato dal path " + photoPath);
        }
    }

    private void savePhoto(Bitmap bmp) {
        File pictureFile = getOutputMediaFile();

        if (pictureFile == null) {
            //Log.e(TAG, "Errore nella creazione del file, ricontrollare i permessi relativi alla memoria.");
            return;
        }
        try {
            FileOutputStream fos = new FileOutputStream(pictureFile, false); //il "false" dovrebbe permettere di sovrascrivere l'immagine, se esiste
            bmp = Bitmap.createScaledBitmap(bmp, 224, 224, true);
            bmp.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.close();
            photoTaken = true;
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Impossibile salvare l'immagine. " + e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, "Impossibile accedere al file: " + e.getMessage());
        } finally {
            photoPath = "storage/emulated/0/DCIM/" + photoName; // /storage/emulated/0/DCIM/PhotoPepper0.jpg
            //Log.d(TAG,"SavePhoto eseguita");
        }

    }

    //Create a File for saving an image or video
    private  File getOutputMediaFile(){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString());

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
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        return false;
    }
}