package com.example.pepperecycle;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.design.activity.RobotActivity;
import com.aldebaran.qi.sdk.design.activity.conversationstatus.SpeechBarDisplayPosition;
import com.aldebaran.qi.sdk.design.activity.conversationstatus.SpeechBarDisplayStrategy;

import java.util.Random;

//Activity contenente il gioco vero e proprio
public class PlayGameActivity extends RobotActivity implements RobotLifecycleCallbacks, View.OnTouchListener {

    private static final String TAG = "PlayGameActivity";

    boolean isPepperTurn;
    static final int N_TURNS = 6;
    byte currentTurn;
    static byte pepperScore;
    static byte userScore;
    boolean tutorialEnabled;
    byte tutorialState;
    byte trialState;
    boolean restartGame; //Indica che si sta giocando una nuova partita dopo aver terminato l'altra
    byte round;
    boolean roundTutorial, endOfTutorial;
    boolean playGameAfterTrial;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        QiSDK.register(this, this);
        //Per far sparire la barra grigia sopra
        setSpeechBarDisplayStrategy(SpeechBarDisplayStrategy.IMMERSIVE);
        setSpeechBarDisplayPosition(SpeechBarDisplayPosition.TOP);

        setContentView(R.layout.activity_play_game);

        isPepperTurn = new Random().nextBoolean() ; //Ritorna un random boolean (Serve per stabilire randomicamente il turno iniziale)
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
        }

        if (restartGame)
            playGameAfterTrial = true;
        else
            playGameAfterTrial = false;

        if(tutorialEnabled) {
            isPepperTurn = false;
        }
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
        currentTurn = 0;
        Intent activity2Intent;
        if (isPepperTurn) {         // Se tocca a Pepper
            activity2Intent = new Intent(PlayGameActivity.this, PlayPepperTurnActivity.class);
        } else {                    // Se tocca all'utente
            activity2Intent = new Intent(PlayGameActivity.this, PlayUserTurnActivity.class);
        }
        activity2Intent.putExtra("pepperScore", pepperScore);
        activity2Intent.putExtra("userScore", userScore);
        activity2Intent.putExtra("tutorialEnabled", tutorialEnabled);
        activity2Intent.putExtra("currentTurn", currentTurn);
        activity2Intent.putExtra("trialState", trialState);
        activity2Intent.putExtra("restartGame", restartGame);

        startActivity(activity2Intent);
        finish();

    }

}