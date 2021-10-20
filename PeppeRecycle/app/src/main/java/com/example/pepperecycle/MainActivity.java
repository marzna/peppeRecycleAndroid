package com.example.pepperecycle;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.aldebaran.qi.Future;
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

import org.opencv.android.JavaCameraView;

public class MainActivity extends RobotActivity implements RobotLifecycleCallbacks, View.OnTouchListener {//, CameraBridgeViewBase.CvCameraViewListener2{

    // Store the Animate action.
    private Animate animate;
    private ImageView imageViewMainBackground;
    //    private QiContext qiContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        QiSDK.register(this, this);

        //Per far sparire la barra grigia sopra
        setSpeechBarDisplayStrategy(SpeechBarDisplayStrategy.IMMERSIVE);
        //setSpeechBarDisplayPosition(SpeechBarDisplayPosition.TOP);
        setContentView(R.layout.activity_main);

        if (!checkPermissions()) {
            // Richiesta permessi
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 2);
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.MANAGE_EXTERNAL_STORAGE}, 3);
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 4); //??
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_NETWORK_STATE}, 5);//??
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, 6);
        }

    }

    private boolean checkPermissions(){
        //Controllo permessi
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


    @Override
    public void onRobotFocusGained(QiContext qiContext) {
        /*// The robot focus is gained.
        this.qiContext = qiContext;*/
        Animation hello = AnimationBuilder.with(qiContext)
                .withResources(R.raw.hello_a007)
                .build();
        //Animate animateHello = AnimateBuilder.with(qiContext)
        animate = AnimateBuilder.with(qiContext)
                .withAnimation(hello)
                .build();

        Say sayHello = SayBuilder.with(qiContext) // Create the builder with the context.
                .withText("Ciao!\\rspd=95\\") // Set the text to say.
                .build(); // Build the say action.
        Say sayPresentation = SayBuilder.with(qiContext) // Create the builder with the context.
                .withText("\\rspd=95\\io sono Pepper e questo è \\rspd=95\\Pepperisàichel!.") // Set the text to say.
                .build(); // Build the say action.


        //TODO -> Bisognerà cambiare l'interazione nel caso dello storytelling
        Say sayPlay = SayBuilder.with(qiContext) // Create the builder with the context.
                .withText("\\rspd=95\\Giochiamo insieme?") // Set the text to say.
                .build(); // Build the say action.

        /*Say sayOther = SayBuilder.with(qiContext) // Create the builder with the context.
                .withText("Va bene, non giochiamo. Vuoi che ti racconti una storia?") // Set the text to say.
                .build(); // Build the say action.*/

        PhraseSet phraseSetYes = PhraseSetBuilder.with(qiContext)
                .withTexts("Si","Voglio giocare", "Play", "Si Pepper", "Giochiamo", "Gioca", "Iniziamo", "Inizia")
                .build();

        PhraseSet phraseSetNo = PhraseSetBuilder.with(qiContext)
                .withTexts("No", "Non voglio", "No Pepper", "Non mi va").build();

        PhraseSet phraseSetRepeat = PhraseSetBuilder.with(qiContext)
                .withTexts("Ripeti", "Ricominciamo", "Ricomincia", "Da capo", "Non ho capito", "Puoi ripetere")
                .build();

        PhraseSet phraseSetStory = PhraseSetBuilder.with(qiContext)
                .withTexts("", "Ricominciamo", "Ricomincia", "Da capo", "Non ho capito", "Puoi ripetere")
                .build();

        PhraseSet phraseSetClose = PhraseSetBuilder.with(qiContext)
                .withTexts("Chiudi il gioco", "Esci", "Basta")
                .build();

        Listen listenPlay = ListenBuilder.with(qiContext) // Create the builder with the QiContext.
                .withPhraseSets(phraseSetYes, phraseSetRepeat, phraseSetClose) // Set the PhraseSets to listen to.
                .build(); // Build the listen action

/*
        //Run Animazione Presentazione
        Future<Void> animateFuture = animate.async().run();
*/
        sayHello.run(); // animateHello.run(); -> Sbagliato (?)
        animate.run();
        sayPresentation.run();
        sayPlay.run();

        // Run the listen action and get the result.
        ListenResult listenResult = listenPlay.run();
        PhraseSet matchedPhraseSet = listenResult.getMatchedPhraseSet();

        if (PhraseSetUtil.equals(matchedPhraseSet, phraseSetYes)) {             // Risposta utente affermativa
            Intent activity2Intent = new Intent(getApplicationContext(), PlayIntroActivity.class);
            startActivity(activity2Intent); // fa partire il gioco
            finish();/*} else if (PhraseSetUtil.equals(matchedPhraseSet, phraseSetNo)) {     // Risposta utente negativa
                            sayOther.run();*/
        } else if (PhraseSetUtil.equals(matchedPhraseSet, phraseSetRepeat)) { // Richiesta utente di ripetere
            Animation correctAnswer = AnimationBuilder.with(qiContext)
                    .withResources(R.raw.coughing_left_b001).build();
            animate = AnimateBuilder.with(qiContext)
                    .withAnimation(correctAnswer).build();
            animate.run();
            Intent activity2Intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(activity2Intent); //Per ripetere la prima pagina
            finish();
        }  else if (PhraseSetUtil.equals(matchedPhraseSet, phraseSetClose)) { // Richiesta utente di ripetere
            Animation correctAnswer = AnimationBuilder.with(qiContext)
                    .withResources(R.raw.hello_a004).build();
            animate = AnimateBuilder.with(qiContext)
                    .withAnimation(correctAnswer).build();

            Say sayGoodbye = SayBuilder.with(qiContext) // Create the builder with the context.
                    .withText("Va bene, sto chiudendo il gioco. Sarà per un'altra volta, ciaoo!") // Set the text to say.
                    .build(); // Build the say action.

            animate.run();
            sayGoodbye.run();

            finish();
        }
    }

    @Override
    public void onRobotFocusLost() {
    }

    @Override
    public void onRobotFocusRefused(String reason) {
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        return false;
    }

    public void buttonPlay(View v) { //Pressione tasto  "giochiamo" TODO Thread?
        Intent activity2Intent = new Intent(getApplicationContext(), PlayIntroActivity.class);
        startActivity(activity2Intent); //Per andare alla seconda pagina
        finish();
    }

    public void buttonStorytelling(View v) { //Pressione tasto "storytelling" TODO Thread?
        Intent activity2Intent = new Intent(getApplicationContext(), TodoActivity.class);
        startActivity(activity2Intent); //Per andare alla pagina relativa a storytelling
        finish();
    }

    public void buttonScores(View v) { //Pressione tasto "scores" TODO Thread?
        Intent activity2Intent = new Intent(getApplicationContext(), TodoActivity.class);
        startActivity(activity2Intent); //Per andare alla pagina degli scores
        finish();
    }

    public void buttonClose(View v) { //Pressione tasto "Chiudi"
        finish();
    }
}