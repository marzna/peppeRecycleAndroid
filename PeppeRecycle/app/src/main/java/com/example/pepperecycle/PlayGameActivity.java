package com.example.pepperecycle;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

//Activity contenente il vero e proprio gioco
public class PlayGameActivity extends RobotActivity implements RobotLifecycleCallbacks, View.OnTouchListener {
    private static final String TAG = "PlayGameActivity";//, CameraBridgeViewBase.CvCameraViewListener2{

    boolean isPepperTurn;
    // int N_PLAYERS = 2; int turn = new Random().nextInt(N_PLAYERS) ; //Ritorna un random int nel range [0, N_PLAYERS-1]
    static final int N_TURNS = 6;
    byte currentTurn;
    //    Map<String, Byte> scores = new HashMap<String, Byte>();
    Map<String, Byte> scores = new HashMap<>();
    static byte pepperScore;
    static byte userScore;
    boolean tutorialEnabled;
    byte tutorialState;
    byte trialState;
    boolean restartGame; //Indica che si sta giocando una nuova partita dopo aver terminato l'altra
    byte round;
    boolean roundTutorial, endOfTutorial;
    boolean playGameAfterTrial;
    // Store the Animate action.
    private Animate animate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        QiSDK.register(this, this);
        //Per far sparire la barra grigia sopra
        setSpeechBarDisplayStrategy(SpeechBarDisplayStrategy.IMMERSIVE);
        setSpeechBarDisplayPosition(SpeechBarDisplayPosition.TOP);

        setContentView(R.layout.activity_play_game);

        /*scores = new HashMap<String, String>() {{
            put(String.valueOf(0), "b");
            put(1, "d");
        }};*/
        isPepperTurn = new Random().nextBoolean() ; //Ritorna un random boolean (Serve per stabilire randomicamente chi inizia a giocare)
        currentTurn = 0;
        restartGame = false;
        pepperScore = 0;
        userScore = 0;
        tutorialState = -1;
        roundTutorial = false;
        endOfTutorial = false;
        round = 0;
        trialState = -1;

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            tutorialEnabled = extras.getBoolean("tutorialEnabled");
            currentTurn = extras.getByte("currentTurn");
            trialState = extras.getByte("trialState");
            restartGame = extras.getBoolean("restartGame");
            round = extras.getByte("round");
            roundTutorial = extras.getBoolean("roundTutorial");
            endOfTutorial = extras.getBoolean("endOfTutorial");
            Log.d(TAG, "Ricevuto trialState: "+ trialState);
        }

        if (restartGame)
            playGameAfterTrial = true; //???
        else
            playGameAfterTrial = false;

        if(tutorialEnabled) {
            isPepperTurn = false;
        }
        /*TODO metti qui l'assegnazione del primo turno*/
        startGame();
    }

    @Override
    public void onRobotFocusGained(QiContext qiContext) {
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        return false;
    }

    @Override
    public void onRobotFocusLost() {

    }

    @Override
    public void onRobotFocusRefused(String reason) {

    }

    void startGame() {
         /*TODO Decommenta alla fine
        isPepperTurn = new Random().nextBoolean() ; //Ritorna un random boolean (Serve per stabilire randomicamente chi inizia a giocare)
        *//*
        scores.put("score_pepper", (byte) 0);
        scores.put("score_user1", (byte) 0);*/
        scores.put("score_pepper", pepperScore);
        scores.put("score_user1", userScore);

        //isPepperTurn=false; //tocca all'utente //TODO RIMUOVI DOPO AVER TESTATO IL TURNO UTENTE
        //byte round = 0; //TODO non serve
        currentTurn = 0;
//        while ( (scores.get("score_pepper") < 3 ) || (scores.get("score_user1") < 3) ) { TODO NON SERVE PERCHÉ IL CHECK VA FATTO IN JudgeConfirmActivity
        //TODO Unboxing of 'scores.get("score_pepper")' may produce 'NullPointerException'
        //TODO Unboxing of 'scores.get("score_user1")' may produce 'NullPointerException'
        //for (int round = 0; round<N_TURNS; round++ ) {
        Intent activity2Intent;
        /*if(tutorialEnabled) {
//            isPepperTurn=false;
            activity2Intent = new Intent(getApplicationContext(), PlayUserTurnActivity.class);//TODO turno utente
        } else {*/
        if (isPepperTurn) {         // Se tocca a Pepper
            activity2Intent = new Intent(PlayGameActivity.this, PlayPepperTurnActivity.class);//TODO turno di Pepper
            // activity2Intent.putExtra("score", score);
            // Intent activity2Intent = new Intent(getApplicationContext(), TodoActivity.class);
        } else {                    // Se tocca all'utente
            activity2Intent = new Intent(getApplicationContext(), PlayUserTurnActivity.class);//TODO turno utente
        }
        /*}*/
//        activity2Intent.putExtra("round", round);
        //activity2Intent.putExtra("scores", (Serializable) scores); //TODO Serializable(?)
        activity2Intent.putExtra("pepperScore", pepperScore);
        activity2Intent.putExtra("userScore", userScore);
        activity2Intent.putExtra("tutorialEnabled", tutorialEnabled);
        activity2Intent.putExtra("currentTurn", currentTurn);
        activity2Intent.putExtra("trialState", trialState);
        activity2Intent.putExtra("restartGame", restartGame);

        startActivity(activity2Intent);
        //isPepperTurn = !isPepperTurn;
        finish();
        //}

        /* TODO Gioco vero e proprio
        Variabile turno che si incrementa.
        Pensando al caso Pepper vs utente, può avere solo valore 0 (Pepper) o 1 (utente1).
        Al primo round, il turno si stabilisce randomicamente
        Il gioco si ripete per 3 round
        /* TODO Turno di Pepper:
        Gli viene mostrata l'immagine.
        Si dice "Ecco" o si preme sul bottone, lui scatta, invia al server.
        // TODO Cambia nel server la risposta data, mettendo solamente il valore in inglese!!
        Pepper attende la risposta del server e poi lo comunica all'utente.
        Successivamente, c'è la schermata di richiesta conferma per il giudice
        Se la risposta è affermativa, Pepper guadagna un punto e dirà una curiosità random
        relativa all’oggetto riconosciuto (ES: "lo sapevi che con tot bottiglie di plaastica...?"
        */

        /* TODO Turno dell'utente:
        Schermata con i bidoni. L'utente deve selezionare il bidone corretto.
        Successivamente, c'è la schermata di richiesta conferma per il giudice
        Se la risposta è affermativa, l'utente guadagna un punto.
        */

        /*Schermata Game Over
        TODO Se l'utente ha vinto
        si  fa

        TODO Se l'utente ha perso
         */
    }

}